import React, { useState } from 'react';
import { Sidebar } from '../components/Sidebar';
import { Header } from '../components/Header';
import { CustomerModal } from '../components/CustomerModal';
import { TransactionModal } from '../components/TransactionModal';
import { Toast } from '../components/Toast';

export const AppLayout = ({
  currentRoute,
  navigateTo,
  title,
  children,
  onRefreshData,
}) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
  const [customerToEdit, setCustomerToEdit] = useState(null);
  const [isTxModalOpen, setIsTxModalOpen] = useState(false);
  const [txDefaultCustomerId, setTxDefaultCustomerId] = useState(null);
  const [txDefaultType, setTxDefaultType] = useState('CREDIT_GIVEN');
  const [toast, setToast] = useState({ message: '', type: 'success' });

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast({ message: '', type: 'success' });
    }, 4000);
  };

  const handleOpenAddCustomer = () => {
    setCustomerToEdit(null);
    setIsCustomerModalOpen(true);
  };

  const handleOpenEditCustomer = (customer) => {
    setCustomerToEdit(customer);
    setIsCustomerModalOpen(true);
  };

  const handleOpenRecordTransaction = (type = 'CREDIT_GIVEN', customerId = null) => {
    setTxDefaultType(type || 'CREDIT_GIVEN');
    setTxDefaultCustomerId(customerId);
    setIsTxModalOpen(true);
  };

  const handleCustomerSuccess = (msg) => {
    showToast(msg, 'success');
    if (onRefreshData) onRefreshData();
  };

  const handleTxSuccess = (msg) => {
    showToast(msg, 'success');
    if (onRefreshData) onRefreshData();
  };

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden font-sans">
      {/* Sidebar */}
      <Sidebar
        currentRoute={currentRoute}
        navigateTo={navigateTo}
        isMobileOpen={isMobileMenuOpen}
        closeMobileMenu={() => setIsMobileMenuOpen(false)}
        onOpenQuickAction={() => handleOpenRecordTransaction('CREDIT_GIVEN')}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Header
          onOpenMobileMenu={() => setIsMobileMenuOpen(true)}
          onOpenAddCustomer={handleOpenAddCustomer}
          onOpenRecordTransaction={() => handleOpenRecordTransaction('CREDIT_GIVEN')}
          title={title}
        />

        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <div className="max-w-7xl mx-auto">
            {typeof children === 'function'
              ? children({
                  openAddCustomer: handleOpenAddCustomer,
                  openEditCustomer: handleOpenEditCustomer,
                  openRecordTx: handleOpenRecordTransaction,
                  showToast,
                })
              : children}
          </div>
        </main>
      </div>

      {/* Global Modals */}
      <CustomerModal
        isOpen={isCustomerModalOpen}
        onClose={() => setIsCustomerModalOpen(false)}
        customerToEdit={customerToEdit}
        onSuccess={handleCustomerSuccess}
      />

      <TransactionModal
        isOpen={isTxModalOpen}
        onClose={() => setIsTxModalOpen(false)}
        defaultCustomerId={txDefaultCustomerId}
        defaultType={txDefaultType}
        onSuccess={handleTxSuccess}
      />

      {/* Global Toast */}
      {toast.message && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast({ message: '', type: 'success' })}
        />
      )}
    </div>
  );
};
