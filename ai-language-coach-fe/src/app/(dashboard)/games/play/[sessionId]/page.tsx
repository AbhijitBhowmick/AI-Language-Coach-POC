'use client';

import { useState, useEffect, use, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { diagnosticApi } from '@/lib/api/client';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Loader2, Heart, Star, SkipForward, Check, X } from 'lucide-react';
import type { GameSession, GameResult, GameRenderData } from '@/types';

const LIVES = 5;

interface AnswerState {
  selectedAnswer?: string;
  filledGaps?: string[];
  unscrambledAnswer?: string;
  matchedPairs?: Record<string, string>;
  sortedCategory?: string;
  sortedItems?: string[];
}

function renderQuestion(
  initialRender: GameRenderData,
  answerState: AnswerState,
  onAnswerStateChange: (state: AnswerState) => void,
  onAnswer: (answer: Record<string, unknown>) => void
) {
  const { templateId, questionText, options, answerSlots, scrambledWords, targetItems, categories, sortItems } = initialRender;

  switch (templateId) {
    case 'standard':
    case 'branching':
      return (
        <div className="space-y-4 w-full max-w-xl">
          <p className="text-xl font-medium text-center">{questionText}</p>
          <div className="grid grid-cols-1 gap-3">
            {(options || []).map((opt, i) => (
              <button
                key={i}
                onClick={() => onAnswer({ answer: opt })}
                className="w-full p-4 rounded-xl border-2 border-border hover:border-primary hover:bg-primary/5 transition-all text-left font-medium"
              >
                {opt}
              </button>
            ))}
          </div>
        </div>
      );

    case 'match_up': {
      const words = targetItems ? Object.keys(targetItems) : [];
      const translations = targetItems ? Object.values(targetItems) as string[] : [];
      const selectedPair = answerState.matchedPairs || {};
      return (
        <div className="space-y-6 w-full max-w-2xl">
          <p className="text-lg font-medium text-center">{questionText}</p>
          <div className="grid grid-cols-2 gap-8">
            <div className="space-y-3">
              {words.map((word, i) => (
                <div
                  key={i}
                  onClick={() => {
                    const current = { ...selectedPair };
                    if (current[word]) {
                      delete current[word];
                    } else if (Object.keys(current).length < words.length) {
                      current[word] = '';
                    }
                    onAnswerStateChange({ matchedPairs: current });
                  }}
                  className={`p-4 rounded-xl border-2 text-center font-semibold text-lg cursor-pointer transition-all ${
                    word in selectedPair ? 'border-primary bg-primary/10' : 'border-primary/30'
                  }`}
                >
                  {word}
                </div>
              ))}
            </div>
            <div className="space-y-3">
              {translations.map((trans, i) => {
                const isPaired = Object.values(selectedPair).includes(trans);
                return (
                  <div
                    key={i}
                    onClick={() => {
                      const unpairedWord = Object.keys(selectedPair).find((w) => !selectedPair[w]);
                      if (unpairedWord) {
                        onAnswerStateChange({
                          matchedPairs: { ...selectedPair, [unpairedWord]: trans },
                        });
                      }
                    }}
                    className={`p-4 rounded-xl border-2 text-center font-semibold text-lg cursor-pointer transition-all ${
                      isPaired ? 'border-primary bg-primary/10' : 'border-border hover:border-primary'
                    }`}
                  >
                    {trans}
                  </div>
                );
              })}
            </div>
          </div>
          <Button
            className="w-full"
            onClick={() => onAnswer({ matchedPairs: selectedPair })}
            disabled={Object.keys(selectedPair).length < words.length}
          >
            Check Matches
          </Button>
        </div>
      );
    }

    case 'cloze_text': {
      const gaps = answerState.filledGaps || answerSlots?.map(() => '') || [];
      return (
        <div className="space-y-6 w-full max-w-xl">
          <p className="text-lg font-medium text-center">Fill in the blanks:</p>
          <p className="text-xl bg-surface-container p-6 rounded-xl leading-relaxed">{questionText}</p>
          <div className="space-y-3">
            {(answerSlots || ['']).map((_, i) => (
              <Input
                key={i}
                value={gaps[i] || ''}
                onChange={(e) => {
                  const next = [...gaps];
                  next[i] = e.target.value;
                  onAnswerStateChange({ filledGaps: next });
                }}
                className="w-full h-12 px-4 rounded-xl border border-input bg-transparent focus:border-ring outline-none"
                placeholder={`Answer ${i + 1}`}
              />
            ))}
          </div>
          <Button className="w-full" onClick={() => onAnswer({ filledGaps: gaps })}>
            Submit Answers
          </Button>
        </div>
      );
    }

    case 'anagram': {
      const unscrambled = answerState.unscrambledAnswer || '';
      return (
        <div className="space-y-6 w-full max-w-lg text-center">
          <p className="text-xl font-medium">Unscramble the word:</p>
          <div className="flex justify-center gap-3 flex-wrap">
            {(scrambledWords || ['']).map((letter, i) => (
              <div key={i} className="w-14 h-14 bg-primary/10 rounded-xl flex items-center justify-center text-2xl font-bold text-primary">
                {letter}
              </div>
            ))}
          </div>
          <Input
            value={unscrambled}
            onChange={(e) => onAnswerStateChange({ unscrambledAnswer: e.target.value })}
            className="w-full h-12 px-4 rounded-xl border border-input bg-transparent text-center text-xl font-bold focus:border-ring outline-none"
            placeholder="Your answer..."
          />
          <Button className="w-full" onClick={() => onAnswer({ answer: unscrambled })} disabled={!unscrambled.trim()}>
            Check
          </Button>
        </div>
      );
    }

    case 'group_sort': {
      const catNames = categories || ['Category 1', 'Category 2'];
      const items = sortItems || [];
      const sorted = answerState.sortedCategory || '';
      return (
        <div className="space-y-6 w-full max-w-2xl">
          <p className="text-lg font-medium text-center">{questionText}</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {catNames.map((cat, i) => {
              const isSelected = sorted === cat;
              return (
                <div
                  key={i}
                  onClick={() => onAnswerStateChange({ sortedCategory: cat })}
                  className={`p-4 rounded-xl min-h-[120px] border-2 transition-all cursor-pointer ${
                    isSelected ? 'border-primary bg-primary/5' : 'border-dashed border-border'
                  }`}
                >
                  <p className="font-semibold text-sm text-center mb-2">{cat}</p>
                  <div className="flex flex-wrap gap-2 justify-center">
                    {isSelected && items.map((item, j) => (
                      <span key={j} className="px-3 py-1 bg-white rounded-lg border text-xs font-medium">
                        {item}
                      </span>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
          <div className="flex justify-center gap-3 flex-wrap">
            {items.map((item, i) => (
              <div
                key={i}
                className="px-4 py-2 bg-white rounded-lg border border-border cursor-pointer hover:border-primary transition-all text-sm font-medium"
              >
                {item}
              </div>
            ))}
          </div>
          <Button
            className="w-full"
            onClick={() => onAnswer({ answer: sorted })}
            disabled={!sorted}
          >
            Submit
          </Button>
        </div>
      );
    }

    case 'speaking_card':
      return (
        <div className="space-y-6 w-full max-w-xl text-center">
          <p className="text-xl font-medium">{questionText}</p>
          <div className="w-24 h-24 bg-primary rounded-full flex items-center justify-center mx-auto cursor-pointer hover:scale-105 transition-transform">
            <svg className="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z" />
              <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z" />
            </svg>
          </div>
          <p className="text-sm text-muted-foreground">Tap the microphone and speak your answer</p>
          <Button className="w-full" onClick={() => onAnswer({ answer: 'recorded' })}>
            Submit Recording
          </Button>
        </div>
      );

    case 'whack_a_mole':
      return (
        <div className="space-y-6 w-full max-w-xl text-center">
          <p className="text-xl font-medium">{questionText}</p>
          <div className="grid grid-cols-3 gap-4">
            {(options || ['Option 1', 'Option 2', 'Option 3', 'Option 4', 'Option 5', 'Option 6']).map((opt, i) => (
              <button
                key={i}
                onClick={() => onAnswer({ answer: opt })}
                className="p-6 bg-white rounded-xl border-2 border-border hover:border-primary hover:bg-primary/5 transition-all font-bold text-lg"
              >
                {opt}
              </button>
            ))}
          </div>
        </div>
      );

    default:
      return (
        <div className="text-center text-muted-foreground">
          <p>Unknown game type: {templateId}</p>
        </div>
      );
  }
}

export default function GamePlayPage(props: { params: Promise<{ sessionId: string }> }) {
  const params = use(props.params);
  const router = useRouter();
  const [session, setSession] = useState<GameSession | null>(null);
  const [initialRender, setRenderData] = useState<GameRenderData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lives, setLives] = useState(LIVES);
  const [score, setScore] = useState(0);
  const [feedback, setFeedback] = useState<GameResult | null>(null);
  const [answerState, setAnswerState] = useState<AnswerState>({});

  useEffect(() => {
    loadGame();
  }, [params.sessionId]);

  const loadGame = async () => {
    try {
      const question = await diagnosticApi.getGameQuestion(params.sessionId);
      const gameSession = question as unknown as GameSession;
      setSession(gameSession);
      if (gameSession.initialRender) {
        setRenderData(gameSession.initialRender);
        setLives(gameSession.initialRender.livesRemaining);
        setScore(gameSession.initialRender.currentScore);
      }
    } catch {
      setError('Failed to load game. The session may have expired.');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswer = useCallback(async (answerPayload: Record<string, unknown>) => {
    if (!session) return;
    try {
      const result = await diagnosticApi.submitGameAnswer(params.sessionId, {
        sessionId: params.sessionId,
        questionIndex: initialRender?.questionNumber || 0,
        ...answerPayload,
        responseTimeMs: 0,
        timeout: false,
      });
      setFeedback(result);
      if (!result.correct) setLives((p) => p - 1);
      setScore(result.totalScore);

      if (result.isCompleted || lives <= 1) {
        setTimeout(() => router.push('/games'), 2000);
      }
    } catch {
      setError('Failed to submit answer.');
    }
  }, [session, params.sessionId, initialRender, lives, router]);

  const handleContinue = () => {
    setFeedback(null);
    setAnswerState({});
    loadGame();
  };

  if (loading) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <div className="text-center space-y-4">
          <p className="text-destructive">{error}</p>
          <Button onClick={() => router.push('/games')}>Back to Games</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[80vh] flex flex-col">
      <div className="flex items-center justify-between px-6 py-4 bg-white/80 backdrop-blur-md sticky top-0 z-10 border-b">
        <div className="flex items-center gap-4 flex-1 max-w-md">
          <div className="flex-1 h-3 bg-surface-container rounded-full overflow-hidden">
            <div
              className="h-full bg-primary rounded-full transition-all duration-500"
              style={{ width: `${initialRender ? ((initialRender.questionNumber / initialRender.totalQuestions) * 100) : 0}%` }}
            />
          </div>
        </div>
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2 px-4 py-1.5 bg-primary/5 rounded-xl">
            <Star className="w-4 h-4 text-primary" />
            <span className="font-bold text-primary">{score} pts</span>
          </div>
          <div className="flex items-center gap-1">
            {Array.from({ length: lives }).map((_, i) => (
              <Heart key={i} className="w-5 h-5 text-destructive fill-destructive" />
            ))}
            {Array.from({ length: Math.max(0, LIVES - lives) }).map((_, i) => (
              <Heart key={`lost-${i}`} className="w-5 h-5 text-muted-foreground" />
            ))}
          </div>
        </div>
      </div>

      <div className="flex-1 flex items-center justify-center p-8">
        {initialRender && renderQuestion(initialRender, answerState, setAnswerState, handleAnswer)}
      </div>

      <div className="border-t bg-white p-4">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <Button variant="outline" onClick={() => router.push('/games')}>
            <X className="w-4 h-4 mr-2" /> Quit
          </Button>
          <p className="text-sm text-muted-foreground">
            Question {initialRender?.questionNumber || 0} of {initialRender?.totalQuestions || 0}
          </p>
          <Button variant="ghost" onClick={() => handleAnswer({ answer: '', timeout: true })}>
            Skip <SkipForward className="w-4 h-4 ml-2" />
          </Button>
        </div>
      </div>

      {feedback && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-sm">
          <div className="bg-white rounded-2xl p-8 shadow-xl max-w-sm w-full mx-4 text-center space-y-4">
            {feedback.correct ? (
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
                <Check className="w-8 h-8 text-green-600" />
              </div>
            ) : (
              <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto">
                <X className="w-8 h-8 text-red-600" />
              </div>
            )}
            <p className="text-xl font-bold">{feedback.correct ? 'Correct!' : 'Incorrect'}</p>
            {feedback.explanation && (
              <p className="text-sm text-muted-foreground">{feedback.explanation}</p>
            )}
            <p className="text-sm">+{feedback.pointsEarned} points</p>
            <Button onClick={handleContinue} className="w-full">
              Continue
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
