export interface Competencia {
  id: string;
  nombre: string;
  descripcion?: string;
  tipo?: string;
}

export interface CompetenciaPonderada {
  competenciaId: string;
  nombre?: string;
  ponderacion: number; // 0 a 10
}
