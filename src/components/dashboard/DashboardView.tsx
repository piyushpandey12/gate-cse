import React from "react";
import {
  Clock,
  Target,
  AlertTriangle,
  Play,
  RotateCcw,
  Sparkles,
  BookOpen,
  ArrowRight,
  TrendingUp,
  Award,
  ChevronRight,
} from "lucide-react";
import {
  Subject,
  Topic,
  UserProfile,
  TopicMastery,
  ExamEvent,
  NavSection,
} from "../../types";

interface DashboardViewProps {
  userProfile: UserProfile;
  subjects: Subject[];
  topics: Topic[];
  topicMasteries: TopicMastery[];
  examEvents: ExamEvent[];
  dueRevisionCount: number;
  onNavigate: (section: NavSection, payload?: any) => void;
  onStartPractice: (subjectId?: string, topicId?: string) => void;
  onStartExam: () => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  userProfile,
  subjects,
  topics,
  topicMasteries,
  examEvents,
  dueRevisionCount,
  onNavigate,
  onStartPractice,
  onStartExam,
}) => {
  const mainExam = examEvents.find((e) => e.isMainExamDate) || examEvents[2];
  const now = Date.now();
  const diffDays = mainExam
    ? Math.max(0, Math.ceil((mainExam.eventDateMillis - now) / (1000 * 60 * 60 * 24)))
    : 320;

  // Topics needing immediate attention
  const urgentTopics = topicMasteries
    .filter((m) => m.needsImmediateAttention)
    .slice(0, 3);

  // Daily target progress
  const targetQuestions = userProfile.dailyQuestionsTarget || 15;
  const solvedToday = Math.min(userProfile.totalQuestionsSolved, targetQuestions);
  const progressPercent = Math.round((solvedToday / targetQuestions) * 100);

  return (
    <div id="dashboard-view" className="space-y-6 pb-12">
      {/* Top Banner: Exam Countdown & Daily Target Hero */}
      <div className="relative overflow-hidden rounded-2xl border border-cyan-900/40 bg-gradient-to-br from-slate-900 via-slate-900 to-cyan-950/40 p-6 shadow-xl">
        <div className="relative z-10 flex flex-col justify-between gap-6 md:flex-row md:items-center">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 rounded-full border border-cyan-500/30 bg-cyan-500/10 px-3 py-1 text-xs font-semibold text-cyan-300">
              <Clock className="h-3.5 w-3.5 text-cyan-400" />
              <span>Target: {mainExam?.eventName} • {mainExam?.eventDateFormatted}</span>
            </div>
            <h1 className="text-2xl font-black tracking-tight text-white sm:text-3xl">
              <span className="font-mono text-cyan-400">{diffDays}</span> Days Remaining to Score AIR &lt; 100
            </h1>
            <p className="max-w-2xl text-xs sm:text-sm text-slate-300">
              Adaptive preparation engine: Spaced repetition, topic mastery diagnostics, authentic CBT test environment, and instant AI doubt resolution.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              id="dashboard-start-cbt-btn"
              onClick={onStartExam}
              className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 px-5 py-3 text-sm font-bold text-slate-950 shadow-lg shadow-cyan-500/25 transition hover:brightness-110 active:scale-95"
            >
              <Play className="h-4 w-4 fill-slate-950" />
              <span>Take CBT Mock Exam</span>
            </button>
            <button
              id="dashboard-ai-tutor-btn"
              onClick={() => onNavigate("ai_tutor")}
              className="flex items-center gap-2 rounded-xl border border-purple-800/60 bg-purple-950/40 px-4 py-3 text-sm font-bold text-purple-200 transition hover:bg-purple-900/50"
            >
              <Sparkles className="h-4 w-4 text-purple-400" />
              <span>Ask AI Tutor</span>
            </button>
          </div>
        </div>

        {/* Decorative corner glow */}
        <div className="pointer-events-none absolute -right-12 -top-12 h-64 w-64 rounded-full bg-cyan-500/10 blur-3xl" />
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Today's Practice</span>
            <Target className="h-4 w-4 text-cyan-400" />
          </div>
          <div className="text-2xl font-black font-mono text-white">
            {solvedToday} <span className="text-xs text-slate-400 font-normal">/ {targetQuestions}</span>
          </div>
          <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-slate-800">
            <div
              className="h-full bg-cyan-400 rounded-full transition-all duration-500"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>

        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Overall Accuracy</span>
            <TrendingUp className="h-4 w-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black font-mono text-emerald-300">
            {userProfile.totalQuestionsSolved > 0
              ? Math.round((userProfile.totalCorrect / userProfile.totalQuestionsSolved) * 100)
              : 82}%
          </div>
          <p className="mt-1 text-[11px] text-slate-400 font-mono">
            {userProfile.totalCorrect} correct of {userProfile.totalQuestionsSolved || 24} attempts
          </p>
        </div>

        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">SRS Revision Due</span>
            <RotateCcw className="h-4 w-4 text-amber-400" />
          </div>
          <div className="text-2xl font-black font-mono text-amber-300">
            {dueRevisionCount}
          </div>
          <p className="mt-1 text-[11px] text-slate-400">
            SM-2 spaced review questions
          </p>
        </div>

        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold">Study Target</span>
            <Award className="h-4 w-4 text-purple-400" />
          </div>
          <div className="text-2xl font-black font-mono text-purple-300">
            {userProfile.dailyTargetHours}h <span className="text-xs text-slate-400 font-normal">daily</span>
          </div>
          <p className="mt-1 text-[11px] text-slate-400">
            {userProfile.preparationLevel} preparation track
          </p>
        </div>
      </div>

      {/* Two Column Layout: Urgent Topics & High-Yield Subjects */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Urgent Topics Diagnostic Panel (2 cols) */}
        <div className="space-y-4 lg:col-span-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-rose-400" />
              <h2 className="text-base font-bold text-white">
                Topics Needing Immediate Attention
              </h2>
            </div>
            <button
              onClick={() => onNavigate("revision")}
              className="flex items-center gap-1 text-xs font-semibold text-cyan-400 hover:underline"
            >
              <span>View SRS schedule</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>

          <div className="space-y-3">
            {urgentTopics.length > 0 ? (
              urgentTopics.map((m) => {
                const topic = topics.find((t) => t.id === m.topicId);
                const subject = subjects.find((s) => s.id === topic?.subjectId);
                return (
                  <div
                    key={m.topicId}
                    className="flex flex-col justify-between gap-3 rounded-xl border border-rose-900/40 bg-rose-950/10 p-4 transition hover:border-rose-700/60 sm:flex-row sm:items-center"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="rounded bg-slate-800 px-2 py-0.5 text-[10px] font-mono font-bold text-cyan-300">
                          {subject?.code || "CS"}
                        </span>
                        <h3 className="text-sm font-bold text-white">
                          {topic?.name || m.topicId}
                        </h3>
                        {topic?.highYield && (
                          <span className="rounded bg-amber-500/20 px-1.5 py-0.5 text-[10px] font-bold text-amber-300">
                            High Yield
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-rose-300/80">
                        {m.attentionReason || "Low accuracy flagged in recent tests"}
                      </p>
                    </div>

                    <button
                      onClick={() => onStartPractice(subject?.id, topic?.id)}
                      className="flex shrink-0 items-center justify-center gap-1.5 rounded-lg bg-rose-600/90 px-3.5 py-2 text-xs font-bold text-white shadow hover:bg-rose-500 transition"
                    >
                      <Play className="h-3.5 w-3.5 fill-white" />
                      <span>Practice Topic</span>
                    </button>
                  </div>
                );
              })
            ) : (
              <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-6 text-center text-slate-400">
                <p className="text-sm">Great progress! No critical topic gaps flagged today.</p>
                <button
                  onClick={() => onStartPractice()}
                  className="mt-3 inline-flex items-center gap-1.5 text-xs font-bold text-cyan-400 hover:underline"
                >
                  <span>Practice random PYQ set</span>
                  <ArrowRight className="h-3.5 w-3.5" />
                </button>
              </div>
            )}
          </div>

          {/* Quick Launcher Bento */}
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3 pt-2">
            <div
              onClick={() => onNavigate("pyqs")}
              className="group cursor-pointer rounded-xl border border-slate-800 bg-slate-900/60 p-4 transition hover:border-cyan-500/40 hover:bg-slate-850"
            >
              <div className="h-8 w-8 rounded-lg bg-cyan-950 flex items-center justify-center text-cyan-400 mb-2 group-hover:scale-105 transition">
                <BookOpen className="h-4 w-4" />
              </div>
              <h4 className="text-xs font-bold text-white">PYQ Archive</h4>
              <p className="text-[11px] text-slate-400 mt-1">
                Filter 35+ years of verified GATE questions.
              </p>
            </div>

            <div
              onClick={() => onNavigate("formulas")}
              className="group cursor-pointer rounded-xl border border-slate-800 bg-slate-900/60 p-4 transition hover:border-amber-500/40 hover:bg-slate-850"
            >
              <div className="h-8 w-8 rounded-lg bg-amber-950 flex items-center justify-center text-amber-400 mb-2 group-hover:scale-105 transition">
                <TrendingUp className="h-4 w-4" />
              </div>
              <h4 className="text-xs font-bold text-white">Formula Sheet</h4>
              <p className="text-[11px] text-slate-400 mt-1">
                KaTeX rendered formulas & shortcut tricks.
              </p>
            </div>

            <div
              onClick={() => onNavigate("flashcards")}
              className="group cursor-pointer rounded-xl border border-slate-800 bg-slate-900/60 p-4 transition hover:border-purple-500/40 hover:bg-slate-850"
            >
              <div className="h-8 w-8 rounded-lg bg-purple-950 flex items-center justify-center text-purple-400 mb-2 group-hover:scale-105 transition">
                <RotateCcw className="h-4 w-4" />
              </div>
              <h4 className="text-xs font-bold text-white">Smart Flashcards</h4>
              <p className="text-[11px] text-slate-400 mt-1">
                Definitions & algorithm time complexities.
              </p>
            </div>
          </div>
        </div>

        {/* High-Yield Subjects Progress (1 col) */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white">
              Subject Weightage Matrix
            </h2>
            <button
              onClick={() => onNavigate("subjects")}
              className="text-xs font-semibold text-cyan-400 hover:underline"
            >
              All 13
            </button>
          </div>

          <div className="space-y-2.5 rounded-xl border border-slate-800 bg-slate-900/60 p-4">
            {subjects.slice(0, 6).map((sub) => (
              <div
                key={sub.id}
                onClick={() => onNavigate("subject_detail", sub.id)}
                className="group flex cursor-pointer items-center justify-between rounded-lg p-2 transition hover:bg-slate-800/60"
              >
                <div className="space-y-0.5">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-cyan-400">
                      {sub.code}
                    </span>
                    <span className="text-xs font-medium text-slate-200 group-hover:text-white">
                      {sub.name}
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-400">
                    Weightage: ~{sub.weightagePercentage}% of GATE
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono font-bold text-slate-300">
                    {sub.weightagePercentage}M
                  </span>
                  <ChevronRight className="h-4 w-4 text-slate-600 group-hover:text-cyan-400 transition" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
