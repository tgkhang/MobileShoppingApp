package com.example.shopapp.ui.user

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopapp.data.model.Product
import com.example.shopapp.data.model.Review
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// Composable cho RatingBar
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RatingBar(rating: Float,
              onRatingChanged: (Float) -> Unit,
              showEmoji: Boolean = true,
              modifier: Modifier = Modifier
) {
    // Ánh xạ số sao với emoji
    val emoji = when (rating.toInt()) {
        1 -> "😞"
        2 -> "😕"
        3 -> "😐"
        4 -> "😊"
        5 -> "🥳"
        else -> "🤔"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // RatingBar với các ngôi sao
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRatingChanged(index + 1f) }
                    .scale(if (index < rating.toInt()) 1.1f else 1.0f)
            )
        }
        if (showEmoji) {
            Spacer(modifier = Modifier.width(8.dp))
            // Hiển thị emoji động
            AnimatedContent(
                targetState = emoji,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                }
            ) { targetEmoji ->
                Text(
                    text = targetEmoji,
                    fontSize = 24.sp
                )
            }
        }
    }
}

// Composable cho mỗi Review
@Composable
fun ReviewItem(
    review: Review,
    userName: String,
    canDelete: Boolean,
    onDelete: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RatingBar(
                        rating = review.rating.toFloat(),
                        onRatingChanged = {},
                        showEmoji = false
                    )
                    Text(
                        text = "by $userName",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = review.createdAt?.toDate()?.let {
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(it)
                    } ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            if (canDelete) {
                AnimatedVisibility(
                    visible = canDelete,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    IconButton(
                        onClick = { onDelete(review) },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Review",
                            tint = Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewOverview(
    averageRating: Float,
    reviewCount: Int,
    reviews: List<Review>
) {
    // Tính số lượng đánh giá cho từng mức sao (1 đến 5)
    val ratingDistribution = IntArray(5) { 0 } // Mảng lưu số lượng đánh giá: [5 sao, 4 sao, 3 sao, 2 sao, 1 sao]
    reviews.forEach { review ->
        val rating = review.rating.toInt().coerceIn(1, 5) // Đảm bảo rating từ 1 đến 5
        ratingDistribution[5 - rating]++ // 5 sao ở index 0, 1 sao ở index 4
    }

    // Tính tỷ lệ phần trăm cho từng mức sao
    val totalReviews = reviews.size
    val ratingPercentages = if (totalReviews > 0) {
        ratingDistribution.map { (it.toFloat() / totalReviews) * 100f }.toFloatArray()
    } else {
        FloatArray(5) { 0f }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding bên trong Card
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bên trái: Điểm trung bình và số lượng đánh giá
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ratings & Reviews",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = averageRating.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Hiển thị 5 ngôi sao với hiệu ứng một phần màu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val fullStars = averageRating.toInt() // Số sao đầy
                    val partialStarFraction = averageRating - fullStars // Phần thập phân (ví dụ: 0.2)
                    val emptyStars = 5 - fullStars - if (partialStarFraction > 0) 1 else 0

                    // Sao đầy
                    repeat(fullStars) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sao một phần (nếu có)
                    if (partialStarFraction > 0) {
                        Box(
                            modifier = Modifier.size(20.dp)
                        ) {
                            // Sao nền (không màu)
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            // Sao có màu (chỉ một phần)
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF388E3C),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(object : Shape {
                                        override fun createOutline(
                                            size: Size,
                                            layoutDirection: LayoutDirection,
                                            density: Density
                                        ): Outline {
                                            val path = Path().apply {
                                                addRect(
                                                    Rect(
                                                        left = 0f,
                                                        top = 0f,
                                                        right = size.width * partialStarFraction,
                                                        bottom = size.height
                                                    )
                                                )
                                            }
                                            return Outline.Generic(path)
                                        }
                                    })
                            )
                        }
                    }

                    // Sao không màu
                    repeat(emptyStars) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val formattedReviewCount = NumberFormat.getInstance().format(reviewCount)
                Text(
                    text = "($formattedReviewCount)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Bên phải: Thanh phân bố số sao
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp), // Khoảng cách giữa hai cột
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 5 downTo 1) {
                    val index = 5 - i
                    val percentage = ratingPercentages[index]
                    // Tạo gradient cho từng mức sao
                    val gradientBrush = when (i) {
                        5 -> Brush.linearGradient(
                            colors = listOf(Color(0xFF1976D2), Color(0xFF388E3C)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                        4 -> Brush.linearGradient(
                            colors = listOf(Color(0xFF303F9F), Color(0xFFAFB42B)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                        3 -> Brush.linearGradient(
                            colors = listOf(Color(0xFFD32F2F), Color(0xFFFBC02D)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                        2 -> Brush.linearGradient(
                            colors = listOf(Color(0xFF689F38), Color(0xFFF57C00)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                        1 -> Brush.linearGradient(
                            colors = listOf(Color(0xFFCE6691), Color(0xFFD32F2F)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                        else -> Brush.linearGradient(
                            colors = listOf(Color.Gray, Color.Gray),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$i",
                            fontSize = 14.sp,
                            modifier = Modifier.width(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = percentage / 100f)
                                    .height(12.dp)
                                    .background(gradientBrush, shape = RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

// Hàm tạo dynamic link
fun createShareLink(product: Product?, context: Context): Uri? {
    val dynamicLink = product?.let {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://myeshopapp.page.link/product_detail/${it.productId}")) // Deep link mới
            .setDomainUriPrefix("https://myeshopapp.page.link") // Domain Firebase Dynamic Links
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.example.shopapp")
                    .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.example.shopapp"))
                    .build()
            )
            .setIosParameters(
                DynamicLink.IosParameters.Builder("com.example.shopapp.ios")
                    .setFallbackUrl(Uri.parse("https://www.example.com")) // Fallback cho iOS
                    .build()
            )
            .buildDynamicLink()
    }
    return dynamicLink?.uri
}