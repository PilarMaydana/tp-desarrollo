import type React from 'react';
import {
  ApiError,
  type BackendApiErrorData,
  type OrdenMeritoErrorState,
} from '../types';

export const PLACEHOLDER_VACIO = '—';

const LAST_SEARCH_STORAGE_KEY = 'orden_merito_last_search_params';

/**
 * Formatea una fecha ISO-8601 sin zona horaria (LocalDateTime del backend, ej. "2026-11-23T19:15:00")
 * al formato dd/MM/aaaa HH:mm SIN pasar por `new Date()` para evitar desplazamientos de huso horario.
 */
export function formatFechaLocal(isoDateTime: string | null | undefined): string {
  if (!isoDateTime) {
    return PLACEHOLDER_VACIO;
  }

  const trimmed = isoDateTime.trim();
  const match = trimmed.match(/^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2}))?/);

  if (!match) {
    return trimmed;
  }

  const [, year, month, day, hours = '00', minutes = '00'] = match;
  return `${day}/${month}/${year} ${hours}:${minutes}`;
}

/**
 * Formatea un puntaje numérico con 2 decimales, o devuelve "—" si es null/undefined.
 */
export function formatPuntaje(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return PLACEHOLDER_VACIO;
  }
  return Number(value).toFixed(2);
}

/**
 * Normaliza a MAYÚSCULAS el valor de un input preservando la posición del cursor,
 * independientemente del estado de Bloq Mayús.
 */
export function toUpperPreservingCaret(e: React.ChangeEvent<HTMLInputElement>): string {
  const { selectionStart, selectionEnd, value } = e.target;
  const upper = value.toUpperCase();
  if (upper !== value) {
    e.target.value = upper;
    if (selectionStart !== null && selectionEnd !== null) {
      e.target.setSelectionRange(selectionStart, selectionEnd);
    }
  }
  return upper;
}

/**
 * Extrae información estructurada de error real (status, codigo, mensaje, detalles)
 * a partir de ApiError o errores de red/runtime.
 */
export function extractOrdenMeritoError(
  err: unknown,
  fallbackMessage = 'Ocurrió un error al comunicarse con el servidor.'
): OrdenMeritoErrorState {
  if (err instanceof ApiError) {
    const data = (err.data ?? null) as Partial<BackendApiErrorData> | null;
    const mensaje = data?.mensaje || err.message || fallbackMessage;
    const detalles = Array.isArray(data?.detalles)
      ? data.detalles.map((d) => ({
          campo: d.campo,
          mensaje: d.mensaje,
        }))
      : [];

    return {
      status: data?.status ?? err.status,
      codigo: data?.codigo,
      mensaje,
      detalles,
    };
  }

  if (err instanceof Error) {
    return {
      mensaje: err.message || fallbackMessage,
      detalles: [],
    };
  }

  return {
    mensaje: fallbackMessage,
    detalles: [],
  };
}

/**
 * Parsea el parámetro de query `evaluacionIds` (ej. "3,4") en un array de números válidos.
 * Si viene vacío o ausente, devuelve undefined (equivale a TODAS).
 */
export function parseEvaluacionIdsParam(raw: string | null | undefined): number[] | undefined {
  if (!raw || raw.trim() === '') {
    return undefined;
  }
  const parsed = raw
    .split(',')
    .map((part) => Number(part.trim()))
    .filter((id) => Number.isInteger(id) && id > 0);

  return parsed.length > 0 ? parsed : undefined;
}

/**
 * Guarda la query string de búsqueda de puestos en sessionStorage para restaurarla al pulsar «Volver».
 */
export function saveLastOrdenMeritoSearch(queryString: string): void {
  if (typeof window === 'undefined') return;
  try {
    if (queryString.trim()) {
      sessionStorage.setItem(LAST_SEARCH_STORAGE_KEY, queryString);
    } else {
      sessionStorage.removeItem(LAST_SEARCH_STORAGE_KEY);
    }
  } catch {
    // Ignorar errores de almacenamiento privado
  }
}

/**
 * Recupera la última query string de búsqueda de puestos almacenada en sessionStorage.
 */
export function getLastOrdenMeritoSearch(): string | null {
  if (typeof window === 'undefined') return null;
  try {
    return sessionStorage.getItem(LAST_SEARCH_STORAGE_KEY);
  } catch {
    return null;
  }
}
