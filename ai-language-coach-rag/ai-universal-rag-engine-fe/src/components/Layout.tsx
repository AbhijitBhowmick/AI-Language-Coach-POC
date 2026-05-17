'use client';

import { useRouter, usePathname } from 'next/navigation';
import { useEffect, useState, ReactNode } from 'react';

interface LayoutProps {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [currentPage, setCurrentPage] = useState('ingestion');
  const [isAuthenticated, setIsAuthenticated] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (!token && pathname !== '/login') {
      router.push('/login');
      setIsAuthenticated(false);
    }
  }, [pathname, router]);

  if (pathname === '/login' || pathname === '/') {
    return <>{children}</>;
  }

  useEffect(() => {
    if (pathname.includes('configuration')) setCurrentPage('configuration');
    else if (pathname.includes('ingestion')) setCurrentPage('ingestion');
    else if (pathname.includes('performance')) setCurrentPage('performance');
    else if (pathname.includes('dashboard')) setCurrentPage('dashboard');
  }, [pathname]);

  const handleLogout = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_id');
    router.push('/login');
  };

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', href: '/dashboard' },
    { id: 'ingestion', label: 'Content Ingestion', href: '/ingestion' },
    { id: 'configuration', label: 'Configuration', href: '/configuration' },
    { id: 'performance', label: 'Analytics', href: '/performance' },
  ];

  const sidebarItems = [
    { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
    { id: 'ingestion', label: 'Content Ingestion', icon: 'upload_file' },
    { id: 'configuration', label: 'Configuration', icon: 'tune' },
    { id: 'performance', label: 'Analytics', icon: 'analytics' },
  ];

  const getIcon = (iconName: string) => {
    return `material-symbols-outlined`;
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen font-['Inter',sans-serif] bg-surface">
      <header className="bg-white h-16 w-full border-b sticky top-0 z-50 shadow-sm border-border-subtle">
        <div className="max-w-7xl mx-auto px-6 flex justify-between items-center h-full">
          <div className="text-xl font-bold text-text-primary">Linguist RAG</div>
          <div className="flex items-center gap-6">
            <nav className="hidden md:flex items-center gap-8 h-full">
              {navItems.map((item) => (
                <a
                  key={item.id}
                  href={item.href}
                  className={`text-sm font-medium transition-colors ${
                    currentPage === item.id
                      ? 'text-primary font-semibold border-b-2 border-primary h-16 flex items-center'
                      : 'text-text-secondary hover:text-primary'
                  }`}
                >
                  {item.label}
                </a>
              ))}
            </nav>
            <div className="flex items-center gap-4 text-text-secondary">
              <span className={`${getIcon('account')} cursor-pointer hover:text-primary material-symbols-outlined`}>
                account_circle
              </span>
              <span className={`${getIcon('settings')} cursor-pointer hover:text-primary material-symbols-outlined`}>
                settings
              </span>
            </div>
          </div>
        </div>
      </header>

      <div className="flex">
        <aside className="w-64 h-[calc(100vh-64px)] fixed left-0 border-r border-border-subtle bg-surface-container-low flex flex-col py-4 gap-2 font-['Inter',sans-serif]">
          <div className="px-6 py-4 mb-4 border-b border-border-subtle">
            <h2 className="text-lg font-black text-primary">RAG Infrastructure</h2>
            <p className="text-xs text-text-secondary font-medium uppercase tracking-wider">Language Learning Engine</p>
          </div>
          <div className="px-3 flex flex-col gap-1">
            {sidebarItems.map((item) => (
              <a
                key={item.id}
                href={item.href}
                className={`flex items-center gap-3 px-3 py-2 rounded-lg transition-all active:scale-95 ${
                  currentPage === item.id
                    ? 'bg-white text-primary font-bold border-r-4 border-primary shadow-sm'
                    : 'text-text-secondary hover:bg-surface-container-low hover:text-text-primary'
                }`}
              >
                <span className="material-symbols-outlined" data-icon={item.icon}>
                  {item.icon}
                </span>
                <span>{item.label}</span>
              </a>
            ))}
          </div>
          <div className="mt-auto px-6 py-4 flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-primary-fixed flex items-center justify-center text-primary">
              <span className="material-symbols-outlined text-sm">person</span>
            </div>
            <div>
              <p className="font-bold text-text-primary">Admin User</p>
              <p className="text-[10px] text-text-secondary">Premium Tier</p>
            </div>
          </div>
        </aside>

        <main className="ml-64 flex-1 p-8">
          <div className="max-w-5xl mx-auto">{children}</div>
        </main>
      </div>
    </div>
  );
}