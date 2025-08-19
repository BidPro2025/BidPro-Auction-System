import React, { useState, useEffect } from 'react';
import ListingsTable from './ListingsTable';

const MyListingsContent = ({ 
    setEditProductID, 
    setPromoteProductID, 
    setCancelProductID, 
    setInvoiceProductID,
    toggleModal,
    apiCall
}) => {
    const [statusFilter, setStatusFilter] = useState('');
    const [filteredListings, setFilteredListings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
  apiCall('/products').then((res) => {
    if (res.success) setFilteredListings(res.data);
    else setError(res.message);
    setLoading(false);
  });
}, []);

    const filterListings = (e) => {
        const status = e.target.value;
        setStatusFilter(status);
        
        if (!filteredListings) return;
        
        const filtered = status 
            ? filteredListings.filter(item => item.status === status)
            : filteredListings;
        
        setFilteredListings(filtered);
    };

    return (
        <div 
            className="tab-pane fade show active"
            id="listings" 
            role="tabpanel"
            aria-labelledby="listings-tab"
        >
            <div className="card">
                <div className="card-header d-flex justify-content-between align-items-center">
                    <h5 className="section-title mb-0">
                        <i className="bi bi-list-ul"></i> My Product Listings
                    </h5>
                    <div>
                        <select
                            className="form-select form-select-sm"
                            id="statusFilter"
                            value={statusFilter}
                            onChange={filterListings}
                            disabled={loading || error}
                        >
                            <option value="">All Status</option>
                            <option value="PENDING">Pending</option>
                            <option value="APPROVED">Approved</option>
                            <option value="ACTIVE">Active</option>
                            <option value="EXPIRED">Expired</option>
                        </select>
                    </div>
                </div>
                <div className="card-body">
                    {loading ? (
                        <div className="text-center py-4">
                            <div className="spinner-border" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                        </div>
                    ) : error ? (
                        <div className="text-center py-4 text-danger">
                            <p>{error}</p>
                        </div>
                    ) : filteredListings.length > 0 ? (
                        <ListingsTable
                            data={filteredListings}
                            setEditProductID={setEditProductID}
                            setPromoteProductID={setPromoteProductID}
                            setCancelProductID={setCancelProductID}
                            setInvoiceProductID={setInvoiceProductID}
                            toggleModal={toggleModal}
                        />
                    ) : (
                        <div className="text-center py-4">
                            <p>No listings found</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MyListingsContent;