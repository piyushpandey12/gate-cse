import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json());

  // Lazy initialize GoogleGenAI client
  let aiClient: GoogleGenAI | null = null;
  function getGenAI(): GoogleGenAI | null {
    if (!aiClient && process.env.GEMINI_API_KEY) {
      aiClient = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
    }
    return aiClient;
  }

  // Health check
  app.get("/api/health", (_req, res) => {
    res.json({ status: "ok", timestamp: Date.now() });
  });

  // AI Tutor endpoint
  app.post("/api/gemini/tutor", async (req, res) => {
    const { query, mode, contextQuestion, solution } = req.body;

    if (!query) {
      return res.status(400).json({ error: "Query is required" });
    }

    const ai = getGenAI();
    if (!ai) {
      // Fallback message when API key is not configured
      let simulatedResponse = `🎓 **GATE CSE Expert Explanation**:\n\nRegarding: "${query}"\n\n`;
      if (mode === "HINT") {
        simulatedResponse += `💡 **Guiding Hint**: Think about the core invariants and worst-case boundary cases. Look closely at the recurrence equation or memory word size given in the problem statement.\n\nKey formula to recall: Check standard properties in Discrete Mathematics and Operating Systems.`;
      } else if (mode === "TRAPS") {
        simulatedResponse += `⚠️ **Key Exam Traps**:\n1. Beware of zero-indexing vs 1-indexing.\n2. In MSQs, every correct option must be chosen; no partial marks are awarded.\n3. Watch out for units (bits vs Bytes, microseconds vs nanoseconds).`;
      } else {
        simulatedResponse += `In standard GATE computer science problems, always formalize the system model first. Break the problem into inputs, standard theorems (Master theorem, Dijkstra's invariants, Paging equations), and systematically evaluate each option.`;
      }
      return res.json({ response: simulatedResponse });
    }

    try {
      let prompt = `You are a top-tier GATE Computer Science & Engineering (CSE) tutor and previous AIR 1 ranker.
Explain concepts with absolute mathematical rigor, clarity, and exam relevance.

User Query: ${query}
Mode: ${mode || "EXPLAIN"}
${contextQuestion ? `Context Question: ${contextQuestion}\n` : ""}
${solution ? `Known Solution / Key: ${solution}\n` : ""}

Guidelines:
- If Mode is "HINT", do NOT reveal the final answer. Ask guided Socratic questions to lead the student to deduce the theorem or formula.
- If Mode is "TRAPS", highlight the subtle traps where GATE candidates lose negative marks (e.g., bit vs byte confusion, strict inequalities, MSQ partial marks absence, floating point precision in NAT).
- Format mathematical formulas clearly using LaTeX expressions (e.g. $O(n \\log n)$, $\\Theta(n^2)$, or block formulas).
- Keep explanations concise, impactful, and easy to review before an exam.`;

      const response = await ai.models.generateContent({
        model: "gemini-2.5-flash",
        contents: prompt,
      });

      const text = response.text || "No response generated.";
      res.json({ response: text });
    } catch (error: any) {
      console.error("Gemini API error:", error);
      res.json({
        response: `🎓 **GATE CSE Concept Guide**:\n\nRegarding: "${query}"\n\n1. Identify the subject and sub-topic.\n2. Recall standard theorems (e.g., Chomsky Hierarchy, Relational Algebra projections, Pipelining speedup).\n3. Double check corner cases before marking final choices.`,
      });
    }
  });

  // Vite middleware for development vs static in production
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (_req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`GATE CSE 2027 server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
