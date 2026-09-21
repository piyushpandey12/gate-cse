import React, { useState } from "react";
import {
  AlertTriangle,
  Filter,
  Play,
  Edit2,
  Check,
  RotateCcw,
} from "lucide-react";
import { Attempt, Question, Subject, Topic, MistakeCategory } from "../../types";

interface MistakesViewProps {
  attempts: Attempt[];
  questions: Question[];
  subjects: Subject[];
  topics: Topic[];
  onUpdateMistake: (attemptId: number, category: MistakeCategory, note: string) => void;
  onStartPractice: (subjectId?: string, topicId?: string) => void;
}

export const MistakesView: React.FC<MistakesViewProps> = ({
  attempts,
  questions,
  subjects,
  topics,
  onUpdateMistake,
  onStartPractice,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>("ALL");
  const [selectedCategory, setSelectedCategory] = useState<string>("ALL");
  const [editingAttemptId, setEditingAttemptId] = useState<number | null>(null);
  const [editCategory, setEditCategory] = useState<MistakeCategory>("CONCEPTUAL");
  const [editNote, setEditNote] = useState<string>("");

  const incorrectAttempts = attempts.filter((a) => !a.isCorrect || a.mistakeCategory);

  const filtered = incorrectAttempts.filter((a) => {
    const q = questions.find((ques) => ques.id === a.questionId);
    if (selectedSubject !== "ALL" && q?.subjectId !== selectedSubject) return false;
    if (selectedCategory !== "ALL" && a.mistakeCategory !== selectedCategory) return false;
    return true;
  });

  const handleStartEdit = (a: Attempt) => {
    setEditingAttemptId(a.id);
    setEditCategory(a.mistakeCategory || "CONCEPTUAL");
    setEditNote(a.userNote || "");
  };

  const handleSaveEdit = (attemptId: number) => {
    onUpdateMistake(attemptId, editCategory, editNote);
    setEditingAttemptId(null);
  };

  return (
    <div id="mistakes-view" className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h1 className="text-xl font-black text-white sm:text-2xl">
          Mistake Notebook & Error Diagnostics
        </h1>
        <p className="text-xs text-slate-400">
          Analyze why errors occurred, categorize root causes (calculation vs conceptual), and re-attempt problem items.
        </p>
      </div>

      {/* Filter Row */}
      <div className="flex flex-wrap items-center gap-3 rounded-xl border border-slate-800 bg-slate-900/80 p-4">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-slate-400" />
          <span className="text-xs font-bold text-slate-300">Filter Errors:</span>
        </div>

        <select
          value={selectedSubject}
          onChange={(e) => setSelectedSubject(e.target.value)}
          className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-1.5 text-xs text-slate-200 focus:outline-none"
        >
          <option value="ALL">All Subjects</option>
          {subjects.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name} ({s.code})
            </option>
          ))}
        </select>

        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-1.5 text-xs text-slate-200 focus:outline-none"
        >
          <option value="ALL">All Mistake Types</option>
          <option value="CONCEPTUAL">Conceptual</option>
          <option value="CALCULATION">Calculation</option>
          <option value="SILLY_MISTAKE">Silly Mistake</option>
          <option value="TIME_PRESSURE">Time Pressure</option>
          <option value="MISREAD">Misread</option>
          <option value="FORMULA">Forgot Formula</option>
          <option value="MEMORY">Memory Lapse</option>
          <option value="GUESS">Guess</option>
        </select>

        <span className="ml-auto text-xs text-slate-400">
          Showing {filtered.length} logged errors
        </span>
      </div>

      {/* Mistakes List */}
      <div className="space-y-3">
        {filtered.length > 0 ? (
          filtered.map((att) => {
            const q = questions.find((ques) => ques.id === att.questionId);
            const sub = subjects.find((s) => s.id === q?.subjectId);
            const top = topics.find((t) => t.id === q?.topicId);
            const isEditing = editingAttemptId === att.id;

            return (
              <div
                key={att.id}
                className="rounded-xl border border-slate-800 bg-slate-900/70 p-4 space-y-3 transition hover:border-slate-700"
              >
                <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800/80 pb-2.5">
                  <div className="flex items-center gap-2">
                    <span className="rounded bg-rose-500/20 px-2 py-0.5 font-mono text-[10px] font-bold text-rose-300 border border-rose-500/30">
                      {att.mistakeCategory || "UNCLASSIFIED"}
                    </span>
                    <span className="rounded bg-slate-800 px-2 py-0.5 font-mono text-xs text-slate-300">
                      {sub?.code || "CS"} • {q?.year ? `GATE ${q.year}` : q?.id}
                    </span>
                    <span className="text-[11px] text-slate-400">
                      {top?.name || q?.topicId}
                    </span>
                  </div>

                  <div className="flex items-center gap-2 text-xs">
                    <button
                      onClick={() => handleStartEdit(att)}
                      className="text-slate-400 hover:text-cyan-300 transition"
                      title="Edit Category or Note"
                    >
                      <Edit2 className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => onStartPractice(q?.subjectId, q?.topicId)}
                      className="flex items-center gap-1 rounded bg-slate-800 px-2.5 py-1 text-xs font-semibold text-cyan-300 hover:bg-slate-700 transition"
                    >
                      <RotateCcw className="h-3 w-3" />
                      <span>Retry</span>
                    </button>
                  </div>
                </div>

                <p className="text-xs font-medium text-slate-200 line-clamp-2">
                  {q?.questionText || "Question statement"}
                </p>

                <div className="flex flex-wrap items-center gap-4 text-[11px] font-mono">
                  <span className="text-rose-400">
                    Your Answer: <strong>{att.selectedAnswer}</strong>
                  </span>
                  <span className="text-emerald-400">
                    Expected: <strong>{q?.correctAnswers}</strong>
                  </span>
                  <span className="text-slate-500">
                    Time Spent: {att.timeSpentSeconds}s
                  </span>
                </div>

                {/* Reflection Note */}
                {isEditing ? (
                  <div className="rounded-lg border border-slate-700 bg-slate-950 p-3 space-y-2">
                    <div className="flex items-center gap-2">
                      <select
                        value={editCategory}
                        onChange={(e) => setEditCategory(e.target.value as MistakeCategory)}
                        className="rounded border border-slate-700 bg-slate-900 px-2 py-1 text-xs text-slate-200"
                      >
                        <option value="CONCEPTUAL">Conceptual Gap</option>
                        <option value="CALCULATION">Calculation Error</option>
                        <option value="SILLY_MISTAKE">Silly Mistake</option>
                        <option value="TIME_PRESSURE">Time Pressure</option>
                        <option value="MISREAD">Misread Question</option>
                        <option value="FORMULA">Forgot Formula</option>
                        <option value="MEMORY">Memory Lapse</option>
                        <option value="GUESS">Blind Guess</option>
                      </select>
                      <button
                        onClick={() => handleSaveEdit(att.id)}
                        className="flex items-center gap-1 rounded bg-cyan-500 px-3 py-1 text-xs font-bold text-slate-950 hover:bg-cyan-400"
                      >
                        <Check className="h-3 w-3" />
                        <span>Save</span>
                      </button>
                    </div>
                    <input
                      type="text"
                      placeholder="Add personal note about this mistake..."
                      value={editNote}
                      onChange={(e) => setEditNote(e.target.value)}
                      className="w-full rounded border border-slate-700 bg-slate-900 px-2.5 py-1 text-xs text-slate-200 focus:outline-none"
                    />
                  </div>
                ) : att.userNote ? (
                  <div className="rounded-lg bg-slate-950/60 p-2.5 text-xs text-amber-200/90 border border-slate-800">
                    <span className="font-semibold text-slate-400">Note: </span>
                    {att.userNote}
                  </div>
                ) : null}
              </div>
            );
          })
        ) : (
          <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-8 text-center text-xs text-slate-400">
            No mistake records found matching this filter.
          </div>
        )}
      </div>
    </div>
  );
};
