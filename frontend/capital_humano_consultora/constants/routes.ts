export const ROUTES = {
  HOME: '/',
  DEV: {
    SANDBOX: '/dev/sandbox',
  },
  EN_DESARROLLO: '/en-desarrollo',
  AUTH: {
    LOGIN: '/en-desarrollo?modulo=Inicio+de+Sesión',
  },
  CONSULTOR: {
    EVALUACIONES: '/en-desarrollo?modulo=Evaluación+de+Candidatos',
    COMPETENCIAS: '/competencias',
    COMPETENCIAS_NUEVO: '/competencias/nueva',
    COMPETENCIAS_NUEVA: '/competencias/nueva',
    REPORTES: '/en-desarrollo?modulo=Reportes',
    PREGUNTAS: '/preguntas',
    PREGUNTAS_NUEVO: '/preguntas/nuevo',
    ORDEN_MERITOS: '/en-desarrollo?modulo=Orden+de+Méritos',
    PUESTOS: '/puestos/nuevo',
    PUESTOS_NUEVO: '/puestos/nuevo',
    FACTORES: '/factores/nuevo',
    FACTORES_NUEVO: '/factores/nuevo',
  },
  CANDIDATO: {
    CUESTIONARIO: '/en-desarrollo?modulo=Cuestionario+del+Candidato',
  },
} as const;
