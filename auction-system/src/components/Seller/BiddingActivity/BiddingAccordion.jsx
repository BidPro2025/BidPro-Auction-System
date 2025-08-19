import React, { useEffect, useState } from 'react';

const BiddingAccordion = () => {
    // State for bidding data, loading, and error
    const [biddingData, setBiddingData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fetch bidding data from backend
        const fetchBiddingData = async () => {  
            try {
                setLoading(true);
                // Replace with your actual API endpoint
                const response = await fetch('/api/bidding-data'); // Example endpoint
                const jsonData = await response.json();
                setBiddingData(jsonData); // Expected format: [{ productID, title, bids, highestBid, status, bidsData: [{ bidder, amount, time, status }, ...] }, ...]
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch bidding data');
                setLoading(false);
            }
        };

        fetchBiddingData();
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div className="accordion" id="biddingAccordion">
            {biddingData.map((item, index) => (
                <div className="accordion-item" key={item.productID}>
                    <h2 className="accordion-header">
                        <button
                            className={`accordion-button ${index === 0 ? '' : 'collapsed'}`}
                            type="button"
                            data-bs-toggle="collapse"
                            data-bs-target={`#collapse${index}`}
                        >
                            <div className="d-flex justify-content-between align-items-center w-100 me-3">
                                <div>
                                    <strong>{item.title}</strong>
                                    <span className={`badge ${item.bids > 0 ? 'bg-success' : 'bg-primary'} ms-2`}>
                                        {item.bids} Bids
                                    </span>
                                </div>
                                <div className="text-end">
                                    <div className="text-success fw-bold">${item.highestBid.toFixed(2)}</div>
                                    <small className="text-muted">{item.status === 'Active' ? 'Highest Bid' : 'Final Price'}</small>
                                </div>
                            </div>
                        </button>
                    </h2>
                    <div
                        id={`collapse${index}`}
                        className={`accordion-collapse collapse ${index === 0 ? 'show' : ''}`}
                    >
                        <div className="accordion-body">
                            <div className="table-responsive">
                                <table className="table table-sm">
                                    <thead>
                                        <tr>
                                            <th>Bidder</th>
                                            <th>Amount</th>
                                            <th>Time</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {item.bidsData.map(bid => (
                                            <tr key={bid.bidder}>
                                                <td>{bid.bidder}</td>
                                                <td className={bid.status === 'Winning' || bid.status === 'Won' ? 'text-success fw-bold' : ''}>
                                                    ${bid.amount.toFixed(2)}
                                                </td>
                                                <td>{bid.time}</td>
                                                <td>
                                                    <span
                                                        className={`badge ${bid.status === 'Winning' || bid.status === 'Won' ? 'bg-success' : 'bg-secondary'}`}
                                                    >
                                                        {bid.status}
                                                    </span>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default BiddingAccordion;