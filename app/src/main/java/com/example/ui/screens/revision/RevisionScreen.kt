package com.example.ui.screens.revision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.MistakeCategory
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@Composable
fun RevisionScreen(
    viewModel: GateViewModel,
    onPracticeQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Error Notebook, 1 = SRS Due, 2 = Flagged Topics

    val mistakeAttempts by viewModel.mistakeAttempts.collectAsState()
    val dueRevisions by viewModel.dueRevisionItems.collectAsState()
    val allQuestions by viewModel.filteredQuestions.collectAsState()
    val attentionTopics by viewModel.topicsNeedingAttention.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<MistakeCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Adaptive Revision & Error Notebook",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Identify why marks were lost and retain solutions via SuperMemo Spaced Repetition",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CyanPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Errors (${mistakeAttempts.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("SRS Due (${dueRevisions.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Flagged (${attentionTopics.size})", fontSize = 12.sp, color = if (attentionTopics.isNotEmpty()) RoseError else MaterialTheme.colorScheme.onSurface) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Mistake Category Filter Chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All Mistakes") }
                )
                MistakeCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.name.replace("_", " ")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val filteredAttempts = if (selectedCategoryFilter == null) mistakeAttempts
            else mistakeAttempts.filter { it.mistakeCategory == selectedCategoryFilter }

            if (filteredAttempts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Error Notebook is Clean!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Any incorrect answers from practice or mock exams will automatically appear here for remediation.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(filteredAttempts) { attempt ->
                        val question = allQuestions.firstOrNull { it.id == attempt.questionId }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RoseError.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = attempt.mistakeCategory?.name?.replace("_", " ") ?: "CONCEPTUAL",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = RoseError,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "${attempt.marksAwarded} Marks",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RoseError,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = question?.questionText ?: "Question #${attempt.questionId}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Your Answer: ${attempt.selectedAnswer} | Correct: ${question?.correctAnswers ?: "See Solution"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                if (!attempt.userNote.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Note: ${attempt.userNote}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = AmberAccent)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onPracticeQuestion(attempt.questionId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Re-Attempt", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.markRevisionMastered(attempt.questionId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Mark Mastered", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Spaced Repetition Queue View
            if (dueRevisions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All Caught Up on Spaced Repetition!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "No questions are currently overdue in your SuperMemo-2 queue.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(dueRevisions) { item ->
                        val question = allQuestions.firstOrNull { it.id == item.questionId }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "SRS Interval: ${item.intervalDays} Days (Rep #${item.repetitionCount})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "DUE TODAY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RoseError,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = question?.questionText ?: "Question #${item.questionId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onPracticeQuestion(item.questionId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Solve Now & Advance SRS", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Flagged Topics Needing Immediate Attention
            if (attentionTopics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Topics Flagged for Remediation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "All attempted topics maintain solid accuracy and healthy error margins.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RoseError.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RoseError)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "The SRS algorithm detected conceptual vulnerabilities or repeated error clusters in these topics.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }

                    items(attentionTopics) { topicMastery ->
                        val topic = allTopics.firstOrNull { it.id == topicMastery.topicId }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = topic?.name ?: topicMastery.topicId,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RoseError.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "ATTENTION REQUIRED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = RoseError,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Accuracy: ${topicMastery.accuracy.toInt()}% • Attempts: ${topicMastery.attemptsCount} • Mastery: ${topicMastery.masteryScore.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )

                                if (!topicMastery.attentionReason.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Reason: ${topicMastery.attentionReason}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val firstQuestionInTopic = allQuestions.firstOrNull { it.topicId == topicMastery.topicId }
                                if (firstQuestionInTopic != null) {
                                    Button(
                                        onClick = { onPracticeQuestion(firstQuestionInTopic.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Remediate Topic (Start Drill)", color = Color.Black, fontWeight = FontWeight.Bold)
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
