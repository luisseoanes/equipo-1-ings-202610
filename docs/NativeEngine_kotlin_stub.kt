package com.obsIA.engine

import com.obsIA.domain.LlmEngine
import com.obsIA.domain.model.ClinicalResponse
import org.json.JSONObject

/**
 * Wraps the native C++ inference engine via JNI.
 *
 * Implements [LlmEngine] so it can be injected into [com.obsIA.domain.QueryOrchestrator]
 * as a drop-in replacement for [com.obsIA.domain.LlmEngineStub].
 *
 * The native library is loaded once when the class is first referenced.
 * See docs/jni_contract.md for the full C++ ↔ Kotlin data contract.
 *
 * Usage (replaces LlmEngineStub in QueryOrchestrator — Issue #4):
 *
 *   val orchestrator = QueryOrchestrator(
 *       emergencyDetector = EmergencyDetector(...),
 *       emergencyRules    = EmergencyClinicalRules(...),
 *       routineRules      = RoutineClinicalRules(...),
 *       llmEngine         = NativeEngine()   // ← this class
 *   )
 */
class NativeEngine : LlmEngine {

    // ── JNI declarations ────────────────────────────────────────────────────

    /** Runs inference on [query]. Returns a JSON string; see jni_contract.md. */
    private external fun processQuery(query: String): String

    /** Returns true only when model weights and triage rules are fully loaded. */
    private external fun isReady(): Boolean

    /** Frees all native memory. Call from onLowMemory(). Idempotent. */
    private external fun release()

    /** Current native RAM usage in MB, or -1 if unavailable. */
    private external fun getMemoryUsageMB(): Int

    // ── Library loading ──────────────────────────────────────────────────────

    companion object {
        init {
            System.loadLibrary("native_engine")
        }
    }

    // ── LlmEngine implementation ─────────────────────────────────────────────

    /**
     * Called by [com.obsIA.domain.QueryOrchestrator] on the LLM path (Step 4).
     *
     * Delegates to the native [processQuery], then extracts the human-readable
     * string from the JSON response according to the JNI contract.
     */
    override fun infer(query: String): String {
        val jsonString = processQuery(query)
        return parseResponse(jsonString)
    }

    // ── Public convenience wrappers ──────────────────────────────────────────

    /** @see isReady */
    fun ready(): Boolean = isReady()

    /** @see getMemoryUsageMB */
    fun memoryMB(): Int = getMemoryUsageMB()

    /** @see release */
    fun free() = release()

    // ── Internal ─────────────────────────────────────────────────────────────

    /**
     * Maps the JSON contract shapes defined in jni_contract.md to a plain String
     * suitable for [com.obsIA.domain.model.ClinicalResponse.LlmGenerated].
     *
     * | status      | returned value                             |
     * |-------------|--------------------------------------------|
     * | "ok"        | response_text                              |
     * | "emergency" | alert_message                              |
     * | "error"     | "Error: <error_message>"                   |
     * | (exception) | fallback string with DISCLAIMER            |
     */
    private fun parseResponse(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            when (json.getString("status")) {
                "emergency" -> json.getString("alert_message")
                "ok"        -> json.getString("response_text")
                else        -> "Error: ${json.optString("error_message", "desconocido")}"
            }
        } catch (e: Exception) {
            "Error al procesar respuesta del motor. ${ClinicalResponse.DISCLAIMER}"
        }
    }
}
