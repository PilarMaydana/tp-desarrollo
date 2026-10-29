export const ROUTES = {
  HOME: '/',
  DEV: {
    SANDBOX: '/dev/sandbox',
  },
  AUTH: {
    LOGIN: '/prototipos/login',
  },
  CONSULTOR: {
    EVALUACIONES: '/prototipos/evaluaciones/evaluar',
    REPORTES: '/prototipos/reportes',
    PREGUNTAS: '/prototipos/preguntas',
    ORDEN_MERITOS: '/prototipos/orden-meritos',
    PUESTOS: '/prototipos/puestos',
    PUESTOS_NUEVO: '/prototipos/puestos/nuevo',
  },
  CANDIDATO: {
    CUESTIONARIO: '/prototipos/candidato/cuestionario',
  },
} as const;
