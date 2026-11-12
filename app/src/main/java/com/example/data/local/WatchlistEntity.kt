package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_items")
data class WatchlistEntity(
    @PrimaryKey val contentKey: String,
    val videosId: String,
    val contentType: String,
    val title: String,
    val posterUrl: String?,
    val releaseYear: String?,
    val imdbRating: String?,
    val videoQuality: String?,
    val isHistoryOnly: Boolean = false,
    val lastWatchedTitle: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
