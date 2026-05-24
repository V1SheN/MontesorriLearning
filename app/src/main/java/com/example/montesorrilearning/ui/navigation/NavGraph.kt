package com.example.montesorrilearning.ui.navigation

import androidx.compose.runtime.Composable
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
            val uiState = authViewModel.uiState.value
            LoginScreen(
                onLogin = { email, password -> authViewModel.login(email, password) },
                onRegister = { email, password, name, role -> authViewModel.register(email, password, name, role) },
                isLoading = uiState.isLoading,
                error = uiState.error
            )
        }

        composable(Routes.TEACHER_DASHBOARD) {
            val uiState = teacherViewModel.uiState.value
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
            val uiState = teacherViewModel.uiState.value

            CaptureScreen(
                child = com.example.montesorrilearning.domain.model.Child(id = childId, name = childName),
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
            val uiState = teacherViewModel.uiState.value
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

        composable(Routes.TEACHER_ENTRY_DETAIL) {
            val uiState = teacherViewModel.uiState.value
            uiState.selectedEntry?.let { entry ->
                EntryDetailScreen(
                    entry = entry,
                    onDelete = {
                        /* delete handled here */
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.PARENT_FEED) {
            val uiState = parentViewModel.uiState.value
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

        composable(Routes.PARENT_ENTRY_DETAIL) {
            val uiState = parentViewModel.uiState.value
            uiState.selectedEntry?.let { entry ->
                ParentEntryDetailScreen(
                    entry = entry,
                    onBack = {
                        parentViewModel.clearSelection()
                        navController.popBackStack()
                    },
                    onShare = { /* share intent */ }
                )
            }
        }

        composable(Routes.ARCHIVE) {
            val uiState = parentViewModel.uiState.value
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
            val uiState = messageViewModel.uiState.value
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
            val uiState = messageViewModel.uiState.value
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
