import React, { useState } from 'react';
import Header from './Header';
import UploadProductContent from './UploadProduct/UploadProductContent';
import MyListingsContent from './MyListings/MyListingsContent';
import BiddingActivityContent from './BiddingActivity/BiddingActivityContent';
import SalesInvoicesContent from './SalesInvoices/SalesInvoicesContent';
import AnalyticsContent from './Analytics/AnalyticsContent';
import ModalNotifications from './Common/ModalNotifications';
import ModalProfile from './Common/ModalProfile';
import ModalEditProduct from './Common/ModalEditProduct';
import ModalPromoteProduct from './Common/ModalPromoteProduct';
import ModalCancelProduct from './Common/ModalCancelProduct';
import ModalGenerateInvoice from './Common/ModalGenerateInvoice';
import { apiCall } from '../../utils/helpers';
import '../../styles/seller.scss';


const SellerDashboard = () => {
    const [activeTab, setActiveTab] = useState('upload');
    const [modal, setModal] = useState({
        notifications: false,
        profile: false,
        edit: false,
        promote: false,
        cancel: false,
        invoice: false,
    });
    const [editProductID, setEditProductID] = useState('');
    const [promoteProductID, setPromoteProductID] = useState('');
    const [cancelProductID, setCancelProductID] = useState('');
    const [invoiceProductID, setInvoiceProductID] = useState('');

    const toggleModal = (modalName, show) => {
        setModal(prev => ({ ...prev, [modalName]: show }));
    };

    const renderTabContent = () => {
        switch (activeTab) {
            case 'upload':
                return <UploadProductContent apiCall={apiCall} />;
            case 'listings':
                return (
                    <MyListingsContent
                        setEditProductID={setEditProductID}
                        setPromoteProductID={setPromoteProductID}
                        setCancelProductID={setCancelProductID}
                        toggleModal={toggleModal}
                    />
                );
            case 'bidding':
                return <BiddingActivityContent />;
            case 'invoices':
                return (
                    <SalesInvoicesContent
                        setInvoiceProductID={setInvoiceProductID}
                        toggleModal={toggleModal}
                        apiCall={apiCall}
                    />
                );
            case 'analytics':
                return <AnalyticsContent />;
            default:
                return <UploadProductContent apiCall={apiCall} />;
        }
    };

    return (
        <div className="container-fluid py-4">
            <div className="main-container p-4">
                <Header toggleModal={toggleModal} />
                <ul className="nav nav-pills mb-4" id="dashboardTabs" role="tablist">
                    <li className="nav-item" role="presentation">
                        <button
                            className={`nav-link ${activeTab === 'upload' ? 'active' : ''}`}
                            onClick={() => setActiveTab('upload')}
                        >
                            <i className="bi bi-cloud-upload"></i> Upload Product
                        </button>
                    </li>
                    <li className="nav-item" role="presentation">
                        <button
                            className={`nav-link ${activeTab === 'listings' ? 'active' : ''}`}
                            onClick={() => setActiveTab('listings')}
                        >
                            <i className="bi bi-list-ul"></i> My Listings
                        </button>
                    </li>
                    <li className="nav-item" role="presentation">
                        <button
                            className={`nav-link ${activeTab === 'bidding' ? 'active' : ''}`}
                            onClick={() => setActiveTab('bidding')}
                        >
                            <i className="bi bi-hammer"></i> Bidding Activity
                        </button>
                    </li>
                    <li className="nav-item" role="presentation">
                        <button
                            className={`nav-link ${activeTab === 'invoices' ? 'active' : ''}`}
                            onClick={() => setActiveTab('invoices')}
                        >
                            <i className="bi bi-receipt"></i> Sales & Invoices
                        </button>
                    </li>
                    <li className="nav-item" role="presentation">
                        <button
                            className={`nav-link ${activeTab === 'analytics' ? 'active' : ''}`}
                            onClick={() => setActiveTab('analytics')}
                        >
                            <i className="bi bi-graph-up"></i> Analytics
                        </button>
                    </li>
                </ul>
                <div className="tab-content" id="dashboardTabsContent">
                    {renderTabContent()}
                </div>
            </div>
            <ModalNotifications show={modal.notifications} toggleModal={toggleModal} />
            <ModalProfile show={modal.profile} toggleModal={toggleModal} />
            <ModalEditProduct
                show={modal.edit}
                toggleModal={toggleModal}
                productID={editProductID}
                apiCall={apiCall}
            />
            <ModalPromoteProduct
                show={modal.promote}
                toggleModal={toggleModal}
                productID={promoteProductID}
                apiCall={apiCall}
            />
            <ModalCancelProduct
                show={modal.cancel}
                toggleModal={toggleModal}
                productID={cancelProductID}
                apiCall={apiCall}
            />
            <ModalGenerateInvoice
                show={modal.invoice}
                toggleModal={toggleModal}
                productID={invoiceProductID}
                apiCall={apiCall}
            />
        </div>
    );
};

export default SellerDashboard;