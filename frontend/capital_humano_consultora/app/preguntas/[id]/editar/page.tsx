'use client';

import React, { Suspense } from 'react';
import { useParams } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { ModificarPreguntaScreen } from '@/features/pregunta';
import { ROUTES } from '@/constants/routes';

function PreguntaEditarContent() {
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

export default function PreguntaEditarPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-[#F6F6F6] flex items-center justify-center p-4">
          <div className="w-9 h-9 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        </div>
      }
    >
      <PreguntaEditarContent />
    </Suspense>
  );
}
