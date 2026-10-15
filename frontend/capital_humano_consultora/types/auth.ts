export type UserRole = 'CONSULTOR' | 'CANDIDATO';

export interface User {
  id: string;
  name: string;
  username: string;
  email?: string;
  role: UserRole;
  avatarUrl?: string;
}

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
