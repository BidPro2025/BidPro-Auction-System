import React, { useState } from 'react';

const ProductsContent = ({ handleAction, products = [] }) => {
    // Utility function for currency formatting
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount || 0);
    };

    // State management
    const [searchQuery, setSearchQuery] = useState('');
    const [filterCategory, setFilterCategory] = useState('');
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [viewMode, setViewMode] = useState('grid');

    // Get unique categories from products
    const categories = [...new Set(products.map(product => product?.category))].filter(Boolean);

    // Filter products based on search and category
    const filteredProducts = products.filter(product => {
        if (!product) return false;

        const matchesSearch =
            (product.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                product.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                product.id?.toLowerCase().includes(searchQuery.toLowerCase()));

        const matchesCategory = !filterCategory || product.category === filterCategory;
        return matchesSearch && matchesCategory;
    });

    // Product action handlers
    const handleEditProduct = (product) => {
        setSelectedProduct(product);
        handleAction('Edit Product', `Editing product: ${product?.title}`);
    };

    const handleDeleteProduct = (product) => {
        if (window.confirm(`Are you sure you want to delete "${product?.title}"?`)) {
            handleAction('Delete Product', `Product "${product?.title}" has been deleted`);
        }
    };

    const handleViewBids = (product) => {
        handleAction('View Bids', `Viewing bids for: ${product?.title}`);
    };

    // Product display components
    const ProductCard = ({ product }) => (
        <div className="col-lg-4 col-md-6 mb-4">
            <div className="card h-100 shadow-sm border-0">
                {/* Product card content */}
            </div>
        </div>
    );

    const ProductListItem = ({ product }) => (
        <div className="card mb-3 border-0 shadow-sm">
            {/* Product list item content */}
        </div>
    );

    return (
        <div className="container-fluid">
            {/* Header Section */}
            <div className="row mb-4">
                <div className="col-12">
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h2 className="mb-1">Product Management</h2>
                            <p className="text-muted mb-0">Manage auction products and inventory</p>
                        </div>
                        <button
                            className="btn btn-primary"
                            onClick={() => handleAction('Add Product', 'Opening add product form')}
                        >
                            <i className="fas fa-plus me-2"></i>Add New Product
                        </button>
                    </div>
                </div>
            </div>

            {/* Filters and Controls */}
            <div className="row mb-4">
                {/* Search and filter controls */}
            </div>

            {/* Statistics Cards */}
            <div className="row mb-4">
                <div className="col-lg-3 col-md-6 mb-3">
                    <div className="card bg-primary text-white border-0">
                        <div className="card-body">
                            <div className="d-flex justify-content-between">
                                <div>
                                    <h6 className="card-title mb-0">Total Products</h6>
                                    <h3 className="mb-0">{products.length}</h3>
                                </div>
                                <div className="align-self-center">
                                    <i className="fas fa-shopping-cart fa-2x opacity-75"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="col-lg-3 col-md-6 mb-3">
                    <div className="card bg-success text-white border-0">
                        <div className="card-body">
                            <div className="d-flex justify-content-between">
                                <div>
                                    <h6 className="card-title mb-0">Active Auctions</h6>
                                    <h3 className="mb-0">{products.filter(p => p?.status === 'active').length}</h3>
                                </div>
                                <div className="align-self-center">
                                    <i className="fas fa-chart-line fa-2x opacity-75"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="col-lg-3 col-md-6 mb-3">
                    <div className="card bg-warning text-white border-0">
                        <div className="card-body">
                            <div className="d-flex justify-content-between">
                                <div>
                                    <h6 className="card-title mb-0">Total Value</h6>
                                    <h3 className="mb-0">
                                        {formatCurrency(products.reduce((sum, p) => sum + (p?.currentBid || 0), 0))}
                                    </h3>
                                </div>
                                <div className="align-self-center">
                                    <i className="fas fa-dollar-sign fa-2x opacity-75"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="col-lg-3 col-md-6 mb-3">
                    <div className="card bg-info text-white border-0">
                        <div className="card-body">
                            <div className="d-flex justify-content-between">
                                <div>
                                    <h6 className="card-title mb-0">Categories</h6>
                                    <h3 className="mb-0">{categories.length}</h3>
                                </div>
                                <div className="align-self-center">
                                    <i className="fas fa-tags fa-2x opacity-75"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Products Display */}
            {filteredProducts.length === 0 ? (
                <div className="text-center py-5">
                    <i className="fas fa-search fa-3x text-muted mb-3"></i>
                    <h5 className="text-muted">No products found</h5>
                    <p className="text-muted">Try adjusting your search criteria</p>
                </div>
            ) : (
                <div className="row">
                    {viewMode === 'grid' ? (
                        filteredProducts.map(product => (
                            <ProductCard key={product?.id || Math.random()} product={product} />
                        ))
                    ) : (
                        <div className="col-12">
                            {filteredProducts.map(product => (
                                <ProductListItem key={product?.id || Math.random()} product={product} />
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default ProductsContent;