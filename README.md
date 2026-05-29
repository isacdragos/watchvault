# WatchVault

WatchVault is a full-stack watchlist application for tracking films, series, and anime. It includes a React/Vite frontend and a Spring Boot backend with authentication, role-based access, CRUD watchlist management, filtering, sorting, user administration, and automated tests.

## Features

- User signup, login, logout, and session timeout handling.
- First registered user becomes an admin; later users receive the regular user role.
- Protected watchlist routes and admin-only user management.
- Add, edit, delete, and view details for watchlist entries.
- Track title, type, status, description, release date, poster image, episode progress, and rating.
- Filter by media type and status tabs, sort the watchlist, and persist UI preferences in cookies.
- REST API backed by Spring Boot, JPA, role/permission tables, and PostgreSQL.
- Client utility tests, server/API tests, Spring Boot integration tests, and Playwright end-to-end tests.

## Tech Stack

### Frontend

- React 19
- Vite
- React Router
- js-cookie
- CSS modules/files per component/page
- Playwright for browser tests

### Backend

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Jakarta Validation
- Spring Security Crypto for password hashing
- PostgreSQL for local/runtime data
- H2 for tests

## Repository Structure

```text
.
+-- src/                                React frontend source
|   +-- api/                            API adapter and frontend/backend DTO mapping
|   +-- components/                     Reusable UI components
|   +-- context/                        Auth and watchlist state providers
|   +-- pages/                          Route-level pages
|   +-- utils/                          Validation, cookies, and activity tracking
+-- watchvault-backend-springboot/      Main Spring Boot backend
|   +-- src/main/java/.../controller    REST controllers
|   +-- src/main/java/.../service       Business logic
|   +-- src/main/java/.../repository    JPA repositories
|   +-- src/main/java/.../entity        Database entities
|   +-- src/main/java/.../dto           Request/response DTOs
|   +-- src/main/resources              Backend config and seed data
+-- backend-java/                       Older Java HTTP backend kept in the repo as a record
+-- tests/                              Node-based client/server tests
+-- e2e/                                Playwright end-to-end tests
+-- public/                             Public images and icons
+-- .github/workflows/                  CI workflow for Playwright
```

## Main Application Flow

The frontend stores the authenticated session in `localStorage` under `watchvault_session`. API requests attach the session token as a Bearer token.

The backend creates session records on login/signup, validates each protected request, and checks permissions before returning data. Users can only access their own shows. Admin endpoints require the `USER_MANAGE` permission.

Seed data in `watchvault-backend-springboot/src/main/resources/data.sql` creates:

- `ADMIN` and `USER` roles.
- `SHOW_READ`, `SHOW_WRITE`, and `USER_MANAGE` permissions.
- Permission assignments for each role.

## API Overview

Base path: `/api`

### Auth

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`

### Shows

- `GET /api/shows?page=0&size=10&status=&search=`
- `GET /api/shows/{id}`
- `POST /api/shows`
- `PUT /api/shows/{id}`
- `DELETE /api/shows/{id}`

### Stats

- `GET /api/stats`

### Admin

- `GET /api/admin/users`
- `POST /api/admin/users/{userId}/promote`
- `DELETE /api/admin/users/{userId}`

## Requirements

- Node.js and npm
- Java 17
- PostgreSQL
- A database named for your `DATABASE_URL`, for example `watchvault_db`

The backend includes a Maven wrapper, so a separate Maven installation is not required.

## Environment Setup

Create a frontend environment file from the example:

```powershell
Copy-Item .env.example .env
```

For local development, make sure the frontend proxy points to the backend URL you actually run:

```env
BACKEND_PROXY_TARGET=http://localhost:8080
VITE_API_BASE_URL=
```

The Spring Boot backend reads database settings from environment variables:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/watchvault_db"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="your_password"
```

By default, the backend runs on port `8080`. To use another port:

```powershell
$env:PORT="8443"
```

## Running Locally

Install frontend dependencies:

```powershell
npm install
```

Start the backend:

```powershell
cd watchvault-backend-springboot
.\mvnw spring-boot:run
```

In another terminal, start the frontend from the repository root:

```powershell
npm run dev
```

Open the Vite URL shown in the terminal, usually:

```text
https://localhost:5173
```

Note: You might need to edit `watchvault-backend-springboot/src/main/resources/application.properties` so that `server.ssl.enabled=true` and generate a valid ssl certificate or edit `watchvault/.env` `watchvault/tests/client/authApi.test.js` `watchvault/vite.config.js` so that all instances of https are http.

## Production Build

Build the frontend:

```powershell
npm run build
```

Preview the built frontend:

```powershell
npm run preview
```

For a production deployment, set `VITE_API_BASE_URL` to the deployed backend URL before building.

## Available Scripts

```text
npm run dev          Start the Vite dev server
npm run build        Build the frontend
npm run preview      Preview the production frontend build
npm run lint         Run ESLint
npm run test:client  Run client-side Node tests
npm run test:server  Run server/API Node tests
```

The repository also has Playwright tests:

```powershell
npx playwright test
```

The Spring Boot tests are run from the backend directory:

```powershell
cd watchvault-backend-springboot
.\mvnw test
```

## Testing Notes

- `tests/client/showValidation.test.js` checks frontend show validation behavior.
- `tests/client/authApi.test.js` checks authentication API adapter behavior.
- `tests/server/watchlistApi.test.js` checks API behavior in the Node/server test area.
- `watchvault-backend-springboot/src/test` contains Spring Boot integration tests.
- `e2e/watchlist-e2e.spec.js` covers adding shows, deleting shows, and filtering.

The Playwright config starts Vite on port `4173`. If local HTTPS/proxy settings change, update `playwright.config.js` accordingly.

## Important Implementation Details

- `src/api/watchlistApi.js` maps frontend labels to backend values, for example `Film` to `movie` and `Plan to watch` to `plan-to-watch`.
- `src/context/AuthContext.jsx` owns login/signup/logout state and handles automatic local logout on inactivity or unauthorized API responses.
- `src/context/WatchlistContext.jsx` loads shows for the current user and exposes add/update/delete operations.
- `src/components/ProtectedRoute.jsx` protects authenticated pages and admin-only pages.
- The backend validates request bodies with Jakarta Validation annotations in DTO classes.
- The backend uses role and permission checks in services/controllers rather than trusting the frontend.

## Common First-Run Issues

### The frontend loads but API calls fail

Check that `BACKEND_PROXY_TARGET` in `.env` matches the backend protocol and port. If the backend is running with default settings, use:

```env
BACKEND_PROXY_TARGET=http://localhost:8080
```

### The backend fails on startup

Check that PostgreSQL is running and these environment variables are set:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
```

### Admin page is unavailable

Only users with the `ADMIN` role can open `/admin`. The first account created in an empty database becomes the admin account.

## Development Notes

- `watchvault-backend-springboot` is the main backend that the app runs on. `backend-java` is an older/simple Java backend and is not the main application backend. It is just a record of a previous iteration of a backend.
- Generated folders such as `dist`, `target`, `out`, `playwright-report`, and `test-results` are build/test outputs.
