import React, { useState, useEffect } from "react";
import {
  Calendar,
  CheckCircle2,
  Circle,
  Plus,
  Trash2,
  Play,
  Pause,
  RotateCcw,
  Clock,
  Award,
} from "lucide-react";
import { StudyTask, ExamEvent, Subject } from "../../types";

interface PlannerViewProps {
  tasks: StudyTask[];
  examEvents: ExamEvent[];
  subjects: Subject[];
  onToggleTask: (id: string) => void;
  onAddTask: (task: StudyTask) => void;
  onDeleteTask: (id: string) => void;
  onLogStudyTime: (minutes: number) => void;
}

export const PlannerView: React.FC<PlannerViewProps> = ({
  tasks,
  examEvents,
  subjects,
  onToggleTask,
  onAddTask,
  onDeleteTask,
  onLogStudyTime,
}) => {
  // Focus Pomodoro Timer State
  const [timerSeconds, setTimerSeconds] = useState<number>(25 * 60);
  const [isTimerRunning, setIsTimerRunning] = useState<boolean>(false);
  const [timerMode, setTimerMode] = useState<"FOCUS" | "BREAK">("FOCUS");

  // New task input
  const [newTaskTitle, setNewTaskTitle] = useState<string>("");
  const [newTaskSubject, setNewTaskSubject] = useState<string>(subjects[0]?.id || "DS");
  const [newTaskMinutes, setNewTaskMinutes] = useState<number>(45);

  useEffect(() => {
    let interval: any = null;
    if (isTimerRunning) {
      interval = setInterval(() => {
        setTimerSeconds((prev) => {
          if (prev <= 1) {
            clearInterval(interval);
            setIsTimerRunning(false);
            if (timerMode === "FOCUS") {
              onLogStudyTime(25);
              setTimerMode("BREAK");
              return 5 * 60;
            } else {
              setTimerMode("FOCUS");
              return 25 * 60;
            }
          }
          return prev - 1;
        });
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [isTimerRunning, timerMode, onLogStudyTime]);

  const handleToggleTimer = () => {
    setIsTimerRunning((r) => !r);
  };

  const handleResetTimer = (mode: "FOCUS" | "BREAK" = "FOCUS") => {
    setIsTimerRunning(false);
    setTimerMode(mode);
    setTimerSeconds(mode === "FOCUS" ? 25 * 60 : 5 * 60);
  };

  const formatTimer = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
  };

  const handleCreateTask = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;

    const task: StudyTask = {
      id: "TASK_" + Date.now(),
      title: newTaskTitle.trim(),
      subjectId: newTaskSubject,
      targetDateFormatted: "Today",
      targetDate: new Date().toISOString().split("T")[0],
      allocatedMinutes: newTaskMinutes,
      estimatedMinutes: newTaskMinutes,
      isCompleted: false,
      priority: "HIGH",
    };

    onAddTask(task);
    setNewTaskTitle("");
  };

  return (
    <div id="planner-view" className="space-y-6 pb-12">
      <div>
        <h1 className="text-xl font-black text-white sm:text-2xl">
          GATE Study Planner & Focus Engine
        </h1>
        <p className="text-xs text-slate-400">
          Plan daily revision blocks, track milestones to February 2027, and stay in flow with the integrated Pomodoro focus timer.
        </p>
      </div>

      {/* Focus Timer & Milestone Split */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Pomodoro Focus Timer (1 col) */}
        <div className="rounded-2xl border border-slate-800 bg-gradient-to-b from-slate-900 via-slate-900 to-cyan-950/20 p-6 flex flex-col items-center justify-between text-center space-y-4 shadow-lg">
          <div className="space-y-1">
            <span className="rounded-full border border-cyan-500/30 bg-cyan-500/10 px-3 py-1 text-[10px] font-bold text-cyan-300 uppercase tracking-wider">
              {timerMode === "FOCUS" ? "Deep Work Session" : "Short Recharging Break"}
            </span>
            <h3 className="text-sm font-bold text-white">Pomodoro Focus Timer</h3>
          </div>

          <div className="my-3 font-mono text-5xl font-black text-cyan-400 drop-shadow-[0_0_15px_rgba(6,182,212,0.3)]">
            {formatTimer(timerSeconds)}
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleToggleTimer}
              className="flex items-center gap-2 rounded-xl bg-cyan-500 px-6 py-2.5 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition shadow"
            >
              {isTimerRunning ? <Pause className="h-4 w-4 fill-slate-950" /> : <Play className="h-4 w-4 fill-slate-950" />}
              <span>{isTimerRunning ? "Pause" : "Start Focus"}</span>
            </button>
            <button
              onClick={() => handleResetTimer("FOCUS")}
              className="rounded-xl border border-slate-700 bg-slate-800 p-2.5 text-slate-400 hover:text-white transition"
              title="Reset to 25m"
            >
              <RotateCcw className="h-4 w-4" />
            </button>
          </div>

          <div className="flex gap-2 text-[10px] font-mono text-slate-400">
            <button
              onClick={() => handleResetTimer("FOCUS")}
              className={`px-2 py-1 rounded ${timerMode === "FOCUS" ? "bg-cyan-500/20 text-cyan-300" : "hover:text-white"}`}
            >
              25m Focus
            </button>
            <button
              onClick={() => handleResetTimer("BREAK")}
              className={`px-2 py-1 rounded ${timerMode === "BREAK" ? "bg-cyan-500/20 text-cyan-300" : "hover:text-white"}`}
            >
              5m Break
            </button>
          </div>
        </div>

        {/* Milestone Timeline (2 cols) */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 space-y-4 lg:col-span-2">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-cyan-400" />
              <h3 className="text-sm font-bold text-white">Official GATE 2027 Timeline Milestones</h3>
            </div>
            <span className="text-[11px] text-slate-400 font-mono">Exam Target: Feb 2027</span>
          </div>

          <div className="space-y-3">
            {examEvents.map((event) => (
              <div
                key={event.id}
                className={`flex items-center justify-between rounded-xl border p-3.5 text-xs transition ${
                  event.isMainExamDate
                    ? "border-cyan-500/50 bg-cyan-950/20 text-cyan-100"
                    : "border-slate-800 bg-slate-950/60 text-slate-300"
                }`}
              >
                <div className="space-y-0.5">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-white">{event.eventName}</span>
                    {event.isMainExamDate && (
                      <span className="rounded bg-cyan-500/30 px-1.5 py-0.5 text-[9px] font-bold text-cyan-300">
                        FINAL TARGET
                      </span>
                    )}
                  </div>
                  <p className="text-[11px] text-slate-400">{event.description}</p>
                </div>

                <div className="text-right font-mono shrink-0 ml-3">
                  <div className="font-bold text-cyan-300">{event.eventDateFormatted}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Daily Study Tasks Tracker */}
      <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 space-y-4">
        <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
          Daily Study Checklist ({tasks.filter((t) => t.isCompleted).length} / {tasks.length} Completed)
        </h3>

        {/* Add task form */}
        <form onSubmit={handleCreateTask} className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <input
            type="text"
            placeholder="Add new study goal (e.g. Solve 10 Pointers PYQs)..."
            value={newTaskTitle}
            onChange={(e) => setNewTaskTitle(e.target.value)}
            className="flex-1 rounded-xl border border-slate-800 bg-slate-950 px-3.5 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />

          <select
            value={newTaskSubject}
            onChange={(e) => setNewTaskSubject(e.target.value)}
            className="rounded-xl border border-slate-800 bg-slate-950 px-3 py-2 text-xs text-slate-200 focus:outline-none"
          >
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>
                {s.code}
              </option>
            ))}
          </select>

          <button
            type="submit"
            className="flex items-center justify-center gap-1.5 rounded-xl bg-cyan-500 px-4 py-2 text-xs font-bold text-slate-950 hover:bg-cyan-400 transition"
          >
            <Plus className="h-4 w-4" />
            <span>Add Task</span>
          </button>
        </form>

        {/* Task items list */}
        <div className="space-y-2 pt-2">
          {tasks.map((task) => {
            const sub = subjects.find((s) => s.id === task.subjectId);

            return (
              <div
                key={task.id}
                className={`flex items-center justify-between rounded-xl border p-3 text-xs transition ${
                  task.isCompleted
                    ? "border-emerald-900/40 bg-emerald-950/10 text-slate-400"
                    : "border-slate-800 bg-slate-950/60 text-slate-200"
                }`}
              >
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => onToggleTask(task.id)}
                    className={`transition ${task.isCompleted ? "text-emerald-400" : "text-slate-600 hover:text-slate-400"}`}
                  >
                    {task.isCompleted ? (
                      <CheckCircle2 className="h-5 w-5" />
                    ) : (
                      <Circle className="h-5 w-5" />
                    )}
                  </button>

                  <div className="space-y-0.5">
                    <span className={task.isCompleted ? "line-through text-slate-500 font-medium" : "font-bold text-white"}>
                      {task.title}
                    </span>
                    <div className="flex items-center gap-2 text-[10px] text-slate-400">
                      <span className="rounded bg-slate-800 px-1.5 py-0.5 font-mono">{sub?.code || task.subjectId}</span>
                      <span>Est: {task.estimatedMinutes ?? task.allocatedMinutes ?? 30}m</span>
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => onDeleteTask(task.id)}
                  className="rounded p-1 text-slate-600 hover:text-rose-400 transition"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
