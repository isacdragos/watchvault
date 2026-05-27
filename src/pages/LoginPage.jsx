import { useContext, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "./AuthPage.css";

function validateLoginForm(form) {
  const errors = {};

  if (!form.username.trim()) {
    errors.username = "Please input username";
  }

  if (!form.password) {
    errors.password = "Please input password";
  }

  return errors;
}

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useContext(AuthContext);
  const [form, setForm] = useState({
    username: "",
    password: "",
  });
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fromPath = location.state?.from?.pathname ?? "/watchlist";

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: "" }));
    setSubmitError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const nextErrors = validateLoginForm(form);
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      await login(form.username, form.password);
      navigate(fromPath, { replace: true });
    } catch (error) {
      setSubmitError(error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <div className="auth-card">
        <h1>Welcome back</h1>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          {submitError ? <p className="auth-banner">{submitError}</p> : null}

          <label className="auth-field">
            <input
              type="text"
              placeholder="Username"
              value={form.username}
              onChange={(event) => handleChange("username", event.target.value)}
              autoComplete="username"
            />
            <span className="auth-error">{errors.username || ""}</span>
          </label>

          <label className="auth-field">
            <input
              type="password"
              placeholder="Password"
              value={form.password}
              onChange={(event) => handleChange("password", event.target.value)}
              autoComplete="current-password"
            />
            <span className="auth-error">{errors.password || ""}</span>
          </label>

          <button type="submit" className="auth-submit" disabled={isSubmitting}>
            {isSubmitting ? "Loading..." : "Login"}
          </button>
        </form>

        <p className="auth-footer">
          Need an account? <Link to="/signup">Sign up</Link>
        </p>
      </div>
    </section>
  );
}
