import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Upload, Trash2, Star, User } from 'lucide-react'
import { useRef } from 'react'
import { apiClient, getErrorMessage } from '@/app/api'
import { LoadingState } from '@/components/common/LoadingSpinner'
import { formatDate, cn } from '@/lib/utils'
import type { UserProfile, Cv } from '@/types'

export function ProfilePage() {
  const queryClient = useQueryClient()
  const fileRef = useRef<HTMLInputElement>(null)

  const { data: profile, isLoading: profileLoading } = useQuery<UserProfile>({
    queryKey: ['profile'],
    queryFn: () => apiClient.get('/profile').then(r => r.data),
  })

  const { data: cvs, isLoading: cvsLoading } = useQuery<Cv[]>({
    queryKey: ['cvs'],
    queryFn: () => apiClient.get('/cvs').then(r => r.data),
  })

  const uploadMutation = useMutation({
    mutationFn: (file: File) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('label', file.name.replace('.pdf', ''))
      return apiClient.post('/cvs', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cvs'] })
      toast.success('CV uploaded')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const setDefaultMutation = useMutation({
    mutationFn: (id: string) => apiClient.put(`/cvs/${id}/default`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cvs'] }),
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  const deleteCvMutation = useMutation({
    mutationFn: (id: string) => apiClient.delete(`/cvs/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cvs'] })
      toast.success('CV deleted')
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  if (profileLoading) return <LoadingState message="Loading profile…" className="mt-20" />

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-zinc-100 tracking-tight">Profile & CV</h1>

      {/* Profile card */}
      <ProfileCard profile={profile} />

      {/* CVs */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-bold text-zinc-300">Uploaded CVs</h2>
          <button
            onClick={() => fileRef.current?.click()}
            disabled={uploadMutation.isPending}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-500 hover:bg-amber-400 disabled:bg-zinc-800 disabled:text-zinc-600 text-zinc-950 font-bold text-xs rounded-lg transition-colors"
          >
            <Upload className="h-3.5 w-3.5" />
            {uploadMutation.isPending ? 'Uploading…' : 'Upload PDF'}
          </button>
          <input
            ref={fileRef}
            type="file"
            accept=".pdf"
            className="hidden"
            onChange={e => {
              const file = e.target.files?.[0]
              if (file) uploadMutation.mutate(file)
              e.target.value = ''
            }}
          />
        </div>

        {cvsLoading ? (
          <p className="text-xs text-zinc-600">Loading CVs…</p>
        ) : !cvs?.length ? (
          <p className="text-xs text-zinc-600">No CVs uploaded yet. Upload a PDF to get started.</p>
        ) : (
          <div className="space-y-2">
            {cvs.map(cv => (
              <div
                key={cv.id}
                className={cn(
                  'flex items-center gap-3 p-3 rounded-lg border',
                  cv.isDefault
                    ? 'border-amber-500/30 bg-amber-500/5'
                    : 'border-zinc-800 bg-zinc-950/50',
                )}
              >
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-zinc-200 truncate">{cv.label}</p>
                  <p className="text-xs text-zinc-600">
                    v{cv.version} · {(cv.fileSizeBytes / 1024).toFixed(0)}KB · {formatDate(cv.createdAt)}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {cv.isDefault && (
                    <span className="text-xs text-amber-400 font-medium">Default</span>
                  )}
                  {!cv.isDefault && (
                    <button
                      onClick={() => setDefaultMutation.mutate(cv.id)}
                      className="text-zinc-600 hover:text-amber-400 transition-colors"
                      title="Set as default"
                    >
                      <Star className="h-4 w-4" />
                    </button>
                  )}
                  <button
                    onClick={() => deleteCvMutation.mutate(cv.id)}
                    className="text-zinc-700 hover:text-red-400 transition-colors"
                    title="Delete"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function ProfileCard({ profile }: { profile: UserProfile | undefined }) {
  if (!profile) return null
  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
      <div className="flex items-center gap-3 mb-4">
        <div className="h-10 w-10 rounded-full bg-amber-500/20 flex items-center justify-center">
          <User className="h-5 w-5 text-amber-400" />
        </div>
        <div>
          <p className="text-sm font-bold text-zinc-100">{profile.fullName ?? 'Your Profile'}</p>
          <p className="text-xs text-zinc-500">{profile.currentLocation ?? '—'}</p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 text-xs">
        <InfoRow label="Target roles" value={profile.targetRoleTitles?.join(', ')} />
        <InfoRow label="Target cities" value={[...(profile.targetCities ?? []), ...(profile.backupCities ?? [])].join(', ')} />
        <InfoRow label="Salary target" value={
          profile.targetSalaryMin != null
            ? `€${profile.targetSalaryMin?.toLocaleString()}–€${profile.targetSalaryMax?.toLocaleString()}`
            : undefined
        } />
        <InfoRow label="Availability" value={profile.availability} />
        <InfoRow label="English" value={profile.englishLevel} />
        <InfoRow label="German" value={profile.germanLevel} />
        <InfoRow label="Seniority" value={profile.seniorityTarget} />
        <InfoRow label="Relocation" value={profile.relocationStatus} />
      </div>

      {profile.positioningSummary && (
        <div className="mt-4 p-3 bg-zinc-800/60 rounded-lg">
          <p className="text-xs text-zinc-400 leading-relaxed">{profile.positioningSummary}</p>
        </div>
      )}
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string | undefined | null }) {
  return (
    <div>
      <p className="text-zinc-500">{label}</p>
      <p className="text-zinc-300 font-medium mt-0.5 truncate">{value ?? '—'}</p>
    </div>
  )
}
