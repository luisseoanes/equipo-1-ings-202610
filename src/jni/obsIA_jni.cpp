#include "obsIA_jni.h"
#include "triage_engine.h"

#include <chrono>
#include <fstream>
#include <iomanip>
#include <memory>
#include <sstream>
#include <string>

#ifdef __ANDROID__
#  include <android/log.h>
#  define LOG_TAG "obsIA_jni"
#  define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#  define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
#  include <cstdio>
#  define LOGI(...) fprintf(stdout, __VA_ARGS__)
#  define LOGE(...) fprintf(stderr, __VA_ARGS__)
#endif

// ── Global state ──────────────────────────────────────────────────────────────

namespace {

std::unique_ptr<obsIA::TriageEngine> g_triage;
bool g_ready    = false;
bool g_released = false;   // set by release(); prevents re-init after teardown

void ensureInit() {
    if (!g_triage) {
        const std::string path =
            "/data/data/com.obsIA/files/triage_rules.json";
        try {
            g_triage = std::make_unique<obsIA::TriageEngine>(path);
            g_ready  = g_triage->isLoaded();
        } catch (...) {
            g_ready = false;
        }
    }
}

// ── JSON helpers ──────────────────────────────────────────────────────────────

/**
 * Escapes special characters for embedding a C++ string inside a JSON string
 * value.  Only handles the characters required by the JNI contract payloads;
 * no external library used (ostringstream only, as required).
 */
std::string jsonEscape(const std::string& s) {
    std::ostringstream out;
    for (unsigned char c : s) {
        switch (c) {
            case '"':  out << "\\\""; break;
            case '\\': out << "\\\\"; break;
            case '\n': out << "\\n";  break;
            case '\r': out << "\\r";  break;
            case '\t': out << "\\t";  break;
            default:
                if (c < 0x20) {
                    out << "\\u" << std::hex << std::setw(4)
                        << std::setfill('0') << static_cast<int>(c);
                } else {
                    out << static_cast<char>(c);
                }
        }
    }
    return out.str();
}

/** Builds the "error" JSON shape from the JNI contract. */
std::string makeErrorJson(const std::string& code,
                          const std::string& message,
                          long long          ms = 0) {
    std::ostringstream oss;
    oss << "{\"status\":\"error\","
        << "\"error_code\":\""    << jsonEscape(code)    << "\","
        << "\"error_message\":\"" << jsonEscape(message) << "\","
        << "\"processing_ms\":"   << ms
        << "}";
    return oss.str();
}

/** Trims leading/trailing ASCII whitespace. */
std::string trim(const std::string& s) {
    const auto begin = s.find_first_not_of(" \t\r\n");
    if (begin == std::string::npos) return {};
    const auto end = s.find_last_not_of(" \t\r\n");
    return s.substr(begin, end - begin + 1);
}

} // anonymous namespace

// ── JNI implementations ───────────────────────────────────────────────────────

extern "C" {

/**
 * processQuery — primary inference entry point.
 *
 * Flow (mandatory per issue spec):
 *   1. jstring → std::string
 *   2. ensureInit()
 *   3. g_triage->evaluate(query)
 *   4. is_emergency  → JSON "emergency"
 *   5. !is_emergency → JSON "ok" + mock text
 *
 * All C++ exceptions are caught and converted to "error" JSON.
 * No exception ever crosses the JNI boundary.
 */
JNIEXPORT jstring JNICALL
Java_com_obsIA_engine_NativeEngine_processQuery(
    JNIEnv* env, jobject /*thiz*/, jstring query)
{
    const auto t_start = std::chrono::steady_clock::now();

    auto elapsedMs = [&]() -> long long {
        const auto now = std::chrono::steady_clock::now();
        return std::chrono::duration_cast<std::chrono::milliseconds>(
                   now - t_start).count();
    };

    auto returnError = [&](const std::string& code,
                           const std::string& msg) -> jstring {
        const std::string json = makeErrorJson(code, msg, elapsedMs());
        return env->NewStringUTF(json.c_str());
    };

    try {
        // ── 1. Validate & convert jstring → std::string ──────────────────
        if (query == nullptr) {
            return returnError("NULL_INPUT", "La consulta no puede ser nula");
        }

        const char* raw = env->GetStringUTFChars(query, nullptr);
        if (!raw) {
            return returnError("NULL_INPUT", "Error al convertir la consulta");
        }
        const std::string query_str = trim(std::string(raw));
        env->ReleaseStringUTFChars(query, raw);

        if (query_str.empty()) {
            return returnError("NULL_INPUT", "La consulta no puede estar vacía");
        }

        // ── Check if engine was explicitly released ───────────────────────
        if (g_released) {
            return returnError("MODEL_NOT_LOADED",
                               "El motor de inferencia ha sido liberado");
        }

        // ── 2. Ensure triage engine is initialised ────────────────────────
        ensureInit();

        if (!g_triage) {
            return returnError("TRIAGE_RULES_MISSING",
                               "No se pudieron cargar las reglas de triaje");
        }

        // ── 3. Run triage ─────────────────────────────────────────────────
        const obsIA::TriageResult result = g_triage->evaluate(query_str);
        const long long total_ms        = elapsedMs();

        std::ostringstream oss;

        if (result.is_emergency) {
            // ── 4. Emergency path ─────────────────────────────────────────
            oss << "{"
                << "\"status\":\"emergency\","
                << "\"triage_rule_id\":\""  << jsonEscape(result.triage_rule_id) << "\","
                << "\"alert_message\":\""   << jsonEscape(result.alert_message)  << "\","
                << "\"response_text\":null,"
                << "\"processing_ms\":"     << total_ms
                << "}";
        } else {
            // ── 5. Non-emergency path (LLM mock) ──────────────────────────
            // TODO: conectar g_llama_engine->infer(prompt) cuando .gguf esté disponible
            const std::string mock_response =
                "Consulta recibida. "
                "ADVERTENCIA: Esta respuesta es generada por un motor simulado "
                "y no sustituye el criterio de un profesional de la salud.";

            oss << "{"
                << "\"status\":\"ok\","
                << "\"triage_rule_id\":null,"
                << "\"alert_message\":null,"
                << "\"response_text\":\"" << jsonEscape(mock_response) << "\","
                << "\"processing_ms\":"   << total_ms
                << "}";
        }

        return env->NewStringUTF(oss.str().c_str());

    } catch (...) {
        // Invariant: no C++ exception crosses the JNI boundary
        const std::string json = makeErrorJson(
            "PARSE_ERROR",
            "Error interno inesperado en el motor nativo",
            elapsedMs());
        return env->NewStringUTF(json.c_str());
    }
}

/**
 * isReady — returns true when triage rules are loaded and the engine is ready.
 * Thread-safe (read-only access to g_ready).
 */
JNIEXPORT jboolean JNICALL
Java_com_obsIA_engine_NativeEngine_isReady(
    JNIEnv* /*env*/, jobject /*thiz*/)
{
    return g_ready ? JNI_TRUE : JNI_FALSE;
}

/**
 * release — frees all native resources.
 * Idempotent; never throws.
 * After this call isReady() returns false and processQuery() returns
 * MODEL_NOT_LOADED.
 */
JNIEXPORT void JNICALL
Java_com_obsIA_engine_NativeEngine_release(
    JNIEnv* /*env*/, jobject /*thiz*/)
{
    try {
        g_triage.reset();
        g_ready    = false;
        g_released = true;
        LOGI("obsIA_jni: native engine released\n");
    } catch (...) {
        // release() never throws — swallow everything
    }
}

/**
 * getMemoryUsageMB — reads VmRSS from /proc/self/status and converts to MB.
 * Returns -1 when unavailable.
 * Thread-safe (no shared mutable state accessed).
 */
JNIEXPORT jint JNICALL
Java_com_obsIA_engine_NativeEngine_getMemoryUsageMB(
    JNIEnv* /*env*/, jobject /*thiz*/)
{
    try {
        std::ifstream status("/proc/self/status");
        if (!status.is_open()) return -1;

        std::string line;
        while (std::getline(status, line)) {
            // Line format: "VmRSS:     12345 kB"
            if (line.rfind("VmRSS:", 0) == 0) {
                std::istringstream iss(line.substr(6));
                long long kb = 0;
                iss >> kb;
                if (iss.fail() || kb <= 0) return -1;
                // Round up kB → MB
                return static_cast<jint>((kb + 1023LL) / 1024LL);
            }
        }
    } catch (...) {
        // Must not throw across JNI boundary
    }
    return -1;
}

} // extern "C"
