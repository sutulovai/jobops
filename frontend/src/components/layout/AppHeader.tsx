import { useLocation } from 'react-router-dom'

function titleForPath(pathname: string): string {
  const seg = pathname.replace(/^\//, '').split('/')[0] || 'today'
  const map: Record<string, string> = {
    today: 'Today',
    analyze: 'Analyze Job',
    pipeline: 'Pipeline',
    companies: 'Companies',
    vacancies: 'Vacancies',
    contacts: 'Contacts',
    messages: 'Messages',
    searches: 'Saved Searches',
    analytics: 'Analytics',
    review: 'Weekly Review',
    profile: 'Profile & CV',
  }
  return map[seg] ?? seg
}

function AppHeader() {
  const { pathname } = useLocation()
  const dateShort = new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })

  return (
    <header className="h-12 flex-shrink-0 flex items-center justify-between px-5 border-b border-[#1E2530] bg-[#070A12]">
      <span className="text-[13px] font-semibold text-[#8B95A1]">{titleForPath(pathname)}</span>
      <div className="flex items-center gap-3.5">
        <span className="font-mono text-[11px] text-[#4A5568]">{dateShort}</span>
        <div className="flex items-center gap-1.5 text-[11px] text-[#8B95A1]">
          <span className="w-1.5 h-1.5 rounded-full bg-[#F5A623] animate-pulse" aria-hidden />
          Munich hunt active
        </div>
      </div>
    </header>
  )
}

export default AppHeader
export { titleForPath }
