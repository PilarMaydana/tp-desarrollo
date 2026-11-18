'use client';

import React from 'react';
import { Icon } from '@/components/ui/Icon';

export interface AuthLayoutProps {
  children: React.ReactNode;
  subtitle?: string;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({
  children,
  subtitle = 'Sistema de Evaluación de Perfiles Laborales',
}) => {
  return (
    <div className="relative min-h-screen bg-[#EBEBEB] flex flex-col justify-center items-center p-4 py-8 overflow-x-hidden">
      {/* Franja morada oscura institucional superior en el fondo */}
      <div
        className="absolute top-0 left-0 right-0 h-[38vh] min-h-[250px] max-h-[360px] bg-gradient-to-r from-[#341E6C] via-[#3F2581] to-[#4F378A] shadow-md z-0"
        aria-hidden="true"
      >
        <div className="absolute inset-0 opacity-15 bg-[radial-gradient(#EADDFF_1px,transparent_1px)] [background-size:16px_16px]" />
        <div className="absolute bottom-0 left-0 right-0 h-10 bg-gradient-to-t from-black/15 to-transparent" />
      </div>

      {/* Tarjeta de autenticación elevada */}
      <div className="relative z-10 w-full max-w-[480px] bg-white rounded-[24px] shadow-2xl p-8 md:p-10 border border-[#E0E0E0]/80 flex flex-col items-center">
        {/* Header con Isotipo y Marca */}
        <div className="w-16 h-16 rounded-2xl bg-[#3F2581] text-white flex items-center justify-center shadow-md mb-4 ring-4 ring-white/90">
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
