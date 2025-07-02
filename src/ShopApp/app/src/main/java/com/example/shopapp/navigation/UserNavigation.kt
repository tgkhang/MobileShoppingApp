package com.example.shopapp.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.data.CartDatabase
import com.example.shopapp.data.dao.FirebaseNotificationDao
import com.example.shopapp.data.dao.FirebaseOrderDao
import com.example.shopapp.data.dao.FirebaseProductDao
import com.example.shopapp.data.dao.FirebaseUserDao
import com.example.shopapp.data.model.OrderFirebase
import com.example.shopapp.data.repository.CartRepository
import com.example.shopapp.data.repository.NotificationRepository
import com.example.shopapp.data.repository.OrderRepositoryFirebase
import com.example.shopapp.data.repository.PaymentRepository
import com.example.shopapp.data.repository.ProductRepository
import com.example.shopapp.data.repository.UserKRepository
import com.example.shopapp.ui.search.SearchScreen
import com.example.shopapp.ui.user.AllReviewsScreen
import com.example.shopapp.ui.user.ImageDetailScreen
import com.example.shopapp.ui.user.ProductDetailScreen
import com.example.shopapp.ui.user.UserHomeScreen
import com.example.shopapp.ui.user.order.CartScreen
import com.example.shopapp.ui.user.order.CheckoutScreen
import com.example.shopapp.ui.user.order.OrderScreen
import com.example.shopapp.ui.user.profile.EditProfileScreen
import com.example.shopapp.ui.user.profile.ProfileScreen
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.CartViewModel
import com.example.shopapp.viewmodel.CartViewModelFactory
import com.example.shopapp.viewmodel.NotificationViewModel
import com.example.shopapp.viewmodel.NotificationViewModelFactory
import com.example.shopapp.viewmodel.OrderViewModelFactoryFirebase
import com.example.shopapp.viewmodel.OrderViewModelFirebase
import com.example.shopapp.viewmodel.PaymentStatus
import com.example.shopapp.viewmodel.PaymentViewModel
import com.example.shopapp.viewmodel.PaymentViewModelFactory
import com.example.shopapp.viewmodel.ProductViewModel
import com.example.shopapp.viewmodel.ProductViewModelFactory
import com.example.shopapp.viewmodel.ProfileViewModel
import com.example.shopapp.viewmodel.ProfileViewModelFactory
import com.example.shopapp.viewmodel.SearchViewModel
import com.example.shopapp.viewmodel.SearchViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
) {
    object Home : BottomNavItem(
        route = Screen.UserHome.route,
        title = "Home",
        selectedIcon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
        unselectedIcon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }
    )

    object Search : BottomNavItem(
        route = Screen.Search.route,
        title = "Search",
        selectedIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
        unselectedIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") }
    )

    object Orders : BottomNavItem(
        route = Screen.Orders.route,
        title = "Orders",
        selectedIcon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Orders") },
        unselectedIcon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Orders") }
    )

    object Cart : BottomNavItem(
        route = Screen.Carts.route,
        title = "Cart",
        selectedIcon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") },
        unselectedIcon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart") }
    )

    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
        unselectedIcon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") }
    )
}

@Composable
fun UserNavigation(authViewModel: AuthViewModel, rootNavController: NavController, intent: Intent?) {
    val navController = rememberNavController()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val productRepository = ProductRepository(FirebaseProductDao())
    val database = CartDatabase.getDatabase(LocalContext.current)
    val cartRepository = CartRepository(database.cartDao())
    val orderRepository = OrderRepositoryFirebase(FirebaseOrderDao())
    val userRepository = UserKRepository(FirebaseUserDao())
    val paymentRepository = PaymentRepository(LocalContext.current)

    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(productRepository, currentUserId)
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(productRepository)
    )
    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(cartRepository)
    )
    val orderViewModel: OrderViewModelFirebase = viewModel(
        key = "user_order_viewmodel",
        factory = OrderViewModelFactoryFirebase(orderRepository)
    )


    val notificationViewModel : NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(NotificationRepository(FirebaseNotificationDao()))
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository, FirebaseAuth.getInstance(), authViewModel)
    )
    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(LocalContext.current.applicationContext as android.app.Application, paymentRepository)
    )

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Orders,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    LaunchedEffect(navController, intent) {
        intent?.let { incomingIntent ->
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(incomingIntent)
                .addOnSuccessListener { pendingDynamicLinkData ->
                    val deepLink = pendingDynamicLinkData?.link
                    deepLink?.let { uri ->
                        Log.d("DynamicLink", "Received deep link: $uri")
                        if (uri.host == "myeshopapp.page.link" && uri.pathSegments.contains("product_detail")) {
                            val productId = uri.lastPathSegment
                            productId?.let {
                                navController.navigate("product_detail/$it") {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                    launchSingleTop = true
                                }
                                Log.d("DynamicLink", "Navigated to product_detail/$it")
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DynamicLink", "Failed to get dynamic link: ${e.message}")
                }
        }
    }

    val paymentStatus by paymentViewModel.paymentStatus.collectAsState()
    LaunchedEffect(paymentStatus) {
        when (paymentStatus) {
            is PaymentStatus.Success -> {
                val pendingOrder = paymentViewModel.getPendingOrder()
                if (pendingOrder != null) {
                    val order = OrderFirebase(
                        orderId = FirebaseFirestore.getInstance().collection("orders").document().id,
                        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "G4AYyav2DUMPFEqtK7lcmfYkKk22",
                        username = pendingOrder.deliveryUsername,
                        phone = pendingOrder.deliveryPhone,
                        address = pendingOrder.deliveryAddress,
                        orderDetail = pendingOrder.orderItems,
                        totalPrice = pendingOrder.totalPrice,
                        status = "pending",
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )

                    orderViewModel.addOrder(
                        order = order,
                        onSuccess = {
                            paymentViewModel.clearPendingOrder()
                            navController.navigate(Screen.Orders.route) {
                                popUpTo(Screen.Checkout.route) { inclusive = true }
                            }
                        },
                        onFailure = { errorMessage ->
                            Log.e("UserNavigation", "Failed to place order: $errorMessage")
                        }
                    )
                }
            }
            is PaymentStatus.Failure -> {
                Log.e("UserNavigation", "Payment failed: ${(paymentStatus as PaymentStatus.Failure).error.message}")
            }
            is PaymentStatus.Canceled -> {
                Log.d("UserNavigation", "Payment canceled")
            }
            is PaymentStatus.Loading -> {
                // Payment in progress
            }
            is PaymentStatus.Idle -> {
                // No action
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, items = navItems)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.UserHome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.UserHome.route) {
                UserHomeScreen(navController, authViewModel, rootNavController, productViewModel, notificationViewModel)
            }

            composable(Screen.Search.route) {
                SearchScreen(navController, productViewModel, searchViewModel)
            }

            composable(Screen.Carts.route) {
                CartScreen(navController, cartViewModel)
            }

            composable(Screen.Orders.route) {
                OrderScreen(navController, orderViewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController, authViewModel, rootNavController)
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    navController,
                    profileViewModel
                )
            }

            composable(Screen.ProductDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    userId = currentUserId,
                    navController = navController,
                    productViewModel = productViewModel,
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.AllReviews.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                AllReviewsScreen(
                    productId = productId,
                    userId = currentUserId,
                    navController = navController,
                    productViewModel = productViewModel,
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(navController, cartViewModel, orderViewModel, paymentViewModel)
            }

            composable(Screen.ImageDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val initialImageIndex = backStackEntry.arguments?.getString("initialImageIndex")?.toIntOrNull() ?: 0

                val products by productViewModel.products.collectAsState()
                val product = products.find { it.productId == productId }
                product?.let {
                    val images = if (it.images.isNotEmpty()) it.images else listOf(it.image)
                    ImageDetailScreen(
                        images = images,
                        initialImageIndex = initialImageIndex,
                        productTitle = it.title,
                        navController = navController
                    )
                } ?: run {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDetailScreen = currentDestination?.route?.startsWith("productDetail") == true
    val isAllReviewsScreen = currentDestination?.route?.startsWith("allReviews") == true
    val isCheckoutScreen = currentDestination?.route == Screen.Checkout.route

    if (!isDetailScreen && !isAllReviewsScreen && !isCheckoutScreen) {
        NavigationBar {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                NavigationBarItem(
                    icon = { if (selected) item.selectedIcon() else item.unselectedIcon() },
                    label = { Text(text = item.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}