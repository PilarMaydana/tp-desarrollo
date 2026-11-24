'use client';

import React from 'react';
import {
  formatFechaLocal,
  PLACEHOLDER_VACIO,
} from '../utils/formatters';
import type { FilaCandidatoReporteDTO } from '../types';

export interface ReporteGrupoEstadoTableProps {
  titulo: string;
  filas: FilaCandidatoReporteDTO[];
  badgeClassName?: string;
}

/**
 * Tabla para los grupos de cuestionarios NO completos (Incompletos, En proceso, Sin contestar, Activos).
 * - Observación (1): Puntaje obtenido siempre se muestra como "—".
 * - Observación (2): Datos adicionales muestra Inicio, Último ingreso y Accesos (null como "—").
 * - Key de React: siempre cuestionarioId.
 */
export const ReporteGrupoEstadoTable: React.FC<ReporteGrupoEstadoTableProps> = ({
  titulo,
  filas,
  badgeClassName = 'bg-gray-100 text-gray-800 border-gray-300',
}) => {
  if (filas.length === 0) return null;

  return (
    <section className="flex flex-col gap-3 print-section">
      <div className="flex items-center gap-2.5">
        <h2 className="text-base md:text-lg font-bold text-[#3F2581] print:text-black">
          {titulo}
        </h2>
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border print:border-gray-500 print:bg-white print:text-black ${badgeClassName}`}
        >
          {filas.length}
        </span>
      </div>

      <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white print:rounded-none print:border-gray-400 print:overflow-visible">
        <div className="overflow-x-auto print:overflow-visible">
          <table className="w-full text-left border-collapse text-sm print:text-xs">
            <thead>
              <tr className="bg-[#F4F0FF] text-[#3F2581] border-b border-[#D0BCFF] print:bg-gray-100 print:text-black print:border-gray-400">
                <th scope="col" className="px-4 py-3 font-bold uppercase text-xs">
                  Tipo doc.
                </th>
                <th scope="col" className="px-4 py-3 font-bold uppercase text-xs">
                  Nro. doc.
                </th>
                <th scope="col" className="px-4 py-3 font-bold uppercase text-xs">
                  Apellido
                </th>
                <th scope="col" className="px-4 py-3 font-bold uppercase text-xs">
                  Nombre
                </th>
                <th scope="col" className="px-4 py-3 font-bold uppercase text-xs">
                  Eval.
                </th>
                <th
                  scope="col"
                  className="px-4 py-3 font-bold uppercase text-xs text-right"
                >
                  Puntaje obtenido
                </th>
                <th scope="col" className="px-4 py-3 font-bold uppercase text-xs">
                  Datos adicionales
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#EADDFF]/60 print:divide-gray-300">
              {filas.map((fila) => (
                <tr
                  key={fila.cuestionarioId}
                  className="hover:bg-[#FBF9FE] print:hover:bg-transparent print-avoid-break"
                >
                  <td className="px-4 py-3 whitespace-nowrap font-medium text-[#333333] print:text-black">
                    {fila.tipoDocumento}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap tabular-nums text-[#333333] print:text-black">
                    {fila.nroDocumento}
                  </td>
                  <td className="px-4 py-3 font-semibold text-[#1C1B1F] print:text-black">
                    {fila.apellido}
                  </td>
                  <td className="px-4 py-3 text-[#1C1B1F] print:text-black">
                    {fila.nombre}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-xs font-medium text-[#4F378A] print:text-black">
                    Eval. #{fila.evaluacionId}
                  </td>
                  <td
                    className="px-4 py-3 text-right tabular-nums text-[#757575] print:text-black"
                    title="No se muestra puntaje en cuestionarios no completos"
                  >
                    {PLACEHOLDER_VACIO}
                  </td>
                  <td className="px-4 py-3 text-xs text-[#333333] print:text-black">
                    <div className="flex flex-col gap-0.5 whitespace-nowrap">
                      <span>
                        <strong className="font-semibold">Inicio:</strong>{' '}
                        {formatFechaLocal(fila.datosAdicionales.fechaInicio)}
                      </span>
                      <span>
                        <strong className="font-semibold">Último ingreso:</strong>{' '}
                        {formatFechaLocal(fila.datosAdicionales.ultimoIngreso)}
                      </span>
                      <span>
                        <strong className="font-semibold">Accesos:</strong>{' '}
                        {fila.datosAdicionales.cantidadAccesos}
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
};

export default ReporteGrupoEstadoTable;
