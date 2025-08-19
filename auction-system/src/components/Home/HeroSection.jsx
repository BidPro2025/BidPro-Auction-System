import React from "react";
import { useNavigate } from "react-router-dom";

const HeroSection = () => {
  const navigate = useNavigate();

  const handleStartBidding = () => {
    navigate('/auctions');
  };

  const handleSellArt = () => {
    navigate('/sell-art');
  };

  return (
    <section
      className="hero-section py-5 min-vh-100 d-flex align-items-center position-relative"
      style={{
        backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('https://images.pexels.com/photos/459203/pexels-photo-459203.jpeg?auto=compress&cs=tinysrgb&w=1920&h=1080&fit=crop')`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundAttachment: 'fixed',
        marginTop: "76px",
      }}
    >
      <div className="container">
        <div className="row align-items-center">
          <div className="col-lg-6">
            <div className="bg-dark bg-opacity-75 p-5 rounded-4 shadow-lg">
              <h1 className="display-3 fw-bold text-white mb-4">
                Discover Rare Antique Paintings
              </h1>
              <p className="lead text-white mb-4 fs-5">
                Join the world's most trusted online auction platform for antique
                paintings. Bid securely from anywhere, anytime with real-time
                updates.
              </p>
              <div className="d-flex gap-3 flex-wrap">
                <button 
                  className="btn btn-primary btn-lg px-5 py-3 rounded-pill shadow"
                  onClick={handleStartBidding}
                  style={{ cursor: 'pointer' }}
                >
                  <i className="bi bi-hammer me-2"></i>
                  Start Bidding
                </button>
                <button 
                  className="btn btn-outline-light btn-lg px-5 py-3 rounded-pill"
                  onClick={handleSellArt}
                  style={{ cursor: 'pointer' }}
                >
                  <i className="bi bi-palette me-2"></i>
                  Sell Your Art
                </button>
              </div>
            </div>
          </div>
          
          <div className="col-lg-6 mt-5 mt-lg-0">
            <div className="row g-4">
              {/* Featured Auction Card */}
              <div className="col-12">
                <div className="bg-white rounded-4 shadow-lg p-4 position-relative overflow-hidden">
                  <div className="row align-items-center mt-3">
                    <div className="col-5">
                      <img 
                        src="https://images.pexels.com/photos/1492364/pexels-photo-1492364.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop" 
                        alt="Renaissance Masterpiece" 
                        className="img-fluid rounded-3 shadow-sm"
                        style={{ aspectRatio: '4/3', objectFit: 'cover' }}
                        
                      />
                    </div>
                    <div className="col-7">
                      <h5 className="fw-bold mb-2">Renaissance Masterpiece</h5>
                      <p className="text-muted mb-2 small">Oil on Canvas, 1580</p>
                      <div className="d-flex justify-content-between align-items-center mb-2">
                        <span className="fw-bold text-primary fs-4">RS-15,240</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Gallery Preview */}
              <div className="col-6">
                <div className="bg-white rounded-4 shadow p-3 text-center">
                  <img 
                    src="https://images.pexels.com/photos/1183992/pexels-photo-1183992.jpeg?auto=compress&cs=tinysrgb&w=300&h=200&fit=crop" 
                    alt="Classical Portrait" 
                    className="img-fluid rounded-3 mb-2"
                    style={{ aspectRatio: '3/2', objectFit: 'cover' }}
                  />
                  <h6 className="fw-bold mb-1">Classical Portrait</h6>
                  <small className="text-primary fw-bold">RS-8,500</small>
                </div>
              </div>

              <div className="col-6">
                <div className="bg-white rounded-4 shadow p-3 text-center">
                  <img 
                    src="https://images.pexels.com/photos/1579708/pexels-photo-1579708.jpeg?auto=compress&cs=tinysrgb&w=300&h=200&fit=crop" 
                    alt="Baroque Landscape" 
                    className="img-fluid rounded-3 mb-2"
                    style={{ aspectRatio: '3/2', objectFit: 'cover' }}
                  />
                  <h6 className="fw-bold mb-1">Baroque Landscape</h6>
                  <small className="text-success fw-bold">RS-12,300</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Decorative Elements */}
      <div className="position-absolute top-0 start-0 w-100 h-100 overflow-hidden pointer-events-none">
        <div className="position-absolute" style={{ top: '20%', left: '10%', opacity: '0.1' }}>
          <i className="bi bi-palette" style={{ fontSize: '8rem', color: 'white' }}></i>
        </div>
        <div className="position-absolute" style={{ bottom: '20%', right: '15%', opacity: '0.1' }}>
          <i className="bi bi-brush" style={{ fontSize: '6rem', color: 'white' }}></i>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;