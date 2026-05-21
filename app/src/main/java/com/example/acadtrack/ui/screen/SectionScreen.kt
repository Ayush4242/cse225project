package com.example.acadtrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.acadtrack.data.model.ClassModel
import com.example.acadtrack.data.model.Section
import com.example.acadtrack.ui.viewmodel.ClassViewModel
import com.example.acadtrack.ui.viewmodel.SectionViewModel
import com.example.acadtrack.ui.viewmodel.ViewModelFactory
import com.example.acadtrack.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionScreen(
    sectionViewModel: SectionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    classViewModel: ClassViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val sectionsState by sectionViewModel.sectionsState.collectAsState()
    val classesState by classViewModel.classesState.collectAsState()
    val createSectionState by sectionViewModel.createSectionState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var sectionName by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf<ClassModel?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sectionViewModel.fetchSections()
        classViewModel.fetchClasses()
    }

    LaunchedEffect(createSectionState) {
        createSectionState?.let {
            if (it is Resource.Success) {
                Toast.makeText(context, "Section created", Toast.LENGTH_SHORT).show()
                showAddDialog = false
                sectionName = ""
                roomNumber = ""
                selectedClass = null
                sectionViewModel.resetCreateState()
            } else if (it is Resource.Error) {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text("Manage Sections", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Section")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = sectionsState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    LazyColumn {
                        items(state.data ?: emptyList()) { section ->
                            SectionItem(section)
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
            title = { Text("Add Section") },
            text = {
                Column {
                    OutlinedTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        label = { Text("Section Name (e.g. A)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedClass?.name ?: "Select Class",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            val classes = (classesState as? Resource.Success)?.data ?: emptyList()
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text("${cls.name} (${cls.department?.name})") },
                                    onClick = {
                                        selectedClass = cls
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (sectionName.isNotBlank() && selectedClass != null) {
                        sectionViewModel.createSection(sectionName, selectedClass!!.id, roomNumber)
                    } else {
                        Toast.makeText(context, "Fill all fields correctly", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Create") }
            }
        )
    }
}

@Composable
fun SectionItem(section: Section) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Section ${section.name}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Class: ${section.classObj?.name}", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Room: ${section.roomNumber ?: "N/A"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
    }
}
