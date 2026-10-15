import { z } from 'zod';

export const LoginConsultorSchema = z.object({
  username: z.string().min(1, 'El nombre de usuario es obligatorio'),
  clave: z.string().min(1, 'La contraseña es obligatoria'),
});

export const LoginCandidatoSchema = z.object({
  tipoDocumento: z.string().min(1, 'Seleccione un tipo de documento'),
  nroDocumento: z.string().min(1, 'El número de documento es obligatorio'),
  clave: z.string().min(1, 'La contraseña es obligatoria'),
});

export type LoginConsultorFormData = z.infer<typeof LoginConsultorSchema>;
export type LoginCandidatoFormData = z.infer<typeof LoginCandidatoSchema>;
