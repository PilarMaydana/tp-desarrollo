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
      className={`bg-white rounded-2xl border-2 border-[#D0BCFF] p-6 shadow-sm hover:shadow-md transition-shadow flex flex-col md:flex-row items-start md:items-center justify-between gap-6 ${className}`}
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
      <div className="flex flex-col justify-center gap-1.5 text-xs md:text-sm text-[#757575] flex-1">
        {estado === 'active' ? (
          <div className="flex items-center gap-2 text-[#3F2581] font-semibold whitespace-nowrap">
            <Icon name="timer" size={18} className="text-[#3F2581] shrink-0" />
            <span>{tiempoRestante || 'Tiempo máximo disponible: 60 minutos'}</span>
          </div>
        ) : estado === 'in-progress' ? (
          <>
            <div className="flex items-center gap-2 whitespace-nowrap">
              <strong className="text-[#333333]">Iniciado el:</strong>
              <span>{fechaInicio}</span>
            </div>
            {tiempoRestante && (
              <div className="flex items-center gap-2 text-[#1B8354] font-semibold whitespace-nowrap">
                <Icon name="timer" size={18} className="text-[#1B8354] shrink-0" />
                <span>{tiempoRestante}</span>
              </div>
            )}
          </>
        ) : (
          <>
            <div className="flex items-center gap-2 whitespace-nowrap">
              <strong className="text-[#333333]">Iniciado el:</strong>
              <span>{fechaInicio}</span>
            </div>
            <div className="flex items-center gap-2 whitespace-nowrap">
              <strong className="text-[#333333]">Finalizado:</strong>
              <span>{ultimaActualizacion}</span>
            </div>
          </>
        )}
      </div>

      {/* Sección Derecha: Acción Contextual */}
      <div className="shrink-0 w-full md:w-auto flex justify-end">
        {estado === 'active' && (
          <Button
            type="button"
            variant="primary"
            onClick={() => onAction && onAction(evaluacion)}
            rightIcon="arrow_forward"
            className="w-full md:w-auto font-semibold px-8 py-2.5"
          >
            Comenzar
          </Button>
        )}

        {estado === 'in-progress' && (
          <Button
            type="button"
            variant="primary"
            onClick={() => onAction && onAction(evaluacion)}
            rightIcon="arrow_forward"
            className="w-full md:w-auto font-semibold px-8 py-2.5"
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
            className="w-full md:w-auto"
          >
            Ver
          </Button>
        )}

        {estado === 'expired' && (
          <Button type="button" variant="secondary" disabled className="w-full md:w-auto opacity-50">
            Expirado
          </Button>
        )}
      </div>
    </div>
  );
};

export default EvaluationCard;
