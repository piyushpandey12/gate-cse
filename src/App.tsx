import React, { useState, useEffect } from "react";
import { Header } from "./components/layout/Header";
import { Navigation } from "./components/layout/Navigation";
import { VirtualCalculator } from "./components/common/VirtualCalculator";
import { DashboardView } from "./components/dashboard/DashboardView";
import { SubjectsView } from "./components/subjects/SubjectsView";
import { PyqExplorerView } from "./components/pyq/PyqExplorerView";
import { PracticeView } from "./components/practice/PracticeView";
import { ExamSimulatorView } from "./components/exam/ExamSimulatorView";
import { RevisionView } from "./components/revision/RevisionView";
import { MistakesView } from "./components/mistakes/MistakesView";
import { ResourcesView } from "./components/resources/ResourcesView";
import { FlashcardsView } from "./components/flashcards/FlashcardsView";
import { FormulasView } from "./components/formulas/FormulasView";
import { NotesView } from "./components/notes/NotesView";
import { PlannerView } from "./components/planner/PlannerView";
import { AnalyticsView } from "./components/analytics/AnalyticsView";
import { AiTutorView } from "./components/ai/AiTutorView";
import { SettingsView } from "./components/settings/SettingsView";
import { StorageService } from "./services/storage";
import { calculateNextReview, updateTopicMasteryAfterAttempt } from "./services/spacedRepetition";
import {
  NavSection,
  Subject,
  Topic,
  Question,
  Formula,
  Flashcard,
  Note,
  StudyTask,
  Resource,
  ExamEvent,
  UserProfile,
  Attempt,
  TestSession,
  RevisionItem,
  TopicMastery,
  MistakeCategory,
} from "./types";

export function App() {
  // App state loaded from StorageService (Room database parity)
  const [userProfile, setUserProfile] = useState<UserProfile>(StorageService.getUserProfile());
  const [subjects, setSubjects] = useState<Subject[]>(StorageService.getSubjects());
  const [topics, setTopics] = useState<Topic[]>(StorageService.getTopics());
  const [questions, setQuestions] = useState<Question[]>(StorageService.getQuestions());
  const [formulas, setFormulas] = useState<Formula[]>(StorageService.getFormulas());
  const [flashcards, setFlashcards] = useState<Flashcard[]>(StorageService.getFlashcards());
  const [notes, setNotes] = useState<Note[]>(StorageService.getNotes());
  const [tasks, setTasks] = useState<StudyTask[]>(StorageService.getTasks());
  const [resources, setResources] = useState<Resource[]>(StorageService.getResources());
  const [examEvents, setExamEvents] = useState<ExamEvent[]>(StorageService.getExamEvents());
  const [attempts, setAttempts] = useState<Attempt[]>(StorageService.getAttempts());
  const [testSessions, setTestSessions] = useState<TestSession[]>(StorageService.getTestSessions());
  const [revisionItems, setRevisionItems] = useState<RevisionItem[]>(StorageService.getRevisionItems());
  const [topicMasteries, setTopicMasteries] = useState<TopicMastery[]>(StorageService.getTopicMasteries());

  // Navigation & UI state
  const [currentSection, setCurrentSection] = useState<NavSection>("dashboard");
  const [selectedSubjectId, setSelectedSubjectId] = useState<string | null>(null);
  const [practiceSubjectId, setPracticeSubjectId] = useState<string | null>(null);
  const [practiceTopicId, setPracticeTopicId] = useState<string | null>(null);
  const [aiContextQuestion, setAiContextQuestion] = useState<Question | null>(null);

  // Virtual Calculator
  const [showCalculator, setShowCalculator] = useState(false);
  // Mobile sidebar
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // Sync state helpers
  const handleToggleBookmarkQuestion = (questionId: string) => {
    StorageService.toggleBookmarkQuestion(questionId);
    setQuestions(StorageService.getQuestions());
  };

  const handleToggleFavoriteFormula = (formulaId: string) => {
    StorageService.toggleFavoriteFormula(formulaId);
    setFormulas(StorageService.getFormulas());
  };

  const handleToggleBookmarkResource = (resourceId: string) => {
    StorageService.toggleBookmarkResource(resourceId);
    setResources(StorageService.getResources());
  };

  const handleToggleResourceCompleted = (resourceId: string) => {
    StorageService.toggleResourceCompleted(resourceId);
    setResources(StorageService.getResources());
  };

  const handleUpdateResourceNotes = (resourceId: string, notesText: string) => {
    StorageService.updateResourceNotes(resourceId, notesText);
    setResources(StorageService.getResources());
  };

  const handleRecordAttempt = (
    question: Question,
    userAnswer: string,
    isCorrect: boolean,
    marks: number,
    timeSpentSeconds: number = 30,
    mistakeCategory: MistakeCategory | null = null,
    userNote: string | null = null
  ) => {
    const attempt: Attempt = {
      id: Date.now(),
      questionId: question.id,
      selectedAnswer: userAnswer,
      isCorrect,
      marksAwarded: marks,
      timeSpentSeconds,
      attemptedAt: Date.now(),
      mistakeCategory,
      userNote,
    };

    StorageService.saveAttempt(attempt);
    setAttempts(StorageService.getAttempts());

    // Update user stats
    const updatedProfile = { ...userProfile };
    updatedProfile.totalQuestionsSolved += 1;
    if (isCorrect) updatedProfile.totalCorrect += 1;
    StorageService.saveUserProfile(updatedProfile);
    setUserProfile(updatedProfile);

    // Update or create Spaced Repetition (SM-2) item
    const existingRevision = revisionItems.find((r) => r.questionId === question.id);
    const quality = isCorrect ? (timeSpentSeconds < 45 ? 5 : 4) : 1;
    const sm2Result = calculateNextReview(
      quality,
      existingRevision?.repetitionCount || 0,
      existingRevision?.easeFactor || 2.5,
      existingRevision?.intervalDays || 1
    );

    const updatedRevisionItem: RevisionItem = {
      id: existingRevision?.id || "REV_" + question.id,
      questionId: question.id,
      topicId: question.topicId,
      easeFactor: sm2Result.easeFactor,
      intervalDays: sm2Result.intervalDays,
      repetitionCount: sm2Result.repetitionCount,
      nextReviewDueDate: sm2Result.nextReviewDueDate,
      lastReviewedAt: Date.now(),
      isMastered: sm2Result.isMastered,
      isOverdue: false,
    };

    StorageService.saveRevisionItem(updatedRevisionItem);
    setRevisionItems(StorageService.getRevisionItems());

    // Update Topic Mastery Diagnostics
    const allAttemptsForTopic = attempts.concat(attempt).filter((a) => {
      const q = questions.find((ques) => ques.id === a.questionId);
      return q?.topicId === question.topicId;
    });

    const currentMastery = topicMasteries.find((m) => m.topicId === question.topicId);
    const updatedMastery = updateTopicMasteryAfterAttempt(
      question.topicId,
      currentMastery,
      allAttemptsForTopic
    );

    StorageService.saveTopicMastery(updatedMastery);
    setTopicMasteries(StorageService.getTopicMasteries());
  };

  const handleUpdateMistake = (attemptId: number, category: MistakeCategory, note: string) => {
    StorageService.updateAttemptMistake(attemptId, category, note);
    setAttempts(StorageService.getAttempts());
  };

  const handleReviewFlashcard = (flashcardId: string, quality: number) => {
    StorageService.updateFlashcardReview(flashcardId, quality);
    setFlashcards(StorageService.getFlashcards());
  };

  const handleMarkRevisionMastered = (questionId: string) => {
    const item = revisionItems.find((r) => r.questionId === questionId);
    if (item) {
      StorageService.saveRevisionItem({ ...item, isMastered: true, isOverdue: false });
      setRevisionItems(StorageService.getRevisionItems());
    }
  };

  const handleSaveNote = (note: Note) => {
    StorageService.saveNote(note);
    setNotes(StorageService.getNotes());
  };

  const handleDeleteNote = (noteId: string) => {
    StorageService.deleteNote(noteId);
    setNotes(StorageService.getNotes());
  };

  const handleTogglePinNote = (noteId: string) => {
    StorageService.togglePinNote(noteId);
    setNotes(StorageService.getNotes());
  };

  const handleToggleTask = (taskId: string) => {
    StorageService.toggleTask(taskId);
    setTasks(StorageService.getTasks());
  };

  const handleAddTask = (task: StudyTask) => {
    StorageService.saveTask(task);
    setTasks(StorageService.getTasks());
  };

  const handleDeleteTask = (taskId: string) => {
    StorageService.deleteTask(taskId);
    setTasks(StorageService.getTasks());
  };

  const handleLogStudyTime = (minutes: number) => {
    const updated = {
      ...userProfile,
      studyMinutesLogged: userProfile.studyMinutesLogged + minutes,
    };
    StorageService.saveUserProfile(updated);
    setUserProfile(updated);
  };

  const handleSaveTestSession = (session: TestSession) => {
    StorageService.saveTestSession(session);
    setTestSessions(StorageService.getTestSessions());
  };

  const handleUpdateProfile = (profile: Partial<UserProfile>) => {
    const updated = { ...userProfile, ...profile };
    StorageService.saveUserProfile(updated);
    setUserProfile(updated);
  };

  const handleResetDatabase = () => {
    StorageService.resetToSeedData();
    setUserProfile(StorageService.getUserProfile());
    setSubjects(StorageService.getSubjects());
    setTopics(StorageService.getTopics());
    setQuestions(StorageService.getQuestions());
    setFormulas(StorageService.getFormulas());
    setFlashcards(StorageService.getFlashcards());
    setNotes(StorageService.getNotes());
    setTasks(StorageService.getTasks());
    setResources(StorageService.getResources());
    setExamEvents(StorageService.getExamEvents());
    setAttempts(StorageService.getAttempts());
    setTestSessions(StorageService.getTestSessions());
    setRevisionItems(StorageService.getRevisionItems());
    setTopicMasteries(StorageService.getTopicMasteries());
  };

  const handleExportData = () => {
    const data = StorageService.exportAllData();
    const blob = new Blob([data], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `gate_cse_study_data_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  // Navigation handlers
  const handleNavigate = (section: NavSection, payload?: any) => {
    setCurrentSection(section);
    setIsMobileMenuOpen(false);

    if (section === "subject_detail" && payload) {
      setSelectedSubjectId(payload);
    } else if (section === "subjects") {
      setSelectedSubjectId(null);
    } else if (section === "ai_tutor") {
      if (payload && payload.questionText) {
        setAiContextQuestion(payload);
      }
    }
  };

  const handleStartPractice = (subjectId?: string, topicId?: string) => {
    setPracticeSubjectId(subjectId || null);
    setPracticeTopicId(topicId || null);
    setCurrentSection("practice");
  };

  const dueRevisionCount = revisionItems.filter((r) => r.isOverdue && !r.isMastered).length;

  return (
    <div className="min-h-screen bg-[#090D16] text-slate-100 font-sans antialiased flex flex-col">
      {/* Header */}
      <Header
        userProfile={userProfile}
        onToggleMobileMenu={() => setIsMobileMenuOpen((o) => !o)}
        onOpenCalculator={() => setShowCalculator(true)}
      />

      {/* Main App Body */}
      <div className="flex-1 flex max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 gap-6">
        {/* Desktop Navigation Sidebar */}
        <Navigation
          activeSection={currentSection}
          onNavigate={handleNavigate}
          dueRevisionCount={dueRevisionCount}
          isOpenMobile={isMobileMenuOpen}
          onCloseMobile={() => setIsMobileMenuOpen(false)}
        />

        {/* Content Workspace */}
        <main className="flex-1 min-w-0">
          {currentSection === "dashboard" && (
            <DashboardView
              userProfile={userProfile}
              subjects={subjects}
              topics={topics}
              topicMasteries={topicMasteries}
              examEvents={examEvents}
              dueRevisionCount={dueRevisionCount}
              onNavigate={handleNavigate}
              onStartPractice={handleStartPractice}
              onStartExam={() => setCurrentSection("exam")}
            />
          )}

          {(currentSection === "subjects" || currentSection === "subject_detail") && (
            <SubjectsView
              subjects={subjects}
              topics={topics}
              topicMasteries={topicMasteries}
              selectedSubjectId={selectedSubjectId}
              onSelectSubject={(id) => {
                setSelectedSubjectId(id);
                if (id) setCurrentSection("subject_detail");
                else setCurrentSection("subjects");
              }}
              onStartPractice={handleStartPractice}
              onNavigate={handleNavigate}
            />
          )}

          {currentSection === "pyqs" && (
            <PyqExplorerView
              questions={questions}
              subjects={subjects}
              topics={topics}
              initialSubjectId={selectedSubjectId}
              onToggleBookmark={handleToggleBookmarkQuestion}
              onRecordAttempt={(q, ans, ok, m) => handleRecordAttempt(q, ans, ok, m)}
              onAskAi={(q) => {
                setAiContextQuestion(q);
                setCurrentSection("ai_tutor");
              }}
            />
          )}

          {currentSection === "practice" && (
            <PracticeView
              questions={questions}
              subjects={subjects}
              topics={topics}
              initialSubjectId={practiceSubjectId}
              initialTopicId={practiceTopicId}
              onRecordAttempt={handleRecordAttempt}
              onNavigate={handleNavigate}
            />
          )}

          {currentSection === "exam" && (
            <ExamSimulatorView
              allQuestions={questions}
              subjects={subjects}
              onSaveTestSession={handleSaveTestSession}
              onToggleCalc={() => setShowCalculator((c) => !c)}
            />
          )}

          {currentSection === "revision" && (
            <RevisionView
              revisionItems={revisionItems}
              questions={questions}
              subjects={subjects}
              topics={topics}
              onMarkMastered={handleMarkRevisionMastered}
              onStartPractice={handleStartPractice}
              onNavigate={handleNavigate}
            />
          )}

          {currentSection === "mistakes" && (
            <MistakesView
              attempts={attempts}
              questions={questions}
              subjects={subjects}
              topics={topics}
              onUpdateMistake={handleUpdateMistake}
              onStartPractice={handleStartPractice}
            />
          )}

          {currentSection === "resources" && (
            <ResourcesView
              resources={resources}
              subjects={subjects}
              onToggleBookmark={handleToggleBookmarkResource}
              onToggleCompleted={handleToggleResourceCompleted}
              onUpdateNotes={handleUpdateResourceNotes}
            />
          )}

          {currentSection === "flashcards" && (
            <FlashcardsView
              flashcards={flashcards}
              subjects={subjects}
              onReviewFlashcard={handleReviewFlashcard}
            />
          )}

          {currentSection === "formulas" && (
            <FormulasView
              formulas={formulas}
              subjects={subjects}
              onToggleFavorite={handleToggleFavoriteFormula}
            />
          )}

          {currentSection === "notes" && (
            <NotesView
              notes={notes}
              subjects={subjects}
              onSaveNote={handleSaveNote}
              onDeleteNote={handleDeleteNote}
              onTogglePin={handleTogglePinNote}
            />
          )}

          {currentSection === "planner" && (
            <PlannerView
              tasks={tasks}
              examEvents={examEvents}
              subjects={subjects}
              onToggleTask={handleToggleTask}
              onAddTask={handleAddTask}
              onDeleteTask={handleDeleteTask}
              onLogStudyTime={handleLogStudyTime}
            />
          )}

          {currentSection === "analytics" && (
            <AnalyticsView
              userProfile={userProfile}
              subjects={subjects}
              attempts={attempts}
              testSessions={testSessions}
              topicMasteries={topicMasteries}
            />
          )}

          {currentSection === "ai_tutor" && (
            <AiTutorView
              initialQuestion={aiContextQuestion}
            />
          )}

          {currentSection === "settings" && (
            <SettingsView
              userProfile={userProfile}
              onUpdateProfile={handleUpdateProfile}
              onResetDatabase={handleResetDatabase}
              onExportData={handleExportData}
            />
          )}
        </main>
      </div>

      {/* Floating Virtual Calculator */}
      <VirtualCalculator
        isOpen={showCalculator}
        onClose={() => setShowCalculator(false)}
      />
    </div>
  );
}

export default App;
