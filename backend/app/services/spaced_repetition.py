from datetime import datetime, timedelta, timezone
from typing import Tuple

class SpacedRepetitionService:
    """
    SuperMemo SM-2 spaced repetition calculation engine.
    Rating scale: 0 (blackout) to 5 (perfect recall).
    Passing rating threshold is >= 3.
    """
    
    @staticmethod
    def calculate_sm2(
        rating: int,
        current_repetitions: int,
        current_interval_days: int,
        current_ease_factor: float
    ) -> Tuple[int, int, float, datetime]:
        # Clamp rating between 0 and 5
        q = max(0, min(5, rating))
        
        # Calculate new ease factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        new_ef = current_ease_factor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        if new_ef < 1.3:
            new_ef = 1.3
            
        if q < 3:
            # Failed recall: reset repetitions to 0 and interval to 1 day
            new_reps = 0
            new_interval = 1
        else:
            # Successful recall
            if current_repetitions == 0:
                new_interval = 1
            elif current_repetitions == 1:
                new_interval = 6
            else:
                new_interval = int(round(current_interval_days * new_ef))
                
            new_reps = current_repetitions + 1
            
        next_due = datetime.now(timezone.utc) + timedelta(days=new_interval)
        return new_reps, new_interval, round(new_ef, 2), next_due
