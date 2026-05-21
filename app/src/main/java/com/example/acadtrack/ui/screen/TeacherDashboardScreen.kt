package com.example.acadtrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.foundation.background
import com.example.acadtrack.ui.components.GreetingWeatherCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.acadtrack.data.model.Mapping
import com.example.acadtrack.data.model.TodayClass
import com.example.acadtrack.ui.navigation.Screen
import com.example.acadtrack.ui.viewmodel.TeacherPortalViewModel
import com.example.acadtrack.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherPortalViewModel,
    userName: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val todayClassesState by viewModel.todayClassesState.collectAsState()
    val myClassesState by viewModel.myClassesState.collectAsState()
    val updateProfileState by viewModel.updateProfileState.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchTodayClasses()
        viewModel.fetchMyClasses()
    }

    LaunchedEffect(updateProfileState) {
        updateProfileState?.let { resource ->
            if (resource is Resource.Success) {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                showProfileDialog = false
                newName = ""; newPassword = ""
            } else if (resource is Resource.Error) {
                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Teacher Dashboard", 
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.fetchTodayClasses()
                        viewModel.fetchMyClasses()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, 
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GreetingWeatherCard(userName = userName)
            }
            
            // Quick Actions Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionCard(
                            title = "Profile",
                            icon = Icons.Default.Person,
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Timetable",
                            icon = Icons.Default.DateRange,
                            onClick = { onNavigate(Screen.TeacherTimetable.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionCard(
                            title = "Holidays",
                            icon = Icons.Default.Event,
                            onClick = { onNavigate(Screen.Holidays.route) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Leaves",
                            icon = Icons.Default.DateRange,
                            onClick = { onNavigate(Screen.Leaves.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Today's Schedule Section
            item {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            when (val state = todayClassesState) {
                is Resource.Loading -> {
                    item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
                }
                is Resource.Success -> {
                    val classes = state.data ?: emptyList()
                    if (classes.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No classes scheduled for today",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(classes) { todayClass ->
                            TodayClassCard(
                                todayClass = todayClass,
                                onSessionClick = {
                                    if (todayClass.status == "ongoing" || todayClass.status == "upcoming") {
                                        onNavigate(Screen.TeacherSession.createRoute(todayClass.subject.id!!, todayClass.section.id!!, todayClass.slotId))
                                    } else {
                                        Toast.makeText(context, "Class schedule has already ended", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAnalyticsClick = {
                                    onNavigate(Screen.TeacherAnalytics.createRoute(todayClass.section.id!!))
                                }
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    item { Text("Error loading today's classes", color = MaterialTheme.colorScheme.error) }
                }
            }

            // All Assigned Classes Section (Always available)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All Assigned Classes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            when (val state = myClassesState) {
                is Resource.Loading -> {
                    item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
                }
                is Resource.Success -> {
                    val mappings = state.data ?: emptyList()
                    if (mappings.isEmpty()) {
                        item { Text("No classes assigned") }
                    } else {
                        items(mappings) { mapping ->
                            MappingCard(
                                mapping = mapping,
                                onSessionClick = {
                                    // Try to find a today class for this mapping
                                    val todayClasses = (todayClassesState as? Resource.Success)?.data ?: emptyList()
                                    val match = todayClasses.find { it.subject.id == mapping.subject?.id && it.section.id == mapping.section?.id }
                                    
                                    if (match != null) {
                                        if (match.status == "ongoing" || match.status == "upcoming") {
                                            onNavigate(Screen.TeacherSession.createRoute(match.subject.id, match.section.id!!, match.slotId))
                                        } else {
                                            Toast.makeText(context, "Class already completed for today", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "No scheduled slot found for this class today", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAnalyticsClick = {
                                    mapping.section?.id?.let { sid ->
                                        onNavigate(Screen.TeacherAnalytics.createRoute(sid))
                                    } ?: Toast.makeText(context, "Section data missing", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    item { Text("Error loading classes", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Update Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New Name (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (Optional)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() || newPassword.isNotBlank()) {
                        viewModel.updateProfile(
                            newName.ifBlank { null },
                            newPassword.ifBlank { null }
                        )
                    } else {
                        showProfileDialog = false
                    }
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title, 
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TodayClassCard(todayClass: TodayClass, onSessionClick: () -> Unit, onAnalyticsClick: () -> Unit) {
    val isOngoing = todayClass.status == "ongoing"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSessionClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isOngoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayClass.subject.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Code: ${todayClass.subject.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                StatusBadge(status = todayClass.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${todayClass.startTime} - ${todayClass.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Section: ${todayClass.section.name}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Class: ${todayClass.section.classObj?.name ?: "N/A"}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onAnalyticsClick) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Analytics",
                        tint = if (isOngoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (todayClass.status == "ongoing") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSessionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Start Session")
                }
            }
        }
    }
}

@Composable
fun MappingCard(mapping: Mapping, onSessionClick: () -> Unit, onAnalyticsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSessionClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mapping.subject?.name ?: "Deleted Subject",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Section: ${mapping.section?.name ?: "Deleted"}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Row {
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Analytics",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "ongoing" -> MaterialTheme.colorScheme.primary
        "completed" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
