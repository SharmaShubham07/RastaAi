package com.example.rastaai.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.data.local.db.CategoryEntity
import com.example.rastaai.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val repo: CourseRepository
) : ViewModel() {

    // Search query
    private val _query = MutableStateFlow("")

    // Selected category (null = All categories)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)

    // Loading state for screen
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Categories (offline-first)
    val categories: StateFlow<List<CategoryEntity>> =
        repo.getCategoriesFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Courses (search + filter combination)
    val courses: StateFlow<List<CourseEntity>> =
        combine(
            _query.debounce(200),
            _selectedCategoryId,
            repo.getAllCourses()
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        ) { query, selectedCategory, allCourses ->

            var filtered = allCourses

            // Search filter
            if (query.isNotBlank()) {
                val q = query.trim().lowercase()
                filtered = filtered.filter {
                    it.title.lowercase().contains(q) ||
                            it.description.lowercase().contains(q)
                }
            }

            // Category filter
            selectedCategory?.let { catId ->
                filtered = filtered.filter { it.categoryId == catId }
            }

            filtered
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Load categories (online → offline fallback)
        viewModelScope.launch {
            _isLoading.value = true
            repo.refreshCategories() // If offline, will silently fail & use DB categories
            _isLoading.value = false
        }
    }

    // → Called from SearchView
    fun setQuery(value: String) {
        _query.value = value
    }

    // → Called when selecting a category
    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    // → Load a single course by ID
    suspend fun getCourse(id: Long): CourseEntity? {
        return repo.getCourse(id)
    }

    // → Delete a course with callback
    fun deleteCourse(course: CourseEntity, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteCourse(course)
                onComplete(Result.success(Unit))
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    // → Pull latest categories from API
    fun refreshCategories(onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repo.refreshCategories()
            onComplete(result)
        }
    }
}
