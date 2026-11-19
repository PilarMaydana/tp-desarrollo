import { z } from 'zod';

/**
 * Límites de validación sincronizados con PreguntaModificacionDTO.java y OpcionRespuestaDTO.java del backend.
 */
export const PREGUNTA_LIMITS = {
  NOMBRE_MAX: 255,
  ENUNCIADO_MAX: 1000,
  DESCRIPCION_MAX: 500,
  OPCION_NOMBRE_MAX: 100,
  OPCION_DESCRIPCION_MAX: 500,
  MIN_OPCIONES: 2,
  SUMA_PONDERACIONES: 10,
} as const;

const DECIMAL_SCALE = 1_000_000;
const OBJETIVO_SUMA_SCALED = PREGUNTA_LIMITS.SUMA_PONDERACIONES * DECIMAL_SCALE;
const NUMERIC_REGEX = /^-?\d+(\.\d+)?$/;

export interface ParsedPonderacion {
  empty: boolean;
  valid: boolean;
  value: number | null;
  scaled: number | null;
}

/**
 * Parsea un valor textual o numérico de ponderación sin perder precisión decimal.
 * Acepta coma o punto como separador decimal.
 */
export function parsearPonderacion(raw: string | number | null | undefined): ParsedPonderacion {
  if (raw === null || raw === undefined) {
    return { empty: true, valid: false, value: null, scaled: null };
  }

  const trimmed = String(raw).trim().replace(',', '.');
  if (trimmed === '') {
    return { empty: true, valid: false, value: null, scaled: null };
  }

  if (!NUMERIC_REGEX.test(trimmed)) {
    return { empty: false, valid: false, value: null, scaled: null };
  }

  const num = Number(trimmed);
  if (!Number.isFinite(num)) {
    return { empty: false, valid: false, value: null, scaled: null };
  }

  const scaled = Math.round(num * DECIMAL_SCALE);
  return { empty: false, valid: true, value: num, scaled };
}

/**
 * Formatea un entero escalado por DECIMAL_SCALE a texto decimal limpio sin ceros sobrantes.
 */
export function formatearScaledDecimal(scaled: number): string {
  const sign = scaled < 0 ? '-' : '';
  const abs = Math.abs(scaled);
  const intPart = Math.floor(abs / DECIMAL_SCALE);
  const fracPart = String(abs % DECIMAL_SCALE)
    .padStart(6, '0')
    .replace(/0+$/, '');
  return fracPart ? `${sign}${intPart}.${fracPart}` : `${sign}${intPart}`;
}

/**
 * Formatea un número decimal para mostrarlo precargado en un input (ej. 10.0 -> "10", 3.5 -> "3.5").
 */
export function formatearNumeroPonderacion(value: number | null | undefined): string {
  if (value === null || value === undefined || !Number.isFinite(value)) {
    return '';
  }
  return formatearScaledDecimal(Math.round(value * DECIMAL_SCALE));
}

export interface SumaPonderacionesResultado {
  sumScaled: number;
  formatted: string;
  esDiezExacto: boolean;
  tieneIncompletosOInvalidos: boolean;
  tieneNegativos: boolean;
}

/**
 * Calcula la suma exacta de ponderaciones en aritmética entera escalada (equivalente a BigDecimal == 10).
 */
export function calcularSumaPonderaciones(
  valores: Array<string | number | null | undefined>
): SumaPonderacionesResultado {
  let sumScaled = 0;
  let tieneIncompletosOInvalidos = false;
  let tieneNegativos = false;

  for (const val of valores) {
    const parsed = parsearPonderacion(val);
    if (!parsed.valid || parsed.scaled === null) {
      tieneIncompletosOInvalidos = true;
      continue;
    }
    if (parsed.scaled < 0) {
      tieneNegativos = true;
    }
    sumScaled += parsed.scaled;
  }

  return {
    sumScaled,
    formatted: formatearScaledDecimal(sumScaled),
    esDiezExacto: !tieneIncompletosOInvalidos && !tieneNegativos && sumScaled === OBJETIVO_SUMA_SCALED,
    tieneIncompletosOInvalidos,
    tieneNegativos,
  };
}

export const OpcionPreguntaModificacionFormSchema = z.object({
  id: z.number().optional(),
  orden: z.number().int().min(1, 'El orden debe ser mayor o igual a 1'),
  nombre: z.string(),
  descripcion: z.string(),
  ponderacion: z.number(),
  ponderacionInput: z.string(),
});

/**
 * Esquema Zod para CU 022 "Modificar Pregunta".
 * Usa superRefine para recopilar TODAS las omisiones, excesos de longitud y errores de ponderación juntos
 * (Flujos Alternativos 2.A y 2.B).
 */
export const ModificarPreguntaSchema = z
  .object({
    nombre: z.string(),
    enunciado: z.string(),
    descripcion: z.string(),
    opciones: z.array(OpcionPreguntaModificacionFormSchema),
  })
  .superRefine((data, ctx) => {
    // 1. Nombre de Pregunta (obligatorio, máx. 255)
    const nombreTrim = (data.nombre ?? '').trim();
    if (nombreTrim.length === 0) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['nombre'],
        message: 'El nombre de la pregunta es obligatorio',
      });
    } else if (nombreTrim.length > PREGUNTA_LIMITS.NOMBRE_MAX) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['nombre'],
        message: `El nombre de la pregunta no puede superar los ${PREGUNTA_LIMITS.NOMBRE_MAX} caracteres`,
      });
    }

    // 2. Pregunta / Enunciado (obligatorio, máx. 1000)
    const enunciadoTrim = (data.enunciado ?? '').trim();
    if (enunciadoTrim.length === 0) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['enunciado'],
        message: 'El enunciado de la pregunta es obligatorio',
      });
    } else if (enunciadoTrim.length > PREGUNTA_LIMITS.ENUNCIADO_MAX) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['enunciado'],
        message: `El enunciado de la pregunta no puede superar los ${PREGUNTA_LIMITS.ENUNCIADO_MAX} caracteres`,
      });
    }

    // 3. Descripción (opcional, máx. 500)
    const descripcionTrim = (data.descripcion ?? '').trim();
    if (descripcionTrim.length > PREGUNTA_LIMITS.DESCRIPCION_MAX) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['descripcion'],
        message: `La descripción no puede superar los ${PREGUNTA_LIMITS.DESCRIPCION_MAX} caracteres`,
      });
    }

    // 4. Opciones de respuesta (mínimo 2)
    const opciones = data.opciones ?? [];
    if (opciones.length < PREGUNTA_LIMITS.MIN_OPCIONES) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['opciones'],
        message: `Debe ingresar al menos ${PREGUNTA_LIMITS.MIN_OPCIONES} opciones de respuesta`,
      });
      return;
    }

    const rawValues: string[] = [];

    opciones.forEach((opcion, idx) => {
      const ordenLabel = opcion.orden ?? idx + 1;
      const optNombreTrim = (opcion.nombre ?? '').trim();
      const optDescTrim = (opcion.descripcion ?? '').trim();

      if (optNombreTrim.length === 0) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['opciones', idx, 'nombre'],
          message: `Opción ${ordenLabel}: El nombre de la opción es obligatorio`,
        });
      } else if (optNombreTrim.length > PREGUNTA_LIMITS.OPCION_NOMBRE_MAX) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['opciones', idx, 'nombre'],
          message: `Opción ${ordenLabel}: El nombre no puede superar los ${PREGUNTA_LIMITS.OPCION_NOMBRE_MAX} caracteres`,
        });
      }

      if (optDescTrim.length > PREGUNTA_LIMITS.OPCION_DESCRIPCION_MAX) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['opciones', idx, 'descripcion'],
          message: `Opción ${ordenLabel}: La descripción no puede superar los ${PREGUNTA_LIMITS.OPCION_DESCRIPCION_MAX} caracteres`,
        });
      }

      const raw = opcion.ponderacionInput ?? '';
      rawValues.push(raw);
      const parsed = parsearPonderacion(raw);

      if (parsed.empty) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['opciones', idx, 'ponderacionInput'],
          message: `Opción ${ordenLabel}: La ponderación es obligatoria`,
        });
      } else if (!parsed.valid || parsed.value === null) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['opciones', idx, 'ponderacionInput'],
          message: `Opción ${ordenLabel}: La ponderación debe ser un número válido`,
        });
      } else if (parsed.value < 0) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['opciones', idx, 'ponderacionInput'],
          message: `Opción ${ordenLabel}: La ponderación debe ser mayor o igual a 0`,
        });
      }
    });

    // 5. Suma exacta de ponderaciones == 10 (aritmética decimal segura)
    const resumenSuma = calcularSumaPonderaciones(rawValues);
    if (resumenSuma.sumScaled !== OBJETIVO_SUMA_SCALED) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['opciones'],
        message: `La suma de las ponderaciones de las opciones de respuesta debe ser exactamente ${PREGUNTA_LIMITS.SUMA_PONDERACIONES} puntos (suma actual: ${resumenSuma.formatted})`,
      });
    }
  });

export type ModificarPreguntaFormData = z.infer<typeof ModificarPreguntaSchema>;
