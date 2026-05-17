import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuthStore } from '../authStore';
import type { AuthenticationResponse } from '@/types';

const mockAuthResponse: AuthenticationResponse = {
  accessToken: 'test-token',
  tokenType: 'Bearer',
  expiresIn: 3600,
  userId: 'user-1',
  email: 'test@example.com',
  tenantId: 'tenant-1',
  status: 'ACTIVE',
  roles: ['USER_STUDENT'],
  firstName: 'John',
  lastName: 'Doe',
};

beforeEach(() => {
  localStorage.clear();
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
  });
});

describe('authStore', () => {
  it('starts unauthenticated', () => {
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
  });

  it('login sets user, token, and isAuthenticated', () => {
    const { login } = useAuthStore.getState();
    login(mockAuthResponse);

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.token).toBe('test-token');
    expect(state.user).toEqual({
      userId: 'user-1',
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      role: 'USER_STUDENT',
      status: 'ACTIVE',
      tenantId: 'tenant-1',
    });
  });

  it('login stores community headers in localStorage', () => {
    const { login } = useAuthStore.getState();
    login(mockAuthResponse);

    expect(localStorage.getItem('userId')).toBe('user-1');
    expect(localStorage.getItem('role')).toBe('USER_STUDENT');
    expect(localStorage.getItem('tenantId')).toBe('tenant-1');
  });

  it('login falls back to first role and ACTIVE status when undefined', () => {
    const { login } = useAuthStore.getState();
    login({
      ...mockAuthResponse,
      roles: undefined,
      status: undefined,
    });

    const { user } = useAuthStore.getState();
    expect(user?.role).toBe('USER_STUDENT');
    expect(user?.status).toBe('ACTIVE');
  });

  it('logout clears state and localStorage', () => {
    const { login, logout } = useAuthStore.getState();
    login(mockAuthResponse);
    logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(localStorage.getItem('userId')).toBeNull();
    expect(localStorage.getItem('role')).toBeNull();
    expect(localStorage.getItem('tenantId')).toBeNull();
  });

  it('setUser replaces the user object', () => {
    const { login, setUser } = useAuthStore.getState();
    login(mockAuthResponse);

    setUser({
      userId: 'user-2',
      email: 'updated@example.com',
      firstName: 'Jane',
      lastName: 'Smith',
      role: 'ADMIN_TEACHER',
      status: 'ACTIVE',
      tenantId: 'tenant-2',
    });

    const { user } = useAuthStore.getState();
    expect(user?.email).toBe('updated@example.com');
    expect(user?.role).toBe('ADMIN_TEACHER');
  });
});
