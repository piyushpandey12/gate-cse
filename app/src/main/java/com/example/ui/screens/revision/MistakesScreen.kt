package com.example.ui.screens.revision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.AttemptEntity
import com.example.data.model.MistakeCategory
import com.example.data.model.QuestionEntity
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakesScreen(
    viewModel: GateViewModel,
    onNavigateToPractice: (String, Int) -> Unit,
    onAskAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mistakeAttempts by viewModel.mistakeAttempts.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<MistakeCategory?>(null) }
    var selectedAttemptForNote by remember { mutableStateOf<AttemptEntity?>(null) }
    var newNoteText by remember { mutableStateOf("") }

    val questionMap = remember(allQuestions) { allQuestions.associateBy { it.id } }

    val filteredAttempts = remember(mistakeAttempts, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            mistakeAttempts
        } else {
            mistakeAttempts.filter { it.mistakeCategory == selectedCategoryFilter }
        }
    }

    if (selectedAttemptForNote != null) {
        AlertDialog(
            onDismissRequest = { selectedAttemptForNote = null },
            title = { Text("Annotate Mistake") },
            text = {
                Column {
                    Text(
                        text = "Add personal reasoning, trap analysis, or formula reminders for future revisions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        label = { Text("Your Note") },
                        placeholder = { Text("e.g. Forgot to multiply by cache block size in formula...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val att = selectedAttemptForNote ?: return@Button
                        val q = questionMap[att.questionId]
                        if (q != null) {
                            // Re-save attempt with updated user note
                            viewModel.recordDirectAttempt(
                                question = q,
                                selectedAnswer = att.selectedAnswer,
                                isCorrect = att.isCorrect,
                                mistakeCategory = att.mistakeCategory,
                                userNote = newNoteText
                            )
                        }
                        selectedAttemptForNote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save Note", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAttemptForNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RoseError.copy(alpha = 0.18f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = RoseError,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dedicated Error Notebook",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${mistakeAttempts.size} Recorded Errors",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        if (mistakeAttempts.isNotEmpty()) {
                            Button(
                                onClick = { onNavigateToPractice("MISTAKES", 10) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("drill_mistakes_button")
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Drill Errors", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Review every conceptual trap, calculation error, and time-pressure lapse. Categorizing mistakes converts weaknesses into exam-day reflexes.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Mistake Category Filter Chips
        item {
            Text(
                text = "Filter by Root Cause",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${mistakeAttempts.size})") }
                    )
                }
                items(MistakeCategory.values()) { cat ->
                    val count = mistakeAttempts.count { it.mistakeCategory == cat }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text("${cat.name.replace("_", " ")} ($count)") }
                        )
                    }
                }
            }
        }

        if (filteredAttempts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (mistakeAttempts.isEmpty()) "Zero Mistakes Recorded!" else "No mistakes in this category.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Solve questions in the Practice Engine to log and analyze mistakes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredAttempts, key = { it.id }) { attempt ->
                val question = questionMap[attempt.questionId]
                val cat = attempt.mistakeCategory ?: MistakeCategory.CONCEPTUAL

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
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
                                color = when (cat) {
                                    MistakeCategory.CONCEPTUAL -> IndigoAccent.copy(alpha = 0.18f)
                                    MistakeCategory.CALCULATION -> AmberAccent.copy(alpha = 0.18f)
                                    MistakeCategory.SILLY_MISTAKE -> RoseError.copy(alpha = 0.18f)
                                    MistakeCategory.TIME_PRESSURE -> Color(0xFFF97316).copy(alpha = 0.18f)
                                    else -> CyanPrimary.copy(alpha = 0.18f)
                                }
                            ) {
                                Text(
                                    text = cat.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (cat) {
                                            MistakeCategory.CONCEPTUAL -> IndigoAccent
                                            MistakeCategory.CALCULATION -> AmberAccent
                                            MistakeCategory.SILLY_MISTAKE -> RoseError
                                            MistakeCategory.TIME_PRESSURE -> Color(0xFFF97316)
                                            else -> CyanPrimary
                                        }
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            val timeStr = remember(attempt.timestamp) {
                                SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(attempt.timestamp))
                            }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (question != null) {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your: ${attempt.selectedAnswer}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = RoseError,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Correct: ${question.correctAnswers}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (!question.shortExplanation.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = question.shortExplanation,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                        if (!attempt.userNote.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = attempt.userNote,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AmberAccent,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    newNoteText = attempt.userNote.orEmpty()
                                    selectedAttemptForNote = attempt
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (attempt.userNote.isNullOrBlank()) "Add Note" else "Edit Note", fontSize = 12.sp)
                            }

                            if (question != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { onAskAi(question.id) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ask AI", fontSize = 12.sp, color = CyanPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
