import { useQuery, useMutation, UseQueryOptions } from '@tanstack/react-query';
import axios from 'axios';
import type {
  UserProfile,
  LanguageConfig,
  LoginRequest,
  LoginResponse,
  ParsedMetrics,
  PipelineStats,
  IngestionHistoryItem,
} from '@/types';

const JAVA_API_URL = process.env.NEXT_PUBLIC_JAVA_API_URL || 'http://localhost:8080/api/v1';
const RAG_API_URL = process.env.NEXT_PUBLIC_RAG_API_URL || 'http://localhost:8000';

const apiClient = axios.create({
  baseURL: JAVA_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

export function useLogin() {
  return useMutation({
    mutationFn: async (credentials: LoginRequest): Promise<LoginResponse> => {
      const response = await axios.post(`${JAVA_API_URL}/auth/login`, credentials);
      return response.data;
    },
  });
}

export function useRegister() {
  return useMutation({
    mutationFn: async (data: {
      email: string;
      password: string;
      firstName: string;
      lastName: string;
    }) => {
      const response = await axios.post(`${JAVA_API_URL}/auth/register`, data);
      return response.data;
    },
  });
}

export function useUserProfile(options?: Omit<UseQueryOptions<UserProfile>, 'queryKey' | 'queryFn'>) {
  return useQuery<UserProfile>({
    queryKey: ['profile'],
    queryFn: async () => {
      const response = await apiClient.get<UserProfile>('/profile');
      return response.data;
    },
    ...options,
  });
}

export function useUpdateProfile() {
  return useMutation({
    mutationFn: async (data: Partial<UserProfile>) => {
      const response = await apiClient.put<UserProfile>('/profile', data);
      return response.data;
    },
  });
}

export function useLanguages(options?: Omit<UseQueryOptions<LanguageConfig[]>, 'queryKey' | 'queryFn'>) {
  return useQuery<LanguageConfig[]>({
    queryKey: ['languages'],
    queryFn: async () => {
      const response = await apiClient.get<LanguageConfig[]>('/config/languages');
      return response.data;
    },
    ...options,
  });
}

export function useLanguageLevels(languageCode: string, options?: Omit<UseQueryOptions<string[]>, 'queryKey' | 'queryFn'>) {
  return useQuery<string[]>({
    queryKey: ['language-levels', languageCode],
    queryFn: async () => {
      const response = await apiClient.get<string[]>(`/config/languages/${languageCode}/levels`);
      return response.data;
    },
    enabled: !!languageCode,
    ...options,
  });
}

export function useIngestContent() {
  return useMutation({
    mutationFn: async (formData: FormData) => {
      const response = await axios.post(`${RAG_API_URL}/ai/v1/ingest`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    },
  });
}

export function useQueryRAG() {
  return useMutation({
    mutationFn: async (query: {
      query: string;
      nativeLang: string;
      domain: string;
      targetLang: string;
      level: string;
    }) => {
      const response = await axios.post(`${RAG_API_URL}/ai/v1/query`, query);
      return response.data;
    },
  });
}

export function useRAGMetrics(refreshInterval?: number, options?: Omit<UseQueryOptions<string>, 'queryKey' | 'queryFn'>) {
  return useQuery<string>({
    queryKey: ['rag-metrics'],
    queryFn: async () => {
      const response = await axios.get(`${RAG_API_URL}/metrics`);
      return response.data;
    },
    refetchInterval: refreshInterval,
    ...options,
  });
}

export function parseMetrics(metricsText: string): ParsedMetrics {
  const metrics: ParsedMetrics = {
    cacheHitRatio: 0,
    cacheHits: 0,
    cacheMisses: 0,
    requestLatency: 0,
    embeddingLatency: 0,
    generationLatency: 0,
    vectorSearchLatency: 0,
    totalRequests: 0,
    successfulRequests: 0,
    failedRequests: 0,
    promptTokens: 0,
    completionTokens: 0,
  };

  const lines = metricsText.split('\n');

  for (const line of lines) {
    if (line.startsWith('rag_cache_hit_ratio') || line.startsWith('rag_cache_hit_ratio{gateway=')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.cacheHitRatio = parseFloat(match[0]);
    } else if (line.startsWith('rag_cache_hits_total') || line.startsWith('rag_cache_hits_total{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.cacheHits = parseInt(match[0]);
    } else if (line.startsWith('rag_cache_misses_total') || line.startsWith('rag_cache_misses_total{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.cacheMisses = parseInt(match[0]);
    } else if (line.startsWith('rag_request_latency_seconds') || line.startsWith('rag_request_latency_seconds{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.requestLatency = parseFloat(match[0]);
    } else if (line.startsWith('rag_embedding_latency_seconds') || line.startsWith('rag_embedding_latency_seconds{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.embeddingLatency = parseFloat(match[0]);
    } else if (line.startsWith('rag_generation_latency_seconds') || line.startsWith('rag_generation_latency_seconds{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.generationLatency = parseFloat(match[0]);
    } else if (line.startsWith('rag_vector_search_latency_seconds') || line.startsWith('rag_vector_search_latency_seconds{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.vectorSearchLatency = parseFloat(match[0]);
    } else if (line.startsWith('rag_requests_total{endpoint="query",status="success"}') || line.startsWith('rag_requests_total{endpoint=')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.successfulRequests = parseInt(match[0]);
    } else if (line.startsWith('rag_requests_total{endpoint="query",status="error"}')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.failedRequests = parseInt(match[0]);
    } else if (line.startsWith('rag_llm_prompt_tokens_total') || line.startsWith('rag_llm_prompt_tokens_total{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.promptTokens = parseInt(match[0]);
    } else if (line.startsWith('rag_llm_completion_tokens_total') || line.startsWith('rag_llm_completion_tokens_total{')) {
      const match = line.match(/[\d.]+$/);
      if (match) metrics.completionTokens = parseInt(match[0]);
    }
  }

  metrics.totalRequests = metrics.successfulRequests + metrics.failedRequests;

  return metrics;
}

export function useIngestionHistory(options?: Omit<UseQueryOptions<IngestionHistoryItem[]>, 'queryKey' | 'queryFn'>) {
  return useQuery<IngestionHistoryItem[]>({
    queryKey: ['ingestion-history'],
    queryFn: async () => {
      const response = await apiClient.get<IngestionHistoryItem[]>('/ai/v1/ingest/history');
      return response.data;
    },
    ...options,
  });
}

export function usePipelineStats(options?: Omit<UseQueryOptions<PipelineStats>, 'queryKey' | 'queryFn'>) {
  return useQuery<PipelineStats>({
    queryKey: ['pipeline-stats'],
    queryFn: async () => {
      const response = await apiClient.get<PipelineStats>('/ai/v1/stats');
      return response.data;
    },
    refetchInterval: 30000,
    ...options,
  });
}
