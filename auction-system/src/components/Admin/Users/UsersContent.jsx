import React, { useEffect, useState } from 'react';
import axios from 'axios';
import UserTable from './UserTable';

const UsersContent = ({ searchQuery, setSearchQuery, filterType, setFilterType, handleAction }) => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showAddForm, setShowAddForm] = useState(false);
    const [formData, setFormData] = useState({
        email: '',
        username: '',
        firstName: '',
        lastName: '',
        phone: '',
        passwordHash: '',
        role: 'BIDDER',
    });
    const [formError, setFormError] = useState('');
    const [formSuccess, setFormSuccess] = useState('');

    useEffect(() => {
        const fetchUsers = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await axios.get('http://localhost:8080/api/admins/users', {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('token')} `,
                    },
                    withCredentials: true,
                });
                const data = response.data.data || response.data;
                if (Array.isArray(data)) {
                    setUsers(data);
                } else {
                    throw new Error('Unexpected response format');
                }
            } catch (err) {
                console.error('Error fetching users:', err);
                if (err.response) {
                    console.log('Response data:', err.response.data);
                    console.log('Response status:', err.response.status);
                    console.log('Response headers:', err.response.headers);
                    if (err.response.headers['content-type'] !== 'application/json') {
                        setError('Server returned non-JSON response. Check API endpoint or server configuration.');
                    } else {
                        setError(err.response.data.message || 'Failed to fetch users.');
                    }
                } else if (err.request) {
                    setError('No response from server. Ensure backend is running at http://localhost:8080.');
                } else {
                    setError(`Error: ${err.message} `);
                }
            } finally {
                setLoading(false);
            }
        };

        fetchUsers();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setFormError('');
        setFormSuccess('');

        try {
            const postResponse = await axios.post('http://localhost:8080/api/admins/users', {
                ...formData,
                status: 'PENDING',
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')} `,
                },
                withCredentials: true,
            });

            setFormSuccess('User added successfully!');
            setFormData({
                email: '',
                username: '',
                firstName: '',
                lastName: '',
                phone: '',
                passwordHash: '',
                role: 'BIDDER',
            });
            setShowAddForm(false);
            const getResponse = await axios.get('http://localhost:8080/api/admins/users', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')} `,
                },
                withCredentials: true,
            });
            const data = getResponse.data.data || getResponse.data;
            if (Array.isArray(data)) {
                setUsers(data);
            }
        } catch (err) {
            if (err.response) {
                console.log('Response data:', err.response.data);
                console.log('Response status:', err.response.status);
                console.log('Response headers:', err.response.headers);
                if (err.response.headers['content-type'] !== 'application/json') {
                    setFormError('Server returned non-JSON response. Check API endpoint or server configuration.');
                } else {
                    setFormError(err.response.data.message || 'Failed to add user.');
                }
            } else if (err.request) {
                setFormError('No response from server. Ensure backend is running at http://localhost:8080.');
            } else {
                setFormError(`Error: ${err.message} `);
            }
            console.error('Error adding user:', err);
        }
    };

    const handleStatusUpdate = async (userID, status) => {
        try {
            const response = await axios.put(`http://localhost:8080/api/admins/users/${userID}/status`,
                { status },
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    },
                    withCredentials: true,
                }
            );
            if (response.data.success) {
                setUsers(users.map(user => user.userID === userID ? response.data.data : user));
                handleAction('Status Update', `User status updated to ${status}`);
            } else {
                setError(response.data.message || 'Failed to update user status');
                handleAction('Error', response.data.message || 'Failed to update user status');
            }
        } catch (err) {
            console.error('Error updating user status:', err);
            if (err.response) {
                console.log('Response data:', err.response.data);
                console.log('Response status:', err.response.status);
                console.log('Response headers:', err.response.headers);
                if (err.response.headers['content-type'] !== 'application/json') {
                    setError('Server returned non-JSON response. Check API endpoint or server configuration.');
                } else {
                    setError(err.response.data.message || 'Failed to update user status.');
                }
            } else if (err.request) {
                setError('No response from server. Ensure backend is running at http://localhost:8080.');
            } else {
                setError(`Error: ${err.message}`);
            }
            handleAction('Error', 'Failed to update user status');
        }
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: 300 }}>
                <div className="spinner-border text-primary" />
            </div>
        );
    }

    if (error) {
        return <div className="text-red-500">{error}</div>;
    }

    const filtered = users.filter(u => {
        const type = u.role === 'ADMIN' ? 'admin'
            : u.role === 'SELLER' ? 'seller'
                : u.role === 'BOTH' ? 'both'
                    : 'bidder';
        const matchesType = !filterType || type === filterType.toLowerCase();
        const matchesSearch = (u.firstName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
            (u.lastName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
            (u.email || '').toLowerCase().includes(searchQuery.toLowerCase());
        return matchesType && matchesSearch;
    });

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2>User Management</h2>
                <button
                    className="btn btn-primary"
                    onClick={() => setShowAddForm(!showAddForm)}
                >
                    <i className="fas fa-plus me-2"></i>{showAddForm ? 'Cancel' : 'Add User'}
                </button>
            </div>
            {showAddForm && (
                <div className="card border-0 shadow-sm rounded-4 mb-4">
                    <div className="card-body">
                        {formError && <p className="text-red-500 mb-4">{formError}</p>}
                        {formSuccess && <p className="text-green-500 mb-4">{formSuccess}</p>}
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium">Email</label>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Username</label>
                                <input
                                    type="text"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">First Name</label>
                                <input
                                    type="text"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Last Name</label>
                                <input
                                    type="text"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Phone</label>
                                <input
                                    type="text"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    className="form-control"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Password</label>
                                <input
                                    type="password"
                                    name="passwordHash"
                                    value={formData.passwordHash}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Role</label>
                                <select
                                    name="role"
                                    value={formData.role}
                                    onChange={handleChange}
                                    className="form-select"
                                >
                                    <option value="BIDDER">Bidder</option>
                                    <option value="SELLER">Seller</option>
                                    <option value="BOTH">Both</option>
                                </select>
                            </div>
                            <button type="submit" className="btn btn-primary w-100">
                                Add User
                            </button>
                        </form>
                    </div>
                </div>
            )}
            <div className="card border-0 shadow-sm rounded-4">
                <div className="card-header bg-white border-0">
                    <div className="row g-3 align-items-center">
                        <div className="col-md-8">
                            <div className="input-group">
                                <span className="input-group-text"><i className="fas fa-search"></i></span>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Search users..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                />
                            </div>
                        </div>
                        <div className="col-md-4">
                            <select
                                className="form-select"
                                value={filterType}
                                onChange={(e) => setFilterType(e.target.value)}
                            >
                                <option value="">All Types</option>
                                <option value="admin">Admin</option>
                                <option value="seller">Seller</option>
                                <option value="both">Both</option>
                                <option value="bidder">Bidder</option>
                            </select>
                        </div>
                    </div>
                </div>
                <UserTable users={filtered} handleAction={handleAction} handleStatusUpdate={handleStatusUpdate} />
            </div>
        </div>
    );
};

export default UsersContent;
