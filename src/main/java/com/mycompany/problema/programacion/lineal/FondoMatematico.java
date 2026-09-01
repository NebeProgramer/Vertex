package com.mycompany.problema.programacion.lineal;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Fondo azul con cuadrícula sutil y fórmulas matemáticas de fondo (marca de
 * agua), usado como contentPane de la ventana principal. El contenido real
 * (jPanel1, blanco) se agrega encima con margen, así que actúa como una
 * "tarjeta" flotando sobre este fondo.
 *
 * Solo se usa en la ventana principal — las 7 ventanas de resultados llevan
 * el fondo azul plano (sin grid ni fórmulas), para no competir visualmente
 * con las tablas de datos.
 */
public class FondoMatematico extends JPanel {

    private static final Color AZUL_BASE = new Color(68, 56, 208);
    private static final Color LINEA_GRID = new Color(255, 255, 255, 25);
    private static final Color FORMULA = new Color(255, 255, 255, 30);
    private static final int PASO_GRID = 26;

    private static final String[] FORMULAS = {
        "Max Z", "\u2211 xij \u2264 b", "3x\u2081+2x\u2082", "x\u2081, x\u2082 \u2265 0", "A\u207B\u00B9b"
    };

    public FondoMatematico() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(AZUL_BASE);
        g2.fillRect(0, 0, w, h);

        // cuadrícula
        g2.setColor(LINEA_GRID);
        for (int x = 0; x < w; x += PASO_GRID) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += PASO_GRID) {
            g2.drawLine(0, y, w, y);
        }

        // fórmulas de fondo, repartidas en diagonal para que se vean
        // asomando por los bordes de la tarjeta blanca sin depender de un
        // tamaño de ventana exacto
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        Composite original = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setColor(FORMULA);
        int n = FORMULAS.length;
        for (int i = 0; i < n; i++) {
            int fx = (int) (w * (0.08 + 0.20 * i)) % Math.max(w - 160, 1);
            int fy = (int) (h * (0.15 + 0.22 * i)) % Math.max(h - 60, 1);
            g2.drawString(FORMULAS[i], fx, fy);
        }
        g2.setComposite(original);

        g2.dispose();
    }
}
