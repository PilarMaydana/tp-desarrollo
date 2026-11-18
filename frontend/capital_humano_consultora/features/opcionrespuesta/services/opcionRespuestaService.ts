import { apiFetch, ApiError } from '@/services/apiClient';
import {
  OpcionRespuestaCreateRequest,
  OpcionRespuestaResponse,
} from '../types';

export const opcionRespuestaService = {
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
