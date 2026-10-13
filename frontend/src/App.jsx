import React, { useState, useEffect } from 'react';
import { useAuth } from './context/AuthContext';
import { AppLayout } from './layouts/AppLayout';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { CustomerListPage } from './pages/CustomerListPage';
import { CustomerLedgerPage } from './pages/CustomerLedgerPage';
import { TransactionListPage } from './pages/TransactionListPage';
import { PendingPaymentsPage } from './pages/PendingPaymentsPage';
import { ReportsPage } from './pages/ReportsPage';
import { AuditLogsPage } from './pages/AuditLogsPage';
import { SettingsPage } from './pages/SettingsPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { LoadingSpinner } from './components/LoadingSpinner';

export const App = () => {
  const { isAuthenticated, loading } = useAuth();

  // Internal routing state with hash sync
  const [route, setRoute] = useState('dashboard');
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Sync route with URL hash for deep linking and back/forward navigation
  useEffect(() => {
    const parseHash = () => {
      const hash = window.location.hash.replace(/^#\/?/, '');
      if (!hash) {
        setRoute('dashboard');
        return;
      }

      if (hash.startsWith('customers/') && hash.endsWith('/ledger')) {
        const parts = hash.split('/');
        const id = parts[1];
        if (id) {
          setSelectedCustomerId(id);
          setRoute('ledger');
          return;
        }
      }

      const knownRoutes = [
        'login',
        'register',
        'dashboard',
        'customers',
        'transactions',
        'reminders',
        'reports',
        'audit-logs',
        'settings',
      ];
      if (knownRoutes.includes(hash)) {
        setRoute(hash);
      } else {
        setRoute('not-found');
      }
    };

    parseHash();
    window.addEventListener('hashchange', parseHash);
    return () => window.removeEventListener('hashchange', parseHash);
  }, []);

  const navigateTo = (newRoute, param = null) => {
    if (newRoute === 'ledger' && param) {
      setSelectedCustomerId(param);
      setRoute('ledger');
      window.location.hash = `#/customers/${param}/ledger`;
    } else {
      setSelectedCustomerId(null);
      setRoute(newRoute);
      window.location.hash = `#/${newRoute}`;
    }
  };

  const triggerGlobalRefresh = () => {
    setRefreshTrigger((prev) => prev + 1);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <LoadingSpinner text="Initializing secure Credit Book session..." />
      </div>
    );
  }

  // Unauthenticated routing
  if (!isAuthenticated) {
    if (route === 'register') {
      return <RegisterPage onNavigateToLogin={() => navigateTo('login')} />;
    }
    return <LoginPage onNavigateToRegister={() => navigateTo('register')} />;
  }

  // Titles for the header
  const routeTitles = {
    dashboard: 'Dashboard',
    customers: 'Customers',
    ledger: 'Customer Ledger',
    transactions: 'Transactions',
    reminders: 'Pending Dues & Reminders',
    reports: 'Reports & Analytics',
    'audit-logs': 'Audit Trail',
    settings: 'Settings',
  };

  return (
    <AppLayout
      currentRoute={route}
      navigateTo={navigateTo}
      title={routeTitles[route] || 'Credit Book'}
      onRefreshData={triggerGlobalRefresh}
    >
      {({ openAddCustomer, openEditCustomer, openRecordTx, showToast }) => {
        switch (route) {
          case 'dashboard':
            return (
              <DashboardPage
                onOpenAddCustomer={openAddCustomer}
                onOpenRecordTransaction={openRecordTx}
                onNavigateToCustomerLedger={(id) => navigateTo('ledger', id)}
                onNavigateToCustomers={() => navigateTo('customers')}
                onNavigateToTransactions={() => navigateTo('transactions')}
              />
            );

          case 'customers':
            return (
              <CustomerListPage
                onOpenAddCustomer={openAddCustomer}
                onOpenEditCustomer={openEditCustomer}
                onNavigateToCustomerLedger={(id) => navigateTo('ledger', id)}
                onOpenRecordTransaction={openRecordTx}
                refreshTrigger={refreshTrigger}
              />
            );

          case 'ledger':
            return (
              <CustomerLedgerPage
                customerId={selectedCustomerId}
                onBack={() => navigateTo('customers')}
                onOpenRecordTransaction={openRecordTx}
                refreshTrigger={refreshTrigger}
              />
            );

          case 'transactions':
            return (
              <TransactionListPage
                onOpenRecordTransaction={openRecordTx}
                onNavigateToCustomerLedger={(id) => navigateTo('ledger', id)}
                refreshTrigger={refreshTrigger}
              />
            );

          case 'reminders':
            return (
              <PendingPaymentsPage
                onNavigateToCustomerLedger={(id) => navigateTo('ledger', id)}
                onOpenRecordTransaction={openRecordTx}
              />
            );

          case 'reports':
            return (
              <ReportsPage
                onNavigateToCustomerLedger={(id) => navigateTo('ledger', id)}
              />
            );

          case 'audit-logs':
            return <AuditLogsPage />;

          case 'settings':
            return <SettingsPage />;

          default:
            return <NotFoundPage onGoHome={() => navigateTo('dashboard')} />;
        }
      }}
    </AppLayout>
  );
};

export default App;
