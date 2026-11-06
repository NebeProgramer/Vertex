/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.problema.programacion.lineal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final double EPS = 1e-9;

    // ---- estado del algoritmo ----
    private final List<double[][]> tableros = new ArrayList<>();      // filas de restricciones (planas)
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
    /** Coeficientes de Z tal como los escribió el usuario (fila Cj del tablero). */
    private double[] cUsuario;

    
    /** Nombres a mostrar (xij originales); identidad = "x" + k, como siempre. */
    private com.mycompany.problema.programacion.lineal.parser.MapeoVariables mapeo
            = com.mycompany.problema.programacion.lineal.parser.MapeoVariables.identidad();

    @Override
    public void setNombresVariables(com.mycompany.problema.programacion.lineal.parser.MapeoVariables mapeo) {
        this.mapeo = mapeo;
    }

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
        VentanaPrefs.aplicar(this);
        Recursos.instalarAyudaEnBarraTitulo(this, "Big M",
                "<html><b>Colorimetría — Big M</b><br>&#128992; Naranja: celda pivote<br>&#128993; Amarillo: fila y columna del pivote<br>&#128994; Verde: números afectados por el cambio de variable</html>",
                VentanaAyudaAlgoritmo.TEORIA_BIGM);
        Recursos.envolverEnFondoAzul(this);
        Recursos.modernizarVentana(this);
        // el texto del paso puede ser largo: que se recorte (completo en el tooltip) antes que empujar los botones
        lblPaso.setMinimumSize(new java.awt.Dimension(40, 16));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        TablaTablero = new javax.swing.JTable();
        btnAnterior = new javax.swing.JButton();
        lblPaso = new javax.swing.JLabel();
        btnSiguiente = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtResultado = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Big M");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(712, 460));
        setMinimumSize(new java.awt.Dimension(712, 460));
        setResizable(true);

        TablaTablero.setEnabled(false);
        TablaTablero.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        TablaTablero.setRowHeight(26);
        jScrollPane1.setViewportView(TablaTablero);

        btnAnterior.setText("< Anterior");
        btnAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnteriorActionPerformed(evt);
            }
        });

        lblPaso.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPaso.setForeground(new java.awt.Color(40, 60, 110));
        lblPaso.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPaso.setText("Tablero 1");

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
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
        if (pasoActual < tableros.size() - 1) {
            pasoActual++;
            mostrarPaso(pasoActual);
        }
    }//GEN-LAST:event_btnSiguienteActionPerformed

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();

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
        tableros.clear();
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
            varNames[k] = mapeo.nombreDe(ordered.get(k));
        }

        int totalCols = totalVars + 1; // + RHS
        double[][] T = new double[m][totalCols];
        int[] basis = new int[m];

        // Como en los apuntes: holgura (+s) o exceso (−s) y artificial A
        // llevan el número de su restricción.
        int colLibre = numVars;
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < numVars; k++) {
                T[i][k] = A[i][k];
            }
            T[i][totalCols - 1] = b[i];

            switch (ops[i]) {
                case "<=" -> {
                    int col = colLibre++;
                    varNames[col] = "s" + (i + 1);
                    T[i][col] = 1.0;
                    basis[i] = col;
                }
                case ">=" -> {
                    int colE = colLibre++;
                    varNames[colE] = "s" + (i + 1);
                    T[i][colE] = -1.0;

                    int colA = colLibre++;
                    varNames[colA] = "A" + (i + 1);
                    esArtificial[colA] = true;
                    T[i][colA] = 1.0;
                    basis[i] = colA;
                }
                case "=" -> {
                    int colA = colLibre++;
                    varNames[colA] = "A" + (i + 1);
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

        cUsuario = c;
        guardarPaso(T, zc, zm, basis, "Tablero inicial (base: holguras/artificiales)", -1, -1);

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
        tableros.add(clone);
        zConst.add(zc.clone());
        zM.add(zm.clone());
        basisHistory.add(basis.clone());
        pasoInfo.add(info);
        pivotRowHistory.add(pivotRow);
        pivotColHistory.add(pivotCol);
    }

    /**
     * Dibuja el tablero idx con el formato de los apuntes: fila Cj (las
     * artificiales llevan +M en Min y −M en Max), columnas Cb / Xb / Sol / θ
     * y filas Zj = Σ Cb·aij y Pj = Cj − Zj, todas como "a + bM".
     */
    private void mostrarPaso(int idx) {
        if (tableros.isEmpty()) {
            return;
        }
        double[][] T = tableros.get(idx);
        int[] basis = basisHistory.get(idx);
        int m = T.length;
        int cols = varNames.length;
        int rhs = T[0].length - 1;

        // Cj del usuario separado en (constante, coeficiente de M)
        double[] cjC = new double[cols];
        double[] cjM = new double[cols];
        for (int k = 0; k < numVars; k++) {
            cjC[k] = cUsuario[k];
        }
        for (int j = 0; j < cols; j++) {
            if (esArtificial[j]) {
                cjM[j] = esMin ? 1.0 : -1.0;
            }
        }

        TableroVista.Datos d = new TableroVista.Datos();
        d.variables = varNames.clone();
        d.cj = new String[cols];
        for (int j = 0; j < cols; j++) {
            d.cj[j] = fmtM(cjC[j], cjM[j]);
        }
        d.cb = new String[m];
        d.xb = new String[m];
        d.cuerpo = new String[m][cols];
        d.sol = new String[m];
        double[] zjC = new double[cols];
        double[] zjM = new double[cols];
        double zC = 0, zMp = 0;
        for (int i = 0; i < m; i++) {
            double cbC = cjC[basis[i]];
            double cbM = cjM[basis[i]];
            d.cb[i] = fmtM(cbC, cbM);
            d.xb[i] = varNames[basis[i]];
            for (int j = 0; j < cols; j++) {
                d.cuerpo[i][j] = fmt(T[i][j]);
                zjC[j] += cbC * T[i][j];
                zjM[j] += cbM * T[i][j];
            }
            d.sol[i] = fmt(T[i][rhs]);
            zC += cbC * T[i][rhs];
            zMp += cbM * T[i][rhs];
        }
        d.zj = new String[cols];
        d.pj = new String[cols];
        for (int j = 0; j < cols; j++) {
            d.zj[j] = fmtM(zjC[j], zjM[j]);
            d.pj[j] = fmtM(cjC[j] - zjC[j], cjM[j] - zjM[j]);
        }
        d.z = fmtM(zC, zMp);

        String info;
        if (idx < tableros.size() - 1) {
            int fila = pivotRowHistory.get(idx + 1);
            int col = pivotColHistory.get(idx + 1);
            d.filaPivote = fila;
            d.colPivote = col;
            d.theta = new String[m];
            for (int i = 0; i < m; i++) {
                double a = T[i][col];
                d.theta[i] = a > EPS ? fmt(T[i][rhs] / a) : "—";
            }
            info = "Entra " + varNames[col] + ", sale " + varNames[basis[fila]];
        } else if (!factible) {
            info = "Sin solución factible: una A sigue en la base con valor > 0";
        } else {
            info = esMin ? "Óptimo: todos los Pj ≥ 0" : "Óptimo: todos los Pj ≤ 0";
        }

        if (idx > 0) {
            double[][] anterior = tableros.get(idx - 1);
            d.cambio = new boolean[m][cols + 1];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j <= cols; j++) {
                    d.cambio[i][j] = Math.abs(T[i][j] - anterior[i][j]) > 1e-6;
                }
            }
        }

        TableroVista.mostrar(TablaTablero, d);
        lblPaso.setText("Tablero " + (idx + 1) + " de " + tableros.size() + "  —  " + info);
        lblPaso.setToolTipText(lblPaso.getText());
        btnAnterior.setEnabled(idx > 0);
        btnSiguiente.setEnabled(idx < tableros.size() - 1);

        txtResultado.setText(construirTextoPaso(idx));
    }

    /**
     * Arma el texto de resultado propio del tablero que se está mostrando:
     * solución básica de ese paso, si es factible respecto al problema
     * original (ninguna variable artificial sigue en la base con valor
     * &gt; 0) y si ya es la solución óptima (el último tablero, siempre que
     * además haya resultado factible).
     */
    private String construirTextoPaso(int idx) {
        double[][] T = tableros.get(idx);
        int[] basis = basisHistory.get(idx);
        double[] zc = zConst.get(idx);
        double[] zm = zM.get(idx);
        int m = T.length;
        int totalCols = T[0].length;

        double[] sol = new double[numVars];
        for (int i = 0; i < m; i++) {
            if (basis[i] < numVars) {
                sol[basis[i]] = T[i][totalCols - 1];
            }
        }
        boolean factiblePaso = true;
        for (int i = 0; i < m; i++) {
            if (esArtificial[basis[i]] && T[i][totalCols - 1] > 1e-6) {
                factiblePaso = false;
                break;
            }
        }
        boolean esUltimaTabla = idx == tableros.size() - 1;
        boolean esOptimo = esUltimaTabla && factible;

        StringBuilder sb = new StringBuilder();
        sb.append("Tablero ").append(idx + 1).append(" de ").append(tableros.size()).append(":\n");
        for (int k = 0; k < numVars; k++) {
            sb.append(varNames[k]).append(" = ").append(fmt(sol[k])).append("\n");
        }
        // la fila Z interna es de maximización: en Min se cambia el signo de las dos partes
        double signo = esMin ? -1 : 1;
        sb.append("Z = ").append(fmtM(signo * zc[totalCols - 1], signo * zm[totalCols - 1])).append("\n");
        sb.append("Factible: ").append(factiblePaso ? "Sí" : "No").append("\n");
        if (esUltimaTabla && !factible) {
            sb.append("Óptimo: No — el problema no tiene solución factible");
        } else {
            sb.append("Óptimo: ").append(esOptimo ? "Sí" : "No, aún se puede mejorar");
        }
        return sb.toString();
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
        return com.mycompany.problema.programacion.lineal.algoritmos.FormatoNumero.fmt(v);
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
    private javax.swing.JTable TablaTablero;
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPaso;
    private javax.swing.JTextArea txtResultado;
    // End of variables declaration//GEN-END:variables
}
