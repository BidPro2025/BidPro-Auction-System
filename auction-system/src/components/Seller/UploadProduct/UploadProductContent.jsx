import React, { useState, useEffect } from "react";
import Sidebar from "../Sidebar";
import { apiCall } from "../../../utils/api"; // Ensure correct path

const UploadProductContent = ({}) => {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    categoryID: "",
    condition: "",
    startingPrice: "",
    incrementGap: "",
    auctionEndDate: "",
    sellerID: "0692ba14-da4c-42f5-b255-431c41f7c4aa", // Default admin ID
    auctionStartDate: "",
    productStatus: "PENDING",
    productCondition: "",
    promotionStatus: "",
  });
  const [categories, setCategories] = useState([]);
  const [imageFiles, setImageFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [categoryLoading, setCategoryLoading] = useState(true);

  // Fetch categories from backend on component mount
  useEffect(() => {
    const fetchCategories = async () => {
      setCategoryLoading(true);
      try {
        const response = await apiCall("/categories", { method: "GET" });
        if (response.success) {
          setCategories(response.data || []);
        } else {
          setError("Error fetching categories: " + response.message);
        }
      } catch (error) {
        setError("Error fetching categories: " + error.message);
      } finally {
        setCategoryLoading(false);
      }
    };
    fetchCategories();
  }, [apiCall]);

  const handleChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files).slice(0, 5); // Limit to 5 images
    setImageFiles(files);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const fd = new FormData();
    Object.keys(formData).forEach((k) => fd.append(k, formData[k]));
    imageFiles.forEach((f) => fd.append("images", f));

    try {
      const res = await apiCall("/products", { method: "POST", body: fd });
      if (res.success) {
        alert("Product uploaded!");
        setFormData({
          title: "",
          description: "",
          categoryID: "",
          condition: "",
          startingPrice: "",
          incrementGap: "",
          auctionEndDate: "",
          sellerID: "0692ba14-da4c-42f5-b255-431c41f7c4aa", // Reset to default admin ID
          auctionStartDate: "",
          productStatus: "PENDING",
          productCondition: "",
          promotionStatus: "",
        });
        setImageFiles([]);
      } else {
        setError(res.message);
      }
    } catch (error) {
      setError("Error uploading product: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="tab-pane fade show active" id="upload" role="tabpanel">
      <div className="row">
        <div className="col-lg-8">
          <div className="card">
            <div className="card-header">
              <h5 className="section-title mb-0">
                <i className="bi bi-plus-circle"></i> Upload New Product
              </h5>
            </div>
            <div className="card-body">
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}
              <form id="uploadForm" onSubmit={handleSubmit}>
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Product Title *</label>
                    <input
                      type="text"
                      className="form-control"
                      id="title"
                      placeholder="e.g., Vintage Antique Vase"
                      value={formData.title}
                      onChange={handleChange}
                      required
                      disabled={loading}
                    />
                  </div>
                  <div className="col-12">
                    <label className="form-label">Description *</label>
                    <textarea
                      className="form-control"
                      id="description"
                      rows="4"
                      placeholder="Detailed product description..."
                      value={formData.description}
                      onChange={handleChange}
                      required
                      disabled={loading}
                    ></textarea>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Category *</label>
                    <select
                      className="form-select"
                      id="categoryID"
                      value={formData.categoryID}
                      onChange={handleChange}
                      required
                      disabled={categoryLoading || loading}
                    >
                      <option value="">Select Category</option>
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                    {categoryLoading && (
                      <div className="form-text">Loading categories...</div>
                    )}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Condition *</label>
                    <select
                      className="form-select"
                      id="condition"
                      value={formData.condition}
                      onChange={handleChange}
                      required
                      disabled={loading}
                    >
                      <option value="">Select Condition</option>
                      <option value="NEW">New</option>
                      <option value="LIKE_NEW">Like New</option>
                      <option value="GOOD">Good</option>
                      <option value="FAIR">Fair</option>
                      <option value="POOR">Poor</option>
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Starting Price ($) *</label>
                    <input
                      type="number"
                      className="form-control"
                      id="startingPrice"
                      step="0.01"
                      placeholder="100.00"
                      value={formData.startingPrice}
                      onChange={handleChange}
                      required
                      min="0.01"
                      disabled={loading}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Increment Gap ($) *</label>
                    <input
                      type="number"
                      className="form-control"
                      id="incrementGap"
                      step="0.01"
                      placeholder="5.00"
                      value={formData.incrementGap}
                      onChange={handleChange}
                      required
                      min="0.01"
                      disabled={loading}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Auction End Date *</label>
                    <input
                      type="datetime-local"
                      className="form-control"
                      id="auctionEndDate"
                      value={formData.auctionEndDate}
                      onChange={handleChange}
                      required
                      min={new Date().toISOString().slice(0, 16)}
                      disabled={loading}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Auction Start Date *</label>
                    <input
                      type="datetime-local"
                      className="form-control"
                      id="auctionStartDate"
                      value={formData.auctionStartDate}
                      onChange={handleChange}
                      required
                      min={new Date().toISOString().slice(0, 16)}
                      disabled={loading}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Product Status *</label>
                    <select
                      className="form-select"
                      id="productStatus"
                      value={formData.productStatus}
                      onChange={handleChange}
                      required
                      disabled={loading || true}
                    >
                      <option value="PENDING">Pending</option>
                      <option value="APPROVED">Approved</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Product Condition *</label>
                    <select
                      className="form-select"
                      id="productCondition"
                      value={formData.productCondition}
                      onChange={handleChange}
                      required
                      disabled={loading}
                    >
                      <option value="">Select Condition</option>
                      <option value="NEW">New</option>
                      <option value="USED">Used</option>
                      <option value="REFURBISHED">Refurbished</option>
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Promotion Status *</label>
                    <select
                      className="form-select"
                      id="promotionStatus"
                      value={formData.promotionStatus}
                      onChange={handleChange}
                      required
                      disabled={loading}
                    >
                      <option value="">Select Promotion Status</option>
                      <option value="NONE">None</option>
                      <option value="PROMOTED">Promoted</option>
                      <option value="FEATURED">Featured</option>
                    </select>
                  </div>
                  <div className="col-12">
                    <label className="form-label">Product Images</label>
                    <input
                      type="file"
                      className="form-control"
                      id="images"
                      multiple
                      accept="image/*"
                      onChange={handleImageChange}
                      disabled={loading}
                    />
                    <div className="form-text">
                      Upload up to 5 images (JPG, PNG, GIF)
                    </div>
                  </div>
                  <input
                    type="hidden"
                    id="sellerID"
                    value={formData.sellerID}
                  />
                </div>
                <div className="mt-4">
                  <button
                    type="submit"
                    className="btn btn-custom btn-lg"
                    disabled={loading || categoryLoading}
                  >
                    {loading ? (
                      <span
                        className="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true"
                      ></span>
                    ) : (
                      <i className="bi bi-cloud-upload"></i>
                    )}
                    Upload Product
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-lg ms-2"
                    onClick={() => {
                      setFormData({
                        title: "",
                        description: "",
                        categoryID: "",
                        condition: "",
                        startingPrice: "",
                        incrementGap: "",
                        auctionEndDate: "",
                        sellerID: "0692ba14-da4c-42f5-b255-431c41f7c4aa", // Reset to default admin ID
                        auctionStartDate: "",
                        productStatus: "PENDING",
                        productCondition: "",
                        promotionStatus: "",
                      });
                      setImageFiles([]);
                    }}
                    disabled={loading}
                  >
                    <i className="bi bi-arrow-clockwise"></i> Reset
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <Sidebar />
        </div>
      </div>
    </div>
  );
};

export default UploadProductContent;