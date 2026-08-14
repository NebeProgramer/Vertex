
package com.mycompany.problema.programacion.lineal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ActualizacionChecker {

    private static final String API_URL =
            "https://api.github.com/repos/NebeProgramer/Vertex/releases/latest";

    // Entrada de descarga: la página del portafolio, no el link crudo de GitHub.
    private static final String LINK_DESCARGA =
            "https://repositorio-aeop.onrender.com/desktop/Vertex";

    private ActualizacionChecker() {
    }

    /**
     * Lanza el chequeo en segundo plano. Si hay una versión nueva, muestra
     * el diálogo en el hilo de Swing. Si no hay internet, la API falla, o
     * el repo todavía no tiene ningún release publicado, no hace nada
     * (falla en silencio a propósito, para no molestar al usuario).
     *
     * @param padre ventana sobre la que centrar el diálogo (puede ser null)
     */
    public static void verificar(Component padre) {
        new Thread(() -> {
            try {
                String versionActual = obtenerVersionActual();
                if (versionActual == null) {
                    // Corriendo desde NetBeans/clases sueltas (sin jar empaquetado):
                    // no hay manifest del que leer la versión, así que no hay con
                    // qué comparar. No molestar.
                    return;
                }

                String json = obtenerJson(API_URL);
                String tag = extraerCampo(json, "tag_name");
                if (tag == null) {
                    return;
                }
                String descripcion = extraerCampo(json, "body");

                String ultimaVersion = tag.startsWith("v") ? tag.substring(1) : tag;

                int[] actual = parseVersion(versionActual);
                int[] nueva = parseVersion(ultimaVersion);
                int tipoCambio = compararVersiones(actual, nueva);
                if (tipoCambio > 0) {
                    SwingUtilities.invokeLater(() -> mostrarDialogo(padre, tag, tipoCambio, descripcion));
                }
            } catch (Exception e) {
                // Sin internet, timeout, o error de la API: no molestar.
            }
        }, "ActualizacionChecker").start();
    }

    /**
     * Convierte una versión "CambioGrande.CambioMenor.ArregloBugs[.Experimental]"
     * en sus 3 primeros números. El 4to segmento (experimental) se ignora a
     * propósito para esta comparación. Cualquier segmento faltante o no
     * numérico se toma como 0, para no romper el chequeo con versiones mal
     * formadas.
     */
    private static int[] parseVersion(String version) {
        String[] partes = version.split("\\.");
        int[] numeros = new int[3];
        for (int i = 0; i < 3 && i < partes.length; i++) {
            numeros[i] = parseNumeroInicial(partes[i]);
        }
        return numeros;
    }

    /** Toma solo los dígitos iniciales de un segmento (ej: "0-beta" -> 0). */
    private static int parseNumeroInicial(String segmento) {
        int fin = 0;
        while (fin < segmento.length() && Character.isDigit(segmento.charAt(fin))) {
            fin++;
        }
        if (fin == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(segmento.substring(0, fin));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Compara posición por posición (CambioGrande, luego CambioMenor, luego
     * ArregloBugs) y se detiene en la primera diferencia, igual que semver.
     *
     * @return 1 si difiere en CambioGrande, 2 si difiere en CambioMenor,
     *         3 si difiere en ArregloBugs, 0 si son iguales, -1 si la
     *         versión actual ya es igual o más nueva que la del tag.
     */
    private static int compararVersiones(int[] actual, int[] nueva) {
        for (int i = 0; i < 3; i++) {
            if (nueva[i] > actual[i]) {
                return i + 1;
            }
            if (nueva[i] < actual[i]) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * Lee la versión actual desde el manifest del jar (Implementation-Version,
     * puesta ahí por el pom.xml al empaquetar). Devuelve null si se está
     * corriendo desde NetBeans/clases sueltas, donde no existe ese manifest.
     */
    private static String obtenerVersionActual() {
        Package pkg = ActualizacionChecker.class.getPackage();
        return (pkg != null) ? pkg.getImplementationVersion() : null;
    }

    private static String obtenerJson(String urlStr) throws Exception {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Accept", "application/vnd.github+json");
        con.setConnectTimeout(4000);
        con.setReadTimeout(4000);

        // 404 = repo sin releases todavía; cualquier otro código != 200 lo
        // tratamos igual: no hay dato confiable para comparar.
        if (con.getResponseCode() != 200) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
        }
        return sb.toString();
    }

    /**
     * Extrae el valor de un campo string del JSON crudo. A diferencia de una
     * búsqueda ingenua de la comilla de cierre, respeta los caracteres
     * escapados (\", \\n, etc.) para no cortar el texto a la mitad cuando el
     * campo trae comillas o saltos de línea internos (como "body").
     */
    private static String extraerCampo(String json, String campo) {
        if (json == null) {
            return null;
        }
        String buscar = "\"" + campo + "\":\"";
        int inicio = json.indexOf(buscar);
        if (inicio == -1) {
            return null;
        }
        inicio += buscar.length();

        int fin = inicio;
        boolean cerrado = false;
        while (fin < json.length()) {
            char c = json.charAt(fin);
            if (c == '\\') {
                fin += 2; // saltar el caracter escapado, sea cual sea
                continue;
            }
            if (c == '"') {
                cerrado = true;
                break;
            }
            fin++;
        }
        if (!cerrado) {
            return null;
        }
        return desescaparJson(json.substring(inicio, fin));
    }

    private static String desescaparJson(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char siguiente = s.charAt(i + 1);
                switch (siguiente) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '/' -> { sb.append('/'); i++; }
                    case 'u' -> {
                        if (i + 5 < s.length()) {
                            try {
                                sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                    }
                    default -> sb.append(siguiente);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void mostrarDialogo(Component padre, String versionNueva, int tipoCambio, String descripcion) {
        String motivo = switch (tipoCambio) {
            case 1 -> "Es una actualización mayor: puede incluir cambios importantes en cómo funciona la app.";
            case 2 -> "Incluye funciones nuevas.";
            case 3 -> "Incluye correcciones de errores.";
            default -> "Hay cambios disponibles.";
        };

        Window ventanaPadre = (padre != null) ? SwingUtilities.getWindowAncestor(padre) : null;
        JDialog dialogo = new JDialog(ventanaPadre, "Actualización disponible", Dialog.ModalityType.APPLICATION_MODAL);

        JLabel mensaje = new JLabel("<html>Hay una nueva versión disponible: <b>" + escapeHtml(versionNueva) + "</b><br>"
                + escapeHtml(motivo) + "</html>");
        mensaje.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        boolean hayDescripcion = descripcion != null && !descripcion.isBlank();

        JLabel linkMasInfo = new JLabel("<html><a href=''>Mostrar más</a></html>");
        linkMasInfo.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        linkMasInfo.setVisible(hayDescripcion);
        if (hayDescripcion) {
            linkMasInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            linkMasInfo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    boolean actualizar = mostrarDetalle(dialogo, versionNueva, descripcion);
                    if (actualizar) {
                        abrirDescarga();
                        dialogo.dispose();
                    }
                }
            });
        }

        JButton btnSi = new JButton("Sí");
        JButton btnNo = new JButton("No");
        btnSi.addActionListener(e -> {
            abrirDescarga();
            dialogo.dispose();
        });
        btnNo.addActionListener(e -> dialogo.dispose());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnSi);
        panelBotones.add(btnNo);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(mensaje, BorderLayout.NORTH);
        panelPrincipal.add(linkMasInfo, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setContentPane(panelPrincipal);
        dialogo.setResizable(false);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true); // modal: bloquea aquí hasta que se llame dispose()
    }

    /**
     * Ventana aparte con la descripción completa del release (el "body" que
     * se escribe al publicar en GitHub) y sus propios botones Sí/No, para
     * que el usuario decida si actualizar después de ver qué trae la nueva
     * versión.
     *
     * @return true si el usuario eligió actualizar desde esta ventana.
     */
    private static boolean mostrarDetalle(Window padre, String versionNueva, String descripcion) {
        JDialog detalle = new JDialog(padre, "Novedades de la versión " + versionNueva, Dialog.ModalityType.APPLICATION_MODAL);

        JTextArea texto = new JTextArea(descripcion);
        texto.setEditable(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setMargin(new java.awt.Insets(10, 10, 10, 10));
        texto.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(texto);
        scroll.setPreferredSize(new Dimension(420, 320));

        boolean[] resultado = {false};
        JButton btnSi = new JButton("Sí, actualizar");
        JButton btnNo = new JButton("No, ahora no");
        btnSi.addActionListener(e -> {
            resultado[0] = true;
            detalle.dispose();
        });
        btnNo.addActionListener(e -> detalle.dispose());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnSi);
        panelBotones.add(btnNo);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        detalle.setContentPane(panel);
        detalle.setResizable(false);
        detalle.pack();
        detalle.setLocationRelativeTo(padre);
        detalle.setVisible(true); // modal: bloquea aquí hasta que se llame dispose()

        return resultado[0];
    }

    private static void abrirDescarga() {
        try {
            Desktop.getDesktop().browse(new URI(LINK_DESCARGA));
        } catch (Exception ex) {
            // No se pudo abrir el navegador; no hay mucho más que hacer.
        }
    }

    /** Escapa lo mínimo necesario para insertar texto dinámico dentro de un JLabel en HTML. */
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
