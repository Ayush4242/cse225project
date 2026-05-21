package com.example.acadtrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.acadtrack.data.model.Section
import com.example.acadtrack.data.model.Student
import com.example.acadtrack.ui.viewmodel.SectionViewModel
import com.example.acadtrack.ui.viewmodel.StudentViewModel
import com.example.acadtrack.ui.viewmodel.ViewModelFactory
import com.example.acadtrack.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    studentViewModel: StudentViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    sectionViewModel: SectionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val studentsState by studentViewModel.studentsState.collectAsState()
    val sectionsState by sectionViewModel.sectionsState.collectAsState()
    val createStudentState by studentViewModel.createStudentState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var parentContact by remember { mutableStateOf("") }
    var admissionYear by remember { mutableStateOf("2024") }
    var selectedSection by remember { mutableStateOf<Section?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sectionExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(Unit) {
        studentViewModel.fetchStudents()
        sectionViewModel.fetchSections()
    }

    LaunchedEffect(createStudentState) {
        createStudentState?.let {
            if (it is Resource.Success) {
                Toast.makeText(context, "Student created", Toast.LENGTH_SHORT).show()
                showAddDialog = false
                name = ""; email = ""; password = ""; rollNumber = ""; parentContact = ""; selectedSection = null
                studentViewModel.resetCreateState()
            } else if (it is Resource.Error) {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Manage Students", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    studentViewModel.fetchStudents(search = it)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search by Roll Number") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            when (val state = studentsState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    LazyColumn {
                        items(state.data ?: emptyList()) { student ->
                            StudentItem(
                                student = student,
                                onDelete = {
                                    studentToDelete = student
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
            title = { Text("Add Student") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, label = { Text("Roll Number") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = parentContact, onValueChange = { parentContact = it }, label = { Text("Parent Contact") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = admissionYear, onValueChange = { admissionYear = it }, label = { Text("Admission Year") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(expanded = sectionExpanded, onExpandedChange = { sectionExpanded = !sectionExpanded }) {
                            OutlinedTextField(
                                value = selectedSection?.let { "${it.classObj?.name} - ${it.name}" } ?: "Select Section",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Section") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = sectionExpanded, onDismissRequest = { sectionExpanded = false }) {
                                val sections = (sectionsState as? Resource.Success)?.data ?: emptyList()
                                sections.forEach { section ->
                                    DropdownMenuItem(
                                        text = { Text("${section.classObj?.name} - ${section.name}") },
                                        onClick = { selectedSection = section; sectionExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (
                        name.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        selectedSection != null
                    ) {

                        studentViewModel.createStudent(
                            name,
                            email,
                            password,
                            rollNumber,
                            selectedSection!!.id!!,
                            parentContact,
                            admissionYear.toIntOrNull() ?: 2024
                        )

                    } else {
                        Toast.makeText(
                            context,
                            "Fill required fields (Name, Email, Password, Section)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text("Create")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to delete ${studentToDelete?.user?.name ?: "Unknown"}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        studentViewModel.deleteStudent(studentToDelete!!.id)
                        showDeleteDialog = false
                        studentToDelete = null
                        Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show()
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
fun StudentItem(student: Student, onDelete: () -> Unit = {}) {
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
                Text(text = student.user?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Student Section",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Roll: ${student.rollNumber} | Year: ${student.admissionYear}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
