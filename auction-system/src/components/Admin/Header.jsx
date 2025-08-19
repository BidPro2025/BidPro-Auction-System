import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const Header = ({ activeTab, toggleSidebar, handleAction }) => {
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [adminData, setAdminData] = useState(null);
    const [error, setError] = useState(null);
    const dropdownRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchAdminData = async () => {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    throw new Error('No auth token found');
                }

                const decoded = jwtDecode(token);
                const email = decoded.sub; // 'sub' contains email

                const response = await axios.get(`http://localhost:8080/api/admins/email/${email}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                setAdminData(response.data.data);
            } catch (err) {
                console.error('Error fetching admin data:', err);
                setError('Failed to load admin details');
                handleAction('Error', 'Failed to load admin details');
                navigate('/login');
            }
        };

        fetchAdminData();

        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [handleAction, navigate]);

    const toggleDropdown = () => setIsDropdownOpen(!isDropdownOpen);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userData');
        sessionStorage.clear();
        handleAction('Logout', 'You have been logged out');
        setTimeout(() => navigate('/login'), 1000);
    };

    if (error) {
        return null; // Prevent rendering on error
    }
    if (!adminData) {
        return <div>Loading...</div>;
    }

    const isAdmin = adminData.role === 'ADMIN';

    return (
        <header className="bg-white shadow-sm border-bottom sticky-top">
            <div className="container-fluid">
                <div className="row align-items-center py-3">
                    <div className="col-md-6">
                        <div className="d-flex align-items-center">
                            <button className="btn btn-light me-3" onClick={toggleSidebar}>
                                <i className="fas fa-bars"></i>
                            </button>
                            <h1 className="h3 mb-0 fw-bold">
                                {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}
                            </h1>
                        </div>
                    </div>

                    <div className="col-md-6">
                        <div className="d-flex justify-content-end align-items-center">
                            {isAdmin && (
                                <button
                                    className="btn btn-light me-3 position-relative"
                                    onClick={() => handleAction('Notifications', 'Loaded 3 pending notifications')}
                                >
                                    <i className="fas fa-bell"></i>
                                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                                        3
                                    </span>
                                </button>
                            )}

                            <div className="dropdown position-relative" ref={dropdownRef}>
                                <button
                                    className="btn text-white rounded-circle border-0 d-flex align-items-center justify-content-center"
                                    type="button"
                                    onClick={toggleDropdown}
                                    style={{
                                        width: '40px',
                                        height: '40px',
                                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                    }}
                                >
                                    {adminData.firstName ? adminData.firstName.charAt(0).toUpperCase() : 'A'}
                                </button>

                                <ul
                                    className={`dropdown-menu dropdown-menu-end ${isDropdownOpen ? 'show' : ''}`}
                                    style={{ minWidth: '280px', position: 'absolute', top: '100%', right: '0', zIndex: 1000 }}
                                >
                                    <li className="px-3 py-2 border-bottom">
                                        <div className="d-flex align-items-center mb-3">
                                            <div
                                                className="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold me-3"
                                                style={{
                                                    width: '50px',
                                                    height: '50px',
                                                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                                    fontSize: '20px',
                                                }}
                                            >
                                                {adminData.firstName ? adminData.firstName.charAt(0).toUpperCase() : 'A'}
                                                {adminData.lastName ? adminData.lastName.charAt(0).toUpperCase() : ''}
                                            </div>
                                            <div>
                                                <h6 className="mb-0 fw-bold">
                                                    {adminData.firstName || 'Admin'} {adminData.lastName || ''}
                                                </h6>
                                                <small className="text-muted">@{adminData.username || 'admin'}</small>
                                            </div>
                                        </div>

                                        <div className="small">
                                            <div className="d-flex justify-content-between mb-1">
                                                <span className="text-muted">Email-ID:</span>
                                                <span className="fw-semibold">{adminData.email || 'N/A'}</span>
                                            </div>
                                            <div className="d-flex justify-content-between">
                                                <span className="text-muted">Phone:</span>
                                                <span className="fw-semibold">{adminData.phone || 'N/A'}</span>
                                            </div>
                                            {isAdmin && (
                                                <div className="d-flex justify-content-between">
                                                    <span className="text-muted">Role:</span>
                                                    <span className="fw-semibold">Administrator</span>
                                                </div>
                                            )}
                                        </div>
                                    </li>

                                    {isAdmin && (
                                        <li>
                                            <button
                                                className="dropdown-item py-2 border-0 bg-transparent w-100 text-start"
                                                onClick={() => handleAction('Manage Users', 'Navigating to user management')}
                                            >
                                                <i className="fas fa-users me-2"></i>Manage Users
                                            </button>
                                        </li>
                                    )}

                                    <li>
                                        <button
                                            className="dropdown-item py-2 text-danger border-0 bg-transparent w-100 text-start"
                                            onClick={handleLogout}
                                        >
                                            <i className="fas fa-sign-out-alt me-2"></i>Logout
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;