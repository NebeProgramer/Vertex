

# Vertex — Solucionador de Programación Lineal

Aplicación de escritorio en Java para modelar y resolver problemas de optimización lineal. Vertex permite escribir una función objetivo y sus restricciones, seleccionar un método de solución o dejar que la aplicación recomiende uno de acuerdo con la estructura del problema.

<p align="center">
  <img src="/src/main/resources/vertex-logo.png" alt="Logo de Vertex" width="180">
</p>

## Descarga

No hace falta compilar nada para usar Vertex: solo necesitas tener **Java 17 o superior** instalado (JRE es suficiente).

**[⬇ Descargar la última versión](https://github.com/NebeProgramer/Vertex/releases/latest/download/Vertex.zip)**

También puedes buscar el `.zip` de cada versión en la sección [Releases](https://github.com/NebeProgramer/Vertex/releases) de este repositorio.

1. Descarga y descomprime `target.zip`.
2. Ejecuta `Vertex.jar` con doble clic, o desde una terminal:

   ```bash
   java -jar Vertex.jar
   ```

La aplicación revisa automáticamente al abrirse si hay una versión más nueva disponible y te avisa con un enlace de descarga.


## Funcionalidades

- Optimización de problemas de **maximización** y **minimización**.
- Validación de la sintaxis de las expresiones y de las restricciones.
- Visualización de tablas y pasos intermedios de los algoritmos.
- Gráfica de las restricciones, región factible y solución óptima cuando el problema tiene dos variables.
- Formato visual de subíndices para variables como `x1`, `x2` o `x11`.

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

## Requisitos

- JDK 17 o superior.
- Apache Maven 3.9 o superior para compilar desde la línea de comandos.
- NetBeans (opcional, recomendado para editar los formularios `.form`).

La única dependencia declarada es `org.netbeans.external:AbsoluteLayout:RELEASE220`.

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

## Estructura principal

```text
src/main/java/
├── com/mycompany/problema/programacion/lineal/
│   ├── PrincipalPage.java          # Interfaz y selección del método
│   ├── MetodoSimplex.java
│   ├── MetodoBigM.java
│   ├── AlgoritmoDual.java
│   ├── MetodoGrafico.java
│   ├── MetodoNumerico.java
│   ├── AlgoritmoHungaro.java
│   ├── AlgoritmoCostosDuales.java
│   ├── parser/                    # Análisis de expresiones y matrices
│   └── modelo/                    # Representación de problemas y restricciones
└── SimplexExamples.txt             # Ejemplos de uso
```

## Estado del proyecto

No se incluyen pruebas automatizadas ni una licencia explícita. Antes de reutilizar o distribuir el código, agregue una licencia adecuada y verifique los resultados con casos de prueba conocidos.
