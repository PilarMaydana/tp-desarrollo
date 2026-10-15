'use client';

import React from 'react';

export interface DividerProps {
  orientation?: 'horizontal' | 'vertical';
  className?: string;
}

export const Divider: React.FC<DividerProps> = ({ orientation = 'horizontal', className = '' }) => {
  if (orientation === 'vertical') {
    return <div className={`w-[1px] bg-[#E0E0E0] self-stretch mx-2 ${className}`} />;
  }
  return <div className={`h-[1px] w-full bg-[#E0E0E0] my-4 ${className}`} />;
};

export default Divider;
