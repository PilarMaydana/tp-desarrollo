'use client';

import React, { useEffect, useState } from 'react';
import { evaluacionService } from '@/services/evaluacionService';
import { Evaluacion } from '@/types/evaluacion';
import { EvaluationCard } from '@/components/organisms/EvaluationCard';

export default function MiCuestionarioPage() {
  const [evaluaciones, setEvaluaciones] = useState<Evaluacion[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    evaluacionService.getEvaluacionesCandidato().then((data) => {
      setEvaluaciones(data);
      setIsLoading(false);
    });
  }, []);

  const handleAction = (evaluacion: Evaluacion) => {
    alert(`Iniciando / visualizando: ${evaluacion.titulo} - ${evaluacion.puestoNombre}`);
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      {/* Título de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Mi Cuestionario</h1>
        <p className="text-sm text-[#757575] mt-1">
          Visualice el estado de sus evaluaciones asignadas y continúe con los cuestionarios pendientes.
        </p>
      </div>

      {/* Lista de Tarjetas de Evaluación */}
      {isLoading ? (
        <div className="p-12 text-center text-[#757575]">Cargando cuestionarios...</div>
      ) : (
        <div className="flex flex-col gap-4">
          {evaluaciones.map((evalItem) => (
            <EvaluationCard
              key={evalItem.id}
              evaluacion={evalItem}
              onAction={handleAction}
            />
          ))}
        </div>
      )}
    </div>
  );
}
