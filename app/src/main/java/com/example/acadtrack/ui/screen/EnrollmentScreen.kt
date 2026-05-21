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
import com.example.acadtrack.data.model.*
import com.example.acadtrack.ui.viewmodel.*
import com.example.acadtrack.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    enrollmentViewModel: EnrollmentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    mappingViewModel: MappingViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    studentViewModel: StudentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val enrollmentsState by enrollmentViewModel.enrollmentsState.collectAsState()
    val mappingsState by mappingViewModel.mappingsState.collectAsState()
    val studentsState by studentViewModel.studentsState.collectAsState()
    val createEnrollmentState by enrollmentViewModel.createEnrollmentState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMapping by remember { mutableStateOf<Mapping?>(null) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    var mappingExpanded by remember { mutableStateOf(false) }
    var studentExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        enrollmentViewModel.fetchEnrollments()
        mappingViewModel.fetchMappings()
        studentViewModel.fetchStudents()
    }

    LaunchedEffect(createEnrollmentState) {
        createEnrollmentState?.let {
            if (it is Resource.Success) {
                Toast.makeText(context, "Student Enrolled Successfully", Toast.LENGTH_SHORT).show()
                showAddDialog = false
                selectedMapping = null
                selectedStudent = null
                enrollmentViewModel.resetCreateState()
            } else if (it is Resource.Error) {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text("Student Class Allocations", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Enrollment")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = enrollmentsState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    if (state.data.isNullOrEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No enrollments found.") }
                    } else {
                        LazyColumn {
                            items(state.data) { enrollment ->
                                EnrollmentItem(
                                    enrollment = enrollment,
                                    onDelete = { enrollmentViewModel.deleteEnrollment(enrollment.id) }
                                )
                            }
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
            title = { Text("Allocate Student to Class") },
            text = {
                Column {
                    // Mapping Dropdown
                    ExposedDropdownMenuBox(expanded = mappingExpanded, onExpandedChange = { mappingExpanded = !mappingExpanded }) {
                        OutlinedTextField(
                            value = selectedMapping?.let { "${it.subject?.name ?: "Unknown"} - ${it.section?.name ?: "Unknown"}" } ?: "Select Class/Subject",
                            onValueChange = {}, readOnly = true, label = { Text("Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mappingExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = mappingExpanded, onDismissRequest = { mappingExpanded = false }) {
                            val mappings = (mappingsState as? Resource.Success)?.data ?: emptyList()
                            mappings.forEach { mapping ->
                                DropdownMenuItem(
                                    text = { Text("${mapping.subject?.name ?: "Unknown"} - ${mapping.section?.name ?: "Unknown"}") },
                                    onClick = { selectedMapping = mapping; mappingExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Student Dropdown
                    ExposedDropdownMenuBox(expanded = studentExpanded, onExpandedChange = { studentExpanded = !studentExpanded }) {
                        OutlinedTextField(
                            value = selectedStudent?.let { "${it.user?.name ?: "Unknown"} (${it.rollNumber})" } ?: "Select Student",
                            onValueChange = {}, readOnly = true, label = { Text("Student") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = studentExpanded, onDismissRequest = { studentExpanded = false }) {
                            val students = (studentsState as? Resource.Success)?.data ?: emptyList()
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text("${student.user?.name ?: "Unknown"} (${student.rollNumber})") },
                                    onClick = { selectedStudent = student; studentExpanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (selectedMapping != null && selectedStudent != null) {
                        enrollmentViewModel.createEnrollment(selectedStudent!!.id, selectedMapping!!.id)
                    } else {
                        Toast.makeText(context, "Please select both Class and Student", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Enroll") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun EnrollmentItem(enrollment: Enrollment, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Student: ${enrollment.student?.user?.name ?: "Unknown"} (${enrollment.student?.rollNumber ?: "N/A"})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Class: ${enrollment.mapping?.subject?.name ?: "No Subject"} - ${enrollment.mapping?.section?.name ?: "No Section"}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Teacher: ${enrollment.mapping?.teacher?.user?.name ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
