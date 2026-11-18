'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import MiCuestionarioPage from '@/app/prototipos/(dashboard)/candidato/cuestionario/page';

export default function CandidatoCuestionarioPage() {
  return (
    <DashboardLayout>
      <MiCuestionarioPage />
    </DashboardLayout>
  );
}
