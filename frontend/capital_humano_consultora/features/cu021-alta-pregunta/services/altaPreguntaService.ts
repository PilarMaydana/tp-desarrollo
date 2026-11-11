import { apiFetch, ApiError } from '@/services/apiClient';
import {
  AltaPreguntaRequest,
  AltaPreguntaResponse,
  BackendApiErrorData,
  CompetenciaItem,
  FactorItem,
  PlantillaOpcionRespuesta,
} from '../types';

export const PLANTILLAS_OPCIONES_RESPUESTA: PlantillaOpcionRespuesta[] = [
  {
    id: 'FREQ-3',
    nombre: 'ESCALA DE FRECUENCIA TRIPLE (NUNCA / A VECES / SIEMPRE)',
    descripcion: 'Escala estándar de 3 niveles de frecuencia.',
    opciones: [
      {
        orden: 1,
        nombre: 'NUNCA',
        descripcion: 'NUNCA TRANSMITE CON CLARIDAD',
        ponderacion: 0.0,
      },
      {
        orden: 2,
        nombre: 'A VECES',
        descripcion: 'TRANSMITE CON CLARIDAD EN OCASIONES',
        ponderacion: 4.0,
      },
      {
        orden: 3,
        nombre: 'SIEMPRE',
        descripcion: 'SIEMPRE TRANSMITE CON RESPETO Y CLARIDAD',
        ponderacion: 6.0,
      },
    ],
  },
  {
    id: 'FREQ-5',
    nombre: 'ESCALA DE FRECUENCIA AMPLIA (5 NIVELES)',
    descripcion: 'Escala graduada de 5 niveles para evaluación detallada.',
    opciones: [
      {
        orden: 1,
        nombre: 'NUNCA',
        descripcion: 'NO MANIFIESTA EL COMPORTAMIENTO ESPERADO',
        ponderacion: 0.0,
      },
      {
        orden: 2,
        nombre: 'RARA VEZ',
        descripcion: 'MANIFIESTA EL COMPORTAMIENTO EN FORMA EXCEPCIONAL',
        ponderacion: 1.0,
      },
      {
        orden: 3,
        nombre: 'A VECES',
        descripcion: 'MANIFIESTA EL COMPORTAMIENTO OCASIONALMENTE',
        ponderacion: 2.5,
      },
      {
        orden: 4,
        nombre: 'FRECUENTEMENTE',
        descripcion: 'MANIFIESTA EL COMPORTAMIENTO DE MANERA REGULAR',
        ponderacion: 3.0,
      },
      {
        orden: 5,
        nombre: 'SIEMPRE',
        descripcion: 'MANIFIESTA EL COMPORTAMIENTO DE FORMA CONSTANTE Y EJEMPLAR',
        ponderacion: 3.5,
      },
    ],
  },
  {
    id: 'LIKERT-5',
    nombre: 'ESCALA LIKERT DE ACUERDO (5 NIVELES)',
    descripcion: 'Medición psicométrica de concordancia con la afirmación.',
    opciones: [
      {
        orden: 1,
        nombre: 'TOTALMENTE EN DESACUERDO',
        descripcion: 'DISCREPA EN SU TOTALIDAD CON LA CONDUCTA DESCRIPTA',
        ponderacion: 0.0,
      },
      {
        orden: 2,
        nombre: 'EN DESACUERDO',
        descripcion: 'DISCREPA EN GRAN MEDIDA CON LA CONDUCTA',
        ponderacion: 1.0,
      },
      {
        orden: 3,
        nombre: 'NEUTRAL',
        descripcion: 'POSICIÓN INTERMEDIA O NEUTRA ANTE LA SITUACIÓN',
        ponderacion: 2.0,
      },
      {
        orden: 4,
        nombre: 'DE ACUERDO',
        descripcion: 'CONCUERDA CON LA APLICACIÓN DE LA CONDUCTA',
        ponderacion: 3.0,
      },
      {
        orden: 5,
        nombre: 'TOTALMENTE DE ACUERDO',
        descripcion: 'CONCUERDA DE MANERA COMPLETA Y CONVICCIÓN TOTAL',
        ponderacion: 4.0,
      },
    ],
  },
  {
    id: 'DICOTOMICA-2',
    nombre: 'ESCALA DICOTÓMICA (NO / SÍ)',
    descripcion: 'Evaluación binaria de presencia o ausencia de conducta.',
    opciones: [
      {
        orden: 1,
        nombre: 'NO',
        descripcion: 'NO CUMPLE O NO EVIDENCIA EL COMPORTAMIENTO REQUERIDO',
        ponderacion: 0.0,
      },
      {
        orden: 2,
        nombre: 'SÍ',
        descripcion: 'CUMPLE PLENAMENTE CON EL COMPORTAMIENTO REQUERIDO',
        ponderacion: 10.0,
      },
    ],
  },
  {
    id: 'CALIDAD-4',
    nombre: 'ESCALA DE DESEMPEÑO (4 NIVELES)',
    descripcion: 'Gradación por nivel de competencia profesional.',
    opciones: [
      {
        orden: 1,
        nombre: 'INSUFICIENTE',
        descripcion: 'DESEMPEÑO DEFICITARIO RESPECTO A LOS ESTÁNDARES',
        ponderacion: 0.0,
      },
      {
        orden: 2,
        nombre: 'EN DESARROLLO',
        descripcion: 'ALCANZA PARCIALMENTE LOS CRITERIOS ESPERADOS',
        ponderacion: 2.0,
      },
      {
        orden: 3,
        nombre: 'COMPETENTE',
        descripcion: 'CUMPLE CABALMENTE CON LAS COMPETENCIAS ASOCIADAS',
        ponderacion: 3.5,
      },
      {
        orden: 4,
        nombre: 'DESTACADO',
        descripcion: 'SUPERA AMPLIAMENTE LOS ESTÁNDARES DE LA ORGANIZACIÓN',
        ponderacion: 4.5,
      },
    ],
  },
  {
    id: 'PERSONALIZADA',
    nombre: 'ESCALA PERSONALIZADA (DEFINIR OPCIONES)',
    descripcion: 'Permite configurar nombres, descripciones y ponderaciones a medida.',
    opciones: [
      {
        orden: 1,
        nombre: 'OPCIÓN 1',
        descripcion: 'DESCRIPCIÓN DE LA PRIMERA OPCIÓN',
        ponderacion: 0.0,
      },
      {
        orden: 2,
        nombre: 'OPCIÓN 2',
        descripcion: 'DESCRIPCIÓN DE LA SEGUNDA OPCIÓN',
        ponderacion: 10.0,
      },
    ],
  },
];

const MOCK_COMPETENCIAS_FALLBACK: CompetenciaItem[] = [
  {
    id: 1,
    codigo: 'COM-01',
    nombre: 'COMUNICACIÓN',
    descripcion: 'CAPACIDAD PARA TRANSMITIR IDEAS E INFORMACIÓN DE MANERA CLARA Y EFECTIVA.',
  },
  {
    id: 2,
    codigo: 'LID-01',
    nombre: 'LIDERAZGO',
    descripcion: 'CAPACIDAD PARA GUIAR, MOTIVAR Y DIRIGIR EQUIPOS DE TRABAJO HACIA EL LOGRO DE OBJETIVOS.',
  },
  {
    id: 3,
    codigo: 'TRAB-01',
    nombre: 'TRABAJO EN EQUIPO',
    descripcion: 'CAPACIDAD PARA COLABORAR DE MANERA EFICAZ CON OTRAS PERSONAS PARA EL LOGRO DE METAS COMUNES.',
  },
  {
    id: 4,
    codigo: 'RESOL-01',
    nombre: 'RESOLUCIÓN DE PROBLEMAS',
    descripcion: 'CAPACIDAD PARA IDENTIFICAR PROBLEMAS COMPLEJOS Y EVALUAR OPCIONES DE SOLUCIÓN.',
  },
];

const MOCK_FACTORES_FALLBACK: Record<string, FactorItem[]> = {
  'COM-01': [
    {
      id: 1,
      codigo: 'FAC-01',
      nombre: 'COMUNICACIÓN ORAL',
      descripcion: 'CAPACIDAD PARA TRANSMITIR INFORMACIÓN DE MANERA VERBAL CLARA.',
      nroOrden: 1,
      codigoCompetencia: 'COM-01',
    },
    {
      id: 2,
      codigo: 'FAC-02',
      nombre: 'COMUNICACIÓN ESCRITA',
      descripcion: 'CAPACIDAD PARA REDACTAR DOCUMENTOS DE MANERA ESTRUCTURADA.',
      nroOrden: 2,
      codigoCompetencia: 'COM-01',
    },
  ],
  'LID-01': [
    {
      id: 3,
      codigo: 'FAC-03',
      nombre: 'TOMA DE DECISIONES',
      descripcion: 'CAPACIDAD PARA ADOPTAR DECISIONES FIRMES Y OPORTUNAS.',
      nroOrden: 1,
      codigoCompetencia: 'LID-01',
    },
    {
      id: 4,
      codigo: 'FAC-04',
      nombre: 'DELEGACIÓN EFECTIVA',
      descripcion: 'CAPACIDAD PARA DISTRIBUIR RESPONSABILIDADES CON CRITERIO.',
      nroOrden: 2,
      codigoCompetencia: 'LID-01',
    },
  ],
  'TRAB-01': [
    {
      id: 5,
      codigo: 'FAC-05',
      nombre: 'COOPERACIÓN INTERPERSONAL',
      descripcion: 'CAPACIDAD PARA COORDINAR ESFUERZOS Y APOYAR A COMPAÑEROS.',
      nroOrden: 1,
      codigoCompetencia: 'TRAB-01',
    },
  ],
  'RESOL-01': [
    {
      id: 6,
      codigo: 'FAC-06',
      nombre: 'PENSAMIENTO CRÍTICO',
      descripcion: 'CAPACIDAD DE ANÁLISIS OBJETIVO DE INFORMACIÓN COMPLEJA.',
      nroOrden: 1,
      codigoCompetencia: 'RESOL-01',
    },
  ],
};

export const altaPreguntaService = {
  /**
   * Obtiene la lista de competencias desde el backend (GET /api/competencias).
   */
  async obtenerCompetencias(): Promise<CompetenciaItem[]> {
    try {
      const data = await apiFetch<CompetenciaItem[]>('/competencias');
      if (Array.isArray(data) && data.length > 0) {
        return data;
      }
      return MOCK_COMPETENCIAS_FALLBACK;
    } catch {
      return MOCK_COMPETENCIAS_FALLBACK;
    }
  },

  /**
   * Obtiene los factores dinámicos asociados a una competencia seleccionada (GET /api/competencias/{codigoCompetencia}/factores).
   */
  async obtenerFactoresPorCompetencia(codigoCompetencia: string): Promise<FactorItem[]> {
    if (!codigoCompetencia) return [];
    try {
      const data = await apiFetch<FactorItem[]>(
        `/competencias/${encodeURIComponent(codigoCompetencia)}/factores`
      );
      if (Array.isArray(data) && data.length > 0) {
        return data;
      }
      return MOCK_FACTORES_FALLBACK[codigoCompetencia] || [];
    } catch {
      return MOCK_FACTORES_FALLBACK[codigoCompetencia] || [];
    }
  },

  /**
   * Registra una nueva pregunta en el sistema (POST /api/preguntas).
   */
  async crearPregunta(datos: AltaPreguntaRequest): Promise<AltaPreguntaResponse> {
    return await apiFetch<AltaPreguntaResponse>('/preguntas', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },
};

export { ApiError };
export type { BackendApiErrorData };

