package com.example.montesorrilearning.ui.parent

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
fun FeedScreen(
    feedEntries: List<WorkEntry>,
    totalEntries: Int,
    totalPhotos: Int,
    onEntryClick: (WorkEntry) -> Unit,
    onRefresh: () -> Unit,
    onArchive: () -> Unit,
    onMessages: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Feed") },
                actions = {
                    TextButton(onClick = onMessages) { Text("Messages") }
                    TextButton(onClick = onLogout) { Text("Logout") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmCream,
                    titleContentColor = WarmBrownDark
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DailySummaryCard(
                    totalEntries = totalEntries,
                    totalPhotos = totalPhotos,
                    onArchive = onArchive
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmBrownDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (feedEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No entries yet today", color = WarmBrown, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(feedEntries, key = { it.id }) { entry ->
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
private fun DailySummaryCard(
    totalEntries: Int,
    totalPhotos: Int,
    onArchive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$totalEntries entries today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmBrownDark
                )
                Text(
                    text = "$totalPhotos photos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmBrown
                )
            }
            FilledTonalButton(onClick = onArchive) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Archive")
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = WarmBrown
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = entry.childName ?: "Child",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmBrownDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = DateUtils.formatTime(entry.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmBrown.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val thumbnailUrl = entry.media.firstOrNull()?.thumbnailKey
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmBrownDark
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SoftGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = entry.montessoriArea,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGreenDark
                )
            }

            if (entry.teacherComment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.teacherComment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View full size")
            }
        }
    }
}
