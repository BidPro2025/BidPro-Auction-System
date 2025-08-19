import React, { useState, useEffect } from 'react';

const RecentActivity = ({ type, handleAction, showTab }) => {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const isUsers = type === 'users';
    const title = isUsers ? 'Recent Users' : 'Active Auctions';
    const viewTab = isUsers ? 'users' : 'bids';

    useEffect(() => {
        const fetchRecentActivity = async () => {
            try {
                setLoading(true);
                // Replace with actual API call
                // const endpoint = isUsers ? '/api/users/recent' : '/api/auctions/active';
                // const response = await fetch(endpoint);
                // const result = await response.json();
                // setData(result);
                
                // For now, setting empty array
                setData([]);
                setLoading(false);
            } catch (err) {
                setError(err.message);
                setLoading(false);
            }
        };

        fetchRecentActivity();
    }, [type]);

    if (loading) {
        return (
            <div className="col-lg-6 mb-4">
                <div className="card h-100 border-0 shadow-sm rounded-4">
                    <div className="card-body d-flex justify-content-center align-items-center" style={{ height: '200px' }}>
                        <div className="spinner-border text-primary" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="col-lg-6 mb-4">
                <div className="card h-100 border-0 shadow-sm rounded-4">
                    <div className="card-body">
                        <div className="alert alert-danger">
                            <i className="fas fa-exclamation-triangle me-2"></i>
                            {error}
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="col-lg-6 mb-4">
            <div className="card h-100 border-0 shadow-sm rounded-4">
                <div className="card-header bg-white d-flex justify-content-between align-items-center border-0">
                    <h5 className="mb-0">{title}</h5>
                    <button className="btn btn-link btn-sm text-primary" onClick={() => showTab(viewTab)}>
                        View All
                    </button>
                </div>
                <div className="card-body">
                    {data.length > 0 ? (
                        data.map(item => (
                            <div
                                key={item.id}
                                className="bg-light rounded-3 p-3 mb-3 cursor-pointer"
                                style={{ transition: 'all 0.3s ease' }}
                                onClick={() => handleAction(isUsers ? 'User View' : 'Auction View', `Loading ${isUsers ? 'user profile' : 'auction details'} for ${item.name || item.title}`)}
                                onMouseOver={(e) => e.currentTarget.style.transform = 'translateX(5px)'}
                                onMouseOut={(e) => e.currentTarget.style.transform = 'translateX(0)'}
                            >
                                {isUsers ? (
                                    <div className="d-flex justify-content-between align-items-center">
                                        <div className="d-flex align-items-center">
                                            <div
                                                className={`rounded-circle d-flex align-items-center justify-content-center text-white fw-bold me-3 ${item.avatarBg || 'bg-primary'}`}
                                                style={{ width: '40px', height: '40px' }}
                                            >
                                                {item.avatar || item.name.charAt(0)}
                                            </div>
                                            <div>
                                                <h6 className="mb-0">{item.name}</h6>
                                                <small className="text-muted">{item.email || 'No email'}</small>
                                            </div>
                                        </div>
                                        <div className="text-end">
                                            <span className={`badge ${item.status === 'Active' ? 'bg-success' : 'bg-warning'}`}>
                                                {item.status || 'Unknown'}
                                            </span>
                                            <br />
                                            <small className="text-muted">{item.type || 'User'}</small>
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        <div className="d-flex justify-content-between align-items-start mb-2">
                                            <h6 className="mb-0">{item.title || 'Untitled Auction'}</h6>
                                            <span className="badge bg-success">{item.currentBid || 'No bids'}</span>
                                        </div>
                                        <div className="d-flex justify-content-between align-items-center">
                                            <small className="text-muted">by {item.seller || 'Unknown seller'}</small>
                                            <small className="text-muted">
                                                <i className="fas fa-chart-line me-1"></i>
                                                {item.bidsCount || 0} bids
                                            </small>
                                        </div>
                                        <small className="text-muted">Ends: {item.endDate || 'No end date'}</small>
                                    </>
                                )}
                            </div>
                        ))
                    ) : (
                        <div className="text-center py-4">
                            <i className="fas fa-info-circle fs-1 text-muted mb-3"></i>
                            <h5 className="text-muted">No {isUsers ? 'recent users' : 'active auctions'} found</h5>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RecentActivity;