import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import type { Company } from '@/types'
import { Globe, Linkedin } from 'lucide-react'
import { cn } from '@/lib/utils'

const TIER_COLORS: Record<string, string> = {
  P1: 'text-amber-400',
  P1_5: 'text-orange-400',
  P2: 'text-blue-400',
  P3: 'text-zinc-400',
}

export function CompanyDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { data: company, isLoading } = useQuery<Company>({
    queryKey: ['companies', id],
    queryFn: () => apiClient.get(`/companies/${id}`).then(r => r.data),
  })

  if (isLoading) return <LoadingState message="Loading company…" className="mt-20" />
  if (!company) return null

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <div className="flex items-start justify-between mb-6">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-zinc-100">{company.name}</h1>
            <span className={cn('text-sm font-bold font-mono', TIER_COLORS[company.priorityTier])}>
              {company.priorityTier}
            </span>
          </div>
          <p className="text-sm text-zinc-500 mt-0.5">{company.city}, {company.country}</p>
        </div>
        <div className="flex gap-2">
          {company.website && (
            <a href={company.website} target="_blank" rel="noopener" className="text-zinc-600 hover:text-zinc-400">
              <Globe className="h-4 w-4" />
            </a>
          )}
          {company.linkedInUrl && (
            <a href={company.linkedInUrl} target="_blank" rel="noopener" className="text-zinc-600 hover:text-zinc-400">
              <Linkedin className="h-4 w-4" />
            </a>
          )}
        </div>
      </div>

      <div className="space-y-4">
        {/* Fit reason */}
        {company.fitReason && (
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
            <p className="text-xs font-semibold text-zinc-400 mb-1">Why this company fits</p>
            <p className="text-sm text-zinc-300">{company.fitReason}</p>
          </div>
        )}

        {/* Key signals */}
        <div className="grid grid-cols-2 gap-3">
          <InfoCard label="English-speaking" value={company.englishLikelihood} />
          <InfoCard label="Relocation support" value={company.relocationFriendly} />
          <InfoCard label="Visa sponsorship" value={company.visaSponsorship} />
          <InfoCard label="Recommended approach" value={company.recommendedStrategy?.replace(/_/g, ' ')} />
        </div>

        {/* Salary */}
        {(company.salaryPitchMin || company.salaryPitchMax) && (
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
            <p className="text-xs font-semibold text-zinc-400 mb-1">Salary pitch range</p>
            <p className="text-sm font-mono text-emerald-400">
              €{company.salaryPitchMin?.toLocaleString()} – €{company.salaryPitchMax?.toLocaleString()}
            </p>
          </div>
        )}

        {/* Roles */}
        {company.likelyRoles.length > 0 && (
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
            <p className="text-xs font-semibold text-zinc-400 mb-2">Likely relevant roles</p>
            <div className="flex flex-wrap gap-1.5">
              {company.likelyRoles.map(r => (
                <span key={r} className="text-xs bg-zinc-800 text-zinc-300 rounded px-2 py-0.5">{r}</span>
              ))}
            </div>
          </div>
        )}

        {/* Notes */}
        {company.notes && (
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-4">
            <p className="text-xs font-semibold text-zinc-400 mb-1">Notes</p>
            <p className="text-sm text-zinc-400">{company.notes}</p>
          </div>
        )}
      </div>
    </div>
  )
}

function InfoCard({ label, value }: { label: string; value: string | undefined | null }) {
  const colors: Record<string, string> = {
    YES: 'text-emerald-400',
    NO: 'text-red-400',
    UNCERTAIN: 'text-zinc-500',
    DIRECT_APPLY: 'text-blue-400',
    RECRUITER_MESSAGE: 'text-amber-400',
    MANAGER_OUTREACH: 'text-violet-400',
    REFERRAL_REQUEST: 'text-orange-400',
  }
  const color = value ? (colors[value] ?? 'text-zinc-300') : 'text-zinc-700'
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-3">
      <p className="text-xs text-zinc-500">{label}</p>
      <p className={cn('text-sm font-medium mt-0.5', color)}>{value ?? '—'}</p>
    </div>
  )
}
