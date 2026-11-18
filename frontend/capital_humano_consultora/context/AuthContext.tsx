'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { AuthResponseDTO, LoginCredentials, User, UserRole } from '@/types/auth';
import { authService } from '@/services/authService';

interface AuthContextType {
  user: User | null;
  role: UserRole;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  saveAuthResponse: (response: AuthResponseDTO, identifier: string) => void;
  logout: () => void;
  switchRole: (role: UserRole) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function toTitleCase(str: string): string {
  return str
    .toLowerCase()
    .split(' ')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

function resolveDisplayName(
  role: UserRole,
  identifier: string,
  apiNombre?: string,
  apiApellido?: string
): string {
  if (apiNombre && apiNombre.trim().length > 0) {
    const rawFullName = apiApellido
      ? `${apiNombre.trim()} ${apiApellido.trim()}`
      : apiNombre.trim();
    return toTitleCase(rawFullName);
  }

  const idUpper = identifier.trim().toUpperCase();

  if (role === 'CONSULTOR') {
    const NOMBRES_CONSULTORES: Record<string, string> = {
      LFERNANDEZ: 'Lucía Fernández',
    };
    return NOMBRES_CONSULTORES[idUpper] || toTitleCase(identifier);
  } else {
    // Candidato: ÚNICAMENTE SU NOMBRE, NUNCA EL DNI
    const NOMBRES_CANDIDATOS: Record<string, string> = {
      '12345678': 'Juan Pérez',
      '87654321': 'María Gómez',
      '11223344': 'Carlos López',
    };
    return NOMBRES_CANDIDATOS[identifier.trim()] || 'Candidato';
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Restaurar usuario desde sesión guardada de forma segura
    try {
      const saved = authService.getCurrentUser();
      if (saved) {
        setUser(saved);
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = async (credentials: LoginCredentials) => {
    setIsLoading(true);
    try {
      const response = await authService.login(credentials);
      authService.saveSession(response);
      setUser(response.user);
    } finally {
      setIsLoading(false);
    }
  };

  const saveAuthResponse = (dto: AuthResponseDTO, identifier: string) => {
    const displayName = resolveDisplayName(dto.rol, identifier, dto.nombre, dto.apellido);
    const newUser: User = {
      id: String(dto.usuarioId),
      name: displayName,
      username: identifier,
      role: dto.rol,
      cuestionarioId: dto.cuestionarioId,
    };
    authService.saveSession({ token: dto.token, user: newUser });
    setUser(newUser);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const switchRole = (newRole: UserRole) => {
    if (newRole === 'CONSULTOR') {
      const cUser: User = {
        id: 'usr-1',
        name: 'Lucía Fernández',
        username: 'lfernandez',
        email: 'lucia.fernandez@consultora.com',
        role: 'CONSULTOR',
      };
      setUser(cUser);
      authService.saveSession({ token: 'mock-consultor-token', user: cUser });
    } else {
      const candUser: User = {
        id: 'cand-1',
        name: 'Juan Pérez',
        username: 'jperez',
        email: 'juan.perez@example.com',
        role: 'CANDIDATO',
      };
      setUser(candUser);
      authService.saveSession({ token: 'mock-candidato-token', user: candUser });
    }
  };

  const role = user?.role || 'CONSULTOR';
  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider
      value={{
        user,
        role,
        isAuthenticated,
        isLoading,
        login,
        saveAuthResponse,
        logout,
        switchRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
