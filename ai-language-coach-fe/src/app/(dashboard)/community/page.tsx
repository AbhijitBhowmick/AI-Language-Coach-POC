'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger, DialogFooter, DialogClose } from '@/components/ui/dialog';
import { useAuthStore } from '@/lib/stores/authStore';
import {
  useCommunities, useJoinCommunity, useLeaveCommunity,
  useCommunityPosts, useCreatePost, useLikePost,
  useLeaderboard, useCreateCommunity, useAddComment, usePostComments,
} from '@/lib/hooks/useApi';
import { useQueryClient } from '@tanstack/react-query';
import { Heart, MessageCircle, Users, Loader2, LogIn, LogOut, Plus, ChevronDown, ChevronUp, Send } from 'lucide-react';
import type { CommunityResponseDTO, PostResponseDTO, CommentResponseDTO } from '@/types';

type CommunityWithMeta = CommunityResponseDTO & { memberCount: number; isJoined: boolean };

const mockCommunities: (CommunityResponseDTO & { memberCount: number; isJoined: boolean })[] = [
  { id: 'mock-1', name: 'Czech Learners', description: 'Learn Czech together', tenantId: 'SYSTEM', createdBy: '', adminTeacherId: '', isActive: true, createdAt: '', memberCount: 234, isJoined: true },
  { id: 'mock-2', name: 'A2 Speakers', description: 'Practice A2 level conversations', tenantId: 'SYSTEM', createdBy: '', adminTeacherId: '', isActive: true, createdAt: '', memberCount: 156, isJoined: false },
  { id: 'mock-3', name: 'Grammar Hub', description: 'Master Czech grammar', tenantId: 'SYSTEM', createdBy: '', adminTeacherId: '', isActive: true, createdAt: '', memberCount: 89, isJoined: true },
];

const mockPosts: (PostResponseDTO & { userName: string; time: string; liked: boolean })[] = [
  { id: 'mp1', communityId: '', userId: '', content: 'Just completed my first voice practice session! The pronunciation feedback was really helpful.', likesCount: 24, commentsCount: 8, createdAt: '', userName: 'Maria K.', time: '2 hours ago', liked: false },
  { id: 'mp2', communityId: '', userId: '', content: "Anyone know a good resource for learning Czech cases? I'm struggling with locative.", likesCount: 15, commentsCount: 23, createdAt: '', userName: 'Jan B.', time: '5 hours ago', liked: true },
  { id: 'mp3', communityId: '', userId: '', content: 'Just scored 95% on the diagnostic test! Moving to B1!', likesCount: 67, commentsCount: 12, createdAt: '', userName: 'Anna S.', time: '1 day ago', liked: false },
];

function CommentsSection({ postId, token }: { postId: string; token: string }) {
  const { data: comments, isLoading } = usePostComments(token, postId);
  const { mutate: addComment } = useAddComment(token);
  const [commentText, setCommentText] = useState('');

  const handleSubmit = () => {
    if (!commentText.trim()) return;
    addComment({ postId, content: commentText });
    setCommentText('');
  };

  if (isLoading) return <Loader2 className="w-4 h-4 animate-spin mx-auto my-2" />;

  const items = comments || [];
  return (
    <div className="mt-3 space-y-3 border-t pt-3">
      {items.length === 0 && <p className="text-xs text-muted-foreground">No comments yet</p>}
      {items.map((c: CommentResponseDTO) => (
        <div key={c.id} className="flex gap-2 text-sm">
          <Avatar className="w-6 h-6">
            <AvatarFallback className="text-[10px]">U</AvatarFallback>
          </Avatar>
          <div>
            <p className="text-muted-foreground">{c.content}</p>
          </div>
        </div>
      ))}
      <div className="flex gap-2">
        <Input
          placeholder="Write a comment..."
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
          className="text-sm h-8"
          onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
        />
        <Button size="sm" variant="ghost" onClick={handleSubmit} disabled={!commentText.trim()}>
          <Send className="w-4 h-4" />
        </Button>
      </div>
    </div>
  );
}

export default function CommunityPage() {
  const { user, token } = useAuthStore();
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<'feed' | 'communities'>('feed');
  const [newPost, setNewPost] = useState('');
  const [selectedCommunity, setSelectedCommunity] = useState<string | null>(null);
  const [joinedIds, setJoinedIds] = useState<Set<string>>(new Set(['mock-1', 'mock-3']));
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [createName, setCreateName] = useState('');
  const [createDescription, setCreateDescription] = useState('');
  const [expandedComments, setExpandedComments] = useState<Set<string>>(new Set());

  const { data: apiCommunities, isLoading: communitiesLoading } = useCommunities(token || '');
  const { data: apiPosts, isLoading: postsLoading } = useCommunityPosts(token || '', selectedCommunity || '');
  const { data: leaderboard } = useLeaderboard(token || '');
  const { mutate: joinCommunity } = useJoinCommunity(token || '');
  const { mutate: leaveCommunity } = useLeaveCommunity(token || '');
  const { mutate: createPost } = useCreatePost(token || '', selectedCommunity || '');
  const { mutate: likePost } = useLikePost(token || '');
  const { mutate: createNewCommunity } = useCreateCommunity(token || '');

  const isAdmin = user?.role === 'SUPER_ADMIN' || user?.role === 'BUSINESS_ADMIN';
  const isTeacher = user?.role === 'ADMIN_TEACHER';
  const canCreateCommunity = isAdmin || isTeacher;
  const userTenantId = user?.tenantId;

  const rawCommunities = apiCommunities && apiCommunities.length > 0 ? apiCommunities : mockCommunities;
  const communities = rawCommunities.map((c) => ({
    ...c,
    memberCount: 'memberCount' in c ? (c as CommunityWithMeta).memberCount || 0 : 0,
    isJoined: joinedIds.has(c.id) || ('isJoined' in c ? (c as CommunityWithMeta).isJoined : false),
  })) as CommunityWithMeta[];

  const filteredCommunities = communities.filter((c) => {
    if (isAdmin) return true;
    if (isTeacher) return c.tenantId === userTenantId || c.tenantId === 'SYSTEM';
    return joinedIds.has(c.id);
  });

  const posts: (PostResponseDTO & { userName: string; time: string; liked: boolean })[] = apiPosts && apiPosts.length > 0
    ? apiPosts.map((p) => ({ ...p, userName: 'User', time: new Date(p.createdAt).toLocaleDateString(), liked: false }))
    : mockPosts;

  const handleLike = (postId: string) => {
    likePost(postId);
  };

  const handleCreatePost = () => {
    if (!newPost.trim() || !selectedCommunity) return;
    createPost(newPost);
    setNewPost('');
  };

  const handleJoin = (communityId: string) => {
    joinCommunity(communityId, {
      onSuccess: () => setJoinedIds((prev) => new Set(prev).add(communityId)),
    });
  };

  const handleLeave = (communityId: string) => {
    leaveCommunity(communityId, {
      onSuccess: () => {
        const next = new Set(joinedIds);
        next.delete(communityId);
        setJoinedIds(next);
      },
    });
  };

  const handleCreateCommunity = () => {
    if (!createName.trim()) return;
    createNewCommunity(
      { name: createName, description: createDescription },
      {
        onSuccess: () => {
          setShowCreateDialog(false);
          setCreateName('');
          setCreateDescription('');
          queryClient.invalidateQueries({ queryKey: ['communities'] });
        },
      }
    );
  };

  const handleToggleComments = (postId: string) => {
    setExpandedComments((prev) => {
      const next = new Set(prev);
      if (next.has(postId)) next.delete(postId);
      else next.add(postId);
      return next;
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Community</h1>
        <div className="flex gap-2">
          <Button variant={activeTab === 'feed' ? 'default' : 'outline'} onClick={() => setActiveTab('feed')}>
            Feed
          </Button>
          <Button variant={activeTab === 'communities' ? 'default' : 'outline'} onClick={() => setActiveTab('communities')}>
            Communities
          </Button>
        </div>
      </div>

      {activeTab === 'feed' ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-4">
            <Card>
              <CardContent className="p-4">
                <div className="flex gap-4">
                  <Avatar>
                    <AvatarFallback>{user?.firstName?.[0]}{user?.lastName?.[0]}</AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    {!selectedCommunity ? (
                      <div className="flex flex-wrap gap-2 mb-3">
                        {communities.filter((c) => c.isJoined).map((c) => (
                          <Button key={c.id} size="xs" variant="outline" onClick={() => setSelectedCommunity(c.id)}>
                            Post in {c.name}
                          </Button>
                        ))}
                      </div>
                    ) : (
                      <>
                        <p className="text-xs text-muted-foreground mb-2">
                          Posting to: {communities.find((c) => c.id === selectedCommunity)?.name}
                          <button className="ml-2 text-primary hover:underline" onClick={() => setSelectedCommunity(null)}>Change</button>
                        </p>
                        <Textarea placeholder="Share something with the community..." value={newPost} onChange={(e) => setNewPost(e.target.value)} className="min-h-[80px]" />
                        <div className="flex justify-end mt-3 gap-2">
                          <Button variant="outline" onClick={() => setSelectedCommunity(null)}>Cancel</Button>
                          <Button onClick={handleCreatePost}>Post</Button>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>

            {postsLoading ? (
              <div className="flex justify-center py-8"><Loader2 className="w-6 h-6 animate-spin" /></div>
            ) : (
              posts.map((post) => (
                <Card key={post.id}>
                  <CardContent className="p-4">
                    <div className="flex gap-3">
                      <Avatar><AvatarFallback>{post.userName.split(' ').map((n) => n[0]).join('')}</AvatarFallback></Avatar>
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <span className="font-semibold">{post.userName}</span>
                          <span className="text-sm text-muted-foreground">• {post.time || new Date(post.createdAt).toLocaleDateString()}</span>
                        </div>
                        <p className="mt-2">{post.content}</p>
                        <div className="flex gap-4 mt-3">
                          <button onClick={() => handleLike(post.id)} className={`flex items-center gap-1 text-sm ${post.liked ? 'text-red-500' : 'text-muted-foreground'}`}>
                            <Heart className={`w-4 h-4 ${post.liked ? 'fill-current' : ''}`} />
                            {post.likesCount}
                          </button>
                          <button onClick={() => handleToggleComments(post.id)} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-primary">
                            <MessageCircle className="w-4 h-4" />
                            {post.commentsCount}
                            {expandedComments.has(post.id) ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                          </button>
                        </div>
                        {expandedComments.has(post.id) && token && (
                          <CommentsSection postId={post.id} token={token} />
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>

          <div className="space-y-4">
            <Card>
              <CardHeader><CardTitle className="text-lg">Your Groups</CardTitle></CardHeader>
              <CardContent className="space-y-2">
                {communitiesLoading ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : (
                  communities.filter((c) => c.isJoined).length === 0 ? (
                    <p className="text-sm text-muted-foreground text-center py-2">No groups joined yet</p>
                  ) : (
                    communities.filter((c) => c.isJoined).map((c) => (
                      <div key={c.id} className="flex items-center gap-3 p-2 rounded-lg hover:bg-surface-container cursor-pointer" onClick={() => { setSelectedCommunity(c.id); setActiveTab('feed'); }}>
                        <Users className="w-8 h-8 text-primary" />
                        <div>
                          <p className="font-medium text-sm">{c.name}</p>
                          <p className="text-xs text-muted-foreground">{c.memberCount} members</p>
                        </div>
                      </div>
                    ))
                  )
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader><CardTitle className="text-lg">Leaderboard</CardTitle></CardHeader>
              <CardContent className="space-y-2">
                {leaderboard && leaderboard.length > 0 ? (
                  leaderboard.slice(0, 5).map((entry, i) => (
                    <div key={entry.userId} className="flex items-center gap-3 p-2">
                      <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs ${i === 0 ? 'bg-yellow-500 text-white' : i === 1 ? 'bg-gray-400 text-white' : i === 2 ? 'bg-orange-400 text-white' : 'bg-surface-container text-muted-foreground'}`}>{i + 1}</span>
                      <span className="text-sm font-medium">{entry.userId.substring(0, 8)}...</span>
                      <span className="ml-auto text-sm text-muted-foreground">{entry.totalScore} pts</span>
                    </div>
                  ))
                ) : (
                  ['Anna S.', 'Jan B.', 'Maria K.', 'Petr L.'].map((name, i) => (
                    <div key={i} className="flex items-center gap-3 p-2">
                      <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs ${i === 0 ? 'bg-yellow-500 text-white' : i === 1 ? 'bg-gray-400 text-white' : i === 2 ? 'bg-orange-400 text-white' : 'bg-surface-container text-muted-foreground'}`}>{i + 1}</span>
                      <span className="text-sm font-medium">{name}</span>
                    </div>
                  ))
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      ) : (
        <div>
          {canCreateCommunity && (
            <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
              <DialogTrigger render={<Button className="mb-4" />}>
                <Plus className="w-4 h-4 mr-2" />Create Community
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Create Community</DialogTitle>
                  <DialogDescription>Create a new community for learners</DialogDescription>
                </DialogHeader>
                <div className="space-y-4 py-2">
                  <Input
                    placeholder="Community name"
                    value={createName}
                    onChange={(e) => setCreateName(e.target.value)}
                  />
                  <Textarea
                    placeholder="Description (optional)"
                    value={createDescription}
                    onChange={(e) => setCreateDescription(e.target.value)}
                  />
                </div>
                <DialogFooter>
                  <DialogClose render={<Button variant="outline" />}>Cancel</DialogClose>
                  <Button onClick={handleCreateCommunity}>Create</Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          )}

          {communitiesLoading ? (
            <div className="col-span-full flex justify-center py-8"><Loader2 className="w-6 h-6 animate-spin" /></div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredCommunities.map((community) => (
                <Card key={community.id} className="hover:shadow-lg transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div>
                        <h3 className="font-semibold text-lg">{community.name}</h3>
                        <p className="text-sm text-muted-foreground mt-1">{community.description || ''}</p>
                      </div>
                      <Users className="w-8 h-8 text-primary" />
                    </div>
                    <div className="flex items-center justify-between mt-4">
                      <span className="text-sm text-muted-foreground">{community.memberCount || 0} members</span>
                      <Button
                        variant={community.isJoined ? 'outline' : 'default'}
                        onClick={() => {
                          if (community.isJoined) {
                            handleLeave(community.id);
                          } else {
                            handleJoin(community.id);
                          }
                        }}
                      >
                        {community.isJoined ? (
                          <><LogOut className="w-4 h-4 mr-1" /> Leave</>
                        ) : (
                          <><LogIn className="w-4 h-4 mr-1" /> Join</>
                        )}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
