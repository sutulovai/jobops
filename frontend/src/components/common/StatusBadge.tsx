import { cn } from '@/lib/utils'
import type { AiRecommendation } from '@/types'

interface StatusBadgeProps {
  status: string
  className?: string
}

const stageColors: Record<string, string> = {
  ADDED_TO_PIPELINE: 'bg-zinc-700/50 text-zinc-300 border-zinc-600/50',
  APPLIED: 'bg-blue-500/15 text-blue-300 border-blue-500/30',
  RECRUITER_CONTACTED: 'bg-blue-500/20 text-blue-400 border-blue-500/40',
  REFERRAL_REQUESTED: 'bg-violet-500/20 text-violet-400 border-violet-500/40',
  HIRING_MANAGER_CONTACTED: 'bg-violet-500/25 text-violet-300 border-violet-500/40',
  RECRUITER_SCREEN_SCHEDULED: 'bg-amber-500/20 text-amber-400 border-amber-500/40',
  RECRUITER_SCREEN_DONE: 'bg-amber-500/25 text-amber-300 border-amber-500/40',
  TECHNICAL_INTERVIEW_SCHEDULED: 'bg-orange-500/20 text-orange-400 border-orange-500/40',
  TECHNICAL_INTERVIEW_DONE: 'bg-orange-500/25 text-orange-300 border-orange-500/40',
  FINAL_INTERVIEW: 'bg-yellow-500/25 text-yellow-300 border-yellow-500/40',
  OFFER: 'bg-emerald-500/25 text-emerald-300 border-emerald-500/40',
  REJECTED: 'bg-red-500/15 text-red-400 border-red-500/30',
  GHOSTED: 'bg-zinc-600/40 text-zinc-500 border-zinc-600/40',
  WITHDRAWN: 'bg-zinc-600/40 text-zinc-500 border-zinc-600/40',
  ARCHIVED: 'bg-zinc-700/30 text-zinc-600 border-zinc-700/40',
  // AI Recommendation
  APPLY: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40',
  MAYBE: 'bg-amber-500/20 text-amber-400 border-amber-500/40',
  SKIP: 'bg-red-500/15 text-red-400 border-red-500/30',
  // Company status
  WATCHLIST: 'bg-zinc-700/50 text-zinc-400 border-zinc-600/50',
  ACTIVE_TARGET: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
  BLACKLISTED: 'bg-red-500/15 text-red-400 border-red-500/30',
  PAUSED: 'bg-zinc-600/40 text-zinc-500 border-zinc-600/40',
}

const stageLabels: Record<string, string> = {
  ADDED_TO_PIPELINE: 'In Pipeline',
  RECRUITER_CONTACTED: 'Recruiter Contacted',
  REFERRAL_REQUESTED: 'Referral Requested',
  HIRING_MANAGER_CONTACTED: 'HM Contacted',
  RECRUITER_SCREEN_SCHEDULED: 'Screen Scheduled',
  RECRUITER_SCREEN_DONE: 'Screen Done',
  TECHNICAL_INTERVIEW_SCHEDULED: 'Tech Interview',
  TECHNICAL_INTERVIEW_DONE: 'Tech Done',
  FINAL_INTERVIEW: 'Final Interview',
  ACTIVE_TARGET: 'Active Target',
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const colorClass = stageColors[status] ?? 'bg-zinc-700/50 text-zinc-400 border-zinc-600/50'
  const label = stageLabels[status] ?? status.replace(/_/g, ' ')

  return (
    <span
      className={cn(
        'inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium border',
        colorClass,
        className,
      )}
    >
      {label}
    </span>
  )
}

export function RecommendationBadge({ rec }: { rec: AiRecommendation }) {
  const styles: Record<AiRecommendation, { color: string; label: string }> = {
    APPLY: { color: 'bg-emerald-500/25 text-emerald-300 border-emerald-500/50', label: '✓ APPLY' },
    MAYBE: { color: 'bg-amber-500/25 text-amber-300 border-amber-500/50', label: '? MAYBE' },
    SKIP: { color: 'bg-red-500/20 text-red-400 border-red-500/40', label: '✗ SKIP' },
  }
  const s = styles[rec]
  return (
    <span className={cn('inline-flex items-center px-2 py-1 rounded font-bold text-sm border font-mono', s.color)}>
      {s.label}
    </span>
  )
}
