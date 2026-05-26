package com.example.montesorrilearning.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.montesorrilearning.domain.model.Syllabus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusEditScreen(
    existing: Syllabus?,
    isLoading: Boolean,
    error: String?,
    successMessage: String?,
    onSave: (termId: String, classroomId: String, montessoriArea: String, title: String, description: String, dayOfWeek: Int, weekNumber: Int?, sortOrder: Int, isExtracurricular: Boolean, activityType: String?) -> Unit,
    onBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    val isEditing = existing != null
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var selectedArea by remember { mutableStateOf(existing?.montessoriArea ?: "practical_life") }
    var dayOfWeek by remember { mutableStateOf(existing?.dayOfWeek?.toString() ?: "1") }
    var weekNumber by remember { mutableStateOf(existing?.weekNumber?.toString() ?: "") }
    var sortOrder by remember { mutableStateOf(existing?.sortOrder?.toString() ?: "0") }
    var isExtracurricular by remember { mutableStateOf(existing?.isExtracurricular ?: false) }
    var activityType by remember { mutableStateOf(existing?.activityType ?: "") }
    var areaExpanded by remember { mutableStateOf(false) }

    val areaOptions = listOf(
        "practical_life" to "Practical Life", "sensorial" to "Sensorial",
        "language" to "Language", "math" to "Mathematics",
        "cultural" to "Cultural Studies", "extracurricular" to "Extra-Curricular"
    )
    val dayOptions = listOf("1" to "Monday", "2" to "Tuesday", "3" to "Wednesday", "4" to "Thursday", "5" to "Friday")

    LaunchedEffect(successMessage) {
        if (successMessage != null) { kotlinx.coroutines.delay(1500); onClearMessages(); onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Syllabus Item" else "New Syllabus Item") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp)); Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            ExposedDropdownMenuBox(expanded = areaExpanded, onExpandedChange = { areaExpanded = !areaExpanded }) {
                OutlinedTextField(value = areaOptions.find { it.first == selectedArea }?.second ?: selectedArea, onValueChange = {}, readOnly = true, label = { Text("Montessori Area") }, leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = areaExpanded, onDismissRequest = { areaExpanded = false }) {
                    areaOptions.forEach { (value, label) -> DropdownMenuItem(text = { Text(label) }, onClick = { selectedArea = value; areaExpanded = false }) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = false, onExpandedChange = { }) {
                    OutlinedTextField(value = dayOptions.find { it.first == dayOfWeek }?.second ?: "Monday", onValueChange = {}, readOnly = true, label = { Text("Day") }, modifier = Modifier.weight(1f).menuAnchor(), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                OutlinedTextField(value = weekNumber, onValueChange = { weekNumber = it.filter { c -> c.isDigit() } }, label = { Text("Week #") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2, maxLines = 4)

            // Extracurricular toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isExtracurricular, onCheckedChange = { isExtracurricular = it })
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Extra-Curricular Activity", style = MaterialTheme.typography.titleSmall)
                    if (isExtracurricular) {
                        Text("Alternative activities like sports, music, art", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (isExtracurricular) {
                OutlinedTextField(value = activityType, onValueChange = { activityType = it }, label = { Text("Activity Type") }, leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }, placeholder = { Text("e.g. music, sports, art, outdoor") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = sortOrder, onValueChange = { sortOrder = it.filter { c -> c.isDigit() } }, label = { Text("Sort Order") }, leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    onSave("", "", selectedArea, title.trim(), description.trim(),
                        dayOfWeek.toIntOrNull() ?: 1, weekNumber.toIntOrNull(),
                        sortOrder.toIntOrNull() ?: 0, isExtracurricular,
                        activityType.ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = title.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) { CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) }
                else { Icon(Icons.Default.Save, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text(if (isEditing) "Update" else "Create") }
            }
        }
    }
}
