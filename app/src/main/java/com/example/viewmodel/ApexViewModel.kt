package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveSet
import com.example.data.ApexRepository
import com.example.data.BodyMeasurementLog
import com.example.data.MealRecord
import com.example.data.ScannedMealEstimate
import com.example.data.UserProfile
import com.example.data.WorkoutLog
import com.example.data.WorkoutRoutine
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private const val TAG = "ApexViewModel"

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class ApexTab(val label: String, val headerSubtitle: String) {
    TREINOS("Treinos", "Treinos"),
    ATIVO("Ativo", "Treino Ativo"),
    IA_COACH("IA Coach", "High Performance Lab"),
    NUTRICAO("Nutrição", "High Performance Lab"),
    PROGRESSO("Progresso", "High Performance Lab")
}

enum class CoachSubView { CHAT, ANALYTICS }

enum class CoachMode(val key: String, val label: String) {
    BIOMECHANIC("biomechanic", "Técnico / Biomecânico"),
    QUICK("quick", "Rápido / Academia"),
    NUTRITION("nutrition", "Nutrição & Deload")
}

data class ChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val isWelcomeCard: Boolean = false,
    val modeTag: String? = null,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

sealed interface MealScanUiState {
    data object Idle : MealScanUiState
    data object Loading : MealScanUiState
    data class Success(val estimate: ScannedMealEstimate) : MealScanUiState
    data class Error(val message: String) : MealScanUiState
}

data class PlateauScanState(
    val isScanning: Boolean = false,
    val progressPercent: Int = 0,
    val stageLabel: String = "Pronto para análise neural...",
    val completedOnce: Boolean = false,
    val geminiInsight: String? = null,
    val errorMessage: String? = null
)

data class ExerciseLibraryItem(
    val name: String,
    val subtitle: String,
    val muscleKey: String,
    val accentSecondary: Boolean = false
)

class ApexViewModel(
    private val repository: ApexRepository,
    private val currentUserId: String
) : ViewModel() {

    private fun <T> Flow<T>.toSafeUiState(errorFallback: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, errorFallback, rootError)
                emit(UiState.Error(rootError.message ?: errorFallback))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val profileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .toSafeUiState("Erro ao carregar perfil do atleta")

    val routinesState: StateFlow<UiState<List<WorkoutRoutine>>> =
        repository.observeRoutines(currentUserId)
            .toSafeUiState("Erro ao carregar fichas de treino")

    val activeSetsState: StateFlow<UiState<List<ActiveSet>>> =
        repository.observeActiveSets(currentUserId)
            .toSafeUiState("Erro ao carregar séries ativas")

    val workoutLogsState: StateFlow<UiState<List<WorkoutLog>>> =
        repository.observeWorkoutLogs(currentUserId)
            .toSafeUiState("Erro ao carregar histórico de treinos")

    val mealsState: StateFlow<UiState<List<MealRecord>>> =
        repository.observeMeals(currentUserId)
            .toSafeUiState("Erro ao carregar refeições")

    val weightLogsState: StateFlow<UiState<List<BodyMeasurementLog>>> =
        repository.observeWeightLogs(currentUserId)
            .toSafeUiState("Erro ao carregar medidas corporais")

    // Convenience accessors for imperative actions
    private fun currentProfile(): UserProfile? =
        (profileState.value as? UiState.Success)?.data

    private fun currentRoutines(): List<WorkoutRoutine> =
        (routinesState.value as? UiState.Success)?.data.orEmpty()

    private fun currentActiveSets(): List<ActiveSet> =
        (activeSetsState.value as? UiState.Success)?.data.orEmpty()

    private fun currentWorkoutLogs(): List<WorkoutLog> =
        (workoutLogsState.value as? UiState.Success)?.data.orEmpty()

    // Navigation & Global Chrome
    private val _selectedTab = MutableStateFlow(ApexTab.TREINOS)
    val selectedTab: StateFlow<ApexTab> = _selectedTab.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    // Treinos filter
    private val _selectedSplitFilter = MutableStateFlow("all")
    val selectedSplitFilter: StateFlow<String> = _selectedSplitFilter.asStateFlow()

    // Active Workout Timer & Session state (starts at 00:00 cleanly, ticks only when a session is active)
    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _restRemainingSeconds = MutableStateFlow(0)
    val restRemainingSeconds: StateFlow<Int> = _restRemainingSeconds.asStateFlow()

    private val _restTotalPresetSeconds = MutableStateFlow(90)
    val restTotalPresetSeconds: StateFlow<Int> = _restTotalPresetSeconds.asStateFlow()

    private val _timerSoundEnabled = MutableStateFlow(true)
    val timerSoundEnabled: StateFlow<Boolean> = _timerSoundEnabled.asStateFlow()

    // IA Coach state (clean welcome message only — no fabricated bench press history)
    private val _coachSubView = MutableStateFlow(CoachSubView.CHAT)
    val coachSubView: StateFlow<CoachSubView> = _coachSubView.asStateFlow()

    private val _coachMode = MutableStateFlow(CoachMode.BIOMECHANIC)
    val coachMode: StateFlow<CoachMode> = _coachMode.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = 1L,
                isUser = false,
                text = "Salve, atleta! Sou o seu Apex Gemini Coach. Estou conectado ao seu perfil biométrico, fichas criadas e séries registradas em tempo real.\n\nPosso ajudar a montar sua primeira divisão de treinos, ajustar cargas, calcular volume semanal ou planejar sua dieta. Como posso ajudar hoje?",
                timestamp = "Agora",
                isWelcomeCard = true
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _plateauScan = MutableStateFlow(PlateauScanState())
    val plateauScan: StateFlow<PlateauScanState> = _plateauScan.asStateFlow()

    // Nutrition AI Scan state
    private val _mealScanState = MutableStateFlow<MealScanUiState>(MealScanUiState.Idle)
    val mealScanState: StateFlow<MealScanUiState> = _mealScanState.asStateFlow()

    // Static reference exercise catalog (user chooses which ones to add to their own routines)
    val exerciseLibrary: List<ExerciseLibraryItem> = listOf(
        ExerciseLibraryItem("Supino Reto com Barra", "Peitoral • Barra Olímpica • Composto", "peitoral"),
        ExerciseLibraryItem("Supino Inclinado com Halteres", "Peitoral Superior • Banco 30°", "peitoral"),
        ExerciseLibraryItem("Puxada Alta Pronada", "Dorsais • Polia Alta • Largura", "dorsais", accentSecondary = true),
        ExerciseLibraryItem("Remada Curvada com Barra", "Dorsais • Espessura • Composto", "dorsais", accentSecondary = true),
        ExerciseLibraryItem("Agachamento Livre (Back Squat)", "Quadríceps, Glúteos • Composto", "quadriceps"),
        ExerciseLibraryItem("Leg Press 45°", "Quadríceps • Máquina Guiada", "quadriceps"),
        ExerciseLibraryItem("Levantamento Terra Romeno", "Posterior de Coxa & Glúteos", "quadriceps"),
        ExerciseLibraryItem("Desenvolvimento Militar", "Ombros • Composto Vertical", "ombros"),
        ExerciseLibraryItem("Elevação Lateral Halteres", "Deltoide Lateral • Isolamento", "ombros"),
        ExerciseLibraryItem("Rosca Direta Barra W", "Bíceps • Flexão de Cotovelo", "biceps", accentSecondary = true),
        ExerciseLibraryItem("Tríceps Corda no Pulley", "Tríceps • Extensão Polia Alta", "triceps"),
        ExerciseLibraryItem("Prancha Abdominal Isométrica", "Core • Estabilização Lombar", "core")
    )

    private var toastJob: Job? = null
    private var chatStreamJob: Job? = null

    init {
        startLiveTimers()
    }

    private fun startLiveTimers() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                if (currentActiveSets().isNotEmpty()) {
                    _elapsedSeconds.update { it + 1 }
                }
                if (_restRemainingSeconds.value > 0) {
                    _restRemainingSeconds.update { (it - 1).coerceAtLeast(0) }
                }
            }
        }
    }

    fun selectTab(tab: ApexTab) {
        _selectedTab.value = tab
    }

    fun setOfflineStatus(offline: Boolean) {
        _isOffline.value = offline
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _toastMessage.value = message
        toastJob = viewModelScope.launch {
            delay(3_000L)
            _toastMessage.value = null
        }
    }

    fun selectSplitFilter(filter: String) {
        _selectedSplitFilter.value = filter
    }

    // --- PROFILE / ONBOARDING ---

    fun saveUserBiometrics(
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
        onSaved: () -> Unit = {}
    ) {
        if (_isSavingProfile.value) return
        val existing = currentProfile() != null
        viewModelScope.launch {
            _isSavingProfile.value = true
            val result = repository.saveInitialProfileAndBiometrics(
                userId = currentUserId,
                username = username,
                displayName = displayName,
                photoUrl = photoUrl,
                sex = sex,
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                waistCm = waistCm,
                chestCm = chestCm,
                armCm = armCm,
                thighCm = thighCm,
                bodyFatPercent = bodyFatPercent,
                activityFactor = activityFactor,
                goalDeltaKcal = goalDeltaKcal,
                goalLabel = goalLabel,
                isExistingProfile = existing
            )
            _isSavingProfile.value = false
            result.onSuccess {
                showToast(if (existing) "Medidas e perfil atualizados!" else "Perfil biométrico salvo! Bem-vindo ao Apex.")
                onSaved()
            }.onFailure { err ->
                showToast(err.localizedMessage ?: "Erro ao salvar perfil.")
            }
        }
    }

    fun recordBodyMeasurement(weightKg: Double, waistCm: Double, bodyFatPercent: Double, note: String) {
        viewModelScope.launch {
            repository.addBodyMeasurementEntry(currentUserId, weightKg, waistCm, bodyFatPercent, note)
                .onSuccess { showToast("Nova medição corporal registrada!") }
                .onFailure { showToast(it.localizedMessage ?: "Erro ao registrar medição.") }
        }
    }

    // --- ROUTINES ---

    fun saveNewRoutine(
        name: String,
        exerciseNames: List<String>,
        targetSets: Int,
        targetReps: String,
        targetRpe: Double,
        targetRestSeconds: Int,
        category: String
    ) {
        viewModelScope.launch {
            val count = currentRoutines().size
            repository.addRoutine(
                userId = currentUserId,
                existingCount = count,
                name = name,
                exerciseNames = exerciseNames,
                targetSets = targetSets,
                targetReps = targetReps,
                targetRpe = targetRpe,
                targetRestSeconds = targetRestSeconds,
                category = category
            ).onSuccess {
                showToast("Ficha \"$name\" salva na sua conta!")
            }.onFailure {
                showToast(it.localizedMessage ?: "Erro ao salvar ficha.")
            }
        }
    }

    fun toggleRoutineFavorite(routine: WorkoutRoutine) {
        viewModelScope.launch {
            repository.toggleRoutineFavorite(currentUserId, routine)
                .onFailure { showToast(it.localizedMessage ?: "Erro ao favoritar ficha.") }
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            repository.deleteRoutine(currentUserId, routineId)
                .onSuccess { showToast("Ficha removida.") }
                .onFailure { showToast(it.localizedMessage ?: "Erro ao remover ficha.") }
        }
    }

    fun startWorkoutFromRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            val existingSets = currentActiveSets()
            repository.startWorkoutSessionFromRoutine(currentUserId, routine, existingSets)
                .onSuccess {
                    _elapsedSeconds.value = 0
                    _restRemainingSeconds.value = 0
                    _restTotalPresetSeconds.value = routine.targetRestSeconds.toInt().coerceIn(30, 300)
                    showToast("Sessão iniciada: ${routine.title}")
                    _selectedTab.value = ApexTab.ATIVO
                }
                .onFailure {
                    showToast(it.localizedMessage ?: "Erro ao iniciar sessão.")
                }
        }
    }

    fun startFreeWorkoutWithExercise(exerciseName: String) {
        viewModelScope.launch {
            repository.startFreeWorkoutWithExercise(currentUserId, exerciseName)
                .onSuccess {
                    _elapsedSeconds.value = 0
                    showToast("Treino iniciado com $exerciseName")
                    _selectedTab.value = ApexTab.ATIVO
                }
                .onFailure {
                    showToast(it.localizedMessage ?: "Erro ao iniciar treino livre.")
                }
        }
    }

    // --- ACTIVE WORKOUT ACTIONS ---

    fun selectRestPreset(seconds: Int) {
        _restTotalPresetSeconds.value = seconds
        _restRemainingSeconds.value = seconds
    }

    fun add30SecondsRest() {
        _restRemainingSeconds.update { it + 30 }
        _restTotalPresetSeconds.update { (it + 30).coerceAtLeast(_restRemainingSeconds.value) }
    }

    fun skipRestTimer() {
        _restRemainingSeconds.value = 0
    }

    fun toggleTimerSound() {
        _timerSoundEnabled.update { !it }
    }

    fun adjustSetWeight(set: ActiveSet, delta: Double) {
        val nextWeight = (set.weightKg + delta).coerceIn(0.0, 1000.0)
        viewModelScope.launch {
            repository.updateActiveSet(
                userId = currentUserId,
                setId = set.id,
                weightKg = nextWeight,
                reps = set.reps,
                rpe = set.rpe,
                completed = set.completed,
                active = set.active
            )
        }
    }

    fun adjustSetReps(set: ActiveSet, delta: Long) {
        val nextReps = (set.reps + delta).coerceIn(1L, 200L)
        viewModelScope.launch {
            repository.updateActiveSet(
                userId = currentUserId,
                setId = set.id,
                weightKg = set.weightKg,
                reps = nextReps,
                rpe = set.rpe,
                completed = set.completed,
                active = set.active
            )
        }
    }

    fun cycleSetRpe(set: ActiveSet) {
        val rpeSteps = listOf(6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0)
        val idx = rpeSteps.indexOfFirst { it >= set.rpe }.let { if (it < 0) 0 else (it + 1) % rpeSteps.size }
        viewModelScope.launch {
            repository.updateActiveSet(
                userId = currentUserId,
                setId = set.id,
                weightKg = set.weightKg,
                reps = set.reps,
                rpe = rpeSteps[idx],
                completed = set.completed,
                active = set.active
            )
        }
    }

    fun toggleSetCompleted(set: ActiveSet) {
        val nowCompleted = !set.completed
        viewModelScope.launch {
            repository.updateActiveSet(
                userId = currentUserId,
                setId = set.id,
                weightKg = set.weightKg,
                reps = set.reps,
                rpe = set.rpe,
                completed = nowCompleted,
                active = !nowCompleted
            ).onSuccess {
                if (nowCompleted) {
                    val preset = _restTotalPresetSeconds.value.coerceAtLeast(60)
                    _restRemainingSeconds.value = preset
                    val vol = (set.weightKg * set.reps).roundToInt()
                    showToast("Série ${set.setLabel} concluída! ${formatPtBrNumber(vol)} kg registrados.")
                }
            }
        }
    }

    fun addActiveSetRow(exerciseName: String) {
        val current = currentActiveSets()
        val sameExerciseSets = current.filter { it.exerciseName == exerciseName }
        val nextOrder = (current.maxOfOrNull { it.setOrder.toInt() } ?: 0) + 1
        val lastWeight = sameExerciseSets.lastOrNull()?.weightKg ?: 0.0
        val lastReps = sameExerciseSets.lastOrNull()?.reps ?: 10L
        viewModelScope.launch {
            repository.addActiveSet(currentUserId, exerciseName, nextOrder, lastWeight, lastReps)
                .onSuccess { showToast("Nova série adicionada em $exerciseName.") }
                .onFailure { showToast(it.localizedMessage ?: "Erro ao adicionar série.") }
        }
    }

    fun deleteActiveSetRow(setId: String) {
        viewModelScope.launch {
            repository.deleteActiveSet(currentUserId, setId)
        }
    }

    fun updateActiveExerciseAndNotes(exerciseName: String, notes: String) {
        viewModelScope.launch {
            repository.updateActiveExerciseAndNotes(currentUserId, exerciseName, notes)
        }
    }

    fun finishActiveWorkout() {
        val profile = currentProfile() ?: return
        val sets = currentActiveSets()
        if (sets.isEmpty()) {
            showToast("Adicione pelo menos uma série antes de finalizar.")
            return
        }
        val mins = (_elapsedSeconds.value / 60).coerceAtLeast(1)
        viewModelScope.launch {
            repository.finishWorkoutSession(currentUserId, profile, sets, mins)
                .onSuccess {
                    _elapsedSeconds.value = 0
                    _restRemainingSeconds.value = 0
                    showToast("Treino salvo no seu histórico! XP e sequência atualizados.")
                    _selectedTab.value = ApexTab.PROGRESSO
                }
                .onFailure {
                    showToast(it.localizedMessage ?: "Erro ao finalizar treino.")
                }
        }
    }

    // --- IA COACH ACTIONS ---

    fun selectCoachSubView(subView: CoachSubView) {
        _coachSubView.value = subView
    }

    fun selectCoachMode(mode: CoachMode) {
        _coachMode.value = mode
    }

    fun sendCoachMessage(rawText: String) {
        val query = rawText.trim()
        if (query.isEmpty() || _isAiGenerating.value) return
        _coachSubView.value = CoachSubView.CHAT

        val userMsgId = System.currentTimeMillis()
        val aiMsgId = userMsgId + 1L
        val profile = currentProfile()
        val logs = currentWorkoutLogs()
        val activeSets = currentActiveSets()
        val contextSummary = buildString {
            if (profile != null) {
                append("Atleta @${profile.username} (${profile.displayName}), ")
                append("sexo ${profile.sex}, ${profile.age} anos, ${profile.weightKg}kg, ${profile.heightCm}cm, ")
                append("objetivo ${profile.goalLabel} (TDEE ${profile.targetCalories} kcal, proteína ${profile.proteinGoalG}g). ")
                if (profile.waistCm > 0) append("Cintura ${profile.waistCm}cm. ")
                if (profile.bodyFatPercent > 0) append("BF ${profile.bodyFatPercent}%. ")
            }
            if (activeSets.isNotEmpty()) {
                val activeEx = activeSets.last().exerciseName
                append("Sessão ativa: $activeEx (${activeSets.size} séries). ")
            }
            append("Treinos no histórico: ${logs.size}.")
            if (logs.isNotEmpty()) {
                append(" Último treino: ${logs.first().title} (${logs.first().tonnageKg}kg).")
            }
        }
        val mode = _coachMode.value

        _chatMessages.update { list ->
            list + ChatMessage(
                id = userMsgId,
                isUser = true,
                text = query,
                timestamp = "Agora"
            ) + ChatMessage(
                id = aiMsgId,
                isUser = false,
                text = "",
                timestamp = "Agora",
                modeTag = mode.label.uppercase(),
                isStreaming = true
            )
        }

        _isAiGenerating.value = true
        chatStreamJob?.cancel()
        chatStreamJob = viewModelScope.launch {
            try {
                repository.streamCoachResponse(
                    userQuery = query,
                    mode = mode.key,
                    athleteContextSummary = contextSummary
                ).collect { accumulatedText ->
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == aiMsgId) msg.copy(text = accumulatedText, isStreaming = true) else msg
                        }
                    }
                }
                _chatMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == aiMsgId) msg.copy(isStreaming = false) else msg
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Coach stream timed out", e)
                updateErrorMessage(aiMsgId, "Tempo limite excedido (60s). Verifique sua conexão e tente novamente.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Coach stream failed", e)
                updateErrorMessage(
                    aiMsgId,
                    "Não foi possível consultar o Apex Gemini Coach: ${e.localizedMessage ?: "verifique sua conexão."}"
                )
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    private fun updateErrorMessage(aiMsgId: Long, errorText: String) {
        _chatMessages.update { list ->
            list.map { msg ->
                if (msg.id == aiMsgId) msg.copy(text = errorText, isStreaming = false, isError = true) else msg
            }
        }
    }

    fun runPlateauScan() {
        if (_plateauScan.value.isScanning) return
        viewModelScope.launch {
            val stages = listOf(
                "Lendo medidas biométricas do atleta...",
                "Avaliando volume e RPE dos treinos salvos...",
                "Verificando tendência de progressão de carga...",
                "Gerando parecer técnico via Gemini..."
            )
            _plateauScan.value = PlateauScanState(isScanning = true, progressPercent = 10, stageLabel = stages[0])
            for (p in 20..85 step 15) {
                delay(110L)
                val idx = ((p / 100f) * stages.size).toInt().coerceIn(0, stages.lastIndex)
                _plateauScan.update { it.copy(progressPercent = p, stageLabel = stages[idx]) }
            }
            val profile = currentProfile()
            val logs = currentWorkoutLogs()
            val summary = buildString {
                if (profile != null) {
                    append("Peso: ${profile.weightKg}kg, Altura: ${profile.heightCm}cm, Objetivo: ${profile.goalLabel}. ")
                }
                if (logs.isEmpty()) {
                    append("O atleta acabou de criar a conta e ainda não possui histórico de treinos registrados.")
                } else {
                    append("Últimos treinos: ")
                    append(logs.take(5).joinToString("; ") { "${it.title} (${it.tonnageKg}kg, ${it.completedSets} séries, melhor: ${it.bestExercise} ${it.bestWeightKg}kg)" })
                }
            }
            try {
                val insight = repository.analyzePlateauWithGemini(summary)
                _plateauScan.value = PlateauScanState(
                    isScanning = false,
                    progressPercent = 100,
                    stageLabel = "Varredura Concluída",
                    completedOnce = true,
                    geminiInsight = insight
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini plateau scan failed", e)
                _plateauScan.value = PlateauScanState(
                    isScanning = false,
                    progressPercent = 100,
                    stageLabel = "Análise indisponível",
                    completedOnce = true,
                    errorMessage = "Não foi possível consultar o Gemini agora: ${e.localizedMessage ?: "verifique sua conexão"}"
                )
            }
        }
    }

    // --- NUTRITION ACTIONS ---

    fun toggleMealCompleted(meal: MealRecord) {
        viewModelScope.launch {
            repository.toggleMeal(currentUserId, meal)
                .onFailure { showToast(it.localizedMessage ?: "Erro ao atualizar refeição.") }
        }
    }

    fun addCustomMeal(
        timeLabel: String,
        title: String,
        description: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        fiberG: Int
    ) {
        viewModelScope.launch {
            repository.addMeal(currentUserId, timeLabel, title, description, calories, proteinG, carbsG, fatG, fiberG)
                .onSuccess { showToast("Refeição \"$title\" registrada!") }
                .onFailure { showToast(it.localizedMessage ?: "Erro ao salvar refeição.") }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repository.deleteMeal(currentUserId, mealId)
                .onSuccess { showToast("Refeição removida.") }
        }
    }

    fun scanMealWithGemini(description: String) {
        val clean = description.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            _mealScanState.value = MealScanUiState.Loading
            try {
                val estimate = repository.estimateMealMacrosWithGemini(clean)
                _mealScanState.value = MealScanUiState.Success(estimate)
            } catch (e: TimeoutCancellationException) {
                _mealScanState.value = MealScanUiState.Error("Tempo esgotado ao estimar macros (60s).")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Meal AI scan failed", e)
                _mealScanState.value = MealScanUiState.Error(
                    e.localizedMessage ?: "Erro ao estimar macros com Gemini."
                )
            }
        }
    }

    fun resetMealScanState() {
        _mealScanState.value = MealScanUiState.Idle
    }

    fun addWater(deltaMl: Int) {
        val currentWater = currentProfile()?.waterMl ?: 0L
        val next = (currentWater + deltaMl).coerceIn(0L, 10000L)
        viewModelScope.launch {
            repository.updateWaterIntake(currentUserId, next)
                .onFailure { showToast(it.localizedMessage ?: "Erro ao salvar hidratação.") }
        }
    }

    // --- EXPORT TELEMETRY ---

    fun buildExportPayload(format: String): String {
        val logs = currentWorkoutLogs()
        val profile = currentProfile()
        return if (format == "CSV") {
            buildString {
                appendLine("sessao,detalhes,tonelagem_kg,series,duracao_min")
                logs.forEach { log ->
                    appendLine("\"${log.title}\",\"${log.metaLine}\",${log.tonnageKg},${log.completedSets},${log.durationMinutes}")
                }
            }
        } else {
            buildString {
                appendLine("{")
                appendLine("  \"app\": \"Apex Performance\",")
                appendLine("  \"usuario\": \"@${profile?.username.orEmpty()}\",")
                appendLine("  \"peso_kg\": ${profile?.weightKg ?: 0.0},")
                appendLine("  \"altura_cm\": ${profile?.heightCm ?: 0.0},")
                appendLine("  \"nivel\": ${profile?.level ?: 1},")
                appendLine("  \"agua_ml\": ${profile?.waterMl ?: 0},")
                appendLine("  \"treinos_salvos\": [")
                logs.forEachIndexed { i, log ->
                    val comma = if (i < logs.lastIndex) "," else ""
                    appendLine("    {\"titulo\": \"${log.title}\", \"tonelagem_kg\": ${log.tonnageKg}, \"series\": ${log.completedSets}}$comma")
                }
                appendLine("  ]")
                appendLine("}")
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun formatPtBrNumber(value: Int): String {
            return NumberFormat.getNumberInstance(Locale("pt", "BR")).format(value)
        }

        fun formatPtBrLong(value: Long): String {
            return NumberFormat.getNumberInstance(Locale("pt", "BR")).format(value)
        }

        fun formatTimer(totalSeconds: Int): String {
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
    }
}
