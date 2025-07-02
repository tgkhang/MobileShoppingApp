package com.example.shopapp.ui.admin.products

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopapp.data.model.Product
import com.example.shopapp.services.CloudinaryService
import com.example.shopapp.viewmodel.ProductViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, viewModel: ProductViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val cloudinaryService = remember { CloudinaryService(context) }

    var title by remember { mutableStateOf("") }
    var mainImage by remember { mutableStateOf("") }
    var additionalImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var sales by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }
    var showError by remember { mutableStateOf(false) }

    var localMainImageUri by remember { mutableStateOf<Uri?>(null) }
    var localAdditionalImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // File picker for multiple images
    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            localAdditionalImageUris = uris
        }
    }

    // File picker for single main image
    val mainImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            localMainImageUri = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
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

                    // Main Product Image
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Main Product Image *",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (localMainImageUri != null) {
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { mainImagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Click to select main image")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { mainImagePicker.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Select Image")
                            }

                            if (localMainImageUri != null) {
                                OutlinedButton(
                                    onClick = { localMainImageUri = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Clear")
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

                        if (localAdditionalImageUris.isNotEmpty()) {
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
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Additional image $index",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Remove image button
                                        IconButton(
                                            onClick = {
                                                localAdditionalImageUris = localAdditionalImageUris
                                                    .filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(50)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove image",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { multipleImagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Click to select additional images")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { multipleImagePicker.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Select Images")
                            }

                            if (localAdditionalImageUris.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { localAdditionalImageUris = emptyList() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Clear All")
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
                            // Existing save logic
                            showError = false
                            isUploading = true
                            coroutineScope.launch {
                                try {
                                    // Upload main image first
                                    val mainImageUrl = cloudinaryService.uploadImage(localMainImageUri!!)

                                    // Upload additional images if present
                                    val additionalImageUrls = mutableListOf<String>()
                                    for (uri in localAdditionalImageUris) {
                                        val result = cloudinaryService.uploadImage(uri)
                                        val imgUrl = result.getOrNull()
                                        if (imgUrl != null) {
                                            additionalImageUrls.add(imgUrl)
                                        } else {
                                            uploadError = "Failed to upload additional image"
                                            isUploading = false
                                            return@launch
                                        }
                                    }

                                    // Create product with all images
                                    val product = Product(
                                        productId = UUID.randomUUID().toString(),
                                        title = title,
                                        image = mainImageUrl.getOrNull() ?: "",  // Main image
                                        images = additionalImageUrls,  // Additional images list
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        description = description,
                                        brand = brand,
                                        model = model,
                                        color = color,
                                        category = category,
                                        popular = false,
                                        discount = discount.toDoubleOrNull() ?: 0.0,
                                        stock = stock.toIntOrNull() ?: 0,
                                        sales = sales.toIntOrNull() ?: 0,
                                        status = status,
                                        review = emptyList(),
                                        createdAt = Timestamp.now(),
                                        updatedAt = Timestamp.now()
                                    )

                                    viewModel.addProduct(product)
                                    delay(1000)  // Allow time for the database operation
                                    isUploading = false
                                    Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    isUploading = false
                                    uploadError = "Failed to upload: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {}
                    .background(Color.Black.copy(alpha = 0.5f)),
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