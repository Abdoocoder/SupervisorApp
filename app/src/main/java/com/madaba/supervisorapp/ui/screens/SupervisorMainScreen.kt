package com.madaba.supervisorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.madaba.supervisorapp.data.models.UserRole
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.ui.viewmodels.SupervisorMainViewModel

/**
 * A Composable function that represents the main screen for a supervisor.
 *
 * This screen displays a list of workers under the supervisor's purview. It provides
 * a top app bar with actions to view the offline queue and to trigger a manual data sync.
 * The main content area shows a list of workers, and each worker item is clickable,
 * leading to a detail screen. If no workers are available, a loading/empty state message is shown.
 *
 * @param viewModel The [SupervisorMainViewModel] instance for this screen, provided by Hilt. It manages the screen's state, such as the list of workers.
 * @param onWorkerClicked A lambda function to be invoked when a worker item in the list is clicked. It passes the worker's ID as a [String].
 * @param onSyncClicked A lambda function to be invoked when the sync icon in the top app bar is clicked.
 * @param onOfflineQueueClicked A lambda function to be invoked when the offline queue icon (DateRange) in the top app bar is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorMainScreen(
    viewModel: SupervisorMainViewModel = hiltViewModel(),
    onWorkerClicked: (String) -> Unit,
    onSyncClicked: () -> Unit,
    onOfflineQueueClicked: () -> Unit,
    onManageWorkersClicked: () -> Unit = {},
    onManageSupervisorsClicked: () -> Unit = {},
    onManageAreasClicked: () -> Unit = {}
) {
    val workers by viewModel.workers.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isAdmin = userRole == UserRole.Admin
    
    LaunchedEffect(syncState) {
        if (syncState is com.madaba.supervisorapp.ui.viewmodels.SyncState.Success) {
            // Reset state after showing success
            kotlinx.coroutines.delay(2000)
            viewModel.resetSyncState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Attendance") },
                actions = {
                    IconButton(onClick = onOfflineQueueClicked) {
                        Icon(Icons.Default.DateRange, contentDescription = "Offline Queue")
                    }
                    if (isAdmin) {
                        IconButton(onClick = onManageWorkersClicked) {
                            Icon(Icons.Default.Settings, contentDescription = "Manage Workers")
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.syncAll()
                            onSyncClicked()
                        },
                        enabled = syncState !is com.madaba.supervisorapp.ui.viewmodels.SyncState.Loading
                    ) {
                        if (syncState is com.madaba.supervisorapp.ui.viewmodels.SyncState.Loading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (workers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No workers found. Syncing...")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(workers, key = { it.id }) { worker ->
                    WorkerItem(worker = worker, onClick = { onWorkerClicked(worker.id) })
                }
            }
        }
    }
}

@Composable
fun WorkerItem(worker: Worker, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = worker.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Text(text = worker.role, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}