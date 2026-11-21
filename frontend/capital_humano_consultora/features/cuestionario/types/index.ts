import type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
} from '@/features/cu021-alta-pregunta';
import type { EstadoCuestionario } from '@/features/evaluacion';
import { ApiError } from '@/services/apiClient';

export { ApiError };
export type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
  EstadoCuestionario,
};

/**
 * Respuesta de GET /api/cuestionarios/{id}/instrucciones (CU 026 - Paso 1).
 * Verificado contra InstruccionesCuestionarioDTO.java del backend.
 */
export interface InstruccionesCuestionarioDTO {
  id: number;
  estado: EstadoCuestionario;
  nombrePuesto: string | null;
  tiempoMaximoMinutos: number;
  instrucciones: string;
}

/**
 * Opción de respuesta visible para el candidato (sin ponderaciones).
 * Verificado contra OpcionPreguntaCandidatoDTO.java del backend.
 */
export interface OpcionPreguntaCandidatoDTO {
  id: number;
  orden: number;
  descripcion: string;
}

/**
 * Pregunta asignada a un bloque del cuestionario.
 * Verificado contra PreguntaAsignadaDTO.java del backend.
 */
export interface PreguntaAsignadaDTO {
  preguntaAsignadaId: number;
  nroOrden: number;
  enunciado: string;
  opciones: OpcionPreguntaCandidatoDTO[];
  respuestaOpcionId: number | null;
}

/**
 * Bloque (paso del wizard) del cuestionario.
 * Verificado contra BloqueCuestionarioDTO.java del backend.
 */
export interface BloqueCuestionarioDTO {
  id: number;
  numero: number;
  completado: boolean;
  preguntas: PreguntaAsignadaDTO[];
}

/**
 * Respuesta de POST /api/cuestionarios/{id}/iniciar (CU 026 - Pasos 2 a 4 y Flujo 1.C).
 * Verificado contra CuestionarioIniciadoDTO.java del backend.
 */
export interface CuestionarioIniciadoDTO {
  id: number;
  estado: EstadoCuestionario;
  fechaInicio: string;
  segundosRestantes: number;
  totalBloques: number;
  bloqueActual: BloqueCuestionarioDTO;
}

/**
 * Ítem de respuesta enviado por el candidato (nunca envía opcionId null).
 * Verificado contra RespuestaItemDTO.java del backend.
 */
export interface RespuestaItemDTO {
  preguntaAsignadaId: number;
  opcionId: number;
}

/**
 * Payload de POST /api/cuestionarios/{id}/bloques/{numero}/respuestas.
 * Verificado contra ResponderBloqueRequestDTO.java del backend.
 */
export interface ResponderBloqueRequestDTO {
  respuestas: RespuestaItemDTO[];
}

/**
 * Respuesta de POST /api/cuestionarios/{id}/bloques/{numero}/respuestas (Pasos 7, 8 y 8.A).
 * Verificado contra ResponderBloqueResponseDTO.java del backend.
 */
export interface ResponderBloqueResponseDTO {
  completado: boolean;
  bloqueSiguiente?: BloqueCuestionarioDTO;
  segundosRestantes?: number;
  estado?: EstadoCuestionario;
  mensaje?: string;
}

/**
 * Fases de la pantalla del candidato en CU 026.
 */
export type FaseCuestionario =
  | 'cargando'
  | 'instrucciones'
  | 'wizard'
  | 'finalizado'
  | 'no_encontrado'
  | 'no_disponible'
  | 'error_conexion';

export type MotivoFinalizacion = 'completado' | 'tiempo_agotado';

export interface CuestionarioErrorPanel {
  titulo: string;
  mensajeGeneral: string | null;
  detalles: ErrorItem[];
  esValidacion?: boolean;
}
