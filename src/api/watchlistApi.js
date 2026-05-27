const DEFAULT_BACKEND_PORT = "8443";

function getApiBaseUrl() {
  if (import.meta.env.DEV) {
    return "";
  }

  const configuredApiBaseUrl = import.meta.env?.VITE_API_BASE_URL?.trim();

  if (configuredApiBaseUrl) {
    return configuredApiBaseUrl.replace(/\/$/, "");
  }

  if (typeof window === "undefined") {
    return `https://localhost:${DEFAULT_BACKEND_PORT}`;
  }

  return `https://${window.location.hostname}:${DEFAULT_BACKEND_PORT}`;
}

const API_BASE_URL = getApiBaseUrl();
let authToken = "";

const FRONTEND_TO_BACKEND_TYPE = {
  Film: "movie",
  Series: "series",
  Anime: "anime",
};

const BACKEND_TO_FRONTEND_TYPE = {
  movie: "Film",
  series: "Series",
  anime: "Anime",
  documentary: "Film",
};

const FRONTEND_TO_BACKEND_STATUS = {
  Watching: "watching",
  Completed: "completed",
  "On Hold": "on-hold",
  Dropped: "dropped",
  "Plan to watch": "plan-to-watch",
};

const BACKEND_TO_FRONTEND_STATUS = {
  watching: "Watching",
  completed: "Completed",
  "on-hold": "On Hold",
  dropped: "Dropped",
  "plan-to-watch": "Plan to watch",
};

function parseProgress(progress) {
  const trimmed = progress.trim();

  if (!trimmed) {
    return {
      episodesWatched: 0,
      totalEpisodes: null,
    };
  }

  const match = trimmed.match(/^(\d+)\/(\d+|\?)$/);

  if (!match) {
    return {
      episodesWatched: 0,
      totalEpisodes: null,
    };
  }

  return {
    episodesWatched: Number(match[1]),
    totalEpisodes: match[2] === "?" ? null : Number(match[2]),
  };
}

function formatProgress(episodesWatched, totalEpisodes) {
  const watched = Number.isInteger(episodesWatched) ? episodesWatched : 0;

  if (totalEpisodes === null || totalEpisodes === undefined) {
    return `${watched}/?`;
  }

  return `${watched}/${totalEpisodes}`;
}

function normalizeReleaseDateForBackend(releaseDate) {
  const trimmed = releaseDate.trim();

  if (!trimmed) {
    return null;
  }

  if (/^\d{4}$/.test(trimmed)) {
    return `${trimmed}-01-01`;
  }

  return trimmed;
}

function mapShowFromBackend(show) {
  return {
    id: show.id,
    title: show.title,
    releaseDate: show.releaseDate ?? "",
    score: show.rating ?? 0,
    progress: formatProgress(show.episodesWatched, show.totalEpisodes),
    type: BACKEND_TO_FRONTEND_TYPE[show.type] ?? "Film",
    status: BACKEND_TO_FRONTEND_STATUS[show.status] ?? "Watching",
    description: show.description ?? "",
    image: show.image ?? "",
  };
}

function mapShowToBackend(show) {
  const { episodesWatched, totalEpisodes } = parseProgress(show.progress ?? "");

  return {
    title: show.title.trim(),
    type: FRONTEND_TO_BACKEND_TYPE[show.type] ?? "movie",
    status: FRONTEND_TO_BACKEND_STATUS[show.status] ?? "watching",
    description: show.description.trim(),
    releaseDate: normalizeReleaseDateForBackend(show.releaseDate ?? ""),
    image: show.image || null,
    episodesWatched,
    totalEpisodes,
    rating: Number(show.score),
    genres: [],
  };
}

async function apiRequest(path, options = {}) {
  const headers = { ...(options.headers ?? {}) };

  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return null;
  }

  const rawBody = await response.text();
  const body = rawBody ? JSON.parse(rawBody) : null;

  if (!response.ok) {
    if (response.status === 401 && typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("watchvault:unauthorized"));
    }
    throw new Error(body?.message ?? "Request failed.");
  }

  return body;
}

export function setAuthToken(token) {
  authToken = token?.trim() ?? "";
}

export async function signupRequest(username, password, confirmPassword) {
  return apiRequest("/api/auth/signup", {
    method: "POST",
    body: JSON.stringify({
      username,
      password,
      confirmPassword,
    }),
  });
}

export async function loginRequest(username, password) {
  return apiRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({
      username,
      password,
    }),
  });
}

export async function logoutRequest() {
  await apiRequest("/api/auth/logout", {
    method: "POST",
  });
}

export async function fetchShows() {
  const body = await apiRequest("/api/shows?size=100", { method: "GET" });
  const items = Array.isArray(body.items) ? body.items : body.content;
  return (items ?? []).map(mapShowFromBackend);
}

export async function fetchShowById(showId) {
  const show = await apiRequest(`/api/shows/${showId}`, { method: "GET" });
  return mapShowFromBackend(show);
}

export async function createShow(show) {
  const createdShow = await apiRequest("/api/shows", {
    method: "POST",
    body: JSON.stringify(mapShowToBackend(show)),
  });

  return mapShowFromBackend(createdShow);
}

export async function updateShow(showId, show) {
  const updatedShow = await apiRequest(`/api/shows/${showId}`, {
    method: "PUT",
    body: JSON.stringify(mapShowToBackend(show)),
  });

  return mapShowFromBackend(updatedShow);
}

export async function deleteShow(showId) {
  await apiRequest(`/api/shows/${showId}`, {
    method: "DELETE",
  });
}

export async function fetchAdminUsers() {
  return apiRequest("/api/admin/users", { method: "GET" });
}

export async function promoteUserToAdmin(userId) {
  return apiRequest(`/api/admin/users/${userId}/promote`, {
    method: "POST",
  });
}

export async function deleteUser(userId) {
  await apiRequest(`/api/admin/users/${userId}`, {
    method: "DELETE",
  });
}
