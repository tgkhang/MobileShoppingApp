package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.Product
import com.example.shopapp.data.repository.IRepository
import com.example.shopapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val productRepository: IRepository<Product>
) : ViewModel() {

    // Search related state
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    // Filter related state
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _priceRange = MutableStateFlow(0f..10000f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange

    private val _minRating = MutableStateFlow(0)
    val minRating: StateFlow<Int> = _minRating

    // Product categories for filter
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    // Filtered results
    private val _filteredResults = MutableStateFlow<List<Product>>(emptyList())
    val filteredResults: StateFlow<List<Product>> = _filteredResults

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            if(productRepository is ProductRepository){
                productRepository.fetchAll().collect { products ->
                    val categoryList = products
                        .map { it.category }
                        .distinct()
                        .sorted()
                    _categories.value = categoryList
                }
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            if (productRepository is ProductRepository)
            {
                productRepository.search(query).collect { results ->
                    _searchResults.value = results
                    applyFilters()
                    _isSearching.value = false
                }
            }
        }
    }

    // Filter functions
    fun setCategory(category: String?) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun setPriceRange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
        applyFilters()
    }

    fun setMinRating(rating: Int) {
        _minRating.value = rating
        applyFilters()
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _priceRange.value = 0f..10000f
        _minRating.value = 0
        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = _searchResults.value.filter { product ->
            // Apply category filter
            val categoryMatch = _selectedCategory.value == null ||
                    product.category == _selectedCategory.value

            // Apply price filter
            val priceMatch = product.price in _priceRange.value.start.toDouble().._priceRange.value.endInclusive.toDouble()

            // Apply rating filter
            val avgRating = if (product.review.isEmpty()) 0.0
            else product.review.map { it.rating }.average()
            val ratingMatch = avgRating >= _minRating.value

            categoryMatch && priceMatch && ratingMatch
        }

        _filteredResults.value = filteredList
    }
}