package com.example.shopapp.ui.admin.users

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopapp.data.model.User
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.UserViewModel
import kotlin.math.ceil

@Composable
fun UserManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel?,
    rootNavController: NavController?,
    userViewModel: UserViewModel,
    onUserClick: (User) -> Unit
) {
    val users by userViewModel.users.collectAsState()
    val currentPage by userViewModel.currentPage.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val hasMoreData by userViewModel.hasMoreData.collectAsState()
    val totalCount by userViewModel.totalCount.collectAsState()
    val pageSize by userViewModel.pageSize.collectAsState()
    val selectedStatus by userViewModel.selectedStatus.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }

    val totalPages = if (totalCount > 0) {
        ceil(totalCount / pageSize.toDouble()).toInt()
    } else {
        1
    }

    // Status options
    val statuses = listOf("active", "banned")

    DisposableEffect(Unit) {
        onDispose {
            userViewModel.resetFiltersAndSearch()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Users",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter button
                IconButton(
                    onClick = {
                        if (!showFilterOptions) {
                            isSearching = false
                            searchQuery = ""
                        } else {
                            userViewModel.resetFiltersAndSearch()
                        }
                        showFilterOptions = !showFilterOptions
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Users",
                        tint = if (isLoading)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            LocalContentColor.current
                    )
                }

                // Search button
                IconButton(
                    onClick = {
                        if (!isSearching) {
                            showFilterOptions = false
                        } else {
                            searchQuery = ""
                            userViewModel.resetFiltersAndSearch()
                        }
                        isSearching = !isSearching
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearching) "Close Search" else "Search Users",
                        tint = if (isLoading)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            LocalContentColor.current
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
                    text = "Filter by status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Status horizontal scroll
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statuses.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = {
                                userViewModel.filterByStatus(if (selectedStatus == status) null else status)
                            },
                            enabled = !isLoading,
                            label = {
                                Text(status.replaceFirstChar { it.uppercase() })
                            },
                            leadingIcon = if (selectedStatus == status) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                // Clear filter button
                if (selectedStatus != null) {
                    TextButton(
                        onClick = { userViewModel.filterByStatus(null) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear Filter")
                    }
                }
            }
        }

        // Search bar
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
                        userViewModel.searchUsers(it)
                    } else {
                        userViewModel.loadInitialUsers()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search by username or email") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            userViewModel.loadInitialUsers()
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
                text = "${users.size} users found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (users.isEmpty() && (searchQuery.isNotEmpty() || selectedStatus != null) && !isLoading) {
            // Empty search/filter results
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
                        imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.FilterAlt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "No users found for \"$searchQuery\""
                        else
                            "No users found with status \"${selectedStatus?.replaceFirstChar { it.uppercase() }}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        searchQuery = ""
                        userViewModel.filterByStatus(null)
                    }) {
                        Text("Clear All Filters")
                    }
                }
            }
        } else if (isLoading && users.isEmpty()) {
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
                items(users) { user ->
                    UserItem(user = user, onUserClick = onUserClick)
                }

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
                                onClick = { userViewModel.goToPage(0) },
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
                                onClick = { userViewModel.previousPage() },
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PageButton(
                                pageNumber = 1,
                                isSelected = currentPage == 0,
                                onClick = { userViewModel.goToPage(0) },
                                enabled = !isLoading
                            )

                            for (page in 1 until totalPages - 1) {
                                if (page <= currentPage + 2 && page >= currentPage - 2) {
                                    PageButton(
                                        pageNumber = page + 1,
                                        isSelected = currentPage == page,
                                        onClick = { userViewModel.goToPage(page) },
                                        enabled = !isLoading
                                    )
                                } else if (page == currentPage - 3 || page == currentPage + 3) {
                                    // Show ellipsis
                                    Text(
                                        text = "...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }

                            if (totalPages > 1) {
                                PageButton(
                                    pageNumber = totalPages,
                                    isSelected = currentPage == totalPages - 1,
                                    onClick = { userViewModel.goToPage(totalPages - 1) },
                                    enabled = !isLoading
                                )
                            }
                        }

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
                                    onClick = { userViewModel.loadNextPage() },
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
                                    onClick = { userViewModel.goToPage(totalPages - 1) },
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
fun UserItem(user: User, onUserClick: (User) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (user.status) {
                        "active" -> Color(0xFF4CAF50)
                        "banned" -> Color(0xFFF44336)
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = user.status,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Role: ${user.role}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (user.phone.isNotEmpty()) {
                    Text(
                        text = "Phone: ${user.phone}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (user.address.isNotEmpty()) {
                    Text(
                        text = "Address: ${user.address}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onUserClick(user) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("View Details")
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