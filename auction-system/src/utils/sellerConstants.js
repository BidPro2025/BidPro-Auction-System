  export const listings = [
  {
    productID: "PRD001",
    title: "Vintage Antique Vase",
    image: "https://via.placeholder.com/50x50",
    category: "Antiques",
    status: "Active",
    startingPrice: 100.0,
    currentBid: 150.0,
    bids: 8,
    endDate: "2025-06-15",
  },
  {
    productID: "PRD002",
    title: "Vintage Wall Clock",
    image: "https://via.placeholder.com/50x50",
    category: "Home & Garden",
    status: "Pending",
    startingPrice: 75.0,
    currentBid: null,
    bids: 0,
    endDate: "2025-06-20",
  },
  {
    productID: "PRD003",
    title: "Silver Jewelry Set",
    image: "https://via.placeholder.com/50x50",
    category: "Jewelry",
    status: "Expired",
    startingPrice: 200.0,
    currentBid: 350.0,
    bids: 15,
    endDate: "2025-05-30",
  },
];

export const productData = {
  PRD001: {
    title: "Vintage Antique Vase",
    description: "A beautiful antique vase from the 19th century.",
    categoryID: "CAT001",
    condition: "GOOD",
    startingPrice: 100.0,
    incrementGap: 5.0,
    auctionEndDate: "2025-06-15T12:00",
  },
  PRD002: {
    title: "Vintage Wall Clock",
    description: "A classic wall clock in working condition.",
    categoryID: "CAT005",
    condition: "VERY_GOOD",
    startingPrice: 75.0,
    incrementGap: 5.0,
    auctionEndDate: "2025-06-20T12:00",
  },
};

export const biddingData = [
  {
    productID: "PRD001",
    title: "Vintage Antique Vase",
    bids: 8,
    highestBid: 150.0,
    status: "Active",
    bidsData: [
      {
        bidder: "yash",
        amount: 150.0,
        time: "2 hours ago",
        status: "Winning",
      },
      {
        bidder: "Hritik",
        amount: 140.0,
        time: "4 hours ago",
        status: "Outbid",
      },
      {
        bidder: "Atharva",
        amount: 130.0,
        time: "6 hours ago",
        status: "Outbid",
      },
    ],
  },
  {
    productID: "PRD003",
    title: "Silver Jewelry Set",
    bids: 15,
    highestBid: 350.0,
    status: "Expired",
    bidsData: [
      {
        bidder: "Atharva",
        amount: 350.0,
        time: "1 day ago",
        status: "Won",
      },
      {
        bidder: "Akash",
        amount: 340.0,
        time: "1 day ago",
        status: "Lost",
      },
    ],
  },
];

export const invoices = [
  {
    invoiceID: "INV001",
    product: "Silver Jewelry Set",
    buyer: "Jiten",
    amount: 350.0,
    date: "2025-05-30",
    status: "Paid",
  },
  {
    invoiceID: "INV002",
    product: "Vintage Clock",
    buyer: "Sam",
    amount: 125.0,
    date: "2025-05-28",
    status: "Pending",
  },
];
