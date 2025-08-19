import React from 'react';

const ListingsTable = ({ data, setEditProductID, setPromoteProductID, setCancelProductID, setInvoiceProductID, toggleModal }) => {
    const viewProduct = (productID) => {
        // Could redirect to a product details page or open a modal
        alert(`Viewing details for product: ${productID}`);
    };

    return (
        <div className="table-responsive">
            <table className="table table-hover" id="listingsTable">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Category</th>
                        <th>Status</th>
                        <th>Starting Price</th>
                        <th>Current Bid</th>
                        <th>Bids</th>
                        <th>End Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {data && data.length > 0 ? (
                        data.map(item => (
                            <tr key={item.productID}>
                                <td>
                                    <div className="d-flex align-items-center">
                                        <img
                                            src={item.image || '/placeholder-image.jpg'}
                                            alt={item.title}
                                            className="rounded me-2"
                                            style={{ width: '50px', height: '50px', objectFit: 'cover' }}
                                        />
                                        <div>
                                            <div className="fw-bold">{item.title || 'N/A'}</div>
                                            <small className="text-muted">ID: {item.productID}</small>
                                        </div>
                                    </div>
                                </td>
                                <td>{item.category || 'N/A'}</td>
                                <td>
                                    <span
                                        className={`badge ${
                                            item.status === 'ACTIVE'
                                                ? 'bg-success'
                                                : item.status === 'PENDING'
                                                ? 'bg-warning'
                                                : item.status === 'EXPIRED'
                                                ? 'bg-danger'
                                                : 'bg-secondary'
                                        }`}
                                    >
                                        {item.status || 'Unknown'}
                                    </span>
                                </td>
                                <td>${(item.startingPrice || 0).toFixed(2)}</td>
                                <td className={item.currentBid ? 'text-success fw-bold' : 'text-muted'}>
                                    {item.currentBid ? `$${(item.currentBid).toFixed(2)}` : 'No bids'}
                                </td>
                                <td><span className="badge badge-custom">{item.bids || 0}</span></td>
                                <td>{item.endDate ? new Date(item.endDate).toLocaleDateString() : 'N/A'}</td>
                                <td>
                                    <div className="btn-group btn-group-sm">
                                        <button
                                            className="btn btn-outline-primary"
                                            title="View Details"
                                            onClick={() => viewProduct(item.productID)}
                                        >
                                            <i className="bi bi-eye"></i>
                                        </button>
                                        {item.status !== 'EXPIRED' && (
                                            <>
                                                <button
                                                    className="btn btn-outline-info"
                                                    title="Promote"
                                                    onClick={() => {
                                                        setPromoteProductID(item.productID);
                                                        toggleModal('promote', true);
                                                    }}
                                                >
                                                    <i className="bi bi-megaphone"></i>
                                                </button>
                                                <button
                                                    className="btn btn-outline-secondary"
                                                    title="Edit"
                                                    onClick={() => {
                                                        setEditProductID(item.productID);
                                                        toggleModal('edit', true);
                                                    }}
                                                >
                                                    <i className="bi bi-pencil"></i>
                                                </button>
                                            </>
                                        )}
                                        {item.status === 'PENDING' && (
                                            <button
                                                className="btn btn-outline-danger"
                                                title="Cancel"
                                                onClick={() => {
                                                    setCancelProductID(item.productID);
                                                    toggleModal('cancel', true);
                                                }}
                                            >
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        )}
                                        {item.status === 'EXPIRED' && (
                                            <button
                                                className="btn btn-outline-success"
                                                title="Generate Invoice"
                                                onClick={() => {
                                                    setInvoiceProductID(item.productID);
                                                    toggleModal('invoice', true);
                                                }}
                                            >
                                                <i className="bi bi-receipt"></i>
                                            </button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="8" className="text-center">No listings available</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default ListingsTable;