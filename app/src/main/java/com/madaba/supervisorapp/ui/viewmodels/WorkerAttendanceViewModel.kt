package com.madaba.supervisorapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.local.AttendanceEntity
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import com.madaba.supervisorapp.data.source.repository.RepositoryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WorkerAttendanceUiState {
    object Loading : WorkerAttendanceUiState()
    data class Success(
        val worker: Worker?,
        val attendances: List<AttendanceEntity>
    ) : WorkerAttendanceUiState()
    data class Error(val message: String) : WorkerAttendanceUiState()
}

sealed class SaveAttendanceResult {
    object Success : SaveAttendanceResult()
    data class Error(val message: String) : SaveAttendanceResult()
    object Loading : SaveAttendanceResult()
}

@HiltViewModel
class WorkerAttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkerAttendanceUiState>(WorkerAttendanceUiState.Loading)
    val uiState: StateFlow<WorkerAttendanceUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableStateFlow<SaveAttendanceResult>(SaveAttendanceResult.Success)
    val saveResult: StateFlow<SaveAttendanceResult> = _saveResult.asStateFlow()

    fun loadWorkerAndAttendances(workerId: String) {
        viewModelScope.launch {
            _uiState.value = WorkerAttendanceUiState.Loading
            try {
                // Get worker from first emission
                val workers = repository.workers.first()
                val worker = workers.find { it.id == workerId }
                
                // Collect attendances for this worker
                repository.getAttendanceForWorker(workerId).collect { attendances ->
                    _uiState.value = WorkerAttendanceUiState.Success(worker, attendances)
                }
            } catch (e: Exception) {
                _uiState.value = WorkerAttendanceUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveAttendance(
        workerId: String,
        date: Long,
        status: String,
        hoursWorked: Int,
        overtimeHours: Int,
        notes: String?,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            _saveResult.value = SaveAttendanceResult.Loading
            
            val attendance = AttendanceEntity(
                workerId = workerId,
                date = date,
                status = status,
                hoursWorked = hoursWorked,
                overtimeHours = overtimeHours,
                notes = notes,
                photoUrl = photoUrl,
                synced = false,
                editedBy = "" // Will be set by repository
            )
            
            when (val result = repository.saveAttendance(attendance)) {
                is RepositoryResult.Success -> {
                    _saveResult.value = SaveAttendanceResult.Success
                    // Reload attendances
                    loadWorkerAndAttendances(workerId)
                }
                is RepositoryResult.Error -> {
                    _saveResult.value = SaveAttendanceResult.Error(result.message)
                }
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = SaveAttendanceResult.Success
    }
}

