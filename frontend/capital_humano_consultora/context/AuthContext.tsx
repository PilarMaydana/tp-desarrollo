'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { LoginCredentials, User, UserRole } from '@/types/auth';
import { authService } from '@/services/authService';

interface AuthContextType {
  user: User | null;
  role: UserRole;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  switchRole: (role: UserRole) => void;
}

const DEFAULT_DEMO_USER: User = {
  id: 'usr-demo-1',
  name: 'Lucía Fernández',
  username: 'lfernandez',
  email: 'lucia.fernandez@consultora.com',
  role: 'CONSULTOR',
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(DEFAULT_DEMO_USER);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Restaurar usuario desde sesión guardada de forma segura
    const timer = setTimeout(() => {
      const saved = authService.getCurrentUser();
      if (saved && saved.id !== DEFAULT_DEMO_USER.id) {
        setUser(saved);
      }
    }, 0);
    return () => clearTimeout(timer);
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
