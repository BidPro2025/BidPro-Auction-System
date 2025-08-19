import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const ModalProfile = ({ show, toggleModal, apiCall }) => {
    const navigate = useNavigate();
    const [profile, setProfile] = useState({
        username: '',
        email: '',
        package: '',
        listingsUsed: 0,
        listingsLimit: 0,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (show) {
            const fetchProfile = async () => {
                try {
                    if (typeof apiCall !== 'function') {
                        throw new Error('API service not available');
                    }
                    
                    setLoading(true);
                    const response = await apiCall('/profile', { method: 'GET' });
                    
                    if (response?.success) {
                        setProfile({
                            username: response.data.username || '',
                            email: response.data.email || '',
                            package: response.data.package || '',
                            listingsUsed: response.data.listingsUsed || 0,
                            listingsLimit: response.data.listingsLimit || 0,
                        });
                    }
                } catch (error) {
                    console.error('Profile fetch error:', error);
                    setError('Failed to load profile');
                } finally {
                    setLoading(false);
                }
            };
            fetchProfile();
        }
    }, [show, apiCall]);

    const handleLogout = async () => {
        try {
            if (typeof apiCall !== 'function') {
                throw new Error('API service not available');
            }

            await apiCall('/logout', { method: 'POST' });
            
            // Clear user data and redirect
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            toggleModal('profile', false);
            navigate('/login');
        } catch (error) {
            console.error('Logout error:', error);
            
            // Still proceed with client-side logout even if API fails
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            toggleModal('profile', false);
            navigate('/login');
        }
    };

    return (
        <div className={`modal fade ${show ? 'show d-block' : ''}`} tabIndex="-1">
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">User Profile</h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={() => toggleModal('profile', false)}
                            aria-label="Close"
                        />
                    </div>
                    <div className="modal-body">
                        {error ? (
                            <div className="alert alert-danger">{error}</div>
                        ) : loading ? (
                            <div className="text-center">
                                <div className="spinner-border text-primary" role="status">
                                    <span className="visually-hidden">Loading...</span>
                                </div>
                            </div>
                        ) : (
                            <>
                                <p><strong>Username:</strong> {profile.username}</p>
                                <p><strong>Email:</strong> {profile.email}</p>
                                <p><strong>Package:</strong> {profile.package}</p>
                                <p><strong>Listings Used:</strong> {profile.listingsUsed}/{profile.listingsLimit}</p>
                            </>
                        )}
                    </div>
                    <div className="modal-footer d-flex justify-content-between">
                        <button
                            type="button"
                            className="btn btn-danger"
                            onClick={handleLogout}
                            disabled={loading}
                        >
                            {loading ? 'Logging out...' : 'Logout'}
                        </button>
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => toggleModal('profile', false)}
                            disabled={loading}
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ModalProfile;