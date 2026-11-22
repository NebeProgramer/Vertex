package com.mycompany.problema.programacion.lineal.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ParserTransporteTest {

    @Test
    void aceptaCostosDecimalesEnMatrizDobleIndice() {
        double[][] matriz = ParserTransporte.construirMatrizCostos("7.5x11+2.25x12+4x21+1.5x22");

        assertEquals(7.5, matriz[0][0]);
        assertEquals(2.25, matriz[0][1]);
        assertEquals(4.0, matriz[1][0]);
        assertEquals(1.5, matriz[1][1]);
    }

    @Test
    void aceptaCoeficienteNegativoYSinCoeficiente() {
        double[][] matriz = ParserTransporte.construirMatrizCostos("-1.5x11+x12");

        assertEquals(-1.5, matriz[0][0]);
        assertEquals(1.0, matriz[0][1]);
    }
}
