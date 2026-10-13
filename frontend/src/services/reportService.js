import api from './api';

export const reportService = {
  getDashboardSummary: async () => {
    const response = await api.get('/dashboard/summary');
    return response.data;
  },

  getReportSummary: async (params = {}) => {
    const response = await api.get('/reports/summary', { params });
    return response.data;
  },

  getPendingPayments: async (params = {}) => {
    const response = await api.get('/reports/pending', { params });
    return response.data;
  },

  exportTransactionsCsv: async (params = {}) => {
    const response = await api.get('/reports/export/transactions/csv', {
      params,
      responseType: 'blob',
    });
    return response.data;
  },

  exportCustomersCsv: async () => {
    const response = await api.get('/reports/export/customers/csv', {
      responseType: 'blob',
    });
    return response.data;
  },

  getAuditLogs: async () => {
    const response = await api.get('/audit-logs');
    return response.data;
  },
};
