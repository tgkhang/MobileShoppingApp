package com.example.shopapp.ui.admin.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopapp.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val event = eventViewModel.selectedEvent

    if (event == null) {
        Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show()
        navController.popBackStack()
        return
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                    Button(
                        onClick = { navController.navigate("edit_event") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Edit")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Event Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (event.eventType == "voucher")
                                Icons.Default.LocalOffer
                            else
                                Icons.Default.FlashOn,
                            contentDescription = event.eventType,
                            tint = if (event.eventType == "voucher")
                                MaterialTheme.colorScheme.primary
                            else
                                Color(0xFFFF9800),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = when (event.eventType) {
                                "voucher" -> "Voucher"
                                "flash sale" -> "Flash Sale"
                                else -> event.eventType.replaceFirstChar { it.uppercase() }
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (event.status) {
                            "active" -> Color(0xFF4CAF50)
                            "upcoming" -> Color(0xFF2196F3)
                            "expired" -> Color(0xFF9E9E9E)
                            else -> Color.Gray
                        }
                    ) {
                        Text(
                            text = event.status.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White
                        )
                    }
                }
            }

            // Basic Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow("Title", event.title)
                    DetailRow("Event ID", event.eventId)
                    DetailRow("Event Type", event.eventType)
                    DetailRow("Description", event.description)
                    DetailRow(
                        "Created At",
                        event.createdAt.toDate().toString()
                    )
                    DetailRow(
                        "Updated At",
                        event.updatedAt.toDate().toString()
                    )
                }
            }

            // Discount Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Discount Information",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow(
                        "Discount Type",
                        if (event.discountType == "percentage") "Percentage" else "Fixed Amount"
                    )
                    DetailRow(
                        "Discount Value",
                        if (event.discountType == "percentage")
                            "${event.discountValue}%"
                        else
                            "$${event.discountValue}"
                    )

                    if (event.minPurchase > 0) {
                        DetailRow("Minimum Purchase", "$${event.minPurchase}")
                    }

                    if (event.maxDiscount > 0) {
                        DetailRow("Maximum Discount", "$${event.maxDiscount}")
                    }

                    if (event.usageLimit > 0) {
                        DetailRow("Usage Limit", "${event.usageLimit} per user")
                    }
                }
            }

            // Validity Period Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Validity Period",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow(
                        "Start Date",
                        dateFormat.format(event.startDate.toDate())
                    )
                    DetailRow(
                        "End Date",
                        dateFormat.format(event.endDate.toDate())
                    )

                    val now = Calendar.getInstance().time
                    val startDate = event.startDate.toDate()
                    val endDate = event.endDate.toDate()

                    val status = when {
                        now.before(startDate) -> "This event hasn't started yet"
                        now.after(endDate) -> "This event has expired"
                        else -> "This event is currently active"
                    }

                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            now.before(startDate) -> Color(0xFF2196F3) // Blue
                            now.after(endDate) -> Color(0xFF9E9E9E) // Gray
                            else -> Color(0xFF4CAF50) // Green
                        }
                    )
                }
            }

            // Applicable Items Card
            if (!event.applicableProducts.isNullOrEmpty() || !event.applicableUsers.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Applicability",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Divider()

                        if (!event.applicableProducts.isNullOrEmpty()) {
                            Text(
                                text = "Applicable Products",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "This event applies to ${event.applicableProducts.size} specific products.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Text(
                                text = "This event applies to all products.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (!event.applicableUsers.isNullOrEmpty()) {
                            Text(
                                text = "Restricted Users",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "This event is restricted to ${event.applicableUsers.size} specific users.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "This event is available to all users.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventViewModel.deleteEvent(event.eventId)
                        Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
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
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.65f)
        )
    }
}