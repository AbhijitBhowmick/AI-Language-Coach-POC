'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Sidebar } from '@/components/layout/Sidebar';
import { useAuthStore } from '@/lib/stores/authStore';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { isAuthenticated, token } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated || !token) {
      router.push('/login');
    }
  }, [isAuthenticated, token, router]);

  if (!isAuthenticated || !token) {
    return null;
  }

  return (
    <div className="flex min-h-screen bg-surface">
      <Sidebar />
      <main className="flex-1 ml-64 p-8">
        {children}
      </main>
    </div>
  );
}