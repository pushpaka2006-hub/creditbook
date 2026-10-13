import React from 'react';
import { Menu, User, Plus, Bell } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Header = ({ onOpenMobileMenu, onOpenAddCustomer, onOpenRecordTransaction, title }) => {
  const { user } = useAuth();

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileMenu}
          className="lg:hidden text-slate-500 hover:text-slate-700 p-2 rounded-lg hover:bg-slate-100 transition-colors"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div>
          <h2 className="text-lg font-bold text-slate-900 tracking-tight">{title || 'Credit Book'}</h2>
        </div>
      </div>

      <div className="flex items-center gap-3">
        {onOpenAddCustomer && (
          <button
            onClick={onOpenAddCustomer}
            className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Customer</span>
          </button>
        )}

        {onOpenRecordTransaction && (
          <button
            onClick={onOpenRecordTransaction}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Record Transaction</span>
          </button>
        )}

        <div className="h-6 w-px bg-slate-200 hidden sm:block mx-1"></div>

        <div className="flex items-center gap-2 pl-1">
          <div className="w-8 h-8 rounded-full bg-sky-100 text-sky-800 font-semibold text-xs flex items-center justify-center border border-sky-200">
            {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
          </div>
          <span className="text-xs font-medium text-slate-700 hidden md:inline-block">
            {user?.fullName}
          </span>
        </div>
      </div>
    </header>
  );
};
