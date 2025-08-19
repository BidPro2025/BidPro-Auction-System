import React from "react";

const Footer = () => {
  return (
    <footer className="bg-dark text-light py-5">
      <div className="container">
        <div className="row justify-content-center"> {/* Centered content */}
          <div className="col-lg-6 text-center mb-4"> {/* Reduced width and centered */}
            <h5 className="fw-bold mb-3">
              <i className="bi bi-hammer text-warning me-2"></i>
              BidPro
            </h5>
            <p className="text-light-75">
              The world's premier platform for antique painting auctions.
              Connecting collectors and artists globally.
            </p>
            <div className="d-flex gap-3 justify-content-center">
              <a href="#" className="text-light">
                <i className="bi bi-facebook fs-4"></i>
              </a>
              <a href="#" className="text-light">
                <i className="bi bi-twitter fs-4"></i>
              </a>
              <a href="#" className="text-light">
                <i className="bi bi-instagram fs-4"></i>
              </a>
            </div>
          </div>
        </div>
        <hr className="my-4" />
        <div className="row align-items-center">
          <div className="col-md-12 text-center"> {/* Full width and centered */}
            <p className="mb-0 text-light-75">
              &copy; 2024 BidPro. All rights reserved.
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;