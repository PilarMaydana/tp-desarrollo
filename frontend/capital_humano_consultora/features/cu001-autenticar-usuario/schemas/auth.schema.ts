import { z } from 'zod';

export const ConsultorLoginSchema = z.object({
  usuario: z
    .string()
    .min(1, 'El nombre de usuario es obligatorio')
    .max(15, 'El identificador de usuario no puede superar los 15 caracteres'),
  contrasena: z
    .string()
    .min(1, 'La contraseña es obligatoria')
    .max(20, 'La contraseña no puede superar los 20 caracteres'),
});

export const CandidatoLoginSchema = z.object({
  tipoDocumento: z.enum(['DNI', 'LE', 'LC', 'PP'] as const, {
    message: 'Seleccione un tipo de documento válido',
  }),
  numeroDocumento: z
    .string()
    .min(1, 'El número de documento es obligatorio')
    .regex(/^[0-9a-zA-Z]+$/, 'El número de documento debe ingresarse sin puntos ni espacios'),
  clave: z
    .string()
    .min(1, 'La contraseña es obligatoria')
    .max(8, 'La clave no puede superar los 8 caracteres'),
});

export type ConsultorLoginFormValues = z.infer<typeof ConsultorLoginSchema>;
export type CandidatoLoginFormValues = z.infer<typeof CandidatoLoginSchema>;
