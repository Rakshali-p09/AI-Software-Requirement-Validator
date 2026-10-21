
import { Link } from "react-router-dom";
import {
  ShieldCheck,
  ArrowUpRight,
  Code2,
  Heart,
} from "lucide-react";

function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="site-footer">
      <div className="footer-main">
        {/* Brand */}
        <div className="footer-brand">
          <Link to="/" className="footer-brand-link">
            <span className="footer-logo-icon">
              <ShieldCheck size={23} />
            </span>

            <span className="footer-brand-name">
              Req<span>Validator</span>
            </span>
          </Link>

          <p className="footer-description">
            Making software requirements clearer, more
            consistent, and easier to validate with AI.
          </p>

          <div className="footer-status">
            <span className="footer-status-dot" />
            AI-powered requirement analysis
          </div>
        </div>

        {/* Product links */}
        <div className="footer-link-group">
          <h4>Product</h4>
          <a href="/#features">Features</a>
          <a href="/#how-it-works">How It Works</a>
          <Link to="/dashboard">Dashboard</Link>
        </div>

        {/* Project links */}
        <div className="footer-link-group">
          <h4>Explore</h4>
          <Link to="/">About the Project</Link>
          <a href="/#features">Technology</a>
          <Link to="/login">Sign In</Link>
        </div>

        {/* Call to action */}
        <div className="footer-cta">
          <div className="footer-cta-icon">
            <ArrowUpRight size={20} />
          </div>

          <h4>Build better requirements.</h4>
          <p>
            Start validating your software requirements
            with ReqValidator.
          </p>

          <Link to="/register" className="footer-cta-link">
            Get Started <ArrowUpRight size={16} />
          </Link>
        </div>
      </div>

      {/* Footer bottom */}
      <div className="footer-bottom">
        <p>
          © {currentYear} ReqValidator. Capstone Project.
        </p>

        <p className="footer-made-with">
          Made with <Heart size={14} /> for better software
        </p>

        <Link to="/" className="footer-back-top">
          Back to home ↑
        </Link>
      </div>

      {/* Small technology icon for accessibility and consistency */}
      <span className="footer-tech-icon" aria-hidden="true">
        <Code2 size={16} />
      </span>
    </footer>
  );
}

export default Footer;
