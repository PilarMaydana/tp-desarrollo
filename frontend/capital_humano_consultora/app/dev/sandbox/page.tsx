'use client';

import React, { Suspense, useState } from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import {
  OrdenMeritoReporteScreen,
  OrdenMeritoSearchScreen,
} from '@/features/ordenmerito';

interface SandboxReporteState {
  puestoId: number;
  evaluacionIds?: number[];
}

function SandboxOrdenMeritoContent() {
  const [reporteTarget, setReporteTarget] =
    useState<SandboxReporteState | null>(null);

  if (reporteTarget) {
    return (
      <OrdenMeritoReporteScreen
        puestoId={reporteTarget.puestoId}
        evaluacionIds={reporteTarget.evaluacionIds}
        onVolver={() => setReporteTarget(null)}
      />
    );
  }

  return (
    <OrdenMeritoSearchScreen
      onEmitirReporte={(puestoId, evaluacionIds) =>
        setReporteTarget({ puestoId, evaluacionIds })
      }
    />
  );
}

export default function DevSandboxPage() {
  return (
    <DashboardLayout>
      <Suspense
        fallback={
          <div className="w-full flex items-center justify-center p-12">
            <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
          </div>
        }
      >
        <SandboxOrdenMeritoContent />
      </Suspense>
    </DashboardLayout>
  );
}
