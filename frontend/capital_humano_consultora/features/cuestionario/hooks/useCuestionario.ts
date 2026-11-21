'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ApiError, cuestionarioService } from '../services/cuestionarioService';
import type {
  BackendApiErrorData,
  BloqueCuestionarioDTO,
  CuestionarioErrorPanel,
  ErrorItem,
  FaseCuestionario,
  InstruccionesCuestionarioDTO,
  MotivoFinalizacion,
  PreguntaAsignadaDTO,
} from '../types';

function parseCuestionarioId(rawId: string | number | null | undefined): number | null {
  if (typeof rawId === 'number') {
    return Number.isInteger(rawId) && rawId > 0 ? rawId : null;
  }
  if (typeof rawId === 'string') {
    const trimmed = rawId.trim();
    if (!/^\d+$/.test(trimmed)) return null;
    const parsed = Number(trimmed);
    return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null;
  }
  return null;
}

function getApiErrorData(err: ApiError): BackendApiErrorData | null {
  if (err.data && typeof err.data === 'object') {
    return err.data as BackendApiErrorData;
  }
  return null;
}

function inicializarRespuestasDeBloque(
  bloque: BloqueCuestionarioDTO
): Record<number, number> {
  const mapa: Record<number, number> = {};
  for (const pregunta of bloque.preguntas) {
    if (
      pregunta.respuestaOpcionId !== null &&
      pregunta.respuestaOpcionId !== undefined
    ) {
      mapa[pregunta.preguntaAsignadaId] = pregunta.respuestaOpcionId;
    }
  }
  return mapa;
}

export function formatearTiempoMMSS(segundosTotales: number): string {
  const seguro = Math.max(0, Math.floor(segundosTotales));
  const minutos = Math.floor(seguro / 60);
  const segundos = seguro % 60;
  return `${String(minutos).padStart(2, '0')}:${String(segundos).padStart(2, '0')}`;
}

export function useCuestionario(rawId: string | number) {
  const router = useRouter();
  const cuestionarioId = useMemo(() => parseCuestionarioId(rawId), [rawId]);

  const [fase, setFase] = useState<FaseCuestionario>(() =>
    cuestionarioId === null ? 'no_encontrado' : 'cargando'
  );
  const [instruccionesData, setInstruccionesData] =
    useState<InstruccionesCuestionarioDTO | null>(null);
  const [nombrePuesto, setNombrePuesto] = useState<string | null>(null);
  const [totalBloques, setTotalBloques] = useState<number>(0);
  const [bloqueActual, setBloqueActual] = useState<BloqueCuestionarioDTO | null>(null);
  const [segundosRestantes, setSegundosRestantes] = useState<number>(0);
  const [respuestasSeleccionadas, setRespuestasSeleccionadas] = useState<
    Record<number, number>
  >({});
  const [preguntasFaltantesIds, setPreguntasFaltantesIds] = useState<number[]>([]);
  const [errorPanel, setErrorPanel] = useState<CuestionarioErrorPanel | null>(null);
  const [mensajeEstado, setMensajeEstado] = useState<string | null>(() =>
    cuestionarioId === null
      ? 'El identificador del cuestionario indicado no es válido.'
      : null
  );
  const [motivoFinalizacion, setMotivoFinalizacion] =
    useState<MotivoFinalizacion | null>(null);
  const [mensajeFinalizacion, setMensajeFinalizacion] = useState<string | null>(null);

  const [isStarting, setIsStarting] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [tiempoAgotado, setTiempoAgotado] = useState<boolean>(false);
  const [mostrarModalTiempoAgotado, setMostrarModalTiempoAgotado] =
    useState<boolean>(false);

  // Guardas por ref para garantizar:
  // 1) Un único GET /instrucciones + POST /iniciar por carga (incluso en React Strict Mode).
  // 2) Un único POST en doble clic / Enter repetido sobre Aceptar o Siguiente/Finalizar.
  const loadedIdRef = useRef<number | null>(null);
  const requestSeqRef = useRef<number>(0);
  const isStartingRef = useRef<boolean>(false);
  const isSubmittingRef = useRef<boolean>(false);
  const segundosRef = useRef<number>(0);

  const aplicarBloqueYTimer = useCallback(
    (bloque: BloqueCuestionarioDTO, nuevosSegundos: number, total?: number) => {
      const segundosClamped = Math.max(0, Math.floor(nuevosSegundos));
      segundosRef.current = segundosClamped;

      if (typeof total === 'number') {
        setTotalBloques(total);
      }
      setBloqueActual(bloque);
      setRespuestasSeleccionadas(inicializarRespuestasDeBloque(bloque));
      setPreguntasFaltantesIds([]);
      setErrorPanel(null);
      setSegundosRestantes(segundosClamped);

      if (segundosClamped <= 0) {
        setTiempoAgotado(true);
        setMostrarModalTiempoAgotado(true);
      } else {
        setTiempoAgotado(false);
        setMostrarModalTiempoAgotado(false);
      }
      setFase('wizard');
    },
    []
  );

  const cargarInicial = useCallback(
    async (id: number) => {
      const seq = ++requestSeqRef.current;

      setFase('cargando');
      setInstruccionesData(null);
      setNombrePuesto(null);
      setBloqueActual(null);
      setTotalBloques(0);
      setRespuestasSeleccionadas({});
      setPreguntasFaltantesIds([]);
      setErrorPanel(null);
      setMensajeEstado(null);
      setMotivoFinalizacion(null);
      setMensajeFinalizacion(null);
      setTiempoAgotado(false);
      setMostrarModalTiempoAgotado(false);
      segundosRef.current = 0;

      try {
        const instrucciones = await cuestionarioService.obtenerInstrucciones(id);
        if (seq !== requestSeqRef.current) return;

        setInstruccionesData(instrucciones);
        setNombrePuesto(instrucciones.nombrePuesto);

        if (instrucciones.estado === 'ACTIVO') {
          setFase('instrucciones');
          return;
        }

        if (instrucciones.estado === 'EN_PROCESO') {
          // Flujo 1.C: reingreso directo al wizard sin pasar por instrucciones
          const iniciado = await cuestionarioService.iniciarCuestionario(id);
          if (seq !== requestSeqRef.current) return;

          aplicarBloqueYTimer(
            iniciado.bloqueActual,
            iniciado.segundosRestantes,
            iniciado.totalBloques
          );
          return;
        }

        // Cualquier otro estado no contemplado como disponible
        setMensajeEstado(
          `El cuestionario se encuentra en estado ${instrucciones.estado} y no está disponible para ser completado.`
        );
        setFase('no_disponible');
      } catch (err: unknown) {
        if (seq !== requestSeqRef.current) return;

        if (err instanceof ApiError) {
          const data = getApiErrorData(err);
          const mensajeBackend = data?.mensaje || err.message;
          const codigo = data?.codigo;

          if (err.status === 404 || codigo === 'RECURSO_NO_ENCONTRADO') {
            setMensajeEstado(mensajeBackend);
            setFase('no_encontrado');
            return;
          }

          if (err.status === 422 || codigo === 'REGLA_NEGOCIO') {
            setMensajeEstado(mensajeBackend);
            setFase('no_disponible');
            return;
          }

          setMensajeEstado(mensajeBackend);
          setFase('error_conexion');
          return;
        }

        setMensajeEstado(
          err instanceof Error && err.message
            ? err.message
            : 'No fue posible comunicarse con el servidor.'
        );
        setFase('error_conexion');
      }
    },
    [aplicarBloqueYTimer]
  );

  // Carga inicial al montar o al cambiar el id del cuestionario (protegida contra Strict Mode)
  useEffect(() => {
    if (cuestionarioId === null) {
      loadedIdRef.current = null;
      return;
    }

    if (loadedIdRef.current === cuestionarioId) {
      return;
    }
    loadedIdRef.current = cuestionarioId;

    void cargarInicial(cuestionarioId);
  }, [cuestionarioId, cargarInicial]);

  // Contador descendente local del wizard (setInterval con limpieza al desmontar)
  useEffect(() => {
    if (fase !== 'wizard' || tiempoAgotado) {
      return;
    }

    const intervalId = window.setInterval(() => {
      const siguiente = Math.max(0, segundosRef.current - 1);
      segundosRef.current = siguiente;
      setSegundosRestantes(siguiente);

      if (siguiente <= 0) {
        window.clearInterval(intervalId);
        setTiempoAgotado(true);
        setMostrarModalTiempoAgotado(true);
      }
    }, 1000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [fase, tiempoAgotado]);

  // Botón «Aceptar» en pantalla de instrucciones (Paso 2)
  const handleAceptarInstrucciones = useCallback(async () => {
    if (cuestionarioId === null || isStartingRef.current) return;

    isStartingRef.current = true;
    setIsStarting(true);
    setErrorPanel(null);

    try {
      const iniciado = await cuestionarioService.iniciarCuestionario(cuestionarioId);
      aplicarBloqueYTimer(
        iniciado.bloqueActual,
        iniciado.segundosRestantes,
        iniciado.totalBloques
      );
      if (typeof window !== 'undefined') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const data = getApiErrorData(err);
        const mensajeBackend = data?.mensaje || err.message;
        const codigo = data?.codigo;

        if (err.status === 404 || codigo === 'RECURSO_NO_ENCONTRADO') {
          setMensajeEstado(mensajeBackend);
          setFase('no_encontrado');
          return;
        }

        if (err.status === 422 || codigo === 'REGLA_NEGOCIO') {
          setMensajeEstado(mensajeBackend);
          setFase('no_disponible');
          return;
        }

        setErrorPanel({
          titulo: 'No fue posible iniciar el cuestionario',
          mensajeGeneral: mensajeBackend,
          detalles: [],
        });
        return;
      }

      setErrorPanel({
        titulo: 'Error de conexión',
        mensajeGeneral:
          err instanceof Error && err.message
            ? err.message
            : 'No fue posible comunicarse con el servidor.',
        detalles: [],
      });
    } finally {
      isStartingRef.current = false;
      setIsStarting(false);
    }
  }, [cuestionarioId, aplicarBloqueYTimer]);

  // Botón «Cancelar» en instrucciones (Flujo 2.A) o salida en pantalla final
  const handleSalir = useCallback(() => {
    // TODO: apuntar a la pantalla de ingreso del candidato cuando exista el CU 01.
    router.push('/');
  }, [router]);

  // Reintento manual solo ante error de conexión / 500 en carga inicial
  const handleReintentarCarga = useCallback(() => {
    if (cuestionarioId === null) return;
    loadedIdRef.current = cuestionarioId;
    void cargarInicial(cuestionarioId);
  }, [cuestionarioId, cargarInicial]);

  // Selección de opción de respuesta en una pregunta del bloque actual
  const handleSeleccionarOpcion = useCallback(
    (preguntaAsignadaId: number, opcionId: number) => {
      if (tiempoAgotado || isSubmittingRef.current) return;

      setRespuestasSeleccionadas((prev) => ({
        ...prev,
        [preguntaAsignadaId]: opcionId,
      }));

      setPreguntasFaltantesIds((prev) => {
        if (!prev.includes(preguntaAsignadaId)) return prev;
        const restantes = prev.filter((id) => id !== preguntaAsignadaId);
        if (restantes.length === 0) {
          setErrorPanel((panelActual) =>
            panelActual?.esValidacion ? null : panelActual
          );
        }
        return restantes;
      });
    },
    [tiempoAgotado]
  );

  // Preguntas del bloque actual ordenadas por nroOrden
  const preguntasOrdenadas: PreguntaAsignadaDTO[] = useMemo(() => {
    if (!bloqueActual) return [];
    return [...bloqueActual.preguntas].sort((a, b) => a.nroOrden - b.nroOrden);
  }, [bloqueActual]);

  const esUltimoBloque = Boolean(
    bloqueActual && totalBloques > 0 && bloqueActual.numero === totalBloques
  );

  // Botón «Siguiente» / «Finalizar» (Pasos 5 a 8 y Flujos 6.A / 8.A)
  const handleEnviarBloque = useCallback(async () => {
    if (
      cuestionarioId === null ||
      !bloqueActual ||
      tiempoAgotado ||
      isSubmittingRef.current
    ) {
      return;
    }

    // 1. Validación local (Flujo 6.A): todas las preguntas del bloque deben tener respuesta
    const faltantes = preguntasOrdenadas.filter(
      (pregunta) => respuestasSeleccionadas[pregunta.preguntaAsignadaId] === undefined
    );

    if (faltantes.length > 0) {
      setPreguntasFaltantesIds(faltantes.map((p) => p.preguntaAsignadaId));
      setErrorPanel({
        titulo: 'Faltan preguntas por responder',
        mensajeGeneral:
          faltantes.length === 1
            ? 'Debe responder la pregunta pendiente del bloque antes de continuar.'
            : 'Debe responder todas las preguntas del bloque antes de continuar.',
        detalles: faltantes.map((p) => ({
          campo: `Pregunta N.° ${p.nroOrden}`,
          mensaje: 'Seleccione una opción de respuesta.',
        })),
        esValidacion: true,
      });

      if (typeof window !== 'undefined') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
      return;
    }

    // 2. Armar payload con todas las preguntas del bloque (nunca opcionId null)
    const respuestasPayload = preguntasOrdenadas.map((pregunta) => ({
      preguntaAsignadaId: pregunta.preguntaAsignadaId,
      opcionId: respuestasSeleccionadas[pregunta.preguntaAsignadaId],
    }));

    isSubmittingRef.current = true;
    setIsSubmitting(true);
    setErrorPanel(null);
    setPreguntasFaltantesIds([]);

    try {
      const response = await cuestionarioService.responderBloque(
        cuestionarioId,
        bloqueActual.numero,
        { respuestas: respuestasPayload }
      );

      // Paso 8: cuestionario finalizado
      if (response.completado) {
        setMotivoFinalizacion('completado');
        setMensajeFinalizacion(
          response.mensaje || 'El cuestionario ha sido completado con éxito'
        );
        setErrorPanel(null);
        setPreguntasFaltantesIds([]);
        setFase('finalizado');
        if (typeof window !== 'undefined') {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
        return;
      }

      // Flujo 8.A: avanzar al siguiente bloque y actualizar timer
      if (response.bloqueSiguiente) {
        aplicarBloqueYTimer(
          response.bloqueSiguiente,
          response.segundosRestantes ?? segundosRef.current
        );
        if (typeof window !== 'undefined') {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
        return;
      }

      setErrorPanel({
        titulo: 'Respuesta inesperada del servidor',
        mensajeGeneral:
          'No se recibió el siguiente bloque del cuestionario. Intente nuevamente.',
        detalles: [],
      });
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const data = getApiErrorData(err);
        const mensajeBackend = data?.mensaje || err.message;
        const codigo = data?.codigo;

        // 400 VALIDACION (Flujo 6.A desde backend)
        if (err.status === 400 && codigo === 'VALIDACION') {
          const detallesRaw = Array.isArray(data?.detalles) ? data.detalles : [];
          const idsFaltantes: number[] = [];
          const detallesPanel: ErrorItem[] = [];

          for (const d of detallesRaw) {
            const parsedPreguntaId = Number(d.campo);
            if (Number.isInteger(parsedPreguntaId) && parsedPreguntaId > 0) {
              idsFaltantes.push(parsedPreguntaId);
            }
            detallesPanel.push({
              mensaje: d.mensaje,
            });
          }

          setPreguntasFaltantesIds(idsFaltantes);
          setErrorPanel({
            titulo: 'Faltan preguntas por responder',
            mensajeGeneral: mensajeBackend,
            detalles: detallesPanel,
            esValidacion: true,
          });
          if (typeof window !== 'undefined') {
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }
          return;
        }

        // 422 REGLA_NEGOCIO (tiempo agotado, bloque inválido, no disponible)
        if (err.status === 422 || codigo === 'REGLA_NEGOCIO') {
          setMensajeEstado(mensajeBackend);
          setFase('no_disponible');
          if (typeof window !== 'undefined') {
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }
          return;
        }

        // 404 RECURSO_NO_ENCONTRADO
        if (err.status === 404 || codigo === 'RECURSO_NO_ENCONTRADO') {
          setMensajeEstado(mensajeBackend);
          setFase('no_encontrado');
          if (typeof window !== 'undefined') {
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }
          return;
        }

        // 500 u otros errores HTTP/red: mantener respuestas y dejar botón activo para reintentar
        setErrorPanel({
          titulo: 'No fue posible guardar las respuestas',
          mensajeGeneral: mensajeBackend,
          detalles: Array.isArray(data?.detalles) ? data.detalles : [],
        });
        if (typeof window !== 'undefined') {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
        return;
      }

      setErrorPanel({
        titulo: 'Error de conexión',
        mensajeGeneral:
          err instanceof Error && err.message
            ? err.message
            : 'No fue posible comunicarse con el servidor. Sus respuestas se mantienen seleccionadas para reintentar.',
        detalles: [],
      });
    } finally {
      isSubmittingRef.current = false;
      setIsSubmitting(false);
    }
  }, [
    cuestionarioId,
    bloqueActual,
    tiempoAgotado,
    preguntasOrdenadas,
    respuestasSeleccionadas,
    aplicarBloqueYTimer,
  ]);

  // Cierre del modal de tiempo agotado -> pantalla final sin llamadas extra al backend
  const handleCerrarModalTiempoAgotado = useCallback(() => {
    setMostrarModalTiempoAgotado(false);
    setMotivoFinalizacion('tiempo_agotado');
    setMensajeFinalizacion(
      'Se agotó el tiempo máximo para completar el cuestionario. Ya no es posible enviar nuevas respuestas.'
    );
    setFase('finalizado');
  }, []);

  const handleDismissErrorPanel = useCallback(() => {
    setErrorPanel(null);
  }, []);

  const faseEfectiva: FaseCuestionario =
    cuestionarioId === null ? 'no_encontrado' : fase;
  const mensajeEstadoEfectivo: string | null =
    cuestionarioId === null
      ? 'El identificador del cuestionario indicado no es válido.'
      : mensajeEstado;

  return {
    cuestionarioId,
    fase: faseEfectiva,
    instruccionesData,
    nombrePuesto,
    totalBloques,
    bloqueActual,
    preguntasOrdenadas,
    esUltimoBloque,
    segundosRestantes,
    tiempoFormateado: formatearTiempoMMSS(segundosRestantes),
    respuestasSeleccionadas,
    preguntasFaltantesIds,
    errorPanel,
    mensajeEstado: mensajeEstadoEfectivo,
    motivoFinalizacion,
    mensajeFinalizacion,
    isStarting,
    isSubmitting,
    tiempoAgotado,
    mostrarModalTiempoAgotado,
    handleAceptarInstrucciones,
    handleSalir,
    handleReintentarCarga,
    handleSeleccionarOpcion,
    handleEnviarBloque,
    handleCerrarModalTiempoAgotado,
    handleDismissErrorPanel,
  };
}
