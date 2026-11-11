package com.mycompany.problema.programacion.lineal.algoritmos;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mycompany.problema.programacion.lineal.modelo.ProblemaPL;
import com.mycompany.problema.programacion.lineal.modelo.Restriccion;
import com.mycompany.problema.programacion.lineal.parser.ParserLP;
import org.junit.jupiter.api.Test;

/** Primal ⇄ Dual: reglas de la forma canónica y dualidad fuerte (mismo óptimo en los dos). */
class DualizadorTest {

    private static final double TOL = 1e-6;

    /** Carpintería: Max 3x1 + 5x2 (todo <=) → Min 4y1 + 12y2 + 18y3 con >=. */
    @Test
    void carpinteria() {
        Dualizador.Resultado d = Dualizador.dualizar("3x1 + 5x2",
                new String[]{"x1 <= 4", "2x2 <= 12", "3x1 + 2x2 <= 18"}, "Max", 'x', 'y');
        assertEquals("Min", d.getTipo());
        assertEquals("4y1 + 12y2 + 18y3", d.getFO());
        assertArrayEquals(new String[]{"y1 + 3y3 >= 3", "2y2 + 2y3 >= 5"}, d.getR());
        assertTrue(d.getPasos().isEmpty());
    }

    /** Min 2x1 + 3x2 (todo >=) → Max 4y1 + 6y2 con <=, que el Simplex resuelve: W = 10 = Z. */
    @Test
    void minimizacionDaMaxConMenorIgual() {
        Dualizador.Resultado d = Dualizador.dualizar("2x1 + 3x2",
                new String[]{"x1 + x2 >= 4", "x1 + 2x2 >= 6"}, "Min", 'x', 'y');
        assertEquals("Max", d.getTipo());
        assertEquals("4y1 + 6y2", d.getFO());
        assertArrayEquals(new String[]{"y1 + y2 <= 2", "y1 + 2y2 <= 3"}, d.getR());

        SimplexRevisado.Resultado r = resolverMenorIgual(d);
        assertEquals(10, r.z, TOL); // el mismo Z que Big M y Simplex Dual en el primal
        assertArrayEquals(new double[]{1, 1}, r.solucion, TOL);
    }

    /** Dualidad fuerte con la carpintería: el dual del dual vuelve en <=, y su óptimo es 36. */
    @Test
    void dualDelDualEsElPrimal() {
        Dualizador.Resultado d = Dualizador.dualizar("3x1 + 5x2",
                new String[]{"x1 <= 4", "2x2 <= 12", "3x1 + 2x2 <= 18"}, "Max", 'x', 'y');
        Dualizador.Resultado p = Dualizador.dualizar(d.getFO(), d.getR(), d.getTipo(), 'y', 'x');
        assertEquals("Max", p.getTipo());
        assertEquals("3x1 + 5x2", p.getFO());
        assertArrayEquals(new String[]{"x1 <= 4", "2x2 <= 12", "3x1 + 2x2 <= 18"}, p.getR());
        assertEquals(36, resolverMenorIgual(p).z, TOL);
    }

    /** Una >= en un Max se multiplica por −1 antes de dualizar. */
    @Test
    void normalizaMayorIgualEnMax() {
        Dualizador.Resultado d = Dualizador.dualizar("x1 + x2",
                new String[]{"x1 + x2 <= 6", "x1 >= 1"}, "Max", 'x', 'y');
        assertEquals("6y1 - y2", d.getFO());
        assertArrayEquals(new String[]{"y1 - y2 >= 1", "y1 >= 1"}, d.getR());
        assertEquals(1, d.getPasos().size());
    }

    /** Una igualdad se parte en dos: y1 (la <=) y y2 (la >= multiplicada por −1). */
    @Test
    void igualdadSeParteEnDos() {
        Dualizador.Resultado d = Dualizador.dualizar("2x1 + 3x2",
                new String[]{"x1 + x2 = 5", "x2 <= 3"}, "Max", 'x', 'y');
        assertEquals("5y1 - 5y2 + 3y3", d.getFO());
        assertArrayEquals(new String[]{"y1 - y2 >= 2", "y1 - y2 + y3 >= 3"}, d.getR());
        // Max 2x1 + 3x2 con x1 + x2 = 5, x2 <= 3 → x = (2, 3), Z = 13; y1 − y2 = 2, y3 = 1 → W = 10 + 3 = 13
    }

    /** Lado derecho 0 y decimales: la variable dual aparece igual en Z (con 0) para que el problema sea válido. */
    @Test
    void ceroYDecimales() {
        Dualizador.Resultado d = Dualizador.dualizar("1.5x1 + x2",
                new String[]{"x1 - x2 <= 0", "0.5x1 + x2 <= 4"}, "Max", 'x', 'y');
        assertEquals("0y1 + 4y2", d.getFO());
        assertArrayEquals(new String[]{"y1 + 0.5y2 >= 1.5", "-y1 + y2 >= 1"}, d.getR());
    }

    @Test
    void letraY() {
        assertEquals("3x1 + x12 - x5", Dualizador.aLetraX("3y1 + y12 - Y5", 'y'));
        assertEquals("3x1", Dualizador.aLetraX("3x1", 'x'));
    }

    @Test
    void variableFueraDeZ() {
        assertThrows(IllegalArgumentException.class, () -> Dualizador.dualizar("x1",
                new String[]{"x1 + x2 <= 3"}, "Max", 'x', 'y'));
    }

    /** Resuelve un problema escrito en x con todas sus restricciones <= y lado derecho >= 0. */
    private static SimplexRevisado.Resultado resolverMenorIgual(Dualizador.Resultado d) {
        String fo = Dualizador.aLetraX(d.getFO(), 'y');
        String[] r = d.getR();
        for (int i = 0; i < r.length; i++) {
            r[i] = Dualizador.aLetraX(r[i], 'y');
        }
        ProblemaPL p = ParserLP.parsear(fo, r, d.getTipo());
        double[][] a = new double[p.getRestricciones().size()][];
        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            Restriccion ri = p.getRestricciones().get(i);
            a[i] = ri.getCoeficientes();
            b[i] = ri.getIndependiente();
        }
        return SimplexRevisado.resolver(p.getFuncionObjetivo(), a, b, !p.isMaximizar());
    }
}
