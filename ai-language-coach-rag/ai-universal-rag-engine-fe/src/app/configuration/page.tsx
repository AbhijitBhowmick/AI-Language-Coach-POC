'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useUserProfile, useUpdateProfile, useLanguages, useLanguageLevels } from '@/lib/api';
import type { LanguageConfig, UserProfile } from '@/types';

export default function ConfigurationPage() {
  const router = useRouter();
  const [selectedLanguage, setSelectedLanguage] = useState('');
  const [selectedLevel, setSelectedLevel] = useState('');
  const [selectedNative, setSelectedNative] = useState('en');
  const [saveSuccess, setSaveSuccess] = useState(false);

  const { data: profile, isLoading: profileLoading, error: profileError } = useUserProfile({
    queryKey: ['profile'],
    queryFn: async () => {
      const token = localStorage.getItem('auth_token');
      const response = await fetch(`${process.env.NEXT_PUBLIC_JAVA_API_URL}/profile`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!response.ok) throw new Error('Failed to fetch profile');
      return response.json() as Promise<UserProfile>;
    },
  });

  const { data: languages, isLoading: languagesLoading } = useLanguages();
  const { data: levels, isLoading: levelsLoading } = useLanguageLevels(selectedLanguage);
  const updateMutation = useUpdateProfile();

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (!token) {
      router.push('/login');
    }
  }, [router]);

  useEffect(() => {
    if (profile) {
      setSelectedLanguage(profile.context?.targetLanguage?.toLowerCase() || 'czech');
      setSelectedLevel(profile.context?.targetLevel || 'A1');
      setSelectedNative(profile.context?.nativeLanguage || 'en');
    }
  }, [profile]);

  useEffect(() => {
    if (selectedLanguage && languages && languages.length > 0 && !selectedLevel) {
      const lang = languages.find(
        (l) => l.languageCode.toLowerCase() === selectedLanguage.toLowerCase()
      );
      if (lang) {
        setSelectedLevel(lang.level || 'A1');
      }
    }
  }, [selectedLanguage, languages, selectedLevel]);

  const handleSave = async () => {
    try {
      await updateMutation.mutateAsync({
        targetLanguage: selectedLanguage,
        targetLevel: selectedLevel,
        nativeLanguage: selectedNative,
      });
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (error) {
      console.error('Failed to update profile:', error);
    }
  };

  if (profileLoading || languagesLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (profileError) {
    return (
      <div className="max-w-7xl mx-auto">
        <div className="bg-error-container border border-border-subtle rounded-xl p-card-padding">
          <div className="flex items-start gap-3">
            <div className="text-error">
              <span className="material-symbols-outlined text-[20px]">error</span>
            </div>
            <div>
              <h4 className="text-h2 font-h2 text-error mb-1">Failed to Load Profile</h4>
              <p className="text-label-sm text-text-secondary mb-3">Please login again to continue.</p>
              <button
                onClick={() => router.push('/login')}
                className="text-primary font-medium hover:underline"
              >
                Go to Login
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const uniqueLanguages = languages?.reduce((acc: LanguageConfig[], lang) => {
    const existing = acc.find((l) => l.languageCode === lang.languageCode);
    if (!existing) {
      acc.push(lang);
    }
    return acc;
  }, []) || [];

  return (
    <div className="min-h-screen">
      <header className="mb-8">
        <h1 className="text-h1 font-h1 text-text-primary mb-2">Learning Configuration</h1>
        <p className="text-body font-body text-text-secondary">
          Customize your language learning settings and preferences.
        </p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <div className="lg:col-span-8 space-y-6">
          <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
            <h3 className="text-h2 font-h2 text-text-primary mb-6">Language Settings</h3>
            <div className="space-y-6">
              <div>
                <label htmlFor="targetLanguage" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Target Language
                </label>
                <select
                  id="targetLanguage"
                  value={selectedLanguage}
                  onChange={(e) => {
                    setSelectedLanguage(e.target.value);
                    setSelectedLevel('');
                  }}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none"
                >
                  <option value="">Select a language</option>
                  {uniqueLanguages.map((lang) => (
                    <option key={lang.languageCode} value={lang.languageCode.toLowerCase()}>
                      {lang.languageName} ({lang.languageCode.toUpperCase()})
                    </option>
                  ))}
                </select>
                <p className="text-[12px] text-text-secondary mt-2">The language you want to learn</p>
              </div>

              <div>
                <label htmlFor="currentLevel" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Current Level
                </label>
                <select
                  id="currentLevel"
                  value={selectedLevel}
                  onChange={(e) => setSelectedLevel(e.target.value)}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none disabled:opacity-50"
                  disabled={!selectedLanguage || levelsLoading}
                >
                  <option value="">Select a level</option>
                  {levels?.map((level) => (
                    <option key={level} value={level}>
                      {level}
                    </option>
                  ))}
                </select>
                <p className="text-[12px] text-text-secondary mt-2">Your current proficiency level</p>
              </div>

              <div>
                <label htmlFor="nativeLanguage" className="block text-label-sm font-label-sm text-text-secondary mb-2">
                  Native Language
                </label>
                <select
                  id="nativeLanguage"
                  value={selectedNative}
                  onChange={(e) => setSelectedNative(e.target.value)}
                  className="w-full px-4 py-2.5 bg-white border border-border-subtle rounded-lg focus:ring-2 focus:ring-primary outline-none appearance-none"
                >
                  <option value="en">English</option>
                  <option value="hi">Hindi</option>
                  <option value="bn">Bengali</option>
                  <option value="te">Telugu</option>
                  <option value="uk">Ukrainian</option>
                  <option value="de">German</option>
                  <option value="fr">French</option>
                  <option value="es">Spanish</option>
                </select>
                <p className="text-[12px] text-text-secondary mt-2">Your native language for linguistic bridging</p>
              </div>

              <div className="pt-4 flex items-center gap-4">
                <button
                  onClick={handleSave}
                  disabled={updateMutation.isPending || !selectedLanguage || !selectedLevel}
                  className="bg-primary text-white px-8 py-3 rounded-lg font-semibold hover:bg-primary-container transition-colors shadow-md active:scale-95 duration-150 flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span className="material-symbols-outlined">save</span>
                  {updateMutation.isPending ? 'Saving...' : 'Save Configuration'}
                </button>

                {saveSuccess && (
                  <div className="flex items-center gap-2 text-chart-green">
                    <span className="material-symbols-outlined">check_circle</span>
                    <span className="text-label-sm">Configuration saved successfully!</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="lg:col-span-4 space-y-6">
          <div className="bg-surface-card border border-border-subtle rounded-xl p-card-padding shadow-sm">
            <h3 className="text-h2 font-h2 text-text-primary mb-4">Current Profile</h3>
            <dl className="space-y-4">
              <div className="p-3 bg-surface-container-low rounded-lg">
                <dt className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">Email</dt>
                <dd className="text-sm text-text-primary font-medium">{profile?.email || 'N/A'}</dd>
              </div>
              <div className="p-3 bg-surface-container-low rounded-lg">
                <dt className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">Plan</dt>
                <dd className="text-sm text-text-primary font-medium">{profile?.planType || 'N/A'}</dd>
              </div>
              <div className="p-3 bg-surface-container-low rounded-lg">
                <dt className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">Readiness Score</dt>
                <dd className="text-sm text-text-primary font-medium">
                  {profile?.readinessScore ? `${(profile.readinessScore * 100).toFixed(0)}%` : 'N/A'}
                </dd>
              </div>
              <div className="p-3 bg-surface-container-low rounded-lg">
                <dt className="text-[10px] uppercase font-bold text-text-secondary tracking-widest mb-1">Diagnostic Completed</dt>
                <dd className="text-sm text-text-primary font-medium">
                  {profile?.diagnosticCompleted ? 'Yes' : 'No'}
                </dd>
              </div>
            </dl>
          </div>

          <div className="bg-surface-container-low border border-border-subtle rounded-xl p-card-padding">
            <div className="flex items-start gap-3">
              <div className="text-primary">
                <span className="material-symbols-outlined text-[20px]">lightbulb</span>
              </div>
              <div>
                <h4 className="text-h2 font-h2 text-text-primary mb-1">Pro Tips</h4>
                <ul className="text-label-sm text-text-secondary space-y-1">
                  <li> Complete the diagnostic test for personalized recommendations</li>
                  <li> Set your level slightly below your comfort zone for optimal growth</li>
                  <li> Change content type regularly to expose yourself to diverse vocabulary</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}