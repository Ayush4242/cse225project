package com.example.acadtrack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.acadtrack.ui.viewmodel.StudentPortalViewModel
import com.example.acadtrack.utils.Resource
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnalyticsScreen(
    viewModel: StudentPortalViewModel,
    onBack: () -> Unit
) {
    val analyticsState by viewModel.analyticsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAnalytics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = analyticsState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val data = state.data ?: emptyList()
                    if (data.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp))
                            Text("No attendance data found")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(data) { subjectStat ->
                                // Note: Using Map since the exact subject analytics model 
                                // might vary or be dynamic from backend aggregation
                                StudentSubjectAnalyticsCard(subjectStat)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        state.message ?: "Error loading stats",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun StudentSubjectAnalyticsCard(stat: Map<String, Any>) {
    val subjectName = stat["subjectName"] as? String ?: "Unknown Subject"
    val present = (stat["attendanceCount"] as? Double)?.toInt() ?: 0
    val total = (stat["totalSessions"] as? Double)?.toInt() ?: 1
    val percentage = (stat["percentage"] as? Double) ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = subjectName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", percentage)}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (percentage < 75) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentage.toFloat() / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (percentage < 75) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Present: $present / $total sessions",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (percentage < 75) {
                Text(
                    text = "Low Attendance Warning!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
