export const ROUTES = {
  HOME: '/',
  AUTH: {
    LOGIN: '/login',
  },
  CONSULTOR: {
    EVALUACIONES: '/evaluaciones/evaluar',
    REPORTES: '/reportes',
    PREGUNTAS: '/preguntas',
    ORDEN_MERITOS: '/orden-meritos',
    PUESTOS: '/puestos',
    PUESTOS_NUEVO: '/puestos/nuevo',
  },
  CANDIDATO: {
    CUESTIONARIO: '/candidato/cuestionario',
  },
} as const;
