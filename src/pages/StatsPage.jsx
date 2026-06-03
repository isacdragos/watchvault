import { useEffect, useMemo, useState } from "react";
import { fetchStats } from "../api/watchlistApi";
import "./StatsPage.css";

const STATUS_ITEMS = [
  ["completed", "Completed", "#b80000"],
  ["watching", "Watching", "#141a1a"],
  ["plan-to-watch", "Plan to Watch", "#d8d8d8"],
  ["on-hold", "On hold", "#777"],
  ["dropped", "Dropped", "#202a2c"],
];

const TYPE_ITEMS = [
  ["series", "Series", "#b80000"],
  ["movie", "Movies", "#141414"],
  ["anime", "Animes", "#c8c8c8"],
];

function valueFor(group, key) {
  return Number(group?.[key] ?? 0);
}

function chartItems(definitions, values) {
  return definitions.map(([key, label, color]) => ({
    key,
    label,
    color,
    value: valueFor(values, key),
  }));
}

function conicGradient(items) {
  const total = items.reduce((sum, item) => sum + item.value, 0);

  if (total === 0) {
    return "conic-gradient(#1c1c1c 0 360deg)";
  }

  let cursor = 0;
  const segments = items.map((item) => {
    const degrees = (item.value / total) * 360;
    const start = cursor;
    cursor += degrees;
    return `${item.color} ${start}deg ${cursor}deg`;
  });

  return `conic-gradient(${segments.join(", ")})`;
}

function DonutChart({ items }) {
  return (
    <div className="stats-chart-group">
      <div className="stats-pie" style={{ background: conicGradient(items) }} aria-hidden="true" />
      <div className="stats-legend">
        {items.map((item) => (
          <div className="stats-legend-item" key={item.key}>
            <span className="stats-swatch" style={{ backgroundColor: item.color }} />
            <span>{item.label}: {item.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function RatingStars({ rating }) {
  const filled = Math.round(Number(rating ?? 0) / 2);

  return (
    <div className="stats-stars" aria-label={`Average rating ${rating} out of 10`}>
      {Array.from({ length: 5 }, (_, index) => (
        <span className={index < filled ? "filled" : ""} key={index}>★</span>
      ))}
    </div>
  );
}

export function StatsPage() {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function loadStats() {
      try {
        const loadedStats = await fetchStats();

        if (!active) {
          return;
        }

        setStats(loadedStats);
        setError("");
      } catch (loadError) {
        if (active) {
          setError(loadError.message);
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    loadStats();

    return () => {
      active = false;
    };
  }, []);

  const statusItems = useMemo(() => chartItems(STATUS_ITEMS, stats?.byStatus), [stats]);
  const typeItems = useMemo(() => chartItems(TYPE_ITEMS, stats?.byType), [stats]);
  const averageRating = stats?.averageRating == null ? "0.0" : Number(stats.averageRating).toFixed(1);
  const episodesWatched = Number(stats?.episodesWatched ?? stats?.totalEpisodesWatched ?? 0);
  const maxEpisodes = Math.max(episodesWatched, 1);
  const daysWatched = Number(stats?.daysWatched ?? 0).toFixed(1);

  return (
    <main className="stats-page">
      <section className="stats-shell">
        <h1>Your stats:</h1>

        {isLoading ? <p className="stats-muted">Loading stats...</p> : null}
        {!isLoading && error ? <p className="stats-error">{error}</p> : null}

        {!isLoading && !error ? (
          <div className="stats-layout">
            <div className="stats-charts">
              <DonutChart items={statusItems} />
              <DonutChart items={typeItems} />
            </div>

            <aside className="stats-summary" aria-label="Watchlist summary">
              <div className="stats-metric">
                <h2>Average Rating</h2>
                <strong>{averageRating}</strong>
                <RatingStars rating={Number(averageRating)} />
              </div>

              <div className="stats-metric">
                <h2>Episodes Watched</h2>
                <div className="stats-progress-label">{episodesWatched}</div>
                <div className="stats-progress">
                  <span style={{ width: `${Math.min((episodesWatched / maxEpisodes) * 100, 100)}%` }} />
                </div>
              </div>

              <div className="stats-metric">
                <h2>Days Watched</h2>
                <strong>{daysWatched}</strong>
              </div>
            </aside>
          </div>
        ) : null}
      </section>
    </main>
  );
}
