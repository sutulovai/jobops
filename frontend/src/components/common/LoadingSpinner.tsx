import { cn } from '@/lib/utils'

interface LoadingSpinnerProps {
  className?: string
  size?: 'sm' | 'md' | 'lg'
}

export function LoadingSpinner({ className, size = 'md' }: LoadingSpinnerProps) {
  const sizeClass = { sm: 'h-4 w-4', md: 'h-6 w-6', lg: 'h-8 w-8' }[size]
  return (
    <div
      className={cn(
        'border-2 border-amber-500 border-t-transparent rounded-full animate-spin',
        sizeClass,
        className,
      )}
    />
  )
}

interface LoadingStateProps {
  message?: string
  className?: string
}

export function LoadingState({ message = 'Loading…', className }: LoadingStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center gap-3 py-12 text-zinc-600', className)}>
      <LoadingSpinner size="md" />
      <span className="text-sm">{message}</span>
    </div>
  )
}
