import { CompetenciaPonderada } from './competencia';

export interface Empresa {
  id: string;
  nombre: string;
}

export interface Puesto {
  id: string;
  codigo: string;
  nombrePuesto: string;
  empresaId: string;
  empresaNombre?: string;
  descripcion?: string;
  competencias: CompetenciaPonderada[];
  fechaCreacion?: string;
  estado?: 'ACTIVO' | 'INACTIVO';
}

export interface CrearPuestoDTO {
  empresaId: string;
  nombrePuesto: string;
  codigo?: string;
  descripcion?: string;
  competencias: CompetenciaPonderada[];
}

export interface PuestosFilterParams {
  codigo?: string;
  nombrePuesto?: string;
  empresaId?: string;
}
