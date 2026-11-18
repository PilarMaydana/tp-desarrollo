'use client';

import React, { useMemo } from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { OpcionRespuestaResumen } from '../types';
import {
  useGestionarOpcionesRespuesta,
  UseGestionarOpcionesRespuestaOptions,
} from '../hooks/useGestionarOpcionesRespuesta';

export interface GestionarOpcionesRespuestaScreenProps
  extends UseGestionarOpcionesRespuestaOptions {
  titulo?: string;
  subtitulo?: string;
}

export const GestionarOpcionesRespuestaScreen: React.FC<
  GestionarOpcionesRespuestaScreenProps
> = ({
  titulo = 'Gestionar Opciones de Respuestas',
  subtitulo = 'Búsqueda, consulta y administración de las escalas y opciones de respuestas.',
  onNavigateNuevo,
  onNavigateSalir,
}) => {
  const {
    nombreBusqueda,
    setNombreBusqueda,
    resultados,
    hasSearched,
    isLoading,
    error,
    selectedId,
    handleBuscar,
    handleSelectRow,
    handleNuevo,
    handleSalir,
  } = useGestionarOpcionesRespuesta({
    onNavigateNuevo,
    onNavigateSalir,
  });

  const columns: Column<OpcionRespuestaResumen>[] = useMemo(
    () => [
      {
        header: 'Nombre',
        accessor: 'nombre',
        width: '35%',
        render: (row) => (
          <span className="font-bold text-[#1C1B1F] text-sm md:text-base">
            {row.nombre}
          </span>
        ),
      },
      {
        header: 'Descripción',
        accessor: 'descripcion',
        width: '65%',
        render: (row) => (
          <span className="text-sm text-[#333333]">
            {row.descripcion}
          </span>
        ),
      },
    ],
    []
  );

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan */}
      <nav
        aria-label="Migas de pan"
        className="flex items-center gap-2 text-sm text-[#757575] font-medium"
      >
        <span>Gestión Consultor</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">
          Opciones de Respuestas
        </span>
      </nav>

      {/* Card principal */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 md:p-10 flex flex-col gap-6">
        {/* Cabecera */}
        <div className="flex flex-col border-b border-[#EADDFF]/60 pb-5">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            {titulo}
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            {subtitulo}
          </p>
        </div>

        {/* Sección de Criterios de Búsqueda */}
        <div className="flex flex-col gap-3 p-5 rounded-2xl bg-[#FBF9FE] border border-[#EADDFF]/80">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold uppercase tracking-wider text-[#3F2581]">
              Criterios de Búsqueda
            </h2>
            <span className="text-xs text-[#757575]">
              Ingrese datos o deje vacío para listar todas
            </span>
          </div>

          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4">
            <div className="flex-1">
              <NotchedField
                label="Nombre"
                labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]"
              >
                <Input
                  placeholder="Ej: FRECUENCIA"
                  value={nombreBusqueda}
                  disabled={isLoading}
                  maxLength={100}
                  onChange={(e) => setNombreBusqueda(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleBuscar();
                  }}
                  className="font-normal placeholder:font-normal bg-white"
                />
              </NotchedField>
            </div>

            <div className="flex items-center justify-end">
              <Button
                type="button"
                variant="primary"
                onClick={handleBuscar}
                isLoading={isLoading}
                leftIcon="search"
                size="md"
                className="w-full sm:w-auto min-w-[120px] font-semibold"
              >
                Buscar
              </Button>
            </div>
          </div>
        </div>

        {/* Grilla de Resultados */}
        <div className="flex flex-col gap-2">
          {/* Mensaje de error real del backend */}
          {error && (
            <div
              role="alert"
              className="w-full p-4 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F] flex flex-col sm:flex-row sm:items-center justify-between gap-3 my-2"
            >
              <span>{error}</span>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                onClick={handleBuscar}
              >
                Reintentar
              </Button>
            </div>
          )}

          {/* Estado de carga */}
          {isLoading && !error && (
            <div className="w-full bg-white rounded-xl border border-[#E0E0E0] p-10 flex flex-col items-center justify-center gap-3">
              <div className="w-8 h-8 border-3 border-[#3F2581] border-t-transparent rounded-full animate-spin" />
              <span className="text-sm text-[#757575] font-medium">
                Buscando opciones de respuesta...
              </span>
            </div>
          )}

          {/* Grilla con resultados o vacío tras buscar */}
          {!isLoading && !error && hasSearched && (
            <DataTable
              columns={columns}
              data={resultados}
              keyExtractor={(row) => String(row.id)}
              selectedId={selectedId}
              onSelectRow={handleSelectRow}
              isLoading={false}
              emptyTitle="No se encontraron opciones de respuesta"
              emptyDescription="No existen opciones de respuesta que coincidan con el criterio ingresado."
              variant="soft-purple"
            />
          )}

          {/* Estado inicial sin resultados (paso 2 del CU) */}
          {!isLoading && !error && !hasSearched && (
            <div className="w-full py-12 px-4 rounded-xl border border-dashed border-[#D0BCFF]/60 bg-[#FBF9FE]/50 text-center flex flex-col items-center justify-center">
              <span className="text-sm text-[#757575] font-medium">
                Ingrese un criterio y presione «Buscar» para listar las opciones de respuesta.
              </span>
            </div>
          )}
        </div>

        {/* Barra de Acciones CRUD y Salir */}
        <ActionToolbar align="between" className="pt-2">
          {/* Botón Salir */}
          <div>
            <Button
              type="button"
              variant="secondary"
              onClick={handleSalir}
            >
              Salir
            </Button>
          </div>

          {/* Botones de Gestión */}
          <div className="flex items-center gap-3">
            {/* TODO CU 31: Dar de baja Opción de Respuesta */}
            <div title="Próximamente">
              <Button
                type="button"
                variant="secondary-soft"
                disabled={true}
                leftIcon="trash"
              >
                Eliminar
              </Button>
            </div>

            {/* TODO CU 30: Modificar Opción de Respuesta */}
            <div title="Próximamente">
              <Button
                type="button"
                variant="secondary"
                disabled={true}
                leftIcon="edit"
              >
                Modificar
              </Button>
            </div>

            {/* CU 29: Dar de alta Opción de Respuesta */}
            <Button
              type="button"
              variant="primary"
              onClick={handleNuevo}
              leftIcon="plus"
            >
              Nuevo
            </Button>
          </div>
        </ActionToolbar>
      </div>
    </div>
  );
};

export default GestionarOpcionesRespuestaScreen;
