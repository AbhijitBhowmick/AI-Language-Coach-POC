import { describe, it, expect, beforeEach, vi } from 'vitest';
import axios from 'axios';

vi.mock('axios', () => {
  const mockAxiosInstance = {
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  };

  return {
    default: {
      ...mockAxiosInstance,
      create: vi.fn(() => mockAxiosInstance),
    },
  };
});

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

describe('authApi', () => {
  it('login posts to /auth/login with email and password', async () => {
    const mockResponse = { data: { accessToken: 'tok', userId: '1', email: 'a@b.com', tenantId: 't1' } };
    vi.mocked(axios.post).mockResolvedValue(mockResponse);

    const { authApi } = await import('../client');
    const result = await authApi.login({ email: 'a@b.com', password: 'secret' });

    expect(axios.post).toHaveBeenCalledWith(
      expect.stringContaining('/auth/login'),
      { email: 'a@b.com', password: 'secret' },
    );
    expect(result).toEqual(mockResponse.data);
  });

  it('register posts to /auth/register', async () => {
    const mockResponse = { data: { accessToken: 'tok', userId: '1', email: 'a@b.com', tenantId: 't1' } };
    vi.mocked(axios.post).mockResolvedValue(mockResponse);

    const { authApi } = await import('../client');
    const payload = { email: 'a@b.com', password: 'secret', firstName: 'A', lastName: 'B' };
    await authApi.register(payload);

    expect(axios.post).toHaveBeenCalledWith(
      expect.stringContaining('/auth/register'),
      payload,
      expect.objectContaining({ headers: { 'Content-Type': 'application/json' } }),
    );
  });

  it('listUsers calls /auth/users with token header', async () => {
    localStorage.setItem('userId', 'u1');
    localStorage.setItem('tenantId', 't1');
    localStorage.setItem('role', 'USER_STUDENT');
    vi.mocked(axios.get).mockResolvedValue({ data: [] });

    const { authApi } = await import('../client');
    await authApi.listUsers('admin-token');

    expect(axios.get).toHaveBeenCalledWith(
      expect.stringContaining('/auth/users'),
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer admin-token',
          'X-User-Id': 'u1',
          'X-Tenant-Id': 't1',
          'X-Role': 'USER_STUDENT',
        }),
      }),
    );
  });

  it('approveUser posts to /auth/users/:id/approve with optional comment', async () => {
    vi.mocked(axios.post).mockResolvedValue({ data: {} });
    const { authApi } = await import('../client');
    await authApi.approveUser('u1', undefined, 'tok');
    expect(axios.post).toHaveBeenCalledWith(
      expect.stringContaining('/auth/users/u1/approve'),
      {},
      expect.any(Object),
    );
  });
});

describe('diagnosticApi', () => {
  it('getGameTemplates fetches templates without auth', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: [{ id: '1', templateId: 'standard' }] });
    const { diagnosticApi } = await import('../client');
    const result = await diagnosticApi.getGameTemplates();
    expect(axios.get).toHaveBeenCalledWith(
      expect.stringContaining('/api/v1/diagnostic/game/templates'),
    );
    expect(result).toHaveLength(1);
  });

  it('startGame posts with params and auth header', async () => {
    vi.mocked(axios.post).mockResolvedValue({ data: { sessionId: 's1', state: {}, initialRender: {} } });
    const { diagnosticApi } = await import('../client');
    await diagnosticApi.startGame({ userId: 'u1', templateId: 'standard' });
    expect(axios.post).toHaveBeenCalledWith(
      expect.stringContaining('/api/v1/diagnostic/game/start'),
      {},
      expect.objectContaining({ params: { userId: 'u1', templateId: 'standard' } }),
    );
  });
});

describe('profileApi', () => {
  it('get calls /profile with auth header', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: { userId: 'u1', email: 'a@b.com', targetLanguage: 'Czech', targetLevel: 'A1', nativeLanguage: 'en', readinessScore: 0, diagnosticCompleted: false, createdAt: 0, updatedAt: 0 } });
    const { profileApi } = await import('../client');
    await profileApi.get('tok');
    expect(axios.get).toHaveBeenCalledWith(
      expect.stringContaining('/profile'),
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer tok' }) }),
    );
  });
});

describe('voiceApi', () => {
  it('getWebSocketUrl swaps http to ws', async () => {
    const { voiceApi } = await import('../client');
    const url = voiceApi.getWebSocketUrl();
    expect(url).toMatch(/^ws:\/\//);
    expect(url).toContain('/api/v1/voice/chat');
  });
});

describe('communityApi', () => {
  it('getCommunities calls with pagination params', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: { content: [], totalPages: 0, totalElements: 0, number: 0, size: 20, first: true, last: true, empty: true } });
    const { communityApi } = await import('../client');
    await communityApi.getCommunities('tok', 0, 20);
    expect(axios.get).toHaveBeenCalledWith(
      expect.stringContaining('/community/communities'),
      expect.objectContaining({ params: { page: 0, size: 20 } }),
    );
  });
});
