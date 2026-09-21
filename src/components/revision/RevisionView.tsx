import React from "react";
import {
  RotateCcw,
  CheckCircle2,
  Calendar,
  AlertTriangle,
  Play,
  Award,
  Zap,
} from "lucide-react";
import { RevisionItem, Question, Subject, Topic, NavSection } from "../../types";

interface RevisionViewProps {
  revisionItems: RevisionItem[];
  questions: Question[];
  subjects: Subject[];
  topics: Topic[];
  onMarkMastered: (questionId: string) => void;
  onStartPractice: (subjectId?: string, topicId?: string) => void;
  onNavigate: (section: NavSection, payload?: any) => void;
}

export const RevisionView: React.FC<RevisionViewProps> = ({
  revisionItems,
  questions,
  subjects,
  topics,
  onMarkMastered,
  onStartPractice,
  onNavigate,
}) => {
  const dueItems = revisionItems.filter((r) => r.isOverdue && !r.isMastered);
  const upcomingItems = revisionItems.filter((r) => !r.isOverdue && !r.isMastered);
  const masteredItems = revisionItems.filter((r) => r.isMastered);

  return (
    <div id="revision-view" className="space-y-6 pb-12">
      {/* Banner */}
      <div className="rounded-2xl border border-slate-800 bg-gradient-to-br from-slate-900 via-slate-900 to-cyan-950/30 p-6 shadow-md">
        <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <span className="rounded bg-cyan-500/20 px-2 py-0.5 font-mono text-xs font-bold text-cyan-300">
                SuperMemo SM-2 Engine
              </span>
              <span className="text-xs text-amber-400 font-semibold">
                {dueItems.length} Due for Review Today
              </span>
            </div>
            <h1 className="text-2xl font-black text-white">
              Spaced Repetition Schedule (SRS)
            </h1>
            <p className="max-w-2xl text-xs text-slate-300">
              Adapts review intervals based on past solve time, mistake types, and ease factors to ensure zero memory decay on GATE exam day.
            </p>
          </div>

          <button
            onClick={() => onStartPractice()}
            className="flex items-center gap-2 rounded-xl bg-cyan-500 px-5 py-2.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition shadow"
          >
            <Play className="h-4 w-4 fill-slate-950" />
            <span>Practice Due Set</span>
          </button>
        </div>
      </div>

      {/* Due Today Section */}
      <div className="space-y-3">
        <div className="flex items-center gap-2 text-sm font-bold text-white">
          <AlertTriangle className="h-4 w-4 text-amber-400" />
          <span>Due for Review Today ({dueItems.length})</span>
        </div>

        {dueItems.length > 0 ? (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {dueItems.map((item) => {
              const q = questions.find((ques) => ques.id === item.questionId);
              const sub = subjects.find((s) => s.id === q?.subjectId);
              const top = topics.find((t) => t.id === item.topicId);

              return (
                <div
                  key={item.id}
                  className="flex flex-col justify-between rounded-xl border border-amber-900/40 bg-amber-950/10 p-4 space-y-3 transition hover:border-amber-700/60"
                >
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="rounded bg-slate-800 px-2 py-0.5 font-mono text-[10px] font-bold text-cyan-300">
                        {sub?.code || "CS"} • {q?.year ? `GATE ${q.year}` : q?.id}
                      </span>
                      <span className="text-[10px] font-mono text-amber-400">
                        Interval: {item.intervalDays}d • Reps: {item.repetitionCount}
                      </span>
                    </div>

                    <h4 className="text-xs font-bold text-white line-clamp-2">
                      {q?.questionText || "Question text unavailable"}
                    </h4>
                    <p className="text-[11px] text-slate-400">
                      Topic: {top?.name || item.topicId}
                    </p>
                  </div>

                  <div className="flex items-center justify-between border-t border-slate-800/80 pt-3">
                    <button
                      onClick={() => onMarkMastered(item.questionId)}
                      className="text-[11px] font-semibold text-emerald-400 hover:underline"
                    >
                      Mark Mastered
                    </button>
                    <button
                      onClick={() => onStartPractice(q?.subjectId, q?.topicId)}
                      className="flex items-center gap-1 rounded bg-slate-800 px-3 py-1.5 text-xs font-bold text-cyan-300 hover:bg-slate-700"
                    >
                      <Play className="h-3 w-3 fill-cyan-300" />
                      <span>Review</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-6 text-center text-xs text-slate-400">
            All caught up! No spaced repetition questions are overdue today.
          </div>
        )}
      </div>

      {/* Upcoming & Mastered Breakdown */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        {/* Upcoming */}
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold text-slate-300 uppercase tracking-wider">
            <Calendar className="h-4 w-4 text-cyan-400" />
            <span>Upcoming Scheduled Revisions ({upcomingItems.length})</span>
          </div>

          <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
            {upcomingItems.map((item) => {
              const q = questions.find((ques) => ques.id === item.questionId);
              return (
                <div
                  key={item.id}
                  className="rounded-lg border border-slate-800 bg-slate-900/60 p-3 text-xs flex items-center justify-between"
                >
                  <div className="space-y-0.5">
                    <span className="font-bold text-slate-200 line-clamp-1">
                      {q?.id}: {q?.questionText}
                    </span>
                    <span className="text-[10px] text-slate-400 font-mono">
                      Due: {item.nextReviewDueDate} (Ease: {item.easeFactor})
                    </span>
                  </div>
                  <span className="font-mono text-[11px] text-cyan-400 font-bold shrink-0 ml-2">
                    +{item.intervalDays}d
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Mastered */}
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold text-slate-300 uppercase tracking-wider">
            <Award className="h-4 w-4 text-emerald-400" />
            <span>Mastered Concepts ({masteredItems.length})</span>
          </div>

          <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
            {masteredItems.map((item) => {
              const q = questions.find((ques) => ques.id === item.questionId);
              return (
                <div
                  key={item.id}
                  className="rounded-lg border border-emerald-900/30 bg-emerald-950/10 p-3 text-xs flex items-center justify-between"
                >
                  <div className="space-y-0.5">
                    <span className="font-bold text-emerald-200 line-clamp-1">
                      {q?.id}: {q?.questionText}
                    </span>
                    <span className="text-[10px] text-emerald-400/80 font-mono">
                      4+ consecutive successful recalls
                    </span>
                  </div>
                  <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0 ml-2" />
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
