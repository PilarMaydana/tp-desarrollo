'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { ROUTES } from '@/constants/routes';

export default function HomePage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      if (isAuthenticated && user) {
        if (user.role === 'CANDIDATO') {
          router.replace(ROUTES.CANDIDATO.CUESTIONARIO);
        } else {
          router.replace(ROUTES.CONSULTOR.PUESTOS);
        }
      } else {
        router.replace(ROUTES.AUTH.LOGIN);
      }
    }
  }, [isAuthenticated, user, isLoading, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#DDDDDD]">
      <div className="flex flex-col items-center gap-3">
        <div className="w-10 h-10 border-4 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        <span className="text-sm font-semibold text-[#3F2581]">Cargando Consultora Capit@l Humano...</span>
      </div>
    </div>
  );
}
