import axios from 'axios';
import type {
  AuthenticationResponse,
  RegisterRequest,
  AdminCreateUserRequest,
  UserResponse,
  UserProfileResponse,
  CreateProfileRequest,
  UpdateProfileRequest,
  DiagnosticTest,
  DiagnosticQuestion,
  AnswerSubmission,
  TestResult,
  GameTemplateEntity,
  GameSession,
  GameAnswer,
  GameResult,
  CreateGameTemplateRequest,
  CommunityResponseDTO,
  CommunityMemberResponseDTO,
  CreateCommunityRequest,
  CreatePostRequest,
  PostResponseDTO,
  CreateCommentRequest,
  CommentResponseDTO,
  LeaderboardEntry,
  Page,
} from '@/types';

const AUTH_SVC = () => process.env.NEXT_PUBLIC_AUTH_API_URL || 'http://localhost:8081';
const PROFILE_SVC = () => process.env.NEXT_PUBLIC_PROFILE_API_URL || 'http://localhost:8082';
const DIAGNOSTIC_SVC = () => process.env.NEXT_PUBLIC_DIAGNOSTIC_API_URL || 'http://localhost:8083';
const VOICE_SVC = () => process.env.NEXT_PUBLIC_VOICE_API_URL || 'http://localhost:8084';
const COMMUNITY_SVC = () => process.env.NEXT_PUBLIC_COMMUNITY_API_URL || 'http://localhost:8085';

const getToken = () => {
  if (typeof window === 'undefined') return null;
  try {
    const stored = localStorage.getItem('auth-storage');
    if (!stored) return null;
    return JSON.parse(stored)?.state?.token || null;
  } catch {
    return null;
  }
};

const getHeaders = (token?: string) => {
  const t = token || getToken();
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (t) headers.Authorization = `Bearer ${t}`;
  if (typeof window !== 'undefined') {
    const tenantId = localStorage.getItem('tenantId');
    const userId = localStorage.getItem('userId');
    const role = localStorage.getItem('role');
    if (tenantId) headers['X-Tenant-Id'] = tenantId;
    if (userId) headers['X-User-Id'] = userId;
    if (role) headers['X-Role'] = role;
  }
  return headers;
};

// Global axios interceptor for JWT + 401 redirect
if (typeof window !== 'undefined') {
  axios.interceptors.request.use((config) => {
    const token = getToken();
    if (token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401 || error.response?.status === 403) {
        localStorage.removeItem('auth-storage');
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('role');
        localStorage.removeItem('tenantId');
        if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
          window.location.href = '/login';
        }
      }
      return Promise.reject(error);
    }
  );
}

// ===================== Auth API =====================

export const authApi = {
  login: async (credentials: { email: string; password: string }): Promise<AuthenticationResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/login`, credentials);
    return data;
  },

  register: async (payload: RegisterRequest): Promise<AuthenticationResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/register`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });
    return data;
  },

  validate: async (token: string): Promise<boolean> => {
    const { data } = await axios.get(`${AUTH_SVC()}/auth/validate`, { headers: { Authorization: `Bearer ${token}` } });
    return data;
  },

  adminCreateUser: async (payload: AdminCreateUserRequest, token: string): Promise<UserResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/users`, payload, { headers: getHeaders(token) });
    return data;
  },

  listUsers: async (token: string): Promise<UserResponse[]> => {
    const { data } = await axios.get(`${AUTH_SVC()}/auth/users`, { headers: getHeaders(token) });
    return data;
  },

  getUser: async (userId: string, token: string): Promise<UserResponse> => {
    const { data } = await axios.get(`${AUTH_SVC()}/auth/users/${userId}`, { headers: getHeaders(token) });
    return data;
  },

  getPendingUsers: async (token: string): Promise<UserResponse[]> => {
    const { data } = await axios.get(`${AUTH_SVC()}/auth/users/pending`, { headers: getHeaders(token) });
    return data;
  },

  approveUser: async (userId: string, comment: string | undefined, token: string): Promise<UserResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/users/${userId}/approve`, comment ? { comment } : {}, { headers: getHeaders(token) });
    return data;
  },

  rejectUser: async (userId: string, reason: string, token: string): Promise<UserResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/users/${userId}/reject`, { reason }, { headers: getHeaders(token) });
    return data;
  },

  suspendUser: async (userId: string, token: string): Promise<UserResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/users/${userId}/suspend`, {}, { headers: getHeaders(token) });
    return data;
  },

  activateUser: async (userId: string, token: string): Promise<UserResponse> => {
    const { data } = await axios.post(`${AUTH_SVC()}/auth/users/${userId}/activate`, {}, { headers: getHeaders(token) });
    return data;
  },
};

// ===================== Profile API =====================

export const profileApi = {
  create: async (payload: CreateProfileRequest, token: string): Promise<UserProfileResponse> => {
    const { data } = await axios.post(`${PROFILE_SVC()}/profile`, payload, { headers: getHeaders(token) });
    return data;
  },

  get: async (token: string): Promise<UserProfileResponse> => {
    const { data } = await axios.get(`${PROFILE_SVC()}/profile`, { headers: getHeaders(token) });
    return data;
  },

  update: async (payload: UpdateProfileRequest, token: string): Promise<UserProfileResponse> => {
    const { data } = await axios.put(`${PROFILE_SVC()}/profile`, payload, { headers: getHeaders(token) });
    return data;
  },

  delete: async (token: string): Promise<void> => {
    await axios.delete(`${PROFILE_SVC()}/profile`, { headers: getHeaders(token) });
  },
};

// ===================== Diagnostic API =====================

export const diagnosticApi = {
  startDiagnostic: async (token: string, params?: { targetLanguage?: string; targetLevel?: string }): Promise<DiagnosticTest> => {
    const { data } = await axios.post(`${DIAGNOSTIC_SVC()}/diagnostic/start`, {}, {
      headers: getHeaders(token),
      params,
    });
    return data;
  },

  getQuestion: async (token: string): Promise<DiagnosticQuestion | null> => {
    try {
      const { data } = await axios.get(`${DIAGNOSTIC_SVC()}/diagnostic/question`, { headers: getHeaders(token) });
      return data;
    } catch {
      return null;
    }
  },

  submitAnswer: async (token: string, payload: AnswerSubmission): Promise<DiagnosticTest> => {
    const { data } = await axios.post(`${DIAGNOSTIC_SVC()}/diagnostic/answer`, payload, { headers: getHeaders(token) });
    return data;
  },

  getResult: async (token: string): Promise<TestResult> => {
    const { data } = await axios.get(`${DIAGNOSTIC_SVC()}/diagnostic/result`, { headers: getHeaders(token) });
    return data;
  },

  // Game endpoints — templates are public, game play requires JWT
  getGameTemplates: async (): Promise<GameTemplateEntity[]> => {
    const { data } = await axios.get(`${DIAGNOSTIC_SVC()}/api/v1/diagnostic/game/templates`);
    return data;
  },

  getGameTemplatesByCategory: async (category: string): Promise<GameTemplateEntity[]> => {
    const { data } = await axios.get(`${DIAGNOSTIC_SVC()}/api/v1/diagnostic/game/templates/${category}`);
    return data;
  },

  createGameTemplate: async (payload: CreateGameTemplateRequest, token: string): Promise<GameTemplateEntity> => {
    const { data } = await axios.post(`${DIAGNOSTIC_SVC()}/api/v1/diagnostic/game/admin/templates`, payload, { headers: getHeaders(token) });
    return data;
  },

  startGame: async (params: { userId: string; templateId: string; targetLanguage?: string; targetLevel?: string }): Promise<GameSession> => {
    const { data } = await axios.post(`${DIAGNOSTIC_SVC()}/api/v1/diagnostic/game/start`, {}, { params, headers: getHeaders() });
    return data;
  },

  getGameQuestion: async (sessionId: string): Promise<unknown> => {
    const { data } = await axios.get(`${DIAGNOSTIC_SVC()}/api/v1/diagnostic/game/${sessionId}/question`, { headers: getHeaders() });
    return data;
  },

  submitGameAnswer: async (sessionId: string, payload: GameAnswer): Promise<GameResult> => {
    const { data } = await axios.post(`${DIAGNOSTIC_SVC()}/api/v1/diagnostic/game/${sessionId}/answer`, payload, { headers: getHeaders() });
    return data;
  },
};

// ===================== Voice API =====================

export const voiceApi = {
  health: async (): Promise<{ status: string }> => {
    const { data } = await axios.get(`${VOICE_SVC()}/api/v1/voice/health`);
    return data;
  },

  getWebSocketUrl: () => `${VOICE_SVC().replace('http', 'ws')}/api/v1/voice/chat`,
};

// ===================== Community API =====================

export const communityApi = {
  createCommunity: async (payload: CreateCommunityRequest, token: string): Promise<CommunityResponseDTO> => {
    const { data } = await axios.post(`${COMMUNITY_SVC()}/community/communities`, payload, { headers: getHeaders(token) });
    return data;
  },

  getCommunities: async (token: string, page = 0, size = 20): Promise<Page<CommunityResponseDTO>> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/communities`, {
      headers: getHeaders(token),
      params: { page, size },
    });
    return data;
  },

  getCommunity: async (id: string, token: string): Promise<CommunityResponseDTO> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/communities/${id}`, { headers: getHeaders(token) });
    return data;
  },

  deleteCommunity: async (id: string, token: string): Promise<void> => {
    await axios.delete(`${COMMUNITY_SVC()}/community/communities/${id}`, { headers: getHeaders(token) });
  },

  joinCommunity: async (communityId: string, token: string): Promise<CommunityMemberResponseDTO> => {
    const { data } = await axios.post(`${COMMUNITY_SVC()}/community/communities/${communityId}/join`, {}, { headers: getHeaders(token) });
    return data;
  },

  leaveCommunity: async (communityId: string, token: string): Promise<void> => {
    await axios.post(`${COMMUNITY_SVC()}/community/communities/${communityId}/leave`, {}, { headers: getHeaders(token) });
  },

  getMembers: async (communityId: string, token: string, page = 0, size = 50): Promise<Page<CommunityMemberResponseDTO>> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/communities/${communityId}/members`, {
      headers: getHeaders(token),
      params: { page, size },
    });
    return data;
  },

  blockMember: async (communityId: string, userId: string, token: string): Promise<void> => {
    await axios.post(`${COMMUNITY_SVC()}/community/communities/${communityId}/members/${userId}/block`, {}, { headers: getHeaders(token) });
  },

  unblockMember: async (communityId: string, userId: string, token: string): Promise<void> => {
    await axios.post(`${COMMUNITY_SVC()}/community/communities/${communityId}/members/${userId}/unblock`, {}, { headers: getHeaders(token) });
  },

  promoteMember: async (communityId: string, userId: string, token: string): Promise<CommunityMemberResponseDTO> => {
    const { data } = await axios.post(`${COMMUNITY_SVC()}/community/communities/${communityId}/members/${userId}/promote`, {}, { headers: getHeaders(token) });
    return data;
  },

  createPost: async (communityId: string, payload: CreatePostRequest, token: string): Promise<PostResponseDTO> => {
    const { data } = await axios.post(`${COMMUNITY_SVC()}/community/communities/${communityId}/posts`, payload, { headers: getHeaders(token) });
    return data;
  },

  getPosts: async (communityId: string, token: string, page = 0, size = 20): Promise<Page<PostResponseDTO>> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/communities/${communityId}/posts`, {
      headers: getHeaders(token),
      params: { page, size },
    });
    return data;
  },

  deletePost: async (postId: string, token: string): Promise<void> => {
    await axios.delete(`${COMMUNITY_SVC()}/community/posts/${postId}`, { headers: getHeaders(token) });
  },

  likePost: async (postId: string, token: string): Promise<void> => {
    await axios.post(`${COMMUNITY_SVC()}/community/posts/${postId}/like`, {}, { headers: getHeaders(token) });
  },

  addComment: async (postId: string, payload: CreateCommentRequest, token: string): Promise<CommentResponseDTO> => {
    const { data } = await axios.post(`${COMMUNITY_SVC()}/community/posts/${postId}/comments`, payload, { headers: getHeaders(token) });
    return data;
  },

  getComments: async (postId: string, token: string, page = 0, size = 20): Promise<Page<CommentResponseDTO>> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/posts/${postId}/comments`, {
      headers: getHeaders(token),
      params: { page, size },
    });
    return data;
  },

  getLeaderboard: async (token: string, limit = 10): Promise<LeaderboardEntry[]> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/leaderboard`, {
      headers: getHeaders(token),
      params: { limit },
    });
    return data;
  },

  getCommunityLeaderboard: async (communityId: string, token: string, limit = 10): Promise<LeaderboardEntry[]> => {
    const { data } = await axios.get(`${COMMUNITY_SVC()}/community/communities/${communityId}/leaderboard`, {
      headers: getHeaders(token),
      params: { limit },
    });
    return data;
  },
};
