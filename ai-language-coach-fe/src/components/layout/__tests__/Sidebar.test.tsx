import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

const mockLogout = vi.fn();

vi.mock('next/navigation', () => ({
  usePathname: () => '/dashboard',
}));

vi.mock('next/link', () => ({
  default: ({ children, href, className }: { children: React.ReactNode; href: string; className?: string }) =>
    <a href={href} className={className}>{children}</a>,
}));

vi.mock('@/lib/stores/authStore', () => ({
  useAuthStore: () => ({
    user: null,
    logout: mockLogout,
  }),
}));

async function renderSidebar() {
  const { Sidebar } = await import('../Sidebar');
  return render(<Sidebar />);
}

describe('Sidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders app name and all nav links', async () => {
    await renderSidebar();
    expect(screen.getByText('LingoAI')).toBeInTheDocument();
    expect(screen.getByText('Language Coach')).toBeInTheDocument();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Community')).toBeInTheDocument();
    expect(screen.getByText('Games')).toBeInTheDocument();
    expect(screen.getByText('Practice')).toBeInTheDocument();
    expect(screen.getByText('Settings')).toBeInTheDocument();
  });

  it('renders logout button', async () => {
    await renderSidebar();
    expect(screen.getByText('Logout')).toBeInTheDocument();
  });

  it('calls logout when Logout button clicked', async () => {
    await renderSidebar();
    await userEvent.click(screen.getByText('Logout'));
    expect(mockLogout).toHaveBeenCalledOnce();
  });

  it('highlights active nav item based on pathname', async () => {
    await renderSidebar();
    const dashboardLink = screen.getByText('Dashboard').closest('a');
    expect(dashboardLink?.className).toContain('bg-primary');
  });

  it('shows user info when user is present', async () => {
    vi.mocked(await import('@/lib/stores/authStore')).useAuthStore = () => ({
      user: {
        userId: 'u1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'USER_STUDENT',
        status: 'ACTIVE' as const,
        tenantId: 't1',
      },
      logout: mockLogout,
    });

    await renderSidebar();
    expect(screen.getByText(/John Doe/)).toBeInTheDocument();
    expect(screen.getByText('USER_STUDENT')).toBeInTheDocument();
  });
});
