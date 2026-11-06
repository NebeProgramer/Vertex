package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.util.prefs.Preferences;

/**
 * Colores de Vertex según su significado (tarjeta, texto, borde...), en
 * versión clara y oscura. Las ventanas piden el color aquí en vez de usar
 * Color.WHITE o un RGB fijo, así el modo oscuro no deja texto negro sobre
 * fondo negro. Se leen al construir cada ventana: al cambiar de tema, la
 * ventana principal se vuelve a crear (ver PrincipalPage.alternarTema).
 *
 * Los colores de la colorimetría (pivote naranja, amarillo, verdes...) NO
 * cambian con el tema, para que la tabla del README siga siendo válida;
 * encima de ellos se usa siempre {@link #textoSobreResaltado()}.
 */
public final class Tema {

    private static final String PREF_OSCURO = "modoOscuro";
    private static boolean oscuro = Preferences.userNodeForPackage(Tema.class).getBoolean(PREF_OSCURO, false);

    private Tema() {
    }

    public static boolean esOscuro() {
        return oscuro;
    }

    /** Cambia el tema y lo recuerda para la próxima vez que se abra Vertex. */
    public static void setOscuro(boolean valor) {
        oscuro = valor;
        Preferences.userNodeForPackage(Tema.class).putBoolean(PREF_OSCURO, valor);
    }

    /** Activa el look and feel de FlatLaf del tema actual y la paleta de Vertex. */
    public static void aplicarLookAndFeel() {
        if (oscuro) {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } else {
            com.formdev.flatlaf.FlatLightLaf.setup();
        }
        Recursos.instalarEstilo();
    }

    private static Color elegir(Color claro, Color osc) {
        return oscuro ? osc : claro;
    }

    /** Fondo de las tarjetas y del contenido de las ventanas. */
    public static Color tarjeta() {
        return elegir(Color.WHITE, new Color(43, 45, 50));
    }

    /** Fondo general de los paneles (detrás de las tarjetas). */
    public static Color fondoPanel() {
        return elegir(new Color(246, 248, 251), new Color(32, 33, 37));
    }

    /** Fondo de los campos de texto y de las celdas normales de las tablas. */
    public static Color campo() {
        return elegir(Color.WHITE, new Color(36, 38, 43));
    }

    /** Texto principal. */
    public static Color tinta() {
        return elegir(new Color(23, 45, 66), new Color(226, 232, 240));
    }

    /** Texto secundario (ayudas, firma, placeholders). */
    public static Color textoSuave() {
        return elegir(new Color(110, 122, 136), new Color(150, 160, 175));
    }

    public static Color borde() {
        return elegir(new Color(220, 226, 234), new Color(64, 68, 76));
    }

    /** Morado de Vertex para fondos de botones principales (el texto encima va en blanco). */
    public static Color acento() {
        return new Color(68, 56, 208);
    }

    /** Morado para TEXTO: en oscuro se aclara, porque el original no se lee sobre fondo oscuro. */
    public static Color acentoTexto() {
        return elegir(new Color(68, 56, 208), new Color(160, 150, 255));
    }

    /** Fondo de la ventana principal y del marco de las ventanas de resultados. */
    public static Color fondoVentana() {
        return elegir(new Color(68, 56, 208), new Color(26, 23, 60));
    }

    public static Color encabezadoTabla() {
        return elegir(new Color(245, 246, 250), new Color(52, 54, 61));
    }

    /** Botones secundarios (Limpiar, Cancelar...): al pasar el mouse y al presionarlos. */
    public static Color hoverSecundario() {
        return elegir(new Color(238, 240, 251), new Color(58, 56, 90));
    }

    public static Color avisoFondo(boolean esError) {
        if (esError) {
            return elegir(new Color(255, 241, 241), new Color(70, 36, 40));
        }
        return elegir(new Color(238, 240, 251), new Color(44, 42, 78));
    }

    public static Color avisoBorde(boolean esError) {
        if (esError) {
            return elegir(new Color(242, 196, 196), new Color(140, 70, 78));
        }
        return elegir(new Color(201, 204, 242), new Color(90, 86, 160));
    }

    /** Texto encima de un resaltado de la colorimetría (fondos pastel): siempre oscuro. */
    public static Color textoSobreResaltado() {
        return new Color(23, 45, 66);
    }

    /** "#RRGGBB" para usar el color en HTML. */
    public static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
