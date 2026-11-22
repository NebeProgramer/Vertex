package com.mycompany.problema.programacion.lineal.algoritmos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Enumeración básica con filtro entero para Branch & Bound. */
public final class ProgramacionEntera {

    private static final double EPS = 1e-7;

    public enum TipoVariable {
        CONTINUA,
        ENTERA,
        BINARIA
    }

    public static final class Resultado {
        public final boolean factible;
        public final boolean noAcotado;
        public final boolean multiples;
        public final double[] solucion;
        public final double z;
        public final List<double[]> solucionesOptimas;

        Resultado(boolean factible, boolean noAcotado, boolean multiples,
                double[] solucion, double z, List<double[]> solucionesOptimas) {
            this.factible = factible;
            this.noAcotado = noAcotado;
            this.multiples = multiples;
            this.solucion = solucion == null ? null : solucion.clone();
            this.z = z;
            this.solucionesOptimas = Collections.unmodifiableList(solucionesOptimas);
        }
    }

    private ProgramacionEntera() {
    }

    public static Resultado resolver(double[] c, double[][] A, double[] b, boolean esMin,
            TipoVariable[] tipos) {
        int n = c.length;
        int m = b.length;
        if (tipos == null || tipos.length != n) {
            throw new IllegalArgumentException("Debe indicarse el tipo de cada variable.");
        }

        List<double[]> candidatos = new ArrayList<>();
        double mejorZ = esMin ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

        double[][] mA = new double[m][n];
        double[] mb = b.clone();
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, mA[i], 0, n);
        }

        generarEnumeraciones(candidatos, mA, mb, c, tipos, 0, new double[n], esMin);

        List<double[]> optimas = new ArrayList<>();
        for (double[] x : candidatos) {
            double z = punto(c, x);
            boolean mejor = esMin ? z < mejorZ - EPS : z > mejorZ + EPS;
            if (mejor) {
                mejorZ = z;
                optimas.clear();
                optimas.add(x.clone());
            } else if (Math.abs(z - mejorZ) <= EPS) {
                optimas.add(x.clone());
            }
        }

        if (optimas.isEmpty()) {
            return new Resultado(false, false, false, null, 0, Collections.emptyList());
        }
        return new Resultado(true, false, optimas.size() > 1, optimas.get(0),
                mejorZ, optimas);
    }

    private static void generarEnumeraciones(List<double[]> candidatos, double[][] A, double[] b,
            double[] c, TipoVariable[] tipos, int k, double[] x, boolean esMin) {
        if (k == tipos.length) {
            if (cumpleRestricciones(A, b, x)) {
                candidatos.add(x.clone());
            }
            return;
        }

        double limite = tipos[k] == TipoVariable.BINARIA ? 1 : 100;
        for (int v = tipos[k] == TipoVariable.CONTINUA ? 0 : 0;
                tipos[k] == TipoVariable.CONTINUA || v <= limite; v++) {
            if (tipos[k] == TipoVariable.CONTINUA) {
                break;
            }
            x[k] = v;
            generarEnumeraciones(candidatos, A, b, c, tipos, k + 1, x, esMin);
        }
    }

    private static boolean cumpleRestricciones(double[][] A, double[] b, double[] x) {
        for (int i = 0; i < b.length; i++) {
            double valor = 0;
            for (int j = 0; j < x.length; j++) {
                valor += A[i][j] * x[j];
            }
            if (valor > b[i] + EPS) {
                return false;
            }
        }
        return true;
    }

    private static double punto(double[] c, double[] x) {
        double z = 0;
        for (int i = 0; i < c.length; i++) {
            z += c[i] * x[i];
        }
        return z;
    }
}
