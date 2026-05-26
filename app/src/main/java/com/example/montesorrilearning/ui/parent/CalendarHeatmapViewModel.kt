package com.example.montesorrilearning.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.repository.ChildRepository
import com.example.montesorrilearning.data.repository.WorkRepository
import com.example.montesorrilearning.domain.model.Child
import com.example.montesorrilearning.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarHeatmapUiState(
    val children: List<Child> = emptyList(),
    val selectedChildId: String? = null,
    val dailyCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendarHeatmapViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val workRepository: WorkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarHeatmapUiState())
    val uiState: StateFlow<CalendarHeatmapUiState> = _uiState.asStateFlow()

    fun loadChildren() {
        viewModelScope.launch {
            childRepository.getChildren().fold(
                onSuccess = { children ->
                    _uiState.value = _uiState.value.copy(children = children)
                    if (children.isNotEmpty() && _uiState.value.selectedChildId == null) {
                        selectChild(children.first().id)
                    }
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun selectChild(childId: String) {
        _uiState.value = _uiState.value.copy(selectedChildId = childId)
        loadRange()
    }

    fun loadRange() {
        val childId = _uiState.value.selectedChildId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val to = DateUtils.todayIso()
            val from = DateUtils.minusMonths(to, 3)
            workRepository.getDailyCountRange(childId, from, to).fold(
                onSuccess = { counts ->
                    val map = counts.associate { it.date to it.count }
                    _uiState.value = _uiState.value.copy(dailyCounts = map, isLoading = false)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }
}
