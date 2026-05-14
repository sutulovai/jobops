// ─────────────────────────────────────────────────────────────────
// JobOps — Reference Implementation
// Single-file React artifact. Agent: read this as the source of
// truth for layout, data structures, logic, and component patterns.
// ─────────────────────────────────────────────────────────────────

import { useState } from "react";

// ─── DESIGN TOKENS ──────────────────────────────────────────────
// Use these everywhere. Do not hardcode other values.
const T = {
  bg:       "#070A12",
  surface:  "#0E1117",
  surfaceEl:"#141920",
  border:   "#1E2530",
  borderSub:"#161C26",
  amber:    "#F5A623",
  amberDim: "rgba(245,166,35,0.12)",
  text:     "#F1F3F6",
  textSec:  "#8B95A1",
  textMut:  "#4A5568",
  success:  "#34C97A",
  danger:   "#E53E3E",
  blue:     "#3B82F6",
};

const S = {
  page:    { background: T.bg, minHeight: "100vh", color: T.text, fontFamily: "'Outfit', sans-serif" },
  card:    { background: T.surface, border: `1px solid ${T.border}`, borderRadius: 12, padding: 16 },
  cardEl:  { background: T.surfaceEl, border: `1px solid ${T.border}`, borderRadius: 12, padding: 16 },
  input:   { background: T.surface, border: `1px solid ${T.border}`, borderRadius: 8, padding: "8px 12px", color: T.text, fontSize: 13, outline: "none", width: "100%" },
  mono:    { fontFamily: "'JetBrains Mono', monospace" },
  label:   { fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: T.textMut },
};

// ─── SEED DATA ───────────────────────────────────────────────────
// Agent: this is the shape of every entity. Match this in your DB schema.

const COMPANIES = [
  // Priority 1 – Munich
  { id: "scalable",     name: "Scalable Capital",  city: "Munich",  priority: 1, industry: "Fintech / Wealth",      english: "yes", relocation: "yes", visa: "yes",       status: "active_target", domain: "regulated fintech and payments infrastructure", whyFit: "Kotlin/Java, payments-adjacent, correctness-critical systems, explicit visa support.", likelyRoles: ["Senior Backend Engineer – Payments"], strategy: "Direct apply + recruiter note same day" },
  { id: "sumup",        name: "SumUp",             city: "Munich",  priority: 1, industry: "Fintech / Payments",    english: "yes", relocation: "yes", visa: "yes",       status: "applied",       domain: "acquiring, POS, and merchant payment systems",  whyFit: "Core payments. Acquiring, merchant onboarding, Kotlin/Java. Direct domain match.", likelyRoles: ["Senior Backend Engineer – Core Payments"], strategy: "Applied. Recruiter screen May 14." },
  { id: "flix",         name: "Flix",              city: "Munich",  priority: 1, industry: "Mobility",              english: "yes", relocation: "yes", visa: "yes",       status: "watchlist",     domain: "high-scale platform and payment systems",       whyFit: "Java/Kotlin backend, high-scale platform, payments team within mobility.", likelyRoles: ["Senior Backend Engineer"], strategy: "Direct apply + recruiter note" },
  { id: "celonis",      name: "Celonis",           city: "Munich",  priority: 1, industry: "B2B SaaS",             english: "yes", relocation: "yes", visa: "yes",       status: "watchlist",     domain: "platform engineering at scale",                 whyFit: "Top-tier Munich product company. JVM stack, Kafka/K8s/AWS.", likelyRoles: ["Senior Backend Engineer – Platform"], strategy: "Referral first, then direct apply" },
  // Priority 1 – Berlin
  { id: "n26",          name: "N26",               city: "Berlin",  priority: 1, industry: "Neobank / Fintech",    english: "yes", relocation: "yes", visa: "yes",       status: "active_target", domain: "banking infrastructure and payment systems",     whyFit: "EN+visa explicit. Regulated banking backend. Payment/risk/platform teams.", likelyRoles: ["Senior Backend Engineer – Banking Platform"], strategy: "Message recruiter Michael Chen before applying" },
  { id: "traderepublic",name: "Trade Republic",    city: "Berlin",  priority: 1, industry: "Fintech / Trading",   english: "yes", relocation: "yes", visa: "yes",       status: "applied",       domain: "regulated trading and financial infrastructure",  whyFit: "Regulated fintech at scale. Transaction-critical systems. Relocation explicit.", likelyRoles: ["Senior Software Engineer – Backend"], strategy: "HM Anna Weber messaged. Follow up." },
  { id: "billie",       name: "Billie",            city: "Berlin",  priority: 1, industry: "B2B Fintech / BNPL",  english: "yes", relocation: "yes", visa: "yes",       status: "applied",       domain: "B2B payments and credit risk systems",           whyFit: "B2B payments, BNPL, credit risk. EN+visa explicit. Applied 16 days ago — overdue.", likelyRoles: ["Senior Backend Engineer – B2B Payments"], strategy: "Follow up immediately — 16 days no response" },
  { id: "gyg",          name: "GetYourGuide",      city: "Berlin",  priority: 1, industry: "Travel / Marketplace", english: "yes", relocation: "yes", visa: "yes",       status: "active_target", domain: "checkout and payment systems at marketplace scale", whyFit: "Payments/checkout domain. Jan Müller (warm contact) works on checkout team.", likelyRoles: ["Senior Backend Engineer – Checkout & Payments"], strategy: "Jan Müller referral first" },
  // Priority 2
  { id: "solaris",      name: "Solaris",           city: "Berlin",  priority: 2, industry: "Banking-as-a-Service", english: "yes", relocation: "yes", visa: "yes",       status: "active_target", domain: "banking infrastructure and BaaS platform",       whyFit: "BaaS — highest domain fit possible. Explicit Blue Card support. Apply immediately.", likelyRoles: ["Senior Backend Engineer – Banking Infrastructure"], strategy: "Apply ASAP — highest fit in pipeline" },
  { id: "mambu",        name: "Mambu",             city: "Berlin",  priority: 2, industry: "Cloud Banking",        english: "yes", relocation: "yes", visa: "uncertain", status: "watchlist",     domain: "cloud-native core banking systems",              whyFit: "Core banking SaaS. Distributed, cloud-native, financial domain.", likelyRoles: ["Senior Software Engineer"], strategy: "Direct apply" },
  { id: "freenow",      name: "FREENOW",           city: "Hamburg", priority: 2, industry: "Mobility / Payments",  english: "yes", relocation: "yes", visa: "yes",       status: "watchlist",     domain: "mobility platform and payment systems",          whyFit: "Mobility + payments. Backend/platform. EN-high.", likelyRoles: ["Senior Backend Engineer"], strategy: "Direct apply + EM outreach" },
  { id: "jetbrains",    name: "JetBrains",         city: "Berlin",  priority: 2, industry: "Developer Tools",      english: "yes", relocation: "yes", visa: "yes",       status: "watchlist",     domain: "developer tooling and backend platform systems",  whyFit: "EN+relocation explicit. High engineering standards. Backend/platform.", likelyRoles: ["Senior Software Engineer"], strategy: "Referral / direct apply" },
];

const APPLICATIONS = [
  { id: "a1", companyId: "sumup",         companyName: "SumUp",          vacancyTitle: "Senior Backend Engineer – Core Payments",           status: "recruiter_screen_scheduled", dateApplied: "2026-05-02", recruiterContacted: true,  hmContacted: false, referralRequested: false, followupCount: 1, nextActionDate: "2026-05-14", notes: "Recruiter screen May 14. Prep: payment system design, Kafka patterns, distributed transactions, idempotency." },
  { id: "a2", companyId: "traderepublic", companyName: "Trade Republic",  vacancyTitle: "Senior Software Engineer – Backend",                status: "hiring_manager_contacted",   dateApplied: "2026-04-29", recruiterContacted: true,  hmContacted: true,  referralRequested: false, followupCount: 2, nextActionDate: "2026-05-12", notes: "HM Anna Weber messaged May 8. No reply in 4 days — follow up today." },
  { id: "a3", companyId: "billie",        companyName: "Billie",          vacancyTitle: "Senior Backend Engineer – B2B Payments",            status: "applied",                    dateApplied: "2026-04-26", recruiterContacted: false, hmContacted: false, referralRequested: false, followupCount: 0, nextActionDate: "2026-05-01", notes: "Applied 16 days ago. Zero response. Overdue." },
  { id: "a4", companyId: "n26",           companyName: "N26",             vacancyTitle: "Senior Backend Engineer – Banking Platform",        status: "should_apply",               recruiterContacted: false, hmContacted: false, referralRequested: false, followupCount: 0, nextActionDate: "2026-05-12", notes: "Fit 91. Contact Michael Chen before applying." },
  { id: "a5", companyId: "scalable",      companyName: "Scalable Capital", vacancyTitle: "Senior Backend Engineer – Payments",               status: "should_apply",               recruiterContacted: false, hmContacted: false, referralRequested: false, followupCount: 0, nextActionDate: "2026-05-12", notes: "Best Munich fit. Apply today." },
  { id: "a6", companyId: "solaris",       companyName: "Solaris",          vacancyTitle: "Senior Backend Engineer – Banking Infrastructure", status: "should_apply",               recruiterContacted: false, hmContacted: false, referralRequested: false, followupCount: 0, nextActionDate: "2026-05-12", notes: "Fit 94. Explicit Blue Card. Apply before anyone else." },
];

const CONTACTS = [
  { id: "ct1", name: "Sarah Fischer", companyId: "scalable",      companyName: "Scalable Capital", title: "Technical Recruiter",         type: "recruiter",           linkedinUrl: "#", relationshipStrength: 2, lastContacted: "2026-05-08", status: "contacted", notes: "Responsive. Use for Scalable application." },
  { id: "ct2", name: "Michael Chen",  companyId: "n26",           companyName: "N26",              title: "Engineering Recruiter",       type: "recruiter",           linkedinUrl: "#", relationshipStrength: 1,                              status: "new",       notes: "Message before applying. Mention payments background." },
  { id: "ct3", name: "Jan Müller",    companyId: "gyg",           companyName: "GetYourGuide",     title: "Senior Backend Engineer",     type: "referral_contact",    linkedinUrl: "#", relationshipStrength: 3, lastContacted: "2026-04-20", status: "warm",      notes: "Met at Munich Java meetup. Checkout team. Can refer." },
  { id: "ct4", name: "Anna Weber",    companyId: "traderepublic", companyName: "Trade Republic",   title: "Head of Backend Engineering", type: "head_of_engineering", linkedinUrl: "#", relationshipStrength: 2, lastContacted: "2026-05-08", status: "contacted", notes: "Messaged 4 days ago. Follow up today." },
  { id: "ct6", name: "Tobias Krause", companyId: "solaris",       companyName: "Solaris",          title: "Engineering Manager",         type: "engineering_manager", linkedinUrl: "#", relationshipStrength: 1,                              status: "new",       notes: "Message after applying to Solaris." },
];

const NEXT_ACTIONS = [
  { id: "na1", type: "apply",            priority: "critical", isOverdue: false, companyId: "solaris",       companyName: "Solaris",          reason: "Fit 94/100 — Banking-as-a-Service is the highest domain match in your pipeline. Explicit Blue Card support. Apply today.", vacancyTitle: "Senior Backend Engineer – Banking Infrastructure", suggestedMessage: `Hi Tobias,\n\nI'm applying for the Senior Backend Engineer – Banking Infrastructure role today. Your BaaS platform is a direct match for my 8+ years in payments infrastructure and regulated distributed systems (Java/Kotlin, Spring Boot, Kafka, AWS).\n\nRelocating to Germany — EU Blue Card ready, 2–3 months notice.\n\nBest,\nArtem` },
  { id: "na2", type: "apply",            priority: "critical", isOverdue: false, companyId: "scalable",      companyName: "Scalable Capital", reason: "Fit 92/100 — Munich payments fintech, exact stack match, salary above target. No reason to wait.", vacancyTitle: "Senior Backend Engineer – Payments", suggestedMessage: `Hi Sarah,\n\nI'm applying for the Senior Backend Engineer – Payments position. My 8+ years in Java/Kotlin payment systems — provider integrations, transaction flows, Kafka event streaming, safe migrations — aligns directly with Scalable Capital's regulated fintech infrastructure.\n\nRelocating to Munich (EU Blue Card, 2–3 months).\n\nBest,\nArtem` },
  { id: "na3", type: "message_recruiter",priority: "high",     isOverdue: true,  companyId: "billie",        companyName: "Billie",           reason: "Applied 16 days ago — zero response. Send follow-up now before the application goes cold.", suggestedMessage: `Hi,\n\nI applied for the Senior Backend Engineer – B2B Payments role at Billie about two weeks ago and wanted to follow up briefly. My background in B2B payments infrastructure and distributed financial systems is a direct match.\n\nIs the role still active?\n\nBest,\nArtem` },
  { id: "na4", type: "message_recruiter",priority: "high",     isOverdue: false, companyId: "n26",           companyName: "N26",              reason: "Fit 91/100 — message Michael Chen before applying. Warm outreach increases response rate significantly.", suggestedMessage: `Hi Michael,\n\nI came across the Senior Backend Engineer – Banking Platform role at N26. It's a direct match for my 8+ years in Java/Kotlin payments infrastructure and regulated distributed systems.\n\nRelocating to Germany, EU Blue Card ready, Munich/Berlin.\n\nWould you be open to a brief call?\n\nBest,\nArtem` },
  { id: "na5", type: "send_followup",   priority: "high",     isOverdue: false, companyId: "traderepublic", companyName: "Trade Republic",   reason: "HM Anna Weber messaged 4 days ago — no reply. One polite follow-up today.", suggestedMessage: `Hi Anna,\n\nBrief follow-up to my message from a few days ago. Completely understand if timing isn't right — just wanted to reiterate my interest. Happy to connect at your convenience.\n\nBest,\nArtem` },
  { id: "na6", type: "request_referral",priority: "medium",   isOverdue: false, companyId: "gyg",           companyName: "GetYourGuide",     reason: "Jan Müller is a warm contact on the checkout team. A referral here is realistic — activate it.", suggestedMessage: `Hi Jan,\n\nGreat connecting at the Munich Java meetup! I noticed the Senior Backend Engineer – Checkout & Payments opening at GetYourGuide — direct match for my background in distributed payments systems.\n\nWould you be comfortable sharing a referral or a brief word to your team?\n\nBest,\nArtem` },
  { id: "na7", type: "prepare_interview",priority:"medium",   isOverdue: false, companyId: "sumup",         companyName: "SumUp",            reason: "Recruiter screen scheduled May 14. Prepare: payment system design, Kafka patterns, idempotency, distributed transactions." },
];

const WEEK_PROGRESS = [
  { label: "Applications",  done: 3,  target: 15 },
  { label: "Recruiter DMs", done: 1,  target: 12 },
  { label: "Manager DMs",   done: 1,  target: 8  },
  { label: "Referrals",     done: 0,  target: 10 },
  { label: "JDs Analyzed",  done: 3,  target: 20 },
];

// ─── MESSAGE TEMPLATES ───────────────────────────────────────────
// Agent: implement these as server-side utils. Same logic here for reference.
// ctx = { companyName, role, domain, city, contactName?, daysSinceApplied? }

function buildMessage(type, ctx) {
  const hi = ctx.contactName ? `Hi ${ctx.contactName},` : "Hi,";
  const { companyName, role, domain, city } = ctx;
  const days = ctx.daysSinceApplied ? ` about ${ctx.daysSinceApplied} days ago` : " recently";

  if (type === "recruiter") return `${hi}\n\nI just applied for the ${role} position at ${companyName}. I'm a Senior Backend Engineer with 8+ years in Java/Kotlin backend systems, including ${domain}.\n\nThe role looks highly relevant — your work on ${domain} is exactly the kind of environment I've been building in. I'm open to relocating to ${city}, available in 2–3 months, and comfortable working fully in English.\n\nHappy to share more context if useful.\n\nBest,\nArtem`;
  if (type === "hm")        return `${hi}\n\nI'm reaching out because your team's work at ${companyName} on ${domain} looks closely aligned with my background.\n\nI've spent 8+ years building Java/Kotlin backend systems — payments, regulated fintech, distributed microservices, observability, safe migrations, production/on-call ownership. This is the kind of system that breaks badly if you cut corners.\n\nI've recently applied for the ${role} position. If the team is open to relocation candidates, I'd be glad to connect.\n\nBest,\nArtem`;
  if (type === "referral")  return `${hi}\n\nI saw that you're on the engineering team at ${companyName}. I'm applying for ${role} and wanted to ask whether you think my background is relevant enough for a referral.\n\n8+ years in Java/Kotlin — payments, regulated fintech, distributed microservices, safe migrations, production ownership. The ${domain} work at ${companyName} is a direct fit.\n\nIf helpful I can share a short summary. Either way, no pressure.\n\nThanks,\nArtem`;
  if (type === "followup")  return `${hi}\n\nJust a short follow-up on my application for ${role} at ${companyName}${days} — wanted to make sure it reached the right person.\n\nThe role is still a strong fit: ${domain}. I'm relocating to Germany, EU Blue Card ready, 2–3 months notice.\n\nHappy to connect at your convenience.\n\nBest,\nArtem`;
  if (type === "cold")      return `${hi}\n\nI came across ${companyName}'s engineering work and ${role} looks like a strong match — your team's focus on ${domain} is exactly where my 8 years of Java/Kotlin experience lands.\n\nRelocating to Germany (EU Blue Card, 2–3 months). Would you be open to a quick chat?\n\nBest,\nArtem`;
  if (type === "thankyou")  return `${hi}\n\nThank you for the time today. I came away more excited about the role, not less.\n\nThe ${domain} challenges your team is working through are exactly what I want to be doing. Happy to provide any further context or references.\n\nBest,\nArtem`;
  return "";
}

// ─── SHARED COMPONENTS ───────────────────────────────────────────

// CopyButton: copy any text to clipboard with amber→green feedback
function CopyButton({ text, label = "Copy" }) {
  const [copied, setCopied] = useState(false);
  const click = () => {
    navigator.clipboard.writeText(text).catch(() => {});
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };
  return (
    <button onClick={click} style={{ display: "flex", alignItems: "center", gap: 6, padding: "7px 14px", borderRadius: 8, fontSize: 12, fontWeight: 700, cursor: "pointer", border: "none", background: copied ? "rgba(52,201,122,0.15)" : T.amber, color: copied ? T.success : T.bg, transition: "all 0.15s", outline: "none" }}>
      {copied ? "✓ Copied!" : `📋 ${label}`}
    </button>
  );
}

// MessageBox: monospace pre-formatted message with copy button below
function MessageBox({ text, copyLabel = "Copy Message" }) {
  return (
    <div>
      <pre style={{ ...S.mono, background: "rgba(0,0,0,0.4)", border: `1px solid ${T.border}`, borderRadius: 10, padding: "14px 16px", fontSize: 13, color: "#C8D0DC", whiteSpace: "pre-wrap", lineHeight: 1.7, margin: 0 }}>
        {text}
      </pre>
      <div style={{ marginTop: 10 }}>
        <CopyButton text={text} label={copyLabel} />
      </div>
    </div>
  );
}

function Badge({ children, color = T.textMut }) {
  return (
    <span style={{ display: "inline-flex", alignItems: "center", padding: "3px 9px", borderRadius: 20, fontSize: 11, fontWeight: 700, background: `${color}18`, color, border: `1px solid ${color}33` }}>
      {children}
    </span>
  );
}

function LikelihoodDot({ v }) {
  const color = v === "yes" ? T.success : v === "no" ? T.danger : T.textMut;
  return <span style={{ ...S.mono, color, fontWeight: 700, fontSize: 13 }}>{v === "yes" ? "✓" : v === "no" ? "✗" : "?"}</span>;
}

// ─── TODAY PAGE ──────────────────────────────────────────────────
// The most important page. Shows what to do right now.
// Actions are sorted: OVERDUE first (red), TODAY second (amber), UPCOMING last.
// Each card shows the full pre-written message — no expand needed.

function TodayPage() {
  const [actions, setActions] = useState(NEXT_ACTIONS);
  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
  const dateStr = new Date().toLocaleDateString("en-GB", { weekday: "long", day: "numeric", month: "long" });

  const overdue  = actions.filter(a => a.isOverdue);
  const today    = actions.filter(a => !a.isOverdue && (a.priority === "critical" || a.priority === "high"));
  const upcoming = actions.filter(a => !a.isOverdue && a.priority === "medium");

  const markDone = (id) => setActions(prev => prev.filter(a => a.id !== id));
  const skip     = (id) => setActions(prev => prev.filter(a => a.id !== id));

  const ACTION_LABELS = {
    apply: "APPLY NOW", message_recruiter: "MESSAGE RECRUITER", message_hm: "MESSAGE HM",
    request_referral: "REQUEST REFERRAL", send_followup: "SEND FOLLOW-UP",
    prepare_interview: "PREP INTERVIEW", send_thankyou: "SEND THANK-YOU",
  };

  function ActionCard({ action }) {
    const isOverdue   = action.isOverdue;
    const dot         = isOverdue ? T.danger : action.priority === "critical" ? T.amber : T.blue;
    const borderColor = isOverdue ? `${T.danger}44` : action.priority === "critical" ? `${T.amber}33` : T.border;
    const bgColor     = isOverdue ? "rgba(229,62,62,0.04)" : T.surface;

    return (
      <div style={{ ...S.card, border: `1px solid ${borderColor}`, background: bgColor, marginBottom: 12 }}>
        {/* Header */}
        <div style={{ display: "flex", alignItems: "flex-start", gap: 10, marginBottom: 12 }}>
          <div style={{ width: 8, height: 8, borderRadius: "50%", background: dot, flexShrink: 0, marginTop: 5 }} />
          <div style={{ flex: 1 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap", marginBottom: 4 }}>
              <span style={{ ...S.label }}>{ACTION_LABELS[action.type] || action.type}</span>
              <span style={{ color: T.amber, fontWeight: 600, fontSize: 14 }}>· {action.companyName}</span>
              {isOverdue && <Badge color={T.danger}>OVERDUE</Badge>}
            </div>
            <p style={{ fontSize: 13, color: T.textSec, margin: 0, lineHeight: 1.55 }}>{action.reason}</p>
          </div>
        </div>

        {/* Message — always visible, never hidden */}
        {action.suggestedMessage && (
          <div style={{ marginBottom: 12 }}>
            <MessageBox text={action.suggestedMessage} copyLabel="Copy Message" />
          </div>
        )}

        {/* Buttons */}
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <button onClick={() => markDone(action.id)} style={{ padding: "7px 14px", borderRadius: 8, fontSize: 12, fontWeight: 600, background: T.surfaceEl, color: T.textSec, border: `1px solid ${T.border}`, cursor: "pointer" }}>
            ✓ Mark Done
          </button>
          <button onClick={() => skip(action.id)} style={{ padding: "7px 12px", fontSize: 12, color: T.textMut, background: "transparent", border: "none", cursor: "pointer" }}>
            Skip
          </button>
        </div>
      </div>
    );
  }

  function SectionHeader({ label, color, count }) {
    return (
      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12 }}>
        <div style={{ width: 6, height: 6, borderRadius: "50%", background: color }} />
        <span style={{ ...S.label, color }}>{label}</span>
        <span style={{ fontSize: 11, color: T.textMut }}>· {count}</span>
        <div style={{ flex: 1, height: 1, background: T.border }} />
      </div>
    );
  }

  const activeApps = APPLICATIONS.filter(a => !["rejected","ghosted","withdrawn","skipped"].includes(a.status));

  return (
    <div style={{ padding: 24, maxWidth: 1100 }}>
      {/* Page header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 20 }}>
        <div>
          <h1 style={{ fontSize: 20, fontWeight: 700, color: T.text, margin: 0 }}>{greeting}, Artem</h1>
          <p style={{ fontSize: 13, color: T.textSec, marginTop: 3 }}>{dateStr}</p>
        </div>
        {actions.length > 0 && (
          <div style={{ ...S.mono, background: T.amberDim, border: `1px solid ${T.amber}44`, borderRadius: 20, padding: "6px 14px", fontSize: 13, fontWeight: 700, color: T.amber }}>
            {actions.length} actions
          </div>
        )}
      </div>

      {/* Week progress */}
      <div style={{ ...S.card, marginBottom: 20 }}>
        <div style={{ ...S.label, marginBottom: 12 }}>Week 1 Progress</div>
        <div style={{ display: "flex", gap: 24, flexWrap: "wrap" }}>
          {WEEK_PROGRESS.map(({ label, done, target }) => (
            <div key={label} style={{ flex: 1, minWidth: 90 }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 5 }}>
                <span style={{ fontSize: 11, color: T.textSec }}>{label}</span>
                <span style={{ ...S.mono, fontSize: 11, fontWeight: 700, color: done >= target ? T.success : done > 0 ? T.amber : T.textMut }}>{done}/{target}</span>
              </div>
              <div style={{ height: 3, background: T.border, borderRadius: 2, overflow: "hidden" }}>
                <div style={{ height: "100%", width: `${Math.min(100, (done / target) * 100)}%`, background: done >= target ? T.success : T.amber, borderRadius: 2 }} />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Main two-column layout */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 300px", gap: 20 }}>

        {/* Left: action feed */}
        <div>
          {overdue.length > 0 && (
            <div style={{ marginBottom: 24 }}>
              <SectionHeader label="Overdue" color={T.danger} count={overdue.length} />
              {overdue.map(a => <ActionCard key={a.id} action={a} />)}
            </div>
          )}
          {today.length > 0 && (
            <div style={{ marginBottom: 24 }}>
              <SectionHeader label="Today" color={T.amber} count={today.length} />
              {today.map(a => <ActionCard key={a.id} action={a} />)}
            </div>
          )}
          {upcoming.length > 0 && (
            <div>
              <SectionHeader label="Upcoming" color={T.textMut} count={upcoming.length} />
              {upcoming.map(a => <ActionCard key={a.id} action={a} />)}
            </div>
          )}
          {actions.length === 0 && (
            <div style={{ textAlign: "center", padding: "60px 0" }}>
              <div style={{ fontSize: 36, marginBottom: 12 }}>✓</div>
              <div style={{ color: T.textSec, fontWeight: 600 }}>All caught up</div>
              <div style={{ color: T.textMut, fontSize: 13, marginTop: 4 }}>No pending actions.</div>
            </div>
          )}
        </div>

        {/* Right: sidebar */}
        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          {/* Stat cards 2x2 */}
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
            {[
              { label: "Today's actions", value: actions.length,    amber: true },
              { label: "Overdue",         value: overdue.length,    red: overdue.length > 0 },
              { label: "Active apps",     value: activeApps.length                          },
              { label: "Response rate",   value: "33%"                                      },
            ].map(s => (
              <div key={s.label} style={{ ...S.card, borderColor: s.red ? `${T.danger}44` : s.amber && s.value > 0 ? `${T.amber}33` : T.border, background: s.red && s.value > 0 ? "rgba(229,62,62,0.06)" : T.surface }}>
                <div style={{ ...S.mono, fontSize: 22, fontWeight: 700, color: s.red && s.value > 0 ? T.danger : s.amber && s.value > 0 ? T.amber : T.text }}>{s.value}</div>
                <div style={{ fontSize: 11, color: T.textMut, marginTop: 3 }}>{s.label}</div>
              </div>
            ))}
          </div>

          {/* Apply next — top fit scores */}
          <div style={{ ...S.card }}>
            <div style={{ ...S.label, marginBottom: 12 }}>Apply Next</div>
            {[{ name: "Solaris", score: 94, city: "Berlin" }, { name: "Scalable Capital", score: 92, city: "Munich" }, { name: "N26", score: 91, city: "Berlin" }, { name: "GetYourGuide", score: 86, city: "Berlin" }].map(c => (
              <div key={c.name} style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: T.text }}>{c.name}</div>
                  <div style={{ fontSize: 11, color: T.textMut }}>{c.city}</div>
                </div>
                <div>
                  <div style={{ ...S.mono, fontSize: 12, fontWeight: 700, color: c.score >= 90 ? T.success : T.amber, textAlign: "right" }}>{c.score}</div>
                  <div style={{ width: 44, height: 3, background: T.border, borderRadius: 2, marginTop: 3 }}>
                    <div style={{ height: "100%", width: `${c.score}%`, background: c.score >= 90 ? T.success : T.amber, borderRadius: 2 }} />
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Active pipeline */}
          <div style={{ ...S.card }}>
            <div style={{ ...S.label, marginBottom: 12 }}>Active Pipeline</div>
            {activeApps.map(a => {
              const c = { should_apply: T.amber, applied: T.blue, hiring_manager_contacted: "#A855F7", recruiter_screen_scheduled: "#06B6D4" }[a.status] || T.textMut;
              return (
                <div key={a.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
                  <span style={{ fontSize: 13, color: T.text, fontWeight: 500 }}>{a.companyName}</span>
                  <span style={{ fontSize: 11, fontWeight: 600, color: c }}>{a.status.replace(/_/g, " ")}</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── COMPANIES PAGE ──────────────────────────────────────────────
// Card grid grouped by priority. Two actions per card:
// - Click card body → detail/message modal
// - "📋 Messages" button → message panel shortcut

const MSG_TABS = [
  { key: "recruiter", label: "Recruiter"    },
  { key: "hm",        label: "Hiring Mgr"  },
  { key: "referral",  label: "Referral Ask" },
  { key: "followup",  label: "Follow-Up"   },
  { key: "cold",      label: "Cold"         },
];

function MessageModal({ company, onClose }) {
  const [tab, setTab] = useState("recruiter");
  const role = company.likelyRoles?.[0] || "Senior Backend Engineer";
  const msg  = buildMessage(tab, { companyName: company.name, role, domain: company.domain, city: company.city });

  return (
    <div onClick={onClose} style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.75)", display: "flex", alignItems: "flex-end", justifyContent: "center", zIndex: 100, padding: 16 }}>
      <div onClick={e => e.stopPropagation()} style={{ ...S.cardEl, width: "100%", maxWidth: 640, maxHeight: "88vh", overflowY: "auto", borderRadius: 16, paddingBottom: 24 }}>
        {/* Header */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 16 }}>
          <div>
            <h2 style={{ fontSize: 16, fontWeight: 700, color: T.text, margin: 0 }}>{company.name}</h2>
            <p style={{ fontSize: 12, color: T.textSec, marginTop: 3 }}>{company.city} · {role}</p>
          </div>
          <button onClick={onClose} style={{ background: "none", border: "none", color: T.textMut, fontSize: 20, cursor: "pointer", lineHeight: 1 }}>✕</button>
        </div>

        {/* Why it fits */}
        <div style={{ ...S.card, marginBottom: 14 }}>
          <div style={{ ...S.label, marginBottom: 6 }}>Why it fits</div>
          <p style={{ fontSize: 13, color: T.textSec, lineHeight: 1.6, margin: 0 }}>{company.whyFit}</p>
        </div>

        {/* Likelihood row */}
        <div style={{ display: "flex", gap: 20, marginBottom: 16 }}>
          {[["English", company.english], ["Relocation", company.relocation], ["Visa", company.visa]].map(([l, v]) => (
            <div key={l} style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 12, color: T.textSec }}>
              <LikelihoodDot v={v} /><span>{l}</span>
            </div>
          ))}
        </div>

        {/* Message tabs */}
        <div style={{ ...S.label, marginBottom: 10 }}>Ready-to-Send Messages</div>
        <div style={{ display: "flex", gap: 6, marginBottom: 14, flexWrap: "wrap" }}>
          {MSG_TABS.map(t => (
            <button key={t.key} onClick={() => setTab(t.key)} style={{ padding: "6px 14px", borderRadius: 20, fontSize: 12, fontWeight: 600, cursor: "pointer", background: tab === t.key ? T.amber : "transparent", color: tab === t.key ? T.bg : T.textSec, border: `1px solid ${tab === t.key ? T.amber : T.border}` }}>
              {t.label}
            </button>
          ))}
        </div>
        <MessageBox text={msg} copyLabel={`Copy ${MSG_TABS.find(t => t.key === tab)?.label}`} />
        <div style={{ marginTop: 12 }}>
          <a href={`https://${company.website}/careers`} target="_blank" rel="noreferrer" style={{ fontSize: 12, color: T.textSec, textDecoration: "none", border: `1px solid ${T.border}`, padding: "6px 14px", borderRadius: 8 }}>
            Careers ↗
          </a>
        </div>
      </div>
    </div>
  );
}

function CompanyCard({ company, onMsg, onDetail }) {
  const pColor = company.priority === 1 ? T.amber : company.priority === 2 ? T.blue : T.textMut;
  const statusColor = { watchlist: T.textMut, active_target: T.amber, applied: T.blue }[company.status] || T.textMut;

  return (
    <div style={{ ...S.card }}>
      {/* Clickable area → detail modal */}
      <div onClick={() => onDetail(company)} style={{ cursor: "pointer" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 8 }}>
          <div>
            <div style={{ fontWeight: 700, fontSize: 14, color: T.text }}>{company.name}</div>
            <div style={{ fontSize: 12, color: T.textMut, marginTop: 2 }}>{company.city} · {company.industry}</div>
          </div>
          <Badge color={pColor}>P{company.priority}</Badge>
        </div>
        <p style={{ fontSize: 12, color: T.textSec, lineHeight: 1.55, marginBottom: 10, overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
          {company.whyFit}
        </p>
        <div style={{ display: "flex", alignItems: "center", gap: 14, fontSize: 12 }}>
          {[["EN", company.english], ["Reloc", company.relocation], ["Visa", company.visa]].map(([l, v]) => (
            <span key={l} style={{ display: "flex", alignItems: "center", gap: 4, color: T.textMut }}>
              <LikelihoodDot v={v} /> {l}
            </span>
          ))}
          <span style={{ marginLeft: "auto", fontSize: 11, fontWeight: 600, color: statusColor }}>{company.status.replace(/_/g, " ")}</span>
        </div>
      </div>

      {/* Action bar — always visible on card */}
      <div style={{ marginTop: 12, paddingTop: 10, borderTop: `1px solid ${T.border}`, display: "flex", gap: 8 }}>
        <button onClick={() => onMsg(company)} style={{ flex: 1, padding: "7px 10px", borderRadius: 8, fontSize: 12, fontWeight: 600, background: T.amberDim, color: T.amber, border: `1px solid ${T.amber}33`, cursor: "pointer" }}>
          📋 Messages
        </button>
        <a href="#" style={{ padding: "7px 12px", borderRadius: 8, fontSize: 12, color: T.textSec, border: `1px solid ${T.border}`, textDecoration: "none" }}>
          Apply ↗
        </a>
      </div>
    </div>
  );
}

function CompaniesPage() {
  const [search, setSearch]             = useState("");
  const [filterPriority, setFilterPriority] = useState("all");
  const [msgCompany, setMsgCompany]     = useState(null);
  const [detailCompany, setDetailCompany] = useState(null);

  const filtered = COMPANIES.filter(c => {
    if (search && !c.name.toLowerCase().includes(search.toLowerCase())) return false;
    if (filterPriority !== "all" && c.priority !== parseInt(filterPriority)) return false;
    return true;
  });

  function PriorityGroup({ label, color, companies }) {
    if (!companies.length) return null;
    return (
      <div style={{ marginBottom: 28 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 14 }}>
          <span style={{ ...S.label, color }}>{label}</span>
          <span style={{ fontSize: 11, color: T.textMut }}>{companies.length} companies</span>
          <div style={{ flex: 1, height: 1, background: T.border }} />
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 12 }}>
          {companies.map(c => <CompanyCard key={c.id} company={c} onMsg={setMsgCompany} onDetail={setDetailCompany} />)}
        </div>
      </div>
    );
  }

  return (
    <div style={{ padding: 24, maxWidth: 1100 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700, color: T.text, margin: "0 0 4px" }}>Companies</h1>
      <p style={{ fontSize: 13, color: T.textSec, marginBottom: 20 }}>{COMPANIES.length} target companies</p>
      <div style={{ display: "flex", gap: 10, marginBottom: 20, flexWrap: "wrap" }}>
        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search…" style={{ ...S.input, width: 200 }} />
        {["all", "1", "2"].map(p => (
          <button key={p} onClick={() => setFilterPriority(p)} style={{ padding: "7px 16px", borderRadius: 20, fontSize: 12, fontWeight: 600, cursor: "pointer", background: filterPriority === p ? T.amber : "transparent", color: filterPriority === p ? T.bg : T.textSec, border: `1px solid ${filterPriority === p ? T.amber : T.border}` }}>
            {p === "all" ? "All" : `P${p}`}
          </button>
        ))}
      </div>
      <PriorityGroup label="Priority 1 — Best fit"      color={T.amber} companies={filtered.filter(c => c.priority === 1)} />
      <PriorityGroup label="Priority 2 — Strong backup" color={T.blue}  companies={filtered.filter(c => c.priority === 2)} />
      {msgCompany    && <MessageModal company={msgCompany}    onClose={() => setMsgCompany(null)} />}
      {detailCompany && <MessageModal company={detailCompany} onClose={() => setDetailCompany(null)} />}
    </div>
  );
}

// ─── JD ANALYZER PAGE ────────────────────────────────────────────
// Calls Claude API server-side in real app. Here it runs client-side for demo.
// System prompt encodes full candidate profile. Returns structured JSON.
// Agent: move the fetch() call to a server action / API route.

const JD_SYSTEM_PROMPT = `You are a job-fit analyst for Artem Sutulov. Analyze the job description and return ONLY a JSON object (no markdown, no preamble).

Artem's profile:
- Role: Senior Backend Engineer, 8+ years
- Stack: Java, Kotlin, Spring Boot, Kafka, PostgreSQL, Redis, MongoDB, Elasticsearch, Docker, Kubernetes, AWS, GCP
- Domain: Online payments, fintech, banking infrastructure, distributed microservices, transaction processing, safe migrations, feature flags, staged rollouts, on-call production ownership
- Relocation: EU Blue Card ready, 2–3 months notice
- Language: English (professional), German (basic)
- Salary: €80k–95k standard; €95k–110k strong fintech/payments fit
- Primary: Munich. Equal: Berlin. Backup: Hamburg, Frankfurt.

Return this exact JSON:
{
  "recommendation": "APPLY" | "MAYBE" | "SKIP",
  "fitScore": <0-100>,
  "confidence": <0-100>,
  "breakdown": { "stack": <0-100>, "domain": <0-100>, "location": <0-100>, "language": <0-100>, "seniority": <0-100>, "companyType": <0-100> },
  "reasoning": "<2-3 sentences>",
  "matchSignals": ["signal 1", "signal 2"],
  "redFlags": ["flag 1"],
  "positioning": "<1-2 sentences>",
  "salaryStrategy": "<1-2 sentences>",
  "outreachMessage": "<full personalized ready-to-send message>",
  "emailSubject": "<subject line>"
}`;

const EXAMPLE_JD = `Senior Backend Engineer – Payments Platform
Scalable Capital, Munich (hybrid)

We are building next-generation wealth management infrastructure and are looking for a Senior Backend Engineer to join our Payments Platform team.

Your role:
- Design and implement scalable payment processing microservices
- Work with Kafka event streams and PostgreSQL for transaction storage
- Build reliable services in Kotlin/Spring Boot
- Drive safe database migrations and staged feature rollouts
- Participate in on-call rotation and incident response

Requirements:
- 5+ years backend engineering experience
- Kotlin or Java proficiency
- Spring Boot and microservices experience
- Distributed systems and event-driven patterns
- Payments or fintech background is a strong plus
- Working language is English

Salary: €90,000 – €115,000 + equity
Location: Munich (hybrid, 2 days/week in office)
Relocation support available.`;

function JDAnalyzerPage() {
  const [jd, setJd]                 = useState("");
  const [company, setCompany]       = useState("");
  const [role, setRole]             = useState("");
  const [location, setLocation]     = useState("");
  const [salary, setSalary]         = useState("");
  const [langReq, setLangReq]       = useState("");
  const [relocation, setRelocation] = useState("");
  const [result, setResult]         = useState(null);
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState(null);

  const analyze = async () => {
    if (!jd.trim()) return;
    setLoading(true); setError(null); setResult(null);
    const context = [company && `Company: ${company}`, role && `Role: ${role}`, location && `Location: ${location}`, salary && `Salary: ${salary}`, langReq && `Language: ${langReq}`, relocation && `Relocation: ${relocation}`].filter(Boolean).join("\n");
    const userContent = context ? `Context:\n${context}\n\nJob Description:\n${jd}` : jd;
    try {
      const res = await fetch("https://api.anthropic.com/v1/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ model: "claude-sonnet-4-20250514", max_tokens: 1000, system: JD_SYSTEM_PROMPT, messages: [{ role: "user", content: userContent }] }),
      });
      if (!res.ok) throw new Error(`API ${res.status}`);
      const data = await res.json();
      const text = data.content?.find(b => b.type === "text")?.text || "";
      setResult(JSON.parse(text.replace(/```json|```/g, "").trim()));
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  const loadExample = () => { setJd(EXAMPLE_JD); setCompany("Scalable Capital"); setRole("Senior Backend Engineer – Payments"); setLocation("Munich (hybrid)"); setSalary("€90k–115k"); setLangReq("English required"); setRelocation("Relocation support available"); };

  const barColor = v => v >= 80 ? T.success : v >= 60 ? T.amber : T.danger;
  const recColors = { APPLY: T.success, MAYBE: T.amber, SKIP: T.danger };

  return (
    <div style={{ padding: 24, maxWidth: 1100 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700, color: T.text, margin: "0 0 4px" }}>Analyze Job Description</h1>
      <p style={{ fontSize: 13, color: T.textSec, marginBottom: 20 }}>Paste a JD — Claude evaluates fit and writes the outreach message.</p>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24, alignItems: "start" }}>
        {/* Left: Input form */}
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
            <span style={{ ...S.label }}>Job Description *</span>
            <button onClick={loadExample} style={{ fontSize: 11, color: T.textSec, background: "none", border: `1px solid ${T.border}`, borderRadius: 6, padding: "4px 10px", cursor: "pointer" }}>Load example</button>
          </div>
          <textarea value={jd} onChange={e => setJd(e.target.value)} placeholder="Paste the full job description here…" style={{ ...S.input, ...S.mono, height: 200, resize: "none", lineHeight: 1.6, fontSize: 12, marginBottom: 12 }} />
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, marginBottom: 12 }}>
            {[["Company name", company, setCompany, "Scalable Capital"], ["Role title", role, setRole, "Senior Backend Engineer"], ["Location", location, setLocation, "Munich / Remote"], ["Visible salary", salary, setSalary, "€80k–100k"], ["Language req.", langReq, setLangReq, "English required"], ["Relocation wording", relocation, setRelocation, "Relocation support provided"]].map(([l, v, s, ph]) => (
              <div key={l}>
                <div style={{ fontSize: 11, color: T.textMut, marginBottom: 4 }}>{l}</div>
                <input value={v} onChange={e => s(e.target.value)} placeholder={ph} style={{ ...S.input, fontSize: 12 }} />
              </div>
            ))}
          </div>
          <button onClick={analyze} disabled={!jd.trim() || loading} style={{ width: "100%", padding: "13px 0", borderRadius: 10, fontSize: 14, fontWeight: 700, background: !jd.trim() || loading ? T.border : T.amber, color: !jd.trim() || loading ? T.textMut : T.bg, border: "none", cursor: !jd.trim() || loading ? "default" : "pointer", display: "flex", alignItems: "center", justifyContent: "center", gap: 8 }}>
            {loading ? "Analyzing with Claude…" : "🔍 Analyze Job Fit"}
          </button>
          {error && <div style={{ marginTop: 10, padding: 12, background: "rgba(229,62,62,0.08)", border: `1px solid ${T.danger}44`, borderRadius: 8, fontSize: 13, color: T.danger }}>{error}</div>}
        </div>

        {/* Right: Results */}
        <div style={{ overflowY: "auto", maxHeight: "calc(100vh - 140px)" }}>
          {!result && !loading && (
            <div style={{ ...S.card, display: "flex", flexDirection: "column", alignItems: "center", padding: "48px 24px", textAlign: "center" }}>
              <div style={{ fontSize: 36, marginBottom: 12 }}>🔍</div>
              <div style={{ fontWeight: 600, fontSize: 14, color: T.textSec }}>Paste a JD and analyze</div>
              <div style={{ fontSize: 12, marginTop: 6, color: T.textMut, maxWidth: 260, lineHeight: 1.6 }}>Claude evaluates fit, scores 6 dimensions, flags risks, and writes the outreach message</div>
            </div>
          )}
          {loading && (
            <div style={{ ...S.card, display: "flex", flexDirection: "column", alignItems: "center", padding: "60px 0" }}>
              <div style={{ width: 32, height: 32, borderRadius: "50%", border: `2px solid ${T.amber}`, borderTopColor: "transparent", animation: "spin 0.8s linear infinite", marginBottom: 16 }} />
              <div style={{ fontSize: 13, color: T.textMut }}>Analyzing against your profile…</div>
            </div>
          )}
          {result && (
            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              {/* Overall verdict */}
              <div style={{ ...S.card }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                  <Badge color={recColors[result.recommendation]}>{result.recommendation}</Badge>
                  <span style={{ ...S.mono, fontSize: 11, color: T.textMut }}>confidence {result.confidence}%</span>
                </div>
                <div style={{ display: "flex", alignItems: "baseline", gap: 6, marginBottom: 10 }}>
                  <span style={{ ...S.mono, fontSize: 44, fontWeight: 700, color: T.text }}>{result.fitScore}</span>
                  <span style={{ color: T.textMut, fontSize: 14 }}>/100</span>
                </div>
                <div style={{ height: 8, background: T.border, borderRadius: 4, overflow: "hidden", marginBottom: 12 }}>
                  <div style={{ height: "100%", width: `${result.fitScore}%`, background: barColor(result.fitScore), borderRadius: 4 }} />
                </div>
                <p style={{ fontSize: 13, color: T.textSec, lineHeight: 1.65, margin: 0 }}>{result.reasoning}</p>
              </div>

              {/* Dimension breakdown */}
              <div style={{ ...S.card }}>
                <div style={{ ...S.label, marginBottom: 12 }}>Fit Breakdown</div>
                {Object.entries(result.breakdown).map(([k, v]) => (
                  <div key={k} style={{ marginBottom: 10 }}>
                    <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
                      <span style={{ fontSize: 12, color: T.textSec }}>{k.charAt(0).toUpperCase() + k.slice(1)}</span>
                      <span style={{ ...S.mono, fontSize: 12, fontWeight: 700, color: barColor(v) }}>{v}/100</span>
                    </div>
                    <div style={{ height: 5, background: T.border, borderRadius: 3, overflow: "hidden" }}>
                      <div style={{ height: "100%", width: `${v}%`, background: barColor(v), borderRadius: 3 }} />
                    </div>
                  </div>
                ))}
              </div>

              {/* Signals */}
              {result.matchSignals?.length > 0 && (
                <div style={{ ...S.card, border: `1px solid ${T.success}33`, background: "rgba(52,201,122,0.04)" }}>
                  <div style={{ ...S.label, color: T.success, marginBottom: 8 }}>Match Signals</div>
                  {result.matchSignals.map((s, i) => <div key={i} style={{ fontSize: 13, color: T.success, opacity: 0.85, marginBottom: 4 }}>✓ {s}</div>)}
                </div>
              )}

              {/* Red flags */}
              {result.redFlags?.length > 0 && (
                <div style={{ ...S.card, border: `1px solid ${T.danger}33`, background: "rgba(229,62,62,0.04)" }}>
                  <div style={{ ...S.label, color: T.danger, marginBottom: 8 }}>Red Flags</div>
                  {result.redFlags.map((f, i) => <div key={i} style={{ fontSize: 13, color: T.danger, opacity: 0.85, marginBottom: 4 }}>⚠ {f}</div>)}
                </div>
              )}

              {/* Strategy */}
              <div style={{ ...S.card }}>
                <div style={{ marginBottom: 12 }}>
                  <div style={{ ...S.label, marginBottom: 6 }}>Positioning</div>
                  <p style={{ fontSize: 13, color: T.textSec, lineHeight: 1.65, margin: 0 }}>{result.positioning}</p>
                </div>
                <div>
                  <div style={{ ...S.label, marginBottom: 6 }}>Salary Strategy</div>
                  <p style={{ fontSize: 13, color: T.textSec, lineHeight: 1.65, margin: 0 }}>{result.salaryStrategy}</p>
                </div>
              </div>

              {/* Outreach message */}
              <div style={{ ...S.card }}>
                <div style={{ ...S.label, marginBottom: 10 }}>Outreach Message</div>
                <MessageBox text={result.outreachMessage} copyLabel="Copy Message" />
                {result.emailSubject && (
                  <div style={{ marginTop: 12 }}>
                    <div style={{ fontSize: 11, color: T.textMut, marginBottom: 6 }}>Email subject</div>
                    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                      <span style={{ ...S.mono, fontSize: 12, color: T.textSec, flex: 1 }}>{result.emailSubject}</span>
                      <CopyButton text={result.emailSubject} label="Copy Subject" />
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── PIPELINE PAGE ───────────────────────────────────────────────
// Applications grouped by stage category.
// Each card expands to show the pre-generated next-action message.
// Stage advance is one click. Ghost/Rejected shortcuts always visible.

// Maps status → message type to show when expanded
const STATUS_MSG_TYPE = {
  should_apply:             "recruiter",
  applied:                  "followup",
  hiring_manager_contacted: "followup",
};

// Maps status → next status when user clicks advance
const STATUS_ADVANCE = {
  should_apply:               "applied",
  applied:                    "recruiter_contacted",
  recruiter_contacted:        "hiring_manager_contacted",
  hiring_manager_contacted:   "recruiter_screen_scheduled",
  recruiter_screen_scheduled: "recruiter_screen_done",
  recruiter_screen_done:      "technical_interview_scheduled",
  technical_interview_scheduled: "technical_interview_done",
  technical_interview_done:   "final_interview",
};

const STAGE_GROUPS = [
  { label: "To Apply",    statuses: ["should_apply"],                                                                           color: T.amber   },
  { label: "In Progress", statuses: ["applied","recruiter_contacted","referral_requested","hiring_manager_contacted"],          color: T.blue    },
  { label: "Interviews",  statuses: ["recruiter_screen_scheduled","recruiter_screen_done","technical_interview_scheduled","technical_interview_done","final_interview"], color: T.success },
  { label: "Closed",      statuses: ["offer","rejected","ghosted","withdrawn"],                                                 color: T.textMut },
];

function PipelinePage() {
  const [apps, setApps]     = useState(APPLICATIONS);
  const [expanded, setExpanded] = useState(null);

  const advance = (id, next) => setApps(p => p.map(a => a.id === id ? { ...a, status: next } : a));
  const close   = (id, s)   => setApps(p => p.map(a => a.id === id ? { ...a, status: s   } : a));

  function AppCard({ app }) {
    const co         = COMPANIES.find(c => c.id === app.companyId);
    const msgType    = STATUS_MSG_TYPE[app.status];
    const nextStatus = STATUS_ADVANCE[app.status];
    const isOverdue  = app.nextActionDate && new Date(app.nextActionDate) < new Date();
    const daysSince  = app.dateApplied ? Math.floor((Date.now() - new Date(app.dateApplied).getTime()) / 86400000) : null;
    const isOpen     = expanded === app.id;

    const msg = msgType && co ? buildMessage(msgType, {
      companyName: co.name, role: app.vacancyTitle, domain: co.domain, city: co.city,
      daysSinceApplied: daysSince,
    }) : null;

    return (
      <div style={{ ...S.card, borderColor: isOverdue ? `${T.danger}44` : T.border, background: isOverdue ? "rgba(229,62,62,0.03)" : T.surface, marginBottom: 10 }}>
        {/* Summary row — always visible, click to expand */}
        <div style={{ cursor: "pointer", display: "flex", justifyContent: "space-between", alignItems: "flex-start" }} onClick={() => setExpanded(isOpen ? null : app.id)}>
          <div>
            <div style={{ fontWeight: 700, fontSize: 14, color: T.text }}>{app.companyName}</div>
            <div style={{ fontSize: 12, color: T.textMut, marginTop: 2 }}>{app.vacancyTitle}</div>
            <div style={{ display: "flex", gap: 14, marginTop: 6, fontSize: 12, color: T.textMut }}>
              {daysSince !== null && <span>{daysSince}d ago</span>}
              <span>{app.recruiterContacted ? "✓ Recruiter" : "○ Recruiter"}</span>
              <span>{app.hmContacted ? "✓ HM" : "○ HM"}</span>
              {isOverdue && <span style={{ color: T.danger, fontWeight: 700 }}>OVERDUE</span>}
            </div>
          </div>
          <span style={{ fontSize: 10, color: T.textMut }}>{isOpen ? "▲" : "▼"}</span>
        </div>

        {/* Expanded: next-action message + status controls */}
        {isOpen && (
          <div style={{ marginTop: 14, paddingTop: 14, borderTop: `1px solid ${T.border}` }}>
            {app.notes && <p style={{ fontSize: 13, color: T.textSec, lineHeight: 1.6, marginBottom: 14 }}>{app.notes}</p>}
            {msg && (
              <div style={{ marginBottom: 14 }}>
                <div style={{ ...S.label, marginBottom: 8 }}>{msgType === "recruiter" ? "Outreach Message" : "Follow-Up Message"}</div>
                <MessageBox text={msg} />
              </div>
            )}
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              {nextStatus && (
                <button onClick={() => advance(app.id, nextStatus)} style={{ padding: "7px 14px", borderRadius: 8, fontSize: 12, fontWeight: 600, background: T.surfaceEl, color: T.textSec, border: `1px solid ${T.border}`, cursor: "pointer" }}>
                  → {nextStatus.replace(/_/g, " ")}
                </button>
              )}
              <button onClick={() => close(app.id, "ghosted")}  style={{ padding: "7px 12px", fontSize: 12, color: T.textMut, background: "none", border: "none", cursor: "pointer" }}>Ghost</button>
              <button onClick={() => close(app.id, "rejected")} style={{ padding: "7px 12px", fontSize: 12, color: T.danger,  background: "none", border: "none", cursor: "pointer" }}>Rejected</button>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div style={{ padding: 24, maxWidth: 900 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700, color: T.text, margin: "0 0 4px" }}>Pipeline</h1>
      <p style={{ fontSize: 13, color: T.textSec, marginBottom: 20 }}>{apps.filter(a => !["rejected","ghosted","withdrawn"].includes(a.status)).length} active applications</p>
      {STAGE_GROUPS.map(group => {
        const groupApps = apps.filter(a => group.statuses.includes(a.status));
        if (!groupApps.length) return null;
        return (
          <div key={group.label} style={{ marginBottom: 28 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 14 }}>
              <span style={{ ...S.label, color: group.color }}>{group.label}</span>
              <div style={{ flex: 1, height: 1, background: T.border }} />
              <span style={{ ...S.mono, fontSize: 11, color: T.textMut }}>{groupApps.length}</span>
            </div>
            {groupApps.map(a => <AppCard key={a.id} app={a} />)}
          </div>
        );
      })}
    </div>
  );
}

// ─── CONTACTS PAGE ───────────────────────────────────────────────
// Contact grid sorted by relationship strength (warm first).
// Click card → message generator opens inline.

const CONTACT_TYPE_COLORS = { recruiter: T.blue, talent_acquisition: T.blue, engineering_manager: "#A855F7", head_of_engineering: "#EC4899", backend_engineer: T.success, referral_contact: T.amber, agency_recruiter: T.textMut };
const CONTACT_TYPE_LABELS = { recruiter: "Recruiter", talent_acquisition: "TA", engineering_manager: "Eng. Manager", head_of_engineering: "Head of Eng.", backend_engineer: "Backend Eng.", referral_contact: "Referral", agency_recruiter: "Agency" };
const CONTACT_STATUS_COLORS = { new: T.textMut, contacted: T.blue, replied: T.success, warm: T.amber };

function ContactsPage() {
  const [selected, setSelected] = useState(null);
  const [msgTab, setMsgTab]     = useState("recruiter");

  const sorted = [...CONTACTS].sort((a, b) => b.relationshipStrength - a.relationshipStrength);

  function ContactCard({ contact }) {
    const typeColor   = CONTACT_TYPE_COLORS[contact.type] || T.textMut;
    const statusColor = CONTACT_STATUS_COLORS[contact.status] || T.textMut;
    const isWarm      = contact.relationshipStrength >= 3;
    const isOpen      = selected?.id === contact.id;

    return (
      <div style={{ ...S.card, borderColor: isWarm ? `${T.amber}33` : T.border, background: isWarm ? "rgba(245,166,35,0.03)" : T.surface }}>
        {/* Card header */}
        <div onClick={() => setSelected(isOpen ? null : contact)} style={{ cursor: "pointer" }}>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
            <div>
              <div style={{ fontWeight: 700, fontSize: 14, color: T.text }}>{contact.name}</div>
              <div style={{ fontSize: 12, color: T.textSec, marginTop: 2 }}>{contact.title}</div>
              <div style={{ fontSize: 12, color: T.textMut, marginTop: 1 }}>{contact.companyName}</div>
            </div>
            <Badge color={typeColor}>{CONTACT_TYPE_LABELS[contact.type]}</Badge>
          </div>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 8 }}>
            <div style={{ display: "flex", gap: 3 }}>
              {[1,2,3,4,5].map(i => <div key={i} style={{ width: 7, height: 7, borderRadius: "50%", background: i <= contact.relationshipStrength ? typeColor : T.border }} />)}
            </div>
            <span style={{ fontSize: 11, fontWeight: 600, color: statusColor }}>{contact.status}</span>
          </div>
          {contact.notes && <p style={{ fontSize: 12, color: T.textMut, lineHeight: 1.5, margin: 0 }}>{contact.notes}</p>}
        </div>

        {/* Inline message generator — opens on click */}
        {isOpen && (
          <div style={{ marginTop: 14, paddingTop: 14, borderTop: `1px solid ${T.border}` }}>
            <div style={{ ...S.label, marginBottom: 10 }}>Message</div>
            <div style={{ display: "flex", gap: 6, marginBottom: 12, flexWrap: "wrap" }}>
              {MSG_TABS.slice(0, 4).map(t => (
                <button key={t.key} onClick={() => setMsgTab(t.key)} style={{ padding: "5px 12px", borderRadius: 16, fontSize: 11, fontWeight: 600, cursor: "pointer", background: msgTab === t.key ? T.amber : "transparent", color: msgTab === t.key ? T.bg : T.textSec, border: `1px solid ${msgTab === t.key ? T.amber : T.border}` }}>
                  {t.label}
                </button>
              ))}
            </div>
            <MessageBox text={buildMessage(msgTab, { companyName: contact.companyName, role: "Senior Backend Engineer", domain: "payments and fintech infrastructure", city: "Munich", contactName: contact.name })} />
          </div>
        )}
      </div>
    );
  }

  return (
    <div style={{ padding: 24, maxWidth: 900 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700, color: T.text, margin: "0 0 4px" }}>Contacts</h1>
      <p style={{ fontSize: 13, color: T.textSec, marginBottom: 20 }}>{CONTACTS.length} contacts · {CONTACTS.filter(c => c.status === "warm").length} warm</p>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: 12 }}>
        {sorted.map(c => <ContactCard key={c.id} contact={c} />)}
      </div>
    </div>
  );
}

// ─── NAVIGATION & ROOT ───────────────────────────────────────────

const PAGES = [
  { id: "today",     label: "Today",       icon: "⊙" },
  { id: "jd",        label: "Analyze Job", icon: "🔍" },
  { id: "pipeline",  label: "Pipeline",    icon: "⊞" },
  { id: "companies", label: "Companies",   icon: "⊟" },
  { id: "contacts",  label: "Contacts",    icon: "◎" },
];

export default function App() {
  const [page, setPage] = useState("today");
  const overdueCount = NEXT_ACTIONS.filter(a => a.isOverdue).length;

  const renderPage = () => {
    if (page === "today")     return <TodayPage />;
    if (page === "jd")        return <JDAnalyzerPage />;
    if (page === "pipeline")  return <PipelinePage />;
    if (page === "companies") return <CompaniesPage />;
    if (page === "contacts")  return <ContactsPage />;
    return <TodayPage />;
  };

  return (
    <div style={{ ...S.page, display: "flex", height: "100vh", overflow: "hidden" }}>
      <style>{`
        * { box-sizing: border-box; }
        body { margin: 0; }
        textarea, input { color-scheme: dark; }
        textarea::placeholder, input::placeholder { color: #4A5568; }
        @keyframes spin { to { transform: rotate(360deg); } }
        ::-webkit-scrollbar { width: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #1E2530; border-radius: 3px; }
        @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600;700&display=swap');
      `}</style>

      {/* Sidebar */}
      <div style={{ width: 200, flexShrink: 0, borderRight: `1px solid ${T.border}`, padding: "16px 0", display: "flex", flexDirection: "column", background: T.bg }}>
        <div style={{ padding: "0 16px 20px", display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{ width: 30, height: 30, borderRadius: 8, background: T.amber, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 900, fontSize: 14, color: T.bg }}>J</div>
          <span style={{ fontWeight: 800, fontSize: 15, color: T.text }}>JobOps</span>
        </div>
        {PAGES.map(p => {
          const active = page === p.id;
          return (
            <button key={p.id} onClick={() => setPage(p.id)} style={{ display: "flex", alignItems: "center", gap: 10, padding: "9px 16px", background: active ? T.amberDim : "none", borderLeft: `3px solid ${active ? T.amber : "transparent"}`, color: active ? T.amber : T.textSec, fontSize: 13, fontWeight: active ? 600 : 400, cursor: "pointer", border: "none", borderLeft: `3px solid ${active ? T.amber : "transparent"}`, textAlign: "left", width: "100%", marginBottom: 2 }}>
              <span>{p.icon}</span>
              <span style={{ flex: 1 }}>{p.label}</span>
              {p.id === "today" && overdueCount > 0 && (
                <span style={{ background: T.danger, color: "#fff", borderRadius: 10, padding: "1px 7px", fontSize: 10, fontWeight: 700 }}>{overdueCount}</span>
              )}
            </button>
          );
        })}
      </div>

      {/* Main content */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <div style={{ height: 48, borderBottom: `1px solid ${T.border}`, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 20px", flexShrink: 0 }}>
          <span style={{ fontSize: 13, fontWeight: 600, color: T.textSec }}>{PAGES.find(p => p.id === page)?.label}</span>
          <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
            <span style={{ ...S.mono, fontSize: 11, color: T.textMut }}>{new Date().toLocaleDateString("en-GB", { day: "numeric", month: "short" })}</span>
            <div style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 11, color: T.textMut }}>
              <div style={{ width: 6, height: 6, borderRadius: "50%", background: T.amber }} />
              Munich hunt active
            </div>
          </div>
        </div>
        <main style={{ flex: 1, overflowY: "auto" }}>
          {renderPage()}
        </main>
      </div>
    </div>
  );
}
