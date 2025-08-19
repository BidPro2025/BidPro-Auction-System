import React, { useState, useEffect } from 'react';

const ModalEditProduct = ({ show, toggleModal, productID, apiCall }) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        categoryID: '',
        condition: '',
        startingPrice: '',
        incrementGap: '',
        auctionEndDate: '',
    });
    const [categories, setCategories] = useState([]);
    const [imageFiles, setImageFiles] = useState([]);

    // Fetch product data and categories when productID changes
    useEffect(() => {
        if (productID && show) {
            const fetchProductData = async () => {
                try {
                    const response = await apiCall(`/product/${productID}`, { method: 'GET' });
                    if (response.success && response.data) {
                        setFormData({
                            title: response.data.title || '',
                            description: response.data.description || '',
                            categoryID: response.data.categoryID || '',
                            condition: response.data.condition || '',
                            startingPrice: response.data.startingPrice || '',
                            incrementGap: response.data.incrementGap || '',
                            auctionEndDate: response.data.auctionEndDate || '',
                        });
                    }
                } catch (error) {
                    console.error('Error fetching product data:', error);
                    alert('Error loading product data: ' + error.message);
                }
            };

            const fetchCategories = async () => {
                try {
                    const response = await apiCall('/categories', { method: 'GET' });
                    if (response.success) {
                        setCategories(response.data);
                    }
                } catch (error) {
                    console.error('Error fetching categories:', error);
                }
            };

            fetchProductData();
            fetchCategories();
        }
    }, [productID, show, apiCall]);

    const handleChange = (e) => {
        const { id, value } = e.target;
        setFormData(prev => ({ ...prev, [id]: value }));
    };

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files).slice(0, 5); // Limit to 5 images
        setImageFiles(files);
    };

    const saveProductEdit = async (e) => {
        e.preventDefault();
        
        const formDataToSend = new FormData();
        formDataToSend.append('productID', productID);
        formDataToSend.append('title', formData.title);
        formDataToSend.append('description', formData.description);
        formDataToSend.append('categoryID', formData.categoryID);
        formDataToSend.append('condition', formData.condition);
        formDataToSend.append('startingPrice', parseFloat(formData.startingPrice));
        formDataToSend.append('incrementGap', parseFloat(formData.incrementGap));
        formDataToSend.append('auctionEndDate', formData.auctionEndDate);
        
        // Append images
        imageFiles.forEach((file, index) => {
            formDataToSend.append(`images[${index}]`, file);
        });

        try {
            const response = await apiCall('/modifyProductListing', {
                method: 'POST',
                body: formDataToSend,
            });
            
            if (response.success) {
                alert('Product updated successfully!');
                toggleModal('edit', false);
            } else {
                alert('Error updating product: ' + response.message);
            }
        } catch (error) {
            alert('Error: ' + error.message);
        }
    };

    return (
        <div className={`modal fade ${show ? 'show d-block' : ''}`} tabIndex="-1">
            <div className="modal-dialog modal-lg">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Edit Product (ID: {productID})</h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={() => toggleModal('edit', false)}
                        ></button>
                    </div>
                    <div className="modal-body">
                        <form id="editForm" onSubmit={saveProductEdit}>
                            <div className="row g-3">
                                <div className="col-md-6">
                                    <label className="form-label">Product Title *</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        id="title"
                                        value={formData.title}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="col-12">
                                    <label className="form-label">Description *</label>
                                    <textarea
                                        className="form-control"
                                        id="description"
                                        rows="4"
                                        value={formData.description}
                                        onChange={handleChange}
                                        required
                                    ></textarea>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Category *</label>
                                    <select
                                        className="form-select"
                                        id="categoryID"
                                        value={formData.categoryID}
                                        onChange={handleChange}
                                        required
                                    >
                                        <option value="">Select Category</option>
                                        {categories.map(category => (
                                            <option key={category.id} value={category.id}>
                                                {category.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Condition *</label>
                                    <select
                                        className="form-select"
                                        id="condition"
                                        value={formData.condition}
                                        onChange={handleChange}
                                        required
                                    >
                                        <option value="">Select Condition</option>
                                        <option value="NEW">New</option>
                                        <option value="LIKE_NEW">Like New</option>
                                        <option value="VERY_GOOD">Very Good</option>
                                        <option value="GOOD">Good</option>
                                        <option value="FAIR">Fair</option>
                                        <option value="POOR">Poor</option>
                                    </select>
                                </div>
                                <div className="col-md-4">
                                    <label className="form-label">Starting Price ($) *</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="startingPrice"
                                        step="0.01"
                                        value={formData.startingPrice}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="col-md-4">
                                    <label className="form-label">Increment Gap ($) *</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="incrementGap"
                                        step="0.01"
                                        value={formData.incrementGap}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="col-md-4">
                                    <label className="form-label">Auction End Date *</label>
                                    <input
                                        type="datetime-local"
                                        className="form-control"
                                        id="auctionEndDate"
                                        value={formData.auctionEndDate}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="col-12">
                                    <label className="form-label">Product Images</label>
                                    <input
                                        type="file"
                                        className="form-control"
                                        id="images"
                                        multiple
                                        accept="image/*"
                                        onChange={handleImageChange}
                                    />
                                    <div className="form-text">Upload up to 5 images (JPG, PNG, GIF)</div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => toggleModal('edit', false)}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="btn btn-custom"
                            onClick={saveProductEdit}
                        >
                            Save Changes
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ModalEditProduct;