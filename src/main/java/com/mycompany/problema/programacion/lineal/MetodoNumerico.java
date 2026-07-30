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
 * Método Numérico (enumeración de soluciones básicas / puntos extremos):
 * arma el sistema en forma estándar (con holguras y excesos como columnas
 * reales, igual que Simplex/Big M), genera TODAS las combinaciones posibles
 * de m variables básicas entre las n totales (C(n,m)), resuelve cada
 * sistema cuadrado con eliminación de Gauss, y marca cuáles son factibles
 * (todas las variables >= 0). La mejor solución factible es el óptimo.
 *
 * @author PC-ANDERSON
 */
public class MetodoNumerico extends javax.swing.JFrame implements MetodoPL {

    private static final Color COLOR_OPTIMO = new Color(255, 176, 120);
    private static final Color COLOR_INFACTIBLE = new Color(255, 210, 210);
    private static final Color COLOR_FACTIBLE = new Color(220, 255, 220);
    private static final double EPS = 1e-7;
    private static final int MAX_VARS_TOTALES = 18; // evita que C(n,m) explote

    private String[] varNames;
    private int filaOptima = -1;
    private String resultadoTexto;

    
    @Override
    public void resolver(String FO, String[] R, String tipo) {
        calculos(FO, R, tipo);
    }

    @Override
    public void mostrar() {
        setVisible(true);
    }

    public MetodoNumerico() {
        initComponents();
        Recursos.aplicarIcono(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        TablaSoluciones = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtResultado = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Metodo Numerico (enumeracion de puntos extremos)");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(712, 460));
        setResizable(false);

        TablaSoluciones.setEnabled(false);
        TablaSoluciones.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        TablaSoluciones.setRowHeight(24);
        jScrollPane1.setViewportView(TablaSoluciones);

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
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MetodoNumerico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MetodoNumerico().setVisible(true);
            }
        });
    }

    // ======================================================================
    // LÓGICA: ENUMERACIÓN DE SOLUCIONES BÁSICAS
    // ======================================================================
    /**
     * @param FO   función objetivo, ej: "3x1+5x2"
     * @param R    restricciones, admite &lt;=, &gt;= y =
     * @param tipo "Max" o "Min"
     */
    public void calculos(String FO, String[] R, String tipo) {
        ParserLP.validarVariablesCompletas(FO, R);
        boolean esMin = tipo != null && tipo.trim().equalsIgnoreCase("Min");
        String foLimpia = FO.replaceAll("\\s+", "");

        // ---- 1) variables de decisión ----
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
        int numVars = ordered.size();

        Map<Integer, String> foCoefs = PrincipalPage.parseCoefficientsMap(foLimpia);
        double[] c = new double[numVars];
        for (int k = 0; k < numVars; k++) {
            c[k] = parseNum(foCoefs.getOrDefault(ordered.get(k), "0"));
        }

        // ---- 2) restricciones -> forma estándar con holguras/excesos reales ----
        int m = R.length;
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
            A[i] = fila;
            b[i] = parseNum(partes[2]);
            ops[i] = partes[1];
        }

        int holguras = 0, excesos = 0;
        for (String op : ops) {
            if (op.equals("<=")) {
                holguras++;
            } else if (op.equals(">=")) {
                excesos++;
            } else if (!op.equals("=")) {
                throw new IllegalArgumentException("Operador de restricción no reconocido: " + op);
            }
        }

        int n = numVars + holguras + excesos; // variables totales del sistema
        if (n > MAX_VARS_TOTALES) {
            throw new IllegalArgumentException(
                    "Este problema tiene " + n + " variables en total (decisión + holguras/excesos).\n"
                    + "La enumeración de C(" + n + "," + m + ") soluciones básicas sería demasiado lenta.\n"
                    + "El método numérico solo es práctico para problemas pequeños.");
        }
        if (n < m) {
            throw new IllegalArgumentException("El sistema tiene menos variables (" + n + ") que restricciones (" + m + "); no se pueden formar bases.");
        }

        varNames = new String[n];
        for (int k = 0; k < numVars; k++) {
            varNames[k] = "x" + ordered.get(k);
        }
        double[][] Amp = new double[m][n]; // matriz ampliada [decisión | holguras | excesos]
        int colLibre = numVars;
        int sCount = 0, eCount = 0;
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < numVars; k++) {
                Amp[i][k] = A[i][k];
            }
        }
        // Asignar columnas en orden estable: primero holguras, luego excesos,
        // recorriendo las restricciones en su orden original.
        sCount = 0;
        for (int i = 0; i < m; i++) {
            if (ops[i].equals("<=")) {
                int col = numVars + sCount;
                varNames[col] = "S" + (sCount + 1);
                Amp[i][col] = 1.0;
                sCount++;
            }
        }
        eCount = 0;
        for (int i = 0; i < m; i++) {
            if (ops[i].equals(">=")) {
                int col = numVars + holguras + eCount;
                varNames[col] = "E" + (eCount + 1);
                Amp[i][col] = -1.0;
                eCount++;
            }
        }

        // ---- 3) generar combinaciones C(n, m) de columnas básicas ----
        List<int[]> combinaciones = new ArrayList<>();
        generarCombinaciones(combinaciones, new int[m], 0, n, m, 0);

        int filaOptimaZ = -1;
        double mejorZ = esMin ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

        Object[][] datos = new Object[combinaciones.size()][n + 3];
        for (int fila = 0; fila < combinaciones.size(); fila++) {
            int[] basicas = combinaciones.get(fila);
            double[] solucion = new double[n];

            double[][] Absica = new double[m][m];
            double[] B = new double[m];
            for (int i = 0; i < m; i++) {
                B[i] = b[i];
                for (int j = 0; j < m; j++) {
                    Absica[i][j] = Amp[i][basicas[j]];
                }
            }

            boolean singular = false;
            double[] resultado = null;
            try {
                resultado = gauss(Absica, B);
            } catch (RuntimeException ex) {
                singular = true;
            }

            boolean factible = !singular;
            if (!singular) {
                for (int j = 0; j < m; j++) {
                    solucion[basicas[j]] = resultado[j];
                }
                for (double v : solucion) {
                    if (v < -EPS) {
                        factible = false;
                        break;
                    }
                }
            }

            double z = 0;
            if (factible) {
                for (int k = 0; k < numVars; k++) {
                    z += c[k] * solucion[k];
                }
            }

            datos[fila][0] = fila + 1;
            for (int k = 0; k < n; k++) {
                datos[fila][k + 1] = singular ? "—" : fmt(solucion[k]);
            }
            datos[fila][n + 1] = singular ? "Singular" : (factible ? "Sí" : "No");
            datos[fila][n + 2] = factible ? fmt(z) : "—";

            if (factible) {
                boolean mejor = esMin ? (z < mejorZ - EPS) : (z > mejorZ + EPS);
                if (mejor) {
                    mejorZ = z;
                    filaOptimaZ = fila;
                }
            }
        }

        filaOptima = filaOptimaZ;

        String[] columnas = new String[n + 3];
        columnas[0] = "#";
        for (int k = 0; k < n; k++) {
            columnas[k + 1] = varNames[k];
        }
        columnas[n + 1] = "Factible";
        columnas[n + 2] = "Z";

        DefaultTableModel model = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TablaSoluciones.setModel(model);

        if (filaOptima == -1) {
            resultadoTexto = "No se encontró ninguna solución básica factible: el problema no tiene solución.";
        } else {
            StringBuilder sb = new StringBuilder("Solución óptima (fila " + (filaOptima + 1) + "):  \n");
            for (int k = 0; k < numVars; k++) {
                sb.append(varNames[k]).append(" = ").append(datos[filaOptima][k + 1]).append("\n");
            }
            sb.append("Z = ").append(datos[filaOptima][n + 2]);
            resultadoTexto = sb.toString();
        }
        txtResultado.setText(resultadoTexto);
        TablaSoluciones.repaint();
    }

    private void generarCombinaciones(List<int[]> lista, int[] combinacionActual, int inicio, int n, int m, int indiceActual) {
        if (indiceActual == m) {
            lista.add(combinacionActual.clone());
            return;
        }
        for (int i = inicio; i < n; i++) {
            combinacionActual[indiceActual] = i;
            generarCombinaciones(lista, combinacionActual, i + 1, n, m, indiceActual + 1);
        }
    }

    /** Eliminación de Gauss con pivoteo parcial. Lanza RuntimeException si el sistema es singular. */
    private double[] gauss(double[][] A, double[] B) {
        int n = B.length;
        double[][] M = new double[n][n];
        double[] R = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            R[i] = B[i];
        }

        for (int k = 0; k < n; k++) {
            int max = k;
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(M[i][k]) > Math.abs(M[max][k])) {
                    max = i;
                }
            }
            double[] tempFila = M[k];
            M[k] = M[max];
            M[max] = tempFila;
            double temp = R[k];
            R[k] = R[max];
            R[max] = temp;

            if (Math.abs(M[k][k]) < 1e-9) {
                throw new RuntimeException("Sistema sin solución única (base singular).");
            }

            for (int i = k + 1; i < n; i++) {
                double factor = M[i][k] / M[k][k];
                for (int j = k; j < n; j++) {
                    M[i][j] -= factor * M[k][j];
                }
                R[i] -= factor * R[k];
            }
        }

        double[] X = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double suma = R[i];
            for (int j = i + 1; j < n; j++) {
                suma -= M[i][j] * X[j];
            }
            X[i] = suma / M[i][i];
        }
        return X;
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
    private javax.swing.JTable TablaSoluciones;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea txtResultado;
    // End of variables declaration//GEN-END:variables
}
