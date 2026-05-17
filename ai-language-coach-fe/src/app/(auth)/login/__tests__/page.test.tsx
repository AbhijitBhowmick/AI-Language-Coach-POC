import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

const mockPush = vi.fn();
const mockLoginMutate = vi.fn();
const mockRegisterMutate = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

vi.mock('@/lib/hooks/useApi', () => ({
  useLogin: () => ({ mutate: mockLoginMutate, isPending: false }),
  useRegister: () => ({ mutate: mockRegisterMutate, isPending: false }),
}));

vi.mock('@/lib/stores/authStore', () => ({
  useAuthStore: () => ({
    login: vi.fn(),
    logout: vi.fn(),
    user: null,
    token: null,
    isAuthenticated: false,
  }),
}));

beforeEach(() => {
  vi.clearAllMocks();
});

async function renderLogin() {
  const Page = (await import('../page')).default;
  return render(<Page />);
}

describe('LoginPage', () => {
  it('renders login form by default', async () => {
    await renderLogin();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sign In' })).toBeInTheDocument();
  });

  it('renders "Don\'t have an account? Register" toggle', async () => {
    await renderLogin();
    expect(screen.getByText("Don't have an account? Register")).toBeInTheDocument();
  });

  it('switches to register form when toggle clicked', async () => {
    await renderLogin();
    await userEvent.click(screen.getByText("Don't have an account? Register"));
    expect(screen.getByLabelText('First Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Last Name')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create Account' })).toBeInTheDocument();
  });

  it('calls login mutate on form submit', async () => {
    await renderLogin();
    await userEvent.type(screen.getByLabelText('Email'), 'test@example.com');
    await userEvent.type(screen.getByLabelText('Password'), 'password123');
    await userEvent.click(screen.getByRole('button', { name: 'Sign In' }));

    expect(mockLoginMutate).toHaveBeenCalledWith(
      { email: 'test@example.com', password: 'password123' },
      expect.any(Object),
    );
  });

  it('calls register mutate on register submit', async () => {
    await renderLogin();
    await userEvent.click(screen.getByText("Don't have an account? Register"));

    await userEvent.type(screen.getByLabelText('First Name'), 'John');
    await userEvent.type(screen.getByLabelText('Last Name'), 'Doe');
    await userEvent.type(screen.getByLabelText('Email'), 'john@example.com');
    await userEvent.type(screen.getByLabelText('Password'), 'pass123');
    await userEvent.click(screen.getByRole('button', { name: 'Create Account' }));

    expect(mockRegisterMutate).toHaveBeenCalledWith(
      expect.objectContaining({
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
      }),
      expect.any(Object),
    );
  });
});
