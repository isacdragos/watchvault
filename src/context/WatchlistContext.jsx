import { createContext, useContext, useEffect, useState } from "react";
import {
  createShow as createShowRequest,
  deleteShow as deleteShowRequest,
  fetchShows,
  updateShow as updateShowRequest,
} from "../api/watchlistApi";
import { AuthContext } from "./AuthContext";

export const WatchlistContext = createContext();

export function WatchlistProvider({ children }) {
  const { currentUser, isAuthenticated, logout } = useContext(AuthContext);
  const [shows, setShows] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isAuthenticated || !currentUser?.username) {
      setShows([]);
      setError("");
      setIsLoading(false);
      return undefined;
    }

    let active = true;
    setIsLoading(true);

    async function loadShows() {
      try {
        const loadedShows = await fetchShows();

        if (!active) {
          return;
        }

        setShows(loadedShows);
        setError("");
      } catch (loadError) {
        if (!active) {
          return;
        }

        if (loadError.message === "Authentication required.") {
          logout();
        }

        setError(loadError.message);
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    loadShows();

    return () => {
      active = false;
    };
  }, [currentUser?.username, isAuthenticated, logout]);

  const addShow = async (newShow) => {
    const createdShow = await createShowRequest(newShow);
    setShows((prev) => [createdShow, ...prev]);
    return createdShow;
  };

  const updateShow = async (showId, updatedShow) => {
    const savedShow = await updateShowRequest(showId, updatedShow);
    const normalizedShowId = Number(showId);
    setShows((prev) => prev.map((show) => (show.id === normalizedShowId ? savedShow : show)));
    return savedShow;
  };

  const deleteShow = async (showId) => {
    await deleteShowRequest(showId);
    const normalizedShowId = Number(showId);
    setShows((prev) => prev.filter((show) => show.id !== normalizedShowId));
  };

  return (
    <WatchlistContext.Provider
      value={{
        shows,
        isLoading,
        error,
        addShow,
        updateShow,
        deleteShow,
      }}
    >
      {children}
    </WatchlistContext.Provider>
  );
}
