package com.mycompany.problema.programacion.lineal.algoritmos;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Simplex Revisado: mismos óptimos que el Simplex de tabla, con las matrices de cada iteración. */
class SimplexRevisadoTest {

    private static final double TOL = 1e-6;

    /** Wyndor: Max 3x1 + 5x2 → x = (2, 6), Z = 36, en 3 iteraciones (igual que el ejemplo del Simplex). */
    @Test
    void wyndor() {
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(new double[]{3, 5},
                new double[][]{{1, 0}, {0, 2}, {3, 2}}, new double[]{4, 12, 18}, false);
        assertFalse(r.noAcotado);
        assertEquals(36, r.z, TOL);
        assertArrayEquals(new double[]{2, 6}, r.solucion, TOL);
        assertEquals(3, r.iteraciones.size());

        // iteración 1: B = I, entra x2 (c̄ = 5, el mayor), d = (0, 2, 2), sale la fila 2 (12/2 = 6)
        SimplexRevisado.Iteracion i1 = r.iteraciones.get(0);
        assertArrayEquals(new double[]{4, 12, 18}, i1.xB, TOL);
        assertEquals(1, i1.entra);
        assertArrayEquals(new double[]{0, 2, 2}, i1.d, TOL);
        assertEquals(1, i1.filaSale);
        assertEquals(0, i1.z, TOL);

        // iteración final: π = c_B·B⁻¹ = (0, 1.5, 1), los precios sombra del problema
        SimplexRevisado.Iteracion fin = r.iteraciones.get(2);
        assertTrue(fin.optima);
        assertArrayEquals(new double[]{0, 1.5, 1}, fin.pi, TOL);
    }

    /** Velas (lote P2): 3 variables, decimales. */
    @Test
    void velas() {
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(new double[]{6.5, 4, 6},
                new double[][]{{0.5, 0.4, 0.6}, {3, 1, 2}, {1, 1, 1}}, new double[]{28, 110, 60}, false);
        assertEquals(310, r.z, TOL);
        assertArrayEquals(new double[]{20, 30, 10}, r.solucion, TOL);
    }

    /** P3 del lote xij (4 variables, todo <=): Z = 18800 con x22 = 2000. */
    @Test
    void p3() {
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(new double[]{7, 6, 9.4, 4.8},
                new double[][]{{4, 0, 0, 0}, {0, 5, 2, 0}, {1, 1, 1, 0}, {8, 10, 6, 6}, {2, 2, 1, 1}},
                new double[]{2000, 10000, 3000, 12000, 4000}, false);
        assertEquals(18800, r.z, TOL);
        assertEquals(2000, r.solucion[2], TOL);
    }

    /** Min: se maximiza −c y se niega Z. */
    @Test
    void minimizacion() {
        // Min x1 − 2x2 con x1 + x2 <= 4, x2 <= 3 → x2 = 3, x1 = 0, Z = −6
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(new double[]{1, -2},
                new double[][]{{1, 1}, {0, 1}}, new double[]{4, 3}, true);
        assertEquals(-6, r.z, TOL);
        assertArrayEquals(new double[]{0, 3}, r.solucion, TOL);
    }

    @Test
    void noAcotado() {
        // Max x1 + x2 con x1 − x2 <= 1: x2 crece sin límite
        SimplexRevisado.Resultado r = SimplexRevisado.resolver(new double[]{1, 1},
                new double[][]{{1, -1}}, new double[]{1}, false);
        assertTrue(r.noAcotado);
        assertTrue(r.iteraciones.get(r.iteraciones.size() - 1).noAcotado);
    }

    @Test
    void ladoDerechoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> SimplexRevisado.resolver(new double[]{1},
                new double[][]{{1}}, new double[]{-2}, false));
    }

    @Test
    void inversa() {
        double[][] inv = Matrices.inversa(new double[][]{{2, 1}, {1, 1}});
        assertArrayEquals(new double[]{1, -1}, inv[0], TOL);
        assertArrayEquals(new double[]{-1, 2}, inv[1], TOL);
        assertThrows(IllegalArgumentException.class, () -> Matrices.inversa(new double[][]{{1, 2}, {2, 4}}));
    }
}
