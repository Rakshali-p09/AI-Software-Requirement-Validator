
import { FileText, CheckCircle, AlertTriangle } from "lucide-react";

function RequirementCard({ requirement }) {
  const text =
    requirement?.requirementText ||
    requirement?.requirement ||
    "No requirement text available";

  const score = Number(requirement?.qualityScore ?? 0);
  const status = requirement?.status || "PENDING";

  const isGood =
    status.toUpperCase() === "PASS" ||
    status.toUpperCase() === "GOOD";

  return (
    <div className="requirement-card">
      <div className="requirement-card-header">
        <div className="requirement-card-icon">
          <FileText size={20} />
        </div>

        <span
          className={`requirement-status ${
            isGood ? "status-good" : "status-review"
          }`}
        >
          {isGood ? (
            <CheckCircle size={14} />
          ) : (
            <AlertTriangle size={14} />
          )}
          {status}
        </span>
      </div>

      <p className="requirement-card-text">{text}</p>

      <div className="requirement-card-footer">
        <span>Quality Score</span>
        <strong>{score}/100</strong>
      </div>

      <div className="requirement-score-track">
        <div
          className="requirement-score-fill"
          style={{
            width: `${Math.max(0, Math.min(100, score))}%`,
          }}
        />
      </div>
    </div>
  );
}

export default RequirementCard;
