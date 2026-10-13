import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import { User, Lock, Building, Phone, Mail, CheckCircle, Shield, Database } from 'lucide-react';

export const SettingsPage = () => {
  const { user, updateUser } = useAuth();

  const [profileData, setProfileData] = useState({
    fullName: '',
    businessName: '',
    phone: '',
    email: '',
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [profileLoading, setProfileLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [profileMsg, setProfileMsg] = useState({ text: '', type: '' });
  const [passwordMsg, setPasswordMsg] = useState({ text: '', type: '' });

  useEffect(() => {
    if (user) {
      setProfileData({
        fullName: user.fullName || '',
        businessName: user.businessName || '',
        phone: user.phone || '',
        email: user.email || '',
      });
    }
  }, [user]);

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileMsg({ text: '', type: '' });
    setProfileLoading(true);

    try {
      const updated = await authService.updateProfile({
        fullName: profileData.fullName,
        businessName: profileData.businessName,
        phone: profileData.phone,
        email: profileData.email,
        password: 'dummy', // Not changed in this form
      });
      updateUser(updated);
      setProfileMsg({ text: 'Business profile updated successfully!', type: 'success' });
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update profile';
      setProfileMsg({ text: msg, type: 'error' });
    } finally {
      setProfileLoading(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordMsg({ text: '', type: '' });

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setPasswordMsg({ text: 'New passwords do not match', type: 'error' });
      return;
    }
    if (passwordData.newPassword.length < 6) {
      setPasswordMsg({ text: 'New password must be at least 6 characters', type: 'error' });
      return;
    }

    setPasswordLoading(true);
    try {
      await authService.changePassword(passwordData.currentPassword, passwordData.newPassword);
      setPasswordMsg({ text: 'Password changed successfully!', type: 'success' });
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      const msg = err.response?.data?.message || 'Current password is incorrect';
      setPasswordMsg({ text: msg, type: 'error' });
    } finally {
      setPasswordLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-xl font-bold text-slate-900 tracking-tight">Account & Merchant Settings</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Configure your business profile, security credentials, and system details
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Business Profile Card */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-sm">
          <div className="flex items-center gap-3 pb-4 border-b border-slate-100 mb-4">
            <div className="w-9 h-9 rounded-lg bg-sky-50 text-sky-600 flex items-center justify-center">
              <Building className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-800">Business Profile</h3>
              <p className="text-[11px] text-slate-400">Store and merchant contact details</p>
            </div>
          </div>

          {profileMsg.text && (
            <div
              className={`p-3 text-xs rounded-lg mb-4 border ${
                profileMsg.type === 'error'
                  ? 'bg-rose-50 border-rose-200 text-rose-700'
                  : 'bg-emerald-50 border-emerald-200 text-emerald-700'
              }`}
            >
              {profileMsg.text}
            </div>
          )}

          <form onSubmit={handleProfileSubmit} className="space-y-3.5">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
              <input
                type="text"
                value={profileData.fullName}
                onChange={(e) => setProfileData({ ...profileData, fullName: e.target.value })}
                required
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Business Name</label>
              <input
                type="text"
                value={profileData.businessName}
                onChange={(e) => setProfileData({ ...profileData, businessName: e.target.value })}
                placeholder="e.g. Sharma General Store"
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Phone Number</label>
              <input
                type="tel"
                value={profileData.phone}
                onChange={(e) => setProfileData({ ...profileData, phone: e.target.value })}
                placeholder="+91 9876543210"
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
              <input
                type="email"
                value={profileData.email}
                disabled
                className="w-full px-3 py-2 text-xs border border-slate-200 bg-slate-50 text-slate-500 rounded-lg"
              />
              <span className="text-[10px] text-slate-400">Account login email cannot be modified directly</span>
            </div>

            <button
              type="submit"
              disabled={profileLoading}
              className="w-full py-2 px-4 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors disabled:opacity-70"
            >
              {profileLoading ? 'Updating Profile...' : 'Save Profile Changes'}
            </button>
          </form>
        </div>

        {/* Change Password Card */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-sm">
          <div className="flex items-center gap-3 pb-4 border-b border-slate-100 mb-4">
            <div className="w-9 h-9 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-800">Change Password</h3>
              <p className="text-[11px] text-slate-400">BCrypt-hashed credentials security</p>
            </div>
          </div>

          {passwordMsg.text && (
            <div
              className={`p-3 text-xs rounded-lg mb-4 border ${
                passwordMsg.type === 'error'
                  ? 'bg-rose-50 border-rose-200 text-rose-700'
                  : 'bg-emerald-50 border-emerald-200 text-emerald-700'
              }`}
            >
              {passwordMsg.text}
            </div>
          )}

          <form onSubmit={handlePasswordSubmit} className="space-y-3.5">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Current Password</label>
              <input
                type="password"
                value={passwordData.currentPassword}
                onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                required
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">New Password</label>
              <input
                type="password"
                value={passwordData.newPassword}
                onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                required
                minLength={6}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Confirm New Password</label>
              <input
                type="password"
                value={passwordData.confirmPassword}
                onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                required
                minLength={6}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-sky-500/20"
              />
            </div>

            <button
              type="submit"
              disabled={passwordLoading}
              className="w-full py-2 px-4 bg-slate-800 hover:bg-slate-900 text-white text-xs font-semibold rounded-lg shadow-sm transition-colors disabled:opacity-70"
            >
              {passwordLoading ? 'Updating Password...' : 'Update Password'}
            </button>
          </form>
        </div>
      </div>

      {/* System Status Details */}
      <div className="bg-slate-50 rounded-xl border border-slate-200 p-5 text-xs text-slate-600">
        <div className="flex items-center gap-2 mb-3">
          <Database className="w-4 h-4 text-sky-600" />
          <h4 className="font-bold text-slate-800">Application Architecture & Environment Info</h4>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <span className="text-slate-400 block text-[11px]">Backend API</span>
            <span className="font-semibold text-slate-800">Java 21 • Spring Boot 3.3.4</span>
          </div>
          <div>
            <span className="text-slate-400 block text-[11px]">Database Layer</span>
            <span className="font-semibold text-slate-800">MySQL 8.0 / Hibernate JPA</span>
          </div>
          <div>
            <span className="text-slate-400 block text-[11px]">Security Provider</span>
            <span className="font-semibold text-slate-800">BCrypt (Cost 10) + JJWT 0.12</span>
          </div>
        </div>
      </div>
    </div>
  );
};
