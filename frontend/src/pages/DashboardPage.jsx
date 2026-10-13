import React, { useState, useEffect } from 'react';
import { reportService } from '../services/reportService';
import { StatCard } from '../components/StatCard';
import { FinancialChart } from '../components/FinancialChart';
import { Badge } from '../components/Badge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { formatCurrency, formatDateTime } from '../utils/formatters';
import {
  Users,
  CreditCard,
  CheckCircle,
  AlertCircle,
  TrendingUp,
  ArrowUpRight,
  ArrowDownLeft,
  UserPlus,
  PlusCircle,
  ExternalLink,
} from 'lucide-react';

export const DashboardPage = ({
  onOpenAddCustomer,
  onOpenRecordTransaction,
  onNavigateToCustomerLedger,
  onNavigateToCustomers,
  onNavigateToTransactions,
}) => {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const data = await reportService.getDashboardSummary();
      setSummary(data);
      setError('');
    } catch (err) {
      setError('Unable to load dashboard metrics. Check server connection.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) {
    return <LoadingSpinner text="Computing real-time credit ledger balances..." />;
  }

  if (error) {
    return (
      <div className="p-6 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl flex items-center justify-between">
        <span>{error}</span>
        <button
          onClick={fetchDashboardData}
          className="px-3 py-1.5 bg-rose-600 text-white text-xs font-semibold rounded-lg hover:bg-rose-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Quick Actions Header Banner */}
      <div className="bg-gradient-to-r from-sky-700 to-sky-900 rounded-2xl p-6 text-white shadow-md flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold tracking-tight">Financial Overview</h2>
          <p className="text-xs text-sky-100 mt-1">
            Real-time ledger summary of customer credit, recoveries, and outstanding receivables.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            onClick={onOpenAddCustomer}
            className="flex items-center gap-2 px-3.5 py-2 bg-white/10 hover:bg-white/20 text-white text-xs font-semibold rounded-xl border border-white/20 backdrop-blur-sm transition-colors"
          >
            <UserPlus className="w-4 h-4" />
            <span>Add Customer</span>
          </button>

          <button
            onClick={() => onOpenRecordTransaction('CREDIT_GIVEN')}
            className="flex items-center gap-2 px-3.5 py-2 bg-sky-500 hover:bg-sky-400 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
          >
            <ArrowUpRight className="w-4 h-4" />
            <span>Give Credit</span>
          </button>

          <button
            onClick={() => onOpenRecordTransaction('PAYMENT_RECEIVED')}
            className="flex items-center gap-2 px-3.5 py-2 bg-emerald-500 hover:bg-emerald-400 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
          >
            <ArrowDownLeft className="w-4 h-4" />
            <span>Record Payment</span>
          </button>
        </div>
      </div>

      {/* Primary Financial Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Outstanding Credit"
          value={formatCurrency(summary?.totalOutstandingCredit)}
          subtitle="Money customers currently owe"
          icon={AlertCircle}
          color="amber"
          trend={`${summary?.pendingPaymentCount || 0} customer(s) with pending balances`}
        />

        <StatCard
          title="Total Payments Received"
          value={formatCurrency(summary?.totalPaymentsReceived)}
          subtitle="Recovered customer payments"
          icon={CheckCircle}
          color="emerald"
          trend="Settled funds in cash & digital UPI"
        />

        <StatCard
          title="Total Money Lent"
          value={formatCurrency(summary?.totalMoneyLent)}
          subtitle="Lifetime credit extended"
          icon={CreditCard}
          color="blue"
          trend="Active store credit ledger"
        />

        <StatCard
          title="Total Customers"
          value={summary?.totalCustomers || 0}
          subtitle="Active ledger accounts"
          icon={Users}
          color="teal"
          trend="Unique customer books"
        />
      </div>

      {/* Borrowing stats if active */}
      {summary?.totalMoneyBorrowed && Number(summary.totalMoneyBorrowed) > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <StatCard
            title="Total Borrowed from Parties"
            value={formatCurrency(summary.totalMoneyBorrowed)}
            subtitle="Money borrowed for capital/inventory"
            icon={TrendingUp}
            color="purple"
          />
          <StatCard
            title="Outstanding Payable Due"
            value={formatCurrency(summary.totalOutstandingPayable)}
            subtitle="Money you owe back to lenders"
            icon={AlertCircle}
            color="rose"
          />
        </div>
      )}

      {/* Chart & Top Debtors Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Monthly Summary Chart (2 cols) */}
        <div className="lg:col-span-2">
          <FinancialChart data={summary?.monthlyChart || []} />
        </div>

        {/* Top Debtors Card (1 col) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-sm flex flex-col">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h4 className="font-semibold text-slate-800 text-sm">Highest Outstanding Due</h4>
              <p className="text-[11px] text-slate-400">Top customers to follow up</p>
            </div>
            <button
              onClick={onNavigateToCustomers}
              className="text-xs font-semibold text-sky-600 hover:text-sky-700"
            >
              View all
            </button>
          </div>

          <div className="flex-1 mt-3 divide-y divide-slate-100 overflow-y-auto max-h-72">
            {summary?.topDebtors && summary.topDebtors.length > 0 ? (
              summary.topDebtors.map((customer) => (
                <div
                  key={customer.id}
                  onClick={() => onNavigateToCustomerLedger(customer.id)}
                  className="py-3 flex items-center justify-between hover:bg-slate-50 px-2 rounded-lg cursor-pointer transition-colors"
                >
                  <div>
                    <h5 className="text-xs font-bold text-slate-800">{customer.fullName}</h5>
                    <p className="text-[11px] text-slate-400">{customer.mobile}</p>
                  </div>
                  <div className="text-right">
                    <span className="text-xs font-bold text-amber-700 block">
                      {formatCurrency(customer.outstandingBalance)}
                    </span>
                    <span className="text-[10px] text-slate-400">Pending</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="py-8 text-center text-xs text-slate-400">
                All customer accounts are fully settled!
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Recent Transactions Table */}
      <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h4 className="font-semibold text-slate-800 text-sm">Recent Transactions</h4>
            <p className="text-[11px] text-slate-400">Latest credit issues and payment receipts</p>
          </div>
          <button
            onClick={onNavigateToTransactions}
            className="inline-flex items-center gap-1 text-xs font-semibold text-sky-600 hover:text-sky-700"
          >
            <span>View all transactions</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 font-semibold border-b border-slate-100">
                <th className="py-3 px-4">Date & Time</th>
                <th className="py-3 px-4">Customer</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Method</th>
                <th className="py-3 px-4">Amount</th>
                <th className="py-3 px-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {summary?.recentTransactions && summary.recentTransactions.length > 0 ? (
                summary.recentTransactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4 text-slate-600 font-medium">
                      {formatDateTime(tx.transactionDate)}
                    </td>
                    <td className="py-3 px-4">
                      <button
                        onClick={() => onNavigateToCustomerLedger(tx.customerId)}
                        className="font-semibold text-sky-700 hover:underline"
                      >
                        {tx.customerName}
                      </button>
                    </td>
                    <td className="py-3 px-4">
                      <Badge type={tx.transactionType} />
                    </td>
                    <td className="py-3 px-4 text-slate-600">{tx.paymentMethod}</td>
                    <td className="py-3 px-4 font-bold text-slate-900">
                      {formatCurrency(tx.amount)}
                    </td>
                    <td className="py-3 px-4">
                      <Badge type={tx.paymentStatus} text={tx.paymentStatus} size="xs" />
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-400">
                    No transactions recorded yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
