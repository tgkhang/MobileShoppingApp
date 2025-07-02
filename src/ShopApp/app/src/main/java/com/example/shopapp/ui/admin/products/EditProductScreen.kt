package com.example.shopapp.ui.admin.products

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.shopapp.viewmodel.ProductViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.shopapp.services.CloudinaryService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    viewModel: ProductViewModel
) {
    val context = LocalContext.current
    val product = viewModel.selectedProduct
    if (product == null) {
        navController.popBackStack()
        Toast.makeText(context, "No product data.", Toast.LENGTH_SHORT).show()
        return
    }

    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val cloudinaryService = remember { CloudinaryService(context) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf(product.title) }
    var mainImage by remember { mutableStateOf(product.image) }
    var additionalImages by remember { mutableStateOf(product.images) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var description by remember { mutableStateOf(product.description) }
    var brand by remember { mutableStateOf(product.brand) }
    var model by remember { mutableStateOf(product.model) }
    var color by remember { mutableStateOf(product.color) }
    var category by remember { mutableStateOf(product.category) }
    var discount by remember { mutableStateOf(product.discount.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var sales by remember { mutableStateOf(product.sales.toString()) }
    var status by remember { mutableStateOf(product.status) }
    var showError by remember { mutableStateOf(false) }

    var localMainImageUri by remember { mutableStateOf<Uri?>(null) }
    var localAdditionalImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Create image pickers
    val mainImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            localMainImageUri = it
        }
    }

    val additionalImagesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            localAdditionalImageUris = uris
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product") },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Main Image Upload UI
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Main Product Image *",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (localMainImageUri != null) {
                            // Show the locally selected new main image
                            AsyncImage(
                                model = localMainImageUri,
                                contentDescription = "Selected main image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Show the existing main image
                            AsyncImage(
                                model = mainImage,
                                contentDescription = "Current main image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { mainImagePicker.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Change Main Image")
                            }

                            if (localMainImageUri != null) {
                                OutlinedButton(
                                    onClick = { localMainImageUri = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reset")
                                }
                            }
                        }
                    }

                    // Additional Images Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Additional Images",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Show existing additional images
                        if (additionalImages.isNotEmpty()) {
                            Text(
                                "Current Additional Images:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                additionalImages.forEachIndexed { index, imageUrl ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Additional image $index",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        IconButton(
                                            onClick = {
                                                additionalImages = additionalImages.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove image",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Show newly selected additional images
                        if (localAdditionalImageUris.isNotEmpty()) {
                            Text(
                                "New Additional Images:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                localAdditionalImageUris.forEachIndexed { index, uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "New additional image $index",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        IconButton(
                                            onClick = {
                                                localAdditionalImageUris = localAdditionalImageUris.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove new image",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { additionalImagesPicker.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add More Images")
                            }

                            if (localAdditionalImageUris.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { localAdditionalImageUris = emptyList() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Clear New")
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            prefix = { Text("$") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            label = { Text("Discount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            suffix = { Text("%") },
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Product Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            var categoryExpanded by remember { mutableStateOf(false) }
                            val categories = mapOf(
                                "audio" to Icons.Default.Headset,
                                "gaming" to Icons.Default.Games,
                                "mobile" to Icons.Default.Smartphone,
                                "tv" to Icons.Default.Tv,
                                "laptop" to Icons.Default.Computer,
                                "tablet" to Icons.Default.TabletMac,
                                "headphone" to Icons.Default.Headphones,
                                "accessories" to Icons.Default.Cable
                            )

                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category *") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    singleLine = true
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                    modifier = Modifier.exposedDropdownSize()
                                ) {
                                    categories.forEach { (categoryName, icon) ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(categoryName.capitalize())
                                                }
                                            },
                                            onClick = {
                                                category = categoryName
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = sales,
                            onValueChange = { sales = it },
                            label = { Text("Sales") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            if (showError) {
                Text(
                    "Please fill all required fields!",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (uploadError != null) {
                Text(
                    uploadError ?: "Unknown error occurred",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (title.isBlank() || price.isBlank() || description.isBlank() ||
                            brand.isBlank() || model.isBlank() || color.isBlank() ||
                            category.isBlank() || stock.isBlank()
                        ) {
                            showError = true
                        } else {
                            showError = false
                            isUploading = true
                            coroutineScope.launch {
                                try {
                                    // Handle main image update if needed
                                    val finalMainImageUrl = if (localMainImageUri != null) {
                                        cloudinaryService.uploadImage(localMainImageUri!!).getOrNull() ?: mainImage
                                    } else {
                                        mainImage
                                    }

                                    // Handle additional images update
                                    val finalAdditionalImages = additionalImages.toMutableList()

                                    // Upload any new additional images
                                    for (uri in localAdditionalImageUris) {
                                        val result = cloudinaryService.uploadImage(uri)
                                        val imgUrl = result.getOrNull()
                                        if (imgUrl != null) {
                                            finalAdditionalImages.add(imgUrl)
                                        } else {
                                            uploadError = "Failed to upload additional image"
                                            isUploading = false
                                            return@launch
                                        }
                                    }

                                    // Update product
                                    val updatedProduct = product.copy(
                                        title = title,
                                        image = finalMainImageUrl,
                                        images = finalAdditionalImages,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        description = description,
                                        brand = brand,
                                        model = model,
                                        color = color,
                                        category = category,
                                        discount = discount.toDoubleOrNull() ?: 0.0,
                                        stock = stock.toIntOrNull() ?: 0,
                                        sales = sales.toIntOrNull() ?: 0,
                                        status = status,
                                        updatedAt = Timestamp.now()
                                    )

                                    viewModel.updateProduct(updatedProduct)
                                    isUploading = false
                                    Toast.makeText(
                                        context,
                                        "Product updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    isUploading = false
                                    uploadError = "Failed to update: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update")
                }
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {} // Prevents clicks through the overlay
                    .background(Color.Black.copy(alpha = 0.5f)), // Semi-transparent dark background
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

