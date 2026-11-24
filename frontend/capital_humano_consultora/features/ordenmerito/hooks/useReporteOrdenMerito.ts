'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { ordenMeritoService } from '../services/ordenMeritoService';
import { extractOrdenMeritoError } from '../utils/formatters';
import type { OrdenMeritoErrorState, ReporteOrdenMeritoDTO } from '../types';

export interface UseReporteOrdenMeritoOptions {
  puestoId: number;
  evaluacionIds?: number[];
}

interface ReporteRequestState {
  requestKey: string;
  reporte: ReporteOrdenMeritoDTO | null;
  error: OrdenMeritoErrorState | null;
}

const INVALID_PUESTO_ERROR: OrdenMeritoErrorState = {
  status: 404,
  codigo: 'RECURSO_NO_ENCONTRADO',
  mensaje: 'Puesto no encontrado',
  detalles: [],
};

export function useReporteOrdenMerito({
  puestoId,
  evaluacionIds,
}: UseReporteOrdenMeritoOptions) {
  const evaluacionIdsKey = useMemo(
    () =>
      evaluacionIds && evaluacionIds.length > 0 ? evaluacionIds.join(',') : '',
    [evaluacionIds]
  );

  const isValidPuestoId = Number.isInteger(puestoId) && puestoId > 0;
  const [reloadCount, setReloadCount] = useState<number>(0);

  const currentRequestKey = `${puestoId}|${evaluacionIdsKey}|${reloadCount}`;

  const [state, setState] = useState<ReporteRequestState>({
    requestKey: '',
    reporte: null,
    error: null,
  });

  useEffect(() => {
    if (!isValidPuestoId) return;

    let cancelled = false;
    const ids = evaluacionIdsKey
      ? evaluacionIdsKey.split(',').map((s) => Number(s))
      : undefined;

    ordenMeritoService
      .emitirReporteOrdenMerito(puestoId, ids)
      .then((data) => {
        if (cancelled) return;
        setState({
          requestKey: currentRequestKey,
          reporte: data,
          error: null,
        });
      })
      .catch((err) => {
        if (cancelled) return;
        setState({
          requestKey: currentRequestKey,
          reporte: null,
          error: extractOrdenMeritoError(
            err,
            'No fue posible emitir el reporte de orden de mérito.'
          ),
        });
      });

    return () => {
      cancelled = true;
    };
  }, [isValidPuestoId, puestoId, evaluacionIdsKey, currentRequestKey]);

  const reintentar = useCallback(() => {
    setReloadCount((prev) => prev + 1);
  }, []);

  if (!isValidPuestoId) {
    return {
      reporte: null,
      isLoading: false,
      error: INVALID_PUESTO_ERROR,
      reintentar,
    };
  }

  const isLoading = state.requestKey !== currentRequestKey;

  return {
    reporte: isLoading ? null : state.reporte,
    isLoading,
    error: isLoading ? null : state.error,
    reintentar,
  };
}
