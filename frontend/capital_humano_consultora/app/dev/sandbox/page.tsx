'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { AltaPreguntaScreen } from '@/features/cu021-alta-pregunta';
import { ROUTES } from '@/constants/routes';

export default function DevSandboxPage() {
  return (
    <DashboardLayout>
      <AltaPreguntaScreen
        onSuccessRedirect={ROUTES.CONSULTOR.PREGUNTAS}
        onCancelRedirect={ROUTES.CONSULTOR.PREGUNTAS}
      />
    </DashboardLayout>
  );
}
