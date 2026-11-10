'use client';

import React, { Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ROUTES } from '@/constants/routes';

function EnDesarrolloContent() {
  const searchParams = useSearchParams();
  const modulo = searchParams.get('modulo') || 'Esta funcionalidad';

  return (
    <DashboardLayout>
      <div className="w-full max-w-2xl mx-auto py-16 px-4 flex flex-col items-center text-center space-y-6">
        {/* Ícono de módulo en construcción */}
        <div className="w-20 h-20 rounded-2xl bg-[#F4F0FF] border-2 border-[#D0BCFF] flex items-center justify-center text-[#3F2581] shadow-xs">
          <Icon name="help_outline" size={42} />
        </div>

        <div className="space-y-2">
          <span className="inline-block text-xs font-bold uppercase tracking-wider text-[#3F2581] bg-[#EADDFF] px-3 py-1 rounded-full">
            Próximamente
          </span>
          <h1 className="text-2xl sm:text-3xl font-bold text-[#3F2581]">
            {modulo} en desarrollo
          </h1>
          <p className="text-sm sm:text-base text-gray-600 max-w-md mx-auto leading-relaxed pt-1">
            Esta sección no forma parte de las funcionalidades activas de esta versión y se encuentra en proceso de implementación.
          </p>
        </div>

        {/* Botón para retornar a la pantalla activa */}
        <div className="pt-4">
          <Link href={ROUTES.CONSULTOR.PUESTOS_NUEVO}>
            <Button variant="primary" leftIcon="arrow_back">
              Volver a Alta de Puesto
            </Button>
          </Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default function EnDesarrolloPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F6F6F6]" />}>
      <EnDesarrolloContent />
    </Suspense>
  );
}

