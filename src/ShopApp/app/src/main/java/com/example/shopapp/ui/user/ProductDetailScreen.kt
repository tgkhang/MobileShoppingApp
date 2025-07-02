package com.example.shopapp.ui.user

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopapp.data.model.OrderFirebase
import com.example.shopapp.data.model.Review
import com.example.shopapp.data.repository.AuthRepository
import com.example.shopapp.navigation.Screen
import com.example.shopapp.ui.components.ProductCard
import com.example.shopapp.viewmodel.CartViewModel
import com.example.shopapp.viewmodel.ProductViewModel
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    userId: String,
    navController: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    val products by productViewModel.products.collectAsState()
    val product = products.find { it.productId == productId }
    val context = LocalContext.current

    // State for quantity selector
    var quantity by remember { mutableStateOf(1) }

    // State for "Added to Cart" snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading by productViewModel.isLoading.collectAsState()
    val hasMoreData by productViewModel.hasMoreData.collectAsState()
    val selectedCategory by productViewModel.selectedCategory.collectAsState()
    var showEmptyState by remember { mutableStateOf(false) }

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

    // Load related products when product category changes
    LaunchedEffect(product?.category) {
        product?.category?.let { category ->
            if (selectedCategory != category) {
                productViewModel.filterByCategory(category)
            }
        }
    }

    // Handle empty state display
    LaunchedEffect(products, isLoading) {
        if (products.isEmpty() && !isLoading) {
            delay(700) // delay before showing empty state
            showEmptyState = true
        } else {
            showEmptyState = false
        }
    }

    // State for review
    val averageRating by productViewModel.averageRating.collectAsState()
    val reviewCount by productViewModel.reviewCount.collectAsState()
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    var hasPurchased by remember { mutableStateOf(false) }
    var isCheckingPurchase by remember { mutableStateOf(true) } // Trạng thái kiểm tra mua hàng

    // Cập nhật selectedProduct khi product thay đổi
    LaunchedEffect(product) {
        product?.let { productViewModel.selectProduct(it) }
    }

    val callbackManager = CallbackManager.Factory.create()
    // AuthRepository lấy thông tin người dùng
    val authRepository = remember {
        AuthRepository(
            auth = FirebaseAuth.getInstance(),
            db = FirebaseFirestore.getInstance(),
            callbackManager
        )
    }

    // State stores usernames of userIds
    val userNames = remember { mutableStateMapOf<String, String>() }

    // State for dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }

    // Download username for each review
    LaunchedEffect(product) {
        product?.review?.forEach { review ->
            if (!userNames.containsKey(review.userId)) {
                val user = authRepository.getUserById(review.userId)
                userNames[review.userId] = user?.username ?: "Unknown User"
            }
        }
    }

    // Check if the user has successfully purchased the product
    LaunchedEffect(userId, productId) {
        Log.d("ProductDetailScreen", "LaunchedEffect running with userId: $userId, productId: $productId")
        if (userId.isNotEmpty() && productId.isNotEmpty()) {
            isCheckingPurchase = true // Bắt đầu kiểm tra
            val db = FirebaseFirestore.getInstance()
            db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("ProductDetailScreen", "Query returned ${querySnapshot.size()} orders")
                    var purchased = false
                    for (document in querySnapshot.documents) {
                        val order = document.toObject(OrderFirebase::class.java)
                        Log.d("ProductDetailScreen", "Order: $order")
                        if (order?.orderDetail?.isNotEmpty() == true) {
                            order.orderDetail.forEach { item ->
                                Log.d("ProductDetailScreen", "OrderDetail item: $item")
                                if (item.productId == productId) {
                                    purchased = true
                                    Log.d("ProductDetailScreen", "Found matching productId: $productId")
                                    return@forEach
                                }
                            }
                        }
                        if (purchased) break
                    }
                    Log.d("ProductDetailScreen", "hasPurchased set to: $purchased")
                    hasPurchased = purchased
                    isCheckingPurchase = false // Kết thúc kiểm tra
                }
                .addOnFailureListener { e ->
                    Log.e("ProductDetailScreen", "Error checking purchase status: $e")
                    hasPurchased = false
                    isCheckingPurchase = false // Kết thúc kiểm tra
                    Toast.makeText(context, "Unable to verify purchase status", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("ProductDetailScreen", "userId or productId is empty")
            isCheckingPurchase = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }

                    // Share button
                    IconButton(onClick = {
                        val shareLink = createShareLink(product, context)
                        shareLink?.let {
                            val shareMessage = "$it"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Check out this product!")
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Product"))
                        } ?: Toast.makeText(context, "Failed to create share link", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = {
                        // Navigate to cart
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
                val isInStock = product?.stock?.let { it > 0 } ?: false

                if (isInStock) {
                    OutlinedButton(
                        onClick = {
                            // Add to cart action
                            product?.let {
                                cartViewModel.addToCart(it, quantity)
                                Toast.makeText(context, "${it.title} added to cart", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("ADD TO CART")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Buy now action - Add to cart and navigate to checkout
                            product?.let {
                                cartViewModel.addToCart(it, quantity)
                                navController.navigate(Screen.Carts.route)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("BUY NOW")
                    }
                } else {
                    OutlinedButton(
                        onClick = { /* No action */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContainerColor = Color(0xFFFFEBEE),
                            disabledContentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("OUT OF STOCK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        product?.let { currentProduct ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Product detail
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Image + Pagination Dots
                            ImageCarousel(
                                images = if (currentProduct.images.isNotEmpty())
                                    currentProduct.images
                                else
                                    listOf(currentProduct.image),
                                productTitle = currentProduct.title,
                                isOutOfStock = currentProduct.stock == 0,
                                productId = currentProduct.productId,
                                navController = navController
                            )

                            Text(currentProduct.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                                Text(
                                    text = averageRating,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " ($reviewCount Ratings)",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
                            }

                            // Price with stock indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "\$${currentProduct.price}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentProduct.stock == 0) Color.Gray else Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                if (currentProduct.stock == 0) {
                                    Text(
                                        "Currently unavailable",
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        "In Stock: ${currentProduct.stock} available",
                                        color = Color(0xFF388E3C),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            if (currentProduct.stock > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text("Quantity: ", fontSize = 16.sp)
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }

                                    Text(
                                        quantity.toString(),
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = { if (quantity < currentProduct.stock) quantity++ },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }

                            Text("Available offers", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OfferItem("5% Unlimited Cashback on Flipkart Axis Bank Credit Card")
                                OfferItem("Flat \$30 discount on first prepaid RuPay transaction")
                                OfferItem("\$30 Off on first prepaid UPI transaction")
                                Text("+5 more", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }

                            Text(currentProduct.description, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OptionCard("FREE Delivery")
                                OptionCard("No cost EMI\n\$2,212/mo")
                                OptionCard("Product Exchange")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    item {
                        // Review overview
                        ReviewOverview(
                            averageRating = averageRating.toFloat(),
                            reviewCount = reviewCount,
                            reviews = currentProduct.review
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Review section
                        Text(
                            "Reviews (${currentProduct.review.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Hiển thị loading indicator khi đang kiểm tra trạng thái mua hàng
                        if (isCheckingPurchase) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            // If not checking purchase status, show review section
                            if (hasPurchased) {
                                Text(
                                    "Add Your Review",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                RatingBar(
                                    rating = rating,
                                    onRatingChanged = { rating = it },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    label = { Text("Your Comment") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (rating > 0 && comment.isNotBlank()) {
                                            productViewModel.addReview(
                                                productId,
                                                rating.toDouble(),
                                                comment
                                            )
                                            rating = 0f // Reset sau khi gửi
                                            comment = ""
                                            Toast.makeText(
                                                context,
                                                "Review submitted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Please provide rating and comment",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text("Submit Review")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                // If not purchased, show a message
                                Text(
                                    text = "You need to purchase and receive this product to leave a review.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Review list
                        if (currentProduct.review.isNotEmpty()) {
                            val latestReviews = currentProduct.review.sortedByDescending { review -> review.createdAt?.toDate() }.take(2)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                latestReviews.forEach { review ->
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
                            if (currentProduct.review.size > 2) {
                                Text(
                                    text = "View All >",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clickable {
                                            navController.navigate(Screen.AllReviews.createRoute(productId))
                                        }
                                )
                            }
                        } else {
                            Text(
                                "No reviews yet.",
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
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
                                            // Xóa review
                                            reviewToDelete?.let { review ->
                                                productViewModel.removeReview(productId, review)
                                                Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
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
                                            showDeleteDialog = false
                                            reviewToDelete = null
                                        }
                                    ) {
                                        Text("No")
                                    }
                                }
                            )
                        }
                    }

                    // First Horizontal Section: Same Category + Same Brand Products
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (products.size > 1) {
                            val sameCategoryBrandProducts = products.filter { relatedProduct ->
                                relatedProduct.productId != currentProduct.productId &&
                                        relatedProduct.category == currentProduct.category &&
                                        relatedProduct.brand == currentProduct.brand
                            }

                            if (sameCategoryBrandProducts.isNotEmpty()) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "More from ${currentProduct.brand} in ${currentProduct.category}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(sameCategoryBrandProducts.take(5)) { brandProduct ->
                                        ProductCard(
                                            product = brandProduct,
                                            modifier = Modifier.width(160.dp),
                                            onClick = {
                                                navController.navigate(
                                                    Screen.ProductDetail.createRoute(brandProduct.productId)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Second Section: Same Category Different Brand Products
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Similar Products in ${currentProduct.category}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Display same category products in a grid (2 columns)
                    val sameCategoryDifferentBrandProducts = products.filter { relatedProduct ->
                        relatedProduct.productId != currentProduct.productId &&
                                relatedProduct.category == currentProduct.category &&
                                relatedProduct.brand != currentProduct.brand
                    }

                    if (sameCategoryDifferentBrandProducts.isNotEmpty()) {
                        items(sameCategoryDifferentBrandProducts.chunked(2)) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { similarProduct ->
                                    ProductCard(
                                        product = similarProduct,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            navController.navigate(
                                                Screen.ProductDetail.createRoute(
                                                    similarProduct.productId
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
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No similar products found",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Loading Indicator at the bottom for pagination
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp
                                )
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
                                    text = "No more products to display",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    images: List<String>,
    productTitle: String,
    isOutOfStock: Boolean,
    productId: String,
    navController: NavController
) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No image available", color = Color.Gray)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { images.size })
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // The image pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) { page ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.ImageDetail.createRoute(productId, page))
                }
            ) {
                AsyncImage(
                    model = images[page],
                    contentDescription = "$productTitle - image ${page + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    alpha = if (isOutOfStock) 0.6f else 1f
                )

                if (isOutOfStock) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xCCFF5252)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "OUT OF STOCK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Pagination indicators
        Row(
            modifier = Modifier
                .padding(8.dp)
                .background(Color.LightGray.copy(alpha = 0.5f), shape = CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(horizontal = 2.dp)
                        .background(
                            color = if (pagerState.currentPage == index) Color.Black else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun OfferItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp)
    }
}

@Composable
fun OptionCard(label: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}