import {
  AuthResponse,
  AuthResponseDTO,
  CandidatoLoginDTO,
  ConsultorLoginDTO,
  LoginCredentials,
  User,
} from '@/types/auth';
import { cu001AuthService } from '@/features/cu001-autenticar-usuario/services/authService';

const MOCK_CONSULTOR: User = {
  id: 'usr-1',
  name: 'Lucía Fernández',
  username: 'lfernandez',
  email: 'lucia.fernandez@consultora.com',
  role: 'CONSULTOR',
};

const MOCK_CANDIDATO: User = {
  id: 'cand-1',
  name: 'Juan Pérez',
  username: 'jperez',
  email: 'juan.perez@example.com',
  role: 'CANDIDATO',
};

export const authService = {
  /**
   * Endpoint oficial CU001: Autenticación de Consultor
   */
  async loginConsultor(dto: ConsultorLoginDTO): Promise<AuthResponseDTO> {
    return cu001AuthService.loginConsultor(dto);
  },

  /**
   * Endpoint oficial CU001: Autenticación de Candidato
   */
  async loginCandidato(dto: CandidatoLoginDTO): Promise<AuthResponseDTO> {
    return cu001AuthService.loginCandidato(dto);
  },

  /**
   * Método legacy para compatibilidad con código anterior
   */
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    try {
      if (credentials.role === 'CONSULTOR') {
        const res = await this.loginConsultor({
          usuario: credentials.username,
          contrasena: credentials.clave,
        });
        const user: User = {
          id: String(res.usuarioId),
          name: credentials.username.toUpperCase(),
          username: credentials.username.toUpperCase(),
          role: res.rol,
        };
        return { token: res.token, user };
      } else {
        const res = await this.loginCandidato({
          tipoDocumento: (credentials.tipoDocumento as 'DNI' | 'LE' | 'LC' | 'PP') || 'DNI',
          numeroDocumento: credentials.nroDocumento || credentials.username,
          clave: credentials.clave,
        });
        const user: User = {
          id: String(res.usuarioId),
          name: `Candidato (${credentials.nroDocumento || credentials.username})`,
          username: credentials.nroDocumento || credentials.username,
          role: res.rol,
          cuestionarioId: res.cuestionarioId,
        };
        return { token: res.token, user };
      }
    } catch {
      // Fallback para desarrollo offline / prototipo funcional
      const user = credentials.role === 'CONSULTOR' ? MOCK_CONSULTOR : MOCK_CANDIDATO;
      const response: AuthResponse = {
        token: `mock-jwt-token-${user.id}-${Date.now()}`,
        user,
      };
      return response;
    }
  },

  getCurrentUser(): User | null {
    if (typeof window === 'undefined') return null;
    const userStr = sessionStorage.getItem('auth_user') || localStorage.getItem('auth_user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  },

  logout(): void {
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('auth_token');
      sessionStorage.removeItem('auth_user');
      sessionStorage.removeItem('cuestionario_id');
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      localStorage.removeItem('cuestionario_id');
    }
  },

  saveSession(authResponse: AuthResponse): void {
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('auth_token', authResponse.token);
      sessionStorage.setItem('auth_user', JSON.stringify(authResponse.user));
      if (authResponse.user.cuestionarioId) {
        sessionStorage.setItem('cuestionario_id', String(authResponse.user.cuestionarioId));
      } else {
        sessionStorage.removeItem('cuestionario_id');
      }
      // Limpiar residuo previo de localStorage para evitar que persista entre ventanas cerradas
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      localStorage.removeItem('cuestionario_id');
    }
  },
};
