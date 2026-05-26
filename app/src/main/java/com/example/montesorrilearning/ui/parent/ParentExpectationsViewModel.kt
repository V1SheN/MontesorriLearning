package com.example.montesorrilearning.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.repository.ChildProgressRepository
import com.example.montesorrilearning.data.repository.SyllabusRepository
import com.example.montesorrilearning.data.repository.TermRepository
import com.example.montesorrilearning.domain.model.ChildProgress
import com.example.montesorrilearning.domain.model.Syllabus
import com.example.montesorrilearning.domain.model.Term
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpectationsUiState(
    val syllabus: List<Syllabus> = emptyList(),
    val progress: List<ChildProgress> = emptyList(),
    val terms: List<Term> = emptyList(),
    val selectedTerm: Term? = null,
    val selectedWeek: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentExpectationsViewModel @Inject constructor(
    private val syllabusRepository: SyllabusRepository,
    private val progressRepository: ChildProgressRepository,
    private val termRepository: TermRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpectationsUiState())
    val uiState: StateFlow<ExpectationsUiState> = _uiState.asStateFlow()

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

    private fun loadWeek() {
        val term = _uiState.value.selectedTerm ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            syllabusRepository.getSyllabus(termId = term.id, weekNumber = _uiState.value.selectedWeek).fold(
                onSuccess = { syllabus ->
                    progressRepository.getProgress().fold(
                        onSuccess = { progress ->
                            _uiState.value = _uiState.value.copy(syllabus = syllabus, progress = progress, isLoading = false)
                        },
                        onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }
}
