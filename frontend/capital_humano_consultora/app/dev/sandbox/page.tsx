'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';

export default function DevSandboxPage() {
  return (
    <DashboardLayout>
      <div className="flex flex-col items-center justify-center p-12 text-center bg-white rounded-2xl border border-gray-200 max-w-lg mx-auto mt-12">
        <h1 className="text-2xl font-bold text-[#3F2581] mb-2">Ruta Sandbox de Desarrollo</h1>
        <p className="text-sm text-gray-600">
          El sandbox se encuentra actualmente libre y disponible para el desarrollo aislado del próximo caso de uso.
        </p>
      </div>
    </DashboardLayout>
  );
}
