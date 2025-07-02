package com.example.shopapp.data.dao

import android.util.Log
import com.example.shopapp.data.model.Product
import com.example.shopapp.data.model.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.firestore.Query

class FirebaseProductDao : ProductDao {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")
    private val TAG = "FirebaseProductDao"

    // Helper function to parse reviews from Firestore document
    private fun parseReviews(reviewList: List<Map<String, Any>>?): List<Review> {
        return reviewList?.mapNotNull { map ->
            try {
                Review(
                    reviewId = map["reviewId"] as? String ?: UUID.randomUUID().toString(),
                    userId = map["userId"] as? String ?: "",
                    rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                    comment = map["comment"] as? String ?: "",
                    createdAt = map["createdAt"] as? Timestamp,
                    updatedAt = map["updatedAt"] as? Timestamp
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing review: ${e.message}")
                null
            }
        } ?: emptyList()
    }


    // Helper function to convert Firestore document to Product with reviews
    private fun documentToProduct(doc: com.google.firebase.firestore.DocumentSnapshot): Product? {
        val product = doc.toObject(Product::class.java) ?: return null
        val reviewList = doc.get("review") as? List<Map<String, Any>> ?: emptyList()
        val parsedReviews = parseReviews(reviewList)
        return product.copy(review = parsedReviews)
    }

    override suspend fun getAllProducts(): Flow<List<Product>> = flow {
        try {
            val snapshot = db.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                val product = doc.toObject(Product::class.java)

                // Manually parse the "review" field
                val reviewList = doc.get("review") as? List<Map<String, Any>> ?: emptyList()
                val parsedReviews = reviewList.mapNotNull { map ->
                    try {
                        Review(
                            reviewId = map["reviewId"] as? String ?: UUID.randomUUID().toString(),
                            userId = map["userId"] as? String ?: "",
                            rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                            comment = map["comment"] as? String ?: "",
                            createdAt = map["createdAt"] as? com.google.firebase.Timestamp,
                            updatedAt = map["updatedAt"] as? com.google.firebase.Timestamp
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing review: ${e.message}")
                        null
                    }
                }

                product?.copy(review = parsedReviews)
            }

            Log.d("ProductRepository", "Total products fetched: ${products.size}")
            emit(products)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error fetching products: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = productsCollection.document(productId).get().await()
            documentToProduct(doc)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product by ID: ${e.message}")
            null
        }
    }

    override suspend fun addProduct(product: Product): Boolean {
        return try {
            db.collection("products").document(product.productId).set(product).await()
            Log.d(TAG, "Product added successfully: ${product.productId}")
            true
        } catch (e: Exception) {
            Log.e(TAG,"Error adding product",e)
            false
        }
    }

    override suspend fun updateProduct(product: Product): Boolean {
        return try {
            db.collection("products")
                .document(product.productId)
                .set(product)
                .await()
            Log.d(TAG, "Product updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product", e)
            false
        }
    }

    override suspend fun deleteProduct(productId: String): Boolean {
        return try {
            db.collection("products").document(productId).delete().await()
            Log.d(TAG, "Product deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product", e)
            false
        }
    }

    override suspend fun searchProductsByKeyword(keyword: String): Flow<List<Product>> = flow {
        try {
            val lowercaseKeyword = keyword.lowercase()
            val capitalizedKeyword = keyword.replaceFirstChar { it.uppercase() }
            val uppercaseKeyword = keyword.uppercase()
            val matchingProducts = mutableListOf<Product>()
            val productIds = mutableSetOf<String>()

            // Function to process a single character prefix search
            suspend fun searchByFirstChar(field: String) {
                if (lowercaseKeyword.isEmpty()) return

                // Get the first character and search for all products starting with it
                val firstChar = lowercaseKeyword.first().toString()
                val firstCharCapitalized = firstChar.uppercase()

                // Search with lowercase first char
                val lowerResults = productsCollection
                    .whereGreaterThanOrEqualTo(field, firstChar)
                    .whereLessThanOrEqualTo(field, firstChar + "\uf8ff")
                    .get().await()

                // Search with uppercase first char
                val upperResults = productsCollection
                    .whereGreaterThanOrEqualTo(field, firstCharCapitalized)
                    .whereLessThanOrEqualTo(field, firstCharCapitalized + "\uf8ff")
                    .get().await()

                // Process results and filter client-side
                for (snapshot in listOf(lowerResults, upperResults)) {
                    snapshot.documents.forEach { doc ->
                        if (!productIds.contains(doc.id)) {
                            documentToProduct(doc)?.let { product ->
                                val fieldValue = when (field) {
                                    "title" -> product.title
                                    "description" -> product.description
                                    "category" -> product.category
                                    else -> ""
                                }

                                // Case insensitive contains check
                                if (fieldValue.lowercase().contains(lowercaseKeyword)) {
                                    productIds.add(doc.id)
                                    matchingProducts.add(product)
                                }
                            }
                        }
                    }
                }
            }
            // Search in each field
            searchByFirstChar("title")
            searchByFirstChar("description")
            searchByFirstChar("category")

            Log.d(TAG, "Search results for '$keyword': ${matchingProducts.size}")
            emit(matchingProducts)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getProductsPage(limit: Int, offset: Int): Flow<List<Product>> = flow {
        try {
            // Firestore doesn't support direct offset,using startAfter
            // first page (offset = 0)
            val query = if (offset == 0) {
                productsCollection.orderBy("productId").limit(limit.toLong())
            } else {
                // For subsequent pages,  get the last document of the previous page
                val lastVisibleDocSnapshot = productsCollection
                    .orderBy("productId")
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    productsCollection
                        .orderBy("productId")
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    //return an empty list if can not find last document
                    emit(emptyList<Product>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val products = snapshot.documents.mapNotNull { documentToProduct(it) }

            Log.d(TAG, "Fetched page with limit=$limit, offset=$offset: ${products.size} products")
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products page: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getProductsPageByCategory(
        categoryId: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        try {
            val baseQuery = productsCollection.whereEqualTo("category", categoryId)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Product>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val products = snapshot.documents.mapNotNull { documentToProduct(it) }
            Log.d(TAG, "Fetched category=$categoryId page with limit=$limit, offset=$offset: ${products.size} products")
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products page by category: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun searchProductsPageByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        try {
            val baseQuery = productsCollection
                .whereGreaterThanOrEqualTo("title", keyword)
                .whereLessThanOrEqualTo("title", keyword + "\uf8ff")
                .orderBy("title", Query.Direction.ASCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Product>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val products = snapshot.documents.mapNotNull { documentToProduct(it) }

            Log.d(TAG, "Searched '$keyword' page with limit=$limit, offset=$offset: ${products.size} products")
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products page: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getTotalProductsCount(): Int {
        return try {
            val snapshot = productsCollection.get().await()
            val count = snapshot.size()
            Log.d(TAG, "Total products count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total products count: ${e.message}")
            0
        }
    }

    override suspend fun getTotalProductsCountByCategory(categoryId: String): Int {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("category", categoryId)
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total products count for category $categoryId: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category products count: ${e.message}")
            0
        }
    }

    override suspend fun getTotalProductsCountByKeyword(keyword: String): Int {
        return try {
            val snapshot = productsCollection
                .whereGreaterThanOrEqualTo("title", keyword)
                .whereLessThanOrEqualTo("title", keyword + "\uf8ff")
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total products count for keyword '$keyword': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting keyword products count: ${e.message}")
            0
        }
    }

    // Hàm để thêm/xóa review
    override suspend fun addReviewToProduct(productId: String, review: Review): Boolean {
        return try {
            val productRef = productsCollection.document(productId)
            productRef.update("review", FieldValue.arrayUnion(review)).await()
            Log.d(TAG, "Review added to product $productId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding review: ${e.message}")
            false
        }
    }

    override suspend fun removeReviewFromProduct(productId: String, review: Review): Boolean {
        return try {
            val productRef = productsCollection.document(productId)
            productRef.update("review", FieldValue.arrayRemove(review)).await()
            Log.d(TAG, "Review removed from product $productId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing review: ${e.message}")
            false
        }
    }

    // Search by title
    override suspend fun searchProductsByTitle(
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        try {
            val baseQuery = productsCollection
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .orderBy("title", Query.Direction.ASCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Product>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val products = snapshot.documents.mapNotNull { documentToProduct(it) }

            Log.d(TAG, "Searched by title '$query' page with limit=$limit, offset=$offset: ${products.size} products")
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products by title: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getTotalProductsCountByTitle(query: String): Int {
        return try {
            val snapshot = productsCollection
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total products count for title '$query': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total products count by title: ${e.message}")
            0
        }
    }
}