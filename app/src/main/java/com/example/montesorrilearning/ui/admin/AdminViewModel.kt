package com.example.montesorrilearning.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.remote.SyllabusRequest
import com.example.montesorrilearning.data.repository.SyllabusRepository
import com.example.montesorrilearning.domain.model.Syllabus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val syllabus: List<Syllabus> = emptyList(),
    val selectedSyllabus: Syllabus? = null,
    val classrooms: List<com.example.montesorrilearning.domain.model.Classroom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadSyllabus(classroomId: String? = null, area: String? = null, year: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            syllabusRepository.getSyllabus(classroomId, area, year).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(syllabus = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun loadSyllabusItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            syllabusRepository.getSyllabusItem(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(selectedSyllabus = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun createSyllabus(
        classroomId: String,
        montessoriArea: String,
        title: String,
        description: String,
        weekNumber: Int?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val request = SyllabusRequest(
                classroomId = classroomId,
                montessoriArea = montessoriArea,
                title = title,
                description = description,
                weekNumber = weekNumber,
                sortOrder = sortOrder
            )
            syllabusRepository.createSyllabus(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Syllabus item created"
                    )
                    loadSyllabus()
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun updateSyllabus(id: String, request: SyllabusRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            syllabusRepository.updateSyllabus(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Updated")
                    loadSyllabus()
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun deleteSyllabus(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            syllabusRepository.deleteSyllabus(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Deleted")
                    loadSyllabus()
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedSyllabus = null)
    }
}
