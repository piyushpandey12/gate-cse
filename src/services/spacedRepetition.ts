import { Attempt, Difficulty, RevisionItem } from "../types";

export interface SrsScheduleResult {
  nextReviewDueDate: string;
  intervalDays: number;
  repetitionCount: number;
  easeFactor: number;
  isMastered: boolean;
  qualityRating: number;
}

export interface TopicAttentionAssessment {
  topicId: string;
  needsImmediateAttention: boolean;
  reason: string | null;
  urgencyScore: number; // 0.0 to 1.0
}

export function calculateQualityRating(
  attempt: Attempt,
  expectedSeconds = 180
): number {
  const timeSpent = attempt.timeSpentSeconds;
  const mistake = attempt.mistakeCategory;

  if (attempt.isCorrect) {
    if (timeSpent <= expectedSeconds * 0.75) return 5;
    if (timeSpent <= expectedSeconds * 1.5) return 4;
    return 3;
  } else {
    switch (mistake) {
      case "CALCULATION":
      case "SILLY_MISTAKE":
      case "TIME_PRESSURE":
        return 2;
      case "FORMULA":
      case "MEMORY":
        return 1;
      case "CONCEPTUAL":
      case "MISREAD":
      case "GUESS":
      case "OTHER":
      default:
        return 0;
    }
  }
}

export function calculateNextSchedule(
  existingItem: RevisionItem | null,
  attempt: Attempt,
  difficulty: Difficulty = "MEDIUM"
): SrsScheduleResult {
  const benchmarkTime =
    difficulty === "EASY" ? 90 : difficulty === "HARD" ? 240 : 180;

  const quality = calculateQualityRating(attempt, benchmarkTime);

  let easeFactor = existingItem?.easeFactor ?? 2.5;
  let repetitionCount = existingItem?.repetitionCount ?? 0;
  let intervalDays = existingItem?.intervalDays ?? 0;

  // SM-2 Ease Factor calculation
  const delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
  easeFactor = Math.max(1.3, easeFactor + delta);

  if (quality >= 3) {
    if (repetitionCount === 0) intervalDays = 1;
    else if (repetitionCount === 1) intervalDays = 3;
    else if (repetitionCount === 2) intervalDays = 7;
    else {
      intervalDays = Math.round(intervalDays * easeFactor);
      if (intervalDays > 60) intervalDays = 60;
    }
    repetitionCount += 1;
  } else {
    repetitionCount = 0;
    intervalDays = quality === 2 ? 2 : 1;
  }

  const d = new Date();
  d.setDate(d.getDate() + intervalDays);
  const nextReviewDueDate = d.toISOString().split("T")[0];

  const isMastered = repetitionCount >= 4 && easeFactor >= 2.2 && intervalDays >= 14;

  return {
    nextReviewDueDate,
    intervalDays,
    repetitionCount,
    easeFactor: Math.round(easeFactor * 100) / 100,
    isMastered,
    qualityRating: quality,
  };
}

export function assessTopicAttention(
  topicId: string,
  attempts: Attempt[],
  currentMasteryScore = 0
): TopicAttentionAssessment {
  if (attempts.length === 0) {
    return {
      topicId,
      needsImmediateAttention: true,
      reason: "Unattempted topic: No PYQs solved yet",
      urgencyScore: 0.65,
    };
  }

  const total = attempts.length;
  const correct = attempts.filter((a) => a.isCorrect).length;
  const accuracy = correct / total;

  const conceptualMistakes = attempts.filter(
    (a) =>
      a.mistakeCategory === "CONCEPTUAL" ||
      a.mistakeCategory === "FORMULA" ||
      a.mistakeCategory === "MEMORY"
  ).length;

  const calculationMistakes = attempts.filter(
    (a) =>
      a.mistakeCategory === "CALCULATION" ||
      a.mistakeCategory === "SILLY_MISTAKE"
  ).length;

  const recentAttempts = attempts.slice(-5);
  const recentIncorrect = recentAttempts.filter((a) => !a.isCorrect).length;

  if (accuracy < 0.4 && total >= 2) {
    return {
      topicId,
      needsImmediateAttention: true,
      reason: `Critical: Low accuracy (${Math.round(accuracy * 100)}%) across ${total} attempts`,
      urgencyScore: 0.95,
    };
  }

  if (conceptualMistakes >= 2) {
    return {
      topicId,
      needsImmediateAttention: true,
      reason: `Recurring concept & formula gaps (${conceptualMistakes} flagged)`,
      urgencyScore: 0.85,
    };
  }

  if (recentIncorrect >= 3) {
    return {
      topicId,
      needsImmediateAttention: true,
      reason: `Recent slump: ${recentIncorrect} of last ${recentAttempts.length} attempts missed`,
      urgencyScore: 0.8,
    };
  }

  if (accuracy < 0.6 && currentMasteryScore < 50) {
    return {
      topicId,
      needsImmediateAttention: true,
      reason: `Below target threshold: Mastery score ${Math.round(currentMasteryScore)}%`,
      urgencyScore: 0.7,
    };
  }

  if (calculationMistakes >= 3) {
    return {
      topicId,
      needsImmediateAttention: false,
      reason: `Careless calculation errors (${calculationMistakes} slips); revise NAT steps`,
      urgencyScore: 0.4,
    };
  }

  return {
    topicId,
    needsImmediateAttention: false,
    reason: `Healthy retention (${Math.round(accuracy * 100)}% accuracy)`,
    urgencyScore: Math.max(0, 1 - accuracy),
  };
}

export function calculateNextReview(
  quality: number,
  repetitionCount: number,
  easeFactor: number,
  intervalDays: number
): {
  nextReviewDueDate: string;
  intervalDays: number;
  repetitionCount: number;
  easeFactor: number;
  isMastered: boolean;
} {
  const delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
  let newEase = Math.max(1.3, easeFactor + delta);
  let newInterval = intervalDays;
  let newRep = repetitionCount;

  if (quality >= 3) {
    if (newRep === 0) newInterval = 1;
    else if (newRep === 1) newInterval = 3;
    else if (newRep === 2) newInterval = 7;
    else newInterval = Math.min(60, Math.round(newInterval * newEase));
    newRep += 1;
  } else {
    newRep = 0;
    newInterval = quality === 2 ? 2 : 1;
  }

  const d = new Date();
  d.setDate(d.getDate() + newInterval);
  const nextReviewDueDate = d.toISOString().split("T")[0];
  const isMastered = newRep >= 4 && newEase >= 2.2 && newInterval >= 14;

  return {
    nextReviewDueDate,
    intervalDays: newInterval,
    repetitionCount: newRep,
    easeFactor: Math.round(newEase * 100) / 100,
    isMastered,
  };
}

export function updateTopicMasteryAfterAttempt(
  topicId: string,
  currentMastery: any,
  topicAttempts: Attempt[]
) {
  const total = topicAttempts.length;
  const correct = topicAttempts.filter((a) => a.isCorrect).length;
  const accuracy = total > 0 ? Math.round((correct / total) * 100) : 0;
  const assessment = assessTopicAttention(topicId, topicAttempts, accuracy);

  let level: "NOVICE" | "DEVELOPING" | "STRONG" | "MASTERED" | "WEAK" = "NOVICE";
  if (accuracy >= 85 && total >= 5) level = "MASTERED";
  else if (accuracy >= 70 && total >= 3) level = "STRONG";
  else if (accuracy >= 50) level = "DEVELOPING";
  else if (total >= 2 && accuracy < 50) level = "WEAK";

  return {
    topicId,
    masteryScore: accuracy,
    level,
    attemptsCount: total,
    accuracy,
    needsImmediateAttention: assessment.needsImmediateAttention,
    attentionReason: assessment.reason,
    lastPracticedAt: Date.now(),
  };
}

