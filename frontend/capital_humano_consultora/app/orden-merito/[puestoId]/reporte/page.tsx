'use client';

import React, { Suspense } from 'react';
import { useParams, useSearchParams } from 'next/navigation';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import {
  OrdenMeritoReporteScreen,
  parseEvaluacionIdsParam,
} from '@/features/ordenmerito';

function OrdenMeritoReporteContent() {
  const params = useParams<{ puestoId: string }>();
  const searchParams = useSearchParams();

  const rawPuestoId = Array.isArray(params?.puestoId)
    ? params.puestoId[0]
    : (params?.puestoId ?? '');
  const puestoId = /^\d+$/.test(rawPuestoId) ? Number(rawPuestoId) : Number.NaN;
  const evaluacionIds = parseEvaluacionIdsParam(
    searchParams.get('evaluacionIds')
  );

  return (
    <DashboardLayout>
      <OrdenMeritoReporteScreen
        puestoId={puestoId}
        evaluacionIds={evaluacionIds}
      />
    </DashboardLayout>
  );
}

export default function OrdenMeritoReportePage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-[#F6F6F6] flex items-center justify-center p-4">
          <div className="w-9 h-9 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        </div>
      }
    >
      <OrdenMeritoReporteContent />
    </Suspense>
  );
}
