'use client';

import { useState, useEffect, useMemo, useCallback } from 'react';
import { competenciaService, CompetenciaCatalogoItem } from '../services/competenciaService';

const PAGE_SIZE = 5;

export function useCompetencias() {
  const [competencias, setCompetencias] = useState<CompetenciaCatalogoItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [busqueda, setBusqueda] = useState<string>('');
  const [filtroEstado, setFiltroEstado] = useState<string>('TODAS');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [selectedCompetenciaId, setSelectedCompetenciaId] = useState<string | null>(null);
  const [competenciaDetalle, setCompetenciaDetalle] = useState<CompetenciaCatalogoItem | null>(null);

  useEffect(() => {
    let active = true;
    competenciaService
      .listarCompetencias()
      .then((lista) => {
        if (active) {
          setCompetencias(lista);
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const recargarCompetencias = useCallback(async () => {
    setIsLoading(true);
    try {
      const lista = await competenciaService.listarCompetencias();
      setCompetencias(lista);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const competenciasFiltradas = useMemo(() => {
    const query = busqueda.trim().toLowerCase();
    return competencias.filter((item) => {
      const matchesBusqueda =
        !query ||
        item.codigo.toLowerCase().includes(query) ||
        item.nombre.toLowerCase().includes(query) ||
        (item.descripcion || '').toLowerCase().includes(query);

      const estadoActual = item.estado || 'ACTIVA';
      const matchesEstado =
        filtroEstado === 'TODAS' || estadoActual === filtroEstado;

      return matchesBusqueda && matchesEstado;
    });
  }, [competencias, busqueda, filtroEstado]);

  const totalItems = competenciasFiltradas.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / PAGE_SIZE));

  // Ajustar página actual si cambia el total filtrado
  const safeCurrentPage = Math.min(currentPage, totalPages);

  const competenciasPaginadas = useMemo(() => {
    const start = (safeCurrentPage - 1) * PAGE_SIZE;
    return competenciasFiltradas.slice(start, start + PAGE_SIZE);
  }, [competenciasFiltradas, safeCurrentPage]);

  const startIndex = totalItems === 0 ? 0 : (safeCurrentPage - 1) * PAGE_SIZE + 1;
  const endIndex = Math.min(safeCurrentPage * PAGE_SIZE, totalItems);

  const handleBusquedaChange = (valor: string) => {
    setBusqueda(valor);
    setCurrentPage(1);
  };

  const handleEstadoChange = (valor: string) => {
    setFiltroEstado(valor);
    setCurrentPage(1);
  };

  const handleLimpiarFiltros = () => {
    setBusqueda('');
    setFiltroEstado('TODAS');
    setCurrentPage(1);
  };

  return {
    competencias,
    competenciasFiltradas,
    competenciasPaginadas,
    isLoading,
    busqueda,
    filtroEstado,
    currentPage: safeCurrentPage,
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
    recargarCompetencias,
  };
}
