import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthenticationResponse, UserRole, UserStatus } from '@/types';

interface AuthState {
  user: {
    userId: string;
    email: string;
    firstName: string;
    lastName: string;
    role: UserRole;
    status: UserStatus;
    tenantId: string;
  } | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (data: AuthenticationResponse) => void;
  logout: () => void;
  setUser: (user: AuthState['user']) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (data: AuthenticationResponse) => {
        const user = {
          userId: data.userId,
          email: data.email,
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          role: (data.roles?.[0] || 'USER_STUDENT') as UserRole,
          status: (data.status || 'ACTIVE') as UserStatus,
          tenantId: data.tenantId,
        };
        if (typeof window !== 'undefined') {
          localStorage.setItem('userId', user.userId);
          localStorage.setItem('role', user.role);
          localStorage.setItem('tenantId', user.tenantId);
        }
        set({ user, token: data.accessToken, isAuthenticated: true });
      },
      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('userId');
          localStorage.removeItem('role');
          localStorage.removeItem('tenantId');
        }
        set({ user: null, token: null, isAuthenticated: false });
      },
      setUser: (user) => set({ user }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
