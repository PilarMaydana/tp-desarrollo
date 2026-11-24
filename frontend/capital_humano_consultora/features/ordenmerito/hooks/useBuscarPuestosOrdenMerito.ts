'use client';

import { useState, useCallback, useEffect, useRef } from 'react';
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

const MENSAJE_CRITERIO_VACIO = 'Ingrese al menos un criterio de búsqueda';

export function useBuscarPuestosOrdenMerito(
  options: UseBuscarPuestosOrdenMeritoOptions = {}
) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const initialCodigo = (searchParams.get('codigo') ?? '').trim().toUpperCase();
  const initialNombre = (searchParams.get('nombre') ?? '').trim().toUpperCase();
  const initialEmpresa = (searchParams.get('empresa') ?? '').trim().toUpperCase();
  const hasInitialParams = Boolean(
    initialCodigo || initialNombre || initialEmpresa
  );

  const [filtros, setFiltros] = useState<
    Required<FiltrosBusquedaPuestosOrdenMerito>
  >({
    codigo: initialCodigo,
    nombre: initialNombre,
    empresa: initialEmpresa,
  });

  const [puestos, setPuestos] = useState<PuestoOrdenMeritoDTO[]>([]);
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(hasInitialParams);
  const [localValidationError, setLocalValidationError] = useState<
    string | null
  >(null);
  const [backendError, setBackendError] =
    useState<OrdenMeritoErrorState | null>(null);

  const ejecutarBusqueda = useCallback(
    async (criterios: FiltrosBusquedaPuestosOrdenMerito) => {
      setIsLoading(true);
      setBackendError(null);
      setLocalValidationError(null);

      try {
        const data = await ordenMeritoService.buscarPuestos(criterios);
        setPuestos(data);
        setHasSearched(true);
      } catch (err) {
        setPuestos([]);
        setHasSearched(true);
        setBackendError(
          extractOrdenMeritoError(err, 'No fue posible buscar los puestos.')
        );
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

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

  const handleBuscar = useCallback(
    (e?: React.FormEvent) => {
      e?.preventDefault();

      const codigo = filtros.codigo.trim().toUpperCase();
      const nombre = filtros.nombre.trim().toUpperCase();
      const empresa = filtros.empresa.trim().toUpperCase();

      if (!codigo && !nombre && !empresa) {
        setLocalValidationError(MENSAJE_CRITERIO_VACIO);
        setBackendError(null);
        return;
      }

      const criteriosNormalizados = { codigo, nombre, empresa };
      setFiltros(criteriosNormalizados);

      // Actualiza query params preservando otros params externos (ej. en sandbox)
      const nextParams = new URLSearchParams(searchParams.toString());
      if (codigo) nextParams.set('codigo', codigo);
      else nextParams.delete('codigo');

      if (nombre) nextParams.set('nombre', nombre);
      else nextParams.delete('nombre');

      if (empresa) nextParams.set('empresa', empresa);
      else nextParams.delete('empresa');

      const filterQs = buildFilterQueryString(criteriosNormalizados);
      saveLastOrdenMeritoSearch(filterQs);

      const fullQs = nextParams.toString();
      router.replace(fullQs ? `?${fullQs}` : window.location.pathname, {
        scroll: false,
      });

      void ejecutarBusqueda(criteriosNormalizados);
    },
    [filtros, searchParams, buildFilterQueryString, router, ejecutarBusqueda]
  );

  const handleReintentar = useCallback(() => {
    const codigo = filtros.codigo.trim().toUpperCase();
    const nombre = filtros.nombre.trim().toUpperCase();
    const empresa = filtros.empresa.trim().toUpperCase();
    if (!codigo && !nombre && !empresa) return;
    void ejecutarBusqueda({ codigo, nombre, empresa });
  }, [filtros, ejecutarBusqueda]);

  const handleCancelar = useCallback(() => {
    if (options.onCancelar) {
      options.onCancelar();
      return;
    }
    // TODO: apuntar a la pantalla principal cuando exista
    router.push('/');
  }, [options, router]);

  // Ejecuta la búsqueda automáticamente al cargar si hay algún criterio en la URL
  const initialSearchDoneRef = useRef(false);
  useEffect(() => {
    if (initialSearchDoneRef.current || !hasInitialParams) return;
    initialSearchDoneRef.current = true;

    let cancelled = false;
    const criterios = {
      codigo: initialCodigo,
      nombre: initialNombre,
      empresa: initialEmpresa,
    };
    saveLastOrdenMeritoSearch(buildFilterQueryString(criterios));

    ordenMeritoService
      .buscarPuestos(criterios)
      .then((data) => {
        if (cancelled) return;
        setPuestos(data);
        setHasSearched(true);
        setBackendError(null);
        setIsLoading(false);
      })
      .catch((err) => {
        if (cancelled) return;
        setPuestos([]);
        setHasSearched(true);
        setBackendError(
          extractOrdenMeritoError(err, 'No fue posible buscar los puestos.')
        );
        setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [
    hasInitialParams,
    initialCodigo,
    initialNombre,
    initialEmpresa,
    buildFilterQueryString,
  ]);

  return {
    filtros,
    puestos,
    hasSearched,
    isLoading,
    localValidationError,
    backendError,
    dismissBackendError: () => setBackendError(null),
    handleFilterChange,
    handleBuscar,
    handleReintentar,
    handleCancelar,
  };
}
