package com.mycompany.problema.programacion.lineal.modelo;

import java.util.Arrays;

/**
 * Una restricción de un Problema de Programación Lineal, ya interpretada:
 * coeficientes de cada variable (en el mismo orden que {@link ProblemaPL#getVariables()}),
 * término independiente y tipo de operador. Evita seguir pasando
 * {@code double[][] R} con columnas mágicas (R[i][2], R[i][3]...) sin saber
 * qué significa cada una.
 */
public class Restriccion {

    private final double[] coeficientes;
    private final double independiente;
    private final TipoRestriccion tipo;
    private final String textoOriginal;

    public Restriccion(double[] coeficientes, double independiente, TipoRestriccion tipo, String textoOriginal) {
        this.coeficientes = coeficientes;
        this.independiente = independiente;
        this.tipo = tipo;
        this.textoOriginal = textoOriginal;
    }

    public double[] getCoeficientes() {
        return coeficientes;
    }

    public double getCoeficiente(int indiceVariable) {
        return coeficientes[indiceVariable];
    }

    public double getIndependiente() {
        return independiente;
    }

    public TipoRestriccion getTipo() {
        return tipo;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    /** true si evaluar esta restricción en el punto dado la satisface (con tolerancia). */
    public boolean seSatisfaceCon(double[] punto, double eps) {
        double suma = 0;
        for (int k = 0; k < coeficientes.length; k++) {
            suma += coeficientes[k] * punto[k];
        }
        return switch (tipo) {
            case MENOR_IGUAL -> suma <= independiente + eps;
            case MAYOR_IGUAL -> suma >= independiente - eps;
            case IGUAL -> Math.abs(suma - independiente) <= eps;
        };
    }

    @Override
    public String toString() {
        return (textoOriginal != null ? textoOriginal : Arrays.toString(coeficientes) + " " + tipo + " " + independiente);
    }
}
