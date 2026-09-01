package com.mycompany.problema.programacion.lineal;

import com.mycompany.problema.programacion.lineal.algoritmos.MetodoPL;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Ventana única para "MultiCalculos": en vez de abrir una ventana por cada
 * algoritmo elegido, reutiliza el contentPane YA RESUELTO de cada una
 * (tabla/texto ya poblados por resolver(), con su fondo azul + tarjeta
 * blanca del paso 5 incluidos) como una carta de un CardLayout, con
 * Anterior/Siguiente arriba para pasar de un algoritmo a otro.
 *
 * Importante: las instancias de MetodoPL que se le pasan NUNCA deben
 * llamar a mostrar() (que las mostraría como ventana aparte) — solo
 * resolver(). Su contentPane se "muda" acá adentro.
 */
public class MultiCalculos extends JFrame {

    private final List<MetodoPL> metodos;
    private final List<String> nombres;
    private final CardLayout cards = new CardLayout();
    private final JPanel panelCards = new JPanel(cards);
    private final JLabel lblNombre = new JLabel("", SwingConstants.CENTER);
    private int indice = 0;

    public MultiCalculos(List<MetodoPL> metodosResueltos, List<String> nombresAlgoritmos) {
        this.metodos = metodosResueltos;
        this.nombres = nombresAlgoritmos;

        java.awt.Color tinta = new java.awt.Color(23, 45, 66);
        java.awt.Color acento = new java.awt.Color(68, 56, 208);
        java.awt.Color bordeSuave = new java.awt.Color(220, 226, 234);

        setTitle("Vertex — Resultados (Multi-algoritmo)");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(760, 520));
        setMinimumSize(new Dimension(760, 520));
        setResizable(true);
        Recursos.aplicarIcono(this);
        VentanaPrefs.aplicar(this);

        for (int i = 0; i < metodos.size(); i++) {
            JFrame ventana = (JFrame) metodos.get(i);
            panelCards.add(ventana.getContentPane(), String.valueOf(i));
        }

        JButton btnAnterior = new JButton("\u2039 Anterior");
        JButton btnSiguiente = new JButton("Siguiente \u203A");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNombre.setForeground(tinta);

        btnAnterior.addActionListener(e -> mostrarIndice((indice - 1 + metodos.size()) % metodos.size()));
        btnSiguiente.addActionListener(e -> mostrarIndice((indice + 1) % metodos.size()));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(java.awt.Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, bordeSuave),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        top.add(btnAnterior, BorderLayout.WEST);
        top.add(lblNombre, BorderLayout.CENTER);
        top.add(btnSiguiente, BorderLayout.EAST);
        // con un solo algoritmo, Anterior/Siguiente no tienen nada que navegar
        btnAnterior.setEnabled(metodos.size() > 1);
        btnSiguiente.setEnabled(metodos.size() > 1);

        getContentPane().setBackground(acento);
        add(top, BorderLayout.NORTH);
        add(panelCards, BorderLayout.CENTER);

        mostrarIndice(0);
        pack();
    }

    private void mostrarIndice(int i) {
        indice = i;
        cards.show(panelCards, String.valueOf(i));
        lblNombre.setText((i + 1) + "/" + metodos.size() + "  \u2014  " + nombres.get(i));
    }
}
