
import { TrendingUp } from "lucide-react";

function StatCard({
  title,
  value,
  icon: Icon,
  color = "teal",
  description,
}) {
  return (
    <div className="stat-card">
      <div className="stat-card-top">
        <div className={`stat-card-icon ${color}`}>
          {Icon ? <Icon size={22} /> : <TrendingUp size={22} />}
        </div>

        <span className="stat-card-label">{title}</span>
      </div>

      <div className="stat-card-value">{value ?? 0}</div>

      {description && (
        <p className="stat-card-description">{description}</p>
      )}
    </div>
  );
}

export default StatCard;
