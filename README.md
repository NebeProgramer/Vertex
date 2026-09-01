# Vertex — Solucionador de Programación Lineal

Aplicación de escritorio en Java para modelar y resolver problemas de optimización lineal. Vertex permite escribir una función objetivo y sus restricciones, seleccionar un método de solución o dejar que la aplicación recomiende uno de acuerdo con la estructura del problema.

<p align="center">
  <img src="/src/main/resources/vertex-logo.png" alt="Logo de Vertex" width="180">
</p>

## Descarga

No hace falta compilar nada para usar Vertex: solo necesitas tener **Java 17 o superior** instalado (JRE es suficiente).

**[⬇ Descargar la última versión](https://github.com/NebeProgramer/Vertex/releases/latest/download/Vertex.zip)**

También puedes buscar el `.zip` de cada versión en la sección [Releases](https://github.com/NebeProgramer/Vertex/releases) de este repositorio.

1. Descarga y descomprime `Vertex.zip`.
2. Ejecuta `Vertex.jar` con doble clic, o desde una terminal:

   ```bash
   java -jar Vertex.jar
   ```

## Funcionalidades

- Optimización de problemas de **maximización** y **minimización**.
- Validación de la sintaxis de las expresiones y de las restricciones.
- Visualización de tablas y pasos intermedios de los algoritmos, indicando en cada paso si la solución es factible y si ya es óptima.
- Gráfica de las restricciones, región factible y solución óptima cuando el problema tiene dos variables, con todos los puntos de intersección factibles marcados y un cuadro flotante con sus valores al pasar el mouse sobre ellos.
- Formato visual de subíndices para variables como `x1`, `x2` o `x11`.
- Interfaz moderna con tema personalizado (FlatLaf), incluyendo un menú de historial deslizante.
- Menú de historial para guardar, editar y eliminar tus cálculos más recientes.
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

La opción **“Decide por mí”** analiza la estructura de entrada y elige entre los métodos disponibles. Para transporte desbalanceado, el método de Costos Duales agrega una fila o columna ficticia de costo cero.

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

## Requisitos

- JDK 17 o superior.
- Apache Maven 3.9 o superior para compilar desde la línea de comandos.
- NetBeans (opcional, recomendado para editar los formularios `.form`).

Dependencias declaradas en el `pom.xml`:

- `org.netbeans.external:AbsoluteLayout:RELEASE220`
- `org.json:json:20240303` — persistencia del historial de cálculos.
- `com.formdev:flatlaf:3.7.2` — interfaz visual moderna.

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

El comando `mvn clean package` genera un único JAR ejecutable en `target/Vertex.jar`, con todas las dependencias incluidas. La clase de inicio es `com.mycompany.problema.programacion.lineal.ProblemaProgramacionLineal`.

> La carpeta `target/` no está incluida en este repositorio (se genera al compilar). Si solo quieres usar la aplicación sin compilarla, ve a la sección [Descarga](#descarga).

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

## Límites y consideraciones

- El método gráfico trabaja únicamente con dos variables.
- El método numérico enumera combinaciones de variables básicas y limita el sistema estándar a 18 variables para evitar un crecimiento combinatorio excesivo.
- El método Simplex Dual admite restricciones `<=`; para restricciones `>=` o `=`, use Big M.
- Los archivos `SimplexExamples.txt` contienen casos de entrada de referencia.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/
│   │   ├── com/mycompany/problema/programacion/lineal/
│   │   │   ├── algoritmos/
│   │   │   │   ├── Json_History.java
│   │   │   │   └── MetodoPL.java
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
│   │   │   ├── MultiCalculos.java
│   │   │   ├── SeleccionMultiAlgoritmo.java
│   │   │   ├── PrincipalPage.java
│   │   │   ├── MenuHistorial.java
│   │   │   ├── VentanaPrefs.java
│   │   │   ├── VentanaDonacion.java
│   │   │   ├── FondoMatematico.java
│   │   │   ├── FondoAzulPlano.java
│   │   │   └── Recursos.java
│   │   │
│   │   └── SimplexExamples.txt
│   │
│   └── resources/
│       ├── vertex-logo.png
│       ├── vertex-icon.png
│       ├── vertex-icon-256.png
│       └── vertex-icon.ico
└──
```

## Estado del proyecto

No se incluyen pruebas automatizadas ni una licencia explícita. Antes de reutilizar o distribuir el código, agregue una licencia adecuada y verifique los resultados con casos de prueba conocidos.

## 💖 Ayúdame a avanzar

Vertex es un proyecto **totalmente gratuito** y cualquier apoyo me ayuda a seguir mejorándolo.

Si te ha resultado útil, puedes ayudarme de dos maneras:

* 💬 **Déjame un comentario** sobre Vertex a través de mi [portafolio personal](https://repositorio-aeop.onrender.com/).
* ☕ **Invítame a un café** mediante una donación para apoyar el desarrollo del proyecto.

Toda ayuda, comentario o sugerencia es bienvenida y me motiva a seguir trabajando en Vertex.

**¡Muchas gracias por el apoyo! 💖**