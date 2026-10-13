import React from 'react';
import { BookOpen, Home } from 'lucide-react';

export const NotFoundPage = ({ onGoHome }) => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4 text-center">
      <div className="w-16 h-16 rounded-2xl bg-sky-50 text-sky-600 flex items-center justify-center mb-4">
        <BookOpen className="w-8 h-8" />
      </div>
      <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight">404</h1>
      <h2 className="text-base font-semibold text-slate-700 mt-1">Page Not Found</h2>
      <p className="text-xs text-slate-500 max-w-sm mt-2 mb-6">
        The financial record or page you requested does not exist or has been relocated.
      </p>
      <button
        onClick={onGoHome}
        className="inline-flex items-center gap-2 px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
      >
        <Home className="w-4 h-4" />
        <span>Return to Dashboard</span>
      </button>
    </div>
  );
};
