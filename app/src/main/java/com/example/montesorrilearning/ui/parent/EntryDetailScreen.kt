package com.example.montesorrilearning.ui.parent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.montesorrilearning.domain.model.WorkEntry
import com.example.montesorrilearning.ui.theme.*
import com.example.montesorrilearning.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entry: WorkEntry,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCream)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            entry.media.firstOrNull()?.let { cover ->
                AsyncImage(
                    model = cover.storageKey,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = WarmBrown)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = entry.childName ?: "Child",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmBrownDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SoftGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = entry.montessoriArea,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = SoftGreenDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = DateUtils.formatForDisplay(entry.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = WarmBrown
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = entry.teacherComment.ifEmpty { "No comment" },
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }

            if (entry.media.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("All Photos", style = MaterialTheme.typography.titleMedium, color = WarmBrownDark)
                Spacer(modifier = Modifier.height(8.dp))
                entry.media.forEach { media ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = media.storageKey,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
