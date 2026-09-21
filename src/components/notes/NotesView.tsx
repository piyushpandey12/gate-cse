import React, { useState } from "react";
import {
  FileText,
  Plus,
  Pin,
  Trash2,
  Edit2,
  Check,
  Search,
  Tag,
} from "lucide-react";
import { Note, Subject } from "../../types";

interface NotesViewProps {
  notes: Note[];
  subjects: Subject[];
  onSaveNote: (note: Note) => void;
  onDeleteNote: (id: string) => void;
  onTogglePin: (id: string) => void;
}

export const NotesView: React.FC<NotesViewProps> = ({
  notes,
  subjects,
  onSaveNote,
  onDeleteNote,
  onTogglePin,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>("ALL");
  const [searchQuery, setSearchQuery] = useState<string>("");

  // Create / Edit modal state
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [title, setTitle] = useState<string>("");
  const [subjectId, setSubjectId] = useState<string>(subjects[0]?.id || "DS");
  const [content, setContent] = useState<string>("");
  const [tags, setTags] = useState<string>("");

  const filtered = notes.filter((n) => {
    if (selectedSubject !== "ALL" && n.subjectId !== selectedSubject) return false;
    if (
      searchQuery &&
      !n.title.toLowerCase().includes(searchQuery.toLowerCase()) &&
      !n.content.toLowerCase().includes(searchQuery.toLowerCase())
    ) {
      return false;
    }
    return true;
  });

  const sortedNotes = [...filtered].sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1;
    if (!a.isPinned && b.isPinned) return 1;
    return b.updatedAt - a.updatedAt;
  });

  const handleOpenCreate = () => {
    setEditId(null);
    setTitle("");
    setSubjectId(subjects[0]?.id || "DS");
    setContent("");
    setTags("");
    setIsEditing(true);
  };

  const handleOpenEdit = (n: Note) => {
    setEditId(n.id);
    setTitle(n.title);
    setSubjectId(n.subjectId);
    setContent(n.content);
    setTags(Array.isArray(n.tags) ? n.tags.join(", ") : (n.tags || ""));
    setIsEditing(true);
  };

  const handleSave = () => {
    if (!title.trim()) return;

    const newNote: Note = {
      id: editId || "NOTE_" + Date.now(),
      subjectId,
      title: title.trim(),
      content: content.trim(),
      tags: tags
        .split(",")
        .map((t) => t.trim())
        .filter(Boolean),
      createdAt: editId
        ? notes.find((n) => n.id === editId)?.createdAt || Date.now()
        : Date.now(),
      updatedAt: Date.now(),
      isPinned: editId ? notes.find((n) => n.id === editId)?.isPinned || false : false,
    };

    onSaveNote(newNote);
    setIsEditing(false);
  };

  return (
    <div id="notes-view" className="space-y-6 pb-12">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-xl font-black text-white sm:text-2xl">
            GATE Revision Notebook & Micro-Summaries
          </h1>
          <p className="text-xs text-slate-400">
            Keep your own fast-revision points, key definitions, and corner-case exceptions organized by subject.
          </p>
        </div>

        <button
          onClick={handleOpenCreate}
          className="flex items-center gap-1.5 rounded-xl bg-cyan-500 px-4 py-2.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition shadow"
        >
          <Plus className="h-4 w-4" />
          <span>New Study Note</span>
        </button>
      </div>

      {/* Filter & Search */}
      <div className="flex flex-col gap-3 rounded-2xl border border-slate-800 bg-slate-900/80 p-4 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search notes by keyword..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full rounded-xl border border-slate-800 bg-slate-950 pl-9 pr-3.5 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none"
          />
        </div>

        <select
          value={selectedSubject}
          onChange={(e) => setSelectedSubject(e.target.value)}
          className="rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-xs text-slate-200 focus:outline-none"
        >
          <option value="ALL">All Subjects</option>
          {subjects.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name} ({s.code})
            </option>
          ))}
        </select>
      </div>

      {/* Notes Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {sortedNotes.map((note) => {
          const sub = subjects.find((s) => s.id === note.subjectId);

          return (
            <div
              key={note.id}
              className={`flex flex-col justify-between rounded-xl border p-4 space-y-3 transition ${
                note.isPinned
                  ? "border-cyan-500/50 bg-slate-900 shadow-md"
                  : "border-slate-800 bg-slate-900/60 hover:border-slate-700"
              }`}
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="rounded bg-cyan-500/20 px-2 py-0.5 font-mono text-[10px] font-bold text-cyan-300">
                    {sub?.code || note.subjectId}
                  </span>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => onTogglePin(note.id)}
                      className={`rounded p-1 transition ${
                        note.isPinned ? "text-cyan-400" : "text-slate-500 hover:text-slate-300"
                      }`}
                      title={note.isPinned ? "Unpin Note" : "Pin Note"}
                    >
                      <Pin className={`h-3.5 w-3.5 ${note.isPinned ? "fill-cyan-400" : ""}`} />
                    </button>
                    <button
                      onClick={() => handleOpenEdit(note)}
                      className="rounded p-1 text-slate-500 hover:text-slate-200 transition"
                      title="Edit Note"
                    >
                      <Edit2 className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => onDeleteNote(note.id)}
                      className="rounded p-1 text-slate-500 hover:text-rose-400 transition"
                      title="Delete Note"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>

                <h3 className="text-sm font-bold text-white leading-snug">
                  {note.title}
                </h3>

                <p className="text-xs text-slate-300 whitespace-pre-line line-clamp-4 leading-relaxed">
                  {note.content}
                </p>
              </div>

              {/* Tags */}
              {(() => {
                const tagList: string[] = Array.isArray(note.tags)
                  ? note.tags
                  : typeof note.tags === "string"
                  ? note.tags.split(",").map((t: string) => t.trim()).filter(Boolean)
                  : [];
                if (tagList.length === 0) return null;
                return (
                  <div className="flex flex-wrap gap-1 border-t border-slate-800/80 pt-2.5">
                    {tagList.map((t: string) => (
                      <span
                        key={t}
                        className="rounded bg-slate-800 px-1.5 py-0.5 text-[10px] text-slate-400 font-mono"
                      >
                        #{t}
                      </span>
                    ))}
                  </div>
                );
              })()}
            </div>
          );
        })}
      </div>

      {/* Editor Modal */}
      {isEditing && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <div className="w-full max-w-lg rounded-2xl border border-slate-800 bg-slate-900 p-6 space-y-4 shadow-2xl">
            <h3 className="text-base font-bold text-white">
              {editId ? "Edit Study Note" : "Create New Study Note"}
            </h3>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-400 mb-1 font-semibold">Title:</label>
                <input
                  type="text"
                  placeholder="e.g. Master Theorem 3 Cases with Edge Traps"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-semibold">Subject:</label>
                <select
                  value={subjectId}
                  onChange={(e) => setSubjectId(e.target.value)}
                  className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-slate-200 focus:outline-none"
                >
                  {subjects.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.code})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-semibold">Note Content:</label>
                <textarea
                  rows={6}
                  placeholder="Write your summary, bullet points, or edge cases..."
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="w-full rounded-xl border border-slate-800 bg-slate-950 p-3 text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-semibold">Tags (comma-separated):</label>
                <input
                  type="text"
                  placeholder="e.g. algorithms, recurrence, high_yield"
                  value={tags}
                  onChange={(e) => setTags(e.target.value)}
                  className="w-full rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-slate-200 focus:outline-none"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setIsEditing(false)}
                className="rounded-xl border border-slate-700 bg-slate-800 px-4 py-2 text-xs font-bold text-slate-300 hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                className="rounded-xl bg-cyan-500 px-4 py-2 text-xs font-bold text-slate-950 hover:bg-cyan-400"
              >
                Save Note
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
