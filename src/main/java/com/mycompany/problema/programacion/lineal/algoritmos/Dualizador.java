package com.mycompany.problema.programacion.lineal.algoritmos;

import com.mycompany.problema.programacion.lineal.parser.ParserLP;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Arma el problema dual de un problema de PL escrito como en el Editor
 * (Z = "3x1 + 5x2", restricciones "3x1 + 2x2 &lt;= 18"), con las reglas
 * de la forma canónica:
 *
 * <ol>
 *   <li>Cada restricción = se parte en dos: una &lt;= y otra &gt;=.</li>
 *   <li>Se normaliza: si Z es Max, todas deben ser &lt;=; si es Min, todas
 *       &gt;=. La que no cumple se multiplica por −1 (coeficientes y lado
 *       derecho), lo que invierte la desigualdad.</li>
 *   <li>Cada restricción (ya partida y normalizada) da una variable dual
 *       y<sub>i</sub> &gt;= 0 y cada variable primal x<sub>j</sub> da una
 *       restricción dual: Σ a<sub>ij</sub>·y<sub>i</sub> &gt;= c<sub>j</sub>
 *       (si el primal es Max) o &lt;= c<sub>j</sub> (si es Min). La matriz
 *       se transpone y b y c cambian de lugar.</li>
 *   <li>Equivalencia: Z<sub>p</sub>Max → Z<sub>d</sub>Min y
 *       Z<sub>p</sub>Min → Z<sub>d</sub>Max.</li>
 * </ol>
 *
 * Como la regla vale en los dos sentidos, el dual del dual vuelve al primal
 * (normalizado). Clase sin Swing: PrincipalPage solo escribe el resultado
 * en los campos, y los tests la prueban directamente.
 */
public final class Dualizador {

    private static final double EPS = 1e-12;

    private Dualizador() {
    }

    /** El problema dual, listo para escribir en el Editor. */
    public static final class Resultado {

        private final String fo;
        private final String[] r;
        private final String tipo;
        private final List<String> pasos;

        Resultado(String fo, String[] r, String tipo, List<String> pasos) {
            this.fo = fo;
            this.r = r;
            this.tipo = tipo;
            this.pasos = Collections.unmodifiableList(pasos);
        }

        /** Función objetivo del dual, ej: "4y1 + 12y2 + 18y3". */
        public String getFO() {
            return fo;
        }

        /** Restricciones del dual, una por variable del problema original. */
        public String[] getR() {
            return r.clone();
        }

        /** "Max" o "Min": el opuesto del problema original. */
        public String getTipo() {
            return tipo;
        }

        /** Qué restricciones se partieron o se multiplicaron por −1 (para avisarle al usuario). */
        public List<String> getPasos() {
            return pasos;
        }
    }

    /**
     * @param FO            función objetivo, con variables {@code letraEntrada}+índice
     * @param R             restricciones (&lt;=, &gt;= o =)
     * @param tipo          "Max" o "Min"
     * @param letraEntrada  letra de las variables del problema recibido ('x' o 'y')
     * @param letraSalida   letra con que se escriben las variables del dual
     * @throws IllegalArgumentException si el problema está mal escrito
     */
    public static Resultado dualizar(String FO, String[] R, String tipo, char letraEntrada, char letraSalida) {
        if (FO == null || FO.trim().isEmpty() || R == null || R.length == 0) {
            throw new IllegalArgumentException("Para pasar al dual hacen falta la función objetivo y las restricciones.");
        }
        if (tipo == null || !(tipo.equalsIgnoreCase("Max") || tipo.equalsIgnoreCase("Min"))) {
            throw new IllegalArgumentException("El tipo de problema debe ser \"Max\" o \"Min\" (recibido: \"" + tipo + "\").");
        }
        boolean maximizar = tipo.equalsIgnoreCase("Max");

        // los parsers de Vertex leen x: la letra de entrada se traduce antes
        String foX = aLetraX(FO, letraEntrada);
        Map<Integer, Double> coefsFO = ParserLP.parsearTerminos(foX);
        if (coefsFO.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron variables en la función objetivo.");
        }

        List<double[]> filas = new ArrayList<>();
        List<Double> lados = new ArrayList<>();
        List<String> pasos = new ArrayList<>();
        List<Map<Integer, Double>> lhsLeidos = new ArrayList<>();
        List<String> operadores = new ArrayList<>();
        List<Double> rhsLeidos = new ArrayList<>();

        TreeSet<Integer> indices = new TreeSet<>(coefsFO.keySet());
        int nRestriccion = 0;
        for (String restriccion : R) {
            nRestriccion++;
            if (restriccion == null || restriccion.trim().isEmpty()) {
                throw new IllegalArgumentException("La restricción " + nRestriccion + " está vacía.");
            }
            String[] partes = ParserLP.partirRestriccion(aLetraX(restriccion, letraEntrada));
            Map<Integer, Double> lhs = ParserLP.parsearTerminos(partes[0]);
            double rhs;
            try {
                rhs = Double.parseDouble(partes[2]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("La restricción " + nRestriccion + " tiene un término independiente no numérico: " + partes[2]);
            }
            for (Integer k : lhs.keySet()) {
                if (!coefsFO.containsKey(k)) {
                    throw new IllegalArgumentException("Variable " + letraEntrada + k
                            + " aparece en restricciones pero no en la función objetivo.");
                }
            }
            lhsLeidos.add(lhs);
            operadores.add(partes[1]);
            rhsLeidos.add(rhs);
        }

        List<Integer> variables = new ArrayList<>(indices);
        int n = variables.size();
        String sentidoPedido = maximizar ? "<=" : ">=";

        for (int i = 0; i < lhsLeidos.size(); i++) {
            double[] a = new double[n];
            for (int j = 0; j < n; j++) {
                a[j] = lhsLeidos.get(i).getOrDefault(variables.get(j), 0.0);
            }
            double b = rhsLeidos.get(i);
            String op = operadores.get(i);
            if ("=".equals(op)) {
                pasos.add("La restricción " + (i + 1) + " es una igualdad: se partió en una <= y una >=.");
                // la del sentido pedido queda igual; la otra se multiplica por −1
                filas.add(a.clone());
                lados.add(b);
                filas.add(negar(a));
                lados.add(-b);
            } else if (op.equals(sentidoPedido)) {
                filas.add(a);
                lados.add(b);
            } else {
                pasos.add("La restricción " + (i + 1) + " (" + op + ") se multiplicó por −1 para quedar " + sentidoPedido + ".");
                filas.add(negar(a));
                lados.add(-b);
            }
        }

        int m = filas.size();
        StringBuilder fo = new StringBuilder();
        for (int i = 0; i < m; i++) {
            agregarTermino(fo, lados.get(i), letraSalida, i + 1, true);
        }

        String sentidoDual = maximizar ? ">=" : "<=";
        String[] rDual = new String[n];
        for (int j = 0; j < n; j++) {
            StringBuilder lhs = new StringBuilder();
            for (int i = 0; i < m; i++) {
                agregarTermino(lhs, filas.get(i)[j], letraSalida, i + 1, false);
            }
            if (lhs.length() == 0) {
                lhs.append("0").append(letraSalida).append(1); // la variable no aparece en ninguna restricción
            }
            rDual[j] = lhs + " " + sentidoDual + " " + numero(coefsFO.get(variables.get(j)));
        }

        return new Resultado(fo.toString(), rDual, maximizar ? "Min" : "Max", pasos);
    }

    /** Cambia cada "y3" (o "Y3") por "x3" en una sola pasada; con 'x' devuelve el texto igual. */
    public static String aLetraX(String texto, char letra) {
        if (texto == null || Character.toLowerCase(letra) == 'x') {
            return texto;
        }
        Matcher m = Pattern.compile("[" + Character.toLowerCase(letra) + Character.toUpperCase(letra) + "](\\d+)")
                .matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, "x" + m.group(1));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static double[] negar(double[] a) {
        double[] r = new double[a.length];
        for (int j = 0; j < a.length; j++) {
            r[j] = a[j] == 0 ? 0 : -a[j];
        }
        return r;
    }

    /**
     * Agrega "± c·letra·k" con el formato del Editor ("3x1 + x2 - 2x3").
     * En la función objetivo van todas las variables, también con 0, para
     * que ninguna restricción use una variable que Z no tenga.
     */
    private static void agregarTermino(StringBuilder sb, double c, char letra, int k, boolean incluirCero) {
        if (Math.abs(c) < EPS) {
            if (!incluirCero) {
                return;
            }
            c = 0;
        }
        boolean negativo = c < 0;
        double abs = Math.abs(c);
        String coef = Math.abs(abs - 1) < EPS ? "" : numero(abs);
        if (sb.length() == 0) {
            sb.append(negativo ? "-" : "");
        } else {
            sb.append(negativo ? " - " : " + ");
        }
        sb.append(coef).append(letra).append(k);
    }

    /** Enteros sin decimales; el resto, tal cual (misma regla que los coeficientes del Editor). */
    static String numero(double v) {
        if (Math.abs(v - Math.round(v)) < 1e-9) {
            return String.valueOf(Math.round(v));
        }
        return String.valueOf(v);
    }
}
