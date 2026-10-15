# Consultora Capit@l Humano - Frontend Next.js

Arquitectura Frontend desarrollada en **Next.js (App Router)** y **TypeScript**, basada fielmente en la especificación de componentes y sistema de diseño de la consultora.

---

## 🏛️ Estructura y Capas de la Arquitectura

```
frontend/capital_humano_consultora/
├── app/                               # Next.js App Router (Páginas y Layouts)
│   ├── (auth)/                        # Grupo de rutas de autenticación
│   │   └── login/page.tsx             # Pantalla de Login (Consultor vs Candidato)
│   ├── (dashboard)/                   # Grupo de rutas protegidas bajo DashboardLayout
│   │   ├── layout.tsx                 # Envoltura estructural con Topbar y Sidebar
│   │   ├── puestos/
│   │   │   ├── page.tsx               # image7.png: Gestionar Puestos (Búsqueda + DataTable)
│   │   │   └── nuevo/page.tsx         # image6.png: Crear Nuevo Puesto (Notched fields + DualTransfer)
│   │   ├── evaluaciones/
│   │   │   └── evaluar/page.tsx       # image3/4: Evaluar Candidatos (Wizard/Stepper + Transfer)
│   │   ├── candidato/
│   │   │   └── cuestionario/page.tsx  # image5.png: Portal del Candidato (EvaluationCards)
│   │   ├── preguntas/page.tsx         # Gestión de reactivos y preguntas
│   │   ├── orden-meritos/page.tsx     # Ranking y orden de méritos
│   │   └── reportes/page.tsx          # Exportación de informes ejecutivos
│   ├── globals.css                    # Design Tokens CSS y directivas Tailwind
│   ├── layout.tsx                     # RootLayout con tipografía Roboto y AuthProvider
│   └── page.tsx                       # Landing con redirección inteligente según sesión
│
├── components/                        # Catálogo de Componentes Atómicos
│   ├── ui/                            # Nivel 1: Primitivas / Átomos
│   │   ├── Button/                    # Variantes primary, secondary, secondary-soft, icon
│   │   ├── Input/                     # Inputs con iconos, toggle password y soporte react-hook-form
│   │   ├── Select/                    # Selector desplegable con chevron estilizado
│   │   ├── Textarea/                  # Área de texto multilínea con borde focus
│   │   ├── Checkbox/                  # Casilla personalizada con fondo lavanda (#CDCEE6)
│   │   ├── Badge/                     # Etiquetas de estado (in-progress, finished, expired)
│   │   ├── Icon/                      # Mapeo unificado a iconos Lucide
│   │   └── Divider/                   # Separadores horizontales y verticales
│   │
│   ├── molecules/                     # Nivel 2: Moléculas funcionales
│   │   ├── NotchedField/              # Material Notched Outline con etiqueta que corta el borde
│   │   ├── SegmentedControl/          # Píldora de selección de rol (Consultor vs Candidato)
│   │   ├── SearchFilterBar/           # Barra adaptable con filtros y botón buscar
│   │   ├── Stepper/                   # Indicador de pasos guiados (Wizard)
│   │   ├── UserProfileBadge/          # Avatar circular (#EADDFF) y datos de usuario en Topbar
│   │   └── EmptyState/                # Placeholder para listas vacías
│   │
│   ├── organisms/                     # Nivel 3: Organismos de negocio
│   │   ├── Topbar/                    # Barra superior con marca, usuario, notificaciones y logout
│   │   ├── Sidebar/                   # Drawer colapsable con RBAC dinámico
│   │   ├── DualTransferList/          # Dos cajas de asignación con ponderaciones (0-10)
│   │   ├── DataTable/                 # Tabla interactiva con selección activa (#EADDFF)
│   │   ├── EvaluationCard/            # Tarjeta de cuestionario para el portal del candidato
│   │   └── ActionToolbar/             # Barra de acciones al pie (Aceptar, Cancelar, CRUD)
│   │
│   └── layouts/                       # Nivel 4: Plantillas de Página
│       ├── DashboardLayout/           # Layout Full-Width (Pantalla completa con Topbar y Sidebar)
│       └── AuthLayout/                # Tarjeta centrada para Login
│
├── context/                           # Estado Global de React
│   ├── AuthContext.tsx                # Sesión, usuario activo, cambio rápido de rol
│   └── SidebarContext.tsx             # Apertura y cierre del drawer lateral
│
├── hooks/                             # Custom Hooks
│   ├── useAuth.ts                     # Consumo de sesión y rol
│   ├── useSidebar.ts                  # Control del drawer
│   └── usePuestos.ts                  # Carga, filtrado y eliminación de puestos
│
├── types/                             # Modelos y DTOs de TypeScript
│   ├── auth.ts                        # Roles, usuarios y credenciales
│   ├── puesto.ts                      # Puestos, empresas y filtros
│   ├── competencia.ts                 # Competencias y ponderaciones (0 a 10)
│   ├── candidato.ts                   # Postulantes y filtros
│   └── evaluacion.ts                  # Estados y cuestionarios
│
├── schemas/                           # Validación de Formularios con Zod
│   ├── auth.schema.ts                 # Validaciones de login
│   └── puesto.schema.ts               # Validación de puesto y competencias ponderadas (min 1, 0-10)
│
├── services/                          # Capa de Servicios y Conexión Backend
│   ├── apiClient.ts                   # Fetch genérico con token Bearer y manejo de errores
│   ├── authService.ts                 # Login/logout e integración con Spring Boot (o fallback mock)
│   ├── puestoService.ts               # CRUD de puestos y consulta de empresas/competencias
│   ├── candidatoService.ts            # Búsqueda de postulantes
│   └── evaluacionService.ts           # Consulta de cuestionarios del candidato
│
└── constants/                         # Constantes del Sistema
    ├── tokens.ts                      # Valores Hex y tipografía del Design System
    ├── routes.ts                      # Mapeo de rutas de la aplicación
    └── navigation.ts                  # Enlaces del Sidebar filtrados por rol
```

---

## 🎨 Design Tokens

- **Primario Corporativo:** `#3F2581`
- **Primario Claro / Acentos:** `#896ECB`
- **Fondo Contenedor Suave:** `#F4F0FF` / `#EADDFF`
- **Header Topbar:** `#3B3B3B`
- **Sidebar Drawer:** `#2E2E2E`
- **Fondo Canvas Figma:** `#DDDDDD`
- **Botones Estándar:** `#D8D8D8` (Hover `#C0C0C0`)
- **Botones Secundarios Soft:** `#CDCEE6` (Hover `#B8B9DA`)
- **Estados:**
  - `in-progress` (Verde): `#1B8354`
  - `finished` (Neutral): `#4A4A4A`
  - `expired` (Rojo): `#D32F2F`

---

## 🚀 Cómo ejecutar el proyecto

1. **Instalar dependencias:**
   ```bash
   npm install
   ```

2. **Ejecutar servidor de desarrollo:**
   ```bash
   npm run dev
   ```

3. **Compilar para producción:**
   ```bash
   npm run build
   ```

4. **Verificación de linter y tipado:**
   ```bash
   npm run lint
   npx tsc --noEmit
   ```
