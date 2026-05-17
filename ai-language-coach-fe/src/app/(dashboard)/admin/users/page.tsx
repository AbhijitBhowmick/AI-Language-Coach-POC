'use client';

import { useState, useEffect } from 'react';
import { useAuthStore } from '@/lib/stores/authStore';
import { authApi } from '@/lib/api/client';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Loader2, Search, ChevronLeft, ChevronRight, Shield, MoreVertical, Edit, Ban, CheckCircle } from 'lucide-react';
import type { UserResponse, UserRole, UserStatus } from '@/types';

const ROLE_COLORS: Record<UserRole, string> = {
  SUPER_ADMIN: 'bg-blue-100 text-blue-700',
  BUSINESS_ADMIN: 'bg-slate-100 text-slate-600',
  ADMIN_TEACHER: 'bg-purple-100 text-purple-700',
  USER_STUDENT: 'bg-green-100 text-green-700',
};

const STATUS_DOTS: Record<UserStatus, string> = {
  ACTIVE: 'bg-green-500',
  PENDING_APPROVAL: 'bg-orange-500',
  REJECTED: 'bg-red-500',
  SUSPENDED: 'bg-red-500',
};

export default function AdminUsersPage() {
  const { user: authUser, token } = useAuthStore();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');

  const isAdmin = authUser?.role === 'SUPER_ADMIN' || authUser?.role === 'BUSINESS_ADMIN';

  useEffect(() => {
    if (token && isAdmin) {
      authApi.listUsers(token)
        .then(setUsers)
        .catch(() => {})
        .finally(() => setLoading(false));
    }
  }, [token, isAdmin]);

  const filteredUsers = users.filter((u) => {
    const matchesSearch = search === '' ||
      u.email.toLowerCase().includes(search.toLowerCase()) ||
      u.firstName.toLowerCase().includes(search.toLowerCase()) ||
      u.lastName.toLowerCase().includes(search.toLowerCase());
    const matchesRole = roleFilter === '' || u.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  const handleSuspend = async (userId: string) => {
    if (!token) return;
    try {
      await authApi.suspendUser(userId, token);
      setUsers((prev) => prev.map((u) => u.userId === userId ? { ...u, status: 'SUSPENDED' as UserStatus } : u));
    } catch {}
  };

  const handleActivate = async (userId: string) => {
    if (!token) return;
    try {
      await authApi.activateUser(userId, token);
      setUsers((prev) => prev.map((u) => u.userId === userId ? { ...u, status: 'ACTIVE' as UserStatus } : u));
    } catch {}
  };

  if (!isAdmin) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-muted-foreground">You do not have permission to view this page.</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-display-lg font-bold text-on-surface">User Management</h1>
          <p className="text-muted-foreground mt-1">Manage platform access, roles, and user account statuses.</p>
        </div>
        <Button>
          <Shield className="w-4 h-4 mr-2" /> Invite New User
        </Button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-6">
        <Card>
          <CardContent className="p-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-muted-foreground uppercase tracking-wider">Total Users</p>
              <h3 className="text-3xl font-bold mt-1">{users.length}</h3>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-muted-foreground uppercase tracking-wider">Pending Approval</p>
              <h3 className="text-3xl font-bold mt-1 text-orange-500">
                {users.filter((u) => u.status === 'PENDING_APPROVAL').length}
              </h3>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-muted-foreground uppercase tracking-wider">Suspended</p>
              <h3 className="text-3xl font-bold mt-1 text-destructive">
                {users.filter((u) => u.status === 'SUSPENDED').length}
              </h3>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
        <div className="p-4 border-b flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="relative">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <Input
                className="pl-9 w-64"
                placeholder="Search users..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <select
              className="h-10 px-3 rounded-lg border border-input bg-transparent text-sm"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value as UserRole | '')}
            >
              <option value="">All Roles</option>
              <option value="SUPER_ADMIN">SUPER_ADMIN</option>
              <option value="BUSINESS_ADMIN">BUSINESS_ADMIN</option>
              <option value="ADMIN_TEACHER">ADMIN_TEACHER</option>
              <option value="USER_STUDENT">USER_STUDENT</option>
            </select>
          </div>
        </div>

        {/* Table */}
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="w-6 h-6 animate-spin text-primary" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-surface-container/50">
                  <th className="px-6 py-4 text-xs font-bold text-muted-foreground uppercase tracking-wider">Name</th>
                  <th className="px-6 py-4 text-xs font-bold text-muted-foreground uppercase tracking-wider">Email</th>
                  <th className="px-6 py-4 text-xs font-bold text-muted-foreground uppercase tracking-wider">Role</th>
                  <th className="px-6 py-4 text-xs font-bold text-muted-foreground uppercase tracking-wider">Status</th>
                  <th className="px-6 py-4 text-xs font-bold text-muted-foreground uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {filteredUsers.map((u) => (
                  <tr key={u.userId} className="hover:bg-surface-container/30 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary">
                          {u.firstName[0]}{u.lastName[0]}
                        </div>
                        <div>
                          <p className="font-medium text-sm">{u.firstName} {u.lastName}</p>
                          <p className="text-xs text-muted-foreground">ID: {u.userId.slice(0, 8)}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-muted-foreground">{u.email}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${ROLE_COLORS[u.role]}`}>
                        {u.role}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="flex items-center gap-1.5 text-sm font-medium">
                        <span className={`w-1.5 h-1.5 rounded-full ${STATUS_DOTS[u.status]}`} />
                        {u.status}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {u.status === 'ACTIVE' && (
                          <button
                            onClick={() => handleSuspend(u.userId)}
                            className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-lg transition-all"
                            title="Suspend"
                          >
                            <Ban className="w-4 h-4" />
                          </button>
                        )}
                        {u.status === 'SUSPENDED' && (
                          <button
                            onClick={() => handleActivate(u.userId)}
                            className="p-1.5 text-muted-foreground hover:text-green-600 hover:bg-green-50 rounded-lg transition-all"
                            title="Activate"
                          >
                            <CheckCircle className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
