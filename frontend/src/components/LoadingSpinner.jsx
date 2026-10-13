import React from 'react';
import { Loader2 } from 'lucide-react';

export const LoadingSpinner = ({ text = "Loading data..." }) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-slate-500">
      <Loader2 className="w-8 h-8 text-sky-600 animate-spin mb-3" />
      <span className="text-sm font-medium text-slate-600">{text}</span>
    </div>
  );
};

export const TableSkeleton = ({ rows = 5, cols = 4 }) => {
  return (
    <div className="animate-pulse space-y-3 p-4">
      {Array.from({ length: rows }).map((_, r) => (
        <div key={r} className="flex gap-4">
          {Array.from({ length: cols }).map((_, c) => (
            <div
              key={c}
              className="h-4 bg-slate-200 rounded"
              style={{ width: `${100 / cols}%` }}
            />
          ))}
        </div>
      ))}
    </div>
  );
};
