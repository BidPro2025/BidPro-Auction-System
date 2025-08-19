import React from "react";
import { useNavigate } from "react-router-dom";
import Header from "../Header";

const Categories = () => {
  const navigate = useNavigate();
  const categories = [
    "Old Master paintings",
    "Pop Art",
    "Abstract Art",
    "Photographs",
  ];

  return (
    <>
      <Header />
      <div className="container mt-5 pt-4">
        <h1 className="mb-4">Browse Categories</h1>
        <div className="row">
          {categories.map((category, index) => (
            <div className="col-md-4 mb-4" key={index}>
              <div className="card h-100">
                <div className="card-body text-center">
                  <h3 className="card-title">{category}</h3>
                  <button
                    className="btn btn-outline-primary"
                    onClick={() => navigate("/login")}
                  >
                    View Items
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};

export default Categories;
