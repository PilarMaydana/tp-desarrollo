'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { GestionarPreguntasScreen } from '@/features/cu020-gestionar-preguntas';
import { ROUTES } from '@/constants/routes';

export default function PreguntasPage() {
  const router = useRouter();

  return (
    <DashboardLayout>
      <GestionarPreguntasScreen
        onNavigateNuevo={() => router.push(ROUTES.CONSULTOR.PREGUNTAS_NUEVO)}
        onNavigateModificar={(id) => router.push(ROUTES.CONSULTOR.PREGUNTAS_EDITAR(id))}
        onCancelRedirect={ROUTES.HOME}
      />
    </DashboardLayout>
  );
}

