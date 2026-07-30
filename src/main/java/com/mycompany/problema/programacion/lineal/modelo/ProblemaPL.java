package com.mycompany.problema.programacion.lineal.modelo;

import java.util.Collections;
import java.util.List;

/**
 * Representa un Problema de Programación Lineal ya interpretado (función
 * objetivo + restricciones + sentido de optimización), en vez de pasar por
 * separado un String de la FO, un String[] de restricciones y un String
 * "Max"/"Min" como se hacía antes en cada ventana.
 *
 * variables() da el índice ORIGINAL (el "n" de xN) en el mismo orden que los
 * arreglos de coeficientes — así "x1+x3" (sin x2) se sigue mostrando como x1
 * y x3, no como x1 y x2.
 */
public class ProblemaPL {

    private final List<Integer> variables;      // índices originales x_N, en orden de columna
    private final double[] funcionObjetivo;      // mismo orden que 'variables'
    private final List<Restriccion> restricciones;
    private final boolean maximizar;
    private final String textoOriginalFO;

    public ProblemaPL(List<Integer> variables, double[] funcionObjetivo,
            List<Restriccion> restricciones, boolean maximizar, String textoOriginalFO) {
        this.variables = Collections.unmodifiableList(variables);
        this.funcionObjetivo = funcionObjetivo;
        this.restricciones = Collections.unmodifiableList(restricciones);
        this.maximizar = maximizar;
        this.textoOriginalFO = textoOriginalFO;
    }

    public List<Integer> getVariables() {
        return variables;
    }

    public int getNumVariables() {
        return variables.size();
    }

    /** Nombre de la variable en la posición k (0-indexada), ej. "x3". */
    public String nombreVariable(int k) {
        return "x" + variables.get(k);
    }

    public double[] getFuncionObjetivo() {
        return funcionObjetivo;
    }

    public List<Restriccion> getRestricciones() {
        return restricciones;
    }

    public boolean isMaximizar() {
        return maximizar;
    }

    public String getTextoOriginalFO() {
        return textoOriginalFO;
    }

    /**
     * Evalúa Z para un punto dado (mismo orden que getVariables()).
     */
    public double evaluarZ(double[] punto) {
        double z = 0;
        for (int k = 0; k < funcionObjetivo.length; k++) {
            z += funcionObjetivo[k] * punto[k];
        }
        return z;
    }
}
