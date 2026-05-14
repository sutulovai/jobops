import { cn } from '@/lib/utils'
import type { ActionPriority } from '@/types'

interface PriorityBadgeProps {
  priority: ActionPriority
  className?: string
}

const styles: Record<ActionPriority, string> = {
  P0: 'bg-red-500/20 text-red-400 border-red-500/40',
  P1: 'bg-amber-500/20 text-amber-400 border-amber-500/40',
  P2: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
  P3: 'bg-zinc-700/50 text-zinc-400 border-zinc-600/50',
}

export function PriorityBadge({ priority, className }: PriorityBadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center px-1.5 py-0.5 rounded text-xs font-bold border font-mono',
        styles[priority],
        className,
      )}
    >
      {priority}
    </span>
  )
}
