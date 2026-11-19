export interface Candidato {
  id: string;
  nroCandidato: string;
  nombre: string;
  apellido: string;
  tipoDocumento: string;
  nroDocumento: string;
  email: string;
}

export interface CandidatosFilterParams {
  nroCandidato?: string;
  apellido?: string;
  nombre?: string;
  tipoDocumento?: string;
  nroDocumento?: string;
}

// Modelos CU006: Importar Candidatos
export interface ImportacionCandidatosResponse {
  candidatosCreados: number;
  candidatosActualizados: number;
  candidatosEliminados: number;
}

export interface DetalleErrorFila {
  fila: number | null;    // null si es error general del archivo (ej. separador o extensión)
  campo: string | null;   // columna causante (ej. 'Tipo documento', 'Fecha de nacimiento')
  mensaje: string;        // descripción legible
}

export interface ErrorImportacionResponse {
  mensaje: string;
  errores: DetalleErrorFila[];
}
