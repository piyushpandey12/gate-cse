export type SubjectCode = 
  | "OS" 
  | "TOC" 
  | "C_PROG" 
  | "DS" 
  | "ALGO" 
  | "COA" 
  | "DL" 
  | "CD" 
  | "DBMS" 
  | "CN" 
  | "DM" 
  | "EM" 
  | "GA";

export interface Subject {
  id: string;
  code: string;
  name: string;
  description: string;
  iconName: string;
  weightagePercentage: number;
  orderIndex: number;
}

export interface Topic {
  id: string;
  subjectId: string;
  name: string;
  chapter: string;
  description: string;
  totalQuestions: number;
  pyqCount: number;
  highYield: boolean;
  orderIndex: number;
}

export type QuestionType = "MCQ" | "MSQ" | "NAT";
export type Difficulty = "EASY" | "MEDIUM" | "HARD";

export interface Question {
  id: string;
  subjectId: string;
  topicId: string;
  year: number | null;
  setSession?: string | null;
  questionNumber?: number | null;
  questionType: QuestionType;
  marks: number;
  negativeMarks: number;
  difficulty: Difficulty;
  questionText: string;
  optionA?: string | null;
  optionB?: string | null;
  optionC?: string | null;
  optionD?: string | null;
  correctAnswers: string; // "A", "A,C" or "17" / "14:15"
  shortExplanation: string;
  detailedSolution: string;
  shortcutTrick?: string | null;
  commonTrap?: string | null;
  keyFormula?: string | null;
  isPyq: boolean;
  isBookmarked?: boolean;
}

export type MistakeCategory =
  | "CONCEPTUAL"
  | "CALCULATION"
  | "SILLY_MISTAKE"
  | "TIME_PRESSURE"
  | "MISREAD"
  | "FORMULA"
  | "MEMORY"
  | "GUESS"
  | "OTHER";

export interface Attempt {
  id: number;
  questionId: string;
  selectedAnswer: string;
  isCorrect: boolean;
  marksAwarded: number;
  timeSpentSeconds: number;
  mistakeCategory?: MistakeCategory | null;
  userNote?: string | null;
  timestamp: number;
  attemptedAt?: number;
}

export type MasteryLevel = "NEW" | "LEARNING" | "WEAK" | "IMPROVING" | "STRONG" | "MASTERED";

export interface TopicMastery {
  topicId: string;
  attemptsCount: number;
  correctCount: number;
  accuracy: number;
  averageTimeSeconds: number;
  masteryScore: number; // 0..100
  level: MasteryLevel;
  lastAttemptedAt?: number | null;
  needsRevision: boolean;
  needsImmediateAttention: boolean;
  attentionReason?: string | null;
}

export type ResourceType =
  | "PLAYLIST"
  | "NPTEL"
  | "BOOK"
  | "NOTE"
  | "REVISION_VIDEO"
  | "PRACTICE_SET";

export interface Resource {
  id: string;
  subjectId: string;
  topicId?: string | null;
  title: string;
  provider: string;
  resourceType: ResourceType;
  description: string;
  url: string;
  recommendedChapters?: string | null;
  isFree: boolean;
  isBookmarked: boolean;
  isCompleted: boolean;
  personalNotes?: string | null;
}

export interface RevisionItem {
  id: number;
  questionId: string;
  topicId: string;
  intervalDays: number;
  repetitionCount: number;
  nextReviewDueDate: string; // "YYYY-MM-DD"
  easeFactor: number;
  lastQualityRating: number;
  isOverdue: boolean;
  isMastered: boolean;
  lastReviewedAt: number;
}

export interface Flashcard {
  id: string;
  subjectId: string;
  topicId: string;
  front: string;
  back: string;
  keyConcept: string;
  intervalDays: number;
  easeFactor: number;
  nextReviewDate: number;
  repetitionCount?: number;
}

export interface Formula {
  id: string;
  subjectId: string;
  topicId: string;
  name: string;
  formulaLatex: string;
  description: string;
  applications: string;
  isFavorite: boolean;
  title?: string;
  latexFormula?: string;
  shortcutTrick?: string;
  commonTrap?: string;
}

export interface StudyNote {
  id: string;
  subjectId: string;
  topicId?: string | null;
  title: string;
  content: string;
  tags: string | string[];
  updatedAt: number;
  createdAt?: number;
  isPinned?: boolean;
}

export type Note = StudyNote;

export interface StudyTask {
  id: string;
  title: string;
  subjectId: string;
  topicId?: string | null;
  allocatedMinutes: number;
  priority: "HIGH" | "MEDIUM" | "LOW";
  isCompleted: boolean;
  targetDate: string;
  estimatedMinutes?: number;
  targetDateFormatted?: string;
}

export interface StudySession {
  id: number;
  subjectId: string;
  topicName: string;
  durationMinutes: number;
  sessionType: "FOCUS" | "POMODORO" | "PRACTICE";
  timestamp: number;
}

export interface TestSession {
  id: string;
  title: string;
  testType: "FULL_MOCK" | "SECTIONAL" | "TOPIC_TEST";
  totalQuestions: number;
  durationMinutes: number;
  questionIds: string[];
  userAnswers: Record<string, string>;
  markedForReview: string[];
  startedAt: number;
  submittedAt?: number | null;
  totalScore: number;
  accuracy: number;
  isCompleted: boolean;
}

export interface UserProfile {
  id: string;
  name: string;
  targetExam: string;
  targetYear: number;
  dailyTargetHours: number;
  dailyQuestionsTarget: number;
  weeklyTestsTarget: number;
  preferredStudyTime: string;
  role: "STUDENT" | "ADMIN";
  currentStreak: number;
  totalMinutesStudied: number;
  totalQuestionsSolved: number;
  totalCorrect: number;
  preparationLevel: string;
  weakSubjectIds: string;
  targetScore?: number;
  targetRank?: string;
  streakDays?: number;
  studyMinutesLogged?: number;
}

export interface ExamEvent {
  id: string;
  eventName: string;
  eventDateMillis: number;
  eventDateFormatted: string;
  isMainExamDate: boolean;
  sourceUrl: string;
  description?: string;
}

export type NavSection =
  | "dashboard"
  | "subjects"
  | "subject_detail"
  | "topic_detail"
  | "pyqs"
  | "practice"
  | "exam"
  | "exam_simulator"
  | "test_result"
  | "revision"
  | "mistakes"
  | "resources"
  | "flashcards"
  | "formulas"
  | "notes"
  | "planner"
  | "analytics"
  | "ai_tutor"
  | "settings";
