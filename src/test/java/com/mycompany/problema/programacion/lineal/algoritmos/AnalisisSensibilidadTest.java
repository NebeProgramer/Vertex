package com.mycompany.problema.programacion.lineal.algoritmos;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Valores de referencia: problema Wyndor Glass (Hillier y Lieberman,
 * "Introducción a la investigación de operaciones", cap. de análisis de
 * sensibilidad): Max 3x1 + 5x2 con x1 <= 4, 2x2 <= 12, 3x1 + 2x2 <= 18.
 */
class AnalisisSensibilidadTest {

    private static final double TOL = 1e-6;
    private static final double INF = Double.POSITIVE_INFINITY;
    private static final double[][] A = {{1, 0}, {0, 2}, {3, 2}};
    private static final double[] B = {4, 12, 18};

    private static AnalisisSensibilidad wyndor() {
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(new double[]{3, 5}, A, B, false);
        return AnalisisSensibilidad.calcular(new double[]{3, 5}, A, B, false, r.baseFinal);
    }

    @Test
    void preciosSombraYHolguras() {
        AnalisisSensibilidad s = wyndor();
        assertEquals(36, s.z, TOL);
        assertArrayEquals(new double[]{0, 1.5, 1}, s.precioSombra, TOL);
        assertArrayEquals(new double[]{2, 0, 0}, s.holgura, TOL); // la planta 1 tiene 2 horas libres
        assertArrayEquals(new double[]{2, 6}, s.valor, TOL);
        assertArrayEquals(new double[]{0, 0}, s.costoReducido, TOL);
    }

    /** Rangos de b: b1 en [2, ∞), b2 en [6, 18], b3 en [12, 24]. */
    @Test
    void rangosLadoDerecho() {
        AnalisisSensibilidad s = wyndor();
        assertArrayEquals(new double[]{INF, 6, 6}, s.aumentoLadoDerecho, TOL);
        assertArrayEquals(new double[]{2, 6, 6}, s.disminucionLadoDerecho, TOL);
    }

    /** Rangos de c: c1 en [0, 7.5], c2 en [2, ∞). */
    @Test
    void rangosCoeficientes() {
        AnalisisSensibilidad s = wyndor();
        assertArrayEquals(new double[]{4.5, INF}, s.aumentoCoeficiente, TOL);
        assertArrayEquals(new double[]{3, 3}, s.disminucionCoeficiente, TOL);
    }

    /** Variable no básica: su costo reducido y hasta dónde puede subir su coeficiente antes de entrar. */
    @Test
    void variableNoBasica() {
        // Max 3x1 + 5x2 + x3, con x3 usando recursos: x1 + x3 <= 4, 2x2 <= 12, 3x1 + 2x2 + 2x3 <= 18
        double[][] a = {{1, 0, 1}, {0, 2, 0}, {3, 2, 2}};
        double[] c = {3, 5, 1};
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(c, a, B, false);
        AnalisisSensibilidad s = AnalisisSensibilidad.calcular(c, a, B, false, r.baseFinal);
        assertEquals(0, s.valor[2], TOL);
        // c̄3 = 1 − π·a3 = 1 − (0·1 + 1.5·0 + 1·2) = −1: Z baja 1 por cada unidad forzada de x3
        assertEquals(-1, s.costoReducido[2], TOL);
        assertEquals(1, s.aumentoCoeficiente[2], TOL); // con c3 > 2 conviene producir x3
        assertEquals(INF, s.disminucionCoeficiente[2], TOL);
    }

    /** Min: mismos números con signos del objetivo del usuario (Min −3x1 − 5x2 ≡ Max 3x1 + 5x2). */
    @Test
    void minimizacion() {
        double[] c = {-3, -5};
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(c, A, B, true);
        AnalisisSensibilidad s = AnalisisSensibilidad.calcular(c, A, B, true, r.baseFinal);
        assertEquals(-36, s.z, TOL);
        assertArrayEquals(new double[]{0, -1.5, -1}, s.precioSombra, TOL); // más recurso → Z (costo) baja
        // c1 = −3 en [−7.5, 0], c2 = −5 en (−∞, −2]
        assertArrayEquals(new double[]{3, 3}, s.aumentoCoeficiente, TOL);
        assertArrayEquals(new double[]{4.5, INF}, s.disminucionCoeficiente, TOL);
    }
}
