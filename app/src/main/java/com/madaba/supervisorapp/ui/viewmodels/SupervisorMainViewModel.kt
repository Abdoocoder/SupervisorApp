package com.madaba.supervisorapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.data.source.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorMainViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    val workers: StateFlow<List<Worker>> = repository.workers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Fetch fresh data from Firestore when the ViewModel is created
        refreshWorkers()
    }

    fun refreshWorkers() {
        viewModelScope.launch {
            repository.refreshWorkers()
        }
    }
}