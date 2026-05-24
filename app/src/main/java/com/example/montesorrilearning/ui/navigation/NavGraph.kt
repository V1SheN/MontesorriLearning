package com.example.montesorrilearning.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.montesorrilearning.ui.auth.AuthViewModel
import com.example.montesorrilearning.ui.auth.LoginScreen
import com.example.montesorrilearning.ui.messaging.MessageListScreen
import com.example.montesorrilearning.ui.messaging.MessageThreadScreen
import com.example.montesorrilearning.ui.messaging.MessageViewModel
import com.example.montesorrilearning.ui.parent.ArchiveScreen
import com.example.montesorrilearning.ui.parent.EntryDetailScreen as ParentEntryDetailScreen
import com.example.montesorrilearning.ui.parent.FeedScreen
import com.example.montesorrilearning.ui.parent.ParentViewModel
import com.example.montesorrilearning.ui.teacher.*

object Routes {
    const val LOGIN = "login"
    const val TEACHER_DASHBOARD = "teacher_dashboard"
    const val CAPTURE = "capture/{childId}/{childName}"
    const val TODAY_ENTRIES = "today_entries"
    const val TEACHER_ENTRY_DETAIL = "teacher_entry_detail/{entryId}"
    const val PARENT_FEED = "parent_feed"
    const val PARENT_ENTRY_DETAIL = "parent_entry_detail/{entryId}"
    const val ARCHIVE = "archive"
    const val MESSAGE_LIST = "message_list"
    const val MESSAGE_THREAD = "message_thread"

    fun captureRoute(childId: String, childName: String) = "capture/$childId/$childName"
    fun teacherEntryDetail(entryId: String) = "teacher_entry_detail/$entryId"
    fun parentEntryDetail(entryId: String) = "parent_entry_detail/$entryId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    teacherViewModel: TeacherViewModel,
    parentViewModel: ParentViewModel,
    messageViewModel: MessageViewModel,
    isLoggedIn: Boolean,
    role: String?
) {
    val startDestination = when {
        !isLoggedIn -> Routes.LOGIN
        role == "teacher" -> Routes.TEACHER_DASHBOARD
        else -> Routes.PARENT_FEED
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            val uiState by authViewModel.uiState.collectAsState()
            LaunchedEffect(uiState.isLoggedIn) {
                if (uiState.isLoggedIn) {
                    val route = when (uiState.role) {
                        "teacher" -> Routes.TEACHER_DASHBOARD
                        else -> Routes.PARENT_FEED
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                onLogin = { email, password -> authViewModel.login(email, password) },
                onRegister = { email, password, name, role -> authViewModel.register(email, password, name, role) },
                isLoading = uiState.isLoading,
                error = uiState.error
            )
        }

        composable(Routes.TEACHER_DASHBOARD) {
            val uiState by teacherViewModel.uiState.collectAsState()
            DashboardScreen(
                children = uiState.children,
                dailyCounts = emptyMap(),
                onChildClick = { child ->
                    teacherViewModel.selectChild(child)
                    navController.navigate(Routes.captureRoute(child.id, child.name))
                },
                onViewToday = {
                    teacherViewModel.loadTodayEntries()
                    navController.navigate(Routes.TODAY_ENTRIES)
                },
                onMessages = {
                    messageViewModel.loadMessages()
                    navController.navigate(Routes.MESSAGE_LIST)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
            LaunchedEffect(Unit) { teacherViewModel.loadChildren() }
        }

        composable(
            route = Routes.CAPTURE,
            arguments = listOf(
                navArgument("childId") { type = NavType.StringType },
                navArgument("childName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            val childName = backStackEntry.arguments?.getString("childName") ?: ""
            val uiState by teacherViewModel.uiState.collectAsState()
            val child = uiState.children.find { it.id == childId }
                ?: com.example.montesorrilearning.domain.model.Child(id = childId, name = childName)

            CaptureScreen(
                child = child,
                capturedPhotos = uiState.capturedPhotos,
                dailyCount = uiState.dailyLimitCount,
                dailyLimitReached = uiState.dailyLimitReached,
                isUploading = uiState.isUploading,
                onPhotoCaptured = { uri -> teacherViewModel.addPhoto(uri) },
                onPhotoRemoved = { index -> teacherViewModel.removePhoto(index) },
                onTitleChange = { teacherViewModel.updateTitle(it) },
                onAreaChange = { teacherViewModel.updateMontessoriArea(it) },
                onCommentChange = { teacherViewModel.updateComment(it) },
                onSubmit = { override -> teacherViewModel.submitEntry(override) },
                onBack = { navController.popBackStack() },
                onDismissLimitWarning = { teacherViewModel.clearDailyLimitWarning() }
            )
        }

        composable(Routes.TODAY_ENTRIES) {
            val uiState by teacherViewModel.uiState.collectAsState()
            TodayEntriesScreen(
                entries = uiState.todayEntries,
                onEntryClick = { entry ->
                    teacherViewModel.selectEntry(entry)
                    navController.navigate(Routes.teacherEntryDetail(entry.id))
                },
                onBack = { navController.popBackStack() }
            )
            LaunchedEffect(Unit) { teacherViewModel.loadTodayEntries() }
        }

        composable(
            route = Routes.TEACHER_ENTRY_DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            val uiState by teacherViewModel.uiState.collectAsState()
            val entry = uiState.selectedEntry ?: uiState.todayEntries.find { it.id == entryId }
            if (entry != null) {
                EntryDetailScreen(
                    entry = entry,
                    onDelete = {
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Entry not found")
                }
            }
        }

        composable(Routes.PARENT_FEED) {
            val uiState by parentViewModel.uiState.collectAsState()
            FeedScreen(
                feedEntries = uiState.feedEntries,
                totalEntries = uiState.dailySummary?.totalEntries ?: uiState.feedEntries.size,
                totalPhotos = uiState.dailySummary?.totalPhotos ?: 0,
                onEntryClick = { entry ->
                    parentViewModel.selectEntry(entry)
                    navController.navigate(Routes.parentEntryDetail(entry.id))
                },
                onRefresh = { parentViewModel.loadFeed() },
                onArchive = { navController.navigate(Routes.ARCHIVE) },
                onMessages = {
                    messageViewModel.loadMessages()
                    navController.navigate(Routes.MESSAGE_LIST)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
            LaunchedEffect(Unit) { parentViewModel.loadFeed() }
        }

        composable(
            route = Routes.PARENT_ENTRY_DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            val uiState by parentViewModel.uiState.collectAsState()
            val entry = uiState.selectedEntry
                ?: uiState.feedEntries.find { it.id == entryId }
                ?: uiState.archivedEntries.find { it.id == entryId }
            if (entry != null) {
                ParentEntryDetailScreen(
                    entry = entry,
                    onBack = {
                        parentViewModel.clearSelection()
                        navController.popBackStack()
                    },
                    onShare = { /* share intent */ }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Entry not found")
                }
            }
        }

        composable(Routes.ARCHIVE) {
            val uiState by parentViewModel.uiState.collectAsState()
            ArchiveScreen(
                entries = uiState.archivedEntries,
                selectedDate = uiState.selectedDate,
                onDateSelected = { date -> parentViewModel.loadArchive(date) },
                onEntryClick = { entry ->
                    parentViewModel.selectEntry(entry)
                    navController.navigate(Routes.parentEntryDetail(entry.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MESSAGE_LIST) {
            val uiState by messageViewModel.uiState.collectAsState()
            MessageListScreen(
                messages = uiState.messages,
                onMessageClick = { msg ->
                    messageViewModel.markRead(msg.id)
                    navController.navigate(Routes.MESSAGE_THREAD)
                },
                onCompose = { navController.navigate(Routes.MESSAGE_THREAD) },
                onBack = { navController.popBackStack() }
            )
            LaunchedEffect(Unit) { messageViewModel.loadMessages() }
        }

        composable(Routes.MESSAGE_THREAD) {
            val uiState by messageViewModel.uiState.collectAsState()
            MessageThreadScreen(
                messages = uiState.messages,
                onSend = { body, subject, classroomId ->
                    messageViewModel.sendMessage(subject, body, classroomId)
                },
                onBack = { navController.popBackStack() },
                sendSuccess = uiState.sendSuccess
            )
        }
    }
}
