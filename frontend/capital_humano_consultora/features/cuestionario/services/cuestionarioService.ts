import { apiFetch, ApiError } from '@/services/apiClient';
import type {
  CuestionarioIniciadoDTO,
  InstruccionesCuestionarioDTO,
  ResponderBloqueRequestDTO,
  ResponderBloqueResponseDTO,
} from '../types';

export const cuestionarioService = {
  /**
   * Obtiene las instrucciones y metadatos de un cuestionario (CU 026 - Paso 1).
   * GET /api/cuestionarios/{id}/instrucciones (NO descuenta accesos).
   * Sin mocks silenciosos: propaga ApiError real (404, 422, 500, red).
   */
  async obtenerInstrucciones(id: number): Promise<InstruccionesCuestionarioDTO> {
    return await apiFetch<InstruccionesCuestionarioDTO>(
      `/cuestionarios/${id}/instrucciones`,
      {
        method: 'GET',
      }
    );
  },

  /**
   * Inicia un cuestionario ACTIVO o reingresa a uno EN_PROCESO (CU 026 - Pasos 2-4 y Flujo 1.C).
   * POST /api/cuestionarios/{id}/iniciar (sin body; descuenta 1 acceso).
   * Sin mocks silenciosos: propaga ApiError real (404, 422, 500, red).
   */
  async iniciarCuestionario(id: number): Promise<CuestionarioIniciadoDTO> {
    return await apiFetch<CuestionarioIniciadoDTO>(
      `/cuestionarios/${id}/iniciar`,
      {
        method: 'POST',
      }
    );
  },

  /**
   * Envía las respuestas del bloque actual del cuestionario (CU 026 - Pasos 5 a 8 y Flujo 8.A).
   * POST /api/cuestionarios/{id}/bloques/{numero}/respuestas
   * Sin mocks silenciosos: propaga ApiError real (400 VALIDACION, 404, 422 REGLA_NEGOCIO, 500, red).
   */
  async responderBloque(
    id: number,
    numero: number,
    payload: ResponderBloqueRequestDTO
  ): Promise<ResponderBloqueResponseDTO> {
    return await apiFetch<ResponderBloqueResponseDTO>(
      `/cuestionarios/${id}/bloques/${numero}/respuestas`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    );
  },
};

export { ApiError };
