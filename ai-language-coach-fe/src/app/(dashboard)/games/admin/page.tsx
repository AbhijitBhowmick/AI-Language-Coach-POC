'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { useAuthStore } from '@/lib/stores/authStore';
import { useCreateGameTemplate } from '@/lib/hooks/useApi';
import { Loader2, Plus, Trash2, ArrowLeft, Save, ShieldCheck } from 'lucide-react';
import type { GameCategory } from '@/types';

const CATEGORIES: { value: GameCategory; label: string }[] = [
  { value: 'VOCABULARY', label: 'Vocabulary' },
  { value: 'GRAMMAR', label: 'Grammar' },
  { value: 'LISTENING', label: 'Listening' },
  { value: 'SPEAKING', label: 'Speaking' },
  { value: 'CUSTOM', label: 'Custom' },
];

interface QuestionEntry {
  questionText: string;
  options: string[];
  correctAnswer: string;
}

const emptyQuestion = (): QuestionEntry => ({ questionText: '', options: ['', '', '', ''], correctAnswer: '' });

export default function GamesAdminPage() {
  const router = useRouter();
  const { user, token } = useAuthStore();
  const { mutate: createTemplate, isPending } = useCreateGameTemplate(token || '');

  const isAdmin = user?.role === 'SUPER_ADMIN' || user?.role === 'BUSINESS_ADMIN' || user?.role === 'ADMIN_TEACHER';
  const [displayName, setDisplayName] = useState('');
  const [templateCategory, setTemplateCategory] = useState<GameCategory>('VOCABULARY');
  const [description, setDescription] = useState('');
  const [defaultTimeSeconds, setDefaultTimeSeconds] = useState(30);
  const [questions, setQuestions] = useState<QuestionEntry[]>([emptyQuestion()]);

  const updateQuestion = (index: number, field: keyof QuestionEntry, value: string | string[]) => {
    setQuestions((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  };

  const addQuestion = () => {
    setQuestions((prev) => [...prev, emptyQuestion()]);
  };

  const removeQuestion = (index: number) => {
    setQuestions((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = () => {
    if (!displayName.trim() || !token) return;
    const templateId = displayName.toLowerCase().replace(/\s+/g, '_');
    createTemplate(
      { templateId, displayName, templateCategory, description, defaultTimeSeconds, questions },
      {
        onSuccess: () => {
          router.push('/games');
        },
      }
    );
  };

  if (!isAdmin) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-muted-foreground">You do not have permission to create game templates.</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push('/games')}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold">Create New Game</h1>
          <p className="text-sm text-muted-foreground">Design a custom game template for learners</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Game Details</CardTitle>
          <CardDescription>Basic configuration for the game template</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>Game Name</Label>
            <Input value={displayName} onChange={(e) => setDisplayName(e.target.value)} placeholder="e.g. Czech Vocabulary Quiz" />
          </div>
          <div className="space-y-2">
            <Label>Category</Label>
            <select
              className="w-full h-10 px-3 rounded-lg border border-input bg-transparent text-sm"
              value={templateCategory}
              onChange={(e) => setTemplateCategory(e.target.value as GameCategory)}
            >
              {CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </select>
          </div>
          <div className="space-y-2">
            <Label>Description</Label>
            <Textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Brief description..." />
          </div>
          <div className="space-y-2">
            <Label>Time per Question (seconds)</Label>
            <Input type="number" value={defaultTimeSeconds} onChange={(e) => setDefaultTimeSeconds(Number(e.target.value))} min={10} max={300} />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Questions</CardTitle>
            <CardDescription>Add questions for the game</CardDescription>
          </div>
          <Button variant="outline" size="sm" onClick={addQuestion}>
            <Plus className="w-4 h-4 mr-1" /> Add Question
          </Button>
        </CardHeader>
        <CardContent className="space-y-6">
          {questions.map((q, i) => (
            <div key={i} className="p-4 rounded-xl border space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Question {i + 1}</span>
                {questions.length > 1 && (
                  <Button variant="ghost" size="icon-sm" onClick={() => removeQuestion(i)}>
                    <Trash2 className="w-4 h-4 text-destructive" />
                  </Button>
                )}
              </div>
              <div className="space-y-2">
                <Label className="text-xs">Question Text</Label>
                <Input value={q.questionText} onChange={(e) => updateQuestion(i, 'questionText', e.target.value)} placeholder="Enter question..." />
              </div>
              <div className="space-y-2">
                <Label className="text-xs">Options</Label>
                {q.options.map((opt, oi) => (
                  <div key={oi} className="flex items-center gap-2">
                    <span className="text-xs text-muted-foreground w-4">{String.fromCharCode(65 + oi)}.</span>
                    <Input
                      value={opt}
                      onChange={(e) => {
                        const next = [...q.options];
                        next[oi] = e.target.value;
                        updateQuestion(i, 'options', next);
                      }}
                      placeholder={`Option ${oi + 1}`}
                      className="text-sm"
                    />
                    <input
                      type="radio"
                      name={`correct-${i}`}
                      checked={q.correctAnswer === opt}
                      onChange={() => updateQuestion(i, 'correctAnswer', opt)}
                      className="w-4 h-4"
                    />
                  </div>
                ))}
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      <Button className="w-full" size="lg" onClick={handleSubmit} disabled={isPending || !displayName.trim()}>
        {isPending ? (
          <><Loader2 className="w-5 h-5 animate-spin mr-2" /> Saving...</>
        ) : (
          <><Save className="w-5 h-5 mr-2" /> Create Game Template</>
        )}
      </Button>
    </div>
  );
}
