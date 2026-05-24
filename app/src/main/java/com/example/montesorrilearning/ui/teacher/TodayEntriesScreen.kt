package com.example.montesorrilearning.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.montesorrilearning.domain.model.WorkEntry
import com.example.montesorrilearning.ui.theme.*
import com.example.montesorrilearning.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayEntriesScreen(
    entries: List<WorkEntry>,
    onEntryClick: (WorkEntry) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Entries") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCream)
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No entries yet today", color = WarmBrown)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SurfaceLight),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries) { entry ->
                    EntryCard(
                        entry = entry,
                        onClick = { onEntryClick(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryCard(
    entry: WorkEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val thumbnailUrl = entry.media.firstOrNull()?.thumbnailKey
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.childName ?: "Child",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmBrownDark
                )
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmBrown
                )
                Text(
                    text = entry.montessoriArea,
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGreenDark
                )
                if (entry.teacherComment.isNotBlank()) {
                    Text(
                        text = entry.teacherComment,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = DateUtils.formatTime(entry.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmBrown.copy(alpha = 0.6f)
                )
            }

            if (entry.media.size > 1) {
                Icon(Icons.Default.Collections, contentDescription = "Multiple photos", modifier = Modifier.size(16.dp), tint = WarmBrown)
            }
        }
    }
}
