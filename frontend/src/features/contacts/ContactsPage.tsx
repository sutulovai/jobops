import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Users } from 'lucide-react'
import { toast } from 'sonner'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { EmptyState } from '@/components/common/EmptyState'
import { CopyButton } from '@/components/common/CopyButton'
import { cn } from '@/lib/utils'
import { generateAllMessagesForContact } from '@/lib/messageTemplates'
import type { Contact, MessageType } from '@/types'

const STRENGTH_SCORE: Record<string, number> = {
  COLD: 1, WARM: 2, CONNECTED: 3, MET: 3, HOT: 5,
}

// text + bg + border + dot bg (must be static for Tailwind JIT)
const TYPE_STYLES: Record<string, { text: string; badge: string; dot: string }> = {
  RECRUITER:               { text: 'text-blue-400',    badge: 'bg-blue-500/10 border-blue-500/25',    dot: 'bg-blue-400'    },
  TALENT_ACQUISITION:      { text: 'text-blue-400',    badge: 'bg-blue-500/10 border-blue-500/25',    dot: 'bg-blue-400'    },
  TECHNICAL_RECRUITER:     { text: 'text-blue-400',    badge: 'bg-blue-500/10 border-blue-500/25',    dot: 'bg-blue-400'    },
  AGENCY_RECRUITER:        { text: 'text-zinc-500',    badge: 'bg-zinc-700/30 border-zinc-700/40',    dot: 'bg-zinc-500'    },
  ENGINEERING_MANAGER:     { text: 'text-violet-400',  badge: 'bg-violet-500/10 border-violet-500/25',dot: 'bg-violet-400'  },
  HEAD_OF_ENGINEERING:     { text: 'text-pink-400',    badge: 'bg-pink-500/10 border-pink-500/25',    dot: 'bg-pink-400'    },
  DIRECTOR_OF_ENGINEERING: { text: 'text-pink-400',    badge: 'bg-pink-500/10 border-pink-500/25',    dot: 'bg-pink-400'    },
  BACKEND_ENGINEER:        { text: 'text-emerald-400', badge: 'bg-emerald-500/10 border-emerald-500/25', dot: 'bg-emerald-400' },
  SENIOR_BACKEND_ENGINEER: { text: 'text-emerald-400', badge: 'bg-emerald-500/10 border-emerald-500/25', dot: 'bg-emerald-400' },
  REFERRAL_CONTACT:        { text: 'text-amber-400',   badge: 'bg-amber-500/10 border-amber-500/25',  dot: 'bg-amber-400'   },
  FOUNDER:                 { text: 'text-red-400',     badge: 'bg-red-500/10 border-red-500/25',      dot: 'bg-red-400'     },
  CTO:                     { text: 'text-red-400',     badge: 'bg-red-500/10 border-red-500/25',      dot: 'bg-red-400'     },
  OTHER:                   { text: 'text-zinc-600',    badge: 'bg-zinc-700/30 border-zinc-700/40',    dot: 'bg-zinc-600'    },
}


const TYPE_LABELS: Record<string, string> = {
  RECRUITER:               'Recruiter',
  TALENT_ACQUISITION:      'TA',
  TECHNICAL_RECRUITER:     'Tech Recruiter',
  AGENCY_RECRUITER:        'Agency',
  ENGINEERING_MANAGER:     'Eng. Manager',
  HEAD_OF_ENGINEERING:     'Head of Eng.',
  DIRECTOR_OF_ENGINEERING: 'Dir. of Eng.',
  BACKEND_ENGINEER:        'Backend Eng.',
  SENIOR_BACKEND_ENGINEER: 'Sr. Backend',
  REFERRAL_CONTACT:        'Referral',
  FOUNDER:                 'Founder',
  CTO:                     'CTO',
  OTHER:                   'Other',
}

const STATUS_COLORS: Record<string, string> = {
  NEW:            'text-zinc-500',
  CONTACTED:      'text-blue-400',
  REPLIED:        'text-emerald-400',
  WARM:           'text-amber-400',
  NOT_RELEVANT:   'text-zinc-600',
  DO_NOT_CONTACT: 'text-red-400',
}

const MSG_TABS = [
  { key: 'recruiter' as const, label: 'Recruiter'    },
  { key: 'hm'        as const, label: 'Hiring Mgr'  },
  { key: 'referral'  as const, label: 'Referral Ask' },
  { key: 'followUp'  as const, label: 'Follow-Up'   },
]

export function ContactsPage() {
  const [selected, setSelected] = useState<string | null>(null)
  const [msgTab, setMsgTab]     = useState<'recruiter' | 'hm' | 'referral' | 'followUp'>('recruiter')

  const { data: contacts, isLoading } = useQuery<Contact[]>({
    queryKey: ['contacts'],
    queryFn: () => apiClient.get('/contacts').then((r) => r.data),
  })

  if (isLoading) return <LoadingState message="Loading contacts…" className="mt-20" />

  const sorted = [...(contacts ?? [])].sort(
    (a, b) => (STRENGTH_SCORE[b.relationshipStrength] ?? 1) - (STRENGTH_SCORE[a.relationshipStrength] ?? 1)
  )

  const warmCount = sorted.filter((c) => c.status === 'WARM' || c.relationshipStrength === 'HOT').length

  return (
    <div className="p-6 max-w-[900px]">
      <div className="mb-6">
        <h1 className="text-xl font-bold text-zinc-100">Contacts</h1>
        <p className="text-[13px] text-zinc-500 mt-0.5">
          {sorted.length} contacts{warmCount > 0 ? ` · ${warmCount} warm` : ''}
        </p>
      </div>

      {!sorted.length ? (
        <EmptyState icon={Users} title="No contacts yet" description="Add contacts as you reach out." />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {sorted.map((contact) => (
            <ContactCard
              key={contact.id}
              contact={contact}
              isOpen={selected === contact.id}
              msgTab={msgTab}
              onToggle={() => setSelected(selected === contact.id ? null : contact.id)}
              onMsgTab={setMsgTab}
            />
          ))}
        </div>
      )}
    </div>
  )
}

function ContactCard({
  contact, isOpen, msgTab, onToggle, onMsgTab,
}: {
  contact: Contact
  isOpen: boolean
  msgTab: 'recruiter' | 'hm' | 'referral' | 'followUp'
  onToggle: () => void
  onMsgTab: (tab: 'recruiter' | 'hm' | 'referral' | 'followUp') => void
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

  const msgTabType = (t: typeof msgTab): MessageType => {
    switch (t) {
      case 'recruiter':
        return 'LINKEDIN_RECRUITER_DM'
      case 'hm':
        return 'LINKEDIN_MANAGER_DM'
      case 'referral':
        return 'REFERRAL_REQUEST'
      case 'followUp':
        return 'FOLLOW_UP'
    }
  }

  const typeStyle   = TYPE_STYLES[contact.contactType] ?? TYPE_STYLES.OTHER
  const statusColor = STATUS_COLORS[contact.status] ?? 'text-zinc-600'
  const strength    = STRENGTH_SCORE[contact.relationshipStrength] ?? 1
  const isWarm      = strength >= 3

  const messages = isOpen
    ? generateAllMessagesForContact({
        companyName: contact.companyName,
        contactName: contact.name,
        fitReason: null,
        city: null,
      })
    : null

  return (
    <div className={cn(
      'bg-zinc-900 border rounded-xl overflow-hidden',
      isWarm ? 'border-amber-500/25' : 'border-zinc-800',
    )}>
      {/* Card header — clickable */}
      <div onClick={onToggle} className="p-4 cursor-pointer">
        <div className="flex justify-between items-start mb-2">
          <div>
            <p className="text-[14px] font-bold text-zinc-100">{contact.name}</p>
            {contact.title && (
              <p className="text-xs text-zinc-500 mt-0.5">{contact.title}</p>
            )}
            {contact.companyName && (
              <p className="text-xs text-zinc-600 mt-0.5">{contact.companyName}</p>
            )}
          </div>
          <span className={cn(
            'text-xs font-semibold px-2 py-0.5 rounded-full border',
            typeStyle.text,
            typeStyle.badge,
          )}>
            {TYPE_LABELS[contact.contactType] ?? contact.contactType}
          </span>
        </div>

        <div className="flex items-center justify-between">
          {/* Strength dots */}
          <div className="flex gap-1">
            {[1, 2, 3, 4, 5].map((i) => (
              <div
                key={i}
                className={cn('w-[7px] h-[7px] rounded-full', i <= strength ? typeStyle.dot : 'bg-zinc-800')}
              />
            ))}
          </div>
          <span className={cn('text-[11px] font-semibold', statusColor)}>
            {contact.status.replace(/_/g, ' ').toLowerCase()}
          </span>
        </div>

        {contact.notes && (
          <p className="text-xs text-zinc-600 leading-[1.5] mt-2 line-clamp-2">{contact.notes}</p>
        )}
      </div>

      {/* Inline message generator — opens on click */}
      {isOpen && messages && (
        <div className="border-t border-zinc-800 p-4">
          <p className="text-[11px] font-bold uppercase tracking-widest text-zinc-600 mb-2.5">Message</p>
          <div className="flex gap-1.5 flex-wrap mb-3">
            {MSG_TABS.map((t) => (
              <button
                key={t.key}
                onClick={(e) => { e.stopPropagation(); onMsgTab(t.key) }}
                className={cn(
                  'px-3 py-1 rounded-full text-xs font-semibold border transition-colors',
                  msgTab === t.key
                    ? 'bg-amber-500 text-zinc-950 border-amber-500'
                    : 'bg-transparent text-zinc-500 border-zinc-700 hover:text-zinc-300',
                )}
              >
                {t.label}
              </button>
            ))}
          </div>
          <pre className="font-mono bg-black/40 border border-zinc-800 rounded-xl px-4 py-3 text-[13px] text-zinc-300 whitespace-pre-wrap leading-[1.7] m-0">
            {messages[msgTab]}
          </pre>
          <div className="mt-2.5">
            <CopyButton
              text={messages[msgTab]}
              size="sm"
              onCopied={() =>
                logCopyMutation.mutate({
                  messageType: msgTabType(msgTab),
                  bodyText: messages[msgTab],
                  contactId: contact.id,
                  companyId: contact.companyId,
                  vacancyId: contact.vacancyId,
                  applicationId: contact.applicationId,
                  channel: contact.preferredChannel ?? 'LINKEDIN',
                })
              }
            />
          </div>
        </div>
      )}
    </div>
  )
}
