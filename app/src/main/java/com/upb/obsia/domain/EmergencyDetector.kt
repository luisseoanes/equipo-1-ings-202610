package com.upb.obsia.domain

import com.upb.obsia.domain.model.UrgencyLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EmergencyConfig(
    val version: String,
    val categories: List<EmergencyCategory>
)

@Serializable
data class EmergencyCategory(
    val id: String,
    val urgency: String,
    val keywords: List<String>
)

class EmergencyDetector(jsonContent: String) {
    private val jsonConfig = Json { ignoreUnknownKeys = true }
    private val config = jsonConfig.decodeFromString<EmergencyConfig>(jsonContent)

    private fun normalizar(texto: String): String = texto.lowercase()
        .replace(Regex("[áàäâ]"), "a")
        .replace(Regex("[éèëê]"), "e")
        .replace(Regex("[íìïî]"), "i")
        .replace(Regex("[óòöô]"), "o")
        .replace(Regex("[úùüû]"), "u")
        .replace(Regex("[ñÑ]"), "n")
        .replace(Regex("[^a-z0-9 ]"), "")
        .trim()

    // Keywords pre-normalizados al cargar para evitar mismatch con texto normalizado
    private val keywordsPorCategoria: List<Pair<String, List<String>>> =
        config.categories.map { cat -> cat.id to cat.keywords.map { normalizar(it) } }

    // Prefijos al inicio de la query que indican pregunta conceptual
    private val prefijosEducativos = listOf(
        "que es ", "que son ", "que significa ", "como se ", "como funciona", "como se llama",
        "cual es la diferencia", "cuales son los ", "cuales son las ",
        "en que consiste ", "cuando se considera", "por que se llama", "explica ",
        "explicame ", "define ", "definicion de", "me explicas", "puedes explicar",
        "que debo saber sobre", "informacion sobre", "que caracteriza", "describeme",
        "que provoca ", "que causa ", "que origina ", "que genera ",
        "a que se debe ", "por que ocurre ", "por que se produce ",
        "cuanto tiempo ", "desde cuando "
    )

    // Indicadores de consulta académica/protocolo en CUALQUIER posición.
    // Un médico que pregunta "fisiopatología de X" o "causas de Y" consulta guías,
    // no reporta una emergencia activa. Se chequean como substring.
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
        // Académico / etiopatogenia — consulta educativa, nunca reporte activo
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

    fun analizar(input: String): UrgencyLevel {
        val textoLimpio = normalizar(input)

        // 1. Prefijo educativo al inicio → pregunta conceptual
        if (prefijosEducativos.any { textoLimpio.startsWith(it) }) {
            android.util.Log.d("ObsIA", "ConsultaEducativa (prefijo) detectada")
            return UrgencyLevel.ROUTINE
        }

        // 2. Indicadores de manejo/protocolo en cualquier posición →
        //    el médico consulta una guía, no reporta una emergencia activa.
        if (indicadoresDeManejo.any { textoLimpio.contains(it) }) {
            android.util.Log.d("ObsIA", "ConsultaDeManejo detectada")
            return UrgencyLevel.ROUTINE
        }

        // 3. Keyword de emergencia
        for ((categoriaId, keywords) in keywordsPorCategoria) {
            for (keyword in keywords) {
                if (textoLimpio.contains(keyword)) {
                    android.util.Log.d("ObsIA", "EmergencyMatch [$categoriaId] con [$keyword]")
                    return UrgencyLevel.EMERGENCY
                }
            }
        }
        return UrgencyLevel.ROUTINE
    }
}
