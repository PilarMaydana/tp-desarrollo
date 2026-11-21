'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { CandidatoCuestionarioPortalScreen } from '@/features/cuestionario';

export default function CandidatoCuestionarioPage() {
  return (
    <DashboardLayout>
      <CandidatoCuestionarioPortalScreen />
    </DashboardLayout>
  );
}
