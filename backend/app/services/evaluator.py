from typing import Tuple, Optional
import math

class QuestionEvaluator:
    """
    Authoritative evaluation engine adhering strictly to GATE examination standards:
    - MCQ: Single selection. Full marks for correct, -1/3 negative marks for incorrect.
    - MSQ: Multiple selections. ALL correct choices must be selected, NO incorrect choice selected. NO negative marks, NO partial marks.
    - NAT: Numerical Answer Type. Evaluated against float numerical range [tolerance_lower, tolerance_upper]. NO negative marks.
    """
    
    @staticmethod
    def evaluate(
        question_type: str,
        correct_answer_str: str,
        user_answer_str: str,
        marks: int = 1,
        negative_marks: float = 0.33,
        tolerance_lower: Optional[float] = None,
        tolerance_upper: Optional[float] = None
    ) -> Tuple[bool, float]:
        q_type = question_type.upper().strip()
        user_clean = user_answer_str.strip()
        
        if not user_clean:
            return False, 0.0
            
        if q_type == "MCQ":
            is_correct = user_clean.upper() == correct_answer_str.strip().upper()
            awarded = float(marks) if is_correct else -float(negative_marks)
            return is_correct, awarded
            
        elif q_type == "MSQ":
            # Example: correct = "A,C", user = "C,A" -> both set to {'A', 'C'}
            correct_set = {opt.strip().upper() for opt in correct_answer_str.split(",") if opt.strip()}
            user_set = {opt.strip().upper() for opt in user_clean.split(",") if opt.strip()}
            
            is_correct = (correct_set == user_set)
            # GATE rule for MSQ: No negative marks, no partial marks
            awarded = float(marks) if is_correct else 0.0
            return is_correct, awarded
            
        elif q_type == "NAT":
            try:
                user_val = float(user_clean)
                if tolerance_lower is not None and tolerance_upper is not None:
                    is_correct = (tolerance_lower <= user_val <= tolerance_upper)
                else:
                    correct_val = float(correct_answer_str.strip())
                    is_correct = math.isclose(user_val, correct_val, rel_tol=1e-3, abs_tol=1e-3)
                    
                # GATE rule for NAT: No negative marks
                awarded = float(marks) if is_correct else 0.0
                return is_correct, awarded
            except ValueError:
                return False, 0.0
                
        # Default fallback
        is_correct = user_clean.lower() == correct_answer_str.strip().lower()
        awarded = float(marks) if is_correct else 0.0
        return is_correct, awarded
