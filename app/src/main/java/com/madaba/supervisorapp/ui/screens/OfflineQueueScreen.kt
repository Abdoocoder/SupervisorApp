
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
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.ui.viewmodels.OfflineQueueUiState
import com.madaba.supervisorapp.ui.viewmodels.OfflineQueueViewModel

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
                        Text(
                            text = "Last sync: just now",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                        )
                        WorkerList(workers = state.workers)
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
fun WorkerList(workers: List<Worker>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(workers) { worker ->
            WorkerItem(worker = worker)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun WorkerItem(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = worker.role,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
