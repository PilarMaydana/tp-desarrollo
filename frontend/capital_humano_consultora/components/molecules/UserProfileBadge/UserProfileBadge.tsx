'use client';

import React from 'react';
import { User } from '@/types/auth';
import { Icon } from '@/components/ui/Icon';

export interface UserProfileBadgeProps {
  user: User | null;
  className?: string;
}

export const UserProfileBadge: React.FC<UserProfileBadgeProps> = ({ user, className = '' }) => {
  const roleLabel = user?.role === 'CANDIDATO' ? 'Candidato:' : 'Consultor:';
  const nameLabel = user?.name || 'Usuario';

  return (
    <div className={`flex items-center gap-3 select-none ${className}`}>
      {/* Avatar circular */}
      <div className="w-11 h-11 rounded-full bg-[#EADDFF] flex items-center justify-center text-[#4F378A] shadow-xs shrink-0">
        <Icon name="person" size={26} />
      </div>

      {/* Rol y Nombre */}
      <div className="flex flex-col text-left leading-tight">
        <span className="text-xs text-white/80 font-normal">{roleLabel}</span>
        <span className="text-sm md:text-base font-medium text-white truncate max-w-[160px] md:max-w-[200px]">
          {nameLabel}
        </span>
      </div>
    </div>
  );
};

export default UserProfileBadge;
