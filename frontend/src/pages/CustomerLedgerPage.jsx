import React, { useState, useEffect } from 'react';
import { customerService } from '../services/customerService';
import { Badge } from '../components/Badge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { formatCurrency, formatDateTime, formatDate } from '../utils/formatters';
import { downloadCsv, printDocument } from '../utils/exportUtils';
import {
  ArrowLeft,
  Printer,
  Download,
  PlusCircle,
  Phone,
  Mail,
  MapPin,
  Calendar,
  AlertCircle,
  CheckCircle2,
  TrendingUp,
  TrendingDown,
} from 'lucide-react';

export const CustomerLedgerPage = ({
  customerId,
  onBack,
  onOpenRecordTransaction,
  refreshTrigger,
}) => {
  const [ledger, setLedger] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const fetchLedger = async () => {
    try {
      setLoading(true);
      const params = {};
      if (startDate) params.startDate = new Date(startDate).toISOString();
      if (endDate) params.endDate = new Date(endDate).toISOString();

      const data = await customerService.getLedger(customerId, params);
      setLedger(data);
      setError('');
    } catch (err) {
      setError('Unable to load customer ledger.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (customerId) {
      fetchLedger();
    }
  }, [customerId, startDate, endDate, refreshTrigger]);

  const handleExportCsv = async () => {
    try {
      const params = {};
      if (startDate) params.startDate = new Date(startDate).toISOString();
      if (endDate) params.endDate = new Date(endDate).toISOString();

      const blobData = await customerService.exportLedgerCsv(customerId, params);
      const filename = `${ledger?.customerName || 'customer'}_ledger_${new Date().toISOString().slice(0, 10)}.csv`;
      
      // Convert blob to download
      const url = window.URL.createObjectURL(new Blob([blobData]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to export CSV');
    }
  };

  if (loading && !ledger) {
    return <LoadingSpinner text="Generating customer chronological ledger..." />;
  }

  if (error) {
    return (
      <div className="p-6 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl flex items-center justify-between">
        <span>{error}</span>
        <button
          onClick={onBack}
          className="px-3 py-1.5 bg-slate-800 text-white text-xs font-semibold rounded-lg"
        >
          Back to Customers
        </button>
      </div>
    );
  }

  const isDue = Number(ledger?.currentOutstandingBalance || 0) > 0;

  return (
    <div className="space-y-6">
      {/* Top Header & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 no-print">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h2 className="text-xl font-bold text-slate-900 tracking-tight">
              Customer Ledger: {ledger?.customerName}
            </h2>
            <p className="text-xs text-slate-500">Official statement of credit, debits, and running balance</p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={() => onOpenRecordTransaction('CREDIT_GIVEN', customerId)}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
          >
            <TrendingUp className="w-3.5 h-3.5" />
            <span>Give Credit</span>
          </button>

          <button
            onClick={() => onOpenRecordTransaction('PAYMENT_RECEIVED', customerId)}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
          >
            <TrendingDown className="w-3.5 h-3.5" />
            <span>Record Payment</span>
          </button>

          <button
            onClick={handleExportCsv}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export CSV</span>
          </button>

          <button
            onClick={printDocument}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print Ledger</span>
          </button>
        </div>
      </div>

      {/* Customer Profile & Financial Summary Card */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-sm print-card">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Customer Profile */}
          <div className="md:col-span-1 border-b md:border-b-0 md:border-r border-slate-100 pb-4 md:pb-0 md:pr-6">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Customer Profile</span>
            <h3 className="text-lg font-bold text-slate-900 mt-1">{ledger?.customerName}</h3>
            
            <div className="mt-3 space-y-1.5 text-xs text-slate-600">
              <div className="flex items-center gap-2">
                <Phone className="w-3.5 h-3.5 text-slate-400" />
                <span className="font-medium">{ledger?.customerMobile}</span>
              </div>
              {ledger?.customerEmail && (
                <div className="flex items-center gap-2">
                  <Mail className="w-3.5 h-3.5 text-slate-400" />
                  <span>{ledger.customerEmail}</span>
                </div>
              )}
              {ledger?.customerAddress && (
                <div className="flex items-center gap-2">
                  <MapPin className="w-3.5 h-3.5 text-slate-400" />
                  <span>{ledger.customerAddress}</span>
                </div>
              )}
            </div>
          </div>

          {/* Account Balances Grid */}
          <div className="md:col-span-2 grid grid-cols-2 sm:grid-cols-4 gap-4 items-center">
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
              <span className="text-[11px] font-medium text-slate-500 uppercase block">Opening Balance</span>
              <span className="text-base font-bold text-slate-800 mt-0.5 block">
                {formatCurrency(ledger?.openingBalance)}
              </span>
            </div>

            <div className="p-3 bg-sky-50 rounded-xl border border-sky-100">
              <span className="text-[11px] font-medium text-sky-700 uppercase block">Total Credit Lent</span>
              <span className="text-base font-bold text-sky-900 mt-0.5 block">
                {formatCurrency(ledger?.totalCredit)}
              </span>
            </div>

            <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-100">
              <span className="text-[11px] font-medium text-emerald-700 uppercase block">Total Repaid</span>
              <span className="text-base font-bold text-emerald-900 mt-0.5 block">
                {formatCurrency(ledger?.totalPaid)}
              </span>
            </div>

            <div className={`p-3 rounded-xl border ${isDue ? 'bg-amber-50 border-amber-200' : 'bg-slate-50 border-slate-200'}`}>
              <span className={`text-[11px] font-bold uppercase block ${isDue ? 'text-amber-700' : 'text-slate-600'}`}>
                Current Outstanding Due
              </span>
              <span className={`text-lg font-extrabold mt-0.5 block ${isDue ? 'text-amber-800' : 'text-slate-800'}`}>
                {formatCurrency(ledger?.currentOutstandingBalance)}
              </span>
            </div>
          </div>
        </div>

        {/* Date Filter Bar */}
        <div className="mt-5 pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3 no-print">
          <div className="flex flex-wrap items-center gap-3 text-xs">
            <span className="font-semibold text-slate-500 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" /> Date Range:
            </span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="px-2.5 py-1.5 border border-slate-300 rounded-lg text-slate-700 focus:outline-none focus:ring-1 focus:ring-sky-500"
            />
            <span className="text-slate-400">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="px-2.5 py-1.5 border border-slate-300 rounded-lg text-slate-700 focus:outline-none focus:ring-1 focus:ring-sky-500"
            />
            {(startDate || endDate) && (
              <button
                onClick={() => { setStartDate(''); setEndDate(''); }}
                className="text-xs text-sky-600 hover:underline font-medium"
              >
                Reset
              </button>
            )}
          </div>

          <div className="text-xs text-slate-500 font-medium">
            Total Ledger Entries: {ledger?.entries?.length || 0}
          </div>
        </div>
      </div>

      {/* Chronological Ledger Table with Running Balance */}
      <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden print-card">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-50/90 text-slate-600 font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Date & Time</th>
                <th className="py-3 px-4">Transaction Type</th>
                <th className="py-3 px-4">Description / Reference</th>
                <th className="py-3 px-4">Method</th>
                <th className="py-3 px-4 text-right">Credit Lent (+)</th>
                <th className="py-3 px-4 text-right">Payment Received (-)</th>
                <th className="py-3 px-4 text-right">Running Balance</th>
                <th className="py-3 px-4 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {/* Row for Opening Balance */}
              <tr className="bg-slate-50/40 italic text-slate-500">
                <td className="py-2.5 px-4">-</td>
                <td className="py-2.5 px-4 font-semibold text-slate-700">OPENING BALANCE</td>
                <td className="py-2.5 px-4">Starting balance brought forward</td>
                <td className="py-2.5 px-4">-</td>
                <td className="py-2.5 px-4 text-right">-</td>
                <td className="py-2.5 px-4 text-right">-</td>
                <td className="py-2.5 px-4 text-right font-bold text-slate-700">
                  {formatCurrency(ledger?.openingBalance)}
                </td>
                <td className="py-2.5 px-4 text-center">-</td>
              </tr>

              {ledger?.entries && ledger.entries.length > 0 ? (
                ledger.entries.map((entry) => {
                  const hasCredit = Number(entry.creditAmount || 0) > 0;
                  const hasPayment = Number(entry.paymentAmount || 0) > 0;
                  return (
                    <tr
                      key={entry.id}
                      className={`hover:bg-slate-50/70 transition-colors ${
                        entry.reversed ? 'bg-rose-50/30 line-through text-slate-400' : ''
                      }`}
                    >
                      <td className="py-3 px-4 text-slate-600 font-medium whitespace-nowrap">
                        {formatDateTime(entry.transactionDate)}
                      </td>
                      <td className="py-3 px-4">
                        <Badge type={entry.transactionType} />
                      </td>
                      <td className="py-3 px-4 text-slate-700">
                        <span className="font-medium block">{entry.description || '-'}</span>
                        {entry.referenceNumber && (
                          <span className="text-[11px] text-slate-400 font-mono">
                            Ref: {entry.referenceNumber}
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-slate-600">{entry.paymentMethod}</td>

                      {/* Credit Lent column */}
                      <td className="py-3 px-4 text-right font-semibold text-sky-700">
                        {hasCredit ? `+ ${formatCurrency(entry.creditAmount)}` : '-'}
                      </td>

                      {/* Payment Received column */}
                      <td className="py-3 px-4 text-right font-semibold text-emerald-700">
                        {hasPayment ? `- ${formatCurrency(entry.paymentAmount)}` : '-'}
                      </td>

                      {/* Running Balance Column */}
                      <td className="py-3 px-4 text-right font-bold text-slate-900 text-sm">
                        {formatCurrency(entry.runningBalance)}
                      </td>

                      <td className="py-3 px-4 text-center">
                        <Badge
                          type={entry.reversed ? 'REVERSED' : 'ACTIVE'}
                          text={entry.reversed ? 'REVERSED' : 'ACTIVE'}
                          size="xs"
                        />
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={8} className="py-8 text-center text-slate-400">
                    No transactions recorded for this customer yet.
                  </td>
                </tr>
              )}
            </tbody>
            {/* Table Footer with Ledger Grand Totals */}
            <tfoot>
              <tr className="bg-slate-100/80 font-bold border-t border-slate-300 text-slate-900">
                <td colSpan={4} className="py-3 px-4 uppercase text-slate-700">Grand Total</td>
                <td className="py-3 px-4 text-right text-sky-800">
                  {formatCurrency(ledger?.totalCredit)}
                </td>
                <td className="py-3 px-4 text-right text-emerald-800">
                  {formatCurrency(ledger?.totalPaid)}
                </td>
                <td className="py-3 px-4 text-right text-base text-amber-800">
                  {formatCurrency(ledger?.currentOutstandingBalance)}
                </td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  );
};
