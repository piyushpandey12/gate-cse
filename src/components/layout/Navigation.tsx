import React from "react";
import {
  LayoutDashboard,
  BookOpen,
  FileSearch,
  CheckCircle2,
  Clock,
  Repeat,
  AlertTriangle,
  Layers,
  Sigma,
  Compass,
  FileText,
  Calendar,
  BarChart3,
  Sparkles,
  Settings,
} from "lucide-react";
import { NavSection } from "../../types";

interface NavigationProps {
  currentSection?: NavSection;
  activeSection?: NavSection;
  onNavigate: (section: NavSection, payload?: any) => void;
  dueRevisionCount: number;
  mistakesCount?: number;
  isOpenMobile?: boolean;
  onCloseMobile?: () => void;
}

export const Navigation: React.FC<NavigationProps> = ({
  currentSection,
  activeSection,
  onNavigate,
  dueRevisionCount,
  mistakesCount = 0,
  isOpenMobile = false,
  onCloseMobile,
}) => {
  const section = activeSection || currentSection || "dashboard";
  const primaryNavItems = [
    { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
    { id: "subjects", label: "Syllabus", icon: BookOpen },
    { id: "pyqs", label: "PYQ Explorer", icon: FileSearch },
    { id: "practice", label: "Practice", icon: CheckCircle2 },
    { id: "exam_simulator", label: "Exam CBT", icon: Clock },
    {
      id: "revision",
      label: "Revision SRS",
      icon: Repeat,
      badge: dueRevisionCount > 0 ? dueRevisionCount : undefined,
      badgeColor: "bg-cyan-500/20 text-cyan-300 border-cyan-500/30",
    },
    {
      id: "mistakes",
      label: "Mistakes",
      icon: AlertTriangle,
      badge: mistakesCount > 0 ? mistakesCount : undefined,
      badgeColor: "bg-rose-500/20 text-rose-300 border-rose-500/30",
    },
  ];

  const secondaryNavItems = [
    { id: "flashcards", label: "Flashcards", icon: Layers },
    { id: "formulas", label: "Formulas", icon: Sigma },
    { id: "resources", label: "Resources", icon: Compass },
    { id: "notes", label: "Notes", icon: FileText },
    { id: "planner", label: "Study Plan", icon: Calendar },
    { id: "analytics", label: "Analytics", icon: BarChart3 },
    { id: "ai_tutor", label: "AI Tutor", icon: Sparkles },
    { id: "settings", label: "Settings", icon: Settings },
  ];

  return (
    <aside
      id="main-sidebar"
      className="hidden w-60 shrink-0 flex-col border-r border-slate-800 bg-[#0B101B] p-3 lg:flex select-none"
    >
      <div className="px-3 py-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
        Preparation Core
      </div>
      <nav className="space-y-1">
        {primaryNavItems.map((item) => {
          const Icon = item.icon;
          const isActive =
            section === item.id ||
            (item.id === "exam_simulator" && section === "exam") ||
            (item.id === "subjects" &&
              (section === "subject_detail" || section === "topic_detail"));

          return (
            <button
              key={item.id}
              id={`nav-${item.id}`}
              onClick={() => onNavigate(item.id as NavSection)}
              className={`group flex w-full items-center justify-between rounded-lg px-3 py-2 text-xs font-semibold transition ${
                isActive
                  ? "bg-cyan-500/15 text-cyan-400 border border-cyan-500/30 shadow-sm shadow-cyan-950/40"
                  : "text-slate-400 hover:bg-slate-800/60 hover:text-slate-200"
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Icon
                  className={`h-4 w-4 transition ${
                    isActive ? "text-cyan-400" : "text-slate-400 group-hover:text-slate-300"
                  }`}
                />
                <span>{item.label}</span>
              </div>
              {item.badge !== undefined && (
                <span
                  className={`rounded-full border px-1.5 py-0.5 text-[10px] font-bold ${item.badgeColor}`}
                >
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      <div className="mt-6 px-3 py-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
        Tools & Mastery
      </div>
      <nav className="space-y-1">
        {secondaryNavItems.map((item) => {
          const Icon = item.icon;
          const isActive = section === item.id;

          return (
            <button
              key={item.id}
              id={`nav-${item.id}`}
              onClick={() => onNavigate(item.id as NavSection)}
              className={`group flex w-full items-center justify-between rounded-lg px-3 py-2 text-xs font-semibold transition ${
                isActive
                  ? "bg-cyan-500/15 text-cyan-400 border border-cyan-500/30 shadow-sm shadow-cyan-950/40"
                  : "text-slate-400 hover:bg-slate-800/60 hover:text-slate-200"
              }`}
            >
              <div className="flex items-center gap-2.5">
                <Icon
                  className={`h-4 w-4 transition ${
                    isActive ? "text-cyan-400" : "text-slate-400 group-hover:text-slate-300"
                  }`}
                />
                <span>{item.label}</span>
              </div>
            </button>
          );
        })}
      </nav>

      {/* Target Exam Info Widget */}
      <div className="mt-auto rounded-xl border border-slate-800 bg-slate-900/60 p-3 text-xs">
        <div className="flex items-center justify-between text-[11px] text-slate-400 mb-1">
          <span>Target Paper</span>
          <span className="font-mono text-cyan-400 font-bold">CS (CSE)</span>
        </div>
        <p className="text-slate-300 font-semibold">GATE 2027</p>
        <div className="mt-2 flex items-center justify-between border-t border-slate-800/80 pt-2 text-[10px] text-slate-400">
          <span>65 Questions</span>
          <span>100 Marks • 180m</span>
        </div>
      </div>
    </aside>
  );
};
