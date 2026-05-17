'use client';

import { useState, useEffect } from 'react';
import { useAuthStore } from '@/lib/stores/authStore';
import { authApi } from '@/lib/api/client';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2, Check, X, Clock, Shield, Mail, History } from 'lucide-react';
import type { UserResponse, UserRole } from '@/types';

export default function AdminApprovalsPage() {
  const { user: authUser, token } = useAuthStore();
  const [pending, setPending] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [rejectId, setRejectId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const isAdmin = authUser?.role === 'SUPER_ADMIN' || authUser?.role === 'BUSINESS_ADMIN';

  useEffect(() => {
    if (token && isAdmin) {
      authApi.getPendingUsers(token)
        .then(setPending)
        .catch(() => {})
        .finally(() => setLoading(false));
    }
  }, [token, isAdmin]);

  const handleApprove = async (userId: string) => {
    if (!token) return;
    setActionLoading(true);
    try {
      await authApi.approveUser(userId, undefined, token);
      setPending((prev) => prev.filter((u) => u.userId !== userId));
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!token || !rejectId) return;
    setActionLoading(true);
    try {
      await authApi.rejectUser(rejectId, rejectReason || 'Registration rejected', token);
      setPending((prev) => prev.filter((u) => u.userId !== rejectId));
      setRejectId(null);
      setRejectReason('');
    } finally {
      setActionLoading(false);
    }
  };

  if (!isAdmin) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-muted-foreground">You do not have permission to view this page.</p>
      </div>
    );
  }

  const instructors = pending.filter((u) => u.role === 'ADMIN_TEACHER');
  const students = pending.filter((u) => u.role === 'USER_STUDENT');

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-display-lg font-bold text-on-surface">Pending Approvals</h1>
        <p className="text-muted-foreground mt-1">Review and manage new instructor applications and student verification requests.</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Total Pending</p>
            <p className="text-2xl font-bold text-primary">{pending.length}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Instructors</p>
            <p className="text-2xl font-bold">{instructors.length}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Students</p>
            <p className="text-2xl font-bold">{students.length}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Role</p>
            <p className="text-lg font-bold text-primary">{authUser?.role.replace(/_/g, ' ')}</p>
          </CardContent>
        </Card>
      </div>

      {/* Approval List */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 className="w-6 h-6 animate-spin text-primary" />
        </div>
      ) : pending.length === 0 ? (
        <div className="text-center py-20 text-muted-foreground">
          <Shield className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p className="text-lg font-medium">No pending approvals</p>
          <p className="text-sm">All users have been reviewed.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {pending.map((u) => (
            <Card key={u.userId} className="hover:border-primary/30 transition-colors">
              <CardContent className="p-6">
                <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
                  <div className="flex items-center gap-4">
                    <div className="h-14 w-14 rounded-full bg-primary/10 flex items-center justify-center font-bold text-lg text-primary">
                      {u.firstName[0]}{u.lastName[0]}
                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-semibold text-on-surface">{u.firstName} {u.lastName}</h3>
                        <span className={`px-2 py-0.5 text-[10px] font-bold rounded-full uppercase ${
                          u.role === 'ADMIN_TEACHER'
                            ? 'bg-purple-100 text-purple-700'
                            : 'bg-surface-container-highest text-muted-foreground'
                        }`}>
                          {u.role === 'ADMIN_TEACHER' ? 'Instructor' : 'Student Verification'}
                        </span>
                      </div>
                      <p className="text-sm text-muted-foreground flex items-center gap-2">
                        <Mail className="w-3.5 h-3.5" /> {u.email}
                      </p>
                      <p className="text-sm text-muted-foreground flex items-center gap-2 mt-0.5">
                        <Clock className="w-3.5 h-3.5" />
                        Registered: {new Date(u.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <Button
                      size="sm"
                      onClick={() => handleApprove(u.userId)}
                      disabled={actionLoading}
                    >
                      <Check className="w-4 h-4 mr-1" /> Approve
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => setRejectId(u.userId)}
                      disabled={actionLoading}
                    >
                      <X className="w-4 h-4 mr-1" /> Reject
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Reject Dialog */}
      <Dialog open={!!rejectId} onOpenChange={() => setRejectId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject User</DialogTitle>
            <DialogDescription>Provide a reason for rejection. The user will be notified.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <Label htmlFor="reason">Rejection Reason</Label>
            <Input
              id="reason"
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Enter reason for rejection..."
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectId(null)}>Cancel</Button>
            <Button variant="destructive" onClick={handleReject} disabled={actionLoading}>
              {actionLoading ? 'Rejecting...' : 'Confirm Reject'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
