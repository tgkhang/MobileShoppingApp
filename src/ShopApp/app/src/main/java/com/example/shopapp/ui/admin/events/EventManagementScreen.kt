package com.example.shopapp.ui.admin.events

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.shopapp.data.model.Event
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.EventViewModel
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.shopapp.ui.admin.products.PageButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@Composable
fun EventManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel?,
    rootNavController: NavController?,
    eventViewModel: EventViewModel,
    onEventClick: (Event) -> Unit
) {
    val events by eventViewModel.events.collectAsState()
    val currentPage by eventViewModel.currentPage.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val hasMoreData by eventViewModel.hasMoreData.collectAsState()
    val totalCount by eventViewModel.totalCount.collectAsState()
    val pageSize by eventViewModel.pageSize.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("") }
    var showFilterOptions by remember { mutableStateOf(false) }

    val totalPages = if (totalCount > 0) {
        ceil(totalCount / pageSize.toDouble()).toInt()
    } else {
        1
    }

    DisposableEffect(Unit) {
        onDispose {
            eventViewModel.resetFiltersAndSearch()
        }
    }

    fun applyFilters() {
        if (filterType.isNotEmpty() && filterStatus.isNotEmpty()) {
            // For AND logic with both type and status filters
            eventViewModel.filterByTypeAndStatus(filterType, filterStatus)
        } else if (filterType.isNotEmpty()) {
            eventViewModel.filterByEventType(filterType)
        } else if (filterStatus.isNotEmpty()) {
            eventViewModel.filterByStatus(filterStatus)
        } else {
            eventViewModel.loadInitialEvents()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .padding(top = 30.dp)) {

        // Header with title and action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event Management",
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter button
                IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Events"
                    )
                }

                // Search button
                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearching) "Close Search" else "Search Events"
                    )
                }

                // Add new event button
                IconButton(onClick = { navController.navigate("add_event") }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Event"
                    )
                }
            }
        }

        // Filter options
        AnimatedVisibility(
            visible = showFilterOptions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Filter options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Event type filter
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Event Type:",
                        modifier = Modifier.width(100.dp)
                    )

                    FilterChip(
                        selected = filterType == "voucher",
                        onClick = {
                            filterType = if (filterType == "voucher") "" else "voucher"
                            applyFilters()
                        },
                        label = { Text("Voucher") },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    FilterChip(
                        selected = filterType == "flash sale",
                        onClick = {
                            filterType = if (filterType == "flash sale") "" else "flash sale"
                            applyFilters()
                        },
                        label = { Text("Flash Sale") }
                    )
                }

                // Status filter
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        modifier = Modifier.width(100.dp)
                    )

                    FilterChip(
                        selected = filterStatus == "active",
                        onClick = {
                            filterStatus = if (filterStatus == "active") "" else "active"
                            applyFilters()
                        },
                        label = { Text("Active") },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    FilterChip(
                        selected = filterStatus == "upcoming",
                        onClick = {
                            filterStatus = if (filterStatus == "upcoming") "" else "upcoming"
                            applyFilters()
                        },
                        label = { Text("Upcoming") },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    FilterChip(
                        selected = filterStatus == "expired",
                        onClick = {
                            filterStatus = if (filterStatus == "expired") "" else "expired"
                            applyFilters()
                        },
                        label = { Text("Expired") }
                    )
                }

                // Clear all filters button
                TextButton(
                    onClick = {
                        filterType = ""
                        filterStatus = ""
                        eventViewModel.loadInitialEvents()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Clear Filters")
                }
            }
        }

        // Search bar (visible when searching)
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isNotEmpty()) {
                        eventViewModel.searchEventsByTitle(it)
                    } else {
                        eventViewModel.loadInitialEvents()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search by title...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            eventViewModel.loadInitialEvents()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Search results count
        if (isSearching) {
            Text(
                text = "${events.size} events found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (events.isEmpty() && searchQuery.isNotEmpty()) {
            // Empty search results
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "No events found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        searchQuery = ""
                        eventViewModel.loadInitialEvents()
                    }) {
                        Text("Clear Search")
                    }
                }
            }
        } else if (isLoading && events.isEmpty()) {
            // Loading state when no events are available
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Events list
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(events) { event ->
                    EventItem(event = event, onEventClick = onEventClick)
                }

                // Only show pagination when not searching
                if (!isSearching && filterType.isEmpty() && filterStatus.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            // Page info text
                            Text(
                                text = "Page ${currentPage + 1} of $totalPages",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 8.dp)
                            )

                            // Pagination controls
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { eventViewModel.goToPage(0) },
                                    enabled = currentPage > 0 && !isLoading,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FirstPage,
                                        contentDescription = "Go to first page",
                                        tint = if (currentPage > 0 && !isLoading)
                                            MaterialTheme.colorScheme.primary
                                        else Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = { eventViewModel.previousPage() },
                                    enabled = currentPage > 0 && !isLoading,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.NavigateBefore,
                                        contentDescription = "Previous page",
                                        tint = if (currentPage > 0 && !isLoading)
                                            MaterialTheme.colorScheme.primary
                                        else Color.Gray
                                    )
                                }
                            }

                            // Page number buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // First page button
                                PageButton(
                                    pageNumber = 1,
                                    isSelected = currentPage == 0,
                                    onClick = { eventViewModel.goToPage(0) },
                                    enabled = !isLoading
                                )

                                // Middle pages
                                for (page in 1 until totalPages - 1) {
                                    if (page <= currentPage + 2 && page >= currentPage - 2) {
                                        PageButton(
                                            pageNumber = page + 1,
                                            isSelected = currentPage == page,
                                            onClick = { eventViewModel.goToPage(page) },
                                            enabled = !isLoading
                                        )
                                    } else if (page == currentPage - 3 || page == currentPage + 3) {
                                        Text(
                                            "...",
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                    }
                                }

                                // Last page button (if more than 1 page)
                                if (totalPages > 1) {
                                    PageButton(
                                        pageNumber = totalPages,
                                        isSelected = currentPage == totalPages - 1,
                                        onClick = { eventViewModel.goToPage(totalPages - 1) },
                                        enabled = !isLoading
                                    )
                                }
                            }

                            // Next/Last page buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { eventViewModel.loadNextPage() },
                                        enabled = hasMoreData && !isLoading,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.NavigateNext,
                                            contentDescription = "Next page",
                                            tint = if (hasMoreData && !isLoading)
                                                MaterialTheme.colorScheme.primary
                                            else Color.Gray
                                        )
                                    }

                                    IconButton(
                                        onClick = { eventViewModel.goToPage(totalPages - 1) },
                                        enabled = hasMoreData && currentPage < totalPages - 1 && !isLoading,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LastPage,
                                            contentDescription = "Last page",
                                            tint = if (hasMoreData && currentPage < totalPages - 1 && !isLoading)
                                                MaterialTheme.colorScheme.primary
                                            else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Overlay loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onEventClick: (Event) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Event type icon and title
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
                            Color(0xFFFF9800), // Orange for flash sales
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (event.status) {
                        "active" -> Color(0xFF4CAF50)
                        "upcoming" -> Color(0xFF2196F3)
                        "expired" -> Color(0xFF9E9E9E)
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = event.status.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event details
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Discount information
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (event.discountType == "percentage")
                        Icons.Default.Percent
                    else
                        Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (event.discountType == "percentage")
                        "${event.discountValue.toInt()}% off"
                    else
                        "$${event.discountValue} off",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date range
            Text(
                text = "Valid: ${dateFormat.format(event.startDate.toDate())} - ${dateFormat.format(event.endDate.toDate())}",
                style = MaterialTheme.typography.bodySmall
            )

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Additional details
                if (event.minPurchase > 0) {
                    Text(
                        text = "Min. Purchase: $${event.minPurchase}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (event.maxDiscount > 0) {
                    Text(
                        text = "Max. Discount: $${event.maxDiscount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (event.usageLimit > 0) {
                    Text(
                        text = "Usage Limit: ${event.usageLimit} per user",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Applicable products (if any)
                if (!event.applicableProducts.isNullOrEmpty()) {
                    Text(
                        text = "Applicable to ${event.applicableProducts.size} products",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Applicable users (if any)
                if (!event.applicableUsers.isNullOrEmpty()) {
                    Text(
                        text = "Restricted to ${event.applicableUsers.size} users",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onEventClick(event) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}