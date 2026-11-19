package com.example.data

import android.util.Log
import com.example.data.local.ActiveSetEntity
import com.example.data.local.ApexDao
import com.example.data.local.MealEntity
import com.example.data.local.UserStatsEntity
import com.example.data.local.WorkoutLogEntity
import com.example.data.local.WorkoutRoutineEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "ApexRepository"

data class ScannedMealEstimate(
    val title: String,
    val description: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fiberG: Int
)

class ApexRepository(private val dao: ApexDao) {

    // Lazily initialized Firebase AI Logic models (never eagerly evaluated at construction)
    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "Você é o Apex Gemini Coach, um treinador especialista em biomecânica, " +
                        "hipertrofia, treinamento baseado em velocidade (VBT), RPE/RIR, deload e " +
                        "nutrição esportiva de alta performance. Responda sempre em português do Brasil, " +
                        "com precisão técnica, tom motivador e direto, usando formatação limpa e concisa " +
                        "para leitura rápida na academia."
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

    val routines: Flow<List<WorkoutRoutineEntity>> = dao.observeRoutines()
    val activeSets: Flow<List<ActiveSetEntity>> = dao.observeActiveSets()
    val workoutLogs: Flow<List<WorkoutLogEntity>> = dao.observeWorkoutLogs()
    val meals: Flow<List<MealEntity>> = dao.observeMeals()
    val userStats: Flow<UserStatsEntity?> = dao.observeUserStats()

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (dao.getRoutinesCount() == 0) {
            dao.insertRoutines(
                listOf(
                    WorkoutRoutineEntity(
                        letter = "A",
                        statusBadge = "HOJE RECOMENDADO",
                        title = "Peito, Ombros & Tríceps",
                        subtitle = "Push Protocol • Foco em Supino Reto e Hipertrofia Deltoide",
                        exerciseCount = 6,
                        totalSets = 22,
                        durationMin = 55,
                        highlightLabel = "Último Supino Reto:",
                        highlightValue = "94 kg × 6 reps",
                        rightTag = "RPE 8.5",
                        category = "hypertrophy",
                        isFavorite = true,
                        isRecommended = true,
                        ctaLabel = "Iniciar Treino Agora",
                        createdAt = now
                    ),
                    WorkoutRoutineEntity(
                        letter = "B",
                        statusBadge = "CONCLUÍDO ONTEM",
                        title = "Costas, Posterior Ombro & Bíceps",
                        subtitle = "Pull Protocol • Densidade Dorsal e Puxada Alta",
                        exerciseCount = 6,
                        totalSets = 24,
                        durationMin = 60,
                        highlightLabel = "Recorde Terra:",
                        highlightValue = "140 kg × 5 reps",
                        rightTag = "Descanso Sugerido: 24h",
                        category = "strength",
                        isFavorite = false,
                        isRecommended = false,
                        ctaLabel = "Repetir",
                        createdAt = now - 1000L
                    ),
                    WorkoutRoutineEntity(
                        letter = "C",
                        statusBadge = "PROGRAMADO SÁBADO",
                        title = "Pernas Completo & Panturrilhas",
                        subtitle = "Leg Protocol • Cadeia Anterior, Posterior e Isquiotibiais",
                        exerciseCount = 7,
                        totalSets = 26,
                        durationMin = 65,
                        highlightLabel = "Alvo Agachamento:",
                        highlightValue = "120 kg",
                        rightTag = "RPE 9.0",
                        category = "strength",
                        isFavorite = false,
                        isRecommended = false,
                        ctaLabel = "Antecipar Treino",
                        createdAt = now - 2000L
                    )
                )
            )
        }

        if (dao.getActiveSetsCount() == 0) {
            dao.insertActiveSets(
                listOf(
                    ActiveSetEntity(
                        setLabel = "W1",
                        weightKg = 60,
                        reps = 12,
                        repsDisplay = "12",
                        rpe = 6.0,
                        isCompleted = true,
                        isActive = false
                    ),
                    ActiveSetEntity(
                        setLabel = "2",
                        weightKg = 80,
                        reps = 8,
                        repsDisplay = "8",
                        rpe = 8.0,
                        isCompleted = true,
                        isActive = false
                    ),
                    ActiveSetEntity(
                        setLabel = "3",
                        weightKg = 84,
                        reps = 8,
                        repsDisplay = "8",
                        rpe = 8.5,
                        isCompleted = false,
                        isActive = true
                    ),
                    ActiveSetEntity(
                        setLabel = "4",
                        weightKg = 84,
                        reps = 8,
                        repsDisplay = "6-8",
                        rpe = 9.0,
                        isCompleted = false,
                        isActive = false
                    )
                )
            )
        }

        if (dao.getWorkoutLogsCount() == 0) {
            dao.insertWorkoutLogs(
                listOf(
                    WorkoutLogEntity(
                        title = "Push A — Peito & Deltóides",
                        metaLine = "Hoje, 07:15 • 62 min • RPE 8.5",
                        tonnageKg = 11420,
                        seriesLabel = "22 Séries",
                        isRecentHighlight = true,
                        iconType = "push",
                        timestamp = now
                    ),
                    WorkoutLogEntity(
                        title = "Pull A — Dorsal & Bíceps",
                        metaLine = "Ontem, 18:30 • 58 min • RPE 8.0",
                        tonnageKg = 10850,
                        seriesLabel = "20 Séries",
                        isRecentHighlight = false,
                        iconType = "pull",
                        timestamp = now - 86_400_000L
                    ),
                    WorkoutLogEntity(
                        title = "Legs A — Quadríceps Foco",
                        metaLine = "Há 3 dias • 65 min • RPE 9.0",
                        tonnageKg = 14120,
                        seriesLabel = "24 Séries",
                        isRecentHighlight = false,
                        iconType = "legs",
                        timestamp = now - 259_200_000L
                    )
                )
            )
        }

        if (dao.getMealsCount() == 0) {
            dao.insertMeals(
                listOf(
                    MealEntity(
                        timeLabel = "07:30",
                        title = "Café Pré-Treino Anabólico",
                        description = "Ovos, Aveia, Morangos • 620 kcal",
                        calories = 620,
                        proteinG = 45,
                        carbsG = 75,
                        fatG = 15,
                        fiberG = 8,
                        isCompleted = true,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAIe3upyBLQRZjKbDlr5eBM9a_MiZEfdyyGItDqYdpYheYJ49_Px_cf15TZobkAWHbX-E-uDkfldDSShwQkomTPNjD7iCcevWjyjulohTS-9C_jvffcZTmOEefORLr0lLiGJVCDj3TtT-kJqIlLOHCIVM-3D781gDQTVuEOAVwJmDYDdR37wCsA9VWYeKGSw4C8f6P3_ri-Rnvc4AY3KOsCoPm5N30z45n0zo2bxn8"
                    ),
                    MealEntity(
                        timeLabel = "10:45",
                        title = "Pós-Treino Imediato",
                        description = "Shake Isolado + Creatina + Dextrose • 380 kcal",
                        calories = 380,
                        proteinG = 40,
                        carbsG = 50,
                        fatG = 2,
                        fiberG = 2,
                        isCompleted = true,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBJqb2LIOr8jyppWptQC2C8m_Df8-6OgganiCrvXloJv0_TUf4izVt4B-bTphKw_ymhRkyva1ZX8CQ2W92d-PoIhLANxSV_1zDs-sdC_iNaljIYWXuuHVRH_i8SrD4uxWQK72ncaBZxNsOJrQ8dK1MGQZZJ4B8NV1h-pWJaEYS7utyMHG4nxzYlgP2hepXfQ9bgKbjS2PES3nmkW037S0P71UKtqO-XPSRBTXDJr-4"
                    ),
                    MealEntity(
                        timeLabel = "13:15",
                        title = "Almoço Densidade Alta",
                        description = "Arroz, Frango Grelhado, Brócolis • 850 kcal",
                        calories = 850,
                        proteinG = 55,
                        carbsG = 95,
                        fatG = 22,
                        fiberG = 12,
                        isCompleted = true,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuASu5tX4bbUJV1etD9_V5lQFz4DtHHGG3-P-D0Rk-iR-DkPTmLWC_9w8ec5k6hqA6fTsMfbCIKI0EDvytIRggVshiNXOKXsSORUdhDoVTRhHhWlg2xcS32Ef9nmPDYc3Q5e81p0wBPysKgmdbw2bcTCcSzxn_yLJsTeK98N1RM3oiwTQa5ljxZzdwxwtLU7tJrAaDbeZOBnUV0M9EUs9YmK4eA0qKc44_IY0z9SH5I"
                    ),
                    MealEntity(
                        timeLabel = "16:45",
                        title = "Lanche Anabólico da Tarde",
                        description = "Iogurte Grego, Whey & Castanhas • 420 kcal",
                        calories = 420,
                        proteinG = 32,
                        carbsG = 35,
                        fatG = 14,
                        fiberG = 5,
                        isCompleted = true,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBrXDcMxvkvKgFX4HMbIwpiu9mkF6qg1-kPGm5oNP5NwcEPu0tLsuI5Lv6EB6AgQQC54oqCIyripBGhZMuYEvSYYCyyyiG8YHI9gOkao4b5wZifWRLH1lcFtCOtaWsniChXN_GhIJQMYb2_NI0cQPBQ77VCHPJfmdvzV26nCaMxj8LyRqz_pLh132apGjgE4gVu_G8HFutscHEACZtET-9-YHAf81VQ30LSN-fJMiY"
                    ),
                    MealEntity(
                        timeLabel = "20:30",
                        title = "Jantar Regenerativo Hipertrófico",
                        description = "Salmão / Patinho, Batata Doce • 580 kcal",
                        calories = 580,
                        proteinG = 45,
                        carbsG = 55,
                        fatG = 15,
                        fiberG = 5,
                        isCompleted = false,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCVCQrc1cYuAxElpTroS5n_ySBlw6vma1wgG1UbEI_QTtaYTZw96H_yiWo-5gwOQTwahDV41mMxbKOIx7X0C8yKXjXuCTwcGgfJtRl24wMFdxBo24DpFPw0uGPpW4YVACAaNHm0Z-K9fGEpwmqNOMas2dasdbInnbHGKoq2E6egQ1eLSSNe4CH9J3QbZShUflgTffW-7GLOMdnZuvjMKwGbrRnnkjQ4HAAXQmulZGQ"
                    )
                )
            )
        }

        if (dao.getUserStatsOnce() == null) {
            dao.upsertUserStats(UserStatsEntity())
        }
    }

    suspend fun addRoutine(
        name: String,
        targetSets: Int,
        targetReps: String,
        targetRpe: Double,
        targetRestSeconds: Int
    ) {
        val count = dao.getRoutinesCount()
        val nextLetter = ('A' + (count % 26)).toString()
        dao.insertRoutine(
            WorkoutRoutineEntity(
                letter = nextLetter,
                statusBadge = "RECÉM-CRIADA",
                title = name.ifBlank { "Nova Ficha Personalizada" },
                subtitle = "Meta: Reps $targetReps • RPE ${String.format("%.1f", targetRpe)} • Descanso ${targetRestSeconds}s",
                exerciseCount = 5,
                totalSets = targetSets * 5,
                durationMin = 50,
                highlightLabel = "Alvo Inicial:",
                highlightValue = "$targetSets séries × $targetReps reps",
                rightTag = "RPE ${String.format("%.1f", targetRpe)}",
                category = if (targetRpe >= 8.5) "strength" else "hypertrophy",
                isFavorite = false,
                isRecommended = false,
                ctaLabel = "Iniciar Treino",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateActiveSet(set: ActiveSetEntity) = dao.updateActiveSet(set)

    suspend fun addActiveSet(nextNumber: Int, defaultWeight: Int) {
        dao.insertActiveSet(
            ActiveSetEntity(
                setLabel = nextNumber.toString(),
                weightKg = defaultWeight,
                reps = 8,
                repsDisplay = "8",
                rpe = 8.5,
                isCompleted = false,
                isActive = false
            )
        )
    }

    suspend fun finishWorkoutSession(
        title: String,
        durationMinutes: Int,
        totalVolumeKg: Int,
        completedSetsCount: Int
    ) {
        dao.insertWorkoutLog(
            WorkoutLogEntity(
                title = title,
                metaLine = "Agora • $durationMinutes min • RPE 8.5",
                tonnageKg = totalVolumeKg,
                seriesLabel = "$completedSetsCount Séries",
                isRecentHighlight = true,
                iconType = "push",
                timestamp = System.currentTimeMillis()
            )
        )
        val stats = dao.getUserStatsOnce() ?: UserStatsEntity()
        dao.upsertUserStats(
            stats.copy(
                currentXp = (stats.currentXp + 180).coerceAtMost(stats.nextLevelXp),
                weeklyBaseVolumeKg = stats.weeklyBaseVolumeKg + totalVolumeKg
            )
        )
    }

    suspend fun toggleMeal(meal: MealEntity) {
        dao.updateMeal(meal.copy(isCompleted = !meal.isCompleted))
    }

    suspend fun addCustomMeal(
        timeLabel: String,
        title: String,
        description: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        fiberG: Int
    ) {
        dao.insertMeal(
            MealEntity(
                timeLabel = timeLabel,
                title = title,
                description = "$description • $calories kcal",
                calories = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                fiberG = fiberG,
                isCompleted = true,
                imageUrl = ""
            )
        )
    }

    suspend fun addWater(deltaMl: Int) {
        val stats = dao.getUserStatsOnce() ?: UserStatsEntity()
        val updated = (stats.waterMl + deltaMl).coerceIn(0, 6000)
        dao.upsertUserStats(stats.copy(waterMl = updated))
    }

    suspend fun updateExerciseNotes(notes: String) {
        val stats = dao.getUserStatsOnce() ?: UserStatsEntity()
        dao.upsertUserStats(stats.copy(exerciseNotes = notes))
    }

    suspend fun updateActiveWorkoutMeta(name: String, subtitle: String) {
        val stats = dao.getUserStatsOnce() ?: UserStatsEntity()
        dao.upsertUserStats(stats.copy(activeWorkoutName = name, activeWorkoutSubtitle = subtitle))
    }

    /**
     * Streams a real Gemini coaching response via Firebase AI Logic.
     */
    fun streamCoachResponse(
        userQuery: String,
        mode: String,
        activeTelemetrySummary: String
    ): Flow<String> = flow {
        val modeDirective = when (mode) {
            "quick" -> "Modo RÁPIDO / ACADEMIA: seja ultra-direto, máximo 3 tópicos práticos imediatos para executar entre séries."
            "nutrition" -> "Modo NUTRIÇÃO & DELOAD: foque em timing de carboidratos/proteínas, hidratação celular, recuperação do SNC e gestão de fadiga."
            else -> "Modo TÉCNICO / BIOMECÂNICO: inclua análise de torque, velocidade concêntrica (VBT), RIR/RPE e prescrição de carga/variação."
        }
        val prompt = """
            $modeDirective
            Telemetria da sessão ativa do atleta: $activeTelemetrySummary
            
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

    /**
     * Requests a fresh Gemini plateau/deload heuristic summary based on athlete tonnage.
     */
    suspend fun analyzePlateauWithGemini(tonnageSummary: String): String {
        return try {
            val response = withTimeout(60_000L) {
                coachModel.generateContent(
                    "Analise brevemente (em 2 frases diretas) este histórico de treino e indique o ajuste ideal de sobrecarga ou deload para Desenvolvimento Militar estagnado em 28 kg: $tonnageSummary"
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

    /**
     * Estimates calories and macronutrients for a described meal using Gemini Structured Output.
     */
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
                calories = json.optInt("calories", 450),
                proteinG = json.optInt("proteinG", 35),
                carbsG = json.optInt("carbsG", 45),
                fatG = json.optInt("fatG", 12),
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
