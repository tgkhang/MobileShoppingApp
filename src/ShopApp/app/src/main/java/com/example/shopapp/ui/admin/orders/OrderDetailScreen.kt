package com.example.shopapp.ui.admin.orders

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.shopapp.viewmodel.OrderViewModel
import com.example.shopapp.viewmodel.ProductViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shopapp.data.model.CartItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel
) {
    val context = LocalContext.current
    val order = orderViewModel.selectedOrder
    if (order == null){
        Toast.makeText(context, "Order not found", Toast.LENGTH_SHORT).show()
        return
    }
    var currentStatus by remember { mutableStateOf(order.status) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order #${order.orderId.takeLast(10)}",
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
                        onClick = {
                            val nextStatus = when (currentStatus) {
                                "pending" -> "shipping"
                                else -> currentStatus
                            }
                            if (nextStatus != currentStatus) {
                                orderViewModel.updateOrderStatus(
                                    order,
                                    nextStatus,
                                    productViewModel.getRepositoryForOrderOperations()
                                )
                                currentStatus = nextStatus
                                Toast.makeText(
                                    context,
                                    "Order status updated to $nextStatus",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = currentStatus == "pending",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            when (currentStatus) {
                                "pending" -> "Mark as shipping"
                                "shipping" -> "Wait for delivering"
                                else -> "Update Status"
                            }
                        )
                    }
                    Button(
                        onClick = {
                            val newStatus = "cancelled"
                            val updatedOrder = order.copy(status = newStatus)
                            orderViewModel.updateOrder(updatedOrder)
                            currentStatus = newStatus
                            Toast.makeText(
                                context,
                                "Order cancelled",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = currentStatus == "pending",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel Order")
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
            // Order Status Card
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
                        text = "Order Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (currentStatus) {
                            "delivered" -> Color(0xFF4CAF50)
                            "shipping" -> Color(0xFF2196F3)
                            "pending" -> Color(0xFFFFEB3B)
                            "cancelled" -> Color(0xFFE57373)
                            else -> Color.Gray
                        }
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

            // Customer Information Card
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
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow("Customer", order.username)
                    DetailRow("User ID", order.userId)
                    DetailRow("Phone", order.phone)
                    DetailRow("Address", order.address)
                }
            }

            // Order Summary Card
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
                        text = "Order Summary",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    DetailRow("Order ID", order.orderId)
                    DetailRow(
                        "Order Date",
                        order.createdAt?.toDate()?.toString() ?: "Unknown"
                    )
                    if (order.updatedAt != null) {
                        DetailRow(
                            "Last Updated",
                            order.updatedAt.toDate().toString()
                        )
                    }
                    DetailRow("Total Items", order.orderDetail.size.toString())
                    DetailRow("Total Amount", "$${order.totalPrice}")
                }
            }

            // Order Items Card
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
                        text = "Order Items",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Divider()

                    order.orderDetail.forEachIndexed { index, item ->
                        OrderItemRow(item, index)
                        if (index < order.orderDetail.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
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
private fun OrderItemRow(item: CartItem, index: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${index + 1}. ${item.productTitle}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${item.price}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Quantity: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Subtotal: $${item.price * item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}