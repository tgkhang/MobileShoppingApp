package com.example.shopapp.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.data.dao.FirebaseProductDao
import com.example.shopapp.data.dao.admin.MockOrderDao
import com.example.shopapp.data.dao.admin.RealOrderDao
import com.example.shopapp.data.dao.admin.MockUserDao
import com.example.shopapp.data.dao.admin.RealUserDao
import com.example.shopapp.data.dao.admin.MockEventDao
import com.example.shopapp.data.dao.admin.RealEventDao
import com.example.shopapp.data.repository.OrderRepository
import com.example.shopapp.data.repository.ProductRepository
import com.example.shopapp.data.repository.UserRepository
import com.example.shopapp.data.repository.EventRepository
import com.example.shopapp.ui.admin.AdminHomeScreen
import com.example.shopapp.ui.admin.events.AddEventScreen
import com.example.shopapp.ui.admin.orders.OrderDetailScreen
import com.example.shopapp.ui.admin.orders.OrderManagementScreen
import com.example.shopapp.ui.admin.events.EventManagementScreen
import com.example.shopapp.ui.admin.events.EventDetailScreen
import com.example.shopapp.ui.admin.events.EditEventScreen
import com.example.shopapp.ui.admin.products.AddProductScreen
import com.example.shopapp.ui.admin.products.EditProductScreen
import com.example.shopapp.ui.admin.products.ProductDetailScreen
import com.example.shopapp.ui.admin.products.ProductManagementScreen
import com.example.shopapp.ui.admin.users.UserDetailScreen
import com.example.shopapp.ui.admin.users.UserManagementScreen
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.OrderViewModel
import com.example.shopapp.viewmodel.OrderViewModelFactory
import com.example.shopapp.viewmodel.EventViewModel
import com.example.shopapp.viewmodel.EventViewModelFactory
import com.example.shopapp.viewmodel.ProductViewModel
import com.example.shopapp.viewmodel.ProductViewModelFactory
import com.example.shopapp.viewmodel.UserViewModel
import com.example.shopapp.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.shopapp.services.NotificationService

@Composable
fun AdminNavigation(authViewModel: AuthViewModel, rootNavController: NavController) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(ProductRepository(FirebaseProductDao()), currentUserId)
    )
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(RealUserDao()))
    )
    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModelFactory(
            OrderRepository(RealOrderDao()),
            NotificationService(FirebaseFirestore.getInstance(), context)
        )
    )

    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(EventRepository(RealEventDao()))
    )

    NavHost(navController = navController, startDestination = Screen.AdminHome.route) {
        composable(Screen.AdminHome.route) { AdminHomeScreen(navController, authViewModel, rootNavController, orderViewModel, productViewModel, userViewModel) }

        composable(Screen.ProductManagement.route) {
            ProductManagementScreen(
                navController = navController,
                authViewModel = authViewModel,
                rootNavController = rootNavController,
                productViewModel = productViewModel,
                onProductClick = { product ->
                    productViewModel.selectProduct(product)
                    navController.navigate("product_detail")
                }
            )
        }
        composable("add_product") { AddProductScreen(navController, productViewModel) }
        composable("product_detail") { ProductDetailScreen(navController, productViewModel) }
        composable("edit_product") { EditProductScreen(navController, productViewModel) }

        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                navController = navController,
                authViewModel = authViewModel,
                rootNavController = rootNavController,
                userViewModel = userViewModel,
                onUserClick = { user ->
                    userViewModel.selectUser(user)
                    navController.navigate("user_detail")
                }
            )
        }
        composable("user_detail") { UserDetailScreen(navController, userViewModel, orderViewModel) }

        composable(Screen.OrderManagement.route){
            OrderManagementScreen(
                navController = navController,
                authViewModel = authViewModel,
                rootNavController = rootNavController,
                orderViewModel = orderViewModel,
                onOrderClick = { order ->
                    orderViewModel.selectOrder(order)
                    navController.navigate("order_detail")
                }
            )
        }
        composable("order_detail") { OrderDetailScreen(navController, orderViewModel, productViewModel) }

        composable(Screen.EventManagement.route) {
            EventManagementScreen(
                navController = navController,
                authViewModel = authViewModel,
                rootNavController = rootNavController,
                eventViewModel = eventViewModel,
                onEventClick = { event ->
                    eventViewModel.selectEvent(event)
                    navController.navigate("event_detail")
                }
            )
        }
        composable("event_detail") { EventDetailScreen(navController, eventViewModel) }
        composable("add_event") { AddEventScreen(navController, eventViewModel) }
        composable("edit_event") { EditEventScreen(navController, eventViewModel) }
    }
}