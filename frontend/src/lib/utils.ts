import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('en-DE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

export function formatRelative(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return 'Today'
  if (diffDays === 1) return 'Yesterday'
  if (diffDays < 7) return `${diffDays}d ago`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}w ago`
  return formatDate(dateStr)
}

export function isDueDateOverdue(dateStr: string | null | undefined): boolean {
  if (!dateStr) return false
  return new Date(dateStr) < new Date(new Date().toDateString())
}

export function salaryDisplay(min: number | null, max: number | null, currency = 'EUR'): string {
  if (!min && !max) return 'Salary not specified'
  if (min && max) return `${currency} ${min.toLocaleString()}–${max.toLocaleString()}`
  if (min) return `${currency} ${min.toLocaleString()}+`
  return `Up to ${currency} ${max!.toLocaleString()}`
}

/** Monday = 1 … Sunday = 7 (Europe/Berlin). */
export function berlinWeekdayMon1Sun7(d = new Date()): number {
  const map: Record<string, number> = { Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6, Sun: 7 }
  const short = new Intl.DateTimeFormat('en-GB', { timeZone: 'Europe/Berlin', weekday: 'short' }).format(d)
  return map[short] ?? 1
}

export function isBerlinPastWednesday(d = new Date()): boolean {
  return berlinWeekdayMon1Sun7(d) >= 4
}
