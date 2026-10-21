
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  FolderKanban,
  ClipboardList,
  TriangleAlert,
  ChartNoAxesCombined,
  ArrowUpRight,
  Plus,
  Upload,
  Sparkles,
  CheckCircle2,
  RefreshCw,
  ShieldCheck,
  FileText,
  CircleAlert,
  ArrowRight,
} from "lucide-react";

import Navbar from "../components/Navbar";
import Footer from "../components/Footer";

const API_URL = "http://localhost:8080";

function Dashboard() {
  const [projects, setProjects] = useState([]);
  const [requirements, setRequirements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  const fetchProjects = useCallback(async () => {
    setLoading(true);
    setStatsLoading(true);
    setError("");

    try {
      const userData = localStorage.getItem("user");
      const token = localStorage.getItem("token");

      if (!userData || !token) {
        setProjects([]);
        setRequirements([]);
        setError("Please log in to view your workspace.");
        return;
      }

      let user;

      try {
        user = JSON.parse(userData);
      } catch {
        throw new Error("Your saved user session is invalid. Please log in again.");
      }

      if (!user?.email) {
        throw new Error("Your account email was not found. Please log in again.");
      }

      const response = await fetch(
        `${API_URL}/api/projects/owner/${encodeURIComponent(user.email)}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.status === 401 || response.status === 403) {
        throw new Error("Your session may have expired. Please log in again.");
      }

      if (!response.ok) {
        throw new Error("Unable to load your projects. Please try again.");
      }

      const data = await response.json();
      const projectList = Array.isArray(data) ? data : [];

      setProjects(projectList);

      const requirementResults = await Promise.all(
        projectList.map(async (project) => {
          const projectId = project.id ?? project._id;

          if (!projectId) return [];

          try {
            const requirementResponse = await fetch(
              `${API_URL}/api/requirements/project/${encodeURIComponent(projectId)}`,
              {
                headers: {
                  Authorization: `Bearer ${token}`,
                },
              }
            );

            if (!requirementResponse.ok) return [];

            const requirementData = await requirementResponse.json();
            return Array.isArray(requirementData) ? requirementData : [];
          } catch (requirementError) {
            console.error(
              `Could not load requirements for project ${projectId}:`,
              requirementError
            );
            return [];
          }
        })
      );

      setRequirements(requirementResults.flat());
    } catch (err) {
      console.error("Dashboard error:", err);
      setError(
        err.message || "Something went wrong while loading your dashboard."
      );
    } finally {
      setLoading(false);
      setStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProjects();
  }, [fetchProjects, refreshKey]);

  const issuesDetected = requirements.filter((requirement) => {
    const hasIssues =
      Array.isArray(requirement.issues) && requirement.issues.length > 0;

    const status = String(requirement.status || "").toUpperCase();
    const hasProblemStatus = ["FAIL", "POOR", "INCONSISTENT", "DUPLICATE"].includes(
      status
    );

    return hasIssues || hasProblemStatus;
  }).length;

  const scoredRequirements = requirements.filter(
    (requirement) =>
      typeof requirement.qualityScore === "number" &&
      Number.isFinite(requirement.qualityScore)
  );

  const averageQuality =
    scoredRequirements.length > 0
      ? Math.round(
          scoredRequirements.reduce(
            (sum, requirement) => sum + requirement.qualityScore,
            0
          ) / scoredRequirements.length
        )
      : null;

  const stats = [
    {
      label: "Total projects",
      value: loading ? "—" : projects.length,
      note: "Your workspace projects",
      tone: "violet",
      Icon: FolderKanban,
    },
    {
      label: "Requirements",
      value: statsLoading ? "—" : requirements.length,
      note: "Across all projects",
      tone: "teal",
      Icon: ClipboardList,
    },
    {
      label: "Issues detected",
      value: statsLoading ? "—" : issuesDetected,
      note: "Requirements flagged for review",
      tone: "orange",
      Icon: TriangleAlert,
    },
    {
      label: "Average quality",
      value: statsLoading
        ? "—"
        : averageQuality === null
          ? "—"
          : `${averageQuality}%`,
      note: "Based on available scores",
      tone: "blue",
      Icon: ChartNoAxesCombined,
    },
  ];

  const handleRetry = () => {
    setRefreshKey((current) => current + 1);
  };

  return (
    <div className="dashboard-page">
      <Navbar />

      <main className="dashboard-shell">
        {/* Page heading */}
        <div className="dashboard-topline">
          <div className="dashboard-breadcrumb">
            <span className="breadcrumb-dot" />
            WORKSPACE <span>/</span> OVERVIEW
          </div>

          <div className="dashboard-live-status">
            <span />
            Requirement workspace
          </div>
        </div>

        {/* Hero section */}
        <section className="dashboard-hero">
          <div className="dashboard-hero-content">
            <span className="dashboard-eyebrow">
              <Sparkles size={14} />
              SOFTWARE QUALITY WORKSPACE
            </span>

            <h1>
              Build better requirements.
              <br />
              <span>Ship with confidence.</span>
            </h1>

            <p>
              Organize your projects, inspect requirement quality, and identify
              issues before they become development problems.
            </p>

            <div className="dashboard-hero-actions">
              <Link
                to="/create-project"
                className="dashboard-primary-button"
              >
                <Plus size={18} />
                Create new project
              </Link>

              <Link to="/upload" className="dashboard-hero-secondary">
                <Upload size={17} />
                Upload SRS
                <ArrowUpRight size={16} />
              </Link>
            </div>

            <div className="dashboard-trust-note">
              <ShieldCheck size={16} />
              <span>One workspace for clearer software requirements</span>
            </div>
          </div>

          <div className="dashboard-hero-visual" aria-hidden="true">
            <div className="hero-orbit hero-orbit-one" />
            <div className="hero-orbit hero-orbit-two" />

            <div className="hero-center">
              <div className="hero-center-icon">
                <CheckCircle2 size={34} />
              </div>
              <span>QUALITY</span>
              <strong>FIRST</strong>
            </div>

            <div className="hero-float hero-float-top">
              <span className="hero-float-check">
                <CheckCircle2 size={20} />
              </span>
              <div>
                <strong>Clear requirements</strong>
                <small>Quality-focused workflow</small>
              </div>
            </div>

            <div className="hero-float hero-float-bottom">
              <span className="hero-float-spark">
                <Sparkles size={20} />
              </span>
              <div>
                <strong>Smarter validation</strong>
                <small>Analyze · Review · Improve</small>
              </div>
            </div>
          </div>
        </section>

        {/* Workspace statistics */}
        <section className="dashboard-section">
          <div className="dashboard-section-heading">
            <div>
              <span className="section-kicker">AT A GLANCE</span>
              <h2>Your workspace</h2>
              <p>A snapshot of your current project activity.</p>
            </div>

            <button
              type="button"
              className="dashboard-refresh-button"
              onClick={handleRetry}
              disabled={loading}
              aria-label="Refresh dashboard"
              title="Refresh dashboard"
            >
              <RefreshCw size={16} className={loading ? "is-spinning" : ""} />
              Refresh
            </button>
          </div>

          <div className="dashboard-stats-grid">
            {stats.map(({ label, value, note, tone, Icon }) => (
              <article className="dashboard-stat-card" key={label}>
                <div className="dashboard-stat-top">
                  <span className={`dashboard-stat-icon ${tone}`}>
                    <Icon size={23} strokeWidth={2} />
                  </span>

                  <span className="dashboard-stat-indicator">
                    {loading ? "LOADING" : "WORKSPACE"}
                  </span>
                </div>

                <p>{label}</p>
                <strong className="dashboard-stat-value">{value}</strong>
                <span className="dashboard-stat-note">{note}</span>
              </article>
            ))}
          </div>
        </section>

        {/* Error state */}
        {error && (
          <div className="dashboard-error" role="alert">
            <span className="dashboard-error-icon">
              <CircleAlert size={21} />
            </span>

            <div className="dashboard-error-copy">
              <strong>Unable to load dashboard data</strong>
              <p>{error}</p>

              {error.toLowerCase().includes("log in") && (
                <Link to="/login" className="dashboard-error-login">
                  Go to login <ArrowUpRight size={15} />
                </Link>
              )}
            </div>

            <button type="button" onClick={handleRetry}>
              <RefreshCw size={15} />
              Try again
            </button>
          </div>
        )}

        {/* Projects and quick actions */}
        <section className="dashboard-main-grid">
          <div className="dashboard-panel projects-panel">
            <div className="dashboard-panel-header">
              <div>
                <span className="section-kicker">YOUR WORK</span>
                <h2>Recent projects</h2>
                <p>Pick up where you left off.</p>
              </div>

              <Link to="/create-project" className="dashboard-text-link">
                <Plus size={16} />
                New project
              </Link>
            </div>

            {loading ? (
              <div className="dashboard-empty-state">
                <div className="dashboard-loader" />
                <h3>Loading your projects</h3>
                <p>Getting your workspace ready...</p>
              </div>
            ) : projects.length === 0 && !error ? (
              <div className="dashboard-empty-state">
                <div className="dashboard-empty-icon">
                  <FolderKanban size={27} />
                </div>
                <h3>Your workspace starts here</h3>
                <p>
                  Create your first project to organize and review software
                  requirements.
                </p>
                <Link
                  to="/create-project"
                  className="dashboard-primary-button"
                >
                  <Plus size={17} />
                  Create your first project
                </Link>
              </div>
            ) : projects.length > 0 ? (
              <div className="dashboard-project-list">
                {projects.map((project, index) => {
                  const projectId = project.id ?? project._id;
                  const projectName = project.name || "Untitled project";

                  return (
                    <article
                      className="dashboard-project-item"
                      key={projectId ?? index}
                    >
                      <div className={`project-avatar avatar-${index % 4}`}>
                        {projectName.trim().charAt(0).toUpperCase()}
                      </div>

                      <div className="dashboard-project-info">
                        <h3>{projectName}</h3>
                        <p>
                          {project.description ||
                            "No project description added yet."}
                        </p>

                        <div className="dashboard-project-meta">
                          <span className="project-status-dot" />
                          {project.status || "In progress"}
                        </div>
                      </div>

                      {projectId ? (
                        <Link
                          to={`/project/${projectId}`}
                          className="dashboard-view-link"
                          aria-label={`View ${projectName}`}
                        >
                          View <ArrowRight size={15} />
                        </Link>
                      ) : (
                        <span className="dashboard-view-link is-disabled">
                          Unavailable
                        </span>
                      )}
                    </article>
                  );
                })}
              </div>
            ) : (
              <div className="dashboard-empty-state">
                <div className="dashboard-empty-icon">
                  <CircleAlert size={25} />
                </div>
                <h3>Projects couldn't be loaded</h3>
                <p>Check your connection and try refreshing the dashboard.</p>
                <button
                  type="button"
                  onClick={handleRetry}
                  className="dashboard-primary-button"
                >
                  <RefreshCw size={16} />
                  Reload projects
                </button>
              </div>
            )}
          </div>

          <aside className="dashboard-panel actions-panel">
            <div className="dashboard-panel-header">
              <div>
                <span className="section-kicker">GET STARTED</span>
                <h2>Quick actions</h2>
                <p>Move your work forward.</p>
              </div>
            </div>

            <div className="dashboard-action-list">
              <Link to="/create-project" className="dashboard-action-card">
                <span className="action-icon action-violet">
                  <Plus size={21} />
                </span>
                <span className="action-copy">
                  <strong>Create a project</strong>
                  <small>Set up a new workspace</small>
                </span>
                <ArrowUpRight className="action-arrow" size={18} />
              </Link>

              <Link to="/upload" className="dashboard-action-card">
                <span className="action-icon action-teal">
                  <Upload size={20} />
                </span>
                <span className="action-copy">
                  <strong>Upload an SRS</strong>
                  <small>Add a requirements document</small>
                </span>
                <ArrowUpRight className="action-arrow" size={18} />
              </Link>

              <Link to="/upload" className="dashboard-action-card">
                <span className="action-icon action-blue">
                  <FileText size={20} />
                </span>
                <span className="action-copy">
                  <strong>Start validation</strong>
                  <small>Continue to requirement analysis</small>
                </span>
                <ArrowUpRight className="action-arrow" size={18} />
              </Link>
            </div>

            <div className="dashboard-tip-card">
              <div className="dashboard-tip-symbol">
                <Sparkles size={20} />
              </div>
              <div>
                <strong>Quality starts with clarity</strong>
                <p>
                  Clear, testable requirements make it easier to build and
                  verify the right software.
                </p>
              </div>
            </div>
          </aside>
        </section>

        <div className="dashboard-bottom-note">
          <span className="bottom-note-icon">
            <CheckCircle2 size={19} />
          </span>
          <p>
            <strong>Your work, organized.</strong> Keep project details and
            requirement quality in one place.
          </p>
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default Dashboard;
