import React from 'react';

const Term = () => {
  return (
    <div className="py-8 px-4 max-w-4xl mx-auto">
      <h1 className="mb-6 text-3xl font-bold text-center text-gray-900">BidPro Terms and Conditions</h1>
      
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">1. Introduction</h2>
        <p className="text-gray-600 mt-2">
          Welcome to BidPro ("the Platform"). These Terms and Conditions govern your use of our antique painting auction services. 
          By accessing or using the Platform, you agree to be bound by these Terms.
        </p>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">2. User Accounts</h2>
        <ul className="text-gray-600 mt-2 list-disc pl-5">
          <li>You must be at least 18 years old to create an account</li>
          <li>You are responsible for maintaining the confidentiality of your account credentials</li>
          <li>All information provided must be accurate and current</li>
        </ul>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">3. Auction Process</h2>
        <ul className="text-gray-600 mt-2 list-disc pl-5">
          <li>All bids are binding and cannot be retracted</li>
          <li>The winning bidder must complete the purchase within 48 hours</li>
          <li>BidPro charges a 10% buyer's premium on all successful bids</li>
        </ul>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">4. Seller Obligations</h2>
        <ul className="text-gray-600 mt-2 list-disc pl-5">
          <li>Sellers must provide accurate descriptions of all items</li>
          <li>All paintings must be authentic with provenance documentation</li>
          <li>Sellers agree to a 15% commission on successful sales</li>
        </ul>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">5. Payment and Fees</h2>
        <ul className="text-gray-600 mt-2 list-disc pl-5">
          <li>All payments must be made in INR (Indian Rupees)</li>
          <li>We accept bank transfers, UPI, and major credit cards</li>
          <li>A 2.5% processing fee applies to all credit card transactions</li>
        </ul>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">6. Authenticity Guarantee</h2>
        <ul className="text-gray-600 mt-2 list-disc pl-5">
          <li>All items are verified by our expert team prior to listing</li>
          <li>Buyers have 14 days to report authenticity concerns</li>
          <li>Refunds will be issued for proven inauthentic items</li>
        </ul>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">7. Limitation of Liability</h2>
        <p className="text-gray-600 mt-2">
          BidPro shall not be liable for any indirect, incidental, or consequential damages arising from use of the Platform, 
          including but not limited to errors in item descriptions or bidding malfunctions.
        </p>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">8. Governing Law</h2>
        <p className="text-gray-600 mt-2">
          These Terms shall be governed by and construed in accordance with the laws of India. Any disputes shall be subject 
          to the exclusive jurisdiction of the courts in Mumbai.
        </p>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-blue-600">9. Changes to Terms</h2>
        <p className="text-gray-600 mt-2">
          We reserve the right to modify these Terms at any time. Continued use of the Platform constitutes acceptance of 
          the modified Terms.
        </p>
      </div>

      <div className="mt-6 p-4 bg-blue-100 rounded-lg">
        <p className="mb-2 text-gray-700"><strong>Last Updated:</strong> {new Date().toLocaleDateString()}</p>
        <p className="mb-0 text-gray-700"><strong>Contact:</strong> legal@bidpro.com for any questions regarding these Terms.</p>
      </div>
    </div>
  );
};

export default Term;