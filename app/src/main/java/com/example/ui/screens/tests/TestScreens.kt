package com.example.ui.screens.tests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionType
import com.example.ui.components.QuestionPalette
import com.example.ui.components.TimerDisplay
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@Composable
fun TestsListScreen(
    viewModel: GateViewModel,
    onLaunchExam: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Official GATE Exam Simulator & Test Series",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Full-length 3-hour computer-based exam simulator with official question palette and negative marking",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Available Test Cards
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, CyanPrimary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "FULL-LENGTH MOCK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "180 Mins • 100 Marks",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "GATE 2027 Grand All-India Mock Test #1",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Complete syllabus coverage: General Aptitude (15M) + CS Core & EM (85M). MCQ, MSQ, and NAT questions with negative marking.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onLaunchExam("FULL_MOCK") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_full_mock_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enter Exam Simulator", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndigoAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SECTIONAL TEST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = IndigoAccent,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "30 Mins • 25 Marks",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Systems Core Speed Drill (OS + COA)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "15 high-speed questions focusing on CPU Scheduling, Memory Paging, Cache, and Pipelining.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onLaunchExam("SECTIONAL") },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Sectional Mock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExamSimulatorScreen(
    testType: String,
    viewModel: GateViewModel,
    onTestSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.testState.collectAsState()
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(testType) {
        viewModel.startExam(testType)
    }

    if (state.isSubmitted) {
        // Result Screen
        ExamResultView(
            state = state,
            onReturn = onTestSubmitted
        )
        return
    }

    val currentQ = state.questions.getOrNull(state.currentIndex)

    if (currentQ == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyanPrimary)
        }
        return
    }

    if (showSubmitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("Submit Exam?") },
            text = {
                val answered = state.userAnswers.values.count { it.isNotBlank() }
                Text("You have answered $answered out of ${state.questions.size} questions. Are you sure you want to submit your test session?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        viewModel.submitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Yes, Submit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Return to Exam")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Exam Simulator Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = "Q ${state.currentIndex + 1}/${state.questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${currentQ.questionType} (${currentQ.marks} Marks)",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            TimerDisplay(secondsRemaining = state.remainingSeconds)

            IconButton(
                onClick = { showPaletteSheet = !showPaletteSheet },
                modifier = Modifier.testTag("toggle_palette_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Question Palette",
                    tint = CyanPrimary
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Question Palette Drawer / Accordion if toggled
        if (showPaletteSheet) {
            QuestionPalette(
                questions = state.questions,
                currentIndex = state.currentIndex,
                userAnswers = state.userAnswers,
                markedForReview = state.markedForReview,
                onSelectQuestion = { idx ->
                    viewModel.selectExamQuestionIndex(idx)
                    showPaletteSheet = false
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Active Exam Question View
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = currentQ.questionText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp
                    )
                )
            }

            item {
                val currentAnswer = state.userAnswers[currentQ.id].orEmpty()

                when (currentQ.questionType) {
                    QuestionType.MCQ -> {
                        val options = listOf(
                            "A" to currentQ.optionA,
                            "B" to currentQ.optionB,
                            "C" to currentQ.optionC,
                            "D" to currentQ.optionD
                        )
                        options.forEach { (opt, text) ->
                            if (!text.isNullOrBlank()) {
                                val isSelected = currentAnswer == opt
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.saveExamAnswer(opt) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.saveExamAnswer(opt) },
                                            colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "$opt) $text", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    QuestionType.MSQ -> {
                        val selList = currentAnswer.split(",").filter { it.isNotBlank() }.toSet()
                        val options = listOf(
                            "A" to currentQ.optionA,
                            "B" to currentQ.optionB,
                            "C" to currentQ.optionC,
                            "D" to currentQ.optionD
                        )
                        options.forEach { (opt, text) ->
                            if (!text.isNullOrBlank()) {
                                val isSelected = selList.contains(opt)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            val newSet = selList.toMutableSet()
                                            if (newSet.contains(opt)) newSet.remove(opt) else newSet.add(opt)
                                            viewModel.saveExamAnswer(newSet.sorted().joinToString(","))
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "$opt) $text", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    QuestionType.NAT -> {
                        OutlinedTextField(
                            value = currentAnswer,
                            onValueChange = { viewModel.saveExamAnswer(it) },
                            label = { Text("Enter Numeric Answer") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Exam Bottom Navigation Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearExamResponse() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear", fontSize = 12.sp)
                }

                val isMarked = state.markedForReview.contains(currentQ.id)
                OutlinedButton(
                    onClick = { viewModel.toggleMarkForReview() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isMarked) IndigoAccent else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text(if (isMarked) "Unmark" else "Mark Review", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (state.currentIndex + 1 < state.questions.size) {
                            viewModel.selectExamQuestionIndex(state.currentIndex + 1)
                        } else {
                            showSubmitConfirmDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier.weight(1.4f).testTag("save_and_next_button")
                ) {
                    Text("Save & Next", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = { showSubmitConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_submit_exam_button")
            ) {
                Text("Submit Exam", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExamResultView(
    state: com.example.viewmodel.TestUiState,
    onReturn: () -> Unit
) {
    val result = state.submittedResult

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mock Exam Assessment Report",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Comprehensive analytics computed and logged to your local profile",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Final Total Score", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = String.format("%.2f", result?.totalScore ?: 0f),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanPrimary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Accuracy", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${result?.accuracy?.toInt() ?: 0}%",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldSuccess
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Questions Breakdown",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total: ${state.questions.size} • Attempted: ${state.userAnswers.size} • Reviewed: ${state.markedForReview.size}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        item {
            Button(
                onClick = onReturn,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Dashboard", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
