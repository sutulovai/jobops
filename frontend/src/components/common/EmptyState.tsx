import { cn } from '@/lib/utils'
import type { LucideIcon } from 'lucide-react'

interface EmptyStateProps {
  icon?: LucideIcon
  title: string
  description?: string
  action?: React.ReactNode
  className?: string
}

export function EmptyState({ icon: Icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center gap-3 py-16 text-center', className)}>
      {Icon && <Icon className="h-10 w-10 text-zinc-700" />}
      <div className="space-y-1">
        <p className="text-sm font-medium text-zinc-400">{title}</p>
        {description && <p className="text-xs text-zinc-600 max-w-xs">{description}</p>}
      </div>
      {action}
    </div>
  )
}
