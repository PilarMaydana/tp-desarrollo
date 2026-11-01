'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, FieldErrors } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ROUTES } from '@/constants/routes';
import { ApiError } from '@/services/apiClient';
import {
  AltaPuestoFormData,
  AltaPuestoSchema,
} from '../schemas/altaPuesto.schema';
import { altaPuestoService } from '../services/altaPuestoService';
import {
  BackendApiErrorData,
  CompetenciaItem,
  ErrorItem,
} from '../types';

export interface UseAltaPuestoOptions {
  onSuccessRedirect?: string;
  onCancelRedirect?: string;
}

export function useAltaPuesto(options: UseAltaPuestoOptions = {}) {
  const router = useRouter();
  const codigoInputRef = useRef<HTMLInputElement | null>(null);

  const [competenciasDisponibles, setCompetenciasDisponibles] = useState<CompetenciaItem[]>([]);
  const [isLoadingCompetencias, setIsLoadingCompetencias] = useState(true);

  // Estados de errores (Flujo 2.A y 2.B)
  const [mensajeErrorGeneral, setMensajeErrorGeneral] = useState<string | null>(null);
  const [listaErrores, setListaErrores] = useState<ErrorItem[]>([]);

  // Estados del modal de confirmación de éxito (Paso 3 y 4)
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [puestoCreadoNombre, setPuestoCreadoNombre] = useState('');

  const form = useForm<AltaPuestoFormData>({
    resolver: zodResolver(AltaPuestoSchema),
    defaultValues: {
      codigo: '',
      nombre: '',
      descripcion: '',
      empresa: '',
      caracteristicas: [],
    },
    mode: 'onSubmit',
  });

  const {
    register,
    handleSubmit,
    control,
    setValue,
    reset,
    setError,
    clearErrors,
    formState: { errors, isSubmitting },
  } = form;

  // Carga inicial de competencias desde la API
  useEffect(() => {
    let isMounted = true;
    altaPuestoService
      .obtenerCompetencias()
      .then((data) => {
        if (isMounted) {
          setCompetenciasDisponibles(data);
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

  // Helper para manejar inputs siempre en mayúsculas (Requerimiento Especial)
  const handleUppercaseChange = useCallback(
    (
      fieldName: 'codigo' | 'nombre' | 'descripcion' | 'empresa',
      e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
      const upper = e.target.value.toUpperCase();
      setValue(fieldName, upper, { shouldValidate: false, shouldDirty: true });
    },
    [setValue]
  );

  // Manejo de Flujo 2.A: Datos obligatorios incompletos
  const onInvalid = useCallback((fieldErrors: FieldErrors<AltaPuestoFormData>) => {
    const listado: ErrorItem[] = [];

    if (fieldErrors.codigo?.message) {
      listado.push({ campo: 'Código', mensaje: String(fieldErrors.codigo.message) });
    }
    if (fieldErrors.nombre?.message) {
      listado.push({ campo: 'Nombre del puesto', mensaje: String(fieldErrors.nombre.message) });
    }
    if (fieldErrors.descripcion?.message) {
      listado.push({ campo: 'Descripción', mensaje: String(fieldErrors.descripcion.message) });
    }
    if (fieldErrors.empresa?.message) {
      listado.push({ campo: 'Empresa', mensaje: String(fieldErrors.empresa.message) });
    }
    if (fieldErrors.caracteristicas?.message) {
      listado.push({ campo: 'Características', mensaje: String(fieldErrors.caracteristicas.message) });
    }

    setMensajeErrorGeneral('Datos obligatorios incompletos. Por favor revise los campos indicados.');
    setListaErrores(listado);
  }, []);

  // Envío del formulario (Paso 2 del flujo principal)
  const onSubmit = async (data: AltaPuestoFormData) => {
    setMensajeErrorGeneral(null);
    setListaErrores([]);

    try {
      // Normalizamos a mayúsculas y trim antes de enviar al backend
      const response = await altaPuestoService.crearPuesto({
        codigo: data.codigo.trim().toUpperCase(),
        nombre: data.nombre.trim().toUpperCase(),
        descripcion: data.descripcion.trim().toUpperCase(),
        empresa: data.empresa.trim().toUpperCase(),
        caracteristicas: data.caracteristicas.map((c) => ({
          competenciaId: Number(c.competenciaId),
          ponderacion: Number(c.ponderacion),
        })),
      });

      // Éxito: Guardar nombre para el modal y abrir confirmación
      setPuestoCreadoNombre(response.nombre || data.nombre);
      setIsSuccessModalOpen(true);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errorData = err.data as BackendApiErrorData | undefined;

        // Flujo 2.B: Código o nombre existente (HTTP 409 Conflict)
        if (err.status === 409) {
          const mensajeConflicto = errorData?.mensaje || err.message;
          const msgLower = mensajeConflicto.toLowerCase();

          if (msgLower.includes('código') || msgLower.includes('codigo')) {
            setError('codigo', { type: 'manual', message: mensajeConflicto });
            setListaErrores([{ campo: 'Código', mensaje: mensajeConflicto }]);
          } else if (msgLower.includes('nombre')) {
            setError('nombre', { type: 'manual', message: mensajeConflicto });
            setListaErrores([{ campo: 'Nombre del puesto', mensaje: mensajeConflicto }]);
          } else {
            setListaErrores([{ campo: 'Duplicado', mensaje: mensajeConflicto }]);
          }

          setMensajeErrorGeneral(mensajeConflicto);
          return;
        }

        // Flujo 2.A: Validación de campos desde el backend (HTTP 400 Bad Request)
        if (err.status === 400 && errorData?.detalles && errorData.detalles.length > 0) {
          const listado: ErrorItem[] = errorData.detalles.map((d) => {
            const fieldKey = d.campo as keyof AltaPuestoFormData;
            if (fieldKey) {
              setError(fieldKey, { type: 'manual', message: d.mensaje });
            }
            return { campo: d.campo, mensaje: d.mensaje };
          });
          setMensajeErrorGeneral(errorData.mensaje || 'Error de validación en los datos enviados');
          setListaErrores(listado);
          return;
        }

        // Reglas de negocio (HTTP 422 Unprocessable Entity)
        if (err.status === 422) {
          const mensajeRegla = errorData?.mensaje || err.message;
          setMensajeErrorGeneral(mensajeRegla);
          setListaErrores([{ campo: 'Regla de negocio', mensaje: mensajeRegla }]);
          return;
        }

        // Otro error HTTP
        setMensajeErrorGeneral(err.message || 'Error al comunicarse con el servidor');
      } else {
        setMensajeErrorGeneral((err as Error).message || 'Ocurrió un error inesperado');
      }
    }
  };

  // Flujo 5.A: Cargar otro puesto (Opción "Sí")
  const handleCargarOtro = useCallback(() => {
    setIsSuccessModalOpen(false);
    reset({
      codigo: '',
      nombre: '',
      descripcion: '',
      empresa: '',
      caracteristicas: [],
    });
    clearErrors();
    setMensajeErrorGeneral(null);
    setListaErrores([]);
    setPuestoCreadoNombre('');

    // Dar foco al primer campo (Código)
    setTimeout(() => {
      codigoInputRef.current?.focus();
    }, 50);
  }, [reset, clearErrors]);

  // Opción "No" en el modal: Finaliza el caso de uso
  const handleFinalizar = useCallback(() => {
    setIsSuccessModalOpen(false);
    const redirectUrl = options.onSuccessRedirect || ROUTES.CONSULTOR.PUESTOS;
    router.push(redirectUrl);
  }, [options.onSuccessRedirect, router]);

  // Botón Cancelar del formulario
  const handleCancelar = useCallback(() => {
    const redirectUrl = options.onCancelRedirect || ROUTES.CONSULTOR.PUESTOS;
    router.push(redirectUrl);
  }, [options.onCancelRedirect, router]);

  const dismissErrores = useCallback(() => {
    setMensajeErrorGeneral(null);
    setListaErrores([]);
  }, []);

  return {
    form,
    register,
    handleSubmit: handleSubmit(onSubmit, onInvalid),
    control,
    errors,
    isSubmitting,
    competenciasDisponibles,
    isLoadingCompetencias,
    mensajeErrorGeneral,
    listaErrores,
    dismissErrores,
    isSuccessModalOpen,
    puestoCreadoNombre,
    handleCargarOtro,
    handleFinalizar,
    handleCancelar,
    handleUppercaseChange,
    codigoInputRef,
  };
}
