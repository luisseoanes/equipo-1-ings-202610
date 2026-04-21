# JNI Contract: C++ ↔ Kotlin Integration

> **Version:** 1.0
> **Status:** Authoritative — do not diverge without updating this document
> **Scope:** Defines the exact data flow between the native C++ inference engine
> and the Kotlin layer (`NativeEngine`) that wraps it.

---

## Overview

```
QueryOrchestrator (Kotlin)
        │
        │ LlmEngine.infer(query)
        ▼
  NativeEngine (Kotlin)          ← this contract lives here
        │
        │ JNI call
        ▼
  native_engine.so (C++)
```

`NativeEngine` implements `LlmEngine` (defined in `com.obsIA.domain`).
The native library is loaded once via `System.loadLibrary("native_engine")`.
All four JNI functions are declared `private external` on `NativeEngine`.

---

## 1. `processQuery(query: String): String`

The primary inference function. Called by `NativeEngine.infer()`.

### Request

| Field   | Type     | Notes                                      |
|---------|----------|--------------------------------------------|
| `query` | `String` | Raw UTF-8 user query; never null or empty. |

The C++ side receives a `jstring` and must handle the full Unicode range.

### Response — shape

All responses are a **JSON object serialized as a UTF-8 string**.
The top-level `"status"` field is always present and drives parsing.

#### status: `"ok"` — successful inference

```json
{
  "status": "ok",
  "triage_rule_id": null,
  "alert_message": null,
  "response_text": "Texto clínico generado. ADVERTENCIA: No sustituye criterio profesional.",
  "processing_ms": 12400
}
```

| Field            | Type            | Notes                              |
|------------------|-----------------|------------------------------------|
| `status`         | `String`        | Always `"ok"`                      |
| `triage_rule_id` | `null`          | Not applicable for LLM path        |
| `alert_message`  | `null`          | Not applicable for LLM path        |
| `response_text`  | `String`        | Clinical text; never null on `"ok"`|
| `processing_ms`  | `Long`          | Wall-clock inference time in ms    |

#### status: `"emergency"` — triage rule triggered

```json
{
  "status": "emergency",
  "triage_rule_id": "OB-001",
  "alert_message": "EMERGENCIA: texto del mensaje",
  "response_text": null,
  "processing_ms": 2
}
```

| Field            | Type     | Notes                                              |
|------------------|----------|----------------------------------------------------|
| `status`         | `String` | Always `"emergency"`                               |
| `triage_rule_id` | `String` | ID of the matched triage rule (e.g. `"OB-001"`)   |
| `alert_message`  | `String` | Human-readable emergency message; never null here  |
| `response_text`  | `null`   | Not used on emergency path                         |
| `processing_ms`  | `Long`   | Time to detect the emergency (typically < 10 ms)   |

#### status: `"error"` — engine-level failure

```json
{
  "status": "error",
  "error_code": "MODEL_NOT_LOADED",
  "error_message": "El motor de inferencia no está inicializado",
  "processing_ms": 0
}
```

| Field           | Type     | Notes                                           |
|-----------------|----------|-------------------------------------------------|
| `status`        | `String` | Always `"error"`                                |
| `error_code`    | `String` | Machine-readable code (see table below)         |
| `error_message` | `String` | Human-readable description in Spanish           |
| `processing_ms` | `Long`   | `0` if failure occurred before any processing   |

### Error codes

| Code                    | Trigger condition                                                      |
|-------------------------|------------------------------------------------------------------------|
| `MODEL_NOT_LOADED`      | `isReady()` would return `false`; weights not yet loaded               |
| `INFERENCE_TIMEOUT`     | Inference exceeded the configured time limit                           |
| `OUT_OF_MEMORY`         | Native heap exhausted during inference                                 |
| `TRIAGE_RULES_MISSING`  | Triage rule file not found or failed to parse at startup               |
| `NULL_INPUT`            | The `query` string was null or empty after trimming                    |
| `PARSE_ERROR`           | Internal tokenisation or JSON serialization failure                    |

---

## 2. `isReady(): Boolean`

Returns `true` only when the engine is **fully initialized** (model weights
loaded, triage rules parsed, tokenizer ready).
Returns `false` during startup or after a failed `release()`.

**JNI signature:** `jboolean Java_com_obsIA_engine_NativeEngine_isReady(JNIEnv*, jobject)`

`NativeEngine.infer()` does **not** gate on `isReady()` internally — the
`"MODEL_NOT_LOADED"` error code from `processQuery` covers that case.
Callers may poll `ready()` before the first query if they want an explicit
readiness check.

---

## 3. `release()`

Frees all native memory: model weights, KV cache, tokenizer tables, triage
rule data.

- **Call site:** `onLowMemory()` callback in the Android `Application` class,
  and any teardown path where the engine will not be used again.
- After `release()`, `isReady()` must return `false`.
- Calling `processQuery` after `release()` must return an `"error"` JSON with
  code `MODEL_NOT_LOADED`.
- `release()` is **idempotent**: calling it twice must not crash.

**JNI signature:** `void Java_com_obsIA_engine_NativeEngine_release(JNIEnv*, jobject)`

---

## 4. `getMemoryUsageMB(): Int`

Returns the current native RAM consumption of the engine in **megabytes**
(integer, rounded up).

- Returns `-1` if the figure is unavailable (e.g. the platform doesn't expose
  RSS, or `release()` has been called).
- Does not trigger garbage collection or any side effects.
- May be called at any time, including before `isReady()` returns `true`.

**JNI signature:** `jint Java_com_obsIA_engine_NativeEngine_getMemoryUsageMB(JNIEnv*, jobject)`

---

## Connecting `NativeEngine` to `QueryOrchestrator`

`QueryOrchestrator` (Issue #2) accepts any `LlmEngine` implementation.
To replace `LlmEngineStub` with `NativeEngine`:

```kotlin
// Before (stub):
val orchestrator = QueryOrchestrator(
    emergencyDetector = EmergencyDetector(...),
    emergencyRules    = EmergencyClinicalRules(...),
    routineRules      = RoutineClinicalRules(...),
    llmEngine         = LlmEngineStub()          // ← stub
)

// After (native, Issue #4 complete):
val orchestrator = QueryOrchestrator(
    emergencyDetector = EmergencyDetector(...),
    emergencyRules    = EmergencyClinicalRules(...),
    routineRules      = RoutineClinicalRules(...),
    llmEngine         = NativeEngine()           // ← real engine
)
```

No other change to `QueryOrchestrator` is required.

---

## Threading model

- The native library is **not thread-safe** by default.
- Callers must ensure `processQuery` is not called concurrently.
- `isReady()` and `getMemoryUsageMB()` are safe to call from any thread.

---

## Invariants the C++ side must uphold

1. `processQuery` always returns valid UTF-8.
2. `processQuery` always returns a JSON object with at least `"status"`.
3. `processQuery` never throws a C++ exception across the JNI boundary;
   all failures are expressed as `"error"` JSON.
4. `release()` never throws.
5. The library name exposed to `System.loadLibrary` is exactly `native_engine`.
