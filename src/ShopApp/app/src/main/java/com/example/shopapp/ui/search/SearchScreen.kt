package com.example.shopapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopapp.navigation.Screen
import com.example.shopapp.ui.components.ProductCard
import com.example.shopapp.viewmodel.ProductViewModel
import com.example.shopapp.viewmodel.SearchViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SearchScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    searchViewModel: SearchViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Filter UI state
    var showFilters by remember { mutableStateOf(false) }

    // Get state from ViewModels
    val products by productViewModel.products.collectAsState(initial = emptyList())
    val searchResults by searchViewModel.searchResults.collectAsState(initial = emptyList())
    val filteredResults by searchViewModel.filteredResults.collectAsState(initial = emptyList())
    val isSearching by searchViewModel.isSearching.collectAsState(initial = false)
    val categories by searchViewModel.categories.collectAsState(initial = emptyList())
    val selectedCategory by searchViewModel.selectedCategory.collectAsState(initial = null)
    val priceRange by searchViewModel.priceRange.collectAsState(initial = 0f..10000f)
    val minRating by searchViewModel.minRating.collectAsState(initial = 0)

    // Create a debounced search flow
    val searchFlow = remember { MutableStateFlow("") }

    LaunchedEffect(Unit) {
        // Request focus on search field when entering screen
        focusRequester.requestFocus()

        // Setup debounced search
        searchFlow
            .debounce(300) // Wait for 300ms of inactivity before searching
            .filter { it.length >= 2 } // Only search for queries with at least 2 characters
            .distinctUntilChanged()
            .collect { query ->
                if (query.isNotEmpty()) {
                    searchViewModel.searchProducts(query)
                }
            }
    }

    // Update search flow when query changes
    LaunchedEffect(searchQuery) {
        searchFlow.value = searchQuery
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                            },
                            placeholder = { Text("Search Product..") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    coroutineScope.launch {
                                        searchFlow.value = searchQuery
                                    }
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter"
                            )
                        }
                    }
                )

                // Filter section
                FilterSection(
                    visible = showFilters,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    priceRange = priceRange,
                    minRating = minRating,
                    onCategorySelected = { searchViewModel.setCategory(it) },
                    onPriceRangeChanged = { searchViewModel.setPriceRange(it) },
                    onMinRatingChanged = { searchViewModel.setMinRating(it) },
                    onClearFilters = { searchViewModel.clearFilters() },
                    onApplyFilters = { showFilters = false }
                )
            }

        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isSearching) {
                // Loading state
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                // No results found
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No products found",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try a different search term",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            } else if (filteredResults.isEmpty() && searchResults.isNotEmpty()) {
                // Results exist but were filtered out
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No matching products",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try adjusting your filters",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            } else {
                // Display search results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Show applied filters
                    item {
                        AppliedFiltersRow(
                            selectedCategory = selectedCategory,
                            priceRange = priceRange,
                            minRating = minRating,
                            defaultPriceRange = 0f..10000f,
                            onClearCategory = { searchViewModel.setCategory(null) },
                            onClearPriceRange = { searchViewModel.setPriceRange(0f..10000f) },
                            onClearRating = { searchViewModel.setMinRating(0) }
                        )
                    }

                    // Show search result count
                    item {
                        SearchResultCount(count = filteredResults.size)
                    }

                    // Products grid
                    items(filteredResults.chunked(2)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { product ->
                                ProductCard(
                                    product = product,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(Screen.ProductDetail.createRoute(product.productId))
                                    }
                                )
                            }

                            // Handle odd number of items in a row
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}