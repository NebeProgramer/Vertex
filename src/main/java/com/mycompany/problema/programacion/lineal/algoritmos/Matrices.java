package com.mycompany.problema.programacion.lineal.algoritmos;

/** Operaciones de matrices que usan el Simplex Revisado y el análisis de sensibilidad. */
public final class Matrices {

    private Matrices() {
    }

    /**
     * Inversa por Gauss-Jordan con pivoteo parcial.
     *
     * @throws IllegalArgumentException si la matriz es singular (la base no es válida)
     */
    public static double[][] inversa(double[][] M) {
        int n = M.length;
        double[][] a = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(M[i], 0, a[i], 0, n);
            a[i][n + i] = 1.0;
        }
        for (int col = 0; col < n; col++) {
            int pivote = col;
            for (int i = col + 1; i < n; i++) {
                if (Math.abs(a[i][col]) > Math.abs(a[pivote][col])) {
                    pivote = i;
                }
            }
            if (Math.abs(a[pivote][col]) < 1e-12) {
                throw new IllegalArgumentException("La matriz B es singular: la base no es válida.");
            }
            double[] tmp = a[col];
            a[col] = a[pivote];
            a[pivote] = tmp;
            double p = a[col][col];
            for (int j = 0; j < 2 * n; j++) {
                a[col][j] /= p;
            }
            for (int i = 0; i < n; i++) {
                if (i != col && a[i][col] != 0) {
                    double f = a[i][col];
                    for (int j = 0; j < 2 * n; j++) {
                        a[i][j] -= f * a[col][j];
                    }
                }
            }
        }
        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], n, inv[i], 0, n);
            for (int j = 0; j < n; j++) {
                if (Math.abs(inv[i][j]) < 1e-12) {
                    inv[i][j] = 0; // evita mostrar "-0" por errores de redondeo
                }
            }
        }
        return inv;
    }

    /** M·v */
    public static double[] porVector(double[][] M, double[] v) {
        double[] r = new double[M.length];
        for (int i = 0; i < M.length; i++) {
            r[i] = punto(M[i], v);
        }
        return r;
    }

    /** v·M (vector fila por matriz) */
    public static double[] vectorPor(double[] v, double[][] M) {
        int columnas = M[0].length;
        double[] r = new double[columnas];
        for (int j = 0; j < columnas; j++) {
            for (int i = 0; i < v.length; i++) {
                r[j] += v[i] * M[i][j];
            }
        }
        return r;
    }

    public static double punto(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }

    public static double[] columna(double[][] M, int j) {
        double[] c = new double[M.length];
        for (int i = 0; i < M.length; i++) {
            c[i] = M[i][j];
        }
        return c;
    }
}
