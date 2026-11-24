'use client';

import React from 'react';
import { NotchedField } from '@/components/molecules/NotchedField';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { AlertaErrores } from '@/features/cu021-alta-pregunta';
import { useBuscarPuestosOrdenMerito } from '../hooks/useBuscarPuestosOrdenMerito';
import { toUpperPreservingCaret } from '../utils/formatters';
import { PuestosOrdenMeritoTable } from './PuestosOrdenMeritoTable';
import type { PuestoOrdenMeritoDTO } from '../types';

export interface OrdenMeritoSearchScreenProps {
  onCancelar?: () => void;
  onEmitirReporte?: (puestoId: number, evaluacionIds?: number[]) => void;
}

export const OrdenMeritoSearchScreen: React.FC<OrdenMeritoSearchScreenProps> = ({
  onCancelar,
}) => {
  const {
    filtros,
    puestos,
    hasSearched,
    isLoading,
    localValidationError,
    backendError,
    dismissBackendError,
    handleFilterChange,
    handleBuscar,
    handleReintentar,
    handleCancelar,
  } = useBuscarPuestosOrdenMerito({ onCancelar });

  const handleSeleccionarPuesto = (puesto: PuestoOrdenMeritoDTO) => {
    // Etapa 3 conectará la consulta de evaluaciones y el modal de selección (pasos 5 y 6)
    void puesto;
  };

  return (
    <div className="flex flex-col gap-4 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan */}
      <nav
        aria-label="Migas de pan"
        className="flex items-center gap-2 text-sm text-[#757575] font-medium"
      >
        <span>Gestión Consultor</span>
        <span className="text-[#9E9E9E]" aria-hidden="true">
          &gt;
        </span>
        <span className="font-semibold text-[#3F2581]">Orden de Méritos</span>
      </nav>

      {/* Card principal */}
      <div className="bg-white rounded-2xl md:rounded-3xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 md:p-10 flex flex-col gap-6">
        {/* Cabecera */}
        <div className="flex flex-col border-b border-[#EADDFF]/60 pb-5">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            Emitir Orden de Mérito
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            Busque un puesto por código, nombre o empresa para emitir el orden de mérito de sus candidatos evaluados.
          </p>
        </div>

        {/* Sección de Criterios de Búsqueda (Paso 2 y 3) */}
        <form
          onSubmit={handleBuscar}
          noValidate
          className="flex flex-col gap-4 p-5 rounded-2xl bg-[#FBF9FE] border border-[#EADDFF]/80"
        >
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="text-sm font-bold uppercase tracking-wider text-[#3F2581]">
              Criterios de Búsqueda
            </h2>
            <span className="text-xs text-[#757575]">
              Ingrese al menos un criterio de búsqueda (se normaliza a mayúsculas)
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <NotchedField
              label="Código"
              labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]"
            >
              <Input
                id="filtro-codigo"
                name="codigo"
                aria-label="Código"
                placeholder="EJ: ING-DATA-01"
                value={filtros.codigo}
                disabled={isLoading}
                autoComplete="off"
                error={Boolean(localValidationError)}
                onChange={(e) => {
                  const upper = toUpperPreservingCaret(e);
                  handleFilterChange('codigo', upper);
                }}
                className="uppercase bg-white"
              />
            </NotchedField>

            <NotchedField
              label="Nombre del Puesto"
              labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]"
            >
              <Input
                id="filtro-nombre"
                name="nombre"
                aria-label="Nombre del Puesto"
                placeholder="EJ: INGENIERO"
                value={filtros.nombre}
                disabled={isLoading}
                autoComplete="off"
                error={Boolean(localValidationError)}
                onChange={(e) => {
                  const upper = toUpperPreservingCaret(e);
                  handleFilterChange('nombre', upper);
                }}
                className="uppercase bg-white"
              />
            </NotchedField>

            <NotchedField
              label="Empresa"
              labelBgClassName="bg-[linear-gradient(to_bottom,#FBF9FE_50%,#FFFFFF_50%)]"
            >
              <Input
                id="filtro-empresa"
                name="empresa"
                aria-label="Empresa"
                placeholder="EJ: DATA CORP"
                value={filtros.empresa}
                disabled={isLoading}
                autoComplete="off"
                error={Boolean(localValidationError)}
                onChange={(e) => {
                  const upper = toUpperPreservingCaret(e);
                  handleFilterChange('empresa', upper);
                }}
                className="uppercase bg-white"
              />
            </NotchedField>
          </div>

          {/* Error de validación local (sin llamada al backend) */}
          {localValidationError && (
            <div
              role="alert"
              className="flex items-center gap-1.5 text-sm font-semibold text-[#D32F2F] pl-1"
            >
              <Icon name="error" size={16} className="text-[#D32F2F] shrink-0" />
              <span>{localValidationError}</span>
            </div>
          )}

          {/* Botón Buscar */}
          <div className="flex items-center justify-end gap-3 pt-1">
            <Button
              type="submit"
              variant="primary"
              isLoading={isLoading}
              leftIcon="search"
              className="min-w-[130px] font-semibold"
            >
              Buscar
            </Button>
          </div>
        </form>

        {/* Error real del backend en la búsqueda */}
        {backendError && (
          <div className="flex flex-col gap-2">
            <AlertaErrores
              titulo={
                backendError.codigo === 'VALIDACION'
                  ? 'Error de validación'
                  : 'Error al consultar puestos'
              }
              mensajeGeneral={backendError.mensaje}
              errores={backendError.detalles}
              onDismiss={dismissBackendError}
            />
            {backendError.codigo !== 'VALIDACION' && (
              <div className="flex justify-end">
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={handleReintentar}
                >
                  Reintentar
                </Button>
              </div>
            )}
          </div>
        )}

        {/* Grilla de Resultados (Paso 4) */}
        <section aria-label="Resultados de búsqueda de puestos" className="flex flex-col gap-3">
          {hasSearched && !backendError && (
            <>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-[#3F2581]">
                  Puestos encontrados
                </h2>
                {!isLoading && (
                  <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#EADDFF] text-[#3F2581]">
                    {puestos.length}
                  </span>
                )}
              </div>

              <PuestosOrdenMeritoTable
                puestos={puestos}
                isLoading={isLoading}
                onEmitirOrdenMerito={handleSeleccionarPuesto}
              />
            </>
          )}

          {isLoading && !hasSearched && (
            <PuestosOrdenMeritoTable
              puestos={[]}
              isLoading={true}
              onEmitirOrdenMerito={handleSeleccionarPuesto}
            />
          )}

          {!isLoading && !hasSearched && !backendError && (
            <div className="w-full py-12 px-4 rounded-xl border border-dashed border-[#D0BCFF]/70 bg-[#FBF9FE]/50 text-center flex flex-col items-center justify-center">
              <span className="text-sm text-[#757575] font-medium">
                Ingrese al menos un criterio de búsqueda y presione «Buscar» para listar los puestos disponibles.
              </span>
            </div>
          )}
        </section>

        {/* Barra de acciones inferior (Flujo 2.A Cancelar) */}
        <ActionToolbar align="left" className="mt-4">
          <Button
            type="button"
            variant="secondary"
            onClick={handleCancelar}
            disabled={isLoading}
          >
            Cancelar
          </Button>
        </ActionToolbar>
      </div>
    </div>
  );
};

export default OrdenMeritoSearchScreen;
