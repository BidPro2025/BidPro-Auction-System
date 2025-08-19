import React from "react";
import { useNavigate } from "react-router-dom";
import Header from "../Header";

const Auctions = () => {
  const navigate = useNavigate();

  return (
    <>
      <Header />
      <div className="container mt-5 pt-4">
        <h1 className="mb-4">Current Auctions</h1>
        <div className="row">
          <div className="col-md-4 mb-4">
            <div className="card h-100">
              <img
                src="https://via.placeholder.com/300x200"
                className="card-img-top"
                alt="Auction Item"
              />
              <div className="card-body">
                <h5 className="card-title">Vintage Painting</h5>
                <p className="card-text">Current bid: $450</p>
                <button
                  onClick={() => navigate("/login")}
                  className="btn btn-primary"
                >
                  Place Bid
                </button>
              </div>
            </div>
          </div>
          {/* You can replicate more auction items below */}
        </div>
      </div>
    </>
  );
};

export default Auctions;
