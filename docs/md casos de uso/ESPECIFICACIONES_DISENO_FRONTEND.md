# Sistema de Diseño y Especificaciones UI para Stitch
**Proyecto:** Consultora Capit@l Humano  
**Objetivo:** Guía integral de diseño y especificación técnica de UI para la reconstrucción consistente de las pantallas de casos de uso restantes en **Stitch**, basándose estrictamente en las pantallas y componentes ya construidos en el frontend.

---

## 1. Identidad Visual y Filosofía de Diseño

El sistema de la **Consultora Capit@l Humano** utiliza un estilo inspirado en **Material Design 3 corporativo**, adaptado para sistemas de gestión empresarial (*Enterprise Web Apps*). Se caracteriza por:
- **Estructura sobria y profesional:** Fondos neutros claros en el área de trabajo (`#F6F6F6`), con contrastes en la cabecera (`#3B3B3B`) y barra lateral (`#2E2E2E`).
- **Color institucional dominante:** Púrpura profundo (`#3F2581`), que actúa como color de acento primario, jerarquía de títulos y acciones prioritarias.
- **Campos con etiquetas incrustadas (*Notched Outline*):** Formularios con campos delimitados y etiquetas flotantes incrustadas en el borde superior sobre fondo blanco.
- **Acciones y controles suaves:** Botones secundarios en tonos grises (`#D8D8D8`) y lavanda suave (`#CDCEE6`), evitando saturar visualmente al usuario.
- **Roles bien definidos:**
  - **Consultor:** Gestión de puestos, evaluaciones, ponderaciones, banco de preguntas, orden de mérito y reportes.
  - **Candidato:** Portal personal para resolver cuestionarios, ver estados y tiempos límites.

---

## 2. Paleta de Colores (Design Tokens)

Todos los colores deben mantenerse exactamente con estos valores hexadecimales:

### 2.1 Colores Principales e Institucionales
| Nombre del Token | Valor Hex | Uso Semántico y Componentes |
| :--- | :--- | :--- |
| `--color-primary` | `#3F2581` | Color primario de marca. Títulos de página (`h1`), encabezados de tablas (`thead`), botones principales (`Button variant="primary"`), borde de campos activos/enfocados, círculos activos del Stepper, texto de enlaces activos en el Sidebar. |
| `--color-primary-light` | `#896ECB` | Púrpura medio para acentos, hover de scrollbars, botones de icono secundarios, bordes intermedios. |
| `--color-primary-soft` | `#F4F0FF` | Fondo lavanda muy claro para contenedores secundarios (cajas de DualTransferList, tarjetas de resumen, hover de filas de tabla, inputs numéricos de ponderación). |
| `--color-primary-container` | `#EADDFF` | Fondo lavanda de acento. Píldora del SegmentedControl, fila seleccionada en `DataTable`, avatar de usuario en Topbar, anillo exterior del paso activo en el Stepper, chips de competencias. |
| `--color-primary-border` | `#4F378A` | Borde púrpura medio para inputs con foco y textos destacados secundarios. |

### 2.2 Colores de Estructura y Fondos de Superficie
| Nombre del Token | Valor Hex | Uso Semántico y Componentes |
| :--- | :--- | :--- |
| `--color-header-bg` | `#3B3B3B` | Fondo de la barra superior (`Topbar`). Gris oscuro neutro. |
| `--color-sidebar-bg` | `#2E2E2E` | Fondo del cajón de navegación lateral desplegable (`Sidebar`). |
| `--color-sidebar-header` | `#252525` | Fondo de la cabecera y pie del Sidebar (separación visual con borde `white/10`). |
| `--color-bg-canvas` | `#DDDDDD` | Fondo del lienzo exterior (usado en la pantalla de Login y fondo de vista previa tipo Figma). |
| `--color-bg-body` | `#F6F6F6` | Fondo general de todas las pantallas interiores dentro del `DashboardLayout`. |
| `--color-bg-surface` | `#FFFFFF` | Superficies de tarjetas, cajas de formularios, modales, campos de texto y filas de tablas. |
| `--border-subtle` | `#CCCCCC` / `#E0E0E0` | Bordes sutiles entre secciones, separadores (`Divider`) y líneas de pie de formulario. |
| `--border-primary-soft` | `#D0BCFF` | Bordes con tinte lavanda para tarjetas principales, contenedores dobles y bordes de `DataTable`. |

### 2.3 Botones y Controles
| Nombre del Token | Valor Hex | Hover Hex | Uso Semántico |
| :--- | :--- | :--- | :--- |
| `--color-btn-primary` | `#3F2581` | `#4E309D` | Acciones principales ("Aceptar", "Siguiente", "Ingresar", "Buscar", "Continuar"). |
| `--color-btn-gray` | `#D8D8D8` | `#C0C0C0` | Botones secundarios estándar ("Cancelar", "Anterior", "Modificar", "Nuevo", "Ver"). Texto `#000000`. |
| `--color-btn-lavender` | `#CDCEE6` | `#B8B9DA` | Botones secundarios suaves ("Agregar", "Eliminar", fondo de Checkbox seleccionado). Texto `#2D1B60`. |
| `--color-btn-danger` | `#D32F2F` | `#B71C1C` | Botones de acciones destructivas o alertas críticas. Texto `#FFFFFF`. |

### 2.4 Estados del Sistema y Feedback
| Estado | Color Primario | Fondo Suave | Borde | Uso en Pantallas |
| :--- | :--- | :--- | :--- | :--- |
| **Éxito (Success)** | `#1B8354` | `#E8F5E9` | `#A5D6A7` | "Estado: en proceso", Tarjeta/Pantalla de confirmación exitosa, badges de éxito. |
| **Error / Peligro** | `#D32F2F` | `#FFEBEE` / `#FEF2F2` | `#FFCDD2` / `#FECACA` | "Estado: expirado", alertas de validación, error en inputs, badge de notificación en Topbar, botón de eliminar. |
| **Neutro / Inactivo** | `#4A4A4A` | `#EEEEEE` | `#BDBDBD` | "Estado: finalizado", elementos deshabilitados o cerrados. |

### 2.5 Textos
| Token | Valor Hex | Uso |
| :--- | :--- | :--- |
| `--color-text-dark` | `#000000` / `#1C1B1F` | Textos principales, etiquetas de campos, valores de tabla. |
| `--color-text-body` | `#333333` | Párrafos descriptivos, contenido de celdas. |
| `--color-text-muted` | `#757575` / `#8E8E8E` | Subtítulos, textos de ayuda, placeholders, metadatos temporales. |
| `--color-text-light` | `#FFFFFF` | Textos sobre fondos oscuros (Topbar, cabecera de tabla, botones primarios). |

---

## 3. Tipografía

- **Familia tipográfica principal:** `'Roboto', -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif`.
- **Pesos disponibles:**
  - `Light`: 300
  - `Regular`: 400
  - `Medium`: 500
  - `Bold`: 700

### 3.1 Escala Jerárquica de Fuentes
| Nivel | Tamaño (px) | Tamaño (rem) | Peso | Color Habitual | Aplicación |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **H1 (Título de Pantalla)** | `28px - 32px` | `1.75rem - 2rem` | 700 (Bold) | `#3F2581` | Título superior de cada página ("Gestionar Puestos", "Crear Nuevo Puesto", "Evaluar Candidatos"). |
| **H2 (Título de Sección)** | `20px - 24px` | `1.25rem - 1.5rem` | 700 (Bold) | `#3F2581` | Subtítulos dentro de tarjetas o paneles ("Competencias disponibles", "Resumen de la Evaluación"). |
| **H3 (Encabezado de Caja)** | `18px` | `1.125rem` | 700 (Bold) | `#3F2581` | Títulos en paneles de transferencia o títulos de EvaluationCard. |
| **H4 / Subtítulo Destacado** | `16px` | `1rem` | 600 (Semibold) | `#3F2581` | Subtítulos en tarjetas de confirmación o cabecera de filtros. |
| **Body (Texto de Contenido)** | `14px - 16px` | `0.875rem - 1rem` | 400 (Regular) | `#333333` | Párrafos, descripciones de ayuda, celdas de tabla. |
| **Form Labels (Notched)** | `12px - 14px` | `0.75rem - 0.875rem` | 500 (Medium) | `#3F2581` | Etiquetas flotantes que cortan el borde superior del input. |
| **Input Value & Options** | `16px` | `1rem` | 400 (Regular) | `#000000` | Texto que escribe o selecciona el usuario. |
| **Button Text** | `14px - 18px` | `0.875rem - 1.125rem` | 500 / 600 | Blanco o `#000000` | Etiquetas de botones (según tamaño `sm`, `md` o `lg`). |
| **Table Header** | `14px` | `0.875rem` | 600 (Semibold) | `#FFFFFF` | Mayúsculas con espaciado entre letras (`tracking-wider`, `uppercase`). |
| **Small / Helper Text** | `12px` | `0.75rem` | 400 (Regular) | `#757575` | Textos de ayuda bajo inputs, errores (`#D32F2F`), notas al pie. |

---

## 4. Estructura y Anatomía de Layouts

### 4.1 Layout de la Aplicación (`DashboardLayout`)
Se utiliza para todas las pantallas posteriores al login.

```
┌────────────────────────────────────────────────────────────────────────┐
│ TOPBAR (Alto: 80px | Fondo: #3B3B3B | Fija / Sticky)                   │
│ [Hamburguesa] [Icono Groups] CONSULTORA CAPIT@L HUMANO  [User] [🔔] [🚪]│
├────────────────────────────────────────────────────────────────────────┤
│ [SIDEBAR DRAWER]  │ CONTENIDO PRINCIPAL (<main>)                        │
│ Ancho: 288px       │ Ancho máximo: 1152px (max-w-6xl) centrado          │
│ Fondo: #2E2E2E    │ Padding: 24px a 48px según pantalla                │
│ (Desplegable)      │ Fondo: #F6F6F6                                     │
│                    │                                                    │
│                    │ 1. Header de Pantalla: Título H1 + Bajada gris     │
│                    │ 2. Barra de Filtros / Stepper (si aplica)          │
│                    │ 3. Zona Central: Formularios / Tablas / Cajas      │
│                    │ 4. Barra de Acciones al pie (ActionToolbar)        │
└────────────────────┴────────────────────────────────────────────────────┘
```

#### Medidas y propiedades exactas de la Topbar:
- **Alto:** `80px` (`h-20`).
- **Fondo:** `#3B3B3B`.
- **Padding horizontal:** `16px` (móvil) / `32px` (tablet) / `48px` (desktop).
- **Z-Index:** `20` (Sticky top).
- **Lado izquierdo:**
  - Botón menú hamburguesa: `p-2`, bordes redondeados `8px`, hover `white/10`.
  - Isotipo `groups`: Icono de 32px en color blanco.
  - Título institucional: `text-xl tracking-wider text-white font-semibold`.
- **Lado derecho:**
  - Badge de Usuario: Avatar circular de `44px` (`w-11 h-11 rounded-full`) con fondo `#EADDFF` e icono `#4F378A` de `26px`. Texto a dos renglones: Rol arriba en `text-xs text-white/80` ("Consultor:" o "Candidato:") y Nombre en `text-base font-medium text-white`.
  - Botón Notificaciones: `40x40px`, fondo transparente con borde `white/20`, icono campana `20px`, punto de notificación rojo `#D32F2F` en la esquina superior derecha (`w-2 h-2 rounded-full`).
  - Botón Salir (Logout): `40x40px`, fondo transparente con borde `white/20`, icono logout `20px`.

#### Medidas y propiedades exactas del Sidebar (Menú Lateral):
- **Ancho:** `288px` (`w-72`).
- **Fondo:** `#2E2E2E`.
- **Cabecera:** `80px` de alto, fondo `#252525`, borde inferior `border-b border-white/10`, texto "Menú Principal" con icono `#CDCEE6` y botón 'X' para cerrar.
- **Items de navegación:**
  - Altura: `48px` (`py-3 px-4`).
  - Borde redondeado: `12px` (`rounded-xl`).
  - Separación: `6px` entre items.
  - **Estado Inactivo:** Texto `#E0E0E0`, icono `#CDCEE6`, hover con fondo `white/10`.
  - **Estado Activo (Página actual):** Fondo `#3F2581`, texto `#FFFFFF`, sombra media (`shadow-md`), icono blanco.
- **Rutas por Rol:**
  - *Consultor:* Evaluaciones (`assignment`), Reportes (`bar_chart`), Preguntas (`help_outline`), Orden de Méritos (`format_list_numbered`), Puestos (`work`).
  - *Candidato:* Mi Cuestionario (`assignment`).

### 4.2 Layout de Autenticación (`AuthLayout`)
- **Fondo exterior de pantalla:** Lienzo `#DDDDDD` centrado vertical y horizontalmente (`min-h-screen`).
- **Tarjeta contenedora:**
  - Ancho máximo: `480px`.
  - Fondo: `#FFFFFF`.
  - Radio de bordes: `24px` (`rounded-[24px]`).
  - Padding: `32px` a `40px` (`p-8 md:p-10`).
  - Sombra: `shadow-2xl` suave.
  - Borde: `1px solid #E0E0E0`.
- **Cabecera de la tarjeta:**
  - Isotipo cuadrado: `64x64px` (`w-16 h-16`), esquinas `rounded-2xl` (16px), fondo `#3F2581`, icono `groups` de `36px` en blanco, sombra ligera.
  - Título: `CONSULTORA CAPIT@L HUMANO` en `text-2xl font-bold text-[#3F2581]`.
  - Bajada: `text-sm text-[#757575] mt-1 mb-8` ("Sistema de Evaluación y Gestión de Competencias").

---

## 5. Catálogo de Componentes UI con Especificación Técnica

### 5.1 Botones (`Button`)
Todos los botones poseen `transition-all duration-200`, cursor pointer y `select-none`.

#### Tamaños de Botones:
- **`sm` (Pequeño):** `py-1.5 px-3`, texto `14px` (`text-sm`), radio `6px` (`rounded-md`), separación de iconos `6px`.
- **`md` (Mediano - Estándar):** `py-2.5 px-6`, texto `16px` (`text-base`), radio `8px` (`rounded-lg`), separación de iconos `8px`.
- **`lg` (Grande - Hero/Submit):** `py-3 px-8`, texto `18px` (`text-lg`), peso semibold, radio `12px` (`rounded-xl`), separación de iconos `12px`.

#### Variantes de Botones:
1. **`primary` (Acción Principal):**
   - Fondo: `#3F2581`
   - Hover: `#4E309D`
   - Active: `#341E6C`
   - Texto: `#FFFFFF`
   - Sombra: `shadow-sm`, hover `shadow-md`
   - Borde: Transparente
2. **`secondary` (Acción Secundaria / Cancelar / Salir):**
   - Fondo: `#D8D8D8`
   - Hover: `#C0C0C0`
   - Active: `#B0B0B0`
   - Texto: `#000000`
   - Borde: `1px solid #CCCCCC`
3. **`secondary-soft` (Acción Intermedia / Agregar / Modificar suave):**
   - Fondo: `#CDCEE6`
   - Hover: `#B8B9DA`
   - Active: `#A6A8CD`
   - Texto: `#2D1B60`
   - Borde: `1px solid #BDBEE0`
4. **`outline` (Secundario Púrpura):**
   - Fondo: Transparente
   - Hover: `#F4F0FF`
   - Texto: `#3F2581`
   - Borde: `2px solid #3F2581`
5. **`danger` (Acción Destructiva / Eliminar definitivo):**
   - Fondo: `#D32F2F`
   - Hover: `#B71C1C`
   - Active: `#9A0007`
   - Texto: `#FFFFFF`
6. **`icon` (Iconos en barras oscuras):**
   - Fondo: Transparente
   - Hover: `white/10`
   - Texto: `#FFFFFF`
   - Borde: `1px solid white/20`
   - Dimensiones: `40x40px` o `48x48px`, radio `8px`.

*Estado Deshabilitado (`disabled`):* Opacidad `0.5`, cursor `not-allowed`, sin eventos de puntero.  
*Estado de Carga (`isLoading`):* Muestra spinner circular blanco o acorde al color de texto (`w-5 h-5 border-2 border-t-transparent animate-spin`).

---

### 5.2 Campos de Formulario

#### 1. Contenedor con Etiqueta Flotante (`NotchedField`)
Este componente es el elemento visual más característico de la aplicación para campos de texto, selectores y áreas de texto.
- **Estructura:**
  - El contenedor posee `position: relative`, ancho completo `w-full`, padding superior `pt-2`.
  - La etiqueta (`<label>`):
    - `position: absolute; top: -4px; left: 16px; z-index: 10;`
    - Fondo: `#FFFFFF` con padding horizontal de `8px` (`px-2`) para cortar el borde del campo debajo.
    - Tipografía: `12px` a `14px` (`text-xs md:text-sm font-medium`).
    - Color: `#3F2581`.
    - Asterisco requerido: `<span class="text-[#D32F2F] font-bold ml-0.5">*</span>`.
  - Texto de error inferior: `text-xs text-[#D32F2F] font-semibold mt-1 pl-1`.

#### 2. Input de Texto (`Input`)
- **Fondo:** `#FFFFFF`.
- **Texto:** `16px`, color `#000000`.
- **Placeholder:** `#8E8E8E`.
- **Padding:** `px-4 py-2.5` (alto aproximado `44px`).
- **Borde redondeado:** `rounded-lg` (`8px`).
- **Borde normal:** `1px solid rgba(63, 37, 129, 0.5)` (`border-[#3F2581]/50`).
- **Borde en foco (`focus`):** `border-[#3F2581]` con halo suave `ring-2 ring-[#3F2581]/20`.
- **Borde en error:** `border-[#D32F2F]` con halo `ring-1 ring-[#D32F2F]`.
- **Icono a la izquierda:** Posicionado absoluto en `left: 14px`, color `#3F2581`, con padding izquierdo en el input de `44px` (`pl-11`).
- **Icono a la derecha / Botón Ver Contraseña:** Posicionado en `right: 14px`, color `#757575`, hover `#3F2581`.

#### 3. Selector Desplegable (`Select`)
- Idénticas dimensiones y bordes que `Input`.
- `appearance: none`.
- Flecha personalizada: Icono `chevron_down` de `18px` en color `#3F2581` posicionado absoluto en `right: 14px` centrado verticalmente.
- Opción placeholder inicial deshabilitada en color `#9E9E9E`.

#### 4. Área de Texto (`Textarea`)
- Mismo estilo de borde que el `Input`.
- Filas sugeridas: `rows={3}` o `rows={4}`.
- Propiedad de redimensión: `resize-y` (permite agrandar solo verticalmente).
- Padding: `16px`.

#### 5. Casilla de Verificación (`Checkbox`)
- **Caja del checkbox:**
  - Tamaño: `24x24px` (`w-6 h-6`).
  - Radio: `rounded-md` (`6px`).
  - **No seleccionado:** Fondo `#FFFFFF`, borde `1px solid #BDBEE0`. Hover: borde `#3F2581`.
  - **Seleccionado:** Fondo `#CDCEE6`, borde `1px solid #3F2581`, tilde '✔' centrada en color `#3F2581` con grosor destacado (`stroke-[3]`).
- **Etiqueta adjunta:** `text-sm font-normal text-[#000000]`.

---

### 5.3 Indicador de Pasos / Wizard (`Stepper`)
Utilizado en flujos guiados paso a paso (ej.: *Evaluar Candidatos*, *Crear Cuestionario*).
- **Contenedor:** Flex horizontal centrado, ancho máximo `672px` (`max-w-2xl mx-auto py-4`).
- **Nodo de paso (Círculo):**
  - Dimensiones: `40x40px` (`w-10 h-10 rounded-full`).
  - Texto central: Número de paso o icono check si está completado.
  - **Paso Activo:** Fondo `#3F2581`, número en blanco, anillo exterior de destaque `#EADDFF` de `4px` (`ring-4 ring-[#EADDFF]`).
  - **Paso Completado:** Fondo `#CDCEE6`, icono check en color `#3F2581` de `18px`.
  - **Paso Pendiente:** Fondo `#E0E0E0`, número en gris `#757575`.
- **Etiqueta del paso (debajo del círculo):**
  - `text-xs md:text-sm font-semibold mt-1.5`.
  - Color activo: `#3F2581`. Color completado: `#4F378A`. Color pendiente: `#9E9E9E`.
- **Línea conectora entre pasos:**
  - Alto: `4px` (`h-1 rounded-full`).
  - Margen lateral: `12px` (`mx-3`).
  - Color si el tramo está completado: `#3F2581`.
  - Color si el tramo está pendiente: `#E0E0E0`.

---

### 5.4 Panel de Transferencia Doble (`DualTransferList`)
Componente clave para asignación de entidades (Competencias a un Puesto, Candidatos a una Evaluación, etc.).
- **Disposición:** Grilla de 2 columnas en desktop (`grid grid-cols-1 lg:grid-cols-2 gap-6`).
- **Caja de Panel (Izquierda y Derecha):**
  - Fondo: `#F4F0FF` (Lavanda muy suave).
  - Borde: `2px solid #D0BCFF`.
  - Radio: `rounded-2xl` (`16px`).
  - Padding interior: `20px` (`p-5`).
  - Sombra: `shadow-xs`.
- **Cabecera de cada caja:**
  - Título en `text-lg font-bold text-[#3F2581]`.
  - Separador inferior fino: `border-b border-[#D0BCFF]/60 pb-3 mb-3`.
  - Caja izquierda: Opción Checkbox "Seleccionar todos".
  - Caja derecha: Badge lavanda "Ponderaciones (0-10)" si aplica.
- **Lista con scroll interior:**
  - Alto: `max-h-[360px] min-h-[220px] overflow-y-auto space-y-2 pr-2`.
- **Item en la lista:**
  - Radio: `rounded-xl` (`12px`).
  - Fondo: `#FFFFFF`. Borde: `1px solid #E0E0E0`.
  - Si está marcado para transferir: Fondo `#EADDFF`, borde `1px solid #3F2581`.
  - Título del item: `text-sm font-semibold text-[#000000]`.
  - Subtítulo opcional: `text-xs text-[#757575]`.
- **Input de Ponderación (0 a 10) en panel derecho:**
  - Dimensiones: Ancho `64px` (`w-16`), alto `36px` (`h-9`).
  - Fondo: `#F4F0FF`, borde `2px solid #3F2581`, radio `8px`.
  - Texto: `text-sm font-bold text-[#3F2581]`, centrado.
- **Acciones en el pie de caja:**
  - Panel izquierdo: Botón "Agregar (N)" alineado a la derecha, variante `secondary-soft` (`#CDCEE6`).
  - Panel derecho: Botón "Quitar (N)" alineado a la derecha, variante `secondary` (`#D8D8D8`). Botón de cesto de basura individual `#D32F2F` en cada renglón.

---

### 5.5 Tabla de Datos (`DataTable`)
Utilizada para visualización de registros, puestos, postulantes, rankings.
- **Contenedor exterior:** `rounded-xl border border-[#D0BCFF] bg-white overflow-hidden shadow-xs`.
- **Cabecera (`thead`):**
  - Fondo: `#3F2581`.
  - Texto: Blanco, `text-sm font-semibold uppercase tracking-wider`.
  - Celdas (`th`): `px-6 py-4 border-b border-[#4E309D]`.
- **Cuerpo (`tbody`):**
  - Separadores entre filas: `divide-y divide-[#EADDFF]/50`.
  - Celdas (`td`): `px-6 py-3.5 text-sm text-[#333333]`.
  - **Fila normal:** Fondo blanco, hover `hover:bg-[#F4F0FF]` interactivo con cursor pointer.
  - **Fila seleccionada (Click):** Fondo `#EADDFF`, texto en negrita `#1C1B1F`.
- **Mensaje de selección activa inferior:** `text-xs text-[#3F2581] font-medium pl-1 mt-1` ("* Registro seleccionado: [Nombre]").

---

### 5.6 Barra de Filtros y Búsqueda (`SearchFilterBar`)
- **Contenedor:** Fondo blanco, `p-4 rounded-xl border border-[#D0BCFF]/60 shadow-xs`.
- **Disposición:** Flex columna en móvil / fila en desktop alineado al fondo (`items-end`).
- **Campos de filtro:** Grilla responsiva (`grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3`).
  - Fondo de los inputs de filtro: `#FAFAFA`.
  - Borde: `1px solid #BDBEE0`. Foco: `#3F2581` con fondo blanco.
  - Texto: `14px` (`text-sm`).
  - Etiqueta pequeña superior: `text-xs font-medium text-[#3F2581] mb-1`.
- **Botón Buscar:** Ubicado a la derecha, variante `primary` (`#3F2581`), icono de lupa a la izquierda, altura fija `42px` para nivelar con los inputs.

---

### 5.7 Selector de Rol en Píldora (`SegmentedControl`)
Utilizado en Login o para alternar vistas rápidamente.
- **Contenedor:** Píldora redondeada completa (`rounded-full`), fondo `#EADDFF`, borde `1px solid #D0BCFF`, padding `6px` (`p-1.5`).
- **Opción Activa:** Fondo `#3F2581`, texto blanco `#FFFFFF`, sombra media (`shadow-md`), radio `rounded-full`, padding `py-2.5 px-4`.
- **Opción Inactiva:** Fondo transparente, texto `#4F378A`, hover `#D9CCF0`, radio `rounded-full`, padding `py-2.5 px-4`.

---

### 5.8 Etiquetas de Estado (`Badge`)
Para indicar el estado de procesos, cuestionarios y candidatos.
- **Estructura común:** Forma de píldora `inline-flex items-center px-3 py-1 rounded-full text-xs md:text-sm font-semibold border`.
- **Punto indicador:** Círculo pequeño a la izquierda `w-2 h-2 rounded-full mr-2 bg-current opacity-75`.
- **Variantes de Estado:**
  1. **En Proceso (`in-progress`):**
     - Fondo: `#E8F5E9` (Verde claro)
     - Texto: `#1B8354` (Verde oscuro)
     - Borde: `#A5D6A7`
  2. **Finalizado (`finished`):**
     - Fondo: `#EEEEEE` (Gris claro)
     - Texto: `#4A4A4A` (Gris oscuro)
     - Borde: `#BDBDBD`
  3. **Expirado / Anulado (`expired`):**
     - Fondo: `#FFEBEE` (Rojo suave)
     - Texto: `#D32F2F` (Rojo fuerte)
     - Borde: `#FFCDD2`

---

### 5.9 Tarjeta de Cuestionario / Evaluación (`EvaluationCard`)
Utilizada en el portal del candidato o en el panel de seguimiento.
- **Contenedor:** Fondo blanco, radio `rounded-2xl` (`16px`), borde `2px solid #D0BCFF`, padding `24px` (`p-6`), sombra suave con hover `shadow-md`.
- **Sección Izquierda:**
  - Caja de icono: `40x40px` (`w-10 h-10 rounded-xl`), fondo `#F4F0FF`, icono de reporte/tarea `#3F2581` de `22px`.
  - Título: `text-lg font-bold text-[#3F2581]`.
  - Puesto: `text-sm font-medium text-[#4A4A4A]`.
  - Badge de estado abajo.
- **Sección Central (Metadatos):**
  - Dos columnas en texto `13px - 14px text-[#757575]`.
  - "Iniciado el: DD/MM/AAAA" y "Última modif: DD/MM/AAAA".
  - Tiempo restante (si aplica): `text-[#3F2581] font-semibold flex items-center gap-1` ("⏱️ 45 minutos restantes").
- **Sección Derecha (Botón de Acción):**
  - Si está en proceso: Botón "Continuar" (`primary`).
  - Si está finalizado: Botón "Ver" (`secondary`).
  - Si está expirado: Botón deshabilitado "Expirado".

---

### 5.10 Estado Vacío (`EmptyState`)
Para tablas sin datos, paneles sin selección previa o listas vacías.
- **Contenedor:** `bg-[#FAF8FF] border border-dashed border-[#D0BCFF] rounded-xl p-8 text-center flex flex-col items-center justify-center`.
- **Icono circular:** `48x48px` (`w-12 h-12 rounded-full`), fondo `#EADDFF`, icono `#3F2581` de `24px`.
- **Título:** `text-base font-semibold text-[#3F2581] mb-1`.
- **Descripción:** `text-sm text-[#757575] max-w-sm mb-4`.
- **Acción opcional:** Botón primario para crear o consultar nuevo registro.

---

### 5.11 Barra Inferior de Acciones (`ActionToolbar`)
Todas las pantallas de formularios o gestión deben terminar con esta barra al pie.
- **Contenedor:** `w-full py-4 px-2 mt-8 flex flex-wrap items-center gap-4 border-t border-[#E0E0E0]`.
- **Alineaciones habituales:**
  - **Alineación Derecha (`align="right"`):**
    - En formularios: `[Cancelar]` (secundario `#D8D8D8`) a la izquierda de `[Aceptar]` (primario `#3F2581`).
    - En tablas CRUD: `[Eliminar]` (`secondary-soft` `#CDCEE6`), `[Modificar]` (`secondary` `#D8D8D8`), `[Nuevo]` (`primary` `#3F2581`).
  - **Alineación a los extremos (`align="between"`):**
    - En Wizards de múltiples pasos: `[← Anterior]` (secundario) a la izquierda, y `[Siguiente →]` o `[Finalizar ✓]` (primario) a la derecha.

---

## 6. Formatos de Presentación: Éxito, Error y Modales

Para mantener consistencia absoluta en Stitch, los mensajes y pantallas de feedback deben seguir estas especificaciones:

### 6.1 Pantalla o Tarjeta de Éxito Completo (Success Screen / Card)
Utilizada al terminar un Wizard (como *Evaluar Candidatos* o *Finalizar Evaluación*) o tras una acción mayor.

```
┌────────────────────────────────────────────────────────┐
│                                                        │
│                      ╭────────╮                        │
│                      │   ✓    │   (64x64px, #E8F5E9)   │
│                      ╰────────╯   (Icono: #1B8354)     │
│                                                        │
│        ¡Evaluaciones Asignadas con Éxito!              │
│        (24px Bold, Color: #1B8354)                     │
│                                                        │
│   Se enviaron las credenciales y el acceso a los       │
│   cuestionarios correspondientes a los 3 candidatos    │
│   seleccionados. (14px, Color: #757575, max-w-md)      │
│                                                        │
│               [ Volver a Puestos ]                     │
│           (Button primary: #3F2581)                    │
│                                                        │
└────────────────────────────────────────────────────────┘
```

#### Especificaciones de la Tarjeta de Éxito:
- **Contenedor:** `bg-white p-8 md:p-12 rounded-2xl border border-[#D0BCFF] shadow-xs flex flex-col items-center text-center gap-3 w-full`.
- **Círculo del Icono:**
  - Diámetro: `64x64px` (`w-16 h-16 rounded-full`).
  - Fondo: `#E8F5E9` (Verde suave).
  - Icono / Símbolo: `✓` en color `#1B8354` de `32px` de tamaño, `font-bold`.
- **Título de Éxito:** `text-2xl md:text-3xl font-bold text-[#1B8354]`.
- **Descripción:** `text-sm text-[#757575] max-w-md mt-1`.
- **Botón de Cierre / Regreso:** `Button variant="primary"` (`#3F2581`), tamaño `md` o `lg`, margen superior `mt-4`.

---

### 6.2 Alertas y Banners de Error

#### 1. Banner de Error en Formulario (Form Submit Error / Login Error):
- **Contenedor:** `w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F] flex items-center gap-2`.
- **En pantallas de login:** Padding `p-3 rounded-lg text-center justify-center`.
- **Contenido:** Icono de advertencia o exclamación + texto explícito del error ("* Las contraseñas ingresadas no coinciden", "* Debe seleccionar al menos una competencia").

#### 2. Error de Validación Inline (debajo del campo):
- **Posición:** Debajo del campo en el `NotchedField`.
- **Tipografía:** `text-xs font-semibold text-[#D32F2F] mt-1 pl-1`.
- **Borde del campo afectado:** Cambia a `border-[#D32F2F]` con halo `ring-1 ring-[#D32F2F]`.

---

### 6.3 Pantalla o Tarjeta de Error Crítico / Fallo de Operación
Utilizada si una operación no puede completarse o hay un fallo del servidor:
- **Contenedor:** `bg-white p-8 rounded-2xl border border-red-200 shadow-xs flex flex-col items-center text-center gap-3`.
- **Círculo del Icono:** `64x64px`, fondo `#FFEBEE`, símbolo `✕` o `!` en color `#D32F2F`, tamaño `32px`.
- **Título de Error:** `text-2xl font-bold text-[#D32F2F]` ("Error al Procesar la Solicitud").
- **Descripción:** `text-sm text-[#757575] max-w-md` ("No se pudo guardar el puesto debido a un conflicto con el código ingresado.").
- **Botones de Acción:**
  - Botón Reintentar (`variant="primary"`).
  - Botón Cancelar (`variant="secondary"`).

---

### 6.4 Diálogo de Confirmación (Modal Dialog)
Utilizado para confirmaciones críticas (ej.: "¿Está seguro de eliminar el puesto seleccionado?").
- **Backdrop / Overlay:** `fixed inset-0 bg-black/40 z-50 backdrop-blur-xs flex items-center justify-center p-4`.
- **Caja del Modal:**
  - Ancho máximo: `440px`.
  - Fondo: `#FFFFFF`.
  - Radio: `rounded-2xl` (`16px`).
  - Sombra: `shadow-2xl`.
  - Borde: `1px solid #E0E0E0`.
  - Padding: `24px` (`p-6`).
- **Encabezado:** Título `text-xl font-bold text-[#3F2581]` con icono de advertencia.
- **Cuerpo:** Texto en `text-sm text-[#333333] mt-2 mb-6`.
- **Acciones (Footer del Modal):** Flex a la derecha con:
  - Botón Cancelar: `variant="secondary"` (`#D8D8D8`).
  - Botón Confirmar Acción: `variant="primary"` (`#3F2581`) o `variant="danger"` (`#D32F2F` si es eliminación definitiva).

---

## 7. Iconografía del Sistema

Los iconos provienen de la biblioteca **Lucide Icons** mapeados semánticamente:

| Nombre Semántico | Icono Lucide Equivalente | Uso Principal |
| :--- | :--- | :--- |
| `menu` | `Menu` | Botón para abrir el drawer lateral en Topbar. |
| `groups` | `Users` | Isotipo principal de marca en Topbar y Login. |
| `person` | `User` | Avatar del usuario en Topbar y selector de candidatos. |
| `notifications`| `Bell` | Botón de notificaciones en Topbar. |
| `logout` | `LogOut` | Botón de cerrar sesión en Topbar. |
| `search` | `Search` | Botón y campo de búsqueda en `SearchFilterBar`. |
| `arrow_back` | `ArrowLeft` | Botón "Anterior" en wizards y regreso de navegación. |
| `arrow_forward`| `ArrowRight` | Botón "Siguiente" o "Continuar" en tarjetas y wizards. |
| `visibility` | `Eye` | Botón ver cuestionario finalizado y ver contraseña. |
| `visibility_off`| `EyeOff` | Ocultar contraseña. |
| `edit` | `Edit2` | Botón "Modificar" en barras de acción CRUD. |
| `lock` | `Lock` | Icono decorativo en inputs de contraseña. |
| `alternate_email` | `AtSign` | Icono decorativo en inputs de usuario/email. |
| `work` | `Briefcase` | Menú Puestos en Sidebar. |
| `assignment` | `ClipboardList` | Menú Evaluaciones / Cuestionarios. |
| `bar_chart` | `BarChart3` | Menú Reportes en Sidebar. |
| `help_outline` | `HelpCircle` | Menú Banco de Preguntas en Sidebar y EmptyStates. |
| `format_list_numbered`| `ListOrdered` | Menú Orden de Méritos en Sidebar. |
| `plus` | `Plus` | Botón "Nuevo" o "Agregar" en listas y formularios. |
| `trash` | `Trash2` | Botón "Eliminar" puesto o quitar item asignado. |
| `check` | `Check` | Checkmark de pasos completados en Stepper y Checkbox activo. |
| `chevron_down` | `ChevronDown` | Indicador derecho en campos `Select`. |
| `close` | `X` | Botón cerrar en el Sidebar y modales. |

---

## 8. Plantillas de Construcción para Pantallas Restantes en Stitch

Para que Stitch genere cualquier pantalla faltante con consistencia total, se debe aplicar una de las siguientes tres estructuras estándar:

### Plantilla A: Pantalla de Consulta / Listado / Ranking (Ej.: *Orden de Méritos*, *Banco de Preguntas*, *Reportes*)
1. **Encabezado:**
   - Título `H1` en púrpura institucional: `text-2xl md:text-3xl font-bold text-[#3F2581]`.
   - Bajada descriptiva en `text-sm text-[#757575] mt-1`.
2. **Barra de Filtros (`SearchFilterBar`):**
   - Inputs en grilla con labels pequeñas superiores.
   - Botón púrpura a la derecha: `[🔍 Buscar]`.
3. **Área de Datos:**
   - Si tiene datos: `DataTable` con cabecera púrpura, filas seleccionables que se iluminan en `#EADDFF`, y badges de estado o puntuaciones en chips lavanda `#EADDFF`.
   - Si no tiene datos: `EmptyState` con borde punteado `#D0BCFF` y fondo `#FAF8FF`.
4. **Pie de Pantalla (`ActionToolbar`):**
   - Alineación derecha con botones secundarios grises y botón principal púrpura (ej.: `[Exportar PDF]` o `[Ver Detalle]`).

---

### Plantilla B: Pantalla de Carga / Formulario con Asignación (Ej.: *Alta de Preguntas*, *Modificar Puesto*, *Configurar Examen*)
1. **Encabezado:** Título `H1` + bajada gris.
2. **Alertas de Validación:** Banner condicional en `bg-red-50 text-[#D32F2F]` en caso de error.
3. **Formulario en Grilla:**
   - Fila 1: Dos o tres campos en columnas (`grid grid-cols-1 md:grid-cols-2 gap-6`) envueltos en `NotchedField`.
   - Fila 2: Campo de descripción amplia (`Textarea` con `NotchedField`).
4. **Sección de Asignación Doble (`DualTransferList`):**
   - Dos cajas `#F4F0FF` con borde `#D0BCFF`.
   - Panel izquierdo: Items con checkbox.
   - Panel derecho: Items con input numérico (0 a 10) y cesto de basura.
5. **Pie de Pantalla (`ActionToolbar align="right"`):**
   - `[Cancelar]` en gris `#D8D8D8`.
   - `[Aceptar]` en púrpura `#3F2581`.

---

### Plantilla C: Pantalla Asistida por Pasos / Wizard (Ej.: *Generación de Evaluaciones*, *Cierre de Proceso*)
1. **Encabezado:** Título `H1` + bajada gris.
2. **Progreso (`Stepper`):** Nodos circulares `#3F2581` con líneas conectoras centradas arriba.
3. **Cuerpo del Paso Actual:**
   - Caja contenedora blanca con radio `16px`, borde `border-[#D0BCFF]`, padding `p-8`.
   - Paso 1: Filtros + Selección en lista.
   - Paso 2: Configuración de opciones o selección de puesto.
   - Paso 3: Tarjeta de confirmación previa o Tarjeta de Éxito (`✓` verde `#1B8354`).
4. **Pie de Pantalla (`ActionToolbar align="between"`):**
   - Izquierda: `[← Anterior]` (deshabilitado en paso 1).
   - Derecha: `[Siguiente →]` o `[Finalizar ✓]`.

---

## 9. Resumen Rápido de Tokens para Configuración en Stitch

```json
{
  "colors": {
    "primary": "#3F2581",
    "primaryHover": "#4E309D",
    "primaryLight": "#896ECB",
    "primarySoft": "#F4F0FF",
    "primaryContainer": "#EADDFF",
    "primaryBorder": "#4F378A",
    "headerBg": "#3B3B3B",
    "sidebarBg": "#2E2E2E",
    "sidebarHeader": "#252525",
    "bgCanvas": "#DDDDDD",
    "bgBody": "#F6F6F6",
    "bgSurface": "#FFFFFF",
    "btnGray": "#D8D8D8",
    "btnGrayHover": "#C0C0C0",
    "btnLavender": "#CDCEE6",
    "btnLavenderHover": "#B8B9DA",
    "statusSuccess": "#1B8354",
    "statusSuccessBg": "#E8F5E9",
    "statusError": "#D32F2F",
    "statusErrorBg": "#FFEBEE",
    "statusNeutral": "#4A4A4A",
    "statusNeutralBg": "#EEEEEE",
    "textDark": "#000000",
    "textMuted": "#757575",
    "borderSubtle": "#CCCCCC",
    "borderLavender": "#D0BCFF"
  },
  "typography": {
    "fontFamily": "Roboto, sans-serif",
    "titlePage": "32px / 700",
    "sectionHeader": "24px / 700",
    "boxHeader": "18px / 700",
    "body": "16px / 400",
    "label": "13px / 500",
    "small": "12px / 400"
  },
  "radii": {
    "button": "8px",
    "input": "8px",
    "card": "16px",
    "badge": "9999px",
    "pill": "9999px",
    "authCard": "24px"
  }
}
```
