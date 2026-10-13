import React, { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { customerService } from '../services/customerService';

export const CustomerModal = ({ isOpen, onClose, customerToEdit, onSuccess }) => {
  const [formData, setFormData] = useState({
    fullName: '',
    mobile: '',
    email: '',
    address: '',
    notes: '',
    status: 'ACTIVE',
    openingBalance: '0',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (customerToEdit) {
      setFormData({
        fullName: customerToEdit.fullName || '',
        mobile: customerToEdit.mobile || '',
        email: customerToEdit.email || '',
        address: customerToEdit.address || '',
        notes: customerToEdit.notes || '',
        status: customerToEdit.status || 'ACTIVE',
        openingBalance: customerToEdit.openingBalance ? String(customerToEdit.openingBalance) : '0',
      });
    } else {
      setFormData({
        fullName: '',
        mobile: '',
        email: '',
        address: '',
        notes: '',
        status: 'ACTIVE',
        openingBalance: '0',
      });
    }
    setError('');
  }, [customerToEdit, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.fullName.trim()) {
      setError('Customer name is required');
      return;
    }
    if (!formData.mobile.trim()) {
      setError('Mobile number is required');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        fullName: formData.fullName.trim(),
        mobile: formData.mobile.trim(),
        email: formData.email.trim() || null,
        address: formData.address.trim() || null,
        notes: formData.notes.trim() || null,
        status: formData.status,
        openingBalance: parseFloat(formData.openingBalance || '0'),
      };

      if (customerToEdit) {
        await customerService.update(customerToEdit.id, payload);
      } else {
        await customerService.create(payload);
      }

      onSuccess(customerToEdit ? 'Customer updated successfully' : 'Customer added successfully');
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Failed to save customer';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={customerToEdit ? 'Edit Customer Information' : 'Add New Customer'}
      maxWidth="max-w-md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">
            {error}
          </div>
        )}

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Customer Full Name <span className="text-rose-500">*</span>
          </label>
          <input
            type="text"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            placeholder="e.g. Ramesh Patel"
            required
            className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Mobile Number <span className="text-rose-500">*</span>
          </label>
          <input
            type="tel"
            name="mobile"
            value={formData.mobile}
            onChange={handleChange}
            placeholder="e.g. +91 9876543210"
            required
            className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="name@example.com"
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Account Status</label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 bg-white"
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
        </div>

        {!customerToEdit && (
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Opening Balance (₹)
            </label>
            <input
              type="number"
              step="0.01"
              name="openingBalance"
              value={formData.openingBalance}
              onChange={handleChange}
              placeholder="0.00"
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
            />
            <p className="text-[11px] text-slate-400 mt-0.5">Prior balance before entering digital transactions</p>
          </div>
        )}

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Address / Shop Details</label>
          <input
            type="text"
            name="address"
            value={formData.address}
            onChange={handleChange}
            placeholder="e.g. Shop 12, Market Yard"
            className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Notes</label>
          <textarea
            name="notes"
            value={formData.notes}
            onChange={handleChange}
            rows={2}
            placeholder="e.g. Regular monthly customer, prefers UPI"
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
            className="px-4 py-2 text-sm font-medium text-white bg-sky-600 hover:bg-sky-700 rounded-lg shadow-sm transition-colors disabled:opacity-70"
          >
            {loading ? 'Saving...' : customerToEdit ? 'Save Changes' : 'Create Customer'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
