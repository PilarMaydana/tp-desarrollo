export interface CompetenciaItem {
  id: string | number;
  codigo?: string;
  nombre: string;
  descripcion?: string;
}

export interface CaracteristicaPuestoItem {
  competenciaId: string | number;
  nombre?: string;
  ponderacion: number;
}

export interface AltaPuestoRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
  empresa: string;
  caracteristicas: Array<{
    competenciaId: number;
    ponderacion: number;
  }>;
}

export interface AltaPuestoResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  empresa: string;
  caracteristicas: Array<{
    id?: number;
    competenciaId: number;
    nombreCompetencia?: string;
    ponderacion: number;
  }>;
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
