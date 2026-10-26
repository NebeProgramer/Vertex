package com.mycompany.problema.programacion.lineal.algoritmos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/** Mismo formato en todas las ventanas: punto decimal, 3 decimales, enteros sin ".000". */
class FormatoNumeroTest {

    @Test
    void enterosSinDecimales() {
        assertEquals("60", FormatoNumero.fmt(60));
        assertEquals("192000", FormatoNumero.fmt(192000.0));
        assertEquals("-25", FormatoNumero.fmt(-25.0));
        assertEquals("0", FormatoNumero.fmt(0.0));
        assertEquals("0", FormatoNumero.fmt(-0.0));
    }

    @Test
    void tresDecimalesFijos() {
        assertEquals("2.500", FormatoNumero.fmt(2.5));
        assertEquals("0.750", FormatoNumero.fmt(0.75));
        assertEquals("-3.750", FormatoNumero.fmt(-3.75));
        assertEquals("0.333", FormatoNumero.fmt(1.0 / 3));
        assertEquals("0.667", FormatoNumero.fmt(2.0 / 3));
    }

    @Test
    void redondeaAntesDeDecidirSiEsEntero() {
        assertEquals("3", FormatoNumero.fmt(2.9999996));   // error de punto flotante
        assertEquals("0", FormatoNumero.fmt(0.0004));       // no "0.000"
        assertEquals("0", FormatoNumero.fmt(-0.0004));      // ni "-0"
        assertEquals("1.001", FormatoNumero.fmt(1.0005));
    }

    @Test
    void siemprePuntoYSinNotacionCientifica() {
        Locale anterior = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("es", "CO")); // donde String.format ponía coma
            assertEquals("78.125", FormatoNumero.fmt(78.125));
            assertEquals("12000000", FormatoNumero.fmt(1.2e7));
            assertEquals("0.001", FormatoNumero.fmt(0.001));
        } finally {
            Locale.setDefault(anterior);
        }
    }
}
