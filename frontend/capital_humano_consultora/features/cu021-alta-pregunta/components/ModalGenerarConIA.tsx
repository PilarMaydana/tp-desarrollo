'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';

export interface ModalGenerarConIAProps {
  isOpen: boolean;
  onClose: () => void;
  onApplyDraft?: (draft: {
    nombre: string;
    enunciado: string;
    descripcion: string;
  }) => void;
  factorNombreSeleccionado?: string;
}

export const ModalGenerarConIA: React.FC<ModalGenerarConIAProps> = ({
  isOpen,
  onClose,
  onApplyDraft,
  factorNombreSeleccionado,
}) => {
  if (!isOpen) return null;

  const handleInsertSample = () => {
    if (onApplyDraft) {
      onApplyDraft({
        nombre: 'RESOLUCIÓN DE CONFLICTOS BAJO PRESIÓN',
        enunciado: '¿DE QUÉ MANERA ABORDA Y TRANSMITE PROPUESTAS DE SOLUCIÓN CUANDO EXISTEN DISCREPANCIAS CRÍTICAS DENTRO DEL EQUIPO DE TRABAJO?',
        descripcion: 'EVALÚA CAPACIDAD DE COMUNICACIÓN ASERTIVA, EMPATÍA Y GENERACIÓN DE CONSENSOS ANTE SITUACIONES TENSAS.',
      });
    }
    onClose();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-titulo-ia"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl border-2 border-[#D0BCFF] p-6 sm:p-8 flex flex-col items-center text-center space-y-5">
        {/* Ícono de IA */}
        <div className="w-14 h-14 rounded-full bg-[#EADDFF] text-[#3F2581] flex items-center justify-center shadow-inner">
          <Icon name="help_outline" size={32} />
        </div>

        {/* Título y estado */}
        <div className="space-y-2">
          <div className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF]">
            Caso de Uso CU 033
          </div>
          <h2 id="modal-titulo-ia" className="text-xl font-bold text-[#3F2581]">
            Generar Pregunta con IA
          </h2>
          <p className="text-sm text-gray-700 leading-relaxed">
            El módulo <strong className="font-semibold text-[#3F2581]">CU 033 – Generar con IA</strong> se encuentra actualmente en desarrollo y permitirá formular reactivos psicométricos y criterios de ponderación automáticamente según la competencia y factor seleccionados.
          </p>

          {factorNombreSeleccionado ? (
            <div className="mt-3 p-3 bg-purple-50 rounded-xl border border-purple-200 text-xs text-purple-900 text-left">
              <span className="font-bold">Factor contextual: </span>
              <span>{factorNombreSeleccionado}</span>
            </div>
          ) : (
            <p className="text-xs text-amber-700 bg-amber-50 p-2.5 rounded-lg border border-amber-200 mt-2">
              Tip: Para una mejor generación asistida, seleccione primero una Competencia y Factor en el formulario.
            </p>
          )}
        </div>

        {/* Botones de acción */}
        <div className="flex flex-col sm:flex-row gap-3 w-full pt-2">
          {onApplyDraft && (
            <Button
              type="button"
              variant="secondary-soft"
              onClick={handleInsertSample}
              className="flex-1 text-sm font-semibold"
            >
              Cargar borrador de prueba
            </Button>
          )}

          <Button
            type="button"
            variant="primary"
            onClick={onClose}
            className="flex-1 text-sm font-semibold"
          >
            Entendido
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ModalGenerarConIA;

