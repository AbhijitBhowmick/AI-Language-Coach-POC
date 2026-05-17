'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/stores/authStore';

export default function AdminPage() {
  const router = useRouter();
  const { user } = useAuthStore();

  useEffect(() => {
    if (user?.role === 'SUPER_ADMIN' || user?.role === 'BUSINESS_ADMIN' || user?.role === 'ADMIN_TEACHER') {
      router.push('/admin/users');
    } else {
      router.push('/dashboard');
    }
  }, [user, router]);

  return null;
}
