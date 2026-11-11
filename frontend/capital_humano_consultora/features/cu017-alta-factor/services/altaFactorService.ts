import { apiFetch, ApiError } from '@/services/apiClient';
import {
  AltaFactorRequest,
  AltaFactorResponse,
  BackendApiErrorData,
  CompetenciaItem,
  FactorItem,
} from '../types';

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
  {
    id: 5,
    codigo: 'GES-01',
    nombre: 'GESTIÓN Y LIDERAZGO ORGANIZACIONAL',
    descripcion: 'CAPACIDAD DE COORDINAR RECURSOS HUMANOS Y ESTRATEGIAS DE IMPACTO.',
  },
];

export const altaFactorService = {
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
   * Obtiene los factores registrados para una competencia específica (GET /api/factores?competenciaId={id}).
   * Permite conocer qué números de orden ya están ocupados.
   */
  async obtenerFactoresPorCompetencia(competenciaId: number): Promise<FactorItem[]> {
    try {
      const data = await apiFetch<FactorItem[]>(`/factores`, {
        params: { competenciaId },
      });
      return Array.isArray(data) ? data : [];
    } catch {
      return [];
    }
  },

  /**
   * Registra un nuevo factor en el sistema (POST /api/factores).
   * No silencia los errores HTTP (400, 404, 409) para gestionar los flujos alternativos.
   */
  async crearFactor(datos: AltaFactorRequest): Promise<AltaFactorResponse> {
    return await apiFetch<AltaFactorResponse>('/factores', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },
};

export { ApiError };
export type { BackendApiErrorData };

