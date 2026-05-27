import './Hero.css';
export default function Hero() {
  return (
    <section className="hero">
      <div className="hero-box">
        <div className="hero-content">
          <h1>The next-generation movie tracker platform</h1>
          <h2>
            Track and share your favorite movies
            and shows with WatchVault.
          </h2>
          <p>
            Discover, organize, and keep track of the movies and shows you love.<br />
            Build your personal watchlist, rate what you've watched,<br />
            and never forget what to watch next.
          </p>
          <button className="cta">Join now</button>
        </div>
      </div>
    </section>
  );
}