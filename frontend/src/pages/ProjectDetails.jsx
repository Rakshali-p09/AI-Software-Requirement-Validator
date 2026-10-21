
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  FolderKanban,
  FileText,
  Upload,
  ClipboardList,
  ChartNoAxesColumn,
  Trash2,
  ShieldCheck,
  Clock3,
  AlertCircle,
  LoaderCircle,
  Sparkles,
  Hash,
  UserRound,
  CalendarDays,
} from "lucide-react";

import StatCard from "../components/StatCard";
import RequirementCard from "../components/RequirementCard";

const API_URL = "http://localhost:8080";

function ProjectDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [project, setProject] = useState(null);
  const [requirements, setRequirements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingRequirements, setLoadingRequirements] = useState(true);
  const [pageError, setPageError] = useState("");
  const [requirementsError, setRequirementsError] = useState("");
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const loadProject = async () => {
      setLoading(true);
      setPageError("");

      const token = localStorage.getItem("token");

      if (!token) {
        navigate("/login");
        return;
      }

      try {
        const response = await fetch(`${API_URL}/api/projects/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error(
            response.status === 404
              ? "This project could not be found."
              : "Unable to load project information."
          );
        }

        const data = await response.json();

        if (!cancelled) {
          setProject(data);
        }
      } catch (error) {
        console.error("Error fetching project:", error);

        if (!cancelled) {
          setPageError(
            error.message === "Failed to fetch"
              ? "Cannot connect to the backend. Make sure Spring Boot is running."
              : error.message || "Unable to load this project."
          );
          setProject(null);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    const loadRequirements = async () => {
      setLoadingRequirements(true);
      setRequirementsError("");

      const token = localStorage.getItem("token");

      if (!token) {
        setLoadingRequirements(false);
        return;
      }

      try {
        const response = await fetch(
          `${API_URL}/api/requirements/project/${id}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!response.ok) {
          throw new Error("Unable to load requirements.");
        }

        const data = await response.json();

        if (!cancelled) {
          setRequirements(Array.isArray(data) ? data : []);
        }
      } catch (error) {
        console.error("Error fetching requirements:", error);

        if (!cancelled) {
          setRequirementsError(
            error.message === "Failed to fetch"
              ? "Cannot connect to the backend."
              : error.message || "Unable to load requirements."
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingRequirements(false);
        }
      }
    };

    loadProject();
    loadRequirements();

    return () => {
      cancelled = true;
    };
  }, [id, navigate]);

  const handleDelete = async () => {
    const confirmed = window.confirm(
      `Delete "${project?.name || "this project"}"? This action cannot be undone.`
    );

    if (!confirmed) return;

    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    setDeleting(true);
    setPageError("");

    try {
      const response = await fetch(`${API_URL}/api/projects/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Failed to delete project.");
      }

      alert("Project deleted successfully!");
      navigate("/dashboard");
    } catch (error) {
      console.error("Error deleting project:", error);

      setPageError(
        error.message === "Failed to fetch"
          ? "Cannot connect to the backend. Make sure Spring Boot is running."
          : error.message || "Unable to delete this project."
      );
    } finally {
      setDeleting(false);
    }
  };

  const completedCount = requirements.filter(
    (req) => req.qualityScore !== null && req.qualityScore !== undefined
  ).length;

  const averageScore =
    completedCount > 0
      ? Math.round(
          requirements.reduce(
            (sum, req) =>
              req.qualityScore !== null && req.qualityScore !== undefined
                ? sum + Number(req.qualityScore || 0)
                : sum,
            0
          ) / completedCount
        )
      : null;

  if (loading) {
    return (
      <div className="pd-page">
        <div className="pd-state-card">
          <LoaderCircle size={32} className="pd-spinner" />
          <h2>Loading project</h2>
          <p>Getting your project information ready...</p>
        </div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="pd-page">
        <div className="pd-state-card">
          <div className="pd-state-icon error">
            <AlertCircle size={27} />
          </div>

          <h2>Project unavailable</h2>
          <p>{pageError || "The requested project could not be found."}</p>

          <Link to="/dashboard" className="pd-primary-button">
            <ArrowLeft size={17} />
            Back to Dashboard
          </Link>
        </div>
      </div>
    );
  }

  const projectId = project.id ?? project._id ?? id;

  return (
    <div className="pd-page">
      <div className="pd-container">
        {/* Top navigation */}
        <header className="pd-topbar">
          <Link to="/dashboard" className="pd-brand">
            <span className="pd-brand-icon">
              <ShieldCheck size={22} />
            </span>
            <span>
              Req<span className="pd-brand-accent">Validator</span>
            </span>
          </Link>

          <Link to="/dashboard" className="pd-back-link">
            <ArrowLeft size={16} />
            Dashboard
          </Link>
        </header>

        {/* Project heading */}
        <section className="pd-hero">
          <div className="pd-hero-main">
            <div className="pd-breadcrumb">
              <Link to="/dashboard">Projects</Link>
              <span>/</span>
              <span>Project details</span>
            </div>

            <div className="pd-project-title-row">
              <div className="pd-project-icon">
                <FolderKanban size={27} />
              </div>

              <div className="pd-project-title-content">
                <span className="pd-eyebrow">PROJECT WORKSPACE</span>
                <h1>{project.name || "Untitled Project"}</h1>
              </div>
            </div>

            <p className="pd-hero-description">
              {project.description ||
                "Manage your requirements and review their validation status."}
            </p>

            <div className="pd-hero-meta">
              <span className="pd-meta-item">
                <Hash size={14} />
                ID: {projectId}
              </span>

              {project.ownerEmail && (
                <span className="pd-meta-item">
                  <UserRound size={14} />
                  {project.ownerEmail}
                </span>
              )}
            </div>
          </div>

          <div className="pd-hero-actions">
            <Link
              to={`/upload?projectId=${encodeURIComponent(projectId)}`}
              className="pd-primary-button"
            >
              <Upload size={17} />
              Add Requirements
              <ArrowRight size={16} />
            </Link>
          </div>
        </section>

        {pageError && (
          <div className="pd-error-banner" role="alert">
            <AlertCircle size={18} />
            <span>{pageError}</span>
          </div>
        )}

        {/* Statistics using StatCard */}
        <section className="pd-stats-grid">
          <StatCard
            title="Total Requirements"
            value={loadingRequirements ? "—" : requirements.length}
            icon={FileText}
            color="teal"
            description="Requirements in this project"
          />

          <StatCard
            title="Analyzed"
            value={loadingRequirements ? "—" : completedCount}
            icon={Sparkles}
            color="violet"
            description="With a quality score"
          />

          <StatCard
            title="Average Quality"
            value={
              loadingRequirements || averageScore === null
                ? "—"
                : `${averageScore}/100`
            }
            icon={ChartNoAxesColumn}
            color="blue"
            description="Based on available scores"
          />

          <StatCard
            title="Project Status"
            value={project.status || "Created"}
            icon={Clock3}
            color="orange"
            description="Current project status"
          />
        </section>

        {/* Main content */}
        <div className="pd-content-grid">
          <section className="pd-panel pd-information-panel">
            <div className="pd-panel-heading">
              <div>
                <span className="pd-eyebrow">OVERVIEW</span>
                <h2>Project information</h2>
                <p>Details associated with this workspace.</p>
              </div>

              <span className="pd-panel-heading-icon">
                <FolderKanban size={20} />
              </span>
            </div>

            <div className="pd-information-list">
              <div className="pd-information-item">
                <span>Project name</span>
                <strong>{project.name || "—"}</strong>
              </div>

              <div className="pd-information-item">
                <span>Description</span>
                <strong className="pd-description-value">
                  {project.description || "No description provided."}
                </strong>
              </div>

              <div className="pd-information-item">
                <span>Owner</span>
                <strong className="pd-break-text">
                  {project.ownerEmail || "—"}
                </strong>
              </div>

              <div className="pd-information-item">
                <span>Status</span>
                <span className="pd-status pd-status-default">
                  {project.status || "Created"}
                </span>
              </div>

              <div className="pd-information-item">
                <span>Project ID</span>
                <strong className="pd-break-text">{projectId}</strong>
              </div>
            </div>
          </section>

          <div className="pd-right-column">
            <section className="pd-panel">
              <div className="pd-panel-heading">
                <div>
                  <span className="pd-eyebrow">WORKFLOW</span>
                  <h2>Project actions</h2>
                  <p>Continue working on your requirements.</p>
                </div>
              </div>

              <div className="pd-action-list">
                <Link
                  to={`/upload?projectId=${encodeURIComponent(projectId)}`}
                  className="pd-action-card"
                >
                  <span className="pd-action-icon teal">
                    <Upload size={20} />
                  </span>
                  <span className="pd-action-copy">
                    <strong>Upload SRS</strong>
                    <small>Upload a requirements document</small>
                  </span>
                  <ArrowRight size={17} className="pd-action-arrow" />
                </Link>

                <Link
                  to={`/upload?projectId=${encodeURIComponent(projectId)}`}
                  className="pd-action-card"
                >
                  <span className="pd-action-icon violet">
                    <ClipboardList size={20} />
                  </span>
                  <span className="pd-action-copy">
                    <strong>Add Requirement</strong>
                    <small>Enter a requirement manually</small>
                  </span>
                  <ArrowRight size={17} className="pd-action-arrow" />
                </Link>

                <Link
                  to={`/validation-results?projectId=${encodeURIComponent(projectId)}`}
                  className="pd-action-card"
                >
                  <span className="pd-action-icon blue">
                    <ChartNoAxesColumn size={20} />
                  </span>
                  <span className="pd-action-copy">
                    <strong>Validation Results</strong>
                    <small>Review requirement quality results</small>
                  </span>
                  <ArrowRight size={17} className="pd-action-arrow" />
                </Link>
              </div>
            </section>

            <section className="pd-panel pd-management-panel">
              <div className="pd-panel-heading">
                <div>
                  <h2>Project management</h2>
                  <p>Manage this workspace.</p>
                </div>
              </div>

              <button
                type="button"
                className="pd-delete-button"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? (
                  <LoaderCircle size={17} className="pd-spinner" />
                ) : (
                  <Trash2 size={17} />
                )}
                {deleting ? "Deleting project..." : "Delete Project"}
              </button>
            </section>
          </div>
        </div>

        {/* Requirements using RequirementCard */}
        <section className="pd-panel pd-requirements-panel">
          <div className="pd-panel-heading pd-requirements-heading">
            <div>
              <span className="pd-eyebrow">REQUIREMENT TRACKER</span>
              <h2>Requirements</h2>
              <p>Review requirements associated with this project.</p>
            </div>

            <span className="pd-requirements-count">
              {loadingRequirements
                ? "Loading..."
                : `${requirements.length} ${
                    requirements.length === 1
                      ? "requirement"
                      : "requirements"
                  }`}
            </span>
          </div>

          {loadingRequirements ? (
            <div className="pd-inline-state">
              <LoaderCircle size={24} className="pd-spinner" />
              <p>Loading requirements...</p>
            </div>
          ) : requirementsError ? (
            <div className="pd-inline-state">
              <AlertCircle size={25} />
              <h3>Unable to load requirements</h3>
              <p>{requirementsError}</p>
            </div>
          ) : requirements.length === 0 ? (
            <div className="pd-empty-state">
              <span className="pd-empty-icon">
                <FileText size={27} />
              </span>

              <h3>No requirements added yet</h3>
              <p>
                Upload an SRS document or add a requirement manually to start
                building your validation workspace.
              </p>

              <Link
                to={`/upload?projectId=${encodeURIComponent(projectId)}`}
                className="pd-primary-button"
              >
                <Upload size={17} />
                Add Your First Requirement
                <ArrowRight size={16} />
              </Link>
            </div>
          ) : (
            <div className="pd-requirements-list">
              {requirements.map((req, index) => (
                <RequirementCard
                  key={req.id ?? req.requirementId ?? index}
                  requirement={{
                    ...req,
                    requirementText:
                      req.requirementText ??
                      req.requirement ??
                      "No requirement text available.",
                  }}
                />
              ))}
            </div>
          )}
        </section>

        <footer className="pd-footer">
          <span>
            <ShieldCheck size={15} />
            ReqValidator Project Workspace
          </span>
          <span>
            <CalendarDays size={14} />
            Capstone Project
          </span>
        </footer>
      </div>
    </div>
  );
}

export default ProjectDetails;
