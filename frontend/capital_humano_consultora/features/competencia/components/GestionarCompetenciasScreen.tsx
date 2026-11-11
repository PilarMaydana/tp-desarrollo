'use client';

import React, { useMemo, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Select, SelectOption } from '@/components/ui/Select';
import { Icon } from '@/components/ui/Icon';
import { Badge } from '@/components/ui/Badge';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { ROUTES } from '@/constants/routes';
import { useCompetencias } from '../hooks/useCompetencias';
import { CompetenciaCatalogoItem } from '../services/competenciaService';

const ESTADO_OPTIONS: SelectOption[] = [
  { value: 'TODAS', label: 'Todos los estados' },
  { value: 'ACTIVA', label: 'Solo Activas' },
  { value: 'INACTIVA', label: 'Inactivas' },
];

export const GestionarCompetenciasScreen: React.FC = () => {
  const router = useRouter();
  const {
    competenciasPaginadas,
    isLoading,
    busqueda,
    filtroEstado,
    currentPage,
    totalPages,
    totalItems,
    startIndex,
    endIndex,
    selectedCompetenciaId,
    setSelectedCompetenciaId,
    competenciaDetalle,
    setCompetenciaDetalle,
    handleBusquedaChange,
    handleEstadoChange,
    handleLimpiarFiltros,
    setCurrentPage,
  } = useCompetencias();

  const handleNuevaCompetencia = () => {
    router.push(ROUTES.CONSULTOR.COMPETENCIAS_NUEVO);
  };

  const handleAltaFactor = useCallback(
    (competenciaId: number) => {
      router.push(`${ROUTES.CONSULTOR.FACTORES_NUEVO}?competenciaId=${competenciaId}`);
    },
    [router]
  );

  const columns: Column<CompetenciaCatalogoItem>[] = useMemo(
    () => [
      {
        header: 'Código',
        accessor: 'codigo',
        width: '14%',
        render: (row) => (
          <span className="inline-flex items-center font-mono font-bold text-xs px-2.5 py-1 rounded-md bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF]/60">
            {row.codigo}
          </span>
        ),
      },
      {
        header: 'Competencia / Descripción',
        accessor: 'nombre',
        width: '44%',
        render: (row) => (
          <div className="flex flex-col py-0.5">
            <span className="font-bold text-[#1C1B1F] text-sm md:text-base leading-snug">
              {row.nombre}
            </span>
            <span
              className="text-xs text-[#757575] line-clamp-1 mt-0.5"
              title={row.descripcion}
            >
              {row.descripcion || 'Sin descripción registrada'}
            </span>
          </div>
        ),
      },
      {
        header: 'Factores Def.',
        accessor: 'factoresCount',
        width: '15%',
        render: (row) => {
          const count = row.factoresCount ?? 0;
          return (
            <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-[#EADDFF] text-[#3F2581]">
              {count} {count === 1 ? 'Factor' : 'Factores'}
            </span>
          );
        },
      },
      {
        header: 'Estado',
        accessor: 'estado',
        width: '13%',
        render: (row) => {
          const isActiva = (row.estado || 'ACTIVA') === 'ACTIVA';
          return (
            <Badge
              status={isActiva ? 'in-progress' : 'finished'}
              label={isActiva ? 'Activa' : 'Inactiva'}
              className="text-xs! px-2.5! py-0.5!"
            />
          );
        },
      },
      {
        header: 'Acciones (ABM)',
        accessor: 'id',
        width: '14%',
        render: (row) => (
          <div
            className="flex items-center gap-1.5"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              title="Ver detalle de competencia"
              onClick={() => setCompetenciaDetalle(row)}
              className="p-2 rounded-lg text-[#3F2581] hover:bg-[#EADDFF]/60 transition-colors cursor-pointer"
            >
              <Icon name="visibility" size={18} />
            </button>
            <button
              type="button"
              title="Asociar factor a esta competencia"
              onClick={() => handleAltaFactor(row.id)}
              className="p-2 rounded-lg text-[#3F2581] hover:bg-[#EADDFF]/60 transition-colors cursor-pointer"
            >
              <Icon name="plus" size={18} />
            </button>
            <button
              type="button"
              title="Modificar competencia (próximamente)"
              onClick={() => router.push(`${ROUTES.EN_DESARROLLO}?modulo=Modificar+Competencia`)}
              className="p-2 rounded-lg text-[#757575] hover:text-[#3F2581] hover:bg-[#F4F0FF] transition-colors cursor-pointer"
            >
              <Icon name="edit" size={18} />
            </button>
            <button
              type="button"
              title="Dar de baja competencia (próximamente)"
              onClick={() => router.push(`${ROUTES.EN_DESARROLLO}?modulo=Baja+de+Competencia`)}
              className="p-2 rounded-lg text-[#757575] hover:text-[#D32F2F] hover:bg-red-50 transition-colors cursor-pointer"
            >
              <Icon name="trash" size={18} />
            </button>
          </div>
        ),
      },
    ],
    [router, setCompetenciaDetalle, handleAltaFactor]
  );

  return (
    <div className="flex flex-col gap-5 w-full max-w-6xl mx-auto pb-12">
      {/* Fila superior: Migas de pan y estado del catálogo */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <nav
          aria-label="Migas de pan"
          className="flex items-center gap-2 text-sm text-[#757575] font-medium"
        >
          <span>Gestión Consultor</span>
          <span className="text-[#9E9E9E]">&gt;</span>
          <span className="font-semibold text-[#3F2581]">Competencias</span>
          <span className="text-[#9E9E9E]">&gt;</span>
          <span>Catálogo General y Descriptores</span>
        </nav>
        <span className="text-xs text-[#757575] font-medium">
          Catálogo activo: {totalItems} {totalItems === 1 ? 'competencia' : 'competencias'}
        </span>
      </div>

      {/* 1. Cabecera Principal en Tarjeta Cohesiva */}
      <div className="bg-white rounded-2xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
              Gestión de Competencias
            </h1>
            <span className="inline-flex items-center px-3 py-0.5 rounded-full text-xs font-semibold bg-[#EADDFF] text-[#3F2581]">
              ABM Activo
            </span>
          </div>
          <p className="text-sm text-[#757575] mt-1">
            Alta, modificación, ponderación y parametrización de competencias organizacionales para evaluaciones de candidatos.
          </p>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          <Button
            type="button"
            variant="primary"
            leftIcon="plus"
            onClick={handleNuevaCompetencia}
          >
            Nueva Competencia
          </Button>
        </div>
      </div>

      {/* 2. Barra de Búsqueda y Filtros */}
      <div className="bg-white rounded-2xl border border-[#D0BCFF] shadow-xs p-4 sm:p-5 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        <div className="flex-1">
          <Input
            leftIcon="search"
            placeholder="Buscar por código, nombre o descriptor..."
            value={busqueda}
            onChange={(e) => handleBusquedaChange(e.target.value)}
          />
        </div>

        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
          <div className="w-full sm:w-52">
            <Select
              options={ESTADO_OPTIONS}
              value={filtroEstado}
              onChange={(e) => handleEstadoChange(e.target.value)}
            />
          </div>

          <Button
            type="button"
            variant="secondary"
            leftIcon="filter"
            onClick={handleLimpiarFiltros}
            disabled={!busqueda && filtroEstado === 'TODAS'}
          >
            Limpiar
          </Button>
        </div>
      </div>

      {/* 3. Grilla de Competencias Registradas + Pie de Paginación */}
      <div className="flex flex-col gap-3">
        <DataTable
          columns={columns}
          data={competenciasPaginadas}
          keyExtractor={(row) => String(row.id)}
          selectedId={selectedCompetenciaId}
          onSelectRow={(row) =>
            setSelectedCompetenciaId((prev) =>
              prev === String(row.id) ? null : String(row.id)
            )
          }
          isLoading={isLoading}
          emptyTitle="No se encontraron competencias"
          emptyDescription="No hay competencias que coincidan con el criterio de búsqueda ingresado, o aún no se han registrado competencias."
          variant="soft-purple"
        />

        {/* Pie de tabla: conteo y controles de paginación */}
        {!isLoading && totalItems > 0 && (
          <div className="bg-white rounded-xl border border-[#D0BCFF] px-6 py-3.5 flex flex-col sm:flex-row items-center justify-between gap-4 shadow-xs">
            <span className="text-xs sm:text-sm text-[#757575]">
              Mostrando <strong className="font-semibold text-[#1C1B1F]">{startIndex}</strong> a{' '}
              <strong className="font-semibold text-[#1C1B1F]">{endIndex}</strong> de{' '}
              <strong className="font-semibold text-[#1C1B1F]">{totalItems}</strong> competencias registradas
            </span>

            <div className="flex items-center gap-1.5">
              <Button
                type="button"
                variant="secondary"
                size="sm"
                disabled={currentPage <= 1}
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              >
                Anterior
              </Button>

              {Array.from({ length: totalPages }, (_, idx) => idx + 1).map((page) => {
                const isActive = page === currentPage;
                return (
                  <button
                    key={page}
                    type="button"
                    onClick={() => setCurrentPage(page)}
                    className={`w-8 h-8 rounded-md text-xs font-bold transition-colors cursor-pointer ${
                      isActive
                        ? 'bg-[#3F2581] text-white shadow-xs'
                        : 'bg-white text-[#3F2581] border border-[#D0BCFF] hover:bg-[#F4F0FF]'
                    }`}
                  >
                    {page}
                  </button>
                );
              })}

              <Button
                type="button"
                variant="secondary"
                size="sm"
                disabled={currentPage >= totalPages}
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              >
                Siguiente
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Modal de Detalle de Competencia */}
      {competenciaDetalle && (
        <div
          className="fixed inset-0 bg-black/40 z-50 backdrop-blur-xs flex items-center justify-center p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-detalle-competencia-title"
        >
          <div className="w-full max-w-lg bg-white rounded-2xl shadow-2xl border border-[#D0BCFF] p-6 sm:p-8 flex flex-col gap-5">
            <div className="flex items-start justify-between gap-4 border-b border-[#EADDFF]/60 pb-4">
              <div className="flex flex-col gap-1">
                <span className="inline-flex w-fit items-center font-mono font-bold text-xs px-2.5 py-0.5 rounded-md bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF]/60">
                  {competenciaDetalle.codigo}
                </span>
                <h2
                  id="modal-detalle-competencia-title"
                  className="text-xl font-bold text-[#3F2581] mt-1"
                >
                  {competenciaDetalle.nombre}
                </h2>
              </div>
              <button
                type="button"
                onClick={() => setCompetenciaDetalle(null)}
                className="p-1.5 rounded-lg text-[#757575] hover:text-[#1C1B1F] hover:bg-[#F6F6F6] transition-colors cursor-pointer"
                aria-label="Cerrar detalle"
              >
                <Icon name="close" size={20} />
              </button>
            </div>

            <div className="flex flex-col gap-3 text-sm">
              <div className="flex flex-col gap-1">
                <span className="text-xs font-bold uppercase tracking-wider text-[#3F2581]">
                  Descripción / Descriptor
                </span>
                <p className="text-[#333333] bg-[#FBF9FE] border border-[#EADDFF]/80 rounded-xl p-4 leading-relaxed">
                  {competenciaDetalle.descripcion || 'Sin descripción registrada.'}
                </p>
              </div>

              <div className="flex items-center justify-between pt-2">
                <span className="text-xs text-[#757575]">
                  Factores asociados:{' '}
                  <strong className="text-[#3F2581]">
                    {competenciaDetalle.factoresCount ?? 0}
                  </strong>
                </span>
                <Badge
                  status="in-progress"
                  label={competenciaDetalle.estado === 'INACTIVA' ? 'Inactiva' : 'Activa'}
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2 border-t border-[#EADDFF]/60">
              <Button
                type="button"
                variant="secondary-soft"
                size="sm"
                leftIcon="plus"
                onClick={() => {
                  const id = competenciaDetalle.id;
                  setCompetenciaDetalle(null);
                  handleAltaFactor(id);
                }}
              >
                Agregar Factor
              </Button>
              <Button
                type="button"
                variant="primary"
                size="sm"
                onClick={() => setCompetenciaDetalle(null)}
              >
                Cerrar
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GestionarCompetenciasScreen;

