'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  FiltrosBusquedaPregunta,
  Pregunta,
  CompetenciaItem,
  FactorItem,
} from '../types';
import { gestionarPreguntasService } from '../services/gestionarPreguntasService';

export interface UseGestionarPreguntasOptions {
  onNavigateNuevo?: () => void;
  onNavigateModificar?: (preguntaId: number) => void;
  onCancelRedirect?: string;
}

export function useGestionarPreguntas(options: UseGestionarPreguntasOptions = {}) {
  const router = useRouter();

  // Criterios de búsqueda
  const [filtros, setFiltros] = useState<FiltrosBusquedaPregunta>({
    competencia: '',
    factor: '',
    nombre: '',
  });

  // Datos de la grilla
  const [preguntas, setPreguntas] = useState<Pregunta[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [hasSearched, setHasSearched] = useState<boolean>(false);

  // Selección de fila única
  const [selectedPreguntaId, setSelectedPreguntaId] = useState<number | null>(null);

  // Errores de validación en pantalla (sin carteles de advertencia flotantes)
  const [errorBusqueda, setErrorBusqueda] = useState<string | null>(null);
  const [errorAccionSinSeleccion, setErrorAccionSinSeleccion] = useState<string | null>(null);

  // Estados de catálogo para los desplegables de filtro
  const [competencias, setCompetencias] = useState<CompetenciaItem[]>([]);
  const [factores, setFactores] = useState<FactorItem[]>([]);
  const [isLoadingFiltros, setIsLoadingFiltros] = useState<boolean>(true);

  // Estados del modal de confirmación de baja (CU 023)
  const [preguntaAEliminar, setPreguntaAEliminar] = useState<Pregunta | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  // Estados del modal de éxito posterior a la baja
  const [nombrePreguntaEliminada, setNombrePreguntaEliminada] = useState<string | null>(null);

  // Carga inicial de competencias y factores para los selectores de búsqueda
  useEffect(() => {
    let isMounted = true;

    Promise.all([
      gestionarPreguntasService.obtenerCompetencias(),
      gestionarPreguntasService.obtenerFactores(),
    ])
      .then(([comps, facs]) => {
        if (isMounted) {
          setCompetencias(comps);
          setFactores(facs);
          setIsLoadingFiltros(false);
        }
      })
      .catch(() => {
        if (isMounted) {
          setIsLoadingFiltros(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  // Factores filtrados según la competencia seleccionada en los filtros
  const factoresDisponibles = useMemo(() => {
    if (!filtros.competencia) {
      return factores;
    }
    const compSeleccionada = competencias.find(
      (c) => c.nombre.toLowerCase() === filtros.competencia?.toLowerCase()
    );
    if (!compSeleccionada) {
      return factores;
    }
    return factores.filter((f) => f.competenciaId === compSeleccionada.id);
  }, [competencias, factores, filtros.competencia]);

  // Pregunta actualmente seleccionada
  const selectedPregunta = useMemo(() => {
    if (selectedPreguntaId == null) return null;
    return preguntas.find((p) => p.id === selectedPreguntaId) || null;
  }, [preguntas, selectedPreguntaId]);

  // Manejador de cambio en inputs y selects de filtros
  const handleFilterChange = useCallback((campo: keyof FiltrosBusquedaPregunta, valor: string) => {
    setFiltros((prev) => {
      const actualizados = { ...prev, [campo]: valor };
      // Si cambia de competencia y el factor seleccionado ya no corresponde, resetear factor
      if (campo === 'competencia') {
        actualizados.factor = '';
      }
      return actualizados;
    });

    // Limpia errores en pantalla de inmediato al interactuar
    setErrorBusqueda(null);
    setErrorAccionSinSeleccion(null);
  }, []);

  // Ejecución de la búsqueda (CU 020 - Flujo Principal)
  const handleBuscar = useCallback(async () => {
    const comp = filtros.competencia?.trim() || '';
    const fac = filtros.factor?.trim() || '';
    const nom = filtros.nombre?.trim() || '';

    // Regla: Al menos uno de los tres campos debe tener contenido
    if (!comp && !fac && !nom) {
      setErrorBusqueda('Debe ingresar datos en al menos uno de los criterios de búsqueda');
      return;
    }

    setErrorBusqueda(null);
    setErrorAccionSinSeleccion(null);
    setIsLoading(true);

    try {
      const resultados = await gestionarPreguntasService.buscarPreguntas({
        competencia: comp,
        factor: fac,
        nombre: nom,
      });

      setPreguntas(resultados);
      setHasSearched(true);

      // Si la fila antes seleccionada ya no existe en el nuevo resultado, deseleccionar
      if (selectedPreguntaId != null && !resultados.some((p) => p.id === selectedPreguntaId)) {
        setSelectedPreguntaId(null);
      }
    } catch {
      setPreguntas([]);
      setHasSearched(true);
    } finally {
      setIsLoading(false);
    }
  }, [filtros, selectedPreguntaId]);

  // Limpiar filtros y reiniciar estado
  const handleLimpiarFiltros = useCallback(() => {
    setFiltros({
      competencia: '',
      factor: '',
      nombre: '',
    });
    setErrorBusqueda(null);
    setErrorAccionSinSeleccion(null);
  }, []);

  // Selección de fila en la grilla
  const handleSelectRow = useCallback((pregunta: Pregunta) => {
    setSelectedPreguntaId((prev) => (prev === pregunta.id ? null : pregunta.id));
    setErrorAccionSinSeleccion(null);
  }, []);

  // Botón «Nuevo» -> Navegar a Alta de Pregunta (CU 021)
  const handleNuevo = useCallback(() => {
    if (options.onNavigateNuevo) {
      options.onNavigateNuevo();
      return;
    }
    router.push('/en-desarrollo?modulo=Alta+de+Pregunta+(CU+021)');
  }, [options, router]);

  // Botón «Modificar» -> Requiere selección previa (CU 022)
  const handleModificar = useCallback(() => {
    if (!selectedPregunta) {
      setErrorAccionSinSeleccion('Debe seleccionar una pregunta de la grilla para modificarla');
      return;
    }

    if (options.onNavigateModificar) {
      options.onNavigateModificar(selectedPregunta.id);
      return;
    }
    router.push(`/en-desarrollo?modulo=Modificar+Pregunta+(CU+022)&id=${selectedPregunta.id}`);
  }, [selectedPregunta, options, router]);

  // Botón «Eliminar» -> Requiere selección previa (CU 023)
  const handleEliminar = useCallback(() => {
    if (!selectedPregunta) {
      setErrorAccionSinSeleccion('Debe seleccionar una pregunta de la grilla para eliminarla');
      return;
    }
    setPreguntaAEliminar(selectedPregunta);
  }, [selectedPregunta]);

  // Modal Confirmación de Baja: Click en [CANCELAR]
  const handleCancelarEliminar = useCallback(() => {
    setPreguntaAEliminar(null);
  }, []);

  // Modal Confirmación de Baja: Click en [ELIMINAR] (CU 023)
  const handleConfirmarEliminar = useCallback(async () => {
    if (!preguntaAEliminar) return;

    setIsDeleting(true);
    try {
      await gestionarPreguntasService.eliminarPregunta(preguntaAEliminar.id);

      const nombreEliminada = preguntaAEliminar.nombre;

      // Actualizar la grilla localmente
      setPreguntas((prev) => prev.filter((p) => p.id !== preguntaAEliminar.id));
      setSelectedPreguntaId(null);
      setPreguntaAEliminar(null);

      // Mostrar modal de éxito
      setNombrePreguntaEliminada(nombreEliminada);
    } catch {
      // Manejo de error si falla
      setPreguntaAEliminar(null);
    } finally {
      setIsDeleting(false);
    }
  }, [preguntaAEliminar]);

  // Modal de Éxito: Click en [ACEPTAR]
  const handleCerrarModalExito = useCallback(() => {
    setNombrePreguntaEliminada(null);
  }, []);

  // Botón «Salir / Cancelar»
  const handleSalir = useCallback(() => {
    if (options.onCancelRedirect) {
      router.push(options.onCancelRedirect);
      return;
    }
    router.push('/en-desarrollo?modulo=Menú+Principal');
  }, [options.onCancelRedirect, router]);

  return {
    filtros,
    preguntas,
    isLoading,
    hasSearched,
    selectedPreguntaId,
    selectedPregunta,
    errorBusqueda,
    errorAccionSinSeleccion,
    competencias,
    factoresDisponibles,
    isLoadingFiltros,
    preguntaAEliminar,
    isDeleting,
    nombrePreguntaEliminada,
    handleFilterChange,
    handleBuscar,
    handleLimpiarFiltros,
    handleSelectRow,
    handleNuevo,
    handleModificar,
    handleEliminar,
    handleCancelarEliminar,
    handleConfirmarEliminar,
    handleCerrarModalExito,
    handleSalir,
  };
}
