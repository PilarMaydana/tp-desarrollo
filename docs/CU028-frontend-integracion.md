# Especificación de Integración Frontend — CU 028: Emitir Orden de mérito

- **Base URL:** `http://localhost:8080/api/orden-merito`
- **CORS:** Habilitado para `http://localhost:3000`.
- **Impresión (Pasos 8 y 9):** Resuelta en el frontend con la impresión del navegador (`window.print()` / estilos `@media print`). No existe endpoint de impresión ni PDF.
- **Formato de fechas:** ISO-8601 sin zona horaria (`LocalDateTime`, ej. `"2026-11-23T19:15:00"`).
- **Normalización de entrada:** El frontend debe convertir los filtros de texto a MAYÚSCULAS; el backend igualmente aplica `trim()` + `toUpperCase()` y búsqueda parcial «contiene» sin distinguir mayúsculas/minúsculas.

---

## 1. Tipos TypeScript

```ts
export type TipoDocumento = "DNI" | "LE" | "LC" | "PP";

export interface PuestoOrdenMeritoDTO {
  puestoId: number;
  codigo: string;
  nombre: string;
  empresa: string;
  cantidadCandidatos: number;
  evaluacionesCompletas: number;
  evaluacionesTotal: number;
}

export interface EvaluacionOrdenMeritoDTO {
  evaluacionId: number;
  fechaCreacion: string;
  cantidadCandidatos: number;
  cantidadCompletos: number;
}

export interface PuestoReporteDTO {
  puestoId: number;
  codigo: string;
  nombre: string;
  empresa: string;
}

export interface EvaluacionIncluidaDTO {
  evaluacionId: number;
  fechaCreacion: string;
}

export interface CompetenciaRequeridaReporteDTO {
  competenciaId: number;
  codigo: string;
  nombre: string;
  ponderacionRequerida: number;
}

/**
 * Observación (2) del CU 028:
 * - Cuestionarios COMPLETO: informa fechaInicio, fechaFin y cantidadAccesos; ultimoIngreso = null.
 * - Cuestionarios NO completos (incompletos, enProceso, sinContestar, activos):
 *   informa fechaInicio, ultimoIngreso (ambos pueden ser null si nunca se inició) y cantidadAccesos;
 *   fechaFin = null.
 */
export interface DatosAdicionalesDTO {
  fechaInicio: string | null;
  fechaFin: string | null;
  ultimoIngreso: string | null;
  cantidadAccesos: number;
}

export interface CompetenciaNoAlcanzadaDTO {
  competenciaId: number;
  codigo: string;
  competencia: string;
  obtenido: number | null;
  requerido: number;
}

export interface FilaOrdenMeritoDTO {
  posicion: number;
  cuestionarioId: number;
  evaluacionId: number;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: number;
  datosAdicionales: DatosAdicionalesDTO;
}

export interface FilaNoAlcanzoMinimoDTO {
  cuestionarioId: number;
  evaluacionId: number;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: number | null;
  datosAdicionales: DatosAdicionalesDTO;
  competenciasNoAlcanzadas: CompetenciaNoAlcanzadaDTO[];
}

/**
 * Observación (1) del CU 028:
 * En cuestionarios no completos, puntajeObtenido siempre es null y NO debe mostrarse.
 */
export interface FilaCandidatoReporteDTO {
  cuestionarioId: number;
  evaluacionId: number;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: null;
  datosAdicionales: DatosAdicionalesDTO;
}

export interface TotalesReporteDTO {
  ordenDeMerito: number;
  noAlcanzaronMinimos: number;
  incompletos: number;
  enProceso: number;
  sinContestar: number;
  activos: number;
  totalGeneral: number;
}

export interface ReporteOrdenMeritoDTO {
  puesto: PuestoReporteDTO;
  evaluacionesIncluidas: EvaluacionIncluidaDTO[];
  fechaEmision: string;
  competenciasRequeridas: CompetenciaRequeridaReporteDTO[];
  ordenDeMerito: FilaOrdenMeritoDTO[];
  noAlcanzaronMinimos: FilaNoAlcanzoMinimoDTO[];
  incompletos: FilaCandidatoReporteDTO[];
  enProceso: FilaCandidatoReporteDTO[];
  sinContestar: FilaCandidatoReporteDTO[];
  activos: FilaCandidatoReporteDTO[];
  totales: TotalesReporteDTO;
}

export interface ApiError {
  timestamp: string;
  status: number;
  codigo: "VALIDACION" | "RECURSO_NO_ENCONTRADO" | "REGLA_NEGOCIO" | string;
  mensaje: string;
  path: string;
  detalles: Array<{ campo: string; mensaje: string }>;
}
```

---

## 2. Contrato de Endpoints

### 2.1. Buscar puestos (Pasos 2 a 4)

- **Método y URL:** `GET /api/orden-merito/puestos`
- **Query params** (al menos uno obligatorio; criterio «contiene», case-insensitive):
  - `codigo` (opcional, string)
  - `nombre` (opcional, string)
  - `empresa` (opcional, string)
- **Respuesta `200 OK`:** `PuestoOrdenMeritoDTO[]` ordenada por `nombre` ascendente (lista vacía `[]` si no hay coincidencias; no es error).

```json
[
  {
    "puestoId": 4,
    "codigo": "ING-DATA-01",
    "nombre": "INGENIERO DE DATOS",
    "empresa": "DATA CORP SA",
    "cantidadCandidatos": 10,
    "evaluacionesCompletas": 1,
    "evaluacionesTotal": 2
  },
  {
    "puestoId": 5,
    "codigo": "ING-CLOUD-01",
    "nombre": "INGENIERO DE PLATAFORMA CLOUD",
    "empresa": "NUBE SUR SRL",
    "cantidadCandidatos": 0,
    "evaluacionesCompletas": 0,
    "evaluacionesTotal": 0
  }
]
```

> **Tip UI:** En la columna "Evaluaciones Completas" se puede renderizar `${p.evaluacionesCompletas} de ${p.evaluacionesTotal}`.

- **Errores posibles:**
  - `400 Bad Request` (`codigo: "VALIDACION"`): los tres criterios están ausentes o vacíos.

```json
{
  "timestamp": "2026-11-23T22:00:00Z",
  "status": 400,
  "codigo": "VALIDACION",
  "mensaje": "Debe ingresar al menos un criterio de búsqueda (código, nombre o empresa).",
  "path": "/api/orden-merito/puestos",
  "detalles": [
    {
      "campo": "criterios",
      "mensaje": "Al menos uno de los parámetros codigo, nombre o empresa debe estar informado."
    }
  ]
}
```

---

### 2.2. Listar evaluaciones de un puesto (Paso 6 y Flujo 6.A)

- **Método y URL:** `GET /api/orden-merito/puestos/{puestoId}/evaluaciones`
- **Path param:** `puestoId` (number)
- **Respuesta `200 OK`:** `EvaluacionOrdenMeritoDTO[]` ordenada de más reciente a más antigua. La opción `«TODAS»` la agrega el frontend.

```json
[
  {
    "evaluacionId": 4,
    "fechaCreacion": "2026-11-21T19:00:00",
    "cantidadCandidatos": 7,
    "cantidadCompletos": 3
  },
  {
    "evaluacionId": 3,
    "fechaCreacion": "2026-11-13T19:00:00",
    "cantidadCandidatos": 3,
    "cantidadCompletos": 3
  }
]
```

- **Errores posibles:**
  - `404 Not Found` (`codigo: "RECURSO_NO_ENCONTRADO"`): el puesto no existe o está eliminado.
  - `422 Unprocessable Entity` (`codigo: "REGLA_NEGOCIO"`): **Flujo 6.A** — el puesto no posee evaluaciones asociadas. El frontend debe mostrar el mensaje y volver al paso 4 (grilla de puestos).

```json
{
  "timestamp": "2026-11-23T22:00:00Z",
  "status": 422,
  "codigo": "REGLA_NEGOCIO",
  "mensaje": "El puesto seleccionado no posee evaluaciones asociadas.",
  "path": "/api/orden-merito/puestos/5/evaluaciones",
  "detalles": []
}
```

---

### 2.3. Emitir reporte de orden de mérito (Paso 7 y Flujo 6.A)

- **Método y URL:** `GET /api/orden-merito/puestos/{puestoId}`
- **Path param:** `puestoId` (number)
- **Query param opcional:** `evaluacionIds` (lista separada por comas, ej. `?evaluacionIds=3,4` o `?evaluacionIds=3`).
  - Si el actor eligió **«TODAS»**, omitir el parámetro (`GET /api/orden-merito/puestos/4`) o enviarlo vacío.
- **Respuesta `200 OK`:** `ReporteOrdenMeritoDTO`.

```json
{
  "puesto": {
    "puestoId": 4,
    "codigo": "ING-DATA-01",
    "nombre": "INGENIERO DE DATOS",
    "empresa": "DATA CORP SA"
  },
  "evaluacionesIncluidas": [
    { "evaluacionId": 4, "fechaCreacion": "2026-11-21T19:00:00" },
    { "evaluacionId": 3, "fechaCreacion": "2026-11-13T19:00:00" }
  ],
  "fechaEmision": "2026-11-23T19:00:00",
  "competenciasRequeridas": [
    { "competenciaId": 5, "codigo": "TEC-01", "nombre": "COMPETENCIAS TÉCNICAS", "ponderacionRequerida": 8.0 },
    { "competenciaId": 1, "codigo": "COM-01", "nombre": "COMUNICACIÓN", "ponderacionRequerida": 7.0 }
  ],
  "ordenDeMerito": [
    {
      "posicion": 1,
      "cuestionarioId": 13,
      "evaluacionId": 3,
      "tipoDocumento": "DNI",
      "nroDocumento": "35100001",
      "apellido": "ALVAREZ",
      "nombre": "VALENTINA",
      "puntajeObtenido": 9.50,
      "datosAdicionales": {
        "fechaInicio": "2026-11-14T19:00:00",
        "fechaFin": "2026-11-14T19:35:00",
        "ultimoIngreso": null,
        "cantidadAccesos": 1
      }
    }
  ],
  "noAlcanzaronMinimos": [
    {
      "cuestionarioId": 17,
      "evaluacionId": 4,
      "tipoDocumento": "DNI",
      "nroDocumento": "35100005",
      "apellido": "ESPINOZA",
      "nombre": "TOMAS",
      "puntajeObtenido": 7.50,
      "datosAdicionales": {
        "fechaInicio": "2026-11-22T05:00:00",
        "fechaFin": "2026-11-22T05:42:00",
        "ultimoIngreso": null,
        "cantidadAccesos": 2
      },
      "competenciasNoAlcanzadas": [
        {
          "competenciaId": 5,
          "codigo": "TEC-01",
          "competencia": "COMPETENCIAS TÉCNICAS",
          "obtenido": 6.50,
          "requerido": 8.0
        }
      ]
    }
  ],
  "incompletos": [
    {
      "cuestionarioId": 19,
      "evaluacionId": 4,
      "tipoDocumento": "DNI",
      "nroDocumento": "35100007",
      "apellido": "GARCIA",
      "nombre": "NICOLAS",
      "puntajeObtenido": null,
      "datosAdicionales": {
        "fechaInicio": "2026-11-22T07:00:00",
        "fechaFin": null,
        "ultimoIngreso": "2026-11-22T08:00:00",
        "cantidadAccesos": 2
      }
    }
  ],
  "enProceso": [
    {
      "cuestionarioId": 20,
      "evaluacionId": 4,
      "tipoDocumento": "PP",
      "nroDocumento": "AR800008",
      "apellido": "HERRERA",
      "nombre": "FLORENCIA",
      "puntajeObtenido": null,
      "datosAdicionales": {
        "fechaInicio": "2026-11-23T18:40:00",
        "fechaFin": null,
        "ultimoIngreso": "2026-11-23T18:55:00",
        "cantidadAccesos": 1
      }
    }
  ],
  "sinContestar": [
    {
      "cuestionarioId": 21,
      "evaluacionId": 4,
      "tipoDocumento": "DNI",
      "nroDocumento": "35100009",
      "apellido": "IBARRA",
      "nombre": "MATEO",
      "puntajeObtenido": null,
      "datosAdicionales": {
        "fechaInicio": null,
        "fechaFin": null,
        "ultimoIngreso": null,
        "cantidadAccesos": 0
      }
    }
  ],
  "activos": [
    {
      "cuestionarioId": 22,
      "evaluacionId": 4,
      "tipoDocumento": "DNI",
      "nroDocumento": "35100010",
      "apellido": "JUAREZ",
      "nombre": "AGUSTINA",
      "puntajeObtenido": null,
      "datosAdicionales": {
        "fechaInicio": null,
        "fechaFin": null,
        "ultimoIngreso": null,
        "cantidadAccesos": 0
      }
    }
  ],
  "totales": {
    "ordenDeMerito": 4,
    "noAlcanzaronMinimos": 2,
    "incompletos": 1,
    "enProceso": 1,
    "sinContestar": 1,
    "activos": 1,
    "totalGeneral": 10
  }
}
```

- **Errores posibles:**
  - `404 Not Found` (`codigo: "RECURSO_NO_ENCONTRADO"`): el puesto no existe.
  - `422 Unprocessable Entity` (`codigo: "REGLA_NEGOCIO"`):
    - El puesto no tiene evaluaciones (Flujo 6.A).
    - Alguno de los `evaluacionIds` indicados no pertenece al puesto (los IDs inválidos se informan en `detalles[]`).

---

## 3. Reglas de Visualización en la UI

1. **Observación (1) — Puntaje Obtenido:** Mostrar la columna `Puntaje Obtenido` únicamente para `ordenDeMerito` y `noAlcanzaronMinimos`. Para `incompletos`, `enProceso`, `sinContestar` y `activos`, ocultar el valor o mostrar `"—"` (`puntajeObtenido` llega siempre como `null`).
2. **Observación (2) — Datos Adicionales:**
   - Para `ordenDeMerito` y `noAlcanzaronMinimos` (COMPLETOS): mostrar `Fecha de Inicio`, `Fecha de Fin` y `Cantidad de Accesos`.
   - Para `incompletos`, `enProceso`, `sinContestar` y `activos` (NO COMPLETOS): mostrar `Fecha de Inicio`, `Último Ingreso` (con `"—"` si son `null`) y `Cantidad de Accesos`.
3. **Clave de filas (`key` en React):** Usar `cuestionarioId` (no `nroDocumento`), ya que un mismo candidato puede aparecer más de una vez si fue evaluado en dos evaluaciones seleccionadas.
4. **Requerimientos especiales de teclado:** `Shift+Tab` retrocede campo; clic y `Enter` ejecutan el botón enfocado; ingreso de criterios literales siempre en MAYÚSCULAS.

---

## 4. IDs del `seed.sql` para Probar Cada Caso

| Caso de prueba | Parámetros / IDs en `seed.sql` | Resultado esperado |
|---|---|---|
| Búsqueda «contiene» con 2 puestos y empresas distintas | `GET /puestos?nombre=ingeniero` | Puesto 4 (`INGENIERO DE DATOS`, `DATA CORP SA`, `10` cand, `1 de 2` evals completas) y Puesto 5 (`INGENIERO DE PLATAFORMA CLOUD`, `NUBE SUR SRL`, `0` cand, `0 de 0`) |
| Búsqueda combinada con empresa | `GET /puestos?nombre=ingeniero&empresa=data` | Solo Puesto 4 (`ING-DATA-01`) |
| Búsqueda sin coincidencias | `GET /puestos?codigo=ZZZZ` | `200 OK` con `[]` |
| Búsqueda sin criterios | `GET /puestos` | `400 VALIDACION` |
| Evaluaciones de Puesto A (2 evaluaciones) | `GET /puestos/4/evaluaciones` | `[eval 4 (7 cand, 3 comp), eval 3 (3 cand, 3 comp)]` |
| Flujo 6.A (Puesto B sin evaluaciones) | `GET /puestos/5/evaluaciones` | `422 REGLA_NEGOCIO` |
| Reporte Puesto A — TODAS las evaluaciones | `GET /puestos/4` | 10 filas totales: 4 en `ordenDeMerito` (ALVAREZ 9.50, desempate BENITEZ/CASTRO 8.50, borde exacto `>=` DIAZ 7.50), 2 en `noAlcanzaronMinimos` (ESPINOZA falla TEC-01, FLORES falla ambas), 1 `incompletos` (GARCIA), 1 `enProceso` (HERRERA), 1 `sinContestar` (IBARRA), 1 `activos` (JUAREZ) |
| Reporte Puesto A — solo evaluación completa (eval 3) | `GET /puestos/4?evaluacionIds=3` | 3 filas en `ordenDeMerito` (ALVAREZ, BENITEZ, CASTRO); resto de grupos vacíos |
| Reporte Puesto A — solo evaluación mixta (eval 4) | `GET /puestos/4?evaluacionIds=4` | 7 filas: DIAZ (orden), ESPINOZA + FLORES (no alcanzaron), GARCIA, HERRERA, IBARRA, JUAREZ |
| Evaluación ajena al puesto | `GET /puestos/4?evaluacionIds=1` | `422 REGLA_NEGOCIO` (la evaluación 1 pertenece al puesto 1) |
| Puesto inexistente | `GET /puestos/999` o `GET /puestos/999/evaluaciones` | `404 RECURSO_NO_ENCONTRADO` |
