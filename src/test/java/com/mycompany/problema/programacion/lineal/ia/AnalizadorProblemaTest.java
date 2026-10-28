package com.mycompany.problema.programacion.lineal.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/** Normalización, validación y recomendación sobre lo que devuelve la IA, sin llamar a ningún modelo real. */
class AnalizadorProblemaTest {

    private static JSONObject normalizar(String json, List<String> advertencias) {
        return AnalizadorProblema.normalizar(new JSONObject(json), advertencias);
    }

    private static JSONObject normalizar(String json) {
        return normalizar(json, new ArrayList<>());
    }

    private static List<String> lista(JSONArray arr) {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            l.add(arr.getString(i));
        }
        return l;
    }

    private static String restriccion(String r) {
        return AnalizadorProblema.reordenarYEscalar(r, 1, new ArrayList<>());
    }

    // ---------------------------------------------------------------
    // Formato
    // ---------------------------------------------------------------

    @Test
    void quitaPrefijoZYNormalizaVariables() {
        JSONObject p = normalizar("{\"Tipo\":\"maximizar\",\"z\":\"Max Z = 3*X_1 + 5·x₂\",\"R\":[]}");
        assertEquals("Max", p.getString("Tipo"));
        assertEquals("3x1+5x2", p.getString("z"));
    }

    @Test
    void normalizaOperadoresYDecimales() {
        JSONObject p = normalizar("{\"Tipo\":\"Min\",\"z\":\"0,5x1 + x2\",\"R\":"
                + "[\"2x1 + x2 ≤ 8\", \"x1 ≥ 1\", \"x1 + x2 =< 10\", \"x1 - x2 => 0\", \"x2 < 4\", \"x1 + x2 == 3\"]}");
        assertEquals("Min", p.getString("Tipo"));
        assertEquals("0.5x1+x2", p.getString("z"));
        assertEquals(Arrays.asList("2x1+x2<=8", "x1>=1", "x1+x2<=10", "x1-x2>=0", "x2<=4", "x1+x2=3"),
                lista(p.getJSONArray("R")));
    }

    @Test
    void descartaNoNegatividadYElCampoDeTrabajo() {
        JSONObject p = normalizar("{\"variables\":[\"x1 = algo\"],\"Tipo\":\"Max\",\"z\":\"x1+x2\",\"R\":"
                + "[\"x1 + x2 <= 4\", \"x1 >= 0\", \"x2>=0\", \"x1, x2 >= 0\", \"x11 >= 0\"]}");
        assertEquals(Arrays.asList("x1+x2<=4"), lista(p.getJSONArray("R")));
        // la salida es exactamente el formato de history.json
        assertEquals(new java.util.TreeSet<>(Arrays.asList("Tipo", "z", "R", "Metodos")), new java.util.TreeSet<>(p.keySet()));
    }

    // ---------------------------------------------------------------
    // Reordenar y escalar
    // ---------------------------------------------------------------

    @Test
    void pasaVariablesALaIzquierda() {
        assertEquals("-x1+2x3<=0", restriccion("2x3<=x1"));
        assertEquals("x1-2x3<=0", restriccion("x1<=2x3"));
        assertEquals("x1+3x2<=18", restriccion("x1<=18-3x2"));
        assertEquals("x1>=10", restriccion("10<=x1"));
        assertEquals("x1<=5", restriccion("x1+5<=10"));
        assertEquals("x1+x2<=4", restriccion("x1+x2<=4")); // sin cambios
    }

    @Test
    void divideLaPotenciaDeDiezComun() {
        assertEquals("3x1+15x2+4x3<=120", restriccion("300000x1+1500000x2+400000x3<=12000000"));
        assertEquals("3x1+15x2<=120", restriccion("300x1+1500x2<=12000"));
        assertEquals("0.5x1+20x2<=100", restriccion("0.5x1+20x2<=100")); // con decimales no se toca
        assertEquals("100x1+x2<=50", restriccion("100x1+x2<=50"));
    }

    @Test
    void avisaCadaCambio() {
        List<String> advertencias = new ArrayList<>();
        AnalizadorProblema.reordenarYEscalar("2x3<=x1", 3, advertencias);
        assertEquals(1, advertencias.size());
        assertTrue(advertencias.get(0).startsWith("Restricción 3"));
        assertTrue(advertencias.get(0).contains("Revisa que el sentido"));
    }

    // ---------------------------------------------------------------
    // Algoritmos recomendados (los decide Vertex, no la IA)
    // ---------------------------------------------------------------

    @Test
    void recomiendaSegunLaEstructura() {
        assertEquals(Arrays.asList("Grafico", "Simplex"), AnalizadorProblema.recomendarAlgoritmos("Max",
                "3x1+5x2", Arrays.asList("x1<=4", "2x2<=12", "3x1+2x2<=18")));
        assertEquals(Arrays.asList("Big M", "Simplex Dual", "Numerico"), AnalizadorProblema.recomendarAlgoritmos("Min",
                "0.5x1+0.8x2+0.3x3", Arrays.asList("2x1+4x2+x3>=20", "3x1+x2+2x3>=15")));
        assertEquals(Arrays.asList("Simplex", "Numerico"), AnalizadorProblema.recomendarAlgoritmos("Max",
                "x1+x2+x3", Arrays.asList("x1+x2+x3<=10")));
        assertEquals(Arrays.asList("Hungaro"), AnalizadorProblema.recomendarAlgoritmos("Min",
                "7x11+9x12+6x21+4x22", Arrays.asList("x11+x12=1", "x21+x22=1", "x11+x21=1", "x12+x22=1")));
        assertEquals(Arrays.asList("Costos Dual"), AnalizadorProblema.recomendarAlgoritmos("Min",
                "4x11+6x12+5x21+3x22", Arrays.asList("x11+x12<=50", "x21+x22<=60", "x11+x21=30", "x12+x22=80")));
    }

    /** xij que no es asignación ni transporte: PL general, no Húngaro/Costos Dual. */
    @Test
    void dobleIndiceGeneralNoVaAHungaro() {
        // P6 del lote xij (mezcla de crudos con porcentajes): >= y = → Big M
        assertEquals(Arrays.asList("Big M", "Numerico"), AnalizadorProblema.recomendarAlgoritmos("Max",
                "20x11+22x12+24x13+23x21+25x22+27x23",
                Arrays.asList("x11+x21<=20", "x11+x12+x13=35", "1.5x11-0.5x12-1.5x13>=0")));
        // todo <= con coeficientes distintos de 1 → Simplex
        assertEquals(Arrays.asList("Simplex", "Numerico"), AnalizadorProblema.recomendarAlgoritmos("Max",
                "7x11+6x12+9.4x22", Arrays.asList("4x11<=2000", "5x12+2x22<=10000")));
    }

    /** Caso real reportado con qwen2.5:3b: montos en pesos, una variable a la derecha y sin algoritmos. */
    @Test
    void casoCampanaPublicitaria() {
        List<String> advertencias = new ArrayList<>();
        JSONObject p = normalizar("{\"Tipo\":\"Max\",\"z\":\"1000x1 + 5000x2 + 1300x3\",\"R\":["
                + "\"300000x1 + 1500000x2 + 400000x3 <= 12000000\",\"x2 <= 4\",\"2x3 <= x1\",\"x3 >= 5\"],"
                + "\"Metodos\":[]}", advertencias);
        assertEquals("1000x1+5000x2+1300x3", p.getString("z")); // Z no se escala: cambiaría su valor
        assertEquals(Arrays.asList("3x1+15x2+4x3<=120", "x2<=4", "-x1+2x3<=0", "x3>=5"), lista(p.getJSONArray("R")));
        assertEquals(Arrays.asList("Big M", "Numerico"), lista(p.getJSONArray("Metodos")));
        assertNull(AnalizadorProblema.validar(p));
        // la del sentido invertido queda señalada para que el usuario la revise
        assertTrue(advertencias.stream().anyMatch(a -> a.contains("2x3<=x1") && a.contains("sentido")));
    }

    @Test
    void expandeParentesis() {
        assertEquals("x1>=0.4x1+0.4x2+0.4x3", AnalizadorProblema.expandirParentesis("x1>=0.4(x1+x2+x3)"));
        assertEquals("x1-2x2-2x3<=5", AnalizadorProblema.expandirParentesis("x1-2(x2+x3)<=5"));
        assertEquals("x1+x2<=10", AnalizadorProblema.expandirParentesis("(x1+x2)<=10"));
        assertEquals("0.5x1+1.5<=3", AnalizadorProblema.expandirParentesis("0.5(x1+3)<=3"));
    }

    /** Lote P6 con qwen2.5:3b: porcentajes del total escritos con paréntesis. */
    @Test
    void casoPorcentajesConParentesis() {
        JSONObject p = normalizar("{\"Tipo\":\"Min\",\"z\":\"12x1 + 7x2 + 5x3\",\"R\":["
                + "{\"frase\":\"200 kg exactos\",\"restriccion\":\"x1 + x2 + x3 = 200\"},"
                + "{\"frase\":\"al menos el 40%\",\"restriccion\":\"x1 >= 0.4*(x1 + x2 + x3)\"},"
                + "{\"frase\":\"maní no más del 30%\",\"restriccion\":\"x3 <= 0.3(x1 + x2 + x3)\"}]}");
        assertEquals(Arrays.asList("x1+x2+x3=200", "0.6x1-0.4x2-0.4x3>=0", "-0.3x1-0.3x2+0.7x3<=0"),
                lista(p.getJSONArray("R")));
        assertNull(AnalizadorProblema.validar(p));
        assertEquals(Arrays.asList("Big M", "Numerico"), lista(p.getJSONArray("Metodos")));
    }

    /** Lote P10 con qwen2.5:3b: el agua (sin costo) quedó fuera de Z. */
    @Test
    void completaZConCostoCero() {
        List<String> advertencias = new ArrayList<>();
        JSONObject p = normalizar("{\"Tipo\":\"Min\",\"z\":\"5x1 + 4x2\",\"R\":["
                + "\"x1 + x2 + x3 = 100\",\"0.6x1 + 0.3x2 + 0x3 >= 30\",\"x3 <= 30\"]}", advertencias);
        assertEquals("5x1+4x2+0x3", p.getString("z"));
        assertNull(AnalizadorProblema.validar(p));
        assertEquals(Arrays.asList("Big M", "Numerico"), lista(p.getJSONArray("Metodos")));
        assertTrue(advertencias.stream().anyMatch(a -> a.contains("x3") && a.contains("costo 0")));
    }

    /** Lote P10, 2.ª corrida: "no más de 30 litros de agua" escrito como x3 >= 30. */
    @Test
    void corrigeElSentidoSegunLaFrase() {
        List<String> advertencias = new ArrayList<>();
        JSONObject p = normalizar("{\"Tipo\":\"Min\",\"z\":\"5x1 + 4x2\",\"R\":["
                + "{\"frase\":\"100 litros de solución\",\"restriccion\":\"x1 + x2 + x3 = 100\"},"
                + "{\"frase\":\"al menos 30 unidades de principio activo\",\"restriccion\":\"0.6x1 + 0.3x2 >= 30\"},"
                + "{\"frase\":\"no puede llevar más de 30 litros de agua\",\"restriccion\":\"x3 >= 30\"}]}", advertencias);
        assertEquals(Arrays.asList("x1+x2+x3=100", "0.6x1+0.3x2>=30", "x3<=30"), lista(p.getJSONArray("R")));
        assertTrue(advertencias.stream().anyMatch(a -> a.contains("se corrigió a \"x3<=30\"")));

        // no se tocan: sentido correcto, proporciones (lado derecho 0 con resta) ni frases ambiguas
        List<String> sinCambios = new ArrayList<>();
        assertEquals("x2<=4", AnalizadorProblema.corregirSentido("x2<=4", "como máximo 4 anuncios", 1, sinCambios));
        assertEquals("x1-2x3<=0", AnalizadorProblema.corregirSentido("x1-2x3<=0", "no pueden superar el doble", 1, sinCambios));
        assertEquals("x1>=5", AnalizadorProblema.corregirSentido("x1>=5", "al menos 5 y como máximo 9", 1, sinCambios));
        assertEquals("x1<=60", AnalizadorProblema.corregirSentido("x1>=60", "no se pueden vender más de 60 panes", 1, new ArrayList<>()));
        assertTrue(sinCambios.isEmpty());
    }

    /** Caso real con qwen2.5:3b: un recurso compartido partido en una restricción por variable. */
    @Test
    void avisaRecursoCompartidoPartido() {
        List<String> advertencias = new ArrayList<>();
        normalizar("{\"Tipo\":\"Max\",\"z\":\"500x1+400x2+450x3\",\"R\":[\"x1+x2+x3<=100\",\"3x1<=270\","
                + "\"2x2<=270\",\"2.5x3<=270\",\"x2>=20\",\"x3<=30\"]}", advertencias);
        assertEquals(1, advertencias.size());
        assertTrue(advertencias.get(0).contains("2, 3, 4"));
        assertTrue(advertencias.get(0).contains("3x1+2x2+2.5x3<=270"));

        // límites iguales con coeficiente 1 pueden ser legítimos: no se avisa
        List<String> sinAviso = new ArrayList<>();
        normalizar("{\"Tipo\":\"Max\",\"z\":\"x1+x2\",\"R\":[\"x1<=4\",\"x2<=4\"]}", sinAviso);
        assertTrue(sinAviso.isEmpty());
    }

    // ---------------------------------------------------------------
    // Validación y reintentos
    // ---------------------------------------------------------------

    @Test
    void validaConElParserDeVertex() {
        JSONObject ok = normalizar("{\"Tipo\":\"Max\",\"z\":\"3x1+5x2\",\"R\":[\"x1<=4\",\"2x2<=12\"]}");
        assertNull(AnalizadorProblema.validar(ok));
        // validar() no debe modificar el problema (validateAndBuild recibe una copia)
        assertEquals(Arrays.asList("x1<=4", "2x2<=12"), lista(ok.getJSONArray("R")));
    }

    @Test
    void propagaErroresDelParser() {
        JSONObject operadorRepetido = normalizar("{\"Tipo\":\"Max\",\"z\":\"3x1+5x2\",\"R\":[\"2x1++3x2<=4\"]}");
        assertTrue(AnalizadorProblema.validar(operadorRepetido).contains("++"));

        JSONObject nombresDeVariable = normalizar("{\"Tipo\":\"Max\",\"z\":\"3mesas+5sillas\",\"R\":[\"mesas<=4\"]}");
        assertTrue(AnalizadorProblema.validar(nombresDeVariable).contains("caracteres no permitidos"));

        JSONObject sinRestricciones = normalizar("{\"Tipo\":\"Max\",\"z\":\"x1\",\"R\":[\"x1>=0\"]}");
        assertNotNull(AnalizadorProblema.validar(sinRestricciones));
    }

    @Test
    void reintentaConElErrorYAceptaLaCorreccion() throws Exception {
        MotorFalso motor = new MotorFalso(
                "{\"Tipo\":\"Max\",\"z\":\"3x1+5x2\",\"R\":[\"2x1++3x2<=18\"]}",
                "{\"Tipo\":\"Max\",\"z\":\"3x1+5x2\",\"R\":[\"x1+3x2<=18\"]}");
        ResultadoAnalisis r = new AnalizadorProblema(motor).analizar("problema");
        assertTrue(r.isValido());
        assertEquals("x1+3x2<=18", r.getProblema().getJSONArray("R").getString(0));
        assertEquals(2, motor.llamadas);
        // el segundo intento debe llevar el error de Vertex en la conversación
        assertTrue(motor.ultimoMensaje.contains("Vertex rechazó"));
    }

    @Test
    void devuelveElUltimoIntentoSiNuncaQuedaValido() throws Exception {
        MotorFalso motor = new MotorFalso("no es json",
                "{\"Tipo\":\"Max\",\"z\":\"x1+x2\",\"R\":[\"x1++x2<=4\"]}");
        ResultadoAnalisis r = new AnalizadorProblema(motor).analizar("problema");
        assertFalse(r.isValido());
        assertNotNull(r.getProblema());
        assertNotNull(r.getError());
        assertEquals(AnalizadorProblema.MAX_INTENTOS, motor.llamadas);
    }

    // ---------------------------------------------------------------
    // Prompt y schema
    // ---------------------------------------------------------------

    /** Los ejemplos del prompt enseñan el formato: deben ser modelos que Vertex acepta sin retoques. */
    @Test
    void losEjemplosDelPromptSonValidos() throws Exception {
        String prompt;
        try (InputStream in = getClass().getResourceAsStream("/ia/prompt-sistema.txt")) {
            prompt = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        int ejemplos = 0;
        for (String linea : prompt.split("\\R")) {
            if (linea.startsWith("Respuesta: ")) {
                List<String> advertencias = new ArrayList<>();
                JSONObject p = normalizar(linea.substring("Respuesta: ".length()), advertencias);
                assertNull(AnalizadorProblema.validar(p), linea);
                assertTrue(advertencias.isEmpty(), linea + " -> " + advertencias);
                ejemplos++;
            }
        }
        assertEquals(5, ejemplos);
    }

    @Test
    void elSchemaPideVariablesAntesQueZ() {
        assertEquals("object", new JSONObject(AnalizadorProblema.ESQUEMA).getString("type")); // JSON válido
        String s = AnalizadorProblema.ESQUEMA;
        assertTrue(s.indexOf("\"variables\"") < s.indexOf("\"Tipo\""));
        assertTrue(s.indexOf("\"Tipo\"") < s.indexOf("\"z\""));
        assertTrue(s.indexOf("\"z\"") < s.indexOf("\"R\""));
    }

    private static final class MotorFalso implements MotorIA {

        private final Deque<String> respuestas;
        int llamadas;
        String ultimoMensaje = "";

        MotorFalso(String... respuestas) {
            this.respuestas = new ArrayDeque<>(Arrays.asList(respuestas));
        }

        @Override
        public String chat(List<JSONObject> mensajes, String esquemaJson) {
            llamadas++;
            ultimoMensaje = mensajes.get(mensajes.size() - 1).getString("content");
            return respuestas.pop();
        }
    }
}
