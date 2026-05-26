package com.example.montesorrilearning.ui.teacher

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.montesorrilearning.domain.model.Child
import com.example.montesorrilearning.domain.model.ChildProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProgressScreen(
    children: List<Child>,
    selectedChild: Child?,
    progressRecords: List<ChildProgress>,
    isLoading: Boolean,
    error: String?,
    successMessage: String?,
    onChildSelected: (Child) -> Unit,
    onUpdateStatus: (childId: String, syllabusId: String?, status: String, notes: String?) -> Unit,
    onBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    var statusDialog by remember { mutableStateOf<ChildProgress?>(null) }
    var selectedStatus by remember { mutableStateOf("pending") }
    var observationNotes by remember { mutableStateOf("") }

    LaunchedEffect(successMessage) {
        if (successMessage != null) { kotlinx.coroutines.delay(1500); onClearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Progress") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp)); Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            if (children.isNotEmpty()) {
                ScrollableTabRow(selectedTabIndex = children.indexOf(selectedChild).coerceAtLeast(0), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), edgePadding = 0.dp) {
                    children.forEach { child -> Tab(selected = child == selectedChild, onClick = { onChildSelected(child) }, text = { Text(child.name, style = MaterialTheme.typography.labelSmall) }) }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else if (selectedChild == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Select a child to view progress", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else if (progressRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(modifier = Modifier.height(12.dp)); Text("No progress records yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(progressRecords, key = { it.id }) { record ->
                        AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400))) {
                            Card(onClick = { selectedStatus = record.status; observationNotes = record.observationNotes ?: ""; statusDialog = record },
                                shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp).animateContentSize(), verticalAlignment = Alignment.CenterVertically) {
                                    StatusIcon(record.status)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(record.displayTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(record.statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor(record.status))
                                        if (!record.observationNotes.isNullOrBlank()) {
                                            Text(record.observationNotes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
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

    statusDialog?.let { record ->
        AlertDialog(
            onDismissRequest = { statusDialog = null },
            title = { Text(record.displayTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Status", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("pending", "in_progress", "completed", "mastered").forEach { s ->
                            FilterChip(selected = selectedStatus == s, onClick = { selectedStatus = s }, label = { Text(s.replace("_", " "), style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(value = observationNotes, onValueChange = { observationNotes = it }, label = { Text("Observation Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2, maxLines = 4)
                }
            },
            confirmButton = { TextButton(onClick = { onUpdateStatus(selectedChild?.id ?: "", record.syllabusId, selectedStatus, observationNotes); statusDialog = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { statusDialog = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatusIcon(status: String) {
    val (icon, tint) = when (status) {
        "mastered" -> Icons.Default.AutoAwesome to MaterialTheme.colorScheme.tertiary
        "completed" -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        "in_progress" -> Icons.Default.Sync to MaterialTheme.colorScheme.secondary
        else -> Icons.Default.RadioButtonUnchecked to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
}

@Composable
private fun statusColor(status: String): Color = when (status) {
    "mastered" -> MaterialTheme.colorScheme.tertiary
    "completed" -> MaterialTheme.colorScheme.primary
    "in_progress" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
