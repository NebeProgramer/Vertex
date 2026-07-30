/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.problema.programacion.lineal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JPanel;

/**
 * Panel que dibuja el plano cartesiano, las rectas de las restricciones,
 * la región factible sombreada y el punto óptimo del método gráfico.
 * Se usa igual que cualquier JPanel; solo hay que llamar a
 * {@link #mostrarResultado} cuando ya se calculó la solución.
 */
public class GraficoPanel extends JPanel {

    private static final Color[] COLORES_RECTAS = {
        new Color(200, 70, 70), new Color(60, 140, 60), new Color(60, 90, 200),
        new Color(200, 140, 30), new Color(150, 70, 170), new Color(30, 150, 150)
    };

    private double[][] Rmat;
    private List<double[]> poligono;
    private double[] optimo;
    private double zOptimo;
    private boolean hayResultado = false;
    private boolean esFactible = true;

    public GraficoPanel() {
        setBackground(Color.WHITE);
    }

    /**
     * @param Rmat     matriz de restricciones [a, b, c, tipo] (tipo: 0=&lt;=, 1=&gt;=, 2==)
     * @param poligono vértices de la región factible ya ordenados (para sombrear); puede ser null si no hay región
     * @param optimo   punto óptimo [x1, x2]; null si no existe
     * @param zOptimo  valor óptimo de Z
     * @param factible si la región factible existe
     */
    public void mostrarResultado(double[][] Rmat, List<double[]> poligono, double[] optimo, double zOptimo, boolean factible) {
        this.Rmat = Rmat;
        this.poligono = poligono;
        this.optimo = optimo;
        this.zOptimo = zOptimo;
        this.esFactible = factible;
        this.hayResultado = true;
        repaint();
    }

    public void limpiar() {
        this.hayResultado = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int margen = 45;

        if (!hayResultado) {
            g2.setColor(Color.GRAY);
            g2.drawString("Aún no hay datos para graficar.", margen, h / 2);
            return;
        }
        if (!esFactible) {
            g2.setColor(new Color(200, 40, 40));
            g2.drawString("No existe región factible para este problema.", margen, h / 2);
            return;
        }

        // ---- rango de los ejes según el tamaño de la región / rectas ----
        double maxX = 1, maxY = 1;
        if (poligono != null) {
            for (double[] p : poligono) {
                maxX = Math.max(maxX, p[0]);
                maxY = Math.max(maxY, p[1]);
            }
        }
        if (optimo != null) {
            maxX = Math.max(maxX, optimo[0]);
            maxY = Math.max(maxY, optimo[1]);
        }
        maxX *= 1.25;
        maxY *= 1.25;

        int plotW = w - 2 * margen;
        int plotH = h - 2 * margen;

        // ---- ejes ----
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(margen, h - margen, w - margen, h - margen); // eje x1
        g2.drawLine(margen, margen, margen, h - margen);         // eje x2
        g2.drawString("x1", w - margen - 5, h - margen + 18);
        g2.drawString("x2", margen - 25, margen + 5);

        // ---- región factible sombreada ----
        if (poligono != null && poligono.size() >= 3) {
            int n = poligono.size();
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = margen + (int) (poligono.get(i)[0] / maxX * plotW);
                ys[i] = h - margen - (int) (poligono.get(i)[1] / maxY * plotH);
            }
            g2.setColor(new Color(120, 180, 255, 110));
            g2.fillPolygon(xs, ys, n);
            g2.setColor(new Color(40, 90, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(xs, ys, n);
        }

        // ---- rectas de cada restricción ----
        if (Rmat != null) {
            for (int i = 0; i < Rmat.length; i++) {
                double a = Rmat[i][0];
                double b = Rmat[i][1];
                double c = Rmat[i][2];
                if (Math.abs(a) < 1e-9 && Math.abs(b) < 1e-9) {
                    continue;
                }
                g2.setColor(COLORES_RECTAS[i % COLORES_RECTAS.length]);
                g2.setStroke(new BasicStroke(2f));

                double x0, y0, x1p, y1p;
                if (Math.abs(b) < 1e-9) {
                    double xv = c / a;
                    x0 = xv;
                    y0 = 0;
                    x1p = xv;
                    y1p = maxY;
                } else {
                    x0 = 0;
                    y0 = c / b;
                    x1p = maxX;
                    y1p = (c - a * maxX) / b;
                }

                int px0 = margen + (int) (x0 / maxX * plotW);
                int py0 = h - margen - (int) (y0 / maxY * plotH);
                int px1 = margen + (int) (x1p / maxX * plotW);
                int py1 = h - margen - (int) (y1p / maxY * plotH);
                g2.drawLine(px0, py0, px1, py1);
                g2.drawString("R" + (i + 1), px1 - 25, py1 + (py1 < py0 ? -5 : 15));
            }
        }

        // ---- punto óptimo ----
        if (optimo != null) {
            int px = margen + (int) (optimo[0] / maxX * plotW);
            int py = h - margen - (int) (optimo[1] / maxY * plotH);
            g2.setColor(new Color(220, 30, 30));
            g2.fillOval(px - 5, py - 5, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString(String.format("(%.2f, %.2f)  Z=%.2f", optimo[0], optimo[1], zOptimo), px + 8, py - 8);
        }
    }
}
