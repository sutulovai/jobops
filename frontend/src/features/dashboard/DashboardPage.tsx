import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { RefreshCw } from 'lucide-react'
import { toast } from 'sonner'
import { apiClient, getErrorMessage } from '@/app/api'
import { useAuthStore } from '@/app/auth'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { CopyButton } from '@/components/common/CopyButton'
import { cn, isBerlinPastWednesday, isDueDateOverdue } from '@/lib/utils'
import {
  generateMessageForAction,
  actionTypeLabel,
  messageTypeForCopiedLog,
} from '@/lib/messageTemplates'
import type { DashboardSummary, NextAction, Company, Application, Vacancy } from '@/types'

function getGreeting(): string {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 17) return 'Good afternoon'
  return 'Good evening'
}

function getNameFromEmail(email: string | null): string {
  if (!email) return 'Artem'
  const local = email.split('@')[0]
  const parts = local.split(/[._-]/)
  return parts[0].charAt(0).toUpperCase() + parts[0].slice(1)
}

const STAGE_COLORS: Record<string, string> = {
  ADDED_TO_PIPELINE: 'text-amber-400',
  APPLIED: 'text-blue-400',
  RECRUITER_CONTACTED: 'text-blue-400',
  REFERRAL_REQUESTED: 'text-blue-400',
  HIRING_MANAGER_CONTACTED: 'text-violet-400',
  RECRUITER_SCREEN_SCHEDULED: 'text-cyan-400',
  RECRUITER_SCREEN_DONE: 'text-cyan-400',
  TECHNICAL_INTERVIEW_SCHEDULED: 'text-emerald-400',
  TECHNICAL_INTERVIEW_DONE: 'text-emerald-400',
  FINAL_INTERVIEW: 'text-emerald-400',
  OFFER: 'text-amber-400',
}

export function DashboardPage() {
  const queryClient = useQueryClient()
  const email = useAuthStore((s) => s.email)

  const { data: summary, isLoading } = useQuery<DashboardSummary>({
    queryKey: ['dashboard-summary'],
    queryFn: () => apiClient.get('/dashboard').then((r) => r.data),
    refetchInterval: 60_000,
  })

  const { data: companies } = useQuery<Company[]>({
    queryKey: ['companies'],
    queryFn: () => apiClient.get('/companies').then((r) => r.data),
  })

  const { data: applications } = useQuery<Application[]>({
    queryKey: ['applications'],
    queryFn: () => apiClient.get('/applications').then((r) => r.data),
  })

  const { data: vacancies } = useQuery<Vacancy[]>({
    queryKey: ['vacancies'],
    queryFn: () => apiClient.get('/vacancies').then((r) => r.data),
  })

  const companiesById = new Map((companies ?? []).map((c) => [c.id, c]))

  const logCopyMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => apiClient.post('/messages/log-copy', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messages'] })
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const doneMutation = useMutation({
    mutationFn: ({ id, messageText }: { id: string; messageText?: string | null }) =>
      apiClient.post(`/actions/${id}/done`, messageText ? { messageText } : {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      queryClient.invalidateQueries({ queryKey: ['applications'] })
      queryClient.invalidateQueries({ queryKey: ['messages'] })
      toast.success('Action marked done')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const skipMutation = useMutation({
    mutationFn: (id: string) => apiClient.post(`/actions/${id}/skip`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      toast.success('Action skipped')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const recalcMutation = useMutation({
    mutationFn: () => apiClient.post('/actions/recalculate'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      toast.success('Actions recalculated')
    },
  })

  if (isLoading) return <LoadingState message="Loading today's actions…" className="mt-20" />

  const wp = summary?.weeklyProgress
  const allActions = summary?.topPriorityActions ?? []
  const totalPending =
    (summary?.todayActionsCount ?? 0) + (summary?.overdueActionsCount ?? 0)
  const responseRateLabel =
    summary?.responseRatePercent != null ? `${summary.responseRatePercent}%` : '—'

  const overdue = allActions.filter((a) => isDueDateOverdue(a.dueDate))
  const today = allActions.filter((a) => !isDueDateOverdue(a.dueDate) && a.priority !== 'P3')
  const upcoming = allActions.filter((a) => !isDueDateOverdue(a.dueDate) && a.priority === 'P3')

  const activeApps = (applications ?? []).filter(
    (a) => !['REJECTED', 'GHOSTED', 'WITHDRAWN', 'ARCHIVED'].includes(a.stage),
  )

  const companyIdsWithApplication = new Set(
    (applications ?? []).map((a) => a.companyId).filter(Boolean) as string[],
  )

  type ApplyNextRow = { companyId: string; companyName: string; city: string | null; vacancyTitle: string; fitScore: number }
  const applyNextRows: ApplyNextRow[] = (() => {
    const bestByCompany = new Map<string, ApplyNextRow>()
    for (const v of vacancies ?? []) {
      if (v.aiFitScore == null) continue
      const co = companiesById.get(v.companyId)
      if (!co || (co.status !== 'WATCHLIST' && co.status !== 'ACTIVE_TARGET')) continue
      if (companyIdsWithApplication.has(v.companyId)) continue
      const row: ApplyNextRow = {
        companyId: v.companyId,
        companyName: co.name,
        city: co.city,
        vacancyTitle: v.title,
        fitScore: v.aiFitScore,
      }
      const ex = bestByCompany.get(v.companyId)
      if (!ex || row.fitScore > ex.fitScore) bestByCompany.set(v.companyId, row)
    }
    return [...bestByCompany.values()].sort((a, b) => b.fitScore - a.fitScore).slice(0, 4)
  })()

  const dateStr = new Date().toLocaleDateString('en-GB', {
    weekday: 'long', day: 'numeric', month: 'long',
  })

  const noCompanies = (companies?.length ?? 0) === 0

  function renderActionCard(action: NextAction, isOverdue: boolean) {
    const company = action.companyId ? companiesById.get(action.companyId) ?? null : null
    const message = generateMessageForAction(
      action.actionType,
      company
        ? { name: company.name, city: company.city, fitReason: company.fitReason }
        : action.companyName
          ? { name: action.companyName, city: null, fitReason: null }
          : null,
      action.vacancyTitle,
      action.contactName,
    )
    const isPrep =
      action.actionType === 'PREPARE_RECRUITER_SCREEN' ||
      action.actionType === 'PREPARE_TECH_INTERVIEW'
    const showOutreachBlock = action.generatedMessageRequired || isPrep

    const logCopy = () => {
      logCopyMutation.mutate({
        messageType: messageTypeForCopiedLog(action),
        bodyText: message,
        companyId: action.companyId,
        vacancyId: action.vacancyId,
        applicationId: action.applicationId,
        contactId: action.contactId,
        channel: 'LINKEDIN',
      })
      toast.success('Copied — logged in Messages')
    }

    return (
      <ActionCard
        key={action.id}
        action={action}
        message={message}
        overdue={isOverdue}
        showOutreachBlock={showOutreachBlock}
        onDone={() =>
          doneMutation.mutate({ id: action.id, messageText: showOutreachBlock ? message : undefined })
        }
        onSkip={() => skipMutation.mutate(action.id)}
        onCopyLogged={showOutreachBlock ? logCopy : undefined}
        isLoading={doneMutation.isPending || skipMutation.isPending}
      />
    )
  }

  return (
    <div className="p-6 max-w-[1100px]">
      <div className="flex items-start justify-between mb-5">
        <div>
          <h1 className="text-xl font-bold text-zinc-100">
            {getGreeting()}, {getNameFromEmail(email)}
          </h1>
          <p className="text-[13px] text-zinc-500 mt-1">{dateStr}</p>
        </div>
        <div className="flex items-center gap-3">
          {totalPending > 0 && (
            <div className="font-mono bg-amber-500/10 border border-amber-500/25 rounded-full px-3 py-1.5 text-[13px] font-bold text-amber-400">
              {totalPending} actions
            </div>
          )}
          <button
            type="button"
            onClick={() => recalcMutation.mutate()}
            disabled={recalcMutation.isPending}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 text-xs text-zinc-400 border border-zinc-800 transition-colors"
          >
            <RefreshCw className={cn('h-3.5 w-3.5', recalcMutation.isPending && 'animate-spin')} />
            Recalculate
          </button>
        </div>
      </div>

      {wp && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4 mb-5">
          <div className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-3">
            Week {wp.weekNumber} Progress
          </div>
          <div className="flex gap-6 flex-wrap">
            {[
              { label: 'Applications', done: wp.applicationsDone, target: wp.applicationsTarget },
              { label: 'Recruiter DMs', done: wp.recruiterMessagesDone, target: wp.recruiterMessagesTarget },
              { label: 'Manager DMs', done: wp.managerMessagesDone, target: wp.managerMessagesTarget },
              { label: 'Referrals', done: wp.referralRequestsDone, target: wp.referralRequestsTarget },
              { label: 'JDs Analyzed', done: wp.jobsAnalyzedDone, target: wp.jobsAnalyzedTarget },
            ].map(({ label, done, target }) => (
              <WeekBar key={label} label={label} done={done} target={target} />
            ))}
          </div>
        </div>
      )}

      {noCompanies && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5 mb-5">
          <p className="text-[13px] text-zinc-400 mb-3 m-0">
            Add targets to get started — JobOps personalises actions and messages around your company list.
          </p>
          <div className="flex flex-wrap gap-2">
            <Link
              to="/companies"
              className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-bold bg-amber-500 hover:bg-amber-400 text-zinc-950"
            >
              Add your first companies
            </Link>
            <Link
              to="/analyze"
              className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold border border-zinc-700 text-zinc-300 hover:bg-zinc-800"
            >
              Analyze a job description
            </Link>
          </div>
        </div>
      )}

      <div className="grid grid-cols-[1fr_300px] gap-5 items-start">
        <div>
          {overdue.length > 0 && (
            <ActionSection
              label="Overdue"
              color="text-red-400"
              dotColor="bg-red-500"
              count={overdue.length}
              mb
            >
              {overdue.map((a) => renderActionCard(a, true))}
            </ActionSection>
          )}
          {today.length > 0 && (
            <ActionSection
              label="Today"
              color="text-amber-400"
              dotColor="bg-amber-500"
              count={today.length}
              mb
            >
              {today.map((a) => renderActionCard(a, false))}
            </ActionSection>
          )}
          {upcoming.length > 0 && (
            <ActionSection
              label="Upcoming"
              color="text-zinc-500"
              dotColor="bg-zinc-600"
              count={upcoming.length}
            >
              {upcoming.map((a) => renderActionCard(a, false))}
            </ActionSection>
          )}
          {allActions.length === 0 && !noCompanies && (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <div className="text-4xl mb-3">✓</div>
              <div className="text-zinc-400 font-semibold">All caught up</div>
              <div className="text-zinc-600 text-[13px] mt-1">No pending actions for today.</div>
            </div>
          )}
          {allActions.length === 0 && noCompanies && (
            <div className="flex flex-col items-center justify-center py-12 text-center text-zinc-600 text-[13px]">
              Your week progress is above — add companies to unlock Today actions.
            </div>
          )}
        </div>

        <div className="flex flex-col gap-3.5">
          <div className="grid grid-cols-2 gap-2.5">
            <StatCard
              label="Today's actions"
              value={totalPending}
              variant={totalPending > 0 ? 'amber' : 'default'}
            />
            <StatCard
              label="Overdue"
              value={summary?.overdueActionsCount ?? overdue.length}
              variant={overdue.length > 0 ? 'red' : 'default'}
            />
            <StatCard label="Active apps" value={activeApps.length} />
            <StatCard label="Response rate" value={responseRateLabel} />
          </div>

          {applyNextRows.length > 0 && (
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
              <div className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-3">
                Apply Next
              </div>
              {applyNextRows.map((row) => (
                <div key={row.companyId} className="flex items-center gap-2 mb-2.5 last:mb-0">
                  <div className="flex-1 min-w-0">
                    <div className="text-[13px] font-semibold text-zinc-100 truncate">{row.companyName}</div>
                    <div className="text-[11px] text-zinc-600 truncate">{row.vacancyTitle}</div>
                  </div>
                  <div className="flex-shrink-0 text-right">
                    <div
                      className={cn(
                        'font-mono text-[12px] font-bold',
                        row.fitScore >= 80 ? 'text-emerald-400' : 'text-amber-400',
                      )}
                    >
                      {row.fitScore}
                    </div>
                    <div className="w-11 h-[3px] bg-zinc-800 rounded mt-1">
                      <div
                        className={cn(
                          'h-full rounded',
                          row.fitScore >= 80 ? 'bg-emerald-400' : 'bg-amber-400',
                        )}
                        style={{ width: `${Math.min(row.fitScore, 100)}%` }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {activeApps.length > 0 && (
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
              <div className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-3">
                Active Pipeline
              </div>
              {activeApps.slice(0, 6).map((a) => (
                <div key={a.id} className="flex justify-between items-center mb-2 last:mb-0">
                  <span className="text-[13px] text-zinc-100 font-medium truncate flex-1 mr-2">
                    {a.companyName}
                  </span>
                  <span
                    className={cn(
                      'text-[11px] font-semibold flex-shrink-0',
                      STAGE_COLORS[a.stage] ?? 'text-zinc-500',
                    )}
                  >
                    {a.stage.replace(/_/g, ' ').toLowerCase()}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function ActionSection({
  label,
  color,
  dotColor,
  count,
  children,
  mb,
}: {
  label: string
  color: string
  dotColor: string
  count: number
  children: React.ReactNode
  mb?: boolean
}) {
  return (
    <div className={mb ? 'mb-6' : ''}>
      <div className="flex items-center gap-2 mb-3">
        <div className={cn('w-1.5 h-1.5 rounded-full flex-shrink-0', dotColor)} />
        <span className={cn('text-[11px] font-bold uppercase tracking-widest', color)}>{label}</span>
        <span className="text-[11px] text-zinc-700">· {count}</span>
        <div className="flex-1 h-px bg-zinc-800" />
      </div>
      {children}
    </div>
  )
}

function StatCard({
  label,
  value,
  variant = 'default',
}: {
  label: string
  value: number | string
  variant?: 'default' | 'amber' | 'red'
}) {
  return (
    <div
      className={cn(
        'rounded-xl border p-3',
        variant === 'red'
          ? 'bg-red-500/[0.06] border-red-500/25'
          : variant === 'amber'
            ? 'bg-zinc-900 border-amber-500/25'
            : 'bg-zinc-900 border-zinc-800',
      )}
    >
      <div
        className={cn(
          'font-mono text-[22px] font-bold',
          variant === 'red' ? 'text-red-400' : variant === 'amber' ? 'text-amber-400' : 'text-zinc-100',
        )}
      >
        {value}
      </div>
      <div className="text-[11px] text-zinc-600 mt-0.5">{label}</div>
    </div>
  )
}

function WeekBar({ label, done, target }: { label: string; done: number; target: number }) {
  const pct = target > 0 ? Math.min((done / target) * 100, 100) : 0
  const hit = pct >= 100
  const behindPace = isBerlinPastWednesday() && target > 0 && done / target < 0.3
  const barColor = hit ? 'bg-emerald-500' : behindPace ? 'bg-red-500' : 'bg-amber-500'
  const labelColor = hit ? 'text-emerald-400' : behindPace ? 'text-red-400' : done > 0 ? 'text-amber-400' : 'text-zinc-600'

  return (
    <div className="flex-1 min-w-[80px]">
      <div className="flex justify-between mb-1.5">
        <span className="text-[11px] text-zinc-500">{label}</span>
        <span className={cn('font-mono text-[11px] font-bold', labelColor)}>
          {done}/{target}
        </span>
      </div>
      <div className="h-[3px] bg-zinc-800 rounded-full overflow-hidden">
        <div className={cn('h-full rounded-full', barColor)} style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

function ActionCard({
  action,
  message,
  overdue,
  showOutreachBlock,
  onDone,
  onSkip,
  onCopyLogged,
  isLoading,
}: {
  action: NextAction
  message: string
  overdue: boolean
  showOutreachBlock: boolean
  onDone: () => void
  onSkip: () => void
  onCopyLogged?: () => void
  isLoading: boolean
}) {
  const dotColor = overdue ? 'bg-red-500' : action.priority === 'P0' ? 'bg-amber-500' : 'bg-blue-500'
  const borderColor = overdue
    ? 'border-red-500/25'
    : action.priority === 'P0'
      ? 'border-amber-500/20'
      : 'border-zinc-800'
  const bgColor = overdue ? 'bg-red-500/[0.04]' : 'bg-zinc-900'

  return (
    <div className={cn('rounded-xl border p-4 mb-3', bgColor, borderColor)}>
      <div className="flex items-start gap-2.5 mb-3">
        <div className={cn('w-2 h-2 rounded-full flex-shrink-0 mt-[5px]', dotColor)} />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap mb-1">
            <span className="text-[11px] font-bold uppercase tracking-widest text-zinc-600">
              {actionTypeLabel(action.actionType)}
            </span>
            {action.companyName && (
              <span className="text-amber-400 font-semibold text-[14px]">· {action.companyName}</span>
            )}
            {overdue && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-bold bg-red-500/15 text-red-400 border border-red-500/25">
                OVERDUE
              </span>
            )}
          </div>
          <p className="text-[13px] text-zinc-400 leading-[1.55] m-0">{action.reason}</p>
        </div>
      </div>

      {showOutreachBlock && (
        <div className="mb-3">
          <pre className="font-mono bg-black/40 border border-zinc-800 rounded-xl px-4 py-3.5 text-[13px] text-zinc-300 whitespace-pre-wrap leading-[1.7] m-0">
            {message}
          </pre>
          <div className="mt-2.5">
            <CopyButton text={message} size="sm" label="Copy Message" onCopied={onCopyLogged} />
          </div>
        </div>
      )}

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={onDone}
          disabled={isLoading}
          className="px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-zinc-800 hover:bg-zinc-700 text-zinc-400 border border-zinc-700 transition-colors disabled:opacity-50"
        >
          ✓ Mark Done
        </button>
        <button
          type="button"
          onClick={onSkip}
          disabled={isLoading}
          className="px-3 py-1.5 text-xs text-zinc-600 hover:text-zinc-400 transition-colors disabled:opacity-50"
        >
          Skip
        </button>
      </div>
    </div>
  )
}
