package com.example.acadtrack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.acadtrack.data.model.Leave
import com.example.acadtrack.ui.viewmodel.LeaveViewModel
import com.example.acadtrack.utils.Resource
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    viewModel: LeaveViewModel,
    isAdmin: Boolean,
    onBack: () -> Unit
) {
    val leavesState by viewModel.leavesState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Form states
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (isAdmin) {
            viewModel.fetchPendingLeaves()
        } else {
            viewModel.fetchMyLeaves()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAdmin) "Leave Approvals" else "My Leave Requests") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (!isAdmin) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Apply Leave")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = leavesState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is Resource.Success -> {
                    val leaves = state.data ?: emptyList()
                    if (leaves.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text(if (isAdmin) "No pending leave requests." else "You have not submitted any leave requests.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(leaves) { leave ->
                                LeaveCard(leave = leave, isAdmin = isAdmin, onUpdateStatus = { status ->
                                    viewModel.updateLeaveStatus(leave.id, status) { success, message ->
                                        if (success) {
                                            Toast.makeText(context, "Status updated to $status", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, message ?: "Failed to update status", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message ?: "Error", color = MaterialTheme.colorScheme.error) }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Apply for Leave") },
            text = {
                Column {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val datePattern = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
                    if (startDate.trim().matches(datePattern) && endDate.trim().matches(datePattern) && reason.isNotBlank()) {
                        viewModel.createLeave(startDate.trim(), endDate.trim(), reason) { success, message ->
                            if (success) {
                                showAddDialog = false
                                startDate = ""
                                endDate = ""
                                reason = ""
                                Toast.makeText(context, "Leave applied successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, message ?: "Failed to apply leave", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter dates in YYYY-MM-DD format and provide a reason", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun LeaveCard(leave: Leave, isAdmin: Boolean, onUpdateStatus: (String) -> Unit) {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startStr = try { df.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(leave.startDate)!!) } catch (e: Exception) { leave.startDate }
    val endStr = try { df.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(leave.endDate)!!) } catch (e: Exception) { leave.endDate }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = leave.reason, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "From: $startStr", style = MaterialTheme.typography.bodySmall)
                    Text(text = "To: $endStr", style = MaterialTheme.typography.bodySmall)
                }
                LeaveStatusBadge(status = leave.status)
            }
            
            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Applicant: ${leave.user?.name ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                        Text(text = leave.role.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                
                if (leave.status == "pending") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { onUpdateStatus("rejected") }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { onUpdateStatus("approved") }, colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50))) {
                            Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveStatusBadge(status: String) {
    val color = when (status) {
        "approved" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "rejected" -> MaterialTheme.colorScheme.error
        else -> androidx.compose.ui.graphics.Color(0xFFFF9800)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
