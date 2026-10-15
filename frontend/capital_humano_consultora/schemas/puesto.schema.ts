import { z } from 'zod';

export const CompetenciaPonderadaSchema = z.object({
  competenciaId: z.string().min(1, 'La competencia es obligatoria'),
  ponderacion: z
    .number()
    .min(0, 'La ponderación mínima es 0')
    .max(10, 'La ponderación máxima es 10'),
});

export const PuestoSchema = z.object({
  empresaId: z.string().min(1, 'Debe seleccionar una empresa'),
  nombrePuesto: z.string().min(1, 'El nombre del puesto es obligatorio'),
  codigo: z.string().optional(),
  descripcion: z.string().optional(),
  competencias: z
    .array(CompetenciaPonderadaSchema)
    .min(1, 'Debe seleccionar al menos una competencia'),
});

export type PuestoFormData = z.infer<typeof PuestoSchema>;
