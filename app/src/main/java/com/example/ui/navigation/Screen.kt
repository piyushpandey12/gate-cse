package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Subjects : Screen("subjects", "Subjects")
    object SubjectDetail : Screen("subject_detail/{subjectId}", "Subject Details") {
        fun createRoute(subjectId: String) = "subject_detail/$subjectId"
    }
    object TopicDetail : Screen("topic_detail/{topicId}", "Topic Details") {
        fun createRoute(topicId: String) = "topic_detail/$topicId"
    }
    object PyqExplorer : Screen("pyqs", "PYQs")
    object PracticeSetup : Screen("practice_setup", "Practice")
    object PracticeSession : Screen("practice_session/{topicId}/{count}", "Practice Session") {
        fun createRoute(topicId: String = "ALL", count: Int = 10) = "practice_session/$topicId/$count"
    }
    object TestsList : Screen("tests", "Tests & Mocks")
    object ExamSimulator : Screen("exam_simulator/{testType}", "Exam Simulator") {
        fun createRoute(testType: String = "FULL_MOCK") = "exam_simulator/$testType"
    }
    object TestResult : Screen("test_result/{testId}", "Test Result") {
        fun createRoute(testId: String) = "test_result/$testId"
    }
    object Revision : Screen("revision", "Spaced Repetition")
    object Mistakes : Screen("mistakes", "Error Notebook")
    object Resources : Screen("resources", "Resources")
    object Flashcards : Screen("flashcards", "Flashcards")
    object Formulas : Screen("formulas", "Formula Sheets")
    object Notes : Screen("notes", "Notes")
    object AiTutor : Screen("ai_tutor", "AI Tutor")
    object Analytics : Screen("analytics", "Analytics")
    object Planner : Screen("planner", "Study Planner")
    object Settings : Screen("settings", "Settings")
    object Admin : Screen("admin", "Admin Panel")
    object Profile : Screen("profile", "Profile")
    object More : Screen("more", "More Features")
}
