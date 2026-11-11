'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { AltaCompetenciaForm } from '@/features/competencia';
import { ROUTES } from '@/constants/routes';

export default function CompetenciasNuevaPage() {
  const router = useRouter();

  return (
    <DashboardLayout>
      <AltaCompetenciaForm
        onSuccessNavigate={() => router.push(ROUTES.CONSULTOR.COMPETENCIAS)}
        onCancelNavigate={() => router.push(ROUTES.CONSULTOR.COMPETENCIAS)}
      />
    </DashboardLayout>
  );
}

