package com.example.montesorrilearning.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.repository.AuthRepository
import com.example.montesorrilearning.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val role: String? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            if (loggedIn) {
                val role = authRepository.getRole()
                _uiState.value = AuthUiState(isLoggedIn = true, role = role)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    val role = authRepository.getRole()
                    _uiState.value = AuthUiState(isLoggedIn = true, role = role)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Login failed")
                }
            )
        }
    }

    fun register(email: String, password: String, displayName: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.register(email, password, displayName, role)
            result.fold(
                onSuccess = {
                    val savedRole = authRepository.getRole()
                    _uiState.value = AuthUiState(isLoggedIn = true, role = savedRole)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Registration failed")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
