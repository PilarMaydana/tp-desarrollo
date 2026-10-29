'use client';

import React from 'react';
import { EmptyState } from '@/components/molecules/EmptyState';
import { Button } from '@/components/ui/Button';

export default function ReportesPage() {
  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Reportes y Estadísticas</h1>
        <p className="text-sm text-[#757575] mt-1">
          Exportación de informes ejecutivos, métricas de candidatos y resúmenes de competencias.
        </p>
      </div>

      <EmptyState
        icon="bar_chart"
        title="Módulo de Reportes"
        description="Generación de informes en formato PDF y Excel con resúmenes detallados de postulaciones y resultados."
        action={<Button variant="primary">Generar Reporte</Button>}
      />
    </div>
  );
}
