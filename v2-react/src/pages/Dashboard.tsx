import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import '../styles/Dasboard.css'

interface Role {
  name: string;
}

interface UserResponse {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles?: string[] | Role[];
}

export default function HomePage() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    const storedUser = localStorage.getItem("user");
    const username = storedUser ? JSON.parse(storedUser).username : null;

    if (!token || !username) {
      setUser(null);
      setLoading(false);
      return;
    }

    fetch(`http://localhost:8080/studystart/api/users/${username}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })
      .then(async (res) => {
        const text = await res.text();
        let data;
        try {
          data = JSON.parse(text);
        } catch {
          throw new Error("Invalid response from server");
        }

        if (res.ok) {
          setUser(data);
        } else {
          setError(data?.error || "Failed to fetch user info");
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading user info...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  if (!user) {
    return (
      <div className="home-container">
        <h2 className="home-title">Welcome, Guest!</h2>
        <p style={{ color: "gray" }}>Functionality is limited.</p>
        <div className="home-buttons">
          <button
            onClick={() => navigate("/profiles")}
            className="btn btn-blue"
          >
            Go to Profiles
          </button>
          <button
            onClick={() => navigate("/documents")}
            className="btn btn-green"
          >
            Go to Documents
          </button>
        </div>
      </div>
    );
  }

  const rolesDisplay = user?.roles
    ? (user.roles as any[]).map((r) => (typeof r === "string" ? r : r.name)).join(", ")
    : "No roles";

  return (
    <div className="home-container">
      <h2 className="home-title">
        Welcome, {user?.firstName} {user?.lastName}!
      </h2>

      <div className="home-info">
        <p><strong>Username:</strong> {user?.username}</p>
        <p><strong>Email:</strong> {user?.email}</p>

        {user?.roles && (
          <p>
            <strong>Roles:</strong> {rolesDisplay}
          </p>
        )}
      </div>

      <div className="home-buttons">
        <button
          onClick={() => navigate("/profiles")}
          className="btn btn-blue"
        >
          Go to Profiles
        </button>

        <button
          onClick={() => navigate("/documents")}
          className="btn btn-green"
        >
          Go to Documents
        </button>
      </div>
    </div>
  );
}
