package com.example.acadtrack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.acadtrack.data.model.Mapping
import com.example.acadtrack.ui.viewmodel.TeacherPortalViewModel
import com.example.acadtrack.ui.viewmodel.TimetableViewModel
import com.example.acadtrack.ui.viewmodel.ViewModelFactory
import com.example.acadtrack.utils.Resource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTimetableScreen(
    teacherViewModel: TeacherPortalViewModel,
    timetableViewModel: TimetableViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onBack: () -> Unit
) {
    val myClassesState by teacherViewModel.myClassesState.collectAsState()
    val timetableState by timetableViewModel.timetableState.collectAsState()
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    LaunchedEffect(Unit) {
        teacherViewModel.fetchMyClasses()
    }

    // This is a simplified teacher timetable view
    // Ideally, the backend would have a "get teacher timetable" endpoint
    // For now, we'll fetch timetables for all sections the teacher is mapped to
    val teacherId = (myClassesState as? Resource.Success)?.data?.firstOrNull()?.teacher?.id

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = myClassesState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val sections = state.data?.mapNotNull { it.section }?.distinctBy { it.id } ?: emptyList()
                    if (sections.isEmpty()) {
                        Text("No classes assigned", modifier = Modifier.align(Alignment.Center))
                    } else {
                        // For simplicity, we just show a message saying it's under development
                        // or we could iterate sections. For now, let's just show classes info.
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("Your Weekly Sessions", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            items(state.data ?: emptyList()) { mapping ->
                                MappingInfoCard(mapping)
                            }
                        }
                    }
                }
                is Resource.Error -> Text(state.message ?: "Error", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun MappingInfoCard(mapping: Mapping) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = mapping.subject?.name ?: "Deleted Subject", style = MaterialTheme.typography.titleMedium)
            Text(text = "Section: ${mapping.section?.name ?: "Deleted Section"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Class: ${mapping.section?.classObj?.name ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
