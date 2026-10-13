import React, { useState, useEffect } from 'react';
import { customerService } from '../services/customerService';
import { SearchBar } from '../components/SearchBar';
import { Badge } from '../components/Badge';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { formatCurrency, formatDate } from '../utils/formatters';
import {
  UserPlus,
  BookOpen,
  Edit2,
  Trash2,
  Phone,
  Mail,
  MapPin,
  TrendingUp,
  Filter,
} from 'lucide-react';

export const CustomerListPage = ({
  onOpenAddCustomer,
  onOpenEditCustomer,
  onNavigateToCustomerLedger,
  onOpenRecordTransaction,
  refreshTrigger,
}) => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [onlyWithDues, setOnlyWithDues] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [feedback, setFeedback] = useState({ message: '', type: '' });

  const fetchCustomers = async () => {
    try {
      setLoading(true);
      const params = {};
      if (search.trim()) params.search = search.trim();
      if (statusFilter) params.status = statusFilter;

      const data = await customerService.getAll(params);
      setCustomers(data);
    } catch (err) {
      setFeedback({ message: 'Failed to load customers', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, [search, statusFilter, refreshTrigger]);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await customerService.delete(deleteTarget.id);
      setFeedback({ message: `Customer "${deleteTarget.fullName}" deleted successfully`, type: 'success' });
      setDeleteTarget(null);
      fetchCustomers();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Failed to delete customer';
      setFeedback({ message: msg, type: 'error' });
      setDeleteTarget(null);
    } finally {
      setIsDeleting(false);
    }
  };

  // Client-side dues filter
  const displayedCustomers = customers.filter((c) => {
    if (onlyWithDues) {
      return Number(c.outstandingBalance || 0) > 0;
    }
    return true;
  });

  return (
    <div className="space-y-5">
      {/* Page Title & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Customer Credit Accounts</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage debtors, customer details, and individual credit ledgers
          </p>
        </div>

        <button
          onClick={onOpenAddCustomer}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
        >
          <UserPlus className="w-4 h-4" />
          <span>Add New Customer</span>
        </button>
      </div>

      {feedback.message && (
        <div
          className={`p-3.5 text-xs rounded-xl flex items-center justify-between border ${
            feedback.type === 'error'
              ? 'bg-rose-50 border-rose-200 text-rose-700'
              : 'bg-emerald-50 border-emerald-200 text-emerald-700'
          }`}
        >
          <span>{feedback.message}</span>
          <button onClick={() => setFeedback({ message: '', type: '' })} className="font-bold underline ml-2">
            Dismiss
          </button>
        </div>
      )}

      {/* Search and Filters Bar */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-4 shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
        <SearchBar
          value={search}
          onChange={setSearch}
          onClear={() => setSearch('')}
          placeholder="Search by customer name, mobile, email..."
        />

        <div className="flex flex-wrap items-center gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 bg-white text-slate-700"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">ACTIVE Only</option>
            <option value="INACTIVE">INACTIVE Only</option>
          </select>

          <label className="inline-flex items-center gap-2 text-xs text-slate-700 font-medium cursor-pointer select-none">
            <input
              type="checkbox"
              checked={onlyWithDues}
              onChange={(e) => setOnlyWithDues(e.target.checked)}
              className="w-4 h-4 text-sky-600 rounded border-slate-300 focus:ring-sky-500"
            />
            <span>Pending Dues Only</span>
          </label>
        </div>
      </div>

      {/* Customer Accounts Table */}
      {loading ? (
        <LoadingSpinner text="Fetching customer ledgers..." />
      ) : displayedCustomers.length === 0 ? (
        <EmptyState
          title="No customers match criteria"
          description="Try clearing search keywords or create a new customer record."
          actionText="Add Customer"
          onAction={onOpenAddCustomer}
        />
      ) : (
        <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50/80 text-slate-500 font-semibold border-b border-slate-100">
                  <th className="py-3 px-4">Customer Details</th>
                  <th className="py-3 px-4">Contact Info</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Total Credit</th>
                  <th className="py-3 px-4">Total Repaid</th>
                  <th className="py-3 px-4">Outstanding Due</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {displayedCustomers.map((customer) => {
                  const hasDue = Number(customer.outstandingBalance || 0) > 0;
                  return (
                    <tr key={customer.id} className="hover:bg-slate-50/60 transition-colors">
                      <td className="py-3.5 px-4">
                        <button
                          onClick={() => onNavigateToCustomerLedger(customer.id)}
                          className="font-bold text-slate-900 text-sm hover:text-sky-600 transition-colors text-left block"
                        >
                          {customer.fullName}
                        </button>
                        {customer.notes && (
                          <span className="text-[11px] text-slate-400 block max-w-xs truncate mt-0.5">
                            {customer.notes}
                          </span>
                        )}
                      </td>

                      <td className="py-3.5 px-4 text-slate-600">
                        <div className="flex items-center gap-1.5 font-medium">
                          <Phone className="w-3.5 h-3.5 text-slate-400" />
                          <span>{customer.mobile}</span>
                        </div>
                        {customer.email && (
                          <div className="flex items-center gap-1.5 text-slate-400 mt-1">
                            <Mail className="w-3.5 h-3.5" />
                            <span className="truncate max-w-[140px]">{customer.email}</span>
                          </div>
                        )}
                      </td>

                      <td className="py-3.5 px-4">
                        <Badge type={customer.status} />
                      </td>

                      <td className="py-3.5 px-4 font-medium text-slate-600">
                        {formatCurrency(customer.totalCreditGiven)}
                      </td>

                      <td className="py-3.5 px-4 font-medium text-emerald-600">
                        {formatCurrency(customer.totalPaymentReceived)}
                      </td>

                      <td className="py-3.5 px-4">
                        <span
                          className={`font-bold text-sm ${
                            hasDue ? 'text-amber-700' : 'text-slate-700'
                          }`}
                        >
                          {formatCurrency(customer.outstandingBalance)}
                        </span>
                        {hasDue && (
                          <span className="block text-[10px] text-amber-600 font-semibold uppercase mt-0.5">
                            Payment Due
                          </span>
                        )}
                      </td>

                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => onNavigateToCustomerLedger(customer.id)}
                            title="Open Ledger"
                            className="p-1.5 text-sky-600 hover:bg-sky-50 rounded-lg transition-colors"
                          >
                            <BookOpen className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => onOpenRecordTransaction('CREDIT_GIVEN', customer.id)}
                            title="Add Credit"
                            className="p-1.5 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            <TrendingUp className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => onOpenEditCustomer(customer)}
                            title="Edit Customer"
                            className="p-1.5 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => setDeleteTarget(customer)}
                            title="Delete Customer"
                            className="p-1.5 text-rose-500 hover:bg-rose-50 rounded-lg transition-colors"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDeleteConfirm}
        isLoading={isDeleting}
        isDanger={true}
        title="Delete Customer Account"
        message={`Are you sure you want to delete "${deleteTarget?.fullName}"? Note: Customers with active non-reversed transactions cannot be deleted to preserve financial audit integrity.`}
        confirmText="Delete Customer"
      />
    </div>
  );
};
