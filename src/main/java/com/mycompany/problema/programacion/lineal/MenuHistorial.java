package com.mycompany.problema.programacion.lineal;

import com.mycompany.problema.programacion.lineal.algoritmos.Json_History;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Timer;

/**
 * Botón hamburguesa (esquina superior derecha de la ventana principal) que al
 * pasar el mouse despliega el historial de cálculos como overlay. El botón y el
 * panel cuentan como una sola "zona caliente": pasar de uno a otro (o navegar
 * la lista) nunca lo cierra; solo se cierra cuando el mouse sale de verdad de
 * ambos, con un pequeño margen de gracia para que el movimiento normal del
 * mouse entre los dos no lo dispare por accidente.
 */
public class MenuHistorial {

    private static final Pattern TERMINO = Pattern.compile("[+-]?\\s*\\d*\\.?\\d*x\\d+", Pattern.CASE_INSENSITIVE);
    private static final int GRACIA_CIERRE_MS = 280;
    private static final int DURACION_FADE_MS = 150;
    private static final int DURACION_SLIDE_MS = 220;

    private final Json_History historial;
    private final SubIndiceTextPane campoZ;
    private final JComboBox<String> tipoBox;
    private final SubIndiceTextPane campoRestricciones;
    private final JComboBox<String> algoritmoBox;

    private final BotonFade boton;
    private final JPanel panel;
    private final JList<JSONObject> lista;
    private final DefaultListModel<JSONObject> modelo = new DefaultListModel<>();
    private JLayeredPane capa;
    private javax.swing.JFrame ownerVentana;
    private Timer timerCierre;
    private Timer timerFade;
    private Timer timerSlide;

    /**
     * Ancho objetivo del panel abierto, y su posición ya calculada por
     * reposicionar().
     */
    private int anchoPanelObjetivo = 300;
    private int anchoPanelActual = 0;
    private int xDerechoPanel;
    private int yPanel;
    private int altoPanel;
    private boolean animandoApertura;
    /**
     * true mientras el popup de "Eliminar" (click derecho) está abierto;
     * bloquea cualquier cierre programado.
     */
    private boolean popupEliminarAbierto;

    public MenuHistorial(Json_History historial, SubIndiceTextPane campoZ, JComboBox<String> tipoBox,
            SubIndiceTextPane campoRestricciones, JComboBox<String> algoritmoBox) {
        this.historial = historial;
        this.campoZ = campoZ;
        this.tipoBox = tipoBox;
        this.campoRestricciones = campoRestricciones;
        this.algoritmoBox = algoritmoBox;

        this.boton = crearBoton();
        this.lista = crearLista();
        this.panel = crearPanel();

        instalarHover(boton);
        instalarHover(panel);
        instalarHover(lista);
    }

    /**
     * Agrega el botón y el panel al layered pane de la ventana, y deja todo
     * posicionado.
     */
    public void instalarEn(javax.swing.JFrame ownerVentana, JLayeredPane capa, int anchoVentana, int altoVentana) {
        this.ownerVentana = ownerVentana;
        this.capa = capa;
        capa.add(panel, JLayeredPane.PALETTE_LAYER);
        capa.add(boton, JLayeredPane.PALETTE_LAYER);
        panel.setVisible(false);
        reposicionar(anchoVentana, altoVentana);
        // el layered pane puede no tener aún su tamaño final (barra de título
        // custom de FlatLaf) al momento de instalar esto en el constructor de
        // la ventana; se reintenta una vez que ya esté realmente dimensionado.
        javax.swing.SwingUtilities.invokeLater(() -> reposicionar(anchoVentana, altoVentana));
    }

    /**
     * Se llama cada vez que la ventana cambia de tamaño, para mantener todo
     * pegado a la esquina. Los parámetros anchoVentana/altoVentana quedan solo
     * por compatibilidad — no se usan.
     *
     * En vez de adivinar dónde empieza el área de contenido (con Insets, que no
     * dio resultados confiables con la barra de título personalizada de
     * FlatLaf), se usa directamente {@code getContentPane().getBounds()} — es
     * la posición REAL que Swing ya calculó para el contenido dentro del
     * layered pane, así que es la única fuente de verdad correcta acá, sea cual
     * sea la decoración de la ventana.
     */
    public void reposicionar(int anchoVentana, int altoVentana) {
        if (capa == null || capa.getWidth() <= 0 || capa.getHeight() <= 0) {
            return;
        }
        java.awt.Rectangle area = (ownerVentana != null)
                ? ownerVentana.getContentPane().getBounds()
                : new java.awt.Rectangle(0, 0, capa.getWidth(), capa.getHeight());
        // por si el contentPane todavía no tiene bounds válidos en este momento
        if (area.width <= 0 || area.height <= 0) {
            area = new java.awt.Rectangle(0, 0, capa.getWidth(), capa.getHeight());
        }

        int margen = 16;
        int anchoBoton = 40;
        int altoBoton = 32;

        int xDerecho = area.x + area.width - margen;
        int yTop = area.y + margen;

        boton.setBounds(xDerecho - anchoBoton, yTop, anchoBoton, altoBoton);

        // el panel cubre todo el alto disponible por debajo del botón, con
        // su borde derecho pegado al mismo borde derecho del botón; el
        // ancho nunca supera el espacio realmente disponible (ventanas
        // angostas no dejan que el panel se salga ni tape toda la pantalla)
        xDerechoPanel = xDerecho;
        yPanel = yTop;
        altoPanel = Math.max(100, (area.y + area.height) - yTop - margen);
        int anchoMaximoDisponible = Math.max(60, area.width - margen * 2);
        anchoPanelObjetivo = Math.min(300, anchoMaximoDisponible);
        if (anchoPanelActual > anchoPanelObjetivo) {
            anchoPanelActual = anchoPanelObjetivo;
        }
        aplicarBoundsPanel();
    }

    // ---------------------------------------------------------------
    // Construcción de componentes
    // ---------------------------------------------------------------
    private BotonFade crearBoton() {
        BotonFade b = new BotonFade();
        b.setBackground(Color.WHITE);
        b.setFocusable(false);
        b.setToolTipText("Historial de cálculos");
        return b;
    }

    private JList<JSONObject> crearLista() {
        JList<JSONObject> l = new JList<JSONObject>(modelo) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int idx = locationToIndex(e.getPoint());
                if (idx < 0 || idx >= modelo.size()) {
                    return null;
                }
                return detalleCompleto(modelo.get(idx));
            }

            @Override
            public javax.swing.JToolTip createToolTip() {
                // mismo problema que el popup de "Eliminar": el tooltip es una
                // ventana flotante aparte, no parte de boton/panel/lista, así
                // que sin esto, mover el mouse hacia el tooltip para leerlo
                // cuenta como "salir" y cierra el menú solo.
                javax.swing.JToolTip tip = super.createToolTip();
                instalarHover(tip);
                return tip;
            }
        };
        l.setCellRenderer(new RendererHistorial());
        l.setFixedCellHeight(40);
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int idx = l.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        inyectar(modelo.get(idx));
                        cerrarInmediato();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenuSiAplica(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenuSiAplica(e);
            }

            private void mostrarMenuSiAplica(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int idx = l.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        l.setSelectedIndex(idx);
                        JSONObject obj = modelo.get(idx);
                        JPopupMenu menu = new JPopupMenu();
                        javax.swing.JMenuItem eliminar = new javax.swing.JMenuItem("Eliminar");
                        eliminar.addActionListener(ev -> {
                            historial.deleteElement(obj.getString("id"));
                            cargarLista();
                        });
                        menu.add(eliminar);
                        // el popup de "Eliminar" no forma parte de boton/panel/lista, así
                        // que el hover normal no lo detecta como "adentro" — sin esto, el
                        // menú se cerraba solo apenas se abría el popup de click derecho.
                        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

                            @Override
                            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent ev) {
                                popupEliminarAbierto = true;
                                cancelarCierre();
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent ev) {
                                popupEliminarAbierto = false;

                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    if (popupEliminarAbierto) {
                                        return;
                                    }

                                    boolean dentro = panel.getMousePosition(true) != null
                                            || boton.getMousePosition(true) != null;

                                    if (!dentro) {
                                        programarCierre();
                                    }
                                });
                            }

                            @Override
                            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent ev) {
                                // popupMenuWillBecomeInvisible() también se encargará del cierre.
                            }
                        });
                        menu.show(l, e.getX(), e.getY());
                    }
                }
            }
        });
        return l;
    }

    /**
     * Texto completo (Z, restricciones, tipo, algoritmo) para el tooltip
     * on-hover de cada fila.
     */
    private static String detalleCompleto(JSONObject obj) {
        StringBuilder sb = new StringBuilder("<html><b>");
        sb.append(obj.optString("Tipo", "")).append(" Z = ").append(obj.optString("z", "")).append("</b><br>");
        JSONArray r = obj.optJSONArray("R");
        if (r != null) {
            for (int i = 0; i < r.length(); i++) {
                sb.append(r.optString(i, "")).append("<br>");
            }
        }
        JSONArray metodos = obj.optJSONArray("Metodos");
        if (metodos != null && metodos.length() > 0) {
            sb.append("<i>Método: ");
            for (int i = 0; i < metodos.length(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(metodos.optString(i, ""));
            }
            sb.append("</i>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    private JPanel crearPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        JLabel titulo = new JLabel("Historial");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(new Color(40, 60, 110));
        titulo.setBorder(BorderFactory.createEmptyBorder(2, 4, 6, 4));
        p.add(titulo, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------
    // Hover: botón + panel + lista cuentan como una sola zona caliente
    // ---------------------------------------------------------------
    private void instalarHover(JComponent c) {
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelarCierre();
                abrir();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                programarCierre();
            }
        });
    }

    private void abrir() {
        if (animandoApertura || (panel.isVisible() && anchoPanelActual >= anchoPanelObjetivo)) {
            return;
        }
        cargarLista();
        panel.setVisible(true);
        panel.getParent().setComponentZOrder(panel, 0);
        animarPanel(true);
        animarFade(true);
    }

    private void cerrarInmediato() {
        cancelarCierre();
        if (timerSlide != null) {
            timerSlide.stop();
        }
        anchoPanelActual = 0;
        panel.setVisible(false);
        boton.setVisible(true);
        boton.setAlpha(1f);
    }

    private void programarCierre() {
        if (popupEliminarAbierto) {
            return;
        }

        cancelarCierre();

        timerCierre = new Timer(GRACIA_CIERRE_MS, e -> cerrar());
        timerCierre.setRepeats(false);
        timerCierre.start();
    }

    private void cancelarCierre() {
        if (timerCierre != null) {
            timerCierre.stop();
        }
    }

    private void cerrar() {
        // El popup tiene prioridad sobre cualquier intento de cierre.
        if (popupEliminarAbierto) {
            return;
        }

        animarPanel(false);
        animarFade(false);
    }

    /**
     * true = el botón se está abriendo (el botón se difumina hasta
     * desaparecer); false = se está cerrando (el botón reaparece).
     */
    private void animarFade(boolean abriendo) {
        if (timerFade != null) {
            timerFade.stop();
        }
        // el botón debe estar VISIBLE antes de animar en cualquiera de las
        // dos direcciones: si se está cerrando el panel, el botón viene de
        // haber quedado oculto (setVisible(false) al terminar el fade-out
        // anterior) y hay que devolverlo a visible ANTES de subirle el
        // alpha, o el fade-in nunca se ve (el componente ni se pinta).
        boton.setVisible(true);
        long inicio = System.currentTimeMillis();
        timerFade = new Timer(15, null);
        timerFade.addActionListener(e -> {
            float progreso = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) DURACION_FADE_MS);
            float alpha = abriendo ? (1f - progreso) : progreso;
            boton.setAlpha(alpha);
            if (progreso >= 1f) {
                timerFade.stop();
                if (abriendo) {
                    boton.setVisible(false);
                }
            }
        });
        timerFade.start();
    }

    /**
     * Anima el panel como un cajón: el borde DERECHO queda fijo (pegado al
     * botón) y el ancho crece de 0 a anchoPanelObjetivo al abrir (se despliega
     * hacia la izquierda), o decrece de vuelta a 0 al cerrar (se repliega hacia
     * la derecha, devolviéndolo a su posición/tamaño original). Cubre todo el
     * alto disponible de la ventana.
     */
    private void animarPanel(boolean abriendo) {
        if (timerSlide != null) {
            timerSlide.stop();
        }
        animandoApertura = abriendo;
        long inicio = System.currentTimeMillis();
        int anchoInicial = anchoPanelActual;
        timerSlide = new Timer(15, null);
        timerSlide.addActionListener(e -> {
            float progreso = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) DURACION_SLIDE_MS);
            int destino = abriendo ? anchoPanelObjetivo : 0;
            anchoPanelActual = Math.round(anchoInicial + (destino - anchoInicial) * progreso);
            aplicarBoundsPanel();
            if (progreso >= 1f) {
                timerSlide.stop();
                animandoApertura = false;
                if (!abriendo) {
                    panel.setVisible(false);
                }
            }
        });
        timerSlide.start();
    }

    private void aplicarBoundsPanel() {
        panel.setBounds(xDerechoPanel - anchoPanelActual, yPanel, Math.max(anchoPanelActual, 1), altoPanel);
        panel.revalidate();
    }

    // ---------------------------------------------------------------
    // Datos
    // ---------------------------------------------------------------
    private void cargarLista() {
        modelo.clear();
        List<JSONObject> elementos = historial.listarElementos();
        for (JSONObject obj : elementos) {
            modelo.addElement(obj);
        }
    }

    private void inyectar(JSONObject obj) {
        campoZ.setText(obj.optString("z", "").trim());
        String tipo = obj.optString("Tipo", "Max");
        tipoBox.setSelectedItem(tipo);

        JSONArray r = obj.optJSONArray("R");
        StringBuilder sb = new StringBuilder();
        if (r != null) {
            for (int i = 0; i < r.length(); i++) {
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append(r.optString(i, ""));
            }
        }
        campoRestricciones.setText(sb.toString());

        JSONArray metodos = obj.optJSONArray("Metodos");
        if (metodos != null && metodos.length() > 0) {
            String primero = metodos.optString(0, null);
            if (primero != null) {
                for (int i = 0; i < algoritmoBox.getItemCount(); i++) {
                    if (algoritmoBox.getItemAt(i).equals(primero)) {
                        algoritmoBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Primeros 2 términos de Z (ej. "3x1+2x2...") tal como se decidió para el
     * historial.
     */
    private static String truncarZ(String z) {
        if (z == null || z.isEmpty()) {
            return "";
        }
        Matcher m = TERMINO.matcher(z);
        int fin = -1;
        int contados = 0;
        while (m.find() && contados < 2) {
            fin = m.end();
            contados++;
        }
        String zTrim = z.trim();
        if (fin < 0 || fin >= zTrim.length()) {
            return zTrim;
        }
        return z.substring(0, fin).trim() + "...";
    }

    // ---------------------------------------------------------------
    // Renderer de cada fila: "3x1+2x2...  |  Simplex"
    // ---------------------------------------------------------------
    private static class RendererHistorial extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                String z = truncarZ(obj.optString("z", ""));
                JSONArray metodos = obj.optJSONArray("Metodos");
                String metodo = (metodos != null && metodos.length() > 0) ? metodos.optString(0, "") : "";
                label.setText("<html>" + z + "<br><small>" + metodo + "</small></html>");
                label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            }
            return label;
        }
    }

    // ---------------------------------------------------------------
    // Botón hamburguesa con soporte de difuminado (alpha)
    // ---------------------------------------------------------------
    private static class BotonFade extends JButton {

        private static final Color COLOR_BARRAS = new Color(68, 56, 208); // mismo acento del resto del rediseño

        private float alpha = 1f;

        BotonFade() {
            setContentAreaFilled(true);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        void setAlpha(float a) {
            this.alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_BARRAS);

            int anchoBarra = (int) (getWidth() * 0.5);
            int altoBarra = Math.max(2, getHeight() / 12);
            int x = (getWidth() - anchoBarra) / 2;
            int espacio = Math.max(altoBarra + 2, getHeight() / 5);
            int yCentro = getHeight() / 2;

            g2.fillRoundRect(x, yCentro - espacio - altoBarra / 2, anchoBarra, altoBarra, altoBarra, altoBarra);
            g2.fillRoundRect(x, yCentro - altoBarra / 2, anchoBarra, altoBarra, altoBarra, altoBarra);
            g2.fillRoundRect(x, yCentro + espacio - altoBarra / 2, anchoBarra, altoBarra, altoBarra, altoBarra);
            g2.dispose();
        }
    }
}
