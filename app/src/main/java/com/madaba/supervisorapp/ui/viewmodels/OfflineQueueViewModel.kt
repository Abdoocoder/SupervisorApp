
package com.madaba.supervisorapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OfflineQueueUiState {
    object Loading : OfflineQueueUiState()
    data class Success(val workers: List<Worker>) : OfflineQueueUiState()
    data class Error(val message: String) : OfflineQueueUiState()
}

@HiltViewModel
class OfflineQueueViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OfflineQueueUiState>(OfflineQueueUiState.Loading)
    val uiState: StateFlow<OfflineQueueUiState> = _uiState

    init {
        getWorkers()
    }

    private fun getWorkers() {
        viewModelScope.launch {
            try {
                attendanceRepository.workers.collectLatest { workers ->
                    _uiState.value = OfflineQueueUiState.Success(workers)
                }
            } catch (e: Exception) {
                _uiState.value = OfflineQueueUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}
