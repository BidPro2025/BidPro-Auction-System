import React from 'react';
import { menuItems } from '../../utils/constants';

const Sidebar = ({ activeTab, showTab, sidebarCollapsed }) => {
    return (
        <nav
            className={`position-fixed top-0 start-0 h-100 bg-white shadow ${sidebarCollapsed ? 'collapsed' : ''}`}
            style={{
                width: sidebarCollapsed ? '70px' : '250px',
                transition: 'all 0.3s ease',
                zIndex: 1000,
                overflowY: 'auto'
            }}
        >
            <div className="p-4 border-bottom">
                <div className="d-flex align-items-center">
                    <div className="rounded-3 p-2 me-3 flex-shrink-0" style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
                        <i className="fas fa-bolt text-white"></i>
                    </div>
                    <span className={`h5 mb-0 fw-bold ${sidebarCollapsed ? 'd-none' : ''}`}>BidPro Admin</span>
                </div>
            </div>
            <div className="p-3">
                <nav className="nav flex-column">
                    {menuItems.map(item => (
                        <a
                            key={item.id}
                            href="#"
                            className={`nav-link d-flex align-items-center p-3 mb-1 rounded ${activeTab === item.id ? 'active bg-primary text-white' : 'text-muted'}`}
                            onClick={(e) => { e.preventDefault(); showTab(item.id); }}
                            style={{ transition: 'all 0.3s ease', justifyContent: sidebarCollapsed ? 'center' : 'flex-start' }}
                        >
                            <i className={item.icon}></i>
                            <span className={`ms-2 ${sidebarCollapsed ? 'd-none' : ''}`}>{item.label}</span>
                        </a>
                    ))}
                </nav>
            </div>
        </nav>
    );
};

export default Sidebar;