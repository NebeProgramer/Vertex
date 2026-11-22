package com.mycompany.problema.programacion.lineal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import javax.swing.JTable;

import org.junit.jupiter.api.Test;

class TableroVistaTest {

    @Test
    void columnaEntranteSimplexDualIncluyeFilaExtraPjSobreA() {
        TableroVista.Datos datos = new TableroVista.Datos();
        datos.variables = new String[]{"x1", "x2", "s1"};
        datos.cj = new String[]{"2", "3", "0"};
        datos.cb = new String[]{"0"};
        datos.xb = new String[]{"s1"};
        datos.cuerpo = new String[][]{{"-1", "-1", "1"}};
        datos.sol = new String[]{"-3"};
        datos.conTheta = false;
        datos.zj = new String[]{"0", "0", "0"};
        datos.z = "0";
        datos.pj = new String[]{"2", "3", "0"};
        datos.etiquetaExtra = "|Pj/a|";
        datos.filaExtra = new String[]{"2", "3", "—"};
        datos.filaPivote = 0;
        datos.colPivote = 0;

        JTable tabla = new JTable();
        TableroVista.mostrar(tabla, datos);

        Component celdaExtra = tabla.getCellRenderer(4, 2)
                .getTableCellRendererComponent(tabla, tabla.getValueAt(4, 2), false, false, 4, 2);

        assertEquals(TableroVista.AMARILLO, celdaExtra.getBackground());
    }
}
