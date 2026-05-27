import { useContext, useState, useEffect } from "react";
import { Tabs } from "../components/Tabs";
import { WatchTable } from "../components/WatchTable";
import { SidebarFilters } from "../components/SidebarFilters";
import { AddShowForm } from "../components/AddShowForm";
import { WatchlistContext } from "../context/WatchlistContext";
import "./Watchlist.css";
import { setCookie, getCookie } from "../utils/cookies";

export function Watchlist() {
  const { addShow, error, isLoading } = useContext(WatchlistContext);
  const [activeTab, setActiveTab] = useState(getCookie("watchvault_activeTab") || "All Shows");
  const [activeType, setActiveType] = useState(getCookie("watchvault_activeType") || "All");
  const [sortBy, setSortBy] = useState(getCookie("watchvault_sortBy") || "Name");
  const [isFormOpen, setIsFormOpen] = useState(false);
  useEffect(() => {
  setCookie("watchvault_activeTab", activeTab);
}, [activeTab]);
useEffect(() => {
  setCookie("watchvault_activeType", activeType);
}, [activeType]);
useEffect(() => {
  setCookie("watchvault_sortBy", sortBy);
}, [sortBy]);
  const handleAddShow = async (newShow) => {
    await addShow(newShow);
    setIsFormOpen(false);
  };

  return (
    <>

      <div className="watchlist-page">
        
        {/* LEFT SIDEBAR */}
        <SidebarFilters onTypeChange={setActiveType}
                        activeType={activeType}
                        onSortChange={setSortBy}
                        sortBy={sortBy}
                        onAddClick={() => setIsFormOpen(true)} />

        {/* MAIN CONTENT */}
        <div className="main-view">
          {/* TABS (PASS STATE + FUNCTION) */}
          <Tabs activeTab={activeTab} setActiveTab={setActiveTab} />

          {isLoading ? <p>Loading shows...</p> : null}
          {!isLoading && error ? <p>Could not load shows: {error}</p> : null}

          {/* TABLE (RECEIVES FILTER) */}
          {!isLoading && !error ? (
            <WatchTable activeTab={activeTab} activeType={activeType} sortBy={sortBy} />
          ) : null}

        </div>
      </div>

      {isFormOpen ? <AddShowForm onClose={() => setIsFormOpen(false)} onSubmit={handleAddShow} /> : null}
    </>
  );
}
