package com.upb.obsia

import com.upb.obsia.domain.EmergencyDetector
import com.upb.obsia.domain.model.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EmergencyDetector — critical smoke test point #1.
 *
 * Verifies that the keyword-based urgency classifier correctly distinguishes
 * active emergency reports from educational/protocol queries without loading
 * any Android context (pure JVM test).
 */
class EmergencyDetectorTest {

    private lateinit var detector: EmergencyDetector

    // Minimal emergency_config.json that mirrors real production categories
    private val minimalConfig = """
        {
          "version": "1.0",
          "categories": [
            {
              "id": "cat_eclampsia",
              "urgency": "EMERGENCY",
              "keywords": ["eclampsia", "convulsiones en embarazo", "crisis convulsiva obstetrica"]
            },
            {
              "id": "cat_hemorragia",
              "urgency": "EMERGENCY",
              "keywords": ["hemorragia postparto", "sangrado severo posparto", "atonía uterina"]
            },
            {
              "id": "cat_preeclampsia",
              "urgency": "EMERGENCY",
              "keywords": ["preeclampsia severa", "tension alta embarazo", "presion 160"]
            }
          ]
        }
    """.trimIndent()

    @Before
    fun setUp() {
        detector = EmergencyDetector(minimalConfig)
    }

    // ── EMERGENCY paths ──────────────────────────────────────────────────────

    @Test
    fun `active eclampsia report returns EMERGENCY`() {
        val result = detector.analizar("paciente con eclampsia y convulsiones")
        assertEquals(UrgencyLevel.EMERGENCY, result)
    }

    @Test
    fun `postpartum hemorrhage report returns EMERGENCY`() {
        val result = detector.analizar("hemorragia postparto abundante no cede")
        assertEquals(UrgencyLevel.EMERGENCY, result)
    }

    @Test
    fun `keyword match is accent-insensitive`() {
        // "eclampsia" with accented input should still match
        val result = detector.analizar("paciente con eclámpsia")
        assertEquals(UrgencyLevel.EMERGENCY, result)
    }

    // ── ROUTINE paths (educational/protocol queries) ──────────────────────────

    @Test
    fun `educational prefix query returns ROUTINE`() {
        val result = detector.analizar("que es la eclampsia")
        assertEquals(UrgencyLevel.ROUTINE, result)
    }

    @Test
    fun `protocol management query returns ROUTINE`() {
        val result = detector.analizar("tratamiento de la eclampsia en gestantes")
        assertEquals(UrgencyLevel.ROUTINE, result)
    }

    @Test
    fun `unrelated query returns ROUTINE`() {
        val result = detector.analizar("cuales son los controles prenatales recomendados")
        assertEquals(UrgencyLevel.ROUTINE, result)
    }

    @Test
    fun `fisiopatologia query returns ROUTINE despite keyword presence`() {
        val result = detector.analizar("fisiopatologia de la eclampsia")
        assertEquals(UrgencyLevel.ROUTINE, result)
    }
}
