#pragma once
#include <string>
#include <vector>

namespace obsIA {

/**
 * Result returned by TriageEngine::evaluate().
 * Maps directly to the "emergency" / "ok" branches of the JNI contract
 * (see docs/jni_contract.md).
 */
struct TriageResult {
    bool        is_emergency;
    std::string triage_rule_id;   // empty when is_emergency == false
    std::string alert_message;    // empty when is_emergency == false
    long long   processing_ms;    // wall-clock time of the evaluate() call
};

/**
 * Keyword-based triage engine.
 *
 * Loads rules from a JSON file at construction time.  If the file is absent
 * or unparseable the engine falls back to built-in emergency rules, so
 * isLoaded() is virtually always true.
 *
 * Thread-safety: evaluate() is read-only after construction; calling it
 * concurrently is safe.  Construction itself is not thread-safe.
 */
class TriageEngine {
public:
    /**
     * Constructs the engine.
     * @param rules_path  Path to triage_rules.json on the device.
     *                    Falls back to built-in rules if the file is missing.
     */
    explicit TriageEngine(const std::string& rules_path);
    ~TriageEngine() = default;

    // Non-copyable, movable
    TriageEngine(const TriageEngine&)            = delete;
    TriageEngine& operator=(const TriageEngine&) = delete;
    TriageEngine(TriageEngine&&)                 = default;
    TriageEngine& operator=(TriageEngine&&)      = default;

    /** Returns true when at least one rule set (file-based or built-in) is loaded. */
    bool isLoaded() const;

    /**
     * Evaluates @p query against all loaded triage rules.
     * Returns the first matching emergency rule, or a non-emergency result.
     * Never throws.
     */
    TriageResult evaluate(const std::string& query) const;

private:
    struct Rule {
        std::string              id;
        std::vector<std::string> keywords;     // lowercase; any match triggers the rule
        std::string              alert_message;
    };

    bool              loaded_ = false;
    std::vector<Rule> rules_;

    // Parses @p path; falls back to loadBuiltinRules() on any failure.
    void loadFromFile(const std::string& path);

    // Loads hard-coded emergency rules for obstetrics / gynaecology.
    void loadBuiltinRules();
};

} // namespace obsIA
