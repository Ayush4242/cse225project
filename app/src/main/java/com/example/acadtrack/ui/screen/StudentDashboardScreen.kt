package com.example.acadtrack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.acadtrack.ui.navigation.Screen
import com.example.acadtrack.ui.components.GreetingWeatherCard
import com.example.acadtrack.ui.viewmodel.StudentPortalViewModel
import com.example.acadtrack.utils.Resource
import java.util.Locale

data class StudentDashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: StudentPortalViewModel,
    userName: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val analyticsState by viewModel.analyticsState.collectAsState()
    val activeSessionsState by viewModel.activeSessionsState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.fetchAnalytics()
        viewModel.fetchActiveSessions()
    }

    val overallPercentage = remember(analyticsState) {
        if (analyticsState is Resource.Success) {
            val data = (analyticsState as Resource.Success).data ?: emptyList()
            if (data.isEmpty()) 0.0 else data.map { it["percentage"] as? Double ?: 0.0 }.average()
        } else 0.0
    }

    val riskLevel = remember(overallPercentage) {
        when {
            overallPercentage >= 75 -> "Safe"
            overallPercentage >= 60 -> "Warning"
            else -> "Critical"
        }
    }

    val riskColor = when (riskLevel) {
        "Safe" -> MaterialTheme.colorScheme.primary
        "Warning" -> androidx.compose.ui.graphics.Color(0xFFFFA000)
        else -> MaterialTheme.colorScheme.error
    }

    val items = listOf(
        StudentDashboardItem("Mark Attendance", Icons.Default.QrCodeScanner, Screen.StudentScanner.route),
        StudentDashboardItem("My Analytics", Icons.Default.BarChart, Screen.StudentAnalytics.route),
        StudentDashboardItem("Holidays", Icons.Default.Event, Screen.Holidays.route),
        StudentDashboardItem("Leaves", Icons.Default.Event, Screen.Leaves.route),
        StudentDashboardItem("Notifications", Icons.Default.Notifications, Screen.Notifications.route)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Student Dashboard", 
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            GreetingWeatherCard(
                userName = userName,
                modifier = Modifier.padding(16.dp)
            )

            // Active Sessions
            if (activeSessionsState is Resource.Success) {
                val activeSessions = (activeSessionsState as Resource.Success).data ?: emptyList()
                if (activeSessions.isNotEmpty()) {
                    Text(
                        text = "Ongoing Classes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    activeSessions.forEach { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            onClick = { onNavigate(Screen.StudentScanner.route) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = session.subject.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Tap to Join", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan to Join")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Quick Stats summary
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Attendance Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Overall", "${String.format(Locale.getDefault(), "%.1f", overallPercentage)}%")
                        StatItem("Risk", riskLevel, riskColor)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        onClick = { onNavigate(item.route) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = item.title, 
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
    }
}
