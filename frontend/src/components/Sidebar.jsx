import React from 'react';
import {
  LayoutDashboard,
  Users,
  Receipt,
  BellRing,
  BarChart3,
  History,
  Settings,
  LogOut,
  BookOpen,
  PlusCircle,
  X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar = ({ currentRoute, navigateTo, isMobileOpen, closeMobileMenu, onOpenQuickAction }) => {
  const { user, logout } = useAuth();

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'customers', label: 'Customers', icon: Users },
    { id: 'transactions', label: 'All Transactions', icon: Receipt },
    { id: 'reminders', label: 'Pending Dues & Reminders', icon: BellRing },
    { id: 'reports', label: 'Reports & Analytics', icon: BarChart3 },
    { id: 'audit-logs', label: 'Audit Trail', icon: History },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  const handleNavClick = (routeId) => {
    navigateTo(routeId);
    if (closeMobileMenu) closeMobileMenu();
  };

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 bg-slate-900/50 z-40 lg:hidden"
          onClick={closeMobileMenu}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed lg:static top-0 bottom-0 left-0 z-50 w-64 bg-white border-r border-slate-200 flex flex-col transition-transform duration-200 ease-in-out ${
          isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Brand Header */}
        <div className="h-16 flex items-center justify-between px-5 border-b border-slate-100">
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => handleNavClick('dashboard')}>
            <div className="w-9 h-9 rounded-lg bg-sky-600 flex items-center justify-center text-white shadow-sm shadow-sky-200">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <h1 className="font-bold text-slate-900 text-base leading-tight tracking-tight">Credit Book</h1>
              <p className="text-[11px] text-slate-400 font-medium">Digital Financial Ledger</p>
            </div>
          </div>

          <button
            onClick={closeMobileMenu}
            className="lg:hidden text-slate-400 hover:text-slate-600 p-1 rounded-md"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Business Badge */}
        <div className="px-4 py-3 border-b border-slate-100 bg-slate-50/70">
          <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">Merchant Account</p>
          <p className="text-xs font-semibold text-slate-800 truncate">{user?.businessName || 'My Business'}</p>
          <p className="text-[11px] text-slate-500 truncate">{user?.fullName}</p>
        </div>

        {/* Quick Action Button */}
        <div className="px-4 pt-4">
          <button
            onClick={() => {
              if (onOpenQuickAction) onOpenQuickAction();
              if (closeMobileMenu) closeMobileMenu();
            }}
            className="w-full flex items-center justify-center gap-2 px-3 py-2.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-all"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Record Transaction</span>
          </button>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentRoute === item.id;
            return (
              <button
                key={item.id}
                onClick={() => handleNavClick(item.id)}
                className={`w-full flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors text-left ${
                  isActive
                    ? 'bg-sky-50 text-sky-700 font-semibold'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-sky-600' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* User Footer & Logout */}
        <div className="p-3 border-t border-slate-100">
          <button
            onClick={logout}
            className="w-full flex items-center gap-3 px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4 text-rose-500" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>
    </>
  );
};
