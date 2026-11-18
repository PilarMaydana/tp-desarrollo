'use client';

import React, { Suspense } from 'react';
import { LoginScreen } from '@/features/cu001-autenticar-usuario';

export default function DevSandboxPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-[#EBEBEB] flex items-center justify-center p-4">
          <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
        </div>
      }
    >
      <LoginScreen />
    </Suspense>
  );
}
