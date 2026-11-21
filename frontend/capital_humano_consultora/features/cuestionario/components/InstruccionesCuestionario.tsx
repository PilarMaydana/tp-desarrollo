'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import type { CuestionarioErrorPanel, InstruccionesCuestionarioDTO } from '../types';

export interface InstruccionesCuestionarioProps {
  datos: InstruccionesCuestionarioDTO;
  isStarting: boolean;
  errorPanel: CuestionarioErrorPanel | null;
  onDismissError: () => void;
  onAceptar: () => void;
  onCancelar: () => void;
}

/**
 * Pantalla «Cuestionario – Instrucciones» (CU 026 - Pasos 1, 2 y Flujo 2.A).
 */
export const InstruccionesCuestionario: React.FC<InstruccionesCuestionarioProps> = ({
  datos,
  isStarting,
  errorPanel,
  onDismissError,
  onAceptar,
  onCancelar,
}) => {
  return (
    <section className="w-full max-w-3xl mx-auto bg-white rounded-2xl border border-[#E0E0E0] shadow-sm overflow-hidden">
      {/* Encabezado de tarjeta */}
      <div className="px-6 sm:px-8 py-6 bg-[#F4F0FF] border-b border-[#EADDFF]">
        <div className="flex items-center gap-3">
          <div
            className="w-11 h-11 rounded-xl bg-[#3F2581] text-white flex items-center justify-center shrink-0"
            aria-hidden="true"
          >
            <Icon name="assignment" size={24} />
          </div>
          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-[#3F2581] tracking-tight">
              Cuestionario – Instrucciones
            </h1>
            <p className="text-sm text-[#4F378A]">
              Lea atentamente las indicaciones antes de comenzar la evaluación.
            </p>
          </div>
        </div>
      </div>

      {/* Contenido */}
      <div className="p-6 sm:p-8 space-y-6">
        {errorPanel && (
          <AlertaErrores
            titulo={errorPanel.titulo}
            mensajeGeneral={errorPanel.mensajeGeneral}
            errores={errorPanel.detalles}
            onDismiss={onDismissError}
          />
        )}

        {/* Metadatos del cuestionario: puesto y tiempo máximo */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="flex items-start gap-3 p-4 rounded-xl bg-[#FAFAFA] border border-[#E5E5E5]">
            <div className="w-9 h-9 rounded-lg bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shrink-0">
              <Icon name="work" size={18} />
            </div>
            <div className="min-w-0">
              <p className="text-xs font-semibold uppercase tracking-wider text-[#757575]">
                Puesto a evaluar
              </p>
              <p className="text-sm sm:text-base font-bold text-[#000000] mt-0.5 break-words">
                {datos.nombrePuesto || 'Sin puesto especificado'}
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3 p-4 rounded-xl bg-[#FAFAFA] border border-[#E5E5E5]">
            <div className="w-9 h-9 rounded-lg bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shrink-0">
              <Icon name="timer" size={18} />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-[#757575]">
                Tiempo máximo
              </p>
              <p className="text-sm sm:text-base font-bold text-[#000000] mt-0.5">
                {datos.tiempoMaximoMinutos}{' '}
                {datos.tiempoMaximoMinutos === 1 ? 'minuto' : 'minutos'}
              </p>
            </div>
          </div>
        </div>

        {/* Texto de instrucciones respetando saltos de línea */}
        <div className="space-y-2">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-[#4F378A]">
            Instrucciones generales
          </h2>
          <div className="p-5 rounded-xl bg-[#FAFAFA] border border-[#E5E5E5] text-sm sm:text-base text-[#222222] leading-relaxed whitespace-pre-line">
            {datos.instrucciones}
          </div>
        </div>

        {/* Botonera: Cancelar (Flujo 2.A) y Aceptar (Paso 2) */}
        <div className="pt-4 border-t border-[#EEEEEE] flex flex-col-reverse sm:flex-row items-stretch sm:items-center justify-end gap-3">
          <Button
            type="button"
            variant="secondary"
            onClick={onCancelar}
            disabled={isStarting}
            className="focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
          >
            Cancelar
          </Button>
          <Button
            type="button"
            variant="primary"
            onClick={onAceptar}
            isLoading={isStarting}
            disabled={isStarting}
            rightIcon="arrow_forward"
            className="focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
          >
            Aceptar
          </Button>
        </div>
      </div>
    </section>
  );
};

export default InstruccionesCuestionario;
