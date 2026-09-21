package com.example.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent

data class MoreHubItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val requiresAdmin: Boolean = false
)

@Composable
fun MoreScreen(
    userRole: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val curriculumItems = listOf(
        MoreHubItem(
            route = Screen.Subjects.route,
            title = "Subjects",
            subtitle = "10 core syllabus subjects & topics",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accentColor = CyanPrimary
        ),
        MoreHubItem(
            route = Screen.Planner.route,
            title = "Study Plan",
            subtitle = "Daily targets & completion tracking",
            icon = Icons.AutoMirrored.Filled.EventNote,
            accentColor = VioletAccent
        ),
        MoreHubItem(
            route = Screen.TestsList.route,
            title = "Tests & Mocks",
            subtitle = "Full-length CBT simulator & topic tests",
            icon = Icons.AutoMirrored.Filled.Assignment,
            accentColor = AmberAccent
        ),
        MoreHubItem(
            route = Screen.Mistakes.route,
            title = "Mistakes",
            subtitle = "Error notebook with root causes",
            icon = Icons.Default.ErrorOutline,
            accentColor = RoseError
        )
    )

    val memoryAndResourceItems = listOf(
        MoreHubItem(
            route = Screen.Notes.route,
            title = "Notes",
            subtitle = "Engineering concepts & personal notes",
            icon = Icons.Default.EditNote,
            accentColor = Color(0xFF10B981)
        ),
        MoreHubItem(
            route = Screen.Flashcards.route,
            title = "Flashcards",
            subtitle = "Active recall cards with SM-2 intervals",
            icon = Icons.Default.Style,
            accentColor = CyanPrimary
        ),
        MoreHubItem(
            route = Screen.Formulas.route,
            title = "Formula Sheet",
            subtitle = "Latex compendium & theorems",
            icon = Icons.Default.Functions,
            accentColor = AmberAccent
        ),
        MoreHubItem(
            route = Screen.Resources.route,
            title = "Resources",
            subtitle = "Curated PDF & faculty video directory",
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
            accentColor = VioletAccent
        )
    )

    val intelligenceAndSettingsItems = mutableListOf(
        MoreHubItem(
            route = Screen.Analytics.route,
            title = "Analytics",
            subtitle = "Accuracy breakdowns & velocity",
            icon = Icons.Default.BarChart,
            accentColor = Color(0xFF6366F1)
        ),
        MoreHubItem(
            route = Screen.AiTutor.route,
            title = "AI Tutor",
            subtitle = "Instant GATE CSE concept explanation",
            icon = Icons.Default.SmartToy,
            accentColor = CyanPrimary
        ),
        MoreHubItem(
            route = Screen.Settings.route,
            title = "Settings",
            subtitle = "Preferences, theme & offline cache",
            icon = Icons.Default.Settings,
            accentColor = Color(0xFF64748B)
        )
    )

    if (userRole?.uppercase() == "ADMIN") {
        intelligenceAndSettingsItems.add(
            MoreHubItem(
                route = Screen.Admin.route,
                title = "Admin Console",
                subtitle = "Diagnostics, database & role controls",
                icon = Icons.Default.AdminPanelSettings,
                accentColor = RoseError,
                requiresAdmin = true
            )
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen_grid")
    ) {
        // Section 1: Curriculum & Practice
        item(span = { GridItemSpan(2) }) {
            SectionHeader(title = "CURRICULUM & PRACTICE")
        }
        items(curriculumItems.size) { index ->
            val item = curriculumItems[index]
            HubCard(item = item, onClick = { onNavigate(item.route) })
        }

        // Section 2: Rapid Recall & Resources
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(title = "REVISION & RESOURCES")
        }
        items(memoryAndResourceItems.size) { index ->
            val item = memoryAndResourceItems[index]
            HubCard(item = item, onClick = { onNavigate(item.route) })
        }

        // Section 3: Intelligence & System
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(title = "INTELLIGENCE & SYSTEM")
        }
        items(intelligenceAndSettingsItems.size) { index ->
            val item = intelligenceAndSettingsItems[index]
            HubCard(item = item, onClick = { onNavigate(item.route) })
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun HubCard(
    item: MoreHubItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("hub_card_${item.title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(item.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                maxLines = 2
            )
        }
    }
}
