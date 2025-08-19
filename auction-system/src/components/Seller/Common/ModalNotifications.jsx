import React, { useState, useEffect } from 'react';

const ModalNotifications = ({ show, toggleModal, apiCall }) => {
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        if (show) {
            const fetchNotifications = async () => {
                try {
                    const response = await apiCall('/notifications', { method: 'GET' });
                    if (response.success) {
                        setNotifications(response.data || []);
                    } else {
                        console.error('Error fetching notifications:', response.message);
                    }
                } catch (error) {
                    console.error('Error:', error);
                }
            };
            fetchNotifications();
        }
    }, [show, apiCall]);

    return (
        <div className={`modal fade ${show ? 'show d-block' : ''}`} tabIndex="-1">
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Notifications</h5>
                        <button
                            type="button"
                            className="btn-close"
                            onClick={() => toggleModal('notifications', false)}
                        ></button>
                    </div>
                    <div className="modal-body">
                        {notifications.length > 0 ? (
                            <ul className="list-group">
                                {notifications.map((notification, index) => (
                                    <li key={index} className="list-group-item">
                                        {notification.message}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>No notifications available.</p>
                        )}
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => toggleModal('notifications', false)}
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ModalNotifications;