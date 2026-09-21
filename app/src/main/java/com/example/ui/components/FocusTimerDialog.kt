package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubjectEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RoseError
import kotlinx.coroutines.delay

@Composable
fun FocusTimerDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSaveSession: (String, String, Int, String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "OS") }
    var topicName by remember { mutableStateOf("Core Concepts & PYQ Revision") }
    var selectedMinutes by remember { mutableStateOf(25) }
    var isRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableStateOf(25 * 60) }

    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            onSaveSession(selectedSubject, topicName, selectedMinutes, "POMODORO")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("focus_timer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏱️ Deep Work & Focus Timer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Track your study sessions and build your streak",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Display
                val mins = secondsRemaining / 60
                val secs = secondsRemaining % 60
                val formatted = String.format("%02d:%02d", mins, secs)

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRunning && secondsRemaining == selectedMinutes * 60) {
                    // Duration Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        listOf(15, 25, 45, 60).forEach { dur ->
                            FilterChip(
                                selected = selectedMinutes == dur,
                                onClick = {
                                    selectedMinutes = dur
                                    secondsRemaining = dur * 60
                                },
                                label = { Text("${dur}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    // Subject Selector
                    OutlinedTextField(
                        value = topicName,
                        onValueChange = { topicName = it },
                        label = { Text("Topic / Study Objective") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isRunning) {
                        Button(
                            onClick = { isRunning = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.weight(1f).testTag("start_timer_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Focus", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { isRunning = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }

                        Button(
                            onClick = {
                                val completedMins = (selectedMinutes * 60 - secondsRemaining) / 60
                                if (completedMins > 0) {
                                    onSaveSession(selectedSubject, topicName, completedMins, "FOCUS")
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Finish & Log")
                        }
                    }
                }
            }
        }
    }
}
