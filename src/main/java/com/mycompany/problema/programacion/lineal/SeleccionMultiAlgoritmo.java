package com.mycompany.problema.programacion.lineal;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/** Selector tipo combo que permite marcar varios algoritmos desde su desplegable. */
public class SeleccionMultiAlgoritmo {

    public static final String[] NOMBRES = {
        "Grafico", "Numerico", "Simplex", "Big M", "Simplex Dual", "Costos Dual", "Hungaro"
    };

    /** Decide si un algoritmo puede convivir con la selección actual. */
    public interface Compatibilidad {
        boolean esCompatible(String nombreAlgoritmo, List<String> seleccionados);
    }

    private final JComboBox<String> comboOriginal;
    private final Compatibilidad compatibilidad;

    private final JPanel panel = new JPanel();
    private final JButton selector = new JButton();
    private final JPopupMenu popup = new JPopupMenu();
    private final JCheckBox decidirAutomaticamente = new JCheckBox("Decide por mi");
    private boolean actualizandoCombo;
    private final Map<String, JCheckBox> checks = new LinkedHashMap<>();
    /** Orden real en que se fueron marcando (no el orden fijo de NOMBRES). */
    private final List<String> ordenSeleccion = new LinkedList<>();

    public SeleccionMultiAlgoritmo(Window ownerVentana, JComboBox<String> comboOriginal, Compatibilidad compatibilidad) {
        this.comboOriginal = comboOriginal;
        this.compatibilidad = compatibilidad;
        comboOriginal.addActionListener(e -> {
            if (!actualizandoCombo) {
                seleccionarUno((String) comboOriginal.getSelectedItem());
            }
        });

        panel.setLayout(new java.awt.BorderLayout());
        panel.setBackground(Color.WHITE);
        selector.setText("Decide por mi");
        selector.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        selector.setFocusPainted(false);
        selector.setBackground(Color.WHITE);
        selector.setForeground(new Color(23, 45, 66));
        selector.setToolTipText("Selecciona uno o varios algoritmos");
        selector.addActionListener(e -> abrirPopup());
        panel.add(selector, java.awt.BorderLayout.CENTER);

        JPanel opciones = new JPanel(new GridLayout(0, 1, 0, 2));
        opciones.setBackground(Color.WHITE);
        opciones.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        decidirAutomaticamente.setBackground(Color.WHITE);
        decidirAutomaticamente.setSelected(true);
        decidirAutomaticamente.addActionListener(e -> {
            if (decidirAutomaticamente.isSelected()) {
                limpiarSeleccion();
            } else if (ordenSeleccion.isEmpty()) {
                decidirAutomaticamente.setSelected(true);
            }
        });
        opciones.add(decidirAutomaticamente);
        for (String nombre : NOMBRES) {
            JCheckBox cb = new JCheckBox(nombre);
            cb.setBackground(Color.WHITE);
            cb.addActionListener(e -> onToggleCheckbox(nombre, cb.isSelected()));
            checks.put(nombre, cb);
            opciones.add(cb);
        }
        popup.setBorder(BorderFactory.createLineBorder(new Color(185, 195, 210)));
        popup.add(opciones);
    }

    public JPanel getPanel() {
        return panel;
    }

    /** El flujo múltiple se activa únicamente cuando hay dos o más algoritmos. */
    public boolean isModoMultiActivo() {
        return ordenSeleccion.size() > 1;
    }

    /** Algoritmos marcados y habilitados, en el orden en que se fueron marcando. */
    public List<String> getSeleccionados() {
        return new LinkedList<>(ordenSeleccion);
    }

    private void onToggleCheckbox(String nombre, boolean seleccionado) {
        if (seleccionado) {
            if (!ordenSeleccion.contains(nombre)) {
                ordenSeleccion.add(nombre);
            }
        } else {
            ordenSeleccion.remove(nombre);
        }
        decidirAutomaticamente.setSelected(false);
        if (ordenSeleccion.isEmpty()) {
            decidirAutomaticamente.setSelected(true);
            cambiarCombo(0);
        } else if (ordenSeleccion.size() == 1) {
            cambiarCombo(ordenSeleccion.get(0));
        }
        actualizarResumen();
        recalcularCompatibilidad();
    }

    private void limpiarSeleccion() {
        for (JCheckBox check : checks.values()) {
            check.setSelected(false);
        }
        ordenSeleccion.clear();
        cambiarCombo(0);
        actualizarResumen();
        recalcularCompatibilidad();
    }

    /** Sincroniza la selección visible cuando el historial carga un algoritmo. */
    public void seleccionarUno(String nombre) {
        limpiarSeleccion();
        if (nombre == null || nombre.isEmpty() || "Decide por mi".equals(nombre)) {
            return;
        }
        JCheckBox check = checks.get(nombre);
        if (check != null && compatibilidad.esCompatible(nombre, ordenSeleccion)) {
            check.setSelected(true);
            ordenSeleccion.add(nombre);
            decidirAutomaticamente.setSelected(false);
            cambiarCombo(nombre);
            actualizarResumen();
        }
    }

    private void actualizarResumen() {
        String resumen;
        if (ordenSeleccion.isEmpty()) {
            resumen = "Decide por mi";
        } else if (ordenSeleccion.size() == 1) {
            resumen = ordenSeleccion.get(0);
        } else {
            resumen = ordenSeleccion.size() + " algoritmos seleccionados";
        }
        selector.setText(resumen);
    }

    private void cambiarCombo(Object valor) {
        actualizandoCombo = true;
        comboOriginal.setSelectedItem(valor);
        actualizandoCombo = false;
    }

    private void abrirPopup() {
        int ancho = selector.getWidth();
        int alto = popup.getPreferredSize().height;
        popup.setPopupSize(ancho, alto);
        popup.show(selector, 0, selector.getHeight());
    }

    /**
     * Habilita/deshabilita cada casilla según su propia función de
     * compatibilidad contra la Z/restricciones actuales (las mismas
     * check*() que ya usa el modo manual de un solo algoritmo). Un
     * algoritmo ya marcado nunca se deshabilita a sí mismo por esto; si
     * deja de ser compatible (porque el usuario editó Z/restricciones) se
     * desmarca automáticamente. Se puede llamar desde afuera para
     * refrescar en vivo mientras el usuario escribe.
     */
    public void recalcularCompatibilidad() {
        boolean cambioSeleccion = false;
        for (Map.Entry<String, JCheckBox> entry : checks.entrySet()) {
            String nombre = entry.getKey();
            JCheckBox cb = entry.getValue();
            boolean yaMarcado = ordenSeleccion.contains(nombre);
            boolean habilitado = yaMarcado || compatibilidad.esCompatible(nombre, ordenSeleccion);
            cb.setEnabled(habilitado);
            if (!habilitado && cb.isSelected()) {
                cb.setSelected(false);
                ordenSeleccion.remove(nombre);
                cambioSeleccion = true;
            }
        }
        if (cambioSeleccion) {
            if (ordenSeleccion.isEmpty()) {
                decidirAutomaticamente.setSelected(true);
                cambiarCombo(0);
            } else if (ordenSeleccion.size() == 1) {
                decidirAutomaticamente.setSelected(false);
                cambiarCombo(ordenSeleccion.get(0));
            }
            actualizarResumen();
        }
    }
}
