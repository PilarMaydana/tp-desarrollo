import { AuthResponse, LoginCredentials, User } from '@/types/auth';
import { apiFetch } from './apiClient';

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
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    try {
      // Intento de conexión al backend Spring Boot
      return await apiFetch<AuthResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
      });
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
    const userStr = localStorage.getItem('auth_user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  },

  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
    }
  },

  saveSession(authResponse: AuthResponse): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('auth_token', authResponse.token);
      localStorage.setItem('auth_user', JSON.stringify(authResponse.user));
    }
  },
};
