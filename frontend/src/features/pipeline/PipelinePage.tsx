import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { EmptyState } from '@/components/common/EmptyState'
import { CopyButton } from '@/components/common/CopyButton'
import { cn } from '@/lib/utils'
import { generateMessageForStage, STAGE_MESSAGE_LABELS, messageTypeForPipelineStage } from '@/lib/messageTemplates'
import type { Application, Company } from '@/types'
import { Kanban } from 'lucide-react'

const TERMINAL = ['REJECTED', 'GHOSTED', 'WITHDRAWN', 'ARCHIVED', 'OFFER']

const SECTIONS = [
  {
    label: 'To Apply',
    color: 'text-amber-400',
    statuses: ['ADDED_TO_PIPELINE'],
  },
  {
    label: 'In Progress',
    color: 'text-blue-400',
    statuses: ['APPLIED', 'RECRUITER_CONTACTED', 'REFERRAL_REQUESTED', 'HIRING_MANAGER_CONTACTED'],
  },
  {
    label: 'Interviews',
    color: 'text-emerald-400',
    statuses: [
      'RECRUITER_SCREEN_SCHEDULED', 'RECRUITER_SCREEN_DONE',
      'TECHNICAL_INTERVIEW_SCHEDULED', 'TECHNICAL_INTERVIEW_DONE', 'FINAL_INTERVIEW',
    ],
  },
  {
    label: 'Closed',
    color: 'text-zinc-500',
    statuses: ['OFFER', 'REJECTED', 'GHOSTED', 'WITHDRAWN', 'ARCHIVED'],
  },
]

const NEXT_STAGES: Record<string, { stages: string[]; primary?: string }> = {
  ADDED_TO_PIPELINE:              { stages: ['APPLIED', 'ARCHIVED'],                                                primary: 'APPLIED' },
  APPLIED:                        { stages: ['RECRUITER_CONTACTED', 'HIRING_MANAGER_CONTACTED', 'RECRUITER_SCREEN_SCHEDULED', 'REJECTED', 'GHOSTED'], primary: 'RECRUITER_CONTACTED' },
  RECRUITER_CONTACTED:            { stages: ['RECRUITER_SCREEN_SCHEDULED', 'REJECTED', 'GHOSTED'],                 primary: 'RECRUITER_SCREEN_SCHEDULED' },
  REFERRAL_REQUESTED:             { stages: ['RECRUITER_SCREEN_SCHEDULED', 'REJECTED', 'GHOSTED'],                 primary: 'RECRUITER_SCREEN_SCHEDULED' },
  HIRING_MANAGER_CONTACTED:       { stages: ['RECRUITER_SCREEN_SCHEDULED', 'REJECTED', 'GHOSTED'],                 primary: 'RECRUITER_SCREEN_SCHEDULED' },
  RECRUITER_SCREEN_SCHEDULED:     { stages: ['RECRUITER_SCREEN_DONE', 'REJECTED'],                                 primary: 'RECRUITER_SCREEN_DONE' },
  RECRUITER_SCREEN_DONE:          { stages: ['TECHNICAL_INTERVIEW_SCHEDULED', 'FINAL_INTERVIEW', 'OFFER', 'REJECTED'], primary: 'TECHNICAL_INTERVIEW_SCHEDULED' },
  TECHNICAL_INTERVIEW_SCHEDULED:  { stages: ['TECHNICAL_INTERVIEW_DONE', 'REJECTED'],                              primary: 'TECHNICAL_INTERVIEW_DONE' },
  TECHNICAL_INTERVIEW_DONE:       { stages: ['FINAL_INTERVIEW', 'OFFER', 'REJECTED'],                              primary: 'FINAL_INTERVIEW' },
  FINAL_INTERVIEW:                { stages: ['OFFER', 'REJECTED'],                                                 primary: 'OFFER' },
}

const STAGE_LABELS: Record<string, string> = {
  APPLIED:                      '✓ Mark Applied',
  RECRUITER_CONTACTED:          'Recruiter Contacted',
  HIRING_MANAGER_CONTACTED:     'HM Contacted',
  REFERRAL_REQUESTED:           'Referral Sent',
  RECRUITER_SCREEN_SCHEDULED:   'Screen Scheduled',
  RECRUITER_SCREEN_DONE:        'Screen Done',
  TECHNICAL_INTERVIEW_SCHEDULED:'Tech Interview Scheduled',
  TECHNICAL_INTERVIEW_DONE:     'Tech Interview Done',
  FINAL_INTERVIEW:              'Final Interview',
  OFFER:                        '🎉 Got Offer',
  REJECTED:                     '✗ Rejected',
  GHOSTED:                      '👻 Ghosted',
  WITHDRAWN:                    'Withdrew',
  ARCHIVED:                     'Archive',
}

export function PipelinePage() {
  const queryClient = useQueryClient()
  const [expanded, setExpanded] = useState<string | null>(null)

  const { data: applications, isLoading } = useQuery<Application[]>({
    queryKey: ['applications'],
    queryFn: () => apiClient.get('/applications').then((r) => r.data),
  })

  const { data: companies } = useQuery<Company[]>({
    queryKey: ['companies'],
    queryFn: () => apiClient.get('/companies').then((r) => r.data),
  })

  const advanceMutation = useMutation({
    mutationFn: ({ id, stage }: { id: string; stage: string }) =>
      apiClient.post(`/applications/${id}/advance`, { stage }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      toast.success('Stage updated')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  if (isLoading) return <LoadingState message="Loading pipeline…" className="mt-20" />

  const companiesById = new Map((companies ?? []).map((c) => [c.id, c]))

  if (!applications?.length) {
    return (
      <div className="p-6 max-w-[900px]">
        <h1 className="text-xl font-bold text-zinc-100 mb-6">Pipeline</h1>
        <EmptyState
          icon={Kanban}
          title="Pipeline is empty"
          description="Analyze a job description and add it to the pipeline to get started."
        />
      </div>
    )
  }

  const activeCount = applications.filter(
    (a) => !['REJECTED', 'GHOSTED', 'WITHDRAWN', 'ARCHIVED'].includes(a.stage)
  ).length

  return (
    <div className="p-6 max-w-[900px]">
      <div className="mb-6">
        <h1 className="text-xl font-bold text-zinc-100">Pipeline</h1>
        <p className="text-[13px] text-zinc-500 mt-0.5">{activeCount} active applications</p>
      </div>

      <div className="space-y-7">
        {SECTIONS.map((section) => {
          const sectionApps = applications.filter((a) => section.statuses.includes(a.stage))
          if (!sectionApps.length) return null
          return (
            <div key={section.label}>
              <div className="flex items-center gap-2 mb-3.5">
                <span className={cn('text-[11px] font-bold uppercase tracking-widest', section.color)}>
                  {section.label}
                </span>
                <div className="flex-1 h-px bg-zinc-800" />
                <span className="font-mono text-[11px] text-zinc-700">{sectionApps.length}</span>
              </div>
              <div className="space-y-2.5">
                {sectionApps.map((app) => {
                  const company = app.companyId ? companiesById.get(app.companyId) ?? null : null
                  return (
                    <PipelineCard
                      key={app.id}
                      app={app}
                      company={company}
                      expanded={expanded === app.id}
                      onToggle={() => setExpanded(expanded === app.id ? null : app.id)}
                      onAdvance={(stage) => advanceMutation.mutate({ id: app.id, stage })}
                      isLoading={advanceMutation.isPending}
                    />
                  )
                })}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

function PipelineCard({
  app, company, expanded, onToggle, onAdvance, isLoading,
}: {
  app: Application
  company: Company | null
  expanded: boolean
  onToggle: () => void
  onAdvance: (stage: string) => void
  isLoading: boolean
}) {
  const queryClient = useQueryClient()
  const logCopyMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => apiClient.post('/messages/log-copy', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messages'] })
      toast.success('Copied — logged in Messages')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const { primary } = NEXT_STAGES[app.stage] ?? {}
  const isTerminal = TERMINAL.includes(app.stage)

  const daysSince = app.dateApplied
    ? Math.floor((Date.now() - new Date(app.dateApplied).getTime()) / 86_400_000)
    : null

  const isOverdue = app.nextActionDate && new Date(app.nextActionDate) < new Date()

  const messageLabel = STAGE_MESSAGE_LABELS[app.stage]
  const messageText = messageLabel
    ? generateMessageForStage(
        app.stage,
        company
          ? { name: company.name, city: company.city, fitReason: company.fitReason }
          : app.companyName
          ? { name: app.companyName, city: null, fitReason: null }
          : null,
        app.vacancyTitle,
        null,
        daysSince ?? undefined,
      )
    : null

  return (
    <div className={cn(
      'bg-zinc-900 border rounded-xl',
      isOverdue ? 'border-red-500/25 bg-red-500/[0.03]' : isTerminal ? 'border-zinc-800/60 opacity-60' : 'border-zinc-800',
    )}>
      {/* Summary row — always visible, click to expand */}
      <div
        className="flex justify-between items-start p-4 cursor-pointer"
        onClick={onToggle}
      >
        <div className="flex-1 min-w-0">
          <p className="text-[14px] font-bold text-zinc-100">{app.companyName ?? 'Unknown company'}</p>
          <p className="text-xs text-zinc-600 mt-0.5 truncate">{app.vacancyTitle ?? 'Unknown role'}</p>
          <div className="flex items-center gap-3.5 mt-1.5 text-xs text-zinc-600">
            {daysSince !== null && <span>{daysSince}d ago</span>}
            <span>{app.recruiterContacted ? '✓ Recruiter' : '○ Recruiter'}</span>
            <span>{app.hiringManagerContacted ? '✓ HM' : '○ HM'}</span>
            {isOverdue && <span className="text-red-400 font-bold">OVERDUE</span>}
            {app.stale && <span className="text-amber-400">Stale</span>}
          </div>
        </div>
        <span className="text-zinc-600 text-[10px] ml-3 flex-shrink-0 mt-0.5">
          {expanded ? '▲' : '▼'}
        </span>
      </div>

      {/* Expanded: notes + message + stage controls */}
      {expanded && (
        <div className="px-4 pb-4 pt-0 border-t border-zinc-800 mt-0">
          <div className="pt-3.5">
            {app.notes && (
              <p className="text-[13px] text-zinc-400 leading-[1.6] mb-3.5">{app.notes}</p>
            )}

            {messageText && (
              <div className="mb-3.5">
                <div className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-2">
                  {app.stage === 'ADDED_TO_PIPELINE' ? 'Outreach Message' : 'Follow-Up Message'}
                </div>
                <pre className="font-mono bg-black/40 border border-zinc-800 rounded-xl px-4 py-3 text-[13px] text-zinc-300 whitespace-pre-wrap leading-[1.7] m-0">
                  {messageText}
                </pre>
                <div className="mt-2">
                  <CopyButton
                    text={messageText}
                    size="sm"
                    onCopied={() =>
                      logCopyMutation.mutate({
                        messageType: messageTypeForPipelineStage(app.stage),
                        bodyText: messageText,
                        companyId: app.companyId,
                        vacancyId: app.vacancyId,
                        applicationId: app.id,
                        channel: 'LINKEDIN',
                      })
                    }
                  />
                </div>
              </div>
            )}

            {!isTerminal && (
              <div className="flex flex-wrap gap-2">
                {primary && (
                  <button
                    onClick={() => onAdvance(primary)}
                    disabled={isLoading}
                    className={cn(
                      'text-xs font-semibold px-3 py-1.5 rounded-lg border transition-colors disabled:opacity-50',
                      primary === 'OFFER'
                        ? 'bg-amber-500/15 hover:bg-amber-500/25 text-amber-400 border-amber-500/25'
                        : 'bg-zinc-800 hover:bg-zinc-700 text-zinc-300 border-zinc-700',
                    )}
                  >
                    → {STAGE_LABELS[primary] ?? primary.replace(/_/g, ' ')}
                  </button>
                )}
                <button
                  onClick={() => onAdvance('GHOSTED')}
                  disabled={isLoading}
                  className="text-xs px-3 py-1.5 text-zinc-600 hover:text-zinc-400 transition-colors disabled:opacity-50"
                >
                  Ghost
                </button>
                <button
                  onClick={() => onAdvance('REJECTED')}
                  disabled={isLoading}
                  className="text-xs px-3 py-1.5 text-red-500/60 hover:text-red-400 transition-colors disabled:opacity-50"
                >
                  Rejected
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
