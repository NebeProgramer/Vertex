/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.mycompany.problema.programacion.lineal.parser.ParserTransporte;
import com.mycompany.problema.programacion.lineal.algoritmos.MetodoPL;

/**
 * Algoritmo Húngaro. Misma estructura visual que el resto de las ventanas
 * de algoritmo: tabla con los pasos (Anterior/Siguiente, con resaltado de
 * color) arriba, y el resumen de la solución abajo — sin el botón de
 * alternar "Paso a paso"/"Resultados" que tenía antes.
 *
 * @author PC-ANDERSON
 */
public class AlgoritmoHungaro extends javax.swing.JFrame implements MetodoPL {

    private static final Color COLOR_LINEA = new Color(255, 255, 170);   // amarillo: fila/columna cubierta
    private static final Color COLOR_ASIGNADO = new Color(180, 255, 180); // verde: celda asignada (cubierta 2 veces)

    private int numInter = 1;
    private final List<double[][]> matricesPasos = new ArrayList<>();
    private final List<String> nombresPasos = new ArrayList<>();
    private final List<int[][]> matricesAux = new ArrayList<>();

    private int pasoActual = 0;
    private String resultadoTexto;
    private int[][] auxMatActual;

    @Override
    public void resolver(String FO, String[] R, String tipo) {
        calculos(FO, R, tipo);
    }

    @Override
    public void mostrar() {
        setVisible(true);
    }

    public AlgoritmoHungaro() {
        initComponents();
        Recursos.aplicarIcono(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        TablaTableau = new javax.swing.JTable();
        btnAnterior = new javax.swing.JButton();
        lblPaso = new javax.swing.JLabel();
        btnSiguiente = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtResultado = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Algoritmo Húngaro");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(712, 460));
        setResizable(false);

        TablaTableau.setEnabled(false);
        TablaTableau.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        TablaTableau.setRowHeight(26);
        jScrollPane1.setViewportView(TablaTableau);

        btnAnterior.setText("< Anterior");
        btnAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnteriorActionPerformed(evt);
            }
        });

        lblPaso.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPaso.setForeground(new java.awt.Color(40, 60, 110));
        lblPaso.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPaso.setText("Tabla 1");

        btnSiguiente.setText("Siguiente >");
        btnSiguiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSiguienteActionPerformed(evt);
            }
        });

        txtResultado.setEditable(false);
        txtResultado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtResultado.setLineWrap(true);
        txtResultado.setRows(4);
        jScrollPane2.setViewportView(txtResultado);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAnterior)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPaso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSiguiente)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAnterior)
                    .addComponent(lblPaso)
                    .addComponent(btnSiguiente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnteriorActionPerformed
        if (pasoActual > 0) {
            pasoActual--;
            mostrarPaso(pasoActual);
        }
    }//GEN-LAST:event_btnAnteriorActionPerformed

    private void btnSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSiguienteActionPerformed
        if (pasoActual < matricesPasos.size() - 1) {
            pasoActual++;
            mostrarPaso(pasoActual);
        }
    }//GEN-LAST:event_btnSiguienteActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AlgoritmoHungaro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new AlgoritmoHungaro().setVisible(true);
        });
    }

    public void calculos(String FO, String[] R, String Tipo) {
        if (!"Max".equals(Tipo) && !"Min".equals(Tipo)) {
            throw new IllegalArgumentException("El tipo de problema debe ser \"Max\" o \"Min\" (recibido: \"" + Tipo + "\").");
        }

        double[][] Zvalidacion = buildZmat(FO);
        if (Zvalidacion.length == 0 || Zvalidacion[0].length == 0) {
            throw new IllegalArgumentException(
                    "El método Húngaro necesita variables de doble índice, tipo x_ij (ej: x11, x23),\n"
                    + "no variables de una sola posición como x1. Revisa el formato de tu función objetivo.");
        }
        if (Zvalidacion.length != Zvalidacion[0].length) {
            throw new IllegalArgumentException(
                    "El método Húngaro requiere una matriz de costos cuadrada (mismo número de\n"
                    + "orígenes que de destinos). Tu problema tiene " + Zvalidacion.length + " fila(s) y "
                    + Zvalidacion[0].length + " columna(s).");
        }
        ParserTransporte.validarMatrizCompleta(FO);

        matricesPasos.clear();
        nombresPasos.clear();
        matricesAux.clear();
        pasoActual = 0;
        numInter = 1;

        boolean esMax = "Max".equals(Tipo);
        double[][] ZmatOrigin = buildZmat(FO);
        double[][] Zmat = buildZmat(FO);
        matricesPasos.add(cloneMatrix(Zmat));
        nombresPasos.add("Matriz original (Zmat)");
        matricesAux.add(null);

        if (esMax) {
            double M = 0;
            for (double[] fila : Zmat) {
                for (double v : fila) {
                    M = Math.max(M, v);
                }
            }
            for (int i = 0; i < Zmat.length; i++) {
                for (int j = 0; j < Zmat[i].length; j++) {
                    Zmat[i][j] = M - Zmat[i][j];
                }
            }
        }

        Zmat = precalculo(Zmat);
        int[] asignacion = Tableros(Zmat, getInter(), null);

        double total = 0;
        StringBuilder sb = new StringBuilder("Asignación óptima:\n");
        for (int i = 0; i < asignacion.length; i++) {
            int j = asignacion[i];
            double costo = ZmatOrigin[i][j];
            total += costo;
            sb.append("Origen ").append(i + 1).append(" → Destino ").append(j + 1)
                    .append("  (costo ").append(fmt(costo)).append(")\n");
        }
        sb.append("Total = ").append(fmt(total));
        resultadoTexto = sb.toString();

        mostrarPaso(0);
    }

    private double[][] precalculo(double[][] Zmat) {
        for (int i = 0; i < Zmat.length; i++) {
            double menorFila = Zmat[i][0];
            for (int j = 1; j < Zmat[i].length; j++) {
                if (menorFila > Zmat[i][j]) {
                    menorFila = Zmat[i][j];
                }
            }
            for (int j = 0; j < Zmat[i].length; j++) {
                Zmat[i][j] -= menorFila;
            }
        }
        matricesPasos.add(cloneMatrix(Zmat));
        nombresPasos.add("Paso 1: reducción por filas");
        matricesAux.add(null);

        for (int j = 0; j < Zmat[0].length; j++) {
            double menorColumna = Zmat[0][j];
            for (int i = 1; i < Zmat.length; i++) {
                if (Zmat[i][j] < menorColumna) {
                    menorColumna = Zmat[i][j];
                }
            }
            for (int i = 0; i < Zmat.length; i++) {
                Zmat[i][j] -= menorColumna;
            }
        }
        matricesPasos.add(cloneMatrix(Zmat));
        nombresPasos.add("Paso 2: reducción por columnas");
        matricesAux.add(null);

        return Zmat;
    }

    private double[][] buildZmat(String FO) {
        return ParserTransporte.construirMatrizCostos(FO);
    }

    /**
     * Núcleo correcto del método Húngaro sobre la matriz ya reducida
     * (filas y columnas). En cada vuelta:
     *  1) Calcula el matching máximo usando solo aristas de costo 0
     *     (algoritmo de caminos de aumento / Kuhn).
     *  2) Si el matching cubre las n filas, ya es la asignación óptima.
     *  3) Si no, calcula el número mínimo de líneas que cubren todos los
     *     ceros mediante el teorema de König (a partir del matching
     *     máximo, NO con la heurística de "cubrir la fila/columna con
     *     más ceros", que puede fallar).
     *  4) Resta el menor valor no cubierto a las celdas no cubiertas y
     *     lo suma a las celdas cubiertas dos veces (intersección de
     *     línea de fila y línea de columna), y repite.
     */
    public int[] Tableros(double[][] Zmat, int numit, int[][] auxSinUsar) {
        int n = Zmat.length;
        int iteracion = numit;

        while (true) {
            int[] matchRow = new int[n];
            Arrays.fill(matchRow, -1);
            int[] matchCol = new int[n];
            Arrays.fill(matchCol, -1);

            for (int i = 0; i < n; i++) {
                boolean[] visitada = new boolean[n];
                intentarAsignar(i, Zmat, visitada, matchCol, matchRow);
            }

            int asignados = 0;
            for (int i = 0; i < n; i++) {
                if (matchRow[i] != -1) {
                    asignados++;
                }
            }

            boolean[] filaMarcada = new boolean[n];
            boolean[] colMarcada = new boolean[n];
            java.util.Deque<Integer> pendientes = new java.util.ArrayDeque<>();
            for (int i = 0; i < n; i++) {
                if (matchRow[i] == -1) {
                    filaMarcada[i] = true;
                    pendientes.push(i);
                }
            }
            while (!pendientes.isEmpty()) {
                int i = pendientes.pop();
                for (int j = 0; j < n; j++) {
                    if (Zmat[i][j] == 0 && !colMarcada[j]) {
                        colMarcada[j] = true;
                        int filaAsociada = matchCol[j];
                        if (filaAsociada != -1 && !filaMarcada[filaAsociada]) {
                            filaMarcada[filaAsociada] = true;
                            pendientes.push(filaAsociada);
                        }
                    }
                }
            }
            boolean[] filaCubierta = new boolean[n];
            for (int i = 0; i < n; i++) {
                filaCubierta[i] = !filaMarcada[i];
            }
            boolean[] colCubierta = colMarcada;

            int[][] aux = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    aux[i][j] = (filaCubierta[i] ? 1 : 0) + (colCubierta[j] ? 1 : 0);
                }
            }
            matricesPasos.add(cloneMatrix(Zmat));
            nombresPasos.add("Tablero " + iteracion + " (líneas mínimas de cobertura, asignados " + asignados + "/" + n + ")");
            matricesAux.add(aux);

            if (asignados == n) {
                return matchRow;
            }

            double menorNoCubierto = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (!filaCubierta[i] && !colCubierta[j] && Zmat[i][j] < menorNoCubierto) {
                        menorNoCubierto = Zmat[i][j];
                    }
                }
            }
            if (menorNoCubierto == Double.MAX_VALUE) {
                menorNoCubierto = 0;
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (!filaCubierta[i] && !colCubierta[j]) {
                        Zmat[i][j] -= menorNoCubierto;
                    } else if (filaCubierta[i] && colCubierta[j]) {
                        Zmat[i][j] += menorNoCubierto;
                    }
                }
            }

            iteracion++;
            setInter(iteracion);
            matricesPasos.add(cloneMatrix(Zmat));
            nombresPasos.add("Ajuste de la iteración " + (iteracion - 1));
            matricesAux.add(null);
        }
    }

    /**
     * Busca un camino de aumento desde la fila dada usando solo aristas de
     * costo 0 (algoritmo de Kuhn para matching bipartito máximo).
     */
    private boolean intentarAsignar(int fila, double[][] Zmat, boolean[] colVisitada, int[] matchCol, int[] matchRow) {
        int n = Zmat[0].length;
        for (int j = 0; j < n; j++) {
            if (Zmat[fila][j] == 0 && !colVisitada[j]) {
                colVisitada[j] = true;
                if (matchCol[j] == -1 || intentarAsignar(matchCol[j], Zmat, colVisitada, matchCol, matchRow)) {
                    matchCol[j] = fila;
                    matchRow[fila] = j;
                    return true;
                }
            }
        }
        return false;
    }

    private void mostrarPaso(int idx) {
        if (matricesPasos.isEmpty()) {
            return;
        }
        double[][] mat = matricesPasos.get(idx);
        String titulo = nombresPasos.get(idx);
        int[][] auxMat = (idx < matricesAux.size()) ? matricesAux.get(idx) : null;
        auxMatActual = auxMat;

        int columnas = mat[0].length;
        String[] nombresColumnas = new String[columnas];
        for (int j = 0; j < columnas; j++) {
            nombresColumnas[j] = "Destino " + (j + 1);
        }

        Object[][] datos = new Object[mat.length][columnas];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < columnas; j++) {
                datos[i][j] = fmt(mat[i][j]);
            }
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, nombresColumnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TablaTableau.setModel(modelo);

        String textoPaso = "Tabla " + (idx + 1) + " de " + matricesPasos.size() + (titulo != null ? "  —  " + titulo : "");
        lblPaso.setText("<html><div style='text-align:center;width:350px'>" + textoPaso + "</div></html>");
        btnAnterior.setEnabled(idx > 0);
        btnSiguiente.setEnabled(idx < matricesPasos.size() - 1);

        if (resultadoTexto != null) {
            txtResultado.setText(resultadoTexto);
        }
        TablaTableau.repaint();
    }

    private double[][] cloneMatrix(double[][] original) {
        double[][] copy = new double[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    private String fmt(double v) {
        if (Math.abs(v - Math.round(v)) < 1e-6) {
            return String.valueOf(Math.round(v));
        }
        return String.format("%.2f", v);
    }

    private int getInter() {
        return numInter;
    }

    private void setInter(int inter) {
        numInter = inter;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TablaTableau;
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPaso;
    private javax.swing.JTextArea txtResultado;
    // End of variables declaration//GEN-END:variables
}
