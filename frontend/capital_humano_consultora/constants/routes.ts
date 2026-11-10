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
    REPORTES: '/en-desarrollo?modulo=Reportes',
    PREGUNTAS: '/en-desarrollo?modulo=Gestión+de+Preguntas',
    ORDEN_MERITOS: '/en-desarrollo?modulo=Orden+de+Méritos',
    PUESTOS: '/puestos/nuevo',
    PUESTOS_NUEVO: '/puestos/nuevo',
  },
  CANDIDATO: {
    CUESTIONARIO: '/en-desarrollo?modulo=Cuestionario+del+Candidato',
  },
} as const;
