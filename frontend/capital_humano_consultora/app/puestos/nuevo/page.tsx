'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { AltaPuestoScreen } from '@/features/cu009-alta-puesto';
import { ROUTES } from '@/constants/routes';

export default function PuestosNuevoPage() {
  return (
    <DashboardLayout>
      <AltaPuestoScreen
        onSuccessRedirect={ROUTES.CONSULTOR.PUESTOS_NUEVO}
        onCancelRedirect={ROUTES.CONSULTOR.PUESTOS_NUEVO}
      />
    </DashboardLayout>
  );
}

