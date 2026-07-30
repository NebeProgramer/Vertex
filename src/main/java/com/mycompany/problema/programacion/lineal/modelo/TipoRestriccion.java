package com.mycompany.problema.programacion.lineal.modelo;

/**
 * Tipo de operador de una restricción de un Problema de Programación Lineal.
 */
public enum TipoRestriccion {
    MENOR_IGUAL("<="),
    MAYOR_IGUAL(">="),
    IGUAL("=");

    private final String simbolo;

    TipoRestriccion(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public static TipoRestriccion desdeSimbolo(String simbolo) {
        return switch (simbolo) {
            case "<=" -> MENOR_IGUAL;
            case ">=" -> MAYOR_IGUAL;
            case "=" -> IGUAL;
            default -> throw new IllegalArgumentException("Operador de restricción no reconocido: '" + simbolo + "'");
        };
    }

    @Override
    public String toString() {
        return simbolo;
    }
}
