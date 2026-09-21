package com.example.ui.screens.pyq

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.QuestionType
import com.example.ui.components.QuestionCard
import com.example.ui.theme.CyanPrimary
import com.example.viewmodel.GateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqExplorerScreen(
    viewModel: GateViewModel,
    onAskAiQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.filteredQuestions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()

    val selectedSubject by viewModel.selectedSubjectFilter.collectAsState()
    val selectedYear by viewModel.selectedYearFilter.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficultyFilter.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val displayedQuestions = remember(questions, searchQuery) {
        if (searchQuery.isBlank()) questions
        else questions.filter {
            it.questionText.contains(searchQuery, ignoreCase = true) ||
            it.topicId.contains(searchQuery, ignoreCase = true) ||
            it.detailedSolution.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Official GATE PYQ Explorer (1987-2026)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Filter by year, subject, question type, and solve directly with instant validation",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search question text, concepts, or formulas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pyq_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Horizontal Filters: Subjects
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedSubject == null,
                    onClick = { viewModel.selectedSubjectFilter.value = null },
                    label = { Text("All Subjects") }
                )
                subjects.forEach { sub ->
                    FilterChip(
                        selected = selectedSubject == sub.id,
                        onClick = {
                            viewModel.selectedSubjectFilter.value = if (selectedSubject == sub.id) null else sub.id
                        },
                        label = { Text(sub.code) }
                    )
                }
            }
        }

        // Horizontal Filters: Years & Question Type
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedYear == null,
                    onClick = { viewModel.selectedYearFilter.value = null },
                    label = { Text("All Years") }
                )
                availableYears.forEach { yr ->
                    FilterChip(
                        selected = selectedYear == yr,
                        onClick = {
                            viewModel.selectedYearFilter.value = if (selectedYear == yr) null else yr
                        },
                        label = { Text("$yr") }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                listOf(QuestionType.MCQ, QuestionType.MSQ, QuestionType.NAT).forEach { qType ->
                    FilterChip(
                        selected = selectedType == qType,
                        onClick = {
                            viewModel.selectedTypeFilter.value = if (selectedType == qType) null else qType
                        },
                        label = { Text(qType.name) }
                    )
                }
            }
        }

        // Question Count Summary
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Showing ${displayedQuestions.size} Questions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (selectedSubject != null || selectedYear != null || selectedType != null) {
                    TextButton(onClick = {
                        viewModel.selectedSubjectFilter.value = null
                        viewModel.selectedYearFilter.value = null
                        viewModel.selectedTypeFilter.value = null
                        viewModel.selectedDifficultyFilter.value = null
                    }) {
                        Text("Reset Filters", fontSize = 11.sp, color = CyanPrimary)
                    }
                }
            }
        }

        // List of Question Cards
        items(displayedQuestions) { question ->
            var userAns by remember(question.id) { mutableStateOf("") }
            var isEval by remember(question.id) { mutableStateOf(false) }

            QuestionCard(
                question = question,
                selectedAnswer = userAns,
                isEvaluated = isEval,
                lastAttempt = null,
                onAnswerSelected = { ans -> userAns = ans },
                onSubmitAnswer = { _, _ -> isEval = true },
                onNextQuestion = { /* In explorer, stays on item */ },
                onToggleBookmark = { cur -> viewModel.toggleBookmark(question.id, cur) },
                onAskAi = { onAskAiQuestion(question.id) }
            )
        }
    }
}
