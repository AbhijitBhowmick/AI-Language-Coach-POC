'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { useAuthStore } from '@/lib/stores/authStore';
import { voiceApi } from '@/lib/api/client';
import { Mic, MicOff, Send, Volume2, Loader2, Sparkles, AlertCircle, CheckCircle2, AlertTriangle } from 'lucide-react';
import type { VoiceServerMessage } from '@/types';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  isTranscription?: boolean;
  isCorrection?: boolean;
}

export default function PracticePage() {
  const { user, token } = useAuthStore();
  const [isRecording, setIsRecording] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      role: 'assistant',
      content: 'Hello! I\'m your AI Language Coach. Let\'s practice speaking together. How are you feeling today?',
      timestamp: new Date().toISOString(),
    },
  ]);
  const [analysis, setAnalysis] = useState<{
    grammarScore: number;
    vocabularyFeedback: string[];
    tips: string[];
  } | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [micError, setMicError] = useState<string | null>(null);
  const [sessionTime, setSessionTime] = useState(0);
  const [lives, setLives] = useState(3);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    if (sessionId) {
      timerRef.current = setInterval(() => {
        setSessionTime((prev) => prev + 1);
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
      setSessionTime(0);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [sessionId]);

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  const speakText = (text: string) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 0.9;
      utterance.pitch = 1;
      window.speechSynthesis.speak(utterance);
    }
  };

  const handleServerMessage = useCallback((msg: VoiceServerMessage) => {
    switch (msg.type) {
      case 'SESSION_STARTED':
        setSessionId(msg.sessionId);
        setIsConnecting(false);
        break;
      case 'TEXT_RESPONSE':
        setMessages(prev => [...prev, {
          id: Date.now().toString(),
          role: 'assistant',
          content: msg.text,
          timestamp: new Date().toISOString(),
        }]);
        break;
      case 'TRANSCRIPTION':
        setMessages(prev => [...prev, {
          id: `trans-${Date.now()}`,
          role: 'user',
          content: msg.text,
          timestamp: new Date().toISOString(),
          isTranscription: true,
        }]);
        break;
      case 'CORRECTION':
        setAnalysis({
          grammarScore: 85,
          vocabularyFeedback: [`Corrected: "${msg.original}" → "${msg.corrected}"`],
          tips: [msg.explanation],
        });
        break;
      case 'ERROR':
        console.error('Voice error:', msg.message);
        break;
      case 'SESSION_ENDED':
        setSessionId(null);
        if (msg.summary) {
          setLives(Math.min(3, Math.floor(msg.summary.score / 20) + 1));
        }
        break;
    }
  }, []);

  const connectWebSocket = useCallback(() => {
    if (!token || !user) return;
    setIsConnecting(true);

    try {
      const ws = new WebSocket(voiceApi.getWebSocketUrl());
      wsRef.current = ws;

      ws.onopen = () => {
        ws.send(JSON.stringify({
          type: 'START_SESSION',
          userId: user.userId,
          token,
        }));
      };

      ws.onmessage = (event) => {
        try {
          const msg: VoiceServerMessage = JSON.parse(event.data);
          handleServerMessage(msg);
        } catch { }
      };

      ws.onerror = () => {
        setIsConnecting(false);
      };

      ws.onclose = () => {
        setIsConnecting(false);
        setSessionId(null);
      };
    } catch {
      setIsConnecting(false);
    }
  }, [token, user, handleServerMessage]);

  const disconnectWebSocket = useCallback(() => {
    if (sessionId && wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ type: 'END_SESSION', sessionId }));
    }
    wsRef.current?.close();
    wsRef.current = null;
    setSessionId(null);
  }, [sessionId]);

  useEffect(() => {
    return () => {
      disconnectWebSocket();
    };
  }, [disconnectWebSocket]);

  const startRecording = async () => {
    setMicError(null);
    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      connectWebSocket();
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0 && wsRef.current?.readyState === WebSocket.OPEN) {
          const reader = new FileReader();
          reader.onload = () => {
            const base64Audio = (reader.result as string).split(',')[1];
            wsRef.current?.send(JSON.stringify({ type: 'AUDIO_DATA', audio: base64Audio }));
          };
          reader.readAsDataURL(event.data);
        }
      };

      mediaRecorder.start(1000);
      setIsRecording(true);
    } catch (err) {
      const msg = err instanceof DOMException && err.name === 'NotAllowedError'
        ? 'Microphone access denied. Please allow microphone permissions in your browser settings.'
        : 'Could not access microphone. Please check your audio device.';
      setMicError(msg);
      console.error('Microphone access denied:', err);
    }
  };

  const stopRecording = () => {
    mediaRecorderRef.current?.stop();
    mediaRecorderRef.current?.stream.getTracks().forEach(track => track.stop());
    mediaRecorderRef.current = null;
    setIsRecording(false);

    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ type: 'AUDIO_END' }));
    }
  };

  const handleSend = () => {
    if (!message.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: message,
      timestamp: new Date().toISOString(),
    };
    setMessages(prev => [...prev, userMessage]);

    if (wsRef.current?.readyState === WebSocket.OPEN && sessionId) {
      wsRef.current.send(JSON.stringify({ type: 'AUDIO_END', transcript: message }));
    } else {
      setTimeout(() => {
        setMessages(prev => [...prev, {
          id: (Date.now() + 1).toString(),
          role: 'assistant',
          content: 'That\'s great practice! Try to use more descriptive vocabulary. Your sentence structure is improving!',
          timestamp: new Date().toISOString(),
        }]);
        setAnalysis({
          grammarScore: 85,
          vocabularyFeedback: ['Good use of past tense', 'Consider using more adjectives'],
          tips: ['Try to speak more naturally', 'Focus on pronunciation'],
        });
      }, 1500);
    }

    setMessage('');
  };

  const toggleRecording = () => {
    if (isRecording) {
      stopRecording();
    } else {
      startRecording();
    }
  };

  return (
    <div className="h-[calc(100vh-120px)]">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-full">
        <div className="lg:col-span-2 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-2xl font-bold">Voice Practice</h1>
              <p className="text-muted-foreground">Chat with AI to improve your speaking skills</p>
            </div>
            <div className="flex items-center gap-2">
              {sessionId && <span className="text-xs text-green-500">● Connected</span>}
            </div>
          </div>

          <Card className="flex-1 flex flex-col">
            <CardContent className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.map((msg) => (
                <div key={msg.id} className={`flex gap-3 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}>
                  <Avatar className={msg.role === 'user' ? 'bg-primary' : 'bg-purple-500'}>
                    <AvatarFallback>{msg.role === 'user' ? 'U' : 'AI'}</AvatarFallback>
                  </Avatar>
                  <div className={`max-w-[70%] rounded-2xl p-4 ${msg.role === 'user' ? 'bg-primary text-white' : 'bg-surface-container'} ${msg.isTranscription ? 'opacity-70' : ''} ${msg.isCorrection ? 'border-2 border-yellow-400' : ''}`}>
                    <p>{msg.content}</p>
                    {msg.isTranscription && <p className="text-xs mt-1 opacity-60">Transcribed from audio</p>}
                    {msg.role === 'assistant' && !msg.isTranscription && (
                      <button
                        onClick={() => speakText(msg.content)}
                        className="mt-2 text-primary hover:text-primary/80 text-sm flex items-center gap-1"
                      >
                        <Volume2 className="w-4 h-4" />
                        Listen
                      </button>
                    )}
                  </div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </CardContent>

            <div className="p-4 border-t">
              {micError && (
                <div className="flex items-center gap-2 mb-3 p-3 bg-destructive/10 rounded-lg text-sm text-destructive">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  <span>{micError}</span>
                  <button onClick={() => setMicError(null)} className="ml-auto text-xs underline">Dismiss</button>
                </div>
              )}
              <div className="flex gap-3">
                <Button
                  variant={isRecording ? 'destructive' : 'outline'}
                  size="icon"
                  className={`rounded-full w-12 h-12 ${isRecording ? 'animate-pulse' : ''}`}
                  onClick={toggleRecording}
                  disabled={isConnecting}
                >
                  {isConnecting ? <Loader2 className="w-5 h-5 animate-spin" /> : isRecording ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
                </Button>
                <Textarea
                  placeholder="Type your message..."
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
                  className="min-h-[48px] resize-none"
                />
                <Button size="icon" className="rounded-full w-12 h-12" onClick={handleSend}>
                  <Send className="w-5 h-5" />
                </Button>
              </div>
            </div>
          </Card>
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-purple-500" />
                Live Analysis
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {analysis ? (
                <>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Grammar Score</span>
                    <span className="text-2xl font-bold text-green-500">{analysis.grammarScore}%</span>
                  </div>
                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Vocabulary Feedback</h4>
                    {analysis.vocabularyFeedback.map((fb, i) => (
                      <div key={i} className="flex items-start gap-2 text-sm">
                        <CheckCircle2 className="w-4 h-4 text-green-500 mt-0.5" />
                        <span>{fb}</span>
                      </div>
                    ))}
                  </div>
                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Tips</h4>
                    {analysis.tips.map((tip, i) => (
                      <div key={i} className="flex items-start gap-2 text-sm">
                        <AlertCircle className="w-4 h-4 text-yellow-500 mt-0.5" />
                        <span>{tip}</span>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <p className="text-sm text-muted-foreground text-center py-4">
                  Send a message to receive feedback
                </p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Lives</p>
                  <p className="text-2xl font-bold text-red-500">
                    {Array.from({ length: lives }).map((_, i) => (
                      <span key={i}>❤️</span>
                    ))}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Session</p>
                  <p className="text-2xl font-bold text-blue-500">{formatTime(sessionTime)}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
