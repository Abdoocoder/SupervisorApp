package com.madaba.supervisorapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import com.madaba.supervisorapp.data.source.repository.RepositoryResult
import com.madaba.supervisorapp.ui.screens.ManageWorkersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageWorkersViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageWorkersUiState>(ManageWorkersUiState.Loading)
    val uiState: StateFlow<ManageWorkersUiState> = _uiState.asStateFlow()

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    val supervisors = repository.supervisors
    val areas = repository.areas

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = ManageWorkersUiState.Loading
            try {
                repository.refreshWorkers()
                repository.getWorkersFlow().collect { workersList ->
                    _workers.value = workersList
                    _uiState.value = ManageWorkersUiState.Success()
                }
            } catch (e: Exception) {
                _uiState.value = ManageWorkersUiState.Error(e.message ?: "خطأ غير معروف")
            }
        }
    }

    fun createWorker(worker: Worker) {
        viewModelScope.launch {
            when (val result = repository.createWorker(worker)) {
                is RepositoryResult.Success -> {
                    loadData()
                }
                is RepositoryResult.Error -> {
                    _uiState.value = ManageWorkersUiState.Error(result.message)
                }
            }
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            when (val result = repository.updateWorker(worker)) {
                is RepositoryResult.Success -> {
                    loadData()
                }
                is RepositoryResult.Error -> {
                    _uiState.value = ManageWorkersUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteWorker(workerId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteWorker(workerId)) {
                is RepositoryResult.Success -> {
                    loadData()
                }
                is RepositoryResult.Error -> {
                    _uiState.value = ManageWorkersUiState.Error(result.message)
                }
            }
        }
    }
}

