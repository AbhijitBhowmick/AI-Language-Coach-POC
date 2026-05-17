'use client';

import { useState, useEffect } from 'react';
import { useAuthStore } from '@/lib/stores/authStore';
import { useUserProfile, useUpdateProfile, useCreateProfile } from '@/lib/hooks/useApi';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { User, Shield, Fingerprint, Trash2, Bell, Brain, Loader2 } from 'lucide-react';
import { profileApi } from '@/lib/api/client';

const CEFR_LEVELS = [
  { value: 'A1', label: 'A1', desc: 'Beginner' },
  { value: 'A2', label: 'A2', desc: 'Elementary' },
  { value: 'B1', label: 'B1', desc: 'Intermediate' },
  { value: 'B2', label: 'B2', desc: 'Upper Intermediate' },
  { value: 'C1', label: 'C1', desc: 'Advanced' },
  { value: 'C2', label: 'C2', desc: 'Proficient' },
];

const LANGUAGES = [
  'Czech', 'Slovak', 'Polish', 'Spanish', 'French',
  'German', 'Italian', 'Japanese', 'Mandarin', 'Hindi',
];

const NATIVE_LANGUAGES = [
  'English (US)', 'English (UK)', 'Czech', 'Slovak',
  'Polish', 'German', 'French', 'Spanish', 'Mandarin', 'Hindi',
];

export default function SettingsPage() {
  const { user, token, logout } = useAuthStore();
  const { data: profile, isLoading: profileLoading } = useUserProfile(token || '');
  const updateProfile = useUpdateProfile(token || '');
  const createProfile = useCreateProfile(token || '');

  const [targetLanguage, setTargetLanguage] = useState('Czech');
  const [nativeLanguage, setNativeLanguage] = useState('en');
  const [targetLevel, setTargetLevel] = useState('A1');
  const [dailyReminders, setDailyReminders] = useState(true);
  const [weeklyReports, setWeeklyReports] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState('');

  useEffect(() => {
    if (profile) {
      setTargetLanguage(profile.targetLanguage || 'Czech');
      setNativeLanguage(profile.nativeLanguage || 'en');
      setTargetLevel(profile.targetLevel || 'A1');
    }
  }, [profile]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setSaveMessage('');
    try {
      if (profile) {
        await updateProfile.mutateAsync({ targetLanguage, targetLevel, nativeLanguage });
      } else {
        await createProfile.mutateAsync({ targetLanguage, targetLevel, nativeLanguage });
      }
      setSaveMessage('Preferences saved successfully!');
    } catch {
      setSaveMessage('Failed to save. Please try again.');
    } finally {
      setSaving(false);
      setTimeout(() => setSaveMessage(''), 3000);
    }
  };

  const handleDeleteAccount = async () => {
    if (!token) return;
    setIsDeleting(true);
    try {
      await profileApi.delete(token);
      logout();
    } catch {
      setShowDeleteDialog(false);
      setIsDeleting(false);
    }
  };

  if (!user || !token) return null;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-display-lg text-on-surface font-bold">Account Settings</h1>
        <p className="text-muted-foreground mt-1">Manage your profile information and learning experience.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-start">
        {/* Profile Information (Read-Only) */}
        <section className="md:col-span-4 space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <User className="w-5 h-5" />
                  Profile
                </CardTitle>
              </div>
            </CardHeader>
            <CardContent className="space-y-5">
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground uppercase tracking-wider">Email</Label>
                <p className="font-semibold bg-surface-container px-3 py-2 rounded-lg border">
                  {user.email}
                </p>
              </div>
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground uppercase tracking-wider">Account Role</Label>
                <p className="flex items-center gap-2 font-semibold bg-surface-container px-3 py-2 rounded-lg border">
                  <Shield className="w-4 h-4 text-primary" />
                  {user.role.replace(/_/g, ' ')}
                </p>
              </div>
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground uppercase tracking-wider">User ID</Label>
                <p className="flex items-center gap-2 font-mono text-xs bg-surface-container px-3 py-2 rounded-lg border">
                  <Fingerprint className="w-4 h-4 text-muted-foreground" />
                  {user.userId}
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Danger Zone */}
          <Card className="border-2 border-destructive/20">
            <CardHeader>
              <div className="flex items-center gap-2">
                <Trash2 className="w-5 h-5 text-destructive" />
                <CardTitle className="text-destructive text-lg">Danger Zone</CardTitle>
              </div>
              <CardDescription>
                Permanently delete your account and all learning progress. This action cannot be undone.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button
                variant="destructive"
                className="w-full"
                onClick={() => setShowDeleteDialog(true)}
              >
                Delete Account
              </Button>
            </CardContent>
          </Card>
        </section>

        {/* Learning Preferences Form */}
        <section className="md:col-span-8">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="flex items-center gap-2">
                    <Brain className="w-5 h-5 text-primary" />
                    Learning Preferences
                  </CardTitle>
                  <CardDescription>
                    Customize how the AI tutor interacts with you.
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {profileLoading ? (
                <div className="flex items-center justify-center py-12">
                  <Loader2 className="w-6 h-6 animate-spin text-primary" />
                </div>
              ) : (
                <form onSubmit={handleSave} className="space-y-8">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <Label htmlFor="targetLanguage">Target Language</Label>
                      <select
                        id="targetLanguage"
                        className="w-full h-10 px-3 rounded-lg border border-input bg-transparent focus:border-ring focus:ring-2 focus:ring-ring/50 outline-none"
                        value={targetLanguage}
                        onChange={(e) => setTargetLanguage(e.target.value)}
                      >
                        {LANGUAGES.map((lang) => (
                          <option key={lang} value={lang}>{lang}</option>
                        ))}
                      </select>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="nativeLanguage">Native Language</Label>
                      <select
                        id="nativeLanguage"
                        className="w-full h-10 px-3 rounded-lg border border-input bg-transparent focus:border-ring focus:ring-2 focus:ring-ring/50 outline-none"
                        value={nativeLanguage}
                        onChange={(e) => setNativeLanguage(e.target.value)}
                      >
                        {NATIVE_LANGUAGES.map((lang) => (
                          <option key={lang} value={lang}>{lang}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <Label>Target Proficiency Level (CEFR)</Label>
                    <div className="grid grid-cols-3 sm:grid-cols-6 gap-3">
                      {CEFR_LEVELS.map((level) => (
                        <label
                          key={level.value}
                          className="cursor-pointer group"
                        >
                          <input
                            type="radio"
                            name="level"
                            value={level.value}
                            checked={targetLevel === level.value}
                            onChange={(e) => setTargetLevel(e.target.value)}
                            className="sr-only peer"
                          />
                          <div className="flex flex-col items-center justify-center p-4 border-2 border-border rounded-xl peer-checked:border-primary peer-checked:bg-primary/5 group-hover:bg-surface-container transition-all">
                            <span className={`font-bold text-lg ${targetLevel === level.value ? 'text-primary' : 'text-muted-foreground'}`}>
                              {level.label}
                            </span>
                            <span className="text-[10px] font-bold text-muted-foreground uppercase">
                              {level.desc}
                            </span>
                          </div>
                        </label>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-center justify-end gap-4 pt-6 border-t">
                    {saveMessage && (
                      <span className={`text-sm ${saveMessage.includes('success') ? 'text-green-600' : 'text-destructive'}`}>
                        {saveMessage}
                      </span>
                    )}
                    <Button type="submit" disabled={saving}>
                      {saving ? 'Saving...' : 'Save Changes'}
                    </Button>
                  </div>
                </form>
              )}
            </CardContent>
          </Card>
        </section>
      </div>

      {/* Notification Preferences */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardContent className="p-6 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-green-50 flex items-center justify-center">
              <Bell className="w-6 h-6 text-green-600" />
            </div>
            <div className="flex-1">
              <h3 className="font-semibold">Daily Reminders</h3>
              <p className="text-sm text-muted-foreground">Get nudged when it&apos;s time to practice.</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={dailyReminders}
              onClick={() => setDailyReminders(!dailyReminders)}
              className={`w-12 h-6 rounded-full relative transition-colors ${
                dailyReminders ? 'bg-primary' : 'bg-input'
              }`}
            >
              <span
                className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow-sm transition-transform ${
                  dailyReminders ? 'right-1' : 'left-1'
                }`}
              />
            </button>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-orange-50 flex items-center justify-center">
              <Brain className="w-6 h-6 text-orange-600" />
            </div>
            <div className="flex-1">
              <h3 className="font-semibold">Weekly Reports</h3>
              <p className="text-sm text-muted-foreground">Analysis of your speaking and grammar trends.</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={weeklyReports}
              onClick={() => setWeeklyReports(!weeklyReports)}
              className={`w-12 h-6 rounded-full relative transition-colors ${
                weeklyReports ? 'bg-primary' : 'bg-input'
              }`}
            >
              <span
                className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow-sm transition-transform ${
                  weeklyReports ? 'right-1' : 'left-1'
                }`}
              />
            </button>
          </CardContent>
        </Card>
      </div>

      {/* Delete Confirmation Dialog */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="text-destructive">Delete Account</DialogTitle>
            <DialogDescription>
              This action is permanent. All your profile data, learning progress, and community activity will be deleted.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDeleteDialog(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDeleteAccount} disabled={isDeleting}>
              {isDeleting ? 'Deleting...' : 'Delete My Account'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
