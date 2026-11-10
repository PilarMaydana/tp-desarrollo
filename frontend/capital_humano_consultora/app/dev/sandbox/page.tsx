'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { GestionarPreguntasScreen } from '@/features/cu020-gestionar-preguntas';
import { ROUTES } from '@/constants/routes';

export default function DevSandboxPage() {
  return (
    <DashboardLayout>
      <GestionarPreguntasScreen
        onCancelRedirect={ROUTES.HOME}
      />
    </DashboardLayout>
  );
}
