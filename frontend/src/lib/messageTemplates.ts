import type { Company, MessageType } from '@/types'

export interface MessageContext {
  companyName: string
  role: string
  domain: string
  city: string
  contactName?: string
  daysSinceApplied?: number
  interviewTopic?: string
}

// Fixed candidate profile
const CANDIDATE = {
  name: 'Artem',
  stack: 'Java/Kotlin, Spring Boot, Kafka, PostgreSQL, distributed systems',
  domain: 'payments, fintech, transaction-critical systems',
  experience: '8+ years',
  visa: 'EU Blue Card ready',
  notice: '2–3 months',
  lang: 'English professional',
}

function prepRecruiterScreenBullets(ctx: MessageContext): string {
  return [
    `Recruiter screen prep — ${ctx.companyName} (${ctx.role})`,
    '',
    `• Tie your wins in ${ctx.domain} to how they ship and scale.`,
    '• Java/Kotlin, Kafka, PostgreSQL — ownership, incidents, safe migrations.',
    '• Relocation: EU Blue Card ready, 2–3 months notice — keep it factual.',
    '• Ask about team topology, on-call, and what strong performance looks like in 90 days.',
  ].join('\n')
}

function prepTechnicalInterviewBullets(ctx: MessageContext): string {
  return [
    `Technical interview prep — ${ctx.companyName} (${ctx.role})`,
    '',
    '• System design: trade-offs, Kafka/PostgreSQL, reliability and cost.',
    `• Deep examples from ${CANDIDATE.domain} — idempotency, retries, outbox, rollbacks.`,
    '• Production: incidents, postmortems, feature flags, staged rollouts.',
    '• Clarify scope, stack ownership, and expectations for a senior IC.',
  ].join('\n')
}

export function recruiterOutreach(ctx: MessageContext): string {
  const contact = ctx.contactName ? `Hi ${ctx.contactName},` : 'Hi,'
  return `${contact}

I came across the ${ctx.role} role at ${ctx.companyName} and wanted to reach out directly.

I'm a Senior Backend Engineer with ${CANDIDATE.experience} in ${CANDIDATE.domain}, building high-throughput systems with Java/Kotlin, Spring Boot, Kafka, and PostgreSQL. ${ctx.companyName}'s work in ${ctx.domain} maps closely to my background.

I'm ${CANDIDATE.visa}, ${CANDIDATE.notice} notice, ${CANDIDATE.lang}. Based in ${ctx.city} or open to relocation.

Would you have 15 minutes to connect? I'd be happy to share more.

Best,
${CANDIDATE.name}`
}

export function hiringManagerOutreach(ctx: MessageContext): string {
  const contact = ctx.contactName ? `Hi ${ctx.contactName},` : 'Hi,'
  return `${contact}

I'm reaching out about the ${ctx.role} position at ${ctx.companyName}.

I have ${CANDIDATE.experience} building ${CANDIDATE.domain} systems — specifically the kind of reliability, migration safety, and production ownership work that matters at your scale. My stack is Java/Kotlin, Spring Boot, Kafka, PostgreSQL.

I'd love to have a brief conversation about what the team is working on and whether my background is a good fit. I'm ${CANDIDATE.visa}, available in ${CANDIDATE.notice}.

Looking forward to connecting.

Best,
${CANDIDATE.name}`
}

export function referralRequest(ctx: MessageContext): string {
  const contact = ctx.contactName ? `Hi ${ctx.contactName},` : 'Hi,'
  return `${contact}

I noticed ${ctx.companyName} is hiring for ${ctx.role} and I'd love to apply. Given your experience there, I thought I'd reach out first.

My background: ${CANDIDATE.experience} in ${CANDIDATE.domain}, Java/Kotlin/Spring Boot, Kafka, PostgreSQL. Strong on reliability and production ownership. ${CANDIDATE.visa}, ${CANDIDATE.lang}.

If you think there's a good fit, I'd really appreciate a referral or just your honest take on whether the role/team would be a good match.

No pressure either way — thanks for reading.

Best,
${CANDIDATE.name}`
}

export function followUp(ctx: MessageContext): string {
  const contact = ctx.contactName ? `Hi ${ctx.contactName},` : 'Hi,'
  const timeRef = ctx.daysSinceApplied ? `about ${ctx.daysSinceApplied} days ago` : 'recently'
  return `${contact}

I applied for the ${ctx.role} position at ${ctx.companyName} ${timeRef} and wanted to follow up to confirm my application was received.

I'm genuinely interested in this role — my background in ${CANDIDATE.domain} with Java/Kotlin and distributed systems maps closely to what you're looking for.

Happy to share more details or answer any questions. Thanks for your time.

Best,
${CANDIDATE.name}`
}

export function coldLinkedIn(ctx: MessageContext): string {
  const contact = ctx.contactName ? `Hi ${ctx.contactName},` : 'Hi,'
  return `${contact}

I'm a Senior Backend Engineer with ${CANDIDATE.experience} in ${CANDIDATE.domain} — Java/Kotlin, Spring Boot, Kafka, PostgreSQL. I've been following ${ctx.companyName}'s work in ${ctx.domain} and it's exactly the space I want to be building in.

I'm exploring senior backend roles in ${ctx.city} for ${CANDIDATE.notice} start. Would love to connect and learn more about what the engineering team is working on.

${CANDIDATE.visa}, ${CANDIDATE.lang}.

Best,
${CANDIDATE.name}`
}

export function postInterviewThankYou(ctx: MessageContext): string {
  const contact = ctx.contactName ? `Hi ${ctx.contactName},` : 'Hi,'
  const topicRef = ctx.interviewTopic ? `the discussion around ${ctx.interviewTopic}` : 'our conversation'
  return `${contact}

Thank you for taking the time today. I really enjoyed ${topicRef} — it gave me a clearer picture of the technical challenges the team is working through.

The ${ctx.role} role at ${ctx.companyName} feels like a strong fit, both in terms of the domain (${ctx.domain}) and the engineering culture you described. I'm excited about the possibility of contributing.

Please let me know if there's anything else you need from my side.

Best,
${CANDIDATE.name}`
}

/** Pipeline stage → message type when logging a copy from Pipeline */
export function messageTypeForPipelineStage(stage: string): MessageType {
  switch (stage) {
    case 'ADDED_TO_PIPELINE':
      return 'LINKEDIN_RECRUITER_DM'
    case 'APPLIED':
    case 'HIRING_MANAGER_CONTACTED':
      return 'FOLLOW_UP'
    default:
      return 'CUSTOM'
  }
}

/** Maps NextAction type → outreach message type for /messages/log-copy */
export function messageTypeForCopiedLog(action: {
  actionType: string
  recommendedMessageType: MessageType | null
}): MessageType {
  if (action.recommendedMessageType) {
    return action.recommendedMessageType
  }
  switch (action.actionType) {
    case 'APPLY_TO_JOB':
    case 'CONTACT_RECRUITER':
      return 'LINKEDIN_RECRUITER_DM'
    case 'CONTACT_HIRING_MANAGER':
      return 'LINKEDIN_MANAGER_DM'
    case 'REQUEST_REFERRAL':
      return 'REFERRAL_REQUEST'
    case 'FOLLOW_UP_RECRUITER':
    case 'FOLLOW_UP_MANAGER':
    case 'FOLLOW_UP_REFERRAL_CONTACT':
      return 'FOLLOW_UP'
    case 'SEND_POST_INTERVIEW_THANK_YOU':
      return 'POST_INTERVIEW_THANK_YOU'
    default:
      return 'CUSTOM'
  }
}

// Maps action types to the right template
export function generateMessageForAction(
  actionType: string,
  company: Pick<Company, 'name' | 'city' | 'fitReason'> | null,
  vacancyTitle?: string | null,
  contactName?: string | null,
  daysSinceApplied?: number,
): string {
  const ctx: MessageContext = {
    companyName: company?.name ?? 'the company',
    role: vacancyTitle ?? 'Senior Backend Engineer',
    domain: extractDomain(company?.fitReason),
    city: company?.city ?? 'Germany',
    contactName: contactName ?? undefined,
    daysSinceApplied,
  }

  switch (actionType) {
    case 'APPLY_TO_JOB':
    case 'CONTACT_RECRUITER':
      return recruiterOutreach(ctx)
    case 'CONTACT_HIRING_MANAGER':
      return hiringManagerOutreach(ctx)
    case 'REQUEST_REFERRAL':
      return referralRequest(ctx)
    case 'FOLLOW_UP_RECRUITER':
    case 'FOLLOW_UP_MANAGER':
    case 'FOLLOW_UP_REFERRAL_CONTACT':
      return followUp(ctx)
    case 'SEND_POST_INTERVIEW_THANK_YOU':
      return postInterviewThankYou(ctx)
    case 'ANALYZE_REJECTION':
      return 'Capture why this role didn’t work (fit, visa, comp, process) in the application notes — you’ll use it in the weekly review.'
    case 'PREPARE_RECRUITER_SCREEN':
      return prepRecruiterScreenBullets(ctx)
    case 'PREPARE_TECH_INTERVIEW':
      return prepTechnicalInterviewBullets(ctx)
    case 'DECIDE_ADD_TO_PIPELINE':
      return 'Review the analyzed role under Vacancies: add it to the pipeline (or mark Skip) so your Today feed stays accurate.'
    case 'CHECK_SAVED_SEARCH':
      return 'Open Saved Searches, run the LinkedIn query, and add any strong fits for analysis.'
    case 'MARK_GHOSTED':
      return 'If there is still no reply after your final follow-up, mark the application as Ghosted in Pipeline — or send one last short line if you want explicit closure.'
    case 'ADD_NEW_JOBS':
      return 'Add at least one new target or JD this week: paste a description into Analyze Job and promote strong fits to your pipeline.'
    case 'EXPAND_CITY_SCOPE':
      return 'Broaden geography: add Berlin, Hamburg, or Frankfurt roles so the funnel is not single-city dependent.'
    case 'REVIEW_WEEKLY_FUNNEL':
      return 'Open Weekly Review and log what moved, what stalled, and what you will change next week.'
    case 'UPDATE_CV_FOR_ROLE':
      return 'Update your CV for the roles you are pursuing, then upload the latest file on Profile & CV.'
    case 'CREATE_CUSTOM_MESSAGE':
      return 'Draft a short custom note in Messages or edit an existing template before sending.'
    case 'ARCHIVE_LOW_FIT_JOB':
      return 'Archive or skip low-fit vacancies so they stop surfacing as decisions.'
    case 'REVIEW_POSITIONING':
      return 'Re-read your Profile positioning summary and tighten how you describe scope and impact.'
    case 'REVIEW_MESSAGE_TEMPLATES':
      return 'Skim your standard outreach angles and adjust for roles you are pursuing this week.'
    case 'INTERVIEW_PREP_REVIEW':
      return 'Block 30 minutes to review your prep notes and top stories before the next interview.'
    default:
      return 'Complete this step in JobOps — there is no boilerplate outreach block for this action.'
  }
}

// Maps pipeline stages to the right template
export function generateMessageForStage(
  stage: string,
  company: Pick<Company, 'name' | 'city' | 'fitReason'> | null,
  vacancyTitle?: string | null,
  contactName?: string | null,
  daysSinceApplied?: number,
): string | null {
  const ctx: MessageContext = {
    companyName: company?.name ?? 'the company',
    role: vacancyTitle ?? 'Senior Backend Engineer',
    domain: extractDomain(company?.fitReason),
    city: company?.city ?? 'Germany',
    contactName: contactName ?? undefined,
    daysSinceApplied,
  }

  switch (stage) {
    case 'ADDED_TO_PIPELINE':
      return recruiterOutreach(ctx)
    case 'APPLIED':
      return followUp(ctx)
    case 'HIRING_MANAGER_CONTACTED':
      return followUp(ctx)
    case 'RECRUITER_SCREEN_SCHEDULED':
      return `Hi,\n\nJust confirming our call scheduled for ${company?.name ?? 'your company'}. I'm looking forward to it.\n\nI'll have notes on my background in ${CANDIDATE.domain} ready and am happy to go wherever the conversation leads.\n\nSee you soon.\n\nBest,\n${CANDIDATE.name}`
    case 'TECHNICAL_INTERVIEW_SCHEDULED':
      return `Hi,\n\nI'm looking forward to the technical interview at ${company?.name ?? 'your company'}. I'll come prepared with examples from my work in ${CANDIDATE.domain} — distributed systems design, Kafka, PostgreSQL, reliability patterns.\n\nHappy to focus on whatever areas are most relevant to the team.\n\nBest,\n${CANDIDATE.name}`
    default:
      return null
  }
}

// Returns all 5 message types for the company modal tabs
export function generateAllMessages(company: Pick<Company, 'name' | 'city' | 'fitReason' | 'likelyRoles'>): {
  recruiter: string
  hm: string
  referral: string
  followUp: string
  cold: string
} {
  const role = company.likelyRoles?.[0] ?? 'Senior Backend Engineer'
  const ctx: MessageContext = {
    companyName: company.name,
    role,
    domain: extractDomain(company.fitReason),
    city: company.city ?? 'Germany',
  }
  return {
    recruiter: recruiterOutreach(ctx),
    hm: hiringManagerOutreach(ctx),
    referral: referralRequest(ctx),
    followUp: followUp(ctx),
    cold: coldLinkedIn(ctx),
  }
}

// Returns messages for a contact (includes contactName in salutation)
export function generateAllMessagesForContact(params: {
  companyName: string | null
  contactName: string
  fitReason?: string | null
  city?: string | null
}): {
  recruiter: string
  hm: string
  referral: string
  followUp: string
} {
  const ctx: MessageContext = {
    companyName: params.companyName ?? 'the company',
    role: 'Senior Backend Engineer',
    domain: extractDomain(params.fitReason),
    city: params.city ?? 'Germany',
    contactName: params.contactName,
  }
  return {
    recruiter: recruiterOutreach(ctx),
    hm: hiringManagerOutreach(ctx),
    referral: referralRequest(ctx),
    followUp: followUp(ctx),
  }
}

// Build a human-readable label for an action card
export function actionTypeLabel(actionType: string): string {
  const labels: Record<string, string> = {
    APPLY_TO_JOB: 'APPLY',
    CONTACT_RECRUITER: 'MESSAGE RECRUITER',
    CONTACT_HIRING_MANAGER: 'MESSAGE HIRING MANAGER',
    REQUEST_REFERRAL: 'REQUEST REFERRAL',
    FOLLOW_UP_RECRUITER: 'SEND FOLLOW-UP',
    FOLLOW_UP_MANAGER: 'SEND FOLLOW-UP',
    FOLLOW_UP_REFERRAL_CONTACT: 'SEND FOLLOW-UP',
    PREPARE_RECRUITER_SCREEN: 'PREPARE INTERVIEW',
    PREPARE_TECH_INTERVIEW: 'PREPARE INTERVIEW',
    SEND_POST_INTERVIEW_THANK_YOU: 'SEND THANK-YOU',
    DECIDE_ADD_TO_PIPELINE: 'DECIDE',
    ADD_NEW_JOBS: 'ADD JOBS',
    MARK_GHOSTED: 'MARK GHOSTED',
    CHECK_SAVED_SEARCH: 'CHECK SEARCHES',
    EXPAND_CITY_SCOPE: 'EXPAND CITIES',
    ANALYZE_REJECTION: 'LOG REJECTION REASON',
  }
  return labels[actionType] ?? actionType.replace(/_/g, ' ')
}

export function actionTypeColor(actionType: string): string {
  if (actionType === 'APPLY_TO_JOB') return 'text-amber-400'
  if (actionType.includes('RECRUITER') || actionType.includes('FOLLOW_UP')) return 'text-blue-400'
  if (actionType.includes('MANAGER') || actionType.includes('HM')) return 'text-violet-400'
  if (actionType.includes('REFERRAL')) return 'text-emerald-400'
  if (actionType.includes('PREPARE')) return 'text-cyan-400'
  if (actionType.includes('THANK')) return 'text-pink-400'
  return 'text-zinc-400'
}

function extractDomain(fitReason?: string | null): string {
  if (!fitReason) return 'payments and fintech'
  // Take first sentence as the domain description
  const first = fitReason.split('.')[0].trim()
  return first.length > 10 ? first : 'payments and fintech'
}

// Label shown on the copy button inside a pipeline card
export const STAGE_MESSAGE_LABELS: Record<string, string> = {
  ADDED_TO_PIPELINE: 'Copy outreach message before applying',
  APPLIED: 'Follow-up (no response yet)',
  HIRING_MANAGER_CONTACTED: 'HM follow-up',
  RECRUITER_SCREEN_SCHEDULED: 'Pre-call confirmation',
  TECHNICAL_INTERVIEW_SCHEDULED: 'Pre-interview note',
}
