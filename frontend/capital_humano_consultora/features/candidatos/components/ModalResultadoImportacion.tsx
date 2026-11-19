'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ImportacionCandidatosResponse } from '@/types/candidato';

export interface ModalResultadoImportacionProps {
  isOpen: boolean;
  data: ImportacionCandidatosResponse | null;
  onAceptar: () => void;
}

export const ModalResultadoImportacion: React.FC<ModalResultadoImportacionProps> = ({
  isOpen,
  data,
  onAceptar,
}) => {
  const aceptarButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      // Auto-enfocar el botón "Aceptar" para facilitar el uso con teclado (Enter)
      const timer = setTimeout(() => {
        aceptarButtonRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isOpen || !data) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-resultado"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-md bg-white rounded-2xl md:rounded-3xl shadow-xl border border-gray-200 p-6 sm:p-8 flex flex-col items-center text-center space-y-6">
        {/* Ícono de confirmación */}
        <div className="w-16 h-16 rounded-full bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shadow-inner">
          <Icon name="check_circle" size={38} className="text-[#3F2581]" />
        </div>

        {/* Título y Mensaje descriptivo */}
        <div className="space-y-1.5">
          <h2 id="modal-titulo-resultado" className="text-xl md:text-2xl font-bold text-[#3F2581]">
            Importación Exitosa
          </h2>
          <p className="text-sm text-gray-600">
            El archivo de candidatos ha sido procesado y sincronizado correctamente en el sistema.
          </p>
        </div>

        {/* Tarjetas de métricas claras devueltas por el backend */}
        <div className="w-full grid grid-cols-3 gap-3">
          <div className="bg-emerald-50/70 border border-emerald-200 rounded-xl p-3 flex flex-col items-center justify-center">
            <span className="text-2xl font-bold text-emerald-700">{data.candidatosCreados}</span>
            <span className="text-xs font-semibold text-emerald-800 mt-1 uppercase tracking-wide">Creados</span>
          </div>

          <div className="bg-blue-50/70 border border-blue-200 rounded-xl p-3 flex flex-col items-center justify-center">
            <span className="text-2xl font-bold text-blue-700">{data.candidatosActualizados}</span>
            <span className="text-xs font-semibold text-blue-800 mt-1 uppercase tracking-wide">Actualizados</span>
          </div>

          <div className="bg-gray-50 border border-gray-200 rounded-xl p-3 flex flex-col items-center justify-center">
            <span className="text-2xl font-bold text-gray-700">{data.candidatosEliminados}</span>
            <span className="text-xs font-semibold text-gray-800 mt-1 uppercase tracking-wide">Eliminados</span>
          </div>
        </div>

        {/* Botón Aceptar para volver a la pantalla principal */}
        <div className="w-full pt-1">
          <Button
            ref={aceptarButtonRef}
            type="button"
            variant="primary"
            onClick={onAceptar}
            className="w-full text-base font-semibold py-3 shadow-sm hover:shadow-md"
          >
            Aceptar
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalResultadoImportacion;
