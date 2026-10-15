'use client';

import React from 'react';
import { EmptyState } from '@/components/molecules/EmptyState';
import { Button } from '@/components/ui/Button';

export default function PreguntasPage() {
  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Gestión de Preguntas</h1>
        <p className="text-sm text-[#757575] mt-1">
          Banco de preguntas y reactivos psicotécnicos vinculados a competencias.
        </p>
      </div>

      <EmptyState
        icon="help_outline"
        title="Módulo de Preguntas"
        description="Aquí podrá gestionar el banco de preguntas por competencia, definir opciones de respuesta y ponderaciones individuales."
        action={<Button variant="primary">Crear Nueva Pregunta</Button>}
      />
    </div>
  );
}
