// ===================== Auth Types =====================

export type UserRole = 'SUPER_ADMIN' | 'BUSINESS_ADMIN' | 'ADMIN_TEACHER' | 'USER_STUDENT';
export type UserStatus = 'ACTIVE' | 'PENDING_APPROVAL' | 'REJECTED' | 'SUSPENDED';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role?: UserRole;
  businessName?: string;
  targetLanguage?: string;
  targetLevel?: string;
  nativeLanguage?: string;
}

export interface AuthenticationResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  tenantId: string;
  status?: UserStatus;
  roles?: UserRole[];
  firstName?: string;
  lastName?: string;
}

export interface UserResponse {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
  tenantId: string;
  createdAt: number;
  updatedAt: number;
}

export interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
  tenantId: string;
}

export interface AdminCreateUserRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  businessName?: string;
  targetLanguage?: string;
  targetLevel?: string;
  nativeLanguage?: string;
}

// ===================== Profile Types =====================

export interface CreateProfileRequest {
  targetLanguage?: string;
  targetLevel?: string;
  nativeLanguage?: string;
}

export interface UpdateProfileRequest {
  targetLanguage?: string;
  targetLevel?: string;
  nativeLanguage?: string;
}

export interface UserProfileResponse {
  userId: string;
  email: string;
  targetLanguage: string;
  targetLevel: string;
  nativeLanguage: string;
  readinessScore: number;
  diagnosticCompleted: boolean;
  createdAt: number;
  updatedAt: number;
}

// ===================== Diagnostic Types =====================

export type QuestionType =
  | 'LISTENING_FILL_BLANK'
  | 'VISUAL_MULTIPLE_CHOICE'
  | 'GRAMMAR_COMPLETION'
  | 'DIALOGUE_COMPLETION';

export interface DiagnosticQuestion {
  questionNumber: number;
  type: QuestionType;
  targetLanguage: string;
  targetLevel: string;
  nativeLanguage: string;
  questionText: string;
  audioUrl?: string;
  imageUrl?: string;
  situation?: string;
  options?: string[];
  correctAnswer: string;
  explanation?: string;
  level: string;
  linguisticBridges?: Record<string, string>;
}

export interface DiagnosticTest {
  userId: string;
  targetLanguage: string;
  targetLevel: string;
  nativeLanguage: string;
  questions: DiagnosticQuestion[];
  currentQuestionIndex: number;
  correctAnswers: number;
  completed: boolean;
  startedAt: number;
  completedAt: number;
}

export interface AnswerSubmission {
  questionNumber: number;
  answer: string;
}

export interface QuestionFeedback {
  questionNumber: number;
  correct: boolean;
  correctAnswer: string;
  explanation: string;
}

export interface TestResult {
  targetLanguage: string;
  targetLevel: string;
  totalQuestions: number;
  correctAnswers: number;
  scorePercentage: number;
  recommendedLevel: string;
  feedback: QuestionFeedback[];
}

// ===================== Game Types =====================

export type GameTemplateId =
  | 'standard'
  | 'match_up'
  | 'cloze_text'
  | 'anagram'
  | 'whack_a_mole'
  | 'speaking_card'
  | 'branching'
  | 'group_sort';

export type GameCategory = 'VOCABULARY' | 'GRAMMAR' | 'LISTENING' | 'SPEAKING' | 'CUSTOM';
export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export interface GameTemplateEntity {
  templateId: GameTemplateId;
  displayName: string;
  templateCategory: GameCategory;
  displayOrder: number;
  iconClass: string;
  description: string;
  defaultTimeSeconds: number;
  pointsPerCorrect: number;
  penaltyPoints: number;
  livesEnabled: boolean;
  branchingEnabled: boolean;
  minQuestions: number;
  maxQuestions: number;
}

export interface CreateGameTemplateRequest {
  templateId: string;
  displayName: string;
  templateCategory: GameCategory;
  description?: string;
  defaultTimeSeconds: number;
  questions: { questionText: string; options: string[]; correctAnswer: string }[];
}

export interface GameSessionState {
  sessionId: string;
  userId: string;
  templateId: GameTemplateId;
  currentQuestionIndex: number;
  totalQuestions: number;
  score: number;
  streakCount: number;
  livesRemaining: number;
  correctAnswers: number;
  startTimeMs: number;
  isCompleted: boolean;
}

export interface GameSession {
  sessionId: string;
  state: GameSessionState;
  initialRender: GameRenderData;
}

export interface GameQuestion {
  questionNumber: number;
  type: QuestionType;
  questionText: string;
  options?: string[];
  correctAnswer: string;
  level: string;
}

export interface GameRenderData {
  templateId: GameTemplateId;
  displayName: string;
  questionNumber: number;
  totalQuestions: number;
  instructionText: string;
  mediaUrl?: string;
  mediaType?: string;
  questionText: string;
  options?: string[];
  answerSlots?: string[];
  scrambledWords?: string[];
  targetItems?: Record<string, string>;
  branches?: Record<string, string>;
  categories?: string[];
  sortItems?: string[];
  timeLimitSeconds: number;
  pointsValue: number;
  livesRemaining: number;
  currentScore: number;
  streakCount: number;
  additionalData: Record<string, unknown>;
}

export interface GameAnswer {
  sessionId: string;
  questionIndex: number;
  answer?: string;
  selectedItems?: string[];
  matchedPairs?: Record<string, string>;
  branchChoice?: string;
  filledGaps?: string[];
  sortedCategories?: string[];
  responseTimeMs: number;
  timeout: boolean;
}

export interface GameResult {
  correct: boolean;
  pointsEarned: number;
  totalScore: number;
  streakCount: number;
  livesRemaining: number;
  correctAnswer: string;
  explanation?: string;
  linguisticBridge?: string;
  responseTimeMs: number;
  timeout: boolean;
  nextQuestionId?: string;
  isCompleted: boolean;
  endData?: GameEndData;
}

export interface GameEndData {
  totalQuestions: number;
  correctAnswers: number;
  totalScore: number;
  bestStreak: number;
  totalTimeMs: number;
  performanceRating: string;
  recommendedLevel: string;
}

// ===================== Voice / WebSocket Types =====================

export interface VoiceStartSession {
  type: 'START_SESSION';
  userId: string;
  token: string;
}

export interface VoiceSessionStarted {
  type: 'SESSION_STARTED';
  sessionId: string;
  language: string;
  level: string;
}

export interface VoiceAudioData {
  type: 'AUDIO_DATA';
  audio: string;
}

export interface VoiceAudioEnd {
  type: 'AUDIO_END';
  transcript?: string;
}

export interface VoiceTextResponse {
  type: 'TEXT_RESPONSE';
  text: string;
  sessionId: string;
}

export interface VoiceTranscription {
  type: 'TRANSCRIPTION';
  text: string;
  confidence: number;
}

export interface VoiceCorrection {
  type: 'CORRECTION';
  original: string;
  corrected: string;
  explanation: string;
}

export interface VoiceError {
  type: 'ERROR';
  message: string;
}

export interface VoiceEndSession {
  type: 'END_SESSION';
  sessionId: string;
}

export interface VoiceSessionEnded {
  type: 'SESSION_ENDED';
  sessionId: string;
  summary: {
    duration: number;
    exchanges: number;
    score: number;
  };
}

export type VoiceClientMessage = VoiceStartSession | VoiceAudioData | VoiceAudioEnd | VoiceEndSession;
export type VoiceServerMessage = VoiceSessionStarted | VoiceTextResponse | VoiceTranscription | VoiceCorrection | VoiceError | VoiceSessionEnded;

// ===================== Community Types =====================

export interface CreateCommunityRequest {
  name: string;
  description?: string;
}

export interface CommunityResponseDTO {
  id: string;
  name: string;
  description?: string;
  tenantId: string;
  createdBy: string;
  adminTeacherId?: string;
  isActive: boolean;
  createdAt: string;
}

export interface CommunityMemberResponseDTO {
  id: string;
  communityId: string;
  userId: string;
  role: 'MEMBER' | 'ADMIN';
  blocked: boolean;
  joinedAt: string;
}

export interface CreatePostRequest {
  content: string;
}

export interface PostResponseDTO {
  id: string;
  communityId: string;
  userId: string;
  content: string;
  likesCount: number;
  commentsCount: number;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}

export interface CommentResponseDTO {
  id: string;
  postId: string;
  userId: string;
  content: string;
  createdAt: string;
}

export interface LeaderboardEntry {
  userId: string;
  totalScore: number;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ===================== UI Convenience Types =====================

export interface CommunityWithJoinStatus extends CommunityResponseDTO {
  memberCount?: number;
  isJoined?: boolean;
}

export interface PostWithUserInfo extends PostResponseDTO {
  userName?: string;
  userAvatar?: string;
  isLiked?: boolean;
}

export interface LeaderboardEntryWithUser extends LeaderboardEntry {
  rank?: number;
  userName?: string;
  avatar?: string;
}
