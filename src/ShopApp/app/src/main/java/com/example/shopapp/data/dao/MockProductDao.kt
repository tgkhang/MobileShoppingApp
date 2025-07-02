package com.example.shopapp.data.dao

import com.example.shopapp.data.model.Product
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class MockProductDao :ProductDao {

    private val productList = mutableListOf<Product>()
    init {
        // Smartphones
        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "Samsung Galaxy S25 Ultra",
                image = "https://picsum.photos/id/1/500/500",
                price = 1199.99,
                description = "The latest flagship smartphone with 6.8-inch Dynamic AMOLED display, 200MP camera, and 16GB RAM.",
                brand = "Samsung",
                model = "Galaxy S25 Ultra",
                color = "Phantom Black",
                category = "Smartphones",
                popular = true,
                discount = 0.0,
                stock = 25,
                sales = 120,
                status = "available",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )

        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "Apple iPhone 16 Pro",
                image = "https://picsum.photos/id/2/500/500",
                price = 1099.99,
                description = "Latest iPhone with A18 Bionic chip, 6.1-inch Super Retina XDR display, and ProMotion technology.",
                brand = "Apple",
                model = "iPhone 16 Pro",
                color = "Sierra Blue",
                category = "Smartphones",
                popular = true,
                discount = 5.0,
                stock = 18,
                sales = 95,
                status = "available",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )

        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "Google Pixel 9",
                image = "https://picsum.photos/id/3/500/500",
                price = 899.99,
                description = "Google's flagship phone with the best camera system, featuring a 50MP main sensor and Google's AI enhancements.",
                brand = "Google",
                model = "Pixel 9",
                color = "Sorta Sage",
                category = "Smartphones",
                popular = false,
                discount = 10.0,
                stock = 12,
                sales = 45,
                status = "available",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )

        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "OnePlus 13",
                image = "https://picsum.photos/id/4/500/500",
                price = 849.99,
                description = "Flagship killer with Snapdragon 8 Gen 3, 120Hz AMOLED display, and 80W fast charging.",
                brand = "OnePlus",
                model = "13",
                color = "Emerald Green",
                category = "Smartphones",
                popular = false,
                discount = 0.0,
                stock = 0,
                sales = 30,
                status = "out of stock",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )

        // Laptops
        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "MacBook Pro 16-inch",
                image = "https://picsum.photos/id/5/500/500",
                price = 2499.99,
                description = "Pro-level laptop with M3 Max chip, 32GB RAM, 1TB SSD, and 16-inch Liquid Retina XDR display.",
                brand = "Apple",
                model = "MacBook Pro 16-inch",
                color = "Space Gray",
                category = "Laptops",
                popular = true,
                discount = 8.0,
                stock = 10,
                sales = 75,
                status = "available",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )

        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "Dell XPS 15",
                image = "https://picsum.photos/id/6/500/500",
                price = 1799.99,
                description = "Premium Windows laptop with 12th Gen Intel Core i9, 32GB RAM, 1TB SSD, and 15.6-inch 4K OLED display.",
                brand = "Dell",
                model = "XPS 15",
                color = "Platinum Silver",
                category = "Laptops",
                popular = true,
                discount = 12.0,
                stock = 8,
                sales = 60,
                status = "available",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )
        productList.add(
            Product(
                productId = UUID.randomUUID().toString(),
                title = "Asus ROG Zephyrus G14",
                image = "https://picsum.photos/id/7/500/500",
                price = 1599.99,
                description = "Gaming laptop with AMD Ryzen 9, RTX 4070, 16GB RAM, and 14-inch 165Hz display.",
                brand = "Asus",
                model = "ROG Zephyrus G14",
                color = "Moonlight White",
                category = "Laptops",
                popular = false,
                discount = 0.0,
                stock = 0,
                sales = 40,
                status = "out of stock",
                review = emptyList(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )
    }


    override suspend fun getAllProducts(): Flow<List<Product>> = flow {
        emit(productList)
    }

    override suspend fun getProductById(productId: String): Product? {
        return productList.find { it.productId == productId }
    }

    override suspend fun addProduct(product: Product): Boolean {
        productList.add(product.copy(productId = UUID.randomUUID().toString()))
        return true
    }

    override suspend fun updateProduct(product: Product): Boolean {
        val index = productList.indexOfFirst { it.productId == product.productId }
        if (index != -1) {
            productList[index] = product
            return true
        }
        return false
    }

    override suspend fun deleteProduct(productId: String): Boolean {
        return productList.removeIf { it.productId == productId }
    }

    override suspend fun searchProductsByKeyword(keyword: String): Flow<List<Product>> = flow {
        emit(productList.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                    it.description.contains(keyword, ignoreCase = true) ||
                    it.brand.contains(keyword, ignoreCase = true)
        })
    }

    override suspend fun getProductsPage(
        limit: Int,
        offset: Int
    ): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getProductsPageByCategory(
        categoryId: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun searchProductsPageByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalProductsCount(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalProductsCountByCategory(categoryId: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalProductsCountByKeyword(keyword: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun searchProductsByTitle(query: String, limit: Int, offset: Int): Flow<List<Product>> {
        return flow {
            emit(productList.filter { it.title.contains(query, ignoreCase = true) })
        }
    }

    override suspend fun getTotalProductsCountByTitle(query: String): Int {
        return productList.count { it.title.contains(query, ignoreCase = true) }
    }
}