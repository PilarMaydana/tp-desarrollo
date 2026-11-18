'use client';

import { useCallback, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  OpcionRespuestaApiError,
  OpcionRespuestaFormData,
  OpcionRespuestaSchema,
} from '../types';
import {
  ApiError,
  opcionRespuestaService,
} from '../services/opcionRespuestaService';

export interface UseAltaOpcionRespuestaOptions {
  onSuccessNavigate?: () => void;
  onCancelNavigate?: () => void;
}

export interface OpcionRowMeta {
  id: string;
  defaultOrden: number | string;
  defaultDescripcion: string;
}

const DEFAULT_ITEMS: OpcionRespuestaFormData['items'] = [
  { orden: 1, descripcion: '' },
  { orden: 2, descripcion: '' },
];

const INITIAL_ROWS: OpcionRowMeta[] = [
  { id: 'row-1', defaultOrden: 1, defaultDescripcion: '' },
  { id: 'row-2', defaultOrden: 2, defaultDescripcion: '' },
];

export function useAltaOpcionRespuesta(
  options: UseAltaOpcionRespuestaOptions = {}
) {
  const { onSuccessNavigate, onCancelNavigate } = options;
  const router = useRouter();

  const [createdOpcionNombre, setCreatedOpcionNombre] = useState<string | null>(
    null
  );
  const [serverError, setServerError] = useState<string | null>(null);
  const [rows, setRows] = useState<OpcionRowMeta[]>(INITIAL_ROWS);
  const nextRowIdRef = useRef<number>(3);

  const form = useForm<OpcionRespuestaFormData>({
    resolver: zodResolver(OpcionRespuestaSchema),
    defaultValues: {
      nombre: '',
      descripcion: '',
      items: DEFAULT_ITEMS.map((item) => ({ ...item })),
    },
  });

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    setFocus,
    setError,
    clearErrors,
    getValues,
    formState: { errors, isSubmitting },
  } = form;

  const handleAddItem = useCallback(() => {
    const currentItems = getValues('items') || [];
    const nextOrden = currentItems.length + 1;
    const newRowId = `row-${nextRowIdRef.current++}`;

    setRows((prev) => [
      ...prev,
      { id: newRowId, defaultOrden: nextOrden, defaultDescripcion: '' },
    ]);
    setValue(
      'items',
      [...currentItems, { orden: nextOrden, descripcion: '' }],
      { shouldDirty: true }
    );
  }, [getValues, setValue]);

  const handleRemoveItem = useCallback(
    (index: number) => {
      const currentItems = getValues('items') || [];
      if (currentItems.length <= 2) {
        return;
      }

      const nextItems = currentItems.filter((_, idx) => idx !== index);
      const nextRows: OpcionRowMeta[] = nextItems.map((item) => ({
        id: `row-${nextRowIdRef.current++}`,
        defaultOrden: item.orden,
        defaultDescripcion: item.descripcion,
      }));

      setRows(nextRows);
      setValue('items', nextItems, { shouldDirty: true });
      clearErrors('items');
    },
    [getValues, setValue, clearErrors]
  );

  // Lista plana con TODAS las omisiones/errores de validación para el cuadro superior
  const errorMessages = useMemo(() => {
    const messages: string[] = [];

    if (errors.nombre?.message) {
      messages.push(errors.nombre.message);
    }

    if (errors.descripcion?.message) {
      messages.push(errors.descripcion.message);
    }

    const itemsRootMessage =
      errors.items?.message || errors.items?.root?.message;
    if (itemsRootMessage) {
      messages.push(itemsRootMessage);
    }

    if (Array.isArray(errors.items)) {
      errors.items.forEach((itemError, idx) => {
        if (itemError?.orden?.message) {
          messages.push(`Opción ${idx + 1} (Orden): ${itemError.orden.message}`);
        }
        if (itemError?.descripcion?.message) {
          messages.push(
            `Opción ${idx + 1} (Descripción): ${itemError.descripcion.message}`
          );
        }
      });
    }

    return messages;
  }, [errors]);

  const onSubmit = async (data: OpcionRespuestaFormData) => {
    setServerError(null);
    try {
      const response = await opcionRespuestaService.crearOpcionRespuesta({
        nombre: data.nombre.trim().toUpperCase(),
        descripcion: data.descripcion.trim().toUpperCase(),
        items: data.items.map((item) => ({
          orden: Number(item.orden),
          descripcion: item.descripcion.trim().toUpperCase(),
        })),
      });

      setCreatedOpcionNombre(
        response.nombre || data.nombre.trim().toUpperCase()
      );
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const errorData = err.data as OpcionRespuestaApiError | undefined;

        // 400 VALIDACION: mapear detalles[].campo (ej. nombre, descripcion, items, items[0].orden, items[0].descripcion)
        if (
          err.status === 400 &&
          errorData?.detalles &&
          errorData.detalles.length > 0
        ) {
          errorData.detalles.forEach((detalle) => {
            const campo = detalle.campo;
            if (campo === 'nombre' || campo === 'descripcion') {
              setError(campo, { type: 'server', message: detalle.mensaje });
              return;
            }
            if (campo === 'items') {
              setError('items', { type: 'server', message: detalle.mensaje });
              return;
            }
            const itemMatch = campo.match(
              /^items\[(\d+)\]\.(orden|descripcion)$/
            );
            if (itemMatch) {
              const idx = Number(itemMatch[1]);
              const subField = itemMatch[2] as 'orden' | 'descripcion';
              setError(`items.${idx}.${subField}`, {
                type: 'server',
                message: detalle.mensaje,
              });
            }
          });
          return;
        }

        // 409 RECURSO_DUPLICADO, 422 REGLA_NEGOCIO u otros errores del backend
        setServerError(
          errorData?.mensaje ||
            err.message ||
            'Error al comunicarse con el servidor'
        );
      } else if (err instanceof Error) {
        setServerError(err.message || 'Error al comunicarse con el servidor');
      } else {
        setServerError('Error al comunicarse con el servidor');
      }
    }
  };

  const handleLoadAnother = useCallback(() => {
    setServerError(null);
    setCreatedOpcionNombre(null);
    const resetRows: OpcionRowMeta[] = [
      {
        id: `row-${nextRowIdRef.current++}`,
        defaultOrden: 1,
        defaultDescripcion: '',
      },
      {
        id: `row-${nextRowIdRef.current++}`,
        defaultOrden: 2,
        defaultDescripcion: '',
      },
    ];
    setRows(resetRows);
    reset({
      nombre: '',
      descripcion: '',
      items: DEFAULT_ITEMS.map((item) => ({ ...item })),
    });
    clearErrors();
    setTimeout(() => {
      setFocus('nombre');
    }, 0);
  }, [reset, clearErrors, setFocus]);

  const handleFinish = useCallback(() => {
    setServerError(null);
    setCreatedOpcionNombre(null);
    if (onSuccessNavigate) {
      onSuccessNavigate();
      return;
    }
    router.push('/opciones-respuesta?nombre=');
  }, [onSuccessNavigate, router]);

  const handleCancel = useCallback(() => {
    if (onCancelNavigate) {
      onCancelNavigate();
      return;
    }
    router.back();
  }, [onCancelNavigate, router]);

  return {
    register,
    rows,
    errors,
    errorMessages,
    isSubmitting,
    serverError,
    createdOpcionNombre,
    handleAddItem,
    handleRemoveItem,
    handleSubmit: handleSubmit(onSubmit),
    handleLoadAnother,
    handleFinish,
    handleCancel,
  };
}
