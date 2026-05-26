package com.example.montesorrilearning.ui.admin

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.montesorrilearning.domain.model.Syllabus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusListScreen(
    syllabus: List<Syllabus>,
    isLoading: Boolean,
    error: String?,
    selectedArea: String?,
    selectedWeek: Int?,
    onAreaFilter: (String?) -> Unit,
    onWeekFilter: (Int?) -> Unit,
    onItemClick: (Syllabus) -> Unit,
    onAdd: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Syllabus?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Syllabus") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(Icons.Default.Add, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            // Filters
            AreaFilterChips(selectedArea = selectedArea, onAreaSelected = onAreaFilter)
            WeekFilterChips(selectedWeek = selectedWeek, onWeekSelected = onWeekFilter)

            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(error, color = MaterialTheme.colorScheme.error); Spacer(modifier = Modifier.height(8.dp)); TextButton(onClick = onRefresh) { Text("Retry") } } }
                syllabus.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(modifier = Modifier.height(12.dp)); Text("No syllabus items", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(syllabus, key = { it.id }) { item ->
                        AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400))) {
                            SyllabusCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaFilterChips(selectedArea: String?, onAreaSelected: (String?) -> Unit) {
    val areas = listOf(null to "All", "practical_life" to "Practical Life", "sensorial" to "Sensorial", "language" to "Language", "math" to "Math", "cultural" to "Cultural", "extracurricular" to "Extra-Curricular")
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        areas.forEach { (value, label) ->
            FilterChip(selected = selectedArea == value, onClick = { onAreaSelected(value) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), selectedLabelColor = MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
private fun WeekFilterChips(selectedWeek: Int?, onWeekSelected: (Int?) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selectedWeek == null, onClick = { onWeekSelected(null) }, label = { Text("All", style = MaterialTheme.typography.labelSmall) })
        (1..5).forEach { w ->
            FilterChip(selected = selectedWeek == w, onClick = { onWeekSelected(w) }, label = { Text("W$w", style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
private fun SyllabusCard(item: Syllabus, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (item.isExtracurricular) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp).animateContentSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text(item.dayLabel, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.height(24.dp), colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant))
                    if (item.weekNumber != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AssistChip(onClick = {}, label = { Text("W${item.weekNumber}", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.height(24.dp), colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant))
                    }
                }
                Text(item.areaDisplayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (item.description.isNotBlank()) {
                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
