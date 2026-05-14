import { cn } from '@/lib/utils'

interface FitScoreBarProps {
  score: number
  label?: string
  showLabel?: boolean
  compact?: boolean
}

export function FitScoreBar({ score, label, showLabel = true, compact = false }: FitScoreBarProps) {
  const color =
    score >= 75 ? 'bg-emerald-500' : score >= 55 ? 'bg-amber-500' : 'bg-red-500'
  const textColor =
    score >= 75 ? 'text-emerald-400' : score >= 55 ? 'text-amber-400' : 'text-red-400'

  if (compact) {
    return (
      <div className="flex items-center gap-1.5">
        <div className="w-16 h-1 bg-zinc-800 rounded-full overflow-hidden">
          <div
            className={cn('h-full rounded-full transition-all', color)}
            style={{ width: `${Math.min(score, 100)}%` }}
          />
        </div>
        <span className={cn('text-xs font-mono font-bold', textColor)}>{score}</span>
      </div>
    )
  }

  return (
    <div className="space-y-1">
      {showLabel && (
        <div className="flex items-center justify-between text-xs">
          <span className="text-zinc-500">{label ?? 'Fit Score'}</span>
          <span className={cn('font-mono font-bold', textColor)}>{score}/100</span>
        </div>
      )}
      <div className="w-full h-1.5 bg-zinc-800 rounded-full overflow-hidden">
        <div
          className={cn('h-full rounded-full transition-all', color)}
          style={{ width: `${Math.min(score, 100)}%` }}
        />
      </div>
    </div>
  )
}
