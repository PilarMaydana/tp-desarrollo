import { ApiError } from '@/services/apiClient';
import type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
} from '@/features/cu021-alta-pregunta';
import type { TipoDocumento } from '@/features/evaluacion';

export { ApiError };
export type {
  BackendApiErrorData,
  BackendApiErrorDetalle,
  ErrorItem,
  TipoDocumento,
};

export interface FiltrosBusquedaPuestosOrdenMerito {
  codigo?: string;
  nombre?: string;
  empresa?: string;
}

/**
 * Verificado contra PuestoOrdenMeritoDTO.java del backend (CU 028 - Paso 4).
 */
export interface PuestoOrdenMeritoDTO {
  puestoId: number;
  codigo: string;
  nombre: string;
  empresa: string;
  cantidadCandidatos: number;
  evaluacionesCompletas: number;
  evaluacionesTotal: number;
}

/**
 * Verificado contra EvaluacionOrdenMeritoDTO.java del backend (CU 028 - Paso 6).
 */
export interface EvaluacionOrdenMeritoDTO {
  evaluacionId: number;
  fechaCreacion: string;
  cantidadCandidatos: number;
  cantidadCompletos: number;
}

/**
 * Verificado contra PuestoReporteDTO.java del backend (CU 028 - Paso 7).
 */
export interface PuestoReporteDTO {
  puestoId: number;
  codigo: string;
  nombre: string;
  empresa: string;
}

/**
 * Verificado contra EvaluacionIncluidaDTO.java del backend (CU 028 - Paso 7).
 */
export interface EvaluacionIncluidaDTO {
  evaluacionId: number;
  fechaCreacion: string;
}

/**
 * Verificado contra CompetenciaRequeridaReporteDTO.java del backend (CU 028 - Paso 7).
 */
export interface CompetenciaRequeridaReporteDTO {
  competenciaId: number;
  codigo: string;
  nombre: string;
  ponderacionRequerida: number;
}

/**
 * Verificado contra DatosAdicionalesDTO.java del backend (CU 028 - Observación 2).
 * - Cuestionarios COMPLETO: fechaInicio, fechaFin y cantidadAccesos (ultimoIngreso = null).
 * - Cuestionarios NO completos: fechaInicio, ultimoIngreso y cantidadAccesos (fechaFin = null).
 */
export interface DatosAdicionalesDTO {
  fechaInicio: string | null;
  fechaFin: string | null;
  ultimoIngreso: string | null;
  cantidadAccesos: number;
}

/**
 * Verificado contra CompetenciaNoAlcanzadaDTO.java del backend (CU 028 - Paso 7).
 */
export interface CompetenciaNoAlcanzadaDTO {
  competenciaId: number;
  codigo: string;
  competencia: string;
  obtenido: number | null;
  requerido: number;
}

/**
 * Verificado contra FilaOrdenMeritoDTO.java del backend (CU 028 - Paso 7).
 */
export interface FilaOrdenMeritoDTO {
  posicion: number;
  cuestionarioId: number;
  evaluacionId: number;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: number;
  datosAdicionales: DatosAdicionalesDTO;
}

/**
 * Verificado contra FilaNoAlcanzoMinimoDTO.java del backend (CU 028 - Paso 7).
 */
export interface FilaNoAlcanzoMinimoDTO {
  cuestionarioId: number;
  evaluacionId: number;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: number | null;
  datosAdicionales: DatosAdicionalesDTO;
  competenciasNoAlcanzadas: CompetenciaNoAlcanzadaDTO[];
}

/**
 * Verificado contra FilaCandidatoReporteDTO.java del backend (CU 028 - Observación 1).
 */
export interface FilaCandidatoReporteDTO {
  cuestionarioId: number;
  evaluacionId: number;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: null;
  datosAdicionales: DatosAdicionalesDTO;
}

/**
 * Verificado contra TotalesReporteDTO.java del backend (CU 028 - Paso 7).
 */
export interface TotalesReporteDTO {
  ordenDeMerito: number;
  noAlcanzaronMinimos: number;
  incompletos: number;
  enProceso: number;
  sinContestar: number;
  activos: number;
  totalGeneral: number;
}

/**
 * Verificado contra ReporteOrdenMeritoDTO.java del backend (CU 028 - Paso 7).
 */
export interface ReporteOrdenMeritoDTO {
  puesto: PuestoReporteDTO;
  evaluacionesIncluidas: EvaluacionIncluidaDTO[];
  fechaEmision: string;
  competenciasRequeridas: CompetenciaRequeridaReporteDTO[];
  ordenDeMerito: FilaOrdenMeritoDTO[];
  noAlcanzaronMinimos: FilaNoAlcanzoMinimoDTO[];
  incompletos: FilaCandidatoReporteDTO[];
  enProceso: FilaCandidatoReporteDTO[];
  sinContestar: FilaCandidatoReporteDTO[];
  activos: FilaCandidatoReporteDTO[];
  totales: TotalesReporteDTO;
}

export interface OrdenMeritoErrorState {
  status?: number;
  codigo?: string;
  mensaje: string;
  detalles: ErrorItem[];
}
