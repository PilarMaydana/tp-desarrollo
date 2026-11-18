'use client';

import { useState, useCallback, useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { OpcionRespuestaResumen } from '../types';
import { opcionRespuestaService } from '../services/opcionRespuestaService';
import { ApiError } from '@/services/apiClient';

export interface UseGestionarOpcionesRespuestaOptions {
  onNavigateNuevo?: () => void;
  onNavigateSalir?: () => void;
}

export function useGestionarOpcionesRespuesta(
  options: UseGestionarOpcionesRespuestaOptions = {}
) {
  const router = useRouter();
  const searchParams = useSearchParams();

  // URL query param `nombre`
  const paramNombre = searchParams.get('nombre');

  // Input state - max 100 caracteres, mayúsculas al tipear
  const [nombreBusqueda, setNombreBusqueda] = useState<string>(
    paramNombre !== null ? paramNombre.toUpperCase() : ''
  );

  const [resultados, setResultados] = useState<OpcionRespuestaResumen[]>([]);
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Fila seleccionada (selección única)
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const selectedOpcion = resultados.find(
    (item) => String(item.id) === selectedId
  );

  // Ejecuta la búsqueda contra el backend
  const ejecutarBusqueda = useCallback(async (criterio: string) => {
    setIsLoading(true);
    setError(null);
    setSelectedId(null);

    try {
      const data = await opcionRespuestaService.buscarOpcionesRespuesta(criterio);
      setResultados(data);
      setHasSearched(true);
    } catch (err) {
      setHasSearched(true);
      if (err instanceof ApiError) {
        const errorData = err.data as { mensaje?: string; message?: string } | undefined;
        setError(
          errorData?.mensaje ||
          errorData?.message ||
          err.message ||
          'Error al buscar las opciones de respuesta.'
        );
      } else if (err instanceof Error) {
        setError(err.message || 'Error de conexión con el servidor.');
      } else {
        setError('Error desconocido al comunicarse con el servidor.');
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Manejo de cambio en el campo de búsqueda (convierte a mayúsculas y limita a 100 caracteres)
  const handleNombreChange = (val: string) => {
    const uppercaseVal = val.toUpperCase().slice(0, 100);
    setNombreBusqueda(uppercaseVal);
  };

  // Botón «Buscar» o Enter en el input: actualiza la URL y ejecuta la búsqueda
  const handleBuscar = useCallback(() => {
    const criterio = nombreBusqueda.trim();
    // Actualiza la URL manteniendo el query param `nombre` (soporta vacío: ?nombre=)
    const newQuery = new URLSearchParams(searchParams.toString());
    newQuery.set('nombre', criterio);
    router.replace(`?${newQuery.toString()}`, { scroll: false });
    ejecutarBusqueda(criterio);
  }, [nombreBusqueda, searchParams, router, ejecutarBusqueda]);

  // Selección de fila
  const handleSelectRow = useCallback((row: OpcionRespuestaResumen) => {
    setSelectedId(String(row.id));
  }, []);

  // Botón «Nuevo» -> CU 29
  const handleNuevo = useCallback(() => {
    if (options.onNavigateNuevo) {
      options.onNavigateNuevo();
      return;
    }
    router.push('/opciones-respuesta/nueva');
  }, [options, router]);

  // Botón «Salir» -> pantalla de inicio
  const handleSalir = useCallback(() => {
    if (options.onNavigateSalir) {
      options.onNavigateSalir();
      return;
    }
    // TODO: apuntar a la pantalla de inicio cuando exista
    router.push('/');
  }, [options, router]);

  // Búsqueda inicial automática si la URL trae el param `nombre` (incluso si viene vacío: ?nombre=)
  const initialSearchDoneRef = useRef(false);
  useEffect(() => {
    if (!initialSearchDoneRef.current && paramNombre !== null) {
      initialSearchDoneRef.current = true;
      setNombreBusqueda(paramNombre.toUpperCase());
      ejecutarBusqueda(paramNombre);
    }
  }, [paramNombre, ejecutarBusqueda]);

  return {
    nombreBusqueda,
    setNombreBusqueda: handleNombreChange,
    resultados,
    hasSearched,
    isLoading,
    error,
    selectedId,
    selectedOpcion,
    handleBuscar,
    handleSelectRow,
    handleNuevo,
    handleSalir,
  };
}
