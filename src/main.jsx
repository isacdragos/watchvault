import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from "./context/AuthContext.jsx";
import { WatchlistProvider } from "./context/WatchlistContext.jsx";

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <WatchlistProvider>
        <App />
      </WatchlistProvider>
    </AuthProvider>
  </StrictMode>
)
