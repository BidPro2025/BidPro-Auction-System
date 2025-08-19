import React, { useState } from 'react';
import BiddingAccordion from './BiddingAccordion';

const BiddingActivityContent = () => {
    // State to trigger refresh in BiddingAccordion
    const [refreshKey, setRefreshKey] = useState(0);

    // Function to handle refresh button click
    const handleRefresh = () => {
        setRefreshKey(prev => prev + 1); // Increment to trigger useEffect in BiddingAccordion
    };

    return (
        <div className="tab-pane fade show active" id="bidding" role="tabpanel" aria-labelledby="bidding-tab">
            <div className="card">
                <div className="card-header d-flex justify-content-between align-items-center">
                    <h5 className="section-title mb-0">
                        <i className="bi bi-hammer me-2"></i> Recent Bidding Activity
                    </h5>
                    <button className="btn btn-sm btn-outline-primary" onClick={handleRefresh}>
                        <i className="bi bi-arrow-clockwise"></i> Refresh
                    </button>
                </div>
                <div className="card-body">
                    <BiddingAccordion refreshKey={refreshKey} />
                </div>
            </div>
        </div>
    );
};

export default BiddingActivityContent;