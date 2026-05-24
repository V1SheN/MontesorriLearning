package com.example.montesorrilearning.ui.teacher

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.montesorrilearning.data.repository.ChildRepository
import com.example.montesorrilearning.data.repository.WorkRepository
import com.example.montesorrilearning.data.remote.DailyCount
import com.example.montesorrilearning.domain.model.Child
import com.example.montesorrilearning.domain.model.WorkEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TeacherUiState(
    val children: List<Child> = emptyList(),
    val todayEntries: List<WorkEntry> = emptyList(),
    val dailyCounts: Map<String, DailyCount> = emptyMap(),
    val capturedPhotos: List<Uri> = emptyList(),
    val currentChild: Child? = null,
    val title: String = "",
    val montessoriArea: String = "",
    val teacherComment: String = "",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val dailyLimitReached: Boolean = false,
    val dailyLimitCount: Int = 0,
    val error: String? = null,
    val uploadSuccess: Boolean = false,
    val selectedEntry: WorkEntry? = null
)

@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val workRepository: WorkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherUiState())
    val uiState: StateFlow<TeacherUiState> = _uiState.asStateFlow()

    fun loadChildren() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            childRepository.getChildren().fold(
                onSuccess = { children ->
                    _uiState.value = _uiState.value.copy(children = children, isLoading = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
            )
        }
    }

    fun loadTodayEntries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            workRepository.getWorkEntries(null, null).fold(
                onSuccess = { entries ->
                    _uiState.value = _uiState.value.copy(todayEntries = entries, isLoading = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
            )
        }
    }

    fun selectChild(child: Child) {
        _uiState.value = _uiState.value.copy(
            currentChild = child,
            capturedPhotos = emptyList(),
            title = "",
            montessoriArea = "",
            teacherComment = "",
            uploadSuccess = false
        )
        checkDailyCount(child.id)
    }

    fun addPhoto(uri: Uri) {
        val photos = _uiState.value.capturedPhotos + uri
        _uiState.value = _uiState.value.copy(capturedPhotos = photos)
    }

    fun removePhoto(index: Int) {
        val photos = _uiState.value.capturedPhotos.toMutableList()
        if (index in photos.indices) {
            photos.removeAt(index)
            _uiState.value = _uiState.value.copy(capturedPhotos = photos)
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateMontessoriArea(area: String) {
        _uiState.value = _uiState.value.copy(montessoriArea = area)
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(teacherComment = comment)
    }

    fun clearDailyLimitWarning() {
        _uiState.value = _uiState.value.copy(dailyLimitReached = false)
    }

    private fun checkDailyCount(childId: String) {
        viewModelScope.launch {
            workRepository.getDailyCount(childId).fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(
                        dailyLimitCount = count.count,
                        dailyLimitReached = count.count >= count.max
                    )
                },
                onFailure = { /* ignore count errors */ }
            )
        }
    }

    fun submitEntry(overrideLimit: Boolean = false) {
        val state = _uiState.value
        val child = state.currentChild ?: return
        if (state.capturedPhotos.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)

            // Upload each photo
            var uploadedCount = 0
            for ((index, uri) in state.capturedPhotos.withIndex()) {
                val isCover = index == 0
                val result = workRepository.uploadPhoto(
                    uri = uri,
                    childId = child.id,
                    isCover = isCover,
                    overrideLimit = overrideLimit
                )
                result.fold(
                    onSuccess = { uploadedCount++ },
                    onFailure = { e ->
                        // Queue for offline upload
                        workRepository.queueOfflineUpload(
                            localPhotoPath = uri.toString(),
                            childId = child.id,
                            title = state.title,
                            montessoriArea = state.montessoriArea,
                            teacherComment = state.teacherComment,
                            isCover = isCover
                        )
                    }
                )
            }

            // Create work entry if we uploaded at least one photo
            if (uploadedCount > 0) {
                workRepository.createWorkEntry(
                    com.example.montesorrilearning.data.remote.WorkEntryRequest(
                        childId = child.id,
                        montessoriArea = state.montessoriArea,
                        title = state.title,
                        teacherComment = state.teacherComment
                    )
                )
            }

            _uiState.value = _uiState.value.copy(
                isUploading = false,
                uploadSuccess = true,
                capturedPhotos = emptyList(),
                title = "",
                montessoriArea = "",
                teacherComment = ""
            )
        }
    }

    fun selectEntry(entry: WorkEntry) {
        _uiState.value = _uiState.value.copy(selectedEntry = entry)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedEntry = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
