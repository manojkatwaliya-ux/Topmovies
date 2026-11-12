package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items WHERE isHistoryOnly = 0 ORDER BY updatedAtMillis DESC")
    fun observeWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist_items WHERE lastWatchedTitle IS NOT NULL ORDER BY updatedAtMillis DESC LIMIT 20")
    fun observeRecentHistory(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE contentKey = :key AND isHistoryOnly = 0)")
    fun observeIsBookmarked(key: String): Flow<Boolean>

    @Query("SELECT * FROM watchlist_items WHERE contentKey = :key LIMIT 1")
    suspend fun getByKey(key: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchlistEntity)

    @Query("DELETE FROM watchlist_items WHERE contentKey = :key")
    suspend fun deleteByKey(key: String)
}
