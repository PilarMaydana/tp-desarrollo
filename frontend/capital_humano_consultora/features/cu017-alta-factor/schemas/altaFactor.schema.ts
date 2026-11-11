import { z } from 'zod';

export const AltaFactorSchema = z.object({
  codigo: z
    .string()
    .trim()
    .min(1, 'El código es obligatorio')
    .max(50, 'El código no puede superar los 50 caracteres'),
  nombre: z
    .string()
    .trim()
    .min(1, 'El nombre es obligatorio')
    .max(100, 'El nombre no puede superar los 100 caracteres'),
  descripcion: z
    .string()
    .trim()
    .min(1, 'La descripción es obligatoria')
    .max(500, 'La descripción no puede superar los 500 caracteres'),
  competenciaId: z
    .union([z.string(), z.number()])
    .refine(
      (val) => {
        if (typeof val === 'number') return val > 0;
        return typeof val === 'string' && val.trim().length > 0 && Number(val) > 0;
      },
      { message: 'Debe seleccionar una competencia' }
    ),
  nroOrden: z
    .union([z.string(), z.number()])
    .refine(
      (val) => {
        if (val === '' || val === null || val === undefined) return false;
        const num = Number(val);
        return !isNaN(num) && Number.isInteger(num) && num >= 1;
      },
      { message: 'El número de orden debe ser mayor a 0' }
    ),
});

export type AltaFactorFormData = z.infer<typeof AltaFactorSchema>;

