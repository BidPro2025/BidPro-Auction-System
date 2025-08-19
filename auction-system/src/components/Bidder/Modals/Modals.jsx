import './Modals.css';
import { PURPLE_PRIMARY } from '../../../utils/bidderConstants';

const Modals = ({
    balance,
    bidProduct,
    modifyBid,
    packageData,
    auctionID,
    autoBid,
    setAutoBid,
    handlePlaceBid,
    handleModifyBid,
    handleBuyPackage,
    handleRegisterAuction,
    products,
    timeLeft,
    setBidProduct,
}) => {
    return (
        <>
            {/* Profile Modal */}
<div className="modal fade" id="profileModal" tabIndex="-1">
    <div className="modal-dialog">
        <div className="modal-content">
            <div className="modal-header" style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                <h5 className="modal-title">User Profile</h5>
                <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div className="modal-body">
                <p><strong>Username:</strong> bidder_user</p>
                <p><strong>Email:</strong> bidder@example.com</p>
                <p><strong>Balance:</strong> ${balance.toFixed(2)}</p>
                <p><strong>Package:</strong> Premium</p>
            </div>
            <div className="modal-footer d-flex justify-content-between">
                <button 
                    type="button" 
                    className="btn btn-danger"
                    onClick={() => {
                        // Close the modal
                        const modal = bootstrap.Modal.getInstance(document.getElementById('profileModal'));
                        modal.hide();
                        
                        // Clear user session (example)
                        localStorage.removeItem('user');
                        localStorage.removeItem('token');
                        
                        // Navigate to login page
                        window.location.href = '/login';
                        
                        // If using React Router:
                        // navigate('/login');
                    }}
                >
                    Logout
                </button>
                <button 
                    type="button" 
                    className="btn btn-secondary" 
                    data-bs-dismiss="modal"
                >
                    Close
                </button>
            </div>
        </div>
    </div>
</div>
            {/* Place Bid Modal */}
            <div className="modal fade" id="placeBidModal" tabIndex="-1">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header" style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                            <h5 className="modal-title">Place Bid</h5>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div className="modal-body">
                            <input type="hidden" id="bidProductID" value={bidProduct?.id} />
                            <div className="mb-3">
                                <label className="form-label">Current Highest Bid ($)</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={bidProduct?.currentBid.toFixed(2) || ''}
                                    readOnly
                                />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Your Bid ($)</label>
                                <input
                                    type="number"
                                    className="form-control"
                                    id="newBidAmount"
                                    min={bidProduct?.minBid}
                                    defaultValue={bidProduct?.minBid}
                                    required
                                />
                            </div>
                            <div className="mb-3">
                                <label className="form-check-label">
                                    <input
                                        type="checkbox"
                                        className="form-check-input"
                                        checked={autoBid}
                                        onChange={(e) => setAutoBid(e.target.checked)}
                                    />
                                    Enable Auto-Bid
                                </label>
                            </div>
                            {autoBid && (
                                <div className="mb-3">
                                    <label className="form-label">Max Auto-Bid Amount ($)</label>
                                    <input type="number" className="form-control" id="maxAutoBidAmount" />
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">
                                Cancel
                            </button>
                            <button type="button" className="btn btn-custom" onClick={handlePlaceBid}>
                                Confirm Bid
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Modify Bid Modal */}
            <div className="modal fade" id="modifyBidModal" tabIndex="-1">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header" style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                            <h5 className="modal-title">Modify Bid</h5>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div className="modal-body">
                            <input type="hidden" id="modifyBidID" value={modifyBid?.bidID} />
                            <div className="mb-3">
                                <label className="form-label">Current Bid ($)</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={modifyBid?.currentBid.toFixed(2) || ''}
                                    readOnly
                                />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">New Bid Amount ($)</label>
                                <input
                                    type="number"
                                    className="form-control"
                                    id="newModifyBidAmount"
                                    min={modifyBid?.minBid}
                                    defaultValue={modifyBid?.minBid}
                                    required
                                />
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">
                                Cancel
                            </button>
                            <button type="button" className="btn btn-custom" onClick={handleModifyBid}>
                                Save Changes
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Buy Package Modal */}
            <div className="modal fade" id="buyPackageModal" tabIndex="-1">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header" style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                            <h5 className="modal-title">Confirm Package Purchase</h5>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div className="modal-body">
                            <p>
                                Purchase the {packageData?.name} Package for ${packageData?.price.toFixed(2)}?
                            </p>
                            <input
                                type="hidden"
                                id="packageID"
                                value={packageData?.name.toUpperCase() + '_PKG'}
                            />
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">
                                Cancel
                            </button>
                            <button type="button" className="btn btn-custom" onClick={handleBuyPackage}>
                                Confirm Purchase
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Register Auction Modal */}
            <div className="modal fade" id="registerAuctionModal" tabIndex="-1">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header" style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                            <h5 className="modal-title">Register for Auction</h5>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div className="modal-body">
                            <p>Register for this auction to participate in bidding?</p>
                            <input type="hidden" id="auctionID" value={auctionID} />
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">
                                Cancel
                            </button>
                            <button type="button" className="btn btn-custom" onClick={handleRegisterAuction}>
                                Register
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Product Details Modals */}
            {products.map((product) => (
                <div className="modal fade" id={`productModal${product.id.slice(-1)}`} tabIndex="-1" key={`modal-${product.id}`}>
                    <div className="modal-dialog modal-lg">
                        <div className="modal-content">
                            <div className="modal-header" style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                                <h5 className="modal-title">{product.title} - Details</h5>
                                <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div className="modal-body">
                                <div className="row">
                                    <div className="col-md-6">
                                        <img src={product.image} className="img-fluid rounded" alt={product.title} />
                                    </div>
                                    <div className="col-md-6">
                                        <h4>{product.title}</h4>
                                        {Object.entries(product.details).map(([key, value]) => (
                                            <p key={key}>
                                                <strong>{key.charAt(0).toUpperCase() + key.slice(1)}:</strong> {value}
                                            </p>
                                        ))}
                                        <p>
                                            <strong>Current Bid:</strong> ${product.currentBid.toFixed(2)}
                                        </p>
                                        <p>
                                            <strong>Auction Ends:</strong> {timeLeft[product.id] || 'Calculating...'}
                                        </p>
                                        <div className="mt-3">
                                            <div className="input-group">
                                                <input
                                                    type="number"
                                                    className="form-control"
                                                    placeholder="Enter bid amount"
                                                    min={product.minBid}
                                                    defaultValue={product.minBid}
                                                />
                                                <button
                                                    className="btn btn-success"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#placeBidModal"
                                                    onClick={() => setBidProduct(product)}
                                                >
                                                    Place Bid
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="row mt-3">
                                    <div className="col-12">
                                        <h5>Description</h5>
                                        <p>{product.description}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </>
    );
};

export default Modals;