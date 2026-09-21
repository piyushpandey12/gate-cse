import React, { useState } from "react";
import {
  Sparkles,
  Send,
  HelpCircle,
  Zap,
  AlertTriangle,
  Lightbulb,
  Bot,
  User,
  RotateCcw,
} from "lucide-react";
import { Question } from "../../types";

interface AiTutorViewProps {
  initialQuestion?: Question | null;
  initialTopic?: string | null;
}

interface Message {
  id: string;
  sender: "USER" | "AI";
  text: string;
  timestamp: string;
}

export const AiTutorView: React.FC<AiTutorViewProps> = ({
  initialQuestion = null,
  initialTopic = null,
}) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: "welcome",
      sender: "AI",
      text: initialQuestion
        ? `Hello! I see you are analyzing **${initialQuestion.id}** (${initialQuestion.subjectId} - ${initialQuestion.questionType}).\n\nProblem Statement:\n"${initialQuestion.questionText}"\n\nHow would you like to explore this? I can provide a gentle hint, break down the derivation, or point out negative-marking traps.`
        : `Welcome to your **GATE CSE AI Tutor**! Powered by rigorous computer science principles and official GATE evaluation standards.\n\nAsk me any doubt regarding:\n- Discrete Mathematics & Graph Theory\n- Data Structures & Master Theorem / Amortized Analysis\n- Operating Systems (Deadlocks, Paging, CPU Scheduling)\n- Computer Networks (Subnetting, TCP Congestion, Go-Back-N)\n- Theory of Computation (Chomsky Hierarchy, Pumping Lemma)\n- Compiler Design (LR Parsing, LL(1) tables, DAGs)\n\nOr click one of the quick prompts below to get started!`,
      timestamp: "Just now",
    },
  ]);

  const [inputQuery, setInputQuery] = useState("");
  const [isThinking, setIsThinking] = useState(false);
  const [activeMode, setActiveMode] = useState<"EXPLAIN" | "HINT" | "TRAPS" | "DERIVATION">("EXPLAIN");

  const handleSendMessage = async (queryText?: string) => {
    const textToSend = queryText || inputQuery;
    if (!textToSend.trim()) return;

    const userMsg: Message = {
      id: "u_" + Date.now(),
      sender: "USER",
      text: textToSend.trim(),
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputQuery("");
    setIsThinking(true);

    try {
      // Call backend API
      const res = await fetch("/api/gemini/tutor", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          query: textToSend,
          mode: activeMode,
          contextQuestion: initialQuestion ? initialQuestion.questionText : null,
          solution: initialQuestion ? initialQuestion.detailedSolution : null,
        }),
      });

      if (res.ok) {
        const data = await res.json();
        const aiMsg: Message = {
          id: "ai_" + Date.now(),
          sender: "AI",
          text: data.response,
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        };
        setMessages((prev) => [...prev, aiMsg]);
      } else {
        throw new Error("API call returned non-200");
      }
    } catch {
      // Curated academic fallback
      let fallbackText = "";
      const lower = textToSend.toLowerCase();

      if (lower.includes("hint") || activeMode === "HINT") {
        fallbackText = `💡 **Tutor Hint**: Focus on identifying the fundamental invariant or recurrence relation.\n\n1. For Recurrence: Check if $a \\cdot T(n/b) + f(n)$ satisfies Case 1, 2, or 3 of Master Theorem.\n2. For Pointers/Arrays in C: Remember that array indexing $a[i]$ is strictly evaluated as $*((a) + (i))$. Operator precedence dictates that postfix operators ($++, --$) bind tighter than prefix dereference ($*$).\n3. For OS Paging: Effective Memory Access Time formula: $EMAT = h(t_{TLB} + t_{mem}) + (1-h)(t_{TLB} + 2t_{mem})$.`;
      } else if (lower.includes("trap") || activeMode === "TRAPS") {
        fallbackText = `⚠️ **GATE Negative Marking Traps to Avoid**:\n- **NAT Boundary Precision**: If asked to calculate up to 2 decimal places, never truncate early during intermediate floating-point steps.\n- **MSQ Partial Marking**: MSQ questions in GATE award ZERO partial marks. Either all correct options are selected with no incorrect ones, or it awards 0.\n- **Go-Back-N vs Selective Repeat**: In GBN, window size of receiver is always strictly 1. Do not confuse this with SR where $W_s = W_r = 2^{m-1}$.`;
      } else {
        fallbackText = `📘 **Concept Breakdown**:\n\nIn GATE Computer Science, rigorous definition matters most.\n\n- **Algorithm Analysis**: Distinguish worst-case time complexity $O(g(n))$ from tight bound $\\Theta(g(n))$. GATE often asks whether a statement is "always true" or "exists an instance".\n- **Deadlock Condition**: Mutual exclusion, Hold and wait, No preemption, and Circular wait are necessary conditions, but only sufficient if single instance per resource type.\n\nLet me know if you would like me to generate practice questions on this topic!`;
      }

      const aiMsg: Message = {
        id: "ai_" + Date.now(),
        sender: "AI",
        text: fallbackText,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } finally {
      setIsThinking(false);
    }
  };

  return (
    <div id="ai-tutor-view" className="max-w-3xl mx-auto space-y-4 pb-12">
      {/* Header Banner */}
      <div className="rounded-2xl border border-purple-900/50 bg-gradient-to-br from-slate-900 via-slate-900 to-purple-950/30 p-5 shadow-lg flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="flex h-6 w-6 items-center justify-center rounded-lg bg-purple-500/20 text-purple-300">
              <Sparkles className="h-3.5 w-3.5" />
            </span>
            <span className="font-mono text-xs font-bold text-purple-300">
              Gemini 2.5 Computer Science Engine
            </span>
          </div>
          <h1 className="text-xl font-black text-white">
            Personal AI Doubt Solver & Concept Coach
          </h1>
          <p className="text-xs text-slate-300">
            Ask doubts, get step-by-step mathematical proofs, and dissect complex PYQs without getting spoiled.
          </p>
        </div>

        {/* Mode Selector */}
        <div className="flex flex-wrap gap-1.5 rounded-xl border border-slate-800 bg-slate-950 p-1 text-[11px] font-bold">
          <button
            onClick={() => setActiveMode("EXPLAIN")}
            className={`px-2.5 py-1 rounded-lg transition ${
              activeMode === "EXPLAIN" ? "bg-purple-600 text-white" : "text-slate-400 hover:text-white"
            }`}
          >
            Explain
          </button>
          <button
            onClick={() => setActiveMode("HINT")}
            className={`px-2.5 py-1 rounded-lg transition ${
              activeMode === "HINT" ? "bg-purple-600 text-white" : "text-slate-400 hover:text-white"
            }`}
          >
            Socratic Hint
          </button>
          <button
            onClick={() => setActiveMode("TRAPS")}
            className={`px-2.5 py-1 rounded-lg transition ${
              activeMode === "TRAPS" ? "bg-purple-600 text-white" : "text-slate-400 hover:text-white"
            }`}
          >
            Exam Traps
          </button>
        </div>
      </div>

      {/* Chat Messages Log */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-4 space-y-4 min-h-[380px] max-h-[500px] overflow-y-auto">
        {messages.map((m) => (
          <div
            key={m.id}
            className={`flex items-start gap-3 ${
              m.sender === "USER" ? "flex-row-reverse" : "flex-row"
            }`}
          >
            <div
              className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-xl text-xs font-bold ${
                m.sender === "USER"
                  ? "bg-cyan-500 text-slate-950"
                  : "bg-purple-900 text-purple-200 border border-purple-700/50"
              }`}
            >
              {m.sender === "USER" ? <User className="h-4 w-4" /> : <Bot className="h-4 w-4" />}
            </div>

            <div
              className={`max-w-[85%] rounded-2xl p-4 text-xs leading-relaxed space-y-2 whitespace-pre-line ${
                m.sender === "USER"
                  ? "bg-cyan-950/60 border border-cyan-800/50 text-cyan-100"
                  : "bg-slate-950/80 border border-slate-800 text-slate-200 shadow-sm"
              }`}
            >
              <p>{m.text}</p>
              <div
                className={`text-[10px] ${
                  m.sender === "USER" ? "text-cyan-400/70 text-right" : "text-slate-500"
                }`}
              >
                {m.timestamp}
              </div>
            </div>
          </div>
        ))}

        {isThinking && (
          <div className="flex items-center gap-2 text-xs text-purple-300 font-mono animate-pulse pl-2">
            <Sparkles className="h-3.5 w-3.5 text-purple-400 animate-spin" />
            <span>AI Tutor is analyzing GATE concept dependencies...</span>
          </div>
        )}
      </div>

      {/* Quick Prompts */}
      <div className="flex flex-wrap gap-2 text-[11px]">
        <button
          onClick={() => handleSendMessage("Explain how Master Theorem Case 2 works with log factors.")}
          className="rounded-lg border border-slate-800 bg-slate-900 px-3 py-1.5 text-slate-300 hover:border-cyan-500/50 hover:text-white transition"
        >
          Master Theorem Log Factors
        </button>
        <button
          onClick={() => handleSendMessage("What is the difference between MSQ and MCQ in GATE CSE?")}
          className="rounded-lg border border-slate-800 bg-slate-900 px-3 py-1.5 text-slate-300 hover:border-cyan-500/50 hover:text-white transition"
        >
          MSQ vs MCQ Scoring
        </button>
        <button
          onClick={() => handleSendMessage("Give me a shortcut trick to solve Pipeline Speedup problems.")}
          className="rounded-lg border border-slate-800 bg-slate-900 px-3 py-1.5 text-slate-300 hover:border-cyan-500/50 hover:text-white transition"
        >
          Pipeline Speedup Trick
        </button>
      </div>

      {/* Input Form */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSendMessage();
        }}
        className="flex items-center gap-2 rounded-2xl border border-slate-800 bg-slate-900 p-2 shadow-lg"
      >
        <input
          type="text"
          placeholder="Ask any GATE CSE theory, formula, or question doubt..."
          value={inputQuery}
          onChange={(e) => setInputQuery(e.target.value)}
          className="flex-1 bg-transparent px-3 text-xs text-slate-200 placeholder-slate-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={!inputQuery.trim() || isThinking}
          className="flex h-9 w-9 items-center justify-center rounded-xl bg-purple-600 text-white transition hover:bg-purple-500 disabled:opacity-40"
        >
          <Send className="h-4 w-4" />
        </button>
      </form>
    </div>
  );
};
