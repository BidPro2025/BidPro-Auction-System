BidPro - Online Auction System
A comprehensive online auction platform designed to manage users, products, bidding, invoicing, and real-time auction activities. Built with Java Spring Boot (Backend), React.js (Frontend), and MySQL (Database), BidPro supports role-based access, real-time bidding, and automated invoice generation.
Table of Contents

Features
Modules
Admin Features
Bidder Features
Seller Features


Tech Stack
Database Design
Getting Started
Prerequisites
Installation

Features

User Management: Role-based access for Admin, Seller, and Bidder.
Product Management: Approval, moderation, and category organization for auction items.
Auction & Bidding: Live auctions with configurable rules.
Invoice Generation: Auto-generated PDF invoices for auction winners.
Notifications: Email updates for account activities and auction events.
Real-time Updates: Future scope for real-time bidding via WebSockets.
Search & Filter: Find products by category, price, or seller.

Modules
Admin Features

User Management: Approve/reject user registrations, view profiles, and monitor activity.
Product Management: Approve/reject listings, edit details, and delete inappropriate items.
Category Management: Add, update, or delete categories with referential integrity checks.
Communication: Send email notifications for approvals, auction results, and updates.
Bidding Oversight: View live/past auctions, set rules, and cancel/reset/extend auctions.

Bidder Features

Auction Browsing: View ongoing, upcoming, and past auctions.
Register for Auctions: Securely participate in live bidding.
Bid on Items: Place and modify bids before auction close.
Search & Filter: Find products via keywords, price range, or categories.
Invoice Access: Download PDF invoices for won auctions.

Seller Features

Upload Products: Add items with name, description, category, images, start/end dates, and starting price.
Set Starting Price: Ensure fair bidding minimums.
Track Bids: Monitor bids on own products during auctions.
Edit/Cancel Listings: Modify or withdraw listings before admin approval.
Listing History: View sold, unsold, expired, and active listings.
Dual Role: Participate as a bidder in other sellers’ auctions.

Tech Stack

Backend: Java Spring Boot
Frontend: React.js
Database: MySQL
Build Tools: Maven
Other Tools:
iText for PDF generation
SMTP/SendGrid for email notifications



Database Design

users: Stores user information with roles (Admin, Seller, Bidder) and status.
products: Auction items linked to users and categories.
categories: Product classifications.
auctions: Auction details with start/end times and rules.
bids: Bid details with timestamps and user/product references.
invoices: Auction win transaction records.

Getting Started
Prerequisites

Java: JDK 17 or higher
Maven: For building the backend
Node.js: For running the React frontend
MySQL: Database server
Git: For cloning the repository

Installation

Clone the Repository
git clone https://github.com/<your-org>/BidPro-Auction-System.git
cd BidPro-Auction-System


Backend Setup

Ensure Java and Maven are installed.
Build and run the Spring Boot application:mvn clean install
mvn spring-boot:run




Frontend Setup

Navigate to the frontend directory:cd frontend


Install dependencies and start the React app:npm install
npm start




Database Setup

Create a MySQL database named bidpro.
Run the provided SQL scripts in the database directory to set up the schema.


Configuration

Update application.properties in the backend for MySQL credentials and SMTP/SendGrid settings.
Ensure environment variables or configuration files are set for sensitive data (e.g., API keys).


Access the Application

Backend API: http://localhost:8080
Frontend: http://localhost:3000
