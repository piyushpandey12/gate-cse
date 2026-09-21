import React, { useState } from "react";
import {
  ArrowLeft,
  BookOpen,
  Play,
  FileSearch,
  Sparkles,
  CheckCircle2,
  ChevronRight,
  TrendingUp,
} from "lucide-react";
import { Subject, Topic, TopicMastery, NavSection } from "../../types";

interface SubjectsViewProps {
  subjects: Subject[];
  topics: Topic[];
  topicMasteries: TopicMastery[];
  selectedSubjectId?: string | null;
  onSelectSubject: (id: string | null) => void;
  onStartPractice: (subjectId?: string, topicId?: string) => void;
  onNavigate: (section: NavSection, payload?: any) => void;
}

export const SubjectsView: React.FC<SubjectsViewProps> = ({
  subjects,
  topics,
  topicMasteries,
  selectedSubjectId,
  onSelectSubject,
  onStartPractice,
  onNavigate,
}) => {
  const [searchQuery, setSearchQuery] = useState("");

  const activeSubject = selectedSubjectId
    ? subjects.find((s) => s.id === selectedSubjectId)
    : null;

  const filteredSubjects = subjects.filter(
    (s) =>
      s.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.description.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // If a subject is selected, render the detailed syllabus breakdown
  if (activeSubject) {
    const subjectTopics = topics.filter((t) => t.subjectId === activeSubject.id);

    return (
      <div id="subject-detail-view" className="space-y-6 pb-12">
        {/* Back navigation */}
        <button
          id="subject-back-btn"
          onClick={() => onSelectSubject(null)}
          className="inline-flex items-center gap-2 rounded-lg border border-slate-800 bg-slate-900/60 px-3 py-1.5 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>Back to All 13 Subjects</span>
        </button>

        {/* Subject Header Banner */}
        <div className="rounded-2xl border border-slate-800 bg-gradient-to-br from-slate-900 via-slate-900 to-slate-950 p-6 shadow-lg">
          <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <span className="rounded bg-cyan-500/20 px-2.5 py-0.5 font-mono text-xs font-bold text-cyan-400 border border-cyan-500/30">
                  {activeSubject.code}
                </span>
                <span className="text-xs font-medium text-amber-400">
                  Estimated Weightage: {activeSubject.weightagePercentage}% (~{Math.round(activeSubject.weightagePercentage)} Marks)
                </span>
              </div>
              <h1 className="text-2xl font-extrabold text-white">
                {activeSubject.name}
              </h1>
              <p className="max-w-2xl text-xs text-slate-300">
                {activeSubject.description}
              </p>
            </div>

            <div className="flex flex-wrap gap-2.5">
              <button
                onClick={() => onStartPractice(activeSubject.id)}
                className="flex items-center gap-1.5 rounded-xl bg-cyan-500 px-4 py-2.5 text-xs font-bold text-slate-950 transition hover:bg-cyan-400 shadow-md"
              >
                <Play className="h-3.5 w-3.5 fill-slate-950" />
                <span>Practice Subject</span>
              </button>
              <button
                onClick={() => onNavigate("pyqs", { subjectId: activeSubject.id })}
                className="flex items-center gap-1.5 rounded-xl border border-slate-700 bg-slate-800 px-4 py-2.5 text-xs font-bold text-slate-200 hover:bg-slate-700 transition"
              >
                <FileSearch className="h-3.5 w-3.5 text-slate-300" />
                <span>Explore PYQs</span>
              </button>
              <button
                onClick={() => onNavigate("ai_tutor", { subject: activeSubject.name })}
                className="flex items-center gap-1.5 rounded-xl border border-purple-800/60 bg-purple-950/40 px-3.5 py-2.5 text-xs font-bold text-purple-300 hover:bg-purple-900/50 transition"
              >
                <Sparkles className="h-3.5 w-3.5 text-purple-400" />
                <span>Ask AI</span>
              </button>
            </div>
          </div>
        </div>

        {/* Topics List */}
        <div className="space-y-3">
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-400">
            Syllabus Topics & Mastery Diagnostics ({subjectTopics.length} Topics)
          </h2>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {subjectTopics.map((topic) => {
              const mastery = topicMasteries.find((m) => m.topicId === topic.id);

              return (
                <div
                  key={topic.id}
                  className="flex flex-col justify-between rounded-xl border border-slate-800 bg-slate-900/70 p-4 transition hover:border-slate-700"
                >
                  <div className="space-y-2">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-sm font-bold text-white">
                            {topic.name}
                          </h3>
                          {topic.highYield && (
                            <span className="rounded bg-amber-500/20 px-1.5 py-0.5 text-[9px] font-bold text-amber-300">
                              High Yield
                            </span>
                          )}
                        </div>
                        <span className="text-[11px] text-slate-400">
                          Chapter: {topic.chapter}
                        </span>
                      </div>

                      <span
                        className={`rounded px-2 py-0.5 text-[10px] font-bold ${
                          mastery?.level === "MASTERED"
                            ? "bg-emerald-500/20 text-emerald-300"
                            : mastery?.level === "STRONG"
                            ? "bg-cyan-500/20 text-cyan-300"
                            : mastery?.level === "WEAK"
                            ? "bg-rose-500/20 text-rose-300"
                            : "bg-slate-800 text-slate-400"
                        }`}
                      >
                        {mastery?.level || "NEW"}
                      </span>
                    </div>

                    <p className="text-xs text-slate-300">
                      {topic.description}
                    </p>
                  </div>

                  <div className="mt-4 flex items-center justify-between border-t border-slate-800/80 pt-3">
                    <div className="text-[11px] text-slate-400">
                      <span>{topic.pyqCount} PYQs available</span>
                      {mastery && mastery.attemptsCount > 0 && (
                        <span className="ml-2 font-mono text-cyan-400">
                          ({mastery.accuracy}% acc)
                        </span>
                      )}
                    </div>

                    <button
                      onClick={() => onStartPractice(activeSubject.id, topic.id)}
                      className="inline-flex items-center gap-1 rounded bg-slate-800 px-2.5 py-1 text-xs font-semibold text-cyan-300 hover:bg-slate-700 hover:text-white transition"
                    >
                      <Play className="h-3 w-3 fill-cyan-300" />
                      <span>Practice</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    );
  }

  // All 13 subjects listing
  return (
    <div id="subjects-view" className="space-y-6 pb-12">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-xl font-black text-white sm:text-2xl">
            Official GATE CSE 2027 Syllabus Matrix
          </h1>
          <p className="text-xs text-slate-400">
            13 Subjects rigorously classified by historical weightage, high-yield chapters, and PYQ frequency.
          </p>
        </div>

        <div className="w-full sm:w-64">
          <input
            id="subject-search-input"
            type="text"
            placeholder="Search subjects..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full rounded-xl border border-slate-800 bg-slate-900 px-3.5 py-2 text-xs text-slate-200 placeholder-slate-500 focus:border-cyan-500 focus:outline-none"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {filteredSubjects.map((sub) => {
          const subTopics = topics.filter((t) => t.subjectId === sub.id);
          const pyqTotal = subTopics.reduce((acc, t) => acc + t.pyqCount, 0);

          return (
            <div
              key={sub.id}
              onClick={() => onSelectSubject(sub.id)}
              className="group cursor-pointer rounded-2xl border border-slate-800 bg-slate-900/60 p-5 transition hover:border-cyan-500/50 hover:bg-slate-850/90 shadow-md"
            >
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2.5">
                  <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-cyan-950/70 border border-cyan-800/40 font-mono text-sm font-bold text-cyan-400">
                    {sub.code}
                  </span>
                  <div>
                    <h3 className="text-sm font-bold text-white group-hover:text-cyan-300 transition">
                      {sub.name}
                    </h3>
                    <span className="text-[11px] text-amber-400 font-semibold">
                      ~{sub.weightagePercentage}% Weightage
                    </span>
                  </div>
                </div>

                <ChevronRight className="h-4 w-4 text-slate-600 group-hover:text-cyan-400 transition" />
              </div>

              <p className="mt-3 line-clamp-2 text-xs text-slate-400">
                {sub.description}
              </p>

              <div className="mt-4 flex items-center justify-between border-t border-slate-800/80 pt-3 text-[11px] text-slate-400">
                <span>{subTopics.length} Chapters/Topics</span>
                <span className="font-mono text-slate-300">{pyqTotal || 150}+ PYQs</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
