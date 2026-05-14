import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { CheckCircle, Search, ExternalLink } from 'lucide-react'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { EmptyState } from '@/components/common/EmptyState'
import { formatRelative, cn } from '@/lib/utils'
import type { SavedSearch } from '@/types'

const PLATFORM_COLORS: Record<string, string> = {
  LINKEDIN: 'bg-blue-500/10 text-blue-400',
  GERMAN_TECH_JOBS: 'bg-emerald-500/10 text-emerald-400',
  WE_ARE_DEVELOPERS: 'bg-violet-500/10 text-violet-400',
  ARBEITNOW: 'bg-orange-500/10 text-orange-400',
  STEPSTONE: 'bg-amber-500/10 text-amber-400',
  RELOCATE_ME: 'bg-cyan-500/10 text-cyan-400',
  BERLIN_STARTUP_JOBS: 'bg-pink-500/10 text-pink-400',
}

export function SearchesPage() {
  const queryClient = useQueryClient()

  const { data: searches, isLoading } = useQuery<SavedSearch[]>({
    queryKey: ['searches'],
    queryFn: () => apiClient.get('/searches').then(r => r.data),
  })

  const checkMutation = useMutation({
    mutationFn: (id: string) => apiClient.post(`/searches/${id}/checked`).then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['searches'] })
      toast.success('Search marked as checked')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  if (isLoading) return <LoadingState message="Loading saved searches…" className="mt-20" />

  const overdue = searches?.filter(s => {
    if (!s.lastCheckedAt) return true
    const hours = s.frequency === 'DAILY' ? 24 : 168
    const diff = Date.now() - new Date(s.lastCheckedAt).getTime()
    return diff > hours * 3600000
  }) ?? []

  const notOverdue = searches?.filter(s => !overdue.includes(s)) ?? []

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Saved Searches</h1>
        <p className="text-sm text-zinc-500 mt-0.5">{searches?.length ?? 0} searches tracked</p>
      </div>

      {!searches?.length ? (
        <EmptyState icon={Search} title="No searches yet" />
      ) : (
        <div className="space-y-6">
          {overdue.length > 0 && (
            <section>
              <h2 className="text-xs font-bold text-red-400 uppercase tracking-widest mb-3 px-1">
                Needs Checking — {overdue.length}
              </h2>
              <div className="space-y-2">
                {overdue.map(s => (
                  <SearchCard key={s.id} search={s} onCheck={() => checkMutation.mutate(s.id)} overdue />
                ))}
              </div>
            </section>
          )}
          {notOverdue.length > 0 && (
            <section>
              <h2 className="text-xs font-bold text-zinc-500 uppercase tracking-widest mb-3 px-1">
                Up to Date — {notOverdue.length}
              </h2>
              <div className="space-y-2">
                {notOverdue.map(s => (
                  <SearchCard key={s.id} search={s} onCheck={() => checkMutation.mutate(s.id)} />
                ))}
              </div>
            </section>
          )}
        </div>
      )}
    </div>
  )
}

function SearchCard({ search: s, onCheck, overdue }: { search: SavedSearch; onCheck: () => void; overdue?: boolean }) {
  return (
    <div className={cn(
      'bg-zinc-900 border rounded-xl p-4',
      overdue ? 'border-red-500/25 bg-red-500/5' : 'border-zinc-800',
    )}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <p className="text-sm font-medium text-zinc-200">{s.title}</p>
            {s.platform && (
              <span className={cn(
                'text-xs rounded px-1.5 py-0.5',
                PLATFORM_COLORS[s.platform] ?? 'bg-zinc-800 text-zinc-500',
              )}>
                {s.platform.replace(/_/g, ' ')}
              </span>
            )}
            <span className="text-xs text-zinc-600">{s.frequency}</span>
            {s.city && <span className="text-xs text-zinc-600">{s.city}</span>}
          </div>

          {s.queryText && (
            <p className="text-xs text-zinc-500 mt-1 font-mono truncate">{s.queryText}</p>
          )}

          <div className="flex items-center gap-4 mt-2 text-xs text-zinc-600">
            <span>{s.jobsAdded ?? 0} added</span>
            <span>{s.applicationsCreated ?? 0} applied</span>
            {s.responseRate != null && (
              <span className={s.responseRate >= 10 ? 'text-emerald-500' : 'text-zinc-500'}>
                {s.responseRate.toFixed(0)}% response
              </span>
            )}
            {s.lastCheckedAt && (
              <span>checked {formatRelative(s.lastCheckedAt)}</span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2 flex-shrink-0">
          {s.url && (
            <a
              href={s.url}
              target="_blank"
              rel="noopener"
              className="text-zinc-600 hover:text-zinc-400"
            >
              <ExternalLink className="h-4 w-4" />
            </a>
          )}
          <button
            onClick={onCheck}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs rounded-lg transition-colors"
          >
            <CheckCircle className="h-3.5 w-3.5" />
            Mark Checked
          </button>
        </div>
      </div>
    </div>
  )
}
