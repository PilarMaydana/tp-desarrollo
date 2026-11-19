'use client';

import React, { useMemo, useState } from 'react';
import { Icon } from '@/components/ui/Icon';
import { Button } from '@/components/ui/Button';
import { OpcionRespuestaItem } from '../types';
import { calcularSumaPonderaciones } from '@/features/pregunta/schemas/modificarPregunta.schema';

export interface GrillaOpcionesRespuestaProps {
  opciones: OpcionRespuestaItem[];
  onChangePonderacion?: (index: number, ponderacion: number) => void;
  /**
   * Modo controlado de entrada textual (CU 022):
   * mantiene el texto tal como lo escribe el consultor (incluyendo vacío o negativos)
   * para permitir validación explícita y mostrar el total exacto en vivo.
   */
  onChangePonderacionRaw?: (index: number, rawValue: string) => void;
  onUpdateOpcion?: (index: number, field: keyof OpcionRespuestaItem, value: string | number) => void;
  onAddOpcion?: () => void;
  onRemoveOpcion?: (index: number) => void;
  isCustomScale?: boolean;
  error?: string;
  rowErrors?: Record<number, string>;
  disabled?: boolean;
  isLoading?: boolean;
}

export const GrillaOpcionesRespuesta: React.FC<GrillaOpcionesRespuestaProps> = ({
  opciones,
  onChangePonderacion,
  onChangePonderacionRaw,
  onUpdateOpcion,
  onAddOpcion,
  onRemoveOpcion,
  isCustomScale = false,
  error,
  rowErrors,
  disabled = false,
  isLoading = false,
}) => {
  // Estado local para los strings mientras se editan en modo legado (CU 021)
  const [inputsValores, setInputsValores] = useState<Record<number, string>>({});

  const isRawControlled = typeof onChangePonderacionRaw === 'function';

  // Cálculo exacto en tiempo real de la suma total de ponderaciones (aritmética decimal segura)
  const resumenSuma = useMemo(() => {
    const valores = opciones.map((opt, idx) => {
      if (isRawControlled) {
        return opt.ponderacionInput ?? '';
      }
      if (inputsValores[idx] !== undefined) {
        return inputsValores[idx];
      }
      return opt.ponderacion ?? 0;
    });
    return calcularSumaPonderaciones(valores);
  }, [opciones, isRawControlled, inputsValores]);

  const esSumaValida = resumenSuma.esDiezExacto;

  // Manejo de cambio en el input de ponderación
  const handleInputChange = (index: number, rawValue: string) => {
    if (isRawControlled && onChangePonderacionRaw) {
      onChangePonderacionRaw(index, rawValue);
      return;
    }

    setInputsValores((prev) => ({ ...prev, [index]: rawValue }));

    const normalized = rawValue.replace(',', '.');
    if (normalized.trim() === '') {
      onChangePonderacion?.(index, 0);
      return;
    }

    const num = parseFloat(normalized);
    if (!isNaN(num)) {
      let bounded = num;
      if (bounded < 0) bounded = 0;
      if (bounded > 10) bounded = 10;
      onChangePonderacion?.(index, bounded);
    }
  };

  const handleInputBlur = (index: number, rawValue: string) => {
    if (isRawControlled) {
      return;
    }

    const normalized = rawValue.replace(',', '.');
    if (normalized.trim() === '') {
      onChangePonderacion?.(index, 0);
      setInputsValores((prev) => ({ ...prev, [index]: '0' }));
      return;
    }

    const num = parseFloat(normalized);
    if (!isNaN(num)) {
      let bounded = num;
      if (bounded < 0) bounded = 0;
      if (bounded > 10) bounded = 10;
      onChangePonderacion?.(index, bounded);
      setInputsValores((prev) => ({ ...prev, [index]: String(bounded) }));
    } else {
      onChangePonderacion?.(index, 0);
      setInputsValores((prev) => ({ ...prev, [index]: '0' }));
    }
  };

  const hasRowErrors = Boolean(rowErrors && Object.keys(rowErrors).length > 0);

  return (
    <div className="flex flex-col gap-2 w-full">
      {/* Contenedor idéntico al DataTable del CU020 con cabecera en lila clarito */}
      <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF]">
                <th
                  style={{ width: '120px' }}
                  className="px-6 py-4 text-base font-bold tracking-wider uppercase text-center text-[#3F2581]"
                >
                  Orden
                </th>
                <th
                  className="px-6 py-4 text-base font-bold tracking-wider uppercase text-[#3F2581]"
                >
                  Descripción
                </th>
                <th
                  style={{ width: '220px' }}
                  className="px-6 py-4 text-base font-bold tracking-wider uppercase text-center text-[#3F2581]"
                >
                  Ponderación
                </th>
                {isCustomScale && (
                  <th
                    style={{ width: '90px' }}
                    className="px-4 py-4 text-base font-bold tracking-wider uppercase text-center text-[#3F2581]"
                  >
                    Acción
                  </th>
                )}
              </tr>
            </thead>

            <tbody className="divide-y divide-[#EADDFF]/50 text-sm">
              {isLoading ? (
                <tr>
                  <td
                    colSpan={isCustomScale ? 4 : 3}
                    className="px-6 py-8 text-center text-sm text-[#757575]"
                  >
                    <div className="inline-flex items-center gap-2.5">
                      <span className="w-4 h-4 border-2 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
                      <span>Cargando ítems de la escala seleccionada...</span>
                    </div>
                  </td>
                </tr>
              ) : opciones.length === 0 ? (
                <tr>
                  <td
                    colSpan={isCustomScale ? 4 : 3}
                    className="px-6 py-8 text-center text-sm text-[#757575]"
                  >
                    No hay opciones de respuesta cargadas.
                  </td>
                </tr>
              ) : (
                opciones.map((opcion, index) => {
                  const displayVal = isRawControlled
                    ? (opcion.ponderacionInput ?? '')
                    : inputsValores[index] !== undefined
                    ? inputsValores[index]
                    : String(opcion.ponderacion ?? 0);

                  const rowError = rowErrors?.[index];

                  const nombreTrim = (opcion.nombre ?? '').trim();
                  const descTrim = (opcion.descripcion ?? '').trim();
                  const sonIguales =
                    nombreTrim !== '' &&
                    descTrim !== '' &&
                    nombreTrim.toUpperCase() === descTrim.toUpperCase();

                  return (
                    <tr
                      key={`${opcion.orden ?? index}-${index}`}
                      className="hover:bg-[#F4F0FF]/60 transition-colors"
                    >
                      {/* Columna 1: Orden (solo lectura) */}
                      <td className="px-6 py-3.5 text-center align-middle">
                        <span className="font-bold text-base text-[#3F2581]">
                          {opcion.orden}
                        </span>
                      </td>

                      {/* Columna 2: Descripción (solo lectura o editable si es personalizada) */}
                      <td className="px-6 py-3.5 align-middle">
                        {isCustomScale ? (
                          <div className="flex flex-col gap-1.5 py-1">
                            <input
                              type="text"
                              placeholder="NOMBRE (EJ. A VECES)"
                              value={opcion.nombre}
                              disabled={disabled}
                              maxLength={100}
                              style={{ textTransform: 'uppercase' }}
                              onChange={(e) =>
                                onUpdateOpcion &&
                                onUpdateOpcion(index, 'nombre', e.target.value.toUpperCase())
                              }
                              className="w-full bg-white text-xs font-bold text-[#3F2581] px-3 py-1.5 rounded-md border border-[#3F2581]/40 focus:border-[#3F2581] uppercase outline-none"
                            />
                            <input
                              type="text"
                              placeholder="DESCRIPCIÓN DE LA OPCIÓN"
                              value={opcion.descripcion}
                              disabled={disabled}
                              maxLength={500}
                              style={{ textTransform: 'uppercase' }}
                              onChange={(e) =>
                                onUpdateOpcion &&
                                onUpdateOpcion(index, 'descripcion', e.target.value.toUpperCase())
                              }
                              className="w-full bg-white text-xs text-gray-700 px-3 py-1.5 rounded-md border border-gray-300 focus:border-[#3F2581] uppercase outline-none"
                            />
                          </div>
                        ) : sonIguales || !nombreTrim || !descTrim ? (
                          <span className="font-medium text-[#1C1B1F] text-sm">
                            {descTrim || nombreTrim}
                          </span>
                        ) : (
                          <div className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2">
                            <span className="font-bold text-[#3F2581] tracking-wide shrink-0">
                              {nombreTrim}
                            </span>
                            <span className="hidden sm:inline text-gray-400 font-normal">—</span>
                            <span className="text-gray-700 text-sm">
                              {descTrim}
                            </span>
                          </div>
                        )}
                      </td>

                      {/* Columna 3: Ponderación (editable numérico >= 0) */}
                      <td className="px-6 py-3.5 align-middle text-center">
                        <div className="flex flex-col items-center justify-center gap-1">
                          <input
                            type="text"
                            inputMode="decimal"
                            value={displayVal}
                            placeholder="0"
                            disabled={disabled}
                            onChange={(e) => handleInputChange(index, e.target.value)}
                            onBlur={(e) => handleInputBlur(index, e.target.value)}
                            aria-label={`Ponderación para opción ${opcion.orden}`}
                            aria-invalid={Boolean(rowError)}
                            className={`w-24 text-center font-bold text-base bg-white text-[#1C1B1F] px-2.5 py-1.5 rounded-lg border outline-none transition-colors disabled:bg-gray-100 disabled:text-gray-400 ${
                              rowError
                                ? 'border-[#D32F2F] focus:border-[#D32F2F] focus:ring-2 focus:ring-[#D32F2F]/20'
                                : 'border-[#3F2581]/50 focus:border-[#3F2581] focus:ring-2 focus:ring-[#3F2581]/20'
                            }`}
                          />
                          {rowError && (
                            <span className="text-[11px] text-[#D32F2F] font-medium leading-tight max-w-[190px]">
                              {rowError}
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Columna 4 (opcional): Eliminar fila para escala personalizada */}
                      {isCustomScale && (
                        <td className="px-4 py-3.5 align-middle text-center">
                          <button
                            type="button"
                            disabled={disabled || opciones.length <= 2}
                            onClick={() => onRemoveOpcion && onRemoveOpcion(index)}
                            title={
                              opciones.length <= 2
                                ? 'Se requieren al menos 2 opciones de respuesta'
                                : 'Eliminar esta opción'
                            }
                            className="p-1.5 text-gray-400 hover:text-[#D32F2F] disabled:opacity-30 disabled:cursor-not-allowed transition-colors rounded-md hover:bg-red-50"
                          >
                            <Icon name="trash" size={17} />
                          </button>
                        </td>
                      )}
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Barra de resumen con suma de ponderaciones en vivo ("Total: X / 10") */}
        <div className="px-6 py-3 bg-[#FBF9FE] border-t border-[#D0BCFF] flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2 text-xs text-[#757575]">
            <span>Las ponderaciones deben ser mayores o iguales a 0 y sumar exactamente 10.</span>
          </div>

          <div className="flex items-center gap-3 ml-auto">
            {isCustomScale && onAddOpcion && (
              <Button
                type="button"
                variant="secondary"
                size="sm"
                onClick={onAddOpcion}
                disabled={disabled}
                leftIcon="plus"
                className="text-xs py-1 px-3"
              >
                Agregar Opción
              </Button>
            )}

            <span
              data-testid="ponderaciones-total"
              className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-sm font-bold border ${
                esSumaValida
                  ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
                  : 'bg-red-50 text-[#D32F2F] border-red-200'
              }`}
            >
              <Icon name={esSumaValida ? 'check' : 'close'} size={15} className="shrink-0" />
              <span>Total: {resumenSuma.formatted} / 10</span>
            </span>
          </div>
        </div>
      </div>

      {/* Advertencia en rojo debajo del recuadro si la suma es diferente de 10 */}
      {!isLoading && !esSumaValida && !hasRowErrors && (
        <p className="text-xs text-[#D32F2F] font-semibold pl-1 flex items-center gap-1 mt-1">
          <Icon name="close" size={14} className="shrink-0" />
          <span>
            {resumenSuma.tieneNegativos
              ? 'Todas las ponderaciones deben ser mayores o iguales a 0 y sumar exactamente 10 puntos.'
              : resumenSuma.tieneIncompletosOInvalidos
              ? `Complete todas las ponderaciones con valores numéricos >= 0 (Total actual: ${resumenSuma.formatted} / 10).`
              : `La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: ${resumenSuma.formatted}).`}
          </span>
        </p>
      )}

      {/* Mensaje de error adicional del resolver/schema */}
      {error && (esSumaValida || hasRowErrors) && (
        <p className="text-xs text-[#D32F2F] font-semibold pl-1 flex items-center gap-1 mt-1">
          <Icon name="close" size={14} className="shrink-0" />
          <span>{error}</span>
        </p>
      )}
    </div>
  );
};

export default GrillaOpcionesRespuesta;
