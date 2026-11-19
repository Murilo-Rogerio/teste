package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val letter: String,
    val statusBadge: String,
    val title: String,
    val subtitle: String,
    val exerciseCount: Int,
    val totalSets: Int,
    val durationMin: Int,
    val highlightLabel: String,
    val highlightValue: String,
    val rightTag: String,
    val category: String, // "hypertrophy", "strength"
    val isFavorite: Boolean = false,
    val isRecommended: Boolean = false,
    val ctaLabel: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "active_sets")
data class ActiveSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setLabel: String, // "W1", "2", "3", "4"
    val weightKg: Int,
    val reps: Int,
    val repsDisplay: String,
    val rpe: Double,
    val isCompleted: Boolean,
    val isActive: Boolean
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val metaLine: String,
    val tonnageKg: Int,
    val seriesLabel: String,
    val isRecentHighlight: Boolean = false,
    val iconType: String = "push", // "push", "pull", "legs"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timeLabel: String,
    val title: String,
    val description: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fiberG: Int,
    val isCompleted: Boolean,
    val imageUrl: String
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val level: Int = 42,
    val heroLevel: Int = 14,
    val currentXp: Int = 2450,
    val nextLevelXp: Int = 3000,
    val streakDays: Int = 5,
    val headerStreakDays: Int = 7,
    val waterMl: Int = 2500,
    val targetWaterMl: Int = 3500,
    val exerciseNotes: String = "Banco plano padrão, pegada média na marcação serrilhada.",
    val activeWorkoutName: String = "Treino A • Push Focus",
    val activeWorkoutSubtitle: String = "Peito, Deltoides & Tríceps",
    val weeklyBaseVolumeKg: Int = 48650
)
