import React, { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { transactionService } from '../services/transactionService';
import { customerService } from '../services/customerService';
import { formatCurrency } from '../utils/formatters';

export const TransactionModal = ({
  isOpen,
  onClose,
  defaultCustomerId,
  defaultType = 'CREDIT_GIVEN',
  onSuccess,
}) => {
  const [customers, setCustomers] = useState([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState(defaultCustomerId || '');
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [transactionType, setTransactionType] = useState(defaultType);
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [referenceNumber, setReferenceNumber] = useState('');
  const [transactionDate, setTransactionDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Fetch customers list for dropdown
  useEffect(() => {
    if (isOpen) {
      customerService.getAll().then((data) => {
        setCustomers(data);
        if (defaultCustomerId) {
          const match = data.find((c) => String(c.id) === String(defaultCustomerId));
          if (match) setSelectedCustomer(match);
        }
      }).catch(() => {});
      setTransactionType(defaultType);
      setSelectedCustomerId(defaultCustomerId || '');
      setAmount('');
      setDescription('');
      setPaymentMethod('CASH');
      setReferenceNumber('');
      setTransactionDate(new Date().toISOString().slice(0, 16));
      setError('');
    }
  }, [isOpen, defaultCustomerId, defaultType]);

  const handleCustomerChange = (e) => {
    const id = e.target.value;
    setSelectedCustomerId(id);
    const match = customers.find((c) => String(c.id) === String(id));
    setSelectedCustomer(match || null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!selectedCustomerId) {
      setError('Please select a customer');
      return;
    }
    const numAmount = parseFloat(amount);
    if (isNaN(numAmount) || numAmount <= 0) {
      setError('Amount must be greater than zero');
      return;
    }

    setLoading(true);
    try {
      await transactionService.create({
        customerId: Number(selectedCustomerId),
        transactionType,
        amount: numAmount,
        transactionDate: transactionDate ? new Date(transactionDate).toISOString() : new Date().toISOString(),
        description: description.trim() || null,
        paymentMethod,
        referenceNumber: referenceNumber.trim() || null,
      });

      onSuccess('Transaction recorded successfully');
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Failed to record transaction';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Record Financial Transaction"
      maxWidth="max-w-lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">
            {error}
          </div>
        )}

        {/* Transaction Type Tabs */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1.5">
            Transaction Type <span className="text-rose-500">*</span>
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => setTransactionType('CREDIT_GIVEN')}
              className={`p-2.5 text-xs font-semibold rounded-lg border text-center transition-all ${
                transactionType === 'CREDIT_GIVEN'
                  ? 'bg-sky-50 border-sky-500 text-sky-700 ring-2 ring-sky-500/20'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              <div className="font-bold">Credit Given (Lent)</div>
              <div className="text-[10px] font-normal opacity-80">Customer owes you</div>
            </button>

            <button
              type="button"
              onClick={() => setTransactionType('PAYMENT_RECEIVED')}
              className={`p-2.5 text-xs font-semibold rounded-lg border text-center transition-all ${
                transactionType === 'PAYMENT_RECEIVED'
                  ? 'bg-emerald-50 border-emerald-500 text-emerald-700 ring-2 ring-emerald-500/20'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              <div className="font-bold">Payment Received</div>
              <div className="text-[10px] font-normal opacity-80">Customer repays you</div>
            </button>

            <button
              type="button"
              onClick={() => setTransactionType('CREDIT_BORROWED')}
              className={`p-2.5 text-xs font-semibold rounded-lg border text-center transition-all ${
                transactionType === 'CREDIT_BORROWED'
                  ? 'bg-purple-50 border-purple-500 text-purple-700 ring-2 ring-purple-500/20'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              <div className="font-bold">Credit Borrowed</div>
              <div className="text-[10px] font-normal opacity-80">You borrowed from party</div>
            </button>

            <button
              type="button"
              onClick={() => setTransactionType('PAYMENT_MADE')}
              className={`p-2.5 text-xs font-semibold rounded-lg border text-center transition-all ${
                transactionType === 'PAYMENT_MADE'
                  ? 'bg-indigo-50 border-indigo-500 text-indigo-700 ring-2 ring-indigo-500/20'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              <div className="font-bold">Payment Made</div>
              <div className="text-[10px] font-normal opacity-80">You repaid lender</div>
            </button>
          </div>
        </div>

        {/* Customer Select */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Customer / Party <span className="text-rose-500">*</span>
          </label>
          <select
            value={selectedCustomerId}
            onChange={handleCustomerChange}
            required
            className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 bg-white"
          >
            <option value="">-- Select Customer --</option>
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.fullName} ({c.mobile})
              </option>
            ))}
          </select>

          {/* Current balance display */}
          {selectedCustomer && (
            <div className="mt-2 p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-xs flex justify-between items-center">
              <span className="text-slate-500">Current Outstanding Due:</span>
              <span className="font-bold text-slate-900">
                {formatCurrency(selectedCustomer.outstandingBalance)}
              </span>
            </div>
          )}
        </div>

        {/* Amount & Date */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Amount (₹) <span className="text-rose-500">*</span>
            </label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0.00"
              required
              className="w-full px-3 py-2 text-sm font-semibold border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Date & Time</label>
            <input
              type="datetime-local"
              value={transactionDate}
              onChange={(e) => setTransactionDate(e.target.value)}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 bg-white"
            />
          </div>
        </div>

        {/* Payment Method & Reference */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Payment Method</label>
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 bg-white"
            >
              <option value="CASH">CASH</option>
              <option value="UPI">UPI (GPay / PhonePe / Paytm)</option>
              <option value="BANK_TRANSFER">Bank Transfer (NEFT/IMPS)</option>
              <option value="CHEQUE">Cheque</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Reference Number</label>
            <input
              type="text"
              value={referenceNumber}
              onChange={(e) => setReferenceNumber(e.target.value)}
              placeholder="e.g. UPI-994821 or CHQ-104"
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
            />
          </div>
        </div>

        {/* Description / Notes */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Description / Bill Details</label>
          <input
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Groceries monthly bill, cement bags, etc."
            className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
          />
        </div>

        <div className="flex justify-end gap-3 pt-3 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-5 py-2 text-sm font-medium text-white bg-sky-600 hover:bg-sky-700 rounded-lg shadow-sm transition-colors disabled:opacity-70"
          >
            {loading ? 'Recording...' : 'Record Transaction'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
