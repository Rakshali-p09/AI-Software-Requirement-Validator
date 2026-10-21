
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import {
  ShieldCheck,
  Mail,
  LockKeyhole,
  UserRound,
  ArrowLeft,
  Sparkles,
} from "lucide-react";

function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      alert("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/register",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: name.trim(),
            email: email.trim(),
            password,
          }),
        }
      );

      const responseText = await response.text();

      if (!response.ok) {
        alert(responseText || "Registration failed.");
        return;
      }

      alert("Registration successful!");
      navigate("/login");
    } catch (error) {
      console.error("Registration request failed:", error);
      alert("Cannot connect to backend. Check whether the backend is running.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page register-page">
      <div className="login-background-glow login-glow-one" />
      <div className="login-background-glow login-glow-two" />

      <Link to="/" className="login-back-home">
        <ArrowLeft size={17} />
        Back to Home
      </Link>

      <div className="login-layout register-layout">
        <section className="login-welcome">
          <Link to="/" className="login-brand">
            <span className="login-brand-icon">
              <ShieldCheck size={27} />
            </span>
            <span>ReqValidator</span>
          </Link>

          <div className="login-welcome-content">
            <div className="login-eyebrow">
              <Sparkles size={15} />
              YOUR REQUIREMENT VALIDATION WORKSPACE
            </div>

            <h1>
              Start building
              <span> better software today.</span>
            </h1>

            <p>
              Create your account to organize projects, upload software
              requirements, identify quality issues, and improve your
              specifications.
            </p>

            <div className="login-benefits">
              <div className="login-benefit">
                <span className="login-benefit-dot" />
                Create and manage projects
              </div>
              <div className="login-benefit">
                <span className="login-benefit-dot" />
                Analyze requirement quality
              </div>
              <div className="login-benefit">
                <span className="login-benefit-dot" />
                Get actionable validation insights
              </div>
            </div>
          </div>

          <p className="login-side-footer">
            Smarter requirements. Better software.
          </p>
        </section>

        <section className="login-form-section">
          <div className="login-card register-card">
            <div className="login-card-icon">
              <UserRound size={25} />
            </div>

            <div className="login-card-heading">
              <h2>Create your account</h2>
              <p>Fill in your details to get started.</p>
            </div>

            <form onSubmit={handleRegister} className="login-form register-form">
              <div className="login-field">
                <label htmlFor="register-name">Full name</label>
                <div className="login-input-wrapper">
                  <UserRound size={18} />
                  <input
                    id="register-name"
                    type="text"
                    placeholder="Enter your full name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    autoComplete="name"
                    required
                  />
                </div>
              </div>

              <div className="login-field">
                <label htmlFor="register-email">Email address</label>
                <div className="login-input-wrapper">
                  <Mail size={18} />
                  <input
                    id="register-email"
                    type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                    required
                  />
                </div>
              </div>

              <div className="login-field">
                <label htmlFor="register-password">Password</label>
                <div className="login-input-wrapper">
                  <LockKeyhole size={18} />
                  <input
                    id="register-password"
                    type="password"
                    placeholder="Create a password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                    required
                  />
                </div>
              </div>

              <div className="login-field">
                <label htmlFor="register-confirm-password">
                  Confirm password
                </label>
                <div className="login-input-wrapper">
                  <LockKeyhole size={18} />
                  <input
                    id="register-confirm-password"
                    type="password"
                    placeholder="Re-enter your password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                    required
                  />
                </div>
              </div>

              <button
                type="submit"
                className="login-submit"
                disabled={loading}
              >
                {loading ? "Creating account..." : "Create account"}
              </button>
            </form>

            <div className="login-divider">
              <span>ALREADY REGISTERED?</span>
            </div>

            <p className="login-register-text">
              Already have an account?{" "}
              <Link to="/login">Sign in</Link>
            </p>
          </div>

          <p className="login-security-note">
            <ShieldCheck size={15} />
            Secure access to your requirement workspace
          </p>
        </section>
      </div>
    </div>
  );
}

export default Register;
