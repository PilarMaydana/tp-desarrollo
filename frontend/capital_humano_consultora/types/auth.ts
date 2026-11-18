export type UserRole = 'CONSULTOR' | 'CANDIDATO';
export type TipoDocumento = 'DNI' | 'LE' | 'LC' | 'PP';

export interface User {
  id: string;
  name: string;
  username: string;
  email?: string;
  role: UserRole;
  avatarUrl?: string;
  cuestionarioId?: number;
}

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

// Compatibilidad con código previo y prototipos
export interface LoginCredentials {
  username: string;
  clave: string;
  role: UserRole;
  tipoDocumento?: string;
  nroDocumento?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
