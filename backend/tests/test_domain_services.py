import unittest
import math
from datetime import datetime, timezone
import sys
import os

# Add root directory to path for imports
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

from backend.app.services.evaluator import QuestionEvaluator
from backend.app.services.spaced_repetition import SpacedRepetitionService
from backend.app.services.next_action_engine import NextActionEngine

class TestQuestionEvaluator(unittest.TestCase):
    def test_mcq_evaluation_correct(self):
        is_corr, marks = QuestionEvaluator.evaluate(
            question_type="MCQ",
            correct_answer_str="B",
            user_answer_str="B",
            marks=2,
            negative_marks=0.66
        )
        self.assertTrue(is_corr)
        self.assertEqual(marks, 2.0)

    def test_mcq_evaluation_incorrect(self):
        is_corr, marks = QuestionEvaluator.evaluate(
            question_type="MCQ",
            correct_answer_str="B",
            user_answer_str="C",
            marks=2,
            negative_marks=0.66
        )
        self.assertFalse(is_corr)
        self.assertAlmostEqual(marks, -0.66, places=2)

    def test_msq_evaluation_exact_set(self):
        # In MSQ, candidate selects "A,C" and correct is "C,A" -> Correct, full marks
        is_corr, marks = QuestionEvaluator.evaluate(
            question_type="MSQ",
            correct_answer_str="A,C",
            user_answer_str="C, A",
            marks=2
        )
        self.assertTrue(is_corr)
        self.assertEqual(marks, 2.0)

    def test_msq_evaluation_partial_is_zero(self):
        # In GATE MSQ, selecting partial options (e.g. only A when correct is A,C) gives 0 marks
        is_corr, marks = QuestionEvaluator.evaluate(
            question_type="MSQ",
            correct_answer_str="A,C",
            user_answer_str="A",
            marks=2
        )
        self.assertFalse(is_corr)
        self.assertEqual(marks, 0.0)

    def test_nat_evaluation_within_tolerance(self):
        is_corr, marks = QuestionEvaluator.evaluate(
            question_type="NAT",
            correct_answer_str="2.5",
            user_answer_str="2.52",
            marks=2,
            tolerance_lower=2.45,
            tolerance_upper=2.55
        )
        self.assertTrue(is_corr)
        self.assertEqual(marks, 2.0)

class TestSpacedRepetitionSM2(unittest.TestCase):
    def test_sm2_failed_recall_resets_interval(self):
        reps, interval, ef, next_due = SpacedRepetitionService.calculate_sm2(
            rating=1, # Failed recall
            current_repetitions=3,
            current_interval_days=10,
            current_ease_factor=2.5
        )
        self.assertEqual(reps, 0)
        self.assertEqual(interval, 1)

    def test_sm2_successful_recall_expands_interval(self):
        reps, interval, ef, next_due = SpacedRepetitionService.calculate_sm2(
            rating=5, # Perfect recall
            current_repetitions=2,
            current_interval_days=6,
            current_ease_factor=2.5
        )
        self.assertEqual(reps, 3)
        self.assertTrue(interval > 6)
        self.assertTrue(ef >= 2.5)

class TestNextActionEngine(unittest.TestCase):
    def test_prioritizes_urgent_revisions_when_high(self):
        action = NextActionEngine.generate_next_action(
            due_revision_count=12,
            weak_topics=[{"id": "OS_DEADLOCK", "name": "Deadlocks", "accuracy": 50.0}],
            current_streak=5,
            recent_accuracy=60.0
        )
        self.assertEqual(action.action_type, "REVISION")
        self.assertEqual(action.urgency_level, "HIGH")

    def test_recommends_weak_topic_practice_when_revisions_low(self):
        action = NextActionEngine.generate_next_action(
            due_revision_count=3,
            weak_topics=[{"id": "OS_DEADLOCK", "name": "Deadlocks", "subject_id": "OS", "accuracy": 55.0}],
            current_streak=5,
            recent_accuracy=60.0
        )
        self.assertEqual(action.action_type, "PRACTICE_WEAK")
        self.assertIn("Deadlocks", action.title)

if __name__ == "__main__":
    unittest.main()
