# Especificación de Arquitectura de Componentes Frontend (Next.js)
**Proyecto:** Consultora Capit@l Humano  
**Fuente de diseño:** Prototipo HTML (`index.html`) y capturas de diseño (`imgs/image.png` a `image7.png`)  
**Stack tecnológico:** Next.js (App Router), TypeScript, CSS Modules / Tailwind CSS, React Hook Form + Zod

---

## 1. Resumen Ejecutivo y Diagnóstico del Sistema

A partir del análisis del prototipo en [index.html](file:///c:/UTN/Desarrollo/experimento/index.html), los estilos en [styles.css](file:///c:/UTN/Desarrollo/experimento/styles.css) y el conjunto de 7 pantallas diseñadas en [imgs/](file:///c:/UTN/Desarrollo/experimento/imgs):

1. **image.png / image2.png:** Pantalla de autenticación y acceso diferenciado (Consultor vs. Candidato) con pestañas tipo *segmented control* e inputs con iconos.
2. **image3.png / image4.png:** Pantalla de "Evaluar Candidatos" en formato *Wizard/Stepper* (Selección -> Puesto -> Finalizar), con barra de filtros, panel de transferencia doble (*Transfer List*) y barra lateral (*Sidebar*) colapsable.
3. **image5.png:** Portal del Candidato ("Mi cuestionario"), listado de evaluaciones mediante tarjetas (*Cards*) con indicadores de estado ("en proceso", "finalizado", "expirado") y metadata temporal.
4. **image6.png (reflejado en index.html):** "Crear Nuevo Puesto", formulario con inputs estilo *Material Notched Outline*, selector doble de competencias con ponderaciones (0-10).
5. **image7.png:** "Gestionar Puestos", barra de búsqueda/filtros, tabla de datos interactiva con selección de fila y barra de acciones CRUD (Eliminar, Modificar, Nuevo).

El sistema cuenta con **dos roles principales** con flujos y permisos distintos:
- **Consultor:** Gestión de puestos, preguntas, evaluaciones, ponderaciones, orden de mérito y reportes.
- **Candidato:** Acceso al cuestionario asignado, visualización de estado, temporizador y resolución de evaluaciones.

---

## 2. Sistema de Diseño (Design Tokens)

Para garantizar consistencia visual a lo largo de todo el proyecto en Next.js, se deben tipificar los siguientes tokens:

### 2.1 Paleta de Colores
| Token | Valor Hex | Uso / Semántica |
| :--- | :--- | :--- |
| `color-primary` | `#3F2581` | Color corporativo principal, bordes de inputs activos, títulos |
| `color-primary-light` | `#896ECB` | Botones de acción secundaria, botones de iconos, tabs activos |
| `color-primary-soft` | `#F4F0FF` / `#EADDFF` | Fondo de cajas contenedor, filas seleccionadas, avatar, badges |
| `color-header-bg` | `#3B3B3B` | Fondo de Topbar / Header |
| `color-sidebar-bg` | `#3B3B3B` / `#2E2E2E` | Fondo del Drawer/Sidebar desplegable |
| `color-bg-body` | `#F6F6F6` (contenedor `#FFFFFF`) | Fondos generales de la aplicación |
| `color-btn-gray` | `#D8D8D8` | Botones estándar (Cancelar, Anterior, Modificar, Nuevo) |
| `color-btn-gray-hover` | `#C0C0C0` | Estado hover de botones grises |
| `color-status-success` | `#1B8354` | "Estado: en proceso" |
| `color-status-error` | `#D32F2F` | "Estado: expirado", errores de validación |
| `color-status-neutral` | `#4A4A4A` | "Estado: finalizado" |
| `color-text-dark` | `#000000` | Texto principal, etiquetas de formularios |
| `color-text-muted` | `#C8C8C8` / `#757575` | Placeholders, descripciones secundarias |

### 2.2 Tipografía
- **Familia principal:** `'Roboto', sans-serif` (importada mediante `next/font/google`).
- **Escala tipográfica:**
  - `Title Page`: 32px, `font-weight: 500`, color primary.
  - `Section / Box Header`: 24px, `font-weight: 500`.
  - `Form Labels`: 16px, `font-weight: 400`.
  - `Input & Body`: 18px.
  - `Button Labels`: 18px a 24px (según jerarquía).

---

## 3. Catálogo de Componentes (Jerarquía Atómica)

Para estructurar una biblioteca modular y reutilizable en Next.js, organizamos los componentes desde átomos básicos hasta plantillas de página completas:

```
src/
├── components/
│   ├── ui/                    # Átomos / Primitivas de UI
│   │   ├── Button/
│   │   ├── Input/
│   │   ├── Select/
│   │   ├── Textarea/
│   │   ├── Checkbox/
│   │   ├── Badge/
│   │   ├── Icon/
│   │   └── Divider/
│   ├── molecules/             # Moléculas funcionales
│   │   ├── NotchedField/
│   │   ├── SegmentedControl/
│   │   ├── SearchFilterBar/
│   │   ├── Stepper/
│   │   ├── UserProfileBadge/
│   │   └── EmptyState/
│   ├── organisms/             # Componentes complejos de negocio
│   │   ├── Topbar/
│   │   ├── Sidebar/
│   │   ├── DualTransferList/
│   │   ├── DataTable/
│   │   ├── EvaluationCard/
│   │   └── ActionToolbar/
│   └── layouts/               # Layouts reutilizables
│       ├── DashboardLayout/
│       └── AuthLayout/
```

---

### 3.1 Nivel 1: Átomos / Componentes UI Primitivos (`components/ui`)

#### 1. `Button` (`components/ui/Button.tsx`)
- **Propósito:** Botón multipropósito utilizado en todo el sistema.
- **Variantes visuales:**
  - `primary`: Púrpura sólido (`#3F2581` o `#896ECB`) con texto blanco (ej.: "INGRESAR", "Siguiente", "Continuar").
  - `secondary`: Gris claro (`#D8D8D8`) con texto oscuro (ej.: "Cancelar", "Aceptar", "Anterior", "Modificar", "Nuevo").
  - `secondary-soft`: Lavanda (`#CDCEE6` / `#EADDFF`) para acciones destacadas sin ser primarias (ej.: "Eliminar", "Agregar").
  - `icon`: Cuadrado con bordes redondeados y borde blanco (ej.: campana, salir, búsqueda).
- **Props principales:** `variant`, `size` ('sm' | 'md' | 'lg'), `isLoading`, `leftIcon`, `rightIcon`, `disabled`, `type`.

#### 2. `Input` (`components/ui/Input.tsx`)
- **Propósito:** Entrada de texto estilizada con borde púrpura o contenedor flotante.
- **Funcionalidades requeridas:**
  - Soporte para icono a la izquierda (`leftIcon`: `@`, candado, etc.).
  - Soporte para icono a la derecha (`rightIcon`: toggle ver/ocultar contraseña).
  - Manejo de estados: `default`, `focused`, `error`, `disabled`.
  - Integrable directamente con `react-hook-form`.

#### 3. `Select` (`components/ui/Select.tsx`)
- **Propósito:** Selector desplegable (Empresas, Tipos de documento).
- **Funcionalidades requeridas:**
  - Flecha indicadora chevron personalizada.
  - Opción placeholder deshabilitada por defecto ("Seleccione empresa", "Tipo de documento").

#### 4. `Textarea` (`components/ui/Textarea.tsx`)
- **Propósito:** Campo de texto multilínea para descripciones largas de puestos o preguntas.
- **Funcionalidades:** Borde púrpura, número de filas configurable, redimensionamiento controlado (`resize: vertical` o `resize: none`).

#### 5. `Checkbox` (`components/ui/Checkbox.tsx`)
- **Propósito:** Casilla de verificación personalizada.
- **Estilo observado en prototipo:** Cuadro redondeado fondo lavanda suave (`#CDCEE6`), tilde '✔' centrada en color púrpura oscuro (`#3F2581`).
- **Props:** `checked`, `onChange`, `label`, `value`, `disabled`.

#### 6. `Badge` / `StatusTag` (`components/ui/Badge.tsx`)
- **Propósito:** Mostrar estados de cuestionarios o entidades.
- **Variantes:**
  - `in-progress` (Verde: "Estado: en proceso")
  - `finished` (Gris oscuro/Neutral: "Estado: finalizado")
  - `expired` (Rojo: "Estado: expirado")

#### 7. `Icon` (`components/ui/Icon.tsx`)
- **Propósito:** Unificador de iconos del sistema (Google Material Symbols / Icons o Lucide React).
- **Iconos identificados:** `menu`, `groups`, `person`, `notifications`, `logout`, `search`, `arrow_back`, `arrow_forward`, `visibility`, `visibility_off`, `edit`, `lock`, `alternate_email`.

---

### 3.2 Nivel 2: Moléculas (`components/molecules`)

#### 8. `NotchedField` (`components/molecules/NotchedField.tsx`)
- **Propósito:** Componente contenedor que replica el estilo exacto de Material Notched Outline visible en `Crear Puesto`, `Evaluar Candidatos` y `Gestionar Puestos`.
- **Estructura visual:**
  - Etiqueta (`<label>`) incrustada en el borde superior con fondo blanco que "corta" el borde púrpura.
  - Indicador de obligatoriedad (`*`).
  - Mensaje de error inferior (`ErrorMessage`).
- **Encapsula:** `Input`, `Select` o `Textarea`.

#### 9. `SegmentedControl` / `AuthRoleTabs` (`components/molecules/SegmentedControl.tsx`)
- **Propósito:** Selector de rol en pantalla de Login (`image.png` e `image2.png`).
- **Opciones:** "Ingreso consultor" vs. "Ingreso candidato".
- **Comportamiento:** Píldora con indicador activo en púrpura intenso y opción inactiva en lavanda desaturado. Emite evento `onTabChange(role)`.

#### 10. `SearchFilterBar` (`components/molecules/SearchFilterBar.tsx`)
- **Propósito:** Barra superior de filtros visible en `Evaluar Candidatos` y `Gestionar Puestos`.
- **Composición:**
  - Grilla responsiva de campos de búsqueda (ej.: Empresa, Nombre, Código, Nro. Candidato).
  - Botón de búsqueda de acción rápida (icono lupa con fondo púrpura).
  - Emisión de evento unificado `onSearch(filters)`.

#### 11. `Stepper` / `WizardProgress` (`components/molecules/Stepper.tsx`)
- **Propósito:** Indicador de avance en flujos guiados (visto en `image3.png` e `image4.png`).
- **Pasos observados:** `[Selección]` ── `[Puesto]` ── `[Finalizar]`.
- **Estados:** `active` (púrpura destacado), `completed` (púrpura suave/marcado), `pending` (gris deshabilitado).

#### 12. `UserProfileBadge` (`components/molecules/UserProfileBadge.tsx`)
- **Propósito:** Mostrar la identidad activa en la barra superior.
- **Composición:** Avatar circular (fondo `#EADDFF`, icono `#4F378A`) + Texto multilínea con Rol ("Consultor:" o "Candidato:") y Nombre de Usuario.

#### 13. `EmptyState` (`components/molecules/EmptyState.tsx`)
- **Propósito:** Placeholder cuando una lista o panel no tiene datos (ej.: "Ninguna seleccionada aún" en competencias seleccionadas).

---

### 3.3 Nivel 3: Organismos (`components/organisms`)

#### 14. `Topbar` / `Header` (`components/organisms/Topbar.tsx`)
- **Propósito:** Barra superior principal persistente.
- **Secciones:**
  - **Izquierda:** Botón hamburguesa (para abrir/cerrar sidebar), Isotipo de grupo (`groups`), Nombre de la empresa ("CONSULTORA CAPIT@L HUMANO").
  - **Derecha:** `UserProfileBadge`, Botón de Notificaciones con indicador de alerta, Botón de Salir (Logout con diálogo de confirmación).

#### 15. `Sidebar` / `NavigationDrawer` (`components/organisms/Sidebar.tsx`)
- **Propósito:** Menú de navegación lateral colapsable (`image3.png` vs `image4.png`).
- **Elementos según Rol:**
  - **Consultor:**
    - Evaluaciones (icono/enlace a `/evaluaciones`)
    - Reportes (enlace a `/reportes`)
    - Preguntas (enlace a `/preguntas`)
    - Orden de Méritos (enlace a `/orden-meritos`)
    - Puestos (enlace a `/puestos`)
  - **Candidato:**
    - Mi cuestionario (enlace a `/cuestionario`)
- **Comportamiento:**
  - Resaltado de ruta activa (`pathname === item.href`).
  - Animación suave de apertura y cierre sobre el contenido.
  - Cierre al hacer click fuera o seleccionar ruta en pantallas móviles/tablets.

#### 16. `DualTransferList` / `TransferBox` (`components/organisms/DualTransferList.tsx`)
- **Propósito:** Componente de selección y asignación en dos cajas (visto en creación de puestos y evaluación de candidatos).
- **Sub-elementos:**
  - **Panel Izquierdo (Disponibles):**
    - Título del panel ("Competencias disponibles" o "Candidatos disponibles").
    - Lista scrollable con checkboxes individuales y opción "seleccionar todos".
    - Botón "Agregar" en la esquina inferior.
  - **Panel Derecho (Seleccionados):**
    - Cabecera con columnas específicas (ej.: "Competencias" y "Ponderaciones (0-10)").
    - Renglón de item con input numérico (0-10) en caso de ponderaciones o botón de eliminación individual.
    - Botón "Quitar" en la esquina inferior.
    - Renderizado de `EmptyState` si la lista está vacía.

#### 17. `DataTable` (`components/organisms/DataTable.tsx`)
- **Propósito:** Tabla de datos para listas extensas (`image7.png` "Gestionar Puestos" y listado de candidatos).
- **Características:**
  - Cabeceras de columnas (`columns: { header, accessor, width }[]`).
  - Selección de fila activa: Fila seleccionada se resalta con fondo lavanda claro (`#EADDFF`).
  - Paginación o scroll virtual si la lista excede el alto del viewport.
  - Indicador de carga (`Skeleton` o `Spinner`).

#### 18. `EvaluationCard` (`components/organisms/EvaluationCard.tsx`)
- **Propósito:** Tarjeta informativa para el portal del candidato (`image5.png`).
- **Estructura interna:**
  - **Izquierda:** Título ("Cuestionario X - Puesto [nombre]"), `Badge` de estado ("en proceso", "finalizado", "expirado").
  - **Centro:** Fechas y horas formateadas ("Iniciado el...", "Última actualización...", "Tiempo restante...").
  - **Derecha:** Botón de acción contextual:
    - Estado en proceso: Botón "Continuar" (variante `primary`).
    - Estado finalizado: Botón "Ver" (variante `secondary`).
    - Estado expirado: Deshabilitado o sin botón.

#### 19. `ActionToolbar` / `FormFooterActions` (`components/organisms/ActionToolbar.tsx`)
- **Propósito:** Barra de acciones al pie de cada pantalla.
- **Casos de uso identificados:**
  - `Crear Puesto`: [Cancelar] | [Aceptar]
  - `Evaluar Candidatos`: [← Anterior] | [Siguiente →]
  - `Gestionar Puestos`: [Eliminar] | [Modificar] | [Nuevo]
- **Props:** `align` ('right' | 'between' | 'center'), `children`.

---

### 3.4 Nivel 4: Layouts y Plantillas (`components/layouts`)

#### 20. `DashboardLayout` (`components/layouts/DashboardLayout.tsx`)
- Contenedor con `Topbar`, `Sidebar` y el `<main class="main-content">`.
- Maneja el estado global del menú lateral (abierto/cerrado) y el contexto del usuario autenticado.

#### 21. `AuthLayout` (`components/layouts/AuthLayout.tsx`)
- Contenedor centrado en pantalla completa con fondo gris suave (`#DDDDDD`), logo centrado superior y tarjeta contenedora blanca con esquinas redondeadas (`border-radius: 20px`) y sombra sutil.

---

## 4. Matriz de Reutilización por Pantalla

| Componente | Login (`image`, `image2`) | Evaluar Candidatos (`image3`, `image4`) | Portal Candidato (`image5`) | Crear Puesto (`image6`, `html`) | Gestionar Puestos (`image7`) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `Button` | ✔ | ✔ | ✔ | ✔ | ✔ |
| `Input` / `NotchedField` | ✔ | ✔ | | ✔ | ✔ |
| `Select` | ✔ | | | ✔ | ✔ |
| `Textarea` | | | | ✔ | |
| `Checkbox` | | ✔ | | ✔ | |
| `Badge` | | | ✔ | | |
| `Topbar` | | ✔ | ✔ | ✔ | ✔ |
| `Sidebar` | | ✔ | ✔ | ✔ | ✔ |
| `Stepper` | | ✔ | | | |
| `DualTransferList` | | ✔ | | ✔ | |
| `DataTable` | | ✔ (lista) | | | ✔ |
| `EvaluationCard` | | | ✔ | | |
| `SearchFilterBar` | | ✔ | | | ✔ |
| `ActionToolbar` | | ✔ | | ✔ | ✔ |

---

## 5. Recomendaciones de Arquitectura y Buenas Prácticas en Next.js

### 5.1 Server Components vs. Client Components
- **Server Components (RSC):**
  - Layouts estructurales (`DashboardLayout`, `AuthLayout`).
  - Obtención de datos iniciales en rutas (lista de puestos, cuestionarios del candidato, listado de empresas).
- **Client Components (`'use client'`):**
  - `DualTransferList`: Maneja estado local de items seleccionados y drag/check.
  - `Sidebar` y `Topbar`: Manejan estado del menú colapsable y dropdowns de sesión.
  - Formularios interactivos (`Crear Puesto`, `Login`): Manejan validaciones con `react-hook-form`.
  - `DataTable`: Maneja la selección activa de fila y eventos de clic.

### 5.2 Estructura de Rutas recomendada (Next.js App Router)
```
app/
├── (auth)/
│   └── login/
│       └── page.tsx           # image.png e image2.png
├── (dashboard)/
│   ├── layout.tsx             # DashboardLayout con Topbar y Sidebar
│   ├── puestos/
│   │   ├── page.tsx           # image7.png (Gestionar Puestos)
│   │   └── nuevo/
│   │       └── page.tsx       # image6.png e index.html (Crear Nuevo Puesto)
│   ├── evaluaciones/
│   │   └── evaluar/
│   │       └── page.tsx       # image3.png e image4.png (Evaluar Candidatos)
│   ├── preguntas/
│   │   └── page.tsx
│   ├── orden-meritos/
│   │   └── page.tsx
│   ├── reportes/
│   │   └── page.tsx
│   └── candidato/
│       └── cuestionario/
│           └── page.tsx       # image5.png (Portal Candidato)
```

### 5.3 Gestión de Formularios y Validación
- Se recomienda usar **`react-hook-form`** con resolvers de **`zod`**.
- En la pantalla `Crear Puesto`, el esquema de validación debe contemplar:
  ```typescript
  const PuestoSchema = z.object({
    empresaId: z.string().min(1, "Debe seleccionar una empresa"),
    nombrePuesto: z.string().min(1, "El nombre del puesto es obligatorio"),
    codigo: z.string().optional(),
    descripcion: z.string().optional(),
    competencias: z.array(
      z.object({
        competenciaId: z.string(),
        ponderacion: z.number().min(0).max(10),
      })
    ).min(1, "Debe seleccionar al menos una competencia"),
  });
  ```

### 5.4 Control de Acceso por Roles (RBAC)
- Implementar Middleware de Next.js (`middleware.ts`) o verificación en sesión (NextAuth / Auth.js / JWT) para restringir el acceso:
  - Rutas de gestión (`/puestos`, `/evaluaciones`, etc.) solo accesibles para rol `CONSULTOR`.
  - Rutas de resolución (`/candidato/*`) solo accesibles para rol `CANDIDATO`.
- La barra de navegación `Sidebar` debe consumir el rol actual para renderizar dinámicamente las opciones correspondientes.
