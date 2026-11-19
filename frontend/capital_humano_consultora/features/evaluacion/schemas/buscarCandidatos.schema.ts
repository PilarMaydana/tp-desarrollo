import { z } from 'zod';

export const BuscarCandidatosSchema = z.object({
  apellido: z.string().trim(),
  nombre: z.string().trim(),
  nroCandidato: z.string().trim(),
});

export type BuscarCandidatosFormData = z.infer<typeof BuscarCandidatosSchema>;
