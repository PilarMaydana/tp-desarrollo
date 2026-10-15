'use client';

import React from 'react';

export type ActionToolbarAlign = 'right' | 'between' | 'center' | 'left';

export interface ActionToolbarProps {
  align?: ActionToolbarAlign;
  children: React.ReactNode;
  className?: string;
}

export const ActionToolbar: React.FC<ActionToolbarProps> = ({
  align = 'right',
  children,
  className = '',
}) => {
  const alignStyles: Record<ActionToolbarAlign, string> = {
    right: 'justify-end',
    between: 'justify-between',
    center: 'justify-center',
    left: 'justify-start',
  };

  return (
    <footer
      className={`w-full py-4 px-2 mt-8 flex flex-wrap items-center gap-4 border-t border-[#E0E0E0] ${alignStyles[align]} ${className}`}
    >
      {children}
    </footer>
  );
};

export default ActionToolbar;
