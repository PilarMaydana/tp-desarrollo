Eres un diseñador UI y desarrollador frontend experto. Genera la interfaz de usuario en pantalla de escritorio (desktop) para el sistema web empresarial de la "Consultora Capit@l Humano".

### REGLAS DE RESOLUCIÓN Y DIMENSIONES:
- Resolución / Canvas: Diseña estrictamente para una resolución fija de 1920 píxeles de ancho por 1080 píxeles de alto (1920x1080 px, aspecto 16:9 desktop Full HD). Distribuye el layout de manera proporcional y profesional dentro de ese lienzo.

### REGLAS DE DISEÑO OBLIGATORIAS:
1. Layout Principal (DashboardLayout):
   - Topbar superior (alto: 80px, fondo: #3B3B3B, texto blanco): menú hamburguesa, isotipo institucional con texto "CONSULTORA CAPIT@L HUMANO", a la derecha avatar con cápsula lavanda (#EADDFF) indicando rol ("Consultor: [Nombre]"), icono de notificaciones (#D32F2F badge) y botón salir.
   - Fondo del área de trabajo: #F6F6F6. Contenedor central blanco (#FFFFFF) con bordes redondeados (16px), borde sutil (#D0BCFF) y padding generoso.

2. Paleta de Colores y Tipografía (Roboto):
   - Color primario/acento: Púrpura institucional #3F2581 (títulos H1, botones principales, bordes activos).
   - Botón Primario: Fondo #3F2581, texto blanco, bordes redondeados 8px.
   - Botón Secundario / Cancelar: Fondo gris claro #D8D8D8, texto negro #000000, bordes 8px.
   - Botón Secundario Suave: Fondo lavanda #CDCEE6, texto #2D1B60.
   - Estados de éxito: Verde #1B8354 sobre fondo #E8F5E9.
   - Estados de error: Rojo #D32F2F sobre fondo #FFEBEE.

3. Estilo de Campos de Formulario:
   - Estilo "Notched Outline" (Material Design 3): campos delimitados con borde (#3F2581 con opacidad o foco) y etiquetas (labels) incrustadas/flotantes en el borde superior en fondo blanco con texto #3F2581 en 12-14px semibold.
   - Entradas en mayúsculas por requerimiento del sistema.
   - Los mensajes de error o validación se ubican debajo del campo en color #D32F2F o en banner superior alert.

4. Barra Inferior de Acciones (ActionToolbar):
   - Al pie del formulario o tarjeta, separada por una línea sutil (#E0E0E0).
   - Botones alineados a la derecha: [Cancelar] (gris #D8D8D8) a la izquierda y [Aceptar] (púrpura #3F2581) a la derecha.

---

### CASO DE USO A DISEÑAR:
Caso de Uso N.º 017: Dar de alta Factor
Rol: Consultor
Objetivo: Crear un nuevo factor en el sistema.

Estructura de la pantalla:
1. Cabecera:
   - Título H1: "Dar de Alta Factor" en color #3F2581 (negrita, 28px).
   - Bajada descriptiva: "Ingrese los datos requeridos para registrar un nuevo factor asociado a una competencia." (#757575).

2. Campos del Formulario (Organizados en grilla limpia):
   - Fila 1 (2 columnas):
     - Campo "Código" (Input de texto notched, obligatorio con asterisco rojo *).
     - Campo "Nombre" (Input de texto notched, obligatorio *).
   - Fila 2 (2 columnas):
     - Campo "Competencia" (Select / Desplegable notched con icono chevron, obligatorio *).
     - Campo "N.º de Orden" (Select / Selector numérico notched para definir la posición del factor dentro de la competencia, obligatorio *).
   - Fila 3 (Ancho completo):
     - Campo "Descripción" (Textarea notched de 3-4 líneas, obligatorio *).

3. Pie de página (ActionToolbar al pie, alineado a la derecha):
   - Botón [Cancelar] (gris #D8D8D8).
   - Botón [Aceptar] (púrpura #3F2581).

4. Modal / Diálogo de Confirmación (Estado de éxito interactivo o superpuesto):
   - Muestra el modal de confirmación centrado con backdrop oscuro suave (bg-black/40):
     - Título: "Factor Creado" (#3F2581).
     - Mensaje: "El factor se ha creado correctamente. ¿Desea cargar otro?".
     - Acciones: Botón [No] (gris #D8D8D8) y Botón [Sí] (púrpura #3F2581).