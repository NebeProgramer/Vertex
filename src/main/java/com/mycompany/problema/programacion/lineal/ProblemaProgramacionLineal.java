/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.problema.programacion.lineal;

import com.formdev.flatlaf.FlatLightLaf;

/**
 *
 * @author PC-ANDERSON
 */
public class ProblemaProgramacionLineal {

    public static void main(String[] args) {
        // FlatLaf debe activarse ANTES de crear cualquier componente Swing,
        // por eso va de primero en main().
        FlatLightLaf.setup();
        Recursos.instalarEstilo();
        // Decoraciones de ventana propias de FlatLaf: permite embeber una
        // JMenuBar en la barra de título (ahí van los "(?)" de ayuda de
        // cada ventana de resultados). Ya viene activado por defecto en
        // Windows 10/11; en Linux hay que pedirlo explícitamente.
        javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
        javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);

        PrincipalPage pp = new PrincipalPage();
        pp.setVisible(true);

        if (VentanaDonacion.debeMostrarse()) {
            new VentanaDonacion(pp).setVisible(true);
        }

        ActualizacionChecker.verificar(pp);
    }
}
