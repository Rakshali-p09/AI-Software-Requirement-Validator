
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  FolderPlus,
  FileText,
  Sparkles,
  ShieldCheck,
  LoaderCircle,
  CheckCircle2,
} from "lucide-react";

function CreateProject() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleCreateProject = async (e) => {
    e.preventDefault();
    setError("");

    const trimmedName = name.trim();
    const trimmedDescription = description.trim();

    if (!trimmedName || !trimmedDescription) {
      setError("Please enter both a project name and description.");
      return;
    }

    const userData = localStorage.getItem("user");
    const token = localStorage.getItem("token");

    if (!userData || !token) {
      alert("Please login first.");
      navigate("/login");
      return;
    }

    let user;

    try {
      user = JSON.parse(userData);
    } catch {
      setError("Your login session is invalid. Please log in again.");
      localStorage.removeItem("user");
      localStorage.removeItem("token");
      navigate("/login");
      return;
    }

    if (!user.email) {
      setError("User email was not found. Please log in again.");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/projects", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name: trimmedName,
          description: trimmedDescription,
          ownerEmail: user.email,
          status: "Created",
        }),
      });

      if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(
          errorMessage || "Unable to create the project. Please try again."
        );
      }

      alert("Project created successfully!");
      navigate("/dashboard");
    } catch (err) {
      console.error("Error creating project:", err);

      setError(
        err.message === "Failed to fetch"
          ? "Cannot connect to the backend. Please make sure the backend server is running."
          : err.message || "Something went wrong. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-project-page">
      <div className="create-project-container">
        <button
          type="button"
          className="create-project-back-link"
          onClick={() => navigate("/dashboard")}
        >
          <ArrowLeft size={17} />
          Back to Dashboard
        </button>

        <div className="create-project-layout">
          {/* Left information panel */}
          <aside className="create-project-info">
            <div className="create-project-info-icon">
              <FolderPlus size={28} />
            </div>

            <span className="create-project-eyebrow">
              PROJECT WORKSPACE
            </span>

            <h1>
              Start with a <span>clear vision.</span>
            </h1>

            <p className="create-project-info-description">
              Create a workspace for your software requirements.
              Organize project details and prepare for requirement
              validation in one place.
            </p>

            <div className="create-project-benefits">
              <div className="create-project-benefit">
                <span className="create-project-benefit-icon">
                  <FileText size={17} />
                </span>
                <div>
                  <strong>Organized requirements</strong>
                  <p>Keep your project information together.</p>
                </div>
              </div>

              <div className="create-project-benefit">
                <span className="create-project-benefit-icon violet">
                  <Sparkles size={17} />
                </span>
                <div>
                  <strong>Validation-ready workspace</strong>
                  <p>Prepare to review your requirements.</p>
                </div>
              </div>

              <div className="create-project-benefit">
                <span className="create-project-benefit-icon blue">
                  <ShieldCheck size={17} />
                </span>
                <div>
                  <strong>One project at a time</strong>
                  <p>Start with the details that matter.</p>
                </div>
              </div>
            </div>

            <div className="create-project-note">
              <CheckCircle2 size={17} />
              <span>
                You can add your requirements after creating the project.
              </span>
            </div>
          </aside>

          {/* Project creation form */}
          <section className="create-project-form-card">
            <div className="create-project-form-heading">
              <span className="create-project-form-kicker">
                NEW WORKSPACE
              </span>

              <h2>Create a project</h2>

              <p>
                Enter the basic details to set up your validation workspace.
              </p>
            </div>

            <form onSubmit={handleCreateProject}>
              <div className="create-project-field">
                <label htmlFor="project-name">
                  Project name <span>*</span>
                </label>

                <div className="create-project-input-wrap">
                  <FolderPlus size={18} />

                  <input
                    id="project-name"
                    type="text"
                    placeholder="e.g. E-commerce Requirement Analysis"
                    value={name}
                    onChange={(e) => {
                      setName(e.target.value);
                      setError("");
                    }}
                    maxLength={100}
                    required
                    disabled={loading}
                  />
                </div>

                <small>
                  Choose a short, meaningful name for your project.
                </small>
              </div>

              <div className="create-project-field">
                <label htmlFor="project-description">
                  Project description <span>*</span>
                </label>

                <div className="create-project-textarea-wrap">
                  <textarea
                    id="project-description"
                    placeholder="Describe the project goals, scope, and what you want to validate..."
                    value={description}
                    onChange={(e) => {
                      setDescription(e.target.value);
                      setError("");
                    }}
                    rows={6}
                    maxLength={1000}
                    required
                    disabled={loading}
                  />
                </div>

                <div className="create-project-field-footer">
                  <small>Include the main purpose and scope.</small>
                  <small>{description.length}/1000</small>
                </div>
              </div>

              {error && (
                <div className="create-project-error" role="alert">
                  {error}
                </div>
              )}

              <div className="create-project-form-actions">
                <button
                  type="button"
                  className="create-project-cancel"
                  onClick={() => navigate("/dashboard")}
                  disabled={loading}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="create-project-submit"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <LoaderCircle
                        size={18}
                        className="create-project-spinner"
                      />
                      Creating...
                    </>
                  ) : (
                    <>
                      Create Project
                      <ArrowRight size={18} />
                    </>
                  )}
                </button>
              </div>
            </form>

            <div className="create-project-secure-note">
              <ShieldCheck size={15} />
              Your project will be associated with your account.
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}

export default CreateProject;
