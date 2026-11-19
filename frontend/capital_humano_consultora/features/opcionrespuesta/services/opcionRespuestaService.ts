import { apiFetch, ApiError } from '@/services/apiClient';
import {
  OpcionRespuestaCreateRequest,
  OpcionRespuestaResponse,
  OpcionRespuestaResumen,
} from '../types';

export const opcionRespuestaService = {
  /**
   * Busca y lista opciones de respuesta aplicando criterio «contiene» sobre el nombre (GET /api/opciones-respuesta?nombre={texto} - CU 024).
   * Si nombre está vacío o no se envía, el backend retorna todas las opciones de respuesta ordenadas por nombre.
   */
  async buscarOpcionesRespuesta(
    nombre?: string
  ): Promise<OpcionRespuestaResumen[]> {
    const params: Record<string, string> = {};
    if (nombre !== undefined && nombre.trim() !== '') {
      params.nombre = nombre.trim();
    }
    return await apiFetch<OpcionRespuestaResumen[]>('/opciones-respuesta', {
      method: 'GET',
      params,
    });
  },

  /**
   * Obtiene el detalle completo de una opción de respuesta (escala) con sus ítems ordenados (GET /api/opciones-respuesta/{id}).
   */
  async obtenerOpcionRespuestaPorId(id: number): Promise<OpcionRespuestaResponse> {
    return await apiFetch<OpcionRespuestaResponse>(`/opciones-respuesta/${id}`, {
      method: 'GET',
    });
  },

  /**
   * Da de alta una nueva opción de respuesta con su lista de ítems (POST /api/opciones-respuesta - CU 029).
   * Propaga errores HTTP reales (400 con detalles[], 409 conflicto, 422 regla de negocio, 500/red) sin silenciarlos.
   */
  async crearOpcionRespuesta(
    payload: OpcionRespuestaCreateRequest
  ): Promise<OpcionRespuestaResponse> {
    const normalizado: OpcionRespuestaCreateRequest = {
      nombre: payload.nombre.trim().toUpperCase(),
      descripcion: payload.descripcion.trim().toUpperCase(),
      items: payload.items.map((item) => ({
        orden: Number(item.orden),
        descripcion: item.descripcion.trim().toUpperCase(),
      })),
    };

    return await apiFetch<OpcionRespuestaResponse>('/opciones-respuesta', {
      method: 'POST',
      body: JSON.stringify(normalizado),
    });
  },
};

export { ApiError };
