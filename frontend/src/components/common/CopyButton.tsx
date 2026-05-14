import { useState } from 'react'
import { cn } from '@/lib/utils'

interface CopyButtonProps {
  text: string
  onCopied?: () => void
  className?: string
  size?: 'sm' | 'md'
  label?: string
}

export function CopyButton({ text, onCopied, className, size = 'md', label = 'Copy' }: CopyButtonProps) {
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(text)
    } catch {
      // fallback
    }
    setCopied(true)
    onCopied?.()
    setTimeout(() => setCopied(false), 2500)
  }

  const padding = size === 'sm' ? 'px-2.5 py-1 text-xs' : 'px-3 py-1.5 text-sm'

  return (
    <button
      onClick={handleCopy}
      className={cn(
        'inline-flex items-center gap-1.5 rounded font-bold transition-all',
        copied
          ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
          : 'bg-amber-500 hover:bg-amber-400 text-zinc-950',
        padding,
        className,
      )}
    >
      {copied ? `✓ Copied!` : `📋 ${label}`}
    </button>
  )
}
