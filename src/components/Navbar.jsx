import './Navbar.css';
import { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

export default function Navbar() {
  const { currentUser, isAdmin, isAuthenticated, logout } = useContext(AuthContext);

  return (
    <nav className="navbar">
      <div className="nav-left">
        <Link to="/">Home</Link>
        <Link to="/watchlist">My List</Link>
        {isAdmin ? <Link to="/admin">Admin</Link> : null}
      </div>

      <div className="logo">
        <img src="/logo.png" alt="WatchVault logo" />
      </div>

      <div className="nav-right">
        {isAuthenticated ? (
          <>
            <span className="nav-user">Hi, {currentUser.username}</span>
            <button type="button" className="nav-action" onClick={logout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/signup">Sign up</Link>
          </>
        )}
      </div>
    </nav>
  );
}
