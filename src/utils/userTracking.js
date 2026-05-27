import { useEffect } from "react";
import { useLocation } from "react-router-dom";
import { getCookie, setCookie } from "../utils/cookies";
export function useActivityTracker() {
  const location = useLocation();
  useEffect(() => {
    setCookie("watchvault_lastPage", location.pathname);
    const currentCount = getCookie("watchvault_visitCount") || 0;
    setCookie("watchvault_visitCount", currentCount + 1);
    setCookie("watchvault_lastVisit", new Date().toISOString());
  }, [location.pathname]);
}