package com.example.shopapp.data.repository

import android.util.Log
import com.example.shopapp.data.dao.ProductDao
import com.example.shopapp.data.model.Product
import com.example.shopapp.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val productDao: ProductDao
) : IRepository<Product> {
    private val TAG = "ProductRepository"
    override suspend fun fetchAll(): Flow<List<Product>> = flow {
        productDao.getAllProducts().collect { productList -> emit(productList) }
    }

    override suspend fun fetchById(id: String): Product? {
        return productDao.getProductById(id)
    }

    override suspend fun create(item: Product): Boolean {
        return productDao.addProduct(item)
    }

    override suspend fun modify(item: Product): Boolean {
        return productDao.updateProduct(item)
    }

    override suspend fun remove(id: String): Boolean {
        return productDao.deleteProduct(id)
    }

    suspend fun search(keyword: String): Flow<List<Product>> = flow {
        productDao.searchProductsByKeyword(keyword).collect { productList -> emit(productList) }
    }

    suspend fun fetchPage(limit: Int, offset: Int): Flow<List<Product>> = flow {
        productDao.getProductsPage(limit, offset).collect { productList -> emit(productList) }
    }

    suspend fun fetchPageByCategory(categoryId: String, limit: Int, offset: Int): Flow<List<Product>> = flow {
        productDao.getProductsPageByCategory(categoryId, limit, offset).collect { productList -> emit(productList) }
    }

    suspend fun searchByKeywordPaginated(keyword: String, limit: Int, offset: Int): Flow<List<Product>> = flow {
        productDao.searchProductsPageByKeyword(keyword, limit, offset).collect { productList -> emit(productList) }
    }

    suspend fun getTotalCount(): Int {
        return productDao.getTotalProductsCount()
    }

    suspend fun getCategoryTotalCount(categoryId: String): Int {
        return productDao.getTotalProductsCountByCategory(categoryId)
    }

    suspend fun getSearchResultCount(keyword: String): Int {
        return productDao.getTotalProductsCountByKeyword(keyword)
    }

    suspend fun addReview(productId: String, review: Review): Boolean {
        return productDao.addReviewToProduct(productId, review)
    }

    suspend fun removeReview(productId: String, review: Review): Boolean {
        return productDao.removeReviewFromProduct(productId, review)
    }

    suspend fun searchProductsByTitle(query: String, limit: Int, offset: Int): Flow<List<Product>> {
        return productDao.searchProductsByTitle(query, limit, offset)
    }

    suspend fun getTotalProductsCountByTitle(query: String): Int {
        return productDao.getTotalProductsCountByTitle(query)
    }

    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)
    }
}
