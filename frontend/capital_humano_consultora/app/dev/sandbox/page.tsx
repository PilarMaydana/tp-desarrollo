'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { EvaluarCandidatosWizard } from '@/features/evaluacion';

export default function DevSandboxPage() {
  return (
    <DashboardLayout>
      <EvaluarCandidatosWizard />
    </DashboardLayout>
  );
}
