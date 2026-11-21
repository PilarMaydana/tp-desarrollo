import React from 'react';
import { CandidateHeaderLayout } from '@/features/cuestionario';

export default function CuestionarioCandidatoLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <CandidateHeaderLayout>{children}</CandidateHeaderLayout>;
}
