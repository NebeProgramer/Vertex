package com.mycompany.problema.programacion.lineal;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;
import javax.swing.JFrame;

/**
 * Recuerda la posición y el tamaño de una ventana entre sesiones, para no
 * repetir esta misma lógica en cada una de las 7 ventanas de algoritmos
 * (ya se usaba a mano en PrincipalPage y en VentanaDonacion).
 *
 * Cada tipo de ventana (MetodoSimplex, AlgoritmoHungaro, etc.) guarda su
 * propia posición de forma independiente, usando el nombre simple de la
 * clase como prefijo de las claves.
 */
public final class VentanaPrefs {

    private VentanaPrefs() {
    }

    /**
     * Si hay una posición/tamaño guardados de una sesión anterior para este
     * tipo de ventana, los restaura. Si no, deja la ventana en su ubicación
     * por defecto (la que Swing le asigne). Además, deja instalado un
     * listener que guarda la posición actual cada vez que la ventana se
     * cierra.
     */
    public static void aplicar(JFrame ventana) {
        restaurar(ventana);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                guardar(ventana);
            }
        });
    }

    private static void restaurar(JFrame ventana) {
        Preferences prefs = Preferences.userNodeForPackage(VentanaPrefs.class);
        String prefijo = ventana.getClass().getSimpleName();
        int x = prefs.getInt(prefijo + "_x", Integer.MIN_VALUE);
        int y = prefs.getInt(prefijo + "_y", Integer.MIN_VALUE);
        int w = prefs.getInt(prefijo + "_w", -1);
        int h = prefs.getInt(prefijo + "_h", -1);

        if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE && w > 0 && h > 0) {
            ventana.setBounds(x, y, w, h);
        }
        // si no hay nada guardado, se deja la ubicación/tamaño por defecto tal cual estaba
    }

    private static void guardar(JFrame ventana) {
        Preferences prefs = Preferences.userNodeForPackage(VentanaPrefs.class);
        String prefijo = ventana.getClass().getSimpleName();
        prefs.putInt(prefijo + "_x", ventana.getX());
        prefs.putInt(prefijo + "_y", ventana.getY());
        prefs.putInt(prefijo + "_w", ventana.getWidth());
        prefs.putInt(prefijo + "_h", ventana.getHeight());
    }
}
