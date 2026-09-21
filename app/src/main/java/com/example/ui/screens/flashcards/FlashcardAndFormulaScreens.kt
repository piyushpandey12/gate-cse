package com.example.ui.screens.flashcards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlashcardEntity
import com.example.ui.components.MathFormulaView
import com.example.ui.theme.*
import com.example.viewmodel.GateViewModel

@Composable
fun FlashcardScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val flashcards by viewModel.allFlashcards.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val currentCard = flashcards.getOrNull(currentIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Active Recall Flashcards (SRS)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Review core definitions, algorithm complexities, and theorems",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (currentCard == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No flashcards available.")
            }
            return
        }

        // Card Counter & Progress
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Card ${currentIndex + 1} of ${flashcards.size}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${currentCard.subjectId} • ${currentCard.keyConcept}",
                style = MaterialTheme.typography.labelSmall.copy(color = CyanPrimary)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Interactive Flashcard
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isFlipped) EmeraldSuccess.copy(alpha = 0.6f) else CyanPrimary.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { isFlipped = !isFlipped }
                .testTag("flashcard_interactive_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFlipped) EmeraldSuccess.copy(alpha = 0.15f) else CyanPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isFlipped) "ANSWER / DERIVATION" else "QUESTION / CONCEPT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isFlipped) EmeraldSuccess else CyanPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (isFlipped) currentCard.back else currentCard.front,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (isFlipped) "Tap to flip back" else "Tap card to reveal answer",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SRS Rating Buttons
        AnimatedVisibility(visible = isFlipped) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Rate your recall ease:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(currentCard, 1)
                            isFlipped = false
                            currentIndex = (currentIndex + 1) % flashcards.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Again\n(1d)", textAlign = TextAlign.Center, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(currentCard, 2)
                            isFlipped = false
                            currentIndex = (currentIndex + 1) % flashcards.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hard\n(3d)", textAlign = TextAlign.Center, color = Color.Black, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(currentCard, 3)
                            isFlipped = false
                            currentIndex = (currentIndex + 1) % flashcards.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Good\n(6d)", textAlign = TextAlign.Center, color = Color.Black, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.reviewFlashcard(currentCard, 5)
                            isFlipped = false
                            currentIndex = (currentIndex + 1) % flashcards.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Easy\n(10d)", textAlign = TextAlign.Center, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
fun FormulaSheetScreen(
    viewModel: GateViewModel,
    modifier: Modifier = Modifier
) {
    val formulas by viewModel.filteredFormulas.collectAsState()
    val searchQuery by viewModel.formulaSearchQuery.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Official Formula & Theorem Compendium",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Rapid reference sheets covering OS paging, DBMS normal forms, Graph theory, and Asymptotics",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.formulaSearchQuery.value = it },
                placeholder = { Text("Search formulas (e.g. Page Fault, EAT, Master Theorem)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("formula_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(formulas) { formula ->
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = formula.subjectId,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleFormulaFavorite(formula.id, formula.isFavorite) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (formula.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (formula.isFavorite) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formula.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MathFormulaView(
                        rawFormula = formula.formulaLatex,
                        title = if (!formula.applications.isNullOrBlank()) "Application: ${formula.applications}" else null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formula.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}
