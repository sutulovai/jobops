import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { FileText, Search } from 'lucide-react'
import { apiClient } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { EmptyState } from '@/components/common/EmptyState'
import { RecommendationBadge } from '@/components/common/StatusBadge'
import { FitScoreBar } from '@/components/common/FitScoreBar'
import { formatRelative, cn } from '@/lib/utils'
import type { Vacancy } from '@/types'

const STATUS_COLORS: Record<string, string> = {
  DISCOVERED: 'text-zinc-500',
  ANALYZING: 'text-amber-400',
  SHOULD_APPLY: 'text-emerald-400',
  MAYBE: 'text-blue-400',
  SKIP: 'text-zinc-600',
  ADDED_TO_PIPELINE: 'text-violet-400',
  APPLIED: 'text-emerald-400',
}

export function VacanciesPage() {
  const [search, setSearch] = useState('')
  const [filterRec, setFilterRec] = useState<string>('ALL')

  const { data: vacancies, isLoading } = useQuery<Vacancy[]>({
    queryKey: ['vacancies'],
    queryFn: () => apiClient.get('/vacancies').then(r => r.data),
  })

  const filtered = vacancies?.filter(v => {
    const q = search.toLowerCase()
    const matchSearch = !q || v.title.toLowerCase().includes(q) || v.companyName?.toLowerCase().includes(q) || false
    const matchRec = filterRec === 'ALL' || v.aiRecommendation === filterRec
    return matchSearch && matchRec
  }) ?? []

  if (isLoading) return <LoadingState message="Loading vacancies…" className="mt-20" />

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Vacancies</h1>
        <p className="text-sm text-zinc-500 mt-0.5">{vacancies?.length ?? 0} jobs tracked</p>
      </div>

      <div className="flex items-center gap-3 mb-5">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-zinc-600" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search vacancies…"
            className="w-full bg-zinc-900 border border-zinc-800 rounded-lg pl-8 pr-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
          />
        </div>
        {(['ALL', 'APPLY', 'MAYBE', 'SKIP'] as const).map((r) => (
          <button
            key={r}
            onClick={() => setFilterRec(r)}
            className={cn(
              'px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors',
              filterRec === r
                ? 'bg-amber-500/20 text-amber-400 border-amber-500/40'
                : 'bg-zinc-900 text-zinc-500 border-zinc-800 hover:border-zinc-700',
            )}
          >
            {r}
          </button>
        ))}
      </div>

      {!filtered.length ? (
        <EmptyState icon={FileText} title="No vacancies" description="Analyze a job to add it here." />
      ) : (
        <div className="space-y-2">
          {filtered.map(v => (
            <Link
              key={v.id}
              to={`/vacancies/${v.id}`}
              className="flex items-center gap-4 bg-zinc-900 border border-zinc-800 rounded-xl p-4 hover:border-zinc-700 transition-colors group"
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-medium text-zinc-100 group-hover:text-amber-400 transition-colors">{v.title}</p>
                  {v.aiRecommendation && <RecommendationBadge rec={v.aiRecommendation} />}
                </div>
                <div className="flex items-center gap-2 mt-0.5 text-xs text-zinc-500">
                  <span>{v.companyName}</span>
                  {v.location && <><span>·</span><span>{v.location}</span></>}
                  <span>·</span>
                  <span>{formatRelative(v.createdAt)}</span>
                </div>
              </div>
              {v.aiFitScore != null && (
                <div className="flex-shrink-0 w-20">
                  <FitScoreBar score={v.aiFitScore} compact />
                </div>
              )}
              <span className={cn('text-xs flex-shrink-0', STATUS_COLORS[v.status] ?? 'text-zinc-500')}>
                {v.status.replace(/_/g, ' ')}
              </span>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
