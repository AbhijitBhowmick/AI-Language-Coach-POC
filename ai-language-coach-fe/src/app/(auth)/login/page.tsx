'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { useLogin, useRegister } from '@/lib/hooks/useApi';
import { profileApi } from '@/lib/api/client';
import { useAuthStore } from '@/lib/stores/authStore';

type AppScreen = 'login' | 'register' | 'pending' | 'rejected' | 'suspended';

export default function LoginPage() {
  const router = useRouter();
  const [screen, setScreen] = useState<AppScreen>('login');
  const [error, setError] = useState('');
  const [rejectReason, setRejectReason] = useState('');

  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [registerData, setRegisterData] = useState({
    firstName: '', lastName: '', email: '', password: '',
    targetLanguage: 'Czech', targetLevel: 'A1', nativeLanguage: 'en',
  });

  const { mutate: login, isPending: isLoggingIn } = useLogin();
  const { mutate: register, isPending: isRegisteringUser } = useRegister();
  const authStore = useAuthStore();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    login(loginData, {
      onSuccess: (data) => {
        if (data.status === 'PENDING_APPROVAL') {
          setScreen('pending');
          return;
        }
        if (data.status === 'REJECTED') {
          setRejectReason('Your registration has been rejected.');
          setScreen('rejected');
          return;
        }
        if (data.status === 'SUSPENDED') {
          setScreen('suspended');
          return;
        }
        authStore.login(data);
        // Auto-create learning profile after successful login
        profileApi.create(
          { targetLanguage: 'Czech', targetLevel: 'A1', nativeLanguage: 'en' },
          data.accessToken
        ).catch(() => {});
        router.push('/dashboard');
      },
      onError: (err: Error) => {
        setError(err.message || 'Login failed');
      },
    });
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    register({ ...registerData, role: 'USER_STUDENT' }, {
      onSuccess: (data) => {
        if (data.status === 'PENDING_APPROVAL') {
          setScreen('pending');
        } else if (data.status === 'ACTIVE') {
          authStore.login(data);
          // Auto-create learning profile with registration preferences
          profileApi.create(
            { targetLanguage: registerData.targetLanguage, targetLevel: registerData.targetLevel, nativeLanguage: registerData.nativeLanguage },
            data.accessToken
          ).catch(() => {});
          router.push('/dashboard');
        } else {
          setScreen('login');
          setError('Registration successful! Please login.');
        }
      },
      onError: (err: Error) => {
        setError(err.message || 'Registration failed');
      },
    });
  };

  if (screen === 'pending') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/5 to-primary/10 p-4">
        <Card className="w-full max-w-md text-center">
          <CardHeader>
            <div className="w-16 h-16 bg-yellow-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl text-white font-bold">!</span>
            </div>
            <CardTitle className="text-2xl">Awaiting Approval</CardTitle>
            <CardDescription>
              Your account is pending approval from an administrator. You will be notified once your account is activated.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" className="w-full" onClick={() => setScreen('login')}>
              Back to Login
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (screen === 'rejected') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/5 to-primary/10 p-4">
        <Card className="w-full max-w-md text-center">
          <CardHeader>
            <div className="w-16 h-16 bg-destructive rounded-2xl flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl text-white font-bold">X</span>
            </div>
            <CardTitle className="text-2xl">Registration Rejected</CardTitle>
            <CardDescription>{rejectReason}</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" className="w-full" onClick={() => setScreen('login')}>
              Back to Login
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (screen === 'suspended') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/5 to-primary/10 p-4">
        <Card className="w-full max-w-md text-center">
          <CardHeader>
            <div className="w-16 h-16 bg-destructive rounded-2xl flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl text-white font-bold">!</span>
            </div>
            <CardTitle className="text-2xl">Account Suspended</CardTitle>
            <CardDescription>
              Your account has been suspended. Please contact your administrator for more information.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" className="w-full" onClick={() => setScreen('login')}>
              Back to Login
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/5 to-primary/10 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="w-16 h-16 bg-primary rounded-2xl flex items-center justify-center mx-auto mb-4">
            <span className="text-3xl text-white font-bold">L</span>
          </div>
          <CardTitle className="text-2xl">LingoAI</CardTitle>
          <CardDescription>
            {screen === 'register' ? 'Create your account' : 'Sign in to continue learning'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {screen === 'login' ? (
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  value={loginData.email}
                  onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={loginData.password}
                  onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                  required
                />
              </div>
              {error && (
                <p className="text-sm text-destructive">{error}</p>
              )}
              <Button type="submit" className="w-full" disabled={isLoggingIn}>
                {isLoggingIn ? 'Signing in...' : 'Sign In'}
              </Button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    placeholder="John"
                    value={registerData.firstName}
                    onChange={(e) => setRegisterData({ ...registerData, firstName: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    placeholder="Doe"
                    value={registerData.lastName}
                    onChange={(e) => setRegisterData({ ...registerData, lastName: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="regEmail">Email</Label>
                <Input
                  id="regEmail"
                  type="email"
                  placeholder="you@example.com"
                  value={registerData.email}
                  onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="registerPassword">Password</Label>
                <Input
                  id="registerPassword"
                  type="password"
                  placeholder="••••••••"
                  value={registerData.password}
                  onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                  required
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="targetLanguage">Target Language</Label>
                  <Input
                    id="targetLanguage"
                    placeholder="Czech"
                    value={registerData.targetLanguage}
                    onChange={(e) => setRegisterData({ ...registerData, targetLanguage: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="targetLevel">Level</Label>
                  <select
                    id="targetLevel"
                    className="w-full px-3 py-2 rounded-lg border bg-background"
                    value={registerData.targetLevel}
                    onChange={(e) => setRegisterData({ ...registerData, targetLevel: e.target.value })}
                  >
                    <option value="A1">A1 - Beginner</option>
                    <option value="A2">A2 - Elementary</option>
                    <option value="B1">B1 - Intermediate</option>
                    <option value="B2">B2 - Upper Intermediate</option>
                    <option value="C1">C1 - Advanced</option>
                    <option value="C2">C2 - Proficient</option>
                  </select>
                </div>
              </div>
              {error && (
                <p className="text-sm text-destructive">{error}</p>
              )}
              <Button type="submit" className="w-full" disabled={isRegisteringUser}>
                {isRegisteringUser ? 'Creating account...' : 'Create Account'}
              </Button>
            </form>
          )}

          <div className="mt-6 text-center text-sm">
            <button
              type="button"
              className="text-primary hover:underline"
              onClick={() => {
                setScreen(screen === 'login' ? 'register' : 'login');
                setError('');
              }}
            >
              {screen === 'login'
                ? "Don't have an account? Register"
                : 'Already have an account? Sign in'}
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
