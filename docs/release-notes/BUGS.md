# Registro de bugs — Vertex

Cada bug tiene un ID único (`n_XXXXXX`) que se referencia en los release notes de tipo **arreglo de bugs**. Al reportar o corregir uno nuevo, agrégalo acá con el siguiente ID disponible — no reutilices IDs aunque un bug se elimine de la lista.

Formato de cada entrada: **ID**, **Nombre corto**, **Descripción**, **Estado** (`Abierto` / `Corregido en X.Y.Z`).

---

### n_000001 — Botón de historial fuera de pantalla
El botón hamburguesa se posicionaba usando el ancho/alto externo del `JFrame` en vez del `layeredPane` real. Con la barra de título personalizada de FlatLaf, esos valores no coinciden, así que el botón terminaba fuera del área visible o superpuesto con los controles de la ventana (minimizar/cerrar).
**Estado:** Corregido en 1.2.1

### n_000002 — Fade in/out del botón de historial no funcionaba
Bug de visibilidad invertido en la animación: al cerrar el panel, el botón no se volvía a hacer visible antes de animarle el alpha, así que el fade-in nunca se veía (el componente seguía oculto).
**Estado:** Corregido en 1.2.1

### n_000003 — Hueco muerto entre el botón y el panel del historial
Quedaba un espacio de 8px sin ningún componente entre el botón y el panel. Cruzar ese hueco con el mouse contaba como "salir" y cerraba el menú antes de tiempo.
**Estado:** Corregido en 1.2.1

### n_000004 — Menú de historial sin animación de apertura/cierre
El panel aparecía y desaparecía de golpe (`setVisible`), sin ninguna transición. Se reemplazó por un efecto de cajón: se despliega desde el botón y se repliega al cerrar.
**Estado:** Corregido en 1.2.1

### n_000005 — Botón "Calcular" no centrado
Quedaba alineado a la derecha (`LINE_END`) en vez de centrado en su fila.
**Estado:** Corregido en 1.2.1

### n_000006 — El popup de "Eliminar" cerraba el menú de historial
Al hacer click derecho sobre un elemento del historial para eliminarlo, el popup de confirmación no formaba parte de la zona de hover rastreada por el menú, así que el menú se cerraba solo apenas aparecía.
**Estado:** Corregido en 1.2.1

### n_000007 — El tooltip de los elementos del historial cerraba el menú
Al pasar el mouse sobre un elemento del historial el tiempo suficiente para que apareciera su tooltip de detalle, mover el cursor hacia él salía de la zona de hover rastreada y cerraba el menú.
**Estado:** Corregido en 1.2.1

### n_000008 — Tooltips redundantes en los campos Z y Restricciones
El tooltip genérico incorporado en `SubIndiceTextPane` seguía apareciendo al pasar el mouse por esos inputs, duplicando la información que ya da el símbolo **(?)** de cada label.
**Estado:** Corregido en 1.2.1

### n_000009 — Símbolo "(?)" invisible en la barra de título
El botón de ayuda de las 7 ventanas de resultados heredaba un color de texto claro (por defecto de FlatLaf para `toolBarButton`) que se perdía contra la barra de título blanca, quedando prácticamente invisible.
**Estado:** Corregido en 1.2.1

### n_000010 — Formato numérico distinto en cada ventana de resultados
Cada una de las 7 ventanas formateaba los números con su propia regla: Gráfico mostraba siempre 3 decimales en el texto (`60.000`) y 2 en la gráfica; Húngaro y Costos Duales usaban 2 decimales y el resto 3. Además, `String.format` respetaba el idioma del sistema, así que en equipos en español aparecía la coma como separador (`200,000`). Ahora todas usan `FormatoNumero.fmt`: punto decimal, 3 decimales fijos y los enteros sin decimales.
**Estado:** Corregido en 2.0.1
