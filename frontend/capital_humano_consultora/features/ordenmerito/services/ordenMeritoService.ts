import { apiFetch, ApiError } from '@/services/apiClient';
import type {
  EvaluacionOrdenMeritoDTO,
  FiltrosBusquedaPuestosOrdenMerito,
  PuestoOrdenMeritoDTO,
  ReporteOrdenMeritoDTO,
} from '../types';

export const ordenMeritoService = {
  /**
   * Busca puestos activos según código, nombre y/o empresa (CU 028 - Pasos 2 a 4).
   * GET /api/orden-merito/puestos?codigo=&nombre=&empresa=
   * Envía solo parámetros no vacíos normalizados a MAYÚSCULAS.
   */
  async buscarPuestos(
    filtros: FiltrosBusquedaPuestosOrdenMerito
  ): Promise<PuestoOrdenMeritoDTO[]> {
    const params: Record<string, string | undefined> = {};

    const codigo = filtros.codigo?.trim().toUpperCase();
    const nombre = filtros.nombre?.trim().toUpperCase();
    const empresa = filtros.empresa?.trim().toUpperCase();

    if (codigo) params.codigo = codigo;
    if (nombre) params.nombre = nombre;
    if (empresa) params.empresa = empresa;

    return await apiFetch<PuestoOrdenMeritoDTO[]>('/orden-merito/puestos', {
      method: 'GET',
      params,
    });
  },

  /**
   * Lista las evaluaciones asociadas a un puesto (CU 028 - Paso 6 y Flujo 6.A).
   * GET /api/orden-merito/puestos/{puestoId}/evaluaciones
   */
  async listarEvaluacionesPorPuesto(
    puestoId: number
  ): Promise<EvaluacionOrdenMeritoDTO[]> {
    return await apiFetch<EvaluacionOrdenMeritoDTO[]>(
      `/orden-merito/puestos/${puestoId}/evaluaciones`,
      {
        method: 'GET',
      }
    );
  },

  /**
   * Emite el reporte completo de Orden de Mérito para un puesto (CU 028 - Paso 7).
   * GET /api/orden-merito/puestos/{puestoId} o ?evaluacionIds=3,4
   * Si evaluacionIds está ausente o vacío, omite el parámetro (equivale a TODAS).
   */
  async emitirReporteOrdenMerito(
    puestoId: number,
    evaluacionIds?: number[]
  ): Promise<ReporteOrdenMeritoDTO> {
    const params: Record<string, string | undefined> = {};
    if (evaluacionIds && evaluacionIds.length > 0) {
      params.evaluacionIds = evaluacionIds.join(',');
    }

    return await apiFetch<ReporteOrdenMeritoDTO>(
      `/orden-merito/puestos/${puestoId}`,
      {
        method: 'GET',
        params,
      }
    );
  },
};

export { ApiError };
