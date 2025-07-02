package com.example.shopapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.User
import com.example.shopapp.data.repository.IRepository
import com.example.shopapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: IRepository<User>) : ViewModel() {
    private val TAG = "UserViewModel"

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _pageSize = MutableStateFlow(4) // Default page size
    val pageSize: StateFlow<Int> = _pageSize

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus

    private val _currentSearchKeyword = MutableStateFlow("")
    val currentSearchKeyword: StateFlow<String> = _currentSearchKeyword

    private val _selectedUser = mutableStateOf<User?>(null)
    var selectedUser: User?
        get() = _selectedUser.value
        set(value) {
            _selectedUser.value = value
        }

    init {
        loadInitialUsers()
    }

    fun loadInitialUsers() {
        viewModelScope.launch {
            _isLoading.value = true

            if (repository is UserRepository) {
                val userDao = repository.getUserDao()
                _totalCount.value = userDao.getTotalUsersCount()

                userDao.getUsersPage(_pageSize.value, 0).collect { userList ->
                    _users.value = userList
                    _currentPage.value = 0
                    _hasMoreData.value = userList.size >= _pageSize.value
                    _isLoading.value = false

                    Log.d(TAG, "Loaded initial page with ${userList.size} users")
                }
            } else {
                // Fallback to non-paginated approach
                repository.fetchAll().collect { userList ->
                    _users.value = userList
                    _isLoading.value = false
                    _hasMoreData.value = false

                    Log.d(TAG, "Loaded all ${userList.size} users (non-paginated)")
                }
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value || !_hasMoreData.value) {
            Log.d(TAG, "Skipping loadNextPage: isLoading=${_isLoading.value}, hasMoreData=${_hasMoreData.value}")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val nextPage = _currentPage.value + 1
            val offset = nextPage * _pageSize.value

            Log.d(TAG, "Loading page $nextPage with offset $offset")

            if (repository is UserRepository) {
                val userDao = repository.getUserDao()

                if (_selectedStatus.value != null) {
                    userDao.searchUserByStatus(
                        _selectedStatus.value!!,
                        _pageSize.value,
                        offset
                    ).collect { results ->
                        handlePageResults(results, nextPage)
                    }
                } else if (_isSearching.value) {
                    userDao.searchUsersByKeyword(_currentSearchKeyword.value, _pageSize.value, offset)
                        .collect { results ->
                            handlePageResults(results, nextPage)
                        }
                } else {
                    userDao.getUsersPage(_pageSize.value, offset).collect { results ->
                        handlePageResults(results, nextPage)
                    }
                }
            } else {
                _hasMoreData.value = false
                _isLoading.value = false
            }
        }
    }

    fun previousPage() {
        if (currentPage.value > 0) {
            viewModelScope.launch {
                _isLoading.value = true
                val prevPage = _currentPage.value - 1
                val offset = prevPage * _pageSize.value

                if (repository is UserRepository) {
                    val userDao = repository.getUserDao()

                    if (_selectedStatus.value != null) {
                        userDao.searchUserByStatus(
                            _selectedStatus.value!!,
                            _pageSize.value,
                            offset
                        ).collect { results ->
                            _users.value = results
                            _currentPage.value = prevPage
                            _hasMoreData.value = true
                            _isLoading.value = false
                        }
                    } else if (_isSearching.value) {
                        // Use the stored search keyword
                        userDao.searchUsersByKeyword(_currentSearchKeyword.value, _pageSize.value, offset)
                            .collect { results ->
                                _users.value = results
                                _currentPage.value = prevPage
                                _hasMoreData.value = true
                                _isLoading.value = false
                            }
                    } else {
                        userDao.getUsersPage(_pageSize.value, offset)
                            .collect { results ->
                                _users.value = results
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

                if (repository is UserRepository) {
                    val userDao = repository.getUserDao()

                    if (_selectedStatus.value != null) {
                        userDao.searchUserByStatus(
                            _selectedStatus.value!!,
                            _pageSize.value,
                            offset
                        ).collect { results ->
                            _users.value = results
                            _currentPage.value = page
                            _hasMoreData.value = results.size >= _pageSize.value
                            _isLoading.value = false
                        }
                    } else if (_isSearching.value) {
                        // Use the stored search keyword
                        userDao.searchUsersByKeyword(_currentSearchKeyword.value, _pageSize.value, offset)
                            .collect { results ->
                                _users.value = results
                                _currentPage.value = page
                                _hasMoreData.value = results.size >= _pageSize.value
                                _isLoading.value = false
                            }
                    } else {
                        userDao.getUsersPage(_pageSize.value, offset)
                            .collect { results ->
                                _users.value = results
                                _currentPage.value = page
                                _hasMoreData.value = results.size >= _pageSize.value
                                _isLoading.value = false
                            }
                    }
                }
            }
        }
    }

    private fun handlePageResults(results: List<User>, page: Int) {
        if (results.isNotEmpty()) {
            _users.value = results
            _currentPage.value = page
            _hasMoreData.value = results.size >= _pageSize.value
        } else {
            _hasMoreData.value = false
        }
        _isLoading.value = false

        Log.d(TAG, "Loaded page $page with ${results.size} users")
    }

    fun searchUsers(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSearching.value = true
            _currentSearchKeyword.value = keyword

            if (repository is UserRepository) {
                val userDao = repository.getUserDao()
                _totalCount.value = userDao.getTotalUsersCountByKeyword(keyword)

                userDao.searchUsersByKeyword(keyword, _pageSize.value, 0).collect { results ->
                    _users.value = results
                    _searchResults.value = results
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false

                    Log.d(TAG, "Search found ${results.size} users for '$keyword'")
                }
            } else {
                // Fallback to in-memory filtering
                val filteredUsers = _users.value.filter { user ->
                    user.username.contains(keyword, ignoreCase = true) ||
                            user.email.contains(keyword, ignoreCase = true) ||
                            user.userId.contains(keyword, ignoreCase = true)
                }
                _searchResults.value = filteredUsers
                _isSearching.value = false
                _isLoading.value = false
            }
        }
    }

    fun filterByStatus(status: String?) {
        viewModelScope.launch {
            _selectedStatus.value = status
            _users.value = emptyList()
            _currentPage.value = -1
            _hasMoreData.value = true
            _isLoading.value = true

            if (repository is UserRepository && status != null) {
                val userDao = repository.getUserDao()
                _totalCount.value = userDao.getTotalUsersCountByStatus(status)

                userDao.searchUserByStatus(status, _pageSize.value, 0).collect { results ->
                    _users.value = results
                    _currentPage.value = 0
                    _hasMoreData.value = results.size >= _pageSize.value
                    _isLoading.value = false

                    Log.d(TAG, "Status filter found ${results.size} users for '$status'")
                }
            } else if (status == null) {
                // Reset to show all users
                loadInitialUsers()
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
        _users.value = emptyList()
        _currentPage.value = -1
        _hasMoreData.value = true
        loadNextPage()
    }

    fun addUser(user: User, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.create(user)) {
                loadInitialUsers()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun updateUser(user: User, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.modify(user)) {
                loadInitialUsers()
                if (selectedUser?.userId == user.userId) {
                    selectedUser = user
                }
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun deleteUser(userId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.remove(userId)) {
                loadInitialUsers()
                if (selectedUser?.userId == userId) {
                    selectedUser = null
                }
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun selectUser(user: User) {
        selectedUser = user
    }

    fun resetFiltersAndSearch() {
        _searchResults.value = emptyList()
        _isSearching.value = false
        _selectedStatus.value = null
        _currentPage.value = 0
        _hasMoreData.value = true
        _currentSearchKeyword.value = ""

        loadInitialUsers()

        Log.d(TAG, "Filters and search reset")
    }

    fun getRepositoryForOperations(): UserRepository? {
        return repository as? UserRepository
    }

    suspend fun getUserById(userId: String): User? {
        return repository.fetchById(userId)
    }
}