import React, { useState, useEffect } from 'react';
import { transactionService } from '../services/transactionService';
import { customerService } from '../services/customerService';
import { SearchBar } from '../components/SearchBar';
import { Badge } from '../components/Badge';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { formatCurrency, formatDateTime } from '../utils/formatters';
import {
  PlusCircle,
  RotateCcw,
  Calendar,
  Filter,
  CheckCircle,
  AlertCircle,
  TrendingUp,
  ArrowUpDown,
} from 'lucide-react';

export const TransactionListPage = ({
  onOpenRecordTransaction,
  onNavigateToCustomerLedger,
  refreshTrigger,
}) => {
  const [transactions, setTransactions] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [selectedType, setSelectedType] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [sortBy, setSortBy] = useState('date-desc');
  const [reversalTarget, setReversalTarget] = useState(null);
  const [reversalReason, setReversalReason] = useState('');
  const [isReversing, setIsReversing] = useState(false);
  const [feedback, setFeedback] = useState({ message: '', type: '' });

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const params = {};
      if (search.trim()) params.query = search.trim();
      if (selectedCustomerId) params.customerId = selectedCustomerId;
      if (selectedType) params.type = selectedType;
      if (startDate) params.startDate = new Date(startDate).toISOString();
      if (endDate) params.endDate = new Date(endDate).toISOString();

      const [txData, custData] = await Promise.all([
        transactionService.getAll(params),
        customerService.getAll(),
      ]);

      setTransactions(txData);
      setCustomers(custData);
    } catch (err) {
      setFeedback({ message: 'Failed to load transactions', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, [search, selectedCustomerId, selectedType, startDate, endDate, refreshTrigger]);

  const handleReverseConfirm = async () => {
    if (!reversalTarget) return;
    setIsReversing(true);
    try {
      await transactionService.reverse(reversalTarget.id, reversalReason.trim() || 'Entry correction');
      setFeedback({
        message: `Transaction #${reversalTarget.id} safely reversed and audited.`,
        type: 'success',
      });
      setReversalTarget(null);
      setReversalReason('');
      fetchTransactions();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Failed to reverse transaction';
      setFeedback({ message: msg, type: 'error' });
      setReversalTarget(null);
    } finally {
      setIsReversing(false);
    }
  };

  // Client-side sorting
  const sortedTransactions = [...transactions].sort((a, b) => {
    if (sortBy === 'date-desc') return new Date(b.transactionDate) - new Date(a.transactionDate);
    if (sortBy === 'date-asc') return new Date(a.transactionDate) - new Date(b.transactionDate);
    if (sortBy === 'amount-desc') return Number(b.amount) - Number(a.amount);
    if (sortBy === 'amount-asc') return Number(a.amount) - Number(b.amount);
    return 0;
  });

  return (
    <div className="space-y-5">
      {/* Page Title & New Transaction Button */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Financial Transactions</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Full ledger transaction history with audit reversal controls
          </p>
        </div>

        <button
          onClick={() => onOpenRecordTransaction()}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Record New Transaction</span>
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

      {/* Filter and Search Bar */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-4 shadow-sm space-y-3">
        <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
          <SearchBar
            value={search}
            onChange={setSearch}
            onClear={() => setSearch('')}
            placeholder="Search descriptions, customer names, refs..."
          />

          <div className="flex items-center gap-2 text-xs">
            <ArrowUpDown className="w-4 h-4 text-slate-400" />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-3 py-2 border border-slate-300 rounded-lg bg-white text-slate-700 font-medium"
            >
              <option value="date-desc">Newest First</option>
              <option value="date-asc">Oldest First</option>
              <option value="amount-desc">Highest Amount</option>
              <option value="amount-asc">Lowest Amount</option>
            </select>
          </div>
        </div>

        {/* Filter Dropdowns row */}
        <div className="flex flex-wrap items-center gap-3 pt-2 border-t border-slate-100 text-xs">
          {/* Customer filter */}
          <select
            value={selectedCustomerId}
            onChange={(e) => setSelectedCustomerId(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg bg-white text-slate-700"
          >
            <option value="">All Customers</option>
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.fullName}
              </option>
            ))}
          </select>

          {/* Type filter */}
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg bg-white text-slate-700"
          >
            <option value="">All Transaction Types</option>
            <option value="CREDIT_GIVEN">Credit Given (Lent)</option>
            <option value="PAYMENT_RECEIVED">Payment Received</option>
            <option value="CREDIT_BORROWED">Credit Borrowed</option>
            <option value="PAYMENT_MADE">Payment Made</option>
          </select>

          {/* Date range inputs */}
          <div className="flex items-center gap-1.5">
            <span className="text-slate-400">From:</span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="px-2 py-1 border border-slate-300 rounded-lg text-slate-700"
            />
          </div>

          <div className="flex items-center gap-1.5">
            <span className="text-slate-400">To:</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="px-2 py-1 border border-slate-300 rounded-lg text-slate-700"
            />
          </div>

          {(selectedCustomerId || selectedType || startDate || endDate) && (
            <button
              onClick={() => {
                setSelectedCustomerId('');
                setSelectedType('');
                setStartDate('');
                setEndDate('');
              }}
              className="text-sky-600 font-semibold hover:underline"
            >
              Reset Filters
            </button>
          )}
        </div>
      </div>

      {/* Transactions Table */}
      {loading ? (
        <LoadingSpinner text="Loading transactions..." />
      ) : sortedTransactions.length === 0 ? (
        <EmptyState
          title="No transactions match search"
          description="Try broadening filters or record a new credit/payment."
          actionText="Record Transaction"
          onAction={() => onOpenRecordTransaction()}
        />
      ) : (
        <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50/80 text-slate-500 font-semibold border-b border-slate-100">
                  <th className="py-3 px-4">ID</th>
                  <th className="py-3 px-4">Date & Time</th>
                  <th className="py-3 px-4">Customer</th>
                  <th className="py-3 px-4">Type</th>
                  <th className="py-3 px-4">Amount</th>
                  <th className="py-3 px-4">Method & Ref</th>
                  <th className="py-3 px-4">Description</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {sortedTransactions.map((tx) => (
                  <tr
                    key={tx.id}
                    className={`hover:bg-slate-50/60 transition-colors ${
                      tx.reversed ? 'bg-rose-50/20 text-slate-400' : ''
                    }`}
                  >
                    <td className="py-3 px-4 font-mono text-slate-400">#{tx.id}</td>
                    <td className="py-3 px-4 text-slate-600 font-medium whitespace-nowrap">
                      {formatDateTime(tx.transactionDate)}
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-900">
                      <button
                        onClick={() => onNavigateToCustomerLedger(tx.customerId)}
                        className="text-sky-700 hover:underline"
                      >
                        {tx.customerName}
                      </button>
                    </td>
                    <td className="py-3 px-4">
                      <Badge type={tx.transactionType} />
                    </td>
                    <td className="py-3 px-4 font-bold text-slate-900 text-sm">
                      {formatCurrency(tx.amount)}
                    </td>
                    <td className="py-3 px-4 text-slate-600">
                      <div>{tx.paymentMethod}</div>
                      {tx.referenceNumber && (
                        <div className="text-[10px] text-slate-400 font-mono">
                          {tx.referenceNumber}
                        </div>
                      )}
                    </td>
                    <td className="py-3 px-4 text-slate-600 max-w-xs truncate">
                      {tx.description || '-'}
                    </td>
                    <td className="py-3 px-4">
                      <Badge
                        type={tx.reversed ? 'REVERSED' : tx.paymentStatus}
                        text={tx.reversed ? 'REVERSED' : tx.paymentStatus}
                        size="xs"
                      />
                    </td>
                    <td className="py-3 px-4 text-right">
                      {!tx.reversed ? (
                        <button
                          onClick={() => setReversalTarget(tx)}
                          title="Reverse this transaction"
                          className="inline-flex items-center gap-1 px-2.5 py-1 text-slate-600 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors border border-slate-200"
                        >
                          <RotateCcw className="w-3.5 h-3.5" />
                          <span>Reverse</span>
                        </button>
                      ) : (
                        <span className="text-[11px] text-slate-400 italic">Reversed</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Reversal Confirmation Dialog */}
      <ConfirmDialog
        isOpen={!!reversalTarget}
        onClose={() => {
          setReversalTarget(null);
          setReversalReason('');
        }}
        onConfirm={handleReverseConfirm}
        isLoading={isReversing}
        isDanger={true}
        title="Confirm Transaction Reversal"
        message={`Are you sure you want to reverse Transaction #${reversalTarget?.id} (${formatCurrency(
          reversalTarget?.amount
        )}) for ${reversalTarget?.customerName}? This will safely negate its effect on the customer's ledger balance while retaining the audit record.`}
        confirmText="Confirm Reversal"
      />
    </div>
  );
};
