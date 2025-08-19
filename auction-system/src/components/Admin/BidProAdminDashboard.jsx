import React, { useState, useCallback } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";
import DashboardContent from "./Dashboard/DashboardContent";
import UsersContent from "./Users/UsersContent";
import AuctionsContent from "./Products/ProductsContent";
import BidsContent from "./Dashboard/BidsContent";
import CategoriesContent from "./Categories/CategoryContent";
import NotificationToast from "../Common/NotificationToast";
import LoadingOverlay from "../Common/LoadingOverlay";
import "../../styles/admin.scss";

const BidProAdminDashboard = () => {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [notification, setNotification] = useState({
    show: false,
    type: "",
    title: "",
    message: "",
  });
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterType, setFilterType] = useState("");

  const showNotification = useCallback((type, title, message) => {
    setNotification({ show: true, type, title, message });
    setTimeout(
      () => setNotification((prev) => ({ ...prev, show: false })),
      3000
    );
  }, []);

  const showLoading = useCallback(() => setLoading(true), []);
  const hideLoading = useCallback(() => setLoading(false), []);

  const handleAction = useCallback(
    (actionName, details = "") => {
      showLoading();
      setTimeout(() => {
        showNotification(
          "success",
          actionName,
          details || `${actionName} completed successfully`
        );
        hideLoading();
      }, 1000);
    },
    [showLoading, showNotification, hideLoading]
  );

  const toggleSidebar = useCallback(() => {
    setSidebarCollapsed((prev) => !prev);
  }, []);

  const showTab = useCallback((tabId) => {
    setActiveTab(tabId);
    setSearchQuery("");
    setFilterType("");
  }, []);

  const renderTabContent = () => {
    switch (activeTab) {
      case "dashboard":
        return (
          <DashboardContent showTab={showTab} handleAction={handleAction} />
        );
      case "users":
        return (
          <UsersContent
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
            filterType={filterType}
            setFilterType={setFilterType}
            handleAction={handleAction}
          />
        );
      case "products":
        return <AuctionsContent handleAction={handleAction} />;
      case "auctions":
        return <AuctionsContent handleAction={handleAction} />;
      case "bids":
        return <BidsContent handleAction={handleAction} />;
      case "categories":
        return <CategoriesContent handleAction={handleAction} />;
      default:
        return (
          <DashboardContent showTab={showTab} handleAction={handleAction} />
        );
    }
  };

  return (
    <div className="bg-light min-vh-100">
      <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
        rel="stylesheet"
      />
      <link
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
        rel="stylesheet"
      />
      <LoadingOverlay loading={loading} />
      <NotificationToast
        notification={notification}
        setNotification={setNotification}
      />
      <Sidebar
        activeTab={activeTab}
        showTab={showTab}
        sidebarCollapsed={sidebarCollapsed}
      />
      <div
        className="transition-all"
        style={{
          marginLeft: sidebarCollapsed ? "70px" : "250px",
          transition: "all 0.3s ease",
          minHeight: "100vh",
        }}
      >
        <Header
          activeTab={activeTab}
          toggleSidebar={toggleSidebar}
          handleAction={handleAction}
          showTab={showTab}
        />
        <main className="container-fluid p-4">{renderTabContent()}</main>
      </div>
    </div>
  );
};

export default BidProAdminDashboard;
