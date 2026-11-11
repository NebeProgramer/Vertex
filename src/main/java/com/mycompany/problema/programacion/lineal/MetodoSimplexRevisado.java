package com.mycompany.problema.programacion.lineal;

import com.mycompany.problema.programacion.lineal.algoritmos.AnalisisSensibilidad;
import com.mycompany.problema.programacion.lineal.algoritmos.FormatoNumero;
import com.mycompany.problema.programacion.lineal.algoritmos.Matrices;
import com.mycompany.problema.programacion.lineal.algoritmos.MetodoPL;
import com.mycompany.problema.programacion.lineal.algoritmos.SimplexRevisado;
import com.mycompany.problema.programacion.lineal.parser.MapeoVariables;
import com.mycompany.problema.programacion.lineal.parser.ParserLP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana del Simplex Revisado, con el formato de los apuntes: cada
 * iteración es un tablero que no se pivotea, sino que se arma con
 * matrices, Tablero = B⁻¹·[A | I | R]. Los bloques del tablero se
 * colorean según el producto del que salen (B⁻¹A, B⁻¹, B⁻¹R y sus filas
 * Zj = Cbᵀ·B⁻¹·…) y debajo se muestran B, B⁻¹ y Cbᵀ de esa iteración.
 * El cálculo vive en {@link SimplexRevisado}. Mismos requisitos que el
 * Simplex: restricciones &lt;= con lado derecho &gt;= 0.
 */
public class MetodoSimplexRevisado extends javax.swing.JFrame implements MetodoPL {

    private final JButton btnAnterior = new JButton("< Anterior");
    private final JButton btnSiguiente = new JButton("Siguiente >");
    private final JLabel lblPaso = new JLabel("Tablero 1", SwingConstants.CENTER);
    private final JTable tablaTablero = new JTable();
    private final JTable tablaB = new JTable();
    private final JTable tablaBinv = new JTable();
    private final JTable tablaCb = new JTable();
    private final JTextArea txtResultado = new JTextArea(4, 20);

    private SimplexRevisado.Resultado resultado;
    /** B⁻¹·[A | I | R] de cada iteración (la última columna es Sol = B⁻¹R). */
    private final List<double[][]> cuerpos = new ArrayList<>();
    private String[] nombres;      // decisión + holguras
    private int numVars;
    private boolean esMin;
    private int pasoActual;
    private String resultadoTexto;
    private double[] c;
    private double[][] A;
    private double[] b;

    private MapeoVariables mapeo = MapeoVariables.identidad();

    public MetodoSimplexRevisado() {
        armarVentana();
        Recursos.aplicarIcono(this);
        VentanaPrefs.aplicar(this);
        Recursos.instalarAyudaEnBarraTitulo(this, "Simplex Revisado",
                "<html><b>Colorimetría — Simplex Revisado</b><br>&#128992; Naranja: elemento pivote<br>"
                + "&#128993; Amarillo: fila que sale y columna que entra<br>"
                + "Bloques del tablero: B<sup>-1</sup>A (azul), B<sup>-1</sup> (naranja suave), B<sup>-1</sup>R (amarillo suave),"
                + " Cb<sup>T</sup>B<sup>-1</sup>A (rojo suave), Cb<sup>T</sup>B<sup>-1</sup> (morado), Cb<sup>T</sup>B<sup>-1</sup>R (rosado),"
                + " Pj (beige)</html>",
                Dualidad.teoria(VentanaAyudaAlgoritmo.TEORIA_REVISADO, VentanaAyudaAlgoritmo.TEORIA_REVISADO_DUAL));
        Recursos.agregarBotonBarraTitulo(this, "Sensibilidad",
                "Precios sombra, holguras y rangos de la solución óptima", this::mostrarSensibilidad);
        Recursos.envolverEnFondoAzul(this);
        Recursos.modernizarVentana(this);
    }

    private void armarVentana() {
        setTitle("Simplex Revisado");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(960, 680));
        setMinimumSize(new Dimension(712, 460));

        lblPaso.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAnterior.addActionListener(e -> mostrarPaso(pasoActual - 1));
        btnSiguiente.addActionListener(e -> mostrarPaso(pasoActual + 1));
        JPanel nav = new JPanel(new BorderLayout(8, 0));
        nav.setOpaque(false);
        nav.add(btnAnterior, BorderLayout.WEST);
        nav.add(lblPaso, BorderLayout.CENTER);
        nav.add(btnSiguiente, BorderLayout.EAST);

        for (JTable t : new JTable[]{tablaTablero, tablaB, tablaBinv, tablaCb}) {
            t.setEnabled(false);
            t.setFont(new Font("Consolas", Font.PLAIN, 13));
            t.setRowHeight(26);
            t.getTableHeader().setReorderingAllowed(false);
        }

        // todo en una sola columna con scroll: tablero, leyenda de bloques y matrices B, B⁻¹, Cbᵀ
        PanelAncho cuerpo = new PanelAncho();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);
        cuerpo.add(titulo("Tablero = B⁻¹ · [ A | I | R ]"));
        cuerpo.add(conEncabezado(tablaTablero));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(leyenda());
        cuerpo.add(Box.createVerticalStrut(12));

        JPanel matrices = new JPanel();
        matrices.setLayout(new BoxLayout(matrices, BoxLayout.X_AXIS));
        matrices.setOpaque(false);
        matrices.setAlignmentX(Component.LEFT_ALIGNMENT);
        matrices.add(bloqueMatriz("B  (columnas de [A | I] de la base)", tablaB));
        matrices.add(Box.createHorizontalStrut(24));
        matrices.add(bloqueMatriz("B⁻¹", tablaBinv));
        matrices.add(Box.createHorizontalStrut(24));
        matrices.add(bloqueMatriz("Cbᵀ", tablaCb));
        matrices.add(Box.createHorizontalGlue());
        cuerpo.add(matrices);

        JScrollPane scroll = new JScrollPane(cuerpo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setPreferredSize(new Dimension(100, 130));

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(nav, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(scrollResultado, BorderLayout.SOUTH);
        setContentPane(contenido);
        pack();
    }

    private static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Tema.acentoTexto());
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Tabla con su encabezado, sin scroll propio (el scroll es el de toda la ventana). */
    private static JPanel conEncabezado(JTable t) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createLineBorder(Tema.borde()));
        p.add(t.getTableHeader(), BorderLayout.NORTH);
        p.add(t, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JPanel bloqueMatriz(String nombre, JTable t) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();   // cada matriz con su tamaño, alineadas arriba
            }
        };
        p.setOpaque(false);
        JLabel l = titulo(nombre);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l, BorderLayout.NORTH);
        p.add(conEncabezado(t), BorderLayout.CENTER);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        return p;
    }

    /** Qué producto de matrices llena cada bloque del tablero (los colores de los apuntes). */
    private static JPanel leyenda() {
        Object[][] items = {
            {TableroVista.Bloque.B_INV_A, "B⁻¹·A  (columnas de las x)"},
            {TableroVista.Bloque.B_INV, "B⁻¹  (columnas de las s)"},
            {TableroVista.Bloque.B_INV_R, "B⁻¹·R = Sol"},
            {TableroVista.Bloque.ZJ_A, "Zj de las x = Cbᵀ·B⁻¹·A"},
            {TableroVista.Bloque.ZJ_S, "Zj de las s = Cbᵀ·B⁻¹"},
            {TableroVista.Bloque.ZJ_R, "Z = Cbᵀ·B⁻¹·R"},
            {TableroVista.Bloque.PJ, "Pj = Cj − Zj"},
        };
        JPanel p = new JPanel(new GridLayout(0, 4, 6, 6));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Object[] it : items) {
            // modernizarVentana pinta todos los JLabel con la tinta del tema: el chip es pastel, va siempre oscuro
            JLabel l = new JLabel((String) it[1], SwingConstants.CENTER) {
                @Override
                public Color getForeground() {
                    return Tema.textoSobreResaltado();
                }
            };
            l.setOpaque(true);
            l.setBackground(((TableroVista.Bloque) it[0]).color);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            p.add(l);
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    @Override
    public void setNombresVariables(MapeoVariables mapeo) {
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

    /** Lee el problema (mismas reglas que el Simplex) y lo resuelve con matrices. */
    public void calculos(String FO, String[] R, String tipo) {
        ParserLP.validarVariablesCompletas(FO, R);
        esMin = tipo != null && tipo.trim().equalsIgnoreCase("Min");
        String foLimpia = FO.replaceAll("\\s+", "");

        Matcher mfo = Pattern.compile("x(\\d+)", Pattern.CASE_INSENSITIVE).matcher(foLimpia);
        TreeSet<Integer> idxs = new TreeSet<>();
        while (mfo.find()) {
            idxs.add(Integer.parseInt(mfo.group(1)));
        }
        if (idxs.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron variables (x1, x2, ...) en la función objetivo.");
        }
        List<Integer> ordered = new ArrayList<>(idxs);
        numVars = ordered.size();

        Map<Integer, String> foCoefs = PrincipalPage.parseCoefficientsMap(foLimpia);
        c = new double[numVars];
        for (int k = 0; k < numVars; k++) {
            c[k] = parseNum(foCoefs.getOrDefault(ordered.get(k), "0"));
        }

        int m = R.length;
        A = new double[m][numVars];
        b = new double[m];
        for (int i = 0; i < m; i++) {
            String[] partes = PrincipalPage.parseRestriction(R[i]);
            if (!"<=".equals(partes[1])) {
                throw new IllegalArgumentException(
                        "El Simplex Revisado (como el Simplex) solo admite restricciones de tipo <=.\n"
                        + "La restricción " + (i + 1) + " (\"" + R[i] + "\") usa '" + partes[1] + "'.\n"
                        + "Prueba con Big M o Simplex Dual para este caso.");
            }
            b[i] = parseNum(partes[2]);
            if (b[i] < 0) {
                throw new IllegalArgumentException(
                        "La restricción " + (i + 1) + " tiene término independiente negativo.\n"
                        + "Prueba con Simplex Dual para este caso.");
            }
            Map<Integer, String> coefs = PrincipalPage.parseCoefficientsMap(partes[0]);
            for (int k = 0; k < numVars; k++) {
                A[i][k] = parseNum(coefs.getOrDefault(ordered.get(k), "0"));
            }
        }

        nombres = new String[numVars + m];
        for (int k = 0; k < numVars; k++) {
            nombres[k] = mapeo.nombreDe(ordered.get(k));
        }
        for (int i = 0; i < m; i++) {
            nombres[numVars + i] = "s" + (i + 1);
        }

        resultado = SimplexRevisado.resolver(c, A, b, esMin);

        // B⁻¹·[A | I | R] de cada iteración
        cuerpos.clear();
        for (SimplexRevisado.Iteracion it : resultado.iteraciones) {
            double[][] t = new double[m][nombres.length + 1];
            for (int j = 0; j < nombres.length; j++) {
                double[] col = Matrices.porVector(it.Binv, Matrices.columna(resultado.aEstandar, j));
                for (int i = 0; i < m; i++) {
                    t[i][j] = col[i];
                }
            }
            for (int i = 0; i < m; i++) {
                t[i][nombres.length] = it.xB[i];
            }
            cuerpos.add(t);
        }

        StringBuilder sb = new StringBuilder();
        if (resultado.noAcotado) {
            sb.append("El problema no tiene solución acotada (unbounded): la variable que entra puede crecer sin límite.");
        } else {
            sb.append("Solución óptima: \n");
            for (int k = 0; k < numVars; k++) {
                sb.append(nombres[k]).append(" = ").append(fmt(resultado.solucion[k])).append("\n");
            }
            sb.append("Z = ").append(fmt(resultado.z));
        }
        resultadoTexto = sb.toString();
        mostrarPaso(0);
    }

    private void mostrarPaso(int idx) {
        if (resultado == null || idx < 0 || idx >= resultado.iteraciones.size()) {
            return;
        }
        pasoActual = idx;
        SimplexRevisado.Iteracion it = resultado.iteraciones.get(idx);
        int total = resultado.iteraciones.size();
        int m = b.length;
        int cols = nombres.length;
        double[][] t = cuerpos.get(idx);

        double[] cj = new double[cols];
        System.arraycopy(c, 0, cj, 0, numVars);

        TableroVista.Datos d = new TableroVista.Datos();
        d.variables = nombres.clone();
        d.cj = new String[cols];
        d.bloqueColumna = new TableroVista.Bloque[cols];
        for (int j = 0; j < cols; j++) {
            d.cj[j] = fmt(cj[j]);
            d.bloqueColumna[j] = j < numVars ? TableroVista.Bloque.B_INV_A : TableroVista.Bloque.B_INV;
        }
        d.cb = new String[m];
        d.xb = new String[m];
        d.cuerpo = new String[m][cols];
        d.sol = new String[m];
        double[] zj = new double[cols];
        double z = 0;
        for (int i = 0; i < m; i++) {
            double cb = cj[it.base[i]];
            d.cb[i] = fmt(cb);
            d.xb[i] = nombres[it.base[i]];
            for (int j = 0; j < cols; j++) {
                d.cuerpo[i][j] = fmt(t[i][j]);
                zj[j] += cb * t[i][j];
            }
            d.sol[i] = fmt(t[i][cols]);
            z += cb * t[i][cols];
        }
        d.zj = new String[cols];
        d.pj = new String[cols];
        for (int j = 0; j < cols; j++) {
            d.zj[j] = fmt(zj[j]);
            d.pj[j] = fmt(cj[j] - zj[j]);
        }
        d.z = fmt(z);

        String info;
        if (it.optima) {
            info = esMin ? "Óptimo: todos los Pj ≥ 0" : "Óptimo: todos los Pj ≤ 0";
        } else {
            d.colPivote = it.entra;
            d.theta = new String[m];
            for (int i = 0; i < m; i++) {
                d.theta[i] = Double.isNaN(it.razones[i]) ? "—" : fmt(it.razones[i]);
            }
            if (it.noAcotado) {
                info = "Entra " + nombres[it.entra] + ": ningún θ válido, no acotado";
            } else {
                d.filaPivote = it.filaSale;
                info = "Entra " + nombres[it.entra] + ", sale " + nombres[it.base[it.filaSale]];
            }
        }
        if (idx > 0) {
            double[][] anterior = cuerpos.get(idx - 1);
            d.cambio = new boolean[m][cols + 1];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j <= cols; j++) {
                    d.cambio[i][j] = Math.abs(t[i][j] - anterior[i][j]) > 1e-6;
                }
            }
        }
        TableroVista.mostrar(tablaTablero, d);

        // B, B⁻¹ y Cbᵀ de esta iteración
        String[] nombresBase = new String[m];
        String[] holguras = new String[m];
        for (int i = 0; i < m; i++) {
            nombresBase[i] = nombres[it.base[i]];
            holguras[i] = nombres[numVars + i];
        }
        llenarMatriz(tablaB, nombresBase, it.B, null);
        llenarMatriz(tablaBinv, holguras, it.Binv, TableroVista.Bloque.B_INV.color);
        double[][] cbFila = {new double[m]};
        for (int i = 0; i < m; i++) {
            cbFila[0][i] = cj[it.base[i]];
        }
        llenarMatriz(tablaCb, nombresBase, cbFila, null);

        lblPaso.setText("Tablero " + (idx + 1) + " de " + total + "  —  " + info);
        btnAnterior.setEnabled(idx > 0);
        btnSiguiente.setEnabled(idx < total - 1);
        txtResultado.setText(textoPaso(it, idx, total));
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private static void llenarMatriz(JTable t, String[] encabezado, double[][] M, Color fondo) {
        Object[][] celdas = new Object[M.length][encabezado.length];
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < encabezado.length; j++) {
                celdas[i][j] = fmt(M[i][j]);
            }
        }
        t.setModel(new DefaultTableModel(celdas, encabezado) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, false, false, row, column);
                setHorizontalAlignment(CENTER);
                comp.setBackground(fondo != null ? fondo : Tema.campo());
                comp.setForeground(fondo != null ? Tema.textoSobreResaltado() : Tema.tinta());
                return comp;
            }
        });
        // matriz chica: ancho fijo por columna
        int ancho = 64 * encabezado.length;
        t.setPreferredScrollableViewportSize(new Dimension(ancho, t.getRowHeight() * M.length));
        for (int j = 0; j < encabezado.length; j++) {
            t.getColumnModel().getColumn(j).setPreferredWidth(64);
        }
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getTableHeader().setPreferredSize(new Dimension(ancho, 26));
    }

    private String textoPaso(SimplexRevisado.Iteracion it, int idx, int total) {
        double[] sol = new double[numVars];
        for (int r = 0; r < it.base.length; r++) {
            if (it.base[r] < numVars) {
                sol[it.base[r]] = it.xB[r];
            }
        }
        StringBuilder sb = new StringBuilder("Tablero ").append(idx + 1).append(" de ").append(total).append(":\n");
        for (int k = 0; k < numVars; k++) {
            sb.append(nombres[k]).append(" = ").append(fmt(sol[k])).append("\n");
        }
        sb.append("Z = ").append(fmt(it.z)).append("\n");
        if (it.noAcotado) {
            sb.append("No acotado: Z puede mejorar sin límite");
        } else {
            sb.append("Óptimo: ").append(it.optima ? "Sí" : Recursos.motivoNoOptimo(esMin));
        }
        return sb.toString();
    }

    private void mostrarSensibilidad() {
        if (resultado == null || resultado.noAcotado) {
            javax.swing.JOptionPane.showMessageDialog(this, "No hay una solución óptima que analizar.",
                    "Sensibilidad", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AnalisisSensibilidad s = AnalisisSensibilidad.calcular(c, A, b, esMin, resultado.baseFinal);
        String[] restricciones = new String[b.length];
        for (int i = 0; i < restricciones.length; i++) {
            restricciones[i] = "R" + (i + 1);
        }
        new VentanaSensibilidad(this, "Simplex Revisado", s, java.util.Arrays.copyOf(nombres, numVars), restricciones)
                .setVisible(true);
    }

    private static double parseNum(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String fmt(double v) {
        return FormatoNumero.fmt(v);
    }

    /** Ocupa el ancho de la ventana, salvo que el tablero sea más ancho (ahí aparece el scroll horizontal). */
    private static final class PanelAncho extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientacion, int direccion) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientacion, int direccion) {
            return 160;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() != null && getParent().getWidth() > getPreferredSize().width;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
