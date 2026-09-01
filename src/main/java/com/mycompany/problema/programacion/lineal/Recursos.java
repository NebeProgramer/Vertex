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
        UIManager.put("Panel.background", new java.awt.Color(246, 248, 251));
        UIManager.put("TextField.background", java.awt.Color.WHITE);
        UIManager.put("TextArea.background", java.awt.Color.WHITE);
        UIManager.put("TextPane.background", java.awt.Color.WHITE);
        UIManager.put("Button.background", new java.awt.Color(68, 56, 208));
        UIManager.put("Button.foreground", java.awt.Color.WHITE);
        UIManager.put("Button.hoverBackground", new java.awt.Color(85, 73, 224));
        UIManager.put("Button.pressedBackground", new java.awt.Color(52, 42, 170));
        UIManager.put("Component.focusColor", new java.awt.Color(68, 56, 208));
        UIManager.put("Component.borderColor", new java.awt.Color(205, 213, 223));
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

    /** Aplica el ícono de la app a una ventana; no hace nada si el recurso no se pudo cargar. */
    public static void aplicarIcono(java.awt.Window ventana) {
        Image icono = obtenerIcono();
        if (icono != null) {
            ventana.setIconImage(icono);
        }
    }

    /**
     * Instala en la ventana una JMenuBar vacía con un único botón "(?)"
     * pegado a la derecha (junto a minimizar/cerrar, gracias a las
     * decoraciones de ventana de FlatLaf). Al pasar el mouse por encima
     * muestra {@code contenidoAyudaHtml} como tooltip — igual que el patrón
     * ya usado en SubIndiceTextPane para los inputs de Z/restricciones, así
     * que el usuario que ya sabe qué significa cada color nunca lo ve.
     *
     * @param ventana           la JFrame donde se instala la barra
     * @param contenidoAyudaHtml texto ya envuelto en {@code <html>...</html>}
     */
    public static void instalarAyudaEnBarraTitulo(javax.swing.JFrame ventana, String contenidoAyudaHtml) {
        javax.swing.JButton boton = new javax.swing.JButton("(?)");
        boton.putClientProperty("JButton.buttonType", "toolBarButton");
        boton.setFocusable(false);
        boton.setToolTipText(contenidoAyudaHtml);

        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        barra.add(javax.swing.Box.createHorizontalGlue());
        barra.add(boton);
        ventana.setJMenuBar(barra);
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
        contenidoOriginal.setBackground(java.awt.Color.WHITE);
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

    private static final java.awt.Color TINTA = new java.awt.Color(23, 45, 66);
    private static final java.awt.Color ACENTO = new java.awt.Color(68, 56, 208);
    private static final java.awt.Color BORDE_SUAVE = new java.awt.Color(220, 226, 234);
    private static final java.awt.Color ENCABEZADO_TABLA = new java.awt.Color(245, 246, 250);

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
            if (c instanceof javax.swing.JLabel) {
                javax.swing.JLabel l = (javax.swing.JLabel) c;
                l.setForeground(TINTA);
            } else if (c instanceof javax.swing.JButton) {
                javax.swing.JButton b = (javax.swing.JButton) c;
                if (!"toolBarButton".equals(b.getClientProperty("JButton.buttonType"))) {
                    b.setFocusPainted(false);
                }
            } else if (c instanceof javax.swing.JTable) {
                javax.swing.JTable t = (javax.swing.JTable) c;
                t.setSelectionBackground(ACENTO);
                t.setSelectionForeground(java.awt.Color.WHITE);
                t.setGridColor(BORDE_SUAVE);
                if (t.getTableHeader() != null) {
                    t.getTableHeader().setBackground(ENCABEZADO_TABLA);
                    t.getTableHeader().setForeground(TINTA);
                    t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(java.awt.Font.BOLD));
                }
            } else if (c instanceof javax.swing.JScrollPane) {
                ((javax.swing.JScrollPane) c).setBorder(javax.swing.BorderFactory.createLineBorder(BORDE_SUAVE));
            } else if (c instanceof javax.swing.JTextArea) {
                javax.swing.JTextArea ta = (javax.swing.JTextArea) c;
                ta.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
            }
            if (c instanceof java.awt.Container) {
                modernizarVentana((java.awt.Container) c);
            }
        }
    }
}
