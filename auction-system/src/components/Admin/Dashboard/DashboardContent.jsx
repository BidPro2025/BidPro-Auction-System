import React, { useState, useEffect } from 'react';
import StatsCard from './StatsCard';
import RecentActivity from './RecentActivity';

const DashboardContent = ({ showTab, handleAction }) => {
    const [dashboardStats, setDashboardStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                // Replace with actual API call
                // const response = await fetch('/api/dashboard/stats');
                // const data = await response.json();
                // setDashboardStats(data);

                // For now, setting null to indicate no data
                setDashboardStats(null);
                setLoading(false);
            } catch (err) {
                setError(err.message);
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '300px' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="alert alert-danger">
                <i className="fas fa-exclamation-triangle me-2"></i>
                {error}
            </div>
        );
    }

    return (
        <div>
            {/* Stats Cards */}
            <div className="row mb-4">
                <StatsCard
                    title="Total Users"
                    value={dashboardStats?.totalUsers || "0"}
                    trend={dashboardStats?.userTrend || "No data"}
                    icon="fas fa-users"
                    gradient="linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                    onClick={() => showTab('users')}
                />
                <StatsCard
                    title="Active Auctions"
                    value={dashboardStats?.activeAuctions || "0"}
                    trend={dashboardStats?.auctionTrend || "No data"}
                    icon="fas fa-chart-line"
                    gradient="linear-gradient(135deg, #f093fb 0%, #f5576c 100%)"
                    onClick={() => showTab('bids')}
                />
                <StatsCard
                    title="Total Bids"
                    value={dashboardStats?.totalBids || "0"}
                    trend={dashboardStats?.bidTrend || "No data"}
                    icon="fas fa-chart-bar"
                    gradient="linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
                    onClick={() => showTab('bids')}
                />
                <StatsCard
                    title="Revenue (₹)"
                    value={dashboardStats?.revenue || "₹0"}
                    trend={dashboardStats?.revenueTrend || "No data"}
                    icon="fas fa-dollar-sign"
                    gradient="linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
                    onClick={() => showTab('reports')}
                />
            </div>

            {/* Recent Activity */}
            <div className="row">
                <RecentActivity type="users" handleAction={handleAction} showTab={showTab} />
                <RecentActivity type="auctions" handleAction={handleAction} showTab={showTab} />
            </div>
        </div>
    );
};

export default DashboardContent;
