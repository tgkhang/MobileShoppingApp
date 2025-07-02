package com.example.shopapp.ui.user

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopapp.R
import com.example.shopapp.localization.LanguageManager
import com.example.shopapp.navigation.Screen
import com.example.shopapp.ui.components.ProductCard
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.ProductViewModel
import kotlinx.coroutines.delay
import com.example.shopapp.viewmodel.NotificationViewModel
import com.example.shopapp.data.model.NotificationItem
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headset
import kotlinx.coroutines.delay
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    rootNavController: NavController,
    productViewModel: ProductViewModel,
    notificationViewModel: NotificationViewModel
) {
    val products by productViewModel.products.collectAsState()
    var isMenuOpen by remember { mutableStateOf(false) }
    val isLoading by productViewModel.isLoading.collectAsState()
    val hasMoreData by productViewModel.hasMoreData.collectAsState()
    val selectedCategory by productViewModel.selectedCategory.collectAsState()
    val context = LocalContext.current
    val permissionAlreadyRequested = remember { mutableStateOf(false) }

    // Extract the value from the State object for safe smart casting
    val categoryValue = selectedCategory

    var showEmptyState by remember { mutableStateOf(false) }
    LaunchedEffect(products, isLoading) {
        if (products.isEmpty() && !isLoading) {
            delay(700)
            showEmptyState = true
        } else {
            showEmptyState = false
        }
    }

    // Categories for electronics store
    val categories = listOf(
        LanguageManager.getString(R.string.category_audio) to Icons.Default.Headset,
        LanguageManager.getString(R.string.category_gaming) to Icons.Default.Games,
        LanguageManager.getString(R.string.category_mobile) to Icons.Default.Smartphone,
        LanguageManager.getString(R.string.category_tv) to Icons.Default.Tv,
        LanguageManager.getString(R.string.category_laptop) to Icons.Default.Computer,
        LanguageManager.getString(R.string.category_tablet) to Icons.Default.TabletMac,
        LanguageManager.getString(R.string.category_headphone) to Icons.Default.Headphones,
        LanguageManager.getString(R.string.category_accessories) to Icons.Default.Cable
    )

    // For pagination
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()

            // Close to the end of the list and not currently loading
            lastVisibleItem != null &&
                    !isLoading &&
                    hasMoreData &&
                    lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    // Monitor for pagination using derivedStateOf
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            Log.d("Pagination", "Loading next page, current page: ${productViewModel.currentPage.value}")
            productViewModel.loadNextPage()
        }
    }

    var showNotifications by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Update permission state
        if (isGranted) {
            authViewModel.checkNotificationPermission(context)
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionAlreadyRequested.value) {
            permissionAlreadyRequested.value = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!authViewModel.checkNotificationPermission(context)) {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        LanguageManager.getString(R.string.app_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showNotifications = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
//                    IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
//                        Icon(Icons.Default.Menu, contentDescription = "Menu")
//                    }
                }
            )
        },
        content = { padding ->
            Box {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Search bar
                    item {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text(LanguageManager.getString(R.string.search_product_placeholder)) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            enabled = false, // Disable direct input
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .clickable {
                                    navController.navigate(Screen.Search.route)
                                }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    // Sale or advertisement section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            LanguageManager.getString(R.string.featured_deals),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Sale or advertisement
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(modifier = Modifier.width(220.dp)) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Image(
                                        painter = painterResource(id = R.drawable.sale1),
                                        contentDescription = "Accessories Sale",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Card(modifier = Modifier.width(220.dp)) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.sale2),
                                        contentDescription = "New Arrivals",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Card(modifier = Modifier.width(220.dp)) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.sale3),
                                        contentDescription = "Holiday Specials",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // Categories section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Categories section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                LanguageManager.getString(R.string.categories),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                            TextButton(onClick = {
                                if (categoryValue != null) {
                                    productViewModel.filterByCategory(null)
                                }
                            }) {
                                Text(
                                    LanguageManager.getString(R.string.all),
                                    color = if (categoryValue == null) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            categories.forEach { (category, icon) ->
                                Card(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable {
                                            // Apply category filter when card is clicked
                                            productViewModel.filterByCategory(
                                                if (categoryValue == category) null else category
                                            )
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (categoryValue == category)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = category,
                                            tint = if (categoryValue == category)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = categoryValue?.replaceFirstChar { it.uppercase() }
                                    ?: LanguageManager.getString(R.string.popular_products),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Products grid
                    items(products.chunked(2)) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { product ->
                                ProductCard(
                                    product = product,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(
                                            Screen.ProductDetail.createRoute(
                                                product.productId
                                            )
                                        )
                                    }
                                )
                            }

                            // Handle odd number of items in a row
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Loading indicator
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // End of list indicator
                    if (!hasMoreData && products.isNotEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LanguageManager.getString(R.string.no_more_products),
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Empty state when no products are available
                if (showEmptyState) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.DevicesOther,
                                contentDescription = "No Products",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = if (categoryValue != null)
                                    LanguageManager.getString(R.string.no_products_found_in_category, categoryValue)
                                else
                                    LanguageManager.getString(R.string.no_products_available),
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Button(onClick = { productViewModel.refreshProducts() }) {
                                Text(LanguageManager.getString(R.string.refresh))
                            }
                        }
                    }
                }

                // Logout Menu Temp
                if (isMenuOpen) {
                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(LanguageManager.getString(R.string.profile)) },
                            onClick = { /* TODO */ }
                        )
                        DropdownMenuItem(
                            text = { Text(LanguageManager.getString(R.string.settings)) },
                            onClick = { /* TODO */ }
                        )
                        DropdownMenuItem(
                            text = { Text(LanguageManager.getString(R.string.log_out)) },
                            onClick = {
                                authViewModel.signOut(rootNavController)
                                isMenuOpen = false
                            }
                        )
                    }
                }
            }
        }
    )

    if (showNotifications) {
        NotificationDialog(
            onDismissRequest = { showNotifications = false },
            notificationViewModel = notificationViewModel,
            currentUserId = authViewModel.getCurrentUser()?.uid ?: "",
        )
    }
}

@Composable
fun NotificationDialog(
    onDismissRequest: () -> Unit,
    notificationViewModel: NotificationViewModel,
    currentUserId: String
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()

    // Load notifications when dialog opens
    LaunchedEffect(key1 = currentUserId) {
        notificationViewModel.loadNotifications(currentUserId)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Notifications") },
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    notifications.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No notifications")
                        }
                    }
                    else -> {
                        LazyColumn {
                            items(notifications) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onMarkAsRead = {
                                        notificationViewModel.markAsRead(notification)
                                    },
                                    onDelete = {
                                        notificationViewModel.deleteNotification(notification)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NotificationItem(
    notification: NotificationItem,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (notification.isRead)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onMarkAsRead),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.getFormattedDate().toLocaleString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete notification",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}