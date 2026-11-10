/**
 * Tipos TypeScript para el Caso de Uso 020: Gestionar Preguntas
 * Cumple estrictamente con la especificación funcional y el contrato de DTOs del Backend.
 */

export interface OpcionRespuesta {
  id?: number;
  nombre: string;
  descripcion?: string;
  nroOrden?: number;
  ponderacion: number;
}

export interface Pregunta {
  id: number;
  competencia: string;
  factor: string;
  factorCodigo?: string;
  nombre: string;
  enunciado: string;
  descripcion?: string;
  opciones: OpcionRespuesta[];
}

export interface FiltrosBusquedaPregunta {
  competencia?: string;
  factor?: string;
  nombre?: string;
}

export interface CompetenciaItem {
  id: number;
  codigo?: string;
  nombre: string;
  descripcion?: string;
}

export interface FactorItem {
  id: number;
  codigo?: string;
  nombre: string;
  descripcion?: string;
  competenciaId?: number;
}

export interface BackendApiErrorData {
  status?: number;
  mensaje?: string;
  message?: string;
  timestamp?: string;
  detalles?: { campo?: string; mensaje: string }[];
}

