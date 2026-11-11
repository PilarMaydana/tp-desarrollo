'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { EmptyState } from '@/components/molecules/EmptyState';
import { Button } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';

export default function CompetenciasPage() {
  const router = useRouter();

  const handleAlta = () => {
    router.push(ROUTES.CONSULTOR.COMPETENCIAS_NUEVO);
  };

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
        <div className="flex flex-col">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Gestión de Competencias</h1>
          <p className="text-sm text-[#757575] mt-1">
            Administración de las competencias evaluables registradas en el sistema.
          </p>
        </div>

        <EmptyState
          icon="check"
          title="Módulo de Competencias"
          description="Registre nuevas competencias evaluables para los puestos de trabajo. Próximamente podrá modificar y dar de baja competencias existentes."
          action={
            <Button
              type="button"
              variant="primary"
              leftIcon="plus"
              onClick={handleAlta}
            >
              Dar de alta competencia
            </Button>
          }
        />
      </div>
    </DashboardLayout>
  );
}

