'use client';

import React from 'react';
import { Evaluacion } from '@/types/evaluacion';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface EvaluationCardProps {
  evaluacion: Evaluacion;
  onAction?: (evaluacion: Evaluacion) => void;
  className?: string;
}

export const EvaluationCard: React.FC<EvaluationCardProps> = ({
  evaluacion,
  onAction,
  className = '',
}) => {
  const { estado, titulo, puestoNombre, fechaInicio, ultimaActualizacion, tiempoRestante } =
    evaluacion;

  return (
    <div
      className={`bg-white rounded-2xl border-2 border-[#D0BCFF] p-6 shadow-sm hover:shadow-md transition-shadow flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6 ${className}`}
    >
      {/* Sección Izquierda: Título y Estado */}
      <div className="flex flex-col gap-2 min-w-[280px]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#F4F0FF] text-[#3F2581] flex items-center justify-center shrink-0">
            <Icon name="assignment" size={22} />
          </div>
          <div className="flex flex-col">
            <h3 className="text-lg font-bold text-[#3F2581] leading-snug">{titulo}</h3>
            <span className="text-sm font-medium text-[#4A4A4A]">Puesto: {puestoNombre}</span>
          </div>
        </div>
        <div className="mt-1">
          <Badge status={estado} />
        </div>
      </div>

      {/* Sección Central: Metadatos temporales */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-xs md:text-sm text-[#757575] flex-1">
        <div className="flex items-center gap-2">
          <strong className="text-[#333333]">Iniciado el:</strong>
          <span>{fechaInicio}</span>
        </div>
        <div className="flex items-center gap-2">
          <strong className="text-[#333333]">Última modif:</strong>
          <span>{ultimaActualizacion}</span>
        </div>
        {tiempoRestante && estado === 'in-progress' && (
          <div className="flex items-center gap-2 col-span-1 sm:col-span-2 text-[#3F2581] font-semibold">
            <span>⏱️ {tiempoRestante}</span>
          </div>
        )}
      </div>

      {/* Sección Derecha: Acción Contextual */}
      <div className="shrink-0 w-full lg:w-auto flex justify-end">
        {estado === 'in-progress' && (
          <Button
            type="button"
            variant="primary"
            onClick={() => onAction && onAction(evaluacion)}
            rightIcon="arrow_forward"
            className="w-full lg:w-auto"
          >
            Continuar
          </Button>
        )}

        {estado === 'finished' && (
          <Button
            type="button"
            variant="secondary"
            onClick={() => onAction && onAction(evaluacion)}
            leftIcon="visibility"
            className="w-full lg:w-auto"
          >
            Ver
          </Button>
        )}

        {estado === 'expired' && (
          <Button type="button" variant="secondary" disabled className="w-full lg:w-auto opacity-50">
            Expirado
          </Button>
        )}
      </div>
    </div>
  );
};

export default EvaluationCard;
