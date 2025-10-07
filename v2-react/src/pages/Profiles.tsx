import "../styles/Profiles.css";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

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

export default function ProfilesPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [filtered, setFiltered] = useState<UserResponse[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  const token = localStorage.getItem("jwtToken");
  const username = localStorage.getItem("username");

  const isGuest = !token || !username;

  useEffect(() => {
    fetch(`http://localhost:8080/studystart/api/users`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
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
          setUsers(data);
          setFiltered(data);
        } else {
          setError(data?.error || "Failed to fetch user info");
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [token]);

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.toLowerCase();
    setSearch(value);
    setFiltered(
      users.filter(
        (u) =>
          u.username.toLowerCase().includes(value) ||
          u.firstName.toLowerCase().includes(value) ||
          u.lastName.toLowerCase().includes(value)
      )
    );
  };

  const isAdmin = () => {
    if (!token) return false;
    try {
      const tokenData = JSON.parse(atob(token.split(".")[1]));
      return tokenData.roles && tokenData.roles.includes("ADMIN");
    } catch {
      return false;
    }
  };

  const handleDelete = async (usernameToDelete: string) => {
    if (!window.confirm(`Delete profile '${usernameToDelete}'?`)) return;
    try {
      const res = await fetch(`http://localhost:8080/studystart/api/users/${usernameToDelete}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        setUsers(users.filter((u) => u.username !== usernameToDelete));
        setFiltered(filtered.filter((u) => u.username !== usernameToDelete));
        alert("User deleted successfully");
      } else {
        const data = await res.json();
        alert(data.error || "Failed to delete user");
      }
    } catch (err) {
      alert("Error deleting user: " + err);
    }
  };

  const handleEdit = (user: UserResponse) => {
    alert(`Editing profile: ${user.username}\n(you can implement an edit form here)`);
  };

  if (loading) return <p>Loading profiles...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <div className="profiles-container">
      <h2 className="profiles-title">
        {isGuest ? "Welcome, Guest!" : "All Profiles"}
      </h2>

      {isGuest && (
        <p style={{ color: "gray" }}>
          You can view profiles, but functionality is limited.
        </p>
      )}

      <div className="profiles-search-bar">
        <input
          type="text"
          value={search}
          onChange={handleSearch}
          placeholder="Search by username or name..."
        />
        <button onClick={() => navigate("/")} className="btn btn-blue">
          Back Home
        </button>
      </div>

      <div className="profiles-list">
        {filtered.length === 0 && <p className="no-profiles">No profiles found.</p>}

        {filtered.map((user) => (
          <div key={user.username} className="profile-card">
            <div className="profile-info">
              <p className="profile-name">{user.firstName} {user.lastName}</p>
              <p className="profile-username">@{user.username}</p>
              <p className="profile-email">{user.email}</p>
              {user.roles && (
                <p className="profile-roles">
                  <strong>Roles:</strong>{" "}
                  {Array.isArray(user.roles)
                    ? user.roles.map((r) => (typeof r === "string" ? r : r.name)).join(", ")
                    : ""}
                </p>
              )}
            </div>

            {/* Only show actions to admins */}
            {isAdmin() && (
              <div className="profile-actions">
                <button onClick={() => handleEdit(user)} className="profile-btn edit">Edit</button>
                <button onClick={() => handleDelete(user.username)} className="profile-btn delete">Delete</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
