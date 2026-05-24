package com.example.montesorrilearning.ui.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.repository.MessageRepository
import com.example.montesorrilearning.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageUiState(
    val messages: List<Message> = emptyList(),
    val selectedThread: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val sendSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            messageRepository.getMessages().fold(
                onSuccess = { messages ->
                    _uiState.value = _uiState.value.copy(messages = messages, isLoading = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
            )
        }
    }

    fun sendMessage(subject: String?, body: String, classroomId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, sendSuccess = false)
            messageRepository.sendMessage(subject, body, classroomId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, sendSuccess = true)
                    loadMessages()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
            )
        }
    }

    fun markRead(messageId: String) {
        viewModelScope.launch {
            messageRepository.markRead(messageId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSendSuccess() {
        _uiState.value = _uiState.value.copy(sendSuccess = false)
    }
}
