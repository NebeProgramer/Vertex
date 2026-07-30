package com.mycompany.problema.programacion.lineal.parser;

import com.mycompany.problema.programacion.lineal.modelo.ProblemaPL;
import com.mycompany.problema.programacion.lineal.modelo.Restriccion;
import com.mycompany.problema.programacion.lineal.modelo.TipoRestriccion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser único para expresiones de Programación Lineal con variables tipo
 * "x1", "x2"... (holguras/excesos/artificiales de cada algoritmo se agregan
 * aparte, después de este parseo). Centraliza lo que antes estaba repetido
 * (con pequeñas diferencias) en PrincipalPage, MetodoGrafico y cada ventana
 * de algoritmo.
 *
 * No se usa para el formato de doble índice (x_ij) de Húngaro/Costos
 * Duales — ese vive en {@link ParserTransporte}, porque es un formato
 * distinto (variables de asignación, no términos de una expresión lineal
 * plana).
 */
public final class ParserLP {

    private static final Pattern OPERADOR = Pattern.compile("(<=|>=|=)");
    // término completo: coeficiente (con signo y decimales opcionales) + x + índice
    private static final Pattern TERMINO_COMPLETO = Pattern.compile("^([+-]?\\d*\\.?\\d*)x(\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VARIABLE = Pattern.compile("x(\\d+)", Pattern.CASE_INSENSITIVE);

    private ParserLP() {
    }

    /**
     * Parsea función objetivo + restricciones + tipo en un {@link ProblemaPL}
     * completo. Valida la entrada y lanza {@link IllegalArgumentException}
     * con un mensaje claro ante entradas mal formadas (FO vacía, término
     * inválido como "2x1++3x2", restricción sin operador, tipo distinto de
     * Max/Min, etc.).
     */
    public static ProblemaPL parsear(String FO, String[] R, String tipo) {
        if (FO == null || FO.trim().isEmpty()) {
            throw new IllegalArgumentException("La función objetivo está vacía.");
        }
        if (tipo == null || !(tipo.equalsIgnoreCase("Max") || tipo.equalsIgnoreCase("Min"))) {
            throw new IllegalArgumentException("El tipo de problema debe ser \"Max\" o \"Min\" (recibido: \"" + tipo + "\").");
        }
        boolean maximizar = tipo.equalsIgnoreCase("Max");

        String foLimpia = FO.replaceAll("\\s+", "");
        Map<Integer, Double> coefsFO = parsearTerminos(foLimpia);
        if (coefsFO.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron variables (x1, x2, ...) en la función objetivo: \"" + FO + "\".");
        }

        List<Integer> variables = new ArrayList<>(new TreeSet<>(coefsFO.keySet()));
        double[] objetivo = new double[variables.size()];
        for (int k = 0; k < variables.size(); k++) {
            objetivo[k] = coefsFO.get(variables.get(k));
        }

        if (R == null || R.length == 0) {
            throw new IllegalArgumentException("No hay restricciones.");
        }

        List<Restriccion> restricciones = new ArrayList<>();
        for (int i = 0; i < R.length; i++) {
            if (R[i] == null || R[i].trim().isEmpty()) {
                throw new IllegalArgumentException("La restricción " + (i + 1) + " está vacía.");
            }
            String[] partes = partirRestriccion(R[i]);
            Map<Integer, Double> coefsFila = parsearTerminos(partes[0]);

            double[] coeficientes = new double[variables.size()];
            for (int k = 0; k < variables.size(); k++) {
                coeficientes[k] = coefsFila.getOrDefault(variables.get(k), 0.0);
            }

            double independiente;
            try {
                independiente = Double.parseDouble(partes[2]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("La restricción " + (i + 1) + " (\"" + R[i] + "\") tiene un término independiente inválido.");
            }

            TipoRestriccion tipoR = TipoRestriccion.desdeSimbolo(partes[1]);
            restricciones.add(new Restriccion(coeficientes, independiente, tipoR, R[i]));
        }

        return new ProblemaPL(variables, objetivo, restricciones, maximizar, FO);
    }

    /**
     * Divide una restricción "lhs OP rhs" en sus 3 partes.
     * Compatible con el antiguo {@code PrincipalPage.parseRestriction}.
     */
    public static String[] partirRestriccion(String restr) {
        if (restr == null) {
            throw new IllegalArgumentException("Restricción nula.");
        }
        String limpia = restr.replaceAll("\\s+", "");
        Matcher opMatcher = OPERADOR.matcher(limpia);
        if (!opMatcher.find()) {
            throw new IllegalArgumentException("Restricción inválida (falta operador <=, >= o =): \"" + restr + "\"");
        }
        String operador = opMatcher.group();
        String lhs = limpia.substring(0, opMatcher.start());
        String rhs = limpia.substring(opMatcher.end());
        if (lhs.isEmpty()) {
            throw new IllegalArgumentException("Restricción inválida (no hay nada antes de '" + operador + "'): \"" + restr + "\"");
        }
        if (rhs.isEmpty()) {
            throw new IllegalArgumentException("Restricción inválida (no hay término independiente después de '" + operador + "'): \"" + restr + "\"");
        }
        return new String[]{lhs, operador, rhs};
    }

    /**
     * Parsea una expresión tipo "2x1-3.5x2+x1" en un mapa índice→coeficiente
     * (variables repetidas se suman). Lanza excepción si algún término no
     * tiene la forma "[+-][número]x[índice]" — por ejemplo "2x1++3x2" deja
     * un token "+" suelto que antes se ignoraba en silencio; ahora se avisa.
     */
    public static Map<Integer, Double> parsearTerminos(String expresion) {
        Map<Integer, Double> coefs = new LinkedHashMap<>();
        String limpia = expresion.replaceAll("\\s+", "");
        if (limpia.isEmpty()) {
            return coefs;
        }

        String[] terminos = limpia.split("(?=[+-])");
        for (String termino : terminos) {
            if (termino.isEmpty() || termino.equals("+") || termino.equals("-")) {
                throw new IllegalArgumentException("Término inválido en la expresión \"" + expresion + "\" (¿operador repetido, como '++' o '--'?).");
            }
            Matcher m = TERMINO_COMPLETO.matcher(termino);
            if (!m.matches()) {
                throw new IllegalArgumentException("Término inválido en la expresión \"" + expresion + "\": \"" + termino + "\".");
            }
            String coefStr = m.group(1);
            int indice = Integer.parseInt(m.group(2));
            double coef;
            if (coefStr.isEmpty() || coefStr.equals("+")) {
                coef = 1.0;
            } else if (coefStr.equals("-")) {
                coef = -1.0;
            } else {
                coef = Double.parseDouble(coefStr);
            }
            coefs.merge(indice, coef, Double::sum); // variables repetidas se suman
        }
        return coefs;
    }

    /** Cuenta cuántas variables distintas (x1, x2...) aparecen en una expresión. */
    public static int contarVariables(String expresion) {
        TreeSet<Integer> idxs = new TreeSet<>();
        Matcher m = VARIABLE.matcher(expresion == null ? "" : expresion);
        while (m.find()) {
            idxs.add(Integer.parseInt(m.group(1)));
        }
        return idxs.size();
    }

    /**
     * Verifica que la función objetivo tenga TODAS las variables usadas en
     * el problema, de x1 hasta la de mayor índice — sin huecos. Nadie
     * escribe explícitamente "0x2" en una fórmula, así que si una
     * restricción usa x9 pero la FO solo tiene x1 y x2, lo más probable
     * es un error de tipeo (o de índice) y no una variable con
     * coeficiente 0 a propósito. Lanza IllegalArgumentException listando
     * las que faltan.
     */
    public static void validarVariablesCompletas(String FO, String[] R) {
        Map<Integer, Double> coefsFO = parsearTerminos(FO.replaceAll("\\s+", ""));
        TreeSet<Integer> usadas = new TreeSet<>(coefsFO.keySet());
        if (R != null) {
            for (String r : R) {
                if (r == null || r.trim().isEmpty()) {
                    continue;
                }
                String lhs = partirRestriccion(r)[0];
                usadas.addAll(parsearTerminos(lhs).keySet());
            }
        }
        if (usadas.isEmpty()) {
            return;
        }
        int maxIdx = usadas.last();

        List<Integer> faltantes = new ArrayList<>();
        for (int i = 1; i <= maxIdx; i++) {
            if (!coefsFO.containsKey(i)) {
                faltantes.add(i);
            }
        }
        if (!faltantes.isEmpty()) {
            StringBuilder nombres = new StringBuilder();
            for (int i = 0; i < faltantes.size(); i++) {
                if (i > 0) {
                    nombres.append(", ");
                }
                nombres.append("x").append(faltantes.get(i));
            }
            boolean singular = faltantes.size() == 1;
            throw new IllegalArgumentException(
                    "A la función objetivo le falta" + (singular ? " " : "n ") + faltantes.size()
                    + (singular ? " variable: " : " variables: ") + nombres + ".\n"
                    + "El problema usa variables hasta x" + maxIdx
                    + ", así que la FO debería incluirlas todas de x1 a x" + maxIdx + ".");
        }
    }
}
