
import "./UploadSRS.css";
import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  Upload,
  FileText,
  FileCheck2,
  FolderKanban,
  Sparkles,
  ShieldCheck,
  X,
  LoaderCircle,
  FileUp,
  ClipboardList,
  CheckCircle2,
  AlertCircle,
} from "lucide-react";

const API_URL = "http://localhost:8080";
const MAX_FILE_SIZE = 20 * 1024 * 1024;
const ALLOWED_EXTENSIONS = [".pdf", ".doc", ".docx", ".txt"];

function UploadSRS() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [projects, setProjects] = useState([]);
  const [projectId, setProjectId] = useState("");
  const [projectName, setProjectName] = useState("");
  const [description, setDescription] = useState("");

  const [file, setFile] = useState(null);
  const [requirement, setRequirement] = useState("");
  const [inputType, setInputType] = useState("file");

  const [uploading, setUploading] = useState(false);
  const [loadingProjects, setLoadingProjects] = useState(true);
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      const userData = localStorage.getItem("user");
      const token = localStorage.getItem("token");

      if (!userData || !token) {
        navigate("/login");
        return;
      }

      let user;

      try {
        user = JSON.parse(userData);
      } catch {
        setError("Your session is invalid. Please log in again.");
        navigate("/login");
        return;
      }

      if (!user.email) {
        setError("Your account email was not found. Please log in again.");
        return;
      }

      const response = await fetch(
        `${API_URL}/api/projects/owner/${encodeURIComponent(user.email)}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to load your projects. Please try again.");
      }

      const data = await response.json();
      const projectList = Array.isArray(data) ? data : [];

      setProjects(projectList);

      const urlProjectId = searchParams.get("projectId");

      if (urlProjectId) {
        const selectedProject = projectList.find(
          (project) => String(project.id) === urlProjectId
        );

        if (selectedProject) {
          setProjectId(String(selectedProject.id));
          setProjectName(selectedProject.name || "");
          setDescription(selectedProject.description || "");
        } else {
          setError(
            "The selected project could not be found. Please select a project below."
          );
        }
      }
    } catch (err) {
      console.error("Error loading projects:", err);
      setError(
        err.message || "Unable to load projects. Please try again."
      );
    } finally {
      setLoadingProjects(false);
    }
  };

  const handleProjectChange = (e) => {
    const selectedId = e.target.value;
    setProjectId(selectedId);
    setError("");
    setSuccess("");

    const selectedProject = projects.find(
      (project) => String(project.id) === selectedId
    );

    if (selectedProject) {
      setProjectName(selectedProject.name || "");
      setDescription(selectedProject.description || "");
    } else {
      setProjectName("");
      setDescription("");
    }
  };

  const validateFile = (selectedFile) => {
    if (!selectedFile) return false;

    const extension = selectedFile.name
      .slice(selectedFile.name.lastIndexOf("."))
      .toLowerCase();

    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      setError("Unsupported file type. Please choose a PDF, DOC, DOCX, or TXT file.");
      return false;
    }

    if (selectedFile.size > MAX_FILE_SIZE) {
      setError("The file is too large. The maximum allowed size is 20 MB.");
      return false;
    }

    setError("");
    setSuccess("");
    setFile(selectedFile);
    return true;
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files?.[0];

    if (selectedFile) {
      validateFile(selectedFile);
    }

    // Allows the same file to be selected again after removing it.
    e.target.value = "";
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragActive(false);

    const droppedFile = e.dataTransfer.files?.[0];

    if (droppedFile) {
      validateFile(droppedFile);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!projectId) {
      setError("Please select a project before continuing.");
      return;
    }

    if (!projectName.trim()) {
      setError("The project name cannot be empty.");
      return;
    }

    if (inputType === "file" && !file) {
      setError("Please select an SRS document to upload.");
      return;
    }

    if (inputType === "text" && !requirement.trim()) {
      setError("Please enter at least one software requirement.");
      return;
    }

    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    setUploading(true);

    try {
      if (inputType === "file") {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("projectId", projectId);

        const response = await fetch(
          `${API_URL}/api/documents/upload`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
            body: formData,
          }
        );

        const responseText = await response.text();

        if (!response.ok) {
          throw new Error(
            responseText || "The document could not be uploaded."
          );
        }

        navigate(`/project/${projectId}`);
      } else {
        const response = await fetch(
          `${API_URL}/api/requirements`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              projectId,
              requirementText: requirement.trim(),
              clarity: "",
              ambiguity: "",
              completeness: "",
              consistency: "",
              testability: "",
              measurability: "",
              qualityScore: null,
              status: "Pending",
            }),
          }
        );

        const responseText = await response.text();

        if (!response.ok) {
          throw new Error(
            responseText || "The requirement could not be saved."
          );
        }

        navigate(`/project/${projectId}`);
      }
    } catch (err) {
      console.error("Requirement submission error:", err);

      setError(
        err.message === "Failed to fetch"
          ? "Cannot connect to the backend. Make sure Spring Boot is running."
          : err.message || "Something went wrong. Please try again."
      );
    } finally {
      setUploading(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  return (
    <div className="upload-page upload-page-redesign">
      <header className="upload-header">
        <Link to="/dashboard" className="upload-logo">
          <span className="upload-logo-icon">
            <ShieldCheck size={23} />
          </span>
          <span>
            Req<span className="upload-logo-accent">Validator</span>
          </span>
        </Link>

        <Link to="/dashboard" className="upload-header-back">
          <ArrowLeft size={16} />
          Dashboard
        </Link>
      </header>

      <main className="upload-container upload-container-redesign">
        <div className="upload-title upload-title-redesign">
          <div className="upload-title-icon">
            <Sparkles size={17} />
            REQUIREMENT ANALYSIS
          </div>

          <h1>
            Turn requirements into
            <span> better software.</span>
          </h1>

          <p>
            Upload your Software Requirements Specification or enter
            requirements manually to prepare them for validation.
          </p>
        </div>

        {loadingProjects ? (
          <section className="upload-state-card">
            <LoaderCircle className="upload-spinner" size={30} />
            <h2>Loading your projects</h2>
            <p>Fetching your project workspaces...</p>
          </section>
        ) : projects.length === 0 ? (
          <section className="upload-state-card">
            <div className="upload-state-icon">
              <FolderKanban size={28} />
            </div>
            <h2>No projects yet</h2>
            <p>
              Create a project first. Then you can upload an SRS document
              or enter requirements manually.
            </p>
            <Link
              to="/create-project"
              className="upload-primary-button"
            >
              Create a Project <ArrowRight size={17} />
            </Link>
          </section>
        ) : (
          <form
            className="upload-redesign-form"
            onSubmit={handleSubmit}
          >
            {/* Project details */}
            <section className="upload-redesign-card">
              <div className="upload-card-heading">
                <div className="upload-card-heading-icon">
                  <FolderKanban size={20} />
                </div>

                <div>
                  <span className="upload-step-label">STEP 01</span>
                  <h2>Project information</h2>
                  <p>Choose the workspace for these requirements.</p>
                </div>
              </div>

              <div className="upload-field">
                <label htmlFor="upload-project-select">
                  Select project <span>*</span>
                </label>

                <select
                  id="upload-project-select"
                  value={projectId}
                  onChange={handleProjectChange}
                  required
                  disabled={uploading}
                >
                  <option value="">Choose a project...</option>
                  {projects.map((project) => (
                    <option
                      key={project.id}
                      value={String(project.id)}
                    >
                      {project.name}
                    </option>
                  ))}
                </select>
              </div>

              {projectId && (
                <div className="upload-selected-project">
                  <CheckCircle2 size={17} />
                  <div>
                    <strong>{projectName}</strong>
                    <p>
                      {description || "No project description provided."}
                    </p>
                  </div>
                </div>
              )}
            </section>

            {/* Choose input type */}
            <section className="upload-redesign-card">
              <div className="upload-card-heading">
                <div className="upload-card-heading-icon violet">
                  <FileText size={20} />
                </div>

                <div>
                  <span className="upload-step-label">STEP 02</span>
                  <h2>Add your requirements</h2>
                  <p>Choose how you want to provide your requirements.</p>
                </div>
              </div>

              <div className="upload-mode-switch">
                <button
                  type="button"
                  className={
                    inputType === "file"
                      ? "upload-mode-option active"
                      : "upload-mode-option"
                  }
                  onClick={() => {
                    setInputType("file");
                    setError("");
                    setSuccess("");
                  }}
                  disabled={uploading}
                >
                  <FileUp size={19} />
                  <span>
                    <strong>Upload SRS</strong>
                    <small>PDF, DOC, DOCX, TXT</small>
                  </span>
                </button>

                <button
                  type="button"
                  className={
                    inputType === "text"
                      ? "upload-mode-option active"
                      : "upload-mode-option"
                  }
                  onClick={() => {
                    setInputType("text");
                    setError("");
                    setSuccess("");
                  }}
                  disabled={uploading}
                >
                  <ClipboardList size={19} />
                  <span>
                    <strong>Enter manually</strong>
                    <small>Write your requirements</small>
                  </span>
                </button>
              </div>

              {inputType === "file" ? (
                <div
                  className={
                    dragActive
                      ? "upload-dropzone drag-active"
                      : "upload-dropzone"
                  }
                  onDragOver={(e) => {
                    e.preventDefault();
                    setDragActive(true);
                  }}
                  onDragLeave={(e) => {
                    e.preventDefault();
                    setDragActive(false);
                  }}
                  onDrop={handleDrop}
                >
                  <input
                    id="upload-srs-file"
                    className="upload-file-input"
                    type="file"
                    accept=".pdf,.doc,.docx,.txt"
                    onChange={handleFileChange}
                    disabled={uploading}
                  />

                  {!file ? (
                    <label
                      htmlFor="upload-srs-file"
                      className="upload-dropzone-label"
                    >
                      <span className="upload-dropzone-icon">
                        <Upload size={27} />
                      </span>

                      <strong>Drop your SRS document here</strong>
                      <span className="upload-dropzone-description">
                        or <span>browse files</span> from your computer
                      </span>

                      <div className="upload-file-format-list">
                        <span>PDF</span>
                        <span>DOC</span>
                        <span>DOCX</span>
                        <span>TXT</span>
                        <span>Max 20 MB</span>
                      </div>
                    </label>
                  ) : (
                    <div className="upload-selected-file">
                      <div className="upload-selected-file-icon">
                        <FileCheck2 size={25} />
                      </div>

                      <div className="upload-selected-file-details">
                        <strong title={file.name}>{file.name}</strong>
                        <span>{formatFileSize(file.size)}</span>
                        <span className="upload-file-ready">
                          <CheckCircle2 size={13} />
                          Ready to upload
                        </span>
                      </div>

                      {!uploading && (
                        <button
                          type="button"
                          className="upload-remove-file"
                          onClick={() => setFile(null)}
                          aria-label="Remove selected file"
                          title="Remove file"
                        >
                          <X size={18} />
                        </button>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <div className="upload-manual-entry">
                  <label htmlFor="upload-manual-requirement">
                    Software requirement <span>*</span>
                  </label>

                  <textarea
                    id="upload-manual-requirement"
                    rows={8}
                    maxLength={10000}
                    placeholder="Example: The system shall allow users to create an account using their email address and password."
                    value={requirement}
                    onChange={(e) => {
                      setRequirement(e.target.value);
                      setError("");
                    }}
                    disabled={uploading}
                    required
                  />

                  <div className="upload-manual-footer">
                    <span>
                      Write clear, specific software requirements.
                    </span>
                    <span>{requirement.length}/10000</span>
                  </div>
                </div>
              )}
            </section>

            {/* Validation information */}
            <section className="upload-validation-note">
              <div className="upload-validation-note-icon">
                <Sparkles size={21} />
              </div>

              <div>
                <h3>Ready for requirement validation?</h3>
                <p>
                  Your requirements can be evaluated for ambiguity,
                  completeness, consistency, testability, measurability,
                  and other quality attributes supported by your validator.
                </p>
              </div>
            </section>

            {error && (
              <div className="upload-feedback-error" role="alert">
                <AlertCircle size={18} />
                <span>{error}</span>
              </div>
            )}

            {success && (
              <div className="upload-feedback-success" role="status">
                <CheckCircle2 size={18} />
                <span>{success}</span>
              </div>
            )}

            <div className="upload-redesign-actions">
              <Link
                to="/dashboard"
                className="upload-cancel-button"
              >
                Cancel
              </Link>

              <button
                type="submit"
                className="upload-primary-button"
                disabled={uploading}
              >
                {uploading ? (
                  <>
                    <LoaderCircle
                      size={18}
                      className="upload-spinner"
                    />
                    Processing...
                  </>
                ) : (
                  <>
                    {inputType === "file"
                      ? "Upload & Continue"
                      : "Save Requirement"}
                    <ArrowRight size={18} />
                  </>
                )}
              </button>
            </div>

            <p className="upload-bottom-note">
              <ShieldCheck size={15} />
              Your submission will be associated with the selected project.
            </p>
          </form>
        )}
      </main>
    </div>
  );
}

export default UploadSRS;
