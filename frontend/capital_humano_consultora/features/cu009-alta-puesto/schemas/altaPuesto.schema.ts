import { z } from 'zod';

export const CaracteristicaPuestoSchema = z.object({
  competenciaId: z.union([z.string(), z.number()]).refine(
    (val) => String(val).trim().length > 0,
    { message: 'La competencia es obligatoria' }
  ),
  nombre: z.string().optional(),
  ponderacion: z
    .number()
    .min(0, 'La ponderación mínima es 0')
    .max(10, 'La ponderación máxima es 10'),
});

export const AltaPuestoSchema = z.object({
  codigo: z
    .string()
    .trim()
    .min(1, 'El código es obligatorio')
    .max(50, 'El código no puede superar los 50 caracteres'),
  nombre: z
    .string()
    .trim()
    .min(1, 'El nombre del puesto es obligatorio')
    .max(100, 'El nombre del puesto no puede superar los 100 caracteres'),
  descripcion: z
    .string()
    .trim()
    .min(1, 'La descripción es obligatoria')
    .max(500, 'La descripción no puede superar los 500 caracteres'),
  empresa: z
    .string()
    .trim()
    .min(1, 'La empresa es obligatoria')
    .max(100, 'La empresa no puede superar los 100 caracteres'),
  caracteristicas: z
    .array(CaracteristicaPuestoSchema)
    .min(1, 'Debe seleccionar al menos una competencia')
    .refine(
      (items) => {
        const ids = items.map((i) => String(i.competenciaId));
        return new Set(ids).size === ids.length;
      },
      { message: 'No se pueden repetir competencias en el mismo puesto' }
    ),
});

export type AltaPuestoFormData = z.infer<typeof AltaPuestoSchema>;
