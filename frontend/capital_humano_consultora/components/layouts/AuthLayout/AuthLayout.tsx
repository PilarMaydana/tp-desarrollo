'use client';

import React from 'react';
import { Icon } from '@/components/ui/Icon';

export interface AuthLayoutProps {
  children: React.ReactNode;
  subtitle?: string;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({
  children,
  subtitle = 'Sistema de Evaluación y Gestión de Competencias',
}) => {
  return (
    <div className="min-h-screen bg-[#DDDDDD] flex flex-col justify-center items-center p-4">
      <div className="w-full max-w-[480px] bg-white rounded-[24px] shadow-2xl p-8 md:p-10 border border-[#E0E0E0] flex flex-col items-center">
        {/* Header con Isotipo y Marca */}
        <div className="w-16 h-16 rounded-2xl bg-[#3F2581] text-white flex items-center justify-center shadow-md mb-4">
          <Icon name="groups" size={36} />
        </div>

        <h1 className="text-xl md:text-2xl font-bold text-[#3F2581] text-center tracking-wide">
          CONSULTORA CAPIT@L HUMANO
        </h1>
        <p className="text-xs md:text-sm text-[#757575] text-center mt-1 mb-8">{subtitle}</p>

        {/* Formulario */}
        <div className="w-full">{children}</div>
      </div>
    </div>
  );
};

export default AuthLayout;
