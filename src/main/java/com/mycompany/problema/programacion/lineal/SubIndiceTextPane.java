/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.problema.programacion.lineal;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * JTextPane que detecta variables tipo "x11", "x2", etc. y dibuja el
 * subíndice (los dígitos después de la x) en una fuente más pequeña y
 * desplazada hacia abajo, como notación matemática (x subíndice). En
 * cuanto aparece un caracter que no es dígito (+, -, *, <, =, espacio...)
 * vuelve al tamaño normal.
 *
 * Se usa igual que un JTextArea: getText(), addKeyListener(), etc.
 * siguen funcionando porque JTextPane también es un JTextComponent.
 */
public class SubIndiceTextPane extends JTextPane {

    private final SimpleAttributeSet normal = new SimpleAttributeSet();
    private final SimpleAttributeSet subindice = new SimpleAttributeSet();
    private boolean actualizando = false;

    public SubIndiceTextPane() {
        String familia = "Segoe UI";
        int tamNormal = 22;
        int tamSub = 12;

        StyleConstants.setFontFamily(normal, familia);
        StyleConstants.setFontSize(normal, tamNormal);
        StyleConstants.setSubscript(normal, false);

        StyleConstants.setFontFamily(subindice, familia);
        StyleConstants.setFontSize(subindice, tamSub);
        StyleConstants.setSubscript(subindice, true);

        setToolTipText("<html>Escribe coeficiente + x + subíndice, ej: <b>2x11 + 3x2</b>.<br>"
                + "El número que sigue a la x se verá en tamaño pequeño automáticamente.</html>");

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                programarFormato();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                programarFormato();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // cambios de estilo, no de texto: no hacer nada (evita bucles)
            }
        });
    }

    private void programarFormato() {
        if (actualizando) {
            return;
        }
        actualizando = true;
        SwingUtilities.invokeLater(() -> {
            try {
                aplicarFormatoSubindices();
            } finally {
                actualizando = false;
            }
        });
    }

    private void aplicarFormatoSubindices() {
        StyledDocument doc = getStyledDocument();
        String texto;
        try {
            texto = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            return;
        }
        int caret = Math.min(getCaretPosition(), texto.length());

        boolean enSubindice = false;
        int inicioSub = -1;

        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);

            if (!enSubindice && (ch == 'x' || ch == 'X')
                    && i + 1 < texto.length() && Character.isDigit(texto.charAt(i + 1))) {
                doc.setCharacterAttributes(i, 1, normal, true);
                enSubindice = true;
                inicioSub = i + 1;
                continue;
            }

            if (enSubindice) {
                if (Character.isDigit(ch)) {
                    continue; // se formatea de una vez al cerrar el tramo
                }
                doc.setCharacterAttributes(inicioSub, i - inicioSub, subindice, true);
                doc.setCharacterAttributes(i, 1, normal, true);
                enSubindice = false;
                continue;
            }

            doc.setCharacterAttributes(i, 1, normal, true);
        }

        if (enSubindice) {
            doc.setCharacterAttributes(inicioSub, texto.length() - inicioSub, subindice, true);
        }

        setCaretPosition(caret);
        // Asegura que lo próximo que se escriba salga en tamaño normal
        // (por si el cursor quedó justo después de un subíndice).
        setCharacterAttributes(normal, false);
    }

    private boolean soloUnaLinea = false;

    /**
     * Activa el modo "una sola línea": el texto nunca hace wrap vertical,
     * crece horizontalmente y el scroll (sin barra visible) sigue al
     * cursor. Se usa en la función objetivo (que siempre es una sola
     * fórmula) para que no estire los componentes vecinos. Las
     * restricciones (que sí necesitan varias líneas) no deben activar esto.
     */
    public void setSoloUnaLinea(boolean soloUnaLinea) {
        this.soloUnaLinea = soloUnaLinea;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return soloUnaLinea ? false : super.getScrollableTracksViewportWidth();
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return soloUnaLinea ? true : super.getScrollableTracksViewportHeight();
    }

    /**
     * En modo "una sola línea", si el texto es corto (o está vacío) el
     * ancho preferido natural del componente es diminuto — sin esto, el
     * resto del recuadro visible sería fondo del JViewport (gris, no
     * clickeable ni editable) en vez del propio componente. Forzamos que
     * nunca sea más angosto que el viewport visible; si el texto crece
     * más que eso, se respeta el ancho real para habilitar el scroll
     * horizontal.
     */
    @Override
    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension pref = super.getPreferredSize();
        if (soloUnaLinea && getParent() instanceof javax.swing.JViewport) {
            int anchoViewport = getParent().getWidth();
            if (pref.width < anchoViewport) {
                pref = new java.awt.Dimension(anchoViewport, pref.height);
            }
        }
        return pref;
    }
}
