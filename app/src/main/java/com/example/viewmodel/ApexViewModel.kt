package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApexRepository
import com.example.data.ScannedMealEstimate
import com.example.data.local.ActiveSetEntity
import com.example.data.local.ApexDatabase
import com.example.data.local.MealEntity
import com.example.data.local.UserStatsEntity
import com.example.data.local.WorkoutLogEntity
import com.example.data.local.WorkoutRoutineEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private const val TAG = "ApexViewModel"

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
    val isVbtCard: Boolean = false,
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
    val stageLabel: String = "Analisando vetores articulares...",
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

class ApexViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ApexDatabase.getInstance(application)
    private val repository = ApexRepository(database.apexDao())

    val routines: StateFlow<List<WorkoutRoutineEntity>> = repository.routines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeSets: StateFlow<List<ActiveSetEntity>> = repository.activeSets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLogEntity>> = repository.workoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val meals: StateFlow<List<MealEntity>> = repository.meals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userStats: StateFlow<UserStatsEntity> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        .let { source ->
            val fallback = MutableStateFlow(UserStatsEntity())
            viewModelScope.launch {
                source.collect { entity ->
                    if (entity != null) fallback.value = entity
                }
            }
            fallback.asStateFlow()
        }

    // Navigation & Global Chrome
    private val _selectedTab = MutableStateFlow(ApexTab.TREINOS)
    val selectedTab: StateFlow<ApexTab> = _selectedTab.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Treinos UI state
    private val _showPwaBanner = MutableStateFlow(true)
    val showPwaBanner: StateFlow<Boolean> = _showPwaBanner.asStateFlow()

    private val _installState = MutableStateFlow("idle") // idle, installing, installed
    val installState: StateFlow<String> = _installState.asStateFlow()

    private val _selectedSplitFilter = MutableStateFlow("all")
    val selectedSplitFilter: StateFlow<String> = _selectedSplitFilter.asStateFlow()

    // Active Workout Timer & Session state
    private val _elapsedSeconds = MutableStateFlow(28 * 60 + 45)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _restRemainingSeconds = MutableStateFlow(75)
    val restRemainingSeconds: StateFlow<Int> = _restRemainingSeconds.asStateFlow()

    private val _restTotalPresetSeconds = MutableStateFlow(90)
    val restTotalPresetSeconds: StateFlow<Int> = _restTotalPresetSeconds.asStateFlow()

    private val _timerSoundEnabled = MutableStateFlow(true)
    val timerSoundEnabled: StateFlow<Boolean> = _timerSoundEnabled.asStateFlow()

    private val _showPrBanner = MutableStateFlow(true)
    val showPrBanner: StateFlow<Boolean> = _showPrBanner.asStateFlow()

    private val _baseSessionVolumeKg = MutableStateFlow(4820)
    val baseSessionVolumeKg: StateFlow<Int> = _baseSessionVolumeKg.asStateFlow()

    // IA Coach state
    private val _coachSubView = MutableStateFlow(CoachSubView.CHAT)
    val coachSubView: StateFlow<CoachSubView> = _coachSubView.asStateFlow()

    private val _coachMode = MutableStateFlow(CoachMode.BIOMECHANIC)
    val coachMode: StateFlow<CoachMode> = _coachMode.asStateFlow()

    private val _isWeight80Applied = MutableStateFlow(false)
    val isWeight80Applied: StateFlow<Boolean> = _isWeight80Applied.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = 1L,
                isUser = false,
                text = "Salve, atleta! Sou o seu Apex Gemini Coach. Estou conectado aos seus sensores e histórico de repetições em tempo real.\n\nAcompanho seu volume semanal, RPE acumulado e velocidade da barra. Qual a dúvida ou ajuste biomecânico para a sessão de hoje?",
                timestamp = "Agora",
                isWelcomeCard = true
            ),
            ChatMessage(
                id = 2L,
                isUser = true,
                text = "Senti que na 3ª série de Supino a velocidade da barra caiu muito nas últimas 2 reps. Devo manter os 84kg na 4ª série?",
                timestamp = "15:42"
            ),
            ChatMessage(
                id = 3L,
                isUser = false,
                text = "Detectei uma queda de ~28% na velocidade concêntrica média entre a rep 6 e 8 na série 3. Seu RPE real saltou de 7.5 para 9.2.",
                timestamp = "15:42",
                isVbtCard = true
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _plateauScan = MutableStateFlow(PlateauScanState())
    val plateauScan: StateFlow<PlateauScanState> = _plateauScan.asStateFlow()

    // Nutrition AI Scan state
    private val _mealScanState = MutableStateFlow<MealScanUiState>(MealScanUiState.Idle)
    val mealScanState: StateFlow<MealScanUiState> = _mealScanState.asStateFlow()

    val exerciseLibrary: List<ExerciseLibraryItem> = listOf(
        ExerciseLibraryItem("Supino Reto com Barra", "Peitoral • Barra Olímpica • Composto", "peitoral"),
        ExerciseLibraryItem("Supino Inclinado com Halteres", "Peitoral Superior • Banco 30°", "peitoral"),
        ExerciseLibraryItem("Puxada Alta Pronada", "Dorsais • Polia Alta • Largura", "dorsais", accentSecondary = true),
        ExerciseLibraryItem("Agachamento Livre (Back Squat)", "Quadríceps, Glúteos • Cadeia Posterior", "quadriceps"),
        ExerciseLibraryItem("Elevação Lateral Halteres", "Deltoide Lateral • Isolamento", "ombros"),
        ExerciseLibraryItem("Rosca Direta Barra W", "Bíceps • Flexão de Cotovelo", "biceps", accentSecondary = true),
        ExerciseLibraryItem("Tríceps Corda no Pulley", "Tríceps • Extensão Polia Alta", "triceps"),
        ExerciseLibraryItem("Prancha Abdominal Isométrica", "Core • Estabilização Lombar", "core")
    )

    private var toastJob: Job? = null
    private var chatStreamJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        startLiveTimers()
    }

    private fun startLiveTimers() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _elapsedSeconds.update { it + 1 }
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
            delay(2_800L)
            _toastMessage.value = null
        }
    }

    fun dismissPwaBanner() {
        _showPwaBanner.value = false
    }

    fun triggerInstallSimulation() {
        if (_installState.value != "idle") return
        viewModelScope.launch {
            _installState.value = "installing"
            delay(850L)
            _installState.value = "installed"
            showToast("App APEX adicionado ao cache e tela inicial!")
        }
    }

    fun selectSplitFilter(filter: String) {
        _selectedSplitFilter.value = filter
        val msg = if (filter == "favs") "Filtrado por: Favoritos" else "Divisão de treino atualizada"
        showToast(msg)
    }

    fun saveNewRoutine(
        name: String,
        targetSets: Int,
        targetReps: String,
        targetRpe: Double,
        targetRestSeconds: Int
    ) {
        viewModelScope.launch {
            repository.addRoutine(name, targetSets, targetReps, targetRpe, targetRestSeconds)
            showToast("Ficha salva! Pronta para rastrear reps e cargas.")
        }
    }

    fun startWorkoutSession(routineTitle: String, routineSubtitle: String) {
        viewModelScope.launch {
            repository.updateActiveWorkoutMeta(routineTitle, routineSubtitle)
            showToast("Iniciando: $routineTitle...")
            _selectedTab.value = ApexTab.ATIVO
        }
    }

    // Active Workout Actions
    fun selectRestPreset(seconds: Int) {
        _restTotalPresetSeconds.value = seconds
        _restRemainingSeconds.value = seconds
    }

    fun add30SecondsRest() {
        _restRemainingSeconds.update { it + 30 }
        _restTotalPresetSeconds.update { it + 30 }
    }

    fun skipRestTimer() {
        _restRemainingSeconds.value = 0
    }

    fun toggleTimerSound() {
        _timerSoundEnabled.update { !it }
    }

    fun dismissPrBanner() {
        _showPrBanner.value = false
    }

    fun adjustSetWeight(set: ActiveSetEntity, delta: Int) {
        val nextWeight = (set.weightKg + delta).coerceAtLeast(2)
        viewModelScope.launch {
            repository.updateActiveSet(set.copy(weightKg = nextWeight))
        }
    }

    fun adjustSetReps(set: ActiveSetEntity, delta: Int) {
        val nextReps = (set.reps + delta).coerceAtLeast(1)
        viewModelScope.launch {
            repository.updateActiveSet(set.copy(reps = nextReps, repsDisplay = nextReps.toString()))
        }
    }

    fun cycleSetRpe(set: ActiveSetEntity) {
        val rpeSteps = listOf(6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0)
        val idx = rpeSteps.indexOfFirst { it >= set.rpe }.let { if (it < 0) 0 else (it + 1) % rpeSteps.size }
        viewModelScope.launch {
            repository.updateActiveSet(set.copy(rpe = rpeSteps[idx]))
        }
    }

    fun toggleSetCompleted(set: ActiveSetEntity) {
        val nowCompleted = !set.isCompleted
        viewModelScope.launch {
            repository.updateActiveSet(set.copy(isCompleted = nowCompleted))
            val setVolume = set.weightKg * set.reps
            if (nowCompleted) {
                _baseSessionVolumeKg.update { it + setVolume }
                _restTotalPresetSeconds.value = 90
                _restRemainingSeconds.value = 90
                showToast("Série ${set.setLabel} concluída! +${formatPtBrNumber(setVolume)} kg no volume.")
            } else {
                _baseSessionVolumeKg.update { (it - setVolume).coerceAtLeast(0) }
            }
        }
    }

    fun addActiveSetRow() {
        val current = activeSets.value
        val nextNum = current.size + 1
        val lastWeight = current.lastOrNull()?.weightKg ?: 84
        viewModelScope.launch {
            repository.addActiveSet(nextNum, lastWeight)
            showToast("Série $nextNum adicionada ao Supino Reto.")
        }
    }

    fun updateExerciseNotes(notes: String) {
        viewModelScope.launch {
            repository.updateExerciseNotes(notes)
        }
    }

    fun finishActiveWorkout() {
        val stats = userStats.value
        val totalVol = _baseSessionVolumeKg.value
        val mins = (_elapsedSeconds.value / 60).coerceAtLeast(1)
        val completedSets = activeSets.value.count { it.isCompleted }.coerceAtLeast(1)
        viewModelScope.launch {
            repository.finishWorkoutSession(
                title = stats.activeWorkoutName,
                durationMinutes = mins,
                totalVolumeKg = totalVol,
                completedSetsCount = completedSets + 18
            )
            showToast("Treino concluído! ${formatPtBrNumber(totalVol)} kg e +180 XP salvos.")
            _selectedTab.value = ApexTab.PROGRESSO
        }
    }

    // IA Coach Actions
    fun selectCoachSubView(subView: CoachSubView) {
        _coachSubView.value = subView
    }

    fun selectCoachMode(mode: CoachMode) {
        _coachMode.value = mode
    }

    fun apply80KgRecommendation() {
        if (_isWeight80Applied.value) return
        _isWeight80Applied.value = true
        viewModelScope.launch {
            val set4 = activeSets.value.firstOrNull { it.setLabel == "4" }
            if (set4 != null) {
                repository.updateActiveSet(set4.copy(weightKg = 80))
            }
            showToast("SÉRIE 4 ATUALIZADA NO LOG (80KG)")
        }
    }

    fun sendCoachMessage(rawText: String) {
        val query = rawText.trim()
        if (query.isEmpty() || _isAiGenerating.value) return

        // Switch to chat view if sent from analytics quick consultation
        _coachSubView.value = CoachSubView.CHAT

        val userMsgId = System.currentTimeMillis()
        val aiMsgId = userMsgId + 1L
        val activeSet = activeSets.value.firstOrNull { it.isActive } ?: activeSets.value.lastOrNull()
        val telemetry = "Exercício atual: Supino Reto com Barra, Série ${activeSet?.setLabel ?: "3"}, " +
            "${activeSet?.weightKg ?: 84}kg x ${activeSet?.reps ?: 8} reps, RPE ${activeSet?.rpe ?: 8.5}, " +
            "Volume da sessão: ${_baseSessionVolumeKg.value} kg."
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
                    activeTelemetrySummary = telemetry
                ).collect { accumulatedText ->
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == aiMsgId) {
                                msg.copy(text = accumulatedText, isStreaming = true)
                            } else {
                                msg
                            }
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
                updateErrorMessage(
                    aiMsgId,
                    "Tempo limite excedido (60s). Verifique sua conexão e tente novamente."
                )
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
                if (msg.id == aiMsgId) {
                    msg.copy(
                        text = errorText,
                        isStreaming = false,
                        isError = true
                    )
                } else {
                    msg
                }
            }
        }
    }

    fun runPlateauScan() {
        if (_plateauScan.value.isScanning) return
        viewModelScope.launch {
            val stages = listOf(
                "Calculando velocidade concêntrica média (VBT)...",
                "Avaliando desaceleração de Reps em Reserva (RIR)...",
                "Cruzando tonelagem com histórico de 21 dias...",
                "Detectando assimetrias na curvatura de força..."
            )
            _plateauScan.value = PlateauScanState(isScanning = true, progressPercent = 0, stageLabel = stages[0])
            for (p in 10..90 step 10) {
                delay(120L)
                val idx = ((p / 100f) * stages.size).toInt().coerceIn(0, stages.lastIndex)
                _plateauScan.update { it.copy(progressPercent = p, stageLabel = stages[idx]) }
            }
            val logsSummary = workoutLogs.value.take(3).joinToString("; ") {
                "${it.title}: ${it.tonnageKg}kg (${it.seriesLabel})"
            }
            try {
                val insight = repository.analyzePlateauWithGemini(logsSummary)
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
                    stageLabel = "Varredura local concluída",
                    completedOnce = true,
                    errorMessage = "Análise neural online indisponível: ${e.localizedMessage ?: "modo offline ativo"}"
                )
            }
        }
    }

    // Nutrition Actions
    fun toggleMealCompleted(meal: MealEntity) {
        viewModelScope.launch {
            repository.toggleMeal(meal)
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
            repository.addCustomMeal(timeLabel, title, description, calories, proteinG, carbsG, fatG, fiberG)
            showToast("Refeição \"$title\" registrada!")
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
        viewModelScope.launch {
            repository.addWater(deltaMl)
        }
    }

    // Export Telemetry (Offline JSON / CSV)
    fun buildExportPayload(format: String): String {
        val logs = workoutLogs.value
        val stats = userStats.value
        return if (format == "CSV") {
            buildString {
                appendLine("sessao,detalhes,tonelagem_kg,series")
                logs.forEach { log ->
                    appendLine("\"${log.title}\",\"${log.metaLine}\",${log.tonnageKg},\"${log.seriesLabel}\"")
                }
            }
        } else {
            buildString {
                appendLine("{")
                appendLine("  \"app\": \"Apex Performance\",")
                appendLine("  \"nivel\": ${stats.level},")
                appendLine("  \"volume_semanal_kg\": ${stats.weeklyBaseVolumeKg},")
                appendLine("  \"agua_ml\": ${stats.waterMl},")
                appendLine("  \"logs\": [")
                logs.forEachIndexed { i, log ->
                    val comma = if (i < logs.lastIndex) "," else ""
                    appendLine("    {\"titulo\": \"${log.title}\", \"tonelagem_kg\": ${log.tonnageKg}, \"series\": \"${log.seriesLabel}\"}$comma")
                }
                appendLine("  ]")
                appendLine("}")
            }
        }
    }

    companion object {
        fun formatPtBrNumber(value: Int): String {
            return NumberFormat.getNumberInstance(Locale("pt", "BR")).format(value)
        }

        fun formatTimer(totalSeconds: Int): String {
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
    }
}
