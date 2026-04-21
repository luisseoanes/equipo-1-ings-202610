#include "triage_engine.h"

#include <algorithm>
#include <chrono>
#include <fstream>
#include <sstream>
#include <cctype>

#ifdef __ANDROID__
#  include <android/log.h>
#  define LOG_TAG "TriageEngine"
#  define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#  define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#else
#  include <cstdio>
#  define LOGI(...) fprintf(stdout, __VA_ARGS__)
#  define LOGW(...) fprintf(stderr, __VA_ARGS__)
#endif

namespace obsIA {

// ── Internal helpers ─────────────────────────────────────────────────────────

namespace {

/**
 * Extracts the value of a JSON string field: "field": "value".
 * Returns an empty string if the field is not found.
 * Not a general-purpose JSON parser — assumes our controlled rules format.
 */
std::string extractStringField(const std::string& obj, const std::string& field) {
    const std::string key = "\"" + field + "\"";
    auto pos = obj.find(key);
    if (pos == std::string::npos) return {};

    pos = obj.find(':', pos + key.size());
    if (pos == std::string::npos) return {};

    pos = obj.find('"', pos + 1);
    if (pos == std::string::npos) return {};

    const auto end = obj.find('"', pos + 1);
    if (end == std::string::npos) return {};

    return obj.substr(pos + 1, end - pos - 1);
}

/**
 * Extracts the values of a JSON string array field: "field": ["a","b",...].
 * Returns an empty vector if the field is not found.
 */
std::vector<std::string> extractArrayField(const std::string& obj,
                                           const std::string& field) {
    std::vector<std::string> result;
    const std::string key = "\"" + field + "\"";

    auto pos = obj.find(key);
    if (pos == std::string::npos) return result;

    const auto arrStart = obj.find('[', pos + key.size());
    if (arrStart == std::string::npos) return result;

    const auto arrEnd = obj.find(']', arrStart);
    if (arrEnd == std::string::npos) return result;

    const std::string arrContent = obj.substr(arrStart + 1, arrEnd - arrStart - 1);

    size_t i = 0;
    while (true) {
        const auto q1 = arrContent.find('"', i);
        if (q1 == std::string::npos) break;
        const auto q2 = arrContent.find('"', q1 + 1);
        if (q2 == std::string::npos) break;
        result.push_back(arrContent.substr(q1 + 1, q2 - q1 - 1));
        i = q2 + 1;
    }
    return result;
}

/** Converts a string to lowercase in-place. */
void toLowerInPlace(std::string& s) {
    std::transform(s.begin(), s.end(), s.begin(),
                   [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
}

} // anonymous namespace

// ── Construction ─────────────────────────────────────────────────────────────

TriageEngine::TriageEngine(const std::string& rules_path) {
    loadFromFile(rules_path);
}

// ── Public API ────────────────────────────────────────────────────────────────

bool TriageEngine::isLoaded() const {
    return loaded_;
}

TriageResult TriageEngine::evaluate(const std::string& query) const {
    const auto t0 = std::chrono::steady_clock::now();

    try {
        // Normalise query to lowercase for case-insensitive matching
        std::string lower = query;
        toLowerInPlace(lower);

        for (const auto& rule : rules_) {
            for (const auto& kw : rule.keywords) {
                if (lower.find(kw) != std::string::npos) {
                    const auto t1 = std::chrono::steady_clock::now();
                    const auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(
                                        t1 - t0).count();
                    return {true, rule.id, rule.alert_message, ms};
                }
            }
        }
    } catch (...) {
        // evaluate() must never throw
        return {false, {}, {}, 0};
    }

    const auto t1 = std::chrono::steady_clock::now();
    const auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();
    return {false, {}, {}, ms};
}

// ── Private ───────────────────────────────────────────────────────────────────

void TriageEngine::loadFromFile(const std::string& path) {
    std::ifstream file(path);
    if (!file.is_open()) {
        LOGW("TriageEngine: rules file not found at '%s' — using built-in rules\n",
             path.c_str());
        loadBuiltinRules();
        return;
    }

    std::ostringstream oss;
    oss << file.rdbuf();
    const std::string content = oss.str();

    // Locate the "rules" array
    const auto rulesPos = content.find("\"rules\"");
    if (rulesPos == std::string::npos) {
        LOGW("TriageEngine: 'rules' key not found in '%s' — using built-in rules\n",
             path.c_str());
        loadBuiltinRules();
        return;
    }

    const auto arrStart = content.find('[', rulesPos);
    if (arrStart == std::string::npos) {
        LOGW("TriageEngine: could not locate rules array — using built-in rules\n");
        loadBuiltinRules();
        return;
    }

    // Walk each { } object inside the array
    size_t pos = arrStart + 1;
    while (true) {
        const auto objStart = content.find('{', pos);
        if (objStart == std::string::npos) break;

        // Find the matching closing brace
        int depth = 1;
        size_t i  = objStart + 1;
        while (i < content.size() && depth > 0) {
            if      (content[i] == '{') ++depth;
            else if (content[i] == '}') --depth;
            ++i;
        }
        if (depth != 0) break; // malformed JSON

        const std::string objStr = content.substr(objStart, i - objStart);

        Rule rule;
        rule.id            = extractStringField(objStr, "id");
        rule.keywords      = extractArrayField(objStr, "keywords");
        rule.alert_message = extractStringField(objStr, "alert_message");

        // Normalise keywords to lowercase once at load time
        for (auto& kw : rule.keywords) {
            toLowerInPlace(kw);
        }

        if (!rule.id.empty() && !rule.keywords.empty() && !rule.alert_message.empty()) {
            rules_.push_back(std::move(rule));
        }
        pos = i;
    }

    if (rules_.empty()) {
        LOGW("TriageEngine: no valid rules parsed from '%s' — using built-in rules\n",
             path.c_str());
        loadBuiltinRules();
        return;
    }

    loaded_ = true;
    LOGI("TriageEngine: loaded %zu rule(s) from '%s'\n", rules_.size(), path.c_str());
}

void TriageEngine::loadBuiltinRules() {
    rules_.clear();

    // ── Obstetric / gynaecological emergencies ────────────────────────────
    rules_.push_back({
        "OB-001",
        {"convulsiones", "eclampsia", "crisis convulsiva", "ataque epileptico",
         "convulsion", "seizure"},
        "EMERGENCIA: Posible eclampsia - activar protocolo de emergencia "
        "obstetrica de inmediato"
    });
    rules_.push_back({
        "OB-002",
        {"hemorragia severa", "hemorragia masiva", "hemorragia postparto",
         "sangrado masivo", "perdida masiva de sangre"},
        "EMERGENCIA: Hemorragia obstetrica severa - requerir atencion "
        "inmediata y trasfusion"
    });
    rules_.push_back({
        "OB-003",
        {"paro cardiorespiratorio", "paro cardiaco", "paro respiratorio",
         "sin pulso", "inconsciente sin respiracion"},
        "EMERGENCIA: Paro cardiorespiratorio - iniciar RCP de inmediato"
    });
    rules_.push_back({
        "OB-004",
        {"embolia de liquido amniotico", "embolia amniotica"},
        "EMERGENCIA: Posible embolia de liquido amniotico - activar "
        "codigo azul obstetrico"
    });
    rules_.push_back({
        "OB-005",
        {"rotura uterina", "ruptura uterina", "utero roto"},
        "EMERGENCIA: Posible rotura uterina - preparar cirugia de emergencia"
    });
    rules_.push_back({
        "OB-006",
        {"placenta previa con sangrado", "desprendimiento de placenta",
         "abruptio placentae", "desprendimiento prematuro"},
        "EMERGENCIA: Desprendimiento o placenta previa con sangrado activo - "
        "atencion obstetrica urgente"
    });
    rules_.push_back({
        "OB-007",
        {"sepsis obstetrica", "sepsis puerperal", "shock septico"},
        "EMERGENCIA: Sepsis obstetrica - iniciar protocolo de sepsis "
        "con antibioticos de amplio espectro"
    });

    loaded_ = true;
    LOGI("TriageEngine: built-in rules loaded (%zu rules)\n", rules_.size());
}

} // namespace obsIA
