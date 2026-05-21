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
import com.example.acadtrack.data.model.Teacher
import com.example.acadtrack.ui.viewmodel.DepartmentViewModel
import com.example.acadtrack.ui.viewmodel.TeacherViewModel
import com.example.acadtrack.ui.viewmodel.ViewModelFactory
import com.example.acadtrack.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    teacherViewModel: TeacherViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    deptViewModel: DepartmentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val teachersState by teacherViewModel.teachersState.collectAsState()
    val departmentsState by deptViewModel.departmentsState.collectAsState()
    val createTeacherState by teacherViewModel.createTeacherState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf<Department?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var teacherToDelete by remember { mutableStateOf<Teacher?>(null) }

    LaunchedEffect(Unit) {
        teacherViewModel.fetchTeachers()
        deptViewModel.fetchDepartments()
    }

    LaunchedEffect(createTeacherState) {
        createTeacherState?.let {
            if (it is Resource.Success) {
                Toast.makeText(context, "Teacher created", Toast.LENGTH_SHORT).show()
                showAddDialog = false
                name = ""; email = ""; password = ""; empId = ""; designation = ""; phone = ""; selectedDept = null
                teacherViewModel.resetCreateState()
            } else if (it is Resource.Error) {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text("Manage Teachers", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Teacher")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = teachersState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    LazyColumn {
                        items(state.data ?: emptyList()) { teacher ->
                            TeacherItem(
                                teacher = teacher,
                                onDelete = {
                                    teacherToDelete = teacher
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message ?: "Error") }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Teacher") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = empId, onValueChange = { empId = it }, label = { Text("Employee ID") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = selectedDept?.name ?: "Select Department",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Department") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                val depts = (departmentsState as? Resource.Success)?.data ?: emptyList()
                                depts.forEach { dept ->
                                    DropdownMenuItem(text = { Text(dept.name) }, onClick = { selectedDept = dept; expanded = false })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && selectedDept != null) {
                        teacherViewModel.createTeacher(name, email, password, empId, selectedDept!!.id, designation, phone)
                    } else {
                        Toast.makeText(context, "Fill required fields (Name, Email, Password, Dept)", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Create") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && teacherToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Teacher") },
            text = { Text("Are you sure you want to delete ${teacherToDelete?.user?.name ?: "Unknown"}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        teacherViewModel.deleteTeacher(teacherToDelete!!.id)
                        showDeleteDialog = false
                        teacherToDelete = null
                        Toast.makeText(context, "Teacher deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TeacherItem(teacher: Teacher, onDelete: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = teacher.user?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = teacher.designation ?: "Teacher", 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ID: ${teacher.employeeId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dept: ${teacher.department?.name ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
