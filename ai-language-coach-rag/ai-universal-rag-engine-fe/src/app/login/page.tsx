'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLogin } from '@/lib/api';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [registerEmail, setRegisterEmail] = useState('');
  const [registerPassword, setRegisterPassword] = useState('');

  const loginMutation = useLogin();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      const response = await loginMutation.mutateAsync({
        username: email,
        password: password,
      });

      localStorage.setItem('auth_token', response.token);
      localStorage.setItem('user_email', response.user.email);
      localStorage.setItem('user_id', response.user.id);

      router.push('/configuration');
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Login failed. Please check your credentials.';
      setError(errorMessage);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_JAVA_API_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: registerEmail,
          password: registerPassword,
          firstName,
          lastName,
        }),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Registration failed');
      }

      setIsRegistering(false);
      setError('Registration successful! Please login.');
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Registration failed. Please try again.';
      setError(errorMessage);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100">
      <div className="card w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-primary-700">AI Language Coach</h1>
          <p className="text-gray-600 mt-2">
            {isRegistering ? 'Create your account' : 'Welcome back'}
          </p>
        </div>

        {error && (
          <div className={`mb-4 p-3 rounded-lg ${error.includes('successful') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            {error}
          </div>
        )}

        {!isRegistering ? (
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input-field"
                placeholder="your@email.com"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="input-field"
                placeholder="••••••••"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loginMutation.isPending}
              className="btn-primary w-full"
            >
              {loginMutation.isPending ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                  First Name
                </label>
                <input
                  id="firstName"
                  type="text"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="input-field"
                  required
                />
              </div>
              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                  Last Name
                </label>
                <input
                  id="lastName"
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="input-field"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="registerEmail" className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                id="registerEmail"
                type="email"
                value={registerEmail}
                onChange={(e) => setRegisterEmail(e.target.value)}
                className="input-field"
                required
              />
            </div>

            <div>
              <label htmlFor="registerPassword" className="block text-sm font-medium text-gray-700 mb-1">
                Password
              </label>
              <input
                id="registerPassword"
                type="password"
                value={registerPassword}
                onChange={(e) => setRegisterPassword(e.target.value)}
                className="input-field"
                required
              />
            </div>

            <button
              type="submit"
              className="btn-primary w-full"
            >
              Create Account
            </button>
          </form>
        )}

        <div className="mt-6 text-center">
          <button
            type="button"
            onClick={() => {
              setIsRegistering(!isRegistering);
              setError('');
            }}
            className="text-primary-600 hover:text-primary-700 text-sm"
          >
            {isRegistering
              ? 'Already have an account? Sign in'
              : "Don't have an account? Register"}
          </button>
        </div>
      </div>
    </div>
  );
}
