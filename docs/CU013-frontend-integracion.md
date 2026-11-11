# Especificación e Integración Frontend — CU 013: Dar de alta Competencia

Documento de referencia para implementar el **Frontend del Caso de Uso N.º 013 ("Dar de alta Competencia")** y conectarlo con el endpoint ya disponible en `/api`.

---

## 1. Resumen Funcional (CU 013)

- **Actor:** Consultor
- **Objetivo:** Registrar una nueva competencia evaluable en el sistema.

### Flujo Principal en UI
1. Mostrar el formulario de **Alta de Competencia** con 3 campos obligatorios:
   - **Código** (`input` de texto, máx. 20 caracteres).
   - **Nombre** (`input` de texto, máx. 100 caracteres).
   - **Descripción** (`textarea`, máx. 500 caracteres).
2. Botones al pie (`ActionToolbar`): **[Cancelar]** (secundario gris `#D8D8D8`) y **[Aceptar]** (primario púrpura `#3F2581`).
3. Al presionar **Aceptar**, enviar `POST /api/competencias`.
4. Si responde `201 Created`, mostrar diálogo/modal de confirmación:
   > *"La competencia **{respuesta.nombre}** se ha creado correctamente. ¿Desea cargar otra?"*
   - Botón **[Sí]**: limpia el formulario, cierra el modal y deja el foco en el primer campo para cargar otra competencia.
   - Botón **[No]**: finaliza el caso de uso (navega a la pantalla anterior / listado).

### Flujos Alternativos en UI
- **2.A. Datos obligatorios incompletos o inválidos (`400 Bad Request`):**
  - Mostrar todas las omisiones/errores juntos (uno por cada campo con error), **sin ocultar ni limpiar los campos ni los botones**.
- **2.B. Código o nombre ya existente (`409 Conflict`):**
  - Informar al usuario el mensaje devuelto por el backend (`error.mensaje`) sin perder los datos ingresados en el formulario.

### Requerimientos Especiales
- **Mayúsculas:** Independientemente del estado de `Bloq Mayús`, el ingreso de datos literales (`codigo`, `nombre`, `descripcion`) se realiza siempre en **MAYÚSCULAS** (convertir el valor con `.toUpperCase()` en el `onChange` de los inputs).
- **Accesibilidad por teclado:** `Tab` avanza al siguiente campo, `Shift + Tab` retrocede al campo anterior, y `Enter` o clic sobre un botón tienen el mismo efecto.

---

## 2. Contrato de la API (Backend)

### Configuración de Conexión
- **Base URL local:** `http://localhost:8080`
- **CORS:** Ya habilitado en el backend para `http://localhost:3000` (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`).
- **Autenticación:** Actualmente pública (`permitAll`) hasta que se implemente el CU 001 (Login LDAP).

### Endpoint: Crear Competencia
- **Método:** `POST`
- **Ruta:** `/api/competencias`
- **Headers:**
  ```http
  Content-Type: application/json
  ```

---

## 3. Reglas de Validación y Normalización

| Campo | Tipo | Obligatorio | Largo Máximo | Normalización en Backend | Restricción BD |
| :--- | :--- | :---: | :---: | :--- | :--- |
| `codigo` | `string` | Sí (`@NotBlank`) | `20` caracteres | `trim()` + `UPPERCASE` | **Único** (`UNIQUE`) |
| `nombre` | `string` | Sí (`@NotBlank`) | `100` caracteres | `trim()` + `UPPERCASE` | **Único** (`UNIQUE`) |
| `descripcion` | `string` | Sí (`@NotBlank`) | `500` caracteres | `trim()` + `UPPERCASE` | — |

> **Nota:** Aunque el frontend envíe los textos en mayúsculas, el `GestorCompetencia` en el backend siempre aplica `.trim().toUpperCase()` a los tres campos antes de verificar duplicados y persistir en la base de datos H2.

---

## 4. Ejemplos de Request y Responses

### 4.1. Request (`POST /api/competencias`)
```json
{
  "codigo": "TRB-01",
  "nombre": "TRABAJO EN EQUIPO",
  "descripcion": "CAPACIDAD PARA COLABORAR EFICAZMENTE CON OTRAS PERSONAS."
}
```

### 4.2. Respuesta Exitosa — `201 Created`
Devuelve el `CompetenciaDTO` con el `id` autogenerado y los campos normalizados:
```json
{
  "id": 3,
  "codigo": "TRB-01",
  "nombre": "TRABAJO EN EQUIPO",
  "descripcion": "CAPACIDAD PARA COLABORAR EFICAZMENTE CON OTRAS PERSONAS."
}
```

### 4.3. Error de Validación de Campos — `400 Bad Request`
Cuando faltan campos obligatorios (vacíos, `null` o solo espacios) o superan el largo máximo, el backend devuelve la estructura estándar `ApiError` con `codigo: "VALIDACION"` y **todos** los errores por campo dentro del arreglo `detalles`:

```json
{
  "timestamp": "2026-11-01T21:58:00.000000Z",
  "status": 400,
  "codigo": "VALIDACION",
  "mensaje": "Error de validacion en los campos enviados",
  "path": "/api/competencias",
  "detalles": [
    {
      "campo": "codigo",
      "mensaje": "El código es obligatorio"
    },
    {
      "campo": "nombre",
      "mensaje": "El nombre es obligatorio"
    },
    {
      "campo": "descripcion",
      "mensaje": "La descripción es obligatoria"
    }
  ]
}
```

**Mensajes posibles en `detalles`:**
- `codigo`: `"El código es obligatorio"` | `"El código no puede superar los 20 caracteres"`
- `nombre`: `"El nombre es obligatorio"` | `"El nombre no puede superar los 100 caracteres"`
- `descripcion`: `"La descripción es obligatoria"` | `"La descripción no puede superar los 500 caracteres"`

### 4.4. Error por Código o Nombre Duplicado — `409 Conflict`
Cuando el `codigo` o el `nombre` (una vez normalizados) ya existen en la base de datos, devuelve `409 Conflict` con `codigo: "RECURSO_DUPLICADO"`:

**Ejemplo por código duplicado:**
```json
{
  "timestamp": "2026-11-01T21:58:00.000000Z",
  "status": 409,
  "codigo": "RECURSO_DUPLICADO",
  "mensaje": "Ya existe una competencia con el código 'COM-01'",
  "path": "/api/competencias",
  "detalles": []
}
```

**Ejemplo por nombre duplicado:**
```json
{
  "timestamp": "2026-11-01T21:58:00.000000Z",
  "status": 409,
  "codigo": "RECURSO_DUPLICADO",
  "mensaje": "Ya existe una competencia con el nombre 'LIDERAZGO'",
  "path": "/api/competencias",
  "detalles": []
}
```

---

## 5. Tipos TypeScript Sugeridos (`types/competencia.ts`)

```ts
export interface CompetenciaCreateRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface CompetenciaResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface CampoError {
  campo: 'codigo' | 'nombre' | 'descripcion' | string;
  mensaje: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  codigo: 'VALIDACION' | 'RECURSO_DUPLICADO' | 'REGLA_NEGOCIO' | 'CUERPO_INVALIDO' | 'ERROR_INTERNO' | string;
  mensaje: string;
  path: string;
  detalles: CampoError[];
}
```

---

## 6. Datos Semilla Precargados en Backend (`seed.sql`)

Al levantar el backend (`./mvnw spring-boot:run`), la base de datos H2 en memoria ya inicia con estas **2 competencias cargadas**, listas para probar errores `409 Conflict` desde el frontend:

| ID | Código | Nombre | Descripción |
| :---: | :--- | :--- | :--- |
| `1` | `COM-01` | `COMUNICACIÓN` | `CAPACIDAD PARA TRANSMITIR IDEAS E INFORMACIÓN DE MANERA CLARA Y EFECTIVA.` |
| `2` | `LID-01` | `LIDERAZGO` | `CAPACIDAD PARA GUIAR, MOTIVAR Y DIRIGIR EQUIPOS DE TRABAJO HACIA EL LOGRO DE OBJETIVOS.` |

---

## 7. Organización Sugerida en `/frontend` (Feature Slice)

Siguiendo la [Metodología de Desarrollo E2E](../docs/metodolog_a_de_desarrollo_con_ia.md):

```text
frontend/
 └── features/ (o dentro de src/)
      └── competencia/
           ├── components/
           │    └── AltaCompetenciaForm.tsx
           ├── services/
           │    └── competenciaService.ts
           ├── hooks/
           │    └── useAltaCompetencia.ts
           └── types/
                └── competencia.ts
```

1. **Desarrollo inicial:** Montar el componente en la ruta `/dev/sandbox` para probar el flujo completo contra `http://localhost:8080/api/competencias`.
2. **Integración:** Mover a su ruta definitiva (ej. `/competencias/nueva`) una vez validado el flujo E2E.

