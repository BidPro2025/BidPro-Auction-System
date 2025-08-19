import React from 'react';

const FeaturesSection = () => {
  const features = [
    {
      icon: 'bi-shield-check',
      title: 'Secure & Trusted',
      description: 'Advanced security measures and verified user authentication for safe transactions.'
    },
    {
      icon: 'bi-broadcast',
      title: 'Real-Time Bidding',
      description: 'Live updates during auctions keep the excitement high and ensure fair bidding.'
    },
    {
      icon: 'bi-globe',
      title: 'Global Access',
      description: 'Bid from anywhere in the world, 24/7 access to international art collections.'
    },
    {
      icon: 'bi-currency-dollar',
      title: 'Flexible Pricing',
      description: 'Sellers can set custom starting prices and auction durations to maximize value.'
    }
  ];

  return (
    <section className="py-5 bg-light">
      <div className="container">
        <div className="row">
          <div className="col-lg-8 mx-auto text-center mb-5">
            <h2 className="display-5 fw-bold mb-3">Why Choose BidPro?</h2>
            <p className="lead text-muted">
              Experience the future of art auctions with our cutting-edge platform
            </p>
          </div>
        </div>
        <div className="row g-4">
          {features.map((feature, index) => (
            <div key={index} className="col-md-6 col-lg-3">
              <div className="card h-100 border-0 shadow-sm">
                <div className="card-body text-center p-4">
                  <div className="feature-icon mb-3">
                    <i className={`${feature.icon} display-4 text-primary`}></i>
                  </div>
                  <h5 className="card-title fw-bold">{feature.title}</h5>
                  <p className="card-text text-muted">{feature.description}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default FeaturesSection;