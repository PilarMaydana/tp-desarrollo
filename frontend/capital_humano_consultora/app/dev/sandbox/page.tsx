'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { ModificarPreguntaScreen } from '@/features/pregunta';
import { ROUTES } from '@/constants/routes';

/**
 * Sandbox temporal de desarrollo para CU 022 "Modificar Pregunta".
 * Apunta al ID 1 del seed (PREGUNTA POO) contra el backend real.
 */
export default function DevSandboxPage() {
  return (
    <DashboardLayout>
      <ModificarPreguntaScreen
        preguntaId={1}
        onSuccessRedirect={ROUTES.CONSULTOR.PREGUNTAS}
        onCancelRedirect={ROUTES.CONSULTOR.PREGUNTAS}
      />
    </DashboardLayout>
  );
}
