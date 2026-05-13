package com.upb.obsia.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EmergencyRulesConfig(
    val rules: List<EmergencyRule>
)

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

class EmergencyClinicalRules(jsonContent: String) {
    private val jsonConfig = Json { ignoreUnknownKeys = true }
    private val config = jsonConfig.decodeFromString<EmergencyRulesConfig>(jsonContent)

    private fun normalizar(texto: String): String = texto.lowercase()
        .replace(Regex("[áàäâ]"), "a")
        .replace(Regex("[éèëê]"), "e")
        .replace(Regex("[íìïî]"), "i")
        .replace(Regex("[óòöô]"), "o")
        .replace(Regex("[úùüû]"), "u")
        .replace(Regex("[ñÑ]"), "n")
        .replace(Regex("[^a-z0-9 ]"), "")
        .trim()

    // Keywords pre-normalizados al cargar para garantizar match consistente
    private val rulesConKeywordsNormalizados: List<Pair<EmergencyRule, List<String>>> =
        config.rules.map { rule -> rule to rule.keywords.map { normalizar(it) } }

    // Mismos indicadores que EmergencyDetector — consulta académica o de protocolo
    // no es un reporte de emergencia activa.
    private val indicadoresDeManejo = listOf(
        // Manejo / protocolo / tratamiento
        "tratamiento de", "tratamiento del", "tratamiento para",
        "manejo de", "manejo del", "manejo para",
        "protocolo de", "protocolo del", "protocolo para",
        "criterios de", "criterios del", "criterios para", "criterios diagnosticos",
        "clasificacion de", "clasificacion del",
        "diagnostico de", "diagnostico del", "diagnostico diferencial",
        "dosis de", "dosis del", "dosis recomendada",
        "indicaciones de", "indicaciones del",
        "como se trata", "como tratar", "como manejar",
        "como diagnosticar", "cuando administrar", "cuando usar",
        "complicaciones de", "tipos de",
        "signos y sintomas de", "cuadro clinico de",
        "conducta ante", "abordaje de", "abordaje del",
        // Académico / etiopatogenia
        "causas de", "causas del", "causas en",
        "factores de riesgo",
        "fisiopatologia de", "fisiopatologia del",
        "patogenia de", "patogenia del",
        "etiologia de", "etiologia del",
        "mecanismo de", "mecanismo del",
        "caracteristicas de", "caracteristicas del",
        "prevencion de", "prevencion del",
        "incidencia de", "prevalencia de", "epidemiologia de",
        "sintomas de", "sintomas del",
        "signos de", "signos del",
        "hallazgos de", "hallazgos en",
        "definicion de", "concepto de",
        "cuando sospechar", "como sospechar",
        "cuando ocurre", "como ocurre"
    )

    private val indicadoresManejoNormalizados = indicadoresDeManejo.map { normalizar(it) }

    private val prefijosEducativos = listOf(
        "que es ", "que son ", "que significa ", "como se ", "como funciona", "como se llama",
        "cual es la diferencia", "cuales son los sintomas", "cuales son las causas",
        "en que consiste ", "cuando se considera", "por que se llama", "explica ",
        "explicame ", "define ", "definicion de", "me explicas", "puedes explicar",
        "que debo saber sobre", "informacion sobre", "que caracteriza", "describeme",
        "cuales son los criterios", "cuales son los signos"
    )

    fun lookup(query: String): EmergencyRuleMatch? {
        val textoLimpio = normalizar(query)

        // Consulta educativa → nunca es emergencia activa
        if (prefijosEducativos.any { textoLimpio.startsWith(it) }) {
            android.util.Log.d("ObsIA", "EmergencyRule omitida: consulta educativa detectada")
            return null
        }

        // Consulta de manejo/protocolo → no es emergencia activa, dejar al LLM
        if (indicadoresManejoNormalizados.any { textoLimpio.contains(it) }) {
            android.util.Log.d("ObsIA", "EmergencyRule omitida: consulta de manejo detectada")
            return null
        }

        for ((rule, keywords) in rulesConKeywordsNormalizados) {
            for (keyword in keywords) {
                if (textoLimpio.contains(keyword)) {
                    android.util.Log.d("ObsIA", "EmergencyRule [${rule.id}] con [$keyword]")
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
}
