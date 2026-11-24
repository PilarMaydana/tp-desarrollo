export const ROUTES = {
  HOME: '/',
  DEV: {
    SANDBOX: '/dev/sandbox',
  },
  EN_DESARROLLO: '/en-desarrollo',
  AUTH: {
    LOGIN: '/',
  },
  CONSULTOR: {
    // TODO: apuntar al listado cuando exista la gestión de evaluaciones
    EVALUACIONES: '/evaluaciones/nueva',
    EVALUACIONES_NUEVA: '/evaluaciones/nueva',
    COMPETENCIAS: '/competencias',
    COMPETENCIAS_NUEVO: '/competencias/nueva',
    COMPETENCIAS_NUEVA: '/competencias/nueva',
    REPORTES: '/en-desarrollo?modulo=Reportes',
    PREGUNTAS: '/preguntas',
    PREGUNTAS_NUEVO: '/preguntas/nuevo',
    PREGUNTAS_EDITAR: (id: number | string) => `/preguntas/${id}/editar`,
    ORDEN_MERITOS: '/orden-merito',
    ORDEN_MERITOS_REPORTE: (puestoId: number | string) => `/orden-merito/${puestoId}/reporte`,
    PUESTOS: '/puestos/nuevo',
    PUESTOS_NUEVO: '/puestos/nuevo',
    FACTORES: '/factores/nuevo',
    FACTORES_NUEVO: '/factores/nuevo',
    OPCIONES_RESPUESTA: '/opciones-respuesta',
    OPCIONES_RESPUESTA_NUEVA: '/opciones-respuesta/nueva',
    CANDIDATOS_IMPORTAR: '/candidatos/importar',
  },
  CANDIDATO: {
    CUESTIONARIO: '/candidato/cuestionario',
    CUESTIONARIO_COMPLETAR: (id: number | string) => `/cuestionario/${id}`,
  },
} as const;
