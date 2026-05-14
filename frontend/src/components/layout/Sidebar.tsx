import { Link, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Search, Kanban, Building2, FileText,
  Users, MessageSquare, BookMarked, BarChart3, ClipboardList,
  User, ChevronLeft, ChevronRight, LogOut,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/app/auth'
import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/app/api'
import type { DashboardSummary } from '@/types'

interface SidebarProps {
  collapsed: boolean
  onToggle: () => void
}

interface NavItem {
  id: string
  label: string
  icon: React.ComponentType<{ className?: string }>
  path: string
  badge?: number
}

export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const location = useLocation()
  const logout = useAuthStore((s) => s.logout)
  const email = useAuthStore((s) => s.email)
  const accessToken = useAuthStore((s) => s.accessToken)

  const { data: summary } = useQuery<DashboardSummary>({
    queryKey: ['dashboard-summary'],
    queryFn: () => apiClient.get('/dashboard').then((r) => r.data),
    enabled: Boolean(accessToken),
    refetchInterval: 60_000,
    retry: false,
  })

  const overdueCount = summary?.overdueActionsCount ?? 0
  const decisionCount = summary?.pendingDecisionsCount ?? 0

  const navItems: NavItem[] = [
    { id: 'today', label: 'Today', icon: LayoutDashboard, path: '/today', badge: overdueCount },
    { id: 'analyze', label: 'Analyze Job', icon: Search, path: '/analyze', badge: decisionCount },
    { id: 'pipeline', label: 'Pipeline', icon: Kanban, path: '/pipeline' },
    { id: 'companies', label: 'Companies', icon: Building2, path: '/companies' },
    { id: 'vacancies', label: 'Vacancies', icon: FileText, path: '/vacancies' },
    { id: 'contacts', label: 'Contacts', icon: Users, path: '/contacts' },
    { id: 'messages', label: 'Messages', icon: MessageSquare, path: '/messages' },
    { id: 'searches', label: 'Saved Searches', icon: BookMarked, path: '/searches' },
    { id: 'analytics', label: 'Analytics', icon: BarChart3, path: '/analytics' },
    { id: 'review', label: 'Weekly Review', icon: ClipboardList, path: '/review' },
    { id: 'profile', label: 'Profile & CV', icon: User, path: '/profile' },
  ]

  const isActive = (path: string) => location.pathname === path || location.pathname.startsWith(path + '/')

  const displayName = email ? email.split('@')[0].split(/[._-]/)[0] : null
  const displayNameFormatted = displayName
    ? displayName.charAt(0).toUpperCase() + displayName.slice(1)
    : null

  return (
    <div
      className={cn(
        'flex flex-col h-full border-r transition-all duration-200',
        collapsed ? 'w-14' : 'w-[200px]',
        'border-[#1E2530] bg-[#070A12]',
      )}
    >
      {/* Logo */}
      <div className={cn('flex items-center h-14 border-b border-[#1E2530]', collapsed ? 'px-3 justify-center' : 'px-4 gap-2.5')}>
        <div className="w-7 h-7 bg-[#F5A623] rounded-md font-bold text-[#070A12] flex items-center justify-center text-sm flex-shrink-0 tracking-tight">
          J
        </div>
        {!collapsed && (
          <div className="flex items-center gap-2 min-w-0">
            <span className="font-bold text-sm text-[#F1F3F6] tracking-tight">JobOps</span>
          </div>
        )}
      </div>

      {/* Nav items */}
      <nav className="flex-1 py-2 px-2 space-y-0.5 overflow-y-auto scrollbar-thin">
        {navItems.map((item) => {
          const active = isActive(item.path)
          return (
            <Link
              key={item.id}
              to={item.path}
              className={cn(
                'relative flex items-center gap-2 px-2 py-2 rounded-md text-xs font-medium transition-colors group border-l-[3px]',
                active
                  ? 'bg-[rgba(245,166,35,0.12)] text-[#F5A623] border-l-[#F5A623]'
                  : 'text-[#8B95A1] border-l-transparent hover:bg-[#141920] hover:text-[#F1F3F6]',
              )}
            >
              <item.icon className="h-4 w-4 flex-shrink-0" />
              {!collapsed && <span className="truncate">{item.label}</span>}
              {!collapsed && item.badge != null && item.badge > 0 && item.id === 'today' && (
                <span className="ml-auto text-xs bg-red-500 text-white font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                  {item.badge > 99 ? '99+' : item.badge}
                </span>
              )}
              {!collapsed && item.badge != null && item.badge > 0 && item.id !== 'today' && (
                <span className="ml-auto text-xs bg-[#F5A623] text-[#070A12] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                  {item.badge > 99 ? '99+' : item.badge}
                </span>
              )}
              {collapsed && item.badge != null && item.badge > 0 && (
                <span
                  className={cn(
                    'absolute top-1.5 right-1.5 w-1.5 h-1.5 rounded-full',
                    item.id === 'today' ? 'bg-red-500' : 'bg-[#F5A623]',
                  )}
                />
              )}
            </Link>
          )
        })}
      </nav>

      {/* User + collapse */}
      <div className="border-t border-[#1E2530] p-2 space-y-1">
        {!collapsed && email && (
          <div className="px-2 py-1.5">
            {displayNameFormatted && (
              <p className="text-xs font-semibold text-zinc-300 truncate">{displayNameFormatted}</p>
            )}
            <p className="text-xs text-zinc-600 truncate">{email}</p>
          </div>
        )}
        <button
          onClick={logout}
          className={cn(
            'w-full flex items-center gap-2 px-2 py-2 rounded-md text-xs text-zinc-500 hover:bg-zinc-900 hover:text-zinc-300 transition-colors',
            collapsed && 'justify-center',
          )}
        >
          <LogOut className="h-4 w-4 flex-shrink-0" />
          {!collapsed && <span>Log out</span>}
        </button>
        <button
          onClick={onToggle}
          className={cn(
            'w-full flex items-center gap-2 px-2 py-2 rounded-md text-xs text-zinc-600 hover:bg-zinc-900 hover:text-zinc-400 transition-colors',
            collapsed && 'justify-center',
          )}
        >
          {collapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          {!collapsed && <span>Collapse</span>}
        </button>
      </div>
    </div>
  )
}
