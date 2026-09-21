package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.RecommendationType
import com.example.data.repository.SmartRecommendation
import com.example.ui.components.FocusTimerDialog
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@Composable
fun DashboardScreen(
    viewModel: GateViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val recommendation by viewModel.recommendation.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsState()
    val dueRevisions by viewModel.dueRevisionItems.collectAsState()
    val attentionTopics by viewModel.topicsNeedingAttention.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val nextExamEvent by viewModel.nextExamEvent.collectAsState()

    var showTimerDialog by remember { mutableStateOf(false) }

    if (showTimerDialog) {
        FocusTimerDialog(
            subjects = subjects,
            onDismiss = { showTimerDialog = false },
            onSaveSession = { sub, top, mins, type ->
                viewModel.logFocusSession(sub, top, mins, type)
                showTimerDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. GATE 2027 Countdown Banner
        item {
            GateCountdownBanner(
                targetExam = userProfile?.targetExam ?: "GATE CSE",
                targetYear = userProfile?.targetYear ?: 2027,
                examDateMillis = nextExamEvent?.eventDateMillis ?: 1801872000000L,
                nextEventTitle = nextExamEvent?.eventName,
                nextEventDate = nextExamEvent?.eventDateFormatted
            )
        }

        // 1b. Immediate Topic Attention Banner (SRS-Triggered)
        if (attentionTopics.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoseError.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, RoseError.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("revision") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoseError.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = RoseError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Action Required: ${attentionTopics.size} Topic${if (attentionTopics.size > 1) "s" else ""} Flagged",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoseError
                                )
                            )
                            Text(
                                text = attentionTopics.first().attentionReason ?: "Low accuracy or repeated conceptual mistakes detected.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                ),
                                maxLines = 2
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Review",
                            tint = RoseError
                        )
                    }
                }
            }
        }

        // 2. Smart Recommendation Engine Card
        item {
            recommendation?.let { rec ->
                SmartRecommendationCard(
                    recommendation = rec,
                    onActionClick = {
                        when (rec.type) {
                            RecommendationType.REVISION -> onNavigate("revision")
                            RecommendationType.PRACTICE_WEAK -> onNavigate("practice_session/${rec.relatedTopicId ?: "ALL"}/10")
                            RecommendationType.STUDY_NEW -> onNavigate("practice_session/OS_DEADLOCK/10")
                            RecommendationType.MOCK_TEST -> onNavigate("exam_simulator/FULL_MOCK")
                        }
                    }
                )
            }
        }

        // 3. Today's Key Metrics Grid
        item {
            Text(
                text = "Today's Preparation Velocity",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val solved = userProfile?.totalQuestionsSolved ?: 0
                val correct = userProfile?.totalCorrect ?: 0
                val acc = if (solved > 0) ((correct.toFloat() / solved) * 100).toInt() else 0

                MetricCard(
                    title = "Questions",
                    value = "$solved",
                    subtitle = "$correct correct ($acc%)",
                    icon = Icons.Default.CheckCircleOutline,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Study Time",
                    value = "${totalStudyMinutes ?: 0}m",
                    subtitle = "Target: 210m/day",
                    icon = Icons.Default.AccessTime,
                    accentColor = AmberAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Revision Due",
                    value = "${dueRevisions.size}",
                    subtitle = "Spaced Repetition",
                    icon = Icons.Default.Autorenew,
                    accentColor = if (dueRevisions.isNotEmpty()) RoseError else EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Quick Action Hub
        item {
            Text(
                text = "Quick Operations Hub",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionTile(
                        title = "Quick Practice",
                        desc = "Adaptive 10 Questions",
                        icon = Icons.Default.PlayArrow,
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("practice_session/ALL/10") }
                    )
                    QuickActionTile(
                        title = "Full Mock Test",
                        desc = "65 Qs • 3h Exam Simulator",
                        icon = Icons.Default.Assignment,
                        accentColor = IndigoAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("exam_simulator/FULL_MOCK") }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionTile(
                        title = "Error Notebook",
                        desc = "Revise Past Mistakes",
                        icon = Icons.Default.ErrorOutline,
                        accentColor = RoseError,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("mistakes") }
                    )
                    QuickActionTile(
                        title = "Focus Timer",
                        desc = "Pomodoro / Deep Work",
                        icon = Icons.Default.Timer,
                        accentColor = AmberAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { showTimerDialog = true }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionTile(
                        title = "Curated Resources",
                        desc = "Playlists, Books, NPTEL",
                        icon = Icons.Default.LibraryBooks,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("resources") }
                    )
                    QuickActionTile(
                        title = "AI Tutor & RAG",
                        desc = "Instant Conceptual Help",
                        icon = Icons.Default.SmartToy,
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("ai_tutor") }
                    )
                }
            }
        }

        // 5. Daily Study Tasks
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Today's Study Plan Tasks",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { onNavigate("planner") }) {
                    Text("View Schedule", fontSize = 12.sp, color = CyanPrimary)
                }
            }
        }

        items(dailyTasks) { task ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleTaskStatus(task.id, task.isCompleted) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { viewModel.toggleTaskStatus(task.id, task.isCompleted) },
                        colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "${task.subjectId} • ${task.allocatedMinutes} mins • Priority: ${task.priority}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GateCountdownBanner(
    targetExam: String,
    targetYear: Int,
    examDateMillis: Long,
    nextEventTitle: String? = null,
    nextEventDate: String? = null
) {
    val currentTime = System.currentTimeMillis()
    val diffMillis = kotlin.math.max(0L, examDateMillis - currentTime)
    val daysRemaining = diffMillis / (1000L * 60 * 60 * 24)
    val hoursRemaining = (diffMillis / (1000L * 60 * 60)) % 24

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "TARGET EXAM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "$targetExam $targetYear",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = if (nextEventTitle != null && nextEventDate != null) {
                        "Next: $nextEventTitle ($nextEventDate)"
                    } else {
                        "Expected Exam: First Sunday of Feb $targetYear"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    ),
                    maxLines = 2
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyanPrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$daysRemaining",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary
                        )
                    )
                    Text(
                        text = "DAYS LEFT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartRecommendationCard(
    recommendation: SmartRecommendation,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = AmberAccent.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, AmberAccent.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SMART ADAPTIVE RECOMMENDATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AmberAccent,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                Text(
                    text = "${recommendation.estimatedMinutes} min",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${recommendation.actionTitle}: ${recommendation.targetName}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_recommendation_button")
            ) {
                Text(
                    text = "Start Recommended Session",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
