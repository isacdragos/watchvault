import "./SidebarFilters.css";

export function SidebarFilters({ onTypeChange, activeType, onSortChange, sortBy, onAddClick }) {
  const types = ["All", "Film", "Series", "Anime"];

  return (
    <aside className="sidebar">
      <div className="sort-by">
        <p>Sort by</p>
        <select value={sortBy} onChange={(e) => onSortChange(e.target.value)}>
          <option>Name</option>
          <option>Rating</option>
        </select>
      </div>

      <div className="filter-type">
        <p>Type:</p>
        {types.map((t) => (
          <button
            key={t}
            className={activeType === t ? "active" : ""}
            onClick={() => onTypeChange(t)}
          >
            {t}
          </button>
        ))}
      </div>

      <button type="button" className="add-to-list" onClick={onAddClick}>Add to list</button>
    </aside>
  );
}