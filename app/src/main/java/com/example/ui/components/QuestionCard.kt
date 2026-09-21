package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun QuestionCard(
    question: QuestionEntity,
    selectedAnswer: String,
    isEvaluated: Boolean,
    lastAttempt: AttemptEntity?,
    onAnswerSelected: (String) -> Unit,
    onSubmitAnswer: (MistakeCategory?, String?) -> Unit,
    onNextQuestion: () -> Unit,
    onToggleBookmark: (Boolean) -> Unit,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSolution by remember(question.id) { mutableStateOf(false) }
    var natInput by remember(question.id) { mutableStateOf(selectedAnswer) }
    var selectedMistake by remember(question.id) { mutableStateOf(MistakeCategory.CONCEPTUAL) }
    var mistakeNote by remember(question.id) { mutableStateOf("") }
    var showMistakeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            // Header: Badges & Bookmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (question.year != null) "GATE ${question.year} ${question.setSession ?: ""}" else "PRACTICE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${question.questionType} (${question.marks}M)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (question.negativeMarks != 0.0f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "-${kotlin.math.abs(question.negativeMarks)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RoseError,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleBookmark(question.isBookmarked) },
                        modifier = Modifier.size(36.dp).testTag("bookmark_button_${question.id}")
                    ) {
                        Icon(
                            imageVector = if (question.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark Question",
                            tint = if (question.isBookmarked) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onAskAi,
                        modifier = Modifier.size(36.dp).testTag("ask_ai_button_${question.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Ask AI",
                            tint = CyanPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question Text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Options based on QuestionType
            when (question.questionType) {
                QuestionType.MCQ -> {
                    val options = listOf(
                        "A" to question.optionA,
                        "B" to question.optionB,
                        "C" to question.optionC,
                        "D" to question.optionD
                    )
                    options.forEach { (letter, optText) ->
                        if (!optText.isNullOrBlank()) {
                            val isSelected = selectedAnswer == letter
                            val isCorrectAnswer = question.correctAnswers.trim().equals(letter, ignoreCase = true)
                            val optionBg = when {
                                isEvaluated && isCorrectAnswer -> EmeraldSuccess.copy(alpha = 0.18f)
                                isEvaluated && isSelected && !isCorrectAnswer -> RoseError.copy(alpha = 0.18f)
                                isSelected -> CyanPrimary.copy(alpha = 0.18f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                            val optionBorder = when {
                                isEvaluated && isCorrectAnswer -> EmeraldSuccess
                                isEvaluated && isSelected && !isCorrectAnswer -> RoseError
                                isSelected -> CyanPrimary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = optionBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, optionBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !isEvaluated) {
                                        onAnswerSelected(letter)
                                    }
                                    .testTag("option_${letter}_${question.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letter,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                QuestionType.MSQ -> {
                    Text(
                        text = "Multiple Select Question (MSQ): Select all correct options",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AmberAccent,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    val selectedLetters = selectedAnswer.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()
                    val options = listOf(
                        "A" to question.optionA,
                        "B" to question.optionB,
                        "C" to question.optionC,
                        "D" to question.optionD
                    )
                    options.forEach { (letter, optText) ->
                        if (!optText.isNullOrBlank()) {
                            val isSelected = selectedLetters.contains(letter)
                            val isActuallyCorrect = question.correctAnswers.split(",").map { it.trim().uppercase() }.contains(letter)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isEvaluated && isActuallyCorrect) EmeraldSuccess
                                    else if (isSelected) CyanPrimary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !isEvaluated) {
                                        val newSet = selectedLetters.toMutableSet()
                                        if (newSet.contains(letter)) newSet.remove(letter) else newSet.add(letter)
                                        onAnswerSelected(newSet.sorted().joinToString(","))
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = CyanPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$letter) $optText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                QuestionType.NAT -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Numerical Answer Type (NAT): Enter exact numerical value",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmberAccent,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = natInput,
                            onValueChange = {
                                natInput = it
                                onAnswerSelected(it)
                            },
                            enabled = !isEvaluated,
                            placeholder = { Text("e.g. 17 or 2.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nat_input_field"),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Submit / Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isEvaluated) {
                    Button(
                        onClick = { onSubmitAnswer(null, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_answer_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Submit & Evaluate",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { expandedSolution = !expandedSolution },
                            modifier = Modifier.weight(1f).testTag("toggle_solution_button")
                        ) {
                            Icon(
                                imageVector = if (expandedSolution) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (expandedSolution) "Hide Solution" else "View Solution")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = onNextQuestion,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.weight(1f).testTag("next_question_button")
                        ) {
                            Text("Next Question", color = Color.Black, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
            }

            // Post-Evaluation Result Banner & Error Categorization
            AnimatedVisibility(visible = isEvaluated && lastAttempt != null) {
                val attempt = lastAttempt ?: return@AnimatedVisibility
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (attempt.isCorrect) EmeraldSuccess.copy(alpha = 0.15f) else RoseError.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (attempt.isCorrect) EmeraldSuccess else RoseError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (attempt.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (attempt.isCorrect) EmeraldSuccess else RoseError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (attempt.isCorrect) "Correct! +${attempt.marksAwarded} Marks" else "Incorrect (${attempt.marksAwarded} Marks)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (attempt.isCorrect) EmeraldSuccess else RoseError
                                )
                            }
                            if (!attempt.isCorrect) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Correct Answer: ${question.correctAnswers}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Logged to Error Notebook for Spaced Repetition revision.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Detailed Solution
            AnimatedVisibility(visible = expandedSolution) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📖 Verified Detailed Solution",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = question.detailedSolution,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                            )

                            if (!question.keyFormula.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                MathFormulaView(
                                    rawFormula = question.keyFormula,
                                    title = "⚡ Core GATE Formula:"
                                )
                            }

                            if (!question.shortcutTrick.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AmberAccent.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "🚀 Exam Shortcut / Trick:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = AmberAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = question.shortcutTrick,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            if (!question.commonTrap.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RoseError.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "⚠️ Common GATE Trap:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = RoseError,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = question.commonTrap,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
