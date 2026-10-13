import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('creditbook_token');
      const storedUser = localStorage.getItem('creditbook_user');

      if (storedToken && storedUser) {
        try {
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
          // Refresh profile in background
          const me = await authService.getCurrentUser();
          setUser((prev) => ({ ...prev, ...me }));
        } catch (e) {
          // Token may be invalid/expired
          localStorage.removeItem('creditbook_token');
          localStorage.removeItem('creditbook_user');
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    };

    initAuth();

    // Listen for 401 session expiry
    const handleAuthExpired = () => {
      setToken(null);
      setUser(null);
    };
    window.addEventListener('auth_expired', handleAuthExpired);
    return () => window.removeEventListener('auth_expired', handleAuthExpired);
  }, []);

  const login = async (email, password) => {
    const data = await authService.login(email, password);
    setToken(data.token);
    setUser({
      id: data.id,
      fullName: data.fullName,
      email: data.email,
      businessName: data.businessName,
    });
    return data;
  };

  const register = async (formData) => {
    const data = await authService.register(formData);
    setToken(data.token);
    setUser({
      id: data.id,
      fullName: data.fullName,
      email: data.email,
      businessName: data.businessName,
    });
    return data;
  };

  const logout = async () => {
    await authService.logout();
    setToken(null);
    setUser(null);
  };

  const updateUser = (updatedFields) => {
    setUser((prev) => {
      const updated = { ...prev, ...updatedFields };
      localStorage.setItem('creditbook_user', JSON.stringify(updated));
      return updated;
    });
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        loading,
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
