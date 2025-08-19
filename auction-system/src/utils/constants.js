export const users = [
  {
    id: "yash",
    name: "Yash Medhane",
    email: "Yash@Gmail.com",
    type: "Bidder",
    status: "Active",
    joined: "Dec 15, 2024",
    avatar: "YM",
    avatarBg: "bg-primary",
  },
  {
    id: "atharva ",
    name: "Atharva Shinde",
    email: "ath@email.com",
    type: "Seller",
    status: "Active",
    joined: "Dec 10, 2024",
    avatar: "AS",
    avatarBg: "bg-info",
  },
];

export const recentUsers = [
  {
    id: "akash-misal",
    name: "Akash Misal",
    email: "akash@123.com",
    type: "Bidder",
    status: "Active",
    avatar: "AM",
    avatarBg: "bg-primary",
  },
  {
    id: "hritik-daud",
    name: "Hritik Daud",
    email: "hritik@email.com",
    type: "Seller",
    status: "Active",
    avatar: "HD",
    avatarBg: "bg-info",
  },
  {
    id: "samyak-waghade",
    name: "Sam",
    email: "sam@email.com",
    type: "Bidder",
    status: "Pending",
    avatar: "SW",
    avatarBg: "bg-warning",
  },
];

export const activeAuctions = [
  {
    id: "vintage-watch",
    title: "Vintage Watch",
    seller: "John Seller",
    currentBid: "₹1,200",
    bidsCount: 15,
    endDate: "June 5, 2025",
  },
  {
    id: "antique-vase",
    title: "Antique Vase",
    seller: "Mary Collector",
    currentBid: "₹850",
    bidsCount: 8,
    endDate: "June 3, 2025",
  },
  {
    id: "classic-car",
    title: "Classic Car",
    seller: "Auto Dealer",
    currentBid: "₹25,000",
    bidsCount: 32,
    endDate: "June 10, 2025",
  },
];

export const menuItems = [
  { id: "dashboard", icon: "fas fa-home", label: "Dashboard" },
  { id: "users", icon: "fas fa-users", label: "Manage Users" },
  { id: "products", icon: "fas fa-shopping-cart", label: "Manage Products" },
  { id: "categories", icon: "fas fa-tags", label: "Categories" },
  { id: "bids", icon: "fas fa-chart-line", label: "Manage Bids" },
];

// BACKEND BASE URL
export const API_BASE = "http://localhost:8080/api";
