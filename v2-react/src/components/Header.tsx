import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/Header.css";

interface User {
  username: string;
  roles: string[];
  email?: string;
  firstName?: string;
  lastName?: string;
}

const Header: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const jwt = localStorage.getItem("jwtToken");
    const savedUser = localStorage.getItem("user");

    // If no token or no user info, logout
    if (!jwt || !savedUser) {
      setUser(null);
      return;
    }

    try {
      const parsedUser = JSON.parse(savedUser);
      const payload = JSON.parse(atob(jwt.split(".")[1]));

      // Expired token check (JWT "exp" is in seconds)
      if (payload.exp * 1000 < Date.now()) {
        console.log("Token expired");
        localStorage.removeItem("jwtToken");
        localStorage.removeItem("user");
        setUser(null);
      } else {
        setUser(parsedUser);
      }
    } catch (err) {
      console.error("Invalid token or user:", err);
      localStorage.removeItem("jwtToken");
      localStorage.removeItem("user");
      setUser(null);
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("user");
    setUser(null);
    navigate("/login");
  };

  return (
    <header className="header">
      <div className="header-left">
        <Link to="/" className="header-logo">StudyStart</Link>
      </div>

      <nav className="header-nav">
        {!user ? (
          <>
            <Link to="/login" className="header-link">Login</Link>
            <Link to="/register" className="header-link">Register</Link>
          </>
        ) : (
          <>
            <span className="header-username">Hello, {user.username}</span>
            <button onClick={handleLogout} className="logout-button">Logout</button>
          </>
        )}
      </nav>
    </header>
  );
};

export default Header;
