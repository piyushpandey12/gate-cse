import React, { useState } from "react";
import {
  Compass,
  BookOpen,
  Video,
  Bookmark,
  CheckCircle2,
  ExternalLink,
  Edit3,
  Check,
} from "lucide-react";
import { Resource, Subject, ResourceType } from "../../types";

interface ResourcesViewProps {
  resources: Resource[];
  subjects: Subject[];
  onToggleBookmark: (id: string) => void;
  onToggleCompleted: (id: string) => void;
  onUpdateNotes: (id: string, notes: string) => void;
}

export const ResourcesView: React.FC<ResourcesViewProps> = ({
  resources,
  subjects,
  onToggleBookmark,
  onToggleCompleted,
  onUpdateNotes,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>("ALL");
  const [selectedType, setSelectedType] = useState<string>("ALL");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [tempNotes, setTempNotes] = useState<string>("");

  const filtered = resources.filter((r) => {
    if (selectedSubject !== "ALL" && r.subjectId !== selectedSubject) return false;
    if (selectedType !== "ALL" && r.resourceType !== selectedType) return false;
    return true;
  });

  const handleStartEdit = (r: Resource) => {
    setEditingId(r.id);
    setTempNotes(r.personalNotes || "");
  };

  const handleSaveEdit = (id: string) => {
    onUpdateNotes(id, tempNotes);
    setEditingId(null);
  };

  return (
    <div id="resources-view" className="space-y-6 pb-12">
      <div>
        <h1 className="text-xl font-black text-white sm:text-2xl">
          Curated Standard Resources & Textbooks
        </h1>
        <p className="text-xs text-slate-400">
          Faculty-verified standard reference textbooks, high-yield video playlists, and NPTEL course recommendations for GATE CSE 2027.
        </p>
      </div>

      {/* Filter Row */}
      <div className="flex flex-wrap items-center gap-3 rounded-xl border border-slate-800 bg-slate-900/80 p-4">
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
          value={selectedType}
          onChange={(e) => setSelectedType(e.target.value)}
          className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-1.5 text-xs text-slate-200 focus:outline-none"
        >
          <option value="ALL">All Resource Formats</option>
          <option value="BOOK">Standard Books</option>
          <option value="PLAYLIST">Video Playlists</option>
          <option value="NPTEL">NPTEL Courses</option>
          <option value="PRACTICE_SET">PYQ Archives</option>
        </select>

        <span className="ml-auto text-xs text-slate-400">
          Showing {filtered.length} curated items
        </span>
      </div>

      {/* Resources Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {filtered.map((res) => {
          const sub = subjects.find((s) => s.id === res.subjectId);
          const isEditing = editingId === res.id;

          return (
            <div
              key={res.id}
              className={`flex flex-col justify-between rounded-xl border p-4 space-y-3 transition ${
                res.isCompleted
                  ? "border-emerald-900/40 bg-emerald-950/10"
                  : "border-slate-800 bg-slate-900/70 hover:border-slate-700"
              }`}
            >
              <div className="space-y-2">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className="rounded bg-cyan-500/20 px-2 py-0.5 font-mono text-[10px] font-bold text-cyan-300">
                      {sub?.code || res.subjectId}
                    </span>
                    <span className="rounded bg-slate-800 px-2 py-0.5 text-[10px] font-bold text-slate-300">
                      {res.resourceType}
                    </span>
                    {res.isFree && (
                      <span className="rounded bg-emerald-500/20 px-1.5 py-0.5 text-[10px] font-bold text-emerald-400">
                        FREE
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => onToggleCompleted(res.id)}
                      className={`rounded p-1 transition ${
                        res.isCompleted ? "text-emerald-400" : "text-slate-500 hover:text-slate-300"
                      }`}
                      title={res.isCompleted ? "Mark Incomplete" : "Mark Completed"}
                    >
                      <CheckCircle2 className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => onToggleBookmark(res.id)}
                      className={`rounded p-1 transition ${
                        res.isBookmarked ? "text-amber-400 fill-amber-400" : "text-slate-500 hover:text-slate-300"
                      }`}
                      title="Bookmark Resource"
                    >
                      <Bookmark className={`h-4 w-4 ${res.isBookmarked ? "fill-amber-400" : ""}`} />
                    </button>
                  </div>
                </div>

                <h3 className="text-sm font-bold text-white leading-snug">
                  {res.title}
                </h3>
                <span className="text-[11px] font-semibold text-slate-400">
                  By {res.provider}
                </span>

                <p className="text-xs text-slate-300 line-clamp-3">
                  {res.description}
                </p>

                {res.recommendedChapters && (
                  <div className="text-[11px] text-amber-300/90 font-medium">
                    ★ Key Chapters: {res.recommendedChapters}
                  </div>
                )}
              </div>

              {/* Personal Notes / Links */}
              <div className="space-y-2 border-t border-slate-800/80 pt-3">
                {isEditing ? (
                  <div className="space-y-2">
                    <input
                      type="text"
                      placeholder="Add personal study notes for this resource..."
                      value={tempNotes}
                      onChange={(e) => setTempNotes(e.target.value)}
                      className="w-full rounded border border-slate-700 bg-slate-950 px-2.5 py-1.5 text-xs text-slate-200 focus:outline-none"
                    />
                    <div className="flex justify-end gap-1.5">
                      <button
                        onClick={() => setEditingId(null)}
                        className="rounded px-2 py-1 text-[10px] text-slate-400 hover:text-white"
                      >
                        Cancel
                      </button>
                      <button
                        onClick={() => handleSaveEdit(res.id)}
                        className="rounded bg-cyan-500 px-2.5 py-1 text-[10px] font-bold text-slate-950 hover:bg-cyan-400"
                      >
                        Save Note
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between text-xs">
                    <button
                      onClick={() => handleStartEdit(res)}
                      className="flex items-center gap-1 text-[11px] text-slate-400 hover:text-cyan-300 transition"
                    >
                      <Edit3 className="h-3 w-3" />
                      <span>{res.personalNotes ? "Edit Note" : "Add Note"}</span>
                    </button>

                    <a
                      href={res.url}
                      target="_blank"
                      rel="noreferrer"
                      className="flex items-center gap-1 text-[11px] font-bold text-cyan-400 hover:underline"
                    >
                      <span>Open Link</span>
                      <ExternalLink className="h-3 w-3" />
                    </a>
                  </div>
                )}

                {res.personalNotes && !isEditing && (
                  <div className="rounded bg-slate-950/60 p-2 text-[11px] text-slate-300 italic border border-slate-800">
                    "{res.personalNotes}"
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
