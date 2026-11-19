'use client';

import { useCallback, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  BuscarCandidatosFormData,
  BuscarCandidatosSchema,
} from '../schemas/buscarCandidatos.schema';
import { ApiError, evaluacionService } from '../services/evaluacionService';
import type {
  BackendApiErrorData,
  CandidatoResumenDTO,
  ErrorItem,
  ErrorPanelState,
} from '../types';

export type WizardStepIndex = 0 | 1 | 2;

export const MENSAJE_SELECCIONAR_CANDIDATOS = 'Debe seleccionar uno o más candidatos';
export const MENSAJE_SELECCIONAR_PUESTO = 'Debe seleccionar una función o puesto';

export function extraerErrorPanelDesdeApiError(
  err: unknown,
  fallbackMensaje: string,
  titulo?: string
): ErrorPanelState {
  if (err instanceof ApiError) {
    const data = err.data as BackendApiErrorData | undefined;
    const mensajeGeneral = data?.mensaje || err.message || fallbackMensaje;
    const detalles: ErrorItem[] = Array.isArray(data?.detalles)
      ? data.detalles.map((d) => ({
          campo: d.campo && !d.campo.startsWith('candidato:') ? d.campo : undefined,
          mensaje: d.mensaje,
        }))
      : [];

    return {
      titulo,
      mensajeGeneral,
      detalles,
    };
  }

  if (err instanceof Error && err.message) {
    return {
      titulo,
      mensajeGeneral: err.message,
      detalles: [],
    };
  }

  return {
    titulo,
    mensajeGeneral: fallbackMensaje,
    detalles: [],
  };
}

export function useEvaluacionWizard() {
  const [currentStep, setCurrentStep] = useState<WizardStepIndex>(0);

  // --- Paso 1: Búsqueda y selección de candidatos ---
  const searchForm = useForm<BuscarCandidatosFormData>({
    resolver: zodResolver(BuscarCandidatosSchema),
    defaultValues: {
      apellido: '',
      nombre: '',
      nroCandidato: '',
    },
  });

  const [resultadosBusqueda, setResultadosBusqueda] = useState<CandidatoResumenDTO[]>([]);
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [searchError, setSearchError] = useState<ErrorPanelState | null>(null);

  const [selectedResultadoIds, setSelectedResultadoIds] = useState<number[]>([]);
  const [candidatosAEvaluar, setCandidatosAEvaluar] = useState<CandidatoResumenDTO[]>([]);
  const [selectedAEvaluarIds, setSelectedAEvaluarIds] = useState<number[]>([]);

  const [step1Error, setStep1Error] = useState<ErrorPanelState | null>(null);
  const [blockedNros, setBlockedNros] = useState<Set<string>>(() => new Set());
  const [isValidatingCandidatos, setIsValidatingCandidatos] = useState<boolean>(false);

  const handleBuscarCandidatos = searchForm.handleSubmit(async (data) => {
    setIsSearching(true);
    setSearchError(null);
    setSelectedResultadoIds([]);

    try {
      const resultados = await evaluacionService.buscarCandidatos({
        apellido: data.apellido,
        nombre: data.nombre,
        nroCandidato: data.nroCandidato,
      });
      setResultadosBusqueda(Array.isArray(resultados) ? resultados : []);
      setHasSearched(true);
    } catch (err: unknown) {
      setSearchError(
        extraerErrorPanelDesdeApiError(
          err,
          'No fue posible obtener el listado de candidatos.',
          'Error al buscar candidatos'
        )
      );
      setHasSearched(true);
    } finally {
      setIsSearching(false);
    }
  });

  const agregadosIdSet = useMemo(
    () => new Set(candidatosAEvaluar.map((c) => c.id)),
    [candidatosAEvaluar]
  );

  const handleToggleResultado = useCallback(
    (candidatoId: number) => {
      if (agregadosIdSet.has(candidatoId)) return;
      setSelectedResultadoIds((prev) =>
        prev.includes(candidatoId)
          ? prev.filter((id) => id !== candidatoId)
          : [...prev, candidatoId]
      );
    },
    [agregadosIdSet]
  );

  const handleToggleAllResultados = useCallback(
    (checked: boolean) => {
      if (checked) {
        const seleccionables = resultadosBusqueda
          .filter((c) => !agregadosIdSet.has(c.id))
          .map((c) => c.id);
        setSelectedResultadoIds(seleccionables);
      } else {
        setSelectedResultadoIds([]);
      }
    },
    [resultadosBusqueda, agregadosIdSet]
  );

  const handleAgregarCandidatos = useCallback(() => {
    // Flujo 4.A: «Agregar» sin selección
    if (selectedResultadoIds.length === 0) {
      setStep1Error({
        titulo: 'Selección requerida',
        mensajeGeneral: MENSAJE_SELECCIONAR_CANDIDATOS,
        detalles: [],
      });
      return;
    }

    const selectedSet = new Set(selectedResultadoIds);
    const nuevos = resultadosBusqueda.filter(
      (c) => selectedSet.has(c.id) && !agregadosIdSet.has(c.id)
    );

    if (nuevos.length === 0) {
      setSelectedResultadoIds([]);
      return;
    }

    setCandidatosAEvaluar((prev) => [...prev, ...nuevos]);
    setSelectedResultadoIds([]);
    setStep1Error(null);
    setBlockedNros(new Set());
  }, [selectedResultadoIds, resultadosBusqueda, agregadosIdSet]);

  const handleToggleAEvaluar = useCallback((candidatoId: number) => {
    setSelectedAEvaluarIds((prev) =>
      prev.includes(candidatoId)
        ? prev.filter((id) => id !== candidatoId)
        : [...prev, candidatoId]
    );
  }, []);

  const handleToggleAllAEvaluar = useCallback(
    (checked: boolean) => {
      if (checked) {
        setSelectedAEvaluarIds(candidatosAEvaluar.map((c) => c.id));
      } else {
        setSelectedAEvaluarIds([]);
      }
    },
    [candidatosAEvaluar]
  );

  // Flujo 5.A: Quitar candidatos de «Candidatos a evaluar»
  const handleQuitarCandidatos = useCallback(() => {
    if (selectedAEvaluarIds.length === 0) return;

    const quitarSet = new Set(selectedAEvaluarIds);
    setCandidatosAEvaluar((prev) => prev.filter((c) => !quitarSet.has(c.id)));
    setSelectedAEvaluarIds([]);
    setStep1Error(null);
    setBlockedNros(new Set());
  }, [selectedAEvaluarIds]);

  // Paso 6 y Flujos 4.A / 6.A: Validar candidatos y avanzar a Paso 2
  const handleSiguientePaso1 = useCallback(async () => {
    if (candidatosAEvaluar.length === 0) {
      setStep1Error({
        titulo: 'Selección requerida',
        mensajeGeneral: MENSAJE_SELECCIONAR_CANDIDATOS,
        detalles: [],
      });
      return;
    }

    setIsValidatingCandidatos(true);
    setStep1Error(null);
    setBlockedNros(new Set());

    try {
      const candidatoIds = candidatosAEvaluar.map((c) => c.id);
      await evaluacionService.validarCandidatos(candidatoIds);
      setCurrentStep(1);
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 422) {
        const data = err.data as BackendApiErrorData | undefined;
        const nrosBloqueados = new Set<string>();
        const detalles: ErrorItem[] = Array.isArray(data?.detalles)
          ? data.detalles.map((d) => {
              if (d.campo?.startsWith('candidato:')) {
                const nro = d.campo.slice('candidato:'.length).trim();
                if (nro) nrosBloqueados.add(nro);
              }
              return { mensaje: d.mensaje };
            })
          : [];

        setBlockedNros(nrosBloqueados);
        setStep1Error({
          titulo:
            'Candidatos que no pueden ser evaluados nuevamente porque tienen cuestionarios activos o en proceso',
          mensajeGeneral: data?.mensaje || err.message,
          detalles,
        });
      } else {
        setStep1Error(
          extraerErrorPanelDesdeApiError(
            err,
            'No fue posible validar los candidatos seleccionados.',
            'Error al validar candidatos'
          )
        );
      }
    } finally {
      setIsValidatingCandidatos(false);
    }
  }, [candidatosAEvaluar]);

  const handleDismissSearchError = useCallback(() => {
    setSearchError(null);
  }, []);

  const handleDismissStep1Error = useCallback(() => {
    setStep1Error(null);
  }, []);

  const handleStepClick = useCallback((stepIndex: number) => {
    if (stepIndex < currentStep && (stepIndex === 0 || stepIndex === 1)) {
      setCurrentStep(stepIndex);
    }
  }, [currentStep]);

  return {
    currentStep,
    setCurrentStep,
    handleStepClick,
    // Paso 1
    register: searchForm.register,
    handleBuscarCandidatos,
    resultadosBusqueda,
    hasSearched,
    isSearching,
    searchError,
    handleDismissSearchError,
    selectedResultadoIds,
    handleToggleResultado,
    handleToggleAllResultados,
    handleAgregarCandidatos,
    candidatosAEvaluar,
    selectedAEvaluarIds,
    handleToggleAEvaluar,
    handleToggleAllAEvaluar,
    handleQuitarCandidatos,
    step1Error,
    blockedNros,
    handleDismissStep1Error,
    isValidatingCandidatos,
    handleSiguientePaso1,
  };
}
