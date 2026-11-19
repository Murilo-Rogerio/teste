package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApexDao {
    // Workout Routines
    @Query("SELECT * FROM workout_routines ORDER BY isRecommended DESC, createdAt DESC")
    fun observeRoutines(): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT COUNT(*) FROM workout_routines")
    suspend fun getRoutinesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<WorkoutRoutineEntity>)

    // Active Workout Sets
    @Query("SELECT * FROM active_sets ORDER BY id ASC")
    fun observeActiveSets(): Flow<List<ActiveSetEntity>>

    @Query("SELECT COUNT(*) FROM active_sets")
    suspend fun getActiveSetsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveSet(set: ActiveSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveSets(sets: List<ActiveSetEntity>)

    @Update
    suspend fun updateActiveSet(set: ActiveSetEntity)

    @Query("DELETE FROM active_sets")
    suspend fun clearActiveSets()

    // Workout Logs
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun observeWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT COUNT(*) FROM workout_logs")
    suspend fun getWorkoutLogsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLogs(logs: List<WorkoutLogEntity>)

    // Meals
    @Query("SELECT * FROM meals ORDER BY id ASC")
    fun observeMeals(): Flow<List<MealEntity>>

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun getMealsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Update
    suspend fun updateMeal(meal: MealEntity)

    // User Stats
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun observeUserStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsOnce(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserStats(stats: UserStatsEntity)
}
