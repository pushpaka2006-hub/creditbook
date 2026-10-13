import React from 'react';

export const Badge = ({ type, text, size = 'sm' }) => {
  let colorClasses = 'bg-slate-100 text-slate-700 border-slate-200';

  const t = (type || text || '').toUpperCase();

  if (t === 'ACTIVE' || t === 'PAID') {
    colorClasses = 'bg-emerald-50 text-emerald-700 border-emerald-200';
  } else if (t === 'INACTIVE') {
    colorClasses = 'bg-slate-100 text-slate-600 border-slate-200';
  } else if (t === 'DUE' || t === 'PENDING') {
    colorClasses = 'bg-amber-50 text-amber-700 border-amber-200';
  } else if (t === 'REVERSED') {
    colorClasses = 'bg-rose-50 text-rose-700 border-rose-200';
  } else if (t === 'CREDIT_GIVEN') {
    colorClasses = 'bg-sky-50 text-sky-700 border-sky-200';
  } else if (t === 'PAYMENT_RECEIVED') {
    colorClasses = 'bg-emerald-50 text-emerald-700 border-emerald-200';
  } else if (t === 'CREDIT_BORROWED') {
    colorClasses = 'bg-purple-50 text-purple-700 border-purple-200';
  } else if (t === 'PAYMENT_MADE') {
    colorClasses = 'bg-indigo-50 text-indigo-700 border-indigo-200';
  }

  const sizeClasses = size === 'xs' ? 'px-1.5 py-0.5 text-xs' : 'px-2.5 py-1 text-xs';

  return (
    <span className={`inline-flex items-center font-medium rounded-full border ${colorClasses} ${sizeClasses}`}>
      {text || type}
    </span>
  );
};
