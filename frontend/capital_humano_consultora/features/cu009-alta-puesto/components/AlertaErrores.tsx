'use client';

import React from 'react';
import { Icon } from '@/components/ui/Icon';
import { ErrorItem } from '../types';

export interface AlertaErroresProps {
  titulo?: string;
  mensajeGeneral?: string | null;
  errores?: ErrorItem[];
  onDismiss?: () => void;
}

export const AlertaErrores: React.FC<AlertaErroresProps> = ({
  titulo = 'Atención: No fue posible registrar el puesto',
  mensajeGeneral,
  errores = [],
  onDismiss,
}) => {
  const tieneContenido = Boolean(mensajeGeneral || errores.length > 0);

  if (!tieneContenido) return null;

  return (
    <div
      role="alert"
      aria-live="assertive"
      className="p-4 bg-red-50 border-2 border-red-300 text-[#D32F2F] rounded-xl flex items-start gap-3 shadow-xs animate-in fade-in duration-150"
    >
      <div className="shrink-0 mt-0.5">
        <Icon name="error" size={22} className="text-[#D32F2F]" />
      </div>

      <div className="flex-1 space-y-1.5">
        <h3 className="text-sm font-bold text-red-900">{titulo}</h3>

        {mensajeGeneral && (
          <p className="text-sm text-red-800 font-medium">{mensajeGeneral}</p>
        )}

        {errores.length > 0 && (
          <div className="pt-1">
            <p className="text-xs font-semibold text-red-800 uppercase tracking-wider mb-1">
              Detalle de omisiones o conflictos:
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-red-700">
              {errores.map((err, idx) => (
                <li key={`${err.campo ?? 'err'}-${idx}`}>
                  {err.campo && (
                    <strong className="font-semibold capitalize">
                      {err.campo}:{' '}
                    </strong>
                  )}
                  <span>{err.mensaje}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          className="shrink-0 p-1 text-red-500 hover:text-red-700 hover:bg-red-100 rounded-md transition-colors"
          title="Cerrar alerta"
        >
          <Icon name="close" size={18} />
        </button>
      )}
    </div>
  );
};

export default AlertaErrores;
