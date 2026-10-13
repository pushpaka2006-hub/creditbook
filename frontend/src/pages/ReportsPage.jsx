import React, { useState, useEffect } from 'react';
import { reportService } from '../services/reportService';
import { StatCard } from '../components/StatCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Badge } from '../components/Badge';
import { formatCurrency, formatDate } from '../utils/formatters';
import { printDocument } from '../utils/exportUtils';
import {
  Download,
  Printer,
  Calendar,
  CreditCard,
  CheckCircle,
  AlertCircle,
  Filter,
  Users,
} from 'lucide-react';

export const ReportsPage = ({ onNavigateToCustomerLedger }) => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState('month'); // today, week, month, all, custom
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const fetchReport = async () => {
    try {
      setLoading(true);
      const params = {};

      const now = new Date();
      if (period === 'today') {
        const start = new Date(now.setHours(0, 0, 0, 0));
        params.startDate = start.toISOString();
      } else if (period === 'week') {
        const start = new Date(now.setDate(now.getDate() - 7));
        params.startDate = start.toISOString();
      } else if (period === 'month') {
        const start = new Date(now.setDate(now.getDate() - 30));
        params.startDate = start.toISOString();
      } else if (period === 'custom') {
        if (startDate) params.startDate = new Date(startDate).toISOString();
        if (endDate) params.endDate = new Date(endDate).toISOString();
      }

      const data = await reportService.getReportSummary(params);
      setReport(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, [period, startDate, endDate]);

  const handleExportTransactionsCsv = async () => {
    try {
      const params = {};
      if (startDate) params.startDate = new Date(startDate).toISOString();
      if (endDate) params.endDate = new Date(endDate).toISOString();
      const blob = await reportService.exportTransactionsCsv(params);

      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `creditbook_transactions_${new Date().toISOString().slice(0, 10)}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      alert('Failed to export transactions CSV');
    }
  };

  const handleExportCustomersCsv = async () => {
    try {
      const blob = await reportService.exportCustomersCsv();
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `creditbook_customers_${new Date().toISOString().slice(0, 10)}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      alert('Failed to export customers CSV');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header and Print/Export Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 no-print">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Financial Reports & Statements</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Credit summaries, recovery ratios, and customer-wise accounting statements
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleExportTransactionsCsv}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-lg transition-colors shadow-sm"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export Transactions CSV</span>
          </button>

          <button
            onClick={handleExportCustomersCsv}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-lg transition-colors shadow-sm"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export Customer Dues CSV</span>
          </button>

          <button
            onClick={printDocument}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg transition-colors shadow-sm"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print Report</span>
          </button>
        </div>
      </div>

      {/* Date Filter Bar */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-4 shadow-sm flex flex-wrap items-center justify-between gap-3 no-print">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-slate-600">Period:</span>
          {['today', 'week', 'month', 'all', 'custom'].map((p) => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold uppercase tracking-wider transition-all ${
                period === p
                  ? 'bg-sky-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {p === 'today'
                ? 'Today'
                : p === 'week'
                ? 'Last 7 Days'
                : p === 'month'
                ? 'Last 30 Days'
                : p === 'all'
                ? 'All Time'
                : 'Custom'}
            </button>
          ))}
        </div>

        {period === 'custom' && (
          <div className="flex items-center gap-2 text-xs">
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="px-2.5 py-1.5 border border-slate-300 rounded-lg text-slate-700"
            />
            <span className="text-slate-400">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="px-2.5 py-1.5 border border-slate-300 rounded-lg text-slate-700"
            />
          </div>
        )}
      </div>

      {loading ? (
        <LoadingSpinner text="Computing comprehensive report statistics..." />
      ) : (
        <>
          {/* Key Metric Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <StatCard
              title="Period Credit Given"
              value={formatCurrency(report?.totalCreditGiven)}
              subtitle="Total money lent in selected window"
              icon={CreditCard}
              color="blue"
            />
            <StatCard
              title="Period Payments Received"
              value={formatCurrency(report?.totalPaymentReceived)}
              subtitle="Recoveries received in selected window"
              icon={CheckCircle}
              color="emerald"
            />
            <StatCard
              title="Net Position Difference"
              value={formatCurrency(report?.netOutstanding)}
              subtitle="Net credit expansion vs repayment"
              icon={AlertCircle}
              color="amber"
            />
          </div>

          {/* Customer Accounts Breakdown Report Table */}
          <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden print-card">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h3 className="font-bold text-slate-800 text-sm">Customer-Wise Ledger Balance Report</h3>
                <p className="text-[11px] text-slate-400">Detailed debtor statement across all registered books</p>
              </div>
              <span className="text-xs font-semibold text-slate-500">
                {report?.customerReports?.length || 0} Accounts
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50/80 text-slate-500 font-semibold border-b border-slate-100">
                    <th className="py-3 px-4">Customer</th>
                    <th className="py-3 px-4">Contact</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Opening (₹)</th>
                    <th className="py-3 px-4 text-right">Total Credit (₹)</th>
                    <th className="py-3 px-4 text-right">Total Paid (₹)</th>
                    <th className="py-3 px-4 text-right">Net Outstanding Due (₹)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {report?.customerReports && report.customerReports.length > 0 ? (
                    report.customerReports.map((c) => {
                      const isDue = Number(c.outstandingBalance || 0) > 0;
                      return (
                        <tr key={c.id} className="hover:bg-slate-50/70 transition-colors">
                          <td className="py-3 px-4 font-bold text-slate-900">
                            <button
                              onClick={() => onNavigateToCustomerLedger(c.id)}
                              className="text-sky-700 hover:underline"
                            >
                              {c.fullName}
                            </button>
                          </td>
                          <td className="py-3 px-4 text-slate-600">{c.mobile}</td>
                          <td className="py-3 px-4">
                            <Badge type={c.status} />
                          </td>
                          <td className="py-3 px-4 text-right text-slate-500">
                            {formatCurrency(c.openingBalance)}
                          </td>
                          <td className="py-3 px-4 text-right font-medium text-sky-700">
                            {formatCurrency(c.totalCreditGiven)}
                          </td>
                          <td className="py-3 px-4 text-right font-medium text-emerald-700">
                            {formatCurrency(c.totalPaymentReceived)}
                          </td>
                          <td className="py-3 px-4 text-right font-bold text-sm">
                            <span className={isDue ? 'text-amber-800' : 'text-slate-700'}>
                              {formatCurrency(c.outstandingBalance)}
                            </span>
                          </td>
                        </tr>
                      );
                    })
                  ) : (
                    <tr>
                      <td colSpan={7} className="py-8 text-center text-slate-400">
                        No customer accounts found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
