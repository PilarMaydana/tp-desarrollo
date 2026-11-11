'use client';

import React, { useMemo, useState } from 'react';
import { Icon } from '@/components/ui/Icon';
import { Button } from '@/components/ui/Button';
import { OpcionRespuestaItem } from '../types';

export interface GrillaOpcionesRespuestaProps {
  opciones: OpcionRespuestaItem[];
  onChangePonderacion: (index: number, ponderacion: number) => void;
  onUpdateOpcion?: (index: number, field: keyof OpcionRespuestaItem, value: string | number) => void;
  onAddOpcion?: () => void;
  onRemoveOpcion?: (index: number) => void;
  isCustomScale?: boolean;
  error?: string;
  disabled?: boolean;
}

export const GrillaOpcionesRespuesta: React.FC<GrillaOpcionesRespuestaProps> = ({
  opciones,
  onChangePonderacion,
  onUpdateOpcion,
  onAddOpcion,
  onRemoveOpcion,
  isCustomScale = false,
  error,
  disabled = false,
}) => {
  // Estado local para los strings mientras se editan, permitiendo comas y puntos
  const [inputsValores, setInputsValores] = useState<Record<number, string>>({});

  // Cálculo en tiempo real de la suma total de ponderaciones
  const sumaTotal = useMemo(() => {
    return opciones.reduce((acc, curr) => {
      const val = typeof curr.ponderacion === 'number' ? curr.ponderacion : Number(curr.ponderacion);
      return acc + (isNaN(val) ? 0 : val);
    }, 0);
  }, [opciones]);

  const esSumaValida = Math.abs(sumaTotal - 10.0) <= 0.001;

  // Manejo de cambio en el input de ponderación
  const handleInputChange = (index: number, rawValue: string) => {
    setInputsValores((prev) => ({ ...prev, [index]: rawValue }));

    // Reemplaza coma por punto para parsear
    const normalized = rawValue.replace(',', '.');
    if (normalized.trim() === '') {
      onChangePonderacion(index, 0);
      return;
    }

    const num = parseFloat(normalized);
    if (!isNaN(num)) {
      // Trunca entre 0 y 10
      let bounded = num;
      if (bounded < 0) bounded = 0;
      if (bounded > 10) bounded = 10;
      onChangePonderacion(index, bounded);
    }
  };

  const handleInputBlur = (index: number, rawValue: string) => {
    const normalized = rawValue.replace(',', '.');
    if (normalized.trim() === '') {
      onChangePonderacion(index, 0);
      setInputsValores((prev) => ({ ...prev, [index]: '0' }));
      return;
    }

    const num = parseFloat(normalized);
    if (!isNaN(num)) {
      let bounded = num;
      if (bounded < 0) bounded = 0;
      if (bounded > 10) bounded = 10;
      onChangePonderacion(index, bounded);
      setInputsValores((prev) => ({ ...prev, [index]: String(bounded) }));
    } else {
      onChangePonderacion(index, 0);
      setInputsValores((prev) => ({ ...prev, [index]: '0' }));
    }
  };

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
                  style={{ width: '200px' }}
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
              {opciones.map((opcion, index) => {
                const displayVal =
                  inputsValores[index] !== undefined
                    ? inputsValores[index]
                    : String(opcion.ponderacion ?? 0);

                return (
                  <tr
                    key={opcion.orden ?? index}
                    className="hover:bg-[#F4F0FF]/60 transition-colors"
                  >
                    {/* Columna 1: Orden (solo lectura, número solo sin círculo) */}
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
                      ) : (
                        <div className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2">
                          <span className="font-bold text-[#3F2581] tracking-wide shrink-0">
                            {opcion.nombre}
                          </span>
                          <span className="hidden sm:inline text-gray-400 font-normal">—</span>
                          <span className="text-gray-700 text-sm">
                            {opcion.descripcion}
                          </span>
                        </div>
                      )}
                    </td>

                    {/* Columna 3: Ponderación (editable numérico >= 0 y <= 10) */}
                    <td className="px-6 py-3.5 align-middle text-center">
                      <div className="flex items-center justify-center">
                        <input
                          type="text"
                          inputMode="decimal"
                          value={displayVal}
                          disabled={disabled}
                          onChange={(e) => handleInputChange(index, e.target.value)}
                          onBlur={(e) => handleInputBlur(index, e.target.value)}
                          aria-label={`Ponderación para opción ${opcion.orden}`}
                          className="w-24 text-center font-bold text-base bg-white text-[#1C1B1F] px-2.5 py-1.5 rounded-lg border border-[#3F2581]/50 focus:border-[#3F2581] focus:ring-2 focus:ring-[#3F2581]/20 outline-none transition-colors"
                        />
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
              })}
            </tbody>
          </table>
        </div>

        {/* Botón de agregar opción para escala personalizada si aplica */}
        {isCustomScale && onAddOpcion && (
          <div className="p-3 bg-[#F4F0FF] border-t border-[#D0BCFF] flex justify-end">
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
          </div>
        )}
      </div>

      {/* Advertencia en rojo debajo del recuadro si la suma es diferente de 10 */}
      {!esSumaValida && (
        <p className="text-xs text-[#D32F2F] font-semibold pl-1 flex items-center gap-1 mt-1">
          <Icon name="close" size={14} className="shrink-0" />
          <span>
            La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: {sumaTotal.toFixed(1)})
          </span>
        </p>
      )}

      {/* Mensaje de error adicional del resolver/schema */}
      {error && esSumaValida && (
        <p className="text-xs text-[#D32F2F] font-semibold pl-1 flex items-center gap-1 mt-1">
          <Icon name="close" size={14} className="shrink-0" />
          <span>{error}</span>
        </p>
      )}
    </div>
  );
};

export default GrillaOpcionesRespuesta;
