package com.example.rastaai.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rastaai.data.local.db.CategoryEntity
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditCourseViewModel @Inject constructor(
    private val repo: CourseRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repo.getCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun loadCourse(id: Long): CourseEntity? = repo.getCourse(id)

    fun saveCourse(
        id: Long?,
        title: String,
        description: String,
        categoryId: Long?,
        categoryName: String?,
        lessons: Int,
        onComplete: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repo.saveCourse(id, title, description, categoryId, categoryName, lessons)
                onComplete(Result.success(Unit))
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    fun refreshCategories(onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repo.refreshCategories()
            onComplete(res)
        }
    }
}
