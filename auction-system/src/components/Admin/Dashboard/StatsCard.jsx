import React from 'react';

const StatsCard = ({ title, value, trend, icon, gradient, onClick }) => {
    return (
        <div className="col-xl-3 col-md-6 mb-4">
            <div
                className="card text-white p-4 border-0 rounded-4 cursor-pointer"
                style={{ background: gradient, transition: 'all 0.3s ease' }}
                onClick={onClick}
                onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
                onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
            >
                <div className="d-flex justify-content-between align-items-center">
                    <div>
                        <p className="mb-1 opacity-75">{title}</p>
                        <h2 className="mb-1">{value}</h2>
                        <small className="opacity-75">{trend}</small>
                    </div>
                    <i className={`${icon} fa-2x opacity-75`}></i>
                </div>
            </div>
        </div>
    );
};

export default StatsCard;