package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Fondo azul plano, sin cuadrícula ni fórmulas, para las 7 ventanas de
 * resultados de los algoritmos — a diferencia de FondoMatematico (que sí
 * usa esos elementos en la ventana principal), acá se mantiene simple a
 * propósito para no competir visualmente con las tablas/datos.
 */
public class FondoAzulPlano extends JPanel {

    private static final Color AZUL = new Color(68, 56, 208);

    public FondoAzulPlano() {
        setOpaque(true);
        setBackground(AZUL);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
