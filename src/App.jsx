import Navbar from './components/Navbar';
import { ProtectedRoute } from "./components/ProtectedRoute";
import Hero from './components/Hero';
import { LoginPage } from "./pages/LoginPage";
import { SignupPage } from "./pages/SignupPage";
import { Watchlist } from "./pages/Watchlist";
import { ShowDetails } from "./pages/ShowDetails";
import { AdminPage } from "./pages/AdminPage";
import './App.css';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useActivityTracker } from "./utils/userTracking";

function AppRoutes() {
  useActivityTracker();

  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Hero />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route
          path="/watchlist"
          element={(
            <ProtectedRoute>
              <Watchlist />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/watchlist/:showId"
          element={(
            <ProtectedRoute>
              <ShowDetails />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/admin"
          element={(
            <ProtectedRoute requiredRole="ADMIN">
              <AdminPage />
            </ProtectedRoute>
          )}
        />
      </Routes>
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}
export default App;
