package com.mycompany.problema.programacion.lineal.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mycompany.problema.programacion.lineal.PrincipalPage;

/**
 * Parser para el formato de "doble índice" (x_ij) que usan Húngaro y
 * Costos Duales — una variable x_ij representa "asignar/enviar del
 * origen i al destino j", no un término más de una expresión lineal
 * plana. Por eso vive aparte de {@link ParserLP}: son dos dialectos
 * distintos de la misma notación "xN".
 *
 * Antes esta lógica estaba duplicada casi línea por línea en
 * AlgoritmoHungaro y AlgoritmoCostosDuales.
 */
public final class ParserTransporte {

    private static final Pattern TERMINO = Pattern.compile("([+-]?\\d*\\.?\\d*)x(\\d+)", Pattern.CASE_INSENSITIVE);

    private ParserTransporte() {
    }

    /**
     * Arma la matriz de costos [origen][destino] a partir de la función
     * objetivo, donde cada variable x_ij tiene su índice partido a la
     * mitad en dígitos de origen/destino (ej. x12 → origen 1, destino 2;
     * x113 → origen 1, destino 13 si el número de dígitos es impar se
     * reparte igual que en el código original).
     */
    public static double[][] construirMatrizCostos(String FO) {
        String limpio = FO.replaceAll("\\s+", "");
        Matcher m = TERMINO.matcher(limpio);

        int maxI = 0, maxJ = 0;
        List<int[]> terminos = new ArrayList<>(); // [coef, i, j]

        while (m.find()) {
            String coefStr = m.group(1);
            int coef = coefStr.isEmpty() || coefStr.equals("+") ? 1
                    : coefStr.equals("-") ? -1 : Integer.parseInt(coefStr);

            String digitos = m.group(2);
            int mitad = digitos.length() / 2;
            int i = Integer.parseInt(digitos.substring(0, mitad));
            int j = Integer.parseInt(digitos.substring(mitad));

            terminos.add(new int[]{coef, i, j});
            maxI = Math.max(maxI, i);
            maxJ = Math.max(maxJ, j);
        }

        double[][] Zmat = new double[maxI][maxJ];
        for (int[] t : terminos) {
            Zmat[t[1] - 1][t[2] - 1] = t[0];
        }
        return Zmat;
    }

    /** Oferta (RHS) de las restricciones "de fila" (todas las x_ij con el mismo origen i). */
    public static double[] getOferta(String[] R, int filas) {
        double[] oferta = new double[filas];
        for (String restr : R) {
            String[] partes = PrincipalPage.parseRestriction(restr);
            double val;
            try {
                val = Double.parseDouble(partes[2]);
            } catch (NumberFormatException ex) {
                continue;
            }
            Set<Integer> idxFilas = indicesOrigen(partes[0]);
            if (idxFilas.size() == 1) {
                oferta[idxFilas.iterator().next() - 1] = val;
            }
        }
        return oferta;
    }

    /** Demanda (RHS) de las restricciones "de columna" (todas las x_ij con el mismo destino j). */
    public static double[] getDemanda(String[] R, int columnas) {
        double[] demanda = new double[columnas];
        for (String restr : R) {
            String[] partes = PrincipalPage.parseRestriction(restr);
            double val;
            try {
                val = Double.parseDouble(partes[2]);
            } catch (NumberFormatException ex) {
                continue;
            }
            Set<Integer> idxCols = indicesDestino(partes[0]);
            if (idxCols.size() == 1) {
                demanda[idxCols.iterator().next() - 1] = val;
            }
        }
        return demanda;
    }

    private static Set<Integer> indicesOrigen(String lhs) {
        Set<Integer> idx = new HashSet<>();
        Matcher m = TERMINO.matcher(lhs);
        while (m.find()) {
            String digitos = m.group(2);
            int mitad = digitos.length() / 2;
            idx.add(Integer.parseInt(digitos.substring(0, mitad)));
        }
        return idx;
    }

    private static Set<Integer> indicesDestino(String lhs) {
        Set<Integer> idx = new HashSet<>();
        Matcher m = TERMINO.matcher(lhs);
        while (m.find()) {
            String digitos = m.group(2);
            int mitad = digitos.length() / 2;
            idx.add(Integer.parseInt(digitos.substring(mitad)));
        }
        return idx;
    }

    /**
     * Verifica que la función objetivo tenga TODAS las combinaciones
     * origen-destino, de x11 hasta x(maxI)(maxJ) — igual idea que
     * {@link ParserLP#validarVariablesCompletas}: nadie escribe "0x_ij"
     * a propósito, así que si falta una combinación lo más probable es
     * un error de tipeo. Lanza IllegalArgumentException listando las
     * que faltan.
     */
    public static void validarMatrizCompleta(String FO) {
        String limpio = FO.replaceAll("\\s+", "");
        Matcher m = TERMINO.matcher(limpio);

        Set<String> presentes = new java.util.HashSet<>();
        int maxI = 0, maxJ = 0;
        while (m.find()) {
            String digitos = m.group(2);
            int mitad = digitos.length() / 2;
            int i = Integer.parseInt(digitos.substring(0, mitad));
            int j = Integer.parseInt(digitos.substring(mitad));
            presentes.add(i + "," + j);
            maxI = Math.max(maxI, i);
            maxJ = Math.max(maxJ, j);
        }
        if (maxI == 0 || maxJ == 0) {
            return;
        }

        List<String> faltantes = new ArrayList<>();
        for (int i = 1; i <= maxI; i++) {
            for (int j = 1; j <= maxJ; j++) {
                if (!presentes.contains(i + "," + j)) {
                    faltantes.add("x" + i + j);
                }
            }
        }
        if (!faltantes.isEmpty()) {
            boolean singular = faltantes.size() == 1;
            throw new IllegalArgumentException(
                    "A la función objetivo le falta" + (singular ? " " : "n ") + faltantes.size()
                    + (singular ? " variable: " : " variables: ") + String.join(", ", faltantes) + ".\n"
                    + "Se esperaban todas las combinaciones de x11 a x" + maxI + "" + maxJ + ".");
        }
    }
}
