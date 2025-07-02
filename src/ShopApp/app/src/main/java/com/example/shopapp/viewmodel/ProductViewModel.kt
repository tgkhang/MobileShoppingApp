package com.example.shopapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.Product
import com.example.shopapp.data.model.Review
import com.example.shopapp.data.repository.IRepository
import com.example.shopapp.data.repository.ProductRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProductViewModel(
    private val repository: IRepository<Product>,
    private val userId: String
) : ViewModel() {
    private val TAG = "ProductViewmodel"

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    private val _currentPage= MutableStateFlow(0)
    val currentPage : StateFlow<Int> = _currentPage

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _pageSize = MutableStateFlow(8) // Default page size
    val pageSize: StateFlow<Int> = _pageSize

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedProduct = mutableStateOf<Product?>(null)

    // StateFlow cho averageRating và reviewCount của sản phẩm được chọn
    private val _averageRating = MutableStateFlow("0.0")
    val averageRating: StateFlow<String> = _averageRating

    private val _reviewCount = MutableStateFlow(0)
    val reviewCount: StateFlow<Int> = _reviewCount

    var selectedProduct: Product?
        get() = _selectedProduct.value
        set(value) {
            _selectedProduct.value = value
            value?.let { updateRatingAndCount(it) }
        }

    init {
        //loadProducts()
        loadInitialProducts()
    }
    fun loadInitialProducts()
    {
        viewModelScope.launch {
            _isLoading.value= true

            if(repository is ProductRepository){
                _totalCount.value= repository.getTotalCount()

                repository.fetchPage(_pageSize.value, 0).collect { productList ->
                    _products.value = productList
                    _currentPage.value = 0
                    _hasMoreData.value = productList.size >= _pageSize.value
                    _isLoading.value = false

                    Log.d("ProductViewModel", "Loaded initial page with ${productList.size} products")
                }

            }
            else{
                // non product repository implement
                repository.fetchAll().collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                    _hasMoreData.value = false

                    Log.d("ProductViewModel", "Loaded all ${productList.size} products (non-paginated)")
                }
            }
        }
    }
    fun loadNextPage() {
        if (_isLoading.value || !_hasMoreData.value) {
            Log.d("ProductViewModel", "Skipping loadNextPage: isLoading=${_isLoading.value}, hasMoreData=${_hasMoreData.value}")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val nextPage = _currentPage.value + 1
            val offset = nextPage * _pageSize.value

            Log.d("ProductViewModel", "Loading page $nextPage with offset $offset")

            if (repository is ProductRepository) {
                if (_selectedCategory.value != null) {
                    repository.fetchPageByCategory(
                        _selectedCategory.value!!,
                        _pageSize.value,
                        offset
                    ).collect { results ->
                        handlePageResults(results, nextPage)
                    }
                } else {
                    repository.fetchPage(_pageSize.value, offset)
                        .collect { results ->
                            handlePageResults(results, nextPage)
                        }
                }
            } else {
                _hasMoreData.value = false
                _isLoading.value = false
            }
        }
    }

    fun handlePageResults(results: List<Product>, page:Int){
        if (results.isNotEmpty()) {
            val currentList = _products.value.toMutableList()
            currentList.addAll(results)
            _products.value = currentList
            _currentPage.value = page
            _hasMoreData.value = results.size >= _pageSize.value
        } else {
            _hasMoreData.value = false
        }
        _isLoading.value = false

        Log.d("ProductViewModel", "Loaded page $page with ${results.size} products")
    }

    fun loadProducts() {
        viewModelScope.launch {
            repository.fetchAll().collect { productList ->
                Log.d("ProductViewModel", "ViewModel received ${productList.size} products")
                _products.value = productList
            }
        }
    }
    fun setPageSize(size: Int) {
        if (size != _pageSize.value) {
            _pageSize.value = size
            resetAndReload()
        }
    }
    private fun resetAndReload() {
        _products.value = emptyList()
        _currentPage.value = -1
        _hasMoreData.value = true
        loadNextPage()
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isSearching.value = true

            if (repository is ProductRepository) {
                repository.search(query).collect { results ->
                    _searchResults.value = results
                    _isSearching.value = false
                    Log.d("ProductViewModel", "Search found ${results.size} products for '$query'")
                }
            } else {
                // Fallback to filtering in-memory if repository doesn't support search
                val filteredProducts = _products.value.filter { product ->
                    product.title.contains(query, ignoreCase = true) ||
                            product.description.contains(query, ignoreCase = true) ||
                            product.brand.contains(query, ignoreCase = true) ||
                            product.category.contains(query, ignoreCase = true)
                }
                _searchResults.value = filteredProducts
                _isSearching.value = false
            }
        }
    }

    fun filterByCategory(categoryId: String?) {
        Log.d("pr", "filterByCategory called with categoryId: $categoryId")
        viewModelScope.launch {
            _selectedCategory.value = categoryId
            _products.value = emptyList()
            _currentPage.value = -1
            _hasMoreData.value = true
            _isLoading.value = true

            if (repository is ProductRepository && categoryId != null) {
                // Get count of products in category first
                _totalCount.value = repository.getCategoryTotalCount(categoryId)

                // Load first page of category products
                repository.fetchPageByCategory(categoryId, _pageSize.value, 0).collect { results ->
                    _products.value = results
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false

                    Log.d("ProductViewModel", "Category filter found ${results.size} products for '$categoryId'")
                }
            } else if (categoryId == null) {
                // Reset to show all products
                loadInitialProducts()
            }
        }
    }

    fun refreshProducts() {
        _products.value = emptyList()
        _currentPage.value = -1
        _hasMoreData.value = true
        _selectedCategory.value = null
        loadNextPage()
    }

    fun deleteProduct(product: Product, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.remove(product.productId)) {
                loadInitialProducts()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun addProduct(product: Product, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.create(product)) {
                loadInitialProducts()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun selectProduct(product: Product) {
        selectedProduct = product
    }

    fun updateProduct(product: Product, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.modify(product)) {
                loadInitialProducts()
                selectedProduct = product
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun nextPage(){
        if (_hasMoreData.value) {
            viewModelScope.launch {
                _isLoading.value = true
                val nextPage = _currentPage.value + 1
                val offset = nextPage * _pageSize.value

                if (repository is ProductRepository) {
                    if (_selectedCategory.value != null) {
                        repository.fetchPageByCategory(
                            _selectedCategory.value!!,
                            _pageSize.value,
                            offset
                        ).collect { results ->
                            _products.value = results
                            _currentPage.value = nextPage
                            _hasMoreData.value = results.size >= _pageSize.value
                            _isLoading.value = false
                        }
                    } else {
                        repository.fetchPage(_pageSize.value, offset)
                            .collect { results ->
                                _products.value = results
                                _currentPage.value = nextPage
                                _hasMoreData.value = results.size >= _pageSize.value
                                _isLoading.value = false
                            }
                    }
                }
            }
        }
    }

    fun previousPage() {
        if (currentPage.value > 0) {
            viewModelScope.launch {
                _isLoading.value = true
                val prevPage = _currentPage.value - 1
                val offset = prevPage * _pageSize.value

                if (repository is ProductRepository) {
                    if (_selectedCategory.value != null) {
                        repository.fetchPageByCategory(
                            _selectedCategory.value!!,
                            _pageSize.value,
                            offset
                        ).collect { results ->
                            _products.value = results
                            _currentPage.value = prevPage
                            _hasMoreData.value = true
                            _isLoading.value = false
                        }
                    } else {
                        repository.fetchPage(_pageSize.value, offset)
                            .collect { results ->
                                _products.value = results
                                _currentPage.value = prevPage
                                _hasMoreData.value = true
                                _isLoading.value = false
                            }
                    }
                }
            }
        }
    }

    fun goToPage(page: Int) {
        if (page != _currentPage.value) {
            viewModelScope.launch {
                _isLoading.value = true
                val offset = page * _pageSize.value

                if (repository is ProductRepository) {
                    if (_selectedCategory.value != null) {
                        repository.fetchPageByCategory(
                            _selectedCategory.value!!,
                            _pageSize.value,
                            offset
                        ).collect { results ->
                            _products.value = results
                            _currentPage.value = page
                            _hasMoreData.value = results.size >= _pageSize.value
                            _isLoading.value = false
                        }
                    } else {
                        repository.fetchPage(_pageSize.value, offset)
                            .collect { results ->
                                _products.value = results
                                _currentPage.value = page
                                _hasMoreData.value = results.size >= _pageSize.value
                                _isLoading.value = false
                            }
                    }
                }
            }
        }
    }

    fun searchProductsByTitle(query: String) {
        viewModelScope.launch {
            _isLoading.value = true

            if (repository is ProductRepository) {
                _totalCount.value = repository.getTotalProductsCountByTitle(query)

                repository.searchProductsByTitle(query, _pageSize.value, 0).collect { results ->
                    _products.value = results
                    _isLoading.value = false
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    Log.d("ProductViewModel", "Search found ${results.size} products for '$query'")
                }
            } else {
                loadInitialProducts()
            }
        }
    }

    fun getRepositoryForOrderOperations(): ProductRepository? {
        return repository as? ProductRepository
    }

    fun resetFiltersAndSearch() {
        _searchResults.value = emptyList()
        _isSearching.value = false
        _selectedCategory.value = null
        _currentPage.value = 0
        _hasMoreData.value = true

        loadInitialProducts()

        Log.d("ProductViewModel", "Filters and search reset")
    }

    suspend fun getProductById(productId: String): Product? {
        _isLoading.value = true

        return try {
            if (repository is ProductRepository) {
                val product = repository.fetchById(productId)
                product
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product by id: ${e.message}")
            null
        } finally {
            _isLoading.value = false
        }
    }

    // Hàm cho review
    fun addReview(productId: String, rating: Double, comment: String) { // Thêm userId sau
        viewModelScope.launch {
            val newReview = Review(
                reviewId = UUID.randomUUID().toString(),
                userId = userId,
                rating = rating,
                comment = comment,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            if (repository is ProductRepository) {
                val success = repository.addReview(productId, newReview)
                if (success) {
                    // Cập nhật danh sách sản phẩm hoặc sản phẩm đã chọn
                    val updatedProduct = repository.fetchById(productId)
                    updatedProduct?.let { product ->
                        val currentList = _products.value.toMutableList()
                        val index = currentList.indexOfFirst { it.productId == productId }
                        if (index != -1) {
                            currentList[index] = product
                            _products.value = currentList
                        }
                        if (selectedProduct?.productId == productId) {
                            selectedProduct = product
                        }
                    }
                    Log.d("ProductViewModel", "Review added to product $productId")
                }
            }
        }
    }

    fun removeReview(productId: String, review: Review) {
        viewModelScope.launch {
            if (repository is ProductRepository) {
                val success = repository.removeReview(productId, review)
                if (success) {
                    val updatedProduct = repository.fetchById(productId)
                    updatedProduct?.let { product ->
                        val currentList = _products.value.toMutableList()
                        val index = currentList.indexOfFirst { it.productId == productId }
                        if (index != -1) {
                            currentList[index] = product
                            _products.value = currentList
                        }
                        if (selectedProduct?.productId == productId) {
                            selectedProduct = product
                        }
                    }
                    Log.d("ProductViewModel", "Review removed from product $productId")
                }
            }
        }
    }

    private fun updateRatingAndCount(product: Product) {
        _averageRating.value = if (product.review.isNotEmpty()) {
            String.format("%.1f", product.review.map { it.rating }.average())
        } else {
            "0.0"
        }
        _reviewCount.value = product.review.size
    }
}