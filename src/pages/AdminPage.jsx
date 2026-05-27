import { useEffect, useState } from "react";
import { deleteUser, fetchAdminUsers, promoteUserToAdmin } from "../api/watchlistApi";
import "./AdminPage.css";

export function AdminPage() {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionMessage, setActionMessage] = useState("");

  useEffect(() => {
    let active = true;

    async function loadUsers() {
      setIsLoading(true);

      try {
        const loadedUsers = await fetchAdminUsers();

        if (!active) {
          return;
        }

        setUsers(loadedUsers);
        setError("");
      } catch (loadError) {
        if (!active) {
          return;
        }

        setError(loadError.message);
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    loadUsers();

    return () => {
      active = false;
    };
  }, []);

  const handlePromote = async (userId) => {
    setActionMessage("");
    const updatedUser = await promoteUserToAdmin(userId);
    setUsers((prev) => prev.map((user) => (user.id === userId ? updatedUser : user)));
    setActionMessage(`Promoted ${updatedUser.username} to admin.`);
  };

  const handleDelete = async (userId, username) => {
    const confirmed = window.confirm(
      `Delete ${username} and all of their watchlist entries? This cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    setActionMessage("");
    await deleteUser(userId);
    setUsers((prev) => prev.filter((user) => user.id !== userId));
    setActionMessage(`Deleted ${username} and their watchlist entries.`);
  };

  return (
    <section className="admin-page">
      <div className="admin-card">
        <div className="admin-header">
          <p className="admin-kicker">Admin</p>
          <h1>User management</h1>
          <p className="admin-subtitle">
            Promote normal users to admins or delete users together with their watchlist data.
          </p>
        </div>

        {isLoading ? <p className="admin-message">Loading users...</p> : null}
        {!isLoading && error ? <p className="admin-error">{error}</p> : null}
        {!isLoading && !error && actionMessage ? <p className="admin-message">{actionMessage}</p> : null}

        {!isLoading && !error ? (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Roles</th>
                  <th>Permissions</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.username}</td>
                    <td>{user.roles.join(", ")}</td>
                    <td>{user.permissions.join(", ")}</td>
                    <td className="admin-actions">
                      <button
                        type="button"
                        className="admin-button"
                        onClick={() => handlePromote(user.id)}
                        disabled={!user.promotable}
                      >
                        {user.promotable ? "Make admin" : "Already admin"}
                      </button>
                      <button
                        type="button"
                        className="admin-button admin-button-danger"
                        onClick={() => handleDelete(user.id, user.username)}
                        disabled={!user.deletable}
                      >
                        Delete user
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </section>
  );
}
