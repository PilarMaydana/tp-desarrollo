'use client';

import React, { Suspense } from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { OrdenMeritoSearchScreen } from '@/features/ordenmerito';

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
        <OrdenMeritoSearchScreen />
      </Suspense>
    </DashboardLayout>
  );
}
