package com.example.montesorrilearning.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.remote.ChildProgressRequest
import com.example.montesorrilearning.data.repository.ChildProgressRepository
import com.example.montesorrilearning.domain.model.ChildProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val records: List<ChildProgress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ChildProgressViewModel @Inject constructor(
    private val repository: ChildProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    fun loadProgress(childId: String? = null, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getProgress(childId, status).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(records = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun updateStatus(childId: String, syllabusId: String?, status: String, notes: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            repository.upsertProgress(ChildProgressRequest(
                childId = childId, syllabusId = syllabusId,
                status = status, observationNotes = notes
            )).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Updated"); loadProgress(childId) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
}
