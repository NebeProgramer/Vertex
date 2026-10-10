package com.mycompany.problema.programacion.lineal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/**
 * Subventana que se abre al hacer clic en el botón "(?)" de la barra de
 * título de cada ventana de algoritmo. Enseña, con el detalle de una clase
 * de investigación de operaciones (fundamento matemático, el ciclo exacto
 * del algoritmo y un ejemplo numérico resuelto y verificado a mano), cómo
 * funciona el método de esa ventana — y al final recuerda la colorimetría
 * de los resaltados de la tabla de pasos.
 *
 * El botón sigue mostrando la colorimetría como tooltip al pasar el mouse
 * (para quien ya sabe qué significa cada color y solo quiere el recordatorio
 * rápido); esta ventana es para quien quiere entender el algoritmo desde
 * cero.
 *
 * El contenido teórico de cada uno de los 7 algoritmos vive acá, centralizado,
 * como constantes {@code TEORIA_*}: cada ventana (MetodoSimplex, MetodoBigM,
 * etc.) solo referencia la constante que le corresponde al llamar a
 * {@link Recursos#instalarAyudaEnBarraTitulo}.
 */
public class VentanaAyudaAlgoritmo extends JDialog {

    private static final Color ACENTO = new Color(68, 56, 208);
    private static final Color TINTA = new Color(23, 45, 66);
    private static final Color BORDE_SUAVE = new Color(220, 226, 234);

    public VentanaAyudaAlgoritmo(java.awt.Window owner, String tituloAlgoritmo, String colorimetriaHtml, String teoriaHtml) {
        super(owner, "Ayuda — " + tituloAlgoritmo, ModalityType.MODELESS);
        initComponents(tituloAlgoritmo, colorimetriaHtml, teoriaHtml);
        Recursos.aplicarIcono(this);
        setLocationRelativeTo(owner);
    }

    private void initComponents(String tituloAlgoritmo, String colorimetriaHtml, String teoriaHtml) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        getContentPane().add(construirEncabezado(tituloAlgoritmo), BorderLayout.NORTH);
        getContentPane().add(construirCuerpo(colorimetriaHtml, teoriaHtml), BorderLayout.CENTER);
        getContentPane().add(construirPie(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(520, 460));
        setSize(760, 720);
    }

    private JPanel construirEncabezado(String tituloAlgoritmo) {
        JPanel encabezado = new JPanel(new GridBagLayout());
        encabezado.setBackground(ACENTO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblSubtitulo = new JLabel("CÓMO FUNCIONA");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubtitulo.setForeground(new Color(220, 216, 255));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 0);
        encabezado.add(lblSubtitulo, gbc);

        JLabel lblTitulo = new JLabel(tituloAlgoritmo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        encabezado.add(lblTitulo, gbc);

        return encabezado;
    }

    private JScrollPane construirCuerpo(String colorimetriaHtml, String teoriaHtml) {
        JEditorPane editor = new JEditorPane();
        editor.setContentType("text/html");
        editor.setEditable(false);
        editor.setFocusable(true);
        editor.setOpaque(true);
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));
        editor.setText(construirHtml(colorimetriaHtml, teoriaHtml));
        editor.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel();
        pie.setBackground(Color.WHITE);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE_SUAVE),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        pie.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(ACENTO);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(evt -> dispose());
        pie.add(btnCerrar);

        return pie;
    }

    /** Arma el documento HTML completo (teoría + colorimetría) con estilos consistentes con la paleta de Vertex. */
    private String construirHtml(String colorimetriaHtml, String teoriaHtml) {
        String tintaHex = String.format("#%02x%02x%02x", TINTA.getRed(), TINTA.getGreen(), TINTA.getBlue());
        String acentoHex = String.format("#%02x%02x%02x", ACENTO.getRed(), ACENTO.getGreen(), ACENTO.getBlue());

        // colorimetriaHtml llega envuelto en <html>...</html> (se reutiliza también como
        // tooltip del botón "(?)"), y con un <b>título</b><br> inicial que ya no hace falta
        // repetir acá porque esta ventana tiene su propio encabezado con el título.
        String colorimetriaFragmento = colorimetriaHtml
                .replaceAll("(?i)</?html>", "")
                .replaceAll("(?i)<b>.*?</b><br>", "");

        String estilos = "<style>"
                + "body{font-family:'Segoe UI',sans-serif;font-size:15px;line-height:150%;color:" + tintaHex + ";}"
                + "h3{font-size:17px;color:" + acentoHex + ";margin-top:22px;margin-bottom:6px;}"
                + "p{margin-top:6px;margin-bottom:6px;}"
                + "ol{margin-top:6px;}"
                + "li{margin-bottom:9px;}"
                + "table{margin-top:12px;margin-bottom:8px;}"
                + "th{background-color:#eeecfb;color:" + acentoHex + ";text-align:left;padding:5px 12px;}"
                + "td{padding:5px 12px;}"
                + ".ejemplo{margin-top:14px;margin-bottom:8px;padding:12px 16px;background-color:#f4f6fb;border-left:4px solid " + acentoHex + ";}"
                + ".nota{margin-top:14px;margin-bottom:8px;padding:10px 16px;background-color:#fff7e0;border-left:4px solid #d9a441;}"
                + ".colorimetria{margin-top:22px;padding:14px 16px;background-color:#f6f5ff;border-left:4px solid " + acentoHex + ";}"
                + "</style>";

        return "<html><head>" + estilos + "</head><body>"
                + teoriaHtml
                + "<div class='colorimetria'>"
                + "<b style='color:" + acentoHex + ";font-size:16px;'>Colorimetría de esta ventana</b><br><br>"
                + colorimetriaFragmento
                + "</div>"
                + "</body></html>";
    }

    private static final String TABLA = "border=\"1\" cellpadding=\"6\" cellspacing=\"0\"";

    /** Contenido teórico de la ventana de Simplex (MetodoSimplex). */
    public static final String TEORIA_SIMPLEX =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El método Simplex encuentra el valor óptimo (máximo o mínimo) de una función objetivo lineal "
            + "Z = c<sub>1</sub>x<sub>1</sub> + c<sub>2</sub>x<sub>2</sub> + ... + c<sub>n</sub>x<sub>n</sub>, sujeta a "
            + "restricciones de tipo &le; y variables no negativas (x<sub>j</sub> &ge; 0). A diferencia de probar "
            + "soluciones al azar, garantiza llegar al óptimo (si existe) en un número finito de pasos.</p>"
            + "<h3>La idea geométrica detrás del álgebra</h3>"
            + "<p>Cada restricción &le; es un semiplano (en 2D) o un semiespacio (en más dimensiones). La "
            + "intersección de todas ellas — más x<sub>j</sub> &ge; 0 — forma un poliedro convexo llamado "
            + "<b>región factible</b>. El teorema fundamental de la programación lineal garantiza que, si existe un "
            + "óptimo, siempre se alcanza en un <b>vértice</b> de ese poliedro, nunca en un punto interior. El "
            + "Simplex no revisa todos los vértices: arranca en uno fácil de encontrar (el origen, con las "
            + "variables de holgura como base) y se mueve por las <b>aristas</b> hacia vértices vecinos que "
            + "mejoren Z, hasta que ningún vecino sea mejor.</p>"
            + "<h3>Forma estándar y variables de holgura</h3>"
            + "<p>Cada restricción \"&le; b\" se convierte en una igualdad agregando una <b>variable de "
            + "holgura</b> s<sub>i</sub> &ge; 0, que representa cuánto \"sobra\" del recurso i: "
            + "a<sub>i1</sub>x<sub>1</sub> + a<sub>i2</sub>x<sub>2</sub> + ... + s<sub>i</sub> = b<sub>i</sub>. Con "
            + "esto el sistema queda listo para representarse en una tabla (tableau): cada fila es una "
            + "restricción, cada columna una variable, y la fila Z lleva los coeficientes del objetivo (con signo "
            + "cambiado, para leer directamente si conviene aumentar cada variable).</p>"
            + "<h3>El ciclo del algoritmo, paso a paso</h3>"
            + "<ol>"
            + "<li><b>Columna pivote (variable que entra):</b> en la fila Z se busca el coeficiente más "
            + "favorable para el objetivo. Esa columna es la variable que, al crecer desde 0, más mejora Z.</li>"
            + "<li><b>Fila pivote (variable que sale) — prueba de la razón mínima:</b> de las filas con "
            + "coeficiente <i>positivo</i> en la columna pivote, se calcula RHS &divide; coeficiente en cada una "
            + "y se elige la de menor cociente. Así ninguna variable básica se vuelve negativa al aumentar la "
            + "que entra (no se sale de la región factible).</li>"
            + "<li><b>Pivoteo (Gauss-Jordan):</b> se divide toda la fila pivote entre el elemento pivote (para "
            + "dejarlo en 1) y se resta un múltiplo de esa fila a todas las demás — incluida la fila Z — para "
            + "dejar la columna pivote con un 1 en la fila pivote y 0 en el resto.</li>"
            + "<li><b>Se repite</b> desde el paso 1 con la tabla actualizada.</li>"
            + "</ol>"
            + "<h3>¿Cuándo se detiene?</h3>"
            + "<p>Cuando en la fila Z ya no queda ningún coeficiente que indique una mejora posible. En ese "
            + "momento, los valores de las variables básicas leídos en la columna de resultados son la solución "
            + "óptima.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: maximizar Z = 3x<sub>1</sub> + 5x<sub>2</sub></b><br>"
            + "sujeto a: x<sub>1</sub> &le; 4 &nbsp;|&nbsp; 2x<sub>2</sub> &le; 12 &nbsp;|&nbsp; "
            + "3x<sub>1</sub> + 2x<sub>2</sub> &le; 18"
            + "<table " + TABLA + ">"
            + "<tr><th>Iteración</th><th>Entra</th><th>Sale</th><th>Razón mínima</th><th>Solución básica</th><th>Z</th></tr>"
            + "<tr><td>0 (inicial)</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td>"
            + "<td>s<sub>1</sub>=4, s<sub>2</sub>=12, s<sub>3</sub>=18</td><td>0</td></tr>"
            + "<tr><td>1</td><td>x<sub>2</sub></td><td>s<sub>2</sub></td><td>12/2 = 6</td>"
            + "<td>x<sub>2</sub>=6, s<sub>1</sub>=4, s<sub>3</sub>=6</td><td>30</td></tr>"
            + "<tr><td>2</td><td>x<sub>1</sub></td><td>s<sub>3</sub></td><td>6/3 = 2</td>"
            + "<td>x<sub>1</sub>=2, x<sub>2</sub>=6, s<sub>1</sub>=2</td><td><b>36 (óptimo)</b></td></tr>"
            + "</table>"
            + "Ningún coeficiente de la fila Z mejora ya el resultado: x<sub>1</sub>=2, x<sub>2</sub>=6, Z=36 es "
            + "la solución óptima. Geométricamente, el Simplex recorrió los vértices "
            + "(0,0) &rarr; (0,6) &rarr; (2,6) del polígono factible."
            + "</div>"
            + "<div class='nota'>"
            + "<b>Ojo con:</b> si en la prueba de la razón mínima ninguna fila tiene coeficiente positivo en la "
            + "columna pivote, el problema es <b>no acotado</b> (Z puede crecer sin límite). Si dos filas empatan "
            + "en la razón mínima hay <b>degeneración</b>: el algoritmo sigue siendo válido, pero puede tardar "
            + "más iteraciones en converger."
            + "</div>";

    /** Contenido teórico de la ventana de Big M (MetodoBigM). */
    public static final String TEORIA_BIGM =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El Simplex normal necesita una base inicial obvia, y las variables de holgura solo la dan "
            + "cuando <i>todas</i> las restricciones son &le; con RHS &ge; 0. El método Big M extiende el "
            + "Simplex a problemas con restricciones &ge; o = , donde no existe esa base a simple vista.</p>"
            + "<h3>Variables artificiales: una base \"prestada\"</h3>"
            + "<p>Por cada restricción &ge; o = se agrega una <b>variable artificial</b> a<sub>i</sub> &ge; 0, "
            + "sin significado físico en el problema: solo completa una base identidad para poder arrancar la "
            + "tabla. Como no debe aparecer en la solución final, se la castiga en la función objetivo con un "
            + "coeficiente enorme <b>M</b>, de forma que el propio algoritmo la empuje fuera de la base en cuanto "
            + "encuentre una alternativa factible. En una restricción &ge; también se resta una variable de "
            + "<b>exceso</b>: a<sub>i1</sub>x<sub>1</sub>+...&minus;e<sub>i</sub>+a<sub>i</sub>=b<sub>i</sub>; en "
            + "una restricción = solo se agrega la artificial.</p>"
            + "<h3>Aritmética exacta con M</h3>"
            + "<p>En vez de reemplazar M por un número concreto (lo que podría dar resultados incorrectos si M "
            + "no es \"suficientemente grande\"), cada celda de la fila Z se guarda como un par <b>(parte "
            + "constante, coeficiente de M)</b>. Comparar dos celdas es comparar primero su coeficiente de M, y "
            + "solo si empatan, comparar la parte constante — así el resultado no depende de qué tan grande se "
            + "imagine M.</p>"
            + "<h3>El ciclo del algoritmo</h3>"
            + "<ol>"
            + "<li>Se arma la tabla inicial con las artificiales como base, y se \"limpia\" la fila Z restando "
            + "M &times; (cada fila con una artificial básica), para que la fila Z no tenga M en las columnas "
            + "básicas.</li>"
            + "<li><b>Columna pivote:</b> igual que en Simplex, la de coeficiente más favorable en Z — "
            + "comparando primero por el coeficiente de M.</li>"
            + "<li><b>Fila pivote:</b> prueba de la razón mínima, igual que en Simplex normal.</li>"
            + "<li><b>Pivoteo</b> con Gauss-Jordan sobre ambas partes (constante y M) de cada celda.</li>"
            + "<li>Se repite hasta que ninguna columna mejore ya la fila Z.</li>"
            + "</ol>"
            + "<h3>Cómo leer el resultado</h3>"
            + "<p>Si al terminar todas las variables artificiales quedaron en 0, la solución es factible para "
            + "el problema original. Si alguna artificial queda positiva en la solución final, el problema "
            + "original <b>no tiene solución factible</b> (las restricciones son contradictorias entre sí).</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: minimizar Z = 2x<sub>1</sub> + 3x<sub>2</sub></b><br>"
            + "sujeto a: x<sub>1</sub>+x<sub>2</sub> &ge; 4 &nbsp;|&nbsp; x<sub>1</sub>+2x<sub>2</sub> &ge; 6"
            + "<p>Con exceso y artificial: x<sub>1</sub>+x<sub>2</sub>&minus;e<sub>1</sub>+a<sub>1</sub>=4 "
            + "&nbsp;|&nbsp; x<sub>1</sub>+2x<sub>2</sub>&minus;e<sub>2</sub>+a<sub>2</sub>=6</p>"
            + "<table " + TABLA + ">"
            + "<tr><th>Iteración</th><th>Entra</th><th>Sale</th><th>Solución básica</th><th>Z</th></tr>"
            + "<tr><td>0 (inicial)</td><td>&mdash;</td><td>&mdash;</td>"
            + "<td>a<sub>1</sub>=4, a<sub>2</sub>=6</td><td>10M</td></tr>"
            + "<tr><td>1</td><td>x<sub>2</sub></td><td>a<sub>2</sub></td>"
            + "<td>x<sub>2</sub>=3, a<sub>1</sub>=1 (aún queda artificial)</td><td>3M+9</td></tr>"
            + "<tr><td>2</td><td>x<sub>1</sub></td><td>a<sub>1</sub></td>"
            + "<td>x<sub>1</sub>=2, x<sub>2</sub>=2</td><td><b>10 (óptimo, sin artificiales)</b></td></tr>"
            + "</table>"
            + "Ambas artificiales salieron de la base en la iteración 2: x<sub>1</sub>=2, x<sub>2</sub>=2, Z=10 "
            + "es factible y óptima para el problema original."
            + "</div>"
            + "<div class='nota'>"
            + "<b>Ojo con:</b> si el resultado final deja alguna artificial positiva, revisa las restricciones — "
            + "probablemente son incompatibles entre sí y el problema no tiene solución."
            + "</div>";

    /** Contenido teórico de la ventana de Simplex Dual (AlgoritmoDual). */
    public static final String TEORIA_DUAL =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El Simplex Dual arranca de una tabla que ya es <b>óptima</b> según la fila Z (ningún cambio "
            + "de variable mejoraría el objetivo) pero que <b>no es factible</b> porque algún lado derecho "
            + "(RHS) quedó negativo — típicamente porque una restricción &ge; se escribió multiplicada por "
            + "&minus;1 como &le; (por ejemplo, x<sub>1</sub>+x<sub>2</sub> &ge; 4 se reescribe como "
            + "&minus;x<sub>1</sub>&minus;x<sub>2</sub> &le; &minus;4). Vertex hace esa conversión solo: acepta restricciones &le; y &ge; "
            + "(las &ge; las multiplica por &minus;1). Si hay =, se necesitan artificiales y corresponde usar Big M.</p>"
            + "<h3>La idea: Simplex \"al revés\"</h3>"
            + "<p>El Simplex normal corrige primero la <i>factibilidad</i> (con holguras) y avanza hacia la "
            + "<i>optimalidad</i> pivoteando por columnas. El método Dual hace lo opuesto: parte ya óptimo y "
            + "pivotea primero por <b>filas</b>, corrigiendo un RHS negativo en cada iteración, sin perder "
            + "nunca la condición de óptimo de la fila Z. Cuando ya no queda ningún RHS negativo, la tabla es "
            + "óptima y factible a la vez.</p>"
            + "<h3>El ciclo del algoritmo</h3>"
            + "<ol>"
            + "<li><b>Fila pivote (variable que sale):</b> se elige la fila con el RHS más negativo — la "
            + "restricción \"más violada\".</li>"
            + "<li><b>Columna pivote (variable que entra) — razón mínima dual:</b> solo se consideran las "
            + "columnas con coeficiente <i>negativo</i> en esa fila (las únicas capaces de subir ese RHS al "
            + "pivotear). Para cada una se calcula |coeficiente de la fila Z &divide; coeficiente de la fila "
            + "pivote|, y se elige la <b>menor</b> razón, para que la fila Z se mantenga sin volverse negativa.</li>"
            + "<li><b>Pivoteo</b> con Gauss-Jordan, igual que en el Simplex normal.</li>"
            + "<li>Se repite desde el paso 1 mientras exista algún RHS negativo.</li>"
            + "</ol>"
            + "<h3>¿Por qué nunca se pierde la optimalidad?</h3>"
            + "<p>La razón mínima del paso 2 se elige justamente para que, tras el pivoteo, todos los "
            + "coeficientes de la fila Z sigan siendo &ge; 0. Por eso el método puede concentrarse solo en "
            + "arreglar la factibilidad: la optimalidad queda garantizada en cada paso por construcción.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: el mismo problema del método Big M, visto como &le;</b><br>"
            + "maximizar Z = &minus;2x<sub>1</sub>&minus;3x<sub>2</sub> (equivale a minimizar "
            + "2x<sub>1</sub>+3x<sub>2</sub>) sujeto a: &minus;x<sub>1</sub>&minus;x<sub>2</sub> &le; &minus;4 "
            + "&nbsp;|&nbsp; &minus;x<sub>1</sub>&minus;2x<sub>2</sub> &le; &minus;6"
            + "<table " + TABLA + ">"
            + "<tr><th>Iteración</th><th>Sale (RHS más negativo)</th><th>Entra (razón mínima)</th>"
            + "<th>Solución básica</th><th>Z</th></tr>"
            + "<tr><td>0 (inicial)</td><td>&mdash;</td><td>&mdash;</td>"
            + "<td>s<sub>1</sub>=&minus;4, s<sub>2</sub>=&minus;6</td><td>0</td></tr>"
            + "<tr><td>1</td><td>s<sub>2</sub> (&minus;6)</td><td>x<sub>2</sub> (razón 1.5)</td>"
            + "<td>x<sub>2</sub>=3, s<sub>1</sub>=&minus;1</td><td>&minus;9</td></tr>"
            + "<tr><td>2</td><td>s<sub>1</sub> (&minus;1)</td><td>x<sub>1</sub> (razón 1)</td>"
            + "<td>x<sub>1</sub>=2, x<sub>2</sub>=2</td><td><b>&minus;10 (óptimo, RHS &ge; 0)</b></td></tr>"
            + "</table>"
            + "Z=&minus;10 equivale a un mínimo real de 10 en x<sub>1</sub>=2, x<sub>2</sub>=2: el mismo punto "
            + "que encuentra Big M para este problema, llegando por un camino completamente distinto."
            + "</div>";

    /** Contenido teórico de la ventana de Costos Duales / MODI (AlgoritmoCostosDuales). */
    public static final String TEORIA_COSTOS_DUALES =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El problema de transporte busca la forma más barata de enviar unidades desde varios "
            + "<b>orígenes</b> (con oferta limitada) hasta varios <b>destinos</b> (con demanda fija), conociendo "
            + "el costo de enviar una unidad por cada ruta origen-destino. Es un caso particular de programación "
            + "lineal, pero con una estructura especial que permite resolverlo sin armar un tableau completo.</p>"
            + "<h3>Paso 1 — solución inicial con Vogel (VAM)</h3>"
            + "<p>Para cada fila y columna se calcula una <b>penalización</b>: la diferencia entre el costo más "
            + "barato y el segundo más barato disponible. Se atiende primero la fila o columna con mayor "
            + "penalización (la que más \"duele\" no aprovechar), asignando la mayor cantidad posible a su celda "
            + "más barata. Se repite, tachando filas/columnas ya satisfechas, hasta asignar toda la oferta y "
            + "demanda. VAM no siempre da el óptimo exacto, pero suele acercarse mucho — por eso el paso "
            + "siguiente lo verifica.</p>"
            + "<h3>Paso 2 — costos duales u<sub>i</sub>, v<sub>j</sub> (MODI)</h3>"
            + "<p>Para las celdas <b>básicas</b> (las que recibieron asignación) se resuelve el sistema "
            + "u<sub>i</sub> + v<sub>j</sub> = costo<sub>ij</sub>, fijando arbitrariamente u<sub>1</sub>=0 como "
            + "referencia. Con m+n&minus;1 celdas básicas hay exactamente m+n&minus;1 ecuaciones para las m+n "
            + "incógnitas u<sub>i</sub>, v<sub>j</sub>.</p>"
            + "<h3>Paso 3 — costo de oportunidad</h3>"
            + "<p>Para cada celda <b>no básica</b> se calcula "
            + "costo de oportunidad = costo<sub>ij</sub> &minus; u<sub>i</sub> &minus; v<sub>j</sub>. Representa "
            + "cuánto <i>aumentaría</i> el costo total por cada unidad que se fuerce a pasar por esa ruta. Si "
            + "todos son &ge; 0, ninguna ruta no usada puede mejorar el costo total: la solución ya es "
            + "<b>óptima</b>.</p>"
            + "<h3>Paso 4 — si no es óptima: el ciclo cerrado (stepping-stone)</h3>"
            + "<p>Si algún costo de oportunidad es negativo, entra a la base la celda más negativa. Se traza su "
            + "<b>ciclo cerrado</b> pasando solo por celdas básicas (alternando movimientos horizontales y "
            + "verticales), marcando las celdas alternadamente con + y &minus;. Se mueve la cantidad "
            + "&theta; = mínimo valor entre las celdas marcadas con &minus; (para no dejar ninguna negativa), "
            + "sumándola en las celdas \"+\" y restándola en las \"&minus;\". La celda \"&minus;\" que llega a "
            + "0 sale de la base, y se repite desde el paso 2.</p>"
            + "<h3>Balanceo</h3>"
            + "<p>Si la oferta total no coincide con la demanda total, se agrega automáticamente una fila u "
            + "columna ficticia con costo 0, para dejar el problema balanceado antes de aplicar VAM.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: 2 plantas, 3 almacenes</b><br>"
            + "Oferta: P<sub>1</sub>=30, P<sub>2</sub>=20 &nbsp;|&nbsp; Demanda: W<sub>1</sub>=10, W<sub>2</sub>=25, "
            + "W<sub>3</sub>=15"
            + "<table " + TABLA + ">"
            + "<tr><th>Costo / asignación</th><th>W<sub>1</sub></th><th>W<sub>2</sub></th><th>W<sub>3</sub></th>"
            + "<th>Oferta</th></tr>"
            + "<tr><td>P<sub>1</sub></td><td>8 &nbsp;(5)</td><td>6 &nbsp;<b>(25)</b></td><td>10</td><td>30</td></tr>"
            + "<tr><td>P<sub>2</sub></td><td>9 &nbsp;(5)</td><td>12</td><td>7 &nbsp;<b>(15)</b></td><td>20</td></tr>"
            + "<tr><td>Demanda</td><td>10</td><td>25</td><td>15</td><td></td></tr>"
            + "</table>"
            + "VAM asigna: (P<sub>1</sub>,W<sub>2</sub>)=25, (P<sub>2</sub>,W<sub>3</sub>)=15, "
            + "(P<sub>1</sub>,W<sub>1</sub>)=5, (P<sub>2</sub>,W<sub>1</sub>)=5. "
            + "Costo total = 25(6)+5(8)+15(7)+5(9) = <b>340</b>.<br><br>"
            + "Con u<sub>1</sub>=0: de (P<sub>1</sub>,W<sub>1</sub>)=8 &rarr; v<sub>1</sub>=8; de "
            + "(P<sub>1</sub>,W<sub>2</sub>)=6 &rarr; v<sub>2</sub>=6; de (P<sub>2</sub>,W<sub>1</sub>)=9 &rarr; "
            + "u<sub>2</sub>=1; de (P<sub>2</sub>,W<sub>3</sub>)=7 &rarr; v<sub>3</sub>=6.<br>"
            + "Costos de oportunidad de las celdas libres: (P<sub>1</sub>,W<sub>3</sub>) = 10&minus;0&minus;6 = "
            + "4 &ge; 0; (P<sub>2</sub>,W<sub>2</sub>) = 12&minus;1&minus;6 = 5 &ge; 0. Ambos no negativos: "
            + "<b>la solución de VAM ya era óptima</b>, sin necesitar ningún ciclo cerrado."
            + "</div>";

    /** Contenido teórico de la ventana de Método Gráfico (MetodoGrafico). */
    public static final String TEORIA_GRAFICO =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El método gráfico resuelve, de forma visual y exacta, problemas de programación lineal con "
            + "<b>exactamente 2 variables</b> de decisión (x<sub>1</sub>, x<sub>2</sub>) — perfecto para ver "
            + "\"con los ojos\" lo que el Simplex hace algebraicamente en más dimensiones.</p>"
            + "<h3>Restricciones como semiplanos</h3>"
            + "<p>Cada restricción lineal es una recta que divide el plano en dos mitades: el lado permitido y "
            + "el prohibido. Por ejemplo, x<sub>1</sub>+x<sub>2</sub> &ge; 4 permite todo punto sobre la recta "
            + "x<sub>1</sub>+x<sub>2</sub>=4 o \"por arriba\" de ella. La intersección de todos los semiplanos "
            + "permitidos (más x<sub>1</sub>, x<sub>2</sub> &ge; 0) forma la <b>región factible</b>: un polígono "
            + "convexo (o una región convexa no acotada).</p>"
            + "<h3>El teorema que hace posible el método</h3>"
            + "<p>Si una función lineal tiene óptimo sobre una región convexa, ese óptimo siempre se alcanza en "
            + "uno de los <b>vértices</b> del polígono, nunca estrictamente adentro. Por eso no hace falta "
            + "revisar todo el interior de la región: basta con identificar los vértices y comparar Z en cada "
            + "uno.</p>"
            + "<h3>Procedimiento</h3>"
            + "<ol>"
            + "<li>Dibujar cada restricción como recta (dando valores a x<sub>1</sub>=0 y x<sub>2</sub>=0 para "
            + "encontrar sus intersecciones con los ejes).</li>"
            + "<li>Sombrear el lado permitido de cada una y quedarse con la intersección de todos los lados: "
            + "esa es la región factible.</li>"
            + "<li>Calcular las coordenadas exactas de cada vértice, resolviendo el sistema de las dos rectas "
            + "que se cruzan ahí.</li>"
            + "<li>Evaluar Z en cada vértice y elegir el mejor (mayor si es maximizar, menor si es minimizar).</li>"
            + "</ol>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: minimizar Z = 2x<sub>1</sub> + 3x<sub>2</sub></b><br>"
            + "sujeto a: x<sub>1</sub>+x<sub>2</sub> &ge; 4 &nbsp;|&nbsp; x<sub>1</sub>+2x<sub>2</sub> &ge; 6"
            + "<table " + TABLA + ">"
            + "<tr><th>Vértice</th><th>Cómo se obtiene</th><th>Z = 2x<sub>1</sub>+3x<sub>2</sub></th></tr>"
            + "<tr><td>(0, 4)</td><td>eje x<sub>2</sub> &cap; x<sub>1</sub>+x<sub>2</sub>=4</td><td>12</td></tr>"
            + "<tr><td>(2, 2)</td><td>intersección de las dos rectas</td><td><b>10 (óptimo)</b></td></tr>"
            + "<tr><td>(6, 0)</td><td>eje x<sub>1</sub> &cap; x<sub>1</sub>+2x<sub>2</sub>=6</td><td>12</td></tr>"
            + "</table>"
            + "El vértice (2,2) da el menor valor de Z: es el mismo problema, y la misma respuesta, que "
            + "obtienen Big M y el Simplex Dual en sus propias ventanas de ayuda, resolviéndolo por caminos "
            + "distintos."
            + "</div>"
            + "<div class='nota'>"
            + "<b>Ojo con:</b> si la región factible es no acotada en la dirección en que crece (o decrece) la "
            + "función objetivo, el problema es <b>no acotado</b> y no tiene óptimo finito."
            + "</div>";

    /** Contenido teórico de la ventana del Algoritmo Húngaro (AlgoritmoHungaro). */
    public static final String TEORIA_HUNGARO =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El problema de <b>asignación</b>: repartir n tareas entre n agentes, una tarea por agente y "
            + "un agente por tarea, minimizando el costo total (o maximizando la ganancia). Es un caso "
            + "particular de transporte donde toda oferta y demanda vale 1, pero el algoritmo húngaro lo "
            + "resuelve de forma mucho más directa que MODI, trabajando siempre sobre la matriz de costos.</p>"
            + "<h3>La idea clave: restar no cambia quién es el mejor</h3>"
            + "<p>Si a toda una fila (o columna) se le resta la misma constante, el costo de <i>cualquier</i> "
            + "asignación cambia siempre en esa misma constante — así que la asignación que era la más barata "
            + "sigue siendo la más barata. El algoritmo aprovecha esto para ir generando ceros hasta que sea "
            + "trivial leer una asignación de costo 0 (el mínimo posible después de las restas).</p>"
            + "<h3>El ciclo del algoritmo</h3>"
            + "<ol>"
            + "<li><b>Reducción de filas:</b> a cada fila se le resta su valor mínimo.</li>"
            + "<li><b>Reducción de columnas:</b> a cada columna (de la matriz ya reducida) se le resta su "
            + "valor mínimo.</li>"
            + "<li><b>Cubrir los ceros:</b> se traza el menor número posible de líneas horizontales/verticales "
            + "que tapen todos los ceros.</li>"
            + "<li><b>¿Alcanza con n líneas?</b> Si el número de líneas iguala el tamaño de la matriz (n), ya "
            + "existe una asignación óptima usando esos ceros — ir al paso 6.</li>"
            + "<li><b>Si faltan líneas:</b> se busca el menor valor no cubierto por ninguna línea. Se resta de "
            + "todas las celdas no cubiertas, y se suma a las celdas cubiertas <i>dos veces</i> (donde se cruzan "
            + "una línea horizontal y una vertical). Se vuelve al paso 3.</li>"
            + "<li><b>Leer la asignación:</b> se elige un cero por fila y por columna (sin repetir fila ni "
            + "columna) hasta asignar las n tareas.</li>"
            + "</ol>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: 3 trabajadores, 3 tareas</b>"
            + "<table " + TABLA + ">"
            + "<tr><th>Costo</th><th>J1</th><th>J2</th><th>J3</th></tr>"
            + "<tr><td>W1</td><td>9</td><td>11</td><td>14</td></tr>"
            + "<tr><td>W2</td><td>6</td><td>15</td><td>13</td></tr>"
            + "<tr><td>W3</td><td>12</td><td>9</td><td>12</td></tr>"
            + "</table>"
            + "Tras reducir filas y columnas quedan ceros en (W1,J1), (W2,J1), (W3,J2) y (W3,J3), cubribles con "
            + "solo 2 líneas (columna J1 y fila W3). Como 2 &lt; 3, se resta el menor valor no cubierto (2) de "
            + "las celdas libres y se suma 2 a la intersección (W3,J1). Ahora los ceros se cubren con 3 líneas y "
            + "aparece una asignación completa:"
            + "<table " + TABLA + ">"
            + "<tr><th>Asignación</th><th>Costo real</th></tr>"
            + "<tr><td>W1 &rarr; J3</td><td>14</td></tr>"
            + "<tr><td>W2 &rarr; J1</td><td>6</td></tr>"
            + "<tr><td>W3 &rarr; J2</td><td>9</td></tr>"
            + "<tr><td><b>Total</b></td><td><b>29 (óptimo)</b></td></tr>"
            + "</table>"
            + "(La asignación W1&rarr;J2, W2&rarr;J1, W3&rarr;J3 también suma 29: hay más de un óptimo, algo "
            + "frecuente cuando quedan varios ceros disponibles al final.)"
            + "</div>";

    /** Contenido teórico de la ventana de Método Numérico (MetodoNumerico). */
    public static final String TEORIA_NUMERICO =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>Encuentra el óptimo de un problema de programación lineal por <b>fuerza bruta</b>: en vez de "
            + "moverse paso a paso mejorando el objetivo (como el Simplex), genera y evalúa <i>todas</i> las "
            + "soluciones básicas posibles y se queda con la mejor que sea factible. Es más lento que el "
            + "Simplex, pero sirve para comprobar el resultado y para ver \"todo el mapa\" de posibilidades a "
            + "la vez.</p>"
            + "<h3>Forma estándar y soluciones básicas</h3>"
            + "<p>Igual que en Simplex/Big M, el problema se lleva a forma estándar con n variables en total "
            + "(originales + holguras/excesos) y m restricciones (ecuaciones). Una <b>solución básica</b> se "
            + "obtiene eligiendo m de esas n variables para que sean \"básicas\" (se resuelven exactamente con "
            + "las m ecuaciones) y fijando las n&minus;m restantes en 0. Hay "
            + "C(n, m) = n! / (m! (n&minus;m)!) formas de elegir cuáles son básicas, así que ese es el número "
            + "de sistemas que se resuelven.</p>"
            + "<h3>El ciclo del algoritmo</h3>"
            + "<ol>"
            + "<li>Se genera cada combinación posible de m variables básicas.</li>"
            + "<li>Se resuelve el sistema cuadrado m&times;m resultante con <b>eliminación de Gauss</b> (si el "
            + "sistema es singular, esa combinación no da una solución válida y se descarta).</li>"
            + "<li>Se revisa si todas las variables de la solución son &ge; 0: si sí, es <b>factible</b>; si "
            + "no, se descarta.</li>"
            + "<li>Entre todas las soluciones factibles, la que da el mejor valor de Z es el <b>óptimo</b> — y "
            + "es exactamente el mismo vértice al que habría llegado el Simplex, solo que aquí se calculan "
            + "todos los vértices de una vez en lugar de visitarlos en orden.</li>"
            + "</ol>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: el mismo problema del método Simplex</b><br>"
            + "maximizar Z = 3x<sub>1</sub> + 5x<sub>2</sub> sujeto a x<sub>1</sub>&le;4, 2x<sub>2</sub>&le;12, "
            + "3x<sub>1</sub>+2x<sub>2</sub>&le;18 &nbsp;(n=5 variables con holguras, m=3 restricciones "
            + "&rarr; C(5,3) = 10 combinaciones posibles)"
            + "<table " + TABLA + ">"
            + "<tr><th>Variables básicas</th><th>Solución</th><th>¿Factible?</th><th>Z</th></tr>"
            + "<tr><td>x<sub>1</sub>, x<sub>2</sub>, s<sub>1</sub></td>"
            + "<td>x<sub>1</sub>=2, x<sub>2</sub>=6, s<sub>1</sub>=2</td><td>Sí</td><td><b>36 (óptimo)</b></td></tr>"
            + "<tr><td>x<sub>1</sub>, x<sub>2</sub>, s<sub>2</sub></td>"
            + "<td>x<sub>1</sub>=4, x<sub>2</sub>=3, s<sub>2</sub>=6</td><td>Sí</td><td>27</td></tr>"
            + "<tr><td>x<sub>1</sub>, x<sub>2</sub>, s<sub>3</sub></td>"
            + "<td>x<sub>1</sub>=4, x<sub>2</sub>=6, s<sub>3</sub>=&minus;6</td><td>No</td><td>&mdash;</td></tr>"
            + "<tr><td>x<sub>1</sub>, s<sub>1</sub>, s<sub>2</sub></td>"
            + "<td>x<sub>1</sub>=6, s<sub>1</sub>=&minus;2</td><td>No</td><td>&mdash;</td></tr>"
            + "<tr><td>x<sub>1</sub>, s<sub>2</sub>, s<sub>3</sub></td>"
            + "<td>x<sub>1</sub>=4, s<sub>2</sub>=12, s<sub>3</sub>=6</td><td>Sí</td><td>12</td></tr>"
            + "<tr><td>x<sub>2</sub>, s<sub>1</sub>, s<sub>3</sub></td>"
            + "<td>x<sub>2</sub>=6, s<sub>1</sub>=4, s<sub>3</sub>=6</td><td>Sí</td><td>30</td></tr>"
            + "<tr><td>s<sub>1</sub>, s<sub>2</sub>, s<sub>3</sub></td>"
            + "<td>x<sub>1</sub>=x<sub>2</sub>=0 (origen)</td><td>Sí</td><td>0</td></tr>"
            + "<tr><td colspan='4'>(las 3 combinaciones restantes dan sistemas inconsistentes: no aportan "
            + "puntos nuevos)</td></tr>"
            + "</table>"
            + "La mejor entre todas las factibles es x<sub>1</sub>=2, x<sub>2</sub>=6 con Z=36: el mismo óptimo "
            + "que encuentra el método Simplex para este problema."
            + "</div>";
}
