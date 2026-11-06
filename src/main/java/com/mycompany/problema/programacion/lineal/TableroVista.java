package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Tablero del Simplex con el formato de los apuntes de clase, común a
 * Simplex, Simplex Revisado, Big M y Simplex Dual:
 *
 * <pre>
 *        |  Cj  | c1 ... cn  |     |
 *   Cb   |  Xb  | x1 ... xn  | Sol |  θ
 *   cb1  |  xb1 | a11 ... a1n| b1  | θ1
 *   ...
 *        |  Zj  | z1 ... zn  |  Z  |
 *        |  Pj  | p1 ... pn  |     |
 * </pre>
 *
 * Con Zj = Σ Cb·aij y Pj = Cj − Zj. Cada ventana arma un {@link Datos}
 * (ya formateado: puede llevar M) y esta clase lo dibuja con la
 * colorimetría de Vertex: pivote naranja, fila que sale y columna que
 * entra en amarillo, celdas que cambiaron en verde claro. El Simplex
 * Revisado colorea además los bloques del tablero según de qué producto
 * de matrices salen (B⁻¹A, B⁻¹, B⁻¹R, Cbᵀ·B⁻¹...).
 */
public final class TableroVista {

    // colorimetría (igual que la tabla del README)
    static final Color NARANJA = new Color(255, 176, 120);
    static final Color AMARILLO = new Color(255, 255, 170);
    static final Color VERDE_CLARO = new Color(200, 255, 200);

    /** Bloques del Simplex Revisado (tonos suaves, como en los apuntes). */
    public enum Bloque {
        NINGUNO(null),
        B_INV_A(new Color(214, 230, 250)),      // B⁻¹A: columnas de las x
        B_INV(new Color(252, 233, 206)),        // B⁻¹: columnas de las holguras
        B_INV_R(new Color(250, 244, 196)),      // B⁻¹R: columna Sol
        ZJ_A(new Color(244, 216, 210)),         // Cbᵀ·B⁻¹A
        ZJ_S(new Color(220, 218, 244)),         // Cbᵀ·B⁻¹
        ZJ_R(new Color(248, 214, 232)),         // Cbᵀ·B⁻¹R
        PJ(new Color(252, 241, 226));           // Pj = Cj − Zj

        public final Color color;

        Bloque(Color color) {
            this.color = color;
        }
    }

    /** Todo lo que se muestra de un tablero, ya como texto. */
    public static final class Datos {

        public String[] variables;   // columnas: x1..xn, s1..sm, A1..
        public String[] cj;
        public String[] cb;          // por fila
        public String[] xb;          // por fila
        public String[][] cuerpo;    // filas × columnas
        public String[] sol;
        /** θ por fila (null = este tablero ya no tiene variable que entre). */
        public String[] theta;
        /** false = el método no usa θ (Simplex Dual) y la columna no se dibuja. */
        public boolean conTheta = true;
        public String[] zj;
        public String z;
        public String[] pj;
        /** Fila extra opcional bajo Pj (Simplex Dual: razones |Pj / a_rj| por columna). */
        public String etiquetaExtra;
        public String[] filaExtra;
        /** Decisión de ESTE tablero (−1 si es el último). */
        public int filaPivote = -1;
        public int colPivote = -1;
        /** Celdas que cambiaron respecto al tablero anterior: [fila][col], col = variables + Sol. */
        public boolean[][] cambio;
        /** Solo Simplex Revisado: bloque de cada columna del cuerpo. */
        public Bloque[] bloqueColumna;
    }

    private TableroVista() {
    }

    /** Pone los datos en la tabla e instala el renderizador con la colorimetría. */
    public static void mostrar(JTable tabla, Datos d) {
        int n = d.variables.length;
        int m = d.xb.length;
        boolean conExtra = d.filaExtra != null;
        int columnas = n + (d.conTheta ? 4 : 3); // Cb, Xb, variables, Sol, [θ]
        int filas = m + 3 + (conExtra ? 1 : 0); // Cj, base, Zj, Pj, [extra]

        String[] encabezado = new String[columnas];
        encabezado[0] = "Cb";
        encabezado[1] = "Xb";
        System.arraycopy(d.variables, 0, encabezado, 2, n);
        encabezado[n + 2] = "Sol";
        if (d.conTheta) {
            encabezado[n + 3] = "θ";
        }

        Object[][] celdas = new Object[filas][columnas];
        celdas[0][1] = "Cj";
        for (int j = 0; j < n; j++) {
            celdas[0][j + 2] = d.cj[j];
        }
        for (int i = 0; i < m; i++) {
            celdas[i + 1][0] = d.cb[i];
            celdas[i + 1][1] = d.xb[i];
            for (int j = 0; j < n; j++) {
                celdas[i + 1][j + 2] = d.cuerpo[i][j];
            }
            celdas[i + 1][n + 2] = d.sol[i];
            if (d.conTheta) {
                celdas[i + 1][n + 3] = d.theta != null ? d.theta[i] : "";
            }
        }
        int filaZ = m + 1;
        celdas[filaZ][1] = "Zj";
        celdas[filaZ + 1][1] = "Pj";
        for (int j = 0; j < n; j++) {
            celdas[filaZ][j + 2] = d.zj[j];
            celdas[filaZ + 1][j + 2] = d.pj[j];
        }
        celdas[filaZ][n + 2] = d.z;
        if (conExtra) {
            celdas[filaZ + 2][1] = d.etiquetaExtra;
            for (int j = 0; j < n; j++) {
                celdas[filaZ + 2][j + 2] = d.filaExtra[j];
            }
        }
        for (Object[] fila : celdas) {
            for (int c = 0; c < fila.length; c++) {
                if (fila[c] == null) {
                    fila[c] = "";
                }
            }
        }

        tabla.setModel(new DefaultTableModel(celdas, encabezado) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tabla.setDefaultRenderer(Object.class, new Renderer(d, m, n));
        tabla.setRowHeight(26);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.repaint();
    }

    private static final class Renderer extends DefaultTableCellRenderer {

        private final Datos d;
        private final int m;
        private final int n;

        Renderer(Datos d, int m, int n) {
            this.d = d;
            this.m = m;
            this.n = n;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, false, false, row, column);
            setHorizontalAlignment(CENTER);
            Font base = table.getFont();
            c.setFont(base);

            int filaBase = row - 1;                    // índice de la fila de la base (0..m-1) o fuera
            boolean esFilaBase = filaBase >= 0 && filaBase < m;
            int filaZ = m + 1;
            int col = column - 2;                      // índice de variable (0..n-1), n = Sol
            boolean esEtiqueta = column == 1 && !esFilaBase;           // "Cj", "Zj", "Pj"
            boolean esCbXb = column <= 1 && esFilaBase;

            Color fondo = Tema.campo();
            if (esEtiqueta || row == 0 && column > 1 && column < n + 2) {
                fondo = Tema.encabezadoTabla();       // fila Cj y etiquetas, como encabezado
            }
            if (esEtiqueta || esCbXb && column == 1) {
                c.setFont(base.deriveFont(Font.BOLD));
            }

            // Simplex Revisado: bloques del tablero
            if (d.bloqueColumna != null && column >= 2 && column <= n + 2) {
                Bloque b = Bloque.NINGUNO;
                if (esFilaBase) {
                    b = column == n + 2 ? Bloque.B_INV_R : d.bloqueColumna[col];
                } else if (row == filaZ) {
                    b = column == n + 2 ? Bloque.ZJ_R
                            : d.bloqueColumna[col] == Bloque.B_INV_A ? Bloque.ZJ_A : Bloque.ZJ_S;
                } else if (row == filaZ + 1 && column < n + 2) {
                    b = Bloque.PJ;
                }
                if (b.color != null) {
                    fondo = b.color;
                }
            }

            // verde claro: cambió respecto al tablero anterior
            if (d.cambio != null && esFilaBase && col >= 0 && col <= n && d.cambio[filaBase][col]) {
                fondo = VERDE_CLARO;
            }
            // amarillo: fila que sale y columna que entra; naranja: pivote
            boolean filaPiv = d.filaPivote >= 0 && filaBase == d.filaPivote;
            boolean colPiv = d.colPivote >= 0 && col == d.colPivote && row <= filaZ + 1;
            if (filaPiv || colPiv) {
                fondo = AMARILLO;
            }
            if (filaPiv && colPiv) {
                fondo = NARANJA;
            }

            c.setBackground(fondo);
            boolean resaltada = fondo.equals(AMARILLO) || fondo.equals(NARANJA) || fondo.equals(VERDE_CLARO)
                    || d.bloqueColumna != null && column >= 2 && column <= n + 2 && !fondo.equals(Tema.campo())
                    && !fondo.equals(Tema.encabezadoTabla());
            c.setForeground(resaltada ? Tema.textoSobreResaltado() : Tema.tinta());
            return c;
        }
    }
}
