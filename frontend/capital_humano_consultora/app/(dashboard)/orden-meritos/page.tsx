'use client';

import React from 'react';
import { EmptyState } from '@/components/molecules/EmptyState';
import { Button } from '@/components/ui/Button';

export default function OrdenMeritosPage() {
  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Orden de Méritos</h1>
        <p className="text-sm text-[#757575] mt-1">
          Ranking y puntuación ponderada obtenida por los candidatos en cada proceso de evaluación.
        </p>
      </div>

      <EmptyState
        icon="format_list_numbered"
        title="Módulo de Orden de Mérito"
        description="Seleccione un puesto y evaluación finalizada para consultar la tabla comparativa de puntajes y candidatos calificados."
        action={<Button variant="primary">Consultar Ranking</Button>}
      />
    </div>
  );
}
