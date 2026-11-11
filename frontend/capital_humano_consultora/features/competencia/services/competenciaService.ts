import { apiFetch, ApiError } from '@/services/apiClient';
import { CompetenciaCreateRequest, CompetenciaResponse } from '@/types/competencia';

export const competenciaService = {
  /**
   * Obtiene el listado completo de competencias registradas (GET /api/competencias).
   * Propaga errores reales del backend para que la vista muestre el estado de error.
   */
  async listarCompetencias(): Promise<CompetenciaResponse[]> {
    const data = await apiFetch<CompetenciaResponse[]>('/competencias');
    return Array.isArray(data) ? data : [];
  },

  /**
   * Da de alta una nueva competencia (POST /api/competencias - CU 013).
   * Propaga errores HTTP (400 con detalles[], 409 conflicto, 500/red) sin silenciarlos.
   */
  async crearCompetencia(payload: CompetenciaCreateRequest): Promise<CompetenciaResponse> {
    const normalizado: CompetenciaCreateRequest = {
      codigo: payload.codigo.trim().toUpperCase(),
      nombre: payload.nombre.trim().toUpperCase(),
      descripcion: payload.descripcion.trim().toUpperCase(),
    };

    return await apiFetch<CompetenciaResponse>('/competencias', {
      method: 'POST',
      body: JSON.stringify(normalizado),
    });
  },
};

export { ApiError };
