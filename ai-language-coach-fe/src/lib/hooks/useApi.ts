import { useMutation, useQuery } from '@tanstack/react-query';
import { authApi, profileApi, diagnosticApi, communityApi } from '@/lib/api/client';
import type {
  AuthenticationResponse,
  RegisterRequest,
  UserProfileResponse,
  GameTemplateEntity,
  GameSession,
  GameAnswer,
  GameResult,
  CreateGameTemplateRequest,
  LeaderboardEntry,
} from '@/types';

// ===================== Auth Hooks =====================

export function useLogin() {
  return useMutation({
    mutationFn: async (credentials: { email: string; password: string }): Promise<AuthenticationResponse> => {
      return authApi.login(credentials);
    },
  });
}

export function useRegister() {
  return useMutation({
    mutationFn: async (data: RegisterRequest): Promise<AuthenticationResponse> => {
      return authApi.register(data);
    },
  });
}

export function useValidateToken(token: string) {
  return useQuery({
    queryKey: ['validate', token],
    queryFn: () => authApi.validate(token),
    enabled: !!token,
  });
}

// ===================== Profile Hooks =====================

export function useUserProfile(token: string) {
  return useQuery<UserProfileResponse>({
    queryKey: ['profile', token],
    queryFn: () => profileApi.get(token),
    enabled: !!token,
    retry: false,
  });
}

export function useCreateProfile(token: string) {
  return useMutation({
    mutationFn: async (data: { targetLanguage?: string; targetLevel?: string; nativeLanguage?: string }) =>
      profileApi.create(data, token),
  });
}

export function useUpdateProfile(token: string) {
  return useMutation({
    mutationFn: async (data: { targetLanguage?: string; targetLevel?: string; nativeLanguage?: string }) =>
      profileApi.update(data, token),
  });
}

// ===================== Diagnostic Hooks =====================

export function useStartDiagnostic(token: string) {
  return useMutation({
    mutationFn: async (params?: { targetLanguage?: string; targetLevel?: string }) =>
      diagnosticApi.startDiagnostic(token, params),
  });
}

export function useDiagnosticQuestion(token: string) {
  return useQuery({
    queryKey: ['diagnostic-question', token],
    queryFn: () => diagnosticApi.getQuestion(token),
    enabled: !!token,
  });
}

export function useSubmitAnswer(token: string) {
  return useMutation({
    mutationFn: async (payload: { questionNumber: number; answer: string }) =>
      diagnosticApi.submitAnswer(token, payload),
  });
}

export function useDiagnosticResult(token: string) {
  return useQuery({
    queryKey: ['diagnostic-result', token],
    queryFn: () => diagnosticApi.getResult(token),
    enabled: !!token,
  });
}

// ===================== Game Hooks =====================

export function useGameTemplates() {
  return useQuery<GameTemplateEntity[]>({
    queryKey: ['game-templates'],
    queryFn: () => diagnosticApi.getGameTemplates(),
  });
}

export function useGameTemplatesByCategory(category: string) {
  return useQuery<GameTemplateEntity[]>({
    queryKey: ['game-templates', category],
    queryFn: () => diagnosticApi.getGameTemplatesByCategory(category),
    enabled: !!category,
  });
}

export function useStartGame() {
  return useMutation({
    mutationFn: async (params: { userId: string; templateId: string; targetLanguage?: string; targetLevel?: string }): Promise<GameSession> =>
      diagnosticApi.startGame(params),
  });
}

export function useCreateGameTemplate(token: string) {
  return useMutation({
    mutationFn: async (payload: CreateGameTemplateRequest): Promise<GameTemplateEntity> =>
      diagnosticApi.createGameTemplate(payload, token),
  });
}

export function useSubmitGameAnswer(sessionId: string) {
  return useMutation({
    mutationFn: async (payload: GameAnswer): Promise<GameResult> =>
      diagnosticApi.submitGameAnswer(sessionId, payload),
  });
}

// ===================== Community Hooks =====================

export function useCommunities(token: string) {
  return useQuery({
    queryKey: ['communities', token],
    queryFn: () => communityApi.getCommunities(token),
    enabled: !!token,
    select: (data) => data.content,
  });
}

export function useJoinCommunity(token: string) {
  return useMutation({
    mutationFn: async (communityId: string) => communityApi.joinCommunity(communityId, token),
  });
}

export function useLeaveCommunity(token: string) {
  return useMutation({
    mutationFn: async (communityId: string) => communityApi.leaveCommunity(communityId, token),
  });
}

export function useCreateCommunity(token: string) {
  return useMutation({
    mutationFn: async (data: { name: string; description?: string }) =>
      communityApi.createCommunity(data, token),
  });
}

export function useCommunityPosts(token: string, communityId: string) {
  return useQuery({
    queryKey: ['posts', communityId, token],
    queryFn: () => communityApi.getPosts(communityId, token),
    enabled: !!token && !!communityId,
    select: (data) => data.content,
  });
}

export function useCreatePost(token: string, communityId: string) {
  return useMutation({
    mutationFn: async (content: string) =>
      communityApi.createPost(communityId, { content }, token),
  });
}

export function useLikePost(token: string) {
  return useMutation({
    mutationFn: async (postId: string) => communityApi.likePost(postId, token),
  });
}

export function useAddComment(token: string) {
  return useMutation({
    mutationFn: async ({ postId, content }: { postId: string; content: string }) =>
      communityApi.addComment(postId, { content }, token),
  });
}

export function usePostComments(token: string, postId: string) {
  return useQuery({
    queryKey: ['comments', postId, token],
    queryFn: () => communityApi.getComments(postId, token),
    enabled: !!token && !!postId,
    select: (data) => data.content,
  });
}

export function useLeaderboard(token: string) {
  return useQuery<LeaderboardEntry[]>({
    queryKey: ['leaderboard', token],
    queryFn: () => communityApi.getLeaderboard(token),
    enabled: !!token,
  });
}

export function useCommunityLeaderboard(token: string, communityId: string) {
  return useQuery<LeaderboardEntry[]>({
    queryKey: ['leaderboard', communityId, token],
    queryFn: () => communityApi.getCommunityLeaderboard(communityId, token),
    enabled: !!token && !!communityId,
  });
}
