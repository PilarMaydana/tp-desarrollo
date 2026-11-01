'use client';

import React, { Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { AltaPuestoScreen } from '@/features/cu009-alta-puesto';
import { ROUTES } from '@/constants/routes';

function DevSandboxContent() {
  const searchParams = useSearchParams();
  const consultorParam =
    searchParams.get('consultor') ||
    searchParams.get('usuario') ||
    undefined;

  return (
    <DashboardLayout consultorNombre={consultorParam}>
      <AltaPuestoScreen
        consultorNombre={consultorParam}
        onSuccessRedirect={ROUTES.CONSULTOR.PUESTOS}
        onCancelRedirect={ROUTES.CONSULTOR.PUESTOS}
      />
    </DashboardLayout>
  );
}

export default function DevSandboxPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F6F6F6]" />}>
      <DevSandboxContent />
    </Suspense>
  );
}
