// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  userId: string
  email: string
}

// ─── Profile ─────────────────────────────────────────────────────────────────

export interface UserProfile {
  id: string
  userId: string
  fullName: string | null
  currentLocation: string | null
  targetCountries: string[]
  targetCities: string[]
  backupCities: string[]
  targetRoleTitles: string[]
  targetSalaryMin: number | null
  targetSalaryMax: number | null
  minimumSalary: number | null
  salaryStretchMax: number | null
  availability: string | null
  relocationStatus: string | null
  visaReadiness: string | null
  englishLevel: string | null
  germanLevel: string | null
  preferredIndustries: string[]
  rejectedIndustries: string[]
  preferredCompanyTypes: string[]
  rejectedCompanyTypes: string[]
  seniorityTarget: string | null
  positioningSummary: string | null
  outreachTone: string | null
  timezone: string
  searchStartDate: string | null
}

// ─── CV ──────────────────────────────────────────────────────────────────────

export interface Cv {
  id: string
  userId: string
  label: string
  version: number
  isDefault: boolean
  originalFilename: string
  fileSizeBytes: number
  createdAt: string
  updatedAt: string
}

// ─── Company ─────────────────────────────────────────────────────────────────

export type PriorityTier = 'P1' | 'P1_5' | 'P2' | 'P3'
export type CompanyStatus = 'WATCHLIST' | 'ACTIVE_TARGET' | 'APPLIED' | 'REJECTED' | 'PAUSED' | 'BLACKLISTED'
export type CompanyType = 'PRODUCT' | 'FINTECH' | 'BANK' | 'ECOMMERCE' | 'B2B_SAAS' | 'MOBILITY' | 'ENTERPRISE' | 'CONSULTING' | 'AGENCY' | 'OTHER'
export type EnglishLikelihood = 'YES' | 'NO' | 'UNCERTAIN'
export type RelocationFriendly = 'YES' | 'NO' | 'UNCERTAIN'
export type RecommendedStrategy = 'DIRECT_APPLY' | 'RECRUITER_MESSAGE' | 'MANAGER_OUTREACH' | 'REFERRAL_REQUEST'

export interface Company {
  id: string
  userId: string
  name: string
  website: string | null
  careersPageUrl: string | null
  linkedInUrl: string | null
  city: string | null
  country: string | null
  officeLocations: string[]
  remotePolicy: string | null
  industry: string | null
  companySize: string | null
  fundingStatus: string | null
  priorityTier: PriorityTier
  englishLikelihood: EnglishLikelihood
  relocationFriendly: RelocationFriendly
  visaSponsorship: RelocationFriendly
  salaryPitchMin: number | null
  salaryPitchMax: number | null
  companyType: CompanyType | null
  fitReason: string | null
  likelyRoles: string[]
  recommendedStrategy: RecommendedStrategy | null
  notes: string | null
  sourceUrl: string | null
  status: CompanyStatus
  createdAt: string
  updatedAt: string
}

// ─── Vacancy ──────────────────────────────────────────────────────────────────

export type VacancyStatus =
  | 'DISCOVERED' | 'ANALYZING' | 'SHOULD_APPLY' | 'MAYBE' | 'SKIP'
  | 'ADDED_TO_PIPELINE' | 'APPLIED' | 'INTERVIEWING' | 'REJECTED' | 'CLOSED' | 'OFFER'

export type AiRecommendation = 'APPLY' | 'MAYBE' | 'SKIP'

export interface Vacancy {
  id: string
  userId: string
  companyId: string
  companyName: string
  title: string
  location: string | null
  remotePolicy: string | null
  url: string | null
  sourceChannel: string | null
  jobDescriptionText: string | null
  stackKeywords: string[]
  domainKeywords: string[]
  salaryRangeMin: number | null
  salaryRangeMax: number | null
  salaryCurrency: string | null
  languageRequirement: string | null
  relocationVisaWording: string | null
  seniority: string | null
  employmentType: string | null
  status: VacancyStatus
  aiFitScore: number | null
  aiConfidence: number | null
  aiRecommendation: AiRecommendation | null
  aiReasoning: string | null
  redFlags: string[]
  uncertaintyFlags: string[]
  discoveredDate: string | null
  createdAt: string
  updatedAt: string
}

// ─── Job Analysis ─────────────────────────────────────────────────────────────

export interface JobAnalysis {
  id: string
  vacancyId: string
  recommendation: AiRecommendation
  fitScore: number
  confidence: number
  summary: string
  reasonsToApply: string[]
  reasonsToSkip: string[]
  redFlags: string[]
  uncertainties: string[]
  missingInfo: string[]
  roleFit: number
  stackFit: number
  domainFit: number
  seniorityFit: number
  locationFit: number
  languageFit: number
  companyTypeFit: number
  germanRequirement: string
  relocationRisk: string
  salaryRisk: string
  freshnessRisk: string
  suggestedPositioning: string | null
  suggestedOutreachAngle: string | null
  suggestedSalaryStrategy: string | null
  suggestedFirstMessage: string | null
  suggestedNextAction: string | null
  suggestedPriority: number | null
  hardBlockers: string[]
  createdAt: string
}

export interface AnalyzeJobRequest {
  jobDescription: string
  jobUrl?: string
  companyName?: string
  roleTitle?: string
  location?: string
  sourceChannel?: string
  sourceNotes?: string
  salaryInfo?: string
  languageRequirement?: string
  relocationWording?: string
  recruiterInfo?: string
  personalNote?: string
}

export interface AnalyzeJobResponse {
  vacancyId: string
  companyId: string | null
  vacancy: Vacancy
  analysis: JobAnalysis
}

// ─── Application ─────────────────────────────────────────────────────────────

export type ApplicationStage =
  | 'ADDED_TO_PIPELINE' | 'APPLIED' | 'RECRUITER_CONTACTED' | 'REFERRAL_REQUESTED'
  | 'HIRING_MANAGER_CONTACTED' | 'RECRUITER_SCREEN_SCHEDULED' | 'RECRUITER_SCREEN_DONE'
  | 'TECHNICAL_INTERVIEW_SCHEDULED' | 'TECHNICAL_INTERVIEW_DONE' | 'FINAL_INTERVIEW'
  | 'OFFER' | 'REJECTED' | 'GHOSTED' | 'WITHDRAWN' | 'ARCHIVED'

export type CityCategory = 'MUNICH' | 'BERLIN' | 'HAMBURG' | 'FRANKFURT' | 'STUTTGART' | 'NUREMBERG' | 'REMOTE' | 'OTHER'

export interface Application {
  id: string
  userId: string
  vacancyId: string
  vacancyTitle: string
  companyId: string
  companyName: string
  cvId: string | null
  stage: ApplicationStage
  applicationChannel: string | null
  sourceChannel: string | null
  dateApplied: string | null
  recruiterContacted: boolean
  hiringManagerContacted: boolean
  referralRequested: boolean
  followUpCount: number
  lastContactDate: string | null
  nextActionDate: string | null
  priority: number
  stale: boolean
  notes: string | null
  outcome: string | null
  rejectionReason: string | null
  cityCategory: CityCategory | null
  createdAt: string
  updatedAt: string
}

// ─── Contact ─────────────────────────────────────────────────────────────────

export type ContactType =
  | 'RECRUITER' | 'TALENT_ACQUISITION' | 'TECHNICAL_RECRUITER' | 'AGENCY_RECRUITER'
  | 'ENGINEERING_MANAGER' | 'HEAD_OF_ENGINEERING' | 'DIRECTOR_OF_ENGINEERING'
  | 'BACKEND_ENGINEER' | 'SENIOR_BACKEND_ENGINEER' | 'REFERRAL_CONTACT'
  | 'FOUNDER' | 'CTO' | 'OTHER'

export type ContactStatus = 'NEW' | 'CONTACTED' | 'REPLIED' | 'WARM' | 'NOT_RELEVANT' | 'DO_NOT_CONTACT'

export interface Contact {
  id: string
  userId: string
  companyId: string | null
  companyName: string | null
  firstName: string | null
  lastName: string | null
  name: string
  role: string | null
  title: string | null
  contactType: ContactType
  linkedIn: string | null
  linkedInUrl: string | null
  email: string | null
  relationshipStrength: 'COLD' | 'WARM' | 'HOT' | 'CONNECTED' | 'MET'
  source: string | null
  lastContactAt: string | null
  lastContactedDate: string | null
  nextFollowUpDate: string | null
  notes: string | null
  preferredChannel: 'LINKEDIN' | 'EMAIL'
  status: ContactStatus
  vacancyId: string | null
  applicationId: string | null
  createdAt: string
  updatedAt: string
}

// ─── Outreach Message ─────────────────────────────────────────────────────────

export type MessageType =
  | 'LINKEDIN_CONNECTION' | 'LINKEDIN_RECRUITER_DM' | 'LINKEDIN_MANAGER_DM'
  | 'REFERRAL_REQUEST' | 'EMAIL_RECRUITER' | 'FOLLOW_UP'
  | 'POST_INTERVIEW_THANK_YOU' | 'SALARY_ANSWER' | 'RELOCATION_ANSWER'
  | 'WHY_GERMANY' | 'WHY_MUNICH' | 'OPEN_TO_OTHER_CITIES' | 'COVER_NOTE' | 'CUSTOM'

export type MessageStatus = 'DRAFT' | 'COPIED' | 'SENT' | 'REPLIED' | 'IGNORED'

export interface OutreachMessage {
  id: string
  userId: string
  contactId: string | null
  contactName: string | null
  companyId: string | null
  companyName: string | null
  vacancyId: string | null
  vacancyTitle: string | null
  applicationId: string | null
  messageType: MessageType
  channel: string
  recipientType: string | null
  generatedText: string
  editedFinalText: string | null
  status: MessageStatus
  tone: string | null
  versionNumber: number
  nextActionId: string | null
  createdAt: string
  copiedAt: string | null
  sentAt: string | null
}

export interface GenerateMessageRequest {
  messageType: MessageType
  contactId?: string
  companyId?: string
  vacancyId?: string
  applicationId?: string
  tone?: string
  lengthTarget?: string
  customInstructions?: string
}

// ─── Next Action ──────────────────────────────────────────────────────────────

export type ActionType =
  | 'ANALYZE_JOB' | 'DECIDE_ADD_TO_PIPELINE' | 'APPLY_TO_JOB'
  | 'CONTACT_RECRUITER' | 'CONTACT_HIRING_MANAGER' | 'REQUEST_REFERRAL'
  | 'FOLLOW_UP_RECRUITER' | 'FOLLOW_UP_MANAGER' | 'FOLLOW_UP_REFERRAL_CONTACT'
  | 'PREPARE_RECRUITER_SCREEN' | 'PREPARE_TECH_INTERVIEW'
  | 'SEND_POST_INTERVIEW_THANK_YOU' | 'FOLLOW_UP_AFTER_INTERVIEW'
  | 'UPDATE_APPLICATION_STATUS' | 'CHECK_STALE_APPLICATION' | 'MARK_GHOSTED'
  | 'ADD_NEW_JOBS' | 'REVIEW_WEEKLY_FUNNEL' | 'EXPAND_CITY_SCOPE'
  | 'UPDATE_CV_FOR_ROLE' | 'CREATE_CUSTOM_MESSAGE' | 'ARCHIVE_LOW_FIT_JOB'
  | 'CHECK_SAVED_SEARCH' | 'REVIEW_POSITIONING' | 'REVIEW_MESSAGE_TEMPLATES'
  | 'INTERVIEW_PREP_REVIEW' | 'ANALYZE_REJECTION'

export type ActionPriority = 'P0' | 'P1' | 'P2' | 'P3'
export type ActionStatus = 'PENDING' | 'DONE' | 'SKIPPED' | 'SNOOZED' | 'CANCELLED' | 'OBSOLETE'

export interface NextAction {
  id: string
  userId: string
  actionType: ActionType
  priority: ActionPriority
  priorityScore: number
  dueDate: string
  status: ActionStatus
  reason: string
  companyId: string | null
  companyName: string | null
  vacancyId: string | null
  vacancyTitle: string | null
  applicationId: string | null
  contactId: string | null
  contactName: string | null
  messageId: string | null
  savedSearchId: string | null
  generatedMessageRequired: boolean
  recommendedMessageType: MessageType | null
  snoozedUntil: string | null
  skippedUntil: string | null
  createdAt: string
  completedAt: string | null
}

// ─── Saved Search ─────────────────────────────────────────────────────────────

export type SearchPlatform =
  | 'LINKEDIN' | 'GERMANTECHJOBS' | 'WEAREEDEVELOPERS' | 'ARBEITNOW'
  | 'RELOCATE_ME' | 'BERLIN_STARTUP_JOBS' | 'STEPSTONE' | 'COMPANY_CAREERS' | 'OTHER'

export interface SavedSearch {
  id: string
  userId: string
  title: string
  platform: SearchPlatform
  url: string | null
  queryText: string | null
  booleanQuery: string | null
  city: string | null
  keywords: string[]
  frequency: 'DAILY' | 'WEEKLY'
  lastCheckedAt: string | null
  nextCheckDate: string | null
  useful: boolean
  yieldRating: 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN'
  jobsAdded: number
  applicationsCreated: number
  responseRate: number | null
  notes: string | null
  active: boolean
  createdAt: string
  updatedAt: string
}

// ─── Analytics ───────────────────────────────────────────────────────────────

export interface DashboardSummary {
  todayActionsCount: number
  overdueActionsCount: number
  pendingDecisionsCount: number
  activeApplicationsCount: number
  applicationsByStage: Record<string, number>
  weeklyProgress: WeeklyProgress
  responseRatePercent: number | null
  topPriorityActions: NextAction[]
}

export interface WeeklyProgress {
  weekNumber: number
  applicationsTarget: number
  applicationsDone: number
  recruiterMessagesTarget: number
  recruiterMessagesDone: number
  managerMessagesTarget: number
  managerMessagesDone: number
  referralRequestsTarget: number
  referralRequestsDone: number
  jobsAnalyzedTarget: number
  jobsAnalyzedDone: number
}

export interface WeeklyReview {
  id: string
  weekNumber: number
  weekStart: string
  weekEnd: string
  jobsAnalyzed: number
  applyCount: number
  maybeCount: number
  skipCount: number
  directApplications: number
  applicationsTarget: number
  recruiterMessages: number
  managerMessages: number
  referralRequests: number
  followUpsSent: number
  totalResponses: number
  directApplyResponseRate: number | null
  warmOutreachResponseRate: number | null
  recruiterScreenRate: number | null
  techInterviewRate: number | null
  staleApplications: number
  ghostedApplications: number
  salaryBlockerCount: number
  languageBlockerCount: number
  relocationBlockerCount: number
  aiSummary: string | null
  whatWorked: string | null
  whatDidntWork: string | null
  aiRecommendations: string | null
  nextWeekTargets: string | null
  aiCityRecommendations: string[]
  createdAt: string
}

// ─── Generic ─────────────────────────────────────────────────────────────────

export interface ApiErrorResponse {
  code: string
  message: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}
