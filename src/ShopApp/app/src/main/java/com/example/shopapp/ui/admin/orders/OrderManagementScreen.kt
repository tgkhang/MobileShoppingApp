package com.example.shopapp.ui.admin.orders

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.shopapp.data.model.Order
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.OrderViewModel
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.sp
import com.example.shopapp.ui.admin.products.PageButton
import kotlin.compareTo
import kotlin.div
import kotlin.math.ceil
import kotlin.text.toDouble
import kotlin.text.toInt

@Composable
fun OrderManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel?,
    rootNavController: NavController?,
    orderViewModel: OrderViewModel,
    onOrderClick: (Order) -> Unit
) {
    val orders by orderViewModel.orders.collectAsState()
    val currentPage by orderViewModel.currentPage.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val hasMoreData by orderViewModel.hasMoreData.collectAsState()
    val totalCount by orderViewModel.totalCount.collectAsState()
    val pageSize by orderViewModel.pageSize.collectAsState()
    val currentStatusFilter by orderViewModel.currentStatusFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentStatusFilter) {
        selectedStatus = currentStatusFilter
    }

    DisposableEffect(Unit) {
        onDispose {
            orderViewModel.resetFiltersAndSearch()
        }
    }

    val totalPages = if (totalCount > 0) {
        ceil(totalCount / pageSize.toDouble()).toInt()
    } else {
        1
    }

    val statusOptions = listOf(
        "pending" to Color(0xFFFFEB3B),
        "shipping" to Color(0xFF2196F3),
        "delivered" to Color(0xFF4CAF50),
        "cancelled" to Color(0xFFE57373)
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .padding(top = 30.dp)) {

        // Header with title, filter and search toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order Management",
                style = MaterialTheme.typography.headlineSmall
            )

            Row {
                IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Orders"
                    )
                }

                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearching) "Close Search" else "Search Orders"
                    )
                }
            }
        }

        // Filter options section
        AnimatedVisibility(
            visible = showFilterOptions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Filter by status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusOptions.forEach { (status, color) ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = {
                                selectedStatus = if (selectedStatus == status) null else status
                                // Apply filter based on selection
                                if (selectedStatus == null) {
                                    orderViewModel.loadInitialOrders()
                                } else {
                                    orderViewModel.filterOrdersByStatus(status)
                                }
                            },
                            label = { Text(status.capitalize()) },
                            leadingIcon = if (selectedStatus == status) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Show active filter or clear button if filter is applied
                if (selectedStatus != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Active filter: ${selectedStatus?.capitalize()}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        TextButton(
                            onClick = {
                                selectedStatus = null
                                orderViewModel.loadInitialOrders()
                            }
                        ) {
                            Text("Clear filter")
                        }
                    }
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
                        orderViewModel.searchOrdersByUsername(it)
                        selectedStatus = null
                    } else {
                        orderViewModel.loadInitialOrders()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search by username...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            orderViewModel.loadInitialOrders()
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

        // Order count summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    searchQuery.isNotEmpty() -> "${orders.size} orders found"
                    selectedStatus != null -> "Showing ${orders.size} ${selectedStatus} orders"
                    else -> "Total orders: ${orders.size}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Empty state handling
        if (orders.isEmpty() && (searchQuery.isNotEmpty() || selectedStatus != null) && !isLoading) {
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
                        imageVector = when {
                            searchQuery.isNotEmpty() -> Icons.Default.SearchOff
                            selectedStatus != null -> Icons.Default.FilterAlt
                            else -> Icons.Default.FindInPage
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = when {
                            searchQuery.isNotEmpty() -> "No orders found for \"$searchQuery\""
                            selectedStatus != null -> "No ${selectedStatus} orders found"
                            else -> "No orders found"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedStatus = null
                        orderViewModel.loadInitialOrders()
                    }) {
                        Text("Clear Filters")
                    }
                }
            }
        } else if (isLoading && orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(orders) { order ->
                    OrderItem(order = order, onOrderClick = onOrderClick)
                }

                if (totalPages > 1 && searchQuery.isEmpty()) {
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
                                    onClick = { orderViewModel.goToPage(0) },
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
                                    onClick = { orderViewModel.previousPage() },
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
                                    onClick = { orderViewModel.goToPage(0) },
                                    enabled = !isLoading
                                )

                                // Middle pages
                                for (page in 1 until totalPages - 1) {
                                    if (page <= currentPage + 2 && page >= currentPage - 2) {
                                        PageButton(
                                            pageNumber = page + 1,
                                            isSelected = currentPage == page,
                                            onClick = { orderViewModel.goToPage(page) },
                                            enabled = !isLoading
                                        )
                                    } else if (page == currentPage - 3 || page == currentPage + 3) {
                                        Text(
                                            "...",
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                    }
                                }

                                // Last page button (if more than 1 page)
                                if (totalPages > 1) {
                                    PageButton(
                                        pageNumber = totalPages,
                                        isSelected = currentPage == totalPages - 1,
                                        onClick = { orderViewModel.goToPage(totalPages - 1) },
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
                                        onClick = { orderViewModel.loadNextPage() },
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
                                        onClick = { orderViewModel.goToPage(totalPages - 1) },
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
fun PageButton(
    pageNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            disabledContainerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            disabledContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.widthIn(min = 36.dp)
    ) {
        Text(
            text = pageNumber.toString(),
            fontSize = 14.sp
        )
    }
}

@Composable
fun OrderItem(order: Order, onOrderClick: (Order) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

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
                Text(
                    text = "Order #${order.orderId.takeLast(5)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (order.status) {
                        "delivered" -> Color(0xFF4CAF50)
                        "shipping" -> Color(0xFF2196F3)
                        "pending" -> Color(0xFFFFEB3B)
                        "cancelled" -> Color(0xFFE57373)
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Customer: ${order.username}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Total: $${order.totalPrice}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Date: ${order.createdAt?.toDate()?.toString()?.substring(0, 10) ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Items: ${order.orderDetail.size}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Phone: ${order.phone}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Address: ${order.address}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Order items preview
                if (order.orderDetail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Items:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Show up to 3 items in the preview
                        order.orderDetail.take(3).forEach { item ->
                            Text(
                                text = "• ${item.productTitle} (x${item.quantity})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (order.orderDetail.size > 3) {
                            Text(
                                text = "• ... and ${order.orderDetail.size - 3} more items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onOrderClick(order) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("View Details")
                }
            }
        }
    }
}