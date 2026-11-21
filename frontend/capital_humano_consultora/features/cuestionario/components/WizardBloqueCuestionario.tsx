'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import type {
  BloqueCuestionarioDTO,
  CuestionarioErrorPanel,
  PreguntaAsignadaDTO,
} from '../types';

export interface WizardBloqueCuestionarioProps {
  nombrePuesto: string | null;
  bloqueActual: BloqueCuestionarioDTO;
  totalBloques: number;
  preguntasOrdenadas: PreguntaAsignadaDTO[];
  esUltimoBloque: boolean;
  segundosRestantes: number;
  tiempoFormateado: string;
  respuestasSeleccionadas: Record<number, number>;
  preguntasFaltantesIds: number[];
  errorPanel: CuestionarioErrorPanel | null;
  isSubmitting: boolean;
  tiempoAgotado: boolean;
  onSeleccionarOpcion: (preguntaAsignadaId: number, opcionId: number) => void;
  onEnviarBloque: () => void;
  onDismissError: () => void;
}

/**
 * Wizard de bloques del cuestionario (CU 026 - Pasos 3 a 8, Flujos 6.A y 8.A).
 * No incluye botón «Anterior» (los bloques ya enviados no pueden volver a editarse).
 * Nunca muestra ni calcula puntajes ni ponderaciones.
 */
export const WizardBloqueCuestionario: React.FC<WizardBloqueCuestionarioProps> = ({
  nombrePuesto,
  bloqueActual,
  totalBloques,
  preguntasOrdenadas,
  esUltimoBloque,
  segundosRestantes,
  tiempoFormateado,
  respuestasSeleccionadas,
  preguntasFaltantesIds,
  errorPanel,
  isSubmitting,
  tiempoAgotado,
  onSeleccionarOpcion,
  onEnviarBloque,
  onDismissError,
}) => {
  const progresoPorcentaje =
    totalBloques > 0
      ? Math.min(100, Math.round((bloqueActual.numero / totalBloques) * 100))
      : 0;

  const cantidadRespondidas = preguntasOrdenadas.filter(
    (p) => respuestasSeleccionadas[p.preguntaAsignadaId] !== undefined
  ).length;

  const esTiempoCritico = segundosRestantes <= 60;
  const esTiempoBajo = segundosRestantes > 60 && segundosRestantes <= 300;

  const timerClasses = esTiempoCritico
    ? 'bg-red-50 text-[#D32F2F] border-red-300'
    : esTiempoBajo
    ? 'bg-amber-50 text-amber-800 border-amber-300'
    : 'bg-[#F4F0FF] text-[#3F2581] border-[#D0BCFF]';

  const bloqueDeshabilitado = tiempoAgotado || isSubmitting;

  return (
    <div className="w-full space-y-6">
      {/* Encabezado del wizard: paso actual, barra de progreso y contador regresivo */}
      <section className="bg-white rounded-2xl border border-[#E0E0E0] shadow-sm p-5 sm:p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-xl sm:text-2xl font-bold text-[#3F2581] tracking-tight">
              Paso {bloqueActual.numero} de {totalBloques}
            </h1>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-[#757575]">
              {nombrePuesto && (
                <span className="font-medium text-[#4A4A4A]">{nombrePuesto}</span>
              )}
              {nombrePuesto && <span aria-hidden="true">•</span>}
              <span>
                Respondidas en este bloque: {cantidadRespondidas} de{' '}
                {preguntasOrdenadas.length}
              </span>
            </div>
          </div>

          {/* Contador regresivo (mm:ss) */}
          <div
            aria-label={`Tiempo restante: ${tiempoFormateado}`}
            className={`inline-flex items-center gap-2.5 px-4 py-2.5 rounded-xl border-2 shrink-0 self-start sm:self-auto ${timerClasses}`}
          >
            <Icon name="timer" size={20} aria-hidden="true" />
            <div className="flex flex-col leading-tight">
              <span className="text-[11px] font-semibold uppercase tracking-wider opacity-80">
                Tiempo restante
              </span>
              <span className="text-lg font-bold tabular-nums tracking-tight">
                {tiempoFormateado}
              </span>
            </div>
          </div>
        </div>

        {/* Barra de progreso */}
        <div className="space-y-1.5">
          <div
            role="progressbar"
            aria-label="Progreso del cuestionario"
            aria-valuenow={bloqueActual.numero}
            aria-valuemin={1}
            aria-valuemax={totalBloques}
            className="w-full h-2.5 bg-[#EBEBEB] rounded-full overflow-hidden"
          >
            <div
              className="h-full bg-[#3F2581] rounded-full transition-all duration-300"
              style={{ width: `${progresoPorcentaje}%` }}
            />
          </div>
          <div className="flex items-center justify-between text-xs text-[#757575]">
            <span>
              Bloque {bloqueActual.numero} de {totalBloques}
            </span>
            <span className="font-medium text-[#4F378A]">{progresoPorcentaje}%</span>
          </div>
        </div>
      </section>

      {/* Panel de errores (en flujo normal del documento: nunca tapa preguntas ni botones) */}
      {errorPanel && (
        <AlertaErrores
          titulo={errorPanel.titulo}
          mensajeGeneral={errorPanel.mensajeGeneral}
          errores={errorPanel.detalles}
          onDismiss={onDismissError}
        />
      )}

      {/* Listado de preguntas del bloque actual ordenadas por nroOrden */}
      <div className="space-y-5">
        {preguntasOrdenadas.map((pregunta) => {
          const faltaResponder = preguntasFaltantesIds.includes(
            pregunta.preguntaAsignadaId
          );
          const opcionSeleccionadaId =
            respuestasSeleccionadas[pregunta.preguntaAsignadaId];
          const opcionesOrdenadas = [...pregunta.opciones].sort(
            (a, b) => a.orden - b.orden
          );
          const errorId = `error-pregunta-${pregunta.preguntaAsignadaId}`;

          return (
            <fieldset
              key={pregunta.preguntaAsignadaId}
              disabled={bloqueDeshabilitado}
              aria-invalid={faltaResponder || undefined}
              aria-describedby={faltaResponder ? errorId : undefined}
              className={`bg-white rounded-2xl border-2 shadow-xs p-5 sm:p-6 transition-colors ${
                faltaResponder
                  ? 'border-[#D32F2F] bg-red-50/40'
                  : 'border-[#E0E0E0]'
              }`}
            >
              <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
                <span
                  className={`inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-bold uppercase tracking-wider ${
                    faltaResponder
                      ? 'bg-red-100 text-[#D32F2F]'
                      : 'bg-[#F4F0FF] text-[#3F2581]'
                  }`}
                >
                  Pregunta N.° {pregunta.nroOrden}
                </span>

                {faltaResponder && (
                  <span
                    id={errorId}
                    className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-semibold text-[#D32F2F]"
                  >
                    <Icon name="error" size={16} aria-hidden="true" />
                    Pregunta sin responder: seleccione una opción
                  </span>
                )}
              </div>

              <legend className="text-base sm:text-lg font-semibold text-[#111111] leading-snug mb-4 block w-full">
                {pregunta.enunciado}
              </legend>

              <div className="space-y-2.5" role="radiogroup">
                {opcionesOrdenadas.map((opcion) => {
                  const isChecked = opcionSeleccionadaId === opcion.id;
                  const inputId = `pregunta-${pregunta.preguntaAsignadaId}-opcion-${opcion.id}`;

                  return (
                    <label
                      key={opcion.id}
                      htmlFor={inputId}
                      className={`flex items-start gap-3 p-3.5 rounded-xl border transition-all ${
                        bloqueDeshabilitado
                          ? 'cursor-not-allowed opacity-70'
                          : 'cursor-pointer'
                      } ${
                        isChecked
                          ? 'border-[#3F2581] bg-[#F4F0FF] shadow-xs'
                          : 'border-[#DCDCDC] bg-white hover:border-[#896ECB] hover:bg-[#FAF8FF]'
                      }`}
                    >
                      <input
                        id={inputId}
                        type="radio"
                        name={`pregunta-${pregunta.preguntaAsignadaId}`}
                        value={opcion.id}
                        checked={isChecked}
                        disabled={bloqueDeshabilitado}
                        onChange={() =>
                          onSeleccionarOpcion(pregunta.preguntaAsignadaId, opcion.id)
                        }
                        className="mt-0.5 h-4 w-4 shrink-0 accent-[#3F2581] focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
                      />
                      <span
                        className={`text-sm sm:text-base leading-snug ${
                          isChecked
                            ? 'font-semibold text-[#2D1B60]'
                            : 'font-normal text-[#222222]'
                        }`}
                      >
                        {opcion.descripcion}
                      </span>
                    </label>
                  );
                })}
              </div>
            </fieldset>
          );
        })}
      </div>

      {/* Pie de acciones: solo Siguiente o Finalizar (sin botón Anterior) */}
      <div className="bg-white rounded-2xl border border-[#E0E0E0] shadow-xs p-5 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <p className="text-xs sm:text-sm text-[#757575]">
          {esUltimoBloque
            ? 'Al presionar «Finalizar» se registrarán sus respuestas y concluirá el cuestionario.'
            : 'Una vez enviado el bloque no podrá volver a modificar sus respuestas.'}
        </p>

        <Button
          type="button"
          variant="primary"
          size="md"
          onClick={onEnviarBloque}
          isLoading={isSubmitting}
          disabled={bloqueDeshabilitado}
          rightIcon={esUltimoBloque ? 'check' : 'arrow_forward'}
          className="shrink-0 font-semibold focus-visible:ring-2 focus-visible:ring-[#3F2581] focus-visible:ring-offset-2"
        >
          {esUltimoBloque ? 'Finalizar' : 'Siguiente'}
        </Button>
      </div>
    </div>
  );
};

export default WizardBloqueCuestionario;
