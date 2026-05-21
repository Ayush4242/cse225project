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
fun MappingScreen(
    mappingViewModel: MappingViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    teacherViewModel: TeacherViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    subjectViewModel: SubjectViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    sectionViewModel: SectionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val mappingsState by mappingViewModel.mappingsState.collectAsState()
    val teachersState by teacherViewModel.teachersState.collectAsState()
    val subjectsState by subjectViewModel.subjectsState.collectAsState()
    val sectionsState by sectionViewModel.sectionsState.collectAsState()
    val createState by mappingViewModel.createMappingState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTeacher by remember { mutableStateOf<Teacher?>(null) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var selectedSection by remember { mutableStateOf<Section?>(null) }
    
    var teacherExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mappingViewModel.fetchMappings()
        teacherViewModel.fetchTeachers()
        subjectViewModel.fetchSubjects()
        sectionViewModel.fetchSections()
    }

    LaunchedEffect(createState) {
        createState?.let {
            if (it is Resource.Success) {
                Toast.makeText(context, "Mapping created", Toast.LENGTH_SHORT).show()
                showAddDialog = false
                selectedTeacher = null; selectedSubject = null; selectedSection = null
                mappingViewModel.resetCreateState()
            } else if (it is Resource.Error) {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text("Subject-Teacher Mapping", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Mapping")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = mappingsState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    LazyColumn {
                        items(state.data ?: emptyList()) { mapping ->
                            MappingItem(
                                mapping = mapping,
                                onDelete = { mappingViewModel.deleteMapping(mapping.id) }
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
            title = { Text("Assign Teacher to Subject") },
            text = {
                Column {
                    // Section Dropdown
                    ExposedDropdownMenuBox(expanded = sectionExpanded, onExpandedChange = { sectionExpanded = !sectionExpanded }) {
                        OutlinedTextField(
                            value = selectedSection?.let { "${it.classObj?.name} - ${it.name}" } ?: "Select Section",
                            onValueChange = {}, readOnly = true, label = { Text("Section") },
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
                    Spacer(modifier = Modifier.height(8.dp))

                    // Subject Dropdown
                    ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = !subjectExpanded }) {
                        OutlinedTextField(
                            value = selectedSubject?.name ?: "Select Subject",
                            onValueChange = {}, readOnly = true, label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            val subjects = (subjectsState as? Resource.Success)?.data ?: emptyList()
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = { selectedSubject = subject; subjectExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Teacher Dropdown
                    ExposedDropdownMenuBox(expanded = teacherExpanded, onExpandedChange = { teacherExpanded = !teacherExpanded }) {
                        OutlinedTextField(
                            value = selectedTeacher?.user?.name ?: "Select Teacher",
                            onValueChange = {}, readOnly = true, label = { Text("Teacher") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = teacherExpanded, onDismissRequest = { teacherExpanded = false }) {
                            val teachers = (teachersState as? Resource.Success)?.data ?: emptyList()
                            teachers.forEach { teacher ->
                                DropdownMenuItem(
                                    text = { Text(teacher.user?.name ?: "Unknown") },
                                    onClick = { selectedTeacher = teacher; teacherExpanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (selectedTeacher != null && selectedSubject != null && selectedSection != null) {

                        mappingViewModel.createMapping(
                            selectedTeacher!!.id!!,
                            selectedSubject!!.id!!,
                            selectedSection!!.id!!
                        )

                    } else {
                        Toast.makeText(
                            context,
                            "Please select all fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text("Map")
                }
            }
        )
    }
}

@Composable
fun MappingItem(mapping: Mapping, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mapping.subject?.name ?: "Unknown Subject", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Teacher: ${mapping.teacher?.user?.name ?: "Unknown Teacher"}", 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Section: ${mapping.section?.classObj?.name ?: "N/A"} - ${mapping.section?.name ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
