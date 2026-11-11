export interface Competencia {
  id: string;
  codigo?: string;
  nombre: string;
  descripcion?: string;
  tipo?: string;
}

export interface CompetenciaCreateRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface CompetenciaResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface CompetenciaPonderada {
  competenciaId: string;
  nombre?: string;
  ponderacion: number; // 0 a 10
}
