export type TipoDocumento = 'DNI' | 'LE' | 'LC' | 'PP';

export type UserRole = 'CONSULTOR' | 'CANDIDATO';

export interface ConsultorLoginDTO {
  usuario: string;
  contrasena: string;
}

export interface CandidatoLoginDTO {
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  clave: string;
}

export interface AuthResponseDTO {
  token: string;
  rol: UserRole;
  usuarioId: number;
  nombre?: string;
  apellido?: string;
  cuestionarioId?: number;
}

export interface ApiErrorDTO {
  status: number;
  codigo: string;
  mensaje: string;
  path?: string;
}
