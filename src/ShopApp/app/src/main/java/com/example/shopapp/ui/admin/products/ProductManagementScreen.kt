package com.example.shopapp.ui.admin.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.shopapp.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import com.example.shopapp.viewmodel.ProductViewModel
import com.example.shopapp.data.model.Product
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import com.example.shopapp.R
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import kotlin.math.ceil
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ProductManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel?,
    rootNavController: NavController?,
    productViewModel: ProductViewModel,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.products.collectAsState()
    val currentPage by productViewModel.currentPage.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val hasMoreData by productViewModel.hasMoreData.collectAsState()
    val totalCount by productViewModel.totalCount.collectAsState()
    val pageSize by productViewModel.pageSize.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf("") }
    var showFilterOptions by remember { mutableStateOf(false) }

    val totalPages = if (totalCount > 0) {
        ceil(totalCount / pageSize.toDouble()).toInt()
    } else {
        1
    }

    // Category options
    val categories = mapOf(
        "audio" to Icons.Default.Headset,
        "gaming" to Icons.Default.Games,
        "mobile" to Icons.Default.Smartphone,
        "tv" to Icons.Default.Tv,
        "laptop" to Icons.Default.Computer,
        "tablet" to Icons.Default.TabletMac,
        "headphone" to Icons.Default.Headphones,
        "accessories" to Icons.Default.Cable
    )

    // Function to apply category filter
    fun applyFilter() {
        if (filterCategory.isNotEmpty()) {
            productViewModel.filterByCategory(filterCategory)
        } else {
            productViewModel.loadInitialProducts()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            productViewModel.resetFiltersAndSearch()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .padding(top = 30.dp))
    {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Products",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter button
                IconButton(
                    onClick = { showFilterOptions = !showFilterOptions },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Products",
                        tint = if (isLoading)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            LocalContentColor.current
                    )
                }

                // Search button
                IconButton(
                    onClick = { isSearching = !isSearching },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearching) "Close Search" else "Search Products",
                        tint = if (isLoading)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            LocalContentColor.current
                    )
                }

                // Add product button
                IconButton(
                    onClick = { navController.navigate("add_product") },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Product",
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
                    text = "Filter by category",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Categories horizontal scroll
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (categoryName, icon) ->
                        FilterChip(
                            selected = filterCategory == categoryName,
                            onClick = {
                                filterCategory = if (filterCategory == categoryName) "" else categoryName
                                applyFilter()
                            },
                            enabled = !isLoading,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(categoryName.capitalize())
                                }
                            },
                            leadingIcon = if (filterCategory == categoryName) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                // Clear filter button
                if (filterCategory.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            filterCategory = ""
                            productViewModel.loadInitialProducts()
                        },
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
                        productViewModel.searchProductsByTitle(it)
                    } else {
                        productViewModel.loadInitialProducts()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search by product title...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            productViewModel.loadInitialProducts()
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
                text = "${products.size} products found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (products.isEmpty() && (searchQuery.isNotEmpty() || filterCategory.isNotEmpty()) && !isLoading) {
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
                            "No products found for \"$searchQuery\""
                        else
                            "No products found in category \"${filterCategory.capitalize()}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        searchQuery = ""
                        filterCategory = ""
                        productViewModel.loadInitialProducts()
                    }) {
                        Text("Clear All Filters")
                    }
                }
            }
        } else if (isLoading && products.isEmpty()) {
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
                items(products) { product ->
                    ProductItem(product = product, onDetailClick = onProductClick)
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
                                onClick = { productViewModel.goToPage(0) },
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
                                onClick = { productViewModel.previousPage() },
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
                                onClick = { productViewModel.goToPage(0) },
                                enabled = !isLoading
                            )

                            for (page in 1 until totalPages - 1) {
                                if (page <= currentPage + 2 && page >= currentPage - 2) {
                                    PageButton(
                                        pageNumber = page + 1,
                                        isSelected = currentPage == page,
                                        onClick = { productViewModel.goToPage(page) },
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
                                    onClick = { productViewModel.goToPage(totalPages - 1) },
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
                                    onClick = { productViewModel.nextPage() },
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
                                    onClick = { productViewModel.goToPage(totalPages - 1) },
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
fun ProductItem(
    product: Product,
    onDetailClick: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.image)
                    .crossfade(true)
                    .error(R.drawable.ic_launcher_foreground)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .build(),
                contentDescription = "Product Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            var isExpanded by remember { mutableStateOf(false) }

            Column(modifier = Modifier
                .weight(1f)
                .clickable { isExpanded = !isExpanded }
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category chip
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val categoryIcon = when(product.category) {
                                "audio" -> Icons.Default.Headset
                                "gaming" -> Icons.Default.Games
                                "mobile" -> Icons.Default.Smartphone
                                "tv" -> Icons.Default.Tv
                                "laptop" -> Icons.Default.Computer
                                "tablet" -> Icons.Default.TabletMac
                                "headphone" -> Icons.Default.Headphones
                                "accessories" -> Icons.Default.Cable
                                else -> Icons.Default.Inventory
                            }

                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Text(
                                text = product.category.capitalize(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Price
                    Text(
                        text = "$${product.price}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Stock indicator
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (product.stock > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    ) {
                        Text(
                            text = if (product.stock > 0) "In Stock" else "Out of Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onDetailClick(product) },
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

@Preview
@Composable
fun pre(){
    //ProductManagementScreen(rememberNavController(), null, null, fakeRepository)
}