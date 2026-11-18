'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import {
  AltaOpcionRespuestaForm,
  AltaOpcionRespuestaFormProps,
} from './AltaOpcionRespuestaForm';

export const AltaOpcionRespuestaScreen: React.FC<AltaOpcionRespuestaFormProps> = (
  props
) => {
  return (
    <DashboardLayout>
      <AltaOpcionRespuestaForm {...props} />
    </DashboardLayout>
  );
};

export default AltaOpcionRespuestaScreen;
