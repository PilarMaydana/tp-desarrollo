'use client';

import { useCallback, useEffect, useState } from 'react';
import { Empresa, Puesto, PuestosFilterParams } from '@/types/puesto';
import { Competencia } from '@/types/competencia';
import { puestoService } from '@/services/puestoService';

export function usePuestos(initialFilters?: PuestosFilterParams) {
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [competencias, setCompetencias] = useState<Competencia[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedPuestoId, setSelectedPuestoId] = useState<string | null>(null);

  const fetchPuestos = useCallback(async (filters?: PuestosFilterParams) => {
    setIsLoading(true);
    try {
      const data = await puestoService.getPuestos(filters);
      setPuestos(data);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    Promise.all([
      puestoService.getEmpresas(),
      puestoService.getCompetencias(),
      puestoService.getPuestos(initialFilters),
    ]).then(([emp, comp, pts]) => {
      setEmpresas(emp);
      setCompetencias(comp);
      setPuestos(pts);
      setIsLoading(false);
    });
  }, [initialFilters]);

  const deletePuesto = async (id: string) => {
    await puestoService.deletePuesto(id);
    setPuestos((prev) => prev.filter((p) => p.id !== id));
    if (selectedPuestoId === id) setSelectedPuestoId(null);
  };

  return {
    puestos,
    empresas,
    competencias,
    isLoading,
    selectedPuestoId,
    setSelectedPuestoId,
    fetchPuestos,
    deletePuesto,
  };
}
