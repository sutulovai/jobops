import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Building2, Search } from 'lucide-react'
import { toast } from 'sonner'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { EmptyState } from '@/components/common/EmptyState'
import { CopyButton } from '@/components/common/CopyButton'
import { cn } from '@/lib/utils'
import { generateAllMessages } from '@/lib/messageTemplates'
import type { Company, MessageType } from '@/types'

const TIER_COLORS: Record<string, string> = {
  P1:   'bg-amber-500/20 text-amber-400 border-amber-500/40',
  P1_5: 'bg-orange-500/15 text-orange-400 border-orange-500/30',
  P2:   'bg-blue-500/10 text-blue-400 border-blue-500/25',
  P3:   'bg-zinc-700/40 text-zinc-400 border-zinc-600/40',
}

const TIER_LABELS: Record<string, string> = {
  P1: 'P1', P1_5: 'P1.5', P2: 'P2', P3: 'P3',
}

const STATUS_COLORS: Record<string, string> = {
  WATCHLIST:     'text-zinc-500',
  ACTIVE_TARGET: 'text-amber-400',
  APPLIED:       'text-blue-400',
  REJECTED:      'text-red-400',
  PAUSED:        'text-zinc-600',
  BLACKLISTED:   'text-red-600',
}

function Likelihood({ value }: { value: 'YES' | 'NO' | 'UNCERTAIN' | null | undefined }) {
  if (value === 'YES') return <span className="font-mono text-xs font-bold text-emerald-400">✓</span>
  if (value === 'NO')  return <span className="font-mono text-xs font-bold text-red-400">✗</span>
  return <span className="font-mono text-xs font-bold text-zinc-600">?</span>
}

export function CompaniesPage() {
  const [search, setSearch]           = useState('')
  const [filterTier, setFilterTier]   = useState<string>('ALL')
  const [msgCompany, setMsgCompany]   = useState<Company | null>(null)

  const { data: companies, isLoading } = useQuery<Company[]>({
    queryKey: ['companies'],
    queryFn: () => apiClient.get('/companies').then((r) => r.data),
  })

  const filtered = companies?.filter((c) => {
    const matchSearch =
      search === '' ||
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      (c.city ?? '').toLowerCase().includes(search.toLowerCase())
    const matchTier = filterTier === 'ALL' || c.priorityTier === filterTier
    return matchSearch && matchTier
  }) ?? []

  const byTier = {
    P1:   filtered.filter((c) => c.priorityTier === 'P1'),
    P1_5: filtered.filter((c) => c.priorityTier === 'P1_5'),
    P2:   filtered.filter((c) => c.priorityTier === 'P2'),
    P3:   filtered.filter((c) => c.priorityTier === 'P3'),
  }

  if (isLoading) return <LoadingState message="Loading companies…" className="mt-20" />

  return (
    <div className="p-6 max-w-[1100px]">
      <div className="mb-6">
        <h1 className="text-xl font-bold text-zinc-100">Companies</h1>
        <p className="text-[13px] text-zinc-500 mt-0.5">{companies?.length ?? 0} target companies</p>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-3 mb-6 flex-wrap">
        <div className="relative flex-1 max-w-[200px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-zinc-600" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search…"
            className="w-full bg-zinc-900 border border-zinc-800 rounded-lg pl-8 pr-3 py-2 text-[13px] text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-amber-500"
          />
        </div>
        {(['ALL', 'P1', 'P1_5', 'P2', 'P3'] as const).map((tier) => (
          <button
            key={tier}
            onClick={() => setFilterTier(tier)}
            className={cn(
              'px-4 py-1.5 rounded-full text-xs font-semibold border transition-colors',
              filterTier === tier
                ? 'bg-amber-500 text-zinc-950 border-amber-500'
                : 'bg-transparent text-zinc-500 border-zinc-700 hover:text-zinc-300',
            )}
          >
            {tier === 'ALL' ? 'All' : tier === 'P1_5' ? 'P1.5' : tier}
          </button>
        ))}
      </div>

      {!filtered.length ? (
        <EmptyState icon={Building2} title="No companies found" />
      ) : (
        <div className="space-y-7">
          {(
            [
              { key: 'P1',   label: 'Priority 1 — Best fit',      color: 'text-amber-400' },
              { key: 'P1_5', label: 'Priority 1.5 — Strong fit',  color: 'text-orange-400' },
              { key: 'P2',   label: 'Priority 2 — Strong backup', color: 'text-blue-400' },
              { key: 'P3',   label: 'Priority 3 — Watch',         color: 'text-zinc-500' },
            ] as { key: keyof typeof byTier; label: string; color: string }[]
          ).map(({ key, label, color }) => {
            const tierCompanies = byTier[key]
            if (!tierCompanies.length) return null
            return (
              <div key={key}>
                <div className="flex items-center gap-3 mb-3.5">
                  <span className={cn('text-[11px] font-bold uppercase tracking-widest', color)}>{label}</span>
                  <span className="text-[11px] text-zinc-700">{tierCompanies.length} companies</span>
                  <div className="flex-1 h-px bg-zinc-800" />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                  {tierCompanies.map((c) => (
                    <CompanyCard
                      key={c.id}
                      company={c}
                      onMsg={() => setMsgCompany(c)}
                      onDetail={() => setMsgCompany(c)}
                    />
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      )}

      {msgCompany && (
        <CompanyDetailModal company={msgCompany} onClose={() => setMsgCompany(null)} />
      )}
    </div>
  )
}

function CompanyCard({
  company: c, onMsg, onDetail,
}: {
  company: Company
  onMsg: () => void
  onDetail: () => void
}) {
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden hover:border-zinc-700 transition-all group">
      {/* Clickable card body → detail modal */}
      <div onClick={onDetail} className="p-4 cursor-pointer">
        <div className="flex items-start justify-between gap-2 mb-2">
          <div className="flex-1 min-w-0">
            <p className="text-[14px] font-bold text-zinc-100 group-hover:text-amber-400 transition-colors truncate">
              {c.name}
            </p>
            <p className="text-xs text-zinc-600 mt-0.5">
              {[c.city, c.industry].filter(Boolean).join(' · ')}
            </p>
          </div>
          <span className={cn(
            'flex-shrink-0 text-xs font-bold px-1.5 py-0.5 rounded border font-mono',
            TIER_COLORS[c.priorityTier] ?? TIER_COLORS.P3,
          )}>
            {TIER_LABELS[c.priorityTier]}
          </span>
        </div>

        {c.fitReason && (
          <p className="text-xs text-zinc-500 line-clamp-2 leading-[1.55] mb-2.5">{c.fitReason}</p>
        )}

        <div className="flex items-center gap-3 text-xs">
          {(
            [['EN', c.englishLikelihood], ['Reloc', c.relocationFriendly], ['Visa', c.visaSponsorship]] as const
          ).map(([l, v]) => (
            <span key={l} className="flex items-center gap-1 text-zinc-600">
              <Likelihood value={v} /> {l}
            </span>
          ))}
          <span className={cn('ml-auto text-[11px] font-semibold', STATUS_COLORS[c.status] ?? 'text-zinc-500')}>
            {c.status.replace(/_/g, ' ').toLowerCase()}
          </span>
        </div>
      </div>

      {/* Action bar — always visible at bottom */}
      <div className="border-t border-zinc-800 px-4 py-2.5 flex gap-2">
        <button
          onClick={(e) => { e.stopPropagation(); onMsg() }}
          className="flex-1 py-1.5 rounded-lg text-xs font-semibold bg-amber-500/10 hover:bg-amber-500/15 text-amber-400 border border-amber-500/25 transition-colors"
        >
          📋 Messages
        </button>
        {c.careersPageUrl ? (
          <a
            href={c.careersPageUrl}
            target="_blank"
            rel="noopener noreferrer"
            onClick={(e) => e.stopPropagation()}
            className="px-3 py-1.5 rounded-lg text-xs text-zinc-500 border border-zinc-700 hover:text-zinc-300 transition-colors no-underline"
          >
            Apply ↗
          </a>
        ) : (
          <span className="px-3 py-1.5 rounded-lg text-xs text-zinc-700 border border-zinc-800">
            Apply ↗
          </span>
        )}
      </div>
    </div>
  )
}

// ─── Message tabs ─────────────────────────────────────────────────────────────

const MESSAGE_TABS = [
  { key: 'recruiter' as const, label: 'Recruiter'    },
  { key: 'hm'        as const, label: 'Hiring Mgr'  },
  { key: 'referral'  as const, label: 'Referral Ask' },
  { key: 'followUp'  as const, label: 'Follow-Up'   },
  { key: 'cold'      as const, label: 'Cold'         },
]

function CompanyDetailModal({ company, onClose }: { company: Company; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [activeTab, setActiveTab] = useState<'recruiter' | 'hm' | 'referral' | 'followUp' | 'cold'>('recruiter')

  const messages = generateAllMessages(company)

  const logCopyMutation = useMutation({
    mutationFn: (body: Record<string, unknown>) => apiClient.post('/messages/log-copy', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['messages'] })
      toast.success('Copied — logged in Messages')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const tabMessageType = (): MessageType => {
    switch (activeTab) {
      case 'recruiter':
        return 'LINKEDIN_RECRUITER_DM'
      case 'hm':
        return 'LINKEDIN_MANAGER_DM'
      case 'referral':
        return 'REFERRAL_REQUEST'
      case 'followUp':
        return 'FOLLOW_UP'
      case 'cold':
        return 'LINKEDIN_CONNECTION'
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center p-4"
      style={{ background: 'rgba(0,0,0,0.75)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="w-full max-w-2xl max-h-[88vh] overflow-y-auto rounded-2xl bg-zinc-900 border border-zinc-700 pb-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-start justify-between p-5 border-b border-zinc-800">
          <div>
            <h2 className="text-[16px] font-bold text-zinc-100">{company.name}</h2>
            <p className="text-xs text-zinc-500 mt-0.5">
              {[company.city, company.likelyRoles?.[0] ?? 'Senior Backend Engineer'].filter(Boolean).join(' · ')}
            </p>
          </div>
          <button onClick={onClose} className="text-zinc-600 hover:text-zinc-300 transition-colors text-xl leading-none mt-0.5">✕</button>
        </div>

        <div className="p-5 space-y-5">
          {/* Why it fits */}
          {company.fitReason && (
            <div className="bg-zinc-900/60 border border-zinc-800 rounded-xl p-4">
              <p className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-2">Why it fits</p>
              <p className="text-[13px] text-zinc-400 leading-[1.6] m-0">{company.fitReason}</p>
            </div>
          )}

          {/* EN / Reloc / Visa row */}
          <div className="flex gap-5">
            {(
              [['English', company.englishLikelihood], ['Relocation', company.relocationFriendly], ['Visa', company.visaSponsorship]] as const
            ).map(([l, v]) => (
              <div key={l} className="flex items-center gap-1.5 text-xs text-zinc-500">
                <Likelihood value={v} />
                <span>{l}</span>
              </div>
            ))}
          </div>

          {/* Message tabs */}
          <div>
            <p className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-2.5">Ready-to-Send Messages</p>
            <div className="flex gap-1.5 mb-3.5 flex-wrap">
              {MESSAGE_TABS.map((t) => (
                <button
                  key={t.key}
                  onClick={() => setActiveTab(t.key)}
                  className={cn(
                    'px-3.5 py-1.5 rounded-full text-xs font-semibold border transition-colors',
                    activeTab === t.key
                      ? 'bg-amber-500 text-zinc-950 border-amber-500'
                      : 'bg-transparent text-zinc-500 border-zinc-700 hover:text-zinc-300',
                  )}
                >
                  {t.label}
                </button>
              ))}
            </div>
            <pre className="font-mono bg-black/40 border border-zinc-800 rounded-xl px-4 py-3.5 text-[13px] text-zinc-300 whitespace-pre-wrap leading-[1.7] m-0">
              {messages[activeTab]}
            </pre>
            <div className="mt-2.5">
              <CopyButton
                text={messages[activeTab]}
                size="sm"
                label={`Copy ${MESSAGE_TABS.find((t) => t.key === activeTab)?.label}`}
                onCopied={() =>
                  logCopyMutation.mutate({
                    messageType: tabMessageType(),
                    bodyText: messages[activeTab],
                    companyId: company.id,
                    channel: 'LINKEDIN',
                  })
                }
              />
            </div>
          </div>

          {/* Likely roles */}
          {company.likelyRoles.length > 0 && (
            <div>
              <p className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-2">Likely Roles</p>
              <div className="flex flex-wrap gap-1.5">
                {company.likelyRoles.map((r) => (
                  <span key={r} className="text-xs bg-zinc-800 text-zinc-300 rounded px-2 py-0.5 border border-zinc-700">{r}</span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
