'use client';

import React, { useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import { useReporteOrdenMerito } from '../hooks/useReporteOrdenMerito';
import {
  formatFechaLocal,
  formatPuntaje,
  getLastOrdenMeritoSearch,
} from '../utils/formatters';
import { ReporteGrupoEstadoTable } from './ReporteGrupoEstadoTable';

export interface OrdenMeritoReporteScreenProps {
  puestoId: number;
  evaluacionIds?: number[];
  onVolver?: () => void;
}

export const OrdenMeritoReporteScreen: React.FC<
  OrdenMeritoReporteScreenProps
> = ({ puestoId, evaluacionIds, onVolver }) => {
  const router = useRouter();
  const { reporte, isLoading, error, reintentar } = useReporteOrdenMerito({
    puestoId,
    evaluacionIds,
  });

  const handleVolver = useCallback(() => {
    if (onVolver) {
      onVolver();
      return;
    }

    const lastSearch = getLastOrdenMeritoSearch();
    if (lastSearch) {
      router.push(`/orden-merito?${lastSearch}`);
      return;
    }

    if (typeof window !== 'undefined' && window.history.length > 1) {
      router.back();
      return;
    }

    router.push('/orden-merito');
  }, [onVolver, router]);

  const handleImprimir = useCallback(() => {
    window.print();
  }, []);

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12 print:max-w-none print:pb-0 print:gap-2">
      {/* Migas de pan (se ocultan al imprimir) */}
      <nav
        aria-label="Migas de pan"
        className="flex items-center gap-2 text-sm text-[#757575] font-medium print:hidden"
      >
        <span>Gestión Consultor</span>
        <span className="text-[#9E9E9E]" aria-hidden="true">
          &gt;
        </span>
        <button
          type="button"
          onClick={handleVolver}
          className="hover:text-[#3F2581] transition-colors cursor-pointer"
        >
          Orden de Méritos
        </button>
        <span className="text-[#9E9E9E]" aria-hidden="true">
          &gt;
        </span>
        <span className="font-semibold text-[#3F2581]">Reporte</span>
      </nav>

      {/* Barra superior de acciones fuera del reporte impreso */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-white rounded-2xl border border-[#D0BCFF] px-5 py-3.5 shadow-xs print:hidden">
        <Button
          type="button"
          variant="secondary"
          leftIcon="arrow_back"
          onClick={handleVolver}
        >
          Volver
        </Button>

        <Button
          type="button"
          variant="primary"
          leftIcon="printer"
          disabled={isLoading || Boolean(error) || !reporte}
          onClick={handleImprimir}
        >
          Imprimir
        </Button>
      </div>

      {/* Estado de carga */}
      {isLoading && (
        <div
          role="status"
          aria-live="polite"
          className="w-full bg-white rounded-2xl border border-[#D0BCFF] p-12 flex flex-col items-center justify-center gap-3 print:hidden"
        >
          <div className="w-9 h-9 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
          <span className="text-sm text-[#757575] font-medium">
            Generando reporte de orden de mérito...
          </span>
        </div>
      )}

      {/* Estado de error (404 / 422 / otros) */}
      {!isLoading && error && (
        <div className="bg-white rounded-2xl border border-[#D0BCFF] p-6 sm:p-8 flex flex-col gap-5 print:hidden">
          {error.status === 404 ? (
            <div className="flex flex-col items-center text-center py-6 gap-3">
              <div className="w-14 h-14 rounded-full bg-red-100 text-[#D32F2F] flex items-center justify-center">
                <Icon name="error" size={30} />
              </div>
              <h1 className="text-xl font-bold text-[#3F2581]">
                Puesto no encontrado
              </h1>
              <p className="text-sm text-[#4A4A4A] max-w-md">
                {error.mensaje !== 'Puesto no encontrado'
                  ? error.mensaje
                  : 'El puesto solicitado no existe o no se encuentra disponible.'}
              </p>
            </div>
          ) : (
            <AlertaErrores
              titulo={
                error.status === 422
                  ? 'No fue posible emitir el orden de mérito'
                  : 'Error al obtener el reporte'
              }
              mensajeGeneral={error.mensaje}
              errores={error.detalles}
            />
          )}

          <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
            <Button
              type="button"
              variant="secondary"
              leftIcon="arrow_back"
              onClick={handleVolver}
            >
              Volver
            </Button>
            {error.status !== 404 && error.status !== 422 && (
              <Button type="button" variant="primary" onClick={reintentar}>
                Reintentar
              </Button>
            )}
          </div>
        </div>
      )}

      {/* Reporte completo (Paso 7) */}
      {!isLoading && !error && reporte && (
        <article className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 md:p-10 flex flex-col gap-8 print:rounded-none print:border-0 print:shadow-none print:p-0 print:gap-5">
          {/* Encabezado del reporte */}
          <header className="flex flex-col gap-5 border-b-2 border-[#3F2581]/20 pb-6 print:border-black print:pb-4">
            <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
              <div>
                <p className="text-xs font-bold uppercase tracking-widest text-[#4F378A] print:text-gray-700">
                  Consultora Capit@l Humano
                </p>
                <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581] mt-0.5 print:text-black print:text-2xl">
                  Orden de Mérito
                </h1>
              </div>

              <div className="text-sm text-[#4A4A4A] sm:text-right print:text-black print:text-xs">
                <span className="font-semibold text-[#3F2581] print:text-black">
                  Fecha de emisión:
                </span>{' '}
                <span className="tabular-nums">
                  {formatFechaLocal(reporte.fechaEmision)}
                </span>
              </div>
            </div>

            {/* Datos del puesto */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-4 rounded-xl bg-[#FBF9FE] border border-[#EADDFF] print:bg-white print:border-gray-400 print:rounded-none print:p-3 print-avoid-break">
              <div className="flex flex-col">
                <span className="text-xs font-bold uppercase tracking-wider text-[#757575] print:text-gray-600">
                  Código del puesto
                </span>
                <span className="text-sm md:text-base font-bold text-[#3F2581] print:text-black">
                  {reporte.puesto.codigo}
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-bold uppercase tracking-wider text-[#757575] print:text-gray-600">
                  Nombre del puesto
                </span>
                <span className="text-sm md:text-base font-bold text-[#1C1B1F] print:text-black">
                  {reporte.puesto.nombre}
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-bold uppercase tracking-wider text-[#757575] print:text-gray-600">
                  Empresa
                </span>
                <span className="text-sm md:text-base font-semibold text-[#1C1B1F] print:text-black">
                  {reporte.puesto.empresa}
                </span>
              </div>
            </div>

            {/* Evaluaciones incluidas */}
            <div className="flex flex-col gap-2 print-avoid-break">
              <h2 className="text-xs font-bold uppercase tracking-wider text-[#3F2581] print:text-black">
                Evaluaciones incluidas ({reporte.evaluacionesIncluidas.length})
              </h2>
              <div className="flex flex-wrap gap-2">
                {reporte.evaluacionesIncluidas.map((ev) => (
                  <span
                    key={ev.evaluacionId}
                    className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF] print:bg-white print:text-black print:border-gray-400"
                  >
                    <span>Eval. #{ev.evaluacionId}</span>
                    <span className="text-[#757575] print:text-gray-700">·</span>
                    <span className="tabular-nums">
                      {formatFechaLocal(ev.fechaCreacion)}
                    </span>
                  </span>
                ))}
              </div>
            </div>

            {/* Tabla de competencias requeridas */}
            <div className="flex flex-col gap-2 print-avoid-break">
              <h2 className="text-xs font-bold uppercase tracking-wider text-[#3F2581] print:text-black">
                Competencias requeridas del puesto
              </h2>
              <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white print:rounded-none print:border-gray-400 print:overflow-visible">
                <div className="overflow-x-auto print:overflow-visible">
                  <table className="w-full text-left border-collapse text-sm print:text-xs">
                    <thead>
                      <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF] print:bg-gray-100 print:text-black print:border-gray-400">
                        <th
                          scope="col"
                          className="px-4 py-2.5 font-bold uppercase text-xs w-36"
                        >
                          Código
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-2.5 font-bold uppercase text-xs"
                        >
                          Nombre
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-2.5 font-bold uppercase text-xs text-right w-48"
                        >
                          Ponderación mínima
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#EADDFF]/60 print:divide-gray-300">
                      {reporte.competenciasRequeridas.map((comp) => (
                        <tr key={comp.competenciaId} className="print-avoid-break">
                          <td className="px-4 py-2.5 font-semibold text-[#3F2581] whitespace-nowrap print:text-black">
                            {comp.codigo}
                          </td>
                          <td className="px-4 py-2.5 text-[#1C1B1F] print:text-black">
                            {comp.nombre}
                          </td>
                          <td className="px-4 py-2.5 text-right font-bold tabular-nums text-[#3F2581] print:text-black">
                            {formatPuntaje(comp.ponderacionRequerida)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </header>

          {/* Resumen de totales */}
          <section
            aria-label="Resumen de totales del reporte"
            className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2.5 print:grid-cols-7 print:gap-1.5 print-avoid-break"
          >
            <div className="p-3 rounded-xl bg-[#3F2581] text-white flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border print:border-black print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.totalGeneral}
              </span>
              <span className="text-[11px] font-medium uppercase tracking-wide opacity-90 print:opacity-100">
                Total general
              </span>
            </div>

            <div className="p-3 rounded-xl bg-[#E8F5E9] border border-[#A5D6A7] text-[#1B8354] flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border-gray-400 print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.ordenDeMerito}
              </span>
              <span className="text-[11px] font-semibold">Orden de mérito</span>
            </div>

            <div className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border-gray-400 print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.noAlcanzaronMinimos}
              </span>
              <span className="text-[11px] font-semibold">Sin mínimos</span>
            </div>

            <div className="p-3 rounded-xl bg-gray-50 border border-gray-200 text-gray-800 flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border-gray-400 print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.incompletos}
              </span>
              <span className="text-[11px] font-semibold">Incompletos</span>
            </div>

            <div className="p-3 rounded-xl bg-gray-50 border border-gray-200 text-gray-800 flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border-gray-400 print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.enProceso}
              </span>
              <span className="text-[11px] font-semibold">En proceso</span>
            </div>

            <div className="p-3 rounded-xl bg-gray-50 border border-gray-200 text-gray-800 flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border-gray-400 print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.sinContestar}
              </span>
              <span className="text-[11px] font-semibold">Sin contestar</span>
            </div>

            <div className="p-3 rounded-xl bg-gray-50 border border-gray-200 text-gray-800 flex flex-col items-center justify-center text-center print:bg-white print:text-black print:border-gray-400 print:rounded-none print:p-2">
              <span className="text-lg font-bold tabular-nums">
                {reporte.totales.activos}
              </span>
              <span className="text-[11px] font-semibold">Activos</span>
            </div>
          </section>

          {/* SECCIÓN 1: Orden de mérito (siempre visible) */}
          <section className="flex flex-col gap-3 print-section">
            <div className="flex items-center gap-2.5">
              <h2 className="text-lg md:text-xl font-bold text-[#3F2581] print:text-black print:text-base">
                Orden de mérito ({reporte.ordenDeMerito.length})
              </h2>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-[#E8F5E9] text-[#1B8354] border border-[#A5D6A7] print:bg-white print:text-black print:border-gray-500">
                {reporte.ordenDeMerito.length}
              </span>
            </div>

            {reporte.ordenDeMerito.length === 0 ? (
              <p className="text-sm text-[#757575] italic px-4 py-6 rounded-xl border border-dashed border-[#D0BCFF] bg-[#FBF9FE] text-center print:bg-white print:border-gray-400 print:text-black">
                Sin candidatos
              </p>
            ) : (
              <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white print:rounded-none print:border-gray-400 print:overflow-visible">
                <div className="overflow-x-auto print:overflow-visible">
                  <table className="w-full text-left border-collapse text-sm print:text-xs">
                    <thead>
                      <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF] print:bg-gray-100 print:text-black print:border-gray-400">
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs text-center w-20"
                        >
                          Posición
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Tipo doc.
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Nro. doc.
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Apellido
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Nombre
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Eval.
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs text-right"
                        >
                          Puntaje obtenido
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Datos adicionales
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#EADDFF]/60 print:divide-gray-300">
                      {reporte.ordenDeMerito.map((fila) => (
                        <tr
                          key={fila.cuestionarioId}
                          className="hover:bg-[#F4F0FF]/50 print:hover:bg-transparent print-avoid-break"
                        >
                          <td className="px-4 py-3 text-center">
                            <span className="inline-flex items-center justify-center w-7 h-7 rounded-full bg-[#3F2581] text-white font-bold text-xs tabular-nums print:bg-white print:text-black print:border print:border-black">
                              {fila.posicion}
                            </span>
                          </td>
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
                          <td className="px-4 py-3 text-right font-bold tabular-nums text-[#1B8354] print:text-black">
                            {formatPuntaje(fila.puntajeObtenido)}
                          </td>
                          <td className="px-4 py-3 text-xs text-[#333333] print:text-black">
                            <div className="flex flex-col gap-0.5 whitespace-nowrap">
                              <span>
                                <strong className="font-semibold">Inicio:</strong>{' '}
                                {formatFechaLocal(fila.datosAdicionales.fechaInicio)}
                              </span>
                              <span>
                                <strong className="font-semibold">Fin:</strong>{' '}
                                {formatFechaLocal(fila.datosAdicionales.fechaFin)}
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
            )}
          </section>

          {/* SECCIÓN 2: Candidatos que no alcanzaron las ponderaciones mínimas (siempre visible) */}
          <section className="flex flex-col gap-3 print-section">
            <div className="flex items-center gap-2.5">
              <h2 className="text-lg md:text-xl font-bold text-[#3F2581] print:text-black print:text-base">
                Candidatos que no alcanzaron las ponderaciones mínimas (
                {reporte.noAlcanzaronMinimos.length})
              </h2>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-900 border border-amber-300 print:bg-white print:text-black print:border-gray-500">
                {reporte.noAlcanzaronMinimos.length}
              </span>
            </div>

            {reporte.noAlcanzaronMinimos.length === 0 ? (
              <p className="text-sm text-[#757575] italic px-4 py-6 rounded-xl border border-dashed border-[#D0BCFF] bg-[#FBF9FE] text-center print:bg-white print:border-gray-400 print:text-black">
                Sin candidatos
              </p>
            ) : (
              <div className="w-full overflow-hidden rounded-xl border border-[#D0BCFF] bg-white print:rounded-none print:border-gray-400 print:overflow-visible">
                <div className="overflow-x-auto print:overflow-visible">
                  <table className="w-full text-left border-collapse text-sm print:text-xs">
                    <thead>
                      <tr className="bg-[#EADDFF] text-[#3F2581] border-b border-[#D0BCFF] print:bg-gray-100 print:text-black print:border-gray-400">
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Tipo doc.
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Nro. doc.
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Apellido
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Nombre
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Eval.
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs text-right"
                        >
                          Puntaje obtenido
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Datos adicionales
                        </th>
                        <th
                          scope="col"
                          className="px-4 py-3 font-bold uppercase text-xs"
                        >
                          Competencias no alcanzadas
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#EADDFF]/60 print:divide-gray-300">
                      {reporte.noAlcanzaronMinimos.map((fila) => (
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
                          <td className="px-4 py-3 text-right font-bold tabular-nums text-[#D32F2F] print:text-black">
                            {formatPuntaje(fila.puntajeObtenido)}
                          </td>
                          <td className="px-4 py-3 text-xs text-[#333333] print:text-black">
                            <div className="flex flex-col gap-0.5 whitespace-nowrap">
                              <span>
                                <strong className="font-semibold">Inicio:</strong>{' '}
                                {formatFechaLocal(fila.datosAdicionales.fechaInicio)}
                              </span>
                              <span>
                                <strong className="font-semibold">Fin:</strong>{' '}
                                {formatFechaLocal(fila.datosAdicionales.fechaFin)}
                              </span>
                              <span>
                                <strong className="font-semibold">Accesos:</strong>{' '}
                                {fila.datosAdicionales.cantidadAccesos}
                              </span>
                            </div>
                          </td>
                          <td className="px-4 py-3 text-xs text-[#333333] print:text-black">
                            <ul className="space-y-1">
                              {fila.competenciasNoAlcanzadas.map((comp) => (
                                <li
                                  key={comp.competenciaId}
                                  className="flex flex-wrap items-baseline gap-1"
                                >
                                  <span className="font-semibold text-[#1C1B1F] print:text-black">
                                    {comp.competencia}
                                  </span>
                                  <span className="text-[#757575] print:text-gray-700">
                                    ({comp.codigo}):
                                  </span>
                                  <span className="font-bold tabular-nums text-[#D32F2F] print:text-black">
                                    {formatPuntaje(comp.obtenido)} /{' '}
                                    {formatPuntaje(comp.requerido)}
                                  </span>
                                </li>
                              ))}
                            </ul>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </section>

          {/* SECCIÓN 3: Resto agrupado por estado (solo se muestran los grupos con filas) */}
          <ReporteGrupoEstadoTable
            titulo="Incompletos"
            filas={reporte.incompletos}
            badgeClassName="bg-orange-50 text-orange-800 border-orange-200"
          />
          <ReporteGrupoEstadoTable
            titulo="En proceso"
            filas={reporte.enProceso}
            badgeClassName="bg-blue-50 text-blue-800 border-blue-200"
          />
          <ReporteGrupoEstadoTable
            titulo="Sin contestar"
            filas={reporte.sinContestar}
            badgeClassName="bg-red-50 text-red-800 border-red-200"
          />
          <ReporteGrupoEstadoTable
            titulo="Activos (no iniciados)"
            filas={reporte.activos}
            badgeClassName="bg-purple-50 text-[#3F2581] border-[#D0BCFF]"
          />
        </article>
      )}
    </div>
  );
};

export default OrdenMeritoReporteScreen;
