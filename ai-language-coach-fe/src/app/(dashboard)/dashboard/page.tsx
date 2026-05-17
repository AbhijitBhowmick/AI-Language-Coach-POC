'use client';

import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/card';
import { useAuthStore } from '@/lib/stores/authStore';
import { Mic, FlaskConical, Gamepad2, Users, Flame, Trophy, Target } from 'lucide-react';

const quickActions = [
  { 
    href: '/practice', 
    label: 'Voice Practice', 
    icon: Mic, 
    color: 'bg-blue-500',
    description: 'Practice speaking with AI'
  },
  { 
    href: '/games', 
    label: 'Diagnostic Test', 
    icon: FlaskConical, 
    color: 'bg-purple-500',
    description: 'Assess your level'
  },
  { 
    href: '/games', 
    label: 'Games', 
    icon: Gamepad2, 
    color: 'bg-green-500',
    description: 'Learn through play'
  },
  { 
    href: '/community', 
    label: 'Community', 
    icon: Users, 
    color: 'bg-orange-500',
    description: 'Join the community'
  },
];

export default function DashboardPage() {
  const { user } = useAuthStore();

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-on-surface">
          Welcome back, {user?.firstName || 'Learner'}!
        </h1>
        <p className="text-muted-foreground mt-1">
          Continue your language learning journey
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {quickActions.map((action) => (
          <Link key={action.label} href={action.href}>
            <Card className="hover:shadow-lg transition-shadow cursor-pointer group">
              <CardContent className="p-6">
                <div className={`w-12 h-12 ${action.color} rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                  <action.icon className="w-6 h-6 text-white" />
                </div>
                <h3 className="font-semibold text-on-surface">{action.label}</h3>
                <p className="text-sm text-muted-foreground mt-1">{action.description}</p>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardContent className="p-6">
            <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <Flame className="w-5 h-5 text-orange-500" />
              Your Progress
            </h2>
            <div className="grid grid-cols-3 gap-4">
              <div className="bg-surface-container rounded-xl p-4 text-center">
                <div className="text-3xl font-bold text-primary">7</div>
                <div className="text-sm text-muted-foreground">Day Streak</div>
              </div>
              <div className="bg-surface-container rounded-xl p-4 text-center">
                <div className="text-3xl font-bold text-green-500">1,250</div>
                <div className="text-sm text-muted-foreground">Points</div>
              </div>
              <div className="bg-surface-container rounded-xl p-4 text-center">
                <div className="text-3xl font-bold text-purple-500">A2</div>
                <div className="text-sm text-muted-foreground">Current Level</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <Trophy className="w-5 h-5 text-yellow-500" />
              Weekly Goals
            </h2>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span>Practice Sessions</span>
                  <span className="text-muted-foreground">3/5</span>
                </div>
                <div className="h-2 bg-surface-container rounded-full overflow-hidden">
                  <div className="h-full bg-primary rounded-full" style={{ width: '60%' }} />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span>Words Learned</span>
                  <span className="text-muted-foreground">45/100</span>
                </div>
                <div className="h-2 bg-surface-container rounded-full overflow-hidden">
                  <div className="h-full bg-green-500 rounded-full" style={{ width: '45%' }} />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span>Games Played</span>
                  <span className="text-muted-foreground">2/10</span>
                </div>
                <div className="h-2 bg-surface-container rounded-full overflow-hidden">
                  <div className="h-full bg-purple-500 rounded-full" style={{ width: '20%' }} />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent className="p-6">
          <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <Target className="w-5 h-5 text-red-500" />
            Recommended Next Steps
          </h2>
          <div className="space-y-3">
            <div className="flex items-center gap-4 p-4 bg-surface-container rounded-xl">
              <Mic className="w-8 h-8 text-blue-500" />
              <div className="flex-1">
                <p className="font-medium">Complete a voice practice session</p>
                <p className="text-sm text-muted-foreground">Improve your speaking skills</p>
              </div>
              <Link 
                href="/practice" 
                className="px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary/90"
              >
                Start
              </Link>
            </div>
            <div className="flex items-center gap-4 p-4 bg-surface-container rounded-xl">
              <FlaskConical className="w-8 h-8 text-purple-500" />
              <div className="flex-1">
                <p className="font-medium">Take a diagnostic test</p>
                <p className="text-sm text-muted-foreground">Check your current level</p>
              </div>
              <Link 
                href="/games" 
                className="px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary/90"
              >
                Start
              </Link>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}