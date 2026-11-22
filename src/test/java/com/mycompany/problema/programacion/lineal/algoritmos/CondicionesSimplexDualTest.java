package com.mycompany.problema.programacion.lineal.algoritmos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Las dos condiciones del Simplex Dual: ∃ VR < 0 en el tablero inicial y Pj óptimos. */
class CondicionesSimplexDualTest {

    @Test
    void signoPjDependeDelTipoDeObjetivo() {
        assertTrue(CondicionesSimplexDual.pjCumplenCondicion(new double[]{2, 3, 0}, true));
        assertFalse(CondicionesSimplexDual.pjCumplenCondicion(new double[]{2, -1, 0}, true));
        assertTrue(CondicionesSimplexDual.pjCumplenCondicion(new double[]{-2, -3, 0}, false));
        assertFalse(CondicionesSimplexDual.pjCumplenCondicion(new double[]{-2, 1, 0}, false));
    }

    /**
     * Max 350y1 + 125y2 − 600y3 (escrito en x) con todo <= y lado derecho positivo:
     * Sol = 2 y 3 (∄ VR < 0) y Pj = 350, 125 > 0 (no es óptimo). Corresponde el Simplex.
     */
    @Test
    void noCumpleNinguna() {
        String motivo = CondicionesSimplexDual.motivoNoAplica("350x1+125x2-600x3",
                new String[]{"x1+x2-2x3<=2", "x1-x3<=3"}, false);
        assertEquals("No es óptimo al inicio: ∃ Pj > 0 y ∄ VR < 0: ya es factible (usa Simplex)", motivo);
    }

    /** Su primal, Min 2x1 + 3x2 con >=: Sol = −350, −125 y Pj = 2, 3 ≥ 0. Aplica. */
    @Test
    void elPrimalSiCumple() {
        assertNull(CondicionesSimplexDual.motivoNoAplica("2x1+3x2",
                new String[]{"x1+x2>=350", "x1>=125", "-2x1-x2>=-600"}, true));
    }

    /** El caso típico de la teoría: Min con costos positivos y >=. */
    @Test
    void minConMayorIgual() {
        assertNull(CondicionesSimplexDual.motivoNoAplica("4x1+12x2+18x3",
                new String[]{"x1+3x3>=3", "2x2+2x3>=5"}, true));
    }

    /** Lado derecho escrito en negativo con <=. */
    @Test
    void ladoDerechoNegativo() {
        assertNull(CondicionesSimplexDual.motivoNoAplica("x1+x2", new String[]{"-x1<=-3", "x2<=4"}, true));
    }

    /** Max con una >=: hay VR < 0, pero los Pj positivos no son óptimos en Max. */
    @Test
    void maxConMayorIgualNoEsOptimo() {
        String motivo = CondicionesSimplexDual.motivoNoAplica("3x1+5x2", new String[]{"x1>=1", "x1+x2<=6"}, false);
        assertEquals("No es óptimo al inicio: ∃ Pj > 0 (usa Big M)", motivo);
    }

    /** Óptimo pero ya factible: nada que corregir. */
    @Test
    void yaFactible() {
        assertEquals("∄ VR < 0: ya es factible (usa Simplex)",
                CondicionesSimplexDual.motivoNoAplica("2x1+3x2", new String[]{"x1+x2<=4"}, true));
    }

    @Test
    void igualdad() {
        assertTrue(CondicionesSimplexDual.motivoNoAplica("2x1+3x2", new String[]{"x1+x2=4"}, true)
                .startsWith("No admite restricciones ="));
    }
}
