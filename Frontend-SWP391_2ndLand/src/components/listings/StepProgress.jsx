import React from 'react';
import { CheckCircle } from 'lucide-react';

export default function StepProgress({ steps, currentStep, onStepClick }) {
  return (
    <div className="progress-stepper">
      {steps.map((step, index) => {
        const Icon = step.icon;
        const isActive = currentStep === step.id;
        const isCompleted = currentStep > step.id;
        
        // Logic bảo mật: chỉ cho phép click vào bước hiện tại hoặc các bước đã hoàn thành
        // Điều này ngăn người dùng skip các bước validation
        const isClickable = isActive || isCompleted;
        
        return (
          <div key={step.id} className="step-item">
            <div
              onClick={() => isClickable && onStepClick(step.id)}
              className={`step-icon ${isCompleted ? 'completed' : isActive ? 'active' : 'inactive'} ${
                isClickable ? 'cursor-pointer' : 'cursor-not-allowed opacity-50'
              }`}
              title={isClickable ? `Đi đến bước ${step.id}` : 'Hoàn thành bước hiện tại để mở khóa'}
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
