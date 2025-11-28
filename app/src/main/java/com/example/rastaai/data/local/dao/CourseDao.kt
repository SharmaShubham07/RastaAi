package com.example.rastaai.data.local.dao

import androidx.room.*
import com.example.rastaai.data.local.db.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourse(id: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun filterByCategory(categoryId: Long): Flow<List<CourseEntity>>
}
