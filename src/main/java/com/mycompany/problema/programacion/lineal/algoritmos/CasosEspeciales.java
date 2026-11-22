package com.mycompany.problema.programacion.lineal.algoritmos;

/** Clasifica mensajes de casos especiales sin depender de Swing. */
public final class CasosEspeciales {

    private CasosEspeciales() {
    }

    public static String noAcotado(boolean esMin, String variable) {
        if (variable == null || variable.isEmpty()) {
            return "Solución no acotada: el objetivo puede mejorar sin límite.";
        }
        return "Solución no acotada: " + variable
                + " puede crecer sin límite y " + (esMin ? "Z puede disminuir" : "Z puede aumentar")
                + " indefinidamente.";
    }

    public static String infactible() {
        return "Problema infactible: no existe ninguna solución que cumpla todas las restricciones.";
    }

    public static String solucionesMultiples(int cantidad) {
        return "Soluciones óptimas múltiples: se encontraron " + cantidad
                + " soluciones básicas óptimas diferentes.";
    }

    public static String degeneracion(double[] valoresBasicos) {
        for (double valor : valoresBasicos) {
            if (Math.abs(valor) < 1e-9) {
                return "Solución degenerada: al menos una variable básica vale cero.";
            }
        }
        return null;
    }
}
