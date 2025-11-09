package com.madaba.supervisorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.madaba.supervisorapp.R
import com.madaba.supervisorapp.data.models.Area
import com.madaba.supervisorapp.data.models.Supervisor
import com.madaba.supervisorapp.data.models.Worker
import com.madaba.supervisorapp.ui.viewmodels.ManageWorkersViewModel

/**
 * Composable function for the Manage Workers screen.
 *
 * This screen allows users to view, add, edit, delete, search, and sort workers.
 *
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param viewModel The ViewModel for this screen, injected by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWorkersScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageWorkersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val supervisors by viewModel.supervisors.collectAsState(initial = emptyList())
    val areas by viewModel.areas.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var editingWorker by remember { mutableStateOf<Worker?>(null) }
    var workerToDelete by remember { mutableStateOf<Worker?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.Name) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_workers_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_worker_button_description))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            when (uiState) {
                is ManageWorkersUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ManageWorkersUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as ManageWorkersUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }) { Text(stringResource(R.string.retry)) }
                    }
                }

                is ManageWorkersUiState.Success -> {
                    val allWorkers = (uiState as ManageWorkersUiState.Success).workers

                    // Filter and sort workers
                    val filteredWorkers = allWorkers.filter { worker ->
                        val supervisorName = supervisors.find { it.id == worker.supervisorId }?.name ?: ""
                        val areaName = areas.find { it.id == worker.areaId }?.name ?: ""
                        worker.name.contains(searchQuery, ignoreCase = true) ||
                                worker.role.contains(searchQuery, ignoreCase = true) ||
                                supervisorName.contains(searchQuery, ignoreCase = true) ||
                                areaName.contains(searchQuery, ignoreCase = true)
                    }.let { list ->
                        when (sortOption) {
                            SortOption.Name -> list.sortedBy { it.name }
                            SortOption.Area -> list.sortedBy { areas.find { area -> area.id == it.areaId }?.name }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search and sort bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(stringResource(R.string.search)) },
                                modifier = Modifier.weight(1f)
                            )
                            DropdownMenuField(
                                label = stringResource(R.string.sort_by),
                                items = SortOption.entries,
                                selectedItem = sortOption,
                                onItemSelected = { sortOption = it }
                            )
                        }

                        if (filteredWorkers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_workers))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredWorkers, key = { it.id }) { worker ->
                                    WorkerItem(
                                        worker = worker,
                                        supervisors = supervisors,
                                        areas = areas,
                                        onEdit = { editingWorker = it },
                                        onDelete = { workerToDelete = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add/Edit Worker Dialog
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
                        if (editingWorker != null) viewModel.updateWorker(worker)
                        else viewModel.createWorker(worker)
                        showAddDialog = false
                        editingWorker = null
                    }
                )
            }

            // Delete Confirmation Dialog
            workerToDelete?.let { worker ->
                DeleteConfirmationDialog(
                    worker = worker,
                    onConfirm = {
                        viewModel.deleteWorker(worker.id)
                        workerToDelete = null
                    },
                    onDismiss = { workerToDelete = null }
                )
            }
        }
    }
}

/**
 * Composable for displaying a single worker's information.
 *
 * @param worker The worker to display.
 * @param supervisors The list of all supervisors.
 * @param areas The list of all areas.
 * @param onEdit Callback to handle the edit action.
 * @param onDelete Callback to handle the delete action.
 */
@Composable
fun WorkerItem(
    worker: Worker,
    supervisors: List<Supervisor>,
    areas: List<Area>,
    onEdit: (Worker) -> Unit,
    onDelete: (Worker) -> Unit
) {
    val supervisorName = supervisors.find { it.id == worker.supervisorId }?.name ?: stringResource(R.string.undefined)
    val areaName = areas.find { it.id == worker.areaId }?.name ?: stringResource(R.string.undefined)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(worker.name, style = MaterialTheme.typography.titleMedium)
                Text(worker.role, style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.supervisor_label, supervisorName), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.area_label, areaName), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = { onEdit(worker) }) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_button_description)) }
                IconButton(onClick = { onDelete(worker) }) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_button_description)) }
            }
        }
    }
}

/**
 * Composable for the Add/Edit Worker dialog.
 *
 * @param worker The worker to edit, or null if adding a new worker.
 * @param supervisors The list of all supervisors.
 * @param areas The list of all areas.
 * @param onDismiss Callback to dismiss the dialog.
 * @param onSave Callback to save the worker.
 */
@Composable
fun WorkerEditDialog(
    worker: Worker?,
    supervisors: List<Supervisor>,
    areas: List<Area>,
    onDismiss: () -> Unit,
    onSave: (Worker) -> Unit
) {
    var name by remember(key1 = worker) { mutableStateOf(worker?.name ?: "") }
    var role by remember(key1 = worker) { mutableStateOf(worker?.role ?: "") }
    var selectedSupervisor by remember(key1 = worker) {
        mutableStateOf(supervisors.find { it.id == worker?.supervisorId })
    }
    var selectedArea by remember(key1 = worker) {
        mutableStateOf(areas.find { it.id == worker?.areaId })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (worker != null) stringResource(R.string.edit_worker_dialog_title) else stringResource(R.string.add_worker_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text(stringResource(R.string.role_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenuField(
                    label = stringResource(R.string.supervisor_dropdown_label),
                    items = supervisors,
                    selectedItem = selectedSupervisor,
                    onItemSelected = { selectedSupervisor = it }
                )

                DropdownMenuField(
                    label = stringResource(R.string.area_dropdown_label),
                    items = areas,
                    selectedItem = selectedArea,
                    onItemSelected = { selectedArea = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && role.isNotBlank() && selectedSupervisor != null && selectedArea != null) {
                        onSave(
                            Worker(
                                id = worker?.id ?: "",
                                name = name,
                                role = role,
                                supervisorId = selectedSupervisor!!.id,
                                areaId = selectedArea!!.id
                            )
                        )
                    }
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/**
 * A generic dropdown menu field.
 *
 * @param T The type of the items in the dropdown.
 * @param label The label for the dropdown field.
 * @param items The list of items to display in the dropdown.
 * @param selectedItem The currently selected item.
 * @param onItemSelected Callback to handle the selection of an item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenuField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit
) where T : Any {
    var expanded by remember { mutableStateOf(false) }
    val displayText = when (selectedItem) {
        is Supervisor -> selectedItem.name
        is Area -> selectedItem.name
        is SortOption -> when (selectedItem) {
            SortOption.Name -> stringResource(id = R.string.sort_option_name)
            SortOption.Area -> stringResource(id = R.string.sort_option_area)
        }
        else -> selectedItem?.toString() ?: ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                val itemName = when (item) {
                    is Supervisor -> item.name
                    is Area -> item.name
                    is SortOption -> when (item) {
                        SortOption.Name -> stringResource(id = R.string.sort_option_name)
                        SortOption.Area -> stringResource(id = R.string.sort_option_area)
                    }
                    else -> item.toString()
                }
                DropdownMenuItem(
                    text = { Text(itemName) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Composable for the delete confirmation dialog.
 *
 * @param worker The worker to be deleted.
 * @param onConfirm Callback to confirm the deletion.
 * @param onDismiss Callback to dismiss the dialog.
 */
@Composable
fun DeleteConfirmationDialog(
    worker: Worker,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_confirmation_dialog_title)) },
        text = { Text(stringResource(R.string.delete_confirmation_dialog_text, worker.name)) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/**
 * Represents the UI state for the Manage Workers screen.
 */
sealed class ManageWorkersUiState {
    /** The screen is currently loading data. */
    object Loading : ManageWorkersUiState()
    /** The data has been loaded successfully. */
    data class Success(val workers: List<Worker>) : ManageWorkersUiState()
    /** An error occurred while loading data. */
    data class Error(val message: String) : ManageWorkersUiState()
}

/**
 * Represents the options for sorting the list of workers.
 */
enum class SortOption {
    Name, Area
}