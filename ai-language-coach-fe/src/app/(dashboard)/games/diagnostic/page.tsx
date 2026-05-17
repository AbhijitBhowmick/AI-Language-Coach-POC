'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { useAuthStore } from '@/lib/stores/authStore';
import { useStartDiagnostic, useDiagnosticQuestion, useSubmitAnswer, useDiagnosticResult } from '@/lib/hooks/useApi';
import { Loader2, Timer, Sparkles, Info, ArrowRight, Check, X } from 'lucide-react';
import type { DiagnosticTest } from '@/types';

const LANGUAGES = ['Czech', 'Slovak', 'Polish', 'Spanish', 'French', 'German'];
const LEVELS = [
  { value: 'A1', label: 'A1 - Absolute Beginner' },
  { value: 'A2', label: 'A2 - Elementary' },
  { value: 'B1', label: 'B1 - Intermediate' },
  { value: 'B2', label: 'B2 - Upper Intermediate' },
];

function SetupForm({
  targetLanguage, setTargetLanguage,
  targetLevel, setTargetLevel,
  error, isPending, onStart,
}: {
  targetLanguage: string; setTargetLanguage: (v: string) => void;
  targetLevel: string; setTargetLevel: (v: string) => void;
  error: string; isPending: boolean; onStart: (e: React.FormEvent) => void;
}) {
  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center max-w-5xl w-full">
        <div className="space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">
            <Sparkles className="w-4 h-4" />
            Official Diagnostics
          </div>
          <h1 className="text-4xl font-bold text-on-surface">Level Placement Test</h1>
          <p className="text-muted-foreground leading-relaxed">
            Assess your current proficiency across 10 interactive questions.
            Our AI-driven engine analyzes your grammar, vocabulary, and phonetic patterns
            to determine the perfect starting point.
          </p>
          <div className="space-y-4">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg bg-white flex items-center justify-center shadow-sm border">
                <Timer className="w-5 h-5 text-primary" />
              </div>
              <div>
                <h4 className="font-semibold text-on-surface">Approx. 5 Minutes</h4>
                <p className="text-sm text-muted-foreground">A quick but deep dive into your skills.</p>
              </div>
            </div>
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg bg-white flex items-center justify-center shadow-sm border">
                <Sparkles className="w-5 h-5 text-primary" />
              </div>
              <div>
                <h4 className="font-semibold text-on-surface">Adaptive Content</h4>
                <p className="text-sm text-muted-foreground">Questions evolve based on your performance.</p>
              </div>
            </div>
          </div>
        </div>

        <Card className="shadow-xl">
          <CardHeader>
            <CardTitle>Configure Test</CardTitle>
            <CardDescription>Set your preferences to begin the assessment.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={onStart} className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="targetLanguage">Target Language</Label>
                <select
                  id="targetLanguage"
                  className="w-full h-12 px-4 rounded-xl border border-input bg-transparent focus:border-ring focus:ring-2 focus:ring-ring/50 outline-none"
                  value={targetLanguage}
                  onChange={(e) => setTargetLanguage(e.target.value)}
                >
                  {LANGUAGES.map((lang) => (
                    <option key={lang} value={lang}>{lang}</option>
                  ))}
                </select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="targetLevel">Estimated Starting Level</Label>
                <select
                  id="targetLevel"
                  className="w-full h-12 px-4 rounded-xl border border-input bg-transparent focus:border-ring focus:ring-2 focus:ring-ring/50 outline-none"
                  value={targetLevel}
                  onChange={(e) => setTargetLevel(e.target.value)}
                >
                  {LEVELS.map((l) => (
                    <option key={l.value} value={l.value}>{l.label}</option>
                  ))}
                </select>
                <p className="text-xs text-muted-foreground italic">
                  If unsure, select &quot;Absolute Beginner&quot; for the most comprehensive test.
                </p>
              </div>

              <div className="bg-surface-container p-4 rounded-xl border">
                <div className="flex gap-3 items-start">
                  <Info className="w-5 h-5 text-primary mt-0.5" />
                  <div>
                    <p className="font-medium text-sm">Test Protocols</p>
                    <ul className="text-sm text-muted-foreground list-disc list-inside mt-1 space-y-1">
                      <li>Do not use external translation tools.</li>
                      <li>Results will be saved to your profile immediately.</li>
                    </ul>
                  </div>
                </div>
              </div>

              {error && <p className="text-sm text-destructive">{error}</p>}

              <Button type="submit" className="w-full h-14 text-lg" disabled={isPending}>
                {isPending ? (
                  <><Loader2 className="w-5 h-5 animate-spin mr-2" /> Starting...</>
                ) : (
                  <><span>Begin Test</span> <ArrowRight className="w-5 h-5 ml-2" /></>
                )}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function QuestionView({
  question, selectedAnswer, setSelectedAnswer, onAnswer, feedback, onNext, submitting, error,
}: {
  question: NonNullable<ReturnType<typeof useDiagnosticQuestion>['data']>;
  selectedAnswer: string; setSelectedAnswer: (v: string) => void;
  onAnswer: () => void; feedback: { correct: boolean; correctAnswer: string; explanation?: string } | null;
  onNext: () => void; submitting: boolean; error: string;
}) {
  if (feedback) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <Card className="max-w-md w-full">
          <CardContent className="p-8 text-center space-y-4">
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
            {!feedback.correct && (
              <p className="text-sm text-muted-foreground">Correct answer: {feedback.correctAnswer}</p>
            )}
            {feedback.explanation && <p className="text-sm text-muted-foreground">{feedback.explanation}</p>}
            <Button className="w-full" onClick={onNext}>
              Next Question
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="max-w-2xl w-full space-y-6">
        <div className="flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-primary" />
          <span className="text-sm font-medium">Question {question.questionNumber}</span>
          <span className="text-sm text-muted-foreground">• {question.type.replace(/_/g, ' ')}</span>
        </div>

        <Card>
          <CardContent className="p-8">
            <h2 className="text-xl font-semibold mb-6">{question.questionText}</h2>
            <div className="space-y-3">
              {(question.options || []).map((opt, i) => (
                <button
                  key={i}
                  onClick={() => setSelectedAnswer(opt)}
                  className={`w-full p-4 rounded-xl border-2 text-left font-medium transition-all ${
                    selectedAnswer === opt
                      ? 'border-primary bg-primary/5'
                      : 'border-border hover:border-primary/50'
                  }`}
                >
                  {opt}
                </button>
              ))}
            </div>

            {error && <p className="text-sm text-destructive mt-4">{error}</p>}

            <Button
              className="w-full mt-6"
              onClick={onAnswer}
              disabled={!selectedAnswer || submitting}
            >
              {submitting ? <Loader2 className="w-4 h-4 animate-spin mr-2" /> : null}
              Submit Answer
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default function DiagnosticPage() {
  const { token } = useAuthStore();
  const startDiagnostic = useStartDiagnostic(token || '');
  const [started, setStarted] = useState(false);

  const { data: question, isLoading: questionLoading, refetch: refetchQuestion } = useDiagnosticQuestion(token || '');
  const { mutate: submitAnswer, isPending: submitting } = useSubmitAnswer(token || '');
  const { data: result, isLoading: resultLoading } = useDiagnosticResult(token || '');

  const [targetLanguage, setTargetLanguage] = useState('Czech');
  const [targetLevel, setTargetLevel] = useState('A1');
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState<{ correct: boolean; correctAnswer: string; explanation?: string } | null>(null);
  const [selectedAnswer, setSelectedAnswer] = useState('');

  const handleStart = (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;
    setError('');

    startDiagnostic.mutate(
      { targetLanguage, targetLevel },
      {
        onSuccess: () => {
          setStarted(true);
        },
        onError: (err: Error) => {
          setError(err.message || 'Failed to start diagnostic. Please try again.');
        },
      }
    );
  };

  const handleAnswer = () => {
    if (!question || !selectedAnswer) return;
    submitAnswer(
      { questionNumber: question.questionNumber, answer: selectedAnswer },
      {
        onSuccess: (_data: DiagnosticTest) => {
          setFeedback({
            correct: selectedAnswer === question.correctAnswer,
            correctAnswer: question.correctAnswer,
            explanation: question.explanation,
          });
        },
        onError: (err: Error) => {
          setError(err.message || 'Failed to submit answer.');
        },
      }
    );
  };

  const handleNext = () => {
    setFeedback(null);
    setSelectedAnswer('');
    refetchQuestion();
  };

  if (!started) {
    return (
      <SetupForm
        targetLanguage={targetLanguage} setTargetLanguage={setTargetLanguage}
        targetLevel={targetLevel} setTargetLevel={setTargetLevel}
        error={error} isPending={startDiagnostic.isPending} onStart={handleStart}
      />
    );
  }

  if (resultLoading) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (questionLoading) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!question) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center text-center space-y-4">
        <div>
          <p className="text-lg font-medium">Test Complete!</p>
          {result && (
            <div className="mt-4 space-y-2">
              <p>Score: {result.scorePercentage}%</p>
              <p>Correct: {result.correctAnswers}/{result.totalQuestions}</p>
              <p>Recommended Level: {result.recommendedLevel}</p>
            </div>
          )}
          <Button className="mt-6" onClick={() => setStarted(false)}>Back to Setup</Button>
        </div>
      </div>
    );
  }

  return (
    <QuestionView
      question={question}
      selectedAnswer={selectedAnswer} setSelectedAnswer={setSelectedAnswer}
      onAnswer={handleAnswer}
      feedback={feedback}
      onNext={handleNext}
      submitting={submitting}
      error={error}
    />
  );
}
