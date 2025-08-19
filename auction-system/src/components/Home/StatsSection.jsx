import React from "react";

const StatsSection = ({ stats }) => {
    return (
        <section className="stats-section py-5 bg-light">
            <div className="container">
                <div className="row text-center">
                    <div className="col-md-3">
                        <h2 className="display-4 fw-bold text-primary">{stats.activeUsers.toLocaleString()}</h2>
                        <p className="text-muted">Active Users</p>
                    </div>
                    <div className="col-md-3">
                        <h2 className="display-4 fw-bold text-success">{stats.liveAuctions}</h2>
                        <p className="text-muted">Live Auctions</p>
                    </div>
                    <div className="col-md-3">
                        <h2 className="display-4 fw-bold text-warning">{stats.totalSales}</h2>
                        <p className="text-muted">Total Sales</p>
                    </div>
                    <div className="col-md-3">
                        <h2 className="display-4 fw-bold text-info">{stats.satisfactionRate}</h2>
                        <p className="text-muted">Satisfaction Rate</p>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default StatsSection;