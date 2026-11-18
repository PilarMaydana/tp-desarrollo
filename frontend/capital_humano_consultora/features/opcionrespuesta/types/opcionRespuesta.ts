import { z } from 'zod';

export interface ItemOpcionRespuestaRequest {
  orden: number;
  descripcion: string;
}

export interface OpcionRespuestaCreateRequest {
  nombre: string;
  descripcion: string;
  items: ItemOpcionRespuestaRequest[];
}

export interface ItemOpcionRespuestaResponse {
  orden: number;
  descripcion: string;
}

export interface OpcionRespuestaResponse {
  id: number;
  nombre: string;
  descripcion: string;
  items: ItemOpcionRespuestaResponse[];
}

export interface OpcionRespuestaResumen {
  id: number;
  nombre: string;
  descripcion: string;
}

export interface CampoError {
  campo: string;
  mensaje: string;
}

export interface OpcionRespuestaApiError {
  timestamp?: string;
  status?: number;
  codigo?: 'VALIDACION' | 'RECURSO_DUPLICADO' | 'REGLA_NEGOCIO' | string;
  mensaje?: string;
  path?: string;
  detalles?: CampoError[];
}

export const ItemOpcionRespuestaFormSchema = z.object({
  orden: z.union([z.string(), z.number()]),
  descripcion: z.string(),
});

export const OpcionRespuestaSchema = z
  .object({
    nombre: z.string(),
    descripcion: z.string(),
    items: z.array(ItemOpcionRespuestaFormSchema),
  })
  .superRefine((data, ctx) => {
    // 1. Nombre: obligatorio, máx 100 caracteres
    const nombreTrim = (data.nombre ?? '').trim();
    if (nombreTrim.length === 0) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['nombre'],
        message: 'El nombre es obligatorio',
      });
    } else if (nombreTrim.length > 100) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['nombre'],
        message: 'El nombre no puede superar los 100 caracteres',
      });
    }

    // 2. Descripción: obligatoria, máx 500 caracteres
    const descripcionTrim = (data.descripcion ?? '').trim();
    if (descripcionTrim.length === 0) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['descripcion'],
        message: 'La descripción es obligatoria',
      });
    } else if (descripcionTrim.length > 500) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['descripcion'],
        message: 'La descripción no puede superar los 500 caracteres',
      });
    }

    // 3. Mínimo 2 ítems
    if (!Array.isArray(data.items) || data.items.length < 2) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['items'],
        message: 'La opción de respuesta debe tener al menos 2 ítems',
      });
    }

    if (!Array.isArray(data.items) || data.items.length === 0) {
      return;
    }

    // 4. Validación individual por cada ítem y recolección para validaciones cruzadas
    const ordenesPorValor = new Map<number, number[]>();
    const descripcionesPorValor = new Map<string, number[]>();
    const ordenesValidosPorIndice = new Map<number, number>();
    let todosLosOrdenesSonEnterosPositivos = true;

    data.items.forEach((item, index) => {
      const rawOrden = item?.orden;
      const ordenStr =
        rawOrden === null || rawOrden === undefined ? '' : String(rawOrden).trim();

      if (ordenStr === '') {
        todosLosOrdenesSonEnterosPositivos = false;
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['items', index, 'orden'],
          message: 'El orden de visualización es obligatorio',
        });
      } else {
        const numOrden = Number(ordenStr);
        if (Number.isNaN(numOrden) || !Number.isInteger(numOrden) || numOrden < 1) {
          todosLosOrdenesSonEnterosPositivos = false;
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['items', index, 'orden'],
            message: 'El orden debe ser un número entero mayor o igual a 1',
          });
        } else {
          ordenesValidosPorIndice.set(index, numOrden);
          const listaIndices = ordenesPorValor.get(numOrden) ?? [];
          listaIndices.push(index);
          ordenesPorValor.set(numOrden, listaIndices);
        }
      }

      const descItemTrim = (item?.descripcion ?? '').trim();
      if (descItemTrim.length === 0) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['items', index, 'descripcion'],
          message: 'La descripción del ítem es obligatoria',
        });
      } else if (descItemTrim.length > 500) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['items', index, 'descripcion'],
          message: 'La descripción del ítem no puede superar los 500 caracteres',
        });
      } else {
        // Comparar sin distinguir mayúsculas ni espacios extras
        const descNormalizada = descItemTrim.replace(/\s+/g, ' ').toUpperCase();
        const listaIndicesDesc = descripcionesPorValor.get(descNormalizada) ?? [];
        listaIndicesDesc.push(index);
        descripcionesPorValor.set(descNormalizada, listaIndicesDesc);
      }
    });

    // 5. Orden repetido
    ordenesPorValor.forEach((indices, numOrden) => {
      if (indices.length > 1) {
        indices.forEach((idx) => {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['items', idx, 'orden'],
            message: `El orden de visualización de los ítems no es consecutivo: el orden ${numOrden} está repetido`,
          });
        });
      }
    });

    // 6. Orden consecutivo 1..n sin huecos
    const n = data.items.length;
    if (n >= 2 && todosLosOrdenesSonEnterosPositivos) {
      const faltantes: number[] = [];
      for (let esperado = 1; esperado <= n; esperado++) {
        if (!ordenesPorValor.has(esperado)) {
          faltantes.push(esperado);
        }
      }

      if (faltantes.length > 0) {
        const primerFaltante = faltantes[0];
        const mensajeHueco = `El orden de visualización de los ítems debe ser consecutivo desde 1 hasta ${n} sin huecos (falta el orden ${primerFaltante})`;

        ordenesValidosPorIndice.forEach((numOrden, idx) => {
          const ocurrencias = ordenesPorValor.get(numOrden)?.length ?? 0;
          if (numOrden > n && ocurrencias === 1) {
            ctx.addIssue({
              code: z.ZodIssueCode.custom,
              path: ['items', idx, 'orden'],
              message: mensajeHueco,
            });
          }
        });
      }
    }

    // 7. Sin descripciones de ítem repetidas
    descripcionesPorValor.forEach((indices, descNormalizada) => {
      if (indices.length > 1) {
        indices.forEach((idx) => {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['items', idx, 'descripcion'],
            message: `La descripción del ítem '${descNormalizada}' está duplicada dentro de la misma opción de respuesta`,
          });
        });
      }
    });
  });

export type ItemOpcionRespuestaFormData = z.infer<typeof ItemOpcionRespuestaFormSchema>;
export type OpcionRespuestaFormData = z.infer<typeof OpcionRespuestaSchema>;
