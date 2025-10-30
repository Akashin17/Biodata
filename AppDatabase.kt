package com.example.biodata.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.biodata.Model.Dao.BiodataDao
@Database(
    entities = [Biodataitem::class], // Hanya entity BiodataItem
    version = 1, // Mulai dari versi 1
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun biodataDao(): BiodataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biodata_database" // Nama database baru
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}