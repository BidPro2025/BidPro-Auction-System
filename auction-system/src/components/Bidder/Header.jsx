import { PURPLE_PRIMARY } from "../../utils/bidderConstants";

const Header = ({ balance }) => {
  return (
    <nav
      className="navbar navbar-expand-lg navbar-dark mb-5"
      style={{ backgroundColor: PURPLE_PRIMARY }}
    >
      <div className="container">
        <a className="navbar-brand" href="#">
          <i className="bi bi-hammer me-2"></i>AuctionHub
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
              <a className="nav-link active" href="#my-bids">
                My Bids
              </a>
            </li>
            <li className="nav-item">
              <a className="nav-link" href="#auctions">
                Auctions
              </a>
            </li>
          </ul>
          <div className="d-flex align-items-center text-white">
            <i className="bi bi-wallet me-2"></i>
            <span>Balance: ${balance.toFixed(2)}</span>
            <button
              className="btn btn-custom ms-3"
              data-bs-toggle="modal"
              data-bs-target="#profileModal"
            >
              <i className="bi bi-person-circle"></i> Profile
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Header;
