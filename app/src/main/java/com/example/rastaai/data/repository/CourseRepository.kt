// data/repository/CourseRepository.kt
package com.example.rastaai.data.repository

import com.example.rastaai.data.local.dao.CategoryDao
import com.example.rastaai.data.local.dao.CourseDao
import com.example.rastaai.data.local.db.CategoryEntity
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.data.remote.CategoryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) {

    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()
    fun searchCourses(query: String) = courseDao.search(query)
    fun filterCoursesByCategory(id: Long) = courseDao.filterByCategory(id)

    suspend fun getCourse(id: Long) = courseDao.getCourse(id)

    suspend fun saveCourse(
        id: Long?,
        title: String,
        description: String,
        categoryId: Long?,
        categoryName: String?,
        lessons: Int
    ) {
        val score = title.length * lessons
        val entity = CourseEntity(
            id = id ?: 0,
            title = title,
            description = description,
            categoryId = categoryId,
            categoryName = categoryName,
            lessons = lessons,
            score = score
        )

        withContext(Dispatchers.IO) {
            if (id == null || id == 0L) {
                courseDao.insert(entity)
            } else {
                val old = courseDao.getCourse(id)
                val updated = entity.copy(createdAt = old?.createdAt ?: System.currentTimeMillis())
                courseDao.update(updated)
            }
        }
    }

    suspend fun deleteCourse(c: CourseEntity) =
        withContext(Dispatchers.IO) { courseDao.delete(c) }

    fun getCategoriesFlow() = categoryDao.getCategories()

    suspend fun refreshCategories(): Result<Unit> {
        return try {
            val res = categoryApi.getCategories()
            if (res.isSuccessful) {
                val body = res.body().orEmpty()

                val mapped = body.map { dto ->
                    CategoryEntity(
                        id = dto.id.toLongOrNull() ?: 0L,
                        name = dto.name
                    )
                }

                categoryDao.clear()
                categoryDao.insertAll(mapped)

                Result.success(Unit)
            } else {
                Result.failure(HttpException(res))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
