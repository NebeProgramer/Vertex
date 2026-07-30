package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;
import java.time.LocalDate;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Ventanita de donación que aparece al iniciar la app (Vertex es gratis).
 * No bloquea el uso del programa: se puede cerrar con la X o con
 * "Tal vez más tarde" y seguir trabajando normalmente.
 *
 * @author PC-ANDERSON
 */
public class VentanaDonacion extends JDialog {

    // TODO(Anderson): reemplaza esta URL por tu página de donaciones
    // (Ko-fi, PayPal.me, Buy Me a Coffee...) o por tu sitio personal con
    // tu información como programador y el enlace de donación ahí.
    private static final String URL_DONACION = "https://tu-pagina-de-donaciones-o-sitio-aqui.com";
    private static final String PREF_POSPUESTA_HASTA = "donacion_pospuesta_hasta";
    private static final int DIAS_POSPONER = 15;

    public VentanaDonacion(java.awt.Frame owner) {
        super(owner, "Apoya a Vertex", false);
        initComponents();
        Recursos.aplicarIcono(this);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 30, 6, 30);

        JLabel lblTaza = new JLabel("\u2615", SwingConstants.CENTER);
        lblTaza.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        gbc.gridy = 0;
        gbc.insets = new Insets(24, 30, 4, 30);
        getContentPane().add(lblTaza, gbc);

        JLabel lblTitulo = new JLabel("Invítame un café", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(40, 60, 110));
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 30, 12, 30);
        getContentPane().add(lblTitulo, gbc);

        JLabel lblVertex = new JLabel("Vertex es gratuito.", SwingConstants.CENTER);
        lblVertex.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVertex.setForeground(new Color(51, 102, 204));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 30, 8, 30);
        getContentPane().add(lblVertex, gbc);

        JTextArea txtMensaje = new JTextArea(
                "Si te ayudó con tus clases o tu trabajo,\npuedes apoyar el desarrollo mediante una donación.");
        txtMensaje.setEditable(false);
        txtMensaje.setFocusable(false);
        txtMensaje.setOpaque(false);
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMensaje.setForeground(new Color(80, 80, 80));
        // JTextArea no centra texto multi-línea por sí sola: forzamos con un panel centrado.
        javax.swing.JPanel panelTexto = new javax.swing.JPanel();
        panelTexto.setOpaque(false);
        panelTexto.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        panelTexto.add(txtMensaje);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 24, 20, 24);
        getContentPane().add(panelTexto, gbc);

        JButton btnDonar = new JButton("Donar");
        btnDonar.setBackground(new Color(51, 102, 204));
        btnDonar.setForeground(Color.WHITE);
        btnDonar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDonar.setFocusPainted(false);
        btnDonar.setPreferredSize(new java.awt.Dimension(160, 40));
        btnDonar.addActionListener(evt -> abrirEnlaceDonacion());
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 30, 8, 30);
        getContentPane().add(btnDonar, gbc);

        JButton btnMasTarde = new JButton("Tal vez más tarde");
        btnMasTarde.setBorderPainted(false);
        btnMasTarde.setContentAreaFilled(false);
        btnMasTarde.setForeground(new Color(130, 130, 130));
        btnMasTarde.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMasTarde.setFocusPainted(false);
        btnMasTarde.addActionListener(evt -> {
            posponer15Dias();
            dispose();
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 30, 20, 30);
        getContentPane().add(btnMasTarde, gbc);

        setSize(380, 400);
    }

    private void abrirEnlaceDonacion() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(URL_DONACION));
            } else {
                JOptionPane.showMessageDialog(this, "Visita: " + URL_DONACION, "Enlace de donación", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el navegador automáticamente.\nVisita manualmente:\n" + URL_DONACION,
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Guarda la fecha hasta la que no se debe volver a mostrar esta ventana (solo se llama desde "Tal vez más tarde", NO al cerrar con la X). */
    private void posponer15Dias() {
        Preferences prefs = Preferences.userNodeForPackage(VentanaDonacion.class);
        prefs.put(PREF_POSPUESTA_HASTA, LocalDate.now().plusDays(DIAS_POSPONER).toString());
    }

    /** true si ya pasaron los 15 días desde la última vez que se pospuso (o si nunca se pospuso). */
    public static boolean debeMostrarse() {
        Preferences prefs = Preferences.userNodeForPackage(VentanaDonacion.class);
        String fechaGuardada = prefs.get(PREF_POSPUESTA_HASTA, null);
        if (fechaGuardada == null) {
            return true;
        }
        try {
            LocalDate hasta = LocalDate.parse(fechaGuardada);
            return !LocalDate.now().isBefore(hasta);
        } catch (Exception ex) {
            return true;
        }
    }
}
