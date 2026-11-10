'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { AltaPuestoScreen } from '@/features/cu009-alta-puesto';
import { ROUTES } from '@/constants/routes';

export default function HomePage() {
  return (
    <DashboardLayout>
      <AltaPuestoScreen
        onSuccessRedirect={ROUTES.HOME}
        onCancelRedirect={ROUTES.HOME}
      />
    </DashboardLayout>
  );
}
