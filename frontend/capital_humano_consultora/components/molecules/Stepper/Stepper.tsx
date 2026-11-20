'use client';

import React from 'react';
import { Icon } from '@/components/ui/Icon';

export interface StepItem {
  id: string | number;
  label: string;
}

export interface StepperProps {
  steps: StepItem[];
  currentStep: number; // 0-indexed
  onStepClick?: (stepIndex: number) => void;
  className?: string;
}

export const Stepper: React.FC<StepperProps> = ({
  steps,
  currentStep,
  onStepClick,
  className = '',
}) => {
  const progressPercent =
    steps.length > 1
      ? (Math.min(currentStep, steps.length - 1) / (steps.length - 1)) * 100
      : 0;

  return (
    <div className={`w-full max-w-lg mx-auto py-2 px-2 ${className}`}>
      <div className="relative flex items-start justify-between">
        {/* Línea horizontal continua centrada exactamente a la altura del punto medio de los círculos (12px) */}
        <div className="absolute top-3 left-3 right-3 -translate-y-1/2 h-[2px] bg-[#E0E0E0] z-0">
          <div
            className="h-full bg-[#3F2581] transition-all duration-300"
            style={{ width: `${progressPercent}%` }}
          />
        </div>

        {/* Nodos de cada paso: círculo pequeño con número y etiqueta por debajo */}
        {steps.map((step, idx) => {
          const isCompleted = idx < currentStep;
          const isActive = idx === currentStep;

          return (
            <div
              key={step.id}
              onClick={() => onStepClick && isCompleted && onStepClick(idx)}
              className={`relative z-10 flex flex-col items-center group select-none ${
                onStepClick && isCompleted ? 'cursor-pointer' : 'cursor-default'
              }`}
            >
              {/* Círculo 1, 2, 3 más pequeño */}
              <div
                className={`w-6 h-6 rounded-full flex items-center justify-center font-bold text-[11px] transition-all shrink-0 ${
                  isActive
                    ? 'bg-[#3F2581] text-white ring-3 ring-[#EADDFF]'
                    : isCompleted
                    ? 'bg-[#3F2581] text-white'
                    : 'bg-white border-2 border-[#BDBDBD] text-[#757575]'
                }`}
              >
                {isCompleted ? <Icon name="check" size={13} /> : idx + 1}
              </div>

              {/* Etiqueta por debajo del círculo */}
              <span
                className={`text-xs mt-1.5 font-medium tracking-tight text-center whitespace-nowrap ${
                  isActive
                    ? 'text-[#3F2581] font-semibold'
                    : isCompleted
                    ? 'text-[#4F378A]'
                    : 'text-[#757575]'
                }`}
              >
                {step.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Stepper;
