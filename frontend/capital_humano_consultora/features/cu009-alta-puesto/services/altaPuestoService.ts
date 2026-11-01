import { apiFetch, ApiError } from '@/services/apiClient';
import {
  AltaPuestoRequest,
  AltaPuestoResponse,
  BackendApiErrorData,
  CompetenciaItem,
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
];

export const altaPuestoService = {
  /**
   * Obtiene las competencias disponibles desde el backend.
   * Si la API no responde, utiliza los datos iniciales de referencia de la base de datos.
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
   * Envía la solicitud para dar de alta un puesto (CU 009).
   * No silencia los errores HTTP (400, 409, 422) para permitir la gestión de los flujos alternativos 2.A y 2.B.
   */
  async crearPuesto(datos: AltaPuestoRequest): Promise<AltaPuestoResponse> {
    return await apiFetch<AltaPuestoResponse>('/puestos', {
      method: 'POST',
      body: JSON.stringify(datos),
    });
  },
};

export { ApiError };
export type { BackendApiErrorData };

