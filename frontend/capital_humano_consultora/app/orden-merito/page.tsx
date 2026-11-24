'use client';

import React, { Suspense } from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { OrdenMeritoSearchScreen } from '@/features/ordenmerito';

function OrdenMeritoSearchContent() {
  return (
    <DashboardLayout>
      <OrdenMeritoSearchScreen />
    </DashboardLayout>
  );
}

export default function OrdenMeritoPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-[#F6F6F6] flex items-center justify-center p-4">
          <div className="w-9 h-9 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        </div>
      }
    >
      <OrdenMeritoSearchContent />
    </Suspense>
  );
}
