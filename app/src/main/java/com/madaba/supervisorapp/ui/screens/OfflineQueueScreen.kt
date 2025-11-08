
package com.madaba.supervisorapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.madaba.supervisorapp.data.source.local.AttendanceEntity
import com.madaba.supervisorapp.ui.viewmodels.OfflineQueueUiState
import com.madaba.supervisorapp.ui.viewmodels.OfflineQueueViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineQueueScreen(
    onNavigateBack: () -> Unit,
    viewModel: OfflineQueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Queue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is OfflineQueueUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is OfflineQueueUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.lastSyncTime != null) {
                                    "Last sync: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(state.lastSyncTime))}"
                                } else {
                                    "No sync yet"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = { viewModel.syncNow() }) {
                                Text("Sync Now")
                            }
                        }
                        if (state.unsyncedAttendances.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All records are synced",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "${state.unsyncedAttendances.size} unsynced records",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            AttendanceList(attendances = state.unsyncedAttendances)
                        }
                    }
                }
                is OfflineQueueUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceList(attendances: List<AttendanceEntity>) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(attendances, key = { it.localId }) { attendance ->
            UnsyncedAttendanceItem(attendance = attendance)
        }
    }
}

@Composable
fun UnsyncedAttendanceItem(attendance: AttendanceEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Worker ID: ${attendance.workerId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = "Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(attendance.date))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Status: ${attendance.status} | Regular Days: ${attendance.regularDays} | Overtime Days: ${attendance.overtimeDays} (${attendance.overtimeType})",
                style = MaterialTheme.typography.bodySmall
            )
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
