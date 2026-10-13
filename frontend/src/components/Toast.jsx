import React from 'react';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export const Toast = ({ message, type = 'success', onClose }) => {
  if (!message) return null;

  const typeConfig = {
    success: {
      bg: 'bg-emerald-50 border-emerald-200 text-emerald-800',
      icon: <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />,
    },
    error: {
      bg: 'bg-rose-50 border-rose-200 text-rose-800',
      icon: <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />,
    },
    info: {
      bg: 'bg-sky-50 border-sky-200 text-sky-800',
      icon: <Info className="w-4 h-4 text-sky-600 flex-shrink-0" />,
    },
  };

  const config = typeConfig[type] || typeConfig.info;

  return (
    <div className={`fixed bottom-5 right-5 z-50 flex items-center gap-3 px-4 py-3 rounded-lg border shadow-lg ${config.bg} animate-in slide-in-from-bottom-5 max-w-md`}>
      {config.icon}
      <span className="text-sm font-medium">{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          className="ml-auto text-slate-400 hover:text-slate-600 p-0.5"
        >
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};
