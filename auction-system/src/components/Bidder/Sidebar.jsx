import { PURPLE_PRIMARY } from "../../utils/bidderConstants";

const Sidebar = () => {
  return (
    <div
      className="sidebar bg-light p-3"
      style={{ minHeight: "100vh", borderRight: "1px solid #ddd" }}
    >
      <h4 className="mb-4" style={{ color: PURPLE_PRIMARY }}>
        Bidder Menu
      </h4>
      <ul className="nav flex-column">
        <li className="nav-item">
          <a
            className="nav-link"
            href="#my-bids"
            style={{ color: PURPLE_PRIMARY }}
          >
            <i className="bi bi-list-ul me-2"></i>My Bids
          </a>
        </li>
        <li className="nav-item">
          <a
            className="nav-link"
            href="#auctions"
            style={{ color: PURPLE_PRIMARY }}
          >
            <i className="bi bi-hammer me-2"></i>Active Auctions
          </a>
        </li>
      </ul>
    </div>
  );
};

export default Sidebar;
