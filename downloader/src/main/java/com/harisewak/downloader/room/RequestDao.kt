package com.harisewak.downloader.room

import androidx.room.*
import com.harisewak.downloader.Request
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {

    @Insert
    suspend fun add(request: Request)

    @Update
    suspend fun update(request: Request)

    @Query("SELECT * FROM request")
    suspend fun getAll(): Flow<List<Request>>

    @Query("SELECT * FROM request WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Request

    @Delete
    suspend fun delete(request: Request)

}