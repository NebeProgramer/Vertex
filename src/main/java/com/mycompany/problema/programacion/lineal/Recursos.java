package com.mycompany.problema.programacion.lineal;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * Carga centralizada del ícono y el logo de la app (Vertex) desde los
 * recursos del jar (src/main/resources), para no repetir la ruta en cada
 * ventana.
 */
public final class Recursos {

    private static Image iconoApp;
    private static ImageIcon logoApp;

    private Recursos() {
    }

    /** Paleta y métricas comunes para todas las ventanas de Vertex. */
    public static void instalarEstilo() {
        UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("TextComponent.margin", new java.awt.Insets(8, 10, 8, 10));
        UIManager.put("ComboBox.padding", new java.awt.Insets(5, 9, 5, 9));
        UIManager.put("Button.margin", new java.awt.Insets(8, 16, 8, 16));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Panel.background", Tema.fondoPanel());
        UIManager.put("TextField.background", Tema.campo());
        UIManager.put("TextArea.background", Tema.campo());
        UIManager.put("TextPane.background", Tema.campo());
        UIManager.put("EditorPane.background", Tema.campo());
        UIManager.put("Table.background", Tema.campo());
        UIManager.put("Button.background", new java.awt.Color(68, 56, 208));
        UIManager.put("Button.foreground", java.awt.Color.WHITE);
        UIManager.put("Button.hoverBackground", new java.awt.Color(85, 73, 224));
        UIManager.put("Button.pressedBackground", new java.awt.Color(52, 42, 170));
        UIManager.put("Component.focusColor", new java.awt.Color(68, 56, 208));
        UIManager.put("Component.borderColor", Tema.esOscuro() ? new java.awt.Color(80, 84, 94) : new java.awt.Color(205, 213, 223));
    }

    /** Ícono cuadrado para la barra de título / taskbar de cada ventana. */
    public static Image obtenerIcono() {
        if (iconoApp == null) {
            URL url = Recursos.class.getResource("/vertex-icon-256.png");
            if (url != null) {
                iconoApp = Toolkit.getDefaultToolkit().getImage(url);
            }
        }
        return iconoApp;
    }

    /** Logo horizontal (ícono + "VERTEX" + subtítulo) para el encabezado de la ventana principal. */
    public static ImageIcon obtenerLogo() {
        if (logoApp == null) {
            URL url = Recursos.class.getResource("/vertex-logo.png");
            if (url != null) {
                logoApp = new ImageIcon(url);
            }
        }
        return logoApp;
    }

    /**
     * Logo escalado a un ancho fijo (alto proporcional), listo para usar
     * directamente como ícono de un JLabel. Se separó de obtenerLogo() para
     * poder usarlo como "custom creation code" en el editor de NetBeans sin
     * que el código de escalado quede enterrado dentro de initComponents()
     * (que NetBeans reescribe por completo cada vez que se guarda el form).
     */
    public static ImageIcon obtenerLogoEscalado(int anchoPx) {
        ImageIcon original = obtenerLogo();
        if (original == null) {
            return null;
        }
        Image escalado = original.getImage().getScaledInstance(anchoPx, -1, Image.SCALE_SMOOTH);
        return new ImageIcon(escalado);
    }

    /**
     * Logo "ND" de ND Studios a una altura fija, para la firma "Un producto"
     * de la pantalla principal. Se escala a 1x y 2x para que se vea nítido
     * también con el escalado de pantalla de Windows (125 %, 150 %...).
     * Devuelve null si el recurso no se pudo cargar.
     */
    public static ImageIcon obtenerMarcaND(int altoPx) {
        URL url = Recursos.class.getResource("/ND-Studios-logos/png/nd-icon-light.png");
        if (url == null) {
            return null;
        }
        try {
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(url);
            int ancho = Math.round(altoPx * original.getWidth() / (float) original.getHeight());
            Image x1 = escalarSuave(original, ancho, altoPx);
            Image x2 = escalarSuave(original, ancho * 2, altoPx * 2);
            return new ImageIcon(new java.awt.image.BaseMultiResolutionImage(x1, x2));
        } catch (java.io.IOException ex) {
            return null;
        }
    }

    private static Image escalarSuave(Image original, int ancho, int alto) {
        java.awt.image.BufferedImage destino = new java.awt.image.BufferedImage(ancho, alto,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = destino.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        // getScaledInstance(SMOOTH) promedia bien al reducir mucho (1200 px → 14 px)
        g.drawImage(original.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        return destino;
    }

    /** Aplica el ícono de la app a una ventana; no hace nada si el recurso no se pudo cargar. */
    public static void aplicarIcono(java.awt.Window ventana) {
        Image icono = obtenerIcono();
        if (icono != null) {
            ventana.setIconImage(icono);
        }
    }

    /** Símbolos que usan los textos "Factible / Óptimo" de los tableros. */
    private static final String SIMBOLOS = "∃≤≥";

    /**
     * La misma fuente (misma familia, estilo y tamaño), pero con respaldo
     * para los caracteres que no trae: Consolas y Segoe UI no tienen el ∃
     * de {@link #motivoNoOptimo} y Swing lo dibujaría como un cuadro.
     * StyleContext arma una fuente compuesta que sigue siendo Consolas (o
     * Segoe UI) y toma solo los glifos que faltan de otra fuente del sistema.
     * deriveFont le quita el UIResource, para que FlatLaf no la reemplace al
     * cambiar entre tema claro y oscuro.
     */
    public static java.awt.Font fuenteConSimbolos(java.awt.Font base) {
        if (base.canDisplayUpTo(SIMBOLOS) == -1) {
            return base;
        }
        java.awt.Font compuesta = javax.swing.text.StyleContext.getDefaultStyleContext()
                .getFont(base.getFamily(), base.getStyle(), base.getSize());
        return compuesta.canDisplayUpTo(SIMBOLOS) == -1
                ? compuesta.deriveFont(base.getStyle(), base.getSize2D()) : base;
    }

    /**
     * Por qué un tablero todavía no es óptimo, con el criterio de los
     * P<sub>j</sub> = C<sub>j</sub> − Z<sub>j</sub>: en Zmax queda alguno
     * &gt; 0, en Zmin alguno &lt; 0.
     */
    public static String motivoNoOptimo(boolean esMin) {
        return "No — ∃ Pj " + (esMin ? "< 0" : "> 0");
    }

    /**
     * Instala en la ventana una JMenuBar vacía con un único botón "(?)"
     * pegado a la derecha (junto a minimizar/cerrar, gracias a las
     * decoraciones de ventana de FlatLaf). Al pasar el mouse por encima
     * muestra {@code contenidoAyudaHtml} como tooltip — igual que el patrón
     * ya usado en SubIndiceTextPane para los inputs de Z/restricciones, así
     * que el usuario que ya sabe qué significa cada color nunca lo ve. Al
     * hacer clic, abre una subventana (ver {@link VentanaAyudaAlgoritmo})
     * con esa misma colorimetría y una explicación teórica básica de cómo
     * funciona el algoritmo de esa ventana.
     *
     * @param ventana            la JFrame donde se instala la barra
     * @param tituloAlgoritmo    nombre corto del algoritmo (p. ej. "Simplex"),
     *                           usado como título de la subventana de ayuda
     * @param contenidoAyudaHtml colorimetría, ya envuelta en {@code <html>...</html>}
     * @param teoriaHtml         explicación teórica del algoritmo, como fragmento HTML
     *                           (sin las etiquetas {@code <html>}/{@code <body>})
     */
    public static void instalarAyudaEnBarraTitulo(javax.swing.JFrame ventana, String tituloAlgoritmo,
            String contenidoAyudaHtml, String teoriaHtml) {
        javax.swing.JButton boton = new javax.swing.JButton("(?)");
        boton.putClientProperty("JButton.buttonType", "toolBarButton");
        boton.setFocusable(false);
        boton.setToolTipText(contenidoAyudaHtml);
        // sin esto, el botón hereda el color por defecto de FlatLaf para
        // toolBarButton, que en la barra de título (blanca) queda
        // prácticamente invisible (texto claro sobre fondo blanco)
        boton.setForeground(Tema.tinta());
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.addActionListener(evt
                -> new VentanaAyudaAlgoritmo(ventana, tituloAlgoritmo, contenidoAyudaHtml, teoriaHtml).setVisible(true));

        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        barra.add(javax.swing.Box.createHorizontalGlue());
        barra.add(boton);
        ventana.setJMenuBar(barra);
    }

    /**
     * Agrega un botón de texto a la barra de título, a la izquierda del
     * "(?)" (por ejemplo "Sensibilidad"). Llamar después de
     * {@link #instalarAyudaEnBarraTitulo}.
     */
    public static javax.swing.JButton agregarBotonBarraTitulo(javax.swing.JFrame ventana, String texto,
            String tooltip, Runnable accion) {
        javax.swing.JButton boton = new javax.swing.JButton(texto);
        boton.putClientProperty("JButton.buttonType", "toolBarButton");
        boton.setFocusable(false);
        boton.setToolTipText(tooltip);
        boton.setForeground(Tema.acentoTexto());
        boton.setFont(boton.getFont().deriveFont(java.awt.Font.BOLD));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.addActionListener(evt -> accion.run());
        javax.swing.JMenuBar barra = ventana.getJMenuBar();
        // justo antes del "(?)", que es el último componente de la barra
        barra.add(boton, Math.max(0, barra.getComponentCount() - 1));
        barra.revalidate();
        return boton;
    }

    /**
     * Envuelve todo el contentPane ya armado por initComponents() (con su
     * GroupLayout intacto) dentro de un fondo azul plano, tratándolo como
     * una sola "tarjeta blanca" con margen — igual que se hizo con
     * jPanel1 en PrincipalPage, pero acá no hay un sub-panel separado que
     * extraer, así que se envuelve el contentPane completo.
     *
     * Debe llamarse DESPUÉS de initComponents() en el constructor de cada
     * ventana, nunca dentro de initComponents (que NetBeans regenera).
     */
    public static void envolverEnFondoAzul(javax.swing.JFrame ventana) {
        javax.swing.JPanel contenidoOriginal = (javax.swing.JPanel) ventana.getContentPane();
        contenidoOriginal.setBackground(Tema.tarjeta());
        contenidoOriginal.setOpaque(true);

        FondoAzulPlano fondo = new FondoAzulPlano();
        fondo.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new java.awt.Insets(16, 16, 16, 16);
        fondo.add(contenidoOriginal, gbc);

        ventana.setContentPane(fondo);
        ventana.revalidate();
        ventana.repaint();
    }

    /**
     * Recorre recursivamente el árbol de componentes de una ventana ya
     * armada y le aplica la misma paleta moderna que ya se usa en
     * PrincipalPage (tinta/acento/tarjeta con borde suave), sin necesidad
     * de conocer los nombres de campo específicos de cada una de las 7
     * ventanas — solo mira el TIPO de cada componente, así que es seguro
     * de llamar sobre cualquier ventana sin arriesgar el layout.
     *
     * Llamar DESPUÉS de envolverEnFondoAzul(), para que alcance a estilizar
     * el contenido real (que a esa altura ya quedó reparentado adentro).
     */
    public static void modernizarVentana(java.awt.Container contenedor) {
        for (java.awt.Component c : contenedor.getComponents()) {
            if (c instanceof javax.swing.JPanel && !(c instanceof FondoAzulPlano) && ((javax.swing.JPanel) c).isOpaque()) {
                c.setBackground(Tema.tarjeta());
            }
            if (c instanceof javax.swing.JLabel) {
                javax.swing.JLabel l = (javax.swing.JLabel) c;
                l.setForeground(Tema.tinta());
            } else if (c instanceof javax.swing.JButton) {
                javax.swing.JButton b = (javax.swing.JButton) c;
                if (!"toolBarButton".equals(b.getClientProperty("JButton.buttonType"))) {
                    b.setFocusPainted(false);
                }
            } else if (c instanceof javax.swing.JTable) {
                javax.swing.JTable t = (javax.swing.JTable) c;
                t.setSelectionBackground(Tema.acento());
                t.setSelectionForeground(java.awt.Color.WHITE);
                t.setGridColor(Tema.borde());
                t.setBackground(Tema.campo());
                t.setForeground(Tema.tinta());
                if (t.getTableHeader() != null) {
                    t.getTableHeader().setBackground(Tema.encabezadoTabla());
                    t.getTableHeader().setForeground(Tema.tinta());
                    t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(java.awt.Font.BOLD));
                }
            } else if (c instanceof javax.swing.JScrollPane) {
                ((javax.swing.JScrollPane) c).setBorder(javax.swing.BorderFactory.createLineBorder(Tema.borde()));
                ((javax.swing.JScrollPane) c).getViewport().setBackground(Tema.campo());
            } else if (c instanceof javax.swing.JTextArea) {
                javax.swing.JTextArea ta = (javax.swing.JTextArea) c;
                // con respaldo para el ∃ de los textos "Factible / Óptimo"
                ta.setFont(fuenteConSimbolos(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13)));
                ta.setBackground(Tema.campo());
                ta.setForeground(Tema.tinta());
            }
            if (c instanceof java.awt.Container) {
                modernizarVentana((java.awt.Container) c);
            }
        }
    }
}
