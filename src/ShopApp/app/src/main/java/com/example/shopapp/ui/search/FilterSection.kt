package com.example.shopapp.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    visible: Boolean,
    categories: List<String>,
    selectedCategory: String?,
    priceRange: ClosedFloatingPointRange<Float>,
    minRating: Int,
    onCategorySelected: (String?) -> Unit,
    onPriceRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit,
    onMinRatingChanged: (Int) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Category Filter
                Text(
                    text = "Category",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("All") }
                    )

                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Range Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Price Range",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "\$${priceRange.start.toInt()} - \$${priceRange.endInclusive.toInt()}",
                        fontSize = 14.sp
                    )
                }

                RangeSlider(
                    value = priceRange,
                    onValueChange = onPriceRangeChanged,
                    valueRange = 0f..10000f,
                    steps = 10
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rating Filter
                Text(
                    text = "Min Rating",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..4).forEach { rating ->
                        FilterChip(
                            selected = minRating == rating,
                            onClick = { onMinRatingChanged(rating) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${rating}+ ")
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFFFD700)
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apply and Clear buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onClearFilters) {
                        Text("Clear")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = onApplyFilters) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}