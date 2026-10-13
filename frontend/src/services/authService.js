import api from './api';

export const authService = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    if (response.data.token) {
      localStorage.setItem('creditbook_token', response.data.token);
      localStorage.setItem('creditbook_user', JSON.stringify(response.data));
    }
    return response.data;
  },

  register: async (data) => {
    const response = await api.post('/auth/register', data);
    if (response.data.token) {
      localStorage.setItem('creditbook_token', response.data.token);
      localStorage.setItem('creditbook_user', JSON.stringify(response.data));
    }
    return response.data;
  },

  logout: async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignore network errors on logout
    } finally {
      localStorage.removeItem('creditbook_token');
      localStorage.removeItem('creditbook_user');
    }
  },

  getCurrentUser: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },

  updateProfile: async (data) => {
    const response = await api.put('/user/profile', data);
    return response.data;
  },

  changePassword: async (currentPassword, newPassword) => {
    const response = await api.post('/user/password', { currentPassword, newPassword });
    return response.data;
  },
};
