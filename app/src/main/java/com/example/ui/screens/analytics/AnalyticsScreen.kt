package com.example.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.MasteryLevel
import com.example.data.model.MistakeCategory
import com.example.ui.components.MetricCard
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@Composable
fun AnalyticsScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val masteryList by viewModel.topicMasteryList.collectAsState()
    val mistakeAttempts by viewModel.mistakeAttempts.collectAsState()
    val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsState()

    val totalSolved = userProfile?.totalQuestionsSolved ?: 0
    val totalCorrect = userProfile?.totalCorrect ?: 0
    val accuracy = if (totalSolved > 0) ((totalCorrect.toFloat() / totalSolved) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Performance Diagnostics & Analytics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Quantitative tracking of concept mastery, mistake distribution, and time velocity",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Summary Metric Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    title = "Overall Accuracy",
                    value = "$accuracy%",
                    subtitle = "$totalCorrect of $totalSolved correct",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Study Velocity",
                    value = "${totalStudyMinutes ?: 0}m",
                    subtitle = "Logged in Focus Mode",
                    icon = Icons.Default.Timer,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Topic Mastery Distribution
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Topic Mastery Distribution",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val mastered = masteryList.count { it.level == MasteryLevel.MASTERED }
                    val strong = masteryList.count { it.level == MasteryLevel.STRONG }
                    val improving = masteryList.count { it.level == MasteryLevel.IMPROVING }
                    val weak = masteryList.count { it.level == MasteryLevel.WEAK }
                    val learning = masteryList.count { it.level == MasteryLevel.LEARNING || it.level == MasteryLevel.NEW }

                    MasteryDistributionRow("Mastered (>85% Acc)", mastered, EmeraldSuccess, masteryList.size)
                    MasteryDistributionRow("Strong (70-85% Acc)", strong, CyanPrimary, masteryList.size)
                    MasteryDistributionRow("Improving (45-70% Acc)", improving, IndigoAccent, masteryList.size)
                    MasteryDistributionRow("Weak (<45% Acc)", weak, RoseError, masteryList.size)
                    MasteryDistributionRow("Learning / Unattempted", learning, MaterialTheme.colorScheme.onSurfaceVariant, masteryList.size)
                }
            }
        }

        // Mistake Pattern Breakdown
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mistake Category Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Categorizing past exam mistakes prevents recurring errors",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalMistakes = maxOf(1, mistakeAttempts.size)
                    MistakeCategory.values().forEach { cat ->
                        val count = mistakeAttempts.count { it.mistakeCategory == cat }
                        val pct = ((count.toFloat() / totalMistakes) * 100).toInt()

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cat.name.replace("_", " "),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "$count ($pct%)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / totalMistakes },
                                color = RoseError,
                                trackColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasteryDistributionRow(
    label: String,
    count: Int,
    color: Color,
    total: Int
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "$count topics",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
