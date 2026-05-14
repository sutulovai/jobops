import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { RefreshCw, TrendingUp, TrendingDown, AlertTriangle, CheckCircle } from 'lucide-react'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState, LoadingSpinner } from '@/components/common/LoadingSpinner'
import { cn, formatDate } from '@/lib/utils'
import type { WeeklyReview } from '@/types'

function MetricRow({ label, value, sub, good }: { label: string; value: string | number; sub?: string; good?: boolean }) {
  return (
    <div className="flex items-center justify-between py-2 border-b border-zinc-800 last:border-0">
      <span className="text-sm text-zinc-400">{label}</span>
      <div className="text-right">
        <span className={cn('text-sm font-mono font-bold',
          good === undefined ? 'text-zinc-200' : good ? 'text-emerald-400' : 'text-amber-400',
        )}>
          {value}
        </span>
        {sub && <p className="text-xs text-zinc-600">{sub}</p>}
      </div>
    </div>
  )
}

function Section({ icon: Icon, title, children, color }: {
  icon: React.ElementType; title: string; children: React.ReactNode; color?: string
}) {
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
      <div className="flex items-center gap-2 mb-4">
        <Icon className={cn('h-4 w-4', color ?? 'text-zinc-400')} />
        <h3 className="text-sm font-bold text-zinc-300">{title}</h3>
      </div>
      {children}
    </div>
  )
}

export function WeeklyReviewPage() {
  const queryClient = useQueryClient()

  const { data: review, isLoading } = useQuery<WeeklyReview>({
    queryKey: ['weekly-review', 'latest'],
    queryFn: () => apiClient.get('/weekly-review/latest').then(r => r.data),
  })

  const generateMutation = useMutation({
    mutationFn: () => apiClient.post('/weekly-review/generate').then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['weekly-review'] })
      toast.success('Weekly review generated')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  if (isLoading) return <LoadingState message="Loading weekly review…" className="mt-20" />

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Weekly Review</h1>
          {review && (
            <p className="text-sm text-zinc-500 mt-0.5">
              Week of {formatDate(review.weekStart)} — {formatDate(review.weekEnd)}
            </p>
          )}
        </div>
        <button
          onClick={() => generateMutation.mutate()}
          disabled={generateMutation.isPending}
          className="flex items-center gap-1.5 px-4 py-2 bg-amber-500 hover:bg-amber-400 disabled:bg-zinc-800 disabled:text-zinc-600 text-zinc-950 font-bold text-sm rounded-lg transition-colors"
        >
          {generateMutation.isPending
            ? <><LoadingSpinner size="sm" /> Generating…</>
            : <><RefreshCw className="h-4 w-4" /> Generate Review</>
          }
        </button>
      </div>

      {!review ? (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 text-center">
          <RefreshCw className="h-8 w-8 text-zinc-700 mx-auto mb-3" />
          <p className="text-sm font-medium text-zinc-400 mb-1">No review yet</p>
          <p className="text-xs text-zinc-600">Generate your first weekly review to get funnel diagnostics and strategic recommendations.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {/* Summary */}
          {review.aiSummary && (
            <div className="bg-amber-500/5 border border-amber-500/20 rounded-xl p-5">
              <p className="text-xs font-bold text-amber-400 uppercase tracking-widest mb-2">Summary</p>
              <p className="text-sm text-zinc-300 leading-relaxed">{review.aiSummary}</p>
            </div>
          )}

          {/* Activity metrics */}
          <Section icon={TrendingUp} title="Activity This Week" color="text-blue-400">
            <MetricRow label="Jobs analyzed" value={review.jobsAnalyzed ?? 0} />
            <MetricRow label="Applications" value={review.directApplications ?? 0} sub={`target: ${review.applicationsTarget ?? '—'}`} good={(review.directApplications ?? 0) >= (review.applicationsTarget ?? 999)} />
            <MetricRow label="Recruiter messages" value={review.recruiterMessages ?? 0} />
            <MetricRow label="Manager messages" value={review.managerMessages ?? 0} />
            <MetricRow label="Referral requests" value={review.referralRequests ?? 0} />
            <MetricRow label="Follow-ups sent" value={review.followUpsSent ?? 0} />
            <div className="flex gap-4 mt-3 pt-3 border-t border-zinc-800 text-xs text-zinc-600">
              {review.applyCount != null && <span>APPLY: {review.applyCount}</span>}
              {review.maybeCount != null && <span>MAYBE: {review.maybeCount}</span>}
              {review.skipCount != null && <span>SKIP: {review.skipCount}</span>}
            </div>
          </Section>

          {/* Response rates */}
          <Section icon={TrendingUp} title="Response Rates" color="text-emerald-400">
            <MetricRow
              label="Direct apply response rate"
              value={`${(review.directApplyResponseRate ?? 0).toFixed(1)}%`}
              sub="target: 10-20%"
              good={(review.directApplyResponseRate ?? 0) >= 8}
            />
            <MetricRow
              label="Warm outreach response rate"
              value={`${(review.warmOutreachResponseRate ?? 0).toFixed(1)}%`}
              sub="target: 25-40%"
              good={(review.warmOutreachResponseRate ?? 0) >= 20}
            />
            <MetricRow
              label="Recruiter screen rate"
              value={`${(review.recruiterScreenRate ?? 0).toFixed(1)}%`}
              sub="target: 12-20%"
              good={(review.recruiterScreenRate ?? 0) >= 10}
            />
            <MetricRow
              label="Tech interview rate"
              value={`${(review.techInterviewRate ?? 0).toFixed(1)}%`}
              sub="target: 40%+ from screens"
              good={(review.techInterviewRate ?? 0) >= 30}
            />
          </Section>

          {/* Blockers */}
          {((review.salaryBlockerCount ?? 0) + (review.languageBlockerCount ?? 0) + (review.relocationBlockerCount ?? 0)) > 0 && (
            <Section icon={AlertTriangle} title="Blockers Detected" color="text-red-400">
              {review.salaryBlockerCount != null && review.salaryBlockerCount > 0 && (
                <MetricRow label="Salary blockers (SKIP)" value={review.salaryBlockerCount} good={false} />
              )}
              {review.languageBlockerCount != null && review.languageBlockerCount > 0 && (
                <MetricRow label="Language blockers (C1 German)" value={review.languageBlockerCount} good={false} />
              )}
              {review.relocationBlockerCount != null && review.relocationBlockerCount > 0 && (
                <MetricRow label="Relocation blockers" value={review.relocationBlockerCount} good={false} />
              )}
            </Section>
          )}

          {/* Stale / ghosted */}
          {((review.staleApplications ?? 0) + (review.ghostedApplications ?? 0)) > 0 && (
            <Section icon={AlertTriangle} title="Pipeline Health" color="text-amber-400">
              <MetricRow label="Stale applications" value={review.staleApplications ?? 0} />
              <MetricRow label="Ghosted applications" value={review.ghostedApplications ?? 0} />
            </Section>
          )}

          {/* AI recommendations */}
          {review.aiRecommendations && (
            <Section icon={CheckCircle} title="Recommendations" color="text-violet-400">
              <p className="text-sm text-zinc-300 leading-relaxed whitespace-pre-wrap">{review.aiRecommendations}</p>
            </Section>
          )}

          {/* Next week targets */}
          {review.nextWeekTargets && (
            <Section icon={TrendingUp} title="Next Week Targets" color="text-amber-400">
              <p className="text-sm text-zinc-300 leading-relaxed whitespace-pre-wrap">{review.nextWeekTargets}</p>
            </Section>
          )}

          {/* What worked / didn't */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {review.whatWorked && (
              <Section icon={TrendingUp} title="What Worked" color="text-emerald-400">
                <p className="text-sm text-zinc-300 leading-relaxed whitespace-pre-wrap">{review.whatWorked}</p>
              </Section>
            )}
            {review.whatDidntWork && (
              <Section icon={TrendingDown} title="What Didn't Work" color="text-red-400">
                <p className="text-sm text-zinc-300 leading-relaxed whitespace-pre-wrap">{review.whatDidntWork}</p>
              </Section>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
