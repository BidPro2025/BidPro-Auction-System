import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './CategoryContent.css';

const CategoryContent = ({ handleAction }) => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [sortBy, setSortBy] = useState('name');
    const [showModal, setShowModal] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        visibility: true,
        displayOrder: 0,
        parentCategory: ''
    });

    const apiCall = axios.create({
        baseURL: 'http://localhost:8080/api',
        headers: {
            'Authorization': `Bearer ${ localStorage.getItem('token') } `,
            'Content-Type': 'application/json'
        }
    });

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            setLoading(true);
            const response = await apiCall.get('/categories');
            setCategories(response.data.data || []);
            setError(null);
            setLoading(false);
        } catch (err) {
            setError('Failed to fetch categories: ' + (err.response?.data?.message || err.message));
            setLoading(false);
        }
    };

    const filteredCategories = categories.filter(category => {
        const matchesSearch = category.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            (category.description?.toLowerCase().includes(searchQuery.toLowerCase()) || false);
        const matchesStatus = filterStatus === 'all' || 
            (filterStatus === 'visible' && category.visibility) || 
            (filterStatus === 'hidden' && !category.visibility);
        return matchesSearch && matchesStatus;
    }).sort((a, b) => {
        switch (sortBy) {
            case 'name':
                return a.name.localeCompare(b.name);
            case 'displayOrder':
                return (a.displayOrder || 0) - (b.displayOrder || 0);
            default:
                return 0;
        }
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                name: formData.name,
                description: formData.description,
                visibility: formData.visibility,
                displayOrder: formData.displayOrder,
                parentCategory: formData.parentCategory || null
            };
            const url = selectedCategory ? `/ categories / ${ selectedCategory.categoryID } ` : '/categories';
            const method = selectedCategory ? 'put' : 'post';

            const response = await apiCall({ url, method, data: payload });
            if (response.data.success) {
                await fetchCategories();
                handleAction('Success', selectedCategory ? 'Category updated successfully' : 'Category added successfully');
                resetForm();
            } else {
                setError(response.data.message || 'Operation failed');
            }
        } catch (err) {
            setError('Failed to save category: ' + (err.response?.data?.message || err.message));
        }
    };

    const resetForm = () => {
        setFormData({
            name: '',
            description: '',
            visibility: true,
            displayOrder: 0,
            parentCategory: ''
        });
        setSelectedCategory(null);
        setShowModal(false);
    };

    const editCategory = (category) => {
        setSelectedCategory(category);
        setFormData({
            name: category.name,
            description: category.description || '',
            visibility: category.visibility,
            displayOrder: category.displayOrder || 0,
            parentCategory: category.parentCategory || ''
        });
        setShowModal(true);
    };

    const deleteCategory = async (id) => {
        if (!window.confirm('Delete this category?')) return;
        try {
            const response = await apiCall.delete(`/ categories / ${ id } `);
            if (response.data.success) {
                await fetchCategories();
                handleAction('Success', 'Category deleted successfully');
            } else {
                setError(response.data.message || 'Failed to delete category');
            }
        } catch (err) {
            setError('Failed to delete category: ' + (err.response?.data?.message || err.message));
        }
    };

    const toggleVisibility = async (categoryId) => {
        try {
            const category = categories.find(c => c.categoryID === categoryId);
            const response = await apiCall.put(`/ categories / ${ categoryId }/visibility`, { visibility: !category.visibility });
if (response.data.success) {
    await fetchCategories();
    handleAction('Success', `Category visibility updated to ${!category.visibility ? 'Visible' : 'Hidden'}`);
} else {
    setError(response.data.message || 'Failed to update visibility');
}
        } catch (err) {
    setError('Failed to update visibility: ' + (err.response?.data?.message || err.message));
}
    };

if (loading) {
    return (
        <div className="d-flex justify-content-center align-items-center" style={{ height: '300px' }}>
            <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
            </div>
        </div>
    );
}

if (error) {
    return (
        <div className="alert alert-danger">
            <i className="fas fa-exclamation-triangle me-2"></i>
            {error}
        </div>
    );
}

return (
    <div className="category-management">
        <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 className="mb-1">Category Management</h2>
                <p className="text-muted mb-0">Organize products into categories</p>
            </div>
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>
                <i className="fas fa-plus me-2"></i>Add Category
            </button>
        </div>

        <div className="row mb-4">
            <div className="col-md-3">
                <div className="card border-0 shadow-sm">
                    <div className="card-body">
                        <div className="d-flex align-items-center">
                            <div className="flex-shrink-0">
                                <div className="bg-primary bg-opacity-10 rounded p-3">
                                    <i className="fas fa-tags text-primary fs-4"></i>
                                </div>
                            </div>
                            <div className="flex-grow-1 ms-3">
                                <h6 className="mb-0">Total Categories</h6>
                                <h4 className="mb-0 text-primary">{categories.length}</h4>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div className="col-md-3">
                <div className="card border-0 shadow-sm">
                    <div className="card-body">
                        <div className="d-flex align-items-center">
                            <div className="flex-shrink-0">
                                <div className="bg-success bg-opacity-10 rounded p-3">
                                    <i className="fas fa-eye text-success fs-4"></i>
                                </div>
                            </div>
                            <div className="flex-grow-1 ms-3">
                                <h6 className="mb-0">Visible Categories</h6>
                                <h4 className="mb-0 text-success">
                                    {categories.filter(cat => cat.visibility).length}
                                </h4>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div className="card border-0 shadow-sm mb-4">
            <div className="card-body">
                <div className="row g-3">
                    <div className="col-md-4">
                        <label className="form-label">Search Categories</label>
                        <div className="position-relative">
                            <input
                                type="text"
                                className="form-control"
                                placeholder="Search by name or description..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                            <i className="fas fa-search position-absolute top-50 end-0 translate-middle-y me-3 text-muted"></i>
                        </div>
                    </div>
                    <div className="col-md-3">
                        <label className="form-label">Filter by Visibility</label>
                        <select
                            className="form-select"
                            value={filterStatus}
                            onChange={(e) => setFilterStatus(e.target.value)}
                        >
                            <option value="all">All</option>
                            <option value="visible">Visible</option>
                            <option value="hidden">Hidden</option>
                        </select>
                    </div>
                    <div className="col-md-3">
                        <label className="form-label">Sort By</label>
                        <select
                            className="form-select"
                            value={sortBy}
                            onChange={(e) => setSortBy(e.target.value)}
                        >
                            <option value="name">Name</option>
                            <option value="displayOrder">Display Order</option>
                        </select>
                    </div>
                    <div className="col-md-2 d-flex align-items-end">
                        <button
                            className="btn btn-outline-secondary w-100"
                            onClick={() => {
                                setSearchQuery('');
                                setFilterStatus('all');
                                setSortBy('name');
                            }}
                        >
                            <i className="fas fa-undo me-2"></i>Reset
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div className="card border-0 shadow-sm">
            <div className="card-body p-0">
                <div className="table-responsive">
                    <table className="table table-hover mb-0">
                        <thead className="bg-light">
                            <tr>
                                <th className="ps-4">Category</th>
                                <th>Description</th>
                                <th>Display Order</th>
                                <th>Parent Category</th>
                                <th>Visibility</th>
                                <th className="text-center">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredCategories.length > 0 ? (
                                filteredCategories.map((category) => (
                                    <tr key={category.categoryID}>
                                        <td className="ps-4">
                                            <div className="d-flex align-items-center">
                                                <div>
                                                    <h6 className="mb-0">{category.name}</h6>
                                                    <small className="text-muted">ID: {category.categoryID}</small>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span className="text-muted">{category.description || 'No description'}</span>
                                        </td>
                                        <td>
                                            <span className="badge bg-info bg-opacity-10 text-info">
                                                {category.displayOrder || 0}
                                            </span>
                                        </td>
                                        <td>
                                            <span className="text-muted">{category.parentCategory || 'None'}</span>
                                        </td>
                                        <td>
                                            <span
                                                className={`badge ${category.visibility ? 'bg-success' : 'bg-warning'} bg-opacity-10 text-${category.visibility ? 'success' : 'warning'}`}
                                            >
                                                {category.visibility ? 'Visible' : 'Hidden'}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="d-flex justify-content-center gap-1">
                                                <button
                                                    className="btn btn-sm btn-outline-primary"
                                                    onClick={() => editCategory(category)}
                                                    title="Edit Category"
                                                >
                                                    <i className="fas fa-edit"></i>
                                                </button>
                                                <button
                                                    className={`btn btn-sm ${category.visibility ? 'btn-outline-warning' : 'btn-outline-success'}`}
                                                    onClick={() => toggleVisibility(category.categoryID)}
                                                    title={category.visibility ? 'Hide' : 'Show'}
                                                >
                                                    <i className={`fas ${category.visibility ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                                                </button>
                                                <button
                                                    className="btn btn-sm btn-outline-danger"
                                                    onClick={() => deleteCategory(category.categoryID)}
                                                    title="Delete Category"
                                                >
                                                    <i className="fas fa-trash"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="6" className="text-center py-4">
                                        {categories.length === 0 ? (
                                            <>
                                                <i className="fas fa-folder-open fs-1 text-muted mb-3"></i>
                                                <h5 className="text-muted">No categories found</h5>
                                                <p className="text-muted">Click "Add Category" to create your first category</p>
                                            </>
                                        ) : (
                                            <>
                                                <i className="fas fa-search fs-1 text-muted mb-3"></i>
                                                <h5 className="text-muted">No matching categories found</h5>
                                                <p className="text-muted">Try adjusting your search or filter criteria</p>
                                            </>
                                        )}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        {showModal && (
            <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
                <div className="modal-dialog modal-lg">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">
                                {selectedCategory ? 'Edit Category' : 'Add New Category'}
                            </h5>
                            <button
                                type="button"
                                className="btn-close"
                                onClick={resetForm}
                            ></button>
                        </div>
                        <form onSubmit={handleSubmit}>
                            <div className="modal-body">
                                <div className="row g-3">
                                    <div className="col-md-6">
                                        <label className="form-label">Category Name *</label>
                                        <input
                                            type="text"
                                            className="form-control"
                                            value={formData.name}
                                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                            required
                                        />
                                    </div>
                                    <div className="col-md-6">
                                        <label className="form-label">Parent Category</label>
                                        <input
                                            type="text"
                                            className="form-control"
                                            value={formData.parentCategory}
                                            onChange={(e) => setFormData({ ...formData, parentCategory: e.target.value })}
                                            placeholder="Enter parent category ID or leave blank"
                                        />
                                    </div>
                                    <div className="col-12">
                                        <label className="form-label">Description</label>
                                        <textarea
                                            className="form-control"
                                            rows="3"
                                            value={formData.description}
                                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                        ></textarea>
                                    </div>
                                    <div className="col-md-6">
                                        <label className="form-label">Display Order</label>
                                        <input
                                            type="number"
                                            className="form-control"
                                            value={formData.displayOrder}
                                            onChange={(e) => setFormData({ ...formData, displayOrder: parseInt(e.target.value) || 0 })}
                                            min="0"
                                        />
                                    </div>
                                    <div className="col-md-6">
                                        <label className="form-label">Visibility</label>
                                        <select
                                            className="form-select"
                                            value={formData.visibility ? 'Visible' : 'Hidden'}
                                            onChange={(e) => setFormData({ ...formData, visibility: e.target.value === 'Visible' })}
                                        >
                                            <option value="Visible">Visible</option>
                                            <option value="Hidden">Hidden</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={resetForm}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                >
                                    {selectedCategory ? 'Update Category' : 'Add Category'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        )}
    </div>
);
};

export default CategoryContent;
