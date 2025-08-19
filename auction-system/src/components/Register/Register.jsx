import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import API from "../../services/api";

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    role: "ADMIN",
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [signupError, setSignupError] = useState("");

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    const val = type === "checkbox" ? checked : value;
    setFormData((prev) => ({ ...prev, [name]: val }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: "" }));
    if (signupError) setSignupError("");
  };

  const handleRoleSelect = (type) => {
    setFormData((prev) => ({ ...prev, role: type.toUpperCase() }));
    setErrors({}); // Clear errors when role changes
  };

  const validateForm = () => {
    const newErrors = {};
    // Always required
    if (!formData.firstName.trim())
      newErrors.firstName = "First name is required";
    if (!formData.lastName.trim()) newErrors.lastName = "Last name is required";
    if (!formData.email.trim()) newErrors.email = "Email is required";
    else if (!/^\S+@\S+\.\S+$/.test(formData.email))
      newErrors.email = "Invalid email";
    if (!formData.password) newErrors.password = "Password is required";
    else if (formData.password.length < 4)
      newErrors.password = "At least 4 chars";
    if (!formData.confirmPassword)
      newErrors.confirmPassword = "Confirm password is required";
    else if (formData.password !== formData.confirmPassword)
      newErrors.confirmPassword = "Passwords do not match";

    // Extra fields for Seller / Bidder
    if (formData.role !== "ADMIN") {
      if (!formData.username.trim())
        newErrors.username = "Username is required";
      else if (formData.username.length < 3)
        newErrors.username = "At least 3 chars";
      if (!formData.phone.trim()) newErrors.phone = "Phone is required";
      else if (!/^\+?\d{10,15}$/.test(formData.phone))
        newErrors.phone = "Invalid phone";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    setSignupError("");

    let payload;
    if (formData.role === "ADMIN") {
      // Admin payload with separate firstName, lastName, and confirmPassword
      payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
        role: "ADMIN",
      };
    } else {
      // Seller / Bidder full payload
      payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        username: formData.username,
        email: formData.email,
        phone: formData.phone,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
        role: formData.role,
      };
    }

    const endpoint =
      formData.role === "ADMIN" ? "/api/admins" : "/api/auth/signup";

    try {
      const { data } = await API.post(endpoint, payload);
      localStorage.setItem("token", data.token || data.data?.token);

      // Debug log
      console.log("Registered role:", formData.role);
      const role = formData.role.trim().toLowerCase();
      if (role === "admin") navigate("/admin");
      else if (role === "seller") navigate("/seller");
      else if (role === "bidder") navigate("/bidder");
      else navigate("/");
    } catch (err) {
      // Enhanced error handling
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data?.errors?.join(", ") ||
        "Registration failed. Please try again.";
      setSignupError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const isAdmin = formData.role === "ADMIN";

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-6">
            <div className="card shadow-sm border-0 overflow-hidden">
              <div className="card-header bg-primary text-white text-center py-3">
                <h2 className="mb-0 fw-bold">
                  <i className="bi bi-person-plus me-2"></i> BidPro Signup
                </h2>
              </div>

              <div className="card-body p-4 p-md-5">
                {signupError && (
                  <div className="alert alert-danger mb-4">
                    <i className="bi bi-exclamation-circle me-2"></i>
                    {signupError}
                  </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                  <div className="mb-4">
                    <label className="form-label fw-bold text-dark mb-3">
                      Register As
                    </label>
                    <div className="d-flex gap-2">
                      {["admin", "seller", "bidder"].map((type) => (
                        <div
                          key={type}
                          className={`flex-grow-1 text-center py-2 border cursor-pointer rounded ${
                            formData.role === type.toUpperCase()
                              ? "bg-primary text-white"
                              : "border-light"
                          }`}
                          onClick={() => handleRoleSelect(type)}
                        >
                          {type.charAt(0).toUpperCase() + type.slice(1)}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="row g-3 mb-4">
                    <div className="col-md-6">
                      <label htmlFor="firstName" className="form-label fw-bold">
                        First Name
                      </label>
                      <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        className={`form-control ${
                          errors.firstName ? "is-invalid" : ""
                        }`}
                        value={formData.firstName}
                        onChange={handleChange}
                        required
                        disabled={isSubmitting}
                      />
                      {errors.firstName && (
                        <div className="invalid-feedback">
                          {errors.firstName}
                        </div>
                      )}
                    </div>
                    <div className="col-md-6">
                      <label htmlFor="lastName" className="form-label fw-bold">
                        Last Name
                      </label>
                      <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        className={`form-control ${
                          errors.lastName ? "is-invalid" : ""
                        }`}
                        value={formData.lastName}
                        onChange={handleChange}
                        required
                        disabled={isSubmitting}
                      />
                      {errors.lastName && (
                        <div className="invalid-feedback">
                          {errors.lastName}
                        </div>
                      )}
                    </div>
                  </div>

                  {!isAdmin && (
                    <>
                      <div className="mb-4">
                        <label
                          htmlFor="username"
                          className="form-label fw-bold"
                        >
                          Username
                        </label>
                        <input
                          type="text"
                          id="username"
                          name="username"
                          className={`form-control ${
                            errors.username ? "is-invalid" : ""
                          }`}
                          value={formData.username}
                          onChange={handleChange}
                          required
                          disabled={isSubmitting}
                        />
                        {errors.username && (
                          <div className="invalid-feedback">
                            {errors.username}
                          </div>
                        )}
                      </div>

                      <div className="mb-4">
                        <label htmlFor="phone" className="form-label fw-bold">
                          Phone
                        </label>
                        <input
                          type="tel"
                          id="phone"
                          name="phone"
                          className={`form-control ${
                            errors.phone ? "is-invalid" : ""
                          }`}
                          value={formData.phone}
                          onChange={handleChange}
                          required
                          disabled={isSubmitting}
                        />
                        {errors.phone && (
                          <div className="invalid-feedback">{errors.phone}</div>
                        )}
                      </div>
                    </>
                  )}

                  <div className="mb-4">
                    <label htmlFor="email" className="form-label fw-bold">
                      Email
                    </label>
                    <input
                      type="email"
                      id="email"
                      name="email"
                      className={`form-control ${
                        errors.email ? "is-invalid" : ""
                      }`}
                      value={formData.email}
                      onChange={handleChange}
                      required
                      disabled={isSubmitting}
                    />
                    {errors.email && (
                      <div className="invalid-feedback">{errors.email}</div>
                    )}
                  </div>

                  <div className="mb-4">
                    <label htmlFor="password" className="form-label fw-bold">
                      Password
                    </label>
                    <input
                      type="password"
                      id="password"
                      name="password"
                      className={`form-control ${
                        errors.password ? "is-invalid" : ""
                      }`}
                      value={formData.password}
                      onChange={handleChange}
                      required
                      disabled={isSubmitting}
                    />
                    {errors.password && (
                      <div className="invalid-feedback">{errors.password}</div>
                    )}
                  </div>

                  <div className="mb-4">
                    <label
                      htmlFor="confirmPassword"
                      className="form-label fw-bold"
                    >
                      Confirm Password
                    </label>
                    <input
                      type="password"
                      id="confirmPassword"
                      name="confirmPassword"
                      className={`form-control ${
                        errors.confirmPassword ? "is-invalid" : ""
                      }`}
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      required
                      disabled={isSubmitting}
                    />
                    {errors.confirmPassword && (
                      <div className="invalid-feedback">
                        {errors.confirmPassword}
                      </div>
                    )}
                  </div>

                  <div className="d-grid">
                    <button
                      type="submit"
                      className="btn btn-primary py-2 fw-bold"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? (
                        <>
                          <span
                            className="spinner-border spinner-border-sm me-2"
                            role="status"
                            aria-hidden="true"
                          ></span>
                          Signing Up...
                        </>
                      ) : (
                        <>
                          <i className="bi bi-person-plus me-2"></i>
                          Sign Up as{" "}
                          {formData.role.charAt(0) +
                            formData.role.slice(1).toLowerCase()}
                        </>
                      )}
                    </button>
                  </div>

                  <div className="text-center mt-4">
                    <Link to="/" className="btn btn-outline-primary mb-3">
                      <i className="bi bi-house-door me-1"></i> Home
                    </Link>
                    <p className="mb-0 text-dark">
                      Already have an account?{" "}
                      <Link to="/login" className="text-primary fw-medium">
                        <i className="bi bi-box-arrow-in-right me-1"></i>Login
                      </Link>
                    </p>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
