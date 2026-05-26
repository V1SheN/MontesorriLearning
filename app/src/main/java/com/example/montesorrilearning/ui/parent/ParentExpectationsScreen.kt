package com.example.montesorrilearning.ui.parent

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.montesorrilearning.domain.model.Syllabus
import com.example.montesorrilearning.domain.model.ChildProgress
import com.example.montesorrilearning.domain.model.Term

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentExpectationsScreen(
    syllabus: List<Syllabus>,
    progress: List<ChildProgress>,
    terms: List<Term>,
    selectedTerm: Term?,
    selectedWeek: Int,
    isLoading: Boolean,
    onTermSelected: (Term) -> Unit,
    onWeekSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val dayLabels = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("This Week's Plan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
        ) {
            // Term tabs
            if (terms.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = terms.indexOf(selectedTerm).coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    edgePadding = 0.dp
                ) {
                    terms.forEach { term ->
                        Tab(selected = term == selectedTerm, onClick = { onTermSelected(term) }, text = { Text(term.name, style = MaterialTheme.typography.labelMedium) })
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (selectedWeek > 1) onWeekSelected(selectedWeek - 1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                }
                Text("Week $selectedWeek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onWeekSelected(selectedWeek + 1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (syllabus.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No activities planned for this week", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dayLabels.forEachIndexed { index, dayLabel ->
                        val dayNum = index + 1
                        val dayItems = syllabus.filter { it.dayOfWeek == dayNum }

                        if (dayItems.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(dayLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            items(dayItems, key = { it.id }) { item ->
                                AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400))) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (item.isExtracurricular) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp).animateContentSize(), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (item.isExtracurricular) Icons.Default.Star else Icons.Default.School,
                                                contentDescription = null,
                                                tint = if (item.isExtracurricular) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                                Text(item.areaDisplayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (item.description.isNotBlank()) {
                                                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                                                }
                                            }
                                            // Show progress status if available
                                            val prog = progress.find { it.syllabusId == item.id }
                                            if (prog != null) {
                                                Text(
                                                    prog.statusLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = when (prog.status) {
                                                        "mastered" -> MaterialTheme.colorScheme.tertiary
                                                        "completed" -> MaterialTheme.colorScheme.primary
                                                        "in_progress" -> MaterialTheme.colorScheme.secondary
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
