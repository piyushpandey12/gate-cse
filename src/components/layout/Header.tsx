import React from "react";
import { Calculator, Flame, Clock, Sparkles, Menu } from "lucide-react";
import { UserProfile, ExamEvent } from "../../types";

interface HeaderProps {
  userProfile: UserProfile;
  examEvents?: ExamEvent[];
  onToggleCalc?: () => void;
  isCalcOpen?: boolean;
  onNavigate?: (section: any) => void;
  onToggleMobileMenu?: () => void;
  onOpenCalculator?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  userProfile,
  examEvents = [],
  onToggleCalc,
  isCalcOpen = false,
  onNavigate,
  onToggleMobileMenu,
  onOpenCalculator,
}) => {
  const mainExam = examEvents?.find((e) => e.isMainExamDate) || examEvents?.[2];
  const now = Date.now();
  const diffTime = mainExam ? mainExam.eventDateMillis - now : 0;
  const diffDays = Math.max(0, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));

  const handleCalcClick = () => {
    if (onOpenCalculator) onOpenCalculator();
    else if (onToggleCalc) onToggleCalc();
  };

  return (
    <header
      id="app-header"
      className="sticky top-0 z-40 flex h-16 w-full items-center justify-between border-b border-slate-800 bg-[#090D16]/90 px-4 sm:px-6 backdrop-blur-md"
    >
      {/* Brand & Target */}
      <div className="flex items-center gap-3">
        <button
          id="brand-logo-btn"
          onClick={() => onNavigate("dashboard")}
          className="flex items-center gap-2.5 text-left transition hover:opacity-90"
        >
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-cyan-500 to-blue-600 shadow-md shadow-cyan-500/20">
            <span className="font-mono text-base font-extrabold text-white">G</span>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold tracking-tight text-white sm:text-base">
                GATE CSE 2027
              </span>
              <span className="hidden rounded-full bg-cyan-950/80 px-2 py-0.5 text-[10px] font-semibold text-cyan-400 border border-cyan-800/60 sm:inline-block">
                All-India Syllabus
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-mono hidden sm:block">
              13 Subjects • 35+ Years PYQ • SM-2 Revision
            </p>
          </div>
        </button>
      </div>

      {/* Center/Right Metrics & Tools */}
      <div className="flex items-center gap-2 sm:gap-4">
        {/* Countdown */}
        <div
          id="header-countdown"
          className="flex items-center gap-2 rounded-lg border border-slate-800 bg-slate-900/80 px-2.5 py-1.5 text-xs text-slate-300"
          title={`Exam Date: ${mainExam?.eventDateFormatted || "Feb 2027"}`}
        >
          <Clock className="h-3.5 w-3.5 text-cyan-400" />
          <span className="font-mono font-bold text-white">{diffDays}</span>
          <span className="text-[11px] text-slate-400 hidden xs:inline">days to GATE</span>
        </div>

        {/* Streak */}
        <div
          id="header-streak"
          className="flex items-center gap-1.5 rounded-lg border border-amber-900/30 bg-amber-950/20 px-2.5 py-1.5 text-xs text-amber-400"
          title="Daily Study Streak"
        >
          <Flame className="h-3.5 w-3.5 fill-amber-400 text-amber-400 animate-pulse" />
          <span className="font-mono font-bold">{userProfile.currentStreak}</span>
          <span className="text-[11px] text-amber-500/80 hidden xs:inline">streak</span>
        </div>

        {/* Calculator Toggle */}
        <button
          id="toggle-calculator-btn"
          onClick={handleCalcClick}
          className={`flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-semibold transition ${
            isCalcOpen
              ? "border-cyan-500 bg-cyan-500/20 text-cyan-300"
              : "border-slate-800 bg-slate-900 text-slate-300 hover:border-slate-700 hover:text-white"
          }`}
          title="Toggle Official GATE Virtual Calculator"
        >
          <Calculator className="h-3.5 w-3.5 text-cyan-400" />
          <span className="hidden sm:inline">Calc</span>
        </button>

        {/* AI Tutor Quick Access */}
        <button
          id="header-ai-tutor-btn"
          onClick={() => onNavigate && onNavigate("ai_tutor")}
          className="flex items-center gap-1.5 rounded-lg border border-purple-800/50 bg-purple-950/30 px-2.5 py-1.5 text-xs font-semibold text-purple-300 transition hover:bg-purple-900/40"
        >
          <Sparkles className="h-3.5 w-3.5 text-purple-400" />
          <span className="hidden md:inline">AI Tutor</span>
        </button>

        {/* Mobile Menu Toggle */}
        {onToggleMobileMenu && (
          <button
            id="header-mobile-menu-btn"
            onClick={onToggleMobileMenu}
            className="flex items-center justify-center p-1.5 rounded-lg border border-slate-800 bg-slate-900 text-slate-300 hover:text-white lg:hidden"
            title="Open Navigation Menu"
          >
            <Menu className="h-4 w-4" />
          </button>
        )}
      </div>
    </header>
  );
};
