package com.example.network.mapper

import com.example.data.model.*
import com.example.network.dto.*

object DtoMapper {

    fun SubjectDto.toEntity() = SubjectEntity(
        id = id,
        name = name,
        code = code,
        description = "",
        iconName = icon_name,
        weightagePercentage = weightage_percent,
        orderIndex = display_order
    )

    fun TopicDto.toEntity() = TopicEntity(
        id = id,
        subjectId = subject_id,
        name = name,
        chapter = "",
        description = summary ?: "",
        totalQuestions = 0,
        pyqCount = pyq_count,
        highYield = frequency_score > 1.0,
        orderIndex = display_order
    )

    fun QuestionDto.toEntity() = QuestionEntity(
        id = id,
        subjectId = subject_id,
        topicId = topic_id,
        year = year,
        setSession = set_session,
        questionNumber = null,
        questionType = when (question_type) {
            "MSQ" -> QuestionType.MSQ
            "NAT" -> QuestionType.NAT
            else -> QuestionType.MCQ
        },
        marks = marks.toFloat(),
        negativeMarks = negative_marks,
        difficulty = when (difficulty) {
            "EASY" -> Difficulty.EASY
            "HARD" -> Difficulty.HARD
            else -> Difficulty.MEDIUM
        },
        questionText = problem_statement,
        optionA = options?.find { it.key == "A" }?.text,
        optionB = options?.find { it.key == "B" }?.text,
        optionC = options?.find { it.key == "C" }?.text,
        optionD = options?.find { it.key == "D" }?.text,
        correctAnswers = correct_answers ?: "",
        shortExplanation = "",
        detailedSolution = detailed_solution ?: "",
        shortcutTrick = shortcut_trick,
        commonTrap = common_trap,
        keyFormula = key_formula,
        isPyq = year != null,
        isBookmarked = false
    )

    fun ResourceDto.toEntity() = ResourceEntity(
        id = id,
        subjectId = subject_id,
        topicId = topic_id,
        title = title,
        provider = provider,
        resourceType = when (resource_type) {
            "NPTEL" -> ResourceType.NPTEL
            "BOOK" -> ResourceType.BOOK
            "NOTE" -> ResourceType.NOTE
            "PRACTICE_SET" -> ResourceType.PRACTICE_SET
            else -> ResourceType.PLAYLIST
        },
        description = description,
        url = url,
        recommendedChapters = recommended_chapters,
        isFree = is_free,
        isBookmarked = is_bookmarked
    )

    fun FlashcardDto.toEntity() = FlashcardEntity(
        id = id,
        subjectId = subject_id,
        topicId = topic_id,
        front = front_prompt,
        back = back_explanation,
        keyConcept = key_tag ?: ""
    )

    fun FormulaDto.toEntity() = FormulaEntity(
        id = id,
        subjectId = subject_id,
        topicId = "",
        name = name,
        formulaLatex = formula_latex,
        description = description,
        applications = applications ?: "",
        isFavorite = is_favorite
    )
}
