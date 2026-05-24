package com.example.montesorrilearning.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.remote.DailySummary
import com.example.montesorrilearning.data.remote.SocketEvent
import com.example.montesorrilearning.data.remote.SocketManager
import com.example.montesorrilearning.data.repository.WorkRepository
import com.example.montesorrilearning.domain.model.WorkEntry
import com.example.montesorrilearning.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentUiState(
    val feedEntries: List<WorkEntry> = emptyList(),
    val dailySummary: DailySummary? = null,
    val archivedEntries: List<WorkEntry> = emptyList(),
    val selectedEntry: WorkEntry? = null,
    val selectedDate: String = DateUtils.todayIso(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
        observeSocketEvents()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val date = DateUtils.todayIso()

            workRepository.getWorkEntries(null, date).fold(
                onSuccess = { entries ->
                    _uiState.value = _uiState.value.copy(
                        feedEntries = entries,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
            )

            workRepository.getDailySummary(date, null).fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(dailySummary = summary)
                },
                onFailure = { /* ignore summary errors */ }
            )
        }
    }

    fun loadArchive(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedDate = date)
            workRepository.getWorkEntries(null, date).fold(
                onSuccess = { entries ->
                    _uiState.value = _uiState.value.copy(
                        archivedEntries = entries,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
            )
        }
    }

    fun selectEntry(entry: WorkEntry) {
        _uiState.value = _uiState.value.copy(selectedEntry = entry)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedEntry = null)
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            socketManager.events.collect { event ->
                when (event) {
                    is SocketEvent.NewEntry -> {
                        loadFeed()
                    }
                    else -> { /* ignore */ }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
