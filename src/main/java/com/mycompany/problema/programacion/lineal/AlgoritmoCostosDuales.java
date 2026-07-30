/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.mycompany.problema.programacion.lineal.parser.ParserTransporte;
import com.mycompany.problema.programacion.lineal.algoritmos.MetodoPL;

/**
 * Método de Costos Duales (MODI / distribución modificada) para problemas
 * de transporte:
 *  1) Vogel (VAM) para la solución básica inicial (ya lo tenías).
 *  2) Calcular u_i, v_j resolviendo u_i + v_j = costo_ij sobre las celdas
 *     básicas (árbol de expansión filas+columnas).
 *  3) Costo de oportunidad de cada celda no básica: costo_ij - u_i - v_j.
 *     Si todos son &gt;= 0, ya es óptimo.
 *  4) Si no, entra la celda más negativa; se busca su ciclo cerrado
 *     (stepping-stone) sobre celdas básicas, se le da +/- alternado, y se
 *     reasigna la cantidad mínima entre las celdas "-".
 *
 * Si oferta total != demanda total, se agrega automáticamente una fila u
 * columna ficticia con costo 0 para balancear.
 *
 * @author PC-ANDERSON
 */
public class AlgoritmoCostosDuales extends javax.swing.JFrame implements MetodoPL {

    private static final Color COLOR_CAMBIO = new Color(200, 255, 200);
    private static final Color COLOR_ENTRA = new Color(255, 176, 120);
    private static final Color COLOR_MAS = new Color(200, 255, 200);
    private static final Color COLOR_MENOS = new Color(255, 190, 190);
    private static final double EPS = 1e-6;

    private final List<double[][]> pasosAsigna = new ArrayList<>();
    private final List<String> pasoInfo = new ArrayList<>();
    private final List<int[][]> pasoResaltado = new ArrayList<>(); // 0 normal, 1 cambió, 2 entra, 3 '+', 4 '-'
    private int pasoActual = 0;

    private double[][] costoReal; // matriz de costos original (con fila/columna ficticia si se agregó)
    private String[] nombresFilas;
    private String[] nombresColumnas;
    private boolean factible = true;
    private double zOptimo;
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

    public AlgoritmoCostosDuales() {
        initComponents();
        Recursos.aplicarIcono(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        TablaTransporte = new javax.swing.JTable();
        btnAnterior = new javax.swing.JButton();
        lblPaso = new javax.swing.JLabel();
        btnSiguiente = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtResultado = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Costos Duales (MODI)");
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        TablaTransporte.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        TablaTransporte.setEnabled(false);
        TablaTransporte.setRowHeight(26);
        jScrollPane1.setViewportView(TablaTransporte);

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
        txtResultado.setRows(6);
        jScrollPane2.setViewportView(txtResultado);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAnterior)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPaso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSiguiente))
                    .addComponent(jScrollPane2))
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
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
        if (pasoActual < pasosAsigna.size() - 1) {
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
            java.util.logging.Logger.getLogger(AlgoritmoCostosDuales.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AlgoritmoCostosDuales().setVisible(true);
            }
        });
    }

    public void calculos(String FO, String[] R, String Tipo) {
        boolean esMax = "Max".equalsIgnoreCase(Tipo);
        if (!esMax && !"Min".equalsIgnoreCase(Tipo)) {
            throw new IllegalArgumentException("El tipo de problema no es Maximización ni Minimización.");
        }

        double[][] Zmat = buildZmat(FO);
        if (Zmat.length == 0 || Zmat[0].length == 0) {
            throw new IllegalArgumentException(
                    "El método de Costos Duales necesita variables de doble índice, tipo x_ij (ej: x11, x23),\n"
                    + "una por cada ruta origen-destino, no variables de una sola posición como x1.\n"
                    + "Revisa el formato de tu función objetivo.");
        }
        ParserTransporte.validarMatrizCompleta(FO);

        double[] oferta = getOferta(R, Zmat.length, Zmat[0].length);
        double[] demanda = getDemanda(R, Zmat.length, Zmat[0].length);

        if (Arrays.stream(oferta).allMatch(v -> v == 0) || Arrays.stream(demanda).allMatch(v -> v == 0)) {
            throw new IllegalArgumentException(
                    "No se pudieron identificar las restricciones de oferta y demanda.\n"
                    + "Cada restricción debe agrupar un solo origen (ej: \"x11+x12+x13=20\") o\n"
                    + "un solo destino (ej: \"x11+x21+x31=10\") — no mezcles orígenes y destinos en la misma línea.");
        }

        resolverTransporte(Zmat, oferta, demanda, esMax);
    }

    private void resolverTransporte(double[][] ZmatOriginal, double[] ofertaOriginal, double[] demandaOriginal, boolean esMax) {
        pasosAsigna.clear();
        pasoInfo.clear();
        pasoResaltado.clear();
        pasoActual = 0;
        factible = true;

        int filas = ZmatOriginal.length;
        int columnas = ZmatOriginal[0].length;

        // ---- balancear oferta/demanda agregando fila o columna ficticia ----
        double totalOferta = Arrays.stream(ofertaOriginal).sum();
        double totalDemanda = Arrays.stream(demandaOriginal).sum();

        int filasF = filas, columnasF = columnas;
        boolean agregoFila = false, agregoColumna = false;
        if (totalOferta > totalDemanda + EPS) {
            columnasF++;
            agregoColumna = true;
        } else if (totalDemanda > totalOferta + EPS) {
            filasF++;
            agregoFila = true;
        }

        costoReal = new double[filasF][columnasF];
        for (int i = 0; i < filas; i++) {
            System.arraycopy(ZmatOriginal[i], 0, costoReal[i], 0, columnas);
        }
        // filas/columnas ficticias quedan en 0 (ya inicializado por defecto)

        double[] oferta = new double[filasF];
        double[] demanda = new double[columnasF];
        System.arraycopy(ofertaOriginal, 0, oferta, 0, filas);
        System.arraycopy(demandaOriginal, 0, demanda, 0, columnas);
        if (agregoColumna) {
            demanda[columnasF - 1] = totalOferta - totalDemanda;
        }
        if (agregoFila) {
            oferta[filasF - 1] = totalDemanda - totalOferta;
        }

        nombresFilas = new String[filasF];
        for (int i = 0; i < filas; i++) {
            nombresFilas[i] = "Origen " + (i + 1);
        }
        if (agregoFila) {
            nombresFilas[filasF - 1] = "Ficticio";
        }
        nombresColumnas = new String[columnasF];
        for (int j = 0; j < columnas; j++) {
            nombresColumnas[j] = "Destino " + (j + 1);
        }
        if (agregoColumna) {
            nombresColumnas[columnasF - 1] = "Ficticio";
        }

        // ---- matriz interna: para Max, se trabaja con costos negados (siempre se minimiza) ----
        double[][] cInterno = new double[filasF][columnasF];
        for (int i = 0; i < filasF; i++) {
            for (int j = 0; j < columnasF; j++) {
                cInterno[i][j] = esMax ? -costoReal[i][j] : costoReal[i][j];
            }
        }

        // ---- 1) Vogel para la solución inicial ----
        double[] ofertaRestante = oferta.clone();
        double[] demandaRestante = demanda.clone();
        double[][] Asigna = precalculo(cInterno, ofertaRestante, demandaRestante);

        guardarPaso(Asigna, "Solución inicial (Vogel)", null);

        // ---- 2) iterar MODI hasta que no haya costos de oportunidad negativos ----
        int iterMax = 100;
        int iter = 0;
        while (iter++ < iterMax) {
            List<int[]> basicas = obtenerCeldasBasicas(Asigna, cInterno, filasF, columnasF);

            Double[] u = new Double[filasF];
            Double[] v = new Double[columnasF];
            calcularUV(basicas, cInterno, u, v);

            // costo de oportunidad de cada celda no básica
            boolean[][] esBasica = new boolean[filasF][columnasF];
            for (int[] cel : basicas) {
                esBasica[cel[0]][cel[1]] = true;
            }

            int entI = -1, entJ = -1;
            double masNegativo = -EPS;
            for (int i = 0; i < filasF; i++) {
                for (int j = 0; j < columnasF; j++) {
                    if (esBasica[i][j] || u[i] == null || v[j] == null) {
                        continue;
                    }
                    double costoOp = cInterno[i][j] - u[i] - v[j];
                    if (costoOp < masNegativo) {
                        masNegativo = costoOp;
                        entI = i;
                        entJ = j;
                    }
                }
            }

            if (entI == -1) {
                break; // óptimo: ningún costo de oportunidad negativo
            }

            // ---- 3) ciclo cerrado desde la celda entrante ----
            List<int[]> ciclo = encontrarCiclo(basicas, entI, entJ, filasF, columnasF);
            if (ciclo == null) {
                // no debería pasar con una base válida, pero por seguridad:
                factible = false;
                break;
            }

            // celdas en posiciones pares del ciclo = '+', impares = '-'
            double theta = Double.MAX_VALUE;
            for (int k = 1; k < ciclo.size(); k += 2) {
                int[] cel = ciclo.get(k);
                theta = Math.min(theta, Asigna[cel[0]][cel[1]]);
            }

            int[][] resaltado = new int[filasF][columnasF];
            for (int k = 0; k < ciclo.size(); k++) {
                int[] cel = ciclo.get(k);
                resaltado[cel[0]][cel[1]] = (k == 0) ? 2 : (k % 2 == 0 ? 3 : 4);
            }
            guardarPaso(Asigna, "Entra (" + nombresFilas[entI] + "," + nombresColumnas[entJ] + "), costo oportunidad = " + fmt(masNegativo), resaltado);

            for (int k = 0; k < ciclo.size(); k++) {
                int[] cel = ciclo.get(k);
                if (k % 2 == 0) {
                    Asigna[cel[0]][cel[1]] += theta;
                } else {
                    Asigna[cel[0]][cel[1]] -= theta;
                }
            }

            guardarPaso(Asigna, "Reasignación (theta = " + fmt(theta) + ")", null);
        }

        // ---- 4) resultado ----
        zOptimo = 0;
        for (int i = 0; i < filasF; i++) {
            for (int j = 0; j < columnasF; j++) {
                zOptimo += costoReal[i][j] * Asigna[i][j];
            }
        }

        StringBuilder sb = new StringBuilder();
        if (!factible) {
            sb.append("No se pudo completar el método (base degenerada sin ciclo válido).");
        } else {
            sb.append("Asignación óptima:\n");
            for (int i = 0; i < filasF; i++) {
                for (int j = 0; j < columnasF; j++) {
                    if (Asigna[i][j] > EPS) {
                        sb.append(nombresFilas[i]).append(" → ").append(nombresColumnas[j])
                                .append("  (envío ").append(fmt(Asigna[i][j]))
                                .append(", costo unitario ").append(fmt(costoReal[i][j])).append(")\n");
                    }
                }
            }
            sb.append("Z = ").append(fmt(zOptimo));
            if (pasosAsigna.size() == 1) {
                sb.append("\n(La solución inicial ya es óptima.");
            }
            if (agregoFila || agregoColumna) {
                sb.append("  (problema desbalanceado: se agregó un ").append(agregoFila ? "origen" : "destino").append(" ficticio con costo 0)");
            }
        }
        resultadoTexto = sb.toString();

        mostrarPaso(0);
    }

    /**
     * Obtiene las celdas básicas actuales (asignación &gt; 0) y, si la base
     * está degenerada (menos de filas+columnas-1 celdas, o no conecta todas
     * las filas/columnas), completa el árbol agregando celdas de costo 0
     * en las posiciones más baratas que no formen ciclo.
     */
    private List<int[]> obtenerCeldasBasicas(double[][] Asigna, double[][] cInterno, int filasF, int columnasF) {
        List<int[]> basicas = new ArrayList<>();
        for (int i = 0; i < filasF; i++) {
            for (int j = 0; j < columnasF; j++) {
                if (Asigna[i][j] > EPS) {
                    basicas.add(new int[]{i, j});
                }
            }
        }

        int necesarias = filasF + columnasF - 1;
        UnionFind uf = new UnionFind(filasF + columnasF);
        for (int[] cel : basicas) {
            uf.union(cel[0], filasF + cel[1]);
        }

        if (basicas.size() < necesarias) {
            // completar con celdas de menor costo que conecten componentes distintas
            List<int[]> candidatas = new ArrayList<>();
            for (int i = 0; i < filasF; i++) {
                for (int j = 0; j < columnasF; j++) {
                    if (Asigna[i][j] <= EPS) {
                        candidatas.add(new int[]{i, j});
                    }
                }
            }
            candidatas.sort((a, b) -> Double.compare(cInterno[a[0]][a[1]], cInterno[b[0]][b[1]]));
            for (int[] cel : candidatas) {
                if (basicas.size() >= necesarias) {
                    break;
                }
                if (uf.find(cel[0]) != uf.find(filasF + cel[1])) {
                    uf.union(cel[0], filasF + cel[1]);
                    basicas.add(cel);
                }
            }
        }
        return basicas;
    }

    /** Resuelve u_i + v_j = costo_ij sobre el árbol de celdas básicas (BFS). */
    private void calcularUV(List<int[]> basicas, double[][] cInterno, Double[] u, Double[] v) {
        // lista de adyacencia: fila i <-> columna j por cada celda básica
        int filasF = u.length;
        List<List<int[]>> adyFila = new ArrayList<>();
        List<List<int[]>> adyCol = new ArrayList<>();
        for (int i = 0; i < filasF; i++) {
            adyFila.add(new ArrayList<>());
        }
        for (int j = 0; j < v.length; j++) {
            adyCol.add(new ArrayList<>());
        }
        for (int[] cel : basicas) {
            adyFila.get(cel[0]).add(cel);
            adyCol.get(cel[1]).add(cel);
        }

        u[0] = 0.0;
        java.util.Deque<int[]> pendientes = new java.util.ArrayDeque<>();
        pendientes.add(new int[]{0, 0}); // {tipo(0=fila,1=col), indice}
        boolean[] visFila = new boolean[filasF];
        boolean[] visCol = new boolean[v.length];
        visFila[0] = true;

        while (!pendientes.isEmpty()) {
            int[] actual = pendientes.poll();
            if (actual[0] == 0) {
                int i = actual[1];
                for (int[] cel : adyFila.get(i)) {
                    int j = cel[1];
                    if (!visCol[j]) {
                        v[j] = cInterno[i][j] - u[i];
                        visCol[j] = true;
                        pendientes.add(new int[]{1, j});
                    }
                }
            } else {
                int j = actual[1];
                for (int[] cel : adyCol.get(j)) {
                    int i = cel[0];
                    if (!visFila[i]) {
                        u[i] = cInterno[i][j] - v[j];
                        visFila[i] = true;
                        pendientes.add(new int[]{0, i});
                    }
                }
            }
        }
    }

    /**
     * Encuentra el ciclo cerrado (stepping-stone) que se forma al agregar la
     * celda entrante (entI, entJ) al conjunto de celdas básicas, mediante
     * poda de celdas con grado 1 por fila/columna hasta que solo quede el
     * ciclo, y luego recorre ese ciclo en orden fila-columna-fila-columna...
     */
    private List<int[]> encontrarCiclo(List<int[]> basicas, int entI, int entJ, int filasF, int columnasF) {
        List<int[]> candidatas = new ArrayList<>(basicas);
        candidatas.add(new int[]{entI, entJ});

        boolean cambiou = true;
        while (cambiou) {
            cambiou = false;
            int[] contFila = new int[filasF];
            int[] contCol = new int[columnasF];
            for (int[] cel : candidatas) {
                contFila[cel[0]]++;
                contCol[cel[1]]++;
            }
            List<int[]> siguientes = new ArrayList<>();
            for (int[] cel : candidatas) {
                if (contFila[cel[0]] > 1 && contCol[cel[1]] > 1) {
                    siguientes.add(cel);
                } else {
                    cambiou = true;
                }
            }
            candidatas = siguientes;
        }

        if (candidatas.isEmpty()) {
            return null;
        }

        // recorrer el ciclo: alternar movimiento por fila / por columna
        List<int[]> ciclo = new ArrayList<>();
        int actualIdx = indiceCelda(candidatas, entI, entJ);
        if (actualIdx == -1) {
            return null;
        }
        boolean moverPorFila = true;
        int[] actual = candidatas.get(actualIdx);
        int[] inicio = actual;
        int[] anterior = null;
        ciclo.add(actual);

        for (int paso = 0; paso < candidatas.size(); paso++) {
            int[] siguiente = null;
            for (int[] cel : candidatas) {
                if (cel == actual || cel == anterior) {
                    continue;
                }
                if (moverPorFila && cel[0] == actual[0]) {
                    siguiente = cel;
                    break;
                }
                if (!moverPorFila && cel[1] == actual[1]) {
                    siguiente = cel;
                    break;
                }
            }
            if (siguiente == null) {
                break; // no se pudo cerrar el ciclo
            }
            if (siguiente == inicio) {
                break; // se cerró el ciclo: no volver a agregar la celda de inicio
            }
            ciclo.add(siguiente);
            anterior = actual;
            actual = siguiente;
            moverPorFila = !moverPorFila;
        }

        return ciclo.size() >= 4 && ciclo.size() % 2 == 0 ? ciclo : null;
    }

    private int indiceCelda(List<int[]> lista, int i, int j) {
        for (int k = 0; k < lista.size(); k++) {
            if (lista.get(k)[0] == i && lista.get(k)[1] == j) {
                return k;
            }
        }
        return -1;
    }

    /** Union-Find simple para detectar ciclos al completar la base. */
    private static class UnionFind {
        private final int[] padre;

        UnionFind(int n) {
            padre = new int[n];
            for (int i = 0; i < n; i++) {
                padre[i] = i;
            }
        }

        int find(int x) {
            while (padre[x] != x) {
                padre[x] = padre[padre[x]];
                x = padre[x];
            }
            return x;
        }

        void union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra != rb) {
                padre[ra] = rb;
            }
        }
    }

    private double[][] precalculo(double[][] Zmat, double[] oferta, double[] demanda) {
        int filas = Zmat.length;
        int columnas = Zmat[0].length;
        double[][] Asigna = new double[filas][columnas];
        boolean[] filaActiva = new boolean[filas];
        boolean[] columnaActiva = new boolean[columnas];

        Arrays.fill(filaActiva, true);
        Arrays.fill(columnaActiva, true);

        while (true) {
            double[] penaFila = calcularPenalizacionesFilas(Zmat, filaActiva, columnaActiva);
            double[] penaColumna = calcularPenalizacionesColumnas(Zmat, filaActiva, columnaActiva);

            double maxPena = -1;
            boolean esFila = true;
            int idx = -1;
            for (int i = 0; i < filas; i++) {
                if (filaActiva[i] && penaFila[i] > maxPena) {
                    maxPena = penaFila[i];
                    idx = i;
                    esFila = true;
                }
            }
            for (int j = 0; j < columnas; j++) {
                if (columnaActiva[j] && penaColumna[j] > maxPena) {
                    maxPena = penaColumna[j];
                    idx = j;
                    esFila = false;
                }
            }

            if (idx == -1) {
                break;
            }

            int selI = -1, selJ = -1;
            double menorCosto = Double.MAX_VALUE;
            if (esFila) {
                selI = idx;
                for (int j = 0; j < columnas; j++) {
                    if (columnaActiva[j] && Zmat[selI][j] < menorCosto) {
                        menorCosto = Zmat[selI][j];
                        selJ = j;
                    }
                }
            } else {
                selJ = idx;
                for (int i = 0; i < filas; i++) {
                    if (filaActiva[i] && Zmat[i][selJ] < menorCosto) {
                        menorCosto = Zmat[i][selJ];
                        selI = i;
                    }
                }
            }

            double asignar = Math.min(oferta[selI], demanda[selJ]);
            Asigna[selI][selJ] += asignar;
            oferta[selI] -= asignar;
            demanda[selJ] -= asignar;

            if (oferta[selI] <= EPS) {
                filaActiva[selI] = false;
            }
            if (demanda[selJ] <= EPS) {
                columnaActiva[selJ] = false;
            }

            boolean fin = true;
            for (boolean f : filaActiva) {
                if (f) {
                    fin = false;
                    break;
                }
            }
            for (boolean cAct : columnaActiva) {
                if (cAct) {
                    fin = false;
                    break;
                }
            }
            if (fin) {
                break;
            }
        }

        return Asigna;
    }

    private double[] calcularPenalizacionesFilas(double[][] costos, boolean[] filaActiva, boolean[] colActiva) {
        int m = costos.length;
        int n = costos[0].length;
        double[] penas = new double[m];
        Arrays.fill(penas, 0);

        for (int i = 0; i < m; i++) {
            if (!filaActiva[i]) {
                continue;
            }
            double menor = Double.MAX_VALUE, segundo = Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!colActiva[j]) {
                    continue;
                }
                double c = costos[i][j];
                if (c < menor) {
                    segundo = menor;
                    menor = c;
                } else if (c < segundo) {
                    segundo = c;
                }
            }
            penas[i] = (segundo == Double.MAX_VALUE) ? 0 : segundo - menor;
        }
        return penas;
    }

    private double[] calcularPenalizacionesColumnas(double[][] costos, boolean[] filaActiva, boolean[] colActiva) {
        int m = costos.length;
        int n = costos[0].length;
        double[] penas = new double[n];
        Arrays.fill(penas, 0);

        for (int j = 0; j < n; j++) {
            if (!colActiva[j]) {
                continue;
            }
            double menor = Double.MAX_VALUE, segundo = Double.MAX_VALUE;
            for (int i = 0; i < m; i++) {
                if (!filaActiva[i]) {
                    continue;
                }
                double c = costos[i][j];
                if (c < menor) {
                    segundo = menor;
                    menor = c;
                } else if (c < segundo) {
                    segundo = c;
                }
            }
            penas[j] = (segundo == Double.MAX_VALUE) ? 0 : segundo - menor;
        }
        return penas;
    }

    private double[][] buildZmat(String FO) {
        return ParserTransporte.construirMatrizCostos(FO);
    }

    private double[] getOferta(String[] R, int filas, int columnas) {
        return ParserTransporte.getOferta(R, filas);
    }

    private double[] getDemanda(String[] R, int filas, int columnas) {
        return ParserTransporte.getDemanda(R, columnas);
    }

    public double getZOptimo() {
        return zOptimo;
    }

    public boolean isFactible() {
        return factible;
    }

    private void guardarPaso(double[][] Asigna, String info, int[][] resaltado) {
        double[][] clone = new double[Asigna.length][];
        for (int i = 0; i < Asigna.length; i++) {
            clone[i] = Asigna[i].clone();
        }
        pasosAsigna.add(clone);
        pasoInfo.add(info);
        pasoResaltado.add(resaltado);
    }

    private void mostrarPaso(int idx) {
        if (pasosAsigna.isEmpty()) {
            return;
        }
        double[][] Asigna = pasosAsigna.get(idx);
        int filasF = Asigna.length;
        int columnasF = Asigna[0].length;

        String[] columnas = new String[columnasF + 1];
        columnas[0] = "";
        for (int j = 0; j < columnasF; j++) {
            columnas[j + 1] = nombresColumnas[j];
        }

        Object[][] datos = new Object[filasF][columnasF + 1];
        for (int i = 0; i < filasF; i++) {
            datos[i][0] = nombresFilas[i];
            for (int j = 0; j < columnasF; j++) {
                double val = Asigna[i][j];
                datos[i][j + 1] = (val > EPS ? fmt(val) : "-") + "  (c=" + fmt(costoReal[i][j]) + ")";
            }
        }

        int[][] aux = new int[filasF][columnasF + 1];
        int[][] resaltadoPaso = pasoResaltado.get(idx);
        if (resaltadoPaso != null) {
            for (int i = 0; i < filasF; i++) {
                for (int j = 0; j < columnasF; j++) {
                    aux[i][j + 1] = resaltadoPaso[i][j];
                }
            }
        } else if (idx > 0) {
            double[][] anterior = pasosAsigna.get(idx - 1);
            for (int i = 0; i < filasF; i++) {
                for (int j = 0; j < columnasF; j++) {
                    if (Math.abs(Asigna[i][j] - anterior[i][j]) > EPS) {
                        aux[i][j + 1] = 1;
                    }
                }
            }
        }
        auxMatActual = aux;

        DefaultTableModel model = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TablaTransporte.setModel(model);

        String info = pasoInfo.get(idx);
        lblPaso.setText("Tabla " + (idx + 1) + " de " + pasosAsigna.size() + (info != null ? "  —  " + info : ""));
        btnAnterior.setEnabled(idx > 0);
        btnSiguiente.setEnabled(idx < pasosAsigna.size() - 1);

        if (resultadoTexto != null) {
            txtResultado.setText(resultadoTexto);
        }
        TablaTransporte.repaint();
    }

    private String fmt(double v) {
        if (Math.abs(v - Math.round(v)) < 1e-6) {
            return String.valueOf(Math.round(v));
        }
        return String.format("%.2f", v);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TablaTransporte;
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPaso;
    private javax.swing.JTextArea txtResultado;
    // End of variables declaration//GEN-END:variables
}
