package com.obsIA.domain

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Represents the full configuration of emergency rules loaded from JSON.
 */
@Serializable
data class EmergencyRulesConfig(
    val rules: List<EmergencyRule>
)

/**
 * Represents a single emergency rule.
 */
@Serializable
data class EmergencyRule(
    val id: String,
    val keywords: List<String>,
    val title: String,
    val why: String,
    val whats_happening: String,
    val danger_level: String,
    val immediate_steps: List<String>,
    val escalates_when: String,
    val disclaimer: String
)

/**
 * Result returned when a rule is matched.
 * This is what the rest of the system will consume.
 */
data class EmergencyRuleMatch(
    val id: String,
    val title: String,
    val why: String,
    val whats_happening: String,
    val danger_level: String,
    val immediate_steps: List<String>,
    val escalates_when: String,
    val disclaimer: String
)

/**
 * EmergencyClinicalRules
 *
 * This class is responsible for:
 * - Loading emergency rules from a JSON file stored in Android assets
 * - Matching user input against predefined emergency keywords
 * - Returning structured clinical guidance when a match is found
 *
 * IMPORTANT:
 * This class uses Android Context to access assets.
 */
class EmergencyClinicalRules(context: Context) {

    /**
     * JSON parser configuration.
     * ignoreUnknownKeys allows flexibility if JSON evolves.
     */
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    /**
     * Load and parse the emergency rules JSON from assets.
     */
    private val config: EmergencyRulesConfig =
        jsonConfig.decodeFromString(
            context.assets
                .open("emergency_rules.json")
                .bufferedReader()
                .use { it.readText() }
        )

    /**
     * Main lookup function.
     *
     * @param query User input text
     * @return EmergencyRuleMatch if a rule is triggered, null otherwise
     */
    fun lookup(query: String): EmergencyRuleMatch? {

        val textoLimpio = normalizeText(query)

        // Iterate over all rules
        for (rule in config.rules) {

            // Check each keyword of the rule
            for (keyword in rule.keywords) {

                val keywordLimpio = normalizeText(keyword)

                if (textoLimpio.contains(keywordLimpio)) {
                    println("LOG: Emergency rule [${rule.id}] triggered by [$keyword]")

                    return EmergencyRuleMatch(
                        id = rule.id,
                        title = rule.title,
                        why = rule.why,
                        whats_happening = rule.whats_happening,
                        danger_level = rule.danger_level,
                        immediate_steps = rule.immediate_steps,
                        escalates_when = rule.escalates_when,
                        disclaimer = rule.disclaimer
                    )
                }
            }
        }

        return null
    }

    /**
     * normalizeText
     *
     * Cleans and normalizes text for robust comparison:
     * - Converts to lowercase
     * - Removes accents
     * - Removes special characters
     *
     * This ensures better matching between user input and keywords.
     */
    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace(Regex("[áàä]"), "a")
            .replace(Regex("[éèë]"), "e")
            .replace(Regex("[íìï]"), "i")
            .replace(Regex("[óòö]"), "o")
            .replace(Regex("[úùü]"), "u")
            .replace(Regex("[ñÑ]"), "n")
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
    }
}
