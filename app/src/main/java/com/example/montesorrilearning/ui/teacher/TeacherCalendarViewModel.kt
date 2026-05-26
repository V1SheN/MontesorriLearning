package com.example.montesorrilearning.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.remote.TeacherPlanRequest
import com.example.montesorrilearning.data.repository.SyllabusRepository
import com.example.montesorrilearning.data.repository.TeacherPlanRepository
import com.example.montesorrilearning.data.repository.TermRepository
import com.example.montesorrilearning.domain.model.Syllabus
import com.example.montesorrilearning.domain.model.TeacherPlan
import com.example.montesorrilearning.domain.model.Term
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarUiState(
    val syllabus: List<Syllabus> = emptyList(),
    val myPlans: List<TeacherPlan> = emptyList(),
    val terms: List<Term> = emptyList(),
    val selectedWeek: Int = 1,
    val selectedTerm: Term? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TeacherCalendarViewModel @Inject constructor(
    private val syllabusRepository: SyllabusRepository,
    private val planRepository: TeacherPlanRepository,
    private val termRepository: TermRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun loadTerms() {
        viewModelScope.launch {
            termRepository.getTerms().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(terms = it, selectedTerm = it.firstOrNull())
                    if (it.isNotEmpty()) loadWeek()
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectTerm(term: Term) {
        _uiState.value = _uiState.value.copy(selectedTerm = term, selectedWeek = 1)
        loadWeek()
    }

    fun selectWeek(week: Int) {
        _uiState.value = _uiState.value.copy(selectedWeek = week)
        loadWeek()
    }

    fun loadWeek() {
        val term = _uiState.value.selectedTerm ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            syllabusRepository.getSyllabus(termId = term.id, weekNumber = _uiState.value.selectedWeek).fold(
                onSuccess = { syllabus ->
                    planRepository.getPlans(termId = term.id).fold(
                        onSuccess = { plans ->
                            _uiState.value = _uiState.value.copy(
                                syllabus = syllabus, myPlans = plans, isLoading = false
                            )
                        },
                        onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun createPlan(
        syllabusId: String?, title: String, montessoriArea: String,
        description: String, plannedDate: String, dayOfWeek: Int,
        weekNumber: Int?, isExtracurricular: Boolean, activityType: String?
    ) {
        val term = _uiState.value.selectedTerm ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val request = TeacherPlanRequest(
                syllabusId = syllabusId, termId = term.id,
                title = title, montessoriArea = montessoriArea,
                description = description, plannedDate = plannedDate,
                dayOfWeek = dayOfWeek, weekNumber = weekNumber,
                isExtracurricular = isExtracurricular, activityType = activityType
            )
            planRepository.createPlan(request).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Plan created"); loadWeek() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun toggleComplete(planId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            planRepository.updatePlan(planId, TeacherPlanRequest(isCompleted = !isCompleted)).fold(
                onSuccess = { loadWeek() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun deletePlan(id: String) {
        viewModelScope.launch {
            planRepository.deletePlan(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Deleted"); loadWeek() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
}
