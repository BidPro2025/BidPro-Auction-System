import React from 'react';

const InvoicesTable = ({ data, setInvoiceProductID, toggleModal, apiCall }) => {
    const downloadInvoice = async (invoiceID) => {
        try {
            const response = await apiCall(`/invoices/${invoiceID}/download`, { method: 'GET' });
            if (response.success && response.data.url) {
                // Trigger download
                const link = document.createElement('a');
                link.href = response.data.url;
                link.download = `invoice_${invoiceID}.pdf`;
                link.click();
            } else {
                alert('Error downloading invoice: ' + response.message);
            }
        } catch (error) {
            alert('Error: ' + error.message);
        }
    };

    const remindBuyer = async (invoiceID) => {
        try {
            const response = await apiCall(`/invoices/${invoiceID}/remind`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
            });
            if (response.success) {
                alert('Reminder sent successfully!');
            } else {
                alert('Error sending reminder: ' + response.message);
            }
        } catch (error) {
            alert('Error: ' + error.message);
        }
    };

    return (
        <div className="table-responsive">
            <table className="table table-hover">
                <thead>
                    <tr>
                        <th>Invoice ID</th>
                        <th>Product</th>
                        <th>Buyer</th>
                        <th>Amount</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {data && data.length > 0 ? (
                        data.map(item => (
                            <tr key={item.invoiceID}>
                                <td>{item.invoiceID}</td>
                                <td>{item.productTitle || 'N/A'}</td>
                                <td>{item.buyerName || 'N/A'}</td>
                                <td className="text-success fw-bold">
                                    ${(item.amount || 0).toFixed(2)}
                                </td>
                                <td>
                                    {item.date ? new Date(item.date).toLocaleDateString() : 'N/A'}
                                </td>
                                <td>
                                    <span
                                        className={`badge ${
                                            item.status === 'PAID' ? 'bg-success' : 'bg-warning'
                                        }`}
                                    >
                                        {item.status || 'Unknown'}
                                    </span>
                                </td>
                                <td>
                                    {item.status === 'PAID' ? (
                                        <button
                                            className="btn btn-outline-primary btn-sm"
                                            onClick={() => downloadInvoice(item.invoiceID)}
                                        >
                                            <i className="bi bi-download"></i> Download
                                        </button>
                                    ) : (
                                        <button
                                            className="btn btn-outline-warning btn-sm"
                                            onClick={() => remindBuyer(item.invoiceID)}
                                        >
                                            <i className="bi bi-bell"></i> Remind
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="7" className="text-center">
                                No invoices available
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default InvoicesTable;