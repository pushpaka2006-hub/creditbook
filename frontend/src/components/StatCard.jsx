import React from 'react';

export const StatCard = ({ title, value, subtitle, icon: Icon, color = 'blue', trend }) => {
  const colorMap = {
    blue: {
      bg: 'bg-sky-50',
      iconBg: 'bg-sky-500',
      text: 'text-sky-700',
    },
    emerald: {
      bg: 'bg-emerald-50',
      iconBg: 'bg-emerald-500',
      text: 'text-emerald-700',
    },
    amber: {
      bg: 'bg-amber-50',
      iconBg: 'bg-amber-500',
      text: 'text-amber-700',
    },
    rose: {
      bg: 'bg-rose-50',
      iconBg: 'bg-rose-500',
      text: 'text-rose-700',
    },
    purple: {
      bg: 'bg-purple-50',
      iconBg: 'bg-purple-500',
      text: 'text-purple-700',
    },
    teal: {
      bg: 'bg-teal-50',
      iconBg: 'bg-teal-500',
      text: 'text-teal-700',
    },
  };

  const scheme = colorMap[color] || colorMap.blue;

  return (
    <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-sm hover:shadow transition-shadow">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wider text-slate-500 mb-1">{title}</p>
          <h3 className="text-2xl font-bold text-slate-900 tracking-tight">{value}</h3>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        </div>
        {Icon && (
          <div className={`w-11 h-11 rounded-lg ${scheme.bg} flex items-center justify-center`}>
            <Icon className={`w-5 h-5 ${scheme.text}`} />
          </div>
        )}
      </div>
      {trend && (
        <div className="mt-3 pt-3 border-t border-slate-100 flex items-center text-xs">
          <span className="text-slate-600">{trend}</span>
        </div>
      )}
    </div>
  );
};
