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
 * Método Simplex Dual: se usa cuando el punto de partida ya es "óptimo" en
 * la fila Z (todos los costos reducidos cumplen la condición de óptimo)
 * pero NO es factible porque alguna restricción quedó con RHS negativo
 * (por ejemplo, al escribir directamente "-x1 &lt;= -3" en vez de armar
 * artificiales). En vez de arrancar con holguras/artificiales y pivotear
 * por columnas como el Simplex normal, aquí se pivotea primero por FILAS
 * (la de RHS más negativo) y luego se busca la columna, hasta que todos
 * los RHS queden &gt;= 0.
 *
 * Admite restricciones tipo &lt;= (con RHS positivo o negativo) y &gt;=,
 * que se multiplican por -1 para quedar como &lt;= con RHS negativo (el
 * caso típico del método: minimizar con costos positivos y restricciones
 * &gt;=). Con =, no aplica (se necesitarían artificiales, ahí es Big M).
 *
 * @author PC-ANDERSON
 */
public class AlgoritmoDual extends javax.swing.JFrame implements MetodoPL {

    private static final double EPS = 1e-9;

    private final List<double[][]> tableros = new ArrayList<>();
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

    public AlgoritmoDual() {
        initComponents();
        Recursos.aplicarIcono(this);
        VentanaPrefs.aplicar(this);
        Recursos.instalarAyudaEnBarraTitulo(this, "Simplex Dual",
                "<html><b>Colorimetría — Simplex Dual</b><br>&#128992; Naranja: celda pivote<br>&#128993; Amarillo: fila y columna del pivote<br>&#128994; Verde: números afectados por el cambio de variable</html>",
                Dualidad.teoria(VentanaAyudaAlgoritmo.TEORIA_DUAL, VentanaAyudaAlgoritmo.TEORIA_DUAL_DUAL));
        Recursos.agregarBotonBarraTitulo(this, "Sensibilidad",
                "Precios sombra, holguras y rangos de la solución óptima", this::mostrarSensibilidad);
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
        setTitle("Simplex Dual");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(712, 460));
        setMinimumSize(new java.awt.Dimension(712, 460));
        setResizable(true);

        TablaTablero.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        TablaTablero.setEnabled(false);
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
        tableros.clear();
        basisHistory.clear();
        pasoInfo.clear();
        pivotRowHistory.clear();
        pivotColHistory.clear();
        pasoActual = 0;
        factible = true;
        sensBase = null;
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
        boolean[] negada = new boolean[m]; // las >= que se multiplicaron por −1 (para la sensibilidad)

        for (int i = 0; i < m; i++) {
            String[] partes = PrincipalPage.parseRestriction(R[i]);
            String lhs = partes[0];
            String op = partes[1];
            String rhs = partes[2];

            if ("=".equals(op)) {
                throw new IllegalArgumentException(
                        "El Simplex Dual (esta implementación) admite restricciones <= y >=, pero no =.\n"
                        + "La restricción " + (i + 1) + " (\"" + R[i] + "\") usa '='.\n"
                        + "Prueba con Big M para restricciones de igualdad.");
            }
            // a·x >= b se multiplica por -1 y queda -a·x <= -b: el lado derecho
            // negativo es justo lo que corrige el Simplex Dual (caso típico:
            // minimizar con costos positivos y restricciones >=)
            double signo = ">=".equals(op) ? -1.0 : 1.0;
            negada[i] = ">=".equals(op);

            b[i] = signo * parseNum(rhs); // aquí SÍ puede ser negativo, a diferencia de Simplex normal

            Map<Integer, String> coefs = PrincipalPage.parseCoefficientsMap(lhs);
            for (int k = 0; k < numVars; k++) {
                A[i][k] = signo * parseNum(coefs.getOrDefault(ordered.get(k), "0"));
            }
        }

        varNames = new String[numVars + numSlack];
        for (int k = 0; k < numVars; k++) {
            varNames[k] = mapeo.nombreDe(ordered.get(k));
        }
        for (int i = 0; i < numSlack; i++) {
            varNames[numVars + i] = "s" + (i + 1);
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

        cUsuario = c;
        guardarPaso(T, basis, "Tablero inicial (dual-factible, RHS puede ser negativo)", -1, -1);

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

        // datos para el análisis de sensibilidad: la base óptima de la forma interna (todo <=, con
        // las >= multiplicadas por −1); solo si se llegó a una solución factible y óptima
        if (factible) {
            sensC = c;
            sensA = A;
            sensB = b;
            sensNegada = negada;
            sensBase = basis.clone();
        }

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

    // ---- análisis de sensibilidad ----
    private double[] sensC;
    private double[][] sensA;
    private double[] sensB;
    private boolean[] sensNegada;
    private int[] sensBase;

    /**
     * Abre las tablas de sensibilidad de la solución óptima (botón de la
     * barra de título), igual que en el Simplex. Las restricciones se
     * muestran como las escribió el usuario: las &gt;= no salen con el signo
     * cambiado (ver AnalisisSensibilidad.calcular con filas negadas).
     */
    private void mostrarSensibilidad() {
        if (sensBase == null) {
            javax.swing.JOptionPane.showMessageDialog(this, factible
                    ? "Todavía no hay una solución óptima que analizar."
                    : "El problema no tiene solución factible: no hay nada que analizar.",
                    "Sensibilidad", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        com.mycompany.problema.programacion.lineal.algoritmos.AnalisisSensibilidad s
                = com.mycompany.problema.programacion.lineal.algoritmos.AnalisisSensibilidad.calcular(
                        sensC, sensA, sensB, esMin, sensBase, sensNegada);
        String[] restricciones = new String[sensB.length];
        for (int i = 0; i < restricciones.length; i++) {
            restricciones[i] = "R" + (i + 1);
        }
        new VentanaSensibilidad(this, "Simplex Dual", s, java.util.Arrays.copyOf(varNames, numVars), restricciones)
                .setVisible(true);
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
        tableros.add(clone);
        basisHistory.add(basis.clone());
        pasoInfo.add(info);
        pivotRowHistory.add(pivotRow);
        pivotColHistory.add(pivotCol);
    }

    /**
     * Dibuja el tablero idx con el formato de los apuntes (Cj, Cb, Xb, Sol,
     * Zj, Pj = Cj − Zj). En el Dual primero se elige la fila (la de Sol más
     * negativa) y luego la columna, con la razón |Pj / a_rj| de las
     * columnas con a_rj &lt; 0: esa razón va en una fila extra bajo Pj.
     */
    private void mostrarPaso(int idx) {
        if (tableros.isEmpty()) {
            return;
        }
        double[][] T = tableros.get(idx);
        int[] basis = basisHistory.get(idx);
        int m = T.length - 1;
        int cols = varNames.length;
        int rhs = T[0].length - 1;

        double[] cj = new double[cols];
        for (int k = 0; k < numVars; k++) {
            cj[k] = cUsuario[k];
        }

        TableroVista.Datos d = new TableroVista.Datos();
        d.variables = varNames.clone();
        d.conTheta = false;
        d.cj = new String[cols];
        for (int j = 0; j < cols; j++) {
            d.cj[j] = fmt(cj[j]);
        }
        d.cb = new String[m];
        d.xb = new String[m];
        d.cuerpo = new String[m][cols];
        d.sol = new String[m];
        double[] zj = new double[cols];
        double z = 0;
        for (int i = 0; i < m; i++) {
            double cb = cj[basis[i]];
            d.cb[i] = fmt(cb);
            d.xb[i] = varNames[basis[i]];
            for (int j = 0; j < cols; j++) {
                d.cuerpo[i][j] = fmt(T[i][j]);
                zj[j] += cb * T[i][j];
            }
            d.sol[i] = fmt(T[i][rhs]);
            z += cb * T[i][rhs];
        }
        double[] pj = new double[cols];
        d.zj = new String[cols];
        d.pj = new String[cols];
        for (int j = 0; j < cols; j++) {
            pj[j] = cj[j] - zj[j];
            d.zj[j] = fmt(zj[j]);
            d.pj[j] = fmt(pj[j]);
        }
        d.z = fmt(z);

        String info;
        if (idx < tableros.size() - 1) {
            int fila = pivotRowHistory.get(idx + 1);
            int col = pivotColHistory.get(idx + 1);
            d.filaPivote = fila;
            d.colPivote = col;
            d.etiquetaExtra = "|Pj/a|";
            d.filaExtra = new String[cols];
            for (int j = 0; j < cols; j++) {
                double a = T[fila][j];
                d.filaExtra[j] = a < -EPS ? fmt(Math.abs(pj[j] / a)) : "—";
            }
            info = "Sale " + varNames[basis[fila]] + ", entra " + varNames[col];
        } else if (!factible) {
            info = "Sin solución factible: la fila con Sol < 0 no tiene ningún a < 0";
        } else {
            info = "Óptimo: todas las Sol ≥ 0";
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
     * solución básica de ese paso, si es factible en ese punto (todos los
     * RHS &gt;= 0 — en Simplex Dual esto NO se cumple hasta el final) y si
     * ya es la solución óptima (el último tablero, y solo si el problema
     * resultó factible).
     */
    private String construirTextoPaso(int idx) {
        double[][] T = tableros.get(idx);
        int[] basis = basisHistory.get(idx);
        int m = T.length - 1;
        int totalCols = T[0].length;

        double[] sol = new double[numVars];
        for (int i = 0; i < m; i++) {
            if (basis[i] < numVars) {
                sol[basis[i]] = T[i][totalCols - 1];
            }
        }
        double zRow = T[m][totalCols - 1];
        double zPaso = esMin ? -zRow : zRow;

        boolean factiblePaso = true;
        for (int i = 0; i < m; i++) {
            if (T[i][totalCols - 1] < -1e-6) {
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
        sb.append("Z = ").append(fmt(zPaso)).append("\n");
        // en el Simplex Dual los Pj son óptimos desde el inicio: lo que falta es la factibilidad
        sb.append("Factible: ").append(factiblePaso ? "Sí" : "No — ∃ Sol < 0").append("\n");
        if (esUltimaTabla && !factible) {
            sb.append("Óptimo: No — el problema no tiene solución factible");
        } else {
            sb.append("Óptimo: ").append(esOptimo ? "Sí"
                    : "No — los Pj ya cumplen (" + (esMin ? "todos ≥ 0" : "todos ≤ 0") + "), pero ∃ Sol < 0");
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
