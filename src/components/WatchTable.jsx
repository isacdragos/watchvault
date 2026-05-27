import { useContext, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { WatchlistContext } from "../context/WatchlistContext";
import "./WatchTable.css";
import { getCookie, setCookie } from "../utils/cookies";

const fallbackPoster = "https://placehold.co/90x135/111111/e8e8e8?text=Poster";

export function WatchTable({ activeTab, activeType, sortBy }) {
  const { shows } = useContext(WatchlistContext);
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 8;

  const filtered = shows.filter((s) => {
    const statusMatch = activeTab === "All Shows" || s.status === activeTab;
    const typeMatch = activeType === "All" || s.type === activeType;
    return statusMatch && typeMatch;
  });

  const sorted = [...filtered].sort((a, b) => {
    if (sortBy === "Name") {
      return a.title.localeCompare(b.title);
    } else if (sortBy === "Rating") {
      return b.score - a.score;
    }
    return 0;
  });

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize));

  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab, activeType, sortBy]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const paginatedRows = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return sorted.slice(start, start + pageSize);
  }, [currentPage, sorted]);

  const startItem = sorted.length === 0 ? 0 : (currentPage - 1) * pageSize + 1;
  const endItem = Math.min(currentPage * pageSize, sorted.length);

  const openDetails = (showId) => {
    const recent = getCookie("watchvault_recentShows") || [];
    const updated = [showId, ...recent.filter((id) => id !== showId)].slice(0, 5);
    setCookie("watchvault_recentShows", updated);
    navigate(`/watchlist/${showId}`);
  };

  return (
    <div className="watch-table">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Score</th>
            <th>Progress</th>
            <th>Type</th>
          </tr>
        </thead>
        <tbody>
          {paginatedRows.length > 0 ? (
            paginatedRows.map((s) => (
              <tr
                key={s.id}
                className="clickable-row"
                tabIndex={0}
                onClick={() => openDetails(s.id)}
              >
                <td className="title-cell">
                  <span>{s.title}</span>
                  <div className="row-poster-preview">
                    <img src={s.image || fallbackPoster} alt={`${s.title} poster`} loading="lazy" />
                  </div>
                </td>
                <td>{s.score}</td>
                <td>{s.progress}</td>
                <td>{s.type}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td className="empty-row" colSpan={4}>
                No shows found for this filter.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div className="table-pagination">
        <p>
          Showing {startItem}-{endItem} of {sorted.length}
        </p>
        <div className="pagination-controls">
          <button
            type="button"
            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            disabled={currentPage === 1}
          >
            Prev
          </button>

          {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
            <button
              key={page}
              type="button"
              onClick={() => setCurrentPage(page)}
              className={page === currentPage ? "active" : ""}
            >
              {page}
            </button>
          ))}

          <button
            type="button"
            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            disabled={currentPage === totalPages}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
