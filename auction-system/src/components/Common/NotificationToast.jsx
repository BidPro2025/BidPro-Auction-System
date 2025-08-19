import React from 'react';

const NotificationToast = ({ notification, setNotification }) => {
    return (
        <div
            className={`position-fixed top-0 end-0 p-3 ${notification.show ? '' : 'd-none'}`}
            style={{ zIndex: 1050, transform: notification.show ? 'translateX(0)' : 'translateX(400px)', transition: 'all 0.3s ease' }}
        >
            <div className={`toast show bg-${notification.type === 'success' ? 'success' : 'danger'} text-white`}>
                <div className="toast-header bg-transparent text-white border-0">
                    <i className={`fas ${notification.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'} me-2`}></i>
                    <strong className="me-auto">{notification.title}</strong>
                    <button
                        type="button"
                        className="btn-close btn-close-white"
                        onClick={() => setNotification(prev => ({ ...prev, show: false }))}
                    ></button>
                </div>
                <div className="toast-body">
                    {notification.message}
                </div>
            </div>
        </div>
    );
};

export default NotificationToast;