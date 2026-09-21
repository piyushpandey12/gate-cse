package com.example.ui.screens.resources

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ResourceType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoAccent
import com.example.viewmodel.GateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceDirectoryScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val allResources by viewModel.allResources.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val uriHandler = LocalUriHandler.current

    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<ResourceType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var bookmarkedOnly by remember { mutableStateOf(false) }

    val filteredResources = remember(allResources, selectedSubjectId, selectedType, searchQuery, bookmarkedOnly) {
        allResources.filter { res ->
            (selectedSubjectId == null || res.subjectId == selectedSubjectId) &&
            (selectedType == null || res.resourceType == selectedType) &&
            (!bookmarkedOnly || res.isBookmarked) &&
            (searchQuery.isBlank() ||
                res.title.contains(searchQuery, ignoreCase = true) ||
                res.provider.contains(searchQuery, ignoreCase = true) ||
                (res.description.contains(searchQuery, ignoreCase = true)))
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
                text = "Curated GATE CSE Preparation Resources",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Hand-picked high-yield YouTube playlists, standard textbooks, NPTEL series, and toppers' notes",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by instructor, book name, or topic...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resource_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Subject Filter Scroll
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedSubjectId == null,
                    onClick = { selectedSubjectId = null },
                    label = { Text("All Subjects") }
                )
                subjects.forEach { sub ->
                    FilterChip(
                        selected = selectedSubjectId == sub.id,
                        onClick = { selectedSubjectId = if (selectedSubjectId == sub.id) null else sub.id },
                        label = { Text(sub.code) }
                    )
                }
            }
        }

        // Resource Type Filter Scroll
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All Types") }
                )
                ResourceType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = if (selectedType == type) null else type },
                        label = { Text(type.name.replace("_", " ")) }
                    )
                }

                FilterChip(
                    selected = bookmarkedOnly,
                    onClick = { bookmarkedOnly = !bookmarkedOnly },
                    label = { Text("⭐ Bookmarked") }
                )
            }
        }

        items(filteredResources) { res ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColor = when (res.resourceType) {
                                ResourceType.PLAYLIST -> AmberAccent
                                ResourceType.BOOK -> IndigoAccent
                                ResourceType.NPTEL -> CyanPrimary
                                ResourceType.NOTE -> EmeraldSuccess
                                ResourceType.REVISION_VIDEO -> AmberAccent
                                ResourceType.PRACTICE_SET -> Color(0xFFEC4899)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = res.resourceType.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = res.subjectId,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleResourceBookmark(res.id, res.isBookmarked) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (res.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (res.isBookmarked) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = res.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Instructor / Author: ${res.provider}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (res.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = res.description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                viewModel.toggleResourceCompleted(res.id, res.isCompleted)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (res.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = if (res.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (res.isCompleted) "Completed" else "Mark Done",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (res.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                try {
                                    uriHandler.openUri(res.url)
                                } catch (e: Exception) {
                                    // Fallback if URL cannot be parsed
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open Link",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Open Resource",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
