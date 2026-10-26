# Vertex — Solucionador de Programación Lineal

Aplicación de escritorio en Java para modelar y resolver problemas de optimización lineal. Vertex permite escribir una función objetivo y sus restricciones, seleccionar un método de solución o dejar que la aplicación recomiende uno de acuerdo con la estructura del problema — y entender, paso a paso, por qué llegó a esa respuesta.

![Logo de Vertex](/src/main/resources/vertex-logo.png)

## Descarga

No hace falta compilar nada para usar Vertex: solo necesitas tener **Java 17 o superior** instalado (JRE es suficiente). Elige la forma que prefieras:

| | Instalador (recomendado) | Portátil |
| --- | --- | --- |
| Descarga | **[⬇ VertexSetup.msi](https://github.com/NebeProgramer/Vertex/releases/latest/download/VertexSetup.msi)** | **[⬇ Vertex.zip](https://github.com/NebeProgramer/Vertex/releases/latest/download/Vertex.zip)** |
| Cómo se usa | Ejecuta el `.msi`, elige dónde instalarlo y listo: queda con acceso directo en el menú de inicio. | Descomprime el `.zip` y ejecuta `Vertex.jar` (doble clic o `java -jar Vertex.jar`). |
| Ventaja | Se desinstala desde el Panel de Control como cualquier programa de Windows. | No requiere permisos de administrador ni instalación. |

También puedes buscar el instalador o el `.zip` de cualquier versión anterior en la sección [Releases](https://github.com/NebeProgramer/Vertex/releases) de este repositorio.

> 🆕 Vertex revisa automáticamente si hay una versión más nueva cada vez que abres la aplicación, y te avisa con un enlace de descarga si la hay.Una 

## Novedades

La versión actual es **2.0.1**, que unifica el formato de los números en todas las ventanas de resultados (punto decimal, 3 decimales y enteros sin `.000`).

La **2.0.0** trajo el cambio grande: la ventana principal tiene una nueva pantalla **Texto libre (IA)** en la que escribes el problema tal como viene en el enunciado. Una IA que corre en tu propio equipo lo convierte en función objetivo y restricciones, Vertex elige los algoritmos que aplican y lo carga todo en el Editor para que lo revises y calcules. Las dos pantallas tienen además un botón **Limpiar**. Ver [Texto libre con IA](#texto-libre-con-ia-opcional).

El historial completo de cambios, versión por versión, está en [`docs/release-notes`](docs/release-notes).

## Funcionalidades

- Optimización de problemas de **maximización** y **minimización**.
- **Texto libre con IA local (opcional):** escribe el enunciado en palabras y Vertex arma el modelo por ti.
- Botón **Limpiar** en el Editor y en Texto libre.
- Validación de la sintaxis de las expresiones y de las restricciones.
- Visualización de tablas y pasos intermedios de los algoritmos, indicando en cada paso si la solución es factible y si ya es óptima.
- Ayuda teórica por algoritmo: cada ventana de resultados tiene un botón **(?)** que explica cómo funciona el método (fundamento, pasos y un ejemplo resuelto), además de recordar el significado de los colores usados en esa ventana.
- Gráfica de las restricciones, región factible y solución óptima cuando el problema tiene dos variables, con todos los puntos de intersección factibles marcados y un cuadro flotante con sus valores al pasar el mouse sobre ellos.
- Formato visual de subíndices para variables como `x1`, `x2` o `x11`.
- Interfaz moderna con tema personalizado (FlatLaf), incluyendo un menú de historial deslizante.
- Menú de historial para guardar, editar y eliminar tus cálculos más recientes — se conserva entre instalaciones y actualizaciones.
- Multicálculo: ventana unificadora de todos los cálculos, para que puedas ver la solución de un problema de todas las maneras que elijas.
- Compatibilidad: evita la selección de métodos incompatibles con la estructura del problema actual.
    Ejemplo: si el problema no tiene exactamente dos variables, el método Gráfico queda deshabilitado automáticamente.
- Revisión automática de nuevas versiones al abrir la aplicación, con un enlace de descarga si hay una disponible.
- Recordatorio de donación opcional: con el botón "Tal vez más tarde" se oculta por 15 días.

## Métodos implementados

| Método | Uso principal |
| --- | --- |
| Gráfico | Problemas con exactamente dos variables de decisión. |
| Simplex | Problemas lineales en forma estándar; es el método general. |
| Big M | Restricciones `>=` o `=` que requieren variables artificiales. |
| Simplex Dual | Casos con restricciones `<=` y términos independientes negativos. |
| Numérico | Enumeración de soluciones básicas o puntos extremos. |
| Húngaro | Problemas de asignación con matriz de costos cuadrada. |
| Costos Duales (MODI) | Problemas de transporte; usa Vogel para la solución inicial y MODI para optimizarla. |

La opción **“Decide por mí”** analiza la estructura de entrada y elige entre los métodos disponibles. Para transporte desbalanceado, el método de Costos Duales agrega una fila o columna ficticia de costo cero. En cualquiera de las 7 ventanas de resultados, el botón **(?)** de la barra de título explica el método correspondiente con el mismo nivel de detalle que una clase de investigación de operaciones.

## Colorimetría

A continuación se especifica una tabla con los colores usados y su significado en cada uno de los algoritmos

| Color | Muestra | Método / Uso Unificado | RGB | HEX |
| :--- | :---: | :--- | :--- | :--- |
| **Naranja** | `🟧` | **Pivote o elemento causante**. Celda pivote exacta (Simplex), número causante de operaciones (Húngaro) y celda que entra a la base (Costos Duales). | `255, 176, 120` | `#FFB078` |
| **Verde Bosque** | `🟩` | **Punto o resultado óptimo**. Punto óptimo (Gráfico) y celda factible y óptima (Numérico). | `40, 150, 70` | `#289646` |
| **Verde Lima** | `🟢` | **Factible pero no óptimo**. Puntos intermedios (Gráfico) y celdas válidas que no alcanzan el óptimo (Numérico). | `200, 230, 110` | `#C8E66E` |
| **Verde Claro** | `💚` | **Cambio de estado o celda clave**. Celdas que cambiaron de valor / suman θ (Simplex/Costos) y celdas con valor cero (Húngaro). | `200, 255, 200` | `#C8FFC8` |
| **Amarillo** | `🟨` | **Área de influencia del pivote**. Fila o columna asociada al pivote (Simplex) o cubierta por líneas de asignación (Húngaro). | `255, 255, 170` | `#FFFFAA` |
| **Rojo** | `🟥` | **Restricción o inviabilidad**. Celdas no factibles (Numérico) y celdas que restan θ (Costos Duales). | `255, 190, 190` | `#FFBEBE` |
| **Azul** | `🟦` | **Región factible**. Polígono de soluciones válidas en el método Gráfico (con opacidad). | `120, 180, 255` | `#78B4FF` |

Esta misma tabla, junto con el resto de la teoría de cada algoritmo, está disponible dentro de la aplicación desde el botón **(?)** de cada ventana de resultados.

## Requisitos

- JDK 17 o superior.
- Apache Maven 3.9 o superior para compilar desde la línea de comandos.
- NetBeans (opcional, recomendado para editar los formularios `.form`).
- WiX Toolset v3.14 (opcional, solo para generar el instalador `.msi` localmente — ver [Generar el instalador](#generar-el-instalador)).

Dependencias declaradas en el `pom.xml`:

- `org.netbeans.external:AbsoluteLayout:RELEASE220`
- `org.json:json:20240303` — persistencia del historial de cálculos.
- `com.formdev:flatlaf:3.7.2` — interfaz visual moderna.
- `org.junit.jupiter:junit-jupiter:5.10.2` (solo para tests).

La conexión con la IA usa el `HttpClient` incluido en Java 17, así que no agrega dependencias.

Estas se descargan automáticamente al compilar con Maven, siempre que tengas conexión a internet en ese momento (si ves un error de resolución de dependencias, revisa tu conexión antes que nada).

## Ejecutar el proyecto

### Con NetBeans

1. Abra la carpeta del proyecto como un proyecto Maven.
2. Ejecute el proyecto con **Run Project**.

### Con Maven

```bash
mvn clean package
java -jar target/Vertex.jar
```

El comando `mvn clean package` genera un único JAR ejecutable en `target/Vertex.jar`, con todas las dependencias incluidas, y además deja listo el archivo portátil de la release en `release/Vertex.zip` (con `Vertex.jar`, `LICENSE` y `NOTICE`). La clase de inicio es `com.mycompany.problema.programacion.lineal.ProblemaProgramacionLineal`.

> La carpeta `target/` no está incluida en este repositorio (se genera al compilar). Si solo quieres usar la aplicación sin compilarla, ve a la sección [Descarga](#descarga).

### Generar el instalador

El `.msi` de las releases se genera con [jpackage](https://openjdk.org/jeps/392), en un profile de Maven aparte para no exigirle WiX Toolset a quien solo quiere compilar el jar:

```bash
mvn clean package -P instalador
```

Requiere tener [WiX Toolset v3.14](https://wixtoolset.org/) instalado y en el `PATH` de Windows.

Al terminar, la carpeta `release/` tiene los dos archivos de la release con los nombres que enlaza la sección [Descarga](#descarga), listos para subir a GitHub:

```text
release/
├── VertexSetup.msi   (instalador)
└── Vertex.zip        (portátil: Vertex.jar + LICENSE + NOTICE)
```

`mvn clean` vacía también `release/`, y la carpeta no se sube al repositorio (está en `.gitignore`).

## Formato de entrada

### Problemas lineales generales

Use variables numeradas como `x1`, `x2`, `x3`, etc. Los espacios son opcionales.

```text
Tipo: Max
Función objetivo: 3x1 + 5x2

Restricciones:
2x1 + x2 <= 8
x1 + 2x2 <= 8
```

Los coeficientes pueden ser enteros, decimales, positivos o negativos. El coeficiente `1` puede omitirse: `x1` equivale a `1x1`. Se aceptan los operadores `<=`, `>=` y `=`.

### Asignación y transporte

Los métodos Húngaro y Costos Duales usan variables de doble índice, por ejemplo `x11`, `x12`, `x21`. Estas representan la relación origen–destino (o trabajador–tarea).

Ejemplo de asignación:

```text
Tipo: Min
Función objetivo: 7x11 + 9x12 + 3x13 + 6x21 + 4x22 + 8x23 + 5x31 + 2x32 + x33

Restricciones:
x11 + x12 + x13 = 1
x21 + x22 + x23 = 1
x31 + x32 + x33 = 1
x11 + x21 + x31 = 1
x12 + x22 + x32 = 1
x13 + x23 + x33 = 1
```

Para estos métodos, incluya todas las combinaciones de origen y destino en la función objetivo. El método Húngaro requiere una matriz cuadrada y restricciones de asignación balanceadas.

## Texto libre con IA (opcional)

En la pestaña **Texto libre (IA)** puedes pegar un enunciado como este:

> Una carpintería fabrica mesas y sillas. Cada mesa deja una ganancia de 3 dólares y cada silla de 5. Se pueden fabricar como mucho 4 mesas por día. Cada silla requiere 2 horas de pintura y hay 12 horas disponibles. Cada mesa usa 3 tablas y cada silla 2, y hay 18 tablas.

Al presionar **Analizar** (o `Ctrl+Enter`), Vertex vuelve al Editor con `Max Z = 3x1 + 5x2`, las tres restricciones y los algoritmos recomendados (Gráfico y Simplex) ya marcados. Luego tú revisas y presionas **Calcular** como siempre.

Cómo funciona:

- El análisis lo hace [Ollama](https://ollama.com/download), un motor de IA **gratuito que corre en tu equipo**: no necesita internet ni cuenta, y el enunciado no sale de tu computador.
- La primera vez, Vertex ofrece descargar el modelo `qwen2.5:3b` (≈2 GB). No requiere tarjeta gráfica; se recomiendan unos 4 GB de RAM libres. En CPU, cada análisis tarda entre unos segundos y medio minuto.
- La IA solo modela (variables, Z y restricciones). Los algoritmos recomendados los elige Vertex según la estructura del problema, con las mismas reglas que habilitan el selector.
- Vertex corrige solo los detalles de formato más comunes: pasa al lado izquierdo las variables que quedaron a la derecha (`x1 <= 2x3` → `x1-2x3<=0`) y simplifica montos con muchos ceros (`300000x1 + ... <= 12000000` → `3x1 + ... <= 120`). En ambos casos deja un aviso para que revises el sentido de la restricción. También avisa cuando un recurso compartido parece haberse partido en varias restricciones.
- Lo que propone la IA pasa por el mismo validador del Editor. Si no cumple el formato de Vertex, se le pide que se corrija. Si aun así queda algo mal, se carga igual y aparece un aviso con el error para que lo arregles a mano.
- Sin Ollama, Vertex funciona exactamente igual; la pestaña solo muestra cómo instalarlo.

Para usar otro modelo o servidor, cambia las preferencias `iaModelo` e `iaUrl` (nodo de Java Preferences `com/mycompany/problema/programacion/lineal/ia`).

## Límites y consideraciones

- El método gráfico trabaja únicamente con dos variables.
- El método numérico enumera combinaciones de variables básicas y limita el sistema estándar a 18 variables para evitar un crecimiento combinatorio excesivo.
- El método Simplex Dual admite restricciones `<=` y `>=` (las `>=` se multiplican por −1) y está pensado para minimizar con costos positivos; para restricciones `=`, use Big M.
- Los archivos `SimplexExamples.txt` contienen casos de entrada de referencia.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/
│   │   ├── com/mycompany/problema/programacion/lineal/
│   │   │   ├── algoritmos/
│   │   │   │   ├── FormatoNumero.java
│   │   │   │   ├── Json_History.java
│   │   │   │   └── MetodoPL.java
│   │   │   │
│   │   │   ├── ia/
│   │   │   │   ├── AnalizadorProblema.java
│   │   │   │   ├── ClienteOllama.java
│   │   │   │   ├── MotorIA.java
│   │   │   │   └── ResultadoAnalisis.java
│   │   │   │
│   │   │   ├── modelo/
│   │   │   │   ├── ProblemaPL.java
│   │   │   │   ├── Restriccion.java
│   │   │   │   └── TipoRestriccion.java
│   │   │   │
│   │   │   ├── parser/
│   │   │   │   ├── ParserLP.java
│   │   │   │   └── ParserTransporte.java
│   │   │   │
│   │   │   ├── AlgoritmoCostosDuales.java
│   │   │   ├── AlgoritmoDual.java
│   │   │   ├── AlgoritmoHungaro.java
│   │   │   ├── MetodoBigM.java
│   │   │   ├── MetodoGrafico.java
│   │   │   ├── MetodoNumerico.java
│   │   │   ├── MetodoSimplex.java
│   │   │   ├── VentanaAyudaAlgoritmo.java
│   │   │   ├── GraficoPanel.java
│   │   │   ├── SubIndiceTextPane.java
│   │   │   ├── MultiCalculos.java
│   │   │   ├── SeleccionMultiAlgoritmo.java
│   │   │   ├── PrincipalPage.java
│   │   │   ├── PantallaTextoLibre.java
│   │   │   ├── ProblemaProgramacionLineal.java
│   │   │   ├── MenuHistorial.java
│   │   │   ├── VentanaPrefs.java
│   │   │   ├── VentanaDonacion.java
│   │   │   ├── ActualizacionChecker.java
│   │   │   ├── FondoMatematico.java
│   │   │   ├── FondoAzulPlano.java
│   │   │   └── Recursos.java
│   │   │
│   │   └── SimplexExamples.txt
│   │
│   └── resources/
│       ├── ia/prompt-sistema.txt
│       ├── vertex-logo.png
│       ├── vertex-icon.png
│       ├── vertex-icon-256.png
│       └── vertex-icon.ico
└──
```

Las notas de cada versión, con el detalle de qué cambió y por qué, viven en [`docs/release-notes`](docs/release-notes).

## Estado del proyecto

Hay pruebas automatizadas para la conversión de texto libre a modelo (`src/test`, se ejecutan con `mvn test`), pero todavía no para los algoritmos. Si vas a reutilizar el código, verifica los resultados con casos de prueba conocidos.

## Licencia

Vertex se distribuye bajo la licencia [Apache 2.0](LICENSE). Puedes usarlo, modificarlo y distribuirlo, conservando el aviso de autoría (© 2026 Anderson Ernesto Osorio Patiño, ver [`NOTICE`](NOTICE)) y el texto de la licencia.

## 💖 Ayúdame a avanzar

Vertex es un proyecto **totalmente gratuito** y cualquier apoyo me ayuda a seguir mejorándolo.

Si te ha resultado útil, puedes ayudarme de dos maneras:

* 💬 **Déjame un comentario** sobre Vertex a través de mi [portafolio personal](https://repositorio-aeop.onrender.com/).
* ☕ **Invítame a un café** mediante una donación para apoyar el desarrollo del proyecto.

Toda ayuda, comentario o sugerencia es bienvenida y me motiva a seguir trabajando en Vertex.

**¡Muchas gracias por el apoyo! 💖**
