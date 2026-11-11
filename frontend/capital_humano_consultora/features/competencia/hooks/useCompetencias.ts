'use client';

import { useState, useEffect, useCallback } from 'react';
import { CompetenciaResponse } from '@/types/competencia';
import { competenciaService, ApiError } from '../services/competenciaService';

export function useCompetencias() {
  const [competencias, setCompetencias] = useState<CompetenciaResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    competenciaService
      .listarCompetencias()
      .then((lista) => {
        if (active) {
          setCompetencias(lista);
          setError(null);
        }
      })
      .catch((err: unknown) => {
        if (active) {
          if (err instanceof ApiError) {
            const errData = err.data as { mensaje?: string } | undefined;
            setError(errData?.mensaje || err.message || 'Error al cargar las competencias desde el servidor.');
          } else if (err instanceof Error) {
            setError(err.message);
          } else {
            setError('Error al cargar las competencias desde el servidor.');
          }
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
    setError(null);
    try {
      const lista = await competenciaService.listarCompetencias();
      setCompetencias(lista);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errData = err.data as { mensaje?: string } | undefined;
        setError(errData?.mensaje || err.message || 'Error al cargar las competencias desde el servidor.');
      } else if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Error al cargar las competencias desde el servidor.');
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  return {
    competencias,
    isLoading,
    error,
    recargarCompetencias,
  };
}
