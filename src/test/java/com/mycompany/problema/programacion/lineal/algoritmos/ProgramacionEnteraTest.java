package com.mycompany.problema.programacion.lineal.algoritmos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProgramacionEnteraTest {

    @Test
    void resuelveBinarias() {
        ProgramacionEntera.TipoVariable[] tipos = {
            ProgramacionEntera.TipoVariable.BINARIA,
            ProgramacionEntera.TipoVariable.BINARIA
        };
        ProgramacionEntera.Resultado r = ProgramacionEntera.resolver(
                new double[]{5, 8}, new double[][]{{1, 1}}, new double[]{1}, false, tipos);

        assertTrue(r.factible);
        assertEquals(8.0, r.z);
        assertEquals(1.0, r.solucion[1]);
    }

    @Test
    void resuelveEnterasConDosSolucionesIguales() {
        ProgramacionEntera.TipoVariable[] tipos = {
            ProgramacionEntera.TipoVariable.ENTERA,
            ProgramacionEntera.TipoVariable.ENTERA
        };
        ProgramacionEntera.Resultado r = ProgramacionEntera.resolver(
                new double[]{1, 1}, new double[][]{{1, 1}}, new double[]{1}, false, tipos);

        assertTrue(r.factible);
        assertEquals(1.0, r.z);
        assertTrue(r.multiples);
    }
}
