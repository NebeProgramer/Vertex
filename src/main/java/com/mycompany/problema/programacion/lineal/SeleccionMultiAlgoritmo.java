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

    /**
     * Decide si un algoritmo puede convivir con el problema escrito y con la
     * selección actual. Devuelve null si es compatible, o el motivo (se
     * muestra como tooltip de la casilla deshabilitada).
     */
    public interface Compatibilidad {
        String motivoIncompatible(String nombreAlgoritmo, List<String> seleccionados);
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
        seleccionarVarios(java.util.Collections.singletonList(nombre));
    }

    /**
     * Restaura una selección completa (p. ej. todos los "Metodos" de un
     * elemento del historial). Los que no sean compatibles con el problema
     * ya cargado en Z/restricciones se omiten. Lista vacía = "Decide por mi".
     */
    public void seleccionarVarios(List<String> nombres) {
        limpiarSeleccion();
        if (nombres != null) {
            for (String nombre : nombres) {
                JCheckBox check = checks.get(nombre);
                if (check != null && !ordenSeleccion.contains(nombre)
                        && compatibilidad.motivoIncompatible(nombre, ordenSeleccion) == null) {
                    check.setSelected(true);
                    ordenSeleccion.add(nombre);
                }
            }
        }
        decidirAutomaticamente.setSelected(ordenSeleccion.isEmpty());
        if (ordenSeleccion.size() == 1) {
            cambiarCombo(ordenSeleccion.get(0));
        }
        actualizarResumen();
        recalcularCompatibilidad();
    }

    private void actualizarResumen() {
        String resumen = "";
        if (ordenSeleccion.isEmpty()) {
            resumen = "Decide por mi";
        } else if (ordenSeleccion.size() == 1) {
            resumen = ordenSeleccion.get(0);
        } else {
            for(int i = 0; i < ordenSeleccion.size();i++){
                if(i == 0){
                    resumen += ordenSeleccion.get(i);
                }else{
                    resumen += "; " + ordenSeleccion.get(i);
                }
                
            }
        }
        selector.setText(resumen);
    }

    /**
     * Sincroniza el combo oculto que lee Calcular. Un Integer es un índice
     * (0 = "Decide por mi"): setSelectedItem(0) no hace nada en un combo de
     * textos y dejaba seleccionado el algoritmo anterior.
     */
    private void cambiarCombo(Object valor) {
        actualizandoCombo = true;
        if (valor instanceof Integer) {
            comboOriginal.setSelectedIndex((Integer) valor);
        } else {
            comboOriginal.setSelectedItem(valor);
        }
        actualizandoCombo = false;
    }

    private void abrirPopup() {
        // El texto pudo cambiar sin teclado (historial, pegar con el mouse...)
        recalcularCompatibilidad();
        int ancho = selector.getWidth();
        int alto = popup.getPreferredSize().height;
        popup.setPopupSize(ancho, alto);
        popup.show(selector, 0, selector.getHeight());
    }

    /**
     * Habilita/deshabilita cada casilla según su propia función de
     * compatibilidad contra la Z/restricciones actuales (las mismas
     * check*() que ya usa el modo manual de un solo algoritmo). Un
     * algoritmo marcado se evalúa contra los demás marcados (no contra sí
     * mismo); si deja de ser compatible (porque el usuario editó
     * Z/restricciones) se desmarca automáticamente. Se puede llamar desde afuera para
     * refrescar en vivo mientras el usuario escribe.
     */
    public void recalcularCompatibilidad() {
        boolean cambioSeleccion = false;
        for (Map.Entry<String, JCheckBox> entry : checks.entrySet()) {
            String nombre = entry.getKey();
            JCheckBox cb = entry.getValue();
            List<String> otros = new LinkedList<>(ordenSeleccion);
            otros.remove(nombre);
            String motivo = compatibilidad.motivoIncompatible(nombre, otros);
            boolean habilitado = motivo == null;
            cb.setEnabled(habilitado);
            cb.setToolTipText(motivo);
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
