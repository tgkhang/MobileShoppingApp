package com.example.shopapp.ui.admin.users

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shopapp.viewmodel.UserViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import com.example.shopapp.viewmodel.OrderViewModel
import com.example.shopapp.data.model.Order
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel
) {
    val context = LocalContext.current
    val user = userViewModel.selectedUser
    if (user == null){
        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
        return
    }
    var currentStatus by remember { mutableStateOf(user.status) }

    val orders = orderViewModel.orders.collectAsState(initial = emptyList())
    var showOrderDialog by remember { mutableStateOf(false) }
    LaunchedEffect(showOrderDialog) {
        if (showOrderDialog) {
            orderViewModel.searchOrdersByUserId(user.userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = user.username,
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
                    Button(
                        onClick = { showOrderDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Order History",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Order History")
                    }
                    Button(
                        onClick = {
                            val newStatus = if (currentStatus == "active") "banned" else "active"
                            val updatedUser = user.copy(status = newStatus)
                            userViewModel.updateUser(updatedUser)
                            currentStatus = newStatus
                            Toast.makeText(
                                context,
                                "User ${if (newStatus == "active") "activated" else "banned"}!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = (user.role != "admin"),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStatus == "active")
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(if (currentStatus == "active") "Ban User" else "Activate User")
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
        ) {
            // User Status Card
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
                    Text(
                        text = "Account Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (currentStatus == "active") Color(0xFF4CAF50) else Color(0xFFE57373)
                    ) {
                        Text(
                            text = currentStatus.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White
                        )
                    }
                }
            }

            // User Information Card
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
                        text = "User Information",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow("Username", user.username)
                    DetailRow("Email", user.email)
                    DetailRow("Role", user.role)

                    if (user.phone.isNotEmpty()) {
                        DetailRow("Phone", user.phone)
                    }

                    if (user.address.isNotEmpty()) {
                        DetailRow("Address", user.address)
                    }
                }
            }

            // Account Dates Card
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
                        text = "Account Timeline",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow(
                        "Created",
                        user.createdAt?.toDate()?.toString() ?: "Unknown"
                    )
                    DetailRow(
                        "Last Updated",
                        user.updatedAt?.toDate()?.toString() ?: "Unknown"
                    )
                }
            }
        }
    }

    // Order History Dialog
    if (showOrderDialog) {
        OrderHistoryDialog(
            orders = orders.value,
            onDismiss = { showOrderDialog = false },
            onOrderClick = { order ->
                orderViewModel.selectOrder(order)
                navController.navigate("order_detail")
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun OrderHistoryDialog(
    orders: List<Order>,
    onDismiss: () -> Unit,
    onOrderClick: (Order) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Order History",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No orders found for this user",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orders) { order ->
                            OrderHistoryItem(
                                order = order,
                                onClick = {
                                    onOrderClick(order)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun OrderHistoryItem(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order #${order.orderId.takeLast(5)}",
                    style = MaterialTheme.typography.titleSmall,
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
                    }
                ) {
                    Text(
                        text = order.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Items: ${order.orderDetail.size}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${order.totalPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Date: ${order.createdAt?.toDate()?.toString()?.substring(0, 10) ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}