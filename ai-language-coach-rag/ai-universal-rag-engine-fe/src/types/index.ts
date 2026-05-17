export interface LearningContext {
  targetLanguage: string;
  targetLevel: string;
  nativeLanguage: string;
}

export interface UserProfile {
  userId: string;
  email: string;
  context: LearningContext;
  planType: 'FREE' | 'STANDARD' | 'PREMIUM';
  readinessScore: number;
  diagnosticCompleted: boolean;
  createdAt: number;
  updatedAt: number;
}

export function getCurrentLevel(profile: UserProfile): string {
  return profile.context?.targetLevel || 'A1';
}

export function getTargetLanguage(profile: UserProfile): string {
  return profile.context?.targetLanguage || 'czech';
}

export interface LanguageConfig {
  id: string;
  languageCode: string;
  languageName: string;
  level: string;
  enabled: boolean;
  displayOrder: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: string;
    email: string;
  };
}

export interface IngestRequest {
  sourceType: 'file' | 'url' | 'audio_url';
  domain: string;
  targetLang: string;
  level: string;
  contentType: string;
  sourceUrl?: string;
}

export interface IngestResponse {
  status: 'success' | 'pending' | 'failed';
  documentId: string;
  chunksCreated: number;
  tokensProcessed?: number;
  encodingLatencyMs?: number;
  errorMessage?: string;
}

export interface IngestionHistoryItem {
  id: string;
  documentId: string;
  sourceUrl?: string;
  fileName?: string;
  domain: string;
  targetLang: string;
  level: string;
  contentType: string;
  chunksCreated: number;
  status: 'success' | 'pending' | 'failed';
  createdAt: number;
}

export interface PipelineStats {
  encodingLatencyMs: number;
  embeddingLoadPercent: number;
  totalChunksProcessed: number;
  averageLatencyMs: number;
}

export interface MetricData {
  name: string;
  value: number;
  labels: Record<string, string>;
}

export interface ParsedMetrics {
  cacheHitRatio: number;
  cacheHits: number;
  cacheMisses: number;
  requestLatency: number;
  embeddingLatency: number;
  generationLatency: number;
  vectorSearchLatency: number;
  totalRequests: number;
  successfulRequests: number;
  failedRequests: number;
  promptTokens: number;
  completionTokens: number;
}

export interface ConfigurationFormData {
  targetLanguage: string;
  currentLevel: string;
  nativeLanguage: string;
}

export interface DomainOption {
  value: string;
  label: string;
}

export interface ProficiencyLevel {
  value: string;
  label: string;
}

export interface ContentTypeOption {
  value: string;
  label: string;
}

export const DOMAIN_OPTIONS: DomainOption[] = [
  { value: 'language-coach', label: 'Language Coach' },
  { value: 'business-economics', label: 'Business & Economics' },
  { value: 'academic-scientific', label: 'Academic/Scientific' },
  { value: 'casual-conversation', label: 'Casual Conversation' },
  { value: 'technical-documentation', label: 'Technical Documentation' },
];

export const PROFICIENCY_LEVELS: ProficiencyLevel[] = [
  { value: 'a1', label: 'A1 - Beginner' },
  { value: 'a2', label: 'A2 - Elementary' },
  { value: 'b1', label: 'B1 - Intermediate' },
  { value: 'b2', label: 'B2 - Upper Intermediate' },
  { value: 'c1', label: 'C1 - Advanced' },
  { value: 'c2', label: 'C2 - Proficient' },
];

export const CONTENT_TYPE_OPTIONS: ContentTypeOption[] = [
  { value: 'news-article', label: 'News Article' },
  { value: 'textbook-chapter', label: 'Textbook Chapter' },
  { value: 'transcript', label: 'Transcript' },
  { value: 'legal-text', label: 'Legal Text' },
  { value: 'grammar', label: 'Grammar' },
  { value: 'vocabulary', label: 'Vocabulary' },
  { value: 'dialogue', label: 'Dialogue' },
];