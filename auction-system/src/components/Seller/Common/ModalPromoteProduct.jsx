import React, { useState, useEffect } from 'react';

const ModalPromoteProduct = ({ show, toggleModal, productID, apiCall }) => {
    const [promotionStatus, setPromotionStatus] = useState('STANDARD');

    useEffect(() => {
        if (productID && show) {
            const fetchPromotionStatus = async () => {
                try {
                    const response = await apiCall(`/product/${productID}`, { method: 'GET' });
                    if (response.success && response.data) {
                        setPromotionStatus(response.data.promotionStatus || 'STANDARD');
                    }
                } catch (error) {
                    console.error('Error fetching promotion status:', error);
                }
            };
            fetchPromotionStatus();
        }
    }, [productID, show, apiCall]);

    const promoteProduct = async (e) => {
        e.preventDefault();
        try {
            const response = await apiCall('/promoteProduct', {
                method: 'POST',
                body: JSON.stringify({ productID, promotionStatus }),
                headers: {
                    'Content-Type': 'application/json',
                },
            });
            if (response.success) {
                alert('Product promoted successfully!');
                toggleModal('promote', false);
            } else {
                alert('Error promoting product: ' + response.message);
            }
        } catch (error) {
            alert('Error: ' + error.message);
        }
    };

    return (
        <div className={`modal fade ${show ? 'show d-block' : ''}`} tabIndex="-1">
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Promote Product (ID: {productID})</h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={() => toggleModal('promote', false)}
                        ></button>
                    </div>
                    <div className="modal-body">
                        <form id="promoteForm" onSubmit={promoteProduct}>
                            <div className="mb-3">
                                <label className="form-label">Promotion Status</label>
                                <select
                                    className="form-select"
                                    id="promotionStatus"
                                    value={promotionStatus}
                                    onChange={(e) => setPromotionStatus(e.target.value)}
                                    required
                                >
                                    <option value="STANDARD">Standard</option>
                                    <option value="FEATURED">Featured</option>
                                    <option value="PREMIUM">Premium</option>
                                </select>
                            </div>
                        </form>
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => toggleModal('promote', false)}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="btn btn-custom"
                            onClick={promoteProduct}
                        >
                            Promote
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ModalPromoteProduct;