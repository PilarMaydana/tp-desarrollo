export interface CompetenciaItem {
  id: number | string;
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface FactorItem {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  competenciaId: number;
  competenciaNombre?: string;
  nroOrden: number;
}

export interface AltaFactorRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
  competenciaId: number;
  nroOrden: number;
}

export interface AltaFactorResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  competenciaId: number;
  competenciaNombre?: string;
  nroOrden: number;
}

export interface DetalleCampoError {
  campo: string;
  mensaje: string;
}

export interface ErrorItem {
  campo?: string;
  mensaje: string;
}

export interface BackendApiErrorData {
  timestamp?: string;
  status?: number;
  codigo?: string;
  mensaje?: string;
  path?: string;
  detalles?: DetalleCampoError[];
}

