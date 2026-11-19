'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { LoginScreen } from '@/features/cu001-autenticar-usuario';
import { ROUTES } from '@/constants/routes';

export default function HomePage() {
  const { user, isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;

    if (isAuthenticated && user) {
      if (user.role === 'CANDIDATO') {
        router.replace(ROUTES.CANDIDATO.CUESTIONARIO);
      } else {
        router.replace(ROUTES.CONSULTOR.PUESTOS_NUEVO);
      }
    }
  }, [user, isAuthenticated, isLoading, router]);

  // Si está verificando sesión o si ya está autenticado (esperando redirección a su portal), mostrar loader breve
  if (isLoading || (isAuthenticated && user)) {
    return (
      <div className="min-h-screen bg-[#EBEBEB] flex items-center justify-center p-4">
        <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  // Primera pantalla del sistema en producción: Login (CU001)
  return <LoginScreen />;
}
