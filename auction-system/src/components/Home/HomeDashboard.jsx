import { useState, useEffect } from "react";
import Header from "./Header";
import HeroSection from "./HeroSection";
import FeaturesSection from "./FeaturesSection";
import FeaturedAuctions from "./FeaturedAuctions";
import StatsSection from "./StatsSection";
import Footer from "./Footer";

const HomeDashboard = () => {
    const [activeUsers, setActiveUsers] = useState(0);
    const [liveAuctions, setLiveAuctions] = useState(0);
    const [totalSales, setTotalSales] = useState(0);
    const [satisfactionRate, setSatisfactionRate] = useState("0%");

    useEffect(() => {
        // Mocking API calls or real-time updates
        setActiveUsers(50000);
        setLiveAuctions(156);
        setTotalSales("$2.5M+");
        setSatisfactionRate("98%");

        // You can add real API calls here if needed
    }, []);

    return (
        <div className="dashboard-container">
            <Header />
            <HeroSection />
            <FeaturesSection />
            <Footer />
        </div>
    );
};

export default HomeDashboard;