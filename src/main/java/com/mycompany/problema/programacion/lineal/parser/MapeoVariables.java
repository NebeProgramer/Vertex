package com.mycompany.problema.programacion.lineal.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Frontera única entre los nombres que escribió el usuario y los índices
 * planos x1..xn que esperan los algoritmos de PL general (Simplex, Big M,
 * Simplex Dual, Gráfico y Numérico).
 *
 * Un problema escrito con doble índice (x11, x23...) se traduce una sola
 * vez a x1..xn compactos con {@link #traducirDobleIndice}, y cada ventana
 * usa {@link #nombreDe} para volver a mostrar el nombre original. Húngaro
 * y Costos Duales no pasan por aquí: leen xij como matriz origen/destino.
 *
 * Inmutable.
 */
public final class MapeoVariables {

    private static final Pattern VARIABLE = Pattern.compile("x(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final MapeoVariables IDENTIDAD = new MapeoVariables(Collections.emptyList());

    /** Posición k-1 → token original del usuario ("x23"); vacío = identidad. */
    private final List<String> nombresOriginales;

    private MapeoVariables(List<String> nombresOriginales) {
        this.nombresOriginales = Collections.unmodifiableList(new ArrayList<>(nombresOriginales));
    }

    /** Sin traducción: nombreDe(k) = "x" + k. Es el caso de los problemas escritos con x1..xn. */
    public static MapeoVariables identidad() {
        return IDENTIDAD;
    }

    /** Nombre que ve el usuario para la variable plana xk (k desde 1). */
    public String nombreDe(int k) {
        if (k >= 1 && k <= nombresOriginales.size()) {
            return nombresOriginales.get(k - 1);
        }
        return "x" + k;
    }

    /** Cantidad de variables traducidas (0 en la identidad). */
    public int tamano() {
        return nombresOriginales.size();
    }

    public boolean esIdentidad() {
        return nombresOriginales.isEmpty();
    }

    /**
     * Traduce un problema con doble índice a x1..xn. Todas las variables de
     * FO y R deben tener la misma cantidad de dígitos, y par; cada token se
     * parte por la mitad (misma convención que ParserTransporte): "x23" →
     * (2, 3), "x0102" → (1, 2). Se ordenan por (i, j) y se numeran sin
     * huecos, aunque falten combinaciones.
     *
     * @throws IllegalArgumentException si hay longitudes mezcladas o impares,
     *         o algún índice vale 0.
     */
    public static Traduccion traducirDobleIndice(String FO, String[] R) {
        Set<String> tokens = new LinkedHashSet<>();
        recolectar(FO, tokens);
        for (String r : R) {
            recolectar(r, tokens);
        }
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron variables xij en el problema.");
        }

        int longitud = -1;
        Map<String, int[]> indices = new HashMap<>();
        for (String digitos : tokens) {
            if (longitud == -1) {
                longitud = digitos.length();
            } else if (digitos.length() != longitud) {
                throw new IllegalArgumentException("Con doble índice, todas las variables deben tener la misma"
                        + " cantidad de dígitos (x11, x12...); x" + digitos + " no coincide.");
            }
            if (longitud % 2 != 0) {
                throw new IllegalArgumentException("La variable x" + digitos + " no se puede leer como xij:"
                        + " necesita una cantidad par de dígitos (x11, x0102...).");
            }
            int mitad = longitud / 2;
            int i = Integer.parseInt(digitos.substring(0, mitad));
            int j = Integer.parseInt(digitos.substring(mitad));
            if (i < 1 || j < 1) {
                throw new IllegalArgumentException("La variable x" + digitos + " tiene un índice 0 (i = " + i
                        + ", j = " + j + "); los índices de xij empiezan en 1.");
            }
            indices.put(digitos, new int[]{i, j});
        }

        List<String> ordenados = new ArrayList<>(tokens);
        ordenados.sort(Comparator.<String>comparingInt(t -> indices.get(t)[0]).thenComparingInt(t -> indices.get(t)[1]));

        Map<String, String> reemplazo = new HashMap<>();
        List<String> originales = new ArrayList<>();
        for (int k = 0; k < ordenados.size(); k++) {
            reemplazo.put(ordenados.get(k), "x" + (k + 1));
            originales.add("x" + ordenados.get(k));
        }

        String foPlano = reescribir(FO, reemplazo);
        String[] rPlano = new String[R.length];
        for (int i = 0; i < R.length; i++) {
            rPlano[i] = reescribir(R[i], reemplazo);
        }
        return new Traduccion(foPlano, rPlano, new MapeoVariables(originales));
    }

    /**
     * Para los algoritmos de PL general los nombres son solo etiquetas: si
     * las variables ya son x1..xn seguidas devuelve el problema tal cual
     * (identidad); si no (x10 y x20, x5 y x7, x3 y x15, x11..x23...), las
     * numera x1..xn por su valor y guarda los nombres originales. Con
     * doble índice da el mismo orden (i, j) que {@link #traducirDobleIndice}.
     */
    public static Traduccion compactar(String FO, String[] R) {
        Set<String> tokens = new LinkedHashSet<>();
        recolectar(FO, tokens);
        for (String r : R) {
            recolectar(r, tokens);
        }
        if (yaSonContiguas(tokens)) {
            return new Traduccion(FO, R, IDENTIDAD);
        }
        List<String> ordenados = new ArrayList<>(tokens);
        ordenados.sort(Comparator.<String, java.math.BigInteger>comparing(java.math.BigInteger::new)
                .thenComparing(Comparator.naturalOrder())); // "x01" y "x1" son etiquetas distintas

        Map<String, String> reemplazo = new HashMap<>();
        List<String> originales = new ArrayList<>();
        for (int k = 0; k < ordenados.size(); k++) {
            reemplazo.put(ordenados.get(k), "x" + (k + 1));
            originales.add("x" + ordenados.get(k));
        }
        String foPlano = reescribir(FO, reemplazo);
        String[] rPlano = new String[R.length];
        for (int i = 0; i < R.length; i++) {
            rPlano[i] = reescribir(R[i], reemplazo);
        }
        return new Traduccion(foPlano, rPlano, new MapeoVariables(originales));
    }

    /** true si los índices son exactamente 1..n, escritos sin ceros a la izquierda. */
    private static boolean yaSonContiguas(Set<String> tokens) {
        for (int k = 1; k <= tokens.size(); k++) {
            if (!tokens.contains(String.valueOf(k))) {
                return false;
            }
        }
        return true;
    }

    private static void recolectar(String texto, Set<String> destino) {
        if (texto == null) {
            return;
        }
        Matcher m = VARIABLE.matcher(texto);
        while (m.find()) {
            destino.add(m.group(1));
        }
    }

    /**
     * Una sola pasada con appendReplacement: un replace encadenado rompería
     * los nombres, porque "x1" está contenido en "x10" y "x11".
     */
    private static String reescribir(String texto, Map<String, String> reemplazo) {
        Matcher m = VARIABLE.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(reemplazo.get(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Resultado de {@link #traducirDobleIndice}: el problema en x1..xn y cómo volver a los nombres xij. */
    public static final class Traduccion {

        private final String foPlano;
        private final String[] rPlano;
        private final MapeoVariables mapeo;

        private Traduccion(String foPlano, String[] rPlano, MapeoVariables mapeo) {
            this.foPlano = foPlano;
            this.rPlano = rPlano;
            this.mapeo = mapeo;
        }

        public String getFOPlano() {
            return foPlano;
        }

        /** Copia: los algoritmos y validateAndBuild modifican el arreglo que reciben. */
        public String[] getRPlano() {
            return rPlano.clone();
        }

        public MapeoVariables getMapeo() {
            return mapeo;
        }
    }
}
