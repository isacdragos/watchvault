import { useContext, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "./AuthPage.css";

function validateSignupForm(form) {
  const errors = {};

  if (!form.username.trim()) {
    errors.username = "Please input username";
  } else if (form.username.trim().length < 3) {
    errors.username = "Username must have at least 3 characters";
  }

  if (!form.password) {
    errors.password = "Please input password";
  } else if (form.password.length < 6) {
    errors.password = "Password must have at least 6 characters";
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = "Please input password";
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = "Passwords do not match";
  }

  return errors;
}

export function SignupPage() {
  const navigate = useNavigate();
  const { signup } = useContext(AuthContext);
  const [form, setForm] = useState({
    username: "",
    password: "",
    confirmPassword: "",
  });
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: "" }));
    setSubmitError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const nextErrors = validateSignupForm(form);
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      await signup(form.username, form.password, form.confirmPassword);
      navigate("/watchlist", { replace: true });
    } catch (error) {
      setSubmitError(error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <div className="auth-card">
        <h1>Sign up</h1>

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
              autoComplete="new-password"
            />
            <span className="auth-error">{errors.password || ""}</span>
          </label>

          <label className="auth-field">
            <input
              type="password"
              placeholder="Re-enter Password"
              value={form.confirmPassword}
              onChange={(event) => handleChange("confirmPassword", event.target.value)}
              autoComplete="new-password"
            />
            <span className="auth-error">{errors.confirmPassword || ""}</span>
          </label>

          <button type="submit" className="auth-submit" disabled={isSubmitting}>
            {isSubmitting ? "Loading..." : "Register"}
          </button>
        </form>

        <p className="auth-footer">
          Already registered? <Link to="/login">Login</Link>
        </p>
      </div>
    </section>
  );
}
