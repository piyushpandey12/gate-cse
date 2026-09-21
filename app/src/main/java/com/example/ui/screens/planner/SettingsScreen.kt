package com.example.ui.screens.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.network.AiProviderType
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GateViewModel,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAiProvider by viewModel.aiProviderType.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val examConfig by viewModel.examConfig.collectAsState()

    var ollamaUrl by remember { mutableStateOf("http://10.0.2.2:11434") }
    var ollamaModel by remember { mutableStateOf("qwen3:0.6b") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Clean Seed Dataset?") },
            text = {
                Text(
                    "This action will clear all user attempt logs, test session results, custom notes, and reset topic mastery to baseline. The official GATE 2027 syllabus, questions, formulas, and resources will be cleanly reloaded."
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
                    Text("Yes, Reset All Data", color = Color.White, fontWeight = FontWeight.Bold)
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Application Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Configure AI intelligence engines, local services, and database integrity",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
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
                            text = "Database successfully reset to clean GATE 2027 state!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 1. AI Engine Configuration
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Pedagogical Provider",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Choose how the AI Tutor processes questions, derivations, and hints:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Choices
                    val providers = listOf(
                        Triple(AiProviderType.OLLAMA, "Ollama Local (Default)", "Self-hosted local LLM at http://10.0.2.2:11434 (qwen3:0.6b)"),
                        Triple(AiProviderType.GEMINI, "Google Gemini Cloud", "Gemini 3.5 Flash cloud API (configured via Secrets panel)"),
                        Triple(AiProviderType.OFFLINE_KNOWLEDGE, "Verified Textbook KB (Offline)", "Zero network required. Uses verified textbook solutions.")
                    )

                    providers.forEach { (type, title, desc) ->
                        val isSelected = activeAiProvider == type
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setAiProviderType(type) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setAiProviderType(type) },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Ollama detailed config if selected
                    if (activeAiProvider == AiProviderType.OLLAMA) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ollama Host & Model Parameters",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = ollamaUrl,
                            onValueChange = {
                                ollamaUrl = it
                                viewModel.configureOllama(ollamaUrl, ollamaModel)
                            },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ollamaModel,
                            onValueChange = {
                                ollamaModel = it
                                viewModel.configureOllama(ollamaUrl, ollamaModel)
                            },
                            label = { Text("Model Tag") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // 2. Goal Preferences Shortcut
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(18.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Candidate Goals & Study Routine",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Target: ${userProfile?.dailyTargetHours ?: 3.5f}h • ${userProfile?.dailyQuestionsTarget ?: 15} Questions/day • ${userProfile?.preferredStudyTime ?: "Morning"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanPrimary)
                }
            }
        }

        // 3. Database Maintenance & Clean Reset
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = RoseError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Database Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reset test attempts, purge cached session logs, and reseed the clean official GATE 2027 curriculum database.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_database_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = RoseError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Database to Clean State", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. About & Verified Authority
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "About GATE CSE 2027 Platform",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Curriculum: Official GATE CS & IT Syllabus\nAuthority: Indian Institute of Technology (IIT)\nOfficial Portal: ${examConfig?.officialWebsiteUrl ?: "https://gate2027.iit.ac.in"}\nVersion: 2.0.0 (Production Release)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}
