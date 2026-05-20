package com.upb.obsia

import com.upb.obsia.domain.EmergencyClinicalRules
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EmergencyClinicalRules — critical smoke test point #2.
 *
 * Verifies that the deterministic rule engine correctly resolves (or skips)
 * a structured clinical response based on keyword matching, and that matched
 * responses contain the required clinical fields (immediate_steps, disclaimer).
 */
class EmergencyClinicalRulesTest {

    private lateinit var rules: EmergencyClinicalRules

    private val minimalRules = """
        {
          "rules": [
            {
              "id": "rule_eclampsia",
              "keywords": ["eclampsia", "convulsion obstetrica"],
              "title": "Eclampsia — Protocolo de Emergencia",
              "why": "Convulsiones en paciente obstétrica son potencialmente fatales.",
              "whats_happening": "Crisis convulsiva secundaria a preeclampsia severa.",
              "danger_level": "CRÍTICO",
              "immediate_steps": [
                "Posición de seguridad (decúbito lateral izquierdo)",
                "Proteger vía aérea",
                "Administrar sulfato de magnesio IV según protocolo",
                "Llamar equipo de emergencias"
              ],
              "escalates_when": "Convulsiones recurrentes o pérdida de consciencia prolongada.",
              "disclaimer": "Esta información es de apoyo. Consulte siempre al especialista."
            }
          ]
        }
    """.trimIndent()

    @Before
    fun setUp() {
        rules = EmergencyClinicalRules(minimalRules)
    }

    // ── Match paths ──────────────────────────────────────────────────────────

    @Test
    fun `eclampsia keyword returns non-null rule match`() {
        val match = rules.lookup("paciente con eclampsia activa")
        assertNotNull(match)
    }

    @Test
    fun `matched rule contains immediate steps`() {
        val match = rules.lookup("eclampsia detectada en sala de partos")
        assertNotNull(match)
        assertTrue("immediate_steps must not be empty", match!!.immediate_steps.isNotEmpty())
    }

    @Test
    fun `matched rule contains disclaimer`() {
        val match = rules.lookup("eclampsia")
        assertNotNull(match)
        assertTrue("disclaimer must not be blank", match!!.disclaimer.isNotBlank())
    }

    @Test
    fun `keyword match is case and accent insensitive`() {
        val match = rules.lookup("Eclámpsia severa")
        assertNotNull(match)
    }

    // ── No-match paths ───────────────────────────────────────────────────────

    @Test
    fun `educational query does not match any rule`() {
        val match = rules.lookup("que es la eclampsia y como se diagnostica")
        assertNull(match)
    }

    @Test
    fun `unrelated query returns null`() {
        val match = rules.lookup("controles prenatales de rutina")
        assertNull(match)
    }
}
