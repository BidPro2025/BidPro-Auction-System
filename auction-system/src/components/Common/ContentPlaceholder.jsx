import React from 'react';

const ContentPlaceholder = ({ icon, title, description, buttonText, action }) => {
    return (
        <div className="card border-0 shadow-sm rounded-4">
            <div className="card-body text-center py-5">
                <i className={`${icon} display-1 text-muted mb-4`} style={{ opacity: 0.5 }}></i>
                <h3>{title}</h3>
                <p className="mb-4 text-muted">{description}</p>
                <button className="btn btn-primary" onClick={() => action(buttonText, `${buttonText} completed`)}>
                    {buttonText}
                </button>
            </div>
        </div>
    );
};

export default ContentPlaceholder;