package com.example.montesorrilearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.montesorrilearning.ui.auth.AuthViewModel
import com.example.montesorrilearning.ui.messaging.MessageViewModel
import com.example.montesorrilearning.ui.navigation.NavGraph
import com.example.montesorrilearning.ui.parent.ParentViewModel
import com.example.montesorrilearning.ui.teacher.TeacherViewModel
import com.example.montesorrilearning.ui.theme.MontessoriTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MontessoriTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MontessoriAppContent()
                }
            }
        }
    }
}

@Composable
fun MontessoriAppContent() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val teacherViewModel: TeacherViewModel = hiltViewModel()
    val parentViewModel: ParentViewModel = hiltViewModel()
    val messageViewModel: MessageViewModel = hiltViewModel()

    val authState by authViewModel.uiState.collectAsState()

    NavGraph(
        navController = navController,
        authViewModel = authViewModel,
        teacherViewModel = teacherViewModel,
        parentViewModel = parentViewModel,
        messageViewModel = messageViewModel,
        isLoggedIn = authState.isLoggedIn,
        role = authState.role
    )
}
