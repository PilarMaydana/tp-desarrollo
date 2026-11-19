'use client';

import React from 'react';
import { useParams } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { ModificarPreguntaScreen } from '@/features/pregunta';
import { ROUTES } from '@/constants/routes';

export default function PreguntaEditarPage() {
  const params = useParams<{ id: string }>();
  const rawId = Array.isArray(params?.id) ? params.id[0] : (params?.id ?? '');

  return (
    <DashboardLayout>
      <ModificarPreguntaScreen
        preguntaId={rawId}
        onSuccessRedirect={ROUTES.CONSULTOR.PREGUNTAS}
        onCancelRedirect={ROUTES.CONSULTOR.PREGUNTAS}
      />
    </DashboardLayout>
  );
}
