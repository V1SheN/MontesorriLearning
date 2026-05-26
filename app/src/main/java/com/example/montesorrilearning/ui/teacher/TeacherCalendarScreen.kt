package com.example.montesorrilearning.ui.teacher

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.montesorrilearning.domain.model.Syllabus
import com.example.montesorrilearning.domain.model.TeacherPlan
import com.example.montesorrilearning.domain.model.Term

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCalendarScreen(
    syllabus: List<Syllabus>,
    myPlans: List<TeacherPlan>,
    terms: List<Term>,
    selectedTerm: Term?,
    selectedWeek: Int,
    isLoading: Boolean,
    error: String?,
    successMessage: String?,
    onTermSelected: (Term) -> Unit,
    onWeekSelected: (Int) -> Unit,
    onToggleComplete: (planId: String, isCompleted: Boolean) -> Unit,
    onDeletePlan: (String) -> Unit,
    onAddPlan: (syllabusItem: Syllabus?) -> Unit,
    onBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    LaunchedEffect(successMessage) {
        if (successMessage != null) { kotlinx.coroutines.delay(1500); onClearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Planner") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
        ) {
            if (error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp)); Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Term selector
            if (terms.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = terms.indexOf(selectedTerm).coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    edgePadding = 0.dp
                ) {
                    terms.forEach { term ->
                        Tab(
                            selected = term == selectedTerm,
                            onClick = { onTermSelected(term) },
                            text = { Text(term.name, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Week selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (selectedWeek > 1) onWeekSelected(selectedWeek - 1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week")
                }
                Text("Week $selectedWeek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onWeekSelected(selectedWeek + 1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next week")
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dayLabels.forEachIndexed { index, dayLabel ->
                        val dayNum = index + 1
                        val daySyllabus = syllabus.filter { it.dayOfWeek == dayNum }
                        val dayPlans = myPlans.filter { it.dayOfWeek == dayNum }

                        if (daySyllabus.isNotEmpty() || dayPlans.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(dayLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            item {
                                daySyllabus.forEach { sItem ->
                                    AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400))) {
                                        Card(onClick = { onAddPlan(sItem) }, shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (sItem.isExtracurricular) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp).animateContentSize(), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(if (sItem.isExtracurricular) Icons.Default.Star else Icons.Default.CheckCircleOutline, contentDescription = null,
                                                    tint = if (sItem.isExtracurricular) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(sItem.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                                    Text(sItem.areaDisplayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            item {
                                dayPlans.forEach { plan ->
                                    AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400))) {
                                        Card(shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (plan.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp).animateContentSize(), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = plan.isCompleted, onCheckedChange = { onToggleComplete(plan.id, plan.isCompleted) })
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(plan.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, textDecoration = if (plan.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                                                    if (plan.teacherNotes != null) { Text(plan.teacherNotes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                                }
                                                IconButton(onClick = { onDeletePlan(plan.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
