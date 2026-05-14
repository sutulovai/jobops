import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Search, CheckCircle2, PlusCircle } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { apiClient, getErrorMessage } from '@/app/api'
import type { AnalyzeJobResponse } from '@/types'
import { RecommendationBadge } from '@/components/common/StatusBadge'
import { FitScoreBar } from '@/components/common/FitScoreBar'
import { CopyButton } from '@/components/common/CopyButton'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { cn } from '@/lib/utils'

const schema = z.object({
  jobDescription: z.string().min(100, 'Paste the full job description (min 100 chars)'),
  jobUrl: z.string().optional(),
  companyName: z.string().optional(),
  roleTitle: z.string().optional(),
  location: z.string().optional(),
  sourceChannel: z.string().optional(),
  salaryInfo: z.string().optional(),
  languageRequirement: z.string().optional(),
  relocationWording: z.string().optional(),
  personalNote: z.string().optional(),
})

type FormData = z.infer<typeof schema>

export function AnalyzePage() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [result, setResult] = useState<AnalyzeJobResponse | null>(null)
  const [addedTopipeline, setAddedTopipeline] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const analyzeMutation = useMutation({
    mutationFn: (data: FormData) => apiClient.post<AnalyzeJobResponse>('/vacancies/analyze', data).then(r => r.data),
    onSuccess: (data) => {
      setResult(data)
      setAddedTopipeline(false)
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      toast.success(`Analysis complete — ${data.analysis.recommendation}`)
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const addToPipelineMutation = useMutation({
    mutationFn: () => apiClient.post('/applications', { vacancyId: result!.vacancyId }).then(r => r.data),
    onSuccess: () => {
      setAddedTopipeline(true)
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      queryClient.invalidateQueries({ queryKey: ['applications'] })
      toast.success('Added to pipeline — mark as Applied when you submit.')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const onSubmit = (data: FormData) => analyzeMutation.mutate(data)

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Analyze Job Description</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Paste a JD — OpenAI evaluates fit against your Germany job search strategy.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Input form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-zinc-400 mb-1.5">Job Description *</label>
            <textarea
              {...register('jobDescription')}
              placeholder="Paste the full job description here…"
              rows={14}
              className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2.5 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500 transition-colors resize-none font-mono"
            />
            {errors.jobDescription && <p className="text-xs text-red-400 mt-1">{errors.jobDescription.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5">Company name</label>
              <input
                {...register('companyName')}
                placeholder="Scalable Capital"
                className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5">Role title</label>
              <input
                {...register('roleTitle')}
                placeholder="Senior Backend Engineer"
                className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5">Location</label>
              <input
                {...register('location')}
                placeholder="Munich / Remote"
                className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5">Source</label>
              <input
                {...register('sourceChannel')}
                placeholder="LinkedIn / Company website"
                className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5">Visible salary</label>
              <input
                {...register('salaryInfo')}
                placeholder="€80k–100k"
                className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5">Language requirement</label>
              <input
                {...register('languageRequirement')}
                placeholder="English required, German B2"
                className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-zinc-400 mb-1.5">Relocation / visa wording (if any)</label>
            <input
              {...register('relocationWording')}
              placeholder="Relocation support provided, visa sponsorship available"
              className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-zinc-400 mb-1.5">Job URL</label>
            <input
              {...register('jobUrl')}
              placeholder="https://…"
              className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
            />
          </div>

          <button
            type="submit"
            disabled={analyzeMutation.isPending}
            className="w-full bg-amber-500 hover:bg-amber-400 disabled:bg-zinc-800 disabled:text-zinc-600 text-zinc-950 font-bold text-sm py-2.5 rounded-lg transition-colors flex items-center justify-center gap-2"
          >
            {analyzeMutation.isPending ? (
              <><LoadingSpinner size="sm" /> Analyzing with OpenAI…</>
            ) : (
              <><Search className="h-4 w-4" /> Analyze Job Fit</>
            )}
          </button>
        </form>

        {/* Results */}
        <div>
          {!result ? (
            <div className="flex flex-col items-center justify-center h-full min-h-[400px] text-center text-zinc-700 gap-3">
              <Search className="h-10 w-10" />
              <p className="text-sm">Paste a job description and click Analyze</p>
              <p className="text-xs text-zinc-800">OpenAI scores fit against your Germany strategy</p>
            </div>
          ) : (
            <AnalysisResult
              result={result}
              addedToPipeline={addedTopipeline}
              isAdding={addToPipelineMutation.isPending}
              onAddToPipeline={() => addToPipelineMutation.mutate()}
              onGoToPipeline={() => navigate('/pipeline')}
            />
          )}
        </div>
      </div>
    </div>
  )
}

function AnalysisResult({
  result, addedToPipeline, isAdding, onAddToPipeline, onGoToPipeline,
}: {
  result: AnalyzeJobResponse
  addedToPipeline: boolean
  isAdding: boolean
  onAddToPipeline: () => void
  onGoToPipeline: () => void
}) {
  const queryClient = useQueryClient()
  const logSuggestedCopy = useMutation({
    mutationFn: (body: Record<string, unknown>) => apiClient.post('/messages/log-copy', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messages'] })
      toast.success('Copied — logged in Messages')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const { analysis, vacancy } = result

  return (
    <div className="space-y-4">
      {/* Top decision */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
        <div className="flex items-start justify-between mb-3">
          <div>
            <p className="text-base font-semibold text-zinc-100">{vacancy.title}</p>
            {vacancy.location && <p className="text-xs text-zinc-500 mt-0.5">{vacancy.location}</p>}
          </div>
          <RecommendationBadge rec={analysis.recommendation as any} />
        </div>

        <FitScoreBar score={analysis.fitScore} label="Overall Fit" />

        <p className="text-xs text-zinc-400 mt-3 leading-relaxed">{analysis.summary}</p>

        {analysis.hardBlockers && analysis.hardBlockers.length > 0 && (
          <div className="mt-3 p-2 bg-red-500/10 border border-red-500/20 rounded-lg">
            <p className="text-xs font-medium text-red-400 mb-1">Hard blockers:</p>
            {analysis.hardBlockers.map((b, i) => (
              <p key={i} className="text-xs text-red-400">• {b}</p>
            ))}
          </div>
        )}
      </div>

      {/* Dimension scores */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
        <p className="text-xs font-semibold text-zinc-400 mb-3">Fit Breakdown</p>
        <div className="space-y-2">
          {[
            { label: 'Stack', score: analysis.stackFit },
            { label: 'Domain', score: analysis.domainFit },
            { label: 'Location', score: analysis.locationFit },
            { label: 'Language', score: analysis.languageFit },
            { label: 'Seniority', score: analysis.seniorityFit },
            { label: 'Company type', score: analysis.companyTypeFit },
          ].filter(d => d.score != null).map((d) => (
            <FitScoreBar key={d.label} score={d.score!} label={d.label} />
          ))}
        </div>
      </div>

      {/* Risk flags */}
      <div className="grid grid-cols-3 gap-2">
        <RiskPill label="Language" risk={analysis.germanRequirement === 'REQUIRED' ? 'HIGH' : analysis.languageFit != null && analysis.languageFit < 50 ? 'MEDIUM' : 'LOW'} />
        <RiskPill label="Relocation" risk={analysis.relocationRisk ?? 'UNCERTAIN'} />
        <RiskPill label="Salary" risk={analysis.salaryRisk ?? 'UNCERTAIN'} />
      </div>

      {/* Suggested message */}
      {analysis.suggestedFirstMessage && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
          <div className="flex items-center justify-between mb-2">
            <p className="text-xs font-semibold text-zinc-400">Suggested first message</p>
            <CopyButton
              text={analysis.suggestedFirstMessage}
              size="sm"
              onCopied={() =>
                logSuggestedCopy.mutate({
                  messageType: 'LINKEDIN_RECRUITER_DM',
                  bodyText: analysis.suggestedFirstMessage!,
                  companyId: result.companyId,
                  vacancyId: result.vacancyId,
                  channel: 'LINKEDIN',
                })
              }
            />
          </div>
          <p className="text-xs text-zinc-300 leading-relaxed font-mono whitespace-pre-wrap">
            {analysis.suggestedFirstMessage}
          </p>
        </div>
      )}

      {/* Reasons */}
      {analysis.reasonsToApply && analysis.reasonsToApply.length > 0 && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
          <p className="text-xs font-semibold text-emerald-400 mb-2">Reasons to apply</p>
          {analysis.reasonsToApply.slice(0, 4).map((r, i) => (
            <p key={i} className="text-xs text-zinc-400">✓ {r}</p>
          ))}
        </div>
      )}

      {analysis.reasonsToSkip && analysis.reasonsToSkip.length > 0 && (
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
          <p className="text-xs font-semibold text-red-400 mb-2">Reasons to skip / watch</p>
          {analysis.reasonsToSkip.slice(0, 3).map((r, i) => (
            <p key={i} className="text-xs text-zinc-400">✗ {r}</p>
          ))}
        </div>
      )}

      {/* Pipeline CTA */}
      {analysis.recommendation !== 'SKIP' && (
        <div className="space-y-2">
          {!addedToPipeline ? (
            <button
              onClick={onAddToPipeline}
              disabled={isAdding}
              className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 disabled:bg-zinc-800 disabled:text-zinc-600 text-white font-bold text-sm py-3 rounded-lg transition-colors"
            >
              {isAdding
                ? <><LoadingSpinner size="sm" /> Adding…</>
                : <><PlusCircle className="h-4 w-4" /> Track this Job (Add to Pipeline)</>
              }
            </button>
          ) : (
            <div className="space-y-2">
              <div className="flex items-center gap-2 justify-center text-emerald-400 text-sm font-medium py-2">
                <CheckCircle2 className="h-4 w-4" />
                Added to pipeline
              </div>
              <button
                onClick={onGoToPipeline}
                className="w-full flex items-center justify-center gap-2 bg-amber-500 hover:bg-amber-400 text-zinc-950 font-bold text-sm py-3 rounded-lg transition-colors"
              >
                Go to Pipeline — Mark as Applied when you submit →
              </button>
            </div>
          )}
          {!addedToPipeline && (
            <p className="text-xs text-zinc-600 text-center">
              After tracking, go to Pipeline and click <strong className="text-zinc-500">Mark Applied</strong> once you've submitted.
            </p>
          )}
        </div>
      )}
    </div>
  )
}

function RiskPill({ label, risk }: { label: string; risk: string }) {
  const colors: Record<string, string> = {
    LOW: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
    MEDIUM: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
    HIGH: 'bg-red-500/15 text-red-400 border-red-500/30',
    BLOCKER: 'bg-red-600/25 text-red-300 border-red-600/50',
    UNCERTAIN: 'bg-zinc-700/40 text-zinc-500 border-zinc-700/50',
    UNKNOWN: 'bg-zinc-700/40 text-zinc-500 border-zinc-700/50',
    REQUIRED: 'bg-red-500/15 text-red-400 border-red-500/30',
    OPTIONAL: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
    NONE: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
    PREFERRED: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
  }
  const color = colors[risk] ?? colors['UNKNOWN']
  return (
    <div className={cn('rounded-lg border p-2 text-center', color)}>
      <p className="text-xs font-medium">{label}</p>
      <p className="text-xs mt-0.5 font-mono">{risk}</p>
    </div>
  )
}
