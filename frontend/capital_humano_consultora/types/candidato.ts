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
