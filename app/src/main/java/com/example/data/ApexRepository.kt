package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.roundToLong

private const val TAG = "ApexRepository"

class ApexRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    // Convenience constructor: always resolves the provisioned named Firestore database ID
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Usuário precisa estar autenticado com Google antes de acessar o Firestore.")
        if (expectedUserId != null && expectedUserId != uid) {
            throw IllegalStateException("Sessão alterada; escrita cross-user bloqueada.")
        }
        return uid
    }

    // Lazily initialized Firebase AI Logic models
    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "Você é o Apex Gemini Coach, um treinador especialista em biomecânica, " +
                        "hipertrofia, treinamento baseado em velocidade (VBT), RPE/RIR, deload e " +
                        "nutrição esportiva de alta performance. Responda sempre em português do Brasil, " +
                        "com precisão técnica, tom motivador e direto, usando formatação limpa e concisa " +
                        "baseada estritamente nos dados biométricos e treinos reais do atleta."
                )
            }
        )
    }

    private val mealEstimatorSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Nome curto da refeição em português"),
            "description" to Schema.string(description = "Resumo dos alimentos principais"),
            "calories" to Schema.integer(description = "Total estimado de calorias (kcal)"),
            "proteinG" to Schema.integer(description = "Proteínas em gramas"),
            "carbsG" to Schema.integer(description = "Carboidratos em gramas"),
            "fatG" to Schema.integer(description = "Gorduras em gramas"),
            "fiberG" to Schema.integer(description = "Fibras em gramas")
        )
    )

    private val mealEstimatorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = mealEstimatorSchema
            }
        )
    }

    // --- REALTIME OBSERVATION STREAMS (USER-SCOPED SUBCOLLECTIONS) ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeRoutines(userId: String): Flow<List<WorkoutRoutine>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/routines"
        emitAll(
            db.collection("users").document(uid).collection("routines")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WorkoutRoutine::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeActiveSets(userId: String): Flow<List<ActiveSet>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/active_sets"
        emitAll(
            db.collection("users").document(uid).collection("active_sets")
                .orderBy("setOrder", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ActiveSet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeWorkoutLogs(userId: String): Flow<List<WorkoutLog>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/workout_logs"
        emitAll(
            db.collection("users").document(uid).collection("workout_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WorkoutLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMeals(userId: String): Flow<List<MealRecord>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/meals"
        emitAll(
            db.collection("users").document(uid).collection("meals")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MealRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeWeightLogs(userId: String): Flow<List<BodyMeasurementLog>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/weight_logs"
        emitAll(
            db.collection("users").document(uid).collection("weight_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(BodyMeasurementLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    private fun logStreamErrorIfActive(
        error: Throwable,
        operationType: OperationType,
        path: String,
        uid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }

    // --- PROFILE & BIOMETRICS MUTATIONS ---

    suspend fun saveInitialProfileAndBiometrics(
        userId: String,
        username: String,
        displayName: String,
        photoUrl: String,
        sex: String,
        age: Int,
        weightKg: Double,
        heightCm: Double,
        waistCm: Double,
        chestCm: Double,
        armCm: Double,
        thighCm: Double,
        bodyFatPercent: Double,
        activityFactor: Double,
        goalDeltaKcal: Int,
        goalLabel: String,
        isExistingProfile: Boolean
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanUsername = username.trim().removePrefix("@").take(30)
            require(cleanUsername.length in 3..30) { "Nome de usuário deve ter entre 3 e 30 caracteres." }
            val cleanDisplayName = displayName.trim().ifBlank { cleanUsername }.take(60)
            val clampedSex = if (sex == "F") "F" else "M"
            val clampedAge = age.coerceIn(12, 100).toLong()
            val clampedWeight = weightKg.coerceIn(30.0, 300.0)
            val clampedHeight = heightCm.coerceIn(100.0, 250.0)
            val clampedWaist = waistCm.coerceIn(0.0, 200.0)
            val clampedChest = chestCm.coerceIn(0.0, 200.0)
            val clampedArm = armCm.coerceIn(0.0, 100.0)
            val clampedThigh = thighCm.coerceIn(0.0, 150.0)
            val clampedBf = bodyFatPercent.coerceIn(0.0, 60.0)
            val clampedActivity = activityFactor.coerceIn(1.2, 1.9)
            val clampedGoalDelta = goalDeltaKcal.coerceIn(-600, 600).toLong()
            val cleanGoalLabel = goalLabel.trim().ifBlank { "Manutenção" }.take(40)
            val waterGoalMl = (clampedWeight * 35.0).roundToLong().coerceIn(1500L, 6000L)

            val userRef = db.collection("users").document(uid)
            docPath = userRef.path

            if (isExistingProfile) {
                val updatePayload = mapOf(
                    "username" to cleanUsername,
                    "displayName" to cleanDisplayName,
                    "photoUrl" to photoUrl.take(500),
                    "sex" to clampedSex,
                    "age" to clampedAge,
                    "weightKg" to clampedWeight,
                    "heightCm" to clampedHeight,
                    "waistCm" to clampedWaist,
                    "chestCm" to clampedChest,
                    "armCm" to clampedArm,
                    "thighCm" to clampedThigh,
                    "bodyFatPercent" to clampedBf,
                    "activityFactor" to clampedActivity,
                    "goalDeltaKcal" to clampedGoalDelta,
                    "goalLabel" to cleanGoalLabel,
                    "onboardingCompleted" to true,
                    "waterGoalMl" to waterGoalMl,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "username" to cleanUsername,
                    "displayName" to cleanDisplayName,
                    "photoUrl" to photoUrl.take(500),
                    "sex" to clampedSex,
                    "age" to clampedAge,
                    "weightKg" to clampedWeight,
                    "heightCm" to clampedHeight,
                    "waistCm" to clampedWaist,
                    "chestCm" to clampedChest,
                    "armCm" to clampedArm,
                    "thighCm" to clampedThigh,
                    "bodyFatPercent" to clampedBf,
                    "activityFactor" to clampedActivity,
                    "goalDeltaKcal" to clampedGoalDelta,
                    "goalLabel" to cleanGoalLabel,
                    "onboardingCompleted" to true,
                    "level" to 1L,
                    "currentXp" to 0L,
                    "nextLevelXp" to 500L,
                    "streakDays" to 0L,
                    "waterMl" to 0L,
                    "waterGoalMl" to waterGoalMl,
                    "activeWorkoutName" to "",
                    "activeWorkoutSubtitle" to "",
                    "activeExerciseName" to "",
                    "exerciseNotes" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(createPayload).await()
            }

            // Record initial / updated body measurement snapshot in weight_logs
            val weightLogRef = userRef.collection("weight_logs").document()
            docPath = weightLogRef.path
            weightLogRef.set(
                mapOf(
                    "userId" to uid,
                    "weightKg" to clampedWeight,
                    "waistCm" to clampedWaist,
                    "bodyFatPercent" to clampedBf,
                    "note" to if (isExistingProfile) "Atualização biométrica" else "Avaliação inicial da conta",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isExistingProfile) OperationType.UPDATE else OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addBodyMeasurementEntry(
        userId: String,
        weightKg: Double,
        waistCm: Double,
        bodyFatPercent: Double,
        note: String
    ): Result<Unit> {
        var docPath = "users/$userId/weight_logs"
        return try {
            val uid = requireUserId(userId)
            val clampedWeight = weightKg.coerceIn(30.0, 300.0)
            val clampedWaist = waistCm.coerceIn(0.0, 200.0)
            val clampedBf = bodyFatPercent.coerceIn(0.0, 60.0)
            val waterGoalMl = (clampedWeight * 35.0).roundToLong().coerceIn(1500L, 6000L)

            val userRef = db.collection("users").document(uid)
            val entryRef = userRef.collection("weight_logs").document()
            docPath = entryRef.path
            entryRef.set(
                mapOf(
                    "userId" to uid,
                    "weightKg" to clampedWeight,
                    "waistCm" to clampedWaist,
                    "bodyFatPercent" to clampedBf,
                    "note" to note.trim().take(140),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            docPath = userRef.path
            userRef.update(
                mapOf(
                    "weightKg" to clampedWeight,
                    "waistCm" to clampedWaist,
                    "bodyFatPercent" to clampedBf,
                    "waterGoalMl" to waterGoalMl,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- ROUTINES MUTATIONS ---

    suspend fun addRoutine(
        userId: String,
        existingCount: Int,
        name: String,
        exerciseNames: List<String>,
        targetSets: Int,
        targetReps: String,
        targetRpe: Double,
        targetRestSeconds: Int,
        category: String
    ): Result<Unit> {
        var docPath = "users/$userId/routines"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid).collection("routines").document()
            docPath = docRef.path
            val letter = ('A' + (existingCount % 26)).toString()
            val cleanTitle = name.trim().ifBlank { "Treino $letter" }.take(80)
            val cleanExercises = exerciseNames.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(20)
            val safeCategory = if (category in listOf("hypertrophy", "strength", "ppl", "upper_lower", "full_body")) {
                category
            } else {
                "hypertrophy"
            }
            val clampedSets = targetSets.coerceIn(1, 20).toLong()
            val cleanReps = targetReps.trim().ifBlank { "8-12" }.take(20)
            val clampedRpe = targetRpe.coerceIn(1.0, 10.0)
            val clampedRest = targetRestSeconds.coerceIn(15, 600).toLong()
            val subtitle = "Alvo: ${clampedSets}x$cleanReps • RPE ${String.format("%.1f", clampedRpe)} • Descanso ${clampedRest}s"

            docRef.set(
                mapOf(
                    "userId" to uid,
                    "letter" to letter,
                    "statusBadge" to "PRONTA",
                    "title" to cleanTitle,
                    "subtitle" to subtitle.take(160),
                    "exerciseNames" to cleanExercises,
                    "targetSets" to clampedSets,
                    "targetReps" to cleanReps,
                    "targetRpe" to clampedRpe,
                    "targetRestSeconds" to clampedRest,
                    "category" to safeCategory,
                    "favorite" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleRoutineFavorite(userId: String, routine: WorkoutRoutine): Result<Unit> {
        val docPath = "users/$userId/routines/${routine.id}"
        return try {
            val uid = requireUserId(userId)
            require(routine.id.isNotBlank()) { "Routine ID obrigatório." }
            db.collection("users").document(uid).collection("routines").document(routine.id)
                .update(
                    mapOf(
                        "favorite" to !routine.favorite,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(userId: String, routineId: String): Result<Unit> {
        val docPath = "users/$userId/routines/$routineId"
        return try {
            val uid = requireUserId(userId)
            require(routineId.isNotBlank()) { "Routine ID obrigatório." }
            db.collection("users").document(uid).collection("routines").document(routineId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- ACTIVE WORKOUT SESSION & SETS ---

    suspend fun startWorkoutSessionFromRoutine(
        userId: String,
        routine: WorkoutRoutine,
        existingActiveSets: List<ActiveSet>
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val firstExercise = routine.exerciseNames.firstOrNull()?.takeIf { it.isNotBlank() } ?: routine.title
            val initialReps = routine.targetReps.takeWhile { it.isDigit() }.toLongOrNull()?.coerceIn(1L, 50L) ?: 10L
            val setsCount = routine.targetSets.toInt().coerceIn(1, 8)

            // Clear old active sets
            val batch = db.batch()
            existingActiveSets.forEach { oldSet ->
                if (oldSet.id.isNotBlank()) {
                    batch.delete(userRef.collection("active_sets").document(oldSet.id))
                }
            }

            // Create fresh sets starting at 0kg so user enters their own real load
            for (i in 1..setsCount) {
                val setRef = userRef.collection("active_sets").document()
                batch.set(
                    setRef,
                    mapOf(
                        "userId" to uid,
                        "exerciseName" to firstExercise.take(80),
                        "setOrder" to i.toLong(),
                        "setLabel" to i.toString(),
                        "weightKg" to 0.0,
                        "reps" to initialReps,
                        "rpe" to routine.targetRpe.coerceIn(1.0, 10.0),
                        "completed" to false,
                        "active" to (i == 1),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.update(
                userRef,
                mapOf(
                    "activeWorkoutName" to routine.title.take(80),
                    "activeWorkoutSubtitle" to routine.subtitle.take(160),
                    "activeExerciseName" to firstExercise.take(80),
                    "exerciseNotes" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            docPath = "users/$uid/active_sets"
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun startFreeWorkoutWithExercise(
        userId: String,
        exerciseName: String
    ): Result<Unit> {
        var docPath = "users/$userId/active_sets"
        return try {
            val uid = requireUserId(userId)
            val cleanName = exerciseName.trim().ifBlank { "Exercício Livre" }.take(80)
            val userRef = db.collection("users").document(uid)
            val batch = db.batch()
            for (i in 1..3) {
                val setRef = userRef.collection("active_sets").document()
                batch.set(
                    setRef,
                    mapOf(
                        "userId" to uid,
                        "exerciseName" to cleanName,
                        "setOrder" to i.toLong(),
                        "setLabel" to i.toString(),
                        "weightKg" to 0.0,
                        "reps" to 10L,
                        "rpe" to 8.0,
                        "completed" to false,
                        "active" to (i == 1),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.update(
                userRef,
                mapOf(
                    "activeWorkoutName" to "Treino Livre",
                    "activeWorkoutSubtitle" to "Sessão iniciada agora",
                    "activeExerciseName" to cleanName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateActiveSet(
        userId: String,
        setId: String,
        weightKg: Double,
        reps: Long,
        rpe: Double,
        completed: Boolean,
        active: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/active_sets/$setId"
        return try {
            val uid = requireUserId(userId)
            require(setId.isNotBlank()) { "Set ID obrigatório." }
            db.collection("users").document(uid).collection("active_sets").document(setId)
                .update(
                    mapOf(
                        "weightKg" to weightKg.coerceIn(0.0, 1000.0),
                        "reps" to reps.coerceIn(1L, 200L),
                        "rpe" to rpe.coerceIn(1.0, 10.0),
                        "completed" to completed,
                        "active" to active,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addActiveSet(
        userId: String,
        exerciseName: String,
        nextOrder: Int,
        defaultWeightKg: Double,
        defaultReps: Long
    ): Result<Unit> {
        var docPath = "users/$userId/active_sets"
        return try {
            val uid = requireUserId(userId)
            val ref = db.collection("users").document(uid).collection("active_sets").document()
            docPath = ref.path
            ref.set(
                mapOf(
                    "userId" to uid,
                    "exerciseName" to exerciseName.trim().ifBlank { "Exercício" }.take(80),
                    "setOrder" to nextOrder.coerceIn(1, 50).toLong(),
                    "setLabel" to nextOrder.toString().take(10),
                    "weightKg" to defaultWeightKg.coerceIn(0.0, 1000.0),
                    "reps" to defaultReps.coerceIn(1L, 200L),
                    "rpe" to 8.0,
                    "completed" to false,
                    "active" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteActiveSet(userId: String, setId: String): Result<Unit> {
        val docPath = "users/$userId/active_sets/$setId"
        return try {
            val uid = requireUserId(userId)
            require(setId.isNotBlank()) { "Set ID obrigatório." }
            db.collection("users").document(uid).collection("active_sets").document(setId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateActiveExerciseAndNotes(
        userId: String,
        activeExerciseName: String,
        notes: String
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "activeExerciseName" to activeExerciseName.trim().take(80),
                        "exerciseNotes" to notes.take(500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun finishWorkoutSession(
        userId: String,
        profile: UserProfile,
        activeSets: List<ActiveSet>,
        durationMinutes: Int
    ): Result<Unit> {
        var docPath = "users/$userId/workout_logs"
        return try {
            val uid = requireUserId(userId)
            val completedSetsList = activeSets.filter { it.completed }
            val setsForStats = if (completedSetsList.isNotEmpty()) completedSetsList else activeSets
            val tonnage = setsForStats.sumOf { (it.weightKg * it.reps).roundToLong() }.coerceIn(0L, 1_000_000L)
            val bestSet = setsForStats.maxByOrNull { it.weightKg }
            val avgRpe = if (setsForStats.isNotEmpty()) setsForStats.map { it.rpe }.average() else 8.0
            val title = profile.activeWorkoutName.ifBlank {
                bestSet?.exerciseName ?: "Sessão de Treino"
            }.take(100)
            val safeDuration = durationMinutes.coerceIn(1, 600).toLong()
            val metaLine = "${safeDuration} min • RPE ${String.format("%.1f", avgRpe)} • ${setsForStats.size} séries"

            val lowerTitle = title.lowercase()
            val iconType = when {
                lowerTitle.contains("perna") || lowerTitle.contains("leg") || lowerTitle.contains("agach") -> "legs"
                lowerTitle.contains("costa") || lowerTitle.contains("pull") || lowerTitle.contains("dorsal") -> "pull"
                lowerTitle.contains("peito") || lowerTitle.contains("push") || lowerTitle.contains("supino") -> "push"
                else -> "custom"
            }

            // Compute XP & level progression cleanly
            val earnedXp = (80L + (setsForStats.size * 15L)).coerceAtMost(350L)
            var nextXp = profile.currentXp + earnedXp
            var nextLevel = profile.level.coerceAtLeast(1L)
            var nextLevelGoal = profile.nextLevelXp.coerceAtLeast(500L)
            while (nextXp >= nextLevelGoal && nextLevel < 999L) {
                nextXp -= nextLevelGoal
                nextLevel += 1L
                nextLevelGoal = (nextLevelGoal + 250L).coerceAtMost(1_000_000L)
            }

            val userRef = db.collection("users").document(uid)
            val logRef = userRef.collection("workout_logs").document()
            docPath = logRef.path
            val batch = db.batch()
            batch.set(
                logRef,
                mapOf(
                    "userId" to uid,
                    "title" to title,
                    "metaLine" to metaLine.take(120),
                    "tonnageKg" to tonnage,
                    "completedSets" to setsForStats.size.toLong(),
                    "durationMinutes" to safeDuration,
                    "bestExercise" to (bestSet?.exerciseName ?: "").take(80),
                    "bestWeightKg" to (bestSet?.weightKg ?: 0.0).coerceIn(0.0, 1000.0),
                    "bestReps" to (bestSet?.reps ?: 0L).coerceIn(0L, 200L),
                    "iconType" to iconType,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            // Clear active sets once saved
            activeSets.forEach { set ->
                if (set.id.isNotBlank()) {
                    batch.delete(userRef.collection("active_sets").document(set.id))
                }
            }
            // Update user progress
            batch.update(
                userRef,
                mapOf(
                    "level" to nextLevel,
                    "currentXp" to nextXp,
                    "nextLevelXp" to nextLevelGoal,
                    "streakDays" to (profile.streakDays + 1L).coerceAtMost(9999L),
                    "activeWorkoutName" to "",
                    "activeWorkoutSubtitle" to "",
                    "activeExerciseName" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- NUTRITION & HYDRATION MUTATIONS ---

    suspend fun toggleMeal(userId: String, meal: MealRecord): Result<Unit> {
        val docPath = "users/$userId/meals/${meal.id}"
        return try {
            val uid = requireUserId(userId)
            require(meal.id.isNotBlank()) { "Meal ID obrigatório." }
            db.collection("users").document(uid).collection("meals").document(meal.id)
                .update(
                    mapOf(
                        "completed" to !meal.completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addMeal(
        userId: String,
        timeLabel: String,
        title: String,
        description: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        fiberG: Int
    ): Result<Unit> {
        var docPath = "users/$userId/meals"
        return try {
            val uid = requireUserId(userId)
            val ref = db.collection("users").document(uid).collection("meals").document()
            docPath = ref.path
            ref.set(
                mapOf(
                    "userId" to uid,
                    "timeLabel" to timeLabel.trim().ifBlank { "12:00" }.take(10),
                    "title" to title.trim().ifBlank { "Refeição" }.take(80),
                    "description" to description.trim().take(200),
                    "calories" to calories.coerceIn(0, 10000).toLong(),
                    "proteinG" to proteinG.coerceIn(0, 1000).toLong(),
                    "carbsG" to carbsG.coerceIn(0, 1000).toLong(),
                    "fatG" to fatG.coerceIn(0, 1000).toLong(),
                    "fiberG" to fiberG.coerceIn(0, 300).toLong(),
                    "completed" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteMeal(userId: String, mealId: String): Result<Unit> {
        val docPath = "users/$userId/meals/$mealId"
        return try {
            val uid = requireUserId(userId)
            require(mealId.isNotBlank()) { "Meal ID obrigatório." }
            db.collection("users").document(uid).collection("meals").document(mealId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateWaterIntake(userId: String, newWaterMl: Long): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "waterMl" to newWaterMl.coerceIn(0L, 10000L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    // --- FIREBASE AI LOGIC (GEMINI) ---

    fun streamCoachResponse(
        userQuery: String,
        mode: String,
        athleteContextSummary: String
    ): Flow<String> = flow {
        val modeDirective = when (mode) {
            "quick" -> "Modo RÁPIDO / ACADEMIA: seja ultra-direto, máximo 3 tópicos práticos imediatos para executar entre séries."
            "nutrition" -> "Modo NUTRIÇÃO & DELOAD: foque em timing de carboidratos/proteínas, hidratação celular, recuperação do SNC e gestão de fadiga."
            else -> "Modo TÉCNICO / BIOMECÂNICO: inclua análise de torque, velocidade concêntrica (VBT), RIR/RPE e prescrição de carga/variação."
        }
        val prompt = """
            $modeDirective
            Dados reais do atleta: $athleteContextSummary
            
            Pergunta do atleta: $userQuery
        """.trimIndent()

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                coachModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("O modelo retornou uma resposta vazia.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini coach streaming timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini coach streaming failed", e)
            throw e
        }
    }

    suspend fun analyzePlateauWithGemini(athleteAndLogsSummary: String): String {
        return try {
            val response = withTimeout(60_000L) {
                coachModel.generateContent(
                    "Analise brevemente (em 2 ou 3 frases diretas em português) os dados biométricos e histórico real deste atleta e sugira a melhor estratégia de progressão de carga ou montagem de treino: $athleteAndLogsSummary"
                )
            }
            response.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Resposta vazia do modelo.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Plateau analysis timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Plateau analysis failed", e)
            throw e
        }
    }

    suspend fun estimateMealMacrosWithGemini(mealDescription: String): ScannedMealEstimate {
        return try {
            val response = withTimeout(60_000L) {
                mealEstimatorModel.generateContent(
                    "Estime as calorias e macronutrientes desta refeição fitness em português do Brasil: $mealDescription"
                )
            }
            val rawJson = response.text ?: throw IllegalStateException("Resposta vazia do scanner IA.")
            val json = JSONObject(rawJson)
            ScannedMealEstimate(
                title = json.optString("title", "Refeição Registrada"),
                description = json.optString("description", mealDescription),
                calories = json.optInt("calories", 400),
                proteinG = json.optInt("proteinG", 30),
                carbsG = json.optInt("carbsG", 40),
                fatG = json.optInt("fatG", 10),
                fiberG = json.optInt("fiberG", 5)
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Meal macro scan timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Meal macro scan failed", e)
            throw e
        }
    }
}
