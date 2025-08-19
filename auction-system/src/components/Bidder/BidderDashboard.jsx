import { useState, useEffect } from "react";
import Header from "../Bidder/Header";
import SearchSection from "../Bidder/Search/SearchSection";
import MyBids from "../Bidder/MyBids/MyBids";
import ActiveAuctions from "../Bidder/ActiveAuctions/ActiveAuctions";
import Packages from "../Bidder/Packages/Packages";
import Modals from "../Bidder/Modals/Modals";
import { products, packages } from "../../utils/product";
import { calculateTimeLeft } from "../../utils/timeUtil";
import { apiCall } from "../../utils/api";
import "../../styles/bidder.scss";

const auctionEndTimes = {
  PRD001: new Date("2025-06-01T18:14:00+05:30"),
  PRD002: new Date("2025-06-02T20:04:00+05:30"),
  PRD003: new Date("2025-06-01T20:31:00+05:30"),
};

function BidderDashboard() {
  const [balance, setBalance] = useState(2500.0);
  const [searchTerm, setSearchTerm] = useState("");
  const [category, setCategory] = useState("");
  const [bidProduct, setBidProduct] = useState(null);
  const [modifyBid, setModifyBid] = useState(null);
  const [packageData, setPackageData] = useState(null);
  const [auctionID, setAuctionID] = useState(null);
  const [autoBid, setAutoBid] = useState(false);
  const [timeLeft, setTimeLeft] = useState({});

  useEffect(() => {
    const updateTimeLeft = () => {
      const now = new Date("2025-06-01T16:04:00+05:30");
      const newTimeLeft = {};
      Object.keys(auctionEndTimes).forEach((productID) => {
        newTimeLeft[productID] = calculateTimeLeft(
          auctionEndTimes[productID],
          now
        );
      });
      setTimeLeft(newTimeLeft);
    };

    updateTimeLeft();
    const interval = setInterval(updateTimeLeft, 60000);
    return () => clearInterval(interval);
  }, []);

  const handleSearch = (e) => setSearchTerm(e.target.value.toLowerCase());

  const handleCategoryFilter = (e) => setCategory(e.target.value.toLowerCase());

  const filteredProducts = products.filter((product) => {
    const matchesSearch =
      product.title.toLowerCase().includes(searchTerm) ||
      product.description.toLowerCase().includes(searchTerm);
    const matchesCategory =
      category === "" || product.category.toLowerCase() === category;
    return matchesSearch && matchesCategory;
  });

  const handlePlaceBid = async () => {
    const amount = parseFloat(document.getElementById("newBidAmount").value);
    const res = await apiCall("/bids", {
      method: "POST",
      body: {
        productID: bidProduct.id,
        amount,
      },
    });
    if (res.success) {
      alert("Bid placed!");
      document.querySelector("#placeBidModal .btn-close").click();
    } else alert(res.message);
  };

  const handleModifyBid = async () => {
    const newAmount = parseFloat(
      document.getElementById("newModifyBidAmount").value
    );
    if (isNaN(newAmount) || newAmount < modifyBid.minBid) {
      alert("New bid amount must be higher than the minimum required bid!");
      return;
    }

    try {
      const response = await apiCall("/modifyBid", {
        bidID: modifyBid.bidID,
        newAmount,
        bidderID: "BIDDER001",
      });
      if (response.success) {
        alert("Bid modified successfully!");
        document
          .getElementById("modifyBidModal")
          .querySelector(".btn-close")
          .click();
      } else {
        alert(`Error: ${response.message}`);
      }
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  const handleBuyPackage = async () => {
    if (balance < packageData.price) {
      alert("Insufficient balance to purchase this package!");
      return;
    }

    try {
      const response = await apiCall("/buyAuctionPackage", {
        userID: "BIDDER001",
        packageID: packageData.name.toUpperCase() + "_PKG",
      });
      if (response.success) {
        setBalance(balance - packageData.price);
        alert("Package purchased successfully!");
        document
          .getElementById("buyPackageModal")
          .querySelector(".btn-close")
          .click();
      } else {
        alert(`Error: ${response.message}`);
      }
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  const handleRegisterAuction = async () => {
    try {
      const response = await apiCall("/registerForAuction", {
        auctionID,
        userID: "BIDDER001",
      });
      if (response.success) {
        alert("Successfully registered for the auction!");
        document
          .getElementById("registerAuctionModal")
          .querySelector(".btn-close")
          .click();
      } else {
        alert(`Error: ${response.message}`);
      }
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  return (
    <div className="container-fluid py-5">
      <div className="main-container p-5">
        <Header balance={balance} />
        <SearchSection
          searchTerm={searchTerm}
          handleSearch={handleSearch}
          category={category}
          handleCategoryFilter={handleCategoryFilter}
        />
        <MyBids
          products={products}
          timeLeft={timeLeft}
          setBidProduct={setBidProduct}
          setModifyBid={setModifyBid}
        />
        <ActiveAuctions
          products={filteredProducts}
          timeLeft={timeLeft}
          setBidProduct={setBidProduct}
          setModifyBid={setModifyBid}
          setAuctionID={setAuctionID}
        />
        <Modals
          balance={balance}
          bidProduct={bidProduct}
          modifyBid={modifyBid}
          packageData={packageData}
          auctionID={auctionID}
          autoBid={autoBid}
          setAutoBid={setAutoBid}
          handlePlaceBid={handlePlaceBid}
          handleModifyBid={handleModifyBid}
          handleBuyPackage={handleBuyPackage}
          handleRegisterAuction={handleRegisterAuction}
          products={products}
          timeLeft={timeLeft}
          setBidProduct={setBidProduct}
        />
      </div>
    </div>
  );
}

export default BidderDashboard;
