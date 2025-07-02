package com.example.shopapp.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.shopapp.viewmodel.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import com.example.shopapp.data.model.Product
import com.example.shopapp.data.model.User
import com.example.shopapp.navigation.Screen
import com.example.shopapp.viewmodel.OrderViewModel
import com.example.shopapp.viewmodel.ProductViewModel
import com.example.shopapp.viewmodel.UserViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel?,
    rootNavController: NavController?,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel
) {
    val orders by orderViewModel.orders.collectAsState()
    val totalCount by orderViewModel.totalCount.collectAsState()
    var bestSellerProduct by remember { mutableStateOf<Product?>(null) }
    var bigSpender by remember { mutableStateOf<User?>(null) }

    val deliveredOrders by remember {
        orderViewModel.loadAllDeliveredOrders()
        orderViewModel.deliveredOrders
    }.collectAsState()

    val pendingOrders by remember {
        orderViewModel.loadAllPendingOrders()
        orderViewModel.pendingOrders
    }.collectAsState()

    // Calculate income reports
    val totalIncome = deliveredOrders.sumOf { it.totalPrice }

    // Get current time to calculate periods
    val currentTimeMillis = System.currentTimeMillis()
    val dayInMillis = 24 * 60 * 60 * 1000

    // Daily income (last 24 hours)
    val dailyIncome = deliveredOrders
        .filter { order ->
            val orderTime = order.updatedAt?.toDate()?.time ?: 0L
            (currentTimeMillis - orderTime) / dayInMillis <= 1
        }
        .sumOf { it.totalPrice }

    // Weekly income (last 7 days)
    val weeklyIncome = deliveredOrders
        .filter { order ->
            val orderTime = order.updatedAt?.toDate()?.time ?: 0L
            (currentTimeMillis - orderTime) / dayInMillis <= 7
        }
        .sumOf { it.totalPrice }

    // Monthly income (last 30 days)
    val monthlyIncome = deliveredOrders
        .filter { order ->
            val orderTime = order.updatedAt?.toDate()?.time ?: 0L
            (currentTimeMillis - orderTime) / dayInMillis <= 30
        }
        .sumOf { it.totalPrice }

    // Find best seller product
    val productSales = mutableMapOf<String, Int>()
    deliveredOrders.forEach { order ->
        order.orderDetail.forEach { item ->
            productSales[item.productId] = (productSales[item.productId] ?: 0) + item.quantity
        }
    }

    val bestSellerId = productSales.entries.maxByOrNull { it.value }?.key ?: ""
    LaunchedEffect(bestSellerId) {
        bestSellerProduct = productViewModel.getProductById(bestSellerId)
    }

    // Find big spender
    val userSpending = mutableMapOf<String, Double>()
    deliveredOrders.forEach { order ->
        userSpending[order.userId] = (userSpending[order.userId] ?: 0.0) + order.totalPrice
    }

    val bigSpenderId = userSpending.entries.maxByOrNull { it.value }?.key ?: ""
    LaunchedEffect(bigSpenderId) {
        bigSpender = userViewModel.getUserById(bigSpenderId)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 30.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Admin Dashboard",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )

                    IconButton(
                        onClick = {
                            if (authViewModel != null && rootNavController != null) {
                                authViewModel.signOut(rootNavController)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        bottomBar = {
            AdminBottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Income Reports Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Income Reports",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IncomeReportItem(
                            title = "Daily",
                            amount = dailyIncome,
                            icon = Icons.Default.Today,
                            modifier = Modifier.weight(1f)
                        )

                        IncomeReportItem(
                            title = "Weekly",
                            amount = weeklyIncome,
                            icon = Icons.Default.DateRange,
                            modifier = Modifier.weight(1f)
                        )

                        IncomeReportItem(
                            title = "Monthly",
                            amount = monthlyIncome,
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = "Total Revenue",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = formatCurrency(totalIncome),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Order Statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Order Statistics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            title = "Total",
                            value = totalCount.toString(),
                            icon = Icons.Default.ShoppingCart,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        StatItem(
                            title = "Delivered",
                            value = deliveredOrders.size.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color.Green,
                            modifier = Modifier.weight(1f)
                        )

                        StatItem(
                            title = "Pending",
                            value = pendingOrders.size.toString(),
                            icon = Icons.Default.Pending,
                            color = Color.Yellow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Best Seller Product
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Best Seller Product",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (bestSellerProduct != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = CenterVertically
                        ) {
                            bestSellerProduct?.let { product ->
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.title,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${productSales[bestSellerId] ?: 0} units sold",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = formatCurrency(product.price),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = {
                                        productViewModel.selectProduct(product)
                                        navController.navigate("product_detail")
                                    }
                                ) {
                                    Text("View Details")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No sales data available",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Big Spender
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Top Customer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (bigSpender != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = CenterVertically
                        ) {
                            bigSpender?.let { user ->
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.username,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = user.email,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "Total spent: ${formatCurrency(userSpending[bigSpenderId] ?: 0.0)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = {
                                        userViewModel.selectUser(user)
                                        navController.navigate("user_detail")
                                    }
                                ) {
                                    Text("View Profile")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No customer data available",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeReportItem(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = formatCurrency(amount),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )

        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Utility function to format currency
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

@Composable
fun AdminBottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Products", Screen.ProductManagement.route, Icons.Filled.ShoppingCart),
        NavigationItem("Orders", Screen.OrderManagement.route, Icons.Filled.Menu),
        NavigationItem("Events", Screen.EventManagement.route, Icons.Filled.Favorite),
        NavigationItem("Users", Screen.UserManagement.route, Icons.Filled.Person)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false, // Update with actual selected state if needed
                onClick = {
                    navController.navigate(item.route)
                }
            )
        }
    }
}

data class NavigationItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
