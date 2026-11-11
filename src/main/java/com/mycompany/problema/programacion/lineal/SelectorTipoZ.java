package com.mycompany.problema.programacion.lineal;

/**
 * La "Z" de la función objetivo con sus dos índices clicables, como se
 * escribe en clase:
 *
 * <pre>
 *  Z ᴾ      superíndice: P (primal) ⇄ D (dual)
 *    ᴹᵃˣ    subíndice:   Max ⇄ Min
 * </pre>
 *
 * Reemplaza al combo Max/Min de la ventana principal, pero el combo
 * ({@code ZType}) sigue existiendo como modelo oculto —igual que
 * {@code AlgoritmoBox} con {@link SeleccionMultiAlgoritmo}—: el subíndice
 * solo cambia su selección y se redibuja cuando él cambia. Así el
 * historial, el cambio de tema y Calcular siguen leyendo el tipo del combo
 * como siempre.
 *
 * El superíndice no decide nada por su cuenta: avisa a PrincipalPage, que
 * es quien arma el dual (ver {@code alternarPrimalDual}) y luego llama a
 * {@link #setModoDual}.
 */
public class SelectorTipoZ extends javax.swing.JPanel {

    private final javax.swing.JComboBox<String> tipoBox;
    private final javax.swing.JLabel letraZ = new javax.swing.JLabel("Z");
    private final javax.swing.JLabel superindice = new javax.swing.JLabel("P");
    private final javax.swing.JLabel subindice = new javax.swing.JLabel("Max");
    private boolean dual = false;

    /**
     * @param tipoBox    combo con "Max" y "Min" (índices 0 y 1)
     * @param alClicModo qué hacer al hacer clic en P/D
     */
    public SelectorTipoZ(javax.swing.JComboBox<String> tipoBox, Runnable alClicModo) {
        super(new java.awt.GridBagLayout());
        this.tipoBox = tipoBox;
        setOpaque(false);

        letraZ.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        letraZ.setForeground(Tema.tinta());
        java.awt.Font chica = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
        prepararIndice(superindice, chica, alClicModo);
        prepararIndice(subindice, chica, () -> tipoBox.setSelectedIndex(tipoBox.getSelectedIndex() == 0 ? 1 : 0));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        add(letraZ, gbc);

        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.insets = new java.awt.Insets(0, 2, 0, 0);
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(superindice, gbc);

        gbc.gridy = 1;
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(subindice, gbc);

        tipoBox.addActionListener(e -> refrescar());
        refrescar();
    }

    private void prepararIndice(javax.swing.JLabel etiqueta, java.awt.Font fuente, Runnable accion) {
        etiqueta.setFont(fuente);
        etiqueta.setForeground(Tema.acentoTexto());
        etiqueta.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        etiqueta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    accion.run();
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                etiqueta.setForeground(Tema.acento());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                etiqueta.setForeground(Tema.acentoTexto());
            }
        });
    }

    public boolean isModoDual() {
        return dual;
    }

    /** Muestra P o D; no toca los campos (eso lo hace PrincipalPage). */
    public void setModoDual(boolean dual) {
        this.dual = dual;
        refrescar();
    }

    private void refrescar() {
        boolean min = tipoBox.getSelectedIndex() == 1;
        subindice.setText(min ? "Min" : "Max");
        subindice.setToolTipText(min ? "Minimizar Z — clic para maximizar" : "Maximizar Z — clic para minimizar");
        superindice.setText(dual ? "D" : "P");
        superindice.setToolTipText(dual
                ? "Problema dual (variables y) — clic para volver al primal"
                : "Problema primal (variables x) — clic para pasar al dual");
        letraZ.setToolTipText("Z" + (dual ? "d" : "p") + " " + (min ? "Min" : "Max"));
        revalidate();
        repaint();
    }
}
