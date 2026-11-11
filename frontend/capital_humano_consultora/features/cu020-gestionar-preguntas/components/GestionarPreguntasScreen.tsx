'use client';

import React, { useMemo } from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Select, SelectOption } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { Pregunta } from '../types';
import { ModalConfirmacionBaja } from './ModalConfirmacionBaja';
import { ModalExitoBaja } from './ModalExitoBaja';
import { useGestionarPreguntas, UseGestionarPreguntasOptions } from '../hooks/useGestionarPreguntas';

export interface GestionarPreguntasScreenProps extends UseGestionarPreguntasOptions {
  titulo?: string;
  subtitulo?: string;
}

/**
 * Pantalla principal: CU 020 – Gestionar Preguntas
 * Estructurada en 3 secciones principales:
 * 1. Filtros de Búsqueda (Competencia, Factor, Nombre de Pregunta)
 * 2. Botones de Acción Superior (Nuevo, Modificar, Eliminar, Salir/Cancelar)
 * 3. Grilla de Resultados (Competencia, Factor, Pregunta con selección única)
 */
export const GestionarPreguntasScreen: React.FC<GestionarPreguntasScreenProps> = ({
  titulo = 'Gestionar Preguntas',
  subtitulo = 'Búsqueda, consulta y administración del banco de preguntas y reactivos evaluables.',
  onNavigateNuevo,
  onNavigateModificar,
  onCancelRedirect,
}) => {
  const {
    filtros,
    preguntas,
    isLoading,
    hasSearched,
    selectedPreguntaId,
    selectedPregunta,
    errorBusqueda,
    errorAccionSinSeleccion,
    competencias,
    factoresDisponibles,
    isLoadingFiltros,
    preguntaAEliminar,
    isDeleting,
    nombrePreguntaEliminada,
    handleFilterChange,
    handleBuscar,
    handleLimpiarFiltros,
    handleSelectRow,
    handleNuevo,
    handleModificar,
    handleEliminar,
    handleCancelarEliminar,
    handleConfirmarEliminar,
    handleCerrarModalExito,
  } = useGestionarPreguntas({
    onNavigateNuevo,
    onNavigateModificar,
    onCancelRedirect,
  });

  // Opciones para el selector de Competencia
  const competenciaOptions: SelectOption[] = useMemo(() => {
    return [
      { value: '', label: 'SELECCIONE COMPETENCIA' },
      ...competencias.map((comp) => ({
        value: comp.nombre,
        label: comp.codigo ? `${comp.codigo} - ${comp.nombre}` : comp.nombre,
      })),
    ];
  }, [competencias]);

  // Opciones para el selector de Factor (filtradas según la competencia seleccionada)
  const factorOptions: SelectOption[] = useMemo(() => {
    return [
      { value: '', label: 'SELECCIONE FACTOR' },
      ...factoresDisponibles.map((f) => ({
        value: f.nombre,
        label: f.codigo ? `${f.codigo} - ${f.nombre}` : f.nombre,
      })),
    ];
  }, [factoresDisponibles]);

  // Definición de columnas de la Grilla de Resultados según punto 1.3
  const columns: Column<Pregunta>[] = useMemo(
    () => [
      {
        header: 'Competencia',
        accessor: 'competencia',
        width: '25%',
        render: (row) => (
          <span className="font-medium text-[#3F2581]">
            {row.competencia || '-'}
          </span>
        ),
      },
      {
        header: 'Factor',
        accessor: 'factor',
        width: '25%',
        render: (row) => (
          <span className="text-[#333333] font-medium">
            {row.factor || '-'}
          </span>
        ),
      },
      {
        header: 'Pregunta',
        accessor: 'nombre',
        width: '50%',
        render: (row) => (
          <div className="flex flex-col py-1">
            <span className="font-semibold text-[#1C1B1F] leading-snug">
              {row.nombre}
            </span>
            {row.enunciado && row.enunciado !== row.nombre && (
              <span className="text-xs text-[#666666] line-clamp-1 mt-0.5" title={row.enunciado}>
                {row.enunciado}
              </span>
            )}
          </div>
        ),
      },
    ],
    []
  );

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan de navegación */}
      <nav aria-label="Migas de pan" className="flex items-center gap-2 text-sm text-[#757575] font-medium">
        <span>Gestión de Preguntas</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">Buscar Preguntas</span>
      </nav>

      {/* Card principal */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 md:p-10 flex flex-col gap-6">
        {/* Cabecera del caso de uso */}
        <div className="flex flex-col border-b border-[#EADDFF]/60 pb-5">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            {titulo}
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            {subtitulo}
          </p>
        </div>

        {/* 1. SECCIÓN: Criterios de Búsqueda (Filtros) */}
        <div className="flex flex-col gap-3 p-5 rounded-2xl bg-[#FBF9FE] border border-[#EADDFF]/80">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold uppercase tracking-wider text-[#3F2581]">
              Criterios de Búsqueda
            </h2>
            <span className="text-xs text-[#757575]">
              Ingrese datos en al menos uno de los tres campos
            </span>
          </div>

          {/* Grid de Filtros con NotchedField */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Filtro: Competencia */}
            <NotchedField label="Competencia" labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]">
              <Select
                options={competenciaOptions}
                value={filtros.competencia || ''}
                disabled={isLoadingFiltros || isLoading}
                onChange={(e) => handleFilterChange('competencia', e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleBuscar();
                }}
                error={Boolean(errorBusqueda)}
                className="font-normal bg-white"
              />
            </NotchedField>

            {/* Filtro: Factor */}
            <NotchedField label="Factor" labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]">
              <Select
                options={factorOptions}
                value={filtros.factor || ''}
                disabled={isLoadingFiltros || isLoading}
                onChange={(e) => handleFilterChange('factor', e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleBuscar();
                }}
                error={Boolean(errorBusqueda)}
                className="font-normal bg-white"
              />
            </NotchedField>

            {/* Filtro: Nombre de Pregunta */}
            <NotchedField label="Nombre de Pregunta" labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]">
              <Input
                placeholder="Ej: Liderazgo estratégico..."
                value={filtros.nombre || ''}
                disabled={isLoading}
                onChange={(e) => handleFilterChange('nombre', e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleBuscar();
                }}
                error={Boolean(errorBusqueda)}
                className="font-normal placeholder:font-normal bg-white"
              />
            </NotchedField>
          </div>

          {/* Mensaje de validación bajo los campos (sin cartel de advertencia) */}
          {errorBusqueda && (
            <p className="text-xs text-[#D32F2F] font-semibold pl-1 animate-in fade-in duration-150">
              {errorBusqueda}
            </p>
          )}

          {/* Botones de acción del filtro directamente integrados */}
          <div className="flex items-center justify-end gap-2.5 pt-1">
            <Button
              type="button"
              variant="secondary"
              onClick={handleLimpiarFiltros}
              disabled={isLoading || (!filtros.competencia && !filtros.factor && !filtros.nombre)}
              size="sm"
              className="min-w-[110px] font-semibold"
            >
              Limpiar
            </Button>

            <Button
              type="button"
              variant="primary"
              onClick={handleBuscar}
              isLoading={isLoading}
              leftIcon="search"
              size="sm"
              className="min-w-[110px] font-semibold"
            >
              Buscar
            </Button>
          </div>
        </div>

        {/* 2. SECCIÓN: Botones de Acción Superior */}
        <div className="flex flex-col gap-2 pt-4">
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            {/* Indicador de fila seleccionada o ayuda sin recuadro sobrecargado */}
            <div className="flex items-center">
              {selectedPregunta ? (
                <span className="text-xs text-[#3F2581] font-semibold animate-in fade-in duration-150">
                  Pregunta seleccionada: <span className="font-bold underline underline-offset-2">{selectedPregunta.nombre}</span>
                </span>
              ) : (
                <span className="text-xs text-[#757575] font-medium">
                  Seleccione una pregunta de la grilla para habilitar Modificar y Eliminar.
                </span>
              )}
            </div>

            {/* Grupo de Botones de Acción Superior (CU 020 punto 1.2) */}
            <div className="flex flex-wrap items-center gap-2 justify-end">
              {/* Botón Generar con IA */}
              <Button
                type="button"
                variant="secondary-soft"
                onClick={() => {
                  alert('Funcionalidad de Generación con IA en desarrollo');
                }}
                leftIcon="sparkles"
                size="sm"
                className="font-semibold text-[#3F2581] bg-[#F3EDF7] hover:bg-[#E8DEF8] border-[#D0BCFF]"
              >
                Generar con IA
              </Button>

              {/* Botón Nuevo -> CU 021 */}
              <Button
                type="button"
                variant="primary"
                onClick={handleNuevo}
                leftIcon="plus"
                size="sm"
                className="font-semibold shadow-xs min-w-[110px]"
              >
                Nuevo
              </Button>

              {/* Botón Modificar -> CU 022 */}
              <Button
                type="button"
                variant="secondary"
                onClick={handleModificar}
                disabled={!selectedPreguntaId}
                leftIcon="edit"
                size="sm"
                className="font-semibold min-w-[110px]"
              >
                Modificar
              </Button>

              {/* Botón Eliminar -> CU 023 */}
              <Button
                type="button"
                variant="secondary"
                onClick={handleEliminar}
                disabled={!selectedPreguntaId}
                leftIcon="trash"
                size="sm"
                className="font-semibold min-w-[110px]"
              >
                Eliminar
              </Button>
            </div>
          </div>

          {/* Mensaje de validación si se presionó Modificar/Eliminar sin selección */}
          {errorAccionSinSeleccion && (
            <p className="text-xs text-[#D32F2F] font-semibold pl-1 text-right animate-in fade-in duration-150">
              {errorAccionSinSeleccion}
            </p>
          )}
        </div>

        {/* 3. SECCIÓN: Grilla de Resultados */}
        <div className="flex flex-col gap-2">
          <DataTable
            columns={columns}
            data={preguntas}
            keyExtractor={(row) => String(row.id)}
            selectedId={selectedPreguntaId != null ? String(selectedPreguntaId) : null}
            onSelectRow={handleSelectRow}
            isLoading={isLoading}
            emptyTitle={hasSearched ? 'Sin resultados' : 'Banco de Preguntas'}
            emptyDescription={
              hasSearched
                ? 'No se encontraron preguntas que coincidan con los criterios ingresados.'
                : 'Ingrese datos en al menos uno de los criterios de búsqueda y presione Buscar para consultar las preguntas.'
            }
          />
        </div>
      </div>

      {/* Modal de Confirmación de Baja (CU 023) */}
      <ModalConfirmacionBaja
        isOpen={Boolean(preguntaAEliminar)}
        preguntaNombre={preguntaAEliminar?.nombre || ''}
        isSubmitting={isDeleting}
        onConfirmar={handleConfirmarEliminar}
        onCancelar={handleCancelarEliminar}
      />

      {/* Modal de Éxito Posterior a la Baja */}
      <ModalExitoBaja
        isOpen={Boolean(nombrePreguntaEliminada)}
        preguntaNombre={nombrePreguntaEliminada || ''}
        onAceptar={handleCerrarModalExito}
      />
    </div>
  );
};

export default GestionarPreguntasScreen;

