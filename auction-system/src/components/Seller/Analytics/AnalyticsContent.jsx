import React, { useEffect, useRef, useState } from 'react';
import Chart from 'chart.js/auto';

const AnalyticsContent = () => {
    const salesChartRef = useRef(null);
    const categoryChartRef = useRef(null);
    let salesChartInstance = useRef(null);
    let categoryChartInstance = useRef(null);

    // State to store backend data
    const [salesData, setSalesData] = useState([]);
    const [categoryData, setCategoryData] = useState([]);
    const [topProducts, setTopProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fetch data from backend
        const fetchData = async () => {
            try {
                setLoading(true);

                // Replace these with your actual API endpoints
                const salesResponse = await fetch('/api/sales'); 
                const salesJson = await salesResponse.json();
                setSalesData(salesJson); 

                const categoryResponse = await fetch('/api/categories'); 
                const categoryJson = await categoryResponse.json();
                setCategoryData(categoryJson);

                const productsResponse = await fetch('/api/top-products'); 
                const productsJson = await productsResponse.json();
                setTopProducts(productsJson); 

                setLoading(false);
            } catch (err) {
                setError('Failed to fetch data');
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    useEffect(() => {
        // Sales Chart
        if (salesChartRef.current && salesData.labels) {
            if (salesChartInstance.current) {
                salesChartInstance.current.destroy();
            }
            salesChartInstance.current = new Chart(salesChartRef.current, {
                type: 'line',
                data: {
                    labels: salesData.labels || [], // e.g., ['Jan', 'Feb', ...]
                    datasets: [
                        {
                            label: 'Sales ($)',
                            data: salesData.values || [], // e.g., [500, 700, ...]
                            borderColor: '#667eea',
                            backgroundColor: 'rgba(102, 126, 234, 0.2)',
                            fill: true,
                        },
                    ],
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { display: true },
                    },
                    scales: {
                        y: { beginAtZero: true },
                    },
                },
            });
        }

        // Category Chart
        if (categoryChartRef.current && categoryData.labels) {
            if (categoryChartInstance.current) {
                categoryChartInstance.current.destroy();
            }
            categoryChartInstance.current = new Chart(categoryChartRef.current, {
                type: 'pie',
                data: {
                    labels: categoryData.labels || [], // e.g., ['Old Master paintings', ...]
                    datasets: [
                        {
                            data: categoryData.values || [], // e.g., [30, 25, ...]
                            backgroundColor: ['#667eea', '#764ba2', '#ff6b6b', '#4ecdc4'],
                        },
                    ],
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { position: 'right' }, // Fixed 'relative' to a valid position
                    },
                },
            });
        }

        // Cleanup charts on component unmount
        return () => {
            if (salesChartInstance.current) {
                salesChartInstance.current.destroy();
            }
            if (categoryChartInstance.current) {
                categoryChartInstance.current.destroy();
            }
        };
    }, [salesData, categoryData]); // Re-run when data changes

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div className="tab-pane fade" id="analytics" role="tabpanel">
            <div className="row">
                <div className="col-lg-6 mb-4">
                    <div className="card">
                        <div className="card-header">
                            <h6><i className="bi bi-weekly"></i> Sales Performance</h6>
                        </div>
                        <div className="card-body">
                            <canvas ref={salesChartRef} width="400" height="200"></canvas>
                        </div>
                    </div>
                </div>
                <div className="col-lg-6 mb-4">
                    <div className="card">
                        <div className="card-header">
                            <h6><i className="bi bi-pie"></i> Category Distribution</h6>
                        </div>
                        <div className="card-body">
                            <canvas ref={categoryChartRef} width="400" height="200"></canvas> {/* Fixed ref */}
                        </div>
                    </div>
                </div>
                <div className="col-12">
                    <div className="card">
                        <div className="card-header">
                            <h6><i className="bi bi-trophy"></i> Top Performing Products</h6>
                        </div>
                        <div className="card-body">
                            <div className="row">
                                {topProducts.map((product, index) => (
                                    <div className="col-md-4 mb-3" key={index}>
                                        <div className="p-3 bg-light rounded">
                                            <div className="d-flex justify-content-between">
                                                <div>
                                                    <h6 className="mb-1">{product.name}</h6>
                                                    <small className="text-muted">{product.bids} bids</small>
                                                </div>
                                                <div className="text-end">
                                                    <div className="text-success fw-bold">${product.price}</div>
                                                    <i className="bi bi-trophy text-warning"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AnalyticsContent;