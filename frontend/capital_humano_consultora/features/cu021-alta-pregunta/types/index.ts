export interface CompetenciaItem {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
}

export interface FactorItem {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  nroOrden?: number;
  codigoCompetencia?: string;
}

export interface OpcionRespuestaItem {
  id?: number;
  orden: number;
  nombre: string;
  descripcion: string;
  ponderacion: number;
  /** Valor textual controlado opcional (permite entradas vacías o sin truncado automático, ej. CU 022). */
  ponderacionInput?: string;
}

export interface PlantillaOpcionRespuesta {
  id: string;
  nombre: string;
  descripcion: string;
  opciones: OpcionRespuestaItem[];
}

export interface AltaPreguntaRequest {
  nombre: string;
  enunciado: string;
  descripcion?: string | null;
  codigoFactor: string;
  opcionesRespuesta: {
    orden: number;
    nombre: string;
    descripcion: string;
    ponderacion: number;
  }[];
}

export interface AltaPreguntaResponse {
  id: number;
  nombre: string;
  enunciado: string;
  descripcion?: string;
  codigoFactor: string;
  codigoCompetencia?: string;
  opcionesRespuesta: OpcionRespuestaItem[];
}

export interface BackendApiErrorDetalle {
  campo: string;
  mensaje: string;
}

export interface BackendApiErrorData {
  status: number;
  codigo: string;
  mensaje: string;
  path?: string;
  detalles?: BackendApiErrorDetalle[];
}

export interface ErrorItem {
  campo?: string;
  mensaje: string;
}

