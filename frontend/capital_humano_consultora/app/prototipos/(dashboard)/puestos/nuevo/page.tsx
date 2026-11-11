'use client';

import React from 'react';
import { AltaPuestoScreen } from '@/features/cu009-alta-puesto';
import { ROUTES } from '@/constants/routes';

export default function CrearPuestoPage() {
  return (
    <AltaPuestoScreen
      onSuccessRedirect={ROUTES.CONSULTOR.PUESTOS}
      onCancelRedirect={ROUTES.CONSULTOR.PUESTOS}
    />
  );
}
