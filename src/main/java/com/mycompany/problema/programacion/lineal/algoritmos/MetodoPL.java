package com.mycompany.problema.programacion.lineal.algoritmos;

/**
 * Contrato común para las ventanas que resuelven un Problema de
 * Programación Lineal (Simplex, Big M, Gráfico, Húngaro, Costos Duales,
 * Simplex Dual, Numérico). Permite que quien las despacha (PrincipalPage)
 * las use de forma polimórfica: "resuelve esto y muéstrate", sin
 * importarle cuál algoritmo es realmente.
 *
 * Cada ventana sigue teniendo su propio método interno (históricamente
 * llamado {@code calculos}) con la lógica matemática real — resolver()
 * solo delega ahí, así que implementar esta interfaz no cambia ningún
 * cálculo existente.
 */
public interface MetodoPL {

    /**
     * Resuelve el problema.
     *
     * @param FO   función objetivo, ej: "3x1+5x2"
     * @param R    restricciones
     * @param tipo "Max" o "Min"
     */
    void resolver(String FO, String[] R, String tipo);

    /** Muestra la ventana con el resultado ya calculado. */
    void mostrar();
}
