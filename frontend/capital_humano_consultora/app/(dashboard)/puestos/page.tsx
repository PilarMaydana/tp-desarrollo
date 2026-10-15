'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { usePuestos } from '@/hooks/usePuestos';
import { Puesto } from '@/types/puesto';
import { SearchFilterBar } from '@/components/molecules/SearchFilterBar';
import { DataTable, Column } from '@/components/organisms/DataTable';
import { ActionToolbar } from '@/components/organisms/ActionToolbar';
import { Button } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';

export default function GestionarPuestosPage() {
  const router = useRouter();
  const {
    puestos,
    empresas,
    isLoading,
    selectedPuestoId,
    setSelectedPuestoId,
    fetchPuestos,
    deletePuesto,
  } = usePuestos();

  const handleSearch = (filters: Record<string, string>) => {
    fetchPuestos({
      codigo: filters.codigo,
      nombrePuesto: filters.nombrePuesto,
      empresaId: filters.empresaId,
    });
  };

  const handleDelete = async () => {
    if (!selectedPuestoId) return;
    const confirmed = window.confirm('¿Está seguro de eliminar el puesto seleccionado?');
    if (confirmed) {
      await deletePuesto(selectedPuestoId);
    }
  };

  const handleModify = () => {
    if (!selectedPuestoId) return;
    router.push(`${ROUTES.CONSULTOR.PUESTOS_NUEVO}?id=${selectedPuestoId}`);
  };

  const handleNew = () => {
    router.push(ROUTES.CONSULTOR.PUESTOS_NUEVO);
  };

  const columns: Column<Puesto>[] = [
    {
      header: 'Código',
      accessor: 'codigo',
      width: '15%',
    },
    {
      header: 'Nombre del Puesto',
      accessor: 'nombrePuesto',
      width: '35%',
    },
    {
      header: 'Empresa',
      accessor: 'empresaNombre',
      width: '25%',
    },
    {
      header: 'Competencias',
      accessor: 'competencias',
      width: '15%',
      render: (row) => (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-[#EADDFF] text-[#3F2581]">
          {row.competencias.length} definidas
        </span>
      ),
    },
    {
      header: 'Estado',
      accessor: 'estado',
      width: '10%',
      render: (row) => (
        <span
          className={`text-xs font-bold px-2 py-1 rounded-md ${
            row.estado === 'ACTIVO'
              ? 'text-[#1B8354] bg-[#E8F5E9]'
              : 'text-[#757575] bg-[#EEEEEE]'
          }`}
        >
          {row.estado || 'ACTIVO'}
        </span>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6 w-full">
      {/* Título de la pantalla */}
      <div className="flex flex-col">
        <h1 className="text-2xl md:text-3xl font-bold text-[#3F2581]">Gestionar Puestos</h1>
        <p className="text-sm text-[#757575] mt-1">
          Búsqueda, visualización y administración de los puestos de trabajo registrados.
        </p>
      </div>

      {/* Barra de Filtros */}
      <SearchFilterBar
        fields={[
          {
            key: 'empresaId',
            label: 'Empresa',
            type: 'select',
            placeholder: 'Todas las empresas',
            options: empresas.map((e) => ({ value: e.id, label: e.nombre })),
          },
          {
            key: 'codigo',
            label: 'Código',
            placeholder: 'Ej: DEV-001',
          },
          {
            key: 'nombrePuesto',
            label: 'Nombre del puesto',
            placeholder: 'Ej: Desarrollador',
          },
        ]}
        onSearch={handleSearch}
      />

      {/* Tabla de Puestos */}
      <div className="flex flex-col gap-2">
        <DataTable
          columns={columns}
          data={puestos}
          keyExtractor={(row) => row.id}
          selectedId={selectedPuestoId}
          onSelectRow={(row) => setSelectedPuestoId(row.id)}
          isLoading={isLoading}
          emptyTitle="No se encontraron puestos"
          emptyDescription="Pruebe cambiando los filtros de búsqueda o registre un nuevo puesto."
        />
        {selectedPuestoId && (
          <span className="text-xs text-[#3F2581] font-medium pl-1">
            * Puesto seleccionado: {puestos.find((p) => p.id === selectedPuestoId)?.nombrePuesto}
          </span>
        )}
      </div>

      {/* Barra de Acciones CRUD */}
      <ActionToolbar align="right">
        <Button
          type="button"
          variant="secondary-soft"
          disabled={!selectedPuestoId}
          onClick={handleDelete}
          leftIcon="trash"
        >
          Eliminar
        </Button>
        <Button
          type="button"
          variant="secondary"
          disabled={!selectedPuestoId}
          onClick={handleModify}
          leftIcon="edit"
        >
          Modificar
        </Button>
        <Button
          type="button"
          variant="primary"
          onClick={handleNew}
          leftIcon="plus"
        >
          Nuevo
        </Button>
      </ActionToolbar>
    </div>
  );
}
