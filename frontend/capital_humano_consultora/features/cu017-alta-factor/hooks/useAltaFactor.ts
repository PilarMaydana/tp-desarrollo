'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ApiError } from '@/services/apiClient';
import {
  AltaFactorFormData,
  AltaFactorSchema,
} from '../schemas/altaFactor.schema';
import { altaFactorService } from '../services/altaFactorService';
import {
  BackendApiErrorData,
  CompetenciaItem,
  FactorItem,
} from '../types';

export interface UseAltaFactorOptions {
  onSuccessRedirect?: string;
  onCancelRedirect?: string;
  defaultCompetenciaId?: number;
}

export function useAltaFactor(options: UseAltaFactorOptions = {}) {
  const router = useRouter();
  const codigoInputRef = useRef<HTMLInputElement | null>(null);

  const [competencias, setCompetencias] = useState<CompetenciaItem[]>([]);
  const [isLoadingCompetencias, setIsLoadingCompetencias] = useState(true);

  // Factores y órdenes existentes de la competencia seleccionada
  const [factoresCompetencia, setFactoresCompetencia] = useState<FactorItem[]>([]);
  const [isLoadingFactores, setIsLoadingFactores] = useState(false);

  // Guardamos el último factor recién creado para actualizar inmediatamente la lista local de ocupados
  const [ultimoFactorCreado, setUltimoFactorCreado] = useState<FactorItem | null>(null);

  // Estados del modal de confirmación de éxito
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [factorCreadoNombre, setFactorCreadoNombre] = useState('');

  const form = useForm<AltaFactorFormData>({
    resolver: zodResolver(AltaFactorSchema),
    defaultValues: {
      codigo: '',
      nombre: '',
      descripcion: '',
      competenciaId: options.defaultCompetenciaId ? String(options.defaultCompetenciaId) : '',
      nroOrden: '',
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
    formState: { errors, isSubmitting },
  } = form;

  const watchedCompetenciaId = useWatch({
    control: form.control,
    name: 'competenciaId',
  });

  // Carga inicial de competencias desde la API
  useEffect(() => {
    let isMounted = true;
    altaFactorService
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

  // Cuando cambia la competencia seleccionada, consultar factores y SIEMPRE actualizar el orden al primer número libre
  useEffect(() => {
    let isMounted = true;
    const compIdNum = watchedCompetenciaId ? Number(watchedCompetenciaId) : null;

    if (!compIdNum || isNaN(compIdNum) || compIdNum <= 0) {
      const timer = setTimeout(() => {
        if (isMounted) {
          setFactoresCompetencia([]);
          setValue('nroOrden', '', { shouldValidate: false });
        }
      }, 0);
      return () => {
        isMounted = false;
        clearTimeout(timer);
      };
    }

    altaFactorService
      .obtenerFactoresPorCompetencia(compIdNum)
      .then((factores) => {
        if (isMounted) {
          // Si tenemos un factor recién creado para esta misma competencia, asegurarse de incluirlo
          const listaCompleta = [...factores];
          if (ultimoFactorCreado && Number(ultimoFactorCreado.competenciaId) === compIdNum) {
            if (!listaCompleta.some((f) => f.nroOrden === ultimoFactorCreado.nroOrden)) {
              listaCompleta.push(ultimoFactorCreado);
            }
          }

          setFactoresCompetencia(listaCompleta);
          setIsLoadingFactores(false);

          // Calcular el primer número de orden libre >= 1 para la competencia seleccionada
          const ordenesOcupados = new Set(listaCompleta.map((f) => f.nroOrden));
          let primerLibre = 1;
          while (ordenesOcupados.has(primerLibre)) {
            primerLibre++;
          }

          // SIEMPRE asignar y actualizar el primer orden libre disponible al cambiar de competencia
          setValue('nroOrden', primerLibre, { shouldValidate: true, shouldDirty: false });
        }
      })
      .catch(() => {
        if (isMounted) {
          setIsLoadingFactores(false);
          setValue('nroOrden', 1, { shouldValidate: true, shouldDirty: false });
        }
      });

    return () => {
      isMounted = false;
    };
  }, [watchedCompetenciaId, ultimoFactorCreado, setValue]);

  // Manejo de validación fallida local: no se usa cartel general, solo se resaltan los campos en rojo con su texto debajo
  const onInvalid = useCallback(() => {
    // Vacío intencionalmente: los errores quedan en `formState.errors` y se muestran bajo cada input
  }, []);

  // Envío del formulario (POST /api/factores)
  const onSubmit = async (data: AltaFactorFormData) => {
    try {
      const response = await altaFactorService.crearFactor({
        codigo: data.codigo.trim().toUpperCase(),
        nombre: data.nombre.trim().toUpperCase(),
        descripcion: data.descripcion.trim().toUpperCase(),
        competenciaId: Number(data.competenciaId),
        nroOrden: Number(data.nroOrden),
      });

      // Creamos el objeto del factor recién persistido
      const factorCreado: FactorItem = {
        id: response.id,
        codigo: response.codigo,
        nombre: response.nombre,
        descripcion: response.descripcion,
        competenciaId: response.competenciaId,
        competenciaNombre: response.competenciaNombre,
        nroOrden: response.nroOrden,
      };

      // Guardamos la referencia para que handleCargarOtro calcule de forma exacta el nuevo número libre
      setUltimoFactorCreado(factorCreado);

      // Actualizamos inmediatamente el listado local de factores ocupados
      setFactoresCompetencia((prev) => {
        const sinDuplicados = prev.filter(
          (f) => f.nroOrden !== factorCreado.nroOrden && f.id !== factorCreado.id
        );
        return [...sinDuplicados, factorCreado];
      });

      // Éxito: Guardar nombre para el modal y abrir confirmación
      setFactorCreadoNombre(response.nombre || data.nombre.toUpperCase());
      setIsSuccessModalOpen(true);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errorData = err.data as BackendApiErrorData | undefined;

        // 409 Conflict: Código duplicado, Nombre duplicado o NroOrden duplicado
        // Se asocia directamente al campo respectivo sin cartel superior
        if (err.status === 409) {
          const mensajeConflicto = errorData?.mensaje || err.message;
          const msgLower = mensajeConflicto.toLowerCase();

          if (msgLower.includes('código') || msgLower.includes('codigo')) {
            setError('codigo', { type: 'manual', message: mensajeConflicto });
          } else if (msgLower.includes('nombre')) {
            setError('nombre', { type: 'manual', message: mensajeConflicto });
          } else if (msgLower.includes('orden')) {
            setError('nroOrden', { type: 'manual', message: mensajeConflicto });
          } else {
            setError('codigo', { type: 'manual', message: mensajeConflicto });
          }
          return;
        }

        // 400 Bad Request: Errores de validación devueltos por el backend
        // Se asocian directamente a cada input sin mostrar ningún cartel general
        if (err.status === 400 && errorData?.detalles && errorData.detalles.length > 0) {
          errorData.detalles.forEach((d) => {
            const fieldKey = d.campo as keyof AltaFactorFormData;
            if (fieldKey) {
              setError(fieldKey, { type: 'manual', message: d.mensaje });
            }
          });
          return;
        }

        // 404 Not Found: Competencia no encontrada
        if (err.status === 404) {
          const mensajeNoEncontrado = errorData?.mensaje || 'La competencia seleccionada no fue encontrada';
          setError('competenciaId', { type: 'manual', message: mensajeNoEncontrado });
          return;
        }

        // Otros errores HTTP asignados al primer campo
        setError('codigo', {
          type: 'manual',
          message: err.message || 'Error al comunicarse con el servidor',
        });
      } else {
        setError('codigo', {
          type: 'manual',
          message: (err as Error).message || 'Ocurrió un error inesperado al registrar el factor',
        });
      }
    }
  };

  // Botón «Sí» del modal: Limpiar el formulario, actualizar el orden ocupado y asignar el nuevo número libre
  const handleCargarOtro = useCallback(() => {
    setIsSuccessModalOpen(false);

    const currentCompId = watchedCompetenciaId;

    if (currentCompId) {
      const compIdNum = Number(currentCompId);

      // Re-consultamos los factores desde el backend para garantizar coherencia
      altaFactorService
        .obtenerFactoresPorCompetencia(compIdNum)
        .then((factoresActuales) => {
          const listaActualizada = [...factoresActuales];
          // Aseguramos que el último factor creado esté presente
          if (ultimoFactorCreado && Number(ultimoFactorCreado.competenciaId) === compIdNum) {
            if (!listaActualizada.some((f) => f.nroOrden === ultimoFactorCreado.nroOrden)) {
              listaActualizada.push(ultimoFactorCreado);
            }
          }

          setFactoresCompetencia(listaActualizada);

          // Calculamos el próximo número de orden disponible
          const ordenesOcupados = new Set(listaActualizada.map((f) => f.nroOrden));
          let proximoLibre = 1;
          while (ordenesOcupados.has(proximoLibre)) {
            proximoLibre++;
          }

          reset({
            codigo: '',
            nombre: '',
            descripcion: '',
            competenciaId: currentCompId,
            nroOrden: proximoLibre,
          });

          setValue('nroOrden', proximoLibre, { shouldValidate: true, shouldDirty: false });
        })
        .catch(() => {
          // Fallback con el estado en memoria si la API falla
          const listaActualizada = [...factoresCompetencia];
          if (ultimoFactorCreado && Number(ultimoFactorCreado.competenciaId) === compIdNum) {
            if (!listaActualizada.some((f) => f.nroOrden === ultimoFactorCreado.nroOrden)) {
              listaActualizada.push(ultimoFactorCreado);
            }
          }

          setFactoresCompetencia(listaActualizada);

          const ordenesOcupados = new Set(listaActualizada.map((f) => f.nroOrden));
          let proximoLibre = 1;
          while (ordenesOcupados.has(proximoLibre)) {
            proximoLibre++;
          }

          reset({
            codigo: '',
            nombre: '',
            descripcion: '',
            competenciaId: currentCompId,
            nroOrden: proximoLibre,
          });

          setValue('nroOrden', proximoLibre, { shouldValidate: true, shouldDirty: false });
        });
    } else {
      reset({
        codigo: '',
        nombre: '',
        descripcion: '',
        competenciaId: '',
        nroOrden: '',
      });
    }

    clearErrors();
    setFactorCreadoNombre('');

    // Auto-enfocar el primer campo (Código)
    setTimeout(() => {
      codigoInputRef.current?.focus();
    }, 60);
  }, [watchedCompetenciaId, ultimoFactorCreado, factoresCompetencia, reset, setValue, clearErrors]);

  // Botón «No» del modal: Redirigir a la pantalla anterior / listado
  const handleFinalizar = useCallback(() => {
    setIsSuccessModalOpen(false);
    const redirectUrl = options.onSuccessRedirect || '/en-desarrollo?modulo=Gestión+de+Factores';
    router.push(redirectUrl);
  }, [options.onSuccessRedirect, router]);

  // Botón Cancelar del formulario
  const handleCancelar = useCallback(() => {
    const redirectUrl = options.onCancelRedirect || '/en-desarrollo?modulo=Gestión+de+Factores';
    router.push(redirectUrl);
  }, [options.onCancelRedirect, router]);

  return {
    form,
    register,
    handleSubmit: handleSubmit(onSubmit, onInvalid),
    errors,
    isSubmitting,
    competencias,
    isLoadingCompetencias,
    factoresCompetencia,
    isLoadingFactores,
    watchedCompetenciaId,
    isSuccessModalOpen,
    factorCreadoNombre,
    handleCargarOtro,
    handleFinalizar,
    handleCancelar,
    codigoInputRef,
  };
}
