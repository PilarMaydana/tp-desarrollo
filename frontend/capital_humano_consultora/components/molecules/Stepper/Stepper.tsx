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
  return (
    <div className={`flex items-center justify-between w-full max-w-2xl mx-auto py-4 ${className}`}>
      {steps.map((step, idx) => {
        const isCompleted = idx < currentStep;
        const isActive = idx === currentStep;

        return (
          <React.Fragment key={step.id}>
            {/* Step Node */}
            <div
              onClick={() => onStepClick && isCompleted && onStepClick(idx)}
              className={`flex flex-col items-center gap-1.5 ${
                onStepClick && isCompleted ? 'cursor-pointer' : 'cursor-default'
              }`}
            >
              <div
                className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm transition-all shadow-xs ${
                  isActive
                    ? 'bg-[#3F2581] text-white ring-4 ring-[#EADDFF]'
                    : isCompleted
                    ? 'bg-[#CDCEE6] text-[#3F2581]'
                    : 'bg-[#E0E0E0] text-[#757575]'
                }`}
              >
                {isCompleted ? <Icon name="check" size={18} /> : idx + 1}
              </div>
              <span
                className={`text-xs md:text-sm font-semibold tracking-wide ${
                  isActive
                    ? 'text-[#3F2581]'
                    : isCompleted
                    ? 'text-[#4F378A]'
                    : 'text-[#9E9E9E]'
                }`}
              >
                {step.label}
              </span>
            </div>

            {/* Connecting Line */}
            {idx < steps.length - 1 && (
              <div
                className={`flex-1 h-1 mx-3 rounded-full transition-all ${
                  idx < currentStep ? 'bg-[#3F2581]' : 'bg-[#E0E0E0]'
                }`}
              />
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
};

export default Stepper;
