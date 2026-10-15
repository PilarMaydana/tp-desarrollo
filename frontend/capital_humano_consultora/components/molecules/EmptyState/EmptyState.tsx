'use client';

import React from 'react';
import { Icon, IconName } from '@/components/ui/Icon';

export interface EmptyStateProps {
  icon?: IconName | string;
  title?: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon = 'help_outline',
  title = 'No hay elementos',
  description = 'No se encontraron registros para mostrar en esta vista.',
  action,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center p-8 text-center text-[#757575] rounded-xl border border-dashed border-[#D0BCFF] bg-[#FAF8FF] ${className}`}
    >
      <div className="w-12 h-12 rounded-full bg-[#EADDFF] flex items-center justify-center text-[#3F2581] mb-3">
        <Icon name={icon} size={24} />
      </div>
      <h4 className="text-base font-semibold text-[#3F2581] mb-1">{title}</h4>
      <p className="text-sm text-[#757575] max-w-sm mb-4">{description}</p>
      {action && <div>{action}</div>}
    </div>
  );
};

export default EmptyState;
