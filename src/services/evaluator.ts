import { Question } from "../types";

export interface QuestionEvaluationResult {
  isCorrect: boolean;
  marksAwarded: number;
  negativeMarksApplied: number;
  normalizedUserAnswer: string;
  expectedAnswer: string;
}

export function evaluateQuestion(
  question: Question,
  rawUserAnswer: string
): QuestionEvaluationResult {
  const trimmedAnswer = rawUserAnswer.trim();
  const expected = question.correctAnswers.trim();

  let isCorrect = false;

  switch (question.questionType) {
    case "MCQ": {
      isCorrect = trimmedAnswer.toUpperCase() === expected.toUpperCase();
      break;
    }
    case "MSQ": {
      const userSet = new Set(
        trimmedAnswer
          .split(/[,;\s]+/)
          .map((s) => s.trim().toUpperCase())
          .filter(Boolean)
      );
      const expectedSet = new Set(
        expected
          .split(/[,;\s]+/)
          .map((s) => s.trim().toUpperCase())
          .filter(Boolean)
      );

      isCorrect =
        userSet.size > 0 &&
        userSet.size === expectedSet.size &&
        [...userSet].every((item) => expectedSet.has(item));
      break;
    }
    case "NAT": {
      isCorrect = evaluateNatAnswer(trimmedAnswer, expected);
      break;
    }
  }

  // GATE marking scheme:
  // MCQ: negative marks apply (typically 1/3 of marks: 0.33 or 0.66)
  // MSQ & NAT: zero negative marking
  const negativeMarks = !isCorrect
    ? question.questionType === "MCQ"
      ? Math.abs(question.negativeMarks)
      : 0
    : 0;

  const marksAwarded = isCorrect ? question.marks : -negativeMarks;

  return {
    isCorrect,
    marksAwarded: Math.round(marksAwarded * 100) / 100,
    negativeMarksApplied: Math.round(negativeMarks * 100) / 100,
    normalizedUserAnswer: trimmedAnswer,
    expectedAnswer: expected,
  };
}

function evaluateNatAnswer(userAnswer: string, expectedAnswer: string): boolean {
  const userVal = parseFloat(userAnswer.replace(/,/g, ""));
  if (isNaN(userVal)) return false;

  // Check range delimiters like ":", " to ", " - "
  const rangeDelimiters = [":", " to ", " - "];
  for (const delimiter of rangeDelimiters) {
    if (expectedAnswer.toLowerCase().includes(delimiter.toLowerCase())) {
      const parts = expectedAnswer.split(new RegExp(delimiter, "i"));
      if (parts.length === 2) {
        const minVal = parseFloat(parts[0].trim().replace(/,/g, ""));
        const maxVal = parseFloat(parts[1].trim().replace(/,/g, ""));
        if (!isNaN(minVal) && !isNaN(maxVal)) {
          const lower = Math.min(minVal, maxVal) - 1e-4;
          const upper = Math.max(minVal, maxVal) + 1e-4;
          return userVal >= lower && userVal <= upper;
        }
      }
    }
  }

  // Single numeric target with 0.015 tolerance
  const targetVal = parseFloat(expectedAnswer.replace(/,/g, ""));
  if (!isNaN(targetVal)) {
    return Math.abs(userVal - targetVal) <= 0.015;
  }

  return userAnswer.trim().toLowerCase() === expectedAnswer.trim().toLowerCase();
}
