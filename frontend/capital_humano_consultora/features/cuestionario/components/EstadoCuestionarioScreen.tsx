'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import type { FaseCuestionario, MotivoFinalizacion } from '../types';

export interface EstadoCuestionarioScreenProps {
  fase: Extract<
    FaseCuestionario,
    'cargando' | 'no_encontrado' | 'no_disponible' | 'error_conexion' | 'finalizado'
  >;
  mensajeEstado?: string | null;
  motivoFinalizacion?: MotivoFinalizacion | null;
  mensajeFinalizacion?: string | null;
  onReintentar?: () => void;
  onSalir: () => void;
}

export const EstadoCuestionarioScreen: React.FC<EstadoCuestionarioScreenProps> = ({
  fase,
  mensajeEstado,
  motivoFinalizacion,
  mensajeFinalizacion,
  onReintentar,
  onSalir,
}) => {
  if (fase === 'cargando') {
    return (
      <section
        aria-live="polite"
        aria-busy="true"
        className="w-full max-w-xl mx-auto bg-white rounded-2xl border border-[#E0E0E0] shadow-sm p-8 sm:p-12 flex flex-col items-center text-center gap-4"
      >
        <div
          className="w-10 h-10 border-4 border-[#3F2581] border-t-transparent rounded-full animate-spin"
          aria-hidden="true"
        />
        <div className="space-y-1">
          <h1 className="text-xl font-bold text-[#3F2581]">
            Cargando cuestionario...
          </h1>
          <p className="text-sm text-[#757575]">
            Aguarde un instante mientras verificamos la disponibilidad de su evaluación.
          </p>
        </div>
      </section>
    );
  }

  if (fase === 'no_encontrado') {
    return (
      <section
        role="status"
        className="w-full max-w-xl mx-auto bg-white rounded-2xl border border-[#E0E0E0] shadow-sm p-8 sm:p-10 flex flex-col items-center text-center gap-5"
      >
        <div className="w-16 h-16 rounded-full bg-[#F4F0FF] text-[#3F2581] flex items-center justify-center">
          <Icon name="file_question" size={34} />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-[#3F2581]">
            Cuestionario no encontrado
          </h1>
          <p className="text-sm sm:text-base text-[#4A4A4A] leading-relaxed">
            {mensajeEstado ||
              'El cuestionario solicitado no existe o no se encuentra asignado a su usuario.'}
          </p>
        </div>
      </section>
    );
  }

  if (fase === 'no_disponible') {
    return (
      <section
        role="alert"
        className="w-full max-w-xl mx-auto bg-white rounded-2xl border-2 border-red-200 shadow-sm p-8 sm:p-10 flex flex-col items-center text-center gap-5"
      >
        <div className="w-16 h-16 rounded-full bg-red-50 text-[#D32F2F] flex items-center justify-center">
          <Icon name="error" size={34} />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-[#3F2581]">
            Cuestionario no disponible
          </h1>
          <p className="text-sm sm:text-base text-[#4A4A4A] leading-relaxed">
            {mensajeEstado ||
              'Este cuestionario ya no se encuentra disponible para ser respondido.'}
          </p>
        </div>
      </section>
    );
  }

  if (fase === 'error_conexion') {
    return (
      <section
        role="alert"
        className="w-full max-w-xl mx-auto bg-white rounded-2xl border-2 border-red-200 shadow-sm p-8 sm:p-10 flex flex-col items-center text-center gap-5"
      >
        <div className="w-16 h-16 rounded-full bg-red-50 text-[#D32F2F] flex items-center justify-center">
          <Icon name="error" size={34} />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-[#3F2581]">
            Error al cargar el cuestionario
          </h1>
          <p className="text-sm sm:text-base text-[#4A4A4A] leading-relaxed">
            {mensajeEstado ||
              'No fue posible comunicarse con el servidor. Verifique su conexión e intente nuevamente.'}
          </p>
        </div>
        {onReintentar && (
          <div className="pt-2">
            <Button
              type="button"
              variant="primary"
              onClick={onReintentar}
              className="focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
            >
              Reintentar
            </Button>
          </div>
        )}
      </section>
    );
  }

  // fase === 'finalizado'
  const esTiempoAgotado = motivoFinalizacion === 'tiempo_agotado';

  if (esTiempoAgotado) {
    return (
      <section
        role="status"
        className="w-full max-w-xl mx-auto bg-white rounded-2xl border-2 border-amber-300 shadow-sm p-8 sm:p-10 flex flex-col items-center text-center gap-5"
      >
        <div className="w-16 h-16 rounded-full bg-amber-50 text-amber-700 flex items-center justify-center">
          <Icon name="timer" size={34} />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-[#3F2581]">
            Tiempo máximo agotado
          </h1>
          <p className="text-sm sm:text-base text-[#4A4A4A] leading-relaxed">
            {mensajeFinalizacion ||
              'Se agotó el tiempo máximo para completar el cuestionario. Ya no es posible enviar nuevas respuestas.'}
          </p>
        </div>
        <div className="pt-2">
          <Button
            type="button"
            variant="primary"
            onClick={onSalir}
            className="focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
          >
            Salir
          </Button>
        </div>
      </section>
    );
  }

  // Paso 8: Finalizado con éxito (sin puntajes ni resultados ni retorno al cuestionario)
  return (
    <section
      role="status"
      className="w-full max-w-xl mx-auto bg-white rounded-2xl border-2 border-[#A5D6A7] shadow-sm p-8 sm:p-10 flex flex-col items-center text-center gap-5"
    >
      <div className="w-16 h-16 rounded-full bg-[#E8F5E9] text-[#1B8354] flex items-center justify-center">
        <Icon name="check_circle" size={36} />
      </div>
      <div className="space-y-2">
        <h1 className="text-2xl font-bold text-[#3F2581]">
          Cuestionario Finalizado
        </h1>
        <p className="text-base sm:text-lg font-medium text-[#1B8354] leading-relaxed">
          {mensajeFinalizacion || 'El cuestionario ha sido completado con éxito'}
        </p>
        <p className="text-sm text-[#757575] leading-relaxed pt-1">
          Sus respuestas fueron registradas correctamente. Gracias por su participación.
        </p>
      </div>
      <div className="pt-2">
        <Button
          type="button"
          variant="primary"
          onClick={onSalir}
          className="focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
        >
          Salir
        </Button>
      </div>
    </section>
  );
};

export default EstadoCuestionarioScreen;
