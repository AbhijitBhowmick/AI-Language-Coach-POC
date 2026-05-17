'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { GraduationCap, ArrowRight, Mic, Gamepad2, Users, Shield } from 'lucide-react';

export default function LandingPage() {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
  }, []);

  if (!mounted) {
    return (
      <div className="min-h-screen bg-surface flex items-center justify-center">
        <div className="w-12 h-12 bg-primary rounded-xl animate-pulse"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface">
      {/* Navigation */}
      <nav className="sticky top-0 w-full z-50 bg-white/90 backdrop-blur-md border-b border-slate-200/50 shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
          <Link href="/" className="flex items-center gap-2">
            <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center">
              <GraduationCap className="w-6 h-6 text-white" />
            </div>
            <span className="text-xl font-bold text-blue-700">LingoAI</span>
          </Link>
          <div className="flex gap-6 text-sm font-medium text-slate-600">
            <a href="#features" className="hover:text-blue-700">Features</a>
            <a href="#" className="hover:text-blue-700">Community</a>
          </div>
          <div className="flex gap-3">
            <Link href="/login" className="px-4 py-2 text-sm font-medium text-blue-700 border border-blue-700 rounded-lg hover:bg-blue-50">
              Sign In
            </Link>
            <Link href="/login" className="px-4 py-2 text-sm font-medium text-white bg-blue-700 rounded-lg hover:bg-blue-800">
              Sign Up
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="py-24 px-6">
        <div className="max-w-7xl mx-auto grid lg:grid-cols-2 gap-16 items-center">
          <div className="space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-blue-100 text-blue-700 text-sm font-semibold rounded-full">
              <span className="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></span>
              The future of language learning
            </div>
            <h1 className="text-5xl font-bold text-slate-900 leading-tight">
              Master Your Target Language with <span className="text-blue-700">AI-Powered</span> Coaching
            </h1>
            <p className="text-lg text-slate-600 max-w-lg">
              Immersive, gamified learning that adapts to your pace. Get real-time voice feedback from our AI tutor.
            </p>
            <div className="flex gap-4 pt-4">
              <Link href="/login" className="px-6 py-3 bg-blue-700 text-white rounded-xl font-semibold hover:bg-blue-800 flex items-center gap-2">
                Start Free <ArrowRight className="w-5 h-5" />
              </Link>
            </div>
            <div className="flex items-center gap-4 pt-4">
              <div className="flex -space-x-2">
                <div className="w-8 h-8 rounded-full bg-blue-300 border-2 border-white"></div>
                <div className="w-8 h-8 rounded-full bg-green-300 border-2 border-white"></div>
                <div className="w-8 h-8 rounded-full bg-yellow-300 border-2 border-white"></div>
                <div className="w-8 h-8 flex items-center justify-center bg-slate-200 text-xs font-bold rounded-full border-2 border-white">+2k</div>
              </div>
              <p className="text-sm text-slate-600">Join 2,000+ learners today</p>
            </div>
          </div>
          <div className="relative">
            <div className="bg-white p-8 rounded-3xl shadow-2xl border">
              <div className="aspect-video bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl flex items-center justify-center">
                <div className="text-center">
                  <Mic className="w-16 h-16 text-blue-700 mx-auto mb-4" />
                  <p className="font-semibold text-slate-700">AI Voice Practice</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="py-20 px-6 bg-white">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-slate-900">Why Choose LingoAI?</h2>
          </div>
          <div className="grid md:grid-cols-3 gap-6">
            <div className="p-6 rounded-2xl border hover:shadow-lg transition">
              <Gamepad2 className="w-10 h-10 text-blue-700 mb-4" />
              <h3 className="font-bold text-lg mb-2">Gamified Learning</h3>
              <p className="text-slate-600">Master vocabulary through quizzes, matching games, and addictive challenges.</p>
            </div>
            <div className="p-6 rounded-2xl bg-blue-700 text-white hover:shadow-xl transition">
              <Mic className="w-10 h-10 mb-4" />
              <h3 className="font-bold text-lg mb-2">Voice AI Coach</h3>
              <p className="text-white/80">Real-time conversation with instant grammar and pronunciation feedback.</p>
            </div>
            <div className="p-6 rounded-2xl border hover:shadow-lg transition">
              <Users className="w-10 h-10 text-red-700 mb-4" />
              <h3 className="font-bold text-lg mb-2">Global Community</h3>
              <p className="text-slate-600">Join learners worldwide, compete on leaderboards, earn badges.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-8 px-6 border-t bg-slate-50">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-2">
            <GraduationCap className="w-6 h-6 text-blue-700" />
            <span className="font-bold text-blue-700">LingoAI</span>
          </div>
          <p className="text-sm text-slate-500">© 2026 LingoAI. All rights reserved.</p>
          <div className="flex items-center gap-2 text-sm text-slate-500">
            <Shield className="w-4 h-4" />
            Secure & Private
          </div>
        </div>
      </footer>
    </div>
  );
}