package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudySession::class, PdfAnnotation::class, PdfBookmark::class], version = 3, exportSchema = false)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
    abstract fun pdfAnnotationDao(): PdfAnnotationDao
    abstract fun pdfBookmarkDao(): PdfBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: PlannerDatabase? = null

        fun getDatabase(context: Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "planner_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
