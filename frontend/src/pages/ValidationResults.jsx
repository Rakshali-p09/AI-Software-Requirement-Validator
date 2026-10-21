
import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  ShieldCheck,
  Sparkles,
  FileText,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Clock3,
  ChartNoAxesColumn,
  RefreshCw,
  LoaderCircle,
  BrainCircuit,
  Lightbulb,
  Target,
  Search,
  ClipboardCheck,
} from "lucide-react";

const API_URL = "http://localhost:8080";

function ValidationResults() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const projectId = searchParams.get("projectId");

  const [summary, setSummary] = useState(null);
  const [requirements, setRequirements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [validating, setValidating] = useState(false);
  const [llmAnalysis, setLlmAnalysis] = useState(null);
  const [llmLoading, setLlmLoading] = useState(false);
  const [error, setError] = useState("");
  const [llmError, setLlmError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const runValidation = async () => {
      if (!projectId) {
        navigate("/dashboard");
        return;
      }

      const token = localStorage.getItem("token");

      if (!token) {
        navigate("/login");
        return;
      }

      setLoading(true);
      setValidating(true);
      setError("");

      try {
        const validationResponse = await fetch(
          `${API_URL}/api/validation/project/${encodeURIComponent(projectId)}`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!validationResponse.ok) {
          const message = await validationResponse.text();
          throw new Error(
            message || "Unable to validate requirements."
          );
        }

        const summaryResponse = await fetch(
          `${API_URL}/api/validation/project/${encodeURIComponent(projectId)}/summary`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!summaryResponse.ok) {
          throw new Error("Failed to load the validation summary.");
        }

        const summaryData = await summaryResponse.json();

        const requirementsResponse = await fetch(
          `${API_URL}/api/requirements/project/${encodeURIComponent(projectId)}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!requirementsResponse.ok) {
          throw new Error("Failed to load the requirements.");
        }

        const requirementsData = await requirementsResponse.json();

        if (!cancelled) {
          setSummary(summaryData);
          setRequirements(
            Array.isArray(requirementsData) ? requirementsData : []
          );
        }
      } catch (err) {
        console.error("Validation error:", err);

        if (!cancelled) {
          setError(
            err.message === "Failed to fetch"
              ? "Cannot connect to the backend. Make sure Spring Boot is running."
              : err.message || "Unable to load validation results."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
          setValidating(false);
        }
      }
    };

    runValidation();

    return () => {
      cancelled = true;
    };
  }, [projectId, navigate]);

  const runLlmAnalysis = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    setLlmLoading(true);
    setLlmError("");

    try {
      const response = await fetch(
        `${API_URL}/api/llm/analyze/project/${encodeURIComponent(projectId)}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const responseText = await response.text();
      let data = {};

      try {
        data = responseText ? JSON.parse(responseText) : {};
      } catch {
        data = { message: responseText };
      }

      if (!response.ok) {
        throw new Error(
          data.message || "Unable to run AI analysis."
        );
      }

      setLlmAnalysis(data);
    } catch (err) {
      console.error("AI analysis error:", err);

      setLlmError(
        err.message === "Failed to fetch"
          ? "Cannot connect to the AI analysis service."
          : err.message || "AI analysis could not be completed."
      );
    } finally {
      setLlmLoading(false);
    }
  };

  const formatAttributeName = (name) =>
    (name || "")
      .replace(/_/g, " ")
      .replace(/\b\w/g, (char) => char.toUpperCase());

  const getStatusClass = (status) => {
    const normalized = (status || "").toUpperCase();

    if (["PASS", "GOOD", "CONSISTENT", "UNIQUE"].includes(normalized)) {
      return "vr-status-good";
    }

    if (["FAIL", "POOR", "INCONSISTENT", "DUPLICATE"].includes(normalized)) {
      return "vr-status-bad";
    }

    return "vr-status-review";
  };

  const getStatusIcon = (status) => {
    const normalized = (status || "").toUpperCase();

    if (["PASS", "GOOD", "CONSISTENT", "UNIQUE"].includes(normalized)) {
      return <CheckCircle2 size={15} />;
    }

    if (["FAIL", "POOR", "INCONSISTENT", "DUPLICATE"].includes(normalized)) {
      return <XCircle size={15} />;
    }

    return <AlertTriangle size={15} />;
  };

  if (loading || validating) {
    return (
      <div className="vr-page">
        <header className="vr-header">
          <Link to="/dashboard" className="vr-brand">
            <span className="vr-brand-icon">
              <ShieldCheck size={22} />
            </span>
            <span>
              Req<span>Validator</span>
            </span>
          </Link>
        </header>

        <main className="vr-container">
          <section className="vr-state-card">
            <div className="vr-loading-icon">
              <LoaderCircle size={34} />
            </div>
            <span className="vr-eyebrow">VALIDATION IN PROGRESS</span>
            <h1>Analyzing your requirements</h1>
            <p>
              The validation engine is checking your requirements.
              Please wait while the results are prepared.
            </p>
            <div className="vr-loading-track">
              <span />
            </div>
            <small>
              <Clock3 size={14} />
              This may take a little while.
            </small>
          </section>
        </main>
      </div>
    );
  }

  if (error || !summary) {
    return (
      <div className="vr-page">
        <header className="vr-header">
          <Link to="/dashboard" className="vr-brand">
            <span className="vr-brand-icon">
              <ShieldCheck size={22} />
            </span>
            <span>
              Req<span>Validator</span>
            </span>
          </Link>
        </header>

        <main className="vr-container">
          <section className="vr-state-card">
            <div className="vr-error-icon">
              <AlertTriangle size={29} />
            </div>
            <h1>Results unavailable</h1>
            <p>
              {error ||
                "A validation summary could not be loaded for this project."}
            </p>
            <div className="vr-state-actions">
              <Link
                to={`/project/${projectId}`}
                className="vr-secondary-button"
              >
                <ArrowLeft size={16} />
                Back to Project
              </Link>
              <button
                type="button"
                className="vr-primary-button"
                onClick={() => window.location.reload()}
              >
                <RefreshCw size={16} />
                Try Again
              </button>
            </div>
          </section>
        </main>
      </div>
    );
  }

  const totalRequirements = Number(summary.totalRequirements) || 0;
  const duplicateRequirements = Number(summary.duplicateRequirements) || 0;
  const inconsistentRequirements =
    Number(summary.inconsistentRequirements) || 0;
  const averageScore = Number(summary.averageScore) || 0;
  const goodRequirements = Number(summary.goodRequirements) || 0;
  const needsImprovement = Number(summary.needsImprovement) || 0;
  const poorRequirements = Number(summary.poorRequirements) || 0;

  const score = Math.max(0, Math.min(100, Math.round(averageScore)));

  const scoreStatus =
    score >= 80 ? "Good" : score >= 60 ? "Needs Improvement" : "Poor";

  const issueCount =
    duplicateRequirements +
    inconsistentRequirements +
    needsImprovement +
    poorRequirements;

  const completenessPercentage =
    totalRequirements > 0
      ? Math.round(
          ((goodRequirements + needsImprovement) / totalRequirements) * 100
        )
      : 0;

  const consistencyPercentage =
    totalRequirements > 0
      ? Math.round(
          ((totalRequirements - inconsistentRequirements) /
            totalRequirements) *
            100
        )
      : 0;

  const ambiguityPercentage =
    totalRequirements > 0
      ? Math.round(
          ((totalRequirements - needsImprovement - poorRequirements) /
            totalRequirements) *
            100
        )
      : 0;

  const testabilityPercentage = score;

  const issueRequirements = requirements.filter((req) => {
    const hasIssues = Array.isArray(req.issues) && req.issues.length > 0;
    const isDuplicate =
      req.duplicateStatus && req.duplicateStatus !== "Unique";
    const isInconsistent =
      req.consistency && req.consistency !== "Consistent";
    const needsAttention = req.status && req.status !== "Good";

    return hasIssues || isDuplicate || isInconsistent || needsAttention;
  });

  const llmResults = Array.isArray(llmAnalysis?.results)
    ? llmAnalysis.results
    : [];

  const metrics = [
    {
      label: "Ambiguity",
      value: ambiguityPercentage,
      description:
        ambiguityPercentage >= 80 ? "Good" : "Needs attention",
      icon: Search,
      tone: "violet",
      suffix: "%",
    },
    {
      label: "Completeness",
      value: completenessPercentage,
      description:
        completenessPercentage >= 80 ? "Good" : "Needs improvement",
      icon: ClipboardCheck,
      tone: "teal",
      suffix: "%",
    },
    {
      label: "Consistency",
      value: consistencyPercentage,
      description:
        consistencyPercentage >= 80 ? "Good" : "Needs attention",
      icon: CheckCircle2,
      tone: "blue",
      suffix: "%",
    },
    {
      label: "Testability",
      value: testabilityPercentage,
      description:
        testabilityPercentage >= 80 ? "Good" : "Needs improvement",
      icon: Target,
      tone: "orange",
      suffix: "%",
    },
  ];

  return (
    <div className="vr-page">
      <header className="vr-header">
        <Link to="/dashboard" className="vr-brand">
          <span className="vr-brand-icon">
            <ShieldCheck size={22} />
          </span>
          <span>
            Req<span>Validator</span>
          </span>
        </Link>

        <Link to={`/project/${projectId}`} className="vr-header-link">
          <ArrowLeft size={16} />
          Back to Project
        </Link>
      </header>

      <main className="vr-container">
        {/* Page heading */}
        <section className="vr-page-heading">
          <span className="vr-eyebrow">
            <Sparkles size={15} />
            VALIDATION REPORT
          </span>
          <h1>Requirement Validation Results</h1>
          <p>
            Review requirement quality, identify potential issues, and
            explore AI-powered explanations.
          </p>
        </section>

        {/* Overall score */}
        <section className="vr-score-panel">
          <div className="vr-score-copy">
            <span className="vr-score-label">OVERALL QUALITY SCORE</span>
            <div className="vr-score-number">
              {score}
              <span>/100</span>
            </div>
            <span className={`vr-score-status ${getStatusClass(scoreStatus)}`}>
              {getStatusIcon(scoreStatus)}
              {scoreStatus}
            </span>
            <p>
              {totalRequirements}{" "}
              {totalRequirements === 1 ? "requirement" : "requirements"}{" "}
              analyzed.
              {issueCount > 0
                ? ` ${issueCount} issue categories need attention.`
                : " Review the details below for your validation summary."}
            </p>
          </div>

          <div className="vr-score-visual">
            <div
              className="vr-score-ring"
              style={{ "--score": `${score * 3.6}deg` }}
            >
              <div className="vr-score-ring-inner">
                <strong>{score}%</strong>
                <span>Quality</span>
              </div>
            </div>
          </div>

          <div className="vr-score-footnote">
            <ShieldCheck size={16} />
            Score based on the validation summary returned by your backend.
          </div>
        </section>

        {/* Summary cards */}
        <section className="vr-overview-grid">
          <div className="vr-overview-card">
            <span className="vr-overview-icon teal">
              <FileText size={20} />
            </span>
            <span className="vr-overview-label">Total Requirements</span>
            <strong>{totalRequirements}</strong>
            <small>Analyzed by the validator</small>
          </div>

          <div className="vr-overview-card">
            <span className="vr-overview-icon violet">
              <CheckCircle2 size={20} />
            </span>
            <span className="vr-overview-label">Good Requirements</span>
            <strong>{goodRequirements}</strong>
            <small>Marked as good</small>
          </div>

          <div className="vr-overview-card">
            <span className="vr-overview-icon orange">
              <AlertTriangle size={20} />
            </span>
            <span className="vr-overview-label">Needs Improvement</span>
            <strong>{needsImprovement}</strong>
            <small>May need refinement</small>
          </div>

          <div className="vr-overview-card">
            <span className="vr-overview-icon red">
              <XCircle size={20} />
            </span>
            <span className="vr-overview-label">Poor Requirements</span>
            <strong>{poorRequirements}</strong>
            <small>Require closer review</small>
          </div>
        </section>

        {/* Quality metrics */}
        <section className="vr-section">
          <div className="vr-section-heading">
            <div>
              <span className="vr-eyebrow">QUALITY BREAKDOWN</span>
              <h2>Requirement quality attributes</h2>
              <p>Review the calculated indicators from your validation summary.</p>
            </div>
          </div>

          <div className="vr-metrics-grid">
            {metrics.map((metric) => {
              const Icon = metric.icon;

              return (
                <article className="vr-metric-card" key={metric.label}>
                  <div className="vr-metric-top">
                    <span className={`vr-metric-icon ${metric.tone}`}>
                      <Icon size={19} />
                    </span>
                    <span className="vr-metric-value">
                      {metric.value}
                      {metric.suffix}
                    </span>
                  </div>

                  <h3>{metric.label}</h3>
                  <p>{metric.description}</p>

                  <div
                    className="vr-progress-track"
                    role="progressbar"
                    aria-label={metric.label}
                    aria-valuenow={metric.value}
                    aria-valuemin={0}
                    aria-valuemax={100}
                  >
                    <span
                      className={`vr-progress-fill ${metric.tone}`}
                      style={{
                        width: `${Math.max(
                          0,
                          Math.min(100, metric.value)
                        )}%`,
                      }}
                    />
                  </div>
                </article>
              );
            })}
          </div>
        </section>

        {/* Detected issues */}
        <section className="vr-section">
          <div className="vr-section-heading vr-issue-heading">
            <div>
              <span className="vr-eyebrow">DETECTED ISSUES</span>
              <h2>Requirements that need attention</h2>
              <p>Review flagged requirements and any available suggestions.</p>
            </div>

            <span className="vr-count-badge">
              {issueRequirements.length} flagged
            </span>
          </div>

          {issueRequirements.length === 0 ? (
            <div className="vr-empty-card">
              <span className="vr-empty-icon">
                <CheckCircle2 size={27} />
              </span>
              <h3>No flagged requirements</h3>
              <p>
                No requirements were flagged by the current issue-filtering
                rules. Review the quality summary and AI analysis as needed.
              </p>
            </div>
          ) : (
            <div className="vr-issues-list">
              {issueRequirements.map((req, index) => (
                <article
                  className="vr-issue-card"
                  key={req.id ?? req.requirementId ?? index}
                >
                  <div className="vr-issue-top">
                    <div className="vr-issue-heading-copy">
                      <span className="vr-issue-alert-icon">
                        <AlertTriangle size={18} />
                      </span>
                      <div>
                        <span className="vr-issue-id">
                          {req.requirementId || `REQ-${index + 1}`}
                        </span>
                        <h3>Requirement needs review</h3>
                      </div>
                    </div>

                    <span className={`vr-status ${getStatusClass(req.status)}`}>
                      {getStatusIcon(req.status)}
                      {req.status || "Review"}
                    </span>
                  </div>

                  <div className="vr-original-requirement">
                    <span>ORIGINAL REQUIREMENT</span>
                    <p>{req.requirementText || "No requirement text available."}</p>
                  </div>

                  {Array.isArray(req.issues) && req.issues.length > 0 && (
                    <div className="vr-detail-block">
                      <h4>
                        <AlertTriangle size={16} />
                        Detected issues
                      </h4>
                      <ul>
                        {req.issues.map((issue, issueIndex) => (
                          <li key={issueIndex}>{issue}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {Array.isArray(req.suggestions) &&
                    req.suggestions.length > 0 && (
                      <div className="vr-suggestion-block">
                        <h4>
                          <Lightbulb size={16} />
                          Suggested improvements
                        </h4>
                        <ul>
                          {req.suggestions.map((suggestion, suggestionIndex) => (
                            <li key={suggestionIndex}>{suggestion}</li>
                          ))}
                        </ul>
                      </div>
                    )}

                  <div className="vr-issue-metadata">
                    <span>
                      Quality score:{" "}
                      {req.qualityScore !== null &&
                      req.qualityScore !== undefined
                        ? `${req.qualityScore}/100`
                        : "Not scored"}
                    </span>
                    <span>Clarity: {req.clarity || "—"}</span>
                    <span>Testability: {req.testability || "—"}</span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        {/* Detailed counts */}
        <section className="vr-section">
          <div className="vr-section-heading">
            <div>
              <span className="vr-eyebrow">VALIDATION SUMMARY</span>
              <h2>Analysis statistics</h2>
              <p>Counts reported by the backend validation summary.</p>
            </div>
          </div>

          <div className="vr-statistics-grid">
            {[
              { label: "Duplicate requirements", value: duplicateRequirements },
              {
                label: "Inconsistent requirements",
                value: inconsistentRequirements,
              },
              { label: "Needs improvement", value: needsImprovement },
              { label: "Poor requirements", value: poorRequirements },
            ].map((item) => (
              <div className="vr-statistic-row" key={item.label}>
                <span>{item.label}</span>
                <strong>{item.value}</strong>
              </div>
            ))}
          </div>
        </section>

        {/* Optional AI analysis */}
        <section className="vr-section vr-ai-section">
          <div className="vr-section-heading">
            <div>
              <span className="vr-eyebrow">AI-POWERED REVIEW</span>
              <h2>Detailed AI analysis</h2>
              <p>
                Run the integrated AI analysis to view explanations,
                quality-attribute findings, and recommendations.
              </p>
            </div>

            <span className="vr-ai-heading-icon">
              <BrainCircuit size={24} />
            </span>
          </div>

          {!llmAnalysis && (
            <div className="vr-ai-prompt">
              <div className="vr-ai-prompt-icon">
                <Sparkles size={24} />
              </div>
              <div className="vr-ai-prompt-copy">
                <h3>Explore deeper requirement insights</h3>
                <p>
                  The AI analysis can provide attribute-level findings
                  and recommendations when returned by your AI service.
                </p>
              </div>

              <button
                type="button"
                className="vr-primary-button"
                onClick={runLlmAnalysis}
                disabled={llmLoading}
              >
                {llmLoading ? (
                  <>
                    <LoaderCircle size={17} className="vr-spinner" />
                    Analyzing...
                  </>
                ) : (
                  <>
                    <BrainCircuit size={17} />
                    Analyze with AI
                  </>
                )}
              </button>
            </div>
          )}

          {llmError && (
            <div className="vr-error-banner" role="alert">
              <AlertTriangle size={18} />
              <span>{llmError}</span>
              <button
                type="button"
                onClick={runLlmAnalysis}
                disabled={llmLoading}
              >
                Try again
              </button>
            </div>
          )}

          {llmAnalysis && (
            <div className="vr-ai-results">
              <div className="vr-ai-summary">
                <span className="vr-ai-summary-icon">
                  <CheckCircle2 size={21} />
                </span>
                <div>
                  <h3>AI analysis response received</h3>
                  <p>
                    Requirements reported by the AI service:{" "}
                    {llmAnalysis.total_requirements ?? llmResults.length}
                  </p>
                </div>
                <button
                  type="button"
                  className="vr-secondary-button"
                  onClick={runLlmAnalysis}
                  disabled={llmLoading}
                >
                  {llmLoading ? (
                    <LoaderCircle size={15} className="vr-spinner" />
                  ) : (
                    <RefreshCw size={15} />
                  )}
                  Run again
                </button>
              </div>

              {llmResults.length === 0 ? (
                <div className="vr-empty-card">
                  <p>
                    The AI service returned no individual requirement
                    analysis results.
                  </p>
                </div>
              ) : (
                <div className="vr-issues-list">
                  {llmResults.map((result, index) => {
                    const analysis = result.analysis || {};
                    const dimensions = analysis.dimensions || {};

                    return (
                      <article
                        className="vr-issue-card vr-ai-result-card"
                        key={result.requirement_id ?? index}
                      >
                        <div className="vr-original-requirement">
                          <span>
                            REQUIREMENT{" "}
                            {result.requirement_id || `#${index + 1}`}
                          </span>
                          <p>{result.requirement || "No text provided."}</p>
                        </div>

                        <div className="vr-ai-overall">
                          <span>Overall status</span>
                          <span
                            className={`vr-status ${getStatusClass(
                              analysis.overall_status
                            )}`}
                          >
                            {getStatusIcon(analysis.overall_status)}
                            {analysis.overall_status || "NEEDS_REVIEW"}
                          </span>
                        </div>

                        {Object.keys(dimensions).length > 0 && (
                          <div className="vr-dimensions">
                            <h4>Quality attributes</h4>

                            {Object.entries(dimensions).map(
                              ([attribute, details]) => {
                                const status =
                                  details?.status || "NEEDS_REVIEW";

                                const ruleReasons = Array.isArray(
                                  details?.rule_reason
                                )
                                  ? details.rule_reason
                                  : [];

                                return (
                                  <div
                                    className="vr-dimension-card"
                                    key={attribute}
                                  >
                                    <div className="vr-dimension-top">
                                      <strong>
                                        {formatAttributeName(attribute)}
                                      </strong>
                                      <span
                                        className={`vr-status ${getStatusClass(
                                          status
                                        )}`}
                                      >
                                        {getStatusIcon(status)}
                                        {status}
                                      </span>
                                    </div>

                                    {ruleReasons.length > 0 && (
                                      <ul>
                                        {ruleReasons.map((reason, i) => (
                                          <li key={i}>{reason}</li>
                                        ))}
                                      </ul>
                                    )}

                                    {details?.llm_reason && (
                                      <p className="vr-ai-explanation">
                                        <strong>AI explanation:</strong>{" "}
                                        {details.llm_reason}
                                      </p>
                                    )}

                                    {ruleReasons.length === 0 &&
                                      !details?.llm_reason && (
                                        <p className="vr-muted-copy">
                                          No specific explanation was provided.
                                        </p>
                                      )}
                                  </div>
                                );
                              }
                            )}
                          </div>
                        )}

                        {analysis.main_issue && (
                          <div className="vr-detail-block">
                            <h4>
                              <AlertTriangle size={16} />
                              Main issue
                            </h4>
                            <p>{analysis.main_issue}</p>
                          </div>
                        )}

                        {analysis.recommendation && (
                          <div className="vr-suggestion-block">
                            <h4>
                              <Lightbulb size={16} />
                              Recommendation
                            </h4>
                            <p>{analysis.recommendation}</p>
                          </div>
                        )}
                      </article>
                    );
                  })}
                </div>
              )}
            </div>
          )}
        </section>

        {/* Footer actions */}
        <div className="vr-bottom-actions">
          <Link
            to={`/project/${projectId}`}
            className="vr-secondary-button"
          >
            <ArrowLeft size={16} />
            Back to Project
          </Link>

          <Link
            to={`/upload?projectId=${encodeURIComponent(projectId)}`}
            className="vr-primary-button"
          >
            Analyze Another
            <ArrowRight size={16} />
          </Link>
        </div>

        <footer className="vr-footer">
          <span>
            <ShieldCheck size={15} />
            ReqValidator
          </span>
          <span>AI Software Requirement Validator · Capstone Project</span>
        </footer>
      </main>
    </div>
  );
}

export default ValidationResults;
