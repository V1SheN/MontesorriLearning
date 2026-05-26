package com.example.montesorrilearning.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.montesorrilearning.ui.admin.*
import com.example.montesorrilearning.ui.auth.AuthViewModel
import com.example.montesorrilearning.ui.auth.LoginScreen
import com.example.montesorrilearning.ui.messaging.MessageListScreen
import com.example.montesorrilearning.ui.messaging.MessageThreadScreen
import com.example.montesorrilearning.ui.messaging.MessageViewModel
import com.example.montesorrilearning.ui.parent.ArchiveScreen
import com.example.montesorrilearning.ui.parent.CalendarHeatmapScreen
import com.example.montesorrilearning.ui.parent.EntryDetailScreen as ParentEntryDetailScreen
import com.example.montesorrilearning.ui.parent.FeedScreen
import com.example.montesorrilearning.ui.parent.ParentViewModel
import com.example.montesorrilearning.ui.parent.ParentExpectationsScreen
import com.example.montesorrilearning.ui.parent.ParentExpectationsViewModel
import com.example.montesorrilearning.ui.parent.ChildProgressViewModel
import com.example.montesorrilearning.ui.settings.NotificationSettingsScreen
import com.example.montesorrilearning.ui.teacher.*

object Routes {
    const val LOGIN = "login"
    const val TEACHER_DASHBOARD = "teacher_dashboard"
    const val CAPTURE = "capture/{childId}/{childName}"
    const val TODAY_ENTRIES = "today_entries"
    const val TEACHER_ENTRY_DETAIL = "teacher_entry_detail/{entryId}"
    const val TEACHER_CALENDAR = "teacher_calendar"
    const val CHILD_PROGRESS = "child_progress"
    const val PARENT_FEED = "parent_feed"
    const val PARENT_ENTRY_DETAIL = "parent_entry_detail/{entryId}"
    const val PARENT_EXPECTATIONS = "parent_expectations"
    const val ARCHIVE = "archive"
    const val MESSAGE_LIST = "message_list"
    const val MESSAGE_THREAD = "message_thread"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_CLASSROOMS = "admin_classrooms"
    const val ADMIN_ANALYTICS = "admin_analytics"
    const val ADMIN_SYLLABUS = "admin_syllabus"
    const val ADMIN_SYLLABUS_EDIT = "admin_syllabus_edit/{itemId}"
    const val ADMIN_SYLLABUS_CREATE = "admin_syllabus_create"
    const val ADMIN_TERMS = "admin_terms"
    const val CALENDAR_HEATMAP = "calendar_heatmap"
    const val NOTIFICATION_SETTINGS = "notification_settings"

    fun captureRoute(childId: String, childName: String) = "capture/$childId/$childName"
    fun teacherEntryDetail(entryId: String) = "teacher_entry_detail/$entryId"
    fun parentEntryDetail(entryId: String) = "parent_entry_detail/$entryId"
    fun syllabusEditRoute(itemId: String) = "admin_syllabus_edit/$itemId"
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
        role == "admin" -> Routes.ADMIN_DASHBOARD
        else -> Routes.PARENT_FEED
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            val uiState by authViewModel.uiState.collectAsState()
            LaunchedEffect(uiState.isLoggedIn) {
                if (uiState.isLoggedIn) {
                    val route = when (uiState.role) {
                        "teacher" -> Routes.TEACHER_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.PARENT_FEED
                    }
                    navController.navigate(route) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            }
            LoginScreen(onLogin = { email, password -> authViewModel.login(email, password) },
                onRegister = { email, password, name, role -> authViewModel.register(email, password, name, role) },
                isLoading = uiState.isLoading, error = uiState.error)
        }

        composable(Routes.TEACHER_DASHBOARD) {
            val uiState by teacherViewModel.uiState.collectAsState()
            DashboardScreen(
                children = uiState.children,
                dailyCounts = uiState.dailyCounts,
                onChildClick = { child -> teacherViewModel.selectChild(child); navController.navigate(Routes.captureRoute(child.id, child.name)) },
                onViewToday = { teacherViewModel.loadTodayEntries(); navController.navigate(Routes.TODAY_ENTRIES) },
                onCalendar = { navController.navigate(Routes.TEACHER_CALENDAR) },
                onProgress = { navController.navigate(Routes.CHILD_PROGRESS) },
                onMessages = { messageViewModel.loadMessages(); navController.navigate(Routes.MESSAGE_LIST) },
                onLogout = { authViewModel.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } })
            LaunchedEffect(Unit) { teacherViewModel.loadChildren() }
        }

        composable(Routes.CAPTURE, arguments = listOf(navArgument("childId") { type = NavType.StringType }, navArgument("childName") { type = NavType.StringType })) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: ""
            val childName = backStackEntry.arguments?.getString("childName") ?: ""
            val uiState by teacherViewModel.uiState.collectAsState()
            val child = uiState.children.find { it.id == childId } ?: com.example.montesorrilearning.domain.model.Child(id = childId, name = childName)
            CaptureScreen(child = child, capturedPhotos = uiState.capturedPhotos, dailyCount = uiState.dailyLimitCount,
                dailyLimitReached = uiState.dailyLimitReached, isUploading = uiState.isUploading,
                onPhotoCaptured = { uri -> teacherViewModel.addPhoto(uri) }, onPhotoRemoved = { index -> teacherViewModel.removePhoto(index) },
                onTitleChange = { teacherViewModel.updateTitle(it) }, onAreaChange = { teacherViewModel.updateMontessoriArea(it) },
                onCommentChange = { teacherViewModel.updateComment(it) }, onSubmit = { override -> teacherViewModel.submitEntry(override) },
                onBack = { navController.popBackStack() }, onDismissLimitWarning = { teacherViewModel.clearDailyLimitWarning() })
        }

        composable(Routes.TODAY_ENTRIES) {
            val uiState by teacherViewModel.uiState.collectAsState()
            TodayEntriesScreen(entries = uiState.todayEntries, onEntryClick = { entry -> teacherViewModel.selectEntry(entry); navController.navigate(Routes.teacherEntryDetail(entry.id)) }, onBack = { navController.popBackStack() })
            LaunchedEffect(Unit) { teacherViewModel.loadTodayEntries() }
        }

        composable(Routes.TEACHER_ENTRY_DETAIL, arguments = listOf(navArgument("entryId") { type = NavType.StringType })) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            val uiState by teacherViewModel.uiState.collectAsState()
            val entry = uiState.selectedEntry ?: uiState.todayEntries.find { it.id == entryId }
            if (entry != null) {
                EntryDetailScreen(entry = entry, onDelete = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            } else { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Entry not found") } }
        }

        // ─── Teacher Calendar ───────────────────────────────────
        composable(Routes.TEACHER_CALENDAR) {
            val calendarViewModel: TeacherCalendarViewModel = viewModel()
            val uiState by calendarViewModel.uiState.collectAsState()
            TeacherCalendarScreen(
                syllabus = uiState.syllabus, myPlans = uiState.myPlans,
                terms = uiState.terms, selectedTerm = uiState.selectedTerm,
                selectedWeek = uiState.selectedWeek, isLoading = uiState.isLoading,
                error = uiState.error, successMessage = uiState.successMessage,
                onTermSelected = { calendarViewModel.selectTerm(it) },
                onWeekSelected = { calendarViewModel.selectWeek(it) },
                onToggleComplete = { id, completed -> calendarViewModel.toggleComplete(id, completed) },
                onDeletePlan = { calendarViewModel.deletePlan(it) },
                onAddPlan = { syllabusItem -> /* TODO: navigate to plan creation */ },
                onBack = { navController.popBackStack() },
                onClearMessages = { calendarViewModel.clearMessages() })
            LaunchedEffect(Unit) { calendarViewModel.loadTerms() }
        }

        // ─── Child Progress ──────────────────────────────────────
        composable(Routes.CHILD_PROGRESS) {
            val progressViewModel: ChildProgressViewModel = viewModel()
            val uiState by progressViewModel.uiState.collectAsState()
            val teacherUiState by teacherViewModel.uiState.collectAsState()

            ChildProgressScreen(
                children = teacherUiState.children,
                selectedChild = teacherUiState.currentChild,
                progressRecords = uiState.records,
                isLoading = uiState.isLoading, error = uiState.error,
                successMessage = uiState.successMessage,
                onChildSelected = { child -> teacherViewModel.selectChild(child); progressViewModel.loadProgress(child.id) },
                onUpdateStatus = { childId, syllabusId, status, notes -> progressViewModel.updateStatus(childId, syllabusId, status, notes) },
                onBack = { navController.popBackStack() },
                onClearMessages = { progressViewModel.clearMessages() })
            LaunchedEffect(Unit) { teacherViewModel.loadChildren() }
        }

        // ─── Parent Screens ──────────────────────────────────────
        composable(Routes.PARENT_FEED) {
            val context = LocalContext.current
            val uiState by parentViewModel.uiState.collectAsState()
            FeedScreen(
                feedEntries = uiState.feedEntries,
                totalEntries = uiState.dailySummary?.totalEntries ?: uiState.feedEntries.size,
                totalPhotos = uiState.dailySummary?.totalPhotos ?: 0,
                onEntryClick = { entry -> parentViewModel.selectEntry(entry); navController.navigate(Routes.parentEntryDetail(entry.id)) },
                onRefresh = { parentViewModel.loadFeed() },
                onArchive = { navController.navigate(Routes.ARCHIVE) },
                onExpectations = { navController.navigate(Routes.PARENT_EXPECTATIONS) },
                onShareSummary = {
                    val text = "Today: ${uiState.dailySummary?.totalEntries ?: 0} entries, ${uiState.dailySummary?.totalPhotos ?: 0} photos"
                    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                    context.startActivity(Intent.createChooser(intent, "Share Summary"))
                },
                onMessages = { messageViewModel.loadMessages(); navController.navigate(Routes.MESSAGE_LIST) },
                onLogout = { authViewModel.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } })
            LaunchedEffect(Unit) { parentViewModel.loadFeed() }
        }

        composable(Routes.PARENT_EXPECTATIONS) {
            val parentExpectationsViewModel: ParentExpectationsViewModel = viewModel()
            val uiState by parentExpectationsViewModel.uiState.collectAsState()
            ParentExpectationsScreen(
                syllabus = uiState.syllabus, progress = uiState.progress,
                terms = uiState.terms, selectedTerm = uiState.selectedTerm,
                selectedWeek = uiState.selectedWeek, isLoading = uiState.isLoading,
                onTermSelected = { parentExpectationsViewModel.selectTerm(it) },
                onWeekSelected = { parentExpectationsViewModel.selectWeek(it) },
                onBack = { navController.popBackStack() })
            LaunchedEffect(Unit) { parentExpectationsViewModel.loadTerms() }
        }

        composable(Routes.PARENT_ENTRY_DETAIL, arguments = listOf(navArgument("entryId") { type = NavType.StringType })) { backStackEntry ->
            val context = LocalContext.current
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            val uiState by parentViewModel.uiState.collectAsState()
            val entry = uiState.selectedEntry ?: uiState.feedEntries.find { it.id == entryId } ?: uiState.archivedEntries.find { it.id == entryId }
            if (entry != null) {
                ParentEntryDetailScreen(entry = entry, onBack = { parentViewModel.clearSelection(); navController.popBackStack() },
                    onShare = { val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, "Check out ${entry.title} from ${entry.childName}!\n\n${entry.teacherComment}"); type = "text/plain" }; context.startActivity(Intent.createChooser(sendIntent, null)) })
            } else { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Entry not found") } }
        }

        composable(Routes.ARCHIVE) {
            val uiState by parentViewModel.uiState.collectAsState()
            ArchiveScreen(entries = uiState.archivedEntries, selectedDate = uiState.selectedDate,
                onDateSelected = { date -> parentViewModel.loadArchive(date) },
                onEntryClick = { entry -> parentViewModel.selectEntry(entry); navController.navigate(Routes.parentEntryDetail(entry.id)) },
                onBack = { navController.popBackStack() })
        }

        composable(Routes.MESSAGE_LIST) {
            val uiState by messageViewModel.uiState.collectAsState()
            MessageListScreen(messages = uiState.messages,
                onMessageClick = { msg -> messageViewModel.markRead(msg.id); navController.navigate(Routes.MESSAGE_THREAD) },
                onCompose = { navController.navigate(Routes.MESSAGE_THREAD) }, onBack = { navController.popBackStack() })
            LaunchedEffect(Unit) { messageViewModel.loadMessages() }
        }

        composable(Routes.MESSAGE_THREAD) {
            val uiState by messageViewModel.uiState.collectAsState()
            MessageThreadScreen(messages = uiState.messages, onSend = { body, subject, classroomId -> messageViewModel.sendMessage(subject, body, classroomId) },
                onBack = { navController.popBackStack() }, sendSuccess = uiState.sendSuccess)
        }

        // ─── Admin Screens ───────────────────────────────────────
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToUsers = { navController.navigate(Routes.ADMIN_USERS) },
                onNavigateToClassrooms = { navController.navigate(Routes.ADMIN_CLASSROOMS) },
                onNavigateToAnalytics = { navController.navigate(Routes.ADMIN_ANALYTICS) },
                onNavigateToSyllabus = { navController.navigate(Routes.ADMIN_SYLLABUS) },
                onNavigateToTerms = { navController.navigate(Routes.ADMIN_TERMS) },
                onLogout = { authViewModel.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } })
        }

        composable(Routes.ADMIN_USERS) { UsersScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_CLASSROOMS) { ClassroomsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_ANALYTICS) { AnalyticsScreen(onBack = { navController.popBackStack() }) }

        // Admin terms
        composable(Routes.ADMIN_TERMS) {
            val adminViewModel: AdminViewModel = viewModel()
            val uiState by adminViewModel.uiState.collectAsState()
            TermManagementScreen(
                terms = uiState.terms, isLoading = uiState.isLoading, error = uiState.error,
                onCreateTerm = { name, start, end, year -> adminViewModel.createTerm(name, start, end, year) },
                onDeleteTerm = { adminViewModel.deleteTerm(it) },
                onBack = { navController.popBackStack() })
            LaunchedEffect(Unit) { adminViewModel.loadTerms() }
        }

        // Admin syllabus list
        composable(Routes.ADMIN_SYLLABUS) {
            val adminViewModel: AdminViewModel = viewModel()
            val uiState by adminViewModel.uiState.collectAsState()
            SyllabusListScreen(
                syllabus = uiState.syllabus, isLoading = uiState.isLoading, error = uiState.error,
                selectedArea = null, selectedWeek = null,
                onAreaFilter = { area -> adminViewModel.loadSyllabus(area = area) },
                onWeekFilter = { week -> adminViewModel.loadSyllabus(weekNumber = week) },
                onItemClick = { item -> navController.navigate(Routes.syllabusEditRoute(item.id)) },
                onAdd = { navController.navigate(Routes.ADMIN_SYLLABUS_CREATE) },
                onRefresh = { adminViewModel.loadSyllabus() }, onBack = { navController.popBackStack() })
            LaunchedEffect(Unit) { adminViewModel.loadSyllabus() }
        }

        composable(Routes.ADMIN_SYLLABUS_EDIT, arguments = listOf(navArgument("itemId") { type = NavType.StringType })) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val adminViewModel: AdminViewModel = viewModel()
            val uiState by adminViewModel.uiState.collectAsState()
            SyllabusEditScreen(
                existing = uiState.selectedSyllabus, isLoading = uiState.isLoading,
                error = uiState.error, successMessage = uiState.successMessage,
                onSave = { _, _, area, title, desc, dow, week, order, extra, atype ->
                    adminViewModel.updateSyllabus(itemId, com.example.montesorrilearning.data.remote.SyllabusRequest(
                        termId = "", classroomId = "", montessoriArea = area, title = title,
                        description = desc, dayOfWeek = dow, weekNumber = week, sortOrder = order,
                        isExtracurricular = extra, activityType = atype))
                },
                onBack = { adminViewModel.clearSelection(); navController.popBackStack() },
                onClearMessages = { adminViewModel.clearMessages() })
            LaunchedEffect(itemId) { adminViewModel.loadSyllabusItem(itemId) }
        }

        composable(Routes.ADMIN_SYLLABUS_CREATE) {
            val adminViewModel: AdminViewModel = viewModel()
            val uiState by adminViewModel.uiState.collectAsState()
            SyllabusEditScreen(
                existing = null, isLoading = uiState.isLoading, error = uiState.error,
                successMessage = uiState.successMessage,
                onSave = { _, _, area, title, desc, dow, week, order, extra, atype ->
                    adminViewModel.createSyllabus("", "", area, title, desc, dow, week, order, extra, atype)
                },
                onBack = { navController.popBackStack() },
                onClearMessages = { adminViewModel.clearMessages() })
        }

        composable(Routes.CALENDAR_HEATMAP) {
            CalendarHeatmapScreen(onBack = { navController.popBackStack() }, onDateSelected = { date -> parentViewModel.loadArchive(date) })
        }

        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
