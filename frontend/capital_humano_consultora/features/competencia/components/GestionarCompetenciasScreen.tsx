'use client';

import React, { useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { ROUTES } from '@/constants/routes';
import { CompetenciaResponse } from '@/types/competencia';
import { useCompetencias } from '../hooks/useCompetencias';

export const GestionarCompetenciasScreen: React.FC = () => {
  const router = useRouter();
  const { competencias, isLoading, error, recargarCompetencias } = useCompetencias();

  const handleNuevaCompetencia = () => {
    router.push(ROUTES.CONSULTOR.COMPETENCIAS_NUEVA);
  };

  const columns: Column<CompetenciaResponse>[] = useMemo(
    () => [
      {
        header: 'Código',
        accessor: 'codigo',
        width: '18%',
        render: (row) => (
          <span className="inline-flex items-center font-mono font-bold text-xs px-2.5 py-1 rounded-md bg-[#F4F0FF] text-[#3F2581] border border-[#D0BCFF]/60">
            {row.codigo}
          </span>
        ),
      },
      {
        header: 'Nombre',
        accessor: 'nombre',
        width: '32%',
        render: (row) => (
          <span className="font-bold text-[#1C1B1F] text-sm md:text-base">
            {row.nombre}
          </span>
        ),
      },
      {
        header: 'Descripción',
        accessor: 'descripcion',
        width: '50%',
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
    <div className="flex flex-col gap-5 w-full max-w-6xl mx-auto pb-12">
      {/* Migas de pan */}
      <nav
        aria-label="Migas de pan"
        className="flex items-center gap-2 text-sm text-[#757575] font-medium"
      >
        <span>Gestión Consultor</span>
        <span className="text-[#9E9E9E]">&gt;</span>
        <span className="font-semibold text-[#3F2581]">Competencias</span>
      </nav>

      {/* Cabecera Principal con botón Nueva Competencia */}
      <div className="bg-white rounded-2xl border border-[#D0BCFF] shadow-xs p-6 sm:p-8 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">
            Gestión de Competencias
          </h1>
          <p className="text-sm text-[#757575] mt-1">
            Listado de competencias evaluables registradas en el sistema.
          </p>
        </div>

        <div className="flex items-center shrink-0">
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

      {/* Estado de error real del backend */}
      {error && (
        <div
          role="alert"
          className="w-full p-5 bg-red-50 border border-red-200 rounded-xl text-sm font-medium text-[#D32F2F] flex flex-col sm:flex-row sm:items-center justify-between gap-4"
        >
          <span>{error}</span>
          <Button
            type="button"
            variant="secondary"
            size="sm"
            onClick={recargarCompetencias}
          >
            Reintentar
          </Button>
        </div>
      )}

      {/* Tabla mínima de competencias (cargando, vacío o con datos) */}
      {!error && (
        <DataTable
          columns={columns}
          data={competencias}
          keyExtractor={(row) => String(row.id ?? row.codigo)}
          isLoading={isLoading}
          emptyTitle="No hay competencias cargadas"
          emptyDescription="Registre una nueva competencia utilizando el botón «Nueva Competencia»."
          variant="soft-purple"
        />
      )}
    </div>
  );
};

export default GestionarCompetenciasScreen;
