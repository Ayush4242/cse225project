package com.example.acadtrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.acadtrack.data.model.Department
import com.example.acadtrack.ui.viewmodel.DepartmentViewModel
import com.example.acadtrack.ui.viewmodel.ViewModelFactory
import com.example.acadtrack.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentScreen(
    viewModel: DepartmentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val departmentsState by viewModel.departmentsState.collectAsState()
    val createState by viewModel.createDepartmentState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deptName by remember { mutableStateOf("") }
    var deptDesc by remember { mutableStateOf("") }

    // Handle Create State
    LaunchedEffect(createState) {
        createState?.let { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(context, "Department added successfully", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                    deptName = ""
                    deptDesc = ""
                    viewModel.resetCreateState()
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message ?: "Error creating department", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Manage Departments", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Department")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = departmentsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val departments = state.data ?: emptyList()
                    if (departments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No departments found")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(departments) { dept ->
                                DepartmentItem(
                                    department = dept,
                                    onDelete = { viewModel.deleteDepartment(dept.id) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "An error occurred", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Department") },
            text = {
                Column {
                    OutlinedTextField(
                        value = deptName,
                        onValueChange = { deptName = it },
                        label = { Text("Department Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deptDesc,
                        onValueChange = { deptDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deptName.isNotBlank()) {
                            viewModel.createDepartment(deptName, deptDesc)
                        } else {
                            Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = createState !is Resource.Loading
                ) {
                    if (createState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Add")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DepartmentItem(department: Department, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = department.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                department.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
