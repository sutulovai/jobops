import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus } from 'lucide-react'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { RecommendationBadge } from '@/components/common/StatusBadge'
import { FitScoreBar } from '@/components/common/FitScoreBar'
import type { Vacancy, JobAnalysis } from '@/types'

export function VacancyDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: vacancy, isLoading } = useQuery<Vacancy>({
    queryKey: ['vacancies', id],
    queryFn: () => apiClient.get(`/vacancies/${id}`).then(r => r.data),
  })

  const { data: analysis } = useQuery<JobAnalysis>({
    queryKey: ['vacancies', id, 'analysis'],
    queryFn: () => apiClient.get(`/vacancies/${id}/analysis`).then(r => r.data),
    enabled: !!id,
  })

  const addToPipelineMutation = useMutation({
    mutationFn: () => apiClient.post('/applications', { vacancyId: id }).then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] })
      toast.success('Added to pipeline')
      navigate('/pipeline')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  if (isLoading) return <LoadingState message="Loading vacancy…" className="mt-20" />
  if (!vacancy) return null

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <div className="flex items-start justify-between mb-5">
        <div>
          <div className="flex items-center gap-3 flex-wrap">
            <h1 className="text-xl font-bold text-zinc-100">{vacancy.title}</h1>
            {vacancy.aiRecommendation && <RecommendationBadge rec={vacancy.aiRecommendation} />}
          </div>
          <p className="text-sm text-zinc-500 mt-0.5">
            {vacancy.companyName}
            {vacancy.location && ` · ${vacancy.location}`}
          </p>
        </div>
        {vacancy.status !== 'ADDED_TO_PIPELINE' && vacancy.status !== 'APPLIED' && analysis?.recommendation !== 'SKIP' && (
          <button
            onClick={() => addToPipelineMutation.mutate()}
            disabled={addToPipelineMutation.isPending}
            className="flex items-center gap-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-500 disabled:bg-zinc-800 text-white font-bold text-sm rounded-lg transition-colors"
          >
            <Plus className="h-4 w-4" />
            Add to Pipeline
          </button>
        )}
      </div>

      {/* Analysis */}
      {analysis && (
        <div className="space-y-4 mb-5">
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
            <p className="text-xs font-semibold text-zinc-400 mb-3">AI Analysis</p>
            <FitScoreBar score={analysis.fitScore} label="Overall Fit" />
            {analysis.summary && (
              <p className="text-xs text-zinc-400 mt-3 leading-relaxed">{analysis.summary}</p>
            )}
          </div>

          {/* Dimension breakdown */}
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
            <p className="text-xs font-semibold text-zinc-400 mb-3">Fit Breakdown</p>
            <div className="space-y-2">
              {([
                ['Stack', analysis.stackFit],
                ['Domain', analysis.domainFit],
                ['Location', analysis.locationFit],
                ['Language', analysis.languageFit],
                ['Seniority', analysis.seniorityFit],
                ['Company type', analysis.companyTypeFit],
              ] as [string, number | undefined][]).filter(([, v]) => v != null).map(([label, score]) => (
                <FitScoreBar key={label} score={score!} label={label} />
              ))}
            </div>
          </div>

          {/* Hard blockers */}
          {analysis.hardBlockers?.length > 0 && (
            <div className="bg-red-500/5 border border-red-500/20 rounded-xl p-4">
              <p className="text-xs font-semibold text-red-400 mb-2">Hard Blockers</p>
              {analysis.hardBlockers.map((b, i) => (
                <p key={i} className="text-xs text-red-400">• {b}</p>
              ))}
            </div>
          )}

          {/* Suggested message */}
          {analysis.suggestedFirstMessage && (
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
              <p className="text-xs font-semibold text-zinc-400 mb-2">Suggested First Message</p>
              <p className="text-xs text-zinc-300 font-mono leading-relaxed whitespace-pre-wrap">
                {analysis.suggestedFirstMessage}
              </p>
            </div>
          )}
        </div>
      )}

      {/* JD Text */}
      {vacancy.jobDescriptionText && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
          <p className="text-xs font-semibold text-zinc-400 mb-2">Job Description</p>
          <p className="text-xs text-zinc-500 leading-relaxed whitespace-pre-wrap max-h-96 overflow-y-auto">
            {vacancy.jobDescriptionText}
          </p>
        </div>
      )}
    </div>
  )
}
