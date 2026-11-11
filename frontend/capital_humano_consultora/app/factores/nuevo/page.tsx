'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { AltaFactorScreen } from '@/features/cu017-alta-factor';
import { ROUTES } from '@/constants/routes';

export default function FactoresNuevoPage() {
  return (
    <DashboardLayout>
      <AltaFactorScreen
        onSuccessRedirect={ROUTES.CONSULTOR.FACTORES_NUEVO}
        onCancelRedirect={ROUTES.CONSULTOR.FACTORES_NUEVO}
      />
    </DashboardLayout>
  );
}

