import React, { useState, useEffect } from 'react';
import { reportService } from '../services/reportService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { Modal } from '../components/Modal';
import { formatCurrency, formatDate, formatTimeAgo } from '../utils/formatters';
import {
  BellRing,
  Copy,
  Check,
  Send,
  MessageCircle,
  Clock,
  ArrowUpDown,
  BookOpen,
  CheckCircle2,
} from 'lucide-react';

export const PendingPaymentsPage = ({ onNavigateToCustomerLedger, onOpenRecordTransaction }) => {
  const [pendingList, setPendingList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sortBy, setSortBy] = useState('balance');
  const [activeReminder, setActiveReminder] = useState(null);
  const [copiedId, setCopiedId] = useState(null);

  const fetchPending = async () => {
    try {
      setLoading(true);
      const data = await reportService.getPendingPayments({ sortBy });
      setPendingList(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, [sortBy]);

  const handleCopyMessage = (text, customerId) => {
    navigator.clipboard.writeText(text);
    setCopiedId(customerId);
    setTimeout(() => setCopiedId(null), 2500);
  };

  const handleWhatsAppShare = (mobile, message) => {
    // Sanitize mobile number
    const cleanNumber = mobile.replace(/[^0-9]/g, '');
    const url = `https://wa.me/${cleanNumber}?text=${encodeURIComponent(message)}`;
    window.open(url, '_blank');
  };

  // Grand total of pending dues
  const totalPendingDue = pendingList.reduce(
    (sum, item) => sum + Number(item.outstandingBalance || 0),
    0
  );

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">
            Pending Dues & Payment Reminders
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Customers with unpaid balances. Generate ready-to-send payment reminder notices.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-500 font-medium">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="px-3 py-2 text-xs border border-slate-300 rounded-lg bg-white text-slate-700 font-semibold focus:outline-none focus:ring-2 focus:ring-sky-500/20"
          >
            <option value="balance">Highest Balance First</option>
            <option value="oldest">Oldest Transaction Date</option>
          </select>
        </div>
      </div>

      {/* Summary Banner */}
      <div className="bg-amber-50 border border-amber-200 rounded-xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-700 flex-shrink-0">
            <BellRing className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-amber-800">
              Total Outstanding Receivables
            </span>
            <h3 className="text-2xl font-black text-amber-900 tracking-tight">
              {formatCurrency(totalPendingDue)}
            </h3>
          </div>
        </div>

        <div className="text-xs font-semibold text-amber-800 bg-amber-100/70 px-3 py-2 rounded-lg">
          {pendingList.length} Customer{pendingList.length === 1 ? '' : 's'} Pending Settlement
        </div>
      </div>

      {/* Debtor Cards / Table */}
      {loading ? (
        <LoadingSpinner text="Analyzing unpaid debtor ledger accounts..." />
      ) : pendingList.length === 0 ? (
        <EmptyState
          icon={CheckCircle2}
          title="All Customer Accounts Are Cleared!"
          description="There are currently zero overdue or pending customer balances."
        />
      ) : (
        <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50/80 text-slate-500 font-semibold border-b border-slate-100">
                  <th className="py-3 px-4">Customer Name</th>
                  <th className="py-3 px-4">Contact Phone</th>
                  <th className="py-3 px-4">Last Activity</th>
                  <th className="py-3 px-4">Outstanding Due</th>
                  <th className="py-3 px-4 text-center">Quick Reminder</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {pendingList.map((item) => (
                  <tr key={item.customerId} className="hover:bg-slate-50/60 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-slate-900 text-sm">
                      <button
                        onClick={() => onNavigateToCustomerLedger(item.customerId)}
                        className="hover:text-sky-600 transition-colors text-left"
                      >
                        {item.customerName}
                      </button>
                    </td>

                    <td className="py-3.5 px-4 text-slate-600 font-medium">{item.mobile}</td>

                    <td className="py-3.5 px-4 text-slate-500">
                      <div>{formatDate(item.lastTransactionDate)}</div>
                      <span className="text-[10px] text-slate-400">
                        {formatTimeAgo(item.lastTransactionDate)}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 font-black text-amber-700 text-sm">
                      {formatCurrency(item.outstandingBalance)}
                    </td>

                    <td className="py-3.5 px-4 text-center">
                      <button
                        onClick={() => setActiveReminder(item)}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-sky-50 text-sky-700 hover:bg-sky-100 border border-sky-200 rounded-lg font-medium text-xs transition-colors"
                      >
                        <MessageCircle className="w-3.5 h-3.5" />
                        <span>Preview Reminder</span>
                      </button>
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleCopyMessage(item.reminderMessage, item.customerId)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 text-slate-600 hover:bg-slate-100 border border-slate-200 rounded-lg text-xs transition-colors"
                          title="Copy notice text"
                        >
                          {copiedId === item.customerId ? (
                            <>
                              <Check className="w-3.5 h-3.5 text-emerald-600" />
                              <span className="text-emerald-600 font-bold">Copied!</span>
                            </>
                          ) : (
                            <>
                              <Copy className="w-3.5 h-3.5" />
                              <span>Copy</span>
                            </>
                          )}
                        </button>

                        <button
                          onClick={() => handleWhatsAppShare(item.mobile, item.reminderMessage)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold transition-colors shadow-sm"
                          title="Send reminder via WhatsApp Web"
                        >
                          <Send className="w-3 h-3" />
                          <span>WhatsApp</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Reminder Message Preview Modal */}
      {activeReminder && (
        <Modal
          isOpen={!!activeReminder}
          onClose={() => setActiveReminder(null)}
          title={`Payment Reminder Notice: ${activeReminder.customerName}`}
          maxWidth="max-w-md"
        >
          <div className="space-y-4">
            <p className="text-xs text-slate-500">
              Prepared message containing customer identity and exact outstanding balance.
            </p>

            <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl font-mono text-xs text-slate-800 leading-relaxed select-all">
              {activeReminder.reminderMessage}
            </div>

            <div className="flex items-center justify-between pt-3 border-t border-slate-100">
              <button
                onClick={() => handleCopyMessage(activeReminder.reminderMessage, activeReminder.customerId)}
                className="inline-flex items-center gap-1.5 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
              >
                <Copy className="w-4 h-4" />
                <span>
                  {copiedId === activeReminder.customerId ? 'Copied to Clipboard!' : 'Copy Notice Text'}
                </span>
              </button>

              <button
                onClick={() => handleWhatsAppShare(activeReminder.mobile, activeReminder.reminderMessage)}
                className="inline-flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg transition-colors shadow-sm"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Open in WhatsApp</span>
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
