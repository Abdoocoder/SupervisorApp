package com.madaba.supervisorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.madaba.supervisorapp.data.source.local.AttendanceEntity
import com.madaba.supervisorapp.ui.viewmodels.SaveAttendanceResult
import com.madaba.supervisorapp.ui.viewmodels.WorkerAttendanceUiState
import com.madaba.supervisorapp.ui.viewmodels.WorkerAttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerAttendanceScreen(
    workerId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorkerAttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    
    var showAddForm by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var status by remember { mutableStateOf("Present") }
    var hoursWorked by remember { mutableStateOf("8") }
    var overtimeHours by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(workerId) {
        viewModel.loadWorkerAndAttendances(workerId)
    }

    LaunchedEffect(saveResult) {
        if (saveResult is SaveAttendanceResult.Success) {
            showAddForm = false
            status = "Present"
            hoursWorked = "8"
            overtimeHours = "0"
            notes = ""
            viewModel.resetSaveResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = uiState) {
                            is WorkerAttendanceUiState.Success -> state.worker?.name ?: "Worker Attendance"
                            else -> "Worker Attendance"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is WorkerAttendanceUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is WorkerAttendanceUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWorkerAndAttendances(workerId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is WorkerAttendanceUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (showAddForm) {
                        AddAttendanceForm(
                            selectedDate = selectedDate,
                            onDateChange = { selectedDate = it },
                            status = status,
                            onStatusChange = { status = it },
                            hoursWorked = hoursWorked,
                            onHoursWorkedChange = { hoursWorked = it },
                            overtimeHours = overtimeHours,
                            onOvertimeHoursChange = { overtimeHours = it },
                            notes = notes,
                            onNotesChange = { notes = it },
                            onSave = {
                                viewModel.saveAttendance(
                                    workerId = workerId,
                                    date = selectedDate,
                                    status = status,
                                    hoursWorked = hoursWorked.toIntOrNull() ?: 0,
                                    overtimeHours = overtimeHours.toIntOrNull() ?: 0,
                                    notes = notes.ifBlank { null },
                                    photoUrl = null
                                )
                            },
                            onCancel = { showAddForm = false },
                            isLoading = saveResult is SaveAttendanceResult.Loading,
                            errorMessage = if (saveResult is SaveAttendanceResult.Error) {
                                (saveResult as SaveAttendanceResult.Error).message
                            } else null
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attendance Records",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Button(onClick = { showAddForm = true }) {
                                Text("Add Record")
                            }
                        }
                        
                        if (state.attendances.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No attendance records yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(state.attendances, key = { it.localId }) { attendance ->
                                    AttendanceRecordItem(attendance = attendance)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddAttendanceForm(
    selectedDate: Long,
    onDateChange: (Long) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit,
    hoursWorked: String,
    onHoursWorkedChange: (String) -> Unit,
    overtimeHours: String,
    onOvertimeHoursChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Attendance Record",
            style = MaterialTheme.typography.titleLarge
        )

        // Date Picker (simplified - using text field for now)
        OutlinedTextField(
            value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate)),
            onValueChange = { },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                    IconButton(onClick = { /* TODO: Show date picker dialog */ }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
            }
        )

        // Status Dropdown
        OutlinedTextField(
            value = status,
            onValueChange = onStatusChange,
            label = { Text("Status") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Present, Absent, Late, etc.") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hoursWorked,
                onValueChange = onHoursWorkedChange,
                label = { Text("Hours Worked") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = overtimeHours,
                onValueChange = onOvertimeHoursChange,
                label = { Text("Overtime Hours") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun AttendanceRecordItem(attendance: AttendanceEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(attendance.date)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!attendance.synced) {
                    androidx.compose.material3.AssistChip(
                        onClick = { },
                        label = { Text("Pending Sync", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Text(
                text = "Status: ${attendance.status}",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hours: ${attendance.hoursWorked}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Overtime: ${attendance.overtimeHours}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!attendance.notes.isNullOrBlank()) {
                Text(
                    text = "Notes: ${attendance.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
