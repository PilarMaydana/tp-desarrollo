'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiError } from '@/services/apiClient';
import {
  AltaPreguntaFormData,
  AltaPreguntaSchema,
} from '../schemas/altaPregunta.schema';
import {
  altaPreguntaService,
  PLANTILLAS_OPCIONES_RESPUESTA,
} from '../services/altaPreguntaService';
import {
  BackendApiErrorData,
  CompetenciaItem,
  FactorItem,
  OpcionRespuestaItem,
} from '../types';

export interface UseAltaPreguntaOptions {
  onSuccessRedirect?: string;
  onCancelRedirect?: string;
  defaultCompetenciaCodigo?: string;
  defaultFactorCodigo?: string;
}

export function useAltaPregunta(options: UseAltaPreguntaOptions = {}) {
  const router = useRouter();
  const nombreInputRef = useRef<HTMLInputElement | null>(null);

  // Catálogos cargados desde la API
  const [competencias, setCompetencias] = useState<CompetenciaItem[]>([]);
  const [isLoadingCompetencias, setIsLoadingCompetencias] = useState(true);

  const [factores, setFactores] = useState<FactorItem[]>([]);
  const [isLoadingFactores, setIsLoadingFactores] = useState(false);

  // Estados de modales
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [preguntaCreadaNombre, setPreguntaCreadaNombre] = useState('');
  const [isIaModalOpen, setIsIaModalOpen] = useState(false);

  // Por defecto usar 'FREQ-3' (Escala de Frecuencia Triple)
  const plantillaPorDefecto = PLANTILLAS_OPCIONES_RESPUESTA[0];

  const form = useForm<AltaPreguntaFormData>({
    resolver: zodResolver(AltaPreguntaSchema),
    defaultValues: {
      codigoCompetencia: options.defaultCompetenciaCodigo || '',
      codigoFactor: options.defaultFactorCodigo || '',
      nombre: '',
      enunciado: '',
      descripcion: '',
      plantillaOpcionId: plantillaPorDefecto.id,
      opcionesRespuesta: plantillaPorDefecto.opciones.map((o) => ({ ...o })),
    },
    mode: 'onSubmit',
  });

  const {
    register,
    handleSubmit,
    setValue,
    reset,
    setError,
    clearErrors,
    control,
    formState: { errors, isSubmitting },
  } = form;

  const watchedCompetenciaCodigo = useWatch({
    control,
    name: 'codigoCompetencia',
  });

  const watchedFactorCodigo = useWatch({
    control,
    name: 'codigoFactor',
  });

  const watchedPlantillaOpcionId = useWatch({
    control,
    name: 'plantillaOpcionId',
  });

  const rawOpcionesRespuesta = useWatch({
    control,
    name: 'opcionesRespuesta',
  });

  const watchedOpcionesRespuesta = useMemo(() => {
    return rawOpcionesRespuesta || [];
  }, [rawOpcionesRespuesta]);

  // Carga inicial de competencias (GET /api/competencias)
  useEffect(() => {
    let isMounted = true;
    altaPreguntaService
      .obtenerCompetencias()
      .then((data) => {
        if (isMounted) {
          setCompetencias(data);
          setIsLoadingCompetencias(false);
        }
      })
      .catch(() => {
        if (isMounted) {
          setIsLoadingCompetencias(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  // Carga dinámica de factores al cambiar de competencia (GET /api/competencias/{codigoCompetencia}/factores)
  useEffect(() => {
    let isMounted = true;

    if (!watchedCompetenciaCodigo) {
      const timer = setTimeout(() => {
        if (isMounted) {
          setFactores([]);
          setValue('codigoFactor', '', { shouldValidate: false });
        }
      }, 0);
      return () => {
        isMounted = false;
        clearTimeout(timer);
      };
    }

    altaPreguntaService
      .obtenerFactoresPorCompetencia(watchedCompetenciaCodigo)
      .then((data) => {
        if (isMounted) {
          setFactores(data);
          setIsLoadingFactores(false);
          // Si el factor actual no pertenece a la competencia, resetearlo
          if (!data.some((f) => f.codigo === watchedFactorCodigo)) {
            setValue('codigoFactor', '', { shouldValidate: false });
          }
        }
      })
      .catch(() => {
        if (isMounted) {
          setIsLoadingFactores(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [watchedCompetenciaCodigo, setValue, watchedFactorCodigo]);

  // Cambio de plantilla de escala de respuesta
  const handleCambioPlantilla = useCallback(
    (plantillaId: string) => {
      setValue('plantillaOpcionId', plantillaId, { shouldValidate: true, shouldDirty: true });
      const plantilla = PLANTILLAS_OPCIONES_RESPUESTA.find((p) => p.id === plantillaId);
      if (plantilla) {
        setValue(
          'opcionesRespuesta',
          plantilla.opciones.map((o) => ({ ...o })),
          { shouldValidate: true, shouldDirty: true }
        );
      } else {
        setValue('opcionesRespuesta', [], { shouldValidate: true, shouldDirty: true });
      }
      clearErrors(['plantillaOpcionId', 'opcionesRespuesta']);
    },
    [setValue, clearErrors]
  );

  // Actualización de ponderación individual
  const handleChangePonderacion = useCallback(
    (index: number, ponderacion: number) => {
      const actualizadas = [...watchedOpcionesRespuesta];
      if (actualizadas[index]) {
        actualizadas[index] = {
          ...actualizadas[index],
          ponderacion: isNaN(ponderacion) ? 0 : Math.min(10, Math.max(0, ponderacion)),
        };
        setValue('opcionesRespuesta', actualizadas, { shouldValidate: true, shouldDirty: true });
        clearErrors('opcionesRespuesta');
      }
    },
    [watchedOpcionesRespuesta, setValue, clearErrors]
  );

  // Actualización de datos de opción para escala personalizada
  const handleUpdateOpcion = useCallback(
    (index: number, field: keyof OpcionRespuestaItem, value: string | number) => {
      const actualizadas = [...watchedOpcionesRespuesta];
      if (actualizadas[index]) {
        actualizadas[index] = {
          ...actualizadas[index],
          [field]: value,
        };
        setValue('opcionesRespuesta', actualizadas, { shouldValidate: true, shouldDirty: true });
      }
    },
    [watchedOpcionesRespuesta, setValue]
  );

  // Agregar opción en escala personalizada
  const handleAddOpcion = useCallback(() => {
    const actualizadas = [...watchedOpcionesRespuesta];
    const nuevoOrden = actualizadas.length + 1;
    actualizadas.push({
      orden: nuevoOrden,
      nombre: `OPCIÓN ${nuevoOrden}`,
      descripcion: `DESCRIPCIÓN DE LA OPCIÓN ${nuevoOrden}`,
      ponderacion: 0.0,
    });
    setValue('opcionesRespuesta', actualizadas, { shouldValidate: true, shouldDirty: true });
  }, [watchedOpcionesRespuesta, setValue]);

  // Eliminar opción en escala personalizada
  const handleRemoveOpcion = useCallback(
    (index: number) => {
      if (watchedOpcionesRespuesta.length <= 2) return;
      const filtradas = watchedOpcionesRespuesta
        .filter((_, i) => i !== index)
        .map((opt, i) => ({
          ...opt,
          orden: i + 1,
        }));
      setValue('opcionesRespuesta', filtradas, { shouldValidate: true, shouldDirty: true });
    },
    [watchedOpcionesRespuesta, setValue]
  );

  // Envío del formulario (POST /api/preguntas)
  const onSubmit = async (data: AltaPreguntaFormData) => {
    try {
      const response = await altaPreguntaService.crearPregunta({
        nombre: data.nombre.trim().toUpperCase(),
        enunciado: data.enunciado.trim().toUpperCase(),
        descripcion: data.descripcion && data.descripcion.trim().length > 0 ? data.descripcion.trim().toUpperCase() : null,
        codigoFactor: data.codigoFactor.trim().toUpperCase(),
        opcionesRespuesta: data.opcionesRespuesta.map((opt, idx) => ({
          orden: opt.orden || idx + 1,
          nombre: opt.nombre.trim().toUpperCase(),
          descripcion: opt.descripcion.trim().toUpperCase(),
          ponderacion: Number(opt.ponderacion),
        })),
      });

      // Éxito: Guardar nombre para el modal y abrir confirmación
      setPreguntaCreadaNombre(response.nombre || data.nombre.toUpperCase());
      setIsSuccessModalOpen(true);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errorData = err.data as BackendApiErrorData | undefined;

        // 409 Conflict: Pregunta ya existente (nombre o enunciado) para ese factor
        if (err.status === 409) {
          const mensajeConflicto = errorData?.mensaje || err.message;
          const msgLower = mensajeConflicto.toLowerCase();

          if (msgLower.includes('nombre')) {
            setError('nombre', { type: 'manual', message: mensajeConflicto });
          } else if (msgLower.includes('enunciado') || msgLower.includes('pregunta')) {
            setError('enunciado', { type: 'manual', message: mensajeConflicto });
          } else {
            setError('nombre', { type: 'manual', message: mensajeConflicto });
          }
          return;
        }

        // 422 Unprocessable Entity: Error de regla de negocio (ponderaciones != 10 o negativas)
        if (err.status === 422) {
          const mensajeNegocio = errorData?.mensaje || err.message;
          setError('opcionesRespuesta', { type: 'manual', message: mensajeNegocio });
          return;
        }

        // 400 Bad Request: Campos obligatorios omitidos o exceso de longitud
        if (err.status === 400 && errorData?.detalles && errorData.detalles.length > 0) {
          errorData.detalles.forEach((d) => {
            const fieldKey = d.campo as keyof AltaPreguntaFormData;
            if (fieldKey) {
              setError(fieldKey, { type: 'manual', message: d.mensaje });
            }
          });
          return;
        }

        // 404 Not Found: Factor o competencia no encontrado
        if (err.status === 404) {
          const mensaje404 = errorData?.mensaje || 'El factor referenciado no fue encontrado';
          setError('codigoFactor', { type: 'manual', message: mensaje404 });
          return;
        }

        // Otros errores HTTP asignados al primer campo
        setError('nombre', {
          type: 'manual',
          message: errorData?.mensaje || err.message || 'Error al comunicarse con el servidor',
        });
      } else {
        setError('nombre', {
          type: 'manual',
          message: (err as Error).message || 'Ocurrió un error inesperado al registrar la pregunta',
        });
      }
    }
  };

  // Botón «Sí» del modal: Limpiar el formulario y volver al paso 1 en blanco
  const handleCargarOtro = useCallback(() => {
    setIsSuccessModalOpen(false);

    const preset = PLANTILLAS_OPCIONES_RESPUESTA[0];

    reset({
      codigoCompetencia: '',
      codigoFactor: '',
      nombre: '',
      enunciado: '',
      descripcion: '',
      plantillaOpcionId: preset.id,
      opcionesRespuesta: preset.opciones.map((o) => ({ ...o })),
    });

    clearErrors();
    setPreguntaCreadaNombre('');

    // Auto-enfocar el primer campo interactivo
    setTimeout(() => {
      nombreInputRef.current?.focus();
    }, 60);
  }, [reset, clearErrors]);

  // Botón «No» del modal: Redirigir al listado de preguntas (/preguntas)
  const handleFinalizar = useCallback(() => {
    setIsSuccessModalOpen(false);
    const redirectUrl = options.onSuccessRedirect || '/preguntas';
    router.push(redirectUrl);
  }, [options.onSuccessRedirect, router]);

  // Botón «Cancelar»: Redirigir al listado de preguntas (/preguntas)
  const handleCancelar = useCallback(() => {
    const redirectUrl = options.onCancelRedirect || '/preguntas';
    router.push(redirectUrl);
  }, [options.onCancelRedirect, router]);

  // Modal «Generar con IA» (CU 033)
  const handleOpenIaModal = useCallback(() => {
    setIsIaModalOpen(true);
  }, []);

  const handleCloseIaModal = useCallback(() => {
    setIsIaModalOpen(false);
  }, []);

  const handleApplyIaDraft = useCallback(
    (draft: { nombre: string; enunciado: string; descripcion: string }) => {
      setValue('nombre', draft.nombre, { shouldValidate: true, shouldDirty: true });
      setValue('enunciado', draft.enunciado, { shouldValidate: true, shouldDirty: true });
      setValue('descripcion', draft.descripcion, { shouldValidate: true, shouldDirty: true });
      clearErrors(['nombre', 'enunciado', 'descripcion']);
    },
    [setValue, clearErrors]
  );

  const factorSeleccionadoNombre = factores.find((f) => f.codigo === watchedFactorCodigo)?.nombre;

  return {
    form,
    register,
    control,
    handleSubmit: handleSubmit(onSubmit),
    errors,
    isSubmitting,
    competencias,
    isLoadingCompetencias,
    factores,
    isLoadingFactores,
    watchedCompetenciaCodigo,
    watchedFactorCodigo,
    watchedPlantillaOpcionId,
    watchedOpcionesRespuesta,
    handleCambioPlantilla,
    handleChangePonderacion,
    handleUpdateOpcion,
    handleAddOpcion,
    handleRemoveOpcion,
    isSuccessModalOpen,
    preguntaCreadaNombre,
    handleCargarOtro,
    handleFinalizar,
    handleCancelar,
    isIaModalOpen,
    handleOpenIaModal,
    handleCloseIaModal,
    handleApplyIaDraft,
    factorSeleccionadoNombre,
    nombreInputRef,
  };
}
