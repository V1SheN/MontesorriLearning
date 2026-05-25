package com.example.montesorrilearning.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit = {},
    onNavigateToClassrooms: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(onClick = onNavigateToUsers, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("User Management")
            }
            FilledTonalButton(onClick = onNavigateToClassrooms, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Classroom Management")
            }
            FilledTonalButton(onClick = onNavigateToAnalytics, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Analytics")
            }
        }
    }
}
