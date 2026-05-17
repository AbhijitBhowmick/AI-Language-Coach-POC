'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useGameTemplates, useStartGame } from '@/lib/hooks/useApi';
import { useAuthStore } from '@/lib/stores/authStore';
import type { GameTemplateEntity } from '@/types';
import {
  Brain, Target, Type, Shuffle, Zap, Mic, Layers,
  Sparkles, Play, Loader2
} from 'lucide-react';

const fallbackTemplates: GameTemplateEntity[] = [
  { templateId: 'standard', displayName: 'Multiple Choice', templateCategory: 'VOCABULARY', displayOrder: 1, iconClass: 'fa-question-circle', description: 'Test your knowledge', defaultTimeSeconds: 30, pointsPerCorrect: 10, penaltyPoints: 0, livesEnabled: true, branchingEnabled: false, minQuestions: 5, maxQuestions: 20 },
  { templateId: 'match_up', displayName: 'Match the Pairs', templateCategory: 'VOCABULARY', displayOrder: 2, iconClass: 'fa-puzzle-piece', description: 'Match words with translations', defaultTimeSeconds: 60, pointsPerCorrect: 15, penaltyPoints: -5, livesEnabled: false, branchingEnabled: false, minQuestions: 6, maxQuestions: 16 },
  { templateId: 'cloze_text', displayName: 'Fill in the Blank', templateCategory: 'GRAMMAR', displayOrder: 3, iconClass: 'fa-edit', description: 'Complete the sentences', defaultTimeSeconds: 45, pointsPerCorrect: 10, penaltyPoints: 0, livesEnabled: false, branchingEnabled: false, minQuestions: 5, maxQuestions: 20 },
  { templateId: 'anagram', displayName: 'Anagram', templateCategory: 'VOCABULARY', displayOrder: 4, iconClass: 'fa-random', description: 'Unscramble the letters', defaultTimeSeconds: 30, pointsPerCorrect: 20, penaltyPoints: -5, livesEnabled: true, branchingEnabled: false, minQuestions: 5, maxQuestions: 15 },
  { templateId: 'whack_a_mole', displayName: 'Whack-a-Word', templateCategory: 'VOCABULARY', displayOrder: 5, iconClass: 'fa-bolt', description: 'Hit the correct word', defaultTimeSeconds: 20, pointsPerCorrect: 15, penaltyPoints: -10, livesEnabled: true, branchingEnabled: false, minQuestions: 5, maxQuestions: 20 },
  { templateId: 'speaking_card', displayName: 'Speaking Card', templateCategory: 'SPEAKING', displayOrder: 6, iconClass: 'fa-microphone', description: 'Practice pronunciation', defaultTimeSeconds: 90, pointsPerCorrect: 25, penaltyPoints: 0, livesEnabled: false, branchingEnabled: false, minQuestions: 3, maxQuestions: 10 },
  { templateId: 'group_sort', displayName: 'Group Sort', templateCategory: 'GRAMMAR', displayOrder: 8, iconClass: 'fa-layer-group', description: 'Categorize the words', defaultTimeSeconds: 60, pointsPerCorrect: 15, penaltyPoints: -5, livesEnabled: false, branchingEnabled: false, minQuestions: 5, maxQuestions: 15 },
];

const categories = [
  { value: 'all', label: 'All Games' },
  { value: 'SPEAKING', label: 'Speaking' },
  { value: 'GRAMMAR', label: 'Grammar' },
  { value: 'VOCABULARY', label: 'Vocabulary' },
];

const templateIcons: Record<string, typeof Brain> = {
  standard: Brain, match_up: Target, cloze_text: Type,
  anagram: Shuffle, whack_a_mole: Zap, speaking_card: Mic, group_sort: Layers,
};

const templateColors: Record<string, string> = {
  SPEAKING: 'bg-blue-500', GRAMMAR: 'bg-purple-500', VOCABULARY: 'bg-green-500', LISTENING: 'bg-amber-500',
};

export default function GamesPage() {
  const router = useRouter();
  const { user } = useAuthStore();
  const { data: apiTemplates, isLoading, error } = useGameTemplates();
  const { mutate: startGame, isPending: startingGame } = useStartGame();
  const [activeCategory, setActiveCategory] = useState('all');

  const templates = apiTemplates && apiTemplates.length > 0 ? apiTemplates : fallbackTemplates;

  const filteredGames = templates.filter(game => {
    const categoryMatch = activeCategory === 'all' || game.templateCategory === activeCategory;
    return categoryMatch;
  });

  const handleStartTest = () => {
    router.push('/games/diagnostic');
  };

  const handlePlay = (templateId: string) => {
    if (!user?.userId) return;
    startGame(
      { userId: user.userId, templateId },
      {
        onSuccess: (session) => {
          router.push(`/games/play/${session.sessionId}`);
        },
        onError: () => {
          router.push(`/games/play/demo-${templateId}`);
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Learning Hub</h1>
          <p className="text-muted-foreground mt-1">
            Practice with interactive games
          </p>
        </div>
      </div>

      <Card className="bg-gradient-to-r from-primary/10 to-purple-500/10 border-none">
        <CardContent className="p-6">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-2xl flex items-center justify-center">
              <Sparkles className="w-8 h-8 text-white" />
            </div>
            <div className="flex-1">
              <h2 className="text-xl font-semibold">Take a Diagnostic Test</h2>
              <p className="text-muted-foreground">
                Discover your current language level with our adaptive assessment
              </p>
            </div>
            <Button size="lg" onClick={handleStartTest}>
              <Play className="w-4 h-4 mr-2" />
              Start Test
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center gap-4">
        <Tabs value={activeCategory} onValueChange={setActiveCategory}>
          <TabsList>
            {categories.map(cat => (
              <TabsTrigger key={cat.value} value={cat.value}>{cat.label}</TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filteredGames.map((game) => {
            const Icon = templateIcons[game.templateId] || Brain;
            const color = templateColors[game.templateCategory] || 'bg-gray-500';
            return (
              <Card key={game.templateId} className="hover:shadow-lg transition-all hover:-translate-y-1 cursor-pointer group">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className={`w-14 h-14 ${color} rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform`}>
                      <Icon className="w-7 h-7 text-white" />
                    </div>
                    <span className="px-2 py-1 bg-surface-container rounded-full text-xs font-medium">
                      {game.templateCategory}
                    </span>
                  </div>
                  <h3 className="font-semibold text-lg">{game.displayName}</h3>
                  <p className="text-sm text-muted-foreground mt-1">{game.description}</p>
                  <div className="flex items-center justify-between mt-4">
                    <span className="text-sm text-muted-foreground">{game.minQuestions}-{game.maxQuestions} items</span>
                    <Button
                      size="sm"
                      onClick={() => handlePlay(game.templateId)}
                      disabled={startingGame}
                    >
                      {startingGame ? <Loader2 className="w-4 h-4 mr-1 animate-spin" /> : <Play className="w-4 h-4 mr-1" />}
                      Play
                    </Button>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {!isLoading && filteredGames.length === 0 && (
        <div className="text-center py-12">
          <p className="text-muted-foreground">No games found for the selected filters</p>
        </div>
      )}

      {error && (
        <p className="text-xs text-muted-foreground text-center">
          Using fallback game data — backend diagnostic service unavailable
        </p>
      )}
    </div>
  );
}
