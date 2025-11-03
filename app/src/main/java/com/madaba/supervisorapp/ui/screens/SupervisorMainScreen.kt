// FILE: app/src/main/java/com/madaba/supervisorapp/ui/screens/SupervisorMainScreen.kt
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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.ui.viewmodels.SupervisorMainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorMainScreen(
    viewModel: SupervisorMainViewModel = hiltViewModel(),
    onWorkerClicked: (String) -> Unit,
    onSyncClicked: () -> Unit,
    onOfflineQueueClicked: () -> Unit
) {
    val workers by viewModel.workers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Attendance") },
                actions = {
                    IconButton(onClick = onOfflineQueueClicked) {
                        Icon(Icons.Default.DateRange, contentDescription = "Offline Queue")
                    }
                    IconButton(onClick = onSyncClicked) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Now")
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