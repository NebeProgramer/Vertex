package com.mycompany.problema.programacion.lineal.algoritmos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Método Simplex Revisado: en lugar de pivotear toda la tabla, en cada
 * iteración trabaja con tres datos del problema —A (coeficientes, ya en
 * forma estándar con holguras), R (lado derecho) y c (objetivo)— y con la
 * matriz B formada por las columnas de A de las variables básicas:
 *
 *   x_B = B⁻¹·R          valores de las variables básicas
 *   π   = c_B·B⁻¹        multiplicadores (precios duales)
 *   c̄_j = c_j − π·a_j    costos reducidos: entra la de mayor c̄_j > 0
 *   d   = B⁻¹·a_q        columna de la que entra: sale la de razón mínima
 *
 * Mismos requisitos que el Simplex de tabla: restricciones &lt;= con lado
 * derecho &gt;= 0 (la base inicial son las holguras). Internamente siempre
 * maximiza; un problema Min se resuelve maximizando −c y negando Z.
 *
 * Clase sin Swing: la ventana MetodoSimplexRevisado solo muestra lo que
 * calcula aquí, y los tests la prueban directamente.
 */
public final class SimplexRevisado {

    private static final double EPS = 1e-9;
    private static final int ITERACIONES_MAX = 200;

    private SimplexRevisado() {
    }

    /** Una iteración completa, con todas las matrices que se muestran en pantalla. */
    public static final class Iteracion {

        /** Índice (columna de A estándar) de cada variable básica, por fila. */
        public int[] base;
        public double[][] B;
        public double[][] Binv;
        public double[] xB;
        public double[] cB;
        public double[] pi;
        /** c̄_j para todas las columnas (0 en las básicas). */
        public double[] costosReducidos;
        /** Columna que entra, o -1 si esta iteración ya es óptima. */
        public int entra = -1;
        /** d = B⁻¹·a_entra (null si no entra nadie). */
        public double[] d;
        /** x_B[i] / d[i] para d[i] &gt; 0; NaN donde no aplica. */
        public double[] razones;
        /** Fila de la base que sale, o -1 (óptimo o no acotado). */
        public int filaSale = -1;
        /** Z con el signo del usuario (ya negado si es Min). */
        public double z;
        public boolean optima;
        public boolean noAcotado;
    }

    public static final class Resultado {

        public final List<Iteracion> iteraciones;
        /** Valores de las n variables de decisión. */
        public final double[] solucion;
        public final double z;
        public final int[] baseFinal;
        public final boolean noAcotado;
        /** [A | I]: m filas, n + m columnas (decisión y holguras). */
        public final double[][] aEstandar;
        /** Costos internos (maximización): c, o −c si el problema es Min; holguras con 0. */
        public final double[] cInterno;

        Resultado(List<Iteracion> iteraciones, double[] solucion, double z, int[] baseFinal,
                boolean noAcotado, double[][] aEstandar, double[] cInterno) {
            this.iteraciones = Collections.unmodifiableList(iteraciones);
            this.solucion = solucion;
            this.z = z;
            this.baseFinal = baseFinal;
            this.noAcotado = noAcotado;
            this.aEstandar = aEstandar;
            this.cInterno = cInterno;
        }
    }

    /**
     * @param c     coeficientes de Z de las n variables de decisión, como los escribió el usuario
     * @param A     m × n coeficientes de las restricciones (todas &lt;=)
     * @param b     lados derechos (todos &gt;= 0)
     * @param esMin true si se minimiza
     */
    public static Resultado resolver(double[] c, double[][] A, double[] b, boolean esMin) {
        int m = b.length;
        int n = c.length;
        for (double bi : b) {
            if (bi < -EPS) {
                throw new IllegalArgumentException("El Simplex Revisado parte de las holguras como base:"
                        + " necesita lados derechos >= 0 (para uno negativo usa Simplex Dual).");
            }
        }
        double[][] a = estandar(A, m, n);
        double[] cI = new double[n + m];
        for (int j = 0; j < n; j++) {
            cI[j] = esMin ? -c[j] : c[j];
        }

        int[] base = new int[m];
        for (int i = 0; i < m; i++) {
            base[i] = n + i; // base inicial: holguras (B = I)
        }

        List<Iteracion> iteraciones = new ArrayList<>();
        boolean noAcotado = false;
        for (int k = 0; k < ITERACIONES_MAX; k++) {
            Iteracion it = calcularIteracion(a, b, cI, base, esMin);
            iteraciones.add(it);
            if (it.optima) {
                break;
            }
            if (it.noAcotado) {
                noAcotado = true;
                break;
            }
            base = base.clone();
            base[it.filaSale] = it.entra;
        }

        Iteracion ultima = iteraciones.get(iteraciones.size() - 1);
        double[] solucion = new double[n];
        for (int i = 0; i < m; i++) {
            if (ultima.base[i] < n) {
                solucion[ultima.base[i]] = ultima.xB[i];
            }
        }
        return new Resultado(iteraciones, solucion, ultima.z, ultima.base.clone(), noAcotado, a, cI);
    }

    /** Todas las matrices de una iteración a partir de la base actual. */
    static Iteracion calcularIteracion(double[][] a, double[] b, double[] cI, int[] base, boolean esMin) {
        int m = b.length;
        int columnas = cI.length;
        Iteracion it = new Iteracion();
        it.base = base.clone();
        it.B = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int r = 0; r < m; r++) {
                it.B[i][r] = a[i][base[r]];
            }
        }
        it.Binv = Matrices.inversa(it.B);
        it.xB = Matrices.porVector(it.Binv, b);
        it.cB = new double[m];
        for (int r = 0; r < m; r++) {
            it.cB[r] = cI[base[r]];
        }
        it.pi = Matrices.vectorPor(it.cB, it.Binv);
        double zInterno = Matrices.punto(it.cB, it.xB);
        it.z = esMin ? -zInterno : zInterno;

        boolean[] esBasica = new boolean[columnas];
        for (int r : base) {
            esBasica[r] = true;
        }
        it.costosReducidos = new double[columnas];
        double mejor = EPS;
        for (int j = 0; j < columnas; j++) {
            if (esBasica[j]) {
                continue;
            }
            it.costosReducidos[j] = cI[j] - Matrices.punto(it.pi, Matrices.columna(a, j));
            if (it.costosReducidos[j] > mejor) {
                mejor = it.costosReducidos[j];
                it.entra = j;
            }
        }
        if (it.entra == -1) {
            it.optima = true;
            return it;
        }

        it.d = Matrices.porVector(it.Binv, Matrices.columna(a, it.entra));
        it.razones = new double[m];
        double menor = Double.MAX_VALUE;
        for (int i = 0; i < m; i++) {
            it.razones[i] = Double.NaN;
            if (it.d[i] > EPS) {
                it.razones[i] = it.xB[i] / it.d[i];
                if (it.razones[i] < menor - EPS) {
                    menor = it.razones[i];
                    it.filaSale = i;
                }
            }
        }
        it.noAcotado = it.filaSale == -1;
        return it;
    }

    /** [A | I] con m filas y n + m columnas. */
    static double[][] estandar(double[][] A, int m, int n) {
        double[][] a = new double[m][n + m];
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, a[i], 0, n);
            a[i][n + i] = 1.0;
        }
        return a;
    }
}
