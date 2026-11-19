import type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
  OpcionRespuestaItem,
} from '@/features/cu021-alta-pregunta';
import type {
  ItemOpcionRespuestaResponse,
  OpcionRespuestaResponse,
  OpcionRespuestaResumen,
} from '@/features/opcionrespuesta';

export type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
  OpcionRespuestaItem,
  ItemOpcionRespuestaResponse,
  OpcionRespuestaResponse,
  OpcionRespuestaResumen,
};

/**
 * Opción de respuesta devuelta por GET /api/preguntas/{id} y PUT /api/preguntas/{id}.
 * Verificado contra OpcionRespuestaDTO.java del backend.
 */
export interface PreguntaOpcionDetalle {
  id?: number;
  nombre: string;
  descripcion?: string | null;
  orden?: number;
  nroOrden?: number;
  ponderacion: number;
}

/**
 * Respuesta de GET /api/preguntas/{id} y PUT /api/preguntas/{id}.
 * Verificado contra PreguntaDTO.java del backend.
 */
export interface PreguntaDetalleResponse {
  id: number;
  competencia?: string | null;
  competenciaId?: number | null;
  codigoCompetencia?: string | null;
  factor?: string | null;
  factorId?: number | null;
  factorCodigo?: string | null;
  nombre: string;
  enunciado: string;
  descripcion?: string | null;
  opciones: PreguntaOpcionDetalle[];
}

/**
 * Ítem de opción enviado en PUT /api/preguntas/{id}.
 * Verificado contra PreguntaModificacionDTO.java + OpcionRespuestaDTO.java.
 */
export interface PreguntaOpcionModificacionRequest {
  orden: number;
  nombre: string;
  descripcion?: string | null;
  ponderacion: number;
}

/**
 * Payload de PUT /api/preguntas/{id}.
 * Competencia y Factor son de solo lectura y NO se envían.
 */
export interface PreguntaModificacionRequest {
  nombre: string;
  enunciado: string;
  descripcion?: string | null;
  opciones: PreguntaOpcionModificacionRequest[];
}

/**
 * Modelo de fila utilizado por el formulario de Modificar Pregunta.
 * Extiende OpcionRespuestaItem para reutilizar directamente GrillaOpcionesRespuesta.
 */
export interface OpcionPreguntaFormItem extends OpcionRespuestaItem {
  ponderacionInput: string;
}
