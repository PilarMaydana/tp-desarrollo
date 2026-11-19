'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ROUTES } from '@/constants/routes';
import { ApiError, preguntaService } from '../services/preguntaService';
import {
  formatearNumeroPonderacion,
  ModificarPreguntaFormData,
  ModificarPreguntaSchema,
  parsearPonderacion,
  PREGUNTA_LIMITS,
} from '../schemas/modificarPregunta.schema';
import type {
  BackendApiErrorData,
  ErrorItem,
  OpcionPreguntaFormItem,
  OpcionRespuestaResumen,
  PreguntaDetalleResponse,
} from '../types';

export interface UseModificarPreguntaOptions {
  preguntaId: number | string;
  onSuccessRedirect?: string;
  onCancelRedirect?: string;
}

const CAMPO_LABELS: Record<string, string> = {
  nombre: 'Nombre de Pregunta',
  enunciado: 'Pregunta',
  descripcion: 'Descripción',
  opciones: 'Opciones de Respuesta',
};

function normalizarOpcionesDesdePregunta(
  pregunta: PreguntaDetalleResponse
): OpcionPreguntaFormItem[] {
  const lista = Array.isArray(pregunta.opciones) ? [...pregunta.opciones] : [];
  lista.sort((a, b) => {
    const ordenA = a.orden ?? a.nroOrden ?? Number.MAX_SAFE_INTEGER;
    const ordenB = b.orden ?? b.nroOrden ?? Number.MAX_SAFE_INTEGER;
    return ordenA - ordenB;
  });

  return lista.map((opt, idx) => {
    const orden = opt.orden ?? opt.nroOrden ?? idx + 1;
    const nombre = (opt.nombre ?? '').trim().toUpperCase();
    const descripcion = (opt.descripcion ?? '').trim().toUpperCase();
    const ponderacionNum = Number(opt.ponderacion ?? 0);

    return {
      id: opt.id,
      orden,
      nombre,
      descripcion,
      ponderacion: Number.isFinite(ponderacionNum) ? ponderacionNum : 0,
      ponderacionInput: formatearNumeroPonderacion(opt.ponderacion),
    };
  });
}

function extraerMensajeErrorReal(err: unknown, fallback: string): string {
  if (err instanceof ApiError) {
    const data = err.data as BackendApiErrorData | undefined;
    return data?.mensaje || err.message || fallback;
  }
  if (err instanceof Error && err.message) {
    return err.message;
  }
  return fallback;
}

interface CargaPreguntaState {
  requestKey: string | null;
  pregunta: PreguntaDetalleResponse | null;
  error: string | null;
  isNotFound: boolean;
}

interface CargaEscalasState {
  requestKey: string | null;
  escalas: OpcionRespuestaResumen[];
  error: string | null;
}

export function useModificarPregunta({
  preguntaId,
  onSuccessRedirect,
  onCancelRedirect,
}: UseModificarPreguntaOptions) {
  const router = useRouter();
  const nombreInputRef = useRef<HTMLInputElement | null>(null);

  const numericId = useMemo(() => {
    const parsed = Number(preguntaId);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  }, [preguntaId]);

  const [reloadNonce, setReloadNonce] = useState<number>(0);
  const currentRequestKey = numericId !== null ? `${numericId}:${reloadNonce}` : null;

  // Estado de carga inicial de la pregunta (sin setState síncrono dentro de useEffect)
  const [cargaPregunta, setCargaPregunta] = useState<CargaPreguntaState>({
    requestKey: null,
    pregunta: null,
    error: null,
    isNotFound: false,
  });

  // Catálogo de escalas ("Opción de Respuesta")
  const [cargaEscalas, setCargaEscalas] = useState<CargaEscalasState>({
    requestKey: null,
    escalas: [],
    error: null,
  });
  const [escalaSeleccionadaId, setEscalaSeleccionadaId] = useState<string>('');
  const [isLoadingEscalaDetalle, setIsLoadingEscalaDetalle] = useState<boolean>(false);

  // Errores del servidor (400 detalles extra, 404, 409, 422, red)
  const [serverError, setServerError] = useState<string | null>(null);
  const [extraServerErrores, setExtraServerErrores] = useState<ErrorItem[]>([]);

  // Trigger declarativo para enfocar "Nombre de Pregunta" ante 409 Conflict (Flujo 2.C)
  const [focusNombreTrigger, setFocusNombreTrigger] = useState<number>(0);

  // Modal de éxito (200 OK)
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState<boolean>(false);

  const form = useForm<ModificarPreguntaFormData>({
    resolver: zodResolver(ModificarPreguntaSchema),
    defaultValues: {
      nombre: '',
      enunciado: '',
      descripcion: '',
      opciones: [],
    },
    mode: 'onSubmit',
    reValidateMode: 'onSubmit',
  });

  const {
    register,
    handleSubmit,
    setValue,
    getValues,
    reset,
    setError,
    clearErrors,
    control,
    formState: { errors, isSubmitting },
  } = form;

  const watchedNombre = useWatch({ control, name: 'nombre' }) ?? '';
  const watchedEnunciado = useWatch({ control, name: 'enunciado' }) ?? '';
  const watchedDescripcion = useWatch({ control, name: 'descripcion' }) ?? '';
  const rawOpciones = useWatch({ control, name: 'opciones' });

  const watchedOpciones = useMemo<OpcionPreguntaFormItem[]>(() => {
    return rawOpciones ?? [];
  }, [rawOpciones]);

  // Efecto para enfocar "Nombre de Pregunta" cuando ocurre un 409 Conflict (Flujo 2.C)
  useEffect(() => {
    if (focusNombreTrigger === 0) return;
    const timer = setTimeout(() => {
      if (nombreInputRef.current) {
        nombreInputRef.current.focus();
        nombreInputRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 0);
    return () => clearTimeout(timer);
  }, [focusNombreTrigger]);

  // Carga inicial: pregunta (GET /api/preguntas/{id}) y escalas (GET /api/opciones-respuesta)
  useEffect(() => {
    if (numericId === null || currentRequestKey === null) {
      return;
    }

    let isMounted = true;
    const reqKey = currentRequestKey;

    preguntaService
      .obtenerPreguntaPorId(numericId)
      .then((data) => {
        if (!isMounted) return;
        setCargaPregunta({
          requestKey: reqKey,
          pregunta: data,
          error: null,
          isNotFound: false,
        });
        setEscalaSeleccionadaId('');
        setServerError(null);
        setExtraServerErrores([]);
        reset({
          nombre: (data.nombre ?? '').toUpperCase(),
          enunciado: (data.enunciado ?? '').toUpperCase(),
          descripcion: (data.descripcion ?? '').toUpperCase(),
          opciones: normalizarOpcionesDesdePregunta(data),
        });
        clearErrors();
      })
      .catch((err: unknown) => {
        if (!isMounted) return;
        setCargaPregunta({
          requestKey: reqKey,
          pregunta: null,
          error: extraerMensajeErrorReal(
            err,
            'No fue posible obtener los datos de la pregunta desde el servidor.'
          ),
          isNotFound: err instanceof ApiError && err.status === 404,
        });
      });

    preguntaService
      .listarEscalasOpcionesRespuesta()
      .then((listaEscalas) => {
        if (!isMounted) return;
        setCargaEscalas({
          requestKey: reqKey,
          escalas: Array.isArray(listaEscalas) ? listaEscalas : [],
          error: null,
        });
      })
      .catch((err: unknown) => {
        if (!isMounted) return;
        setCargaEscalas({
          requestKey: reqKey,
          escalas: [],
          error: extraerMensajeErrorReal(
            err,
            'No se pudieron cargar las escalas de opciones de respuesta.'
          ),
        });
      });

    return () => {
      isMounted = false;
    };
  }, [numericId, currentRequestKey, reset, clearErrors]);

  // Estados derivados de carga inicial
  const isLoadingPregunta =
    numericId !== null && cargaPregunta.requestKey !== currentRequestKey;
  const pregunta =
    numericId !== null && cargaPregunta.requestKey === currentRequestKey
      ? cargaPregunta.pregunta
      : null;
  const loadError =
    numericId === null
      ? 'El identificador de la pregunta no es válido.'
      : cargaPregunta.requestKey === currentRequestKey
      ? cargaPregunta.error
      : null;
  const isNotFoundError =
    numericId === null
      ? true
      : cargaPregunta.requestKey === currentRequestKey
      ? cargaPregunta.isNotFound
      : false;

  const isLoadingEscalas =
    numericId !== null && cargaEscalas.requestKey !== currentRequestKey;
  const escalas =
    numericId !== null && cargaEscalas.requestKey === currentRequestKey
      ? cargaEscalas.escalas
      : [];
  const escalasError =
    numericId !== null && cargaEscalas.requestKey === currentRequestKey
      ? cargaEscalas.error
      : null;

  const handleRetryLoad = useCallback(() => {
    setReloadNonce((n) => n + 1);
  }, []);

  // Cambio de escala en "Opción de Respuesta": consulta GET /api/opciones-respuesta/{id}
  // y reemplaza la grilla con ponderaciones vacías.
  const handleCambioEscala = useCallback(
    async (escalaIdStr: string) => {
      setEscalaSeleccionadaId(escalaIdStr);
      setServerError(null);
      setExtraServerErrores([]);

      if (!escalaIdStr) {
        return;
      }

      const escalaId = Number(escalaIdStr);
      if (!Number.isInteger(escalaId) || escalaId <= 0) {
        return;
      }

      setIsLoadingEscalaDetalle(true);
      try {
        const detalle = await preguntaService.obtenerEscalaOpcionRespuestaPorId(escalaId);
        const itemsOrdenados = [...(detalle.items ?? [])].sort(
          (a, b) => (a.orden ?? 0) - (b.orden ?? 0)
        );

        const nuevasOpciones: OpcionPreguntaFormItem[] = itemsOrdenados.map((item, idx) => {
          const descNorm = (item.descripcion ?? '').trim().toUpperCase();
          return {
            orden: item.orden ?? idx + 1,
            nombre: descNorm.slice(0, PREGUNTA_LIMITS.OPCION_NOMBRE_MAX),
            descripcion: descNorm,
            ponderacion: 0,
            ponderacionInput: '',
          };
        });

        setValue('opciones', nuevasOpciones, {
          shouldDirty: true,
          shouldValidate: false,
        });
        clearErrors('opciones');
      } catch (err: unknown) {
        setServerError(
          extraerMensajeErrorReal(
            err,
            'No fue posible cargar los ítems de la escala seleccionada.'
          )
        );
      } finally {
        setIsLoadingEscalaDetalle(false);
      }
    },
    [setValue, clearErrors]
  );

  // Edición libre de ponderación (mantiene vacío, decimales o negativos para validación explícita)
  const handleChangePonderacionRaw = useCallback(
    (index: number, rawValue: string) => {
      const actuales = getValues('opciones') ?? [];
      if (!actuales[index]) return;

      const parsed = parsearPonderacion(rawValue);
      const actualizadas = actuales.map((opt, idx) =>
        idx === index
          ? {
              ...opt,
              ponderacionInput: rawValue,
              ponderacion: parsed.valid && parsed.value !== null ? parsed.value : 0,
            }
          : opt
      );

      setValue('opciones', actualizadas, {
        shouldDirty: true,
        shouldValidate: false,
      });
      clearErrors([`opciones.${index}.ponderacionInput`, 'opciones']);
      setServerError(null);
      setExtraServerErrores([]);
    },
    [getValues, setValue, clearErrors]
  );

  // Recopilación agrupada de TODOS los errores (Zod + detalles del backend) sin tapar campos ni botones
  const erroresAgrupados = useMemo<ErrorItem[]>(() => {
    const items: ErrorItem[] = [];
    const vistos = new Set<string>();

    const agregar = (campo: string | undefined, mensaje: string | undefined) => {
      if (!mensaje) return;
      const key = `${campo ?? ''}::${mensaje}`;
      if (vistos.has(key)) return;
      vistos.add(key);
      items.push({ campo, mensaje });
    };

    if (errors.nombre?.message) {
      agregar(CAMPO_LABELS.nombre, errors.nombre.message);
    }
    if (errors.enunciado?.message) {
      agregar(CAMPO_LABELS.enunciado, errors.enunciado.message);
    }
    if (errors.descripcion?.message) {
      agregar(CAMPO_LABELS.descripcion, errors.descripcion.message);
    }

    const opcionesRootMsg = errors.opciones?.message || errors.opciones?.root?.message;
    if (opcionesRootMsg) {
      agregar(CAMPO_LABELS.opciones, opcionesRootMsg);
    }

    if (Array.isArray(errors.opciones)) {
      errors.opciones.forEach((optErr, idx) => {
        if (!optErr) return;
        const labelOpcion = `Opción ${watchedOpciones[idx]?.orden ?? idx + 1}`;
        if (optErr.nombre?.message) {
          agregar(labelOpcion, optErr.nombre.message);
        }
        if (optErr.descripcion?.message) {
          agregar(labelOpcion, optErr.descripcion.message);
        }
        if (optErr.ponderacionInput?.message) {
          agregar(labelOpcion, optErr.ponderacionInput.message);
        }
      });
    }

    extraServerErrores.forEach((e) => agregar(e.campo, e.mensaje));

    // Evitar duplicar el mensajeGeneral si ya está exactamente listado en los items
    if (serverError) {
      return items.filter((item) => item.mensaje !== serverError);
    }

    return items;
  }, [errors, watchedOpciones, extraServerErrores, serverError]);

  // Errores por fila para resaltar inputs de ponderación en GrillaOpcionesRespuesta
  const rowErrors = useMemo<Record<number, string>>(() => {
    const map: Record<number, string> = {};
    if (Array.isArray(errors.opciones)) {
      errors.opciones.forEach((optErr, idx) => {
        const msg =
          optErr?.ponderacionInput?.message ||
          optErr?.nombre?.message ||
          optErr?.descripcion?.message;
        if (msg) {
          map[idx] = msg;
        }
      });
    }
    return map;
  }, [errors.opciones]);

  // Envío PUT /api/preguntas/{id}
  const onSubmit = useCallback(
    async (data: ModificarPreguntaFormData) => {
      if (numericId === null) return;

      setServerError(null);
      setExtraServerErrores([]);

      const descripcionTrim = data.descripcion.trim();
      const payload = {
        nombre: data.nombre.trim().toUpperCase(),
        enunciado: data.enunciado.trim().toUpperCase(),
        descripcion: descripcionTrim.length > 0 ? descripcionTrim.toUpperCase() : null,
        opciones: data.opciones.map((opt, idx) => {
          const parsed = parsearPonderacion(opt.ponderacionInput);
          const descTrim = opt.descripcion.trim();
          return {
            orden: opt.orden ?? idx + 1,
            nombre: opt.nombre.trim().toUpperCase(),
            descripcion: descTrim.length > 0 ? descTrim.toUpperCase() : null,
            ponderacion: parsed.value ?? Number(opt.ponderacion),
          };
        }),
      };

      try {
        const actualizada = await preguntaService.modificarPregunta(numericId, payload);
        setCargaPregunta((prev) => ({
          ...prev,
          pregunta: actualizada,
          error: null,
          isNotFound: false,
        }));
        setIsSuccessModalOpen(true);
      } catch (err: unknown) {
        if (err instanceof ApiError) {
          const errorData = err.data as BackendApiErrorData | undefined;
          const mensajeBackend = errorData?.mensaje || err.message;

          // 400 Bad Request (VALIDACION): mapear todos los detalles[] sin tapar campos ni botones
          if (err.status === 400 && errorData?.detalles && errorData.detalles.length > 0) {
            const noMapeados: ErrorItem[] = [];

            errorData.detalles.forEach((detalle) => {
              const campo = detalle.campo || '';
              if (campo === 'nombre' || campo === 'enunciado' || campo === 'descripcion') {
                setError(campo, { type: 'server', message: detalle.mensaje });
                return;
              }
              if (campo === 'opciones' || campo === 'opcionesRespuesta') {
                setError('opciones', { type: 'server', message: detalle.mensaje });
                return;
              }
              const optMatch = campo.match(
                /^(?:opciones|opcionesRespuesta)\[(\d+)\]\.(ponderacion|nombre|descripcion)$/
              );
              if (optMatch) {
                const idx = Number(optMatch[1]);
                const subCampo = optMatch[2];
                if (subCampo === 'ponderacion') {
                  setError(`opciones.${idx}.ponderacionInput`, {
                    type: 'server',
                    message: detalle.mensaje,
                  });
                } else if (subCampo === 'nombre' || subCampo === 'descripcion') {
                  setError(`opciones.${idx}.${subCampo}`, {
                    type: 'server',
                    message: detalle.mensaje,
                  });
                }
                return;
              }

              noMapeados.push({
                campo: CAMPO_LABELS[campo] || campo || undefined,
                mensaje: detalle.mensaje,
              });
            });

            if (noMapeados.length > 0) {
              setExtraServerErrores(noMapeados);
            }
            return;
          }

          // 409 Conflict (RECURSO_DUPLICADO - Flujo 2.C): mostrar mensaje y hacer foco en "Nombre de Pregunta"
          if (err.status === 409) {
            const msg =
              mensajeBackend ||
              'Ya existe otra pregunta con esos datos para el factor seleccionado.';
            setServerError(msg);
            const msgLower = msg.toLowerCase();
            if (msgLower.includes('nombre')) {
              setError('nombre', { type: 'server', message: msg });
            }
            if (msgLower.includes('enunciado')) {
              setError('enunciado', { type: 'server', message: msg });
            }
            if (!msgLower.includes('nombre') && !msgLower.includes('enunciado')) {
              setError('nombre', { type: 'server', message: msg });
            }
            setFocusNombreTrigger((n) => n + 1);
            return;
          }

          // 422 Unprocessable Entity (REGLA_NEGOCIO): ej. suma de ponderaciones != 10
          if (err.status === 422) {
            const msg =
              mensajeBackend ||
              'La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos.';
            setServerError(msg);
            setError('opciones', { type: 'server', message: msg });
            return;
          }

          // 404 Not Found u otros errores HTTP: mostrar mensaje real del backend
          setServerError(mensajeBackend || 'No fue posible guardar las modificaciones.');
        } else {
          setServerError(
            extraerMensajeErrorReal(
              err,
              'Error de conexión con el servidor al guardar las modificaciones.'
            )
          );
        }
      }
    },
    [numericId, setError]
  );

  const onInvalid = useCallback(() => {
    setServerError(null);
    setExtraServerErrores([]);
  }, []);

  const handleConfirmarExito = useCallback(() => {
    setIsSuccessModalOpen(false);
    router.push(onSuccessRedirect || ROUTES.CONSULTOR.PREGUNTAS);
  }, [onSuccessRedirect, router]);

  const handleCancelar = useCallback(() => {
    router.push(onCancelRedirect || ROUTES.CONSULTOR.PREGUNTAS);
  }, [onCancelRedirect, router]);

  const handleDismissErrores = useCallback(() => {
    setServerError(null);
    setExtraServerErrores([]);
    clearErrors();
  }, [clearErrors]);

  return {
    pregunta,
    isLoadingPregunta,
    loadError,
    isNotFoundError,
    handleRetryLoad,
    escalas,
    isLoadingEscalas,
    escalasError,
    escalaSeleccionadaId,
    isLoadingEscalaDetalle,
    handleCambioEscala,
    register,
    handleSubmit: handleSubmit(onSubmit, onInvalid),
    errors,
    isSubmitting,
    watchedNombre,
    watchedEnunciado,
    watchedDescripcion,
    watchedOpciones,
    handleChangePonderacionRaw,
    serverError,
    erroresAgrupados,
    rowErrors,
    handleDismissErrores,
    isSuccessModalOpen,
    handleConfirmarExito,
    handleCancelar,
    nombreInputRef,
  };
}
