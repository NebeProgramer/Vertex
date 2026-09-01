/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.problema.programacion.lineal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

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
    private List<double[]> puntosFactibles; // cada elemento: {x1, x2, Z}
    private List<int[]> pantallaFactibles;  // posición en pantalla de cada punto de puntosFactibles (mismo índice)
    private double[] optimo;
    private double zOptimo;
    private boolean hayResultado = false;
    private boolean esFactible = true;

    public GraficoPanel() {
        setBackground(Color.WHITE);
        // habilita el cuadro flotante (tooltip) al pasar el mouse sobre un punto factible
        ToolTipManager.sharedInstance().registerComponent(this);
        setToolTipText("");
    }

    /**
     * @param Rmat            matriz de restricciones [a, b, c, tipo] (tipo: 0=&lt;=, 1=&gt;=, 2==)
     * @param poligono        vértices de la región factible ya ordenados (para sombrear); puede ser null si no hay región
     * @param puntosFactibles todas las intersecciones que son puntos factibles, como {x1, x2, Z}; puede ser null
     * @param optimo          punto óptimo [x1, x2]; null si no existe
     * @param zOptimo         valor óptimo de Z
     * @param factible        si la región factible existe
     */
    public void mostrarResultado(double[][] Rmat, List<double[]> poligono, List<double[]> puntosFactibles,
            double[] optimo, double zOptimo, boolean factible) {
        this.Rmat = Rmat;
        this.poligono = poligono;
        this.puntosFactibles = puntosFactibles;
        this.optimo = optimo;
        this.zOptimo = zOptimo;
        this.esFactible = factible;
        this.hayResultado = true;
        repaint();
    }

    public void limpiar() {
        this.hayResultado = false;
        this.puntosFactibles = null;
        this.pantallaFactibles = null;
        repaint();
    }

    /**
     * Busca si el mouse está sobre alguno de los puntos factibles dibujados
     * (usa las posiciones de pantalla calculadas en el último paintComponent)
     * y arma el texto del cuadro flotante con x1, x2 y Z de ese punto.
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        if (pantallaFactibles == null || puntosFactibles == null) {
            return null;
        }
        int mx = event.getX();
        int my = event.getY();
        for (int i = 0; i < pantallaFactibles.size(); i++) {
            int[] pantalla = pantallaFactibles.get(i);
            double dist = Math.hypot(mx - pantalla[0], my - pantalla[1]);
            if (dist <= 8) {
                double[] datos = puntosFactibles.get(i);
                boolean esOptimo = optimo != null
                        && Math.abs(datos[0] - optimo[0]) < 1e-6
                        && Math.abs(datos[1] - optimo[1]) < 1e-6;
                return String.format(
                        "<html>x1 = %.3f<br>x2 = %.3f<br>Z = %.3f%s</html>",
                        datos[0], datos[1], datos[2],
                        esOptimo ? "<br><b>(óptimo)</b>" : "");
            }
        }
        return null;
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

        // ---- puntos factibles, no óptimos (verde lima) ----
        // Rojo queda reservado en todo el proyecto para "no factible"; un vértice
        // factible pero no óptimo pasa a verde lima, en la misma familia que el
        // óptimo (verde bosque) pero con tono/saturación distintos, no solo brillo.
        pantallaFactibles = new ArrayList<>();
        if (puntosFactibles != null) {
            for (double[] p : puntosFactibles) {
                int px = margen + (int) (p[0] / maxX * plotW);
                int py = h - margen - (int) (p[1] / maxY * plotH);
                pantallaFactibles.add(new int[]{px, py});

                boolean esOptimo = optimo != null
                        && Math.abs(p[0] - optimo[0]) < 1e-6
                        && Math.abs(p[1] - optimo[1]) < 1e-6;
                if (!esOptimo) {
                    g2.setColor(new Color(200, 230, 110));
                    g2.fillOval(px - 4, py - 4, 8, 8);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawOval(px - 4, py - 4, 8, 8);
                }
            }
        }

        // ---- punto óptimo (verde bosque) ----
        if (optimo != null) {
            int px = margen + (int) (optimo[0] / maxX * plotW);
            int py = h - margen - (int) (optimo[1] / maxY * plotH);
            g2.setColor(new Color(40, 150, 70));
            g2.fillOval(px - 6, py - 6, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawOval(px - 6, py - 6, 12, 12);
            g2.drawString(String.format("(%.2f, %.2f)  Z=%.2f", optimo[0], optimo[1], zOptimo), px + 8, py - 8);
        }
    }
}
