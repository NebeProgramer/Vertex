<!--
Plantilla: NUEVAS FUNCIONES (versión X.Y.0)
Úsala para versiones que agregan funciones nuevas sin romper compatibilidad
con la anterior. Es la plantilla "estándar" de release.
Reemplaza todo lo que está entre {llaves} y borra las secciones que no apliquen.
-->

# Vertex {X.Y} está aquí
{1-2 líneas de intro resumiendo el enfoque de este release — ej. "ahora con una interfaz más bonita, clara y pulida, además de nuevas herramientas para..."}

---

## 🚀 Nuevas funciones

### {emoji} {Nombre de la función}
{Descripción breve, en tono cercano, de qué hace y por qué le sirve al usuario.}

<!-- repetir el bloque ### por cada función nueva -->

---

## 🎨 Colorimetría
<!-- Solo incluir esta sección si la tabla de colores cambió en este release. -->
Estos son los colores utilizados en Vertex y sus respectivos significados:

| Color | Muestra | Método / Uso Unificado | RGB | HEX |
| :--- | :---: | :--- | :---: | :---: |
| **Naranja** | 🟧 | **Pivote o elemento causante.** Celda pivote exacta (*Simplex*), número causante de operaciones (*Húngaro*) y celda que entra a la base (*Costos Duales*). | `255, 176, 120` | `#FFB078` |
| **Verde Bosque** | 🟩 | **Punto o resultado óptimo.** Punto óptimo (*Gráfico*) y celda factible y óptima (*Numérico*). | `40, 150, 70` | `#289646` |
| **Verde Lima** | 🟢 | **Factible pero no óptimo.** Puntos intermedios (*Gráfico*) y celdas válidas que no alcanzan el óptimo (*Numérico*). | `200, 230, 110` | `#C8E66E` |
| **Verde Claro** | 💚 | **Cambio de estado o celda clave.** Celdas que cambiaron de valor / suman $\theta$ (*Simplex/Costos*) y celdas con valor cero (*Húngaro*). | `200, 255, 200` | `#C8FFC8` |
| **Amarillo** | 🟨 | **Área de influencia del pivote.** Fila o columna asociada al pivote (*Simplex*) o cubierta por líneas de asignación (*Húngaro*). | `255, 255, 170` | `#FFFFAA` |
| **Rojo** | 🟥 | **Restricción o inviabilidad.** Celdas no factibles (*Numérico*) y celdas que restan $\theta$ (*Costos Duales*). | `255, 190, 190` | `#FFBEBE` |
| **Azul** | 🟦 | **Región factible.** Polígono de soluciones válidas en el método *Gráfico* (con opacidad). | `120, 180, 255` | `#78B4FF` |

---

## 🙏 Agradecimientos
{Menciones a quien haya ayudado, y agradecimiento general a quien deja comentarios/sugerencias.}

---

## 📦 Descarga
Descarga `Vertex.zip` de esta misma release, descomprímelo y ejecuta `Vertex.jar` (o `java -jar Vertex.jar`).
