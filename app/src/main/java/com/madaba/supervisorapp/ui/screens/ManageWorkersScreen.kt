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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.ui.viewmodels.ManageWorkersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWorkersScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageWorkersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val supervisors by viewModel.supervisors.collectAsState()
    val areas by viewModel.areas.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingWorker by remember { mutableStateOf<Worker?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة العمال") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Worker")
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is ManageWorkersUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ManageWorkersUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as ManageWorkersUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }
            is ManageWorkersUiState.Success -> {
                if (workers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا يوجد عمال")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workers, key = { it.id }) { worker ->
                            WorkerManagementItem(
                                worker = worker,
                                onEdit = { editingWorker = it },
                                onDelete = { viewModel.deleteWorker(it.id) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog || editingWorker != null) {
            WorkerEditDialog(
                worker = editingWorker,
                supervisors = supervisors,
                areas = areas,
                onDismiss = {
                    showAddDialog = false
                    editingWorker = null
                },
                onSave = { worker ->
                    if (editingWorker != null) {
                        viewModel.updateWorker(worker)
                    } else {
                        viewModel.createWorker(worker)
                    }
                    showAddDialog = false
                    editingWorker = null
                }
            )
        }
    }
}

@Composable
fun WorkerManagementItem(
    worker: Worker,
    onEdit: (Worker) -> Unit,
    onDelete: (Worker) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = worker.role,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = { onEdit(worker) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(worker) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun WorkerEditDialog(
    worker: Worker?,
    supervisors: List<com.madaba.supervisorapp.data.models.Supervisor>,
    areas: List<com.madaba.supervisorapp.data.models.Area>,
    onDismiss: () -> Unit,
    onSave: (Worker) -> Unit
) {
    var name by remember(key1 = worker) { mutableStateOf(worker?.name ?: "") }
    var role by remember(key1 = worker) { mutableStateOf(worker?.role ?: "") }
    var selectedSupervisorId by remember(key1 = worker) { mutableStateOf(worker?.supervisorId ?: "") }
    var selectedAreaId by remember(key1 = worker) { mutableStateOf(worker?.areaId ?: "") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (worker != null) "تعديل عامل" else "إضافة عامل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("المنصب") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Supervisor dropdown (simplified)
                OutlinedTextField(
                    value = selectedSupervisorId,
                    onValueChange = { selectedSupervisorId = it },
                    label = { Text("معرف المراقب") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Area dropdown (simplified)
                OutlinedTextField(
                    value = selectedAreaId,
                    onValueChange = { selectedAreaId = it },
                    label = { Text("معرف المنطقة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Worker(
                            id = worker?.id ?: "",
                            name = name,
                            role = role,
                            supervisorId = selectedSupervisorId,
                            areaId = selectedAreaId
                        )
                    )
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

sealed class ManageWorkersUiState {
    object Loading : ManageWorkersUiState()
    data class Success(val message: String? = null) : ManageWorkersUiState()
    data class Error(val message: String) : ManageWorkersUiState()
}

