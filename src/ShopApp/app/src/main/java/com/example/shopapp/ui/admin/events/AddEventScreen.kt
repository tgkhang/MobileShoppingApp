package com.example.shopapp.ui.admin.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopapp.data.model.Event
import com.example.shopapp.viewmodel.EventViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    navController: NavController,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Event basic information
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("voucher") } // Default event type

    // Discount information
    var discountType by remember { mutableStateOf("percentage") } // Default discount type
    var discountValue by remember { mutableStateOf("") }
    var minPurchase by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }

    // Date selection
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000))) } // Default 7 days from now

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Error state
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Please fill all required fields!") }

    // Event type dropdown
    var eventTypeExpanded by remember { mutableStateOf(false) }
    val eventTypes = listOf("voucher", "flash sale")

    // Discount type dropdown
    var discountTypeExpanded by remember { mutableStateOf(false) }
    val discountTypes = listOf("percentage", "fixed amount")

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Event") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Event Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = eventTypeExpanded,
                        onExpandedChange = { eventTypeExpanded = !eventTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (eventType) {
                                "voucher" -> "Voucher"
                                "flash sale" -> "Flash Sale"
                                else -> eventType
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Event Type *") },
                            trailingIcon = {
                                Icon(Icons.Default.ExpandMore, "Expand dropdown")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = eventTypeExpanded,
                            onDismissRequest = { eventTypeExpanded = false }
                        ) {
                            eventTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (type) {
                                                "voucher" -> "Voucher"
                                                "flash sale" -> "Flash Sale"
                                                else -> type
                                            }
                                        )
                                    },
                                    onClick = {
                                        eventType = type
                                        eventTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Discount Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Discount Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Discount Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = discountTypeExpanded,
                        onExpandedChange = { discountTypeExpanded = !discountTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (discountType) {
                                "percentage" -> "Percentage"
                                "fixed amount" -> "Fixed Amount"
                                else -> discountType
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Discount Type *") },
                            trailingIcon = {
                                Icon(Icons.Default.ExpandMore, "Expand dropdown")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = discountTypeExpanded,
                            onDismissRequest = { discountTypeExpanded = false }
                        ) {
                            discountTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (type) {
                                                "percentage" -> "Percentage"
                                                "fixed amount" -> "Fixed Amount"
                                                else -> type
                                            }
                                        )
                                    },
                                    onClick = {
                                        discountType = type
                                        discountTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Discount Value
                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text("Discount Value *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        prefix = if (discountType == "fixed amount") { { Text("$") } } else { {} },
                        suffix = if (discountType == "percentage") { { Text("%") } } else { {} },
                        singleLine = true
                    )

                    // Additional Options
                    OutlinedTextField(
                        value = minPurchase,
                        onValueChange = { minPurchase = it },
                        label = { Text("Minimum Purchase") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("$") },
                        singleLine = true
                    )

                    if (discountType == "percentage") {
                        OutlinedTextField(
                            value = maxDiscount,
                            onValueChange = { maxDiscount = it },
                            label = { Text("Maximum Discount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("$") },
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = usageLimit,
                        onValueChange = { usageLimit = it },
                        label = { Text("Usage Limit Per User") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Date Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Validity Period",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Start Date Picker
                    OutlinedTextField(
                        value = dateFormatter.format(startDate),
                        onValueChange = {},
                        label = { Text("Start Date *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "Select date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // End Date Picker
                    OutlinedTextField(
                        value = dateFormatter.format(endDate),
                        onValueChange = {},
                        label = { Text("End Date *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "Select date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Error message
            if (showError) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // Validate required fields
                        if (title.isBlank() || description.isBlank() || discountValue.isBlank()) {
                            showError = true
                            errorMessage = "Please fill all required fields!"
                        } else if (discountValue.toDoubleOrNull() == null ||
                            discountValue.toDoubleOrNull()!! <= 0) {
                            showError = true
                            errorMessage = "Please enter a valid discount value!"
                        } else if (discountType == "percentage" &&
                            discountValue.toDoubleOrNull()!! > 100) {
                            showError = true
                            errorMessage = "Percentage discount cannot exceed 100%!"
                        } else if (endDate.before(startDate)) {
                            showError = true
                            errorMessage = "End date must be after start date!"
                        } else {
                            // Create and save the event
                            showError = false
                            coroutineScope.launch {
                                val newEvent = Event(
                                    eventId = UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    eventType = eventType,
                                    discountType = discountType,
                                    discountValue = discountValue.toDoubleOrNull() ?: 0.0,
                                    minPurchase = minPurchase.toDoubleOrNull() ?: 0.0,
                                    maxDiscount = maxDiscount.toDoubleOrNull() ?: 0.0,
                                    usageLimit = usageLimit.toIntOrNull() ?: 0,
                                    startDate = Timestamp(startDate),
                                    endDate = Timestamp(endDate),
                                    status = calculateStatus(startDate, endDate),
                                    applicableProducts = emptyList(),
                                    applicableUsers = emptyList(),
                                    createdAt = Timestamp.now(),
                                    updatedAt = Timestamp.now()
                                )

                                eventViewModel.addEvent(newEvent)
                                delay(500)
                                navController.popBackStack()
                                Toast.makeText(
                                    context,
                                    "Event added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Date(millis)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Date(millis)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

// Helper function to determine event status based on start and end dates
private fun calculateStatus(startDate: Date, endDate: Date): String {
    val now = Date()
    return when {
        now.before(startDate) -> "upcoming"
        now.after(endDate) -> "expired"
        else -> "active"
    }
}