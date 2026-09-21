import {
  Subject,
  Topic,
  Question,
  Resource,
  Flashcard,
  Formula,
  StudyNote,
  StudyTask,
  StudySession,
  TestSession,
  UserProfile,
  Attempt,
  RevisionItem,
  TopicMastery,
  ExamEvent,
  MistakeCategory,
} from "../types";
import {
  INITIAL_SUBJECTS,
  INITIAL_TOPICS,
  INITIAL_QUESTIONS,
  INITIAL_RESOURCES,
  INITIAL_FORMULAS,
  INITIAL_FLASHCARDS,
  INITIAL_TASKS,
  INITIAL_EXAM_EVENTS,
  INITIAL_USER_PROFILE,
} from "../data/seedData";
import {
  calculateNextSchedule,
  assessTopicAttention,
} from "./spacedRepetition";

const STORAGE_KEYS = {
  QUESTIONS: "gate_cse_questions_v1",
  ATTEMPTS: "gate_cse_attempts_v1",
  REVISION_ITEMS: "gate_cse_revision_items_v1",
  RESOURCES: "gate_cse_resources_v1",
  FLASHCARDS: "gate_cse_flashcards_v1",
  FORMULAS: "gate_cse_formulas_v1",
  NOTES: "gate_cse_notes_v1",
  TASKS: "gate_cse_tasks_v1",
  SESSIONS: "gate_cse_sessions_v1",
  TEST_SESSIONS: "gate_cse_test_sessions_v1",
  USER_PROFILE: "gate_cse_user_profile_v1",
};

export class StorageService {
  static getSubjects(): Subject[] {
    return INITIAL_SUBJECTS;
  }

  static getTopics(): Topic[] {
    return INITIAL_TOPICS;
  }

  static getExamEvents(): ExamEvent[] {
    return INITIAL_EXAM_EVENTS;
  }

  static getQuestions(): Question[] {
    const raw = localStorage.getItem(STORAGE_KEYS.QUESTIONS);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.QUESTIONS, JSON.stringify(INITIAL_QUESTIONS));
      return INITIAL_QUESTIONS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return INITIAL_QUESTIONS;
    }
  }

  static toggleQuestionBookmark(questionId: string): Question[] {
    const questions = this.getQuestions();
    const updated = questions.map((q) =>
      q.id === questionId ? { ...q, isBookmarked: !q.isBookmarked } : q
    );
    localStorage.setItem(STORAGE_KEYS.QUESTIONS, JSON.stringify(updated));
    return updated;
  }

  static getAttempts(): Attempt[] {
    const raw = localStorage.getItem(STORAGE_KEYS.ATTEMPTS);
    if (!raw) return [];
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }

  static recordAttempt(
    question: Question,
    selectedAnswer: string,
    isCorrect: boolean,
    marksAwarded: number,
    timeSpentSeconds: number,
    mistakeCategory?: MistakeCategory | null,
    userNote?: string | null
  ): Attempt {
    const attempts = this.getAttempts();
    const newAttempt: Attempt = {
      id: Date.now() + Math.floor(Math.random() * 1000),
      questionId: question.id,
      selectedAnswer,
      isCorrect,
      marksAwarded,
      timeSpentSeconds,
      mistakeCategory,
      userNote,
      timestamp: Date.now(),
    };
    attempts.push(newAttempt);
    localStorage.setItem(STORAGE_KEYS.ATTEMPTS, JSON.stringify(attempts));

    // Update revision item schedule
    const revisionItems = this.getRevisionItems();
    const existingRev = revisionItems.find((r) => r.questionId === question.id) || null;
    const schedule = calculateNextSchedule(existingRev, newAttempt, question.difficulty);

    const todayStr = new Date().toISOString().split("T")[0];
    const isOverdue = schedule.nextReviewDueDate <= todayStr;

    const updatedRevItem: RevisionItem = {
      id: existingRev?.id ?? Date.now(),
      questionId: question.id,
      topicId: question.topicId,
      intervalDays: schedule.intervalDays,
      repetitionCount: schedule.repetitionCount,
      nextReviewDueDate: schedule.nextReviewDueDate,
      easeFactor: schedule.easeFactor,
      lastQualityRating: schedule.qualityRating,
      isOverdue,
      isMastered: schedule.isMastered,
      lastReviewedAt: Date.now(),
    };

    const nextRevs = revisionItems.filter((r) => r.questionId !== question.id);
    nextRevs.push(updatedRevItem);
    localStorage.setItem(STORAGE_KEYS.REVISION_ITEMS, JSON.stringify(nextRevs));

    // Update User Profile stats
    const profile = this.getUserProfile();
    profile.totalQuestionsSolved += 1;
    if (isCorrect) profile.totalCorrect += 1;
    this.saveUserProfile(profile);

    return newAttempt;
  }

  static updateMistakeDetails(
    attemptId: number,
    mistakeCategory: MistakeCategory,
    userNote?: string | null
  ): Attempt[] {
    const attempts = this.getAttempts();
    const updated = attempts.map((a) =>
      a.id === attemptId ? { ...a, mistakeCategory, userNote: userNote ?? a.userNote } : a
    );
    localStorage.setItem(STORAGE_KEYS.ATTEMPTS, JSON.stringify(updated));
    return updated;
  }

  static getRevisionItems(): RevisionItem[] {
    const raw = localStorage.getItem(STORAGE_KEYS.REVISION_ITEMS);
    if (!raw) return [];
    try {
      const items: RevisionItem[] = JSON.parse(raw);
      const todayStr = new Date().toISOString().split("T")[0];
      return items.map((it) => ({
        ...it,
        isOverdue: it.nextReviewDueDate <= todayStr && !it.isMastered,
      }));
    } catch {
      return [];
    }
  }

  static markRevisionMastered(questionId: string): RevisionItem[] {
    const items = this.getRevisionItems();
    const updated = items.map((it) =>
      it.questionId === questionId
        ? { ...it, isMastered: true, intervalDays: 60, isOverdue: false }
        : it
    );
    localStorage.setItem(STORAGE_KEYS.REVISION_ITEMS, JSON.stringify(updated));
    return updated;
  }

  static getTopicMasteries(): TopicMastery[] {
    const topics = this.getTopics();
    const attempts = this.getAttempts();
    const questions = this.getQuestions();

    return topics.map((topic) => {
      const topicQuestionIds = new Set(
        questions.filter((q) => q.topicId === topic.id).map((q) => q.id)
      );
      const topicAttempts = attempts.filter((a) => topicQuestionIds.has(a.questionId));
      const attemptsCount = topicAttempts.length;
      const correctCount = topicAttempts.filter((a) => a.isCorrect).length;
      const accuracy = attemptsCount > 0 ? (correctCount / attemptsCount) * 100 : 0;
      const totalTime = topicAttempts.reduce((acc, a) => acc + a.timeSpentSeconds, 0);
      const averageTimeSeconds = attemptsCount > 0 ? Math.round(totalTime / attemptsCount) : 0;

      // Calculate mastery score 0..100
      let masteryScore = 0;
      if (attemptsCount > 0) {
        const accuracyFactor = accuracy * 0.7;
        const volumeFactor = Math.min(30, (attemptsCount / 5) * 30);
        masteryScore = Math.min(100, Math.round(accuracyFactor + volumeFactor));
      }

      let level: TopicMastery["level"] = "NEW";
      if (attemptsCount === 0) level = "NEW";
      else if (accuracy >= 85 && attemptsCount >= 4) level = "MASTERED";
      else if (accuracy >= 70) level = "STRONG";
      else if (accuracy >= 50) level = "IMPROVING";
      else if (accuracy >= 30) level = "LEARNING";
      else level = "WEAK";

      const assessment = assessTopicAttention(topic.id, topicAttempts, masteryScore);

      return {
        topicId: topic.id,
        attemptsCount,
        correctCount,
        accuracy: Math.round(accuracy),
        averageTimeSeconds,
        masteryScore,
        level,
        lastAttemptedAt: topicAttempts.length > 0 ? topicAttempts[topicAttempts.length - 1].timestamp : null,
        needsRevision: assessment.needsImmediateAttention,
        needsImmediateAttention: assessment.needsImmediateAttention,
        attentionReason: assessment.reason,
      };
    });
  }

  static getResources(): Resource[] {
    const raw = localStorage.getItem(STORAGE_KEYS.RESOURCES);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.RESOURCES, JSON.stringify(INITIAL_RESOURCES));
      return INITIAL_RESOURCES;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return INITIAL_RESOURCES;
    }
  }

  static toggleResourceBookmark(id: string): Resource[] {
    const resources = this.getResources();
    const updated = resources.map((r) =>
      r.id === id ? { ...r, isBookmarked: !r.isBookmarked } : r
    );
    localStorage.setItem(STORAGE_KEYS.RESOURCES, JSON.stringify(updated));
    return updated;
  }

  static toggleResourceCompleted(id: string): Resource[] {
    const resources = this.getResources();
    const updated = resources.map((r) =>
      r.id === id ? { ...r, isCompleted: !r.isCompleted } : r
    );
    localStorage.setItem(STORAGE_KEYS.RESOURCES, JSON.stringify(updated));
    return updated;
  }

  static updateResourceNotes(id: string, notes: string): Resource[] {
    const resources = this.getResources();
    const updated = resources.map((r) =>
      r.id === id ? { ...r, personalNotes: notes } : r
    );
    localStorage.setItem(STORAGE_KEYS.RESOURCES, JSON.stringify(updated));
    return updated;
  }

  static getFlashcards(): Flashcard[] {
    const raw = localStorage.getItem(STORAGE_KEYS.FLASHCARDS);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.FLASHCARDS, JSON.stringify(INITIAL_FLASHCARDS));
      return INITIAL_FLASHCARDS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return INITIAL_FLASHCARDS;
    }
  }

  static reviewFlashcard(flashcardId: string, quality: number): Flashcard[] {
    const list = this.getFlashcards();
    const updated = list.map((fc) => {
      if (fc.id !== flashcardId) return fc;
      let easeFactor = fc.easeFactor;
      let interval = fc.intervalDays;
      let rep = (fc.repetitionCount ?? 0) + 1;

      // SM-2 for flashcard
      const delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
      easeFactor = Math.max(1.3, easeFactor + delta);

      if (quality >= 3) {
        if (rep === 1) interval = 1;
        else if (rep === 2) interval = 3;
        else interval = Math.round(interval * easeFactor);
      } else {
        interval = 1;
        rep = 0;
      }

      return {
        ...fc,
        intervalDays: interval,
        easeFactor: Math.round(easeFactor * 100) / 100,
        repetitionCount: rep,
        nextReviewDate: Date.now() + interval * 86400000,
      };
    });

    localStorage.setItem(STORAGE_KEYS.FLASHCARDS, JSON.stringify(updated));
    return updated;
  }

  static getFormulas(): Formula[] {
    const raw = localStorage.getItem(STORAGE_KEYS.FORMULAS);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.FORMULAS, JSON.stringify(INITIAL_FORMULAS));
      return INITIAL_FORMULAS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return INITIAL_FORMULAS;
    }
  }

  static toggleFormulaFavorite(id: string): Formula[] {
    const formulas = this.getFormulas();
    const updated = formulas.map((f) =>
      f.id === id ? { ...f, isFavorite: !f.isFavorite } : f
    );
    localStorage.setItem(STORAGE_KEYS.FORMULAS, JSON.stringify(updated));
    return updated;
  }

  static getNotes(): StudyNote[] {
    const raw = localStorage.getItem(STORAGE_KEYS.NOTES);
    if (!raw) {
      const initial: StudyNote[] = [
        {
          id: "NOTE_1",
          subjectId: "OS",
          topicId: "OS_DEADLOCK",
          title: "Banker's Safety Algorithm Quick Checklist",
          content: "1. Work = Available\n2. Finish[i] = false for all i\n3. Find an i such that Finish[i] == false and Need[i] <= Work\n4. Work = Work + Allocation[i], Finish[i] = true\n5. Repeat until all Finish are true (Safe) or stuck (Unsafe).",
          tags: "Deadlocks, Formulas, ExamTrick",
          updatedAt: Date.now(),
        },
      ];
      localStorage.setItem(STORAGE_KEYS.NOTES, JSON.stringify(initial));
      return initial;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }

  static saveNote(note: Omit<StudyNote, "id" | "updatedAt"> & { id?: string }): StudyNote[] {
    const notes = this.getNotes();
    const id = note.id || "NOTE_" + Math.random().toString(36).substring(2, 9);
    const existingIndex = notes.findIndex((n) => n.id === id);
    const updatedNote: StudyNote = {
      ...note,
      id,
      updatedAt: Date.now(),
    };

    if (existingIndex >= 0) {
      notes[existingIndex] = updatedNote;
    } else {
      notes.unshift(updatedNote);
    }

    localStorage.setItem(STORAGE_KEYS.NOTES, JSON.stringify(notes));
    return notes;
  }

  static deleteNote(id: string): StudyNote[] {
    const notes = this.getNotes().filter((n) => n.id !== id);
    localStorage.setItem(STORAGE_KEYS.NOTES, JSON.stringify(notes));
    return notes;
  }

  static getTasks(): StudyTask[] {
    const raw = localStorage.getItem(STORAGE_KEYS.TASKS);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(INITIAL_TASKS));
      return INITIAL_TASKS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return INITIAL_TASKS;
    }
  }

  static toggleTaskStatus(taskId: string): StudyTask[] {
    const tasks = this.getTasks();
    const updated = tasks.map((t) =>
      t.id === taskId ? { ...t, isCompleted: !t.isCompleted } : t
    );
    localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(updated));
    return updated;
  }

  static addTask(task: Omit<StudyTask, "id">): StudyTask[] {
    const tasks = this.getTasks();
    const newTask: StudyTask = {
      ...task,
      id: "TASK_" + Math.random().toString(36).substring(2, 9),
    };
    tasks.unshift(newTask);
    localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(tasks));
    return tasks;
  }

  static deleteTask(taskId: string): StudyTask[] {
    const tasks = this.getTasks().filter((t) => t.id !== taskId);
    localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(tasks));
    return tasks;
  }

  static getStudySessions(): StudySession[] {
    const raw = localStorage.getItem(STORAGE_KEYS.SESSIONS);
    if (!raw) return [];
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }

  static logStudySession(
    subjectId: string,
    topicName: string,
    durationMinutes: number,
    sessionType: StudySession["sessionType"] = "FOCUS"
  ): StudySession[] {
    const sessions = this.getStudySessions();
    const newSession: StudySession = {
      id: Date.now(),
      subjectId,
      topicName,
      durationMinutes,
      sessionType,
      timestamp: Date.now(),
    };
    sessions.unshift(newSession);
    localStorage.setItem(STORAGE_KEYS.SESSIONS, JSON.stringify(sessions));

    const profile = this.getUserProfile();
    profile.totalMinutesStudied += durationMinutes;
    this.saveUserProfile(profile);

    return sessions;
  }

  static getTestSessions(): TestSession[] {
    const raw = localStorage.getItem(STORAGE_KEYS.TEST_SESSIONS);
    if (!raw) return [];
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }

  static saveTestSession(session: TestSession): TestSession[] {
    const sessions = this.getTestSessions();
    const filtered = sessions.filter((s) => s.id !== session.id);
    filtered.unshift(session);
    localStorage.setItem(STORAGE_KEYS.TEST_SESSIONS, JSON.stringify(filtered));
    return filtered;
  }

  static getUserProfile(): UserProfile {
    const raw = localStorage.getItem(STORAGE_KEYS.USER_PROFILE);
    if (!raw) {
      localStorage.setItem(STORAGE_KEYS.USER_PROFILE, JSON.stringify(INITIAL_USER_PROFILE));
      return INITIAL_USER_PROFILE;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return INITIAL_USER_PROFILE;
    }
  }

  static saveUserProfile(profile: UserProfile): UserProfile {
    localStorage.setItem(STORAGE_KEYS.USER_PROFILE, JSON.stringify(profile));
    return profile;
  }

  static toggleBookmarkQuestion(questionId: string): Question[] {
    return this.toggleQuestionBookmark(questionId);
  }

  static toggleFavoriteFormula(id: string): Formula[] {
    return this.toggleFormulaFavorite(id);
  }

  static toggleBookmarkResource(id: string): Resource[] {
    return this.toggleResourceBookmark(id);
  }

  static saveAttempt(attempt: Attempt): Attempt[] {
    const attempts = this.getAttempts();
    attempts.push(attempt);
    localStorage.setItem(STORAGE_KEYS.ATTEMPTS, JSON.stringify(attempts));
    return attempts;
  }

  static saveRevisionItem(item: RevisionItem): RevisionItem[] {
    const items = this.getRevisionItems();
    const filtered = items.filter((it) => it.id !== item.id && it.questionId !== item.questionId);
    filtered.push(item);
    localStorage.setItem(STORAGE_KEYS.REVISION_ITEMS, JSON.stringify(filtered));
    return filtered;
  }

  static saveTopicMastery(mastery: TopicMastery): void {
    const key = "gate_cse_topic_mastery_custom";
    const raw = localStorage.getItem(key);
    let list: TopicMastery[] = raw ? JSON.parse(raw) : [];
    list = list.filter((m) => m.topicId !== mastery.topicId);
    list.push(mastery);
    localStorage.setItem(key, JSON.stringify(list));
  }

  static updateAttemptMistake(
    attemptId: number,
    mistakeCategory: MistakeCategory,
    userNote?: string | null
  ): Attempt[] {
    return this.updateMistakeDetails(attemptId, mistakeCategory, userNote);
  }

  static updateFlashcardReview(flashcardId: string, quality: number): Flashcard[] {
    return this.reviewFlashcard(flashcardId, quality);
  }

  static togglePinNote(noteId: string): StudyNote[] {
    const notes = this.getNotes();
    const updated = notes.map((n) =>
      n.id === noteId ? { ...n, isPinned: !n.isPinned } : n
    );
    localStorage.setItem(STORAGE_KEYS.NOTES, JSON.stringify(updated));
    return updated;
  }

  static toggleTask(taskId: string): StudyTask[] {
    return this.toggleTaskStatus(taskId);
  }

  static saveTask(task: StudyTask): StudyTask[] {
    return this.addTask(task);
  }

  static resetToSeedData(): void {
    this.resetDatabase();
  }

  static exportAllData(): string {
    const allData = {
      profile: this.getUserProfile(),
      attempts: this.getAttempts(),
      revisionItems: this.getRevisionItems(),
      notes: this.getNotes(),
      tasks: this.getTasks(),
      resources: this.getResources(),
      flashcards: this.getFlashcards(),
      formulas: this.getFormulas(),
      exportTimestamp: Date.now(),
    };
    return JSON.stringify(allData, null, 2);
  }

  static resetDatabase(): void {
    localStorage.setItem(STORAGE_KEYS.QUESTIONS, JSON.stringify(INITIAL_QUESTIONS));
    localStorage.removeItem(STORAGE_KEYS.ATTEMPTS);
    localStorage.removeItem(STORAGE_KEYS.REVISION_ITEMS);
    localStorage.setItem(STORAGE_KEYS.RESOURCES, JSON.stringify(INITIAL_RESOURCES));
    localStorage.setItem(STORAGE_KEYS.FLASHCARDS, JSON.stringify(INITIAL_FLASHCARDS));
    localStorage.setItem(STORAGE_KEYS.FORMULAS, JSON.stringify(INITIAL_FORMULAS));
    localStorage.removeItem(STORAGE_KEYS.NOTES);
    localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(INITIAL_TASKS));
    localStorage.removeItem(STORAGE_KEYS.SESSIONS);
    localStorage.removeItem(STORAGE_KEYS.TEST_SESSIONS);
    localStorage.setItem(STORAGE_KEYS.USER_PROFILE, JSON.stringify(INITIAL_USER_PROFILE));
  }
}
