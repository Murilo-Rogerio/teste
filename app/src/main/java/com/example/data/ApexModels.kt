package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.roundToInt

/**
 * Perfil biométrico e estado de progresso do atleta em /users/{userId}.
 * Todas as propriedades possuem valor padrão para desserialização segura no Firestore.
 */
@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val sex: String = "M", // "M" ou "F"
    val age: Long = 25L,
    val weightKg: Double = 75.0,
    val heightCm: Double = 175.0,
    val waistCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val armCm: Double = 0.0,
    val thighCm: Double = 0.0,
    val bodyFatPercent: Double = 0.0,
    val activityFactor: Double = 1.55,
    val goalDeltaKcal: Long = 0L,
    val goalLabel: String = "Manutenção",
    val onboardingCompleted: Boolean = false,
    val level: Long = 1L,
    val currentXp: Long = 0L,
    val nextLevelXp: Long = 500L,
    val streakDays: Long = 0L,
    val waterMl: Long = 0L,
    val waterGoalMl: Long = 2600L,
    val activeWorkoutName: String = "",
    val activeWorkoutSubtitle: String = "",
    val activeExerciseName: String = "",
    val exerciseNotes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    /** Taxa Metabólica Basal via fórmula científica Mifflin-St Jeor */
    val bmrKcal: Int
        get() {
            val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
            return if (sex == "F") (base - 161.0).roundToInt() else (base + 5.0).roundToInt()
        }

    /** Meta calórica diária (TDEE + ajuste do objetivo) */
    val targetCalories: Int
        get() = ((bmrKcal * activityFactor) + goalDeltaKcal).roundToInt().coerceAtLeast(1200)

    /** Meta de proteína (2.0g/kg a 2.2g/kg dependendo do objetivo) */
    val proteinGoalG: Int
        get() {
            val multiplier = if (goalDeltaKcal < 0L) 2.2 else 2.0
            return (weightKg * multiplier).roundToInt().coerceAtLeast(80)
        }

    /** Meta de gorduras (~0.9g/kg) */
    val fatGoalG: Int
        get() = (weightKg * 0.9).roundToInt().coerceAtLeast(45)

    /** Meta de carboidratos (restante das calorias) */
    val carbsGoalG: Int
        get() {
            val remainingKcal = targetCalories - (proteinGoalG * 4) - (fatGoalG * 9)
            return (remainingKcal / 4).coerceAtLeast(60)
        }

    /** Meta de fibras (~14g por 1000 kcal) */
    val fiberGoalG: Int
        get() = ((targetCalories / 1000.0) * 14.0).roundToInt().coerceIn(22, 45)
}

@Keep
@IgnoreExtraProperties
data class WorkoutRoutine(
    @DocumentId val id: String = "",
    val userId: String = "",
    val letter: String = "A",
    val statusBadge: String = "PRONTA",
    val title: String = "",
    val subtitle: String = "",
    val exerciseNames: List<String> = emptyList(),
    val targetSets: Long = 4L,
    val targetReps: String = "8-12",
    val targetRpe: Double = 8.0,
    val targetRestSeconds: Long = 90L,
    val category: String = "hypertrophy",
    val favorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val exerciseCount: Int get() = exerciseNames.size
    val totalSets: Int get() = (exerciseNames.size.coerceAtLeast(1) * targetSets.toInt())
    val estimatedDurationMin: Int
        get() {
            val sets = totalSets
            val restSec = targetRestSeconds.toInt()
            return (((sets * (45 + restSec)) / 60).coerceIn(20, 120))
        }
}

@Keep
@IgnoreExtraProperties
data class ActiveSet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseName: String = "",
    val setOrder: Long = 1L,
    val setLabel: String = "1",
    val weightKg: Double = 0.0,
    val reps: Long = 8L,
    val rpe: Double = 8.0,
    val completed: Boolean = false,
    val active: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WorkoutLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val metaLine: String = "",
    val tonnageKg: Long = 0L,
    val completedSets: Long = 0L,
    val durationMinutes: Long = 0L,
    val bestExercise: String = "",
    val bestWeightKg: Double = 0.0,
    val bestReps: Long = 0L,
    val iconType: String = "custom",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MealRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val timeLabel: String = "12:00",
    val title: String = "",
    val description: String = "",
    val calories: Long = 0L,
    val proteinG: Long = 0L,
    val carbsG: Long = 0L,
    val fatG: Long = 0L,
    val fiberG: Long = 0L,
    val completed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BodyMeasurementLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val weightKg: Double = 75.0,
    val waistCm: Double = 0.0,
    val bodyFatPercent: Double = 0.0,
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ScannedMealEstimate(
    val title: String,
    val description: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fiberG: Int
)
