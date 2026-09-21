# GATE CSE 2027 Preparation Platform

A comprehensive, adaptive GATE Computer Science & Engineering (CSE) preparation web platform rewritten from Android to React, TypeScript, Vite, and Express.

## Features

- **Syllabus Directory & Weightage Matrix**: Complete coverage of all 10 core GATE CSE subjects, engineering mathematics, and general aptitude.
- **PYQ Explorer**: Filter previous years' question papers by subject, topic, year (2015–2024), question type (MCQ, MSQ, NAT), and marks.
- **Practice Mode & Immediate Evaluation**: Interactive question solving with instant evaluation, mark calculation, detailed explanations, shortcut tricks, and trap alerts.
- **GATE CBT Exam Simulator**: Realistic Computer Based Test (CBT) environment mirroring the official TCS iON GATE exam interface with countdown timer, question palette, review flagging, and marking scheme.
- **Virtual Scientific Calculator**: Authentic GATE virtual calculator implementation featuring algebraic hierarchy, trigonometric, exponential, and memory functions.
- **Spaced Repetition System (SRS)**: SuperMemo SM-2 algorithm scheduling reviews for weak and mastered concepts with dynamic interval calculation.
- **Mistake Journal & Error Categorization**: Systematic logging of errors (conceptual, calculation, silly, time pressure, misread, formula trap) with actionable notes.
- **Revision Notebook & Formula Handbook**: KaTeX-powered high-yield formula sheets and micro-notes with pinning and search.
- **Study Planner & Focus Pomodoro**: Daily study checklist, milestone timeline to GATE 2027, and integrated focus timer.
- **Performance Analytics & Diagnostics**: Accuracy tracking, study time logging, test session metrics, and subject readiness breakdown.
- **Gemini AI GATE Tutor**: Server-side conversational AI tutor for step-by-step problem breakdowns, counter-examples, and concept explanations.

## Tech Stack

- **Frontend**: React 18, TypeScript, Tailwind CSS v4, Lucide React icons, KaTeX
- **Backend**: Node.js 22, Express, `@google/genai` SDK
- **Persistence**: LocalStorage client-side persistence with export and reset support

## Running Locally

1. Install dependencies:
   ```bash
   npm install
   ```

2. Configure environment:
   ```bash
   cp .env.example .env
   # Add your GEMINI_API_KEY to .env for AI Tutor features
   ```

3. Start development server:
   ```bash
   npm run dev
   ```
   Open `http://localhost:3000` in your browser.

4. Build for production:
   ```bash
   npm run build
   npm start
   ```
