import React, { useState, useEffect } from "react";
import {
  Play,
  RotateCcw,
  Clock,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  ArrowRight,
  Sparkles,
  Zap,
} from "lucide-react";
import {
  Question,
  Subject,
  Topic,
  MistakeCategory,
  NavSection,
} from "../../types";
import { evaluateQuestion } from "../../services/evaluator";
import { FormulaMath } from "../common/FormulaMath";

interface PracticeViewProps {
  questions: Question[];
  subjects: Subject[];
  topics: Topic[];
  initialSubjectId?: string | null;
  initialTopicId?: string | null;
  onRecordAttempt: (
    question: Question,
    userAnswer: string,
    isCorrect: boolean,
    marks: number,
    timeSpentSeconds: number,
    mistakeCategory?: MistakeCategory | null,
    userNote?: string | null
  ) => void;
  onNavigate: (section: NavSection, payload?: any) => void;
}

export const PracticeView: React.FC<PracticeViewProps> = ({
  questions,
  subjects,
  topics,
  initialSubjectId = null,
  initialTopicId = null,
  onRecordAttempt,
  onNavigate,
}) => {
  // Session setup states
  const [selectedSubjectId, setSelectedSubjectId] = useState<string>(
    initialSubjectId || "ALL"
  );
  const [selectedTopicId, setSelectedTopicId] = useState<string>(
    initialTopicId || "ALL"
  );
  const [questionCount, setQuestionCount] = useState<number>(5);

  // Active session states
  const [isSessionActive, setIsSessionActive] = useState<boolean>(false);
  const [sessionQuestions, setSessionQuestions] = useState<Question[]>([]);
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [currentAnswer, setCurrentAnswer] = useState<string>("");
  const [isEvaluated, setIsEvaluated] = useState<boolean>(false);
  const [lastEval, setLastEval] = useState<{
    isCorrect: boolean;
    marksAwarded: number;
    normalizedAnswer: string;
  } | null>(null);

  // Per-question timer
  const [timeSpent, setTimeSpent] = useState<number>(0);

  // Mistake recording
  const [mistakeCategory, setMistakeCategory] = useState<MistakeCategory>("CONCEPTUAL");
  const [userNote, setUserNote] = useState<string>("");

  // Running scores
  const [sessionScore, setSessionScore] = useState<number>(0);
  const [correctCount, setCorrectCount] = useState<number>(0);
  const [incorrectCount, setIncorrectCount] = useState<number>(0);
  const [isFinished, setIsFinished] = useState<boolean>(false);

  // Timer effect
  useEffect(() => {
    let interval: any = null;
    if (isSessionActive && !isEvaluated && !isFinished) {
      interval = setInterval(() => {
        setTimeSpent((t) => t + 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [isSessionActive, isEvaluated, isFinished]);

  const handleStartSession = () => {
    let pool = [...questions];
    if (selectedSubjectId !== "ALL") {
      pool = pool.filter((q) => q.subjectId === selectedSubjectId);
    }
    if (selectedTopicId !== "ALL") {
      pool = pool.filter((q) => q.topicId === selectedTopicId);
    }

    if (pool.length === 0) {
      pool = [...questions]; // Fallback if filtered pool empty
    }

    // Shuffle and pick
    const selected = pool.sort(() => 0.5 - Math.random()).slice(0, questionCount);

    setSessionQuestions(selected);
    setCurrentIndex(0);
    setCurrentAnswer("");
    setIsEvaluated(false);
    setLastEval(null);
    setTimeSpent(0);
    setSessionScore(0);
    setCorrectCount(0);
    setIncorrectCount(0);
    setIsFinished(false);
    setIsSessionActive(true);
  };

  const currentQ = sessionQuestions[currentIndex];

  const handleSelectOption = (optKey: string) => {
    if (isEvaluated) return;
    if (currentQ?.questionType === "MSQ") {
      const selected = new Set(currentAnswer.split(",").filter(Boolean));
      if (selected.has(optKey)) selected.delete(optKey);
      else selected.add(optKey);
      setCurrentAnswer(Array.from(selected).sort().join(","));
    } else {
      setCurrentAnswer(optKey);
    }
  };

  const handleEvaluate = () => {
    if (!currentQ || !currentAnswer.trim()) return;

    const result = evaluateQuestion(currentQ, currentAnswer);
    setLastEval({
      isCorrect: result.isCorrect,
      marksAwarded: result.marksAwarded,
      normalizedAnswer: result.normalizedUserAnswer,
    });
    setIsEvaluated(true);

    if (result.isCorrect) {
      setSessionScore((s) => s + result.marksAwarded);
      setCorrectCount((c) => c + 1);
      onRecordAttempt(
        currentQ,
        result.normalizedUserAnswer,
        true,
        result.marksAwarded,
        timeSpent,
        null,
        null
      );
    } else {
      setSessionScore((s) => s + result.marksAwarded);
      setIncorrectCount((i) => i + 1);
    }
  };

  const handleSaveMistakeAndNext = () => {
    if (currentQ && lastEval && !lastEval.isCorrect) {
      onRecordAttempt(
        currentQ,
        lastEval.normalizedAnswer,
        false,
        lastEval.marksAwarded,
        timeSpent,
        mistakeCategory,
        userNote.trim() || null
      );
    }

    // Move to next question or finish
    if (currentIndex + 1 < sessionQuestions.length) {
      setCurrentIndex((idx) => idx + 1);
      setCurrentAnswer("");
      setIsEvaluated(false);
      setLastEval(null);
      setTimeSpent(0);
      setUserNote("");
      setMistakeCategory("CONCEPTUAL");
    } else {
      setIsFinished(true);
    }
  };

  // If session finished, show summary
  if (isFinished) {
    const accuracy =
      sessionQuestions.length > 0
        ? Math.round((correctCount / sessionQuestions.length) * 100)
        : 0;

    return (
      <div id="practice-finished-card" className="max-w-xl mx-auto space-y-6 pb-12 pt-6">
        <div className="rounded-2xl border border-slate-800 bg-slate-900/90 p-6 text-center shadow-xl space-y-4">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-cyan-500/20 text-cyan-400">
            <CheckCircle2 className="h-8 w-8" />
          </div>

          <h2 className="text-2xl font-black text-white">
            Practice Session Completed!
          </h2>
          <p className="text-xs text-slate-400">
            Every question has been evaluated according to official GATE marking scheme and logged into your SM-2 revision schedule.
          </p>

          <div className="grid grid-cols-3 gap-3 border-y border-slate-800 py-4">
            <div>
              <span className="text-[10px] font-bold text-slate-400 uppercase">Score</span>
              <div className="font-mono text-2xl font-black text-cyan-300">
                {sessionScore.toFixed(2)}
              </div>
            </div>
            <div>
              <span className="text-[10px] font-bold text-slate-400 uppercase">Accuracy</span>
              <div className="font-mono text-2xl font-black text-emerald-400">
                {accuracy}%
              </div>
            </div>
            <div>
              <span className="text-[10px] font-bold text-slate-400 uppercase">Result</span>
              <div className="font-mono text-2xl font-black text-slate-200">
                {correctCount} / {sessionQuestions.length}
              </div>
            </div>
          </div>

          <div className="flex justify-center gap-3 pt-2">
            <button
              onClick={handleStartSession}
              className="flex items-center gap-2 rounded-xl bg-cyan-500 px-5 py-2.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition"
            >
              <RotateCcw className="h-4 w-4" />
              <span>Practice Another Set</span>
            </button>
            <button
              onClick={() => setIsSessionActive(false)}
              className="rounded-xl border border-slate-700 bg-slate-800 px-4 py-2.5 text-xs font-bold text-slate-200 hover:bg-slate-700 transition"
            >
              Change Topic
            </button>
          </div>
        </div>
      </div>
    );
  }

  // If session not active, show setup configuration
  if (!isSessionActive) {
    const availableTopics =
      selectedSubjectId === "ALL"
        ? topics
        : topics.filter((t) => t.subjectId === selectedSubjectId);

    return (
      <div id="practice-setup-card" className="max-w-2xl mx-auto space-y-6 pb-12">
        <div>
          <h1 className="text-xl font-black text-white sm:text-2xl">
            Custom Topicwise Practice Arena
          </h1>
          <p className="text-xs text-slate-400">
            Targeted drills with real-time feedback, negative marking simulation, and SuperMemo SM-2 error categorization.
          </p>
        </div>

        <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 space-y-5 shadow-lg">
          {/* Select Subject */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">
              Select Subject:
            </label>
            <select
              value={selectedSubjectId}
              onChange={(e) => {
                setSelectedSubjectId(e.target.value);
                setSelectedTopicId("ALL");
              }}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 p-3 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
            >
              <option value="ALL">All 13 GATE Subjects (Randomized)</option>
              {subjects.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.code} - ~{s.weightagePercentage}%)
                </option>
              ))}
            </select>
          </div>

          {/* Select Topic */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">
              Select Specific Topic:
            </label>
            <select
              value={selectedTopicId}
              onChange={(e) => setSelectedTopicId(e.target.value)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 p-3 text-xs text-slate-200 focus:border-cyan-500 focus:outline-none"
            >
              <option value="ALL">All Topics in Selected Subject</option>
              {availableTopics.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name} {t.highYield ? "★ (High Yield)" : ""}
                </option>
              ))}
            </select>
          </div>

          {/* Question Count */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">
              Question Count:
            </label>
            <div className="grid grid-cols-4 gap-2">
              {[5, 10, 15, 20].map((count) => (
                <button
                  key={count}
                  type="button"
                  onClick={() => setQuestionCount(count)}
                  className={`rounded-xl border py-2.5 text-xs font-bold transition ${
                    questionCount === count
                      ? "border-cyan-500 bg-cyan-500/20 text-cyan-300"
                      : "border-slate-800 bg-slate-950 text-slate-400 hover:text-white"
                  }`}
                >
                  {count} Questions
                </button>
              ))}
            </div>
          </div>

          <button
            id="start-practice-btn"
            onClick={handleStartSession}
            className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 py-3.5 text-sm font-bold text-slate-950 shadow-lg shadow-cyan-500/20 hover:brightness-110 active:scale-98 transition"
          >
            <Play className="h-4 w-4 fill-slate-950" />
            <span>Launch Practice Drills</span>
          </button>
        </div>
      </div>
    );
  }

  // Active Practice Question View
  return (
    <div id="active-practice-view" className="max-w-3xl mx-auto space-y-6 pb-12">
      {/* Session Progress Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center gap-3">
          <span className="font-mono text-sm font-bold text-cyan-400">
            Q {currentIndex + 1} of {sessionQuestions.length}
          </span>
          <span className="rounded bg-slate-800 px-2 py-0.5 font-mono text-xs text-slate-300">
            {currentQ.questionType} • {currentQ.marks}M
          </span>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1.5 text-xs font-mono text-slate-400">
            <Clock className="h-3.5 w-3.5 text-cyan-400" />
            <span>{timeSpent}s</span>
          </div>

          <button
            onClick={() => setIsSessionActive(false)}
            className="text-xs font-semibold text-rose-400 hover:underline"
          >
            End Session
          </button>
        </div>
      </div>

      {/* Question Card */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 space-y-5 shadow-md">
        <div className="text-sm font-medium leading-relaxed text-slate-100 whitespace-pre-line">
          {currentQ.questionText}
        </div>

        {/* Input / Options */}
        {currentQ.questionType === "NAT" ? (
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-400">
              Numerical Answer (Range or Float):
            </label>
            <input
              type="text"
              disabled={isEvaluated}
              placeholder="e.g. 17 or 2.67"
              value={currentAnswer}
              onChange={(e) => setCurrentAnswer(e.target.value)}
              className="w-60 rounded-xl border border-slate-700 bg-slate-950 px-3.5 py-2 font-mono text-sm text-cyan-300 placeholder-slate-600 focus:border-cyan-500 focus:outline-none"
            />
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
            {[
              { key: "A", text: currentQ.optionA },
              { key: "B", text: currentQ.optionB },
              { key: "C", text: currentQ.optionC },
              { key: "D", text: currentQ.optionD },
            ]
              .filter((o) => !!o.text)
              .map((opt) => {
                const isSelected = (currentAnswer || "")
                  .split(",")
                  .includes(opt.key);

                return (
                  <div
                    key={opt.key}
                    onClick={() => handleSelectOption(opt.key)}
                    className={`flex cursor-pointer items-start gap-3 rounded-xl border p-3.5 text-xs transition ${
                      isSelected
                        ? "border-cyan-500 bg-cyan-500/15 text-cyan-100"
                        : "border-slate-800 bg-slate-950/60 text-slate-300 hover:border-slate-700"
                    } ${isEvaluated ? "pointer-events-none" : ""}`}
                  >
                    <span
                      className={`flex h-5 w-5 shrink-0 items-center justify-center rounded font-mono text-xs font-bold ${
                        isSelected
                          ? "bg-cyan-500 text-slate-950"
                          : "bg-slate-800 text-slate-400"
                      }`}
                    >
                      {opt.key}
                    </span>
                    <span className="flex-1 leading-relaxed">{opt.text}</span>
                  </div>
                );
              })}
          </div>
        )}

        {/* Evaluation Control */}
        {!isEvaluated ? (
          <div className="border-t border-slate-800/80 pt-4 flex justify-end">
            <button
              onClick={handleEvaluate}
              disabled={!currentAnswer.trim()}
              className="flex items-center gap-2 rounded-xl bg-cyan-500 px-6 py-2.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 disabled:opacity-40 transition"
            >
              <span>Submit & Evaluate</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>
        ) : (
          /* Post-Evaluation & Feedback */
          <div className="rounded-xl border border-slate-800 bg-slate-950 p-5 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div className="flex items-center gap-2">
                {lastEval?.isCorrect ? (
                  <span className="flex items-center gap-1.5 font-bold text-emerald-400">
                    <CheckCircle2 className="h-5 w-5" />
                    Correct! (+{lastEval.marksAwarded} Marks)
                  </span>
                ) : (
                  <span className="flex items-center gap-1.5 font-bold text-rose-400">
                    <XCircle className="h-5 w-5" />
                    Incorrect ({lastEval?.marksAwarded} Marks)
                  </span>
                )}
              </div>
              <span className="font-mono text-xs font-bold text-cyan-300">
                Official Key: {currentQ.correctAnswers}
              </span>
            </div>

            {/* Explanation */}
            <div className="text-xs text-slate-300 whitespace-pre-line leading-relaxed">
              <p className="font-bold text-white mb-1">Detailed Derivation:</p>
              {currentQ.detailedSolution}
            </div>

            {/* Formula */}
            {currentQ.keyFormula && (
              <div className="rounded-lg border border-cyan-900/40 bg-cyan-950/20 p-2.5 text-xs">
                <span className="font-bold text-cyan-300">Key Formula: </span>
                <FormulaMath math={currentQ.keyFormula} />
              </div>
            )}

            {/* If incorrect: Mistake Notebook Categorization */}
            {!lastEval?.isCorrect && (
              <div className="rounded-xl border border-amber-900/40 bg-amber-950/20 p-4 space-y-3">
                <div className="flex items-center gap-2 text-xs font-bold text-amber-300">
                  <AlertTriangle className="h-4 w-4" />
                  <span>Log Error in Mistake Notebook (SM-2 Diagnostic):</span>
                </div>

                <div className="grid grid-cols-2 gap-2 sm:grid-cols-4 text-[11px]">
                  {[
                    { id: "CONCEPTUAL", label: "Conceptual Gap" },
                    { id: "CALCULATION", label: "Calculation Error" },
                    { id: "SILLY_MISTAKE", label: "Silly Slip" },
                    { id: "TIME_PRESSURE", label: "Time Pressure" },
                    { id: "MISREAD", label: "Misread Question" },
                    { id: "FORMULA", label: "Forgot Formula" },
                    { id: "MEMORY", label: "Memory Lapse" },
                    { id: "GUESS", label: "Blind Guess" },
                  ].map((cat) => (
                    <button
                      key={cat.id}
                      type="button"
                      onClick={() => setMistakeCategory(cat.id as MistakeCategory)}
                      className={`rounded-lg border p-2 text-center transition ${
                        mistakeCategory === cat.id
                          ? "border-amber-400 bg-amber-400/20 text-amber-200 font-bold"
                          : "border-slate-800 bg-slate-900 text-slate-400 hover:text-slate-200"
                      }`}
                    >
                      {cat.label}
                    </button>
                  ))}
                </div>

                <input
                  type="text"
                  placeholder="Personal reflection: Why did I pick this? (Optional)"
                  value={userNote}
                  onChange={(e) => setUserNote(e.target.value)}
                  className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-xs text-slate-200 placeholder-slate-500 focus:border-amber-400 focus:outline-none"
                />
              </div>
            )}

            {/* Next Question Button */}
            <div className="flex justify-end pt-2">
              <button
                onClick={handleSaveMistakeAndNext}
                className="flex items-center gap-2 rounded-xl bg-cyan-500 px-6 py-2.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition shadow"
              >
                <span>
                  {currentIndex + 1 < sessionQuestions.length
                    ? "Next Question"
                    : "Finish Practice Session"}
                </span>
                <ArrowRight className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
