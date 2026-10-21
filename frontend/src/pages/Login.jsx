
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { ShieldCheck, Mail, LockKeyhole, ArrowLeft, Sparkles } from "lucide-react";

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: email.trim(),
            password,
          }),
        }
      );

      const responseText = await response.text();

      let data;
      try {
        data = JSON.parse(responseText);
      } catch {
        data = { message: responseText };
      }

      if (!response.ok) {
        alert(data.message || data.error || "Login failed.");
        return;
      }

      if (!data.token) {
        alert("Login response did not contain an authentication token.");
        console.error("Unexpected login response:", data);
        return;
      }

      localStorage.setItem("token", data.token);

      if (data.user) {
        localStorage.setItem("user", JSON.stringify(data.user));
      }

      alert("Login successful!");
      navigate("/dashboard");
    } catch (error) {
      console.error("Login request failed:", error);
      alert("Cannot connect to backend. Check whether the backend is running.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-background-glow login-glow-one" />
      <div className="login-background-glow login-glow-two" />

      <Link to="/" className="login-back-home">
        <ArrowLeft size={17} />
        Back to Home
      </Link>

      <div className="login-layout">
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
              INTELLIGENT REQUIREMENT VALIDATION
            </div>

            <h1>
              Build better software,
              <span> starting with better requirements.</span>
            </h1>

            <p>
              Validate your software requirements, identify quality issues,
              and make your project specifications clearer and more reliable.
            </p>

            <div className="login-benefits">
              <div className="login-benefit">
                <span className="login-benefit-dot" />
                Requirement quality analysis
              </div>
              <div className="login-benefit">
                <span className="login-benefit-dot" />
                Duplicate and inconsistency detection
              </div>
              <div className="login-benefit">
                <span className="login-benefit-dot" />
                Actionable improvement suggestions
              </div>
            </div>
          </div>

          <p className="login-side-footer">
            Smarter requirements. Better software.
          </p>
        </section>

        <section className="login-form-section">
          <div className="login-card">
            <div className="login-card-icon">
              <LockKeyhole size={25} />
            </div>

            <div className="login-card-heading">
              <h2>Welcome back</h2>
              <p>Enter your credentials to access your workspace.</p>
            </div>

            <form onSubmit={handleLogin} className="login-form">
              <div className="login-field">
                <label htmlFor="login-email">Email address</label>
                <div className="login-input-wrapper">
                  <Mail size={18} />
                  <input
                    id="login-email"
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
                <label htmlFor="login-password">Password</label>
                <div className="login-input-wrapper">
                  <LockKeyhole size={18} />
                  <input
                    id="login-password"
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="current-password"
                    required
                  />
                </div>
              </div>

              <div className="login-forgot">
                <a
                  href="#forgot"
                  onClick={(e) => {
                    e.preventDefault();
                    alert("Please contact your administrator to reset your password.");
                  }}
                >
                  Forgot password?
                </a>
              </div>

              <button
                type="submit"
                className="login-submit"
                disabled={loading}
              >
                {loading ? "Signing in..." : "Sign in to your account"}
                {!loading && <ArrowLeft className="login-submit-arrow" size={18} />}
              </button>
            </form>

            <div className="login-divider">
              <span>NEW TO REQVALIDATOR?</span>
            </div>

            <p className="login-register-text">
              Don't have an account?{" "}
              <Link to="/register">Create an account</Link>
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

export default Login;
