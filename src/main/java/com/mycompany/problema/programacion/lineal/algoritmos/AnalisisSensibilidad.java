package com.mycompany.problema.programacion.lineal.algoritmos;

/**
 * Análisis de sensibilidad a partir de la base óptima de un problema con
 * restricciones &lt;= (forma estándar con holguras), como la que dejan el
 * Simplex y el Simplex Revisado. Con B⁻¹ de esa base calcula:
 *
 * - Precio sombra de cada restricción: cuánto cambia Z por cada unidad
 *   más de lado derecho (π = c_B·B⁻¹, con el signo del objetivo del usuario).
 * - Holgura de cada restricción (cuánto sobra del recurso).
 * - Costo reducido de cada variable de decisión.
 * - Rango del lado derecho de cada restricción en el que el precio sombra
 *   sigue siendo válido (la base óptima no cambia).
 * - Rango del coeficiente de Z de cada variable en el que la solución
 *   óptima no cambia.
 *
 * Convenciones de signo como las de Solver de Excel: en Max el costo
 * reducido es &lt;= 0, en Min es &gt;= 0. Los "aumento/disminución
 * permisibles" pueden ser Double.POSITIVE_INFINITY.
 */
public final class AnalisisSensibilidad {

    private static final double EPS = 1e-9;

    // ---- variables de decisión ----
    public final double[] valor;
    public final double[] costoReducido;
    public final double[] coeficiente;
    public final double[] aumentoCoeficiente;
    public final double[] disminucionCoeficiente;

    // ---- restricciones ----
    public final double[] holgura;
    public final double[] precioSombra;
    public final double[] ladoDerecho;
    public final double[] aumentoLadoDerecho;
    public final double[] disminucionLadoDerecho;

    public final double z;

    private AnalisisSensibilidad(int n, int m) {
        valor = new double[n];
        costoReducido = new double[n];
        coeficiente = new double[n];
        aumentoCoeficiente = new double[n];
        disminucionCoeficiente = new double[n];
        holgura = new double[m];
        precioSombra = new double[m];
        ladoDerecho = new double[m];
        aumentoLadoDerecho = new double[m];
        disminucionLadoDerecho = new double[m];
        z = 0;
    }

    private AnalisisSensibilidad(AnalisisSensibilidad base, double z) {
        valor = base.valor;
        costoReducido = base.costoReducido;
        coeficiente = base.coeficiente;
        aumentoCoeficiente = base.aumentoCoeficiente;
        disminucionCoeficiente = base.disminucionCoeficiente;
        holgura = base.holgura;
        precioSombra = base.precioSombra;
        ladoDerecho = base.ladoDerecho;
        aumentoLadoDerecho = base.aumentoLadoDerecho;
        disminucionLadoDerecho = base.disminucionLadoDerecho;
        this.z = z;
    }

    /**
     * @param c         coeficientes de Z de las n variables de decisión (como los escribió el usuario)
     * @param A         m × n coeficientes de las restricciones (todas &lt;=)
     * @param b         lados derechos
     * @param esMin     true si se minimiza
     * @param baseFinal columnas básicas de la tabla óptima (0..n-1 decisión, n..n+m-1 holguras)
     */
    public static AnalisisSensibilidad calcular(double[] c, double[][] A, double[] b, boolean esMin, int[] baseFinal) {
        int n = c.length;
        int m = b.length;
        double[][] a = SimplexRevisado.estandar(A, m, n);
        double[] cI = new double[n + m];
        for (int j = 0; j < n; j++) {
            cI[j] = esMin ? -c[j] : c[j];
        }

        double[][] B = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int r = 0; r < m; r++) {
                B[i][r] = a[i][baseFinal[r]];
            }
        }
        double[][] Binv = Matrices.inversa(B);
        double[] xB = Matrices.porVector(Binv, b);
        double[] cB = new double[m];
        for (int r = 0; r < m; r++) {
            cB[r] = cI[baseFinal[r]];
        }
        double[] pi = Matrices.vectorPor(cB, Binv);

        int[] filaDe = new int[n + m];
        java.util.Arrays.fill(filaDe, -1);
        for (int r = 0; r < m; r++) {
            filaDe[baseFinal[r]] = r;
        }
        double[] cBarra = new double[n + m];
        for (int j = 0; j < n + m; j++) {
            if (filaDe[j] < 0) {
                cBarra[j] = cI[j] - Matrices.punto(pi, Matrices.columna(a, j));
            }
        }

        AnalisisSensibilidad s = new AnalisisSensibilidad(n, m);
        double zInterno = Matrices.punto(cB, xB);

        for (int j = 0; j < n; j++) {
            s.coeficiente[j] = c[j];
            s.valor[j] = filaDe[j] >= 0 ? limpiar(xB[filaDe[j]]) : 0;
            s.costoReducido[j] = limpiar(esMin ? -cBarra[j] : cBarra[j]);

            // rango del cambio δ del costo INTERNO (maximización) que mantiene la base óptima
            double bajo, alto;
            if (filaDe[j] < 0) {
                bajo = Double.NEGATIVE_INFINITY; // bajarlo la hace aún menos atractiva
                alto = -cBarra[j];               // subirlo más que esto la haría entrar
            } else {
                bajo = Double.NEGATIVE_INFINITY;
                alto = Double.POSITIVE_INFINITY;
                int r = filaDe[j];
                for (int k = 0; k < n + m; k++) {
                    if (filaDe[k] >= 0) {
                        continue;
                    }
                    double alfa = Matrices.punto(Binv[r], Matrices.columna(a, k));
                    if (alfa > EPS) {
                        bajo = Math.max(bajo, cBarra[k] / alfa);
                    } else if (alfa < -EPS) {
                        alto = Math.min(alto, cBarra[k] / alfa);
                    }
                }
            }
            // en Min el coeficiente del usuario es −c interno: el rango se invierte
            s.aumentoCoeficiente[j] = limpiar(esMin ? -bajo : alto);
            s.disminucionCoeficiente[j] = limpiar(esMin ? alto : -bajo);
        }

        for (int i = 0; i < m; i++) {
            s.ladoDerecho[i] = b[i];
            double usado = 0;
            for (int j = 0; j < n; j++) {
                usado += A[i][j] * s.valor[j];
            }
            s.holgura[i] = limpiar(b[i] - usado);
            s.precioSombra[i] = limpiar(esMin ? -pi[i] : pi[i]);

            // x_B + Δ·B⁻¹e_i >= 0
            double bajo = Double.NEGATIVE_INFINITY, alto = Double.POSITIVE_INFINITY;
            for (int r = 0; r < m; r++) {
                double beta = Binv[r][i];
                if (beta > EPS) {
                    bajo = Math.max(bajo, -xB[r] / beta);
                } else if (beta < -EPS) {
                    alto = Math.min(alto, -xB[r] / beta);
                }
            }
            s.aumentoLadoDerecho[i] = limpiar(alto);
            s.disminucionLadoDerecho[i] = limpiar(-bajo);
        }
        return new AnalisisSensibilidad(s, limpiar(esMin ? -zInterno : zInterno));
    }

    /** Quita el "-0" y el ruido de punto flotante (1e-13) para mostrar los números limpios. */
    private static double limpiar(double v) {
        if (Double.isInfinite(v)) {
            return v;
        }
        return Math.abs(v) < 1e-10 ? 0 : v;
    }
}
