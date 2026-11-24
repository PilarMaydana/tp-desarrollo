'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { ordenMeritoService } from '../services/ordenMeritoService';
import { extractOrdenMeritoError } from '../utils/formatters';
import type { OrdenMeritoErrorState, ReporteOrdenMeritoDTO } from '../types';

export interface UseReporteOrdenMeritoOptions {
  puestoId: number;
  evaluacionIds?: number[];
}

export function useReporteOrdenMerito({
  puestoId,
  evaluacionIds,
}: UseReporteOrdenMeritoOptions) {
  const [reporte, setReporte] = useState<ReporteOrdenMeritoDTO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<OrdenMeritoErrorState | null>(null);

  const evaluacionIdsKey = useMemo(
    () => (evaluacionIds && evaluacionIds.length > 0 ? evaluacionIds.join(',') : ''),
    [evaluacionIds]
  );

  const cargarReporte = useCallback(async () => {
    if (!Number.isInteger(puestoId) || puestoId <= 0) {
      setReporte(null);
      setIsLoading(false);
      setError({
        status: 404,
        codigo: 'RECURSO_NO_ENCONTRADO',
        mensaje: 'Puesto no encontrado',
        detalles: [],
      });
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const ids = evaluacionIdsKey
        ? evaluacionIdsKey.split(',').map((s) => Number(s))
        : undefined;
      const data = await ordenMeritoService.emitirReporteOrdenMerito(puestoId, ids);
      setReporte(data);
    } catch (err) {
      setReporte(null);
      setError(
        extractOrdenMeritoError(
          err,
          'No fue posible emitir el reporte de orden de mérito.'
        )
      );
    } finally {
      setIsLoading(false);
    }
  }, [puestoId, evaluacionIdsKey]);

  useEffect(() => {
    void cargarReporte();
  }, [cargarReporte]);

  return {
    reporte,
    isLoading,
    error,
    reintentar: cargarReporte,
  };
}
