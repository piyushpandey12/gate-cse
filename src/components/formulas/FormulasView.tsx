import React, { useState } from "react";
import {
  Zap,
  Search,
  Star,
  Copy,
  Check,
  AlertTriangle,
  BookOpen,
} from "lucide-react";
import { Formula, Subject } from "../../types";
import { FormulaMath } from "../common/FormulaMath";

interface FormulasViewProps {
  formulas: Formula[];
  subjects: Subject[];
  onToggleFavorite: (id: string) => void;
}

export const FormulasView: React.FC<FormulasViewProps> = ({
  formulas,
  subjects,
  onToggleFavorite,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>("ALL");
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const filtered = formulas.filter((f) => {
    const fTitle = f.title || f.name || "";
    const fLatex = f.latexFormula || f.formulaLatex || "";
    if (selectedSubject !== "ALL" && f.subjectId !== selectedSubject) return false;
    if (
      searchQuery &&
      !fTitle.toLowerCase().includes(searchQuery.toLowerCase()) &&
      !fLatex.toLowerCase().includes(searchQuery.toLowerCase()) &&
      !f.description.toLowerCase().includes(searchQuery.toLowerCase())
    ) {
      return false;
    }
    return true;
  });

  const handleCopyLatex = (f: Formula) => {
    const code = f.latexFormula || f.formulaLatex || "";
    navigator.clipboard.writeText(code);
    setCopiedId(f.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div id="formulas-view" className="space-y-6 pb-12">
      <div>
        <h1 className="text-xl font-black text-white sm:text-2xl">
          High-Yield Formula & Theorem Handbook
        </h1>
        <p className="text-xs text-slate-400">
          Curated mathematical definitions, recurrence relations, pipeline equations, and network theorems rendered via KaTeX.
        </p>
      </div>

      {/* Filter & Search Bar */}
      <div className="flex flex-col gap-3 rounded-2xl border border-slate-800 bg-slate-900/80 p-4 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search formulas, theorems, recurrence..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full rounded-xl border border-slate-800 bg-slate-950 pl-9 pr-3.5 py-2 text-xs text-slate-200 placeholder-slate-500 focus:border-cyan-500 focus:outline-none"
          />
        </div>

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
      </div>

      {/* Formulas Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {filtered.map((formula) => {
          const sub = subjects.find((s) => s.id === formula.subjectId);
          const isCopied = copiedId === formula.id;

          return (
            <div
              key={formula.id}
              className="flex flex-col justify-between rounded-xl border border-slate-800 bg-slate-900/70 p-5 space-y-4 shadow-sm hover:border-slate-700 transition"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="rounded bg-cyan-500/20 px-2 py-0.5 font-mono text-[10px] font-bold text-cyan-300">
                      {sub?.code || formula.subjectId}
                    </span>
                    <h3 className="text-sm font-bold text-white">
                      {formula.title || formula.name}
                    </h3>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => handleCopyLatex(formula)}
                      className="rounded p-1 text-slate-400 hover:text-slate-200 transition"
                      title="Copy LaTeX code"
                    >
                      {isCopied ? (
                        <Check className="h-3.5 w-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="h-3.5 w-3.5" />
                      )}
                    </button>
                    <button
                      onClick={() => onToggleFavorite(formula.id)}
                      className="rounded p-1 text-slate-400 hover:text-amber-400 transition"
                      title="Favorite Formula"
                    >
                      <Star
                        className={`h-4 w-4 ${
                          formula.isFavorite ? "fill-amber-400 text-amber-400" : ""
                        }`}
                      />
                    </button>
                  </div>
                </div>

                {/* Mathematical Equation Card */}
                <div className="rounded-xl border border-cyan-950 bg-slate-950/80 p-3.5 text-center overflow-x-auto text-cyan-200">
                  <FormulaMath math={formula.latexFormula || formula.formulaLatex} block />
                </div>

                <p className="text-xs text-slate-300 leading-relaxed">
                  {formula.description}
                </p>

                {formula.shortcutTrick && (
                  <div className="rounded-lg border border-amber-900/40 bg-amber-950/20 p-2.5 space-y-1">
                    <div className="flex items-center gap-1.5 font-semibold text-amber-300 text-[11px]">
                      <Zap className="h-3.5 w-3.5" />
                      <span>Exam Application Trick:</span>
                    </div>
                    <p className="text-amber-200/90 text-xs pl-5">
                      {formula.shortcutTrick}
                    </p>
                  </div>
                )}

                {formula.commonTrap && (
                  <div className="rounded-lg border border-rose-900/40 bg-rose-950/20 p-2.5 space-y-1">
                    <div className="flex items-center gap-1.5 font-semibold text-rose-300 text-[11px]">
                      <AlertTriangle className="h-3.5 w-3.5" />
                      <span>Common Mistake Trap:</span>
                    </div>
                    <p className="text-rose-200/90 text-xs pl-5">
                      {formula.commonTrap}
                    </p>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
