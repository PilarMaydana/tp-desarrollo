'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { GestionarCompetenciasScreen } from '@/features/competencia';

export default function CompetenciasPage() {
  return (
    <DashboardLayout>
      <GestionarCompetenciasScreen />
    </DashboardLayout>
  );
}
