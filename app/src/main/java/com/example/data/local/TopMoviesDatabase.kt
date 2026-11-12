package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WatchlistEntity::class], version = 1, exportSchema = false)
abstract class TopMoviesDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: TopMoviesDatabase? = null

        fun getInstance(context: Context): TopMoviesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TopMoviesDatabase::class.java,
                    "topmovies_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
