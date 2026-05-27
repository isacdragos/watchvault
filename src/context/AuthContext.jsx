import { createContext, useEffect, useMemo, useState } from "react";
import {
  loginRequest,
  logoutRequest,
  setAuthToken,
  signupRequest,
} from "../api/watchlistApi";

const SESSION_STORAGE_KEY = "watchvault_session";

export const AuthContext = createContext();

function normalizeSessionUser(session) {
  if (!session?.username || !session?.token) {
    return null;
  }

  return {
    username: session.username,
    roles: Array.isArray(session.roles) ? session.roles : [],
    permissions: Array.isArray(session.permissions) ? session.permissions : [],
    token: typeof session.token === "string" ? session.token : "",
    sessionTimeoutMinutes: Number(session.sessionTimeoutMinutes) || 15,
  };
}

function readStoredSession() {
  try {
    const stored = localStorage.getItem(SESSION_STORAGE_KEY);
    const session = normalizeSessionUser(stored ? JSON.parse(stored) : null);
    setAuthToken(session?.token ?? "");
    return session;
  } catch {
    setAuthToken("");
    return null;
  }
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(() => readStoredSession());

  useEffect(() => {
    setAuthToken(currentUser?.token ?? "");

    if (currentUser) {
      localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(currentUser));
      return;
    }

    localStorage.removeItem(SESSION_STORAGE_KEY);
  }, [currentUser]);

  useEffect(() => {
    const handleUnauthorized = () => {
      setAuthToken("");
      setCurrentUser(null);
    };

    window.addEventListener("watchvault:unauthorized", handleUnauthorized);
    return () => {
      window.removeEventListener("watchvault:unauthorized", handleUnauthorized);
    };
  }, []);

  useEffect(() => {
    if (!currentUser) {
      return undefined;
    }

    const timeoutMs = currentUser.sessionTimeoutMinutes * 60 * 1000;
    let timeoutId;

    const resetTimer = () => {
      window.clearTimeout(timeoutId);
      timeoutId = window.setTimeout(() => {
        setAuthToken("");
        setCurrentUser(null);
      }, timeoutMs);
    };

    const events = ["click", "keydown", "mousemove", "scroll", "touchstart"];
    resetTimer();
    events.forEach((eventName) => window.addEventListener(eventName, resetTimer));

    return () => {
      window.clearTimeout(timeoutId);
      events.forEach((eventName) => window.removeEventListener(eventName, resetTimer));
    };
  }, [currentUser]);

  const value = useMemo(() => ({
    currentUser,
    isAuthenticated: Boolean(currentUser),
    isAdmin: Boolean(currentUser?.roles?.includes("ADMIN")),
    hasRole(roleName) {
      return Boolean(currentUser?.roles?.includes(roleName));
    },
    async login(username, password) {
      const sessionUser = normalizeSessionUser(await loginRequest(username.trim(), password));
      setAuthToken(sessionUser.token);
      setCurrentUser(sessionUser);
      return sessionUser;
    },
    async signup(username, password, confirmPassword) {
      const sessionUser = normalizeSessionUser(
        await signupRequest(username.trim(), password, confirmPassword),
      );
      setAuthToken(sessionUser.token);
      setCurrentUser(sessionUser);
      return sessionUser;
    },
    async logout() {
      const activeToken = currentUser?.token ?? "";

      try {
        if (activeToken) {
          await logoutRequest();
        }
      } catch {
        // Best-effort logout; local session should still be cleared.
      }

      setAuthToken("");
      setCurrentUser(null);
    },
  }), [currentUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
