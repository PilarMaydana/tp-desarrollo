'use client';

import React, { Suspense } from 'react';
import { useParams } from 'next/navigation';
import { CompletarCuestionarioScreen } from '@/features/cuestionario';

// TODO CU 01: el ingreso real será por tipo de documento, número y clave.
function CuestionarioContent() {
  const params = useParams<{ id: string }>();
  const rawId = Array.isArray(params?.id) ? params.id[0] : (params?.id ?? '');

  return <CompletarCuestionarioScreen key={rawId} cuestionarioId={rawId} />;
}

export default function CompletarCuestionarioPage() {
  return (
    <Suspense
      fallback={
        <div className="w-full max-w-xl mx-auto bg-white rounded-2xl border border-[#E0E0E0] shadow-sm p-12 flex flex-col items-center justify-center gap-4">
          <div className="w-10 h-10 border-4 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
          <p className="text-sm text-[#757575]">Cargando cuestionario...</p>
        </div>
      }
    >
      <CuestionarioContent />
    </Suspense>
  );
}
