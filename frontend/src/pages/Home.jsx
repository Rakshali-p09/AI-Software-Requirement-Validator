
import { Link } from "react-router-dom";
import {
  ShieldCheck,
  FileSearch,
  CopyCheck,
  GitCompareArrows,
  ChartNoAxesCombined,
  ArrowRight,
  Upload,
  BrainCircuit,
  CheckCircle2,
  Sparkles,
  ScanSearch,
  ShieldAlert,
} from "lucide-react";

import Navbar from "../components/Navbar";
import Footer from "../components/Footer";

function Home() {
  const features = [
    {
      icon: <FileSearch size={23} />,
      title: "Ambiguity Detection",
      description:
        'Identify vague terms such as "fast", "easy", and "user-friendly" that can lead to misunderstandings.',
      tone: "teal",
    },
    {
      icon: <CopyCheck size={23} />,
      title: "Duplicate Detection",
      description:
        "Find repeated or highly similar requirements to keep your specification clear and consistent.",
      tone: "violet",
    },
    {
      icon: <GitCompareArrows size={23} />,
      title: "Conflict Detection",
      description:
        "Highlight potentially conflicting requirements before they cause problems during development.",
      tone: "blue",
    },
    {
      icon: <ChartNoAxesCombined size={23} />,
      title: "Quality Scoring",
      description:
        "Review requirement quality and use actionable feedback to improve your software specification.",
      tone: "orange",
    },
  ];

  const steps = [
    {
      number: "01",
      icon: <Upload size={22} />,
      title: "Upload Requirements",
      description:
        "Upload your SRS document or enter your software requirements manually.",
    },
    {
      number: "02",
      icon: <BrainCircuit size={22} />,
      title: "Analyze with AI",
      description:
        "Evaluate requirements for ambiguity, duplication, conflicts, and completeness.",
    },
    {
      number: "03",
      icon: <CheckCircle2 size={22} />,
      title: "Review Results",
      description:
        "Explore detected issues, quality indicators, and suggestions for improvement.",
    },
  ];

  return (
    <div className="home-page">
      <Navbar />

      <main>
        {/* Hero */}
        <section className="home-hero">
          <div className="home-hero-grid">
            <div className="home-hero-content">
              <div className="home-eyebrow">
                <Sparkles size={15} />
                AI-POWERED REQUIREMENT ANALYSIS
              </div>

              <h1>
                Better Requirements.
                <span> Better Software.</span>
              </h1>

              <p className="home-hero-description">
                Detect ambiguity, duplication, conflicts, and
                quality issues in your software requirements
                before they become costly development problems.
              </p>

              <div className="home-hero-actions">
                <Link to="/register" className="home-primary-button">
                  Start Validation
                  <ArrowRight size={18} />
                </Link>

                <a href="#features" className="home-secondary-button">
                  Explore Features
                </a>
              </div>

              <div className="home-trust-points">
                <span>
                  <CheckCircle2 size={16} />
                  Requirement quality analysis
                </span>
                <span>
                  <CheckCircle2 size={16} />
                  Actionable feedback
                </span>
              </div>
            </div>

            {/* Illustrative analysis preview */}
            <div className="home-visual">
              <div className="home-glow home-glow-one" />
              <div className="home-glow home-glow-two" />

              <div className="analysis-window">
                <div className="analysis-window-header">
                  <div className="window-dots" aria-hidden="true">
                    <span />
                    <span />
                    <span />
                  </div>

                  <span className="window-label">
                    REQUIREMENT ANALYZER
                  </span>

                  <span className="window-live">
                    <span />
                    DEMO
                  </span>
                </div>

                <div className="analysis-window-body">
                  <div className="analysis-title-row">
                    <div>
                      <span className="preview-overline">
                        SAMPLE ANALYSIS
                      </span>
                      <h2>Project Requirements</h2>
                    </div>

                    <div className="preview-shield">
                      <ShieldCheck size={23} />
                    </div>
                  </div>

                  <div className="preview-requirement preview-valid">
                    <div className="preview-requirement-icon">
                      <CheckCircle2 size={19} />
                    </div>

                    <div className="preview-requirement-copy">
                      <div className="preview-requirement-heading">
                        <strong>User Authentication</strong>
                        <span className="preview-status valid">
                          Clear
                        </span>
                      </div>

                      <p>
                        The system shall authenticate users using
                        their registered email and password.
                      </p>
                    </div>
                  </div>

                  <div className="preview-requirement preview-warning">
                    <div className="preview-requirement-icon">
                      <ShieldAlert size={19} />
                    </div>

                    <div className="preview-requirement-copy">
                      <div className="preview-requirement-heading">
                        <strong>System Performance</strong>
                        <span className="preview-status warning">
                          Review
                        </span>
                      </div>

                      <p>
                        The system should respond quickly to user
                        requests.
                      </p>

                      <div className="preview-suggestion">
                        <Sparkles size={14} />
                        Define a measurable response-time target.
                      </div>
                    </div>
                  </div>

                  <div className="preview-footer">
                    <div className="preview-footer-icon">
                      <ScanSearch size={19} />
                    </div>

                    <div className="preview-footer-copy">
                      <strong>Analysis preview</strong>
                      <span>
                        Example findings — not live analysis
                      </span>
                    </div>

                    <span className="preview-footer-arrow">
                      <ArrowRight size={17} />
                    </span>
                  </div>
                </div>
              </div>

              <div className="floating-chip floating-chip-top">
                <span className="floating-chip-icon teal">
                  <CheckCircle2 size={17} />
                </span>
                <span>
                  <strong>Clarity Check</strong>
                  <small>Requirements review</small>
                </span>
              </div>

              <div className="floating-chip floating-chip-bottom">
                <span className="floating-chip-icon violet">
                  <BrainCircuit size={18} />
                </span>
                <span>
                  <strong>AI Assistance</strong>
                  <small>Improvement suggestions</small>
                </span>
              </div>
            </div>
          </div>

          <div className="home-hero-bottom">
            <span>BUILT FOR BETTER SOFTWARE SPECIFICATIONS</span>
            <div>
              <span>Clarity</span>
              <i />
              <span>Consistency</span>
              <i />
              <span>Quality</span>
            </div>
          </div>
        </section>

        {/* Features */}
        <section id="features" className="home-features">
          <div className="home-section-heading">
            <span className="home-section-kicker">
              CORE CAPABILITIES
            </span>

            <h2>
              Find requirement issues
              <span> before development begins.</span>
            </h2>

            <p>
              Improve your Software Requirements Specification
              with structured analysis and clear feedback.
            </p>
          </div>

          <div className="home-features-grid">
            {features.map((feature) => (
              <article
                className="home-feature-card"
                key={feature.title}
              >
                <div className={`home-feature-icon ${feature.tone}`}>
                  {feature.icon}
                </div>

                <h3>{feature.title}</h3>

                <p>{feature.description}</p>

                <div className="home-feature-bottom">
                  <span>Requirement analysis</span>
                  <ArrowRight size={16} />
                </div>
              </article>
            ))}
          </div>
        </section>

        {/* How it works */}
        <section id="how-it-works" className="home-workflow">
          <div className="home-section-heading">
            <span className="home-section-kicker">
              SIMPLE WORKFLOW
            </span>

            <h2>
              From raw requirements
              <span> to clearer specifications.</span>
            </h2>

            <p>
              Follow a straightforward workflow to review and
              improve your software requirements.
            </p>
          </div>

          <div className="home-steps-grid">
            {steps.map((step, index) => (
              <article className="home-step-card" key={step.number}>
                <div className="home-step-top">
                  <span className="home-step-number">
                    {step.number}
                  </span>

                  <span className="home-step-icon">
                    {step.icon}
                  </span>
                </div>

                {index < steps.length - 1 && (
                  <div className="home-step-connector" />
                )}

                <h3>{step.title}</h3>
                <p>{step.description}</p>
              </article>
            ))}
          </div>
        </section>

        {/* Call to action */}
        <section className="home-cta">
          <div className="home-cta-content">
            <div className="home-cta-icon">
              <ShieldCheck size={27} />
            </div>

            <span className="home-section-kicker">
              START WITH CLARITY
            </span>

            <h2>
              Make your requirements
              <span> ready for development.</span>
            </h2>

            <p>
              Start reviewing your software requirements and
              discover where your specification can improve.
            </p>

            <Link to="/register" className="home-primary-button">
              Get Started
              <ArrowRight size={18} />
            </Link>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}

export default Home;
