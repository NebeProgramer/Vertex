package com.mycompany.problema.programacion.lineal.algoritmos;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formato numérico único para todas las ventanas de resultados (tablas,
 * texto de la solución, gráfica y tooltips), para que un mismo valor se
 * vea igual en cualquier algoritmo:
 *
 * - Punto como separador decimal, sin importar el idioma del sistema
 *   (String.format usaba la coma en equipos en español).
 * - Redondeo a 3 decimales; se muestran siempre los 3 ("2.500").
 * - Si al redondear queda entero, va sin decimales ("60", no "60.000").
 * - Sin notación científica ni separador de miles.
 */
public final class FormatoNumero {

    private static final int DECIMALES = 3;

    private FormatoNumero() {
    }

    public static String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return String.valueOf(v);
        }
        BigDecimal redondeado = BigDecimal.valueOf(v).setScale(DECIMALES, RoundingMode.HALF_UP);
        if (redondeado.signum() == 0) {
            return "0"; // también evita "-0"
        }
        if (redondeado.stripTrailingZeros().scale() <= 0) {
            return redondeado.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        }
        return redondeado.toPlainString();
    }
}
