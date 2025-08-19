import React from "react";
import { useNavigate, useLocation } from "react-router-dom";

const Header = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary fixed-top">
      <div className="container">
        <a
          className="navbar-brand fw-bold fs-3"
          href="#"
          style={{ color: "#ffc107" }}
        >
          <i className="bi bi-hammer text-warning me-2"></i>
          BidPro
        </a>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto">
            <li className="nav-item">
              <a
                className={`nav-link${isActive("/") ? " active" : ""}`}
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/");
                }}
                style={{
                  color: isActive("/") ? "white" : "#adb5bd",
                  fontWeight: "500",
                  padding: "8px 15px",
                  borderRadius: "4px",
                  margin: "0 2px",
                }}
              >
                Home
              </a>
            </li>
            <li className="nav-item">
              <a
                className={`nav-link${isActive("/auction") ? " active" : ""}`}
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/auction");
                }}
                style={{
                  color: isActive("/auction") ? "white" : "#adb5bd",
                  fontWeight: "500",
                  padding: "8px 15px",
                  borderRadius: "4px",
                  margin: "0 2px",
                  transition: "all 0.3s ease",
                }}
                onMouseOver={(e) => (e.currentTarget.style.color = "white")}
                onMouseOut={(e) =>
                  (e.currentTarget.style.color = isActive("/auction")
                    ? "white"
                    : "#adb5bd")
                }
              >
                Auctions
              </a>
            </li>
            <li className="nav-item">
              <a
                className={`nav-link${
                  isActive("/categories") ? " active" : ""
                }`}
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/categories");
                }}
                style={{
                  color: isActive("/categories") ? "white" : "#adb5bd",
                  fontWeight: "500",
                  padding: "8px 15px",
                  borderRadius: "4px",
                  margin: "0 2px",
                  transition: "all 0.3s ease",
                }}
                onMouseOver={(e) => (e.currentTarget.style.color = "white")}
                onMouseOut={(e) =>
                  (e.currentTarget.style.color = isActive("/categories")
                    ? "white"
                    : "#adb5bd")
                }
              >
                Categories
              </a>
            </li>
            <li className="nav-item">
              <a
                className={`nav-link${isActive("/help") ? " active" : ""}`}
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/help");
                }}
                style={{
                  color: isActive("/help") ? "white" : "#adb5bd",
                  fontWeight: "500",
                  padding: "8px 15px",
                  borderRadius: "4px",
                  margin: "0 2px",
                  transition: "all 0.3s ease",
                }}
                onMouseOver={(e) => (e.currentTarget.style.color = "white")}
                onMouseOut={(e) =>
                  (e.currentTarget.style.color = isActive("/help")
                    ? "white"
                    : "#adb5bd")
                }
              >
                How It Works
              </a>
            </li>
          </ul>
          <div className="d-flex">
            <button
              className="btn btn-outline-light me-2"
              onClick={() => navigate("/login")}
              style={{
                fontWeight: "500",
                padding: "8px 20px",
                transition: "all 0.3s ease",
              }}
            >
              Login
            </button>
            <button
              className="btn btn-warning text-dark"
              onClick={() => navigate("/register")}
              style={{
                fontWeight: "500",
                padding: "8px 20px",
                transition: "all 0.3s ease",
              }}
            >
              Sign Up
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Header;
