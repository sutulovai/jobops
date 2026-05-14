import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import type { DashboardSummary } from '@/types'
import { cn } from '@/lib/utils'

function Metric({ label, value, sub, color }: { label: string; value: string | number; sub?: string; color?: string }) {
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
      <p className="text-xs text-zinc-500">{label}</p>
      <p className={cn('text-2xl font-bold mt-1', color ?? 'text-zinc-100')}>{value}</p>
      {sub && <p className="text-xs text-zinc-600 mt-0.5">{sub}</p>}
    </div>
  )
}

function RateBar({ label, value, benchmark }: { label: string; value: number; benchmark: number }) {
  const pct = Math.min(100, value)
  const good = value >= benchmark
  return (
    <div>
      <div className="flex items-center justify-between mb-1">
        <span className="text-xs text-zinc-400">{label}</span>
        <span className={cn('text-xs font-mono font-bold', good ? 'text-emerald-400' : 'text-amber-400')}>
          {value.toFixed(1)}%
          <span className="text-zinc-600 font-normal ml-1">/ {benchmark}% target</span>
        </span>
      </div>
      <div className="h-1.5 bg-zinc-800 rounded-full overflow-hidden">
        <div
          className={cn('h-full rounded-full', good ? 'bg-emerald-500' : 'bg-amber-500')}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

export function AnalyticsPage() {
  const { data, isLoading } = useQuery<DashboardSummary>({
    queryKey: ['dashboard-summary'],
    queryFn: () => apiClient.get('/dashboard').then(r => r.data),
  })

  if (isLoading) return <LoadingState message="Loading analytics…" className="mt-20" />
  if (!data) return null

  const wp = data.weeklyProgress
  const stats = data.applicationsByStage ?? {}

  const totalApps = Object.values(stats).reduce((a, b) => a + b, 0)
  const activeApps = data.activeApplicationsCount ?? 0

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Analytics</h1>
        <p className="text-sm text-zinc-500 mt-0.5">Funnel performance at a glance</p>
      </div>

      {/* Key metrics */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-8">
        <Metric label="Total applications" value={totalApps} />
        <Metric label="Active in pipeline" value={activeApps} color="text-amber-400" />
        <Metric label="Today's actions" value={data.todayActionsCount ?? 0} />
        <Metric label="Overdue" value={data.overdueActionsCount ?? 0} color={data.overdueActionsCount ? 'text-red-400' : 'text-zinc-100'} />
      </div>

      {/* Pipeline breakdown */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5 mb-6">
        <h2 className="text-xs font-bold text-zinc-400 uppercase tracking-widest mb-4">Pipeline Stages</h2>
        <div className="grid grid-cols-3 md:grid-cols-6 gap-3">
          {Object.entries(stats).map(([stage, count]) => (
            <div key={stage} className="text-center">
              <p className="text-lg font-bold text-zinc-100">{count}</p>
              <p className="text-xs text-zinc-600">{stage.replace(/_/g, ' ')}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Weekly progress */}
      {wp && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5 mb-6">
          <h2 className="text-xs font-bold text-zinc-400 uppercase tracking-widest mb-4">This Week vs Targets</h2>
          <div className="space-y-3">
            <RateBar
              label="Direct Applications"
              value={wp.applicationsTarget > 0 ? (wp.applicationsDone / wp.applicationsTarget) * 100 : 0}
              benchmark={75}
            />
            <RateBar
              label="Recruiter Messages"
              value={wp.recruiterMessagesTarget > 0 ? (wp.recruiterMessagesDone / wp.recruiterMessagesTarget) * 100 : 0}
              benchmark={75}
            />
            <RateBar
              label="Manager Outreach"
              value={wp.managerMessagesTarget > 0 ? (wp.managerMessagesDone / wp.managerMessagesTarget) * 100 : 0}
              benchmark={75}
            />
            <RateBar
              label="Referral Requests"
              value={wp.referralRequestsTarget > 0 ? (wp.referralRequestsDone / wp.referralRequestsTarget) * 100 : 0}
              benchmark={75}
            />
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-4 pt-4 border-t border-zinc-800 text-center">
            <div>
              <p className="text-lg font-bold text-zinc-100">{wp.applicationsDone} / {wp.applicationsTarget}</p>
              <p className="text-xs text-zinc-600">Applications</p>
            </div>
            <div>
              <p className="text-lg font-bold text-zinc-100">{wp.recruiterMessagesDone} / {wp.recruiterMessagesTarget}</p>
              <p className="text-xs text-zinc-600">Recruiter DMs</p>
            </div>
            <div>
              <p className="text-lg font-bold text-zinc-100">{wp.managerMessagesDone} / {wp.managerMessagesTarget}</p>
              <p className="text-xs text-zinc-600">Manager DMs</p>
            </div>
            <div>
              <p className="text-lg font-bold text-zinc-100">{wp.referralRequestsDone} / {wp.referralRequestsTarget}</p>
              <p className="text-xs text-zinc-600">Referrals</p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
