import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { MessageSquare, Plus, RefreshCw } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { apiClient, getErrorMessage } from '@/app/api'
import { CopyButton } from '@/components/common/CopyButton'
import { LoadingState, LoadingSpinner } from '@/components/common/LoadingSpinner'
import { EmptyState } from '@/components/common/EmptyState'
import { formatRelative, cn } from '@/lib/utils'
import type { OutreachMessage } from '@/types'

const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'bg-zinc-700/40 text-zinc-400 border-zinc-600/40',
  COPIED: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
  SENT: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
  REPLIED: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
  IGNORED: 'bg-zinc-700/30 text-zinc-600 border-zinc-700/30',
}

const MESSAGE_TYPES = [
  { value: 'LINKEDIN_RECRUITER_DM', label: 'LinkedIn — Recruiter DM' },
  { value: 'LINKEDIN_MANAGER_DM', label: 'LinkedIn — Manager DM' },
  { value: 'REFERRAL_REQUEST', label: 'Referral Request' },
  { value: 'EMAIL_RECRUITER', label: 'Email — Recruiter' },
  { value: 'FOLLOW_UP', label: 'Follow-up' },
  { value: 'POST_INTERVIEW_THANK_YOU', label: 'Post-interview Thank You' },
  { value: 'SALARY_ANSWER', label: 'Salary Answer' },
  { value: 'RELOCATION_ANSWER', label: 'Relocation Answer' },
  { value: 'WHY_GERMANY', label: 'Why Germany?' },
  { value: 'WHY_MUNICH', label: 'Why Munich?' },
  { value: 'COVER_NOTE', label: 'Cover Note' },
  { value: 'LINKEDIN_CONNECTION', label: 'LinkedIn Connection Note' },
]

export function MessagesPage() {
  const queryClient = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [filterStatus, setFilterStatus] = useState<string>('ALL')

  const { data: messages, isLoading } = useQuery<OutreachMessage[]>({
    queryKey: ['messages'],
    queryFn: () => apiClient.get('/messages').then((r) => r.data),
  })

  const generateMutation = useMutation({
    mutationFn: (data: Record<string, string>) =>
      apiClient.post('/messages/generate', data).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messages'] })
      setShowForm(false)
      toast.success('Message generated')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const copyMutation = useMutation({
    mutationFn: (id: string) => apiClient.post(`/messages/${id}/copied`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['messages'] }),
  })

  const sentMutation = useMutation({
    mutationFn: (id: string) => apiClient.post(`/messages/${id}/sent`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messages'] })
      toast.success('Marked as sent')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  if (isLoading) return <LoadingState message="Loading messages…" className="mt-20" />

  const filtered = messages?.filter((m) =>
    filterStatus === 'ALL' || m.status === filterStatus
  ) ?? []

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Messages</h1>
          <p className="text-sm text-zinc-500 mt-0.5">Copy-paste ready outreach</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-1.5 px-4 py-2 bg-amber-500 hover:bg-amber-400 text-zinc-950 font-bold text-sm rounded-lg transition-colors"
        >
          <Plus className="h-4 w-4" />
          Generate Message
        </button>
      </div>

      {/* Status filter */}
      <div className="flex gap-2 mb-5 flex-wrap">
        {['ALL', 'DRAFT', 'COPIED', 'SENT', 'REPLIED'].map((s) => (
          <button
            key={s}
            onClick={() => setFilterStatus(s)}
            className={cn(
              'px-3 py-1 rounded-lg text-xs font-medium border transition-colors',
              filterStatus === s
                ? 'bg-amber-500/20 text-amber-400 border-amber-500/40'
                : 'bg-zinc-900 text-zinc-500 border-zinc-800 hover:border-zinc-700',
            )}
          >
            {s === 'ALL' ? `All (${messages?.length ?? 0})` : s.toLowerCase()}
          </button>
        ))}
      </div>

      {/* Generate form */}
      {showForm && (
        <GenerateForm
          onSubmit={(data) => generateMutation.mutate(data)}
          onClose={() => setShowForm(false)}
          isLoading={generateMutation.isPending}
        />
      )}

      {!filtered.length ? (
        <EmptyState
          icon={MessageSquare}
          title="No messages"
          description="Generate your first message — recruiter DM, manager outreach, or follow-up."
        />
      ) : (
        <div className="space-y-3">
          {filtered.map((msg) => (
            <MessageCard
              key={msg.id}
              message={msg}
              onCopy={() => copyMutation.mutate(msg.id)}
              onMarkSent={() => sentMutation.mutate(msg.id)}
              isSending={sentMutation.isPending}
            />
          ))}
        </div>
      )}
    </div>
  )
}

function GenerateForm({
  onSubmit, onClose, isLoading,
}: {
  onSubmit: (data: Record<string, string>) => void
  onClose: () => void
  isLoading: boolean
}) {
  const { register, handleSubmit } = useForm<Record<string, string>>()

  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4 mb-6">
      <div className="flex items-center justify-between mb-4">
        <p className="text-sm font-semibold text-zinc-300">Generate Message</p>
        <button onClick={onClose} className="text-zinc-600 hover:text-zinc-400 text-xs">
          Cancel
        </button>
      </div>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <div>
          <label className="block text-xs font-medium text-zinc-400 mb-1.5">Message type *</label>
          <select
            {...register('messageType', { required: true })}
            className="w-full bg-zinc-800 border border-zinc-700 rounded-lg px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:border-amber-500"
          >
            {MESSAGE_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-medium text-zinc-400 mb-1.5">Tone</label>
            <select
              {...register('tone')}
              className="w-full bg-zinc-800 border border-zinc-700 rounded-lg px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:border-amber-500"
            >
              <option value="professional">Professional</option>
              <option value="direct">Direct</option>
              <option value="warm">Warm</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-zinc-400 mb-1.5">Channel</label>
            <select
              {...register('channel')}
              className="w-full bg-zinc-800 border border-zinc-700 rounded-lg px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:border-amber-500"
            >
              <option value="LINKEDIN">LinkedIn</option>
              <option value="EMAIL">Email</option>
            </select>
          </div>
        </div>
        <div>
          <label className="block text-xs font-medium text-zinc-400 mb-1.5">Custom instructions</label>
          <input
            {...register('customInstructions')}
            placeholder="Mention Revolut payments experience, keep under 150 words…"
            className="w-full bg-zinc-800 border border-zinc-700 rounded-lg px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
          />
        </div>
        <button
          type="submit"
          disabled={isLoading}
          className="w-full bg-amber-500 hover:bg-amber-400 disabled:bg-zinc-800 disabled:text-zinc-600 text-zinc-950 font-bold text-sm py-2.5 rounded-lg transition-colors flex items-center justify-center gap-2"
        >
          {isLoading ? (
            <>
              <LoadingSpinner size="sm" /> Generating…
            </>
          ) : (
            <>
              <RefreshCw className="h-4 w-4" /> Generate
            </>
          )}
        </button>
      </form>
    </div>
  )
}

function MessageCard({
  message: m, onCopy, onMarkSent, isSending,
}: {
  message: OutreachMessage
  onCopy: () => void
  onMarkSent: () => void
  isSending: boolean
}) {
  const text = m.editedFinalText ?? m.generatedText
  const statusCls = STATUS_COLORS[m.status] ?? STATUS_COLORS.DRAFT

  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <span className="text-xs font-medium text-zinc-300">
            {m.messageType.replace(/_/g, ' ')}
          </span>
          {m.companyName && (
            <span className="text-xs text-zinc-600 ml-2">→ {m.companyName}</span>
          )}
          {m.contactName && (
            <span className="text-xs text-zinc-600 ml-1">({m.contactName})</span>
          )}
        </div>
        <span className={cn('flex-shrink-0 text-xs font-bold px-1.5 py-0.5 rounded border', statusCls)}>
          {m.status.toLowerCase()}
        </span>
      </div>

      {/* Always show full text — no truncation */}
      <div className="bg-zinc-950 border border-zinc-800 rounded-lg p-3 mb-3">
        <p className="text-xs text-zinc-300 font-mono leading-relaxed whitespace-pre-wrap">{text}</p>
      </div>

      <div className="flex items-center gap-2 flex-wrap">
        <CopyButton text={text} size="sm" onCopied={onCopy} />
        {m.status === 'DRAFT' && (
          <button
            onClick={onMarkSent}
            disabled={isSending}
            className="flex items-center gap-1.5 px-2.5 py-1 rounded text-xs font-medium bg-blue-500/15 hover:bg-blue-500/25 text-blue-400 border border-blue-500/30 transition-colors disabled:opacity-50"
          >
            Mark Sent
          </button>
        )}
        <p className="text-xs text-zinc-700 ml-auto">{formatRelative(m.createdAt)}</p>
      </div>
    </div>
  )
}
