import type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
} from '@/features/cu021-alta-pregunta';
import { ApiError } from '@/services/apiClient';

export { ApiError };
export type { BackendApiErrorData, BackendApiErrorDetalle, ErrorItem };

export type TipoDocumento = 'DNI' | 'LE' | 'LC' | 'PP';

export type EstadoCuestionario =
  | 'ACTIVO'
  | 'EN_PROCESO'
  | 'COMPLETO'
  | 'INCOMPLETO'
  | 'SIN_CONTESTAR';

export interface FiltrosBusquedaCandidatos {
  apellido?: string;
  nombre?: string;
  nroCandidato?: string;
}

/**
 * Verificado contra CandidatoResumenDTO.java del backend.
 */
export interface CandidatoResumenDTO {
  id: number;
  nroCandidato: string;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  email?: string | null;
}

/**
 * Verificado contra ValidarCandidatosRequestDTO.java del backend.
 */
export interface ValidarCandidatosRequestDTO {
  candidatoIds: number[];
}

/**
 * Verificado contra ValidacionCandidatosResponseDTO.java del backend.
 */
export interface ValidacionCandidatosResponseDTO {
  valido: boolean;
  cantidadCandidatos: number;
  candidatos: CandidatoResumenDTO[];
}

/**
 * Verificado contra PuestoResumenDTO.java del backend.
 */
export interface PuestoResumenDTO {
  id: number;
  codigo: string;
  nombre: string;
  empresa: string;
}

/**
 * Verificado contra CompetenciaRequeridaDTO.java del backend.
 */
export interface CompetenciaRequeridaDTO {
  competenciaId: number;
  codigo: string;
  nombre: string;
  ponderacionRequerida: number;
}

/**
 * Verificado contra DetallePuestoEvaluacionDTO.java del backend.
 */
export interface DetallePuestoEvaluacionDTO {
  puestoId: number;
  codigo: string;
  nombre: string;
  empresa: string;
  competencias: CompetenciaRequeridaDTO[];
}

/**
 * Verificado contra PrevisualizarEvaluacionRequestDTO.java del backend.
 */
export interface PrevisualizarEvaluacionRequestDTO {
  puestoId: number;
  candidatoIds: number[];
}

/**
 * Verificado contra CandidatoClaveDTO.java del backend.
 */
export interface CandidatoClaveDTO {
  candidatoId: number;
  nroCandidato: string;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  claveAcceso: string;
}

/**
 * Verificado contra PrevisualizacionEvaluacionDTO.java del backend.
 */
export interface PrevisualizacionEvaluacionDTO {
  puestoId: number;
  puestoCodigo: string;
  puestoNombre: string;
  empresa: string;
  tiempoMaximoMinutos: number;
  cantidadPreguntasPorBloque: number;
  candidatos: CandidatoClaveDTO[];
}

/**
 * Verificado contra CrearEvaluacionRequestDTO.java del backend.
 */
export interface CrearEvaluacionRequestDTO {
  puestoId: number;
  candidatoIds: number[];
  clavesPorCandidato: Record<string, string>;
}

/**
 * Verificado contra CuestionarioResumenDTO.java del backend.
 */
export interface CuestionarioResumenDTO {
  cuestionarioId: number;
  candidatoId: number;
  nroCandidato: string;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  claveAcceso: string;
  estado: EstadoCuestionario;
  cantidadBloques: number;
  cantidadPreguntas: number;
}

/**
 * Verificado contra EvaluacionCreadaDTO.java del backend.
 */
export interface EvaluacionCreadaDTO {
  evaluacionId: number;
  puestoId: number;
  puestoCodigo: string;
  puestoNombre: string;
  empresa: string;
  fechaCreacion: string;
  tiempoMaximoMinutos: number;
  cantidadPreguntasPorBloque: number;
  cuestionarios: CuestionarioResumenDTO[];
}

export interface ArchivoExcelDescargado {
  blob: Blob;
  filename: string;
}

export interface ErrorPanelState {
  titulo?: string;
  mensajeGeneral: string | null;
  detalles: ErrorItem[];
}
