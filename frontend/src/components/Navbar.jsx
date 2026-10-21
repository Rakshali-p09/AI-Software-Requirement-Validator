
import { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import {
  Menu,
  X,
  ShieldCheck,
  LayoutDashboard,
  ArrowRight,
} from "lucide-react";

function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);

  const isLoggedIn = Boolean(localStorage.getItem("token"));

  const closeMenu = () => setMenuOpen(false);

  const linkClass = ({ isActive }) =>
    `navbar-link${isActive ? " active" : ""}`;

  return (
    <header className="site-navbar">
      <div className="navbar-inner">
        <Link
          to="/"
          className="navbar-brand"
          onClick={closeMenu}
          aria-label="ReqValidator Home"
        >
          <span className="navbar-brand-icon">
            <ShieldCheck size={23} />
          </span>

          <span className="navbar-brand-text">
            Req<span>Validator</span>
            <small>AI REQUIREMENT ANALYSIS</small>
          </span>
        </Link>

        <button
          type="button"
          className="navbar-menu-toggle"
          onClick={() => setMenuOpen((open) => !open)}
          aria-label={menuOpen ? "Close menu" : "Open menu"}
          aria-expanded={menuOpen}
        >
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <nav
          className={`navbar-navigation ${
            menuOpen ? "navbar-navigation-open" : ""
          }`}
          aria-label="Main navigation"
        >
          <NavLink
            to="/"
            end
            className={linkClass}
            onClick={closeMenu}
          >
            Home
          </NavLink>

          <a
            href="/#features"
            className="navbar-link"
            onClick={closeMenu}
          >
            Features
          </a>

          <a
            href="/#how-it-works"
            className="navbar-link"
            onClick={closeMenu}
          >
            How It Works
          </a>

          {isLoggedIn ? (
            <NavLink
              to="/dashboard"
              className={linkClass}
              onClick={closeMenu}
            >
              <LayoutDashboard size={16} />
              Dashboard
            </NavLink>
          ) : (
            <div className="navbar-actions">
              <Link
                to="/login"
                className="navbar-login-link"
                onClick={closeMenu}
              >
                Login
              </Link>

              <Link
                to="/register"
                className="navbar-register-button"
                onClick={closeMenu}
              >
                Get Started
                <ArrowRight size={16} />
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Navbar;
