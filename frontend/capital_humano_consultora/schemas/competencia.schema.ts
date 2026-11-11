import { z } from 'zod';

export const CompetenciaSchema = z.object({
  codigo: z
    .string()
    .trim()
    .min(1, 'El código es obligatorio')
    .max(20, 'El código no puede superar los 20 caracteres'),
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
});

export type CompetenciaFormData = z.infer<typeof CompetenciaSchema>;

