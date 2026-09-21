import React, { useState } from "react";
import {
  Settings,
  Target,
  Clock,
  RotateCcw,
  Download,
  Check,
  AlertTriangle,
} from "lucide-react";
import { UserProfile } from "../../types";

interface SettingsViewProps {
  userProfile: UserProfile;
  onUpdateProfile: (profile: Partial<UserProfile>) => void;
  onResetDatabase: () => void;
  onExportData: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({
  userProfile,
  onUpdateProfile,
  onResetDatabase,
  onExportData,
}) => {
  const [targetExam, setTargetExam] = useState(userProfile.targetExam || "GATE CSE 2027");
  const [targetScore, setTargetScore] = useState(userProfile.targetScore || 80);
  const [targetRank, setTargetRank] = useState(userProfile.targetRank || "AIR < 100");
  const [dailyHours, setDailyHours] = useState(userProfile.dailyTargetHours || 6);
  const [dailyQuestions, setDailyQuestions] = useState(userProfile.dailyQuestionsTarget || 15);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [showResetConfirm, setShowResetConfirm] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    onUpdateProfile({
      targetExam,
      targetScore,
      targetRank,
      dailyTargetHours: dailyHours,
      dailyQuestionsTarget: dailyQuestions,
    });
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 2500);
  };

  return (
    <div id="settings-view" className="max-w-2xl mx-auto space-y-6 pb-12">
      <div>
        <h1 className="text-xl font-black text-white sm:text-2xl">
          Preparation Goals & System Settings
        </h1>
        <p className="text-xs text-slate-400">
          Tailor your daily question targets, target percentile, and manage local storage databases.
        </p>
      </div>

      <form onSubmit={handleSave} className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 space-y-5 shadow-md">
        <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300 border-b border-slate-800 pb-2">
          Aspirant Profile & Targets
        </h3>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 text-xs">
          <div>
            <label className="block text-slate-400 font-semibold mb-1">Target Exam:</label>
            <input
              type="text"
              value={targetExam}
              onChange={(e) => setTargetExam(e.target.value)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3.5 py-2 text-slate-200 focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div>
            <label className="block text-slate-400 font-semibold mb-1">Target All India Rank (AIR):</label>
            <input
              type="text"
              value={targetRank}
              onChange={(e) => setTargetRank(e.target.value)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3.5 py-2 text-slate-200 focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div>
            <label className="block text-slate-400 font-semibold mb-1">Target GATE Score (/100):</label>
            <input
              type="number"
              min={30}
              max={100}
              value={targetScore}
              onChange={(e) => setTargetScore(parseInt(e.target.value) || 75)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3.5 py-2 text-slate-200 focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div>
            <label className="block text-slate-400 font-semibold mb-1">Daily Study Target (Hours):</label>
            <input
              type="number"
              min={1}
              max={16}
              value={dailyHours}
              onChange={(e) => setDailyHours(parseInt(e.target.value) || 6)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3.5 py-2 text-slate-200 focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div className="sm:col-span-2">
            <label className="block text-slate-400 font-semibold mb-1">Daily Questions Practice Target:</label>
            <input
              type="number"
              min={5}
              max={100}
              value={dailyQuestions}
              onChange={(e) => setDailyQuestions(parseInt(e.target.value) || 15)}
              className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3.5 py-2 text-slate-200 focus:outline-none focus:border-cyan-500"
            />
          </div>
        </div>

        <div className="flex items-center justify-between border-t border-slate-800 pt-4">
          {savedSuccess ? (
            <span className="flex items-center gap-1 text-xs font-bold text-emerald-400">
              <Check className="h-4 w-4" />
              <span>Targets Updated Successfully</span>
            </span>
          ) : (
            <span />
          )}

          <button
            type="submit"
            className="rounded-xl bg-cyan-500 px-5 py-2 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition"
          >
            Save Target Settings
          </button>
        </div>
      </form>

      {/* Data Management */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 space-y-4">
        <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
          Data Management & Backups
        </h3>
        <p className="text-xs text-slate-400">
          Export your practice logs, mistake notes, and flashcard SRS repetitions, or reset the local database back to default seed data.
        </p>

        <div className="flex flex-wrap gap-3 pt-2">
          <button
            onClick={onExportData}
            className="flex items-center gap-2 rounded-xl border border-slate-700 bg-slate-800 px-4 py-2.5 text-xs font-bold text-slate-200 hover:bg-slate-700 transition"
          >
            <Download className="h-4 w-4" />
            <span>Export Study Data (JSON)</span>
          </button>

          <button
            onClick={() => setShowResetConfirm(true)}
            className="flex items-center gap-2 rounded-xl border border-rose-900/50 bg-rose-950/30 px-4 py-2.5 text-xs font-bold text-rose-300 hover:bg-rose-900/50 transition"
          >
            <RotateCcw className="h-4 w-4" />
            <span>Reset Database to Seed</span>
          </button>
        </div>
      </div>

      {showResetConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-2xl border border-rose-900/50 bg-slate-900 p-6 space-y-4 shadow-2xl">
            <div className="flex items-center gap-2 text-rose-400 font-bold">
              <AlertTriangle className="h-5 w-5" />
              <span>Confirm Database Reset</span>
            </div>
            <p className="text-xs text-slate-300">
              This will restore all subjects, topics, PYQs, and flashcards back to their factory seeds. Your past attempt logs and custom notes will be reset.
            </p>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setShowResetConfirm(false)}
                className="rounded-lg border border-slate-700 bg-slate-800 px-3.5 py-1.5 text-xs font-bold text-slate-300"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  setShowResetConfirm(false);
                  onResetDatabase();
                }}
                className="rounded-lg bg-rose-600 px-4 py-1.5 text-xs font-bold text-white hover:bg-rose-500"
              >
                Confirm Reset
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
