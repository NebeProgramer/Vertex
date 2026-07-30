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
 * Método Simplex Dual: se usa cuando el punto de partida ya es "óptimo" en
 * la fila Z (todos los costos reducidos cumplen la condición de óptimo)
 * pero NO es factible porque alguna restricción quedó con RHS negativo
 * (por ejemplo, al escribir directamente "-x1 &lt;= -3" en vez de armar
 * artificiales). En vez de arrancar con holguras/artificiales y pivotear
 * por columnas como el Simplex normal, aquí se pivotea primero por FILAS
 * (la de RHS más negativo) y luego se busca la columna, hasta que todos
 * los RHS queden &gt;= 0.
 *
 * Solo admite restricciones tipo &lt;= (con RHS positivo o negativo). Si
 * hay &gt;= o =, no aplica este método (se necesitarían artificiales, ahí
 * es Big M).
 *
 * @author PC-ANDERSON
 */
public class AlgoritmoDual extends javax.swing.JFrame implements MetodoPL {

    private static final Color COLOR_CAMBIO = new Color(200, 255, 200);
    private static final Color COLOR_PIVOTE_LINEA = new Color(255, 255, 170);
    private static final Color COLOR_PIVOTE_CELDA = new Color(255, 176, 120);
    private static final double EPS = 1e-9;

    private final List<double[][]> tableaus = new ArrayList<>();
    private final List<int[]> basisHistory = new ArrayList<>();
    private final List<String> pasoInfo = new ArrayList<>();
    private final List<Integer> pivotRowHistory = new ArrayList<>();
    private final List<Integer> pivotColHistory = new ArrayList<>();

    private String[] varNames;
    private int numVars;
    private int numSlack;
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

    public AlgoritmoDual() {
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
        setTitle("Simplex Dual");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(712, 460));
        setResizable(false);

        TablaTableau.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        TablaTableau.setEnabled(false);
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(btnAnterior)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPaso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSiguiente))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING))
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
            java.util.logging.Logger.getLogger(AlgoritmoDual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AlgoritmoDual().setVisible(true);
            }
        });
    }

    // ======================================================================
    // LÓGICA DEL SIMPLEX DUAL
    // ======================================================================
    /**
     * @param FO   función objetivo, ej: "3x1+2x2"
     * @param R    restricciones, SOLO tipo &lt;= (RHS puede ser negativo), ej: {"x1+x2<=10", "-x1<=-3"}
     * @param tipo "Max" o "Min"
     */
    public void calculos(String FO, String[] R, String tipo) {
        ParserLP.validarVariablesCompletas(FO, R);
        tableaus.clear();
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
        numSlack = m;
        double[][] A = new double[m][numVars];
        double[] b = new double[m];

        for (int i = 0; i < m; i++) {
            String[] partes = PrincipalPage.parseRestriction(R[i]);
            String lhs = partes[0];
            String op = partes[1];
            String rhs = partes[2];

            if (!"<=".equals(op)) {
                throw new IllegalArgumentException(
                        "El Simplex Dual (esta implementación) solo admite restricciones tipo <=.\n"
                        + "La restricción " + (i + 1) + " (\"" + R[i] + "\") usa '" + op + "'.\n"
                        + "Prueba con Big M para restricciones >= o =.");
            }

            b[i] = parseNum(rhs); // aquí SÍ puede ser negativo, a diferencia de Simplex normal

            Map<Integer, String> coefs = PrincipalPage.parseCoefficientsMap(lhs);
            for (int k = 0; k < numVars; k++) {
                A[i][k] = parseNum(coefs.getOrDefault(ordered.get(k), "0"));
            }
        }

        varNames = new String[numVars + numSlack];
        for (int k = 0; k < numVars; k++) {
            varNames[k] = "x" + ordered.get(k);
        }
        for (int i = 0; i < numSlack; i++) {
            varNames[numVars + i] = "S" + (i + 1);
        }

        int totalCols = numVars + numSlack + 1;
        double[][] T = new double[m + 1][totalCols];
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < numVars; k++) {
                T[i][k] = A[i][k];
            }
            T[i][numVars + i] = 1.0;
            T[i][totalCols - 1] = b[i];
        }

        double[] cInterno = new double[numVars];
        for (int k = 0; k < numVars; k++) {
            cInterno[k] = esMin ? -c[k] : c[k];
        }
        for (int k = 0; k < numVars; k++) {
            T[m][k] = -cInterno[k];
        }

        int[] basis = new int[m];
        for (int i = 0; i < m; i++) {
            basis[i] = numVars + i;
        }

        // Verificar que el punto de partida ya sea "dual factible" (fila Z
        // óptima). Si no lo es, este método no aplica tal cual.
        for (int j = 0; j < numVars + numSlack; j++) {
            if (T[m][j] < -EPS) {
                throw new IllegalArgumentException(
                        "Este problema no arranca dual-factible (la fila Z ya tendría un costo reducido negativo).\n"
                        + "El Simplex Dual solo aplica cuando el punto de partida ya es óptimo en Z y solo falta\n"
                        + "corregir factibilidad (RHS negativos). Prueba con Simplex o Big M.");
            }
        }

        guardarPaso(T, basis, "Tabla inicial (dual-factible, RHS puede ser negativo)", -1, -1);

        int iterMax = 200;
        int iter = 0;
        while (iter++ < iterMax) {
            // fila pivote: la de RHS más negativo
            int pivotRow = -1;
            double masNegativo = -EPS;
            for (int i = 0; i < m; i++) {
                if (T[i][totalCols - 1] < masNegativo) {
                    masNegativo = T[i][totalCols - 1];
                    pivotRow = i;
                }
            }
            if (pivotRow == -1) {
                break; // todos los RHS >= 0 → factible y óptimo
            }

            // columna pivote: razón mínima entre Z[j] y |fila[j]| para fila[j] < 0
            int pivotCol = -1;
            double mejorRatio = Double.MAX_VALUE;
            for (int j = 0; j < numVars + numSlack; j++) {
                if (T[pivotRow][j] < -EPS) {
                    double ratio = T[m][j] / (-T[pivotRow][j]);
                    if (ratio < mejorRatio - EPS) {
                        mejorRatio = ratio;
                        pivotCol = j;
                    }
                }
            }
            if (pivotCol == -1) {
                factible = false;
                break; // no hay forma de corregir esa fila → infactible
            }

            String entra = varNames[pivotCol];
            String sale = varNames[basis[pivotRow]];

            double pivotVal = T[pivotRow][pivotCol];
            for (int j = 0; j < totalCols; j++) {
                T[pivotRow][j] /= pivotVal;
            }
            for (int i = 0; i <= m; i++) {
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

            basis[pivotRow] = pivotCol;
            guardarPaso(T, basis, "Sale " + sale + ", entra " + entra + " (fila pivote " + (pivotRow + 1) + ")", pivotRow, pivotCol);
        }

        solucion = new double[numVars];
        for (int i = 0; i < m; i++) {
            if (basis[i] < numVars) {
                solucion[basis[i]] = T[i][totalCols - 1];
            }
        }
        double zRow = T[m][totalCols - 1];
        zOptimo = esMin ? -zRow : zRow;

        StringBuilder sb = new StringBuilder();
        if (!factible) {
            sb.append("El problema NO tiene solución factible (una fila con RHS negativo no tiene ninguna entrada negativa para corregirla).");
        } else {
            sb.append("Solución óptima:  ");
            for (int k = 0; k < numVars; k++) {
                sb.append(varNames[k]).append(" = ").append(fmt(solucion[k])).append("   ");
            }
            sb.append("Z = ").append(fmt(zOptimo));
        }
        resultadoTexto = sb.toString();

        mostrarPaso(0);
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

    private void guardarPaso(double[][] T, int[] basis, String info, int pivotRow, int pivotCol) {
        double[][] clone = new double[T.length][];
        for (int i = 0; i < T.length; i++) {
            clone[i] = T[i].clone();
        }
        tableaus.add(clone);
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
        int[] basis = basisHistory.get(idx);
        int m = T.length - 1;
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
            datos[m][j + 1] = fmt(T[m][j]);
        }
        datos[m][columnas.length - 1] = fmt(T[m][totalCols - 1]);

        int filasTabla = m + 1;
        int colsTabla = columnas.length;
        int[][] aux = new int[filasTabla][colsTabla];

        if (idx > 0) {
            double[][] anterior = tableaus.get(idx - 1);
            for (int i = 0; i < filasTabla; i++) {
                for (int j = 1; j < colsTabla; j++) {
                    if (Math.abs(T[i][j - 1] - anterior[i][j - 1]) > 1e-6) {
                        aux[i][j] = 1;
                    }
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
