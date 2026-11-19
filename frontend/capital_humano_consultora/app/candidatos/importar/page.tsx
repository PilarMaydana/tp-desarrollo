'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { ImportarCandidatosForm } from '@/features/candidatos';
import { ROUTES } from '@/constants/routes';

export default function CandidatosImportarPage() {
  return (
    <DashboardLayout>
      <ImportarCandidatosForm
        onSuccessRedirect={ROUTES.HOME}
        onCancelRedirect={ROUTES.HOME}
      />
    </DashboardLayout>
  );
}
