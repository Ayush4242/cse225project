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
fun TimetableScreen(
    timetableViewModel: TimetableViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    sectionViewModel: SectionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    mappingViewModel: MappingViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val timetableState by timetableViewModel.timetableState.collectAsState()
    val sectionsState by sectionViewModel.sectionsState.collectAsState()
    val mappingsState by mappingViewModel.mappingsState.collectAsState()

    var selectedSection by remember { mutableStateOf<Section?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }

    // Slot Form States
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    var selectedDay by remember { mutableStateOf(days[0]) }
    var dayExpanded by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var selectedMapping by remember { mutableStateOf<Mapping?>(null) }
    var mappingExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sectionViewModel.fetchSections()
    }

    LaunchedEffect(selectedSection) {
        selectedSection?.id?.let { id ->
            timetableViewModel.fetchTimetable(id)
            mappingViewModel.fetchMappings(sectionId = id)
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text("Timetable Builder", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) 
        },
        floatingActionButton = {
            if (selectedSection != null) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Slot")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Section Selector
            ExposedDropdownMenuBox(
                expanded = sectionExpanded,
                onExpandedChange = { sectionExpanded = !sectionExpanded },
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = selectedSection?.let { "${it.classObj?.name} - ${it.name}" } ?: "Select Section to View",
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

            // Timetable Display
            when (val state = timetableState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    val slots = state.data?.slots ?: emptyList()
                    if (slots.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No slots scheduled yet") }
                    } else {
                        LazyColumn {
                            val groupedSlots = slots.groupBy { it.day }
                            days.forEach { day ->
                                val daySlots = groupedSlots[day] ?: emptyList()
                                if (daySlots.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    items(daySlots.sortedBy { it.startTime }) { slot ->
                                        SlotItem(
                                            slot = slot,
                                            onDelete = {
                                                selectedSection?.id?.let { sid ->
                                                    slot.id?.let { tid -> timetableViewModel.deleteSlot(sid, tid) }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message ?: "Error") }
                else -> {
                    if (selectedSection == null) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Please select a section") }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Time Slot") },
            text = {
                Column {
                    // Day Selector
                    ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = !dayExpanded }) {
                        OutlinedTextField(
                            value = selectedDay, onValueChange = {}, readOnly = true, label = { Text("Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                            days.forEach { day ->
                                DropdownMenuItem(text = { Text(day) }, onClick = { selectedDay = day; dayExpanded = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Time Inputs (Simplified for now)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start (HH:mm)") }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End (HH:mm)") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mapping Selector (Subject + Teacher)
                    ExposedDropdownMenuBox(expanded = mappingExpanded, onExpandedChange = { mappingExpanded = !mappingExpanded }) {
                        OutlinedTextField(
                            value = selectedMapping?.let { "${it.subject?.name ?: "Unknown"} (${it.teacher?.user?.name ?: "N/A"})" } ?: "Select Subject-Teacher",
                            onValueChange = {}, readOnly = true, label = { Text("Mapping") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mappingExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = mappingExpanded, onDismissRequest = { mappingExpanded = false }) {
                            val mappings = (mappingsState as? Resource.Success)?.data ?: emptyList()
                            mappings.forEach { mapping ->
                                DropdownMenuItem(
                                    text = { Text("${mapping.subject?.name ?: "Unknown"} - ${mapping.teacher?.user?.name ?: "N/A"}") },
                                    onClick = { selectedMapping = mapping; mappingExpanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (selectedMapping != null && selectedSection != null) {
                        val teacherId = selectedMapping!!.teacher?.id
                            val subjectId = selectedMapping!!.subject?.id
                            if (teacherId != null && subjectId != null) {
                                timetableViewModel.addSlot(
                                    sectionId = selectedSection!!.id!!,
                                    day = selectedDay,
                                    startTime = startTime,
                                    endTime = endTime,
                                    subjectId = subjectId,
                                    teacherId = teacherId
                                )
                                showAddDialog = false
                            } else {
                                Toast.makeText(context, "Mapping is missing teacher or subject", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Add Slot") }
            }
        )
    }
}

@Composable
fun SlotItem(slot: TimeSlot, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${slot.startTime} - ${slot.endTime}", 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = slot.subject?.name ?: "Deleted Subject", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Prof. ${slot.teacher?.user?.name ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
