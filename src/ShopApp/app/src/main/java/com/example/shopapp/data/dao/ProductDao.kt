package com.example.shopapp.data.dao

import com.example.shopapp.data.model.Product
import com.example.shopapp.data.model.Review
import kotlinx.coroutines.flow.Flow

interface ProductDao {
    suspend fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(productId: String): Product?
    suspend fun addProduct(product: Product): Boolean
    suspend fun updateProduct(product: Product): Boolean
    suspend fun deleteProduct(productId: String): Boolean
    suspend fun searchProductsByKeyword(keyword: String): Flow<List<Product>>

    //pagination optimize
    suspend fun getProductsPage(limit: Int, offset: Int): Flow<List<Product>>
    suspend fun getProductsPageByCategory(categoryId: String, limit: Int, offset: Int): Flow<List<Product>>
    suspend fun searchProductsPageByKeyword(keyword: String, limit: Int, offset: Int): Flow<List<Product>>
    suspend fun getTotalProductsCount(): Int
    suspend fun getTotalProductsCountByCategory(categoryId: String): Int
    suspend fun getTotalProductsCountByKeyword(keyword: String): Int

    // review
    suspend fun addReviewToProduct(productId: String, review: Review): Boolean {
        return false
    }

    suspend fun removeReviewFromProduct(productId: String, review: Review): Boolean {
        return false
    }

    // search by title
    suspend fun searchProductsByTitle(query: String, limit: Int, offset: Int): Flow<List<Product>>
    suspend fun getTotalProductsCountByTitle(query: String): Int
}
