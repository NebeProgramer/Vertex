/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.mycompany.problema.programacion.lineal.algoritmos.MetodoPL;
import com.mycompany.problema.programacion.lineal.parser.ParserLP;

/**
 * Método Big M: igual que el Simplex tabular, pero permite restricciones
 * &gt;= y = agregando variables artificiales penalizadas con una M
 * simbólica muy grande en la función objetivo (para forzarlas a salir de
 * la base). La M nunca se reemplaza por un número: cada celda de la fila Z
 * se representa como el par (constante, coeficiente de M) para que el
 * resultado sea exacto, no dependa de "qué tan grande" se escoja M.
 *
 * @author PC-ANDERSON
 */
public class MetodoBigM extends javax.swing.JFrame implements MetodoPL {

    private static final Color COLOR_CAMBIO = new Color(200, 255, 200);
    private static final Color COLOR_PIVOTE_LINEA = new Color(255, 255, 170);
    private static final Color COLOR_PIVOTE_CELDA = new Color(255, 176, 120);
    private static final double EPS = 1e-9;

    // ---- estado del algoritmo ----
    private final List<double[][]> tableaus = new ArrayList<>();      // filas de restricciones (planas)
    private final List<double[]> zConst = new ArrayList<>();          // parte constante de la fila Z por paso
    private final List<double[]> zM = new ArrayList<>();              // coeficiente de M de la fila Z por paso
    private final List<int[]> basisHistory = new ArrayList<>();
    private final List<String> pasoInfo = new ArrayList<>();
    private final List<Integer> pivotRowHistory = new ArrayList<>();
    private final List<Integer> pivotColHistory = new ArrayList<>();

    private String[] varNames;
    private boolean[] esArtificial;
    private int numVars;
    private int pasoActual = 0;
    private boolean esMin = false;
    private boolean factible = true;
    private double zOptimo;
    private double[] solucion;
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

    public MetodoBigM() {
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
        setTitle("Big M");
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
        txtResultado.setRows(3);
        jScrollPane2.setViewportView(txtResultado);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2)
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
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
        if (pasoActual < tableaus.size() - 1) {
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
            java.util.logging.Logger.getLogger(MetodoBigM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MetodoBigM().setVisible(true);
            }
        });
    }

    // ======================================================================
    // LÓGICA DEL MÉTODO BIG M
    // ======================================================================
    /**
     * @param FO   función objetivo, ej: "3x1+5x2"
     * @param R    restricciones, admite &lt;=, &gt;= y =, ej: {"x1+x2&gt;=2", "2x1+x2&lt;=10"}
     * @param tipo "Max" o "Min"
     */
    public void calculos(String FO, String[] R, String tipo) {
        ParserLP.validarVariablesCompletas(FO, R);
        tableaus.clear();
        zConst.clear();
        zM.clear();
        basisHistory.clear();
        pasoInfo.clear();
        pivotRowHistory.clear();
        pivotColHistory.clear();
        pasoActual = 0;
        factible = true;
        esMin = tipo != null && tipo.trim().equalsIgnoreCase("Min");

        String foLimpia = FO.replaceAll("\\s+", "");

        Pattern varPattern = Pattern.compile("x(\\d+)", Pattern.CASE_INSENSITIVE);
        TreeSet<Integer> idxs = new TreeSet<>();
        Matcher mfo = varPattern.matcher(foLimpia);
        while (mfo.find()) {
            idxs.add(Integer.parseInt(mfo.group(1)));
        }
        if (idxs.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron variables (x1, x2, ...) en la función objetivo.");
        }
        List<Integer> ordered = new ArrayList<>(idxs);
        numVars = ordered.size();

        Map<Integer, String> foCoefs = PrincipalPage.parseCoefficientsMap(foLimpia);
        double[] c = new double[numVars];
        for (int k = 0; k < numVars; k++) {
            c[k] = parseNum(foCoefs.getOrDefault(ordered.get(k), "0"));
        }

        int m = R.length;

        // ---- 1) parsear restricciones, normalizando RHS >= 0 ----
        double[][] A = new double[m][numVars];
        double[] b = new double[m];
        String[] ops = new String[m];

        for (int i = 0; i < m; i++) {
            String[] partes = PrincipalPage.parseRestriction(R[i]);
            Map<Integer, String> coefs = PrincipalPage.parseCoefficientsMap(partes[0]);
            double[] fila = new double[numVars];
            for (int k = 0; k < numVars; k++) {
                fila[k] = parseNum(coefs.getOrDefault(ordered.get(k), "0"));
            }
            String op = partes[1];
            double rhsVal = parseNum(partes[2]);

            if (rhsVal < 0) {
                for (int k = 0; k < numVars; k++) {
                    fila[k] = -fila[k];
                }
                rhsVal = -rhsVal;
                if (op.equals("<=")) {
                    op = ">=";
                } else if (op.equals(">=")) {
                    op = "<=";
                }
            }
            A[i] = fila;
            b[i] = rhsVal;
            ops[i] = op;
        }

        // ---- 2) contar columnas extra (holguras, excesos, artificiales) ----
        int holguras = 0, excesos = 0, artificiales = 0;
        for (String op : ops) {
            switch (op) {
                case "<=" -> holguras++;
                case ">=" -> { excesos++; artificiales++; }
                case "=" -> artificiales++;
                default -> throw new IllegalArgumentException("Operador de restricción no reconocido: " + op);
            }
        }

        int totalVars = numVars + holguras + excesos + artificiales;
        varNames = new String[totalVars];
        esArtificial = new boolean[totalVars];
        for (int k = 0; k < numVars; k++) {
            varNames[k] = "x" + ordered.get(k);
        }

        int totalCols = totalVars + 1; // + RHS
        double[][] T = new double[m][totalCols];
        int[] basis = new int[m];

        int colLibre = numVars;
        int sCount = 0, eCount = 0, aCount = 0;
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < numVars; k++) {
                T[i][k] = A[i][k];
            }
            T[i][totalCols - 1] = b[i];

            switch (ops[i]) {
                case "<=" -> {
                    sCount++;
                    int col = colLibre++;
                    varNames[col] = "S" + sCount;
                    T[i][col] = 1.0;
                    basis[i] = col;
                }
                case ">=" -> {
                    eCount++;
                    int colE = colLibre++;
                    varNames[colE] = "E" + eCount;
                    T[i][colE] = -1.0;

                    aCount++;
                    int colA = colLibre++;
                    varNames[colA] = "A" + aCount;
                    esArtificial[colA] = true;
                    T[i][colA] = 1.0;
                    basis[i] = colA;
                }
                case "=" -> {
                    aCount++;
                    int colA = colLibre++;
                    varNames[colA] = "A" + aCount;
                    esArtificial[colA] = true;
                    T[i][colA] = 1.0;
                    basis[i] = colA;
                }
            }
        }

        // ---- 3) fila Z simbólica (constante, coeficiente de M) ----
        double[] zc = new double[totalVars + 1]; // + RHS
        double[] zm = new double[totalVars + 1];
        for (int k = 0; k < numVars; k++) {
            double cInterno = esMin ? -c[k] : c[k];
            zc[k] = -cInterno;
        }
        for (int j = 0; j < totalVars; j++) {
            if (esArtificial[j]) {
                zm[j] = 1.0; // penalización +M en forma reducida (-cInterno = -(-M) = M)
            }
        }

        // Eliminar el coeficiente de M bajo cada artificial que está en la base
        // (equivale a un pivoteo "gratis" para dejar la fila Z en términos
        // de costos reducidos respecto a la base inicial).
        for (int i = 0; i < m; i++) {
            if (esArtificial[basis[i]]) {
                for (int j = 0; j < totalCols; j++) {
                    zm[j] -= T[i][j];
                }
            }
        }

        guardarPaso(T, zc, zm, basis, "Tabla inicial (base: holguras/artificiales)", -1, -1);

        // ---- 4) iterar ----
        int iterMax = 300;
        int iter = 0;
        while (iter++ < iterMax) {
            int pivotCol = mejorColumna(zc, zm, totalVars);
            if (pivotCol == -1) {
                break; // óptimo alcanzado
            }

            int pivotRow = -1;
            double mejorRatio = Double.MAX_VALUE;
            for (int i = 0; i < m; i++) {
                if (T[i][pivotCol] > EPS) {
                    double ratio = T[i][totalCols - 1] / T[i][pivotCol];
                    if (ratio < mejorRatio - EPS) {
                        mejorRatio = ratio;
                        pivotRow = i;
                    }
                }
            }
            if (pivotRow == -1) {
                throw new IllegalArgumentException("El problema no tiene solución acotada (unbounded).");
            }

            String entra = varNames[pivotCol];
            String sale = varNames[basis[pivotRow]];

            double pivotVal = T[pivotRow][pivotCol];
            for (int j = 0; j < totalCols; j++) {
                T[pivotRow][j] /= pivotVal;
            }
            for (int i = 0; i < m; i++) {
                if (i == pivotRow) {
                    continue;
                }
                double factor = T[i][pivotCol];
                if (Math.abs(factor) > 1e-12) {
                    for (int j = 0; j < totalCols; j++) {
                        T[i][j] -= factor * T[pivotRow][j];
                    }
                }
            }
            double factorC = zc[pivotCol];
            double factorM = zm[pivotCol];
            for (int j = 0; j < totalCols; j++) {
                zc[j] -= factorC * T[pivotRow][j];
                zm[j] -= factorM * T[pivotRow][j];
            }

            basis[pivotRow] = pivotCol;
            guardarPaso(T, zc, zm, basis, "Entra " + entra + ", sale " + sale + " (fila pivote " + (pivotRow + 1) + ")", pivotRow, pivotCol);
        }

        // ---- 5) extraer solución ----
        solucion = new double[numVars];
        for (int i = 0; i < m; i++) {
            if (basis[i] < numVars) {
                solucion[basis[i]] = T[i][totalCols - 1];
            }
        }
        // ¿alguna artificial quedó en la base con valor > 0? -> infactible
        for (int i = 0; i < m; i++) {
            if (esArtificial[basis[i]] && T[i][totalCols - 1] > 1e-6) {
                factible = false;
            }
        }

        double zRowConst = zc[totalCols - 1];
        zOptimo = esMin ? -zRowConst : zRowConst;

        StringBuilder sb = new StringBuilder();
        if (!factible) {
            sb.append("El problema NO tiene solución factible (queda una variable artificial en la base con valor > 0).");
        } else {
            sb.append("Solución óptima:  \n");
            for (int k = 0; k < numVars; k++) {
                sb.append(varNames[k]).append(" = ").append(fmt(solucion[k])).append("\n");
            }
            sb.append("Z = ").append(fmt(zOptimo));
        }
        resultadoTexto = sb.toString();

        mostrarPaso(0);
    }

    /**
     * Busca la columna con el costo reducido más negativo, comparando
     * primero por el coeficiente de M (M se trata como "infinitamente
     * grande") y solo si empatan por la parte constante.
     */
    private int mejorColumna(double[] zc, double[] zm, int totalVars) {
        int mejor = -1;
        double mejorM = -EPS;
        double mejorC = -EPS;
        for (int j = 0; j < totalVars; j++) {
            boolean mejorQue;
            if (zm[j] < mejorM - EPS) {
                mejorQue = true;
            } else if (Math.abs(zm[j] - mejorM) <= EPS && zc[j] < mejorC - EPS) {
                mejorQue = true;
            } else {
                mejorQue = false;
            }
            boolean negativo = (zm[j] < -EPS) || (Math.abs(zm[j]) <= EPS && zc[j] < -EPS);
            if (negativo && (mejor == -1 || mejorQue)) {
                mejor = j;
                mejorM = zm[j];
                mejorC = zc[j];
            }
        }
        return mejor;
    }

    public double getZOptimo() {
        return zOptimo;
    }

    public double[] getSolucion() {
        return solucion;
    }

    public boolean isFactible() {
        return factible;
    }

    private void guardarPaso(double[][] T, double[] zc, double[] zm, int[] basis, String info, int pivotRow, int pivotCol) {
        double[][] clone = new double[T.length][];
        for (int i = 0; i < T.length; i++) {
            clone[i] = T[i].clone();
        }
        tableaus.add(clone);
        zConst.add(zc.clone());
        zM.add(zm.clone());
        basisHistory.add(basis.clone());
        pasoInfo.add(info);
        pivotRowHistory.add(pivotRow);
        pivotColHistory.add(pivotCol);
    }

    private void mostrarPaso(int idx) {
        if (tableaus.isEmpty()) {
            return;
        }
        double[][] T = tableaus.get(idx);
        double[] zc = zConst.get(idx);
        double[] zm = zM.get(idx);
        int[] basis = basisHistory.get(idx);
        int m = T.length;
        int totalCols = T[0].length;

        String[] columnas = new String[varNames.length + 2];
        columnas[0] = "Base";
        for (int j = 0; j < varNames.length; j++) {
            columnas[j + 1] = varNames[j];
        }
        columnas[columnas.length - 1] = "RHS";

        Object[][] datos = new Object[m + 1][columnas.length];
        for (int i = 0; i < m; i++) {
            datos[i][0] = varNames[basis[i]];
            for (int j = 0; j < totalCols - 1; j++) {
                datos[i][j + 1] = fmt(T[i][j]);
            }
            datos[i][columnas.length - 1] = fmt(T[i][totalCols - 1]);
        }
        datos[m][0] = "Z";
        for (int j = 0; j < totalCols - 1; j++) {
            datos[m][j + 1] = fmtM(zc[j], zm[j]);
        }
        datos[m][columnas.length - 1] = fmtM(zc[totalCols - 1], zm[totalCols - 1]);

        // ---- matriz auxiliar de colores ----
        int filasTabla = m + 1;
        int colsTabla = columnas.length;
        int[][] aux = new int[filasTabla][colsTabla];

        if (idx > 0) {
            double[][] anterior = tableaus.get(idx - 1);
            double[] zcAnt = zConst.get(idx - 1);
            double[] zmAnt = zM.get(idx - 1);
            for (int i = 0; i < m; i++) {
                for (int j = 1; j < colsTabla; j++) {
                    if (Math.abs(T[i][j - 1] - anterior[i][j - 1]) > 1e-6) {
                        aux[i][j] = 1;
                    }
                }
            }
            for (int j = 1; j < colsTabla; j++) {
                if (Math.abs(zc[j - 1] - zcAnt[j - 1]) > 1e-6 || Math.abs(zm[j - 1] - zmAnt[j - 1]) > 1e-6) {
                    aux[m][j] = 1;
                }
            }
        }

        int pRow = pivotRowHistory.get(idx);
        int pCol = pivotColHistory.get(idx);
        if (pRow >= 0 && pCol >= 0) {
            for (int j = 0; j < colsTabla; j++) {
                aux[pRow][j] = 2;
            }
            for (int i = 0; i < filasTabla; i++) {
                if (aux[i][pCol + 1] != 3) {
                    aux[i][pCol + 1] = 2;
                }
            }
            aux[pRow][pCol + 1] = 3;
        }
        auxMatActual = aux;

        DefaultTableModel model = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TablaTableau.setModel(model);

        String info = pasoInfo.get(idx);
        lblPaso.setText("Tabla " + (idx + 1) + " de " + tableaus.size() + (info != null ? "  —  " + info : ""));
        btnAnterior.setEnabled(idx > 0);
        btnSiguiente.setEnabled(idx < tableaus.size() - 1);

        if (resultadoTexto != null) {
            txtResultado.setText(resultadoTexto);
        }
        TablaTableau.repaint();
    }

    private double parseNum(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String fmt(double v) {
        if (Math.abs(v - Math.round(v)) < 1e-6) {
            return String.valueOf(Math.round(v));
        }
        return String.format("%.3f", v);
    }

    /** Formatea una celda de la fila Z mostrando el término de M cuando aplica, ej: "5 + 2M" o "-3M". */
    private String fmtM(double constPart, double mPart) {
        if (Math.abs(mPart) < 1e-9) {
            return fmt(constPart);
        }
        String mTexto = (Math.abs(Math.abs(mPart) - 1.0) < 1e-9) ? "M" : fmt(Math.abs(mPart)) + "M";
        String signoM = mPart < 0 ? "-" : "+";
        if (Math.abs(constPart) < 1e-9) {
            return (mPart < 0 ? "-" : "") + mTexto;
        }
        return fmt(constPart) + " " + signoM + " " + mTexto;
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
