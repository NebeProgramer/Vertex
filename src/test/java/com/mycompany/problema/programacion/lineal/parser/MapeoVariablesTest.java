package com.mycompany.problema.programacion.lineal.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mycompany.problema.programacion.lineal.PrincipalPage;

import org.junit.jupiter.api.Test;

/** Traducción xij → x1..xn para los algoritmos de PL general, y vuelta a los nombres originales. */
class MapeoVariablesTest {

    /** a) Compacta sin huecos aunque falten combinaciones (no hay x13 ni x21). */
    @Test
    void compactaEnOrdenIJ() {
        MapeoVariables.Traduccion t = MapeoVariables.traducirDobleIndice(
                "3x22 + 2x11 + x23 + 5x12", new String[]{"x11 + x12 <= 4", "x22 - 2.5x23 >= 1"});
        assertEquals("3x3 + 2x1 + x4 + 5x2", t.getFOPlano());
        assertArrayEquals(new String[]{"x1 + x2 <= 4", "x3 - 2.5x4 >= 1"}, t.getRPlano());
        MapeoVariables m = t.getMapeo();
        assertEquals(4, m.tamano());
        assertEquals("x11", m.nombreDe(1));
        assertEquals("x12", m.nombreDe(2));
        assertEquals("x22", m.nombreDe(3));
        assertEquals("x23", m.nombreDe(4));
    }

    /** b) 11 variables: x12 no puede acabar como "x1" + "2" ni chocar con x10/x11 planos. */
    @Test
    void onceVariablesSinColisiones() {
        String fo = "x11+x12+x13+x14+x15+x16+x21+x22+x23+x24+x25";
        MapeoVariables.Traduccion t = MapeoVariables.traducirDobleIndice(fo, new String[]{"x12+x25<=7", "x26<=1"});
        // x26 solo está en R: también se numera (12 variables en total)
        assertEquals("x1+x2+x3+x4+x5+x6+x7+x8+x9+x10+x11", t.getFOPlano());
        assertArrayEquals(new String[]{"x2+x11<=7", "x12<=1"}, t.getRPlano());
        assertEquals("x26", t.getMapeo().nombreDe(12));
        assertEquals("x16", t.getMapeo().nombreDe(6));
    }

    /** c) Longitudes mezcladas: no se puede saber dónde termina i. */
    @Test
    void longitudesMezcladas() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MapeoVariables.traducirDobleIndice("x11+x123", new String[]{"x11<=1"}));
        assertTrue(ex.getMessage().contains("x123"));
        assertThrows(IllegalArgumentException.class,
                () -> MapeoVariables.traducirDobleIndice("x111+x112", new String[]{"x111<=1"})); // impar
    }

    /** d) "x10" sería i=1, j=0: los índices empiezan en 1. */
    @Test
    void indiceCero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MapeoVariables.traducirDobleIndice("x11+x10", new String[]{"x10<=1"}));
        assertTrue(ex.getMessage().contains("x10"));
    }

    @Test
    void identidad() {
        MapeoVariables m = MapeoVariables.identidad();
        assertTrue(m.esIdentidad());
        assertEquals("x1", m.nombreDe(1));
        assertEquals("x12", m.nombreDe(12));
    }

    /** 10 o más variables simples (x1..x11) ya no dan "mezcla de índices". */
    @Test
    void validateAndBuildAceptaDiezOMasVariablesSimples() {
        String fo = "x1+x2+x3+x4+x5+x6+x7+x8+x9+x10+x11";
        String[][] m = PrincipalPage.validateAndBuild(fo, new String[]{"x1+x10+x11<=5", "x9-x10>=0"});
        assertEquals(13, m[0].length); // 11 coeficientes + operador + lado derecho
        assertEquals("1", m[0][10]);   // coeficiente de x11 en su columna
    }

    /** Los nombres son etiquetas: mezclas que antes eran error ahora se aceptan... */
    @Test
    void validateAndBuildAceptaCualquierNombre() {
        PrincipalPage.validateAndBuild("x1+x12", new String[]{"x1+x12<=4"});
        PrincipalPage.validateAndBuild("3x10+5x20", new String[]{"x10<=4", "2x20<=12"});
        // ...pero una variable de una restricción sigue teniendo que estar en Z
        assertThrows(IllegalArgumentException.class,
                () -> PrincipalPage.validateAndBuild("x1+x2", new String[]{"x1+x3<=4"}));
    }

    /** compactar: x1..xn seguidas no se tocan (mismos objetos, identidad). */
    @Test
    void compactarDejaIgualLasContiguas() {
        String[] r = {"x1+x10+x11<=5"};
        MapeoVariables.Traduccion t = MapeoVariables.compactar("x1+x2+x3+x4+x5+x6+x7+x8+x9+x10+x11", r);
        assertTrue(t.getMapeo().esIdentidad());
        assertEquals("x1+x2+x3+x4+x5+x6+x7+x8+x9+x10+x11", t.getFOPlano());
        assertArrayEquals(r, t.getRPlano());
    }

    /** compactar: cualquier otro nombre se numera por su valor y se muestra como lo escribió el usuario. */
    @Test
    void compactarEtiquetas() {
        MapeoVariables.Traduccion t = MapeoVariables.compactar("3x10+5x20", new String[]{"x10<=4", "3x10+2x20<=18"});
        assertEquals("3x1+5x2", t.getFOPlano());
        assertArrayEquals(new String[]{"x1<=4", "3x1+2x2<=18"}, t.getRPlano());
        assertEquals("x10", t.getMapeo().nombreDe(1));
        assertEquals("x20", t.getMapeo().nombreDe(2));

        t = MapeoVariables.compactar("x15+x3", new String[]{"x3+x15<=4"}); // orden por valor: x3 antes que x15
        assertEquals("x2+x1", t.getFOPlano());
        assertEquals("x3", t.getMapeo().nombreDe(1));

        t = MapeoVariables.compactar("x7+x5", new String[]{"x5<=1"}); // huecos (x1..x4, x6)
        assertEquals("x2+x1", t.getFOPlano());
        assertEquals("x7", t.getMapeo().nombreDe(2));

        // con doble índice da el mismo orden (i, j) que traducirDobleIndice
        String fo = "3x22+2x11+x23+5x12";
        String[] rs = {"x11+x12<=4"};
        assertEquals(MapeoVariables.traducirDobleIndice(fo, rs).getFOPlano(), MapeoVariables.compactar(fo, rs).getFOPlano());
    }
}
