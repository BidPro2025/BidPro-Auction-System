import './MyBids.css';
import { PURPLE_PRIMARY } from '../../../utils/bidderConstants';

const MyBids = ({ products, timeLeft, setBidProduct, setModifyBid }) => {
    return (
        <section id="my-bids" className="mb-5">
            <h2 className="section-title">
                <i className="bi bi-list-ul me-2"></i>My Active Bids
            </h2>
            <div className="table-responsive">
                <table className="table table-striped">
                    <thead style={{ backgroundColor: PURPLE_PRIMARY, color: 'white' }}>
                        <tr>
                            <th>Item</th>
                            <th>My Bid</th> 
                            <th>Current Price</th>
                            <th>Status</th>
                            <th>Time Left</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.slice(0, 2).map((product) => (
                            <tr key={product.id}>
                                <td>{product.title}</td>
                                <td>${product.yourBid?.toFixed(2) || 'N/A'}</td>
                                <td>${product.currentBid.toFixed(2)}</td>
                                <td>
                                    <span
                                        className={`badge ${product.yourBid >= product.currentBid ? 'bg-success' : 'bg-warning'
                                            }`}
                                    >
                                        {product.yourBid >= product.currentBid ? 'Winning' : 'Outbid'}
                                    </span>
                                </td>
                                <td>{timeLeft[product.id] || 'Calculating...'}</td>
                                <td>
                                    <button
                                        className={`btn btn-sm ${product.yourBid >= product.currentBid ? 'btn-warning' : 'btn-primary'
                                            }`}
                                        style={{
                                            backgroundColor: product.yourBid >= product.currentBid ? '#ffc107' : PURPLE_PRIMARY,
                                            borderColor: product.yourBid >= product.currentBid ? '#ffc107' : PURPLE_PRIMARY,
                                        }}
                                        data-bs-toggle="modal"
                                        data-bs-target={
                                            product.yourBid >= product.currentBid ? '#modifyBidModal' : '#placeBidModal'
                                        }
                                        onClick={() =>
                                            product.yourBid >= product.currentBid
                                                ? setModifyBid({
                                                    bidID: `BID00${product.id.slice(-1)}`,
                                                    currentBid: product.yourBid,
                                                    minBid: product.minBid,
                                                })
                                                : setBidProduct(product)
                                        }
                                    >
                                        {product.yourBid >= product.currentBid ? 'Modify Bid' : 'Increase Bid'}
                                    </button>
                                    <button
                                        className="btn btn-sm btn-outline-secondary ms-1"
                                        data-bs-toggle="modal"
                                        data-bs-target={`#productModal${product.id.slice(-1)}`}
                                    >
                                        View
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>
    );
};

export default MyBids;