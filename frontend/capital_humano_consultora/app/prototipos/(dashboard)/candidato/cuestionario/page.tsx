'use client';

import React, { useEffect, useState } from 'react';
import { evaluacionService } from '@/services/evaluacionService';
import { Evaluacion } from '@/types/evaluacion';
import { EvaluationCard } from '@/components/organisms/EvaluationCard';
import { useAuth } from '@/context/AuthContext';

export default function MiCuestionarioPage() {
  const { user } = useAuth();
  const [evaluacion, setEvaluacion] = useState<Evaluacion | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Se obtiene el cuestionario activo asignado a la clave con la que ingresó el candidato
    evaluacionService.getCuestionarioActivo(user?.cuestionarioId).then((data) => {
      setEvaluacion(data);
      setIsLoading(false);
    });
  }, [user?.cuestionarioId]);

  const handleAction = (item: Evaluacion) => {
    alert(
      item.estado === 'active'
        ? `Iniciando CU026 - Completar Cuestionario: ${item.titulo}`
        : `Reanudando CU026 - Continuar Cuestionario en proceso: ${item.titulo}`
    );
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      {/* Título de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
          {evaluacion?.estado === 'in-progress'
            ? 'Cuestionario en Proceso'
            : 'Cuestionario Asignado'}
        </h1>
        <p className="text-sm md:text-base text-[#555555] mt-1 font-medium">
          {evaluacion?.estado === 'in-progress'
            ? 'Presione "Continuar" para reanudar la evaluación.'
            : 'Presione "Comenzar" para iniciar la evaluación.'}
        </p>
      </div>

      {/* Cuestionario Único Activo / En Proceso */}
      {isLoading ? (
        <div className="p-12 text-center text-[#757575]">Cargando cuestionario...</div>
      ) : evaluacion ? (
        <div className="flex flex-col gap-4">
          <EvaluationCard evaluacion={evaluacion} onAction={handleAction} />
        </div>
      ) : (
        <div className="p-8 text-center text-[#757575] bg-white rounded-xl border border-gray-200">
          No posee un cuestionario activo en este momento.
        </div>
      )}
    </div>
  );
}
