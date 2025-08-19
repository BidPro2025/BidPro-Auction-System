import React from 'react';

const Sidebar = () => {
    return (
        <div className="sidebar">
            <h6 className="text-white mb-3">
                <i className="bi bi-info-circle"></i> Upload Guidelines
            </h6>
            <ul className="text-white-50 small">
                <li>Provide accurate product descriptions</li>
                <li>Use high-quality images</li>
                <li>Set reasonable starting prices</li>
                <li>Products require admin approval</li>
                <li>Follow community guidelines</li>
            </ul>
            <div className="mt-4">
                <h6 className="text-white mb-3">
                    <i className="bi bi-speedometer2"></i> Package Status
                </h6>
                <div className="bg-white p-3 rounded">
                    <small className="text-muted">Current Package: <strong>Premium</strong></small>
                    <div className="progress mt-2" style={{ height: '8px' }}>
                        <div
                            className="progress-bar"
                            style={{ width: '60%', background: 'linear-gradient(45deg, #667eea, #764ba2)' }}
                        ></div>
                    </div>
                    <small className="text-muted mt-1 d-block">12/20 listings used</small>
                </div>
            </div>
        </div>
    );
};

export default Sidebar;

