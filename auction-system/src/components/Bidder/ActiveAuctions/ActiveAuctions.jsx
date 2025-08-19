import './ActiveAuctions.css';
import { PURPLE_PRIMARY, PURPLE_SECONDARY } from '../../../utils/bidderConstants';

const ActiveAuctions = ({ products, timeLeft, setBidProduct, setModifyBid, setAuctionID }) => {
    return (
        <section id="auctions" className="mb-5">
            <h2 className="section-title">
                <i className="bi bi-hammer me-2"></i>Active Auctions
            </h2>
            <div className="row">
                {products.map((product) => (
                    <div className="col-lg-4 col-md-6 mb-4" key={product.id}>
                        <div className="card product-card">
                            <img src={product.image} className="card-img-top" alt={product.title} />
                            <div className="card-body">
                                <h5 className="card-title">{product.title}</h5>
                                <p className="card-text">{product.description}</p>
                                <div className="d-flex justify-content-between align-items-center mb-2">
                                    <span
                                        className={`badge price-badge ${product.yourBid >= product.currentBid ? 'bg-primary' : 'bg-success'
                                            }`}
                                        style={{ backgroundColor: product.yourBid >= product.currentBid ? PURPLE_PRIMARY : '#28a745' }}
                                    >
                                        Current: ${product.currentBid.toFixed(2)}
                                    </span>
                                    <span className="time-left">{timeLeft[product.id] || 'Calculating...'}</span>
                                </div>
                                <div className="mb-3">
                                    {product.yourBid ? (
                                        product.yourBid >= product.currentBid ? (
                                            <span className="badge winning-bid" style={{ backgroundColor: PURPLE_PRIMARY }}>You're winning!</span>
                                        ) : (
                                            <small className="text-muted">Your bid: ${product.yourBid.toFixed(2)}</small>
                                        )
                                    ) : (
                                        <small className="text-muted">No bids yet</small>
                                    )}
                                </div>
                                <div className="input-group mb-2">
                                    <input
                                        type="number"
                                        className="form-control bid-input"
                                        placeholder={product.minBid.toString()}
                                        min={product.minBid}
                                    />
                                    <button
                                        className="btn btn-success btn-sm"
                                        data-bs-toggle="modal"
                                        data-bs-target="#placeBidModal"
                                        onClick={() => setBidProduct(product)}
                                    >
                                        Place Bid
                                    </button>
                                </div>
                                <div className="d-grid gap-2">
                                    <button
                                        className="btn btn-outline-primary btn-sm"
                                        data-bs-toggle="modal"
                                        data-bs-target={`#productModal${product.id.slice(-1)}`}
                                    >
                                        View Details
                                    </button>
                                    {product.yourBid && (
                                        <button
                                            className="btn btn-warning btn-sm"
                                            data-bs-toggle="modal"
                                            data-bs-target="#modifyBidModal"
                                            onClick={() =>
                                                setModifyBid({
                                                    bidID: `BID00${product.id.slice(-1)}`,
                                                    currentBid: product.yourBid,
                                                    minBid: product.minBid,
                                                })
                                            }
                                        >
                                            Modify Bid
                                        </button>
                                    )}
                                    <button
                                        className="btn btn-info btn-sm"
                                        data-bs-toggle="modal"
                                        data-bs-target="#registerAuctionModal"
                                        onClick={() => setAuctionID(`AUC00${product.id.slice(-1)}`)}
                                    >
                                        Register for Auction
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </section>
    );
};

export default ActiveAuctions;