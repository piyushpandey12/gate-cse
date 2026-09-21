import React, { useState, useMemo } from "react";
import {
  Search,
  Filter,
  Bookmark,
  ChevronDown,
  ChevronUp,
  Sparkles,
  CheckCircle2,
  XCircle,
  HelpCircle,
  Zap,
  AlertTriangle,
} from "lucide-react";
import { Question, Subject, Topic, QuestionType, Difficulty } from "../../types";
import { evaluateQuestion } from "../../services/evaluator";
import { FormulaMath } from "../common/FormulaMath";

interface PyqExplorerViewProps {
  questions: Question[];
  subjects: Subject[];
  topics: Topic[];
  initialSubjectId?: string | null;
  onToggleBookmark: (id: string) => void;
  onRecordAttempt: (
    question: Question,
    userAnswer: string,
    isCorrect: boolean,
    marks: number
  ) => void;
  onAskAi: (question: Question) => void;
}

export const PyqExplorerView: React.FC<PyqExplorerViewProps> = ({
  questions,
  subjects,
  topics,
  initialSubjectId = null,
  onToggleBookmark,
  onRecordAttempt,
  onAskAi,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>(initialSubjectId || "ALL");
  const [selectedType, setSelectedType] = useState<string>("ALL");
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>("ALL");
  const [selectedYear, setSelectedYear] = useState<string>("ALL");
  const [bookmarkedOnly, setBookmarkedOnly] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  // Track expanded solutions: questionId -> boolean
  const [expandedSolutions, setExpandedSolutions] = useState<Record<string, boolean>>({});
  // Track live user inputs in PYQ explorer: questionId -> string
  const [userInputs, setUserInputs] = useState<Record<string, string>>({});
  // Track evaluation state: questionId -> { isEvaluated, isCorrect, marksAwarded }
  const [evalStates, setEvalStates] = useState<
    Record<string, { isEvaluated: boolean; isCorrect: boolean; marksAwarded: number }>
  >({});

  const filteredQuestions = useMemo(() => {
    return questions.filter((q) => {
      if (selectedSubject !== "ALL" && q.subjectId !== selectedSubject) return false;
      if (selectedType !== "ALL" && q.questionType !== selectedType) return false;
      if (selectedDifficulty !== "ALL" && q.difficulty !== selectedDifficulty) return false;
      if (selectedYear !== "ALL" && q.year !== parseInt(selectedYear)) return false;
      if (bookmarkedOnly && !q.isBookmarked) return false;
      if (
        searchQuery &&
        !q.questionText.toLowerCase().includes(searchQuery.toLowerCase()) &&
        !q.id.toLowerCase().includes(searchQuery.toLowerCase())
      ) {
        return false;
      }
      return true;
    });
  }, [
    questions,
    selectedSubject,
    selectedType,
    selectedDifficulty,
    selectedYear,
    bookmarkedOnly,
    searchQuery,
  ]);

  const years = [2024, 2023, 2022, 2021, 2020];

  const handleSelectOption = (questionId: string, optionKey: string, isMsq: boolean) => {
    const current = userInputs[questionId] || "";
    if (isMsq) {
      const selected = new Set(current.split(",").filter(Boolean));
      if (selected.has(optionKey)) selected.delete(optionKey);
      else selected.add(optionKey);
      const updated = Array.from(selected).sort().join(",");
      setUserInputs((prev) => ({ ...prev, [questionId]: updated }));
    } else {
      setUserInputs((prev) => ({ ...prev, [questionId]: optionKey }));
    }
  };

  const handleCheckAnswer = (q: Question) => {
    const rawAnswer = userInputs[q.id] || "";
    if (!rawAnswer) return;

    const evalResult = evaluateQuestion(q, rawAnswer);
    setEvalStates((prev) => ({
      ...prev,
      [q.id]: {
        isEvaluated: true,
        isCorrect: evalResult.isCorrect,
        marksAwarded: evalResult.marksAwarded,
      },
    }));

    // Reveal solution automatically when checked
    setExpandedSolutions((prev) => ({ ...prev, [q.id]: true }));

    // Record attempt in database
    onRecordAttempt(q, rawAnswer, evalResult.isCorrect, evalResult.marksAwarded);
  };

  const toggleSolution = (qId: string) => {
    setExpandedSolutions((prev) => ({ ...prev, [qId]: !prev[qId] }));
  };

  return (
    <div id="pyq-explorer-view" className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-xl font-black text-white sm:text-2xl">
            GATE CSE Previous Year Questions (PYQs)
          </h1>
          <p className="text-xs text-slate-400">
            Topicwise repository with verified answers, KaTeX mathematical derivations, and traps.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setBookmarkedOnly((prev) => !prev)}
            className={`flex items-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-semibold transition ${
              bookmarkedOnly
                ? "border-amber-500 bg-amber-500/20 text-amber-300"
                : "border-slate-800 bg-slate-900 text-slate-400 hover:text-slate-200"
            }`}
          >
            <Bookmark className={`h-3.5 w-3.5 ${bookmarkedOnly ? "fill-amber-400" : ""}`} />
            <span>Bookmarked Only</span>
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-4 space-y-3 shadow-md">
        <div className="flex flex-col gap-3 md:flex-row md:items-center">
          {/* Search */}
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search problem text, keywords, topics..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 pl-9 pr-3.5 py-2 text-xs text-slate-200 placeholder-slate-500 focus:border-cyan-500 focus:outline-none"
            />
          </div>

          {/* Subject Filter */}
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
          >
            <option value="ALL">All Subjects</option>
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name} ({s.code})
              </option>
            ))}
          </select>

          {/* Type Filter */}
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
          >
            <option value="ALL">All Question Types</option>
            <option value="MCQ">MCQ (Single Choice)</option>
            <option value="MSQ">MSQ (Multiple Choice)</option>
            <option value="NAT">NAT (Numerical)</option>
          </select>

          {/* Difficulty Filter */}
          <select
            value={selectedDifficulty}
            onChange={(e) => setSelectedDifficulty(e.target.value)}
            className="rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
          >
            <option value="ALL">All Difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>

          {/* Year Filter */}
          <select
            value={selectedYear}
            onChange={(e) => setSelectedYear(e.target.value)}
            className="rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
          >
            <option value="ALL">All Years</option>
            {years.map((y) => (
              <option key={y} value={y.toString()}>
                GATE {y}
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center justify-between text-xs text-slate-400 pt-1">
          <span>Showing {filteredQuestions.length} questions</span>
          <button
            onClick={() => {
              setSelectedSubject("ALL");
              setSelectedType("ALL");
              setSelectedDifficulty("ALL");
              setSelectedYear("ALL");
              setBookmarkedOnly(false);
              setSearchQuery("");
            }}
            className="text-cyan-400 hover:underline"
          >
            Reset filters
          </button>
        </div>
      </div>

      {/* Questions Listing */}
      <div className="space-y-4">
        {filteredQuestions.map((q, idx) => {
          const sub = subjects.find((s) => s.id === q.subjectId);
          const top = topics.find((t) => t.id === q.topicId);
          const isExpanded = !!expandedSolutions[q.id];
          const currentAnswer = userInputs[q.id] || "";
          const evaluation = evalStates[q.id];

          return (
            <div
              key={q.id}
              id={`pyq-card-${q.id}`}
              className="rounded-2xl border border-slate-800 bg-slate-900/70 p-5 shadow-sm transition hover:border-slate-700"
            >
              {/* Question Metadata Header */}
              <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800/80 pb-3">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="rounded bg-cyan-950/70 border border-cyan-800/40 px-2 py-0.5 font-mono text-xs font-bold text-cyan-300">
                    GATE {q.year || 2024}
                  </span>
                  <span className="rounded bg-slate-800 px-2 py-0.5 text-xs font-semibold text-slate-300">
                    {sub?.code || q.subjectId}
                  </span>
                  <span className="rounded bg-slate-800/60 px-2 py-0.5 text-[11px] text-slate-400">
                    {top?.name || q.topicId}
                  </span>
                  <span
                    className={`rounded px-1.5 py-0.5 text-[10px] font-bold ${
                      q.questionType === "NAT"
                        ? "bg-amber-500/20 text-amber-300 border border-amber-500/30"
                        : q.questionType === "MSQ"
                        ? "bg-purple-500/20 text-purple-300 border border-purple-500/30"
                        : "bg-blue-500/20 text-blue-300 border border-blue-500/30"
                    }`}
                  >
                    {q.questionType}
                  </span>
                  <span className="text-[11px] font-mono text-slate-400">
                    {q.marks} Mark{q.marks > 1 ? "s" : ""}
                    {q.negativeMarks > 0 && ` (-${q.negativeMarks})`}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => onToggleBookmark(q.id)}
                    className="rounded p-1 text-slate-400 hover:text-amber-400 transition"
                    title="Bookmark question"
                  >
                    <Bookmark
                      className={`h-4 w-4 ${
                        q.isBookmarked ? "fill-amber-400 text-amber-400" : ""
                      }`}
                    />
                  </button>
                  <button
                    onClick={() => onAskAi(q)}
                    className="flex items-center gap-1 rounded-lg border border-purple-800/50 bg-purple-950/30 px-2.5 py-1 text-xs font-semibold text-purple-300 hover:bg-purple-900/40 transition"
                  >
                    <Sparkles className="h-3 w-3 text-purple-400" />
                    <span>Ask AI</span>
                  </button>
                </div>
              </div>

              {/* Problem Statement */}
              <div className="mt-4 text-sm font-medium leading-relaxed text-slate-200">
                <p className="whitespace-pre-line">{q.questionText}</p>
              </div>

              {/* Options or NAT Input */}
              <div className="mt-4 space-y-2">
                {q.questionType === "NAT" ? (
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-400">Your Answer:</span>
                      <input
                        type="text"
                        placeholder="Enter numerical value"
                        value={currentAnswer}
                        onChange={(e) =>
                          setUserInputs((prev) => ({ ...prev, [q.id]: e.target.value }))
                        }
                        className="w-48 rounded-lg border border-slate-700 bg-slate-950 px-3 py-1.5 font-mono text-sm text-cyan-300 placeholder-slate-600 focus:border-cyan-500 focus:outline-none"
                      />
                    </div>
                    <button
                      onClick={() => handleCheckAnswer(q)}
                      className="rounded-lg bg-cyan-500 px-4 py-1.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition"
                    >
                      Check Answer
                    </button>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
                    {[
                      { key: "A", text: q.optionA },
                      { key: "B", text: q.optionB },
                      { key: "C", text: q.optionC },
                      { key: "D", text: q.optionD },
                    ]
                      .filter((o) => !!o.text)
                      .map((opt) => {
                        const isMsq = q.questionType === "MSQ";
                        const selectedList = (currentAnswer || "").split(",");
                        const isSelected = selectedList.includes(opt.key);

                        return (
                          <div
                            key={opt.key}
                            onClick={() => handleSelectOption(q.id, opt.key, isMsq)}
                            className={`flex cursor-pointer items-start gap-2.5 rounded-xl border p-3 text-xs transition ${
                              isSelected
                                ? "border-cyan-500 bg-cyan-500/10 text-cyan-200"
                                : "border-slate-800 bg-slate-950/60 text-slate-300 hover:border-slate-700"
                            }`}
                          >
                            <span
                              className={`flex h-5 w-5 shrink-0 items-center justify-center rounded font-mono text-[11px] font-bold ${
                                isSelected
                                  ? "bg-cyan-500 text-slate-950"
                                  : "bg-slate-800 text-slate-400"
                              }`}
                            >
                              {opt.key}
                            </span>
                            <span className="flex-1 leading-normal">{opt.text}</span>
                          </div>
                        );
                      })}
                  </div>
                )}
              </div>

              {/* Action Buttons: Check Answer & Expand Solution */}
              <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-slate-800/80 pt-3">
                <div className="flex items-center gap-2">
                  {q.questionType !== "NAT" && (
                    <button
                      onClick={() => handleCheckAnswer(q)}
                      disabled={!currentAnswer}
                      className="rounded-lg bg-cyan-500/90 px-3 py-1.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 disabled:opacity-40 transition"
                    >
                      Verify Selection
                    </button>
                  )}

                  {evaluation && (
                    <div className="flex items-center gap-1.5 text-xs font-bold font-mono">
                      {evaluation.isCorrect ? (
                        <span className="flex items-center gap-1 text-emerald-400">
                          <CheckCircle2 className="h-4 w-4" />
                          Correct (+{evaluation.marksAwarded}M)
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-rose-400">
                          <XCircle className="h-4 w-4" />
                          Incorrect ({evaluation.marksAwarded}M)
                        </span>
                      )}
                    </div>
                  )}
                </div>

                <button
                  onClick={() => toggleSolution(q.id)}
                  className="flex items-center gap-1 text-xs font-semibold text-slate-400 hover:text-cyan-400 transition"
                >
                  <span>{isExpanded ? "Hide Detailed Solution" : "View Verified Solution"}</span>
                  {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                </button>
              </div>

              {/* Expandable Solution Drawer */}
              {isExpanded && (
                <div className="mt-4 rounded-xl border border-slate-800 bg-slate-950/80 p-4 space-y-3.5 text-xs text-slate-300">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                    <span className="font-bold text-white uppercase tracking-wider text-[11px]">
                      Verified GATE Solution
                    </span>
                    <span className="rounded bg-emerald-500/20 px-2 py-0.5 font-mono font-bold text-emerald-300">
                      Official Key: {q.correctAnswers}
                    </span>
                  </div>

                  {/* Detailed Derivation */}
                  <div className="space-y-1.5 whitespace-pre-line leading-relaxed text-slate-200">
                    <p>{q.detailedSolution}</p>
                  </div>

                  {/* Formula */}
                  {q.keyFormula && (
                    <div className="rounded-lg border border-cyan-900/40 bg-cyan-950/20 p-3 space-y-1">
                      <div className="flex items-center gap-1.5 font-semibold text-cyan-300 text-[11px]">
                        <Zap className="h-3.5 w-3.5" />
                        <span>Core Formula / Mathematical Property:</span>
                      </div>
                      <div className="font-mono text-cyan-100 text-xs pl-5">
                        <FormulaMath math={q.keyFormula} />
                      </div>
                    </div>
                  )}

                  {/* Shortcut Trick */}
                  {q.shortcutTrick && (
                    <div className="rounded-lg border border-amber-900/40 bg-amber-950/20 p-3 space-y-1">
                      <div className="flex items-center gap-1.5 font-semibold text-amber-300 text-[11px]">
                        <Zap className="h-3.5 w-3.5" />
                        <span>Exam Speed Trick:</span>
                      </div>
                      <p className="text-amber-200/90 pl-5">{q.shortcutTrick}</p>
                    </div>
                  )}

                  {/* Common Trap */}
                  {q.commonTrap && (
                    <div className="rounded-lg border border-rose-900/40 bg-rose-950/20 p-3 space-y-1">
                      <div className="flex items-center gap-1.5 font-semibold text-rose-300 text-[11px]">
                        <AlertTriangle className="h-3.5 w-3.5" />
                        <span>Common Negative Marking Trap:</span>
                      </div>
                      <p className="text-rose-200/90 pl-5">{q.commonTrap}</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
