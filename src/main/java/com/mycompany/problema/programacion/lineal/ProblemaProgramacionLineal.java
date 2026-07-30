/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.problema.programacion.lineal;
/**
 *
 * @author PC-ANDERSON
 */
public class ProblemaProgramacionLineal {

    public static void main(String[] args) {
        PrincipalPage pp = new PrincipalPage();
        pp.setVisible(true);

        if (VentanaDonacion.debeMostrarse()) {
            new VentanaDonacion(pp).setVisible(true);
        }
    }
}
