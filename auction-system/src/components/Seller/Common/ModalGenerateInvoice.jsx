import React, { useState, useEffect } from 'react';

const ModalGenerateInvoice = ({ show, toggleModal, productID, apiCall }) => {
    const [auctionID, setAuctionID] = useState('');

    // Fetch auctionID when productID changes
    useEffect(() => {
        if (productID && show) {
            const fetchAuctionID = async () => {
                try {
                    const response = await apiCall(`/product/${productID}`, { method: 'GET' });
                    if (response.success && response.data) {
                        setAuctionID(response.data.auctionID || '');
                    }
                } catch (error) {
                    console.error('Error fetching auction data:', error);
                    alert('Error loading auction data: ' + error.message);
                }
            };
            fetchAuctionID();
        }
    }, [productID, show, apiCall]);

    const generateInvoice = async () => {
        try {
            const response = await apiCall('/generateSalesInvoice', {
                method: 'POST',
                body: JSON.stringify({ productID, auctionID }),
                headers: {
                    'Content-Type': 'application/json',
                },
            });
            if (response.success) {
                alert('Invoice generated successfully!');
                toggleModal('invoice', false);
            } else {
                alert('Error generating invoice: ' + response.message);
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
                        <h5 className="modal-title">Generate Invoice (ID: {productID})</h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={() => toggleModal('invoice', false)}
                        ></button>
                    </div>
                    <div className="modal-body">
                        <p>Generate an invoice for this product?</p>
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => toggleModal('invoice', false)}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="btn btn-custom"
                            onClick={generateInvoice}
                            disabled={!auctionID}
                        >
                            Generate
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ModalGenerateInvoice;