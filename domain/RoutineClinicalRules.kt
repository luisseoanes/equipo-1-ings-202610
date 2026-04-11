package com.obsIA.domain

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Root configuration object for routine clinical rules.
 * Loaded from a JSON file stored in Android assets.
 */
@Serializable
data class RoutineRulesConfig(
    val rules: List<RoutineRule>
)

/**
 * Represents a single routine clinical rule.
 */
@Serializable
data class RoutineRule(
    val id: String,
    val keywords: List<String>,
    val context: String,
    val response: String,
    val when_to_consult: String,
    val disclaimer: String
)

/**
 * Result returned when a routine rule is matched.
 * This is the structured output consumed by the orchestrator.
 */
data class RoutineRuleMatch(
    val id: String,
    val context: String,
    val response: String,
    val when_to_consult: String,
    val disclaimer: String
)

/**
 * RoutineClinicalRules
 *
 * Responsibilities:
 * - Load routine clinical rules from JSON stored in Android assets
 * - Match user queries against predefined routine keywords
 * - Return structured clinical guidance when a routine rule is found
 *
 * IMPORTANT:
 * This class depends on Android Context to read files from assets.
 */
class RoutineClinicalRules(context: Context) {

    /**
     * JSON parser configuration.
     * ignoreUnknownKeys makes the parser more resilient to future JSON changes.
     */
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    /**
     * Loads and parses routine rules from the assets folder.
     */
    private val config: RoutineRulesConfig =
        jsonConfig.decodeFromString(
            context.assets
                .open("routine_rules.json")
                .bufferedReader()
                .use { it.readText() }
        )

    /**
     * Looks up a routine clinical rule based on the user query.
     *
     * @param query User input text
     * @return RoutineRuleMatch if a rule is matched, null otherwise
     */
    fun lookup(query: String): RoutineRuleMatch? {
        val cleanQuery = normalizeText(query)

        for (rule in config.rules) {
            for (keyword in rule.keywords) {
                val cleanKeyword = normalizeText(keyword)

                if (cleanQuery.contains(cleanKeyword)) {
                    println("LOG: Routine rule [${rule.id}] triggered by [$keyword]")
                    return RoutineRuleMatch(
                        id = rule.id,
                        context = rule.context,
                        response = rule.response,
                        when_to_consult = rule.when_to_consult,
                        disclaimer = rule.disclaimer
                    )
                }
            }
        }

        return null
    }

    /**
     * Normalizes text for more robust comparisons.
     *
     * Actions:
     * - Converts to lowercase
     * - Removes accents
     * - Removes special characters
     *
     * @param text Input text
     * @return Normalized text
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

/**
 * Temporary placeholder for future LLM integration.
 *
 * This will later be replaced by the real JNI bridge / local inference engine.
 *
 * @param context Clinical context returned by a routine rule
 * @param query Original user query
 * @return Placeholder response indicating pending LLM integration
 */
fun sendToLLM(context: String, query: String): String {
    println("LOG: [PLACEHOLDER] LLM called with context [${context.take(50)}...]")
    println("LOG: [PLACEHOLDER] Original query [$query]")
    return "LLM_PENDING"
}
