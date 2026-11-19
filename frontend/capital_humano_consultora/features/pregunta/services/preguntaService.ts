import { apiFetch, ApiError } from '@/services/apiClient';
import { opcionRespuestaService } from '@/features/opcionrespuesta';
import type {
  OpcionRespuestaResponse,
  OpcionRespuestaResumen,
  PreguntaDetalleResponse,
  PreguntaModificacionRequest,
} from '../types';

export const preguntaService = {
  /**
   * Obtiene los datos detallados de una pregunta activa por ID (GET /api/preguntas/{id}).
   * Sin mocks silenciosos: propaga ApiError real (404, 500, error de red).
   */
  async obtenerPreguntaPorId(id: number): Promise<PreguntaDetalleResponse> {
    return await apiFetch<PreguntaDetalleResponse>(`/preguntas/${id}`, {
      method: 'GET',
    });
  },

  /**
   * Modifica los datos y opciones ponderadas de una pregunta existente (PUT /api/preguntas/{id} - CU 022).
   * Competencia y Factor son de solo lectura y no se incluyen en el payload.
   * Sin mocks silenciosos: propaga ApiError real (400, 404, 409, 422, 500, red).
   */
  async modificarPregunta(
    id: number,
    payload: PreguntaModificacionRequest
  ): Promise<PreguntaDetalleResponse> {
    return await apiFetch<PreguntaDetalleResponse>(`/preguntas/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Lista las escalas disponibles para el selector "Opción de Respuesta" (GET /api/opciones-respuesta).
   * Reutiliza opcionRespuestaService.
   */
  async listarEscalasOpcionesRespuesta(): Promise<OpcionRespuestaResumen[]> {
    return await opcionRespuestaService.buscarOpcionesRespuesta();
  },

  /**
   * Obtiene los ítems de una escala seleccionada (GET /api/opciones-respuesta/{id}).
   * Reutiliza opcionRespuestaService.
   */
  async obtenerEscalaOpcionRespuestaPorId(id: number): Promise<OpcionRespuestaResponse> {
    return await opcionRespuestaService.obtenerOpcionRespuestaPorId(id);
  },
};

export { ApiError };
