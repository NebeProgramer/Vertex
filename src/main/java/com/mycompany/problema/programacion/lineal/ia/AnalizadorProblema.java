package com.mycompany.problema.programacion.lineal.ia;

import com.mycompany.problema.programacion.lineal.PrincipalPage;
import com.mycompany.problema.programacion.lineal.parser.ParserLP;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Convierte un enunciado en texto libre al modelo de Vertex usando un
 * {@link MotorIA}. No toca Swing, para poder probarlo sin interfaz.
 *
 * Pipeline: prompt → respuesta JSON (forzada por schema) → normalización
 * determinista (formato, variables al lado izquierdo, escala) →
 * validación con el parser real de Vertex ({@link PrincipalPage#validateAndBuild}).
 * Si el parser la rechaza, se le devuelve el error al modelo para que se
 * corrija (hasta {@link #MAX_INTENTOS} intentos).
 *
 * Los algoritmos recomendados NO los decide el modelo: los calcula
 * {@link #recomendarAlgoritmos} con las mismas reglas del selector, porque
 * un modelo pequeño los elige mal y sin ellos el prompt queda más enfocado.
 */
public class AnalizadorProblema {

    public static final int MAX_INTENTOS = 2;

    private static final Pattern PREFIJO_Z = Pattern.compile("^(max|min)?[a-z]*\\s*z\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern VARIABLE_GUION = Pattern.compile("x_\\{?(\\d+)\\}?", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPERADOR_ESTRICTO = Pattern.compile("([<>])(?!=)");
    private static final Pattern DECIMAL_COMA = Pattern.compile("(\\d),(\\d)");
    private static final Pattern NO_NEGATIVIDAD = Pattern.compile("^x\\d+(,x\\d+)*>=0(\\.0+)?$");
    private static final Pattern CARACTERES_Z = Pattern.compile("^[x0-9+\\-./]+$");
    private static final Pattern CARACTERES_R = Pattern.compile("^[x0-9+\\-./<>=]+$");
    private static final Pattern TERMINO = Pattern.compile("[+-]?[^+-]+");
    private static final Pattern TERMINO_VARIABLE = Pattern.compile("^([+-]?)(\\d*\\.?\\d*)x(\\d+)$");
    private static final Pattern VARIABLE = Pattern.compile("x(\\d+)");

    /** Índices de variable ordenados como se leen: x1 < x2 < x10, x11 < x12 < x21. */
    private static final Comparator<String> ORDEN_INDICES
            = Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder());

    /**
     * Texto fijo (no JSONObject, que no conserva el orden de las claves): el
     * modelo genera las propiedades en este orden. "variables" va primero
     * para que defina qué es cada x antes de escribir Z, y cada restricción
     * lleva antes la "frase" del enunciado de la que sale, para que elija
     * bien la variable y el sentido del operador. Ambos campos son solo de
     * trabajo: {@link #normalizar} los descarta y deja R como lista de textos.
     */
    static final String ESQUEMA = "{\"type\":\"object\",\"properties\":{"
            + "\"variables\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},"
            + "\"Tipo\":{\"type\":\"string\",\"enum\":[\"Max\",\"Min\"]},"
            + "\"z\":{\"type\":\"string\"},"
            + "\"R\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{"
            + "\"frase\":{\"type\":\"string\"},\"restriccion\":{\"type\":\"string\"}},"
            + "\"required\":[\"frase\",\"restriccion\"]}}},"
            + "\"required\":[\"variables\",\"Tipo\",\"z\",\"R\"]}";

    private final MotorIA motor;
    private final String promptSistema;

    public AnalizadorProblema(MotorIA motor) {
        this.motor = motor;
        this.promptSistema = cargarPrompt();
    }

    public ResultadoAnalisis analizar(String enunciado) throws IOException, InterruptedException {
        List<JSONObject> mensajes = new ArrayList<>();
        mensajes.add(mensaje("system", promptSistema));
        mensajes.add(mensaje("user", enunciado.trim()));

        JSONObject problema = null;
        List<String> advertencias = new ArrayList<>();
        String error = null;

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            String respuesta = motor.chat(mensajes, ESQUEMA);
            mensajes.add(mensaje("assistant", respuesta));
            advertencias = new ArrayList<>();
            try {
                problema = normalizar(new JSONObject(respuesta), advertencias);
                error = validar(problema);
            } catch (JSONException ex) {
                error = "La respuesta no es un JSON válido (" + ex.getMessage() + ").";
            }
            if (error == null) {
                return new ResultadoAnalisis(problema, true, null, advertencias);
            }
            mensajes.add(mensaje("user", "Vertex rechazó ese modelo: " + error
                    + "\nCorrígelo respetando las reglas de formato y responde solo con el JSON."));
        }
        return new ResultadoAnalisis(problema, false, error, advertencias);
    }

    // ---------------------------------------------------------------
    // Normalización y validación (estáticas y públicas para los tests)
    // ---------------------------------------------------------------

    /**
     * Deja la respuesta del modelo en el formato de history.json y arregla
     * lo que los modelos pequeños suelen escribir distinto a Vertex: "Z =",
     * ≤/≥, x_1, X1, 3*x1, decimales con coma, restricciones de no
     * negatividad (Vertex ya las asume), variables al lado derecho
     * ("2x3 <= x1") y montos con muchos ceros. Lo que no se pueda arreglar
     * lo detecta {@link #validar}. "Metodos" se calcula aquí.
     */
    public static JSONObject normalizar(JSONObject crudo, List<String> advertencias) {
        JSONObject problema = new JSONObject();

        String tipo = crudo.optString("Tipo", "Max").toLowerCase();
        problema.put("Tipo", tipo.startsWith("min") ? "Min" : "Max");

        String z = expandirParentesis(normalizarExpresion(crudo.optString("z", "")));
        z = PREFIJO_Z.matcher(z).replaceFirst("");

        List<String> restricciones = new ArrayList<>();
        JSONArray r = crudo.optJSONArray("R");
        if (r != null) {
            for (int i = 0; i < r.length(); i++) {
                // {"frase", "restriccion"} (schema de trabajo) o texto plano (formato history.json)
                JSONObject conFrase = r.optJSONObject(i);
                String texto = conFrase != null ? conFrase.optString("restriccion", "") : r.optString(i, "");
                String restriccion = expandirParentesis(normalizarExpresion(texto));
                if (restriccion.isEmpty() || NO_NEGATIVIDAD.matcher(restriccion).matches()) {
                    continue;
                }
                if (conFrase != null) {
                    restriccion = corregirSentido(restriccion, conFrase.optString("frase", ""),
                            restricciones.size() + 1, advertencias);
                }
                restricciones.add(reordenarYEscalar(restriccion, restricciones.size() + 1, advertencias));
            }
        }
        problema.put("R", restricciones);
        z = completarZ(z, restricciones, advertencias);
        problema.put("z", z);
        avisarRecursoPartido(restricciones, advertencias);
        problema.put("Metodos", recomendarAlgoritmos(problema.getString("Tipo"), z, restricciones));
        return problema;
    }

    /**
     * Regla 7 del prompt, aplicada por Vertex: una variable que solo aparece
     * en las restricciones (p. ej. el agua, sin costo) se agrega a Z con
     * coeficiente 0 ("+0x3"), porque Vertex exige que Z las contenga todas.
     */
    static String completarZ(String z, List<String> restricciones, List<String> advertencias) {
        if (z.isEmpty()) {
            return z; // validar() reportará que falta la función objetivo
        }
        java.util.Set<String> enZ = new java.util.HashSet<>();
        Matcher mz = VARIABLE.matcher(z);
        while (mz.find()) {
            enZ.add(mz.group(1));
        }
        java.util.Set<String> faltantes = new java.util.TreeSet<>(ORDEN_INDICES);
        for (String r : restricciones) {
            Matcher mr = VARIABLE.matcher(r);
            while (mr.find()) {
                if (!enZ.contains(mr.group(1))) {
                    faltantes.add(mr.group(1));
                }
            }
        }
        if (faltantes.isEmpty()) {
            return z;
        }
        StringBuilder sb = new StringBuilder(z);
        List<String> nombres = new ArrayList<>();
        for (String indice : faltantes) {
            sb.append("+0x").append(indice);
            nombres.add("x" + indice);
        }
        advertencias.add("Se agregó a Z con costo 0: " + String.join(", ", nombres) + " (aparece"
                + (nombres.size() > 1 ? "n" : "") + " en las restricciones pero no en la función objetivo)."
                + " Revisa que su costo sea realmente 0.");
        return sb.toString();
    }

    private static final Pattern FRASE_MENOR = Pattern.compile(
            "no .{0,30}mas de|como maximo|a lo sumo|no (puede|pueden|debe|deben)( .{0,15})? (superar|pasar|exceder)"
            + "|\\bhasta\\b|\\bmaximo\\b|disponible|presupuesto|solo (permite|admite)");
    private static final Pattern FRASE_MAYOR = Pattern.compile(
            "al menos|por lo menos|como minimo|no menos de|\\bminimo\\b|se necesitan|se requieren");
    private static final Pattern FORMA_SIMPLE = Pattern.compile("^(\\d*\\.?\\d*x\\d+)(\\+\\d*\\.?\\d*x\\d+)*(<=|>=)\\d+(\\.\\d+)?$");

    /**
     * Error típico: "no más de 30 litros de agua" escrito como "x3>=30". Con
     * la "frase" que el modelo copia antes de cada restricción, si la frase
     * pide claramente <= (o >=) y la restricción tiene la forma simple
     * "coeficientes positivos · x op número", se invierte el operador y se
     * avisa. Las proporciones ("x1-2x3<=0"), las variables a la derecha y
     * las frases ambiguas no se tocan: ahí el sentido depende de qué
     * variable quede de cada lado.
     */
    static String corregirSentido(String restriccion, String frase, int numero, List<String> advertencias) {
        if (frase.isBlank() || !FORMA_SIMPLE.matcher(restriccion).matches()) {
            return restriccion;
        }
        String f = java.text.Normalizer.normalize(frase.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        boolean pideMenor = FRASE_MENOR.matcher(f).find();
        boolean pideMayor = FRASE_MAYOR.matcher(f).find();
        if (pideMenor == pideMayor) {
            return restriccion; // ninguna pista o pistas contradictorias
        }
        String esperado = pideMenor ? "<=" : ">=";
        String actual = restriccion.contains("<=") ? "<=" : ">=";
        if (esperado.equals(actual)) {
            return restriccion;
        }
        String corregida = restriccion.replace(actual, esperado);
        advertencias.add("Restricción " + numero + ": la frase «" + frase.trim() + "» indica " + esperado
                + ", pero la IA escribió \"" + restriccion + "\"; se corrigió a \"" + corregida + "\".");
        return corregida;
    }

    private static final Pattern PARENTESIS = Pattern.compile("([+-]?)(\\d*\\.?\\d*)\\(([^()]*)\\)");

    /**
     * Distribuye "k(...)" en sus términos: "x1>=0.4(x1+x2+x3)" →
     * "x1>=0.4x1+0.4x2+0.4x3". Típico al escribir porcentajes del total. Si
     * algo no se puede leer, se deja igual y validar() explica el error.
     */
    static String expandirParentesis(String s) {
        String actual = s;
        for (int vuelta = 0; vuelta < 10 && actual.contains("("); vuelta++) {
            Matcher m = PARENTESIS.matcher(actual);
            StringBuilder sb = new StringBuilder();
            boolean cambio = false;
            while (m.find()) {
                String numero = m.group(2);
                BigDecimal factor;
                try {
                    factor = numero.isEmpty() || numero.equals(".") ? BigDecimal.ONE : new BigDecimal(numero);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (m.group(1).equals("-")) {
                    factor = factor.negate();
                }
                StringBuilder interior = new StringBuilder();
                Matcher t = TERMINO.matcher(m.group(3));
                boolean ok = true;
                while (t.find()) {
                    String termino = t.group();
                    Matcher v = TERMINO_VARIABLE.matcher(termino);
                    BigDecimal valor;
                    String variable = "";
                    try {
                        if (v.matches()) {
                            String n = v.group(2);
                            valor = n.isEmpty() || n.equals(".") ? BigDecimal.ONE : new BigDecimal(n);
                            if (v.group(1).equals("-")) {
                                valor = valor.negate();
                            }
                            variable = "x" + v.group(3);
                        } else {
                            valor = new BigDecimal(termino);
                        }
                    } catch (NumberFormatException ex) {
                        ok = false;
                        break;
                    }
                    BigDecimal producto = valor.multiply(factor);
                    if (producto.signum() == 0) {
                        continue;
                    }
                    interior.append(producto.signum() < 0 ? "-" : "+");
                    BigDecimal abs = producto.abs().stripTrailingZeros();
                    if (variable.isEmpty() || abs.compareTo(BigDecimal.ONE) != 0) {
                        interior.append(abs.toPlainString());
                    }
                    interior.append(variable);
                }
                if (!ok) {
                    continue;
                }
                String reemplazo = interior.length() == 0 ? "+0" : interior.toString();
                // sin signo inicial si el paréntesis abre la expresión o va tras un operador
                int inicio = m.start();
                boolean alInicio = inicio == 0 || "<>=".indexOf(actual.charAt(inicio - 1)) >= 0;
                if (alInicio && reemplazo.startsWith("+")) {
                    reemplazo = reemplazo.substring(1);
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(reemplazo));
                cambio = true;
            }
            m.appendTail(sb);
            if (!cambio) {
                break;
            }
            actual = sb.toString();
        }
        return actual;
    }

    /**
     * Error típico de los modelos pequeños: "maíz usa 3 h, trigo 2 y soya
     * 2.5, y hay 270 h" escrito como 3x1<=270, 2x2<=270, 2.5x3<=270 en vez
     * de una sola suma. No se fusiona solo (x1<=4, x2<=4 puede ser
     * correcto), pero se avisa con la restricción sugerida cuando hay 2+
     * restricciones de una variable con el mismo operador y lado derecho, y
     * algún coeficiente distinto de 1.
     */
    private static void avisarRecursoPartido(List<String> restricciones, List<String> advertencias) {
        Map<String, List<Integer>> grupos = new java.util.LinkedHashMap<>();
        List<Restriccion> leidas = new ArrayList<>();
        for (int i = 0; i < restricciones.size(); i++) {
            Restriccion r = Restriccion.leer(restricciones.get(i));
            leidas.add(r);
            if (r != null && r.coeficientes.size() == 1 && r.ladoDerecho.signum() != 0) {
                String clave = r.operador + r.ladoDerecho.stripTrailingZeros().toPlainString();
                grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(i);
            }
        }
        for (List<Integer> grupo : grupos.values()) {
            if (grupo.size() < 2) {
                continue;
            }
            Restriccion suma = new Restriccion();
            suma.operador = leidas.get(grupo.get(0)).operador;
            suma.ladoDerecho = leidas.get(grupo.get(0)).ladoDerecho;
            boolean algunCoeficienteDistintoDeUno = false;
            StringBuilder numeros = new StringBuilder();
            for (int i : grupo) {
                for (Map.Entry<String, BigDecimal> e : leidas.get(i).coeficientes.entrySet()) {
                    suma.coeficientes.merge(e.getKey(), e.getValue(), BigDecimal::add);
                    algunCoeficienteDistintoDeUno |= e.getValue().compareTo(BigDecimal.ONE) != 0;
                }
                numeros.append(numeros.length() == 0 ? "" : ", ").append(i + 1);
            }
            if (algunCoeficienteDistintoDeUno && suma.coeficientes.size() == grupo.size()) {
                advertencias.add("Las restricciones " + numeros + " tienen el mismo límite. Si son un recurso"
                        + " compartido (horas, presupuesto, material...), deberían ser una sola: \""
                        + suma.formatear() + "\".");
            }
        }
    }

    /** null si Vertex acepta el problema; si no, el mensaje de error del parser. */
    public static String validar(JSONObject problema) {
        String z = problema.optString("z", "");
        JSONArray r = problema.optJSONArray("R");
        if (z.isEmpty()) {
            return "La función objetivo (z) está vacía.";
        }
        if (r == null || r.length() == 0) {
            return "No hay restricciones (R).";
        }
        if (!CARACTERES_Z.matcher(z).matches()) {
            return "La función objetivo \"" + z + "\" tiene caracteres no permitidos"
                    + " (solo números, x1..xn, +, -, . y /).";
        }
        String[] restricciones = new String[r.length()];
        for (int i = 0; i < r.length(); i++) {
            restricciones[i] = r.getString(i);
            if (!CARACTERES_R.matcher(restricciones[i]).matches()) {
                return "La restricción " + (i + 1) + " (\"" + restricciones[i] + "\") tiene caracteres"
                        + " no permitidos (solo números, x1..xn, +, -, ., /, <=, >= y =).";
            }
        }
        try {
            // validateAndBuild modifica el arreglo (quita espacios): se le pasa una copia
            PrincipalPage.validateAndBuild(z, restricciones.clone());
            return null;
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Mismas reglas que habilitan cada casilla del selector
     * (PrincipalPage.motivoIncompatible), en orden de preferencia:
     * xij → Húngaro (asignación) o Costos Dual (transporte); xi → Gráfico
     * con 2 variables, Simplex si todo es "<=" con lado derecho ≥ 0, Big M
     * si hay ">=" o "=", Simplex Dual para Min con solo ">=" (o lado
     * derecho negativo) y Numérico como respaldo cuando el problema es
     * pequeño y no aplica el Gráfico.
     */
    public static List<String> recomendarAlgoritmos(String tipo, String z, List<String> restricciones) {
        List<String> metodos = new ArrayList<>();
        List<String> indices = new ArrayList<>();
        Matcher m = VARIABLE.matcher(z);
        while (m.find()) {
            if (!indices.contains(m.group(1))) {
                indices.add(m.group(1));
            }
        }
        if (indices.isEmpty() || restricciones.isEmpty()) {
            return metodos;
        }

        int longitud = indices.get(0).length();
        boolean dobleIndice = longitud % 2 == 0
                && indices.stream().allMatch(i -> i.length() == longitud);
        if (dobleIndice) {
            metodos.add(esAsignacion(restricciones) ? "Hungaro" : "Costos Dual");
            return metodos;
        }

        boolean todoMenorIgual = true, todoMayorIgual = true, hayRhsNegativo = false;
        for (String r : restricciones) {
            Restriccion rest = Restriccion.leer(r);
            if (rest == null) {
                return metodos; // validar() reportará el error
            }
            todoMenorIgual &= rest.operador.equals("<=");
            todoMayorIgual &= rest.operador.equals(">=");
            hayRhsNegativo |= rest.ladoDerecho.signum() < 0;
        }

        if (indices.size() == 2) {
            metodos.add("Grafico");
        }
        if (todoMenorIgual && !hayRhsNegativo) {
            metodos.add("Simplex");
        } else if (todoMenorIgual) {
            metodos.add("Simplex Dual");
        } else {
            metodos.add("Big M");
            if (todoMayorIgual && "Min".equals(tipo)) {
                metodos.add("Simplex Dual");
            }
        }
        // Numérico enumera bases: solo vale la pena en problemas pequeños
        if (indices.size() != 2 && indices.size() + restricciones.size() <= 18 && metodos.size() < 3) {
            metodos.add("Numerico");
        }
        return metodos;
    }

    private static boolean esAsignacion(List<String> restricciones) {
        for (String r : restricciones) {
            Restriccion rest = Restriccion.leer(r);
            if (rest == null || !rest.operador.equals("=") || rest.ladoDerecho.compareTo(BigDecimal.ONE) != 0) {
                return false;
            }
            for (BigDecimal coef : rest.coeficientes.values()) {
                if (coef.compareTo(BigDecimal.ONE) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Si hay variables al lado derecho o números en el izquierdo, los pasa
     * a su lado ("2x3<=x1" → "-x1+2x3<=0"; "10<=x1" → "x1>=10"); si todos
     * los números comparten una potencia de 10, la divide
     * ("300000x1+1500000x2<=12000000" → "3x1+15x2<=120"). Deja aviso de
     * cada cambio para que el usuario revise el sentido de la restricción.
     */
    static String reordenarYEscalar(String restriccion, int numero, List<String> advertencias) {
        Restriccion rest = Restriccion.leer(restriccion);
        if (rest == null) {
            return restriccion; // validar() reportará el error con el mensaje del parser
        }
        String resultado = restriccion;
        if (rest.reordenada) {
            resultado = rest.formatear();
            advertencias.add("Restricción " + numero + ": \"" + restriccion + "\" se reescribió como \""
                    + resultado + "\" (variables a la izquierda). Revisa que el sentido sea el correcto.");
        }
        int ceros = rest.cerosComunes();
        if (ceros > 0) {
            String antes = resultado;
            rest.dividirEntreDiezALa(ceros);
            resultado = rest.formatear();
            advertencias.add("Restricción " + numero + ": \"" + antes + "\" se simplificó a \"" + resultado
                    + "\" (dividida entre " + BigDecimal.TEN.pow(ceros).toPlainString() + ").");
        }
        return resultado;
    }

    /** Restricción ya normalizada (sin espacios), en forma Σ coef·x op número. */
    private static final class Restriccion {

        final Map<String, BigDecimal> coeficientes = new TreeMap<>(ORDEN_INDICES);
        String operador;
        BigDecimal ladoDerecho = BigDecimal.ZERO;
        boolean reordenada;

        /** null si no se puede leer (validar() se encarga de explicar por qué). */
        static Restriccion leer(String texto) {
            String[] partes;
            try {
                partes = ParserLP.partirRestriccion(texto);
            } catch (IllegalArgumentException ex) {
                return null;
            }
            Restriccion r = new Restriccion();
            r.operador = partes[1];
            BigDecimal constanteIzq = BigDecimal.ZERO;
            BigDecimal constanteDer = BigDecimal.ZERO;
            Map<String, BigDecimal> variablesDer = new TreeMap<>(ORDEN_INDICES);
            if (!sumarTerminos(partes[0], r.coeficientes, BigDecimal.ONE)
                    || !sumarTerminos(partes[2], variablesDer, BigDecimal.ONE)) {
                return null;
            }
            constanteIzq = r.coeficientes.getOrDefault("", BigDecimal.ZERO);
            constanteDer = variablesDer.getOrDefault("", BigDecimal.ZERO);
            r.coeficientes.remove("");
            variablesDer.remove("");

            boolean izquierdaSinVariables = r.coeficientes.isEmpty();
            r.reordenada = !variablesDer.isEmpty() || constanteIzq.signum() != 0;
            if (izquierdaSinVariables && !variablesDer.isEmpty()) {
                // "10 <= x1": se invierten los lados en vez de dejar todo negativo
                r.coeficientes.putAll(variablesDer);
                r.ladoDerecho = constanteIzq.subtract(constanteDer);
                r.operador = invertir(r.operador);
            } else {
                for (Map.Entry<String, BigDecimal> e : variablesDer.entrySet()) {
                    r.coeficientes.merge(e.getKey(), e.getValue().negate(), BigDecimal::add);
                }
                r.ladoDerecho = constanteDer.subtract(constanteIzq);
            }
            r.coeficientes.values().removeIf(c -> c.signum() == 0);
            return r;
        }

        /** Suma los términos de un lado; la clave "" acumula la constante. */
        private static boolean sumarTerminos(String lado, Map<String, BigDecimal> destino, BigDecimal signo) {
            Matcher m = TERMINO.matcher(lado);
            while (m.find()) {
                String termino = m.group();
                Matcher v = TERMINO_VARIABLE.matcher(termino);
                BigDecimal valor;
                String clave;
                try {
                    if (v.matches()) {
                        String numero = v.group(2);
                        valor = numero.isEmpty() || numero.equals(".") ? BigDecimal.ONE : new BigDecimal(numero);
                        if (v.group(1).equals("-")) {
                            valor = valor.negate();
                        }
                        clave = v.group(3);
                    } else {
                        valor = new BigDecimal(termino);
                        clave = "";
                    }
                } catch (NumberFormatException ex) {
                    return false;
                }
                destino.merge(clave, valor.multiply(signo), BigDecimal::add);
            }
            return true;
        }

        private static String invertir(String op) {
            return op.equals("<=") ? ">=" : op.equals(">=") ? "<=" : op;
        }

        /** Mayor k tal que 10^k divide a todos los números distintos de 0 (todos enteros). */
        int cerosComunes() {
            int minimo = Integer.MAX_VALUE;
            List<BigDecimal> numeros = new ArrayList<>(coeficientes.values());
            numeros.add(ladoDerecho);
            for (BigDecimal n : numeros) {
                if (n.signum() == 0) {
                    continue;
                }
                BigDecimal limpio = n.stripTrailingZeros();
                if (limpio.scale() > 0) {
                    return 0; // hay decimales: no se toca
                }
                minimo = Math.min(minimo, -limpio.scale());
            }
            return minimo == Integer.MAX_VALUE ? 0 : minimo;
        }

        void dividirEntreDiezALa(int ceros) {
            coeficientes.replaceAll((k, c) -> c.movePointLeft(ceros));
            ladoDerecho = ladoDerecho.movePointLeft(ceros);
        }

        String formatear() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, BigDecimal> e : coeficientes.entrySet()) {
                BigDecimal c = e.getValue();
                if (c.signum() < 0) {
                    sb.append('-');
                } else if (sb.length() > 0) {
                    sb.append('+');
                }
                BigDecimal abs = c.abs();
                if (abs.compareTo(BigDecimal.ONE) != 0) {
                    sb.append(numero(abs));
                }
                sb.append('x').append(e.getKey());
            }
            if (sb.length() == 0) {
                sb.append('0');
            }
            return sb.append(operador).append(numero(ladoDerecho)).toString();
        }

        private static String numero(BigDecimal n) {
            return n.signum() == 0 ? "0" : n.stripTrailingZeros().toPlainString();
        }
    }

    private static String normalizarExpresion(String s) {
        String limpio = s.replaceAll("\\s+", "")
                .replace("≤", "<=").replace("≥", ">=")
                .replace("=<", "<=").replace("=>", ">=")
                .replace('−', '-').replace('–', '-')
                .replace("*", "").replace("·", "").replace("×", "");
        // "<" y ">" estrictos: en PL equivalen a <= y >=
        limpio = OPERADOR_ESTRICTO.matcher(limpio).replaceAll("$1=");
        limpio = limpio.replace("==", "=");
        limpio = reemplazarSubindices(limpio);
        limpio = VARIABLE_GUION.matcher(limpio).replaceAll("x$1");
        limpio = DECIMAL_COMA.matcher(limpio).replaceAll("$1.$2");
        return limpio.replace('X', 'x');
    }

    private static String reemplazarSubindices(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append(c >= '₀' && c <= '₉' ? (char) ('0' + (c - '₀')) : c);
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Prompt
    // ---------------------------------------------------------------

    private static String cargarPrompt() {
        try (InputStream in = AnalizadorProblema.class.getResourceAsStream("/ia/prompt-sistema.txt")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso /ia/prompt-sistema.txt");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer /ia/prompt-sistema.txt", ex);
        }
    }

    private static JSONObject mensaje(String rol, String contenido) {
        return new JSONObject().put("role", rol).put("content", contenido);
    }
}
