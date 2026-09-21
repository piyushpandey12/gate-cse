import React, { useState } from "react";
import {
  Layers,
  RotateCw,
  ArrowRight,
  ArrowLeft,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  Zap,
} from "lucide-react";
import { Flashcard, Subject } from "../../types";

interface FlashcardsViewProps {
  flashcards: Flashcard[];
  subjects: Subject[];
  onReviewFlashcard: (id: string, quality: number) => void;
}

export const FlashcardsView: React.FC<FlashcardsViewProps> = ({
  flashcards,
  subjects,
  onReviewFlashcard,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>("ALL");
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isFlipped, setIsFlipped] = useState<boolean>(false);

  const filtered = flashcards.filter(
    (fc) => selectedSubject === "ALL" || fc.subjectId === selectedSubject
  );

  const currentCard = filtered[currentIndex] || filtered[0];

  const handleNext = () => {
    setIsFlipped(false);
    if (currentIndex + 1 < filtered.length) {
      setCurrentIndex((i) => i + 1);
    } else {
      setCurrentIndex(0);
    }
  };

  const handlePrev = () => {
    setIsFlipped(false);
    if (currentIndex > 0) {
      setCurrentIndex((i) => i - 1);
    } else {
      setCurrentIndex(filtered.length - 1);
    }
  };

  const handleRate = (quality: number) => {
    if (!currentCard) return;
    onReviewFlashcard(currentCard.id, quality);
    handleNext();
  };

  return (
    <div id="flashcards-view" className="max-w-2xl mx-auto space-y-6 pb-12">
      <div>
        <h1 className="text-xl font-black text-white sm:text-2xl">
          High-Yield Flashcards (Definitions & Complexities)
        </h1>
        <p className="text-xs text-slate-400">
          Reinforce theorem statements, complexity bounds, and architecture hazards with active recall and SM-2 scheduling.
        </p>
      </div>

      {/* Filter */}
      <div className="flex items-center justify-between">
        <select
          value={selectedSubject}
          onChange={(e) => {
            setSelectedSubject(e.target.value);
            setCurrentIndex(0);
            setIsFlipped(false);
          }}
          className="rounded-xl border border-slate-800 bg-slate-900 px-3.5 py-2 text-xs text-slate-200 focus:outline-none"
        >
          <option value="ALL">All Subjects</option>
          {subjects.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name} ({s.code})
            </option>
          ))}
        </select>

        <span className="font-mono text-xs font-bold text-slate-400">
          {filtered.length > 0 ? `${currentIndex + 1} / ${filtered.length}` : "0 / 0"}
        </span>
      </div>

      {/* 3D Flashcard Container */}
      {currentCard ? (
        <div className="space-y-4">
          <div
            id="interactive-flashcard"
            onClick={() => setIsFlipped((f) => !f)}
            className="group relative h-72 w-full cursor-pointer rounded-2xl border border-slate-700 bg-gradient-to-br from-slate-900 via-slate-900 to-slate-950 p-6 shadow-xl transition-all duration-300 hover:border-cyan-500/50"
          >
            <div className="flex h-full flex-col justify-between">
              {/* Card Header */}
              <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                <span className="rounded bg-cyan-500/20 px-2.5 py-0.5 font-mono text-[10px] font-bold text-cyan-300">
                  {currentCard.subjectId} • {currentCard.keyConcept}
                </span>
                <span className="flex items-center gap-1 text-[11px] text-slate-400">
                  <RotateCw className="h-3 w-3 group-hover:rotate-180 transition-transform duration-500" />
                  <span>Click card to flip</span>
                </span>
              </div>

              {/* Card Center Content */}
              <div className="my-auto text-center px-4">
                {!isFlipped ? (
                  <div className="space-y-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500">
                      Front Prompt
                    </span>
                    <h3 className="text-base sm:text-lg font-bold text-white leading-relaxed">
                      {currentCard.front}
                    </h3>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-400">
                      Back Explanation
                    </span>
                    <p className="text-sm text-slate-200 whitespace-pre-line leading-relaxed font-medium">
                      {currentCard.back}
                    </p>
                  </div>
                )}
              </div>

              {/* Card Footer */}
              <div className="flex items-center justify-between border-t border-slate-800/80 pt-2 text-[10px] text-slate-400 font-mono">
                <span>SRS Interval: {currentCard.intervalDays}d</span>
                <span>Ease Factor: {currentCard.easeFactor}</span>
              </div>
            </div>
          </div>

          {/* SM-2 Self-Rating Controls (Shown when flipped) */}
          {isFlipped ? (
            <div className="space-y-2">
              <span className="text-center block text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                How well did you recall this concept?
              </span>
              <div className="grid grid-cols-4 gap-2">
                <button
                  onClick={() => handleRate(1)}
                  className="rounded-xl border border-rose-900/50 bg-rose-950/40 py-2 text-xs font-bold text-rose-300 hover:bg-rose-900/60 transition"
                >
                  Again (1d)
                </button>
                <button
                  onClick={() => handleRate(2)}
                  className="rounded-xl border border-amber-900/50 bg-amber-950/40 py-2 text-xs font-bold text-amber-300 hover:bg-amber-900/60 transition"
                >
                  Hard (2d)
                </button>
                <button
                  onClick={() => handleRate(4)}
                  className="rounded-xl border border-blue-900/50 bg-blue-950/40 py-2 text-xs font-bold text-blue-300 hover:bg-blue-900/60 transition"
                >
                  Good (+4d)
                </button>
                <button
                  onClick={() => handleRate(5)}
                  className="rounded-xl border border-emerald-900/50 bg-emerald-950/40 py-2 text-xs font-bold text-emerald-300 hover:bg-emerald-900/60 transition"
                >
                  Easy (+7d)
                </button>
              </div>
            </div>
          ) : (
            /* Navigation Controls */
            <div className="flex items-center justify-between pt-2">
              <button
                onClick={handlePrev}
                className="flex items-center gap-1 rounded-xl border border-slate-800 bg-slate-900 px-4 py-2 text-xs font-bold text-slate-300 hover:bg-slate-800 transition"
              >
                <ArrowLeft className="h-3.5 w-3.5" />
                <span>Previous</span>
              </button>
              <button
                onClick={handleNext}
                className="flex items-center gap-1 rounded-xl bg-cyan-500 px-4 py-2 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition shadow"
              >
                <span>Next Card</span>
                <ArrowRight className="h-3.5 w-3.5" />
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-8 text-center text-xs text-slate-400">
          No flashcards found for the selected subject.
        </div>
      )}
    </div>
  );
};
