import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { apiCall } from './utils/api';
import Login from './components/Login/login';
import BidProAdminDashboard from './components/Admin/BidProAdminDashboard';
import SellerDashboard from './components/Seller/SellerDashboard';
import BidderDashboard from './components/Bidder/BidderDashboard';
import HomeDashboard from './components/Home/HomeDashboard';
import './styles/globals.css';
import Dashboard from './components/Admin/Dashboard/DashboardContent';
import Register from './components/Register/Register';
import Auctions from './components/Home/Pages/Auction';
import Categories from './components/Home/Pages/Categories';
import Help from './components/Home/Pages/Help';
import Term from './components/Home/Pages/Term';
import ForgotPassword from './components/Login/ForgotPassword';

const App = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      apiCall('/auth/validate', { method: 'GET' })
        .then((response) => {
          if (!response.data.success) {
            console.warn('Token validation failed:', response.data.message);
            localStorage.removeItem('token');
            setIsAuthenticated(false);
          }
        })
        .catch((err) => {
          console.warn('Token validation error:', err);
          localStorage.removeItem('token');
          setIsAuthenticated(false);
        });
    }
  }, []);

  const handleLogin = () => {
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
  };

  const ProtectedRoute = ({ children }) => {
    if (!isAuthenticated) {
      return <Navigate to="/login" replace />;
    }
    return children;
  };

  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomeDashboard apiCall={apiCall} />} />
        <Route path="/login" element={<Login onLogin={handleLogin} apiCall={apiCall} />} />
        <Route path="/register" element={<Register apiCall={apiCall} />} />
        <Route path="/terms" element={<Term apiCall={apiCall} />} />
        <Route path="/auction" element={<Auctions apiCall={apiCall} />} />
        <Route path="/categories" element={<Categories apiCall={apiCall} />} />
        <Route path="/help" element={<Help apiCall={apiCall} />} />
        <Route path="/forgot-password" element={<ForgotPassword apiCall={apiCall} />} />
        <Route
          path="/admin"
          element={
            <ProtectedRoute>
              <BidProAdminDashboard apiCall={apiCall} onLogout={handleLogout} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/seller"
          element={
            <ProtectedRoute>
              <SellerDashboard apiCall={apiCall} onLogout={handleLogout} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bidder"
          element={
            <ProtectedRoute>
              <BidderDashboard apiCall={apiCall} onLogout={handleLogout} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard apiCall={apiCall} onLogout={handleLogout} />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
};

export default App;