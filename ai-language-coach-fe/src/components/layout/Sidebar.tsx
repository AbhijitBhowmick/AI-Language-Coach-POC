'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import { useAuthStore } from '@/lib/stores/authStore';
import { Button } from '@/components/ui/button';
import { 
  LayoutDashboard, 
  Users, 
  Gamepad2, 
  Mic, 
  Settings, 
  LogOut,
  GraduationCap,
  ShieldCheck,
  UserCheck,
  Puzzle
} from 'lucide-react';

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/community', label: 'Community', icon: Users },
  { href: '/games', label: 'Games', icon: Gamepad2 },
  { href: '/practice', label: 'Practice', icon: Mic },
  { href: '/settings', label: 'Settings', icon: Settings },
];

const adminNavItems = [
  { href: '/admin/users', label: 'User Management', icon: ShieldCheck },
  { href: '/admin/approvals', label: 'Approvals', icon: UserCheck },
  { href: '/games/admin', label: 'Game Templates', icon: Puzzle },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuthStore();

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-white border-r border-border flex flex-col">
      <div className="p-6 border-b border-border">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center">
            <GraduationCap className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-primary">LingoAI</h1>
            <p className="text-xs text-muted-foreground">Language Coach</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={cn(
              'flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all',
              pathname === item.href
                ? 'bg-primary text-white shadow-lg shadow-primary/25'
                : 'text-muted-foreground hover:bg-surface-container hover:text-on-surface'
            )}
          >
            <item.icon className="w-5 h-5" />
            {item.label}
          </Link>
        ))}
      </nav>

      {user && (user.role === 'SUPER_ADMIN' || user.role === 'BUSINESS_ADMIN' || user.role === 'ADMIN_TEACHER') && (
        <>
          <div className="px-4 mb-1">
            <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Admin</p>
          </div>
          <nav className="px-4 space-y-1 mb-4">
            {adminNavItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  'flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all',
                  pathname === item.href
                    ? 'bg-primary text-white shadow-lg shadow-primary/25'
                    : 'text-muted-foreground hover:bg-surface-container hover:text-on-surface'
                )}
              >
                <item.icon className="w-5 h-5" />
                {item.label}
              </Link>
            ))}
          </nav>
        </>
      )}

      <div className="p-4 border-t border-border">
        {user && (
          <div className="flex items-center gap-3 px-4 py-3 mb-3">
            <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
              <span className="text-primary font-semibold">
                {user.firstName?.[0]}{user.lastName?.[0]}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">
                {user.firstName} {user.lastName}
              </p>
              <p className="text-xs text-muted-foreground truncate">
                {user.role}
              </p>
            </div>
          </div>
        )}
        <Button
          variant="ghost"
          className="w-full justify-start text-muted-foreground hover:text-destructive"
          onClick={logout}
        >
          <LogOut className="w-4 h-4 mr-3" />
          Logout
        </Button>
      </div>
    </aside>
  );
}