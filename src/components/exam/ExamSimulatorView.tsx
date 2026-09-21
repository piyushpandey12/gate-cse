import React, { useState, useEffect } from "react";
import {
  Clock,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  RotateCcw,
  ArrowRight,
  ArrowLeft,
  Calculator,
  Award,
} from "lucide-react";
import confetti from "canvas-confetti";
import { Question, TestSession, Subject } from "../../types";
import { evaluateQuestion } from "../../services/evaluator";

interface ExamSimulatorViewProps {
  allQuestions: Question[];
  subjects: Subject[];
  onSaveTestSession: (session: TestSession) => void;
  onToggleCalc: () => void;
}

export const ExamSimulatorView: React.FC<ExamSimulatorViewProps> = ({
  allQuestions,
  subjects,
  onSaveTestSession,
  onToggleCalc,
}) => {
  // Test configuration
  const [testType, setTestType] = useState<"FULL_MOCK" | "SECTIONAL">("SECTIONAL");
  const [isTestActive, setIsTestActive] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  // Active exam state
  const [questions, setQuestions] = useState<Question[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [userAnswers, setUserAnswers] = useState<Record<string, string>>({});
  const [markedForReview, setMarkedForReview] = useState<Set<string>>(new Set());
  const [visitedQuestions, setVisitedQuestions] = useState<Set<string>>(new Set());
  const [remainingSeconds, setRemainingSeconds] = useState(30 * 60);
  const [showSubmitModal, setShowSubmitModal] = useState(false);

  // Test Results
  const [resultSession, setResultSession] = useState<TestSession | null>(null);

  // Exam Countdown Timer
  useEffect(() => {
    let timer: any = null;
    if (isTestActive && !isSubmitted) {
      timer = setInterval(() => {
        setRemainingSeconds((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            handleSubmitTest();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [isTestActive, isSubmitted]);

  const handleStartExam = (type: "FULL_MOCK" | "SECTIONAL") => {
    setTestType(type);
    const count = type === "FULL_MOCK" ? Math.min(65, allQuestions.length) : Math.min(15, allQuestions.length);
    const duration = type === "FULL_MOCK" ? 180 * 60 : 30 * 60;

    // Pick randomized set
    const selected = [...allQuestions].sort(() => 0.5 - Math.random()).slice(0, count);

    setQuestions(selected);
    setCurrentIndex(0);
    setUserAnswers({});
    setMarkedForReview(new Set());
    setVisitedQuestions(new Set([selected[0]?.id]));
    setRemainingSeconds(duration);
    setIsTestActive(true);
    setIsSubmitted(false);
    setResultSession(null);
    setShowSubmitModal(false);
  };

  const currentQ = questions[currentIndex];

  const handleSelectAnswer = (ans: string) => {
    if (!currentQ) return;
    setUserAnswers((prev) => ({ ...prev, [currentQ.id]: ans }));
  };

  const handleClearResponse = () => {
    if (!currentQ) return;
    setUserAnswers((prev) => {
      const next = { ...prev };
      delete next[currentQ.id];
      return next;
    });
  };

  const handleToggleReview = () => {
    if (!currentQ) return;
    setMarkedForReview((prev) => {
      const next = new Set(prev);
      if (next.has(currentQ.id)) next.delete(currentQ.id);
      else next.add(currentQ.id);
      return next;
    });
  };

  const handleNavigateQuestion = (index: number) => {
    if (index >= 0 && index < questions.length) {
      setCurrentIndex(index);
      setVisitedQuestions((prev) => new Set(prev).add(questions[index].id));
    }
  };

  const handleSaveAndNext = () => {
    if (currentIndex + 1 < questions.length) {
      handleNavigateQuestion(currentIndex + 1);
    }
  };

  const handleSubmitTest = () => {
    setShowSubmitModal(false);
    setIsSubmitted(true);

    let totalScore = 0;
    let correctCount = 0;
    let attemptedCount = 0;

    questions.forEach((q) => {
      const ans = userAnswers[q.id];
      if (ans && ans.trim()) {
        attemptedCount += 1;
        const res = evaluateQuestion(q, ans);
        totalScore += res.marksAwarded;
        if (res.isCorrect) correctCount += 1;
      }
    });

    const accuracy = attemptedCount > 0 ? (correctCount / attemptedCount) * 100 : 0;
    const session: TestSession = {
      id: "TEST_" + Date.now(),
      title: testType === "FULL_MOCK" ? "GATE CSE All-India Grand Mock Test" : "Sectional Mini Mock Test",
      testType,
      totalQuestions: questions.length,
      durationMinutes: testType === "FULL_MOCK" ? 180 : 30,
      questionIds: questions.map((q) => q.id),
      userAnswers,
      markedForReview: Array.from(markedForReview),
      startedAt: Date.now() - (testType === "FULL_MOCK" ? 180 * 60 - remainingSeconds : 30 * 60 - remainingSeconds) * 1000,
      submittedAt: Date.now(),
      totalScore: Math.round(totalScore * 100) / 100,
      accuracy: Math.round(accuracy * 10) / 10,
      isCompleted: true,
    };

    setResultSession(session);
    onSaveTestSession(session);

    try {
      confetti({ particleCount: 75, spread: 70, origin: { y: 0.6 } });
    } catch {
      // safe fallback
    }
  };

  const formatTime = (secs: number) => {
    const hrs = Math.floor(secs / 3600);
    const mins = Math.floor((secs % 3600) / 60);
    const s = secs % 60;
    if (hrs > 0) {
      return `${hrs.toString().padStart(2, "0")}:${mins.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
    }
    return `${mins.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
  };

  // 1. Result Screen
  if (isSubmitted && resultSession) {
    return (
      <div id="exam-results-screen" className="max-w-4xl mx-auto space-y-6 pb-12">
        <div className="rounded-2xl border border-slate-800 bg-slate-900/90 p-6 shadow-xl space-y-6">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 border-b border-slate-800 pb-5">
            <div>
              <span className="rounded bg-cyan-500/20 px-2.5 py-0.5 font-mono text-xs font-bold text-cyan-300">
                GATE CBT Results
              </span>
              <h1 className="text-2xl font-black text-white mt-1">
                {resultSession.title}
              </h1>
            </div>
            <button
              onClick={() => {
                setIsTestActive(false);
                setIsSubmitted(false);
              }}
              className="flex items-center gap-1.5 rounded-xl bg-cyan-500 px-4 py-2 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition"
            >
              <RotateCcw className="h-4 w-4" />
              <span>Back to Simulator</span>
            </button>
          </div>

          {/* Metric Cards */}
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div className="rounded-xl border border-slate-800 bg-slate-950 p-4">
              <span className="text-[11px] font-bold uppercase text-slate-400">Total Marks</span>
              <div className="text-2xl font-mono font-black text-cyan-300 mt-1">
                {resultSession.totalScore}
              </div>
            </div>
            <div className="rounded-xl border border-slate-800 bg-slate-950 p-4">
              <span className="text-[11px] font-bold uppercase text-slate-400">Accuracy</span>
              <div className="text-2xl font-mono font-black text-emerald-400 mt-1">
                {resultSession.accuracy}%
              </div>
            </div>
            <div className="rounded-xl border border-slate-800 bg-slate-950 p-4">
              <span className="text-[11px] font-bold uppercase text-slate-400">Attempted</span>
              <div className="text-2xl font-mono font-black text-slate-200 mt-1">
                {Object.keys(resultSession.userAnswers).length} / {resultSession.totalQuestions}
              </div>
            </div>
            <div className="rounded-xl border border-slate-800 bg-slate-950 p-4">
              <span className="text-[11px] font-bold uppercase text-slate-400">Test Type</span>
              <div className="text-sm font-mono font-bold text-purple-300 mt-2">
                {resultSession.testType}
              </div>
            </div>
          </div>

          {/* Question by Question Review */}
          <div className="space-y-3">
            <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400">
              Question-by-Question Solution Analysis
            </h3>

            <div className="space-y-3">
              {questions.map((q, idx) => {
                const userAns = resultSession.userAnswers[q.id];
                const res = userAns ? evaluateQuestion(q, userAns) : null;

                return (
                  <div
                    key={q.id}
                    className="rounded-xl border border-slate-800 bg-slate-950 p-4 space-y-2 text-xs"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-slate-200">
                        Q{idx + 1}. {q.id} ({q.questionType} - {q.marks}M)
                      </span>
                      {res ? (
                        res.isCorrect ? (
                          <span className="font-mono font-bold text-emerald-400">
                            + {res.marksAwarded}M (Correct)
                          </span>
                        ) : (
                          <span className="font-mono font-bold text-rose-400">
                            {res.marksAwarded}M (Incorrect)
                          </span>
                        )
                      ) : (
                        <span className="text-slate-500 font-mono">Unattempted (0M)</span>
                      )}
                    </div>

                    <p className="text-slate-300">{q.questionText}</p>

                    <div className="flex flex-wrap gap-4 pt-1 font-mono text-[11px]">
                      <span className="text-slate-400">
                        Your Answer: <strong className="text-white">{userAns || "None"}</strong>
                      </span>
                      <span className="text-emerald-400">
                        Official Key: <strong>{q.correctAnswers}</strong>
                      </span>
                    </div>

                    <div className="border-t border-slate-800/80 pt-2 text-slate-400 text-[11px] whitespace-pre-line">
                      {q.detailedSolution}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 2. Exam Launcher Screen (If not active)
  if (!isTestActive) {
    return (
      <div id="exam-launcher-screen" className="max-w-3xl mx-auto space-y-6 pb-12">
        <div>
          <h1 className="text-xl font-black text-white sm:text-2xl">
            GATE CBT Exam Simulator (Computer Based Test)
          </h1>
          <p className="text-xs text-slate-400">
            Official TCS iON interface layout, countdown clock, authentic question palette, and integrated scientific calculator.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Sectional Mini Mock */}
          <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 flex flex-col justify-between space-y-4 hover:border-slate-700 transition">
            <div className="space-y-2">
              <span className="rounded bg-cyan-500/20 px-2 py-0.5 font-mono text-[10px] font-bold text-cyan-300">
                Speed Drill
              </span>
              <h3 className="text-lg font-bold text-white">
                Sectional Mini Mock Test
              </h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                15 Questions • 30 Minutes • Negative Marking • Perfect for quick lunch or morning test sessions.
              </p>
            </div>

            <button
              id="start-sectional-mock-btn"
              onClick={() => handleStartExam("SECTIONAL")}
              className="w-full flex items-center justify-center gap-2 rounded-xl bg-cyan-500 py-3 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition"
            >
              <Clock className="h-4 w-4" />
              <span>Start Sectional Test</span>
            </button>
          </div>

          {/* Full Mock */}
          <div className="rounded-2xl border border-cyan-800/40 bg-gradient-to-br from-slate-900 via-slate-900 to-cyan-950/30 p-6 flex flex-col justify-between space-y-4 hover:border-cyan-600/50 transition">
            <div className="space-y-2">
              <span className="rounded bg-purple-500/20 px-2 py-0.5 font-mono text-[10px] font-bold text-purple-300">
                Exam Benchmark
              </span>
              <h3 className="text-lg font-bold text-white">
                All-India Full Grand Mock
              </h3>
              <p className="text-xs text-slate-300 leading-relaxed">
                Full 65 Questions • 180 Minutes (3 Hours) • Comprehensive evaluation simulating the authentic GATE CSE morning/afternoon slot.
              </p>
            </div>

            <button
              id="start-full-mock-btn"
              onClick={() => handleStartExam("FULL_MOCK")}
              className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 py-3 text-xs font-bold text-slate-950 hover:brightness-110 transition shadow-lg shadow-cyan-500/20"
            >
              <Award className="h-4 w-4" />
              <span>Start Full 3-Hour Mock</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 3. Active Official GATE CBT Screen
  return (
    <div id="cbt-active-screen" className="space-y-4 pb-12">
      {/* Top CBT Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 bg-[#070A10] p-3 rounded-xl">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5 font-bold text-white text-xs">
            <span className="h-2.5 w-2.5 rounded-full bg-emerald-400 animate-pulse" />
            <span>GATE CSE 2027 CBT</span>
          </div>
          <span className="text-xs text-slate-400 hidden sm:inline">|</span>
          <span className="text-xs font-medium text-slate-300 hidden sm:inline">
            Candidate: Aspirant
          </span>
        </div>

        <div className="flex items-center gap-3">
          {/* Virtual Calculator button */}
          <button
            onClick={onToggleCalc}
            className="flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800 px-2.5 py-1 text-xs font-semibold text-cyan-300 hover:bg-slate-700 transition"
          >
            <Calculator className="h-3.5 w-3.5" />
            <span>Calculator</span>
          </button>

          {/* Time Remaining Counter */}
          <div className="flex items-center gap-1.5 rounded-lg border border-cyan-800/60 bg-cyan-950/40 px-3 py-1 font-mono text-sm font-bold text-cyan-300">
            <Clock className="h-4 w-4 text-cyan-400" />
            <span>{formatTime(remainingSeconds)}</span>
          </div>

          <button
            onClick={() => setShowSubmitModal(true)}
            className="rounded-lg bg-emerald-600 px-3 py-1 text-xs font-bold text-white hover:bg-emerald-500 transition shadow"
          >
            Submit Test
          </button>
        </div>
      </div>

      {/* Main CBT Split: Question Panel (Left) + Question Palette (Right) */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-4">
        {/* Left: Question Area (3 cols) */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 flex flex-col justify-between space-y-6 lg:col-span-3">
          <div className="space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <span className="font-mono text-xs font-bold text-cyan-400">
                Question No. {currentIndex + 1}
              </span>
              <div className="flex items-center gap-2 text-xs font-mono">
                <span className="rounded bg-slate-800 px-2 py-0.5 text-slate-300">
                  {currentQ?.questionType}
                </span>
                <span className="text-emerald-400">+{currentQ?.marks} Marks</span>
                {currentQ?.negativeMarks > 0 && (
                  <span className="text-rose-400">-{currentQ?.negativeMarks}</span>
                )}
              </div>
            </div>

            <div className="text-sm font-medium leading-relaxed text-slate-100 whitespace-pre-line min-h-[140px]">
              {currentQ?.questionText}
            </div>

            {/* Answer Input Area */}
            <div className="mt-4 pt-2">
              {currentQ?.questionType === "NAT" ? (
                <div className="space-y-2">
                  <label className="text-xs font-bold text-slate-400">
                    Enter Numerical Value:
                  </label>
                  <input
                    type="text"
                    placeholder="Enter answer"
                    value={userAnswers[currentQ.id] || ""}
                    onChange={(e) => handleSelectAnswer(e.target.value)}
                    className="w-56 rounded-xl border border-slate-700 bg-slate-950 px-3 py-2 font-mono text-sm text-cyan-300 placeholder-slate-600 focus:border-cyan-500 focus:outline-none"
                  />
                </div>
              ) : (
                <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
                  {[
                    { key: "A", text: currentQ?.optionA },
                    { key: "B", text: currentQ?.optionB },
                    { key: "C", text: currentQ?.optionC },
                    { key: "D", text: currentQ?.optionD },
                  ]
                    .filter((o) => !!o.text)
                    .map((opt) => {
                      const isMsq = currentQ?.questionType === "MSQ";
                      const currentSelected = userAnswers[currentQ?.id] || "";
                      const isSelected = isMsq
                        ? currentSelected.split(",").includes(opt.key)
                        : currentSelected === opt.key;

                      return (
                        <div
                          key={opt.key}
                          onClick={() => {
                            if (isMsq) {
                              const s = new Set(currentSelected.split(",").filter(Boolean));
                              if (s.has(opt.key)) s.delete(opt.key);
                              else s.add(opt.key);
                              handleSelectAnswer(Array.from(s).sort().join(","));
                            } else {
                              handleSelectAnswer(opt.key);
                            }
                          }}
                          className={`flex cursor-pointer items-start gap-3 rounded-xl border p-3.5 text-xs transition ${
                            isSelected
                              ? "border-cyan-500 bg-cyan-500/15 text-cyan-100"
                              : "border-slate-800 bg-slate-950/60 text-slate-300 hover:border-slate-700"
                          }`}
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
            </div>
          </div>

          {/* Bottom Actions Bar */}
          <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-800 pt-4">
            <div className="flex items-center gap-2">
              <button
                onClick={handleToggleReview}
                className={`rounded-lg border px-3 py-2 text-xs font-bold transition ${
                  markedForReview.has(currentQ?.id)
                    ? "border-purple-500 bg-purple-500/20 text-purple-300"
                    : "border-slate-700 bg-slate-800 text-slate-300 hover:bg-slate-700"
                }`}
              >
                {markedForReview.has(currentQ?.id) ? "Marked for Review" : "Mark for Review & Next"}
              </button>
              <button
                onClick={handleClearResponse}
                className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-xs font-bold text-slate-300 hover:text-rose-400 transition"
              >
                Clear Response
              </button>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => handleNavigateQuestion(currentIndex - 1)}
                disabled={currentIndex === 0}
                className="flex items-center gap-1 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-xs font-bold text-slate-300 disabled:opacity-40 transition hover:bg-slate-700"
              >
                <ArrowLeft className="h-3.5 w-3.5" />
                <span>Previous</span>
              </button>
              <button
                onClick={handleSaveAndNext}
                className="flex items-center gap-1 rounded-lg bg-cyan-500 px-4 py-2 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition shadow"
              >
                <span>Save & Next</span>
                <ArrowRight className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        </div>

        {/* Right: Question Palette (1 col) */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-4 space-y-4">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300 border-b border-slate-800 pb-2">
            Question Palette
          </h4>

          {/* Official Legend */}
          <div className="grid grid-cols-2 gap-2 text-[10px] text-slate-400">
            <div className="flex items-center gap-1.5">
              <span className="h-3 w-3 rounded bg-emerald-500" />
              <span>Answered</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-3 w-3 rounded bg-rose-500" />
              <span>Not Answered</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-3 w-3 rounded bg-purple-500" />
              <span>Review</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-3 w-3 rounded bg-slate-700" />
              <span>Not Visited</span>
            </div>
          </div>

          {/* Palette Grid */}
          <div className="grid grid-cols-5 gap-1.5 max-h-72 overflow-y-auto pr-1">
            {questions.map((q, idx) => {
              const hasAnswered = !!userAnswers[q.id]?.trim();
              const isMarked = markedForReview.has(q.id);
              const isVisited = visitedQuestions.has(q.id);
              const isCurrent = currentIndex === idx;

              let badgeStyle = "bg-slate-800 text-slate-400"; // Not visited
              if (isMarked) {
                badgeStyle = hasAnswered
                  ? "bg-purple-600 text-white ring-2 ring-emerald-400"
                  : "bg-purple-600 text-white";
              } else if (hasAnswered) {
                badgeStyle = "bg-emerald-600 text-white";
              } else if (isVisited) {
                badgeStyle = "bg-rose-600 text-white";
              }

              return (
                <button
                  key={q.id}
                  onClick={() => handleNavigateQuestion(idx)}
                  className={`h-8 w-8 rounded-lg font-mono text-xs font-bold transition ${badgeStyle} ${
                    isCurrent ? "ring-2 ring-cyan-400 shadow-md scale-105" : ""
                  }`}
                >
                  {idx + 1}
                </button>
              );
            })}
          </div>

          <div className="border-t border-slate-800 pt-3">
            <button
              onClick={() => setShowSubmitModal(true)}
              className="w-full rounded-xl bg-emerald-600 py-2.5 text-xs font-bold text-white hover:bg-emerald-500 transition shadow"
            >
              Submit Entire Test
            </button>
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {showSubmitModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900 p-6 space-y-4 shadow-2xl">
            <h3 className="text-base font-bold text-white">
              Are you sure you want to submit?
            </h3>
            <p className="text-xs text-slate-300">
              You have answered {Object.keys(userAnswers).length} of {questions.length} questions. Once submitted, your score will be calculated and saved.
            </p>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setShowSubmitModal(false)}
                className="rounded-lg border border-slate-700 bg-slate-800 px-4 py-2 text-xs font-bold text-slate-300 hover:bg-slate-700"
              >
                Continue Test
              </button>
              <button
                onClick={handleSubmitTest}
                className="rounded-lg bg-emerald-600 px-4 py-2 text-xs font-bold text-white hover:bg-emerald-500"
              >
                Confirm Submit
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
