package com.mycompany.problema.programacion.lineal;

import com.mycompany.problema.programacion.lineal.algoritmos.AnalisisSensibilidad;
import com.mycompany.problema.programacion.lineal.algoritmos.FormatoNumero;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Tablas del análisis de sensibilidad de la solución óptima (variables y
 * restricciones), con el mismo estilo que la ayuda teórica. Se abre desde
 * el botón "Sensibilidad" de la barra de título del Simplex y del Simplex
 * Revisado.
 */
public class VentanaSensibilidad extends JDialog {

    public VentanaSensibilidad(java.awt.Window owner, String metodo, AnalisisSensibilidad s,
            String[] nombresVariables, String[] nombresRestricciones) {
        super(owner, "Análisis de sensibilidad — " + metodo, ModalityType.MODELESS);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.tarjeta());
        getContentPane().add(encabezado(metodo, s), BorderLayout.NORTH);
        getContentPane().add(cuerpo(s, nombresVariables, nombresRestricciones), BorderLayout.CENTER);
        getContentPane().add(pie(), BorderLayout.SOUTH);
        instalarAyudaEnBarraTitulo();
        Recursos.aplicarIcono(this);
        setMinimumSize(new Dimension(640, 480));
        setSize(1040, 640);
        setLocationRelativeTo(owner);
    }

    private JPanel encabezado(String metodo, AnalisisSensibilidad s) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Tema.acento());
        p.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        JLabel sub = new JLabel("ANÁLISIS DE SENSIBILIDAD — " + metodo.toUpperCase());
        sub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sub.setForeground(new Color(220, 216, 255));
        gbc.gridy = 0;
        p.add(sub, gbc);
        JLabel titulo = new JLabel("¿Qué pasa si cambian los datos?   (Z óptimo = " + FormatoNumero.fmt(s.z) + ")");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        gbc.gridy = 1;
        p.add(titulo, gbc);
        return p;
    }

    /**
     * "(?)" en la barra de título, igual que en las ventanas de los métodos
     * (ver Recursos.instalarAyudaEnBarraTitulo, que es solo para JFrame).
     */
    private void instalarAyudaEnBarraTitulo() {
        JButton ayuda = new JButton("(?)");
        ayuda.putClientProperty("JButton.buttonType", "toolBarButton");
        ayuda.setFocusable(false);
        ayuda.setForeground(Tema.tinta());
        ayuda.setToolTipText("Qué significa cada columna y cómo se calcula");
        ayuda.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        ayuda.addActionListener(e -> new VentanaAyudaAlgoritmo(this, "Análisis de sensibilidad", null,
                VentanaAyudaAlgoritmo.TEORIA_SENSIBILIDAD).setVisible(true));
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        barra.add(javax.swing.Box.createHorizontalGlue());
        barra.add(ayuda);
        setJMenuBar(barra);
    }

    private JScrollPane cuerpo(AnalisisSensibilidad s, String[] vars, String[] restr) {
        JPanel p = new PanelAnchoVentana(new GridBagLayout());
        p.setBackground(Tema.tarjeta());
        p.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);

        gbc.gridy = 0;
        p.add(subtitulo("Variables de decisión"), gbc);
        Object[][] filasV = new Object[vars.length][];
        for (int j = 0; j < vars.length; j++) {
            filasV[j] = new Object[]{vars[j], f(s.valor[j]), f(s.costoReducido[j]), f(s.coeficiente[j]),
                f(s.aumentoCoeficiente[j]), f(s.disminucionCoeficiente[j]),
                rango(s.coeficiente[j], s.disminucionCoeficiente[j], s.aumentoCoeficiente[j])};
        }
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        p.add(tabla(filasV, new String[]{"Variable", "Valor", "Costo reducido", "Coef. en Z",
            "Aumento permisible", "Disminución permisible", "Rango del coeficiente"}), gbc);

        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.gridy = 3;
        p.add(subtitulo("Restricciones"), gbc);
        Object[][] filasR = new Object[restr.length][];
        for (int i = 0; i < restr.length; i++) {
            filasR[i] = new Object[]{restr[i], f(s.holgura[i]), f(s.precioSombra[i]), f(s.ladoDerecho[i]),
                f(s.aumentoLadoDerecho[i]), f(s.disminucionLadoDerecho[i]),
                rango(s.ladoDerecho[i], s.disminucionLadoDerecho[i], s.aumentoLadoDerecho[i])};
        }
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        p.add(tabla(filasR, new String[]{"Restricción", "Holgura", "Precio sombra", "Lado derecho",
            "Aumento permisible", "Disminución permisible", "Rango del lado derecho"}), gbc);

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(Tema.acentoTexto());
        return l;
    }

    private JScrollPane tabla(Object[][] filas, String[] columnas) {
        JTable t = new JTable(new DefaultTableModel(filas, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        t.setRowHeight(28);
        // solo lectura y sin selección (ni filas, ni celdas, ni foco)
        t.setRowSelectionAllowed(false);
        t.setColumnSelectionAllowed(false);
        t.setFocusable(false);
        t.getTableHeader().setReorderingAllowed(false);
        t.setFont(new Font("Consolas", Font.PLAIN, 13));
        t.setBackground(Tema.campo());
        t.setForeground(Tema.tinta());
        t.setGridColor(Tema.borde());
        t.getTableHeader().setBackground(Tema.encabezadoTabla());
        t.getTableHeader().setForeground(Tema.tinta());
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        t.setDefaultRenderer(Object.class, centrado);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(Tema.borde()));
        sp.getViewport().setBackground(Tema.campo());
        // ancho mínimo: GridBag la estira al ancho de la ventana (sin barra horizontal)
        sp.setPreferredSize(new Dimension(200, 38 + 28 * Math.max(1, filas.length)));
        sp.setMinimumSize(new Dimension(200, 38 + 28 * Math.max(1, filas.length))); // nunca aplastada a una fila
        return sp;
    }

    private JPanel pie() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setBackground(Tema.tarjeta());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.borde()),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        JButton cerrar = new JButton("Cerrar");
        cerrar.setBackground(Tema.acento());
        cerrar.setForeground(Color.WHITE);
        cerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cerrar.setFocusPainted(false);
        cerrar.addActionListener(e -> dispose());
        p.add(cerrar);
        return p;
    }

    /** Panel que se ajusta al ancho visible del scroll: las tablas se estiran o encogen, sin barra horizontal. */
    private static final class PanelAnchoVentana extends JPanel implements javax.swing.Scrollable {

        PanelAnchoVentana(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientacion, int direccion) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientacion, int direccion) {
            return visible.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static String f(double v) {
        return Double.isInfinite(v) ? "∞" : FormatoNumero.fmt(v);
    }

    /** "[0, 7.500]" o "[2, ∞)" a partir del valor actual y los cambios permisibles. */
    private static String rango(double actual, double disminucion, double aumento) {
        String bajo = Double.isInfinite(disminucion) ? "(−∞" : "[" + FormatoNumero.fmt(actual - disminucion);
        String alto = Double.isInfinite(aumento) ? "∞)" : FormatoNumero.fmt(actual + aumento) + "]";
        return bajo + ", " + alto;
    }
}
