package com.mycompany.problema.programacion.lineal;

import com.mycompany.problema.programacion.lineal.ia.AnalizadorProblema;
import com.mycompany.problema.programacion.lineal.ia.ClienteOllama;
import com.mycompany.problema.programacion.lineal.ia.ResultadoAnalisis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

/**
 * Segunda pantalla de la ventana principal: el usuario escribe el problema
 * en texto libre y la IA local (Ollama) lo convierte al formato de Vertex.
 * El resultado se entrega por {@code alAnalizar}, que lo inyecta en el
 * Editor igual que un elemento del historial.
 */
public class PantallaTextoLibre extends JPanel {

    private static final Color TINTA = new Color(23, 45, 66);
    private static final Color ACENTO = new Color(68, 56, 208);
    private static final Color BORDE = new Color(220, 226, 234);
    private static final Color GRIS = new Color(95, 108, 123);
    private static final Color VERDE = new Color(40, 150, 70);
    private static final Color ROJO = new Color(200, 60, 60);
    private static final Color AMBAR = new Color(200, 130, 20);

    private final ClienteOllama motor = new ClienteOllama();
    private final Consumer<ResultadoAnalisis> alAnalizar;

    private static final String PLACEHOLDER = "Ej: Una carpintería fabrica mesas y sillas. Cada mesa deja 3 dólares"
            + " de ganancia y cada silla 5. Se pueden fabricar como mucho 4 mesas por día...";

    /** JTextArea con texto de ejemplo en gris mientras está vacía (FlatLaf solo lo hace en campos de una línea). */
    private final JTextArea enunciado = new JTextArea(12, 60) {
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (getDocument().getLength() > 0) {
                return;
            }
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(150, 160, 172));
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            Insets in = getInsets();
            int ancho = getWidth() - in.left - in.right;
            int y = in.top + fm.getAscent();
            StringBuilder linea = new StringBuilder();
            for (String palabra : PLACEHOLDER.split(" ")) {
                String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
                if (fm.stringWidth(prueba) > ancho && linea.length() > 0) {
                    g2.drawString(linea.toString(), in.left, y);
                    y += fm.getHeight();
                    linea = new StringBuilder(palabra);
                } else {
                    linea = new StringBuilder(prueba);
                }
            }
            g2.drawString(linea.toString(), in.left, y);
            g2.dispose();
        }
    };
    private final JLabel estado = new JLabel();
    private final JButton botonInstalar = crearBotonSecundario("Cómo instalar Ollama");
    private final JButton botonDescargar = crearBotonSecundario("Descargar modelo (≈2 GB)");
    private final JButton botonReintentar = crearBotonSecundario("Volver a comprobar");
    private final JProgressBar progreso = new JProgressBar();
    private final JLabel mensaje = new JLabel(" ");
    private final JButton botonLimpiar = crearBotonSecundario("Limpiar");
    private final JButton botonAnalizar = new JButton("Analizar");
    private final JButton botonCancelar = crearBotonSecundario("Cancelar");

    private SwingWorker<?, ?> trabajoActual;

    public PantallaTextoLibre(Consumer<ResultadoAnalisis> alAnalizar) {
        this.alAnalizar = alAnalizar;
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);
        gbc.gridy = 0;
        add(crearTarjetaEnunciado(), gbc);
        gbc.gridy = 1;
        add(crearTarjetaEstado(), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(crearFilaBotones(), gbc);
    }

    /** Se llama cada vez que el usuario abre esta pestaña. */
    public void alMostrar() {
        comprobarMotor();
        enunciado.requestFocusInWindow();
    }

    // ---------------------------------------------------------------
    // Construcción
    // ---------------------------------------------------------------
    private JPanel crearTarjetaEnunciado() {
        JPanel tarjeta = crearTarjeta();

        JLabel titulo = new JLabel("Describe tu problema");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(TINTA);
        // ancho fijo: sin él, el HTML pide todo el texto en una línea y GridBag
        // cae a los tamaños mínimos en ventanas medianas
        JLabel ayuda = new JLabel("<html><div style='width:700px'>Escríbelo como en un enunciado de clase: qué se"
                + " quiere maximizar o minimizar, con qué datos y qué límites hay. La IA lo traduce a Z y"
                + " restricciones, y luego puedes revisarlo en el Editor antes de calcular.</div></html>");
        ayuda.setForeground(GRIS);
        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setOpaque(false);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(ayuda, BorderLayout.CENTER);
        tarjeta.add(encabezado, BorderLayout.NORTH);

        enunciado.setLineWrap(true);
        enunciado.setWrapStyleWord(true);
        enunciado.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        enunciado.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "analizar");
        enunciado.getActionMap().put("analizar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (botonAnalizar.isEnabled()) {
                    analizar();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(enunciado);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(215, 222, 231)));
        scroll.setPreferredSize(new Dimension(770, 280));
        scroll.setMinimumSize(new Dimension(400, 180));
        tarjeta.add(scroll, BorderLayout.CENTER);

        mensaje.setForeground(ROJO);
        tarjeta.add(mensaje, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaEstado() {
        JPanel tarjeta = crearTarjeta();
        estado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tarjeta.add(estado, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        progreso.setPreferredSize(new Dimension(100, 6));
        progreso.setVisible(false);
        tarjeta.add(progreso, BorderLayout.SOUTH);
        acciones.add(botonInstalar);
        acciones.add(botonDescargar);
        acciones.add(botonReintentar);
        tarjeta.add(acciones, BorderLayout.EAST);

        botonInstalar.addActionListener(e -> abrirDescargaOllama());
        botonDescargar.addActionListener(e -> descargarModelo());
        botonReintentar.addActionListener(e -> comprobarMotor());
        mostrarEstado("Comprobando motor de IA…", GRIS);
        botonInstalar.setVisible(false);
        botonDescargar.setVisible(false);
        return tarjeta;
    }

    private JPanel crearFilaBotones() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        fila.setOpaque(false);

        botonAnalizar.setBackground(ACENTO);
        botonAnalizar.setForeground(Color.WHITE);
        botonAnalizar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botonAnalizar.setFocusPainted(false);
        botonAnalizar.setPreferredSize(new Dimension(140, 40));
        botonAnalizar.setToolTipText("Analizar el enunciado (Ctrl+Enter)");
        botonAnalizar.addActionListener(e -> analizar());

        botonLimpiar.setPreferredSize(new Dimension(120, 40));
        botonLimpiar.addActionListener(e -> limpiar());

        botonCancelar.setPreferredSize(new Dimension(120, 40));
        botonCancelar.setVisible(false);
        botonCancelar.addActionListener(e -> {
            if (trabajoActual != null) {
                trabajoActual.cancel(true);
            }
        });

        fila.add(botonLimpiar);
        fila.add(botonAnalizar);
        fila.add(botonCancelar);
        return fila;
    }

    private static JPanel crearTarjeta() {
        JPanel tarjeta = new JPanel(new BorderLayout(12, 10));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return tarjeta;
    }

    /** Botón blanco con borde (el botón por defecto de Vertex es el morado de acción principal). */
    static JButton crearBotonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(Color.WHITE);
        b.setForeground(TINTA);
        b.setFocusPainted(false);
        b.putClientProperty("FlatLaf.style", "hoverBackground: #EEF0FB; pressedBackground: #DDE1F5;"
                + " borderColor: #CDD5DF");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ---------------------------------------------------------------
    // Acciones
    // ---------------------------------------------------------------
    private void limpiar() {
        enunciado.setText("");
        mensaje.setText(" ");
        enunciado.requestFocusInWindow();
    }

    private void comprobarMotor() {
        botonReintentar.setEnabled(false);
        mostrarEstado("Comprobando motor de IA…", GRIS);
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                if (!motor.disponible()) {
                    return 0;
                }
                return motor.modeloInstalado() ? 2 : 1;
            }

            @Override
            protected void done() {
                botonReintentar.setEnabled(true);
                int resultado;
                try {
                    resultado = get();
                } catch (InterruptedException | ExecutionException ex) {
                    resultado = 0;
                }
                botonInstalar.setVisible(resultado == 0);
                botonDescargar.setVisible(resultado == 1);
                if (resultado == 0) {
                    mostrarEstado("Ollama no encontrado: instálalo y ábrelo para usar la IA", ROJO);
                } else if (resultado == 1) {
                    mostrarEstado("Ollama listo, falta descargar el modelo " + motor.getModelo(), AMBAR);
                } else {
                    mostrarEstado("IA lista · " + motor.getModelo(), VERDE);
                }
            }
        }.execute();
    }

    private void mostrarError(String texto) {
        String seguro = texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        mensaje.setText("<html><div style='width:700px'>" + seguro + "</div></html>");
    }

    private void mostrarEstado(String texto, Color color) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        estado.setText("<html><span style='color:" + hex + "'>●</span>&nbsp; " + texto + "</html>");
        estado.setForeground(TINTA);
    }

    private void abrirDescargaOllama() {
        try {
            Desktop.getDesktop().browse(new URI(ClienteOllama.LINK_DESCARGA));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Descarga Ollama desde " + ClienteOllama.LINK_DESCARGA,
                    "Ollama", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void descargarModelo() {
        ocupado(true);
        botonDescargar.setEnabled(false);
        progreso.setVisible(true);
        progreso.setIndeterminate(true);
        mostrarEstado("Descargando " + motor.getModelo() + "…", AMBAR);
        SwingWorker<Void, Double> trabajo = new SwingWorker<Void, Double>() {
            @Override
            protected Void doInBackground() throws Exception {
                motor.descargarModelo(this::publish);
                return null;
            }

            @Override
            protected void process(List<Double> avances) {
                double ultimo = avances.get(avances.size() - 1);
                progreso.setIndeterminate(ultimo < 0);
                if (ultimo >= 0) {
                    progreso.setValue((int) Math.round(ultimo * 100));
                    mostrarEstado("Descargando " + motor.getModelo() + "… " + progreso.getValue() + "%", AMBAR);
                }
            }

            @Override
            protected void done() {
                ocupado(false);
                botonDescargar.setEnabled(true);
                progreso.setVisible(false);
                try {
                    get();
                } catch (CancellationException ex) {
                    // el usuario canceló: se vuelve a comprobar abajo
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError("No se pudo descargar el modelo: " + causa(ex));
                }
                comprobarMotor();
            }
        };
        trabajoActual = trabajo;
        trabajo.execute();
    }

    private void analizar() {
        String texto = enunciado.getText().trim();
        if (texto.isEmpty()) {
            mostrarError("Escribe primero el enunciado del problema.");
            return;
        }
        mensaje.setText(" ");
        ocupado(true);
        progreso.setVisible(true);
        progreso.setIndeterminate(true);
        mostrarEstado("Analizando con " + motor.getModelo() + "… (puede tardar unos segundos)", ACENTO);

        SwingWorker<ResultadoAnalisis, Void> trabajo = new SwingWorker<ResultadoAnalisis, Void>() {
            @Override
            protected ResultadoAnalisis doInBackground() throws Exception {
                if (!motor.disponible()) {
                    throw new IllegalStateException("Ollama no está abierto. Instálalo o ábrelo y vuelve a intentarlo.");
                }
                if (!motor.modeloInstalado()) {
                    throw new IllegalStateException("Falta descargar el modelo " + motor.getModelo() + ".");
                }
                return new AnalizadorProblema(motor).analizar(texto);
            }

            @Override
            protected void done() {
                ocupado(false);
                progreso.setVisible(false);
                try {
                    ResultadoAnalisis resultado = get();
                    if (resultado.getProblema() == null) {
                        mostrarError("La IA no devolvió un modelo legible: " + resultado.getError());
                        comprobarMotor();
                        return;
                    }
                    mostrarEstado("IA lista · " + motor.getModelo(), VERDE);
                    alAnalizar.accept(resultado);
                } catch (CancellationException ex) {
                    mostrarError("Análisis cancelado.");
                    comprobarMotor();
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError(causa(ex));
                    comprobarMotor();
                }
            }
        };
        trabajoActual = trabajo;
        trabajo.execute();
    }

    private void ocupado(boolean si) {
        botonAnalizar.setEnabled(!si);
        botonLimpiar.setEnabled(!si);
        botonReintentar.setEnabled(!si);
        enunciado.setEditable(!si);
        botonCancelar.setVisible(si);
        revalidate();
    }

    private static String causa(Exception ex) {
        Throwable t = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        if (t instanceof java.net.http.HttpTimeoutException) {
            return "La IA tardó demasiado en responder. Prueba con un enunciado más corto o un modelo más liviano.";
        }
        if (t instanceof java.net.ConnectException) {
            return "No se pudo conectar con Ollama. ¿Está abierto?";
        }
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }
}
