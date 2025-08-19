import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import API from "../../services/api";
import { jwtDecode } from "jwt-decode";

const Login = ({ onLogin }) => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    rememberMe: false,
    userType: "admin",
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loginError, setLoginError] = useState("");

  useEffect(() => {
    console.log("Initial formData:", formData);
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decoded = jwtDecode(token);
        const role = decoded.role?.toLowerCase();
        if (role && ["admin", "seller", "bidder"].includes(role)) {
          console.log("User already logged in, navigating to:", `/${role}`);
          navigate(`/${role}`, { replace: true });
        } else {
          console.warn("Invalid role in token:", role);
          localStorage.removeItem("token");
        }
      } catch (err) {
        console.warn("Invalid token on mount:", err);
        localStorage.removeItem("token");
      }
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: "" }));
    if (loginError) setLoginError("");
  };

  const handleUserTypeSelect = (type) => {
    setFormData((prev) => ({ ...prev, userType: type }));
    console.log("Selected userType:", type);
    setErrors({});
    setLoginError("");
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.email.trim()) newErrors.email = "Email is required";
    else if (!/^\S+@\S+\.\S+$/.test(formData.email))
      newErrors.email = "Invalid email address";
    if (!formData.password) newErrors.password = "Password is required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    setLoginError("");

    try {
      console.log("Submitting formData:", formData);
      const endpoint =
        formData.userType === "admin"
          ? "/api/admins/login"
          : "/api/auth/signin";
      const { data } = await API.post(endpoint, {
        email: formData.email,
        password: formData.password,
      });

      console.log("Login response:", data);

      if (!data.success) {
        throw new Error(data.message || "Signin failed");
      }

      const userDTO = data.data;
      const token = userDTO.token;

      if (!token) {
        throw new Error("No token received from server");
      }
      localStorage.setItem("token", token);
      onLogin();

      let role = userDTO?.role?.toString().toLowerCase() || null;
      if (!role) {
        try {
          const decoded = jwtDecode(token);
          console.log("Decoded JWT:", decoded);
          role = decoded.role?.toLowerCase();
        } catch (err) {
          console.warn("Failed to decode JWT:", err);
        }
      }

      if (!role || !["admin", "seller", "bidder"].includes(role)) {
        console.warn(
          "No valid role, using formData.userType:",
          formData.userType
        );
        role = formData.userType.toLowerCase();
        setLoginError(
          "Warning: Role not provided by server. Using selected role."
        );
      } else if (role !== formData.userType.toLowerCase()) {
        console.warn(
          "Role mismatch: server role:",
          role,
          "vs userType:",
          formData.userType
        );
        setLoginError(
          `Warning: Selected role (${formData.userType}) does not match server role (${role}). Using server role.`
        );
      }

      console.log("Final role for navigation:", role);
      console.log("Attempting navigation to:", `/${role}`);
      navigate(`/${role}`, { replace: true });
    } catch (err) {
      console.error("Login error:", err.response?.data || err.message);
      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        "Invalid credentials. Please try again.";
      setLoginError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-6">
            <div className="card shadow-sm border-0 overflow-hidden">
              <div className="card-header bg-primary text-white text-center py-3">
                <h2 className="mb-0 fw-bold">
                  <i className="bi bi-box-seam me-2"></i> BidPro Login
                </h2>
              </div>

              <div className="card-body p-4 p-md-5">
                {loginError && (
                  <div className="alert alert-danger mb-4">
                    <i className="bi bi-exclamation-circle me-2"></i>
                    {loginError}
                  </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                  <div className="mb-4">
                    <label className="form-label fw-bold text-dark mb-3">
                      Login As
                    </label>
                    <div className="d-flex gap-2">
                      {["admin", "bidder", "seller"].map((type) => (
                        <div
                          key={type}
                          className={`flex-grow-1 text-center py-2 border cursor-pointer rounded ${
                            formData.userType === type
                              ? "bg-primary text-white"
                              : "border-light"
                          }`}
                          onClick={() => handleUserTypeSelect(type)}
                        >
                          {type.charAt(0).toUpperCase() + type.slice(1)}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="mb-4">
                    <label htmlFor="email" className="form-label fw-bold">
                      Email
                    </label>
                    <input
                      type="email"
                      id="email"
                      className={`form-control ${
                        errors.email ? "is-invalid" : ""
                      }`}
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder="Enter your email"
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
                      className={`form-control ${
                        errors.password ? "is-invalid" : ""
                      }`}
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Enter your password"
                      required
                      disabled={isSubmitting}
                    />
                    {errors.password && (
                      <div className="invalid-feedback">{errors.password}</div>
                    )}
                  </div>

                  <div className="mb-4 form-check">
                    <input
                      type="checkbox"
                      className="form-check-input"
                      id="rememberMe"
                      name="rememberMe"
                      checked={formData.rememberMe}
                      onChange={handleChange}
                      disabled={isSubmitting}
                    />
                    <label htmlFor="rememberMe" className="form-check-label">
                      Remember me
                    </label>
                  </div>

                  <div className="d-grid mb-4">
                    <button
                      type="submit"
                      className="btn btn-primary fw-bold"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? (
                        <>
                          <span
                            className="spinner-border spinner-border-sm me-2"
                            role="status"
                            aria-hidden="true"
                          ></span>
                          Logging in…
                        </>
                      ) : (
                        `Login as ${
                          formData.userType.charAt(0).toUpperCase() +
                          formData.userType.slice(1)
                        }`
                      )}
                    </button>
                  </div>
                </form>

                <div className="text-center">
                  <Link to="/" className="btn btn-outline-primary mb-3">
                    <i className="bi bi-house-door me-1"></i> Home
                  </Link>
                  <Link to="/forgot-password" className="d-block mb-2">
                    Forgot password?
                  </Link>
                  <p className="mb-0">
                    Don't have an account? <Link to="/register">Register</Link>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
