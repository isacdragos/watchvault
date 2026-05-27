import { useContext, useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { WatchlistContext } from "../context/WatchlistContext";
import { fetchShowById } from "../api/watchlistApi";
import "./ShowDetails.css";
import {
  normalizeShowForm,
  SHOW_STATUSES,
  SHOW_TYPES,
  validateShowForm,
} from "../utils/showValidation";

const scoreStars = (score) => {
  const fiveScale = Math.max(0, Math.min(5, Number(score) / 2));

  return Array.from({ length: 5 }, (_, index) => {
    const position = index + 1;
    if (fiveScale >= position) return "full";
    if (fiveScale >= position - 0.5) return "half";
    return "empty";
  });
};

const statusLabel = (status) => {
  if (!status) return "Not started";
  return status;
};

export function ShowDetails() {
  const { showId } = useParams();
  const navigate = useNavigate();
  const { deleteShow, isLoading, shows, updateShow } = useContext(WatchlistContext);
  const normalizedShowId = Number(showId);
  const showFromContext = useMemo(
    () => shows.find((entry) => entry.id === normalizedShowId) ?? null,
    [normalizedShowId, shows],
  );
  const [isEditing, setIsEditing] = useState(false);
  const [entry, setEntry] = useState(null);
  const [entryError, setEntryError] = useState("");
  const [isEntryLoading, setIsEntryLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [form, setForm] = useState({
    title: "",
    releaseDate: "",
    description: "",
    type: "Film",
    status: "Watching",
    progress: "",
    score: "0",
    image: "",
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (showFromContext) {
      setEntry(showFromContext);
      setEntryError("");
    }
  }, [showFromContext]);

  useEffect(() => {
    if (!Number.isFinite(normalizedShowId)) {
      setEntry(null);
      setEntryError("This entry has an invalid id.");
      setIsEntryLoading(false);
      return undefined;
    }

    let active = true;
    setIsEntryLoading(true);

    async function loadEntry() {
      try {
        const loadedEntry = await fetchShowById(normalizedShowId);

        if (!active) {
          return;
        }

        setEntry(loadedEntry);
        setEntryError("");
      } catch (error) {
        if (!active) {
          return;
        }

        setEntryError(error.message);
      } finally {
        if (active) {
          setIsEntryLoading(false);
        }
      }
    }

    loadEntry();

    return () => {
      active = false;
    };
  }, [normalizedShowId]);

  const show = entry;

  const handleDelete = async () => {
    const confirmed = window.confirm("Delete this entry from your watchlist?");
    if (!confirmed) return;

    await deleteShow(normalizedShowId);
    navigate("/watchlist");
  };

  const startEditing = () => {
    if (!show) return;

    setForm({
      title: show.title || "",
      releaseDate: show.releaseDate || "",
      description: show.description || "",
      type: show.type || "Film",
      status: show.status || "Watching",
      progress: show.progress || "",
      score: String(show.score ?? 0),
      image: show.image || "",
    });
    setErrors({});
    setSaveError("");
    setIsEditing(true);
  };

  const handlePosterChange = (event) => {
    const file = event.target.files?.[0];
    if (!file || !file.type.startsWith("image/")) {
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const dataUrl = e.target?.result;
      if (typeof dataUrl === "string") {
        setForm((prev) => ({ ...prev, image: dataUrl }));
      }
    };
    reader.readAsDataURL(file);
  };

  const saveEdit = async () => {
    const foundErrors = validateShowForm(form);
    setErrors(foundErrors);
    setSaveError("");

    if (Object.keys(foundErrors).length > 0) {
      return;
    }

    setIsSaving(true);

    try {
      const savedShow = await updateShow(normalizedShowId, {
        ...show,
        ...normalizeShowForm(form, form.image || show.image),
        image: form.image || show.image,
      });

      setEntry(savedShow);
      setErrors({});
      setIsEditing(false);
    } catch (error) {
      setSaveError(error.message);
    } finally {
      setIsSaving(false);
    }
  };

  const activeScore = isEditing ? Number(form.score) || 0 : show?.score;

  if (isLoading || isEntryLoading) {
    return (
      <section className="details-page missing">
        <h1>Loading entry...</h1>
      </section>
    );
  }

  if (!show) {
    return (
      <section className="details-page missing">
        <h1>Entry not found</h1>
        <p>{entryError || "This item could not be found in your current watchlist."}</p>
        <Link to="/watchlist" className="details-back-link">
          Back to My List
        </Link>
      </section>
    );
  }

  const stars = scoreStars(activeScore);

  return (
    <section className="details-page">
      <div className="details-shell">
        <div
          className={`details-poster-wrapper ${isEditing ? "editable" : ""}`}
          onClick={() => {
            if (isEditing) {
              document.getElementById(`poster-input-${showId}`)?.click();
            }
          }}
        >
          <img
            className="details-poster"
            src={isEditing ? form.image : show.image}
            alt={`${show.title} poster`}
          />
          {isEditing ? (
            <>
              <div className="details-poster-overlay">Click to change</div>
              <input
                id={`poster-input-${showId}`}
                type="file"
                accept="image/*"
                onChange={handlePosterChange}
                style={{ display: "none" }}
              />
            </>
          ) : null}
        </div>

        <div className="details-content">
          {!isEditing ? <p className="details-kicker">{show.type}</p> : null}

          {isEditing ? (
            <div className="details-edit-grid">
              <label>
                Title
                <input
                  type="text"
                  value={form.title}
                  onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))}
                />
                {errors.title ? <span className="field-error">{errors.title}</span> : null}
              </label>

              <label>
                Release date
                <input
                  type="text"
                  value={form.releaseDate}
                  onChange={(event) => setForm((prev) => ({ ...prev, releaseDate: event.target.value }))}
                  placeholder="YYYY or YYYY-MM-DD"
                />
                {errors.releaseDate ? <span className="field-error">{errors.releaseDate}</span> : null}
              </label>

              <label>
                Description
                <textarea
                  rows={4}
                  value={form.description}
                  onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
                />
                {errors.description ? <span className="field-error">{errors.description}</span> : null}
              </label>

              <div className="details-edit-row">
                <label>
                  Type
                  <select
                    value={form.type}
                    onChange={(event) => setForm((prev) => ({ ...prev, type: event.target.value }))}
                  >
                    {SHOW_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                  {errors.type ? <span className="field-error">{errors.type}</span> : null}
                </label>

                <label>
                  Status
                  <select
                    value={form.status}
                    onChange={(event) => setForm((prev) => ({ ...prev, status: event.target.value }))}
                  >
                    {SHOW_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                  {errors.status ? <span className="field-error">{errors.status}</span> : null}
                </label>
              </div>

              <div className="details-edit-row">
                <label>
                  Progress
                  <input
                    type="text"
                    value={form.progress}
                    onChange={(event) => setForm((prev) => ({ ...prev, progress: event.target.value }))}
                    placeholder="Example: 6/10"
                  />
                  {errors.progress ? <span className="field-error">{errors.progress}</span> : null}
                </label>

                <label>
                  Score
                  <input
                    type="number"
                    min="0"
                    max="10"
                    step="0.5"
                    value={form.score}
                    onChange={(event) => setForm((prev) => ({ ...prev, score: event.target.value }))}
                  />
                  {errors.score ? <span className="field-error">{errors.score}</span> : null}
                </label>
              </div>

              {saveError ? <span className="field-error">{saveError}</span> : null}
            </div>
          ) : (
            <>
              <h1>{show.title}</h1>
              <p className="details-release">Release date: {show.releaseDate || "Unknown"}</p>
              <p className="details-description">{show.description || "No description available."}</p>

              <div className="details-badges">
                <span className="details-pill status">{statusLabel(show.status)}</span>
                <span className="details-pill progress">Progress: {show.progress}</span>
              </div>
            </>
          )}

          {!isEditing ? (
            <div className="details-score-row" aria-label={`Score ${activeScore} out of 10`}>
              <span className="details-score-label">Score:</span>
              <div className="details-stars" role="img" aria-hidden="true">
                {stars.map((fillState, index) => (
                  <span key={`${show.id}-star-${index}`} className={`star ${fillState}`}>
                    ★
                  </span>
                ))}
              </div>
            </div>
          ) : null}

          <div className="details-actions">
            <button type="button" className="details-btn secondary" onClick={handleDelete}>
              Delete
            </button>

            {isEditing ? (
              <>
                <button
                  type="button"
                  className="details-btn secondary"
                  onClick={() => {
                    setSaveError("");
                    setIsEditing(false);
                  }}
                  disabled={isSaving}
                >
                  Cancel
                </button>
                <button type="button" className="details-btn primary" onClick={saveEdit} disabled={isSaving}>
                  {isSaving ? "Saving..." : "Save"}
                </button>
              </>
            ) : (
              <button type="button" className="details-btn primary" onClick={startEditing}>
                Edit
              </button>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
