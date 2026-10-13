import api from './api';

export const customerService = {
  getAll: async (params = {}) => {
    const response = await api.get('/customers', { params });
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/customers/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await api.post('/customers', data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/customers/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    const response = await api.delete(`/customers/${id}`);
    return response.data;
  },

  getLedger: async (customerId, params = {}) => {
    const response = await api.get(`/customers/${customerId}/ledger`, { params });
    return response.data;
  },

  exportLedgerCsv: async (customerId, params = {}) => {
    const response = await api.get(`/customers/${customerId}/ledger/export/csv`, {
      params,
      responseType: 'blob',
    });
    return response.data;
  },
};
