import React, { useState } from 'react';
import { Search, ChevronDown, ChevronRight, Book, MessageCircle, Phone, Mail, Clock, CheckCircle } from 'lucide-react';

const HelpCenter = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedFaq, setExpandedFaq] = useState(null);

  const categories = [
    {
      title: 'Getting Started',
      icon: Book,
      articles: 12,
      description: 'Learn the basics of using BidPro'
    },
    {
      title: 'Bidding & Auctions',
      icon: CheckCircle,
      articles: 18,
      description: 'How to bid and participate in auctions'
    },
    {
      title: 'Account & Security',
      icon: CheckCircle,
      articles: 8,
      description: 'Manage your account and security settings'
    },
    {
      title: 'Payments & Fees',
      icon: CheckCircle,
      articles: 15,
      description: 'Understanding payments and fee structure'
    },
    {
      title: 'Selling on BidPro',
      icon: CheckCircle,
      articles: 10,
      description: 'How to list and sell your artwork'
    },
    {
      title: 'Shipping & Delivery',
      icon: CheckCircle,
      articles: 7,
      description: 'Information about shipping and delivery'
    }
  ];

  const faqs = [
    {
      question: 'How do I place a bid on an auction?',
      answer: 'To place a bid, simply click on the auction item you\'re interested in, enter your bid amount (which must be higher than the current bid), and click "Place Bid". You\'ll need to be logged in and have a verified payment method.'
    },
    {
      question: 'What happens if I win an auction?',
      answer: 'Congratulations! You\'ll receive an email confirmation and invoice. Payment is due within 48 hours. Once payment is confirmed, we\'ll arrange secure shipping of your artwork to your registered address.'
    },
    {
      question: 'How are artworks authenticated?',
      answer: 'Every artwork goes through our rigorous authentication process by certified art experts. We examine provenance, condition, materials, and style. Each piece comes with a certificate of authenticity.'
    },
    {
      question: 'Can I return an item if I\'m not satisfied?',
      answer: 'We offer a 14-day return policy for items that are significantly different from their description. All returns must be in original condition and properly packaged. Return shipping costs may apply.'
    },
    {
      question: 'What are the buyer\'s fees?',
      answer: 'BidPro charges a buyer\'s premium of 15% on the hammer price for items up to $50,000, and 12% for amounts above $50,000. This covers authentication, insurance, and platform services.'
    },
    {
      question: 'How do I sell my artwork on BidPro?',
      answer: 'To sell, create a seller account, submit high-quality photos and detailed information about your artwork. Our experts will review and authenticate your piece before listing it for auction.'
    }
  ];

  const contactOptions = [
    {
      icon: MessageCircle,
      title: 'Live Chat',
      description: 'Chat with our support team',
      availability: 'Available 24/7',
      action: 'Start Chat'
    },
    {
      icon: Phone,
      title: 'Phone Support',
      description: 'Speak directly with an expert',
      availability: 'Mon-Fri, 9AM-6PM EST',
      action: 'Call Now'
    },
    {
      icon: Mail,
      title: 'Email Support',
      description: 'Send us a detailed message',
      availability: 'Response within 24 hours',
      action: 'Send Email'
    }
  ];

  const toggleFaq = (index) => {
    setExpandedFaq(expandedFaq === index ? null : index);
  };

  return (
    <div className="min-h-screen bg-gray-50 pt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Header */}
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Help Center</h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-8">
            Find answers to your questions and get the support you need
          </p>
          
          {/* Search Bar */}
          <div className="max-w-2xl mx-auto relative">
            <Search className="h-5 w-5 absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search for help articles..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-4 py-4 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-amber-500 text-lg"
            />
          </div>
        </div>

        {/* Help Categories */}
        <div className="mb-16">
          <h2 className="text-2xl font-bold text-gray-900 mb-8">Browse by Category</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {categories.map((category, index) => {
              const IconComponent = category.icon;
              return (
                <div key={index} className="bg-white rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow cursor-pointer group">
                  <div className="flex items-start space-x-4">
                    <div className="flex-shrink-0">
                      <div className="w-12 h-12 bg-amber-100 rounded-lg flex items-center justify-center group-hover:bg-amber-200 transition-colors">
                        <IconComponent className="h-6 w-6 text-amber-600" />
                      </div>
                    </div>
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-gray-900 mb-2">{category.title}</h3>
                      <p className="text-gray-600 text-sm mb-3">{category.description}</p>
                      <p className="text-amber-600 text-sm font-medium">{category.articles} articles</p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* FAQ Section */}
        <div className="mb-16">
          <h2 className="text-2xl font-bold text-gray-900 mb-8">Frequently Asked Questions</h2>
          <div className="bg-white rounded-xl shadow-sm">
            {faqs.map((faq, index) => (
              <div key={index} className="border-b border-gray-200 last:border-b-0">
                <button
                  onClick={() => toggleFaq(index)}
                  className="w-full px-6 py-4 text-left flex items-center justify-between hover:bg-gray-50 transition-colors"
                >
                  <span className="text-lg font-medium text-gray-900">{faq.question}</span>
                  {expandedFaq === index ? (
                    <ChevronDown className="h-5 w-5 text-gray-500" />
                  ) : (
                    <ChevronRight className="h-5 w-5 text-gray-500" />
                  )}
                </button>
                {expandedFaq === index && (
                  <div className="px-6 pb-4">
                    <p className="text-gray-600 leading-relaxed">{faq.answer}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Contact Support */}
        <div className="bg-white rounded-2xl p-8 shadow-sm">
          <div className="text-center mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Still Need Help?</h2>
            <p className="text-gray-600">Our support team is here to assist you</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {contactOptions.map((option, index) => {
              const IconComponent = option.icon;
              return (
                <div key={index} className="text-center p-6 border border-gray-200 rounded-xl hover:border-amber-300 transition-colors">
                  <div className="inline-flex items-center justify-center w-16 h-16 bg-amber-100 rounded-full mb-4">
                    <IconComponent className="h-8 w-8 text-amber-600" />
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">{option.title}</h3>
                  <p className="text-gray-600 mb-2">{option.description}</p>
                  <div className="flex items-center justify-center space-x-1 text-sm text-gray-500 mb-4">
                    <Clock className="h-4 w-4" />
                    <span>{option.availability}</span>
                  </div>
                  <button className="bg-amber-500 hover:bg-amber-600 text-white px-6 py-2 rounded-lg font-medium transition-colors">
                    {option.action}
                  </button>
                </div>
              );
            })}
          </div>
        </div>

        {/* Additional Resources */}
        <div className="mt-16 text-center">
          <h3 className="text-xl font-bold text-gray-900 mb-4">Additional Resources</h3>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button className="bg-slate-900 hover:bg-slate-800 text-white px-6 py-3 rounded-lg font-medium transition-colors">
              Download User Guide
            </button>
            <button className="border border-gray-300 hover:border-gray-400 text-gray-700 px-6 py-3 rounded-lg font-medium transition-colors">
              Video Tutorials
            </button>
            <button className="border border-gray-300 hover:border-gray-400 text-gray-700 px-6 py-3 rounded-lg font-medium transition-colors">
              Community Forum
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HelpCenter;