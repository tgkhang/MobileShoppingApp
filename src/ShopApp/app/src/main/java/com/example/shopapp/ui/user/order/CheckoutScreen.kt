package com.example.shopapp.ui.user.order

import android.app.Activity
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopapp.data.model.CartItemFirebase
import com.example.shopapp.data.model.OrderFirebase
import com.example.shopapp.data.repository.AuthRepository
import com.example.shopapp.data.repository.PaymentRepository
import com.example.shopapp.navigation.Screen
import com.example.shopapp.viewmodel.CartViewModel
import com.example.shopapp.viewmodel.OrderViewModelFirebase
import com.example.shopapp.viewmodel.PaymentStatus
import com.example.shopapp.viewmodel.PaymentViewModel
import com.example.shopapp.viewmodel.PaymentViewModelFactory
import com.example.shopapp.viewmodel.PendingOrder
import com.example.shopapp.zalopay.ZaloPayManager
import com.example.shopapp.zalopay.api.CreateOrder
import com.facebook.CallbackManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import vn.zalopay.sdk.ZaloPayError
//import vn.zalopay.sdk.ZaloPaySDK
//import vn.zalopay.sdk.listeners.PayOrderListener

enum class PaymentMethod {
    PAYPAL,
    COD,
    ZALOPAY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModelFirebase,
    paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            paymentRepository = PaymentRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: throw IllegalStateException("Context must be an Activity")
    val coroutineScope = rememberCoroutineScope()
    val cartItems by cartViewModel.cartItems.collectAsState(initial = emptyList())
    val cartTotal by cartViewModel.cartTotal.collectAsState(initial = 0.0)
    val paymentStatus by paymentViewModel.paymentStatus.collectAsState()

    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var deliverToSomeoneElse by remember { mutableStateOf(false) }
    var otherRecipientName by remember { mutableStateOf("") }
    var otherRecipientAddress by remember { mutableStateOf("") }
    var otherRecipientPhone by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var selectedVoucher by remember { mutableStateOf("No Voucher") }
    var showVoucherDialog by remember { mutableStateOf(false) }
    var isPlacingOrder by remember { mutableStateOf(false) }
    var showPaymentConfirmationDialog by remember { mutableStateOf(false) }
    var zpTransToken by remember { mutableStateOf("") }

    val callbackManager = CallbackManager.Factory.create()
    val authRepository = remember {
        AuthRepository(
            auth = FirebaseAuth.getInstance(),
            db = FirebaseFirestore.getInstance(),
            callbackManager
        )
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: "G4AYyav2DUMPFEqtK7lcmfYkKk22"
    var username by remember { mutableStateOf("Tin") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val user = authRepository.getUserById(userId)
            username = user?.username ?: "Unknown User"
            address = user?.address ?: ""
            phoneNumber = user?.phone ?: ""
        }
    }

    // Theo dõi paymentStatus để hiển thị Toast khi thanh toán PayPal thất bại hoặc bị hủy
    LaunchedEffect(paymentStatus) {
        when (paymentStatus) {
            is PaymentStatus.Failure -> {
                isPlacingOrder = false
                val errorMessage = (paymentStatus as PaymentStatus.Failure).error.message ?: "Unknown error"
                Toast.makeText(context, "PayPal payment failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
            is PaymentStatus.Canceled -> {
                isPlacingOrder = false
                Toast.makeText(context, "PayPal payment canceled", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val itemsTotal = cartTotal ?: 0.0
    val discount = 0.0
    val finalTotal = itemsTotal - discount
    // Convert USD to VND for ZaloPay (1 USD = 25,000 VND)
    val finalTotalVND = (finalTotal * 25000).toLong().toString()

    fun placeOrder(
        deliveryUsername: String,
        deliveryAddress: String,
        deliveryPhone: String,
        totalPrice: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val orderItems = cartItems.map { entity ->
            CartItemFirebase(
                productId = entity.productId,
                productTitle = entity.productTitle,
                productImage = entity.productImage,
                price = entity.price,
                quantity = entity.quantity
            )
        }

        // Lưu thông tin đơn hàng tạm thời vào PaymentViewModel trước khi thanh toán PayPal
        val pendingOrder = PendingOrder(
            deliveryUsername = deliveryUsername,
            deliveryAddress = deliveryAddress,
            deliveryPhone = deliveryPhone,
            totalPrice = totalPrice,
            orderItems = orderItems
        )
        paymentViewModel.updatePaymentStatus(PaymentStatus.Idle) // Reset trạng thái
        paymentViewModel.clearPendingOrder() // Xóa đơn hàng cũ
        paymentViewModel.setPendingOrder(pendingOrder) // Lưu đơn hàng mới
        paymentViewModel.saveState() // Lưu trạng thái vào SharedPreferences

        val order = OrderFirebase(
            orderId = FirebaseFirestore.getInstance().collection("orders").document().id,
            userId = userId,
            username = deliveryUsername,
            phone = deliveryPhone,
            address = deliveryAddress,
            orderDetail = orderItems,
            totalPrice = totalPrice,
            status = "pending",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        orderViewModel.addOrder(
            order = order,
            onSuccess = {
                cartViewModel.clearCart()
                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Orders.route) {
                    popUpTo(Screen.Checkout.route) { inclusive = true }
                }
                onSuccess()
            },
            onFailure = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                onFailure(errorMessage)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Checkout",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "\$${String.format("%.2f", finalTotal)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Button(
                        onClick = {
                            if (userId.isEmpty()) {
                                Toast.makeText(context, "Please log in to place an order", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (username.isEmpty()) {
                                Toast.makeText(context, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val deliveryAddress: String
                            val deliveryPhone: String
                            val deliveryUsername: String

                            if (deliverToSomeoneElse) {
                                if (otherRecipientName.isBlank()) {
                                    Toast.makeText(context, "Please enter recipient name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (otherRecipientAddress.isBlank()) {
                                    Toast.makeText(context, "Please enter recipient address", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (otherRecipientPhone.isBlank()) {
                                    Toast.makeText(context, "Please enter recipient phone number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                deliveryUsername = otherRecipientName
                                deliveryAddress = otherRecipientAddress
                                deliveryPhone = otherRecipientPhone
                            } else {
                                if (address.isBlank()) {
                                    Toast.makeText(context, "Please enter your address", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (phoneNumber.isBlank()) {
                                    Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                deliveryUsername = username
                                deliveryAddress = address
                                deliveryPhone = phoneNumber
                            }

                            if (selectedPaymentMethod == null) {
                                Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (selectedPaymentMethod == PaymentMethod.ZALOPAY && !isPlacingOrder) {
                                isPlacingOrder = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        ZaloPayManager.init(context)
                                        Log.d("CheckoutScreen", "ZaloPay SDK initialized successfully")
                                    } catch (e: Exception) {
                                        Log.e("CheckoutScreen", "Failed to initialize ZaloPay SDK: ${e.message}", e)
                                        coroutineScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "ZaloPay initialization failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                            isPlacingOrder = false
                                        }
                                        return@launch
                                    }

                                    try {
                                        val createOrder = CreateOrder()
                                        val response = createOrder.createOrder(finalTotalVND)
                                        Log.d("CheckoutScreen", "CreateOrder response: $response")
                                        if (response != null) {
                                            val returnCode = response.getInt("return_code")
                                            if (returnCode == 1) {
                                                zpTransToken = response.getString("zp_trans_token")
                                                Log.d("CheckoutScreen", "zp_trans_token: $zpTransToken")
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    showPaymentConfirmationDialog = true
                                                    isPlacingOrder = false
                                                }
                                            } else {
                                                val returnMessage = response.getString("return_message")
                                                val subReturnCode = response.getInt("sub_return_code")
                                                val subReturnMessage = response.getString("sub_return_message")
                                                Log.e("CheckoutScreen", "CreateOrder failed: return_code=$returnCode, return_message=$returnMessage, sub_return_code=$subReturnCode, sub_return_message=$subReturnMessage")
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    Toast.makeText(context, "ZaloPay error: $subReturnMessage", Toast.LENGTH_LONG).show()
                                                    isPlacingOrder = false
                                                }
                                            }
                                        } else {
                                            Log.e("CheckoutScreen", "CreateOrder response is null")
                                            coroutineScope.launch(Dispatchers.Main) {
                                                Toast.makeText(context, "Failed to create ZaloPay order: Response null", Toast.LENGTH_SHORT).show()
                                                isPlacingOrder = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("CheckoutScreen", "CreateOrder exception: ${e.message}", e)
                                        coroutineScope.launch(Dispatchers.Main) {
                                            Toast.makeText(context, "ZaloPay error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            isPlacingOrder = false
                                        }
                                    }
                                }
                            } else {
                                showPaymentConfirmationDialog = true
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !isPlacingOrder
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Place Order",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delivery Information",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = deliverToSomeoneElse,
                                onCheckedChange = { deliverToSomeoneElse = it }
                            )
                            Text(
                                text = "Deliver to someone else",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        if (deliverToSomeoneElse) {
                            OutlinedTextField(
                                value = otherRecipientName,
                                onValueChange = { otherRecipientName = it },
                                label = { Text("Recipient Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            OutlinedTextField(
                                value = otherRecipientAddress,
                                onValueChange = { otherRecipientAddress = it },
                                label = { Text("Recipient Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            OutlinedTextField(
                                value = otherRecipientPhone,
                                onValueChange = { otherRecipientPhone = it },
                                label = { Text("Recipient Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Order Items (${cartItems.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.productImage,
                                    contentDescription = item.productTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productTitle,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Quantity: ${item.quantity} x \$${String.format("%.2f", item.price)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Text(
                                    text = "\$${String.format("%.2f", item.price * item.quantity)}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                        .clickable { showVoucherDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Voucher",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = selectedVoucher,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Payment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        PaymentMethodOption(
                            method = "Cash on Delivery",
                            icon = Icons.Default.Money,
                            isSelected = selectedPaymentMethod == PaymentMethod.COD,
                            onSelect = { selectedPaymentMethod = PaymentMethod.COD }
                        )
                        PaymentMethodOption(
                            method = "PayPal",
                            icon = Icons.Default.AccountBalance,
                            isSelected = selectedPaymentMethod == PaymentMethod.PAYPAL,
                            onSelect = { selectedPaymentMethod = PaymentMethod.PAYPAL }
                        )
                        PaymentMethodOption(
                            method = "ZaloPay",
                            icon = Icons.Default.PhoneAndroid,
                            isSelected = selectedPaymentMethod == PaymentMethod.ZALOPAY,
                            onSelect = { selectedPaymentMethod = PaymentMethod.ZALOPAY }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Details",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Items",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "\$${String.format("%.2f", itemsTotal)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Discount",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "-\$${String.format("%.2f", discount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "\$${String.format("%.2f", finalTotal)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showVoucherDialog) {
        AlertDialog(
            onDismissRequest = { showVoucherDialog = false },
            title = {
                Text(
                    text = "Select Voucher",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoucherOption(
                        voucher = "No Voucher",
                        isSelected = selectedVoucher == "No Voucher",
                        onSelect = {
                            selectedVoucher = "No Voucher"
                            showVoucherDialog = false
                        }
                    )
                    VoucherOption(
                        voucher = "10% Off (SAVE10)",
                        isSelected = selectedVoucher == "10% Off (SAVE10)",
                        onSelect = {
                            selectedVoucher = "10% Off (SAVE10)"
                            showVoucherDialog = false
                        }
                    )
                    VoucherOption(
                        voucher = "Free Shipping (FREESHIP)",
                        isSelected = selectedVoucher == "Free Shipping (FREESHIP)",
                        onSelect = {
                            selectedVoucher = "Free Shipping (FREESHIP)"
                            showVoucherDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVoucherDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPaymentConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showPaymentConfirmationDialog = false
                isPlacingOrder = false
            },
            title = { Text("Confirm Payment") },
            text = {
                Text(
                    when (selectedPaymentMethod) {
                        PaymentMethod.PAYPAL -> "Proceed with PayPal payment of \$${String.format("%.2f", finalTotal)}?"
                        PaymentMethod.COD -> "Proceed with Cash on Delivery (COD) payment of \$${String.format("%.2f", finalTotal)}?"
                        PaymentMethod.ZALOPAY -> "Proceed with ZaloPay payment of ${String.format("%,d", finalTotalVND.toLong())} VND?"
                        else -> "Proceed with payment of \$${String.format("%.2f", finalTotal)}?"
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    when (selectedPaymentMethod) {
                        PaymentMethod.PAYPAL -> {
                            showPaymentConfirmationDialog = false
                            isPlacingOrder = true
                            paymentViewModel.initiatePayPalPayment(finalTotal, "USD")
                        }
                        PaymentMethod.COD -> {
                            isPlacingOrder = true
                            val deliveryUsername = if (deliverToSomeoneElse) otherRecipientName else username
                            val deliveryAddress = if (deliverToSomeoneElse) otherRecipientAddress else address
                            val deliveryPhone = if (deliverToSomeoneElse) otherRecipientPhone else phoneNumber

                            placeOrder(
                                deliveryUsername = deliveryUsername,
                                deliveryAddress = deliveryAddress,
                                deliveryPhone = deliveryPhone,
                                totalPrice = finalTotal,
                                onSuccess = {
                                    isPlacingOrder = false
                                    showPaymentConfirmationDialog = false
                                },
                                onFailure = {
                                    isPlacingOrder = false
                                    showPaymentConfirmationDialog = false
                                }
                            )
                        }
                        PaymentMethod.ZALOPAY -> {
                            showPaymentConfirmationDialog = false
                            isPlacingOrder = true
//                            ZaloPaySDK.getInstance().payOrder(
//                                activity,
//                                zpTransToken,
//                                "demozpdk://app",
//                                object : PayOrderListener {
//                                    override fun onPaymentSucceeded(transactionId: String, transToken: String, appTransID: String) {
//                                        Log.d("CheckoutScreen", "ZaloPay payment successful: transactionId=$transactionId, transToken=$transToken, appTransID=$appTransID")
//                                        val deliveryUsername = if (deliverToSomeoneElse) otherRecipientName else username
//                                        val deliveryAddress = if (deliverToSomeoneElse) otherRecipientAddress else address
//                                        val deliveryPhone = if (deliverToSomeoneElse) otherRecipientPhone else phoneNumber
//                                        placeOrder(
//                                            deliveryUsername = deliveryUsername,
//                                            deliveryAddress = deliveryAddress,
//                                            deliveryPhone = deliveryPhone,
//                                            totalPrice = finalTotal,
//                                            onSuccess = {
//                                                isPlacingOrder = false
//                                                zpTransToken = ""
//                                                Toast.makeText(
//                                                    context,
//                                                    "ZaloPay payment successful: TransactionId: $transactionId",
//                                                    Toast.LENGTH_LONG
//                                                ).show()
//                                            },
//                                            onFailure = { error ->
//                                                isPlacingOrder = false
//                                                Toast.makeText(context, "Failed to save order: $error", Toast.LENGTH_SHORT).show()
//                                            }
//                                        )
//                                    }
//
//                                    override fun onPaymentCanceled(zpTransToken: String, appTransID: String) {
//                                        Log.d("CheckoutScreen", "ZaloPay payment canceled: zpTransToken=$zpTransToken, appTransID=$appTransID")
//                                        isPlacingOrder = false
//                                        Toast.makeText(
//                                            context,
//                                            "ZaloPay payment canceled: zpTransToken: $zpTransToken",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//
//                                    override fun onPaymentError(error: ZaloPayError, zpTransToken: String, appTransID: String) {
//                                        Log.e("CheckoutScreen", "ZaloPay payment error: error=${error.name}, zpTransToken=$zpTransToken, appTransID=$appTransID")
//                                        isPlacingOrder = false
//                                        Toast.makeText(
//                                            context,
//                                            "ZaloPay payment error: ${error.name}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//                            )
                        }
                        else -> {
                            showPaymentConfirmationDialog = false
                            isPlacingOrder = false
                        }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPaymentConfirmationDialog = false
                    isPlacingOrder = false
                }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun PaymentMethodOption(
    method: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = method,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun VoucherOption(
    voucher: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocalOffer,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = voucher,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}