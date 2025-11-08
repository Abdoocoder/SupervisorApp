package com.madaba.supervisorapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madaba.supervisorapp.data.models.UserRole
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import com.madaba.supervisorapp.data.source.repository.RepositoryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorMainViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    init {
        // Load user role and workers
        viewModelScope.launch {
            loadUserRole()
            refreshWorkers()
            // Collect workers flow
            repository.getWorkersFlow().collect { workersList ->
                _workers.value = workersList
            }
        }
    }

    private suspend fun loadUserRole() {
        _userRole.value = repository.getCurrentUserRole()
    }

    fun refreshWorkers() {
        viewModelScope.launch {
            repository.refreshWorkers()
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            val workersResult = repository.refreshWorkers()
            val attendanceResult = repository.syncUnsyncedAttendances()
            
            when {
                workersResult is RepositoryResult.Error -> {
                    _syncState.value = SyncState.Error(workersResult.message)
                }
                attendanceResult is RepositoryResult.Error -> {
                    _syncState.value = SyncState.Error(attendanceResult.message)
                }
                else -> {
                    val syncedCount = (attendanceResult as? RepositoryResult.Success)?.data ?: 0
                    _syncState.value = SyncState.Success(syncedCount)
                }
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val syncedCount: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}