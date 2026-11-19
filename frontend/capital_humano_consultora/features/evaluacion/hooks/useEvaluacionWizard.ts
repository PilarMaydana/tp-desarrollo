'use client';

import { useCallback, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
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
  DetallePuestoEvaluacionDTO,
  ErrorItem,
  ErrorPanelState,
  PrevisualizacionEvaluacionDTO,
  PuestoResumenDTO,
} from '../types';

export type WizardStepIndex = 0 | 1 | 2;

export const MENSAJE_SELECCIONAR_CANDIDATOS = 'Debe seleccionar uno o más candidatos';
export const MENSAJE_SELECCIONAR_PUESTO = 'Debe seleccionar una función o puesto';

export interface Modal9AState {
  isOpen: boolean;
  mensaje: string | null;
  competencias: string[];
}

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
  const router = useRouter();
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

  // --- Paso 2: Selección de puesto y competencias ---
  const [puestos, setPuestos] = useState<PuestoResumenDTO[]>([]);
  const [hasLoadedPuestos, setHasLoadedPuestos] = useState<boolean>(false);
  const [isLoadingPuestos, setIsLoadingPuestos] = useState<boolean>(false);
  const [puestosError, setPuestosError] = useState<ErrorPanelState | null>(null);

  const [selectedPuestoId, setSelectedPuestoId] = useState<string>('');
  const [detallePuesto, setDetallePuesto] = useState<DetallePuestoEvaluacionDTO | null>(null);
  const [isLoadingCompetencias, setIsLoadingCompetencias] = useState<boolean>(false);
  const [step2Error, setStep2Error] = useState<ErrorPanelState | null>(null);
  const [modal9A, setModal9A] = useState<Modal9AState>({
    isOpen: false,
    mensaje: null,
    competencias: [],
  });
  const [isLoadingPreview, setIsLoadingPreview] = useState<boolean>(false);
  const competenciasRequestIdRef = useRef<number>(0);

  // --- Paso 3: Previsualización ---
  const [previsualizacion, setPrevisualizacion] =
    useState<PrevisualizacionEvaluacionDTO | null>(null);

  // Descartar completamente el estado del wizard en memoria
  const resetWizardState = useCallback(() => {
    competenciasRequestIdRef.current += 1;
    searchForm.reset({ apellido: '', nombre: '', nroCandidato: '' });
    setCurrentStep(0);
    setResultadosBusqueda([]);
    setHasSearched(false);
    setIsSearching(false);
    setSearchError(null);
    setSelectedResultadoIds([]);
    setCandidatosAEvaluar([]);
    setSelectedAEvaluarIds([]);
    setStep1Error(null);
    setBlockedNros(new Set());
    setIsValidatingCandidatos(false);
    setPuestos([]);
    setHasLoadedPuestos(false);
    setIsLoadingPuestos(false);
    setPuestosError(null);
    setSelectedPuestoId('');
    setDetallePuesto(null);
    setIsLoadingCompetencias(false);
    setStep2Error(null);
    setModal9A({ isOpen: false, mensaje: null, competencias: [] });
    setIsLoadingPreview(false);
    setPrevisualizacion(null);
  }, [searchForm]);

  const cargarPuestos = useCallback(async () => {
    setIsLoadingPuestos(true);
    setPuestosError(null);
    try {
      const lista = await evaluacionService.listarPuestos();
      setPuestos(Array.isArray(lista) ? lista : []);
      setHasLoadedPuestos(true);
    } catch (err: unknown) {
      setPuestosError(
        extraerErrorPanelDesdeApiError(
          err,
          'No fue posible obtener el listado de puestos.',
          'Error al cargar puestos'
        )
      );
    } finally {
      setIsLoadingPuestos(false);
    }
  }, []);

  // --- Handlers Paso 1 ---
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
      if (!hasLoadedPuestos) {
        void cargarPuestos();
      }
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
  }, [candidatosAEvaluar, hasLoadedPuestos, cargarPuestos]);

  // --- Handlers Paso 2 ---
  const handleSelectPuesto = useCallback(async (puestoIdStr: string) => {
    const requestId = ++competenciasRequestIdRef.current;
    setSelectedPuestoId(puestoIdStr);
    setDetallePuesto(null);
    setStep2Error(null);

    if (!puestoIdStr) {
      setIsLoadingCompetencias(false);
      return;
    }

    const puestoId = Number(puestoIdStr);
    if (!Number.isInteger(puestoId) || puestoId <= 0) {
      setIsLoadingCompetencias(false);
      return;
    }

    setIsLoadingCompetencias(true);

    try {
      const detalle = await evaluacionService.obtenerCompetenciasPuesto(puestoId);
      if (competenciasRequestIdRef.current !== requestId) return;
      setDetallePuesto(detalle);
    } catch (err: unknown) {
      if (competenciasRequestIdRef.current !== requestId) return;

      // Flujo 9.A (422): competencias sin factores válidos o puesto sin competencias evaluables
      if (err instanceof ApiError && err.status === 422) {
        const data = err.data as BackendApiErrorData | undefined;
        const competencias = Array.isArray(data?.detalles)
          ? data.detalles.map((d) => d.mensaje).filter(Boolean)
          : [];
        setModal9A({
          isOpen: true,
          mensaje: data?.mensaje || err.message,
          competencias,
        });
      } else {
        setStep2Error(
          extraerErrorPanelDesdeApiError(
            err,
            'No fue posible consultar las competencias del puesto seleccionado.',
            'Error al consultar competencias'
          )
        );
      }
    } finally {
      if (competenciasRequestIdRef.current === requestId) {
        setIsLoadingCompetencias(false);
      }
    }
  }, []);

  // Flujo 9.A.3 - 9.A.4: Al presionar «Aceptar» el caso de uso finaliza y vuelve al inicio
  const handleAceptarModal9A = useCallback(() => {
    resetWizardState();
    // TODO: apuntar a la pantalla principal cuando exista
    router.push('/');
  }, [resetWizardState, router]);

  // Volver al Paso 1 conservando los candidatos agregados
  const handleAnteriorPaso2 = useCallback(() => {
    setStep2Error(null);
    setCurrentStep(0);
  }, []);

  // Paso 10 y Flujo 10.A: Previsualizar evaluación y avanzar al Paso 3
  const handleSiguientePaso2 = useCallback(async () => {
    // Flujo 10.A: «Siguiente» sin puesto elegido
    if (!selectedPuestoId) {
      setStep2Error({
        titulo: 'Selección requerida',
        mensajeGeneral: MENSAJE_SELECCIONAR_PUESTO,
        detalles: [],
      });
      return;
    }

    const puestoId = Number(selectedPuestoId);
    if (!Number.isInteger(puestoId) || puestoId <= 0) {
      setStep2Error({
        titulo: 'Selección requerida',
        mensajeGeneral: MENSAJE_SELECCIONAR_PUESTO,
        detalles: [],
      });
      return;
    }

    setIsLoadingPreview(true);
    setStep2Error(null);

    try {
      const preview = await evaluacionService.previsualizarEvaluacion({
        puestoId,
        candidatoIds: candidatosAEvaluar.map((c) => c.id),
      });
      setPrevisualizacion(preview);
      setCurrentStep(2);
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 422) {
        const data = err.data as BackendApiErrorData | undefined;
        const esErrorCompetencias =
          Array.isArray(data?.detalles) &&
          data.detalles.length > 0 &&
          data.detalles.every((d) => d.campo === 'competencia');

        if (esErrorCompetencias) {
          setModal9A({
            isOpen: true,
            mensaje: data?.mensaje || err.message,
            competencias: data.detalles!.map((d) => d.mensaje).filter(Boolean),
          });
          return;
        }
      }

      setStep2Error(
        extraerErrorPanelDesdeApiError(
          err,
          'No fue posible generar la previsualización de la evaluación.',
          'Error al previsualizar evaluación'
        )
      );
    } finally {
      setIsLoadingPreview(false);
    }
  }, [selectedPuestoId, candidatosAEvaluar]);

  const handleDismissSearchError = useCallback(() => {
    setSearchError(null);
  }, []);

  const handleDismissStep1Error = useCallback(() => {
    setStep1Error(null);
  }, []);

  const handleDismissStep2Error = useCallback(() => {
    setStep2Error(null);
  }, []);

  const handleStepClick = useCallback(
    (stepIndex: number) => {
      if (stepIndex >= currentStep) return;
      if (stepIndex === 0) {
        setStep2Error(null);
        setPrevisualizacion(null);
        setCurrentStep(0);
      } else if (stepIndex === 1) {
        setPrevisualizacion(null);
        setCurrentStep(1);
      }
    },
    [currentStep]
  );

  return {
    currentStep,
    setCurrentStep,
    handleStepClick,
    resetWizardState,
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
    // Paso 2
    puestos,
    isLoadingPuestos,
    puestosError,
    cargarPuestos,
    selectedPuestoId,
    handleSelectPuesto,
    detallePuesto,
    isLoadingCompetencias,
    step2Error,
    handleDismissStep2Error,
    modal9A,
    handleAceptarModal9A,
    isLoadingPreview,
    handleAnteriorPaso2,
    handleSiguientePaso2,
    // Paso 3
    previsualizacion,
  };
}
