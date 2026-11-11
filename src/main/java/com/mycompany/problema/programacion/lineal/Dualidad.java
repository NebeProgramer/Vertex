package com.mycompany.problema.programacion.lineal;

/**
 * Modo del problema que se está escribiendo en el Editor: primal (P, con
 * variables x) o dual (D, con variables y). Lo fija PrincipalPage al hacer
 * clic en el superíndice de Z (ver {@link SelectorTipoZ}).
 *
 * Igual que {@link Tema}, es un estado global que cada ventana lee al
 * construirse: las ventanas de resultados se crean al calcular, así que
 * cada una se queda con la teoría (primal o dual) del problema que resolvió
 * aunque después se cambie de modo.
 */
public final class Dualidad {

    private static boolean dual = false;

    private Dualidad() {
    }

    public static boolean esDual() {
        return dual;
    }

    public static void setDual(boolean esDual) {
        dual = esDual;
    }

    /** Letra de las variables en el modo actual: 'x' en el primal, 'y' en el dual. */
    public static char letra() {
        return dual ? 'y' : 'x';
    }

    /** La teoría que corresponde al modo actual. */
    public static String teoria(String primal, String dual) {
        return esDual() ? dual : primal;
    }
}
