import React, { useState } from "react";
import "../styles/Register.css";
import { Link, useNavigate } from "react-router-dom";


export default function LoginPage() {
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    setError(null);
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/studystart/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (response.ok) {
        setMessage("Log in successful!");
        console.log("JWT: ", data.token);
         console.log("Full response:", data);

        const user = {
          username: data.username,
          roles: Array.isArray(data.roles) ? data.roles : [data.roles || "GUEST"], // Fallback to "USER" if roles is missing
        };
        localStorage.setItem("user", JSON.stringify(user));
        localStorage.setItem("jwtToken", data.token);

        await new Promise((resolve) => setTimeout(resolve, 100));

        navigate('/');
        setFormData({
          username: "",
          password: ""
        });
      } else {
        setError(data?.error || "Log in failed");
      }
    } catch (err: any) {
      setError(err.message || "Network error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page">
      <div className="register-container">
        <h2 className="register-title">Log in</h2>

        {error && <div className="error-message">{error}</div>}
        {message && <div className="success-message">{message}</div>}

        <form onSubmit={handleSubmit} className="register-form">
          <input
            name="username"
            placeholder="Username"
            value={formData.username}
            onChange={handleChange}
            required
          />

          <input
            name="password"
            type="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
          />

          <button type="submit" disabled={loading}>
            {loading ? "Entering..." : "Login"}
          </button>
        </form>

        <p className="login-link">
          Don't have an account? <a href="/register">Register</a>
        </p>
      </div>
    </div>
  );
}
