package com.madaba.supervisorapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madaba.supervisorapp.data.source.local.AttendanceEntity
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OfflineQueueUiState {
    object Loading : OfflineQueueUiState()
    data class Success(
        val unsyncedAttendances: List<AttendanceEntity>,
        val lastSyncTime: Long?
    ) : OfflineQueueUiState()
    data class Error(val message: String) : OfflineQueueUiState()
}

@HiltViewModel
class OfflineQueueViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OfflineQueueUiState>(OfflineQueueUiState.Loading)
    val uiState: StateFlow<OfflineQueueUiState> = _uiState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    init {
        loadUnsyncedAttendances()
    }

    private fun loadUnsyncedAttendances() {
        viewModelScope.launch {
            try {
                attendanceRepository.unsyncedAttendances.collectLatest { attendances ->
                    _uiState.value = OfflineQueueUiState.Success(attendances, _lastSyncTime.value)
                }
            } catch (e: Exception) {
                _uiState.value = OfflineQueueUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = attendanceRepository.syncUnsyncedAttendances()
            if (result is com.madaba.supervisorapp.data.source.repository.RepositoryResult.Success) {
                _lastSyncTime.value = System.currentTimeMillis()
            }
        }
    }
}
