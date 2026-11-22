package com.mycompany.problema.programacion.lineal.algoritmos;

import com.mycompany.problema.programacion.lineal.parser.ParserLP;
import java.util.Map;

/**
 * Las dos condiciones de la teoría para aplicar el Simplex Dual, revisadas
 * sobre el tablero inicial que arma AlgoritmoDual: cada &gt;= se multiplica
 * por −1 para quedar &lt;=, se agrega una holgura por restricción y la base
 * son las holguras (C<sub>b</sub> = 0, así que Z<sub>j</sub> = 0 y
 * P<sub>j</sub> = C<sub>j</sub>).
 *
 * <ol>
 *   <li><b>∃ VR &lt; 0:</b> alguna variable básica (Sol) es negativa: el
 *       problema todavía no es factible. Si ninguna lo es, ya es factible y
 *       corresponde el Simplex.</li>
 *   <li><b>Óptimo:</b> en Zmax todos los P<sub>j</sub> &le; 0; en Zmin todos
 *       &ge; 0. Si no, el Simplex Dual no puede arrancar (Simplex o Big M).</li>
 * </ol>
 *
 * Además, como en AlgoritmoDual, no admite restricciones =. Lo usan el
 * selector de algoritmos (para explicar por qué no se ofrece) y "Decide por
 * mí" (para elegirlo solo cuando de verdad aplica).
 */
public final class CondicionesSimplexDual {

    private static final double EPS = 1e-9;

    private CondicionesSimplexDual() {
    }

    /**
     * @param FO    función objetivo con variables x (ej: "2x1 + 3x2")
     * @param R     restricciones con variables x
     * @param esMin true si se minimiza
     * @return null si el Simplex Dual aplica; si no, el motivo (qué condición falla)
     * @throws IllegalArgumentException si el problema está mal escrito
     */
    public static String motivoNoAplica(String FO, String[] R, boolean esMin) {
        boolean hayVrNegativa = false;
        for (String r : R) {
            String[] partes = ParserLP.partirRestriccion(r);
            if ("=".equals(partes[1])) {
                return "No admite restricciones = (usa Big M)";
            }
            double b = Double.parseDouble(partes[2]);
            // una >= se multiplica por −1: su Sol inicial es −b
            double sol = ">=".equals(partes[1]) ? -b : b;
            hayVrNegativa |= sol < -EPS;
        }

        // tablero inicial: base = holguras, C_b = 0 → Z_j = 0 → P_j = C_j
        boolean optimo = true;
        for (Map.Entry<Integer, Double> termino : ParserLP.parsearTerminos(FO).entrySet()) {
            double pj = termino.getValue();
            if (esMin ? pj < -EPS : pj > EPS) {
                optimo = false;
                break;
            }
        }

        String faltaOptimo = "No es óptimo al inicio: ∃ Pj " + (esMin ? "< 0" : "> 0");
        String faltaVr = "∄ VR < 0: ya es factible";
        if (!optimo && !hayVrNegativa) {
            return faltaOptimo + " y " + faltaVr + " (usa Simplex)";
        }
        if (!optimo) {
            return faltaOptimo + " (usa Big M)";
        }
        if (!hayVrNegativa) {
            return faltaVr + " (usa Simplex)";
        }
        return null;
    }
}
