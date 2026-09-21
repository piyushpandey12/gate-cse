#!/usr/bin/env python3
"""
Database seeding script for development environment.
Populates PostgreSQL with initial GATE CSE reference data.

Usage:
    python -m backend.scripts.seed_db
"""
import asyncio
import uuid
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", ".."))

from backend.app.db.session import AsyncSessionLocal
from backend.app.models.syllabus import Subject, Topic
from backend.app.models.question import Question
from backend.app.models.resource import Resource
from backend.app.models.revision import Flashcard, Formula


SUBJECTS = [
    {"id": "OS", "code": "OS", "name": "Operating Systems", "weightage_percent": 9.5, "display_order": 1, "color_hex": "#06B6D4", "icon_name": "Memory", "total_pyq_count": 180},
    {"id": "TOC", "code": "TOC", "name": "Theory of Computation", "weightage_percent": 8.0, "display_order": 2, "color_hex": "#8B5CF6", "icon_name": "AccountTree", "total_pyq_count": 150},
    {"id": "ALGO", "code": "ALGO", "name": "Algorithms", "weightage_percent": 10.0, "display_order": 3, "color_hex": "#F59E0B", "icon_name": "TrendingUp", "total_pyq_count": 200},
    {"id": "DS", "code": "DS", "name": "Data Structures", "weightage_percent": 8.5, "display_order": 4, "color_hex": "#10B981", "icon_name": "AccountTree", "total_pyq_count": 160},
    {"id": "DBMS", "code": "DBMS", "name": "Database Management Systems", "weightage_percent": 8.0, "display_order": 5, "color_hex": "#EF4444", "icon_name": "Storage", "total_pyq_count": 150},
    {"id": "CN", "code": "CN", "name": "Computer Networks", "weightage_percent": 8.5, "display_order": 6, "color_hex": "#3B82F6", "icon_name": "Wifi", "total_pyq_count": 160},
    {"id": "COA", "code": "COA", "name": "Computer Organization & Architecture", "weightage_percent": 7.0, "display_order": 7, "color_hex": "#EC4899", "icon_name": "Memory", "total_pyq_count": 130},
    {"id": "CD", "code": "CD", "name": "Compiler Design", "weightage_percent": 6.0, "display_order": 8, "color_hex": "#F97316", "icon_name": "Code", "total_pyq_count": 110},
    {"id": "C", "code": "C", "name": "Programming in C", "weightage_percent": 5.0, "display_order": 9, "color_hex": "#6366F1", "icon_name": "Code", "total_pyq_count": 90},
    {"id": "DL", "code": "DL", "name": "Digital Logic", "weightage_percent": 5.5, "display_order": 10, "color_hex": "#14B8A6", "icon_name": "DeveloperBoard", "total_pyq_count": 100},
    {"id": "EM", "code": "EM", "name": "Engineering Mathematics", "weightage_percent": 13.0, "display_order": 11, "color_hex": "#A855F7", "icon_name": "Calculate", "total_pyq_count": 250},
    {"id": "DM", "code": "DM", "name": "Discrete Mathematics", "weightage_percent": 7.5, "display_order": 12, "color_hex": "#0EA5E9", "icon_name": "Functions", "total_pyq_count": 140},
    {"id": "GA", "code": "GA", "name": "General Aptitude", "weightage_percent": 15.0, "display_order": 13, "color_hex": "#84CC16", "icon_name": "School", "total_pyq_count": 300},
]

TOPICS = [
    {"id": "OS_PROCESS", "subject_id": "OS", "name": "Process Management", "difficulty": "MEDIUM", "pyq_count": 40, "frequency_score": 1.2, "display_order": 1, "summary": "Process states, scheduling algorithms, IPC, threads, deadlocks"},
    {"id": "OS_MEMORY", "subject_id": "OS", "name": "Memory Management", "difficulty": "HARD", "pyq_count": 35, "frequency_score": 1.1, "display_order": 2, "summary": "Paging, segmentation, virtual memory, page replacement"},
    {"id": "OS_DEADLOCK", "subject_id": "OS", "name": "Deadlocks", "difficulty": "MEDIUM", "pyq_count": 25, "frequency_score": 1.0, "display_order": 3, "summary": "Banker's algorithm, resource allocation, deadlock prevention"},
    {"id": "OS_FILE", "subject_id": "OS", "name": "File Systems", "difficulty": "EASY", "pyq_count": 20, "frequency_score": 0.8, "display_order": 4, "summary": "File allocation, directory structure, disk scheduling"},
    {"id": "TOC_DFA", "subject_id": "TOC", "name": "Finite Automata & Regular Languages", "difficulty": "MEDIUM", "pyq_count": 45, "frequency_score": 1.3, "display_order": 1, "summary": "DFA, NFA, regular expressions, closure properties"},
    {"id": "TOC_PDA", "subject_id": "TOC", "name": "Context-Free Grammars & PDAs", "difficulty": "HARD", "pyq_count": 40, "frequency_score": 1.2, "display_order": 2, "summary": "CFG, PDA, pumping lemma, CFL closure properties"},
    {"id": "TOC_TM", "subject_id": "TOC", "name": "Turing Machines & Decidability", "difficulty": "HARD", "pyq_count": 35, "frequency_score": 1.1, "display_order": 3, "summary": "TM variants, decidability, NP-completeness"},
    {"id": "ALGO_SORTING", "subject_id": "ALGO", "name": "Sorting & Searching", "difficulty": "MEDIUM", "pyq_count": 30, "frequency_score": 1.0, "display_order": 1, "summary": "Comparison sorts, linear sorts, binary search variations"},
    {"id": "ALGO_GRAPH", "subject_id": "ALGO", "name": "Graph Algorithms", "difficulty": "HARD", "pyq_count": 45, "frequency_score": 1.3, "display_order": 2, "summary": "BFS, DFS, shortest paths, MST, topological sort"},
    {"id": "ALGO_DP", "subject_id": "ALGO", "name": "Dynamic Programming", "difficulty": "HARD", "pyq_count": 50, "frequency_score": 1.4, "display_order": 3, "summary": "Optimal substructure, overlapping subproblems, memoization"},
    {"id": "ALGO_ASYMPTOTIC", "subject_id": "ALGO", "name": "Asymptotic Analysis", "difficulty": "MEDIUM", "pyq_count": 25, "frequency_score": 1.0, "display_order": 4, "summary": "Big-O, amortized analysis, recurrence relations"},
    {"id": "DBMS_NORMALIZATION", "subject_id": "DBMS", "name": "Normalization", "difficulty": "MEDIUM", "pyq_count": 35, "frequency_score": 1.1, "display_order": 1, "summary": "Functional dependencies, 1NF-BCNF, decomposition"},
    {"id": "DBMS_SQL", "subject_id": "DBMS", "name": "SQL & Relational Algebra", "difficulty": "MEDIUM", "pyq_count": 40, "frequency_score": 1.2, "display_order": 2, "summary": "Joins, subqueries, views, triggers, relational algebra"},
    {"id": "DBMS_TRANSACTION", "subject_id": "DBMS", "name": "Transaction Processing", "difficulty": "HARD", "pyq_count": 30, "frequency_score": 1.0, "display_order": 3, "summary": "ACID, concurrency control, recovery, 2PL, timestamps"},
    {"id": "CN_APPLICATION", "subject_id": "CN", "name": "Application Layer", "difficulty": "EASY", "pyq_count": 30, "frequency_score": 0.9, "display_order": 1, "summary": "HTTP, DNS, SMTP, FTP, socket programming"},
    {"id": "CN_TRANSPORT", "subject_id": "CN", "name": "Transport Layer", "difficulty": "HARD", "pyq_count": 40, "frequency_score": 1.2, "display_order": 2, "summary": "TCP, UDP, flow control, congestion control, reliability"},
    {"id": "CN_NETWORK", "subject_id": "CN", "name": "Network Layer", "difficulty": "MEDIUM", "pyq_count": 35, "frequency_score": 1.1, "display_order": 3, "summary": "IP addressing, routing algorithms, subnetting, NAT"},
]

SAMPLE_QUESTIONS = [
    {
        "id": "GATE_OS_2024_01",
        "topic_id": "OS_DEADLOCK",
        "subject_id": "OS",
        "year": 2024,
        "set_session": "Set 1",
        "question_type": "MCQ",
        "marks": 2,
        "negative_marks": 0.66,
        "difficulty": "MEDIUM",
        "problem_statement": "Consider a system with 3 resource types R1 (4 instances), R2 (2 instances), R3 (3 instances). Three processes P1, P2, P3 are running. P1 has been allocated (1,0,1), P2 has (0,1,1), P3 has (1,1,0). Maximum needs: P1(2,1,2), P2(1,2,2), P3(2,1,1). Which of the following is the safe sequence?",
        "options": [{"key": "A", "text": "P1, P2, P3"}, {"key": "B", "text": "P2, P1, P3"}, {"key": "C", "text": "P3, P1, P2"}, {"key": "D", "text": "P1, P3, P2"}],
        "correct_answers": "B",
        "detailed_solution": "Using Banker's Safety Algorithm:\nAvailable = (4-2, 2-2, 3-2) = (2, 0, 1)\nNeed matrix: P1(1,1,1), P2(1,1,1), P3(1,0,1)\nStep 1: P2 can run (Need(1,1,1) <= Available(2,0,1)? NO)\nActually Need P3 = (1,0,1) <= (2,0,1) YES\nP3 runs: Available = (2+1, 0+1, 1+0) = (3,1,1)\nP1 runs: Available = (3+1, 1+0, 1+1) = (4,1,2)\nP2 runs: Available = (4+0, 1+1, 2+1) = (4,2,3)\nSafe sequence: P3, P1, P2",
        "key_formula": "Need_{i,j} = Max_{i,j} - Allocation_{i,j}",
        "shortcut_trick": "Start with the process whose Need vector is smallest in all dimensions.",
        "common_trap": "Students often calculate Available before checking the Need matrix correctly.",
        "total_attempts": 0,
        "correct_attempts": 0,
    },
    {
        "id": "GATE_ALGO_2024_02",
        "topic_id": "ALGO_DP",
        "subject_id": "ALGO",
        "year": 2024,
        "set_session": "Set 1",
        "question_type": "NAT",
        "marks": 2,
        "negative_marks": 0.0,
        "difficulty": "HARD",
        "problem_statement": "How many distinct BSTs can be formed with 5 distinct keys?",
        "options": None,
        "correct_answers": "42",
        "tolerance_lower": 41.5,
        "tolerance_upper": 42.5,
        "detailed_solution": "The number of distinct BSTs with n keys is the nth Catalan number: C(n) = (2n)! / ((n+1)! * n!)\nC(5) = 10! / (6! * 5!) = 3628800 / (720 * 120) = 3628800 / 86400 = 42",
        "key_formula": "C(n) = \\frac{(2n)!}{(n+1)! \\cdot n!} = \\frac{1}{n+1}\\binom{2n}{n}",
        "shortcut_trick": "Memorize Catalan numbers: C(0)=1, C(1)=1, C(2)=2, C(3)=5, C(4)=14, C(5)=42",
        "common_trap": "Confusing BST count with number of binary trees. For BSTs, structure depends on key ordering.",
        "total_attempts": 0,
        "correct_attempts": 0,
    },
    {
        "id": "GATE_DBMS_2024_03",
        "topic_id": "DBMS_NORMALIZATION",
        "subject_id": "DBMS",
        "year": 2024,
        "set_session": "Set 1",
        "question_type": "MCQ",
        "marks": 1,
        "negative_marks": 0.33,
        "difficulty": "MEDIUM",
        "problem_statement": "Given R(A,B,C,D) with FDs: A->B, BC->D, D->A. What is the candidate key?",
        "options": [{"key": "A", "text": "AC"}, {"key": "B", "text": "ABC"}, {"key": "C", "text": "BC"}, {"key": "D", "text": "ADC"}],
        "correct_answers": "A",
        "detailed_solution": "Closure of AC: A->B, so AC->{A,B,C}. BC->D, so {A,B,C}->{A,B,C,D}. AC is a candidate key.\nClosure of BC: BC->{B,C}, D->A, so BC->{A,B,C,D}. BC is also a candidate key.\nBoth AC and BC are candidate keys.",
        "key_formula": "X^+ = X (closure under FDs)",
        "shortcut_trick": "Find attributes not on any RHS of FDs - they must be in every candidate key.",
        "common_trap": "Only checking one closure and stopping. Must verify minimality.",
        "total_attempts": 0,
        "correct_attempts": 0,
    },
    {
        "id": "GATE_CN_2024_04",
        "topic_id": "CN_TRANSPORT",
        "subject_id": "CN",
        "year": 2024,
        "set_session": "Set 1",
        "question_type": "MSQ",
        "marks": 2,
        "negative_marks": 0.0,
        "difficulty": "MEDIUM",
        "problem_statement": "Which of the following are true about TCP?\nA. TCP provides reliable delivery\nB. TCP uses 3-way handshake for connection setup\nC. TCP provides flow control using sliding window\nD. TCP is connectionless",
        "options": [{"key": "A", "text": "TCP provides reliable delivery"}, {"key": "B", "text": "TCP uses 3-way handshake for connection setup"}, {"key": "C", "text": "TCP provides flow control using sliding window"}, {"key": "D", "text": "TCP is connectionless"}],
        "correct_answers": "A,B,C",
        "detailed_solution": "TCP is connection-oriented, reliable, provides flow control (sliding window), and congestion control. UDP is the connectionless transport protocol.",
        "key_formula": None,
        "shortcut_trick": "TCP = connection-oriented + reliable. UDP = connectionless + unreliable.",
        "common_trap": "Selecting D because students confuse TCP/UDP properties.",
        "total_attempts": 0,
        "correct_attempts": 0,
    },
]

RESOURCES = [
    {"id": "RES_OS_GALVIN", "subject_id": "OS", "title": "Operating System Concepts (Silberschatz, Galvin & Gagne)", "provider": "Galvin / Wiley", "resource_type": "BOOK", "description": "Standard textbook for GATE OS. Detailed treatment of Deadlock, Process Synchronization, and Memory Management.", "url": "https://www.os-book.com", "recommended_chapters": "Chapters 7 & 8", "is_free": False},
    {"id": "RES_OS_SMASHERS", "subject_id": "OS", "title": "Deadlocks in OS Complete Playlist", "provider": "Gate Smashers", "resource_type": "PLAYLIST", "description": "High-yield visual lectures covering Banker's algorithm numericals.", "url": "https://www.youtube.com/playlist?list=GateSmashers_OS", "is_free": True},
    {"id": "RES_TOC_AMIT", "subject_id": "TOC", "title": "Theory of Computation Complete Course", "provider": "Amit Khurana", "resource_type": "PLAYLIST", "description": "Comprehensive playlist with rigorous proofs.", "url": "https://www.youtube.com/playlist?list=AmitKhurana_TOC", "is_free": True},
    {"id": "RES_ALGO_CLRS", "subject_id": "ALGO", "title": "Introduction to Algorithms (CLRS)", "provider": "Cormen (CLRS) / MIT Press", "resource_type": "BOOK", "description": "Gold standard algorithm compendium.", "url": "https://mitpress.mit.edu/books/introduction-algorithms", "recommended_chapters": "Chapters 3 & 4", "is_free": False},
    {"id": "RES_G_OVERFLOW", "subject_id": "ALL", "title": "GATE Overflow Topicwise PYQs (1987-2026)", "provider": "GateOverflow & GatePlus", "resource_type": "PRACTICE_SET", "description": "Over 35 years of authentic GATE CSE questions.", "url": "https://gateoverflow.in", "is_free": True},
]

FLASHCARDS = [
    {"id": "FC_COFMAN", "subject_id": "OS", "topic_id": "OS_DEADLOCK", "front_prompt": "What are the 4 Coffman conditions for deadlock?", "back_explanation": "1. Mutual Exclusion\n2. Hold and Wait\n3. No Preemption\n4. Circular Wait", "latex_formula": None, "key_tag": "OS"},
    {"id": "FC_RICE", "subject_id": "TOC", "topic_id": "TOC_TM", "front_prompt": "State Rice's Theorem", "back_explanation": "For any non-trivial property P of partial functions, the set {M | M decides P} is undecidable.", "latex_formula": "P \\neq \\emptyset \\land P \\neq \\mathcal{P} \\implies L_P \\text{ is undecidable}", "key_tag": "TOC"},
]

FORMULAS = [
    {"id": "F_CATALAN", "subject_id": "ALGO", "name": "Catalan Number", "formula_latex": "C_n = \\frac{1}{n+1}\\binom{2n}{n}", "description": "Number of distinct BSTs, valid parentheses, triangulations with n+2 points", "applications": "BST counting, balanced parentheses, parenthesization"},
    {"id": "F_MASTER", "subject_id": "ALGO", "name": "Master Theorem", "formula_latex": "T(n) = aT(n/b) + \\Theta(n^d)", "description": "If a < b^d: T(n) = Theta(n^d). If a = b^d: T(n) = Theta(n^d log n). If a > b^d: T(n) = Theta(n^(log_b a))", "applications": "Solving divide-and-conquer recurrences"},
    {"id": "F_SM2", "subject_id": "OS", "name": "SM-2 Ease Factor", "formula_latex": "EF' = EF + (0.1 - (5-q)(0.08 + (5-q) \\times 0.02))", "description": "Updates ease factor in spaced repetition based on quality rating q (0-5)", "applications": "Spaced repetition scheduling, flashcard review intervals"},
]


async def seed():
    print("Seeding PostgreSQL database...")
    async with AsyncSessionLocal() as db:
        try:
            # Subjects
            for s in SUBJECTS:
                db.add(Subject(**s))
            await db.flush()
            print(f"  Inserted {len(SUBJECTS)} subjects")

            # Topics
            for t in TOPICS:
                db.add(Topic(**t))
            await db.flush()
            print(f"  Inserted {len(TOPICS)} topics")

            # Questions
            for q in SAMPLE_QUESTIONS:
                db.add(Question(**q))
            await db.flush()
            print(f"  Inserted {len(SAMPLE_QUESTIONS)} questions")

            # Resources
            for r in RESOURCES:
                db.add(Resource(**r))
            await db.flush()
            print(f"  Inserted {len(RESOURCES)} resources")

            # Flashcards
            for f in FLASHCARDS:
                db.add(Flashcard(**f))
            await db.flush()
            print(f"  Inserted {len(FLASHCARDS)} flashcards")

            # Formulas
            for f in FORMULAS:
                db.add(Formula(**f))
            await db.flush()
            print(f"  Inserted {len(FORMULAS)} formulas")

            await db.commit()
            print("Database seeded successfully!")
        except Exception as e:
            await db.rollback()
            print(f"Error seeding database: {e}")
            raise


if __name__ == "__main__":
    asyncio.run(seed())
