package com.example.shopapp.ui.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun AppliedFiltersRow(
    selectedCategory: String?,
    priceRange: ClosedFloatingPointRange<Float>,
    minRating: Int,
    defaultPriceRange: ClosedFloatingPointRange<Float> = 0f..10000f,
    onClearCategory: () -> Unit,
    onClearPriceRange: () -> Unit,
    onClearRating: () -> Unit
) {
    // Only show filters section if any filter is applied
    if (selectedCategory != null || priceRange != defaultPriceRange || minRating > 0) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Applied Filters:",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedCategory != null) {
                    SuggestionChip(
                        onClick = onClearCategory,
                        label = { Text("Category: $selectedCategory") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                if (priceRange != defaultPriceRange) {
                    SuggestionChip(
                        onClick = onClearPriceRange,
                        label = { Text("Price: \$${priceRange.start.toInt()}-\$${priceRange.endInclusive.toInt()}") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                if (minRating > 0) {
                    SuggestionChip(
                        onClick = onClearRating,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Min Rating: $minRating")
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCount(count: Int) {
    Text(
        text = "Found $count products",
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}