import React from 'react';

const ModalCancelProduct = ({ show, toggleModal, productID, apiCall }) => {
    const cancelProduct = async () => {
        try {
            const response = await apiCall('/cancelProductListing', {
                method: 'POST',
                body: JSON.stringify({ productID }),
                headers: {
                    'Content-Type': 'application/json',
                },
            });
            
            if (response.success) {
                alert('Product listing canceled successfully!');
                toggleModal('cancel', false);
            } else {
                alert('Error canceling product: ' + response.message);
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
                        <h5 className="modal-title">Cancel Product Listing</h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={() => toggleModal('cancel', false)}
                        ></button>
                    </div>
                    <div className="modal-body">
                        <p>Are you sure you want to cancel this product listing? (ID: {productID})</p>
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => toggleModal('cancel', false)}
                        >
                            No
                        </button>
                        <button
                            type="button"
                            className="btn btn-danger"
                            onClick={cancelProduct}
                        >
                            Yes, Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ModalCancelProduct;