import { z } from 'zod';

export const OpcionRespuestaFormSchema = z.object({
  id: z.number().optional(),
  orden: z
    .number()
    .int('El orden debe ser un número entero')
    .min(1, 'El orden debe ser mayor o igual a 1'),
  nombre: z
    .string()
    .trim()
    .min(1, 'El nombre de la opción es obligatorio')
    .max(100, 'El nombre de la opción no puede superar los 100 caracteres'),
  descripcion: z
    .string()
    .trim()
    .min(1, 'La descripción de la opción es obligatoria')
    .max(500, 'La descripción de la opción no puede superar los 500 caracteres'),
  ponderacion: z
    .number()
    .min(0, 'La ponderación debe ser mayor o igual a cero')
    .max(10, 'La ponderación máxima es 10'),
});

export const AltaPreguntaSchema = z.object({
  codigoCompetencia: z
    .string()
    .trim()
    .min(1, 'Debe seleccionar una competencia'),
  codigoFactor: z
    .string()
    .trim()
    .min(1, 'Debe seleccionar un factor'),
  nombre: z
    .string()
    .trim()
    .min(1, 'El nombre es obligatorio')
    .max(100, 'El nombre no puede superar los 100 caracteres'),
  enunciado: z
    .string()
    .trim()
    .min(1, 'La pregunta es obligatoria')
    .max(500, 'La pregunta no puede superar los 500 caracteres'),
  descripcion: z
    .string()
    .trim()
    .max(500, 'La descripción no puede superar los 500 caracteres')
    .optional()
    .or(z.literal('')),
  plantillaOpcionId: z
    .string()
    .min(1, 'Debe seleccionar una escala de opción de respuesta'),
  opcionesRespuesta: z
    .array(OpcionRespuestaFormSchema)
    .min(2, 'Debe ingresar al menos 2 opciones de respuesta')
    .superRefine((opciones, ctx) => {
      // Validar valores negativos individuales
      const tieneNegativos = opciones.some((o) => typeof o.ponderacion === 'number' && o.ponderacion < 0);
      if (tieneNegativos) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Todas las ponderaciones deben ser mayores o iguales a cero',
        });
        return;
      }

      // Validar que la suma dé exactamente 10
      const suma = opciones.reduce((acc, curr) => acc + (Number(curr.ponderacion) || 0), 0);
      if (Math.abs(suma - 10.0) > 0.001) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `La suma de las ponderaciones de las opciones de respuesta debe ser exactamente 10 puntos (suma actual: ${Number(suma.toFixed(2))})`,
        });
      }
    }),
});

export type AltaPreguntaFormData = z.infer<typeof AltaPreguntaSchema>;
