package com.example.shopapp.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopapp.data.model.Review
import com.example.shopapp.data.repository.AuthRepository
import com.example.shopapp.navigation.Screen
import com.example.shopapp.viewmodel.CartViewModel
import com.example.shopapp.viewmodel.ProductViewModel
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    productId: String,
    userId: String,
    navController: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    val products by productViewModel.products.collectAsState()
    val product = products.find { it.productId == productId }
    val context = LocalContext.current
    var quantity by remember { mutableStateOf(1) }

    // Sắp xếp reviews theo thời gian
    val allReviews = product?.review?.sortedByDescending { it.createdAt?.toDate() } ?: emptyList()
    Log.d("AllReviewsScreen", "allReviews.size = ${allReviews.size}")

    val callbackManager = CallbackManager.Factory.create()

    // AuthRepository lấy thông tin người dùng
    val authRepository = remember {
        AuthRepository(
            auth = FirebaseAuth.getInstance(),
            db = FirebaseFirestore.getInstance(),
            callbackManager
        )
    }

    // State lưu trữ username của các userId
    val userNames = remember { mutableStateMapOf<String, String>() }

    // State quản lý dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }

    // username cho từng review
    LaunchedEffect(product) {
        product?.review?.forEach { review ->
            if (!userNames.containsKey(review.userId)) {
                val user = authRepository.getUserById(review.userId)
                userNames[review.userId] = user?.username ?: "Unknown User"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Reviews") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Carts.route)
                    }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        product?.let {
                            cartViewModel.addToCart(it, quantity)
                            Toast.makeText(context, "${it.title} added to cart", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("ADD TO CART")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        product?.let {
                            cartViewModel.addToCart(it, quantity)
                            navController.navigate(Screen.Orders.route)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("BUY NOW")
                }
            }
        }
    ) { padding ->
        product?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reviews (${it.review.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )
                if (it.review.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(allReviews) { _, review ->
                            ReviewItem(
                                review = review,
                                userName = userNames[review.userId] ?: "Loading...",
                                canDelete = userId == review.userId,
                                onDelete = {
                                    reviewToDelete = review
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                } else {
                    Text("No reviews yet.", color = Color.Gray)
                }
            }
            // Dialog xác nhận xóa
            if (showDeleteDialog && reviewToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        // Đóng dialog khi nhấn ngoài dialog
                        showDeleteDialog = false
                        reviewToDelete = null
                    },
                    title = {
                        Text(text = "Confirm Delete")
                    },
                    text = {
                        Text(text = "Are you sure you want to delete your review?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Thực hiện xóa review
                                reviewToDelete?.let { review ->
                                    productViewModel.removeReview(productId, review)
                                    Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
                                    Log.d("AllReviewsScreen", "Review deleted: ${review.reviewId}")
                                }
                                showDeleteDialog = false
                                reviewToDelete = null
                            }
                        ) {
                            Text("Yes", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                // Đóng dialog mà không xóa
                                showDeleteDialog = false
                                reviewToDelete = null
                            }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}