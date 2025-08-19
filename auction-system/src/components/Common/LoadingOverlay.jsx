import React from 'react';

const LoadingOverlay = ({ loading }) => {
    return loading && (
        <>
            <div className="position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50" style={{ zIndex: 9998 }}></div>
            <div className="position-fixed top-50 start-50 translate-middle" style={{ zIndex: 9999 }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        </>
    );
};

export default LoadingOverlay;