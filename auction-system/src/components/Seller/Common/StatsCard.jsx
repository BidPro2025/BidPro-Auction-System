import React from 'react';

const StatsCard = ({ icon, value, label }) => {
    return (
        <div className="col-md-3 mb-3">
            <div className="card stats-card">
                <div className="card-body text-center">
                    <i className={`${icon} fs-1 mb-2`}></i>
                    <h4 className="mb-1">{value !== undefined ? value : 'Loading...'}</h4>
                    <p className="mb-0">{label}</p>
                </div>
            </div>
        </div>
    );
};

export default StatsCard;