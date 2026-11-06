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

    // colores: ver Tema (se leen al abrir cada ayuda, así siguen el tema actual)

    public VentanaAyudaAlgoritmo(java.awt.Window owner, String tituloAlgoritmo, String colorimetriaHtml, String teoriaHtml) {
        super(owner, "Ayuda — " + tituloAlgoritmo, ModalityType.MODELESS);
        initComponents(tituloAlgoritmo, colorimetriaHtml, teoriaHtml);
        Recursos.aplicarIcono(this);
        setLocationRelativeTo(owner);
    }

    private void initComponents(String tituloAlgoritmo, String colorimetriaHtml, String teoriaHtml) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.tarjeta());

        getContentPane().add(construirEncabezado(tituloAlgoritmo), BorderLayout.NORTH);
        getContentPane().add(construirCuerpo(colorimetriaHtml, teoriaHtml), BorderLayout.CENTER);
        getContentPane().add(construirPie(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(520, 460));
        setSize(760, 720);
    }

    private JPanel construirEncabezado(String tituloAlgoritmo) {
        JPanel encabezado = new JPanel(new GridBagLayout());
        encabezado.setBackground(Tema.acento());
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
        editor.setBackground(Tema.tarjeta());
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
        pie.setBackground(Tema.tarjeta());
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.borde()),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        pie.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(Tema.acento());
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
        String tintaHex = Tema.hex(Tema.tinta());
        String acentoHex = Tema.hex(Tema.acentoTexto());
        boolean osc = Tema.esOscuro();

        // colorimetriaHtml llega envuelto en <html>...</html> (se reutiliza también como
        // tooltip del botón "(?)"), y con un <b>título</b><br> inicial que ya no hace falta
        // repetir acá porque esta ventana tiene su propio encabezado con el título.
        // sin colorimetría (p. ej. la ayuda de sensibilidad, que no tiene tablero) no va ese recuadro
        String colorimetriaFragmento = colorimetriaHtml == null ? null : colorimetriaHtml
                .replaceAll("(?i)</?html>", "")
                .replaceAll("(?i)<b>.*?</b><br>", "");

        String estilos = "<style>"
                + "body{font-family:'Segoe UI',sans-serif;font-size:15px;line-height:150%;color:" + tintaHex + ";}"
                + "h3{font-size:17px;color:" + acentoHex + ";margin-top:22px;margin-bottom:6px;}"
                + "p{margin-top:6px;margin-bottom:6px;}"
                + "ol{margin-top:6px;}"
                + "li{margin-bottom:9px;}"
                + "table{margin-top:12px;margin-bottom:8px;}"
                + "th{background-color:" + (osc ? "#34325a" : "#eeecfb") + ";color:" + acentoHex + ";text-align:left;padding:5px 12px;}"
                + "td{padding:5px 12px;}"
                + ".ejemplo{margin-top:14px;margin-bottom:8px;padding:12px 16px;background-color:" + (osc ? "#33353c" : "#f4f6fb") + ";border-left:4px solid " + acentoHex + ";}"
                + ".nota{margin-top:14px;margin-bottom:8px;padding:10px 16px;background-color:" + (osc ? "#3d3424" : "#fff7e0") + ";border-left:4px solid #d9a441;}"
                + ".colorimetria{margin-top:22px;padding:14px 16px;background-color:" + (osc ? "#302e52" : "#f6f5ff") + ";border-left:4px solid " + acentoHex + ";}"
                + "</style>";

        return "<html><head>" + estilos + "</head><body>"
                + teoriaHtml
                + (colorimetriaFragmento == null ? "" : "<div class='colorimetria'>"
                + "<b style='color:" + acentoHex + ";font-size:16px;'>Colorimetría de esta ventana</b><br><br>"
                + colorimetriaFragmento
                + "</div>")
                + "</body></html>";
    }

    private static final String TABLA = "border=\"1\" cellpadding=\"6\" cellspacing=\"0\"";

    /** Ayuda (?) de la ventana principal: cómo formular un problema y escribirlo en Vertex. */
    public static final String TEORIA_FORMULACION =
            "<h3>¿Qué es la programación lineal?</h3>"
            + "<p>Es una rama de las ciencias exactas que se aplica a problemas cotidianos y entrega la "
            + "<b>solución óptima</b>. Sus orígenes se remontan a la Segunda Guerra Mundial, cuando George "
            + "Dantzig la desarrolló para optimizar los recursos. Para plantear un problema de programación "
            + "lineal (PPL) la herramienta más poderosa es el <b>pensamiento y la imaginación</b>: entender el "
            + "enunciado antes de escribir números.</p>"
            + "<h3>Primero: definir las variables</h3>"
            + "<p>Cada variable de decisión x<sub>j</sub> es un número real que el problema debe decidir. Al "
            + "definirla se dice qué es, en qué unidades se mide y cómo se interpreta:</p>"
            + "<table " + TABLA + ">"
            + "<tr><th>Variable</th><th>Tipo</th><th>Descripción</th></tr>"
            + "<tr><td>x<sub>1</sub></td><td>mesas / día</td><td>Cantidad de mesas que se fabrican por día</td></tr>"
            + "<tr><td>x<sub>2</sub></td><td>sillas / día</td><td>Cantidad de sillas que se fabrican por día</td></tr>"
            + "</table>"
            + "<p>Y el <b>objetivo</b>: se <b>maximiza</b> lo deseado (utilidad, producción) y se <b>minimiza</b> "
            + "lo no deseado (costo, tiempo, desperdicio).</p>"
            + "<h3>Las 3 partes de la formulación</h3>"
            + "<p>El modelo matemático de un PPL tiene tres partes: la <b>función objetivo</b> (F.O.), las "
            + "<b>restricciones</b> o limitaciones y la <b>condición de no negatividad</b>. Cada una se puede "
            + "escribir de forma matemática (1, 2, 3) o matricial (1', 2', 3').</p>"
            + "<h3>1. Función objetivo</h3>"
            + "<p>Optimizar una función lineal f(x) = Z, sujeta a un conjunto finito, independiente y homogéneo de "
            + "restricciones, basadas en un conjunto de variables reales que deben cumplir la condición de no "
            + "negatividad. Max &rarr; Z<sub>Max</sub>; Min &rarr; Z<sub>Min</sub>.</p>"
            + "<table " + TABLA + ">"
            + "<tr><th>Forma</th><th>Escritura</th></tr>"
            + "<tr><td>Matemática (1)</td><td>Z = &plusmn;c<sub>1</sub>x<sub>1</sub> &plusmn; c<sub>2</sub>x<sub>2</sub> "
            + "&plusmn; ... &plusmn; c<sub>n</sub>x<sub>n</sub></td></tr>"
            + "<tr><td>Matricial (1')</td><td>Z = CX</td></tr>"
            + "</table>"
            + "<p><b>C</b> = (&plusmn;c<sub>1</sub>, &plusmn;c<sub>2</sub>, ..., &plusmn;c<sub>n</sub>): coeficientes "
            + "reales y constantes (el valor por unidad de cada variable). <b>X</b> = (x<sub>1</sub>, x<sub>2</sub>, "
            + "..., x<sub>n</sub>)<sup>T</sup>: vector de variables reales, cada una con su sentido, significado e "
            + "interpretación.</p>"
            + "<h3>2. Restricciones</h3>"
            + "<p>Son condiciones impuestas al problema; buscan satisfacer las necesidades del mismo. Deben ser:</p>"
            + "<ul>"
            + "<li><b>Finitas:</b> se pueden numerar.</li>"
            + "<li><b>Independientes:</b> cada una tiene carácter individual.</li>"
            + "<li><b>Homogéneas:</b> en cada una todo está en la misma unidad (conversión unitaria).</li>"
            + "</ul>"
            + "<table " + TABLA + ">"
            + "<tr><th>Forma</th><th>Escritura</th></tr>"
            + "<tr><td>Matemática (2), con inecuaciones</td><td>"
            + "&plusmn;a<sub>11</sub>x<sub>1</sub> &plusmn; a<sub>12</sub>x<sub>2</sub> &plusmn; ... &plusmn; "
            + "a<sub>1n</sub>x<sub>n</sub> &le;&ge; b<sub>1</sub><br>"
            + "&plusmn;a<sub>21</sub>x<sub>1</sub> &plusmn; a<sub>22</sub>x<sub>2</sub> &plusmn; ... &plusmn; "
            + "a<sub>2n</sub>x<sub>n</sub> &le;&ge; b<sub>2</sub><br>...<br>"
            + "&plusmn;a<sub>m1</sub>x<sub>1</sub> &plusmn; a<sub>m2</sub>x<sub>2</sub> &plusmn; ... &plusmn; "
            + "a<sub>mn</sub>x<sub>n</sub> &le;&ge; b<sub>m</sub></td></tr>"
            + "<tr><td>Matricial (2')</td><td>AX &le;&ge; B</td></tr>"
            + "</table>"
            + "<p><b>A</b> = (a<sub>ij</sub>), de m &times; n: la <b>matriz de coeficientes tecnológicos</b>. Cada "
            + "a<sub>ij</sub> responde: ¿cuánto del recurso i gasta (o aporta) una unidad de x<sub>j</sub>? Por eso "
            + "la fila i es la restricción i y la columna j es lo que consume x<sub>j</sub> en todos los recursos. "
            + "<b>B</b> = (b<sub>1</sub>, b<sub>2</sub>, ..., b<sub>m</sub>)<sup>T</sup>: el vector de "
            + "<b>términos independientes</b> (disponibilidad o requerimiento de cada recurso). Los tipos de "
            + "restricción son &le; (un recurso disponible), &ge; (un mínimo que cumplir) y = (un valor exacto).</p>"
            + "<h3>3. Condición de no negatividad</h3>"
            + "<p>Toda variable en PL debe ser &ge; 0.</p>"
            + "<table " + TABLA + ">"
            + "<tr><th>Forma</th><th>Escritura</th></tr>"
            + "<tr><td>Matemática (3)</td><td>x<sub>i</sub> &ge; 0 &nbsp;&forall; i = 1, ..., n</td></tr>"
            + "<tr><td>Matricial (3')</td><td>X &ge; 0</td></tr>"
            + "</table>"
            + "<h3>El modelo completo</h3>"
            + "<table " + TABLA + ">"
            + "<tr><th>Matemático (con 1, 2, 3)</th><th>Matricial (con 1', 2', 3')</th></tr>"
            + "<tr><td>Max/Min Z = &plusmn;c<sub>1</sub>x<sub>1</sub> &plusmn; c<sub>2</sub>x<sub>2</sub> &plusmn; ... "
            + "&plusmn; c<sub>n</sub>x<sub>n</sub><br>s.a.<br>"
            + "&plusmn;a<sub>11</sub>x<sub>1</sub> &plusmn; ... &plusmn; a<sub>1n</sub>x<sub>n</sub> &le;&ge; b<sub>1</sub><br>"
            + "...<br>"
            + "&plusmn;a<sub>m1</sub>x<sub>1</sub> &plusmn; ... &plusmn; a<sub>mn</sub>x<sub>n</sub> &le;&ge; b<sub>m</sub><br>"
            + "x<sub>i</sub> &ge; 0 &nbsp;&forall; i = 1, ..., n</td>"
            + "<td>Max/Min Z = CX<br>s.a.<br>AX &le;&ge; B<br>X &ge; 0</td></tr>"
            + "</table>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo: la carpintería</b><br>"
            + "Cada mesa deja 3 dólares y cada silla 5. Se pueden fabricar como mucho 4 mesas por día. Cada silla "
            + "requiere 2 horas de pintura y hay 12 horas. Cada mesa usa 3 tablas y cada silla 2, y hay 18 tablas."
            + "<ul>"
            + "<li><b>Variables:</b> x<sub>1</sub> = mesas por día, x<sub>2</sub> = sillas por día.</li>"
            + "<li><b>Objetivo:</b> la utilidad es deseada, así que se maximiza.</li>"
            + "<li><b>Matemático:</b> Max Z = 3x<sub>1</sub> + 5x<sub>2</sub> &nbsp;s.a.&nbsp; x<sub>1</sub> &le; 4 "
            + "(mesas) &nbsp;|&nbsp; 2x<sub>2</sub> &le; 12 (horas de pintura) &nbsp;|&nbsp; 3x<sub>1</sub> + "
            + "2x<sub>2</sub> &le; 18 (tablas) &nbsp;|&nbsp; x<sub>1</sub>, x<sub>2</sub> &ge; 0.</li>"
            + "<li><b>Matricial:</b> C = (3, 5), X = (x<sub>1</sub>, x<sub>2</sub>)<sup>T</sup>, "
            + "A = [[1, 0], [0, 2], [3, 2]], B = (4, 12, 18)<sup>T</sup>: Max Z = CX, AX &le; B, X &ge; 0.</li>"
            + "</ul>"
            + "Cada restricción es homogénea: la de pintura está toda en horas, la de tablas toda en tablas. La "
            + "solución óptima es x<sub>1</sub> = 2, x<sub>2</sub> = 6, Z = 36."
            + "</div>"
            + "<h3>Cómo escribirlo en Vertex</h3>"
            + "<ul>"
            + "<li><b>Z:</b> elige Max o Min en el selector y escribe la función con coeficiente, x y subíndice: "
            + "<b>3x1 + 5x2</b>. No hace falta escribir \"Z =\".</li>"
            + "<li><b>Sujeto a:</b> una restricción por línea (Enter para otra), con un solo operador "
            + "<b>&lt;=</b>, <b>&gt;=</b> o <b>=</b> y un número a la derecha: <b>3x1 + 2x2 &lt;= 18</b>.</li>"
            + "<li>La no negatividad (x<sub>j</sub> &ge; 0) ya está incluida: no hace falta escribirla.</li>"
            + "<li>Las variables pueden llamarse como quieras (x1, x10, x23...). Para asignación y transporte se "
            + "usa doble índice x<sub>ij</sub>: x<sub>11</sub>, x<sub>12</sub>...</li>"
            + "<li>¿Tienes el enunciado en texto? La pestaña <b>Texto libre (IA)</b> puede formularlo por ti; "
            + "revisa siempre el modelo antes de calcular.</li>"
            + "</ul>";

    /** Contenido teórico de la ventana de Simplex (MetodoSimplex). */
    public static final String TEORIA_SIMPLEX =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>Maximizar o minimizar Z = c<sub>1</sub>x<sub>1</sub> + ... + c<sub>n</sub>x<sub>n</sub> sujeto a "
            + "restricciones &le; con lado derecho &ge; 0 y x<sub>j</sub> &ge; 0. Solo se usa en un PPL "
            + "<b>estandarizado</b>: cada restricción &le; se vuelve igualdad sumando su <b>holgura</b> "
            + "s<sub>i</sub> &ge; 0 (el recurso que sobra; si vale 0, se agotó), que entra a la F.O. con "
            + "coeficiente 0.</p>"
            + "<h3>Símbolos del tablero</h3>"
            + "<table " + TABLA + ">"
            + "<tr><th>Símbolo</th><th>Qué es</th></tr>"
            + "<tr><td>C<sub>j</sub></td><td>Coeficiente de cada variable en la F.O. (fila de arriba).</td></tr>"
            + "<tr><td>X<sub>b</sub></td><td>Variables básicas: una por fila.</td></tr>"
            + "<tr><td>C<sub>b</sub></td><td>El C<sub>j</sub> de cada variable básica.</td></tr>"
            + "<tr><td>a<sub>ij</sub></td><td>Coeficiente tecnológico: cuánto del recurso i gasta una unidad de "
            + "x<sub>j</sub> (cuerpo del tablero).</td></tr>"
            + "<tr><td>Sol</td><td>Valor de cada variable básica (al inicio, el lado derecho b<sub>i</sub>).</td></tr>"
            + "<tr><td>Z<sub>j</sub></td><td>&Sigma; C<sub>b</sub>&middot;a<sub>ij</sub> de cada columna; en la "
            + "columna Sol es el valor de Z.</td></tr>"
            + "<tr><td>P<sub>j</sub></td><td>C<sub>j</sub> &minus; Z<sub>j</sub>: cuánto mejora Z por cada unidad "
            + "que entre de x<sub>j</sub>.</td></tr>"
            + "<tr><td>&theta;</td><td>Sol &divide; coeficiente de la columna que entra (solo si es &gt; 0).</td></tr>"
            + "</table>"
            + "<h3>Solución básica factible inicial</h3>"
            + "<p>Las s<sub>i</sub> forman una matriz identidad: son las variables básicas y valen b<sub>i</sub>; "
            + "x<sub>1</sub>, ..., x<sub>n</sub> valen 0 (el origen). El tablero tiene (m + 4) filas &times; "
            + "(n + 4) columnas.</p>"
            + "<h3>El ciclo, tablero por tablero</h3>"
            + "<ol>"
            + "<li><b>Factibilidad:</b> todos los valores de Sol &ge; 0.</li>"
            + "<li><b>Optimalidad:</b> Zmax es óptimo si todos los P<sub>j</sub> &le; 0; Zmin, si todos los "
            + "P<sub>j</sub> &ge; 0. Si no se cumple, se mejora la solución.</li>"
            + "<li><b>Entra</b> a la base: en Zmax la de mayor P<sub>j</sub> &gt; 0; en Zmin la de menor "
            + "P<sub>j</sub> &lt; 0. Su columna es la <b>columna pivote</b> k.</li>"
            + "<li><b>Sale</b> la de menor &theta;<sub>i</sub> = Sol<sub>i</sub> / a<sub>ik</sub> (se excluyen "
            + "los a<sub>ik</sub> &le; 0). Esa es la <b>fila pivote</b> r.</li>"
            + "<li><b>Pivoteo:</b> el pivote a<sub>rk</sub> pasa a valer 1 (la fila pivote se divide entre él); "
            + "las demás filas: fila vieja &minus; (su valor en la columna pivote) &middot; fila pivote nueva. "
            + "La columna pivote queda con 1 en el pivote y 0 en el resto.</li>"
            + "<li><b>Repetir:</b> se actualizan X<sub>b</sub> y C<sub>b</sub>, se recalculan Z<sub>j</sub> y "
            + "P<sub>j</sub> y se vuelve a revisar la optimalidad. Un tablero por iteración.</li>"
            + "</ol>"
            + "<p><b>¿Por qué la menor &theta;?</b> Cuando x<sub>k</sub> sube desde 0, cada básica cambia así: "
            + "x<sub>bi</sub> = b<sub>i</sub> &minus; a<sub>ik</sub>&middot;x<sub>k</sub>. Si a<sub>ik</sub> &gt; 0 "
            + "baja y llega a 0 cuando x<sub>k</sub> = &theta;<sub>i</sub>. La primera en llegar a 0 es la de menor "
            + "&theta;: si x<sub>k</sub> subiera más, esa variable quedaría negativa.</p>"
            + "<p><b>Lectura:</b> básicas = valor de Sol, no básicas = 0, Z = Z<sub>j</sub> de la columna Sol.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: Zmax = 3x<sub>1</sub> + 5x<sub>2</sub></b><br>"
            + "sujeto a: x<sub>1</sub> &le; 4 &nbsp;|&nbsp; 2x<sub>2</sub> &le; 12 &nbsp;|&nbsp; "
            + "3x<sub>1</sub> + 2x<sub>2</sub> &le; 18<br>Tablero 1:"
            + "<table " + TABLA + ">"
            + "<tr><th>C<sub>b</sub></th><th>X<sub>b</sub></th><th>x<sub>1</sub></th><th>x<sub>2</sub></th>"
            + "<th>s<sub>1</sub></th><th>s<sub>2</sub></th><th>s<sub>3</sub></th><th>Sol</th><th>&theta;</th></tr>"
            + "<tr><td></td><td><b>C<sub>j</sub></b></td><td>3</td><td>5</td><td>0</td><td>0</td><td>0</td><td></td><td></td></tr>"
            + "<tr><td>0</td><td>s<sub>1</sub></td><td>1</td><td>0</td><td>1</td><td>0</td><td>0</td><td>4</td><td>&mdash;</td></tr>"
            + "<tr><td>0</td><td>s<sub>2</sub></td><td>0</td><td><b>2</b></td><td>0</td><td>1</td><td>0</td><td>12</td><td><b>6</b></td></tr>"
            + "<tr><td>0</td><td>s<sub>3</sub></td><td>3</td><td>2</td><td>0</td><td>0</td><td>1</td><td>18</td><td>9</td></tr>"
            + "<tr><td></td><td><b>Z<sub>j</sub></b></td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td>0</td><td></td></tr>"
            + "<tr><td></td><td><b>P<sub>j</sub></b></td><td>3</td><td><b>5</b></td><td>0</td><td>0</td><td>0</td><td></td><td></td></tr>"
            + "</table>"
            + "Entra x<sub>2</sub> (mayor P<sub>j</sub> = 5), sale s<sub>2</sub> (menor &theta; = 6), pivote 2."
            + "<table " + TABLA + ">"
            + "<tr><th>Tablero</th><th>Entra</th><th>Sale</th><th>Menor &theta;</th><th>Solución básica</th><th>Z</th></tr>"
            + "<tr><td>1</td><td>x<sub>2</sub></td><td>s<sub>2</sub></td><td>12/2 = 6</td>"
            + "<td>s<sub>1</sub>=4, s<sub>2</sub>=12, s<sub>3</sub>=18</td><td>0</td></tr>"
            + "<tr><td>2</td><td>x<sub>1</sub></td><td>s<sub>3</sub></td><td>6/3 = 2</td>"
            + "<td>s<sub>1</sub>=4, x<sub>2</sub>=6, s<sub>3</sub>=6</td><td>30</td></tr>"
            + "<tr><td>3</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td>"
            + "<td>s<sub>1</sub>=2, x<sub>2</sub>=6, x<sub>1</sub>=2</td><td><b>36 (óptimo)</b></td></tr>"
            + "</table>"
            + "En el tablero 3 los P<sub>j</sub> son 0, 0, 0, &minus;1.5, &minus;1: todos &le; 0, así que es óptimo: "
            + "x<sub>1</sub>=2, x<sub>2</sub>=6, Z=36. Geométricamente se recorrieron los vértices "
            + "(0,0) &rarr; (0,6) &rarr; (2,6)."
            + "</div>"
            + "<div class='nota'>"
            + "<b>Ojo con:</b> si ningún a<sub>ik</sub> de la columna que entra es &gt; 0, no hay &theta; válido y "
            + "el problema es <b>no acotado</b>. Con restricciones &ge; no hay identidad de partida: se usa Big M."
            + "</div>";

    /** Contenido teórico de la ventana de Simplex Revisado (MetodoSimplexRevisado). */
    public static final String TEORIA_REVISADO =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El mismo que el Simplex (restricciones &le; con lado derecho &ge; 0) y llega al mismo óptimo por "
            + "los mismos tableros. La diferencia: el tablero no se pivotea, <b>cada bloque se obtiene con "
            + "B<sup>&minus;1</sup></b> a partir de los datos originales.</p>"
            + "<h3>Definiciones</h3>"
            + "<ul>"
            + "<li><b>A:</b> coeficientes tecnológicos de las x (sin holguras). <b>R:</b> términos independientes. "
            + "<b>[A | I]:</b> matriz completa (las holguras de las &le; forman I).</li>"
            + "<li><b>B:</b> columnas originales de [A | I] de las variables básicas, en el orden de las filas del "
            + "tablero. <b>C<sub>b</sub>:</b> costos de las básicas, en el mismo orden.</li>"
            + "</ul>"
            + "<h3>Bloques del tablero: Tablero = B<sup>&minus;1</sup>&middot;[A | I | R]</h3>"
            + "<table " + TABLA + ">"
            + "<tr><th>Bloque</th><th>Fórmula</th><th>Color en Vertex</th></tr>"
            + "<tr><td>Columnas de las x</td><td>B<sup>&minus;1</sup>A</td><td>azul</td></tr>"
            + "<tr><td>Columnas de las s</td><td>B<sup>&minus;1</sup></td><td>naranja suave</td></tr>"
            + "<tr><td>Columna Sol</td><td>B<sup>&minus;1</sup>R</td><td>amarillo suave</td></tr>"
            + "<tr><td>Z<sub>j</sub> de las x</td><td>C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>A</td><td>rojo suave</td></tr>"
            + "<tr><td>Z<sub>j</sub> de las s</td><td>C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup></td><td>morado</td></tr>"
            + "<tr><td>Z (Z<sub>j</sub> de Sol)</td><td>C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>R</td><td>rosado</td></tr>"
            + "<tr><td>P<sub>j</sub></td><td>C<sub>j</sub> &minus; Z<sub>j</sub>; para s<sub>i</sub>: "
            + "P = &minus;(C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>)<sub>i</sub></td><td>beige</td></tr>"
            + "</table>"
            + "<h3>Criterios</h3>"
            + "<p>Los mismos del Simplex: factibilidad (B<sup>&minus;1</sup>R &ge; 0), optimalidad con P<sub>j</sub> "
            + "(Zmax: todos &le; 0; Zmin: todos &ge; 0), entra el mejor P<sub>j</sub> y sale la menor &theta;. "
            + "Después, en B se cambia la columna de la que sale por la de la que entra y se calcula la nueva "
            + "B<sup>&minus;1</sup>.</p>"
            + "<h3>Verificación</h3>"
            + "<p>Con el bloque B<sup>&minus;1</sup> de un tablero se reconstruye el resto: "
            + "B<sup>&minus;1</sup>A debe dar las columnas de las x, B<sup>&minus;1</sup>R la columna Sol y "
            + "C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>R el valor de Z. Si algo no coincide, hay un error "
            + "aritmético.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: Zmax = 3x<sub>1</sub> + 5x<sub>2</sub></b><br>"
            + "sujeto a: x<sub>1</sub> &le; 4 &nbsp;|&nbsp; 2x<sub>2</sub> &le; 12 &nbsp;|&nbsp; "
            + "3x<sub>1</sub> + 2x<sub>2</sub> &le; 18 &nbsp;(el mismo del Simplex, para comparar)"
            + "<table " + TABLA + ">"
            + "<tr><th>Tablero</th><th>X<sub>b</sub></th><th>B<sup>&minus;1</sup>R (Sol)</th>"
            + "<th>C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup></th><th>Entra / sale</th><th>Z</th></tr>"
            + "<tr><td>1</td><td>s<sub>1</sub>, s<sub>2</sub>, s<sub>3</sub></td><td>4, 12, 18</td><td>0, 0, 0</td>"
            + "<td>x<sub>2</sub> / s<sub>2</sub> (&theta; = 6)</td><td>0</td></tr>"
            + "<tr><td>2</td><td>s<sub>1</sub>, x<sub>2</sub>, s<sub>3</sub></td><td>4, 6, 6</td><td>0, 2.5, 0</td>"
            + "<td>x<sub>1</sub> / s<sub>3</sub> (&theta; = 2)</td><td>30</td></tr>"
            + "<tr><td>3</td><td>s<sub>1</sub>, x<sub>2</sub>, x<sub>1</sub></td><td>2, 6, 2</td><td>0, 1.5, 1</td>"
            + "<td>&mdash; (óptimo)</td><td><b>36</b></td></tr>"
            + "</table>"
            + "En el tablero 3, B tiene las columnas de s<sub>1</sub>, x<sub>2</sub> y x<sub>1</sub> en [A | I] y "
            + "Z = C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>R = 0&middot;4 + 1.5&middot;12 + 1&middot;18 = 36."
            + "</div>"
            + "<h3>Precios sombra</h3>"
            + "<p>Z<sub>j</sub> de la holgura s<sub>i</sub> = (C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>)<sub>i</sub> "
            + "= precio sombra de la restricción i: en el ejemplo (0, 1.5, 1). El P<sub>j</sub> de esa holgura es "
            + "lo mismo con signo contrario. El botón <b>Sensibilidad</b> de la barra de título muestra además "
            + "hasta dónde puede cambiar cada dato sin que cambie la base óptima.</p>"
            + "<div class='nota'>"
            + "<b>Dónde cambia B<sup>&minus;1</sup>:</b> con una restricción &ge;, la columna de s es &minus;I y ese "
            + "bloque da &minus;B<sup>&minus;1</sup>; en Big M la columna de A<sub>i</sub> es I y ese bloque da "
            + "B<sup>&minus;1</sup>. En Vertex, el Revisado acepta los mismos problemas que el Simplex; para "
            + "&ge; o = usa Big M y para lados derechos negativos, Simplex Dual."
            + "</div>";

    /** Contenido teórico de la ventana de Big M (MetodoBigM). */
    public static final String TEORIA_BIGM =
            "<h3>¿Cuándo se usa?</h3>"
            + "<p>Cuando hay restricciones &ge; (o =). Al estandarizar una &ge; se resta la variable de "
            + "<b>exceso</b> s<sub>i</sub> y queda un &minus;1 en la matriz: ya no hay identidad, y la solución "
            + "inicial (x = 0) no es factible.</p>"
            + "<h3>Variables artificiales A<sub>i</sub></h3>"
            + "<p>Se agrega una variable artificial A<sub>i</sub> &ge; 0 en esa restricción, con +1. Es un parche "
            + "para tener una base inicial; no significa nada real y hay que sacarla de la base durante las "
            + "iteraciones. Para eso se refleja en la F.O. con un coeficiente <b>M</b> (un número mayor que "
            + "cualquier otro del problema) que la castiga:</p>"
            + "<table " + TABLA + ">"
            + "<tr><th>Tipo de problema</th><th>C<sub>j</sub> de cada A<sub>i</sub></th><th>Por qué</th></tr>"
            + "<tr><td>Zmin</td><td>+M</td><td>Es un costo enorme: el Min la evita</td></tr>"
            + "<tr><td>Zmax</td><td>&minus;M</td><td>Es una pérdida enorme: el Max la evita</td></tr>"
            + "</table>"
            + "<table " + TABLA + ">"
            + "<tr><th>Restricción</th><th>Se agrega</th><th>Variable básica inicial</th></tr>"
            + "<tr><td>&le;</td><td>+ s<sub>i</sub> (holgura)</td><td>s<sub>i</sub> (C<sub>b</sub> = 0)</td></tr>"
            + "<tr><td>&ge;</td><td>&minus; s<sub>i</sub> (exceso) y + A<sub>i</sub></td><td>A<sub>i</sub> (C<sub>b</sub> = &plusmn;M)</td></tr>"
            + "<tr><td>=</td><td>+ A<sub>i</sub> (sin s<sub>i</sub>)</td><td>A<sub>i</sub> (C<sub>b</sub> = &plusmn;M)</td></tr>"
            + "</table>"
            + "<h3>El tablero y las reglas</h3>"
            + "<p>Es el mismo Simplex, pero con las A<sub>i</sub> en la base y M dentro de Z<sub>j</sub> y "
            + "P<sub>j</sub>, que quedan como a + bM. Para comparar dos P<sub>j</sub> se mira primero el "
            + "coeficiente de M y, si empatan, la parte sin M. Vertex nunca reemplaza M por un número: el "
            + "resultado es exacto.</p>"
            + "<table " + TABLA + ">"
            + "<tr><th>Estado</th><th>Condición</th><th>Si falla</th></tr>"
            + "<tr><td>Factibilidad</td><td>no hay ninguna A<sub>i</sub> &gt; 0 en la base</td><td>la solución "
            + "todavía no es factible</td></tr>"
            + "<tr><td>Optimalidad (Zmin)</td><td>no hay ningún P<sub>j</sub> &lt; 0</td><td>se puede mejorar</td></tr>"
            + "<tr><td>Optimalidad (Zmax)</td><td>no hay ningún P<sub>j</sub> &gt; 0</td><td>se puede mejorar</td></tr>"
            + "</table>"
            + "<h3>Receta completa (Zmin)</h3>"
            + "<ol>"
            + "<li>Estandarizar: &le; &rarr; + s<sub>i</sub>; &ge; &rarr; &minus; s<sub>i</sub> + A<sub>i</sub>.</li>"
            + "<li>F.O.: s<sub>i</sub> con coeficiente 0; A<sub>i</sub> con +M (Zmin) o &minus;M (Zmax).</li>"
            + "<li>Tablero 1: base = A<sub>i</sub> y s<sub>i</sub>; C<sub>b</sub> = M en las filas de A<sub>i</sub>; "
            + "calcular Z<sub>j</sub> y P<sub>j</sub> (llevan M).</li>"
            + "<li>Revisar: ¿hay A<sub>i</sub> &gt; 0? ¿hay P<sub>j</sub> &lt; 0? Si sí a alguna, se mejora.</li>"
            + "<li>Entra el P<sub>j</sub> más negativo (comparando a + bM), sale la menor &theta; y se pivotea "
            + "como en el Simplex.</li>"
            + "<li>Repetir hasta que no haya A<sub>i</sub> &gt; 0 y todos los P<sub>j</sub> &ge; 0. Leer: "
            + "básicas = Sol, no básicas = 0, Z = Z<sub>j</sub> de Sol.</li>"
            + "</ol>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: Zmin = 2x<sub>1</sub> + 3x<sub>2</sub></b><br>"
            + "sujeto a: x<sub>1</sub>+x<sub>2</sub> &ge; 4 &nbsp;|&nbsp; x<sub>1</sub>+2x<sub>2</sub> &ge; 6<br>"
            + "Estandarizado: x<sub>1</sub>+x<sub>2</sub>&minus;s<sub>1</sub>+A<sub>1</sub>=4 &nbsp;|&nbsp; "
            + "x<sub>1</sub>+2x<sub>2</sub>&minus;s<sub>2</sub>+A<sub>2</sub>=6; F.O. con +M en A<sub>1</sub> y A<sub>2</sub>."
            + "<table " + TABLA + ">"
            + "<tr><th>Tablero</th><th>P<sub>j</sub> de x<sub>1</sub>, x<sub>2</sub></th><th>Entra</th><th>Sale (menor &theta;)</th>"
            + "<th>Solución básica</th><th>Z</th></tr>"
            + "<tr><td>1</td><td>2&minus;2M, 3&minus;3M</td><td>x<sub>2</sub></td><td>A<sub>2</sub> (6/2 = 3)</td>"
            + "<td>A<sub>1</sub>=4, A<sub>2</sub>=6</td><td>10M</td></tr>"
            + "<tr><td>2</td><td>0.5&minus;0.5M, 0</td><td>x<sub>1</sub></td><td>A<sub>1</sub> (1/0.5 = 2)</td>"
            + "<td>A<sub>1</sub>=1, x<sub>2</sub>=3</td><td>9 + M</td></tr>"
            + "<tr><td>3</td><td>0, 0</td><td>&mdash;</td><td>&mdash;</td>"
            + "<td>x<sub>1</sub>=2, x<sub>2</sub>=2</td><td><b>10 (óptimo)</b></td></tr>"
            + "</table>"
            + "En el tablero 2, x<sub>1</sub> (0.5&minus;0.5M) y s<sub>2</sub> (1.5&minus;0.5M) empatan en M; gana "
            + "x<sub>1</sub> por la parte sin M. En el tablero 3 ya no hay A<sub>i</sub> en la base y los P<sub>j</sub> "
            + "de s<sub>1</sub> y s<sub>2</sub> valen 1: factible y óptimo."
            + "</div>"
            + "<h3>Interpretación de los P<sub>j</sub> de las s<sub>i</sub> en el tablero final</h3>"
            + "<table " + TABLA + ">"
            + "<tr><th>Restricción de s<sub>i</sub></th><th>Una unidad más del límite...</th></tr>"
            + "<tr><td>&ge; (exceso)</td><td>empeora Z (en Min sube) en P<sub>j</sub></td></tr>"
            + "<tr><td>&le; (holgura)</td><td>mejora Z (en Min baja, en Max sube) en |P<sub>j</sub>|</td></tr>"
            + "</table>"
            + "<p>En el ejemplo, pedir 5 en vez de 4 en la primera restricción sube el mínimo de 10 a 11.</p>"
            + "<div class='nota'>"
            + "<b>Ojo con:</b> si al final queda alguna A<sub>i</sub> &gt; 0 en la base, el problema no tiene "
            + "solución factible (las restricciones se contradicen). Cuando una A<sub>i</sub> sale de la base, su "
            + "columna ya no se vuelve a usar."
            + "</div>";

    /** Contenido teórico de la ventana de Simplex Dual (AlgoritmoDual). */
    public static final String TEORIA_DUAL =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El Simplex Dual arranca de un tablero que ya cumple la <b>optimalidad</b> (en Zmin todos los "
            + "P<sub>j</sub> &ge; 0; en Zmax todos &le; 0) pero que <b>no es factible</b>: algún valor de Sol es "
            + "negativo. Es lo que pasa al multiplicar una restricción &ge; por &minus;1 para escribirla como &le; "
            + "(x<sub>1</sub>+x<sub>2</sub> &ge; 4 &rarr; &minus;x<sub>1</sub>&minus;x<sub>2</sub> &le; &minus;4): la "
            + "holgura s<sub>i</sub> arranca valiendo &minus;4. Vertex hace esa conversión solo; con restricciones = "
            + "se necesitan artificiales y corresponde Big M.</p>"
            + "<h3>La idea: Simplex al revés</h3>"
            + "<p>El Simplex parte factible y avanza hacia la optimalidad: primero elige quién entra y luego quién "
            + "sale. El Dual parte óptimo y avanza hacia la factibilidad: primero elige <b>quién sale</b> (la fila) "
            + "y luego <b>quién entra</b> (la columna), sin perder nunca la optimalidad de los P<sub>j</sub>. "
            + "Cuando ya no queda ningún Sol negativo, el tablero es factible y óptimo a la vez.</p>"
            + "<h3>El ciclo, tablero por tablero</h3>"
            + "<ol>"
            + "<li><b>Sale</b> la variable básica con el Sol más negativo: la restricción más violada. Esa es la "
            + "fila pivote r.</li>"
            + "<li><b>Entra</b>, entre las columnas con a<sub>rj</sub> &lt; 0 en esa fila (las únicas que pueden "
            + "subir ese Sol), la de menor razón |P<sub>j</sub> / a<sub>rj</sub>|. Vertex muestra esa razón en una "
            + "fila extra bajo P<sub>j</sub>.</li>"
            + "<li><b>Pivoteo</b> igual que en el Simplex: fila pivote &divide; pivote; demás filas: fila vieja "
            + "&minus; (su valor en la columna pivote) &middot; fila pivote nueva.</li>"
            + "<li>Se repite mientras exista algún Sol &lt; 0. Si una fila con Sol &lt; 0 no tiene ningún "
            + "a<sub>rj</sub> &lt; 0, el problema no tiene solución factible.</li>"
            + "</ol>"
            + "<p><b>¿Por qué la menor razón?</b> Es la que deja todos los P<sub>j</sub> del lado óptimo después "
            + "del pivoteo; por eso el método solo tiene que arreglar la factibilidad.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: Zmin = 2x<sub>1</sub> + 3x<sub>2</sub></b> (el mismo de Big M)<br>"
            + "sujeto a: x<sub>1</sub>+x<sub>2</sub> &ge; 4 &nbsp;|&nbsp; x<sub>1</sub>+2x<sub>2</sub> &ge; 6, que "
            + "quedan &minus;x<sub>1</sub>&minus;x<sub>2</sub>+s<sub>1</sub> = &minus;4 &nbsp;|&nbsp; "
            + "&minus;x<sub>1</sub>&minus;2x<sub>2</sub>+s<sub>2</sub> = &minus;6"
            + "<table " + TABLA + ">"
            + "<tr><th>Tablero</th><th>Sale (Sol más negativo)</th><th>Razones |P<sub>j</sub>/a<sub>rj</sub>|</th>"
            + "<th>Entra</th><th>Solución básica</th><th>Z</th></tr>"
            + "<tr><td>1</td><td>s<sub>2</sub> (&minus;6)</td><td>x<sub>1</sub>: 2/1 = 2; x<sub>2</sub>: 3/2 = 1.5</td>"
            + "<td>x<sub>2</sub></td><td>s<sub>1</sub>=&minus;4, s<sub>2</sub>=&minus;6</td><td>0</td></tr>"
            + "<tr><td>2</td><td>s<sub>1</sub> (&minus;1)</td><td>x<sub>1</sub>: 0.5/0.5 = 1; s<sub>2</sub>: 1.5/0.5 = 3</td>"
            + "<td>x<sub>1</sub></td><td>s<sub>1</sub>=&minus;1, x<sub>2</sub>=3</td><td>9</td></tr>"
            + "<tr><td>3</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td>"
            + "<td>x<sub>1</sub>=2, x<sub>2</sub>=2</td><td><b>10 (óptimo, Sol &ge; 0)</b></td></tr>"
            + "</table>"
            + "El mismo punto que encuentra Big M, por un camino distinto: aquí Z sube de 0 a 10 mientras se "
            + "corrige la factibilidad; en Big M baja desde 10M."
            + "</div>";

    /** Contenido teórico de la ventana de Costos Duales / MODI (AlgoritmoCostosDuales). */
    public static final String TEORIA_COSTOS_DUALES =
            "<h3>¿Qué problema resuelve?</h3>"
            + "<p>El problema de transporte busca la forma más barata de enviar unidades desde varios "
            + "<b>orígenes</b> (con oferta limitada) hasta varios <b>destinos</b> (con demanda fija), conociendo "
            + "el costo de enviar una unidad por cada ruta origen-destino. Es un caso particular de programación "
            + "lineal, pero con una estructura especial que permite resolverlo sin armar un tablero completo.</p>"
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
            + "<p>Problemas con <b>2 variables</b> (x<sub>1</sub>, x<sub>2</sub>), trabajando en el primer "
            + "cuadrante: ahí ya se cumple x<sub>1</sub> &ge; 0 y x<sub>2</sub> &ge; 0. Permite ver con los ojos lo "
            + "que el Simplex hace con álgebra.</p>"
            + "<h3>Procedimiento</h3>"
            + "<ol>"
            + "<li><b>Restricción &rarr; recta.</b> Cada restricción se vuelve igualdad: 1 restricción = 1 recta "
            + "L<sub>n</sub>. x<sub>1</sub> = 0 y x<sub>2</sub> = 0 también son rectas del plano.</li>"
            + "<li><b>Puntos de corte con los ejes:</b> x<sub>1</sub> = 0 da el corte en x<sub>2</sub>, y "
            + "x<sub>2</sub> = 0 el corte en x<sub>1</sub>.</li>"
            + "<li><b>Zona factible F y puntos extremos E.</b> F es la zona que cumple todas las restricciones a la "
            + "vez. Sus esquinas son los puntos extremos, que salen del cruce de dos rectas: "
            + "E<sub>n</sub> = L<sub>i</sub> &cap; L<sub>j</sub>, y F = {E<sub>1</sub>, E<sub>2</sub>, ..., "
            + "E<sub>n</sub>}.</li>"
            + "<li><b>Reemplazar cada E en la F.O.</b> y anotar el Z de cada uno.</li>"
            + "<li><b>Criterio de elección.</b> Zmax: el mayor Z. Zmin: el menor Z.</li>"
            + "</ol>"
            + "<p><b>¿Por qué basta con los vértices?</b> Si una función lineal tiene óptimo sobre una zona "
            + "convexa, siempre se alcanza en un punto extremo, nunca estrictamente adentro.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: Zmin = 2x<sub>1</sub> + 3x<sub>2</sub></b><br>"
            + "sujeto a: L<sub>1</sub>: x<sub>1</sub>+x<sub>2</sub> &ge; 4 &nbsp;|&nbsp; L<sub>2</sub>: "
            + "x<sub>1</sub>+2x<sub>2</sub> &ge; 6"
            + "<table " + TABLA + ">"
            + "<tr><th>E</th><th>Cómo se obtiene</th><th>En F.O.</th><th>Z</th></tr>"
            + "<tr><td>E<sub>1</sub> = (0, 4)</td><td>L<sub>1</sub> &cap; eje x<sub>2</sub></td>"
            + "<td>2(0) + 3(4)</td><td>12</td></tr>"
            + "<tr><td>E<sub>2</sub> = (2, 2)</td><td>L<sub>1</sub> &cap; L<sub>2</sub></td>"
            + "<td>2(2) + 3(2)</td><td><b>10 (óptimo)</b></td></tr>"
            + "<tr><td>E<sub>3</sub> = (6, 0)</td><td>L<sub>2</sub> &cap; eje x<sub>1</sub></td>"
            + "<td>2(6) + 3(0)</td><td>12</td></tr>"
            + "</table>"
            + "Es Zmin: gana el menor Z, E<sub>2</sub> = (2, 2) con Z = 10. Es el mismo problema, y la misma "
            + "respuesta, de los ejemplos de Big M y Simplex Dual."
            + "</div>"
            + "<div class='nota'>"
            + "<b>Ojo con:</b> si F es no acotada en la dirección en que mejora la F.O., el problema es <b>no "
            + "acotado</b> y no tiene óptimo finito."
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
            + "<p>Encuentra el óptimo de forma <b>enumerativa</b>: estandarizar, fijar variables en 0 y probar "
            + "todas las combinaciones posibles. No avanza mejorando como el Simplex, sino que revisa todas las "
            + "soluciones básicas y se queda con la mejor factible.</p>"
            + "<h3>Procedimiento</h3>"
            + "<ol>"
            + "<li><b>Estandarizar.</b> Cada desigualdad se vuelve ecuación con una variable s<sub>i</sub> &ge; 0, "
            + "que entra a la F.O. con coeficiente 0:"
            + "<table " + TABLA + ">"
            + "<tr><th>Restricción</th><th>Se hace</th><th>La variable es</th></tr>"
            + "<tr><td>&le;</td><td>se suma s<sub>i</sub></td><td>holgura: recurso que sobra (si vale 0, se agotó)</td></tr>"
            + "<tr><td>&ge;</td><td>se resta s<sub>i</sub></td><td>exceso: cuánto se pasa del mínimo</td></tr>"
            + "</table></li>"
            + "<li><b>Contar.</b> M = número de ecuaciones; N = número de incógnitas (variables originales + "
            + "s<sub>i</sub>). Como N &gt; M, hay infinitas soluciones.</li>"
            + "<li><b>Forzar una solución única.</b> Se igualan a 0, de forma ordenada, N &minus; M variables (las "
            + "<b>no básicas</b>) y se resuelve el sistema con las M restantes (las <b>básicas</b>). Hay que "
            + "probar todas las combinaciones: C(N, M) = N! / (M! (N&minus;M)!).</li>"
            + "<li><b>Tabla.</b> Una fila por combinación: Nº, el valor de cada x y cada s, ¿Fact.? (Sí si todas "
            + "son &ge; 0; No si alguna es &lt; 0), Z (solo si es factible) y, con 2 variables, el Punto "
            + "(x<sub>1</sub>, x<sub>2</sub>). Si el sistema no tiene solución única, la fila sale como Singular.</li>"
            + "<li><b>Criterio de elección</b> entre las factibles: Zmax el mayor Z; Zmin el menor Z.</li>"
            + "</ol>"
            + "<p>Cada solución básica factible (SBF) es un vértice de la zona factible: el método gráfico y el "
            + "numérico encuentran los mismos puntos. El inconveniente es que las combinaciones crecen muy rápido; "
            + "por eso existe el Simplex.</p>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo resuelto: Zmax = 3x<sub>1</sub> + 5x<sub>2</sub></b> (el mismo del Simplex)<br>"
            + "x<sub>1</sub>+s<sub>1</sub>=4 &nbsp;|&nbsp; 2x<sub>2</sub>+s<sub>2</sub>=12 &nbsp;|&nbsp; "
            + "3x<sub>1</sub>+2x<sub>2</sub>+s<sub>3</sub>=18 &nbsp;&rarr; M = 3, N = 5, C(5, 3) = 10 combinaciones"
            + "<table " + TABLA + ">"
            + "<tr><th>Nº</th><th>Básicas</th><th>x<sub>1</sub></th><th>x<sub>2</sub></th><th>s<sub>1</sub></th>"
            + "<th>s<sub>2</sub></th><th>s<sub>3</sub></th><th>¿Fact.?</th><th>Z</th><th>Punto</th></tr>"
            + "<tr><td>1</td><td>x<sub>1</sub>, x<sub>2</sub>, s<sub>1</sub></td><td>2</td><td>6</td><td>2</td><td>0</td><td>0</td><td>Sí</td><td><b>36</b></td><td>(2, 6)</td></tr>"
            + "<tr><td>2</td><td>x<sub>1</sub>, x<sub>2</sub>, s<sub>2</sub></td><td>4</td><td>3</td><td>0</td><td>6</td><td>0</td><td>Sí</td><td>27</td><td>(4, 3)</td></tr>"
            + "<tr><td>3</td><td>x<sub>1</sub>, x<sub>2</sub>, s<sub>3</sub></td><td>4</td><td>6</td><td>0</td><td>0</td><td>&minus;6</td><td>No</td><td>&mdash;</td><td>(4, 6)</td></tr>"
            + "<tr><td>4</td><td>x<sub>1</sub>, s<sub>1</sub>, s<sub>2</sub></td><td>6</td><td>0</td><td>&minus;2</td><td>12</td><td>0</td><td>No</td><td>&mdash;</td><td>(6, 0)</td></tr>"
            + "<tr><td>5</td><td>x<sub>1</sub>, s<sub>1</sub>, s<sub>3</sub></td><td colspan='5'>singular (s<sub>2</sub> = 0 contradice 2x<sub>2</sub> + s<sub>2</sub> = 12)</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td></tr>"
            + "<tr><td>6</td><td>x<sub>1</sub>, s<sub>2</sub>, s<sub>3</sub></td><td>4</td><td>0</td><td>0</td><td>12</td><td>6</td><td>Sí</td><td>12</td><td>(4, 0)</td></tr>"
            + "<tr><td>7</td><td>x<sub>2</sub>, s<sub>1</sub>, s<sub>2</sub></td><td>0</td><td>9</td><td>4</td><td>&minus;6</td><td>0</td><td>No</td><td>&mdash;</td><td>(0, 9)</td></tr>"
            + "<tr><td>8</td><td>x<sub>2</sub>, s<sub>1</sub>, s<sub>3</sub></td><td>0</td><td>6</td><td>4</td><td>0</td><td>6</td><td>Sí</td><td>30</td><td>(0, 6)</td></tr>"
            + "<tr><td>9</td><td>x<sub>2</sub>, s<sub>2</sub>, s<sub>3</sub></td><td colspan='5'>singular (s<sub>1</sub> = 0 contradice x<sub>1</sub> + s<sub>1</sub> = 4)</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td></tr>"
            + "<tr><td>10</td><td>s<sub>1</sub>, s<sub>2</sub>, s<sub>3</sub></td><td>0</td><td>0</td><td>4</td><td>12</td><td>18</td><td>Sí</td><td>0</td><td>(0, 0)</td></tr>"
            + "</table>"
            + "Es Zmax: entre las factibles gana la 1, x<sub>1</sub>=2, x<sub>2</sub>=6 con Z=36, el mismo óptimo "
            + "del Simplex."
            + "</div>";

    /** Contenido teórico de la ventana de análisis de sensibilidad (VentanaSensibilidad). */
    public static final String TEORIA_SENSIBILIDAD =
            "<h3>¿Qué responde?</h3>"
            + "<p>Con el tablero óptimo en la mano, qué pasa si cambian los datos: si un recurso aumenta, si "
            + "cambia una utilidad o un costo, o si se obliga a usar una variable que hoy vale 0. Todo sale del "
            + "tablero final, sin volver a resolver el problema.</p>"
            + "<h3>Tabla de variables de decisión</h3>"
            + "<table " + TABLA + ">"
            + "<tr><th>Columna</th><th>Qué significa</th></tr>"
            + "<tr><td>Valor</td><td>Valor óptimo de la variable (Sol si es básica, 0 si no).</td></tr>"
            + "<tr><td>Costo reducido</td><td>El P<sub>j</sub> de la variable en el tablero final: cuánto cambia Z "
            + "por cada unidad que se fuerce de una variable que vale 0. En las básicas es 0.</td></tr>"
            + "<tr><td>Coef. en Z</td><td>Su C<sub>j</sub> actual.</td></tr>"
            + "<tr><td>Aumento / disminución permisible</td><td>Cuánto puede subir o bajar ese C<sub>j</sub> sin "
            + "que cambie la solución óptima (los mismos P<sub>j</sub> siguen del lado óptimo).</td></tr>"
            + "<tr><td>Rango del coeficiente</td><td>El intervalo resultante [C<sub>j</sub> &minus; disminución, "
            + "C<sub>j</sub> + aumento]. Dentro de él la solución no cambia (Z sí, porque cambia C<sub>j</sub>).</td></tr>"
            + "</table>"
            + "<h3>Tabla de restricciones</h3>"
            + "<table " + TABLA + ">"
            + "<tr><th>Columna</th><th>Qué significa</th></tr>"
            + "<tr><td>Holgura</td><td>Valor de s<sub>i</sub> en el óptimo: cuánto sobra del recurso. Si es "
            + "&gt; 0, la restricción no está activa.</td></tr>"
            + "<tr><td>Precio sombra</td><td>Cuánto cambia Z por cada unidad más de lado derecho (por ejemplo, una "
            + "hora más de máquina). Es el Z<sub>j</sub> de s<sub>i</sub> en el tablero final, "
            + "(C<sub>b</sub><sup>T</sup>B<sup>&minus;1</sup>)<sub>i</sub>; su P<sub>j</sub> es lo mismo con signo "
            + "contrario. Si sobra recurso, vale 0.</td></tr>"
            + "<tr><td>Aumento / disminución permisible</td><td>Cuánto puede subir o bajar el lado derecho "
            + "b<sub>i</sub> sin que ninguna Sol = B<sup>&minus;1</sup>R se vuelva negativa.</td></tr>"
            + "<tr><td>Rango del lado derecho</td><td>El intervalo resultante: dentro de él, el precio sombra sigue "
            + "siendo válido.</td></tr>"
            + "</table>"
            + "<h3>Cómo se calculan los rangos</h3>"
            + "<ul>"
            + "<li><b>Lado derecho:</b> si b<sub>i</sub> cambia en &Delta;, la nueva Sol es B<sup>&minus;1</sup>R + "
            + "&Delta;&middot;(columna i de B<sup>&minus;1</sup>). Se busca el mayor &Delta; hacia arriba y hacia "
            + "abajo que deja todas las Sol &ge; 0.</li>"
            + "<li><b>Coeficiente de una variable no básica:</b> solo cambia su propio P<sub>j</sub>; el límite es "
            + "cuando ese P<sub>j</sub> llega a 0 (ahí conviene que entre).</li>"
            + "<li><b>Coeficiente de una variable básica:</b> cambia su C<sub>b</sub> y con él todos los "
            + "Z<sub>j</sub>; el límite es el primer P<sub>j</sub> de una no básica que cruce al lado no óptimo.</li>"
            + "</ul>"
            + "<div class='ejemplo'>"
            + "<b>Ejemplo: Zmax = 3x<sub>1</sub> + 5x<sub>2</sub></b>, con x<sub>1</sub> &le; 4, 2x<sub>2</sub> &le; 12, "
            + "3x<sub>1</sub> + 2x<sub>2</sub> &le; 18 (óptimo x<sub>1</sub>=2, x<sub>2</sub>=6, Z=36)"
            + "<table " + TABLA + ">"
            + "<tr><th>Restricción</th><th>Holgura</th><th>Precio sombra</th><th>Rango del lado derecho</th></tr>"
            + "<tr><td>R1: x<sub>1</sub> &le; 4</td><td>2</td><td>0</td><td>[2, &infin;)</td></tr>"
            + "<tr><td>R2: 2x<sub>2</sub> &le; 12</td><td>0</td><td>1.5</td><td>[6, 18]</td></tr>"
            + "<tr><td>R3: 3x<sub>1</sub>+2x<sub>2</sub> &le; 18</td><td>0</td><td>1</td><td>[12, 24]</td></tr>"
            + "</table>"
            + "Una unidad más en R2 (13 en vez de 12) sube Z en 1.5, a 37.5; vale mientras b<sub>2</sub> esté entre "
            + "6 y 18. A R1 le sobran 2 unidades, por eso su precio sombra es 0. Los coeficientes de Z pueden "
            + "moverse en c<sub>1</sub> &isin; [0, 7.5] y c<sub>2</sub> &isin; [2, &infin;) sin que cambie la "
            + "solución."
            + "</div>"
            + "<div class='nota'>"
            + "<b>Convenciones:</b> los valores siguen las mismas convenciones que el informe de sensibilidad de "
            + "Solver de Excel, así que se pueden comparar uno con otro. Los rangos valen para cambiar <i>un</i> "
            + "dato a la vez; si cambian varios, hay que volver a resolver."
            + "</div>";
}
