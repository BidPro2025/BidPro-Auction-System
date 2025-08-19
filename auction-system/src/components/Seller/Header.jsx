import React from 'react';
import StatsCard from './Common/StatsCard';

const Header = ({ toggleModal }) => {
    return (
        <div className="row mb-4">
            <div className="col-12">
                <div className="d-flex justify-content-between align-items-center">
                    <div>
                        <h2 className="text-primary mb-1">
                            <i className="bi bi-shop"></i> Seller Dashboard
                        </h2>
                        <p className="text-muted mb-0">Manage your auction listings and track performance</p>
                    </div>
                    <div>
                        <button
                            className="btn btn-outline-primary me-2"
                            onClick={() => toggleModal('notifications', true)}
                        >
                            <i className="bi bi-bell"></i> Notifications
                        </button>
                        <button
                            className="btn btn-custom"
                            onClick={() => toggleModal('profile', true)}
                        >
                            <i className="bi bi-person-circle"></i> Profile
                        </button>
                    </div>
                </div>
            </div>
            <div className="row mt-4">
                <StatsCard icon="bi bi-box-seam" value="24" label="Active Listings" />
                <StatsCard icon="bi bi-currency-dollar" value="$5,420" label="Total Sales" />
                <StatsCard icon="bi bi-people" value="187" label="Total Bidders" />
                <StatsCard icon="bi bi-trophy" value="92%" label="Success Rate" />
            </div>
        </div>
    );
};

export default Header;

