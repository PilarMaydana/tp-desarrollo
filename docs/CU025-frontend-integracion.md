# Especificación de Integración Frontend — CU 025: Evaluar a Candidato

- **Base URL:** `http://localhost:8080/api/evaluaciones`
- **CORS:** Habilitado para `http://localhost:3000` con `Content-Disposition` expuesto.
- **Tipos de datos comunes:** Tipos de documento admitidos (`DNI`, `LE`, `LC`, `PP`).

---

## 1. Tipos TypeScript

```ts
export type TipoDocumento = "DNI" | "LE" | "LC" | "PP";
export type EstadoCuestionario = "ACTIVO" | "EN_PROCESO" | "COMPLETO" | "INCOMPLETO" | "SIN_CONTESTAR";

export interface CandidatoResumenDTO {
  id: number;
  nroCandidato: string;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  email?: string;
}

export interface PuestoResumenDTO {
  id: number;
  codigo: string;
  nombre: string;
  empresa: string;
}

export interface CompetenciaRequeridaDTO {
  competenciaId: number;
  codigo: string;
  nombre: string;
  ponderacionRequerida: number;
}

export interface DetallePuestoEvaluacionDTO {
  puestoId: number;
  codigo: string;
  nombre: string;
  empresa: string;
  competencias: CompetenciaRequeridaDTO[];
}

export interface CandidatoClaveDTO {
  candidatoId: number;
  nroCandidato: string;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  claveAcceso: string;
}

export interface PrevisualizacionEvaluacionDTO {
  puestoId: number;
  puestoCodigo: string;
  puestoNombre: string;
  empresa: string;
  tiempoMaximoMinutos: number;
  cantidadPreguntasPorBloque: number;
  candidatos: CandidatoClaveDTO[];
}

export interface EvaluacionCreadaDTO {
  evaluacionId: number;
  puestoId: number;
  puestoCodigo: string;
  puestoNombre: string;
  empresa: string;
  fechaCreacion: string;
  tiempoMaximoMinutos: number;
  cantidadPreguntasPorBloque: number;
  cuestionarios: Array<{
    cuestionarioId: number;
    candidatoId: number;
    nroCandidato: string;
    apellido: string;
    nombre: string;
    tipoDocumento: TipoDocumento;
    nroDocumento: string;
    claveAcceso: string;
    estado: EstadoCuestionario;
    cantidadBloques: number;
    cantidadPreguntas: number;
  }>;
}

export interface ApiError {
  status: number;
  codigo: string;
  mensaje: string;
  detalles: Array<{ campo: string; mensaje: string }>;
}
```

---

## 2. Endpoints por Paso del Wizard

### Paso 1: Búsqueda de candidatos (Pasos 2-3)
- **Método y URL:** `GET /api/evaluaciones/candidatos?apellido=&nombre=&nroCandidato=`
- **Parámetros de consulta (opcionales):** `apellido`, `nombre`, `nroCandidato` (búsqueda parcial case-insensitive).
- **Respuesta `200 OK`:** `CandidatoResumenDTO[]`.

---

### Paso 2: Validar selección de candidatos (Pasos 4.A, 6 y 6.A)
- **Método y URL:** `POST /api/evaluaciones/validar-candidatos`
- **Body:**
  ```json
  {
    "candidatoIds": [3, 4]
  }
  ```
- **Respuesta `200 OK`:**
  ```json
  {
    "valido": true,
    "cantidadCandidatos": 2,
    "candidatos": [ /* CandidatoResumenDTO[] */ ]
  }
  ```
- **Errores esperados:**
  - `400 Bad Request` (Flujo 4.A): Lista de candidatos vacía.
  - `422 Unprocessable Entity` (Flujo 6.A): Candidatos con cuestionarios en estado `ACTIVO` o `EN_PROCESO`. Los candidatos bloqueados se detallan en `detalles[]`.

---

### Paso 3: Selección de Puesto y Competencias Requeridas (Pasos 8, 9 y 9.A)
- **Listar puestos:**
  - **Método y URL:** `GET /api/evaluaciones/puestos`
  - **Respuesta `200 OK`:** `PuestoResumenDTO[]`.

- **Consultar competencias del puesto seleccionado:**
  - **Método y URL:** `GET /api/evaluaciones/puestos/{puestoId}/competencias`
  - **Respuesta `200 OK`:** `DetallePuestoEvaluacionDTO`.
  - **Errores esperados:**
    - `422 Unprocessable Entity` (Flujo 9.A): El puesto posee competencias sin factores válidos con al menos 2 preguntas. La propiedad `detalles[]` lista los nombres exactos de las competencias no evaluables.

---

### Paso 4: Previsualización de Claves (Paso 11 / Flujo 12.A)
- **Método y URL:** `POST /api/evaluaciones/previsualizar`
- **Body:**
  ```json
  {
    "puestoId": 1,
    "candidatoIds": [3, 4]
  }
  ```
- **Respuesta `200 OK`:** `PrevisualizacionEvaluacionDTO`.
- **Comportamiento:** Genera claves aleatorias en memoria sin persistir en base de datos. Permite mostrar la grilla informativa con las columnas: `Apellido`, `Nombre`, `Tipo documento`, `Nro. documento` y `Clave de ingreso`. Si el usuario pulsa "Cancelar" (Flujo 12.A), se descarta el estado en el frontend sin efectos colaterales.

---

### Paso 5: Confirmar y Guardar Evaluación (Pasos 12-13)
- **Método y URL:** `POST /api/evaluaciones`
- **Body:**
  ```json
  {
    "puestoId": 1,
    "candidatoIds": [3, 4],
    "clavesPorCandidato": {
      "3": "A7K9M2P4",
      "4": "B8L1N3Q5"
    }
  }
  ```
  *(Reutilizar el mapeo de `clavesPorCandidato` recibido en la previsualización para garantizar consistencia).*
- **Respuesta `201 Created`:** `EvaluacionCreadaDTO` (contiene `evaluacionId` generado).

---

### Paso 6: Exportación a Excel `.xls` (Pasos 14-17)
- **Método y URL:** `GET /api/evaluaciones/{evaluacionId}/exportar-excel`
- **Respuesta `200 OK`:** Archivo binario `.xls` (`application/vnd.ms-excel`).
- **Header:** `Content-Disposition: attachment; filename="aaaa-mm-dd-hh-mm-NombreFunción.xls"`
- **Consumo sugerido en frontend:**
  ```ts
  const descargarExcel = async (evaluacionId: number) => {
    const res = await fetch(`http://localhost:8080/api/evaluaciones/${evaluacionId}/exportar-excel`);
    if (!res.ok) throw new Error("Error al descargar el archivo Excel");
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `evaluacion-${evaluacionId}.xls`;
    a.click();
    window.URL.revokeObjectURL(url);
  };
  ```

---

## 3. Reglas de Negocio en la UI del CU 025

1. **Entrada de filtros:** Normalizar visualmente las entradas a MAYÚSCULAS.
2. **Paso 13 (Confirmación exitosa):** Al recibir código `201 Created`, mostrar modal o cartel informativo indicando el éxito y consultando: *«¿Desea exportar la lista de candidatos con sus claves a un archivo Excel?»* con botones **«Sí»** y **«No»**.
3. **Flujo 14.A:** Si pulsa **«No»**, redirigir a la pantalla principal / dashboard.
4. **Flujo 14:** Si pulsa **«Sí»**, disparar la descarga a través del endpoint de exportación.
