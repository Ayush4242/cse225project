package com.example.acadtrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.acadtrack.data.model.Attendance
import com.example.acadtrack.data.model.Session
import com.example.acadtrack.ui.viewmodel.TeacherPortalViewModel
import com.example.acadtrack.utils.QrUtils
import com.example.acadtrack.utils.Resource
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSessionScreen(
    subjectId: String,
    sectionId: String,
    slotId: String,
    viewModel: TeacherPortalViewModel,
    onBack: () -> Unit
) {
    val activeSessionState by viewModel.activeSessionState.collectAsState()
    val attendanceListState by viewModel.attendanceListState.collectAsState()
    val context = LocalContext.current

    var wasSessionActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchActiveSession()
    }

    LaunchedEffect(activeSessionState) {
        val state = activeSessionState
        if (state is Resource.Success) {
            if (state.data != null) {
                wasSessionActive = true
            } else if (wasSessionActive) {
                Toast.makeText(context, "Session ended successfully", Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                // Initial load saw no active session, stay on screen to show "Start Session"
            }
        } else if (state is Resource.Error) {
            Toast.makeText(context, state.message ?: "Error", Toast.LENGTH_SHORT).show()
            // If the error is 404 because session already ended, we should go back
            if (state.message?.contains("not found", ignoreCase = true) == true && wasSessionActive) {
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Class Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = activeSessionState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val session = state.data
                    if (session == null || !session.isActive) {
                        StartSessionView(
                            onStart = { viewModel.startSession(subjectId, sectionId, slotId) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        ActiveSessionUI(
                            session = session,
                            attendanceListState = attendanceListState,
                            onEndSession = { viewModel.endSession(session.id) }
                        )
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.fetchActiveSession() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StartSessionView(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.QrCode,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Ready to start the class?",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Starting a session will generate a QR code for students to scan.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Session")
        }
    }
}

@Composable
fun ActiveSessionUI(
    session: Session,
    attendanceListState: Resource<List<Attendance>>,
    onEndSession: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("QR Code", "Attendance")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> QrDisplayView(session = session)
                1 -> AttendanceListView(attendanceListState = attendanceListState)
            }
        }

        // Global End Session Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Button(
                onClick = onEndSession,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("End Session")
            }
        }
    }
}

@Composable
fun QrDisplayView(session: Session) {
    val qrBitmap = remember(session.qrCodeToken) {
        QrUtils.generateQrCode(
            text = "{\"sessionId\":\"${session.id}\",\"token\":\"${session.qrCodeToken}\"}"
        )
    }

    var timeLeft by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(session.qrExpiry) {
        val expiryTime = try {
            val cleanDate = session.qrExpiry.substringBefore(".").replace("Z", "")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(cleanDate)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
        
        while (true) {
            val now = System.currentTimeMillis()
            timeLeft = (expiryTime - now) / 1000
            if (timeLeft <= 0) break
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Scan to Mark Attendance",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${session.subject.name} - ${session.section.name}",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("Error generating QR")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (timeLeft > 0) "QR Refreshes in: ${timeLeft}s" else "Refreshing...",
            color = if (timeLeft < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun AttendanceListView(attendanceListState: Resource<List<Attendance>>) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (attendanceListState) {
            is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is Resource.Success -> {
                val list = attendanceListState.data ?: emptyList()
                if (list.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("No students present yet", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                "Total Present: ${list.size}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(list) { attendance ->
                            AttendanceItem(attendance)
                        }
                    }
                }
            }
            is Resource.Error -> {
                Text(
                    attendanceListState.message ?: "Error loading attendance",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AttendanceItem(attendance: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = attendance.student?.user?.name ?: "Unknown Student",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Roll: ${attendance.student?.rollNumber ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = "PRESENT",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
