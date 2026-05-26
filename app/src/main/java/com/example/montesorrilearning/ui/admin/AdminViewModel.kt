package com.example.montesorrilearning.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.remote.SyllabusRequest
import com.example.montesorrilearning.data.remote.TermRequest
import com.example.montesorrilearning.data.repository.SyllabusRepository
import com.example.montesorrilearning.data.repository.TermRepository
import com.example.montesorrilearning.domain.model.Syllabus
import com.example.montesorrilearning.domain.model.Term
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val syllabus: List<Syllabus> = emptyList(),
    val selectedSyllabus: Syllabus? = null,
    val terms: List<Term> = emptyList(),
    val selectedTerm: Term? = null,
    val classrooms: List<com.example.montesorrilearning.domain.model.Classroom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val syllabusRepository: SyllabusRepository,
    private val termRepository: TermRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadSyllabus(
        classroomId: String? = null, termId: String? = null,
        area: String? = null, weekNumber: Int? = null,
        dayOfWeek: Int? = null, isExtracurricular: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            syllabusRepository.getSyllabus(classroomId, termId, area, weekNumber, dayOfWeek, isExtracurricular).fold(
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
        termId: String, classroomId: String, montessoriArea: String,
        title: String, description: String, dayOfWeek: Int,
        weekNumber: Int?, sortOrder: Int, isExtracurricular: Boolean, activityType: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val request = SyllabusRequest(
                termId = termId, classroomId = classroomId, montessoriArea = montessoriArea,
                title = title, description = description, dayOfWeek = dayOfWeek,
                weekNumber = weekNumber, sortOrder = sortOrder,
                isExtracurricular = isExtracurricular, activityType = activityType
            )
            syllabusRepository.createSyllabus(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Created")
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
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Deleted"); loadSyllabus() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    // ─── Terms ──────────────────────────────────────────────────
    fun loadTerms(year: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            termRepository.getTerms(year).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(terms = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun createTerm(name: String, startDate: String, endDate: String, year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            termRepository.createTerm(TermRequest(name, startDate, endDate, year)).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Term created"); loadTerms() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun updateTerm(id: String, name: String, startDate: String, endDate: String, year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            termRepository.updateTerm(id, TermRequest(name, startDate, endDate, year)).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Updated"); loadTerms() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun deleteTerm(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            termRepository.deleteTerm(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Deleted"); loadTerms() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
    fun clearSelection() { _uiState.value = _uiState.value.copy(selectedSyllabus = null, selectedTerm = null) }
}
