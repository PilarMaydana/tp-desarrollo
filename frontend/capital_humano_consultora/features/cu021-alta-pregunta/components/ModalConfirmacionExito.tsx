'use client';

import React, { useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface ModalConfirmacionExitoProps {
  isOpen: boolean;
  preguntaNombre: string;
  onConfirmarSi: () => void;
  onConfirmarNo: () => void;
}

export const ModalConfirmacionExito: React.FC<ModalConfirmacionExitoProps> = ({
  isOpen,
  preguntaNombre,
  onConfirmarSi,
  onConfirmarNo,
}) => {
  const siButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (isOpen) {
      // Auto-enfocar el botón "Sí" por defecto para agilizar la interacción con teclado
      const timer = setTimeout(() => {
        siButtonRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-exito-pregunta"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-8 flex flex-col items-center text-center space-y-5">
        {/* Ícono de confirmación */}
        <div className="w-14 h-14 rounded-full bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shadow-inner">
          <Icon name="check" size={34} />
        </div>

        {/* Título y Mensaje de Éxito según especificación del CU 021 */}
        <div className="space-y-2">
          <h2 id="modal-titulo-exito-pregunta" className="text-xl font-bold text-[#3F2581]">
            Alta Exitosa
          </h2>
          <p className="text-base text-gray-800 leading-relaxed">
            La Pregunta <strong className="text-[#3F2581] font-semibold">{preguntaNombre}</strong> se ha creado correctamente.
          </p>
          <p className="text-sm font-medium text-gray-600">
            ¿Desea cargar otra?
          </p>
        </div>

        {/* Botones No y Sí */}
        <div className="grid grid-cols-2 gap-3 w-full pt-2">
          <Button
            type="button"
            variant="secondary"
            onClick={onConfirmarNo}
            className="w-full text-base font-semibold"
          >
            No
          </Button>

          <Button
            ref={siButtonRef}
            type="button"
            variant="primary"
            onClick={onConfirmarSi}
            className="w-full text-base font-semibold"
          >
            Sí
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalConfirmacionExito;

