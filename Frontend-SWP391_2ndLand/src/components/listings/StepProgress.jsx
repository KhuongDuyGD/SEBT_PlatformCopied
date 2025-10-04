import React from 'react';
import { CheckCircle } from 'lucide-react';

export default function StepProgress({ steps, currentStep, onStepClick }) {
  return (
    <div className="progress-stepper">
      {steps.map((step, index) => {
        const Icon = step.icon;
        const isActive = currentStep === step.id;
        const isCompleted = currentStep > step.id;
        return (
          <div key={step.id} className="step-item">
            <div
              onClick={() => onStepClick(step.id)}
              className={`step-icon ${isCompleted ? 'completed' : isActive ? 'active' : 'inactive'}`}
            >
              {isCompleted ? <CheckCircle className="w-6 h-6" /> : <Icon className="w-6 h-6" />}
            </div>
            <div className="step-info">
              <div className="step-number">Bước {step.id}</div>
              <div className="step-title">{step.title}</div>
            </div>
            {index < steps.length - 1 && (
              <div className={`step-connector ${isCompleted ? 'completed' : 'inactive'}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}
