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

    // morado de Vertex (o su versión oscura): ver Tema.fondoVentana()

    public FondoAzulPlano() {
        setOpaque(true);
        setBackground(Tema.fondoVentana());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
