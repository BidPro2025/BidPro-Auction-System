import React from "react";
import Header from "../Header";

const Help = () => {
  const faqs = [
    {
      question: "How do I place a bid?",
      answer: "Navigate to the auction item and click the 'Place Bid' button.",
    },
    {
      question: "What payment methods do you accept?",
      answer: "We accept all major credit cards, PayPal, and bank transfers.",
    },
  ];

  return (
    <>
      <Header />
      <div className="container mt-5 pt-4">
        <h1 className="mb-4">Help Center</h1>
        <div className="row">
          <div className="col-md-8">
            <div className="card">
              <div className="card-body">
                <h3 className="card-title">Frequently Asked Questions</h3>
                <div className="accordion" id="faqAccordion">
                  {faqs.map((faq, index) => (
                    <div className="accordion-item" key={index}>
                      <h2 className="accordion-header">
                        <button
                          className="accordion-button collapsed"
                          type="button"
                          data-bs-toggle="collapse"
                          data-bs-target={`#faq${index}`}
                        >
                          {faq.question}
                        </button>
                      </h2>
                      <div
                        id={`faq${index}`}
                        className="accordion-collapse collapse"
                        data-bs-parent="#faqAccordion"
                      >
                        <div className="accordion-body">{faq.answer}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <h3 className="card-title">Contact Support</h3>
                <p>Can't find what you're looking for?</p>
                <a href="/contact" className="btn btn-primary">
                  Contact Us
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Help;
