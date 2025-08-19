import React, { useState, useEffect } from 'react';
import InvoicesTable from './InvoicesTable';

const SalesInvoicesContent = ({ setInvoiceProductID, toggleModal, apiCall }) => {
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchInvoices = async () => {
            setLoading(true);
            try {
                const response = await apiCall('/invoices', { method: 'GET' });
                if (response.success) {
                    setInvoices(response.data || []);
                } else {
                    setError('Error fetching invoices: ' + response.message);
                }
            } catch (error) {
                setError('Error: ' + error.message);
            } finally {
                setLoading(false);
            }
        };
        fetchInvoices();
    }, [apiCall]);

    const handleNewInvoice = () => {
        toggleModal('invoice', true);
    };

    return (
        <div
            className="tab-pane fade show active"
            id="invoices"
            role="tabpanel"
            aria-labelledby="invoices-tab"
        >
            <div className="row">
                <div className="col-lg-8">
                    <div className="card">
                        <div className="card-header d-flex justify-content-between align-items-center">
                            <h5 className="section-title mb-0">
                                <i className="bi bi-receipt me-2"></i> Sales Invoices
                            </h5>
                            <button
                                className="btn btn-sm btn-primary"
                                onClick={handleNewInvoice}
                            >
                                <i className="bi bi-plus-lg"></i> New Invoice
                            </button>
                        </div>
                        <div className="card-body">
                            {loading ? (
                                <div className="text-center py-4">
                                    <div className="spinner-border" role="status">
                                        <span className="visually-hidden">Loading...</span>
                                    </div>
                                </div>
                            ) : error ? (
                                <div className="text-center py-4 text-danger">
                                    <p>{error}</p>
                                </div>
                            ) : (
                                <InvoicesTable
                                    data={invoices}
                                    apiCall={apiCall}
                                />
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SalesInvoicesContent;