package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkoutRoutineEntity::class,
        ActiveSetEntity::class,
        WorkoutLogEntity::class,
        MealEntity::class,
        UserStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ApexDatabase : RoomDatabase() {
    abstract fun apexDao(): ApexDao

    companion object {
        @Volatile
        private var INSTANCE: ApexDatabase? = null

        fun getInstance(context: Context): ApexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApexDatabase::class.java,
                    "apex_performance_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
