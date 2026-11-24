'use client';

import { useState, useCallback, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { ordenMeritoService } from '../services/ordenMeritoService';
import {
  extractOrdenMeritoError,
  saveLastOrdenMeritoSearch,
} from '../utils/formatters';
import type {
  FiltrosBusquedaPuestosOrdenMerito,
  OrdenMeritoErrorState,
  PuestoOrdenMeritoDTO,
} from '../types';

export interface UseBuscarPuestosOrdenMeritoOptions {
  onCancelar?: () => void;
}

interface SearchRequestState {
  requestKey: string;
  puestos: PuestoOrdenMeritoDTO[];
  error: OrdenMeritoErrorState | null;
}

const MENSAJE_CRITERIO_VACIO = 'Ingrese al menos un criterio de búsqueda';

export function useBuscarPuestosOrdenMerito(
  options: UseBuscarPuestosOrdenMeritoOptions = {}
) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const urlCodigo = (searchParams.get('codigo') ?? '').trim().toUpperCase();
  const urlNombre = (searchParams.get('nombre') ?? '').trim().toUpperCase();
  const urlEmpresa = (searchParams.get('empresa') ?? '').trim().toUpperCase();
  const hasUrlParams = Boolean(urlCodigo || urlNombre || urlEmpresa);
  const urlQueryKey = `${urlCodigo}|${urlNombre}|${urlEmpresa}`;

  const [filtros, setFiltros] = useState<
    Required<FiltrosBusquedaPuestosOrdenMerito>
  >({
    codigo: urlCodigo,
    nombre: urlNombre,
    empresa: urlEmpresa,
  });

  const [reloadCount, setReloadCount] = useState<number>(0);
  const [localValidationError, setLocalValidationError] = useState<
    string | null
  >(null);
  const [dismissedErrorKey, setDismissedErrorKey] = useState<string | null>(
    null
  );

  const currentRequestKey = `${urlQueryKey}|${reloadCount}`;

  const [requestState, setRequestState] = useState<SearchRequestState>({
    requestKey: '',
    puestos: [],
    error: null,
  });

  const handleFilterChange = useCallback(
    (field: keyof FiltrosBusquedaPuestosOrdenMerito, rawValue: string) => {
      const upper = rawValue.toUpperCase();
      setFiltros((prev) => ({ ...prev, [field]: upper }));
      if (localValidationError) {
        setLocalValidationError(null);
      }
    },
    [localValidationError]
  );

  const buildFilterQueryString = useCallback(
    (criterios: Required<FiltrosBusquedaPuestosOrdenMerito>) => {
      const filterParams = new URLSearchParams();
      if (criterios.codigo) filterParams.set('codigo', criterios.codigo);
      if (criterios.nombre) filterParams.set('nombre', criterios.nombre);
      if (criterios.empresa) filterParams.set('empresa', criterios.empresa);
      return filterParams.toString();
    },
    []
  );

  // Ejecuta la búsqueda contra el backend cuando existen criterios en la URL
  // (compatible con React StrictMode sin bloquearse en estado cargando)
  useEffect(() => {
    if (!hasUrlParams) return;

    let cancelled = false;
    const criterios = {
      codigo: urlCodigo,
      nombre: urlNombre,
      empresa: urlEmpresa,
    };

    saveLastOrdenMeritoSearch(buildFilterQueryString(criterios));

    ordenMeritoService
      .buscarPuestos(criterios)
      .then((data) => {
        if (cancelled) return;
        setRequestState({
          requestKey: currentRequestKey,
          puestos: data,
          error: null,
        });
      })
      .catch((err) => {
        if (cancelled) return;
        setRequestState({
          requestKey: currentRequestKey,
          puestos: [],
          error: extractOrdenMeritoError(
            err,
            'No fue posible buscar los puestos.'
          ),
        });
      });

    return () => {
      cancelled = true;
    };
  }, [
    hasUrlParams,
    urlCodigo,
    urlNombre,
    urlEmpresa,
    currentRequestKey,
    buildFilterQueryString,
  ]);

  const handleBuscar = useCallback(
    (e?: React.FormEvent) => {
      e?.preventDefault();

      const codigo = filtros.codigo.trim().toUpperCase();
      const nombre = filtros.nombre.trim().toUpperCase();
      const empresa = filtros.empresa.trim().toUpperCase();

      if (!codigo && !nombre && !empresa) {
        setLocalValidationError(MENSAJE_CRITERIO_VACIO);
        setDismissedErrorKey(currentRequestKey);
        return;
      }

      setLocalValidationError(null);
      setDismissedErrorKey(null);

      const criteriosNormalizados = { codigo, nombre, empresa };
      setFiltros(criteriosNormalizados);

      const filterQs = buildFilterQueryString(criteriosNormalizados);
      saveLastOrdenMeritoSearch(filterQs);

      const nextQueryKey = `${codigo}|${nombre}|${empresa}`;
      if (nextQueryKey === urlQueryKey) {
        // Mismos criterios: forzar una nueva consulta
        setReloadCount((prev) => prev + 1);
        return;
      }

      // Actualiza query params preservando otros params externos
      const nextParams = new URLSearchParams(searchParams.toString());
      if (codigo) nextParams.set('codigo', codigo);
      else nextParams.delete('codigo');

      if (nombre) nextParams.set('nombre', nombre);
      else nextParams.delete('nombre');

      if (empresa) nextParams.set('empresa', empresa);
      else nextParams.delete('empresa');

      const fullQs = nextParams.toString();
      router.replace(fullQs ? `?${fullQs}` : window.location.pathname, {
        scroll: false,
      });
    },
    [
      filtros,
      currentRequestKey,
      buildFilterQueryString,
      urlQueryKey,
      searchParams,
      router,
    ]
  );

  const handleReintentar = useCallback(() => {
    if (!hasUrlParams) return;
    setDismissedErrorKey(null);
    setReloadCount((prev) => prev + 1);
  }, [hasUrlParams]);

  const handleCancelar = useCallback(() => {
    if (options.onCancelar) {
      options.onCancelar();
      return;
    }
    // TODO: apuntar a la pantalla principal cuando exista
    router.push('/');
  }, [options, router]);

  const isLoading =
    hasUrlParams && requestState.requestKey !== currentRequestKey;
  const hasSearched =
    hasUrlParams && requestState.requestKey === currentRequestKey;
  const backendError =
    hasSearched && dismissedErrorKey !== currentRequestKey
      ? requestState.error
      : null;

  return {
    filtros,
    puestos: hasSearched ? requestState.puestos : [],
    hasSearched,
    isLoading,
    localValidationError,
    backendError,
    dismissBackendError: () => setDismissedErrorKey(currentRequestKey),
    handleFilterChange,
    handleBuscar,
    handleReintentar,
    handleCancelar,
  };
}
