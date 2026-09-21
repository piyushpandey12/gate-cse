import React, { useState } from "react";
import { X, RotateCcw, Copy, Check } from "lucide-react";

interface VirtualCalculatorProps {
  onClose: () => void;
  isOpen?: boolean;
}

export const VirtualCalculator: React.FC<VirtualCalculatorProps> = ({ onClose, isOpen = true }) => {
  if (!isOpen) return null;
  const [display, setDisplay] = useState("0");
  const [memory, setMemory] = useState<number>(0);
  const [copied, setCopied] = useState(false);

  const handleDigit = (digit: string) => {
    setDisplay((prev) => (prev === "0" || prev === "Error" ? digit : prev + digit));
  };

  const handleOp = (op: string) => {
    setDisplay((prev) => prev + " " + op + " ");
  };

  const handleClear = () => {
    setDisplay("0");
  };

  const handleBackspace = () => {
    setDisplay((prev) => (prev.length > 1 ? prev.slice(0, -1) : "0"));
  };

  const handleEquals = () => {
    try {
      // Safe sanitized arithmetic evaluation
      const sanitized = display
        .replace(/×/g, "*")
        .replace(/÷/g, "/")
        .replace(/\^/g, "**")
        .replace(/π/g, `${Math.PI}`)
        .replace(/e/g, `${Math.E}`);

      // eslint-disable-next-line no-eval
      const result = Function(`"use strict"; return (${sanitized})`)();
      const rounded = Math.round(result * 1e8) / 1e8;
      setDisplay(String(rounded));
    } catch {
      setDisplay("Error");
    }
  };

  const handleSci = (fn: string) => {
    try {
      const val = parseFloat(display);
      if (isNaN(val)) return;
      let res = 0;
      switch (fn) {
        case "sin":
          res = Math.sin((val * Math.PI) / 180);
          break;
        case "cos":
          res = Math.cos((val * Math.PI) / 180);
          break;
        case "tan":
          res = Math.tan((val * Math.PI) / 180);
          break;
        case "ln":
          res = Math.log(val);
          break;
        case "log":
          res = Math.log10(val);
          break;
        case "sqrt":
          res = Math.sqrt(val);
          break;
        case "sqr":
          res = Math.pow(val, 2);
          break;
        case "inv":
          res = 1 / val;
          break;
        case "fact":
          res = factorial(Math.floor(val));
          break;
        default:
          return;
      }
      setDisplay(String(Math.round(res * 1e8) / 1e8));
    } catch {
      setDisplay("Error");
    }
  };

  const factorial = (n: number): number => {
    if (n < 0) return NaN;
    if (n <= 1) return 1;
    let acc = 1;
    for (let i = 2; i <= Math.min(n, 20); i++) acc *= i;
    return acc;
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(display);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div
      id="gate-virtual-calculator"
      className="fixed bottom-6 right-6 z-50 w-84 rounded-xl border border-slate-700 bg-slate-900/95 p-4 shadow-2xl backdrop-blur-md"
    >
      <div className="flex items-center justify-between border-b border-slate-800 pb-2 mb-3">
        <div className="flex items-center gap-2">
          <span className="h-2.5 w-2.5 rounded-full bg-cyan-400 animate-pulse" />
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200">
            GATE Virtual Calculator
          </h4>
        </div>
        <div className="flex items-center gap-1">
          <button
            id="calc-copy-btn"
            onClick={handleCopy}
            className="rounded p-1 text-slate-400 hover:bg-slate-800 hover:text-slate-200 transition"
            title="Copy value"
          >
            {copied ? <Check className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
          </button>
          <button
            id="calc-close-btn"
            onClick={onClose}
            className="rounded p-1 text-slate-400 hover:bg-slate-800 hover:text-rose-400 transition"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Screen */}
      <div className="relative mb-3 rounded-lg border border-slate-700/80 bg-slate-950 p-2.5 text-right font-mono">
        <div className="text-[10px] text-slate-400">
          {memory !== 0 ? `M: ${memory}` : ""}
        </div>
        <div className="overflow-x-auto text-xl font-bold text-cyan-300">
          {display}
        </div>
      </div>

      {/* Calculator Buttons Grid */}
      <div className="grid grid-cols-5 gap-1.5 text-xs font-semibold">
        {/* Scientific Row 1 */}
        <button onClick={() => handleSci("sin")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">sin</button>
        <button onClick={() => handleSci("cos")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">cos</button>
        <button onClick={() => handleSci("tan")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">tan</button>
        <button onClick={() => handleDigit("π")} className="rounded bg-slate-800 py-1.5 text-cyan-400 hover:bg-slate-700">π</button>
        <button onClick={handleBackspace} className="rounded bg-rose-950/60 text-rose-300 py-1.5 hover:bg-rose-900/60">⌫</button>

        {/* Scientific Row 2 */}
        <button onClick={() => handleSci("ln")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">ln</button>
        <button onClick={() => handleSci("log")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">log</button>
        <button onClick={() => handleSci("sqrt")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">√x</button>
        <button onClick={() => handleSci("sqr")} className="rounded bg-slate-800 py-1.5 text-slate-300 hover:bg-slate-700">x²</button>
        <button onClick={handleClear} className="rounded bg-amber-950/60 text-amber-300 py-1.5 hover:bg-amber-900/60">C</button>

        {/* Numbers & Ops Row 1 */}
        <button onClick={() => handleDigit("7")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">7</button>
        <button onClick={() => handleDigit("8")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">8</button>
        <button onClick={() => handleDigit("9")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">9</button>
        <button onClick={() => handleOp("/")} className="rounded bg-cyan-950/50 text-cyan-300 py-2 hover:bg-cyan-900/50">÷</button>
        <button onClick={() => handleSci("inv")} className="rounded bg-slate-800 py-2 text-slate-300 hover:bg-slate-700">1/x</button>

        {/* Numbers & Ops Row 2 */}
        <button onClick={() => handleDigit("4")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">4</button>
        <button onClick={() => handleDigit("5")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">5</button>
        <button onClick={() => handleDigit("6")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">6</button>
        <button onClick={() => handleOp("*")} className="rounded bg-cyan-950/50 text-cyan-300 py-2 hover:bg-cyan-900/50">×</button>
        <button onClick={() => handleSci("fact")} className="rounded bg-slate-800 py-2 text-slate-300 hover:bg-slate-700">n!</button>

        {/* Numbers & Ops Row 3 */}
        <button onClick={() => handleDigit("1")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">1</button>
        <button onClick={() => handleDigit("2")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">2</button>
        <button onClick={() => handleDigit("3")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">3</button>
        <button onClick={() => handleOp("-")} className="rounded bg-cyan-950/50 text-cyan-300 py-2 hover:bg-cyan-900/50">−</button>
        <button onClick={() => handleOp("^")} className="rounded bg-slate-800 py-2 text-slate-300 hover:bg-slate-700">xʸ</button>

        {/* Numbers & Ops Row 4 */}
        <button onClick={() => handleDigit("0")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">0</button>
        <button onClick={() => handleDigit(".")} className="rounded bg-slate-850 py-2 text-slate-100 hover:bg-slate-800">.</button>
        <button onClick={() => handleDigit("e")} className="rounded bg-slate-800 py-2 text-cyan-400 hover:bg-slate-700">e</button>
        <button onClick={() => handleOp("+")} className="rounded bg-cyan-950/50 text-cyan-300 py-2 hover:bg-cyan-900/50">+</button>
        <button onClick={handleEquals} className="rounded bg-cyan-500 font-bold text-slate-950 py-2 hover:bg-cyan-400 shadow-md">=</button>
      </div>

      {/* Memory keys */}
      <div className="mt-2.5 flex items-center justify-between border-t border-slate-800 pt-2 text-[10px] text-slate-400">
        <button onClick={() => setMemory(0)} className="hover:text-cyan-300">MC</button>
        <button onClick={() => setDisplay(String(memory))} className="hover:text-cyan-300">MR</button>
        <button onClick={() => setMemory(parseFloat(display) || 0)} className="hover:text-cyan-300">MS</button>
        <button onClick={() => setMemory((m) => m + (parseFloat(display) || 0))} className="hover:text-cyan-300">M+</button>
        <button onClick={() => setMemory((m) => m - (parseFloat(display) || 0))} className="hover:text-cyan-300">M-</button>
      </div>
    </div>
  );
};
