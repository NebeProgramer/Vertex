package com.mycompany.problema.programacion.lineal;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;

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
}
