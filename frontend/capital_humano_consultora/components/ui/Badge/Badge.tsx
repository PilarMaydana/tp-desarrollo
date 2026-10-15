'use client';

import React from 'react';
import { EstadoEvaluacion } from '@/types/evaluacion';

export interface BadgeProps {
  status: EstadoEvaluacion;
  label?: string;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ status, label, className = '' }) => {
  const configs: Record<EstadoEvaluacion, { defaultText: string; bgClass: string; textClass: string; borderClass: string }> = {
    'in-progress': {
      defaultText: 'Estado: en proceso',
      bgClass: 'bg-[#E8F5E9]',
      textClass: 'text-[#1B8354]',
      borderClass: 'border-[#A5D6A7]',
    },
    finished: {
      defaultText: 'Estado: finalizado',
      bgClass: 'bg-[#EEEEEE]',
      textClass: 'text-[#4A4A4A]',
      borderClass: 'border-[#BDBDBD]',
    },
    expired: {
      defaultText: 'Estado: expirado',
      bgClass: 'bg-[#FFEBEE]',
      textClass: 'text-[#D32F2F]',
      borderClass: 'border-[#FFCDD2]',
    },
  };

  const current = configs[status] || configs['finished'];

  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs md:text-sm font-semibold border tracking-wide ${current.bgClass} ${current.textClass} ${current.borderClass} ${className}`}
    >
      <span className="w-2 h-2 rounded-full mr-2 bg-current opacity-75" />
      {label || current.defaultText}
    </span>
  );
};

export default Badge;
