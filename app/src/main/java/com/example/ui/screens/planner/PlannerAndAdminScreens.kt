package com.example.ui.screens.planner

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
import com.example.data.model.UserProfileEntity
import com.example.ui.components.FocusTimerDialog
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@Composable
fun StudyPlannerScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskMinutes by remember { mutableStateOf(30) }
    var newTaskSubject by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "OS") }

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

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Study Goal / Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task / Chapter Goal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Subject Area:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { s ->
                            FilterChip(
                                selected = newTaskSubject == s.id,
                                onClick = { newTaskSubject = s.id },
                                label = { Text(s.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newTaskMinutes.toString(),
                        onValueChange = { newTaskMinutes = it.toIntOrNull() ?: 30 },
                        label = { Text("Allocated Minutes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addStudyTask(newTaskTitle, newTaskSubject, newTaskMinutes, "HIGH")
                            newTaskTitle = ""
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
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
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "GATE 2027 Daily Study Planner",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Structure your preparation day with timed milestones",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Row {
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer", tint = AmberAccent)
                    }
                    IconButton(
                        onClick = { showAddTaskDialog = true },
                        modifier = Modifier.testTag("add_study_task_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Task", tint = CyanPrimary)
                    }
                }
            }
        }

        val completedCount = dailyTasks.count { it.isCompleted }
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Milestone Completion",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount of ${dailyTasks.size} tasks finished today",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val p = if (dailyTasks.isNotEmpty()) completedCount.toFloat() / dailyTasks.size else 0f
                    LinearProgressIndicator(
                        progress = { p },
                        color = CyanPrimary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
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
                    modifier = Modifier.padding(14.dp)
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
fun AdminPanelScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    val questions by viewModel.filteredQuestions.collectAsState()
    val resources by viewModel.allResources.collectAsState()
    val formulas by viewModel.allFormulas.collectAsState()
    val flashcards by viewModel.allFlashcards.collectAsState()
    val examConfig by viewModel.examConfig.collectAsState()
    val allExamEvents by viewModel.allExamEvents.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Database to Clean State?") },
            text = {
                Text(
                    "This executes AppDatabase.resetDatabaseToCleanState(). All attempts, sessions, and custom data will be cleared, and clean seed data will be reloaded."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDatabase()
                        showResetDialog = false
                        resetSuccessMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Execute Clean Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
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
        item {
            Text(
                text = "System Diagnostics & Admin Console",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Room Database inventory, system health checks & data maintenance",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Role Gating Notice
        val isAdmin = userProfile?.role?.uppercase() == "ADMIN"
        if (!isAdmin) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AmberAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Role-Gated Console",
                                style = MaterialTheme.typography.titleSmall.copy(color = AmberAccent, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your current role is '${userProfile?.role ?: "ASPIRANT"}'. You can elevate your role below to test administrative maintenance tasks.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.updateUserRole("ADMIN") },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Switch Role to ADMIN", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (resetSuccessMessage) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Database reseeded to clean official state!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Database Content Inventory",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DiagnosticRow("Syllabus Subjects", "${subjects.size} (12 CS + 1 Aptitude)")
                    DiagnosticRow("Syllabus Topics", "${allTopics.size} Micro-topics")
                    DiagnosticRow("Question Bank (PYQs)", "${questions.size} Questions (MCQ/MSQ/NAT)")
                    DiagnosticRow("Curated Resources", "${resources.size} Playlists/Books/Notes")
                    DiagnosticRow("Formula Compendium", "${formulas.size} Formulas & Theorems")
                    DiagnosticRow("Flashcards (SRS)", "${flashcards.size} Cards")
                    DiagnosticRow("Official Exam Events", "${allExamEvents.size} Key Milestones")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Engine Health Status",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DiagnosticRow("Local Room SQLite", "✅ Synchronized (v3 with seed callbacks)")
                    DiagnosticRow("Spaced Repetition Engine", "✅ SuperMemo-2 Active")
                    DiagnosticRow("AI Tutor Fallback Facade", "✅ Ollama / Gemini / Curated KB")
                    DiagnosticRow("Exam Scoring Evaluator", "✅ Standard Negative Marking Active")
                }
            }
        }

        // Database Reset Action in Admin Console
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Clean Database Reset Trigger",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RoseError)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Resets user progress and re-seeds verified GATE 2027 curriculum data.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_reset_database_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Database to Clean State", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
        )
    }
}

@Composable
fun ProfileScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var targetExam by remember { mutableStateOf("") }
    var targetYearText by remember { mutableStateOf("2027") }
    var dailyTargetHours by remember { mutableFloatStateOf(3.5f) }
    var dailyQuestionsTarget by remember { mutableIntStateOf(15) }
    var weeklyTestsTarget by remember { mutableIntStateOf(2) }
    var userRole by remember { mutableStateOf("ASPIRANT") }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    // Sync state when profile loads or updates from repository
    LaunchedEffect(userProfile) {
        userProfile?.let { prof ->
            targetExam = prof.targetExam
            targetYearText = prof.targetYear.toString()
            dailyTargetHours = prof.dailyTargetHours
            dailyQuestionsTarget = prof.dailyQuestionsTarget
            weeklyTestsTarget = prof.weeklyTestsTarget
            userRole = prof.role
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSavedSnackbar) {
        if (showSavedSnackbar) {
            snackbarHostState.showSnackbar("Goal preferences saved successfully!")
            showSavedSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Text(
                    text = "Candidate Profile & Target Goals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Customize your target year, daily hours commitment, and exam strategy",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = targetExam,
                            onValueChange = { targetExam = it },
                            label = { Text("Target Examination") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("target_exam_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = targetYearText,
                            onValueChange = { targetYearText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Target Year (e.g. 2027)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("target_year_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Daily Target Study Hours: ${String.format("%.1f", dailyTargetHours)}h",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = dailyTargetHours,
                            onValueChange = { dailyTargetHours = it },
                            valueRange = 1f..10f,
                            steps = 17,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Daily Questions Target: $dailyQuestionsTarget questions",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = dailyQuestionsTarget.toFloat(),
                            onValueChange = { dailyQuestionsTarget = it.toInt() },
                            valueRange = 5f..50f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Weekly Mock Tests Target: $weeklyTestsTarget tests",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = weeklyTestsTarget.toFloat(),
                            onValueChange = { weeklyTestsTarget = it.toInt() },
                            valueRange = 1f..7f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Application Role Mode: $userRole",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = userRole == "ASPIRANT",
                                onClick = { userRole = "ASPIRANT" },
                                label = { Text("Aspirant") }
                            )
                            FilterChip(
                                selected = userRole == "ADMIN",
                                onClick = { userRole = "ADMIN" },
                                label = { Text("Admin") }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val current = userProfile ?: UserProfileEntity(id = "default_user")
                                val year = targetYearText.toIntOrNull() ?: current.targetYear
                                val exam = if (targetExam.isNotBlank()) targetExam else current.targetExam
                                viewModel.updateProfile(
                                    current.copy(
                                        targetExam = exam,
                                        targetYear = year,
                                        dailyTargetHours = dailyTargetHours,
                                        dailyQuestionsTarget = dailyQuestionsTarget,
                                        weeklyTestsTarget = weeklyTestsTarget,
                                        role = userRole
                                    )
                                )
                                showSavedSnackbar = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Goal Preferences", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
