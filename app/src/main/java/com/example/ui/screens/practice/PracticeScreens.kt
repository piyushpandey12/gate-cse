package com.example.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.Difficulty
import com.example.data.model.QuestionType
import com.example.ui.components.QuestionCard
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeSetupScreen(
    viewModel: GateViewModel,
    onStartPractice: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()

    var selectedMode by remember { mutableStateOf("ADAPTIVE") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var questionCount by remember { mutableStateOf(10) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Interactive Practice Engine",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Strengthen concepts through timed PYQs with instant evaluation & error logging",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Practice Mode Cards
        item {
            Text(
                text = "Select Practice Mode",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val modes = listOf(
                Triple("ADAPTIVE", "Adaptive Smart Drill", "Prioritizes your weak topics and overdue spaced repetitions"),
                Triple("HIGH_YIELD", "Recent High-Yield PYQs", "Focuses on high-frequency questions from 2020-2026"),
                Triple("WEAK_TOPICS", "Weak Topics Bootcamp", "Drills questions in areas where your accuracy is below 70%"),
                Triple("CUSTOM", "Custom Subject & Topic", "Choose specific subject, topic, and question volume")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEach { (modeId, title, desc) ->
                    val isSelected = selectedMode == modeId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMode = modeId }
                            .testTag("practice_mode_$modeId")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMode = modeId },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Question Count Selector
        item {
            Text(
                text = "Session Volume",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20).forEach { count ->
                    FilterChip(
                        selected = questionCount == count,
                        onClick = { questionCount = count },
                        label = { Text("$count Questions") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary
                        )
                    )
                }
            }
        }

        // Custom Subject / Topic Selection if Custom Mode is chosen
        if (selectedMode == "CUSTOM") {
            item {
                Text(
                    text = "Select Subject",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(subjects) { sub ->
                        val isSubSelected = selectedSubjectId == sub.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSubSelected) IndigoAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSubSelected) IndigoAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSubjectId = sub.id
                                    selectedTopicId = null
                                }
                        ) {
                            Text(
                                text = "${sub.code} - ${sub.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Start Practice Button
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val topic = selectedTopicId ?: selectedSubjectId ?: "ALL"
                    onStartPractice(topic, questionCount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("launch_practice_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Start Practice Session",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PracticeSessionScreen(
    topicId: String,
    questionCount: Int,
    viewModel: GateViewModel,
    onFinishSession: () -> Unit,
    onAskAiQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.practiceState.collectAsState()

    LaunchedEffect(topicId, questionCount) {
        viewModel.startPractice(topicId, questionCount)
    }

    if (state.isFinished) {
        // Summary Card
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Practice Drill Completed!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "All attempts logged to Room database & topic mastery updated",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Score", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = String.format("%.2f", state.score),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanPrimary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Correct", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${state.correctCount}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldSuccess
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Incorrect", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${state.incorrectCount}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = RoseError
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onFinishSession,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("return_dashboard_button")
                    ) {
                        Text("Return to Dashboard", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentQuestion = state.questions.getOrNull(state.currentIndex)

    if (currentQuestion == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyanPrimary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Progress & Timer Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Question ${state.currentIndex + 1} of ${state.questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanPrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
                ) {
                    val mins = state.timeSpentSeconds / 60
                    val secs = state.timeSpentSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val progress = (state.currentIndex + 1).toFloat() / state.questions.size
            LinearProgressIndicator(
                progress = { progress },
                color = CyanPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        // Active Question Card
        item {
            QuestionCard(
                question = currentQuestion,
                selectedAnswer = state.selectedAnswer,
                isEvaluated = state.isEvaluated,
                lastAttempt = state.lastAttempt,
                onAnswerSelected = { ans -> viewModel.selectPracticeAnswer(ans) },
                onSubmitAnswer = { cat, note -> viewModel.submitPracticeCurrentAnswer(cat, note) },
                onNextQuestion = { viewModel.nextPracticeQuestion() },
                onToggleBookmark = { cur -> viewModel.toggleBookmark(currentQuestion.id, cur) },
                onAskAi = { onAskAiQuestion(currentQuestion.id) }
            )
        }
    }
}
