package com.obsIA.domain

import android.content.Context
import com.obsIA.domain.model.UrgencyLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Root configuration object for emergency detection.
 * Loaded from JSON.
 */
@Serializable
data class EmergencyConfig(
    val version: String,
    val categories: List<EmergencyCategory>
)

/**
 * Represents a category of emergency (e.g., hemorrhage, convulsion).
 */
@Serializable
data class EmergencyCategory(
    val id: String,
    val urgency: String,
    val keywords: List<String>
)

/**
 * EmergencyDetector
 *
 * Responsibilities:
 * - Load emergency keyword categories from JSON (assets)
 * - Normalize user input
 * - Detect if input corresponds to an emergency
 *
 * Output:
 * - UrgencyLevel.EMERGENCY
 * - UrgencyLevel.ROUTINE
 */
class EmergencyDetector(context: Context) {

    /**
     * JSON parser configuration.
     */
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    /**
     * Load JSON configuration from Android assets.
     */
    private val config: EmergencyConfig =
        jsonConfig.decodeFromString(
            context.assets
                .open("emergency_keywords.json")
                .bufferedReader()
                .use { it.readText() }
        )

    /**
     * analizar
     *
     * Evaluates user input and determines urgency level.
     *
     * @param input User query
     * @return UrgencyLevel (EMERGENCY or ROUTINE)
     */
    fun analizar(input: String): UrgencyLevel {

        val textoLimpio = normalizeText(input)

        // Iterate through all categories
        for (categoria in config.categories) {

            // Check each keyword
            for (keyword in categoria.keywords) {

                val keywordLimpio = normalizeText(keyword)

                if (textoLimpio.contains(keywordLimpio)) {
                    println("LOG: Match in [${categoria.id}] with [$keyword]")
                    return UrgencyLevel.EMERGENCY
                }
            }
        }

        return UrgencyLevel.ROUTINE
    }

    /**
     * normalizeText
     *
     * Normalizes text to improve matching:
     * - Lowercase
     * - Removes accents
     * - Removes special characters
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
