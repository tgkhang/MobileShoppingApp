package com.example.shopapp.ui.user.order

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopapp.R
import com.example.shopapp.data.model.OrderFirebase
import com.example.shopapp.localization.LanguageManager
import com.example.shopapp.viewmodel.OrderViewModelFirebase
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    orderViewModel: OrderViewModelFirebase
) {
    val orders by orderViewModel.orders.collectAsState()
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "G4AYyav2DUMPFEqtK7lcmfYkKk22"

    // Load orders theo UserId
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            orderViewModel.searchOrdersByUserId(currentUserId)
        }
    }

    // Lọc đơn hàng theo trạng thái
    val pendingOrders = orders.filter { it.status == "pending" }.sortedByDescending { it.createdAt?.seconds ?: 0L }
    val shippingOrders = orders.filter { it.status == "shipping" }.sortedByDescending { it.createdAt?.seconds ?: 0L }
    val deliveredOrders = orders.filter { it.status == "delivered" }.sortedByDescending { it.createdAt?.seconds ?: 0L }
    val cancelledOrders = orders.filter { it.status == "cancelled" }.sortedByDescending { it.createdAt?.seconds ?: 0L }

    // Danh sách các tab
    val tabTitles = listOf(
        LanguageManager.getString(R.string.pending),
        LanguageManager.getString(R.string.shipping),
        LanguageManager.getString(R.string.delivered),
        LanguageManager.getString(R.string.cancelled)
    )
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LanguageManager.getString(R.string.my_orders)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TabRow cho các trạng thái
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // HorizontalPager cho các trang trạng thái
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OrderList(
                        orders = pendingOrders,
                        orderViewModel = orderViewModel,
                        showCancelButton = true,
                        onViewDetails = { /* chưa làm */ }
                    )
                    1 -> OrderList(
                        orders = shippingOrders,
                        orderViewModel = orderViewModel,
                        showMarkAsReceivedButton = true,
                        onViewDetails = { /* chưa làm */ }
                    )
                    2 -> OrderList(
                        orders = deliveredOrders,
                        orderViewModel = orderViewModel,
                        onViewDetails = { /* chưa làm */ }
                    )
                    3 -> OrderList(
                        orders = cancelledOrders,
                        orderViewModel = orderViewModel,
                        onViewDetails = { /* chưa làm */ }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderList(
    orders: List<OrderFirebase>,
    orderViewModel: OrderViewModelFirebase,
    showCancelButton: Boolean = false,
    showMarkAsReceivedButton: Boolean = false,
    onViewDetails: (OrderFirebase) -> Unit
) {
    val context = LocalContext.current

    // Lấy các chuỗi cần thiết trong @Composable context
    val noOrdersMessage = LanguageManager.getString(R.string.you_have_no_orders_yet)
    val orderCancelledSuccessfully = LanguageManager.getString(R.string.order_cancelled_successfully)
    val cannotCancelOrder = LanguageManager.getString(R.string.cannot_cancel_order)
    val orderMarkedAsReceived = LanguageManager.getString(R.string.order_marked_as_received)
    val cannotMarkAsReceived = LanguageManager.getString(R.string.cannot_mark_as_received)

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = noOrdersMessage,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                OrderCard(
                    order = order,
                    onCancelOrder = {
                        if (order.status == "pending") {
                            orderViewModel.updateOrder(
                                order.copy(
                                    status = "cancelled",
                                    updatedAt = Timestamp.now()
                                )
                            )
                            Toast.makeText(context, orderCancelledSuccessfully, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, cannotCancelOrder, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onMarkAsReceived = {
                        if (order.status == "shipping") {
                            orderViewModel.updateOrder(
                                order.copy(
                                    status = "delivered",
                                    updatedAt = Timestamp.now()
                                )
                            )
                            Toast.makeText(context, orderMarkedAsReceived, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, cannotMarkAsReceived, Toast.LENGTH_SHORT).show()
                        }
                    },
                    showCancelButton = showCancelButton,
                    showMarkAsReceivedButton = showMarkAsReceivedButton,
                    onViewDetails = { onViewDetails(order) }
                )
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderFirebase,
    onCancelOrder: () -> Unit,
    onMarkAsReceived: () -> Unit,
    onViewDetails: () -> Unit,
    showCancelButton: Boolean = false,
    showMarkAsReceivedButton: Boolean = false
) {
    // State cho dialog xác nhận
    var showCancelDialog by remember { mutableStateOf(false) }
    var showReceivedDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${LanguageManager.getString(R.string.order_prefix)}${order.orderId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, // Giới hạn 1 dòng để tránh xuống hàng
                    overflow = TextOverflow.Ellipsis // Thêm dấu ... nếu quá dài
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = when (order.status) {
                            "pending" -> Icons.Default.HourglassEmpty
                            "shipping" -> Icons.Default.LocalShipping
                            "delivered" -> Icons.Default.Verified
                            "cancelled" -> Icons.Default.Verified
                            else -> Icons.Default.HourglassEmpty
                        },
                        contentDescription = null,
                        tint = when (order.status) {
                            "pending" -> Color(0xFFFFA500)
                            "shipping" -> Color(0xFF1E90FF)
                            "delivered" -> Color(0xFF32CD32)
                            "cancelled" -> Color.Red
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (order.status) {
                            "pending" -> LanguageManager.getString(R.string.pending)
                            "shipping" -> LanguageManager.getString(R.string.shipping)
                            "delivered" -> LanguageManager.getString(R.string.delivered)
                            "cancelled" -> LanguageManager.getString(R.string.cancelled)
                            else -> LanguageManager.getString(R.string.pending)
                        }.uppercase(),
                        color = when (order.status) {
                            "pending" -> Color(0xFFFFA500)
                            "shipping" -> Color(0xFF1E90FF)
                            "delivered" -> Color(0xFF32CD32)
                            "cancelled" -> Color.Red
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order details
            Text(
                text = "${LanguageManager.getString(R.string.placed_on)} ${
                    order.createdAt?.toDate()?.let {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "N/A"
                }",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${LanguageManager.getString(R.string.total)} \$${String.format("%.2f", order.totalPrice)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Order items
            order.orderDetail.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = item.productImage,
                        contentDescription = item.productTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(end = 12.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f) // Chiếm không gian còn lại, tránh ép xuống hàng
                            .padding(end = 8.dp) // Thêm padding để tránh sát lề
                    ) {
                        Text(
                            text = item.productTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1, // Giới hạn 1 dòng
                            overflow = TextOverflow.Ellipsis, // Thêm dấu ... nếu quá dài
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${LanguageManager.getString(R.string.quantity_prefix)} ${item.quantity} x \$${String.format("%.2f", item.price)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "\$${String.format("%.2f", item.price * item.quantity)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Thêm divider giữa các sản phẩm, trừ sản phẩm cuối
                if (index < order.orderDetail.size - 1) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery information section
            Text(
                text = LanguageManager.getString(R.string.delivery_information),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = order.address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, // Giới hạn 1 dòng
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = order.phone,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onViewDetails() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        /*
                        Text(
                            text = "View Details",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        */
                    }
                }

                // Nút Cancel và Mark as Received
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCancelButton && order.status == "pending") {
                        Button(
                            onClick = { showCancelDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = LanguageManager.getString(R.string.cancel_order),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (showMarkAsReceivedButton && order.status == "shipping") {
                        Button(
                            onClick = { showReceivedDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = LanguageManager.getString(R.string.mark_as_received),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Dialog xác nhận hủy đơn hàng
            if (showCancelDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text(LanguageManager.getString(R.string.cancel_order)) },
                    text = { Text(LanguageManager.getString(R.string.are_you_sure_cancel_order)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onCancelOrder()
                                showCancelDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(LanguageManager.getString(R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text(LanguageManager.getString(R.string.no))
                        }
                    }
                )
            }

            // Dialog xác nhận nhận hàng
            if (showReceivedDialog) {
                AlertDialog(
                    onDismissRequest = { showReceivedDialog = false },
                    title = { Text(LanguageManager.getString(R.string.mark_as_received)) },
                    text = { Text(LanguageManager.getString(R.string.are_you_sure_received_order)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onMarkAsReceived()
                                showReceivedDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(LanguageManager.getString(R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReceivedDialog = false }) {
                            Text(LanguageManager.getString(R.string.no))
                        }
                    }
                )
            }
        }
    }
}